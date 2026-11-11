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