# Ad hoc notes for Scaling Reads

## High level gists
1. As your application grows from hundreds to millions of users, read traffic often becomes the first bottleneck. 
   1. While writes create data, reads consume it - and read traffic typically grows much faster than write traffic. 
   2. The **Scaling Reads** pattern addresses high-volume read requests through database optimization, horizontal scaling, and intelligent caching.
2. For most applications, the read-to-write ratio starts at 10:1 but often reaches 100:1 or higher. 
   1. Consider Instagram: when you open the app, you see dozens of photos requiring hundreds of database queries for metadata, user info, and engagement data. 
   2. Meanwhile, you might only post once per day - a single write operation.
3. The solution follows a natural progression: 
   1. optimize read performance within your database through indexing and denormalization, scale horizontally with read replicas, then add external caching layers like Redis and CDNs.
4. Key considerations include:
   1. managing cache invalidation, 
   2. handling replication lag in read replicas, 
   3. and dealing with hot keys where millions of users request the same popular content simultaneously.


More to come. 
