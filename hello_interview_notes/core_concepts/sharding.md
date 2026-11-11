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