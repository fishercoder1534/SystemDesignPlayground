# Ad hoc notes for Numbers to Know

## High level gists
1. You don't need to do back-of-the-envelope calculations at the start of an interview. 
   1. That's not what interviewers care about. 
   2. What matters is doing them when you need to make a decision. 
   3. Should you shard the database? Can a single Redis instance handle the cache load? 
   4. You can't answer these questions without rough numbers.
2. The trick is knowing which numbers to use. 
   1. Modern hardware is way more powerful than most candidates realize. 
   2. A well-tuned database server handles tens of thousands of queries per second. 
   3. A single Redis instance handles hundreds of thousands of operations per second. 
   4. If you're using 2010-era numbers in your head, you'll propose sharding and caching way earlier than you need to.
3. Start with the latency numbers because they affect almost every design decision. 
   1. Memory access takes **nanoseconds**. 
   2. SSD reads take **microseconds**. 
   3. Network calls within a data center take 1-10 **milliseconds**. 
   4. Cross-continent calls take tens to hundreds of **milliseconds**. 
   5. When you're deciding whether to cache something or whether geographic distribution is worth the complexity, these gaps are what matter.
4. Do your capacity calculations in context when you need them. 
   1. If your interviewer asks "how many servers do we need," that's when you pull out the numbers. Walk through it. 
   2. "We're expecting 50K requests per second, each server can handle maybe 5K requests, so we need around 10 servers plus some headroom." 
   3. The interviewer wants to see you think through the math, not recite memorized facts.
5. Storage capacity matters for sharding decisions. 
   1. A single Postgres instance handles a few terabytes comfortably. 
   2. You don't need sharding until you're hitting tens or hundreds of terabytes. 
   3. If someone proposes sharding at 500GB, they're adding massive complexity for no reason.



| Component | Key Metrics | Scale Triggers |
| :--- | :--- | :--- |
| **Caching** | - ~1 millisecond latency<br>- 100k+ operations/second<br>- Memory-bound (up to 1TB) | - Hit rate < 80%<br>- Latency > 1ms<br>- Memory usage > 80%<br>- Cache churn/thrashing |
| **Databases** | - Up to 50k transactions/second<br>- Sub-5ms read latency (cached)<br>- 64 TiB+ storage capacity | - Write throughput > 10k TPS<br>- Read latency > 5ms uncached<br>- Geographic distribution needs |
| **App Servers** | - 100k+ concurrent connections<br>- 8-64 cores @ 2-4 GHz<br>- 64-512GB RAM standard, up to 2TB | - CPU > 70% utilization<br>- Response latency > SLA<br>- Connections near 100k/instance<br>- Memory > 80% |
| **Message Queues** | - Up to 1 million msgs/sec per broker<br>- Sub-5ms end-to-end latency<br>- Up to 50TB storage | - Throughput near 800k msgs/sec<br>- Partition count ~200k per cluster<br>- Growing consumer lag |


-----
Credit:
1. https://www.hellointerview.com/learn/system-design/in-a-hurry/core-concepts#quiz-core-concepts