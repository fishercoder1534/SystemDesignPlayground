# Ad hoc notes for Caching

## High level gists
1. The performance difference is massive. A cache hit on Redis takes around 1ms compared to 20-50ms for a typical database query. 
   1. When you're serving millions of requests, that 20-50x speedup matters. 
   2. You also reduce load on your database, letting it handle more write traffic and avoiding the need to scale it prematurely.
2. The pattern you'll use 90% of the time is cache-aside with Redis. 
   1. On a read, check the cache first. If the data is there, return it. 
   2. If not, query the database, store the result in the cache with a TTL, and return it. 
   3. This is straightforward to implement and works for most read-heavy systems.
3. But caching introduces real complexity. The hardest part is **invalidation**. 
   1. When a user updates their profile in the database, you need to delete or update the cached copy. Otherwise the next read returns stale data. 
   2. There are a few strategies here. You can invalidate the cache entry immediately after writes, use short TTLs and accept some staleness, or combine both. 
   3. The right choice depends on how fresh your data needs to be.
4. You also need to think about cache stampedes. 
   1. When a popular cache entry expires, many concurrent requests all miss at the same time and pile onto the database to regenerate it. 
   2. This thundering herd can spike your database load and take down your whole system. 
   3. You can prevent this with:
      1. locking (only one request regenerates the entry while the rest wait), 
      2. early recomputation (refresh entries before they expire), 
      3. or staggering TTLs so entries don't all expire at once. 
      4. A related but distinct problem is a full cache outage. If Redis goes down entirely, every request hits the database. 
         1. Defenses here include a small in-process fallback cache, circuit breakers to shed load, or graceful degradation until Redis recovers.
5. A common mistake is caching everything. 
   1. Cache only data that's read frequently and doesn't change often. 
   2. If you're caching data that changes on every request, you're just adding latency and complexity for no benefit. 
   3. Profile your system first, then cache the hot paths.
6. CDN caching is different. It's for static assets like images, videos, and JavaScript files served from edge locations close to users. 
   1. In-process caching works for small values that change rarely, like feature flags or config data. 
   2. But for your core application data, external caching with Redis is the default.


-----------
In system design interviews, caching comes up almost every time you need to handle **high read traffic**. Your database becomes the bottleneck, latency starts creeping up, and the interviewer is waiting for you to say the word: cache.

Reading a user profile from **Postgres** may take **50 ms**, but reading from an in-memory cache like Redis takes just **1 ms**. That's a 50x improvement in latency. Databases store data on disk, and every query pays the cost of disk access. Memory sits much closer to the CPU and avoids that entirely.

Caches are essential for scalable systems. They reduce load on the database and cut latency dramatically. But they also create new challenges around **invalidation** and **failure handling**.

This breakdown covers the basics of caching, when and where to use it, common pitfalls, and how to talk about caching clearly in interviews.

## Where to Cache
Caching shows up in multiple layers of a system:
1. Browsers cache. 
2. CDNs cache. 
3. Applications cache. 
4. Even databases have built-in caching layers.

### External Caching
An external cache is a standalone cache service that your application talks to over the network. This is what most people think of when they hear caching. You store frequently accessed data in something like Redis or Memcached so you do not have to hit the database every time.

External caches scale well because every application server can share the same cache. They also support eviction policies like LRU and expiration via TTL so your memory footprint stays controlled.

In system design interviews, **external caching with Redis** is the **default** answer when discussing caching strategies. Interviewers expect you to mention it for any high-traffic system. Start here, then layer on other caching types such as CDN or client-side caching only if the problem calls for them.

### CDN (Content Delivery Network)
### Client-Side Caching
### In-Process Caching

## Cache Architectures
### Cache-Aside (Lazy Loading)
### Write-Through Caching
### Write-Behind (Write-Back) Caching
### Read-Through Caching

## Cache Eviction Policies
### LRU (Least Recently Used)
### LFU (Least Frequently Used)
### FIFO (First In First Out)
### TTL (Time To Live)

## Common Caching Problems
### Cache Stampede (Thundering Herd)
### Cache Consistency
### Hot Keys

## Caching in System Design Interviews
### When to Bring Up Caching
### How to Introduce Caching

## Conclusion




----------
Credit:
1. https://www.hellointerview.com/learn/system-design/core-concepts/caching