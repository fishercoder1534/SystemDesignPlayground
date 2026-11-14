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
A CDN is a geographically distributed network of servers that caches content close to users. Instead of every request traveling to your origin server, a CDN stores copies of your content at edge servers around the world.

Modern CDNs like Cloudflare, Fastly, and Akamai can cache much more than static files. They can also cache public API responses, HTML pages, and even run edge logic to personalize content or enforce security rules before requests reach your servers. But the most common and most impactful use of a CDN is still media delivery.

How it works:
1. A user requests an image from your app.
2. The request goes to the nearest CDN edge server.
3. If the image is cached there, it is returned immediately.
4. If not, the CDN fetches it from your origin server, stores it, and returns it.
5. Future users in that region get the image instantly from the CDN.


Without a CDN, every image request travels to your origin. If your server is in Virginia and the user is in India, that adds 250–300 ms of latency per request. With a CDN, the same image is served from a nearby edge server in 20–40 ms. That is a massive performance difference.

Even though modern CDNs can cache API responses and dynamic content, in system design interviews the safest time to introduce a CDN is when your system serves static media at scale. Start with that reason first, then expand only if the problem calls for more.

### Client-Side Caching
Client-side caching stores data close to the requester to avoid unnecessary network calls. This usually means the user's device, like a browser (HTTP cache, localStorage) or mobile app using local memory or on-device storage.

But it can also mean caching within a client library. For example, Redis clients cache cluster metadata like which nodes are in the cluster and which slots are assigned to them. That way, the client can route requests directly to the right node without querying the cluster on every operation.

For user-facing caching, you have limited control from the backend. Data can go stale and invalidation is harder. The Strava app keeps your run data on the device while you are offline and syncs it later. A browser reusing a previously downloaded image from disk is also caching.

### In-Process Caching
Most candidates, and engineers, overlook the fact that servers run on machines with a lot of memory. Each hardware generation ships more of it. You can use that memory to cache data directly inside the application process instead of always calling out to Redis or the database.

The idea is simple: if your service keeps requesting the same small pieces of data again and again, store them in a local cache inside the process. Reads from local memory are even faster than reads from Redis because they avoid any network call.

This light-weight form of caching makes sense for small pieces of data that are requested frequently like:
1. Configuration values
2. Feature flags
3. Small reference datasets
4. Hot keys
5. Rate limiting counters
6. Precomputed values

In-process caching is blazing fast, but it comes with obvious limitations. Each instance of your application has its own cache, so cached data is not shared across servers. If one instance updates or invalidates a cached value, the others will not know.

Use in-process caching for **small**, **frequently accessed** values that **rarely change**. It is great for speed but not a replacement for Redis. In system design interviews, mention this only as an optimization layer after you have already introduced an external cache.

## Cache Architectures
Not all caching works the same way. How you read from and write to the cache changes performance, consistency, and complexity. These are the four core cache patterns you should know for system design interviews.

### Cache-Aside (Lazy Loading)
This is the most common caching pattern and the one you should default to in interviews.
How it works:
1. Application checks the cache.
2. If the data is there, return it.
3. If not, fetch from the database, store it in the cache, and return it.

Cache-aside only caches data when needed, which keeps the cache lean. The downside is that a cache miss causes extra latency.

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