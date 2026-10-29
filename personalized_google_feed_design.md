# System Design Interview Cheat Sheet: Personalized Google Feed

## 1. Scale & Quantitative Estimates

| Metric | Target Value | Calculation / Engineering Constraint |
| :--- | :--- | :--- |
| **Daily Active Users (DAU)** | 1 Billion | Core target global audience footprint. |
| **Read QPS** | ~57,870 | Assuming average user views feed 5 times/day: (5B requests / 86400s) $\times$ peak traffic multiplier (~2.0). |
| **Write QPS (Ingestion)** | ~50 - 100 articles/sec | Global syndicated articles, blogs, video metadata updates crawled concurrently. |
| **P99 Read Latency** | < 200 ms | Crucial non-functional requirement for user retention. |
| **Daily Video/Article Metadata Storage**| ~500 GB / day | 5 Million new items parsed daily $\times$ 100 KB rich metadata per item (text, thumbnails, tags). |
| **User Feature Store Size** | ~10 TB | 1 Billion active user vectors $\times$ 10 KB per profile feature snapshot. |

---

## 2. End-to-End System Architecture

```text
[ Client (App/Web) ]
        │ (GET /v1/feed?cursor=...)
        ▼
[ API Gateway / Load Balancer ]
        │
        ├─────────────────────────────────────────────────┐
        ▼ (Sync Read Path: Latency Critical)             ▼ (Async Event Logging)
[ Feed Generation Service ]                         [ Kafka Distributed Event Bus ]
        │                                                 │
        ├─────────────────────────┐                       ▼
        ▼                         ▼                 [ Stream Processor (Flink) ]
[ Redis Pre-computed Cache ]  [ Online Scoring Engine ]   │
   (Sorted Sets by Score)      (Heavy DL / GBDT Model)     ▼
                                  ▲                 [ Feature Store (DynamoDB) ]
                                  │                       │
                                  └───────────────────────┤
                                                          ▼
                                                    [ Data Lake (HDFS/S3) ]
                                                          ▲
                                                          │ (Offline Model Training)
                                                    [ ML Training Cluster ]
```

---

## 3. The 3-Stage Feed Ranking Pipeline

### Stage 1: Candidate Generation (Retrieval)
* **Objective:** Reduce candidate space from **Millions of items to ~1,000 items**.
* **Collaborative Filtering:** Fetches items liked by users with overlapping implicit feedback matrix profiles.
* **Vector Embeddings:** Conducts Approximate Nearest Neighbor (ANN) search inside a vector database (e.g., Milvus, Faiss) matching 512-dimension User Profiles against Article Content vectors.
* **Hard Filters:** Instantly drops NSFW content, blocked domains, or items matching the user's explicit dismissal history.

### Stage 2: Heavy Ranking (Scoring)
* **Objective:** Reduce candidate space from **~1,000 items to top 50 highly targeted items**.
* **Model Choice:** Deep Learning Recommendation Model (DLRM) or Two-Tower Neural Network combined with a Gradient Boosted Decision Tree (GBDT) layer.
* **Feature Integration:**
  * *User Features:* Age, device, historical Click-Through Rate (CTR) over discrete category vectors.
  * *Context Features:* Local time of day, current geohash location context.
  * *Item Features:* Publisher authority rank, interaction decay velocity, absolute freshness delta.
* **Optimization Target:** Multi-task prediction objective scoring $P(	ext{Click}) 	imes P(	ext{Read-Through}) 	imes 	ext{Weight}_{	ext{Engagement}}$.

### Stage 3: Diversification & Guardrails (Re-ranking)
* **Objective:** Polish the final top 20 items presented directly to the client screen.
* **Deduplication:** Utilizes MinHash / SimHash to group clusters of near-identical syndication copies under a single link card.
* **Domain Margins:** Restricts consecutive items from matching identical domains (e.g., maximum 2 consecutive items from the same domain to prevent echo chambers).
* **Exploration Injection:** Reserves 5-10% of slots for multi-armed bandit (MAB) testing of new, fresh articles lacking statistical interaction features.

---

## 4. Key Bottlenecks & Strategic Mitigations

* **Handling Cold Start (New Users & Items):**
  * *New Users:* Fall back to localized regional trending indexes combined with rapid multi-choice topic onboarding.
  * *New Items:* Seed exploratory distributions to a random 0.1% sub-segment of low-activity users to dynamically establish base engagement rates.
* **Decoupling ML Inference from Read Paths:**
  * Do not run real-time multi-layer neural network evaluations synchronously during user `GET` network round-trips.
  * Execute asynchronous background workers that periodically populate Redis sorted cache groups (`ZSET`) for the most active 20% of users.
* **Cache Invalidation:**
  * When a massive breaking news alert hits, trigger a fan-out invalidation message over the edge network cluster to flush stale pre-computed feed caches.
