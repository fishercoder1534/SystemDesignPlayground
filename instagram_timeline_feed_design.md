# System Design Interview: Designing an Instagram Timeline Feed

A structured, end-to-end blueprint for designing a highly scalable, personalized social graph feed system like the Instagram Timeline.

---

## 1. Requirements & Scope Clarification

### Functional Requirements
* **Timeline Retrieval:** Users can view a scrollable timeline feed consisting of photos and videos from people they explicitly follow.
* **Post Creation:** Users can create posts containing media (photo/video URLs) and text captions.
* **Feed Ranking:** The feed is not purely chronological; it is sorted by relationship affinity, interest signals, and time decay weights.
* **Pagination:** Seamless scrolling support via infinite pagination without content duplication.

### Non-Functional Requirements
* **Scale:** 500 Million Daily Active Users (DAU), with an average follower count of 500 per user.
* **Low Latency:** Timeline generation and retrieval page load times must be sub-200ms (P99).
* **High Availability:** The system must favor availability over strict consistency (CAP Theorem). Eventual consistency is completely acceptable for likes, comments, and post-propagation delays.
* **The Fanout Problem:** Must handle systemic bottlenecks caused by "Celebrity / Hot Key" accounts (users with 100M+ followers) without pipeline exhaustion.

---

## 2. High-Level Architecture

The framework relies on a decoupled macro-service ecosystem that splits the write path (Publishing a Post) from the read path (Loading the Feed).

```
+-----------------------------------------------------------------------+
|                             API Gateway                               |
+-------+---------------------------------------+-----------------------+
        | (Write: Create Post)                  | (Read: Fetch Timeline)
        v                                       v
+-------+------------+                  +-------+------------+
|    Post Service    |                  |    Feed Service    |
+-------+------------+                  +-------+------------+
        |                                       |
        +---------------+                       +---------------+
        |               |                       |               |
        v               v                       v               v
+-------+---+   +-------+---+           +-------+---+   +-------+---+
|   Kafka   |   |   Post    |           | User Feed |   |  Ranking  |
| Streaming |   | Metadata  |           |   Cache   |   |  Engine   |
+---+-------+   +-----------+           +-----+-----+   +-----------+
    |                                         ^
    v (Async Worker)                          |
+---+-------+                                 |
|  Fanout   |---------------------------------+
|  Engine   | (Pushes regular user posts to follower caches)
+-----------+
```

### Main Workflow Components
1. **API Gateway:** Entry point handling authentication, rate-limiting, TLS termination, and request routing.
2. **Post Service:** Handles write operations for new media attachments, maps metadata parameters, and triggers the media delivery pipeline.
3. **Feed Service:** Processes incoming user timeline read requests, aggregates underlying raw content caches, coordinates with ranking systems, and builds the paginated payload.
4. **Fanout Engine:** Async workers consuming events from a Kafka stream to update individual user timeline arrays in real time.

---

## 3. Data Models & Storage Tiering

Because relational databases degrade under highly dimensional recursive queries at scale, storage is split based on data access paradigms.

### User/Graph Service (The Social Graph)
* **Responsibility:** Manages tracking vectors for relationships (who follows whom, block lists).
* **Storage Engine:** Relational database optimized for indexing joins (e.g., PostgreSQL with extensive caching) or a native graph engine (e.g., Neo4j).
* **Schema Design:**
  * `User Graph Table`: `follower_id` (BigInt), `followee_id` (BigInt) -> *Composite Primary Key*.

### Post Metadata Store
* **Responsibility:** Persists core post data configurations and text captions.
* **Storage Engine:** NoSQL Wide-Column Store (e.g., Cassandra / AWS DynamoDB) due to predictable index lookups by `post_id` or `user_id`.
* **Schema Design:**
  * `post_id` (UUID, Partition Key)
  * `user_id` (BigInt, Clustering Key)
  * `media_url` (String)
  * `caption` (Text)
  * `created_at` (Timestamp)

### Binary Media Assets Store
* **Responsibility:** Distributed immutable object storage for images and videos.
* **Storage Engine:** Object stores (e.g., Amazon S3) wrapped uniformly by a Geo-Distributed **Content Delivery Network (CDN)** to keep static assets physically close to end users.

---

## 4. Deep Dive: Resolving the Fanout Dilemma

The primary technical challenge of a social graph feed is finding the right strategy to propagate a post to all followers.

```
                           +----------------------+
                           |   New Post Event     |
                           +----------+-----------+
                                      |
                     Is the author a Celebrity / Influencer?
                                      |
                     +----------------+----------------+
                     | Yes                             | No
                     v                                 v
         +-----------------------+         +-----------------------+
         |      PULL MODEL       |         |      PUSH MODEL       |
         |  (Fanout on Read)     |         |  (Fanout on Write)    |
         +-----------+-----------+         +-----------+-----------+
                     |                                 |
         Do not update caches.             Lookup all follower IDs.
         Store post metadata in            Asynchronously append
         the global database only.         post_id to each follower's
                     |                     Redis Feed Cache vector.
                     |                                 |
                     +----------------+----------------+
                                      |
                                      v
                     +----------------+----------------+
                     |  User Requests Feed / Timeline  |
                     +----------------+----------------+
                                      |
                     1. Read user's pre-computed cache.
                     2. Query Graph DB for followed celebrities.
                     3. Interleave/merge both pools.
                     4. Pass combined list to Ranking Engine.
```

### 1. Push Model (Fanout on Write)
* **Execution:** When a regular user creates a post, the `Fanout Engine` fetches their follower list from the Graph Database and appends the `post_id` directly to the memory caches of all followers.
* **Pros:** Feed delivery is instantaneous ($O(1)$ lookup from memory cache).
* **Cons:** High write amplification if active user counts are large.

### 2. Pull Model (Fanout on Read)
* **Execution:** Feeds are never pre-computed. When a user requests their timeline, the system dynamically scans the social graph to find everyone they follow, retrieves their latest posts, and merges them on the fly.
* **Pros:** High storage optimization; eliminates back-end write cascades when a post is created.
* **Cons:** Highly resource-intensive and slow ($O(N)$ database query path where $N$ is the number of accounts followed).

### 3. The Hybrid Production Solution
* **Regular Users (<25k followers):** Handled entirely via the **Push Model**. Their posts are pre-cached directly into their followers' timelines.
* **Celebrities/Influencers (25k+ followers):** Handled via the **Pull Model**. Their posts bypass the fanout cache engine. Instead, when a follower opens their feed, the system dynamically pulls the celebrity's latest posts and merges them with the follower's pre-computed regular feed cache.

---

## 5. Timeline Ranking Engine

Once a user's raw candidate pool (~500 posts) is aggregated, it must be ordered by relevance.

### Feed Cache Structure
* Implemented inside an in-memory database like **Redis**.
* Uses a **Redis Sorted Set (ZSET)** per user.
  * **Key:** `user_feed:{user_id}`
  * **Value:** `{post_id}`
  * **Score:** `{ranking_score}` or `{timestamp}`

### The Ranking Scoring Heuristic
A multi-stage heuristic or ML scoring model evaluates each post based on explicit indicators:

$$	ext{Final Score} = rac{	ext{Affinity} 	imes 	ext{Weight}}{	ext{Time Decay}^\gamma}$$

* **Affinity Score ($A$):** Tracks historical interaction closeness between the viewer and creator (e.g., profile clicks, direct messages, comments, likes).
* **Content Weight Score ($W$):** Measures the format likelihood based on explicit historical patterns (e.g., if a user watches video reels 80% of the time, video weights are scaled up).
* **Time Decay ($\gamma$):** An exponential penalty multiplier tied to post age to keep the timeline fresh and prevent stale posts from lingering at the top.

---

## 6. Interview Checklist & Optimization Matrix

### Pagination Architecture
* **Do Not Use:** Offset Pagination (`LIMIT 20 OFFSET 40`). If new items are published concurrently, the pagination index shifts, causing duplicate items to surface on the client side.
* **Use Instead:** **Cursor-Based Pagination**. The server returns a deterministic token (e.g., `cursor={timestamp}_{post_id}`) representing the boundary of the last viewed post. Subsequent queries fetch data strictly older than the specified cursor.

### Cache Eviction and Maintenance
* Do not allocate expensive RAM caches for inactive users.
* If a user has not logged in for 14-30 days, their Redis ZSET is completely evicted.
* When an inactive user opens the application, an event worker catches the cache miss, dynamically executes a **Pull Pipeline** once to reconstruct the active 500-item array, and opens the push pipeline gateway again.