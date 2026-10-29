# System Design: Designing a Scalable Rate Limiter

## 1. Requirements Clarification & Scope

### Functional Requirements
* **Throttling Criterion:** Ability to limit requests based on identifier types (e.g., **IP address** for unauthenticated traffic, **User ID** or **API Key** for authenticated sessions).
* **System Behavior:** Throttled requests must immediately return an **HTTP 429 Too Many Requests** status code.
* **Configurability:** Support for dynamic, tier-based boundaries (e.g., Free vs. Premium tiers) adjustable at runtime.

### Non-Functional Requirements
* **Low Latency:** Must operate as high-performance middleware adding minimal overhead (**< 2–5ms**) to the total API lifecycle.
* **High Availability & Fault Tolerance:** The system must handle millions of concurrent requests globally. It must incorporate graceful degradation strategies (e.g., fail-open fallback architectures).
* **Distributed Accuracy:** Ensure precise transaction counting across widely distributed infrastructure while mitigating concurrency anomalies.

---

## 2. Core Core Algorithms & Structural Trade-offs

| Algorithm | Pros | Cons | Best Used For |
| :--- | :--- | :--- | :--- |
| **Token Bucket** | Memory efficient; handles sudden **bursts of traffic** smoothly. | Complex to track state globally in distributed systems. | Standard APIs (e.g., Stripe, GitHub). |
| **Leaky Bucket** | Smooths out traffic into a **continuous, stable flow**. | Can delay legitimate burst requests, causing consumer latency. | Traffic shaping / E-commerce checkout queues. |
| **Fixed Window** | Extremely simple to implement; minimal memory usage. | **Traffic spikes at window boundaries** can allow double the limit. | Loose limits over large time windows. |
| **Sliding Window Log** | **100% accurate**; completely prevents boundary spikes. | High memory usage (stores every request timestamp in memory). | Low-volume, ultra-strict security constraints. |
| **Sliding Window Counter** | High accuracy; highly memory-efficient blend of Fixed Window and Log. | Slight approximation error (~0.05%), but rarely a blocker. | **Industry standard for distributed systems**. |

---

## 3. Multi-Tier Defense Architecture
A Staff-level strategy separates the rate limiter into a multi-tiered architecture to maximize performance and save compute costs across layers.

```text
[ Global Edge / CDN ]      --> Layer 1: Volumetric DDoS / Malicious Actors (Edge-cached IP blocks)
          |
[ API Gateway / Envoy ]    --> Layer 2: Tiered Access & Authentication (App ID / API Token tracking)
          |
[ App / Service Mesh ]     --> Layer 3: Downstream Resiliency & Pod Protection (Concurrency/Circuit Breaking)
```

1. **Layer 1: The Edge/CDN Layer**
   Handles unauthenticated, volumetric traffic using broad attributes such as geographic IP subnets or autonomous system number (ASN) fingerprinting. If an IP exceeds threshold limits (e.g., >10,000 req/sec), traffic is discarded at the edge before hitting core cloud infrastructure.
2. **Layer 2: The API Gateway Layer**
   Manages authenticated multi-tenancy. This tier applies granular business domain logic, mapping authenticated API tokens to specific user service tiers (e.g., Free vs. Premium).
3. **Layer 3: The Application Service Mesh Layer**
   Implements strict concurrency limiting via infrastructure proxies (e.g., Envoy sidecars) to protect underlying resource-heavy computing environments (e.g., PDF compilation engines) from thread pool exhaustion.

---

## 4. Deep Dive & Distributed Scale (Staff-Level Focal Points)

### Global Data Synchronization vs. Edge Performance
Deploying an active-active setup across global environments (`us-east`, `eu-west`, `ap-southeast`) makes global synchronization expensive. To solve this, employ **Local Precision with Asynchronous Top-ups** to favor eventual consistency over strong global locking:
* Allocate local quota slices to each distinct region dynamically based on historical traffic patterns (e.g., 50% US, 30% EU, 20% AP).
* Local edge components decrement nearby regional memory caches (e.g., localized Redis instances) with sub-2ms write times.
* A background task asynchronously aggregates real-time regional usages, recalculating and re-balancing local allocations at fixed intervals (e.g., every 1 to 5 seconds).

### Concurrency and Memory Optimization
* **Race Conditions:** Simultaneous requests arriving at independent reverse proxies can read stale cache state before writing an increment. Use **Redis Lua Scripts** to group lookup-and-increment operations into atomic transactions executed locally within a single-threaded cache pipeline.
* **Noisy Neighbors:** Isolate processing keys within large distributed caches using **Redis Clustering Hash Tags** (e.g., `{tenant_123}:quota`). This ensures all transaction data for a single consumer maps to the identical shard partition, optimizing execution contexts.
* **O(1) Sliding Windows:** To prevent memory exhaustion from storing individual timestamps, use a mathematical approximation tracking only the current and preceding window state:

$$	ext{Approximate Count} = 	ext{Count}_{	ext{prev}} 	imes \left(1 - rac{	ext{Time}_{	ext{elapsed}}}{	ext{Window}_{	ext{size}}}ight) + 	ext{Count}_{	ext{current}}$$

---

## 5. System Resiliency & Backpressure

### Fail-Open vs. Fail-Closed
For customer-facing API pathways, the rate limiter infrastructure should be configured to **fail-open**. If the backing cache tier experiences severe service interruption or network partitioning, the gateway layer must emit operational metrics, bypass checking logic, and allow traffic through to prevent complete system downtime. Downstream protections (e.g., cluster circuit breakers) act as the final defensive boundary.

### Client Backpressure Communication
Forced scaling limits require proper programmatic backpressure feedback. Every HTTP 429 response must deliver structural metadata back to the calling process:

* `X-RateLimit-Limit`: Total structural limit allocated to the caller within the active window.
* `X-RateLimit-Remaining`: Remaining request allowance available before truncation.
* `X-RateLimit-Reset`: Unix epoch timestamp defining when the active limit refreshes.
* `Retry-After`: Minimum delta time in seconds the client must wait before retrying.