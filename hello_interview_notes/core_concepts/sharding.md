# Ad hoc notes for Sharding

## High level gists
1. Sharding comes up when you've outgrown a single database and need to split your data across multiple independent servers. 
   1. This happens when you hit storage limits (a single Postgres instance maxes out in the TB), 
   2. write throughput limits (tens of thousands of writes per second), 
   3. or read throughput that even replicas can't handle.
2. The most important decision is your shard key. 
   1. This determines how data gets distributed and affects everything else in your design. 
   2. For a user-centric app like Instagram, sharding by user_id means all of a user's posts, likes, and comments live on one shard. User-scoped queries are fast because they only hit one shard. 
   3. But now global queries like "trending posts across all users" become expensive because you have to hit every shard and aggregate results. 
   4. That's the tradeoff.
3. Most systems use **hash-based sharding** where you hash the shard key and use *modulo* to pick a shard. 
   1. This distributes data **evenly** and avoids hot spots. 
   2. Range-based sharding can work if your access patterns naturally partition (like multi-tenant SaaS where each company only queries their own data), but it's easy to create hot spots if one range gets more traffic. 
   3. Directory-based sharding uses a lookup table to decide where data lives. It's flexible but adds a dependency and latency to every request, so it's rarely worth it in interviews.
4. The biggest mistake with sharding is doing it **too early**. 
   1. A well-tuned single database with read replicas can handle way more than most candidates think. 
   2. Before you propose sharding, do the capacity math. 
   3. If you're at 10K writes per second and 100GB of data, you don't need sharding yet. 
   4. Bring it up when the numbers justify it, not as a default scaling strategy.
5. Sharding creates new problems you need to address. 
   1. Cross-shard transactions become nearly impossible, so you need to design your shard boundaries to avoid them. 
   2. If a user transfer in your banking app requires updating accounts on different shards, you'll need distributed transactions or sagas, which are complex and slow. 
   3. Hot spots happen when one shard gets disproportionate traffic (think Taylor Swift's shard getting hammered while others sit idle). 
   4. And resharding is painful. You can't just add a new shard without moving massive amounts of data around. 
   5. In interviews, bring up sharding **after** you've justified why a single database won't work. 
   6. Then clearly state your **shard key choice** and explain the tradeoff (fast for X queries, slow for Y queries). That's all most interviewers need to see.



## My notes
1. When it comes to **sharding**, it generally refers to **NoSQL** DBs.
   1. It's relatively rare to shard PostgreSQL or MySQL DBs across the broader industry, whether hosted in the cloud or in-house. 
   2. Most organizations never reach the traffic or storage limits that justify the massive engineering cost of sharding a relational database.
2. Why Most Teams Avoid Sharding SQL
   1. Modern Hardware Goes Very Far: 
      1. Today’s cloud instances and on-premise servers can easily handle tens of terabytes of storage and tens of thousands of queries per second on a single primary node. 
      2. Upgrading a server vertically is vastly **cheaper** than paying engineers to build and maintain a sharded architecture.
   2. Simpler Steps Come First: Before sharding, teams almost always exhaust easier scaling tactics. 
      1. These include adding read replicas, 
      2. caching hot data in Redis, 
      3. optimizing slow SQL queries, 
      4. and partitioning large tables within a single database.
   3. Severe Operational Overhead: 
      1. Sharding traditional SQL breaks standard relational features like cross-shard JOIN queries, 
      2. foreign keys, 
      3. and simple ACID transactions. 
      4. Managing schema migrations and rebalancing data across shards requires specialized tooling and constant engineering effort.
3. Where SQL Sharding Actually Happens 
   1. While uncommon overall, sharding SQL-like databases is standard practice in specific, **extreme**-scale scenarios:
      1. Hyper-Scale Tech Companies: Companies like Uber, Slack, Shopify, and Instagram run **heavily sharded MySQL** or **PostgreSQL** fleets because their **write** volume far exceeds what any single machine on earth can handle.
      2. Multi-Tenant B2B SaaS: Some enterprise software companies isolate large customers into separate database instances (tenant-based sharding) for strict security, compliance, and noisy-neighbor isolation rather than pure performance.
      3. Distributed SQL Adoption: Rather than manually sharding legacy Postgres or MySQL, modern teams with massive write workloads increasingly adopt Distributed SQL databases (such as CockroachDB, TiDB, or YugabyteDB) or middleware like **Vitess**. 
         1. These systems mimic standard SQL while automating sharding under the hood.



-------------
Credit:
1. https://www.hellointerview.com/learn/system-design/in-a-hurry/core-concepts#sharding
2. Gemini