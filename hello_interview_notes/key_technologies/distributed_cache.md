# Ad hoc notes for Distributed Cache

## High level gists
1. In most system design interviews you'll be tasked with both scaling your system and lowering system latency. 
   1. One common way to do this is to use a distributed cache. 
   2. A cache is just a server, or cluster of servers, that stores data in memory. 
   3. They're great for storing data that's expensive to compute or retrieve from a database.
2. You'll want to use a cache to:
   1. **Save Aggregated Metrics**: 
      1. Consider an analytics platform that aggregates data from numerous sources to display a dashboard of metrics. 
      2. The data for these metrics is expensive to compute, so the platform calculates metrics asynchronously (like hourly via a background job) and stores the results in a distributed cache. 
      3. When a user requests a dashboard, the platform can retrieve the data from the cache instead of recomputing it, reducing latency.
   2. **Reduce Number of DB Queries**: 
      1. In a web application, user sessions are often stored in a distributed cache to reduce the load on the database. 
      2. This is especially important for systems that need to support a large number of concurrent users. 
      3. When a user logs in, the system can store their session data in the cache, allowing the system to quickly retrieve the data when the user makes a request.
   3. **Speed Up Expensive Queries**: 
      1. Some complex queries take a long time to run on a traditional, disk based database. 
      2. For example, if you have a social media platform like Twitter, you might want to show users a list of posts from people they follow. 
      3. This is a complex query that requires joining multiple tables and filtering by multiple columns. 
      4. Running this query on Postgres could take ages. 
      5. Instead, you can run the query once, store the results in a distributed cache, and then retrieve the results from the cache when a user requests them.


## Things you should know about distributed caches for your interview
1. **Eviction Policy**: Distributed caches have different eviction policies that determine which items are removed from the cache when the cache is full. Some common eviction policies are:
   1. Least Recently Used (LRU): Evicts the least recently accessed items first. 
   2. First In, First Out (FIFO): Evicts items in the order they were added. 
   3. Least Frequently Used (LFU): Removes items that are least frequently accessed.
2. **Cache Invalidation Strategy**: 
   1. This is the strategy you'll use to ensure that the data in your cache is up to date. 
   2. For example, if you are designing Ticketmaster and caching popular events, then you'll need to invalidate an event in the cache if the event in your Database was updated (like the venue changed).
3. **Cache Write Strategy**: This is the strategy you use to make sure that data is written to your cache in a consistent way. Some strategies are:
   1. **Write-Through Cache**: Writes data to both the cache and the underlying datastore simultaneously. Ensures consistency but can be slower for write operations.
   2. **Write-Around Cache**: Writes data directly to the datastore, bypassing the cache. This can minimize cache pollution but might increase data fetch times on subsequent reads.
   3. **Write-Back Cache**: Writes data to the cache and then asynchronously writes the data to the datastore. This can be faster for write operations but can lead to data loss if the cache fails before the data is written to the datastore.
4. Don't forget to be explicit about what data you are storing in the cache, including the data structure you're using. 
   1. Remember, modern caches have many different data structures you can leverage, they are not just simple key-value stores. 
   2. So for example, if you are storing a list of events in your cache, you might want to use a sorted set so that you can easily retrieve the most popular events. 
   3. Many candidates will just say, "I'll store the events in a cache" and leave it at that. 
   4. This is a missed opportunity and may invite follow-up questions.
5. The two most common in-memory caches are Redis and Memcached. 
   1. Redis is a key-value store that supports many different data structures, including strings, hashes, lists, sets, sorted sets, bitmaps, and hyperloglogs. 
   2. Memcached is a simple key-value store that supports strings and binary objects.


-----
Credit:
1. https://www.hellointerview.com/learn/system-design/in-a-hurry/key-technologies#distributed-cache