# Ad hoc notes for Consistent Hashing

## High level gists
1. Consistent hashing solves a specific problem that comes up with **distributed caches** and **sharded databases**. 
   1. When you use simple hash-based distribution (hash(key) % N to pick which server stores the data), adding or removing a server changes N. 
   2. That means almost every key maps to a different server, so you'd have to move most of your data around. 
   3. With millions of cache entries or database records, that's a disaster.
2. Consistent hashing fixes this by arranging both servers and keys on a **virtual ring**. 
   1. You hash each key and place it on the ring, then the key belongs to the next server you encounter going clockwise. 
   2. When you add a new server, only the keys between that new server and the previous server need to move. 
   3. When you remove a server, only its keys relocate to the next server on the ring. Everything else stays put.
3. The improvement is massive. 
   1. With simple modulo hashing, adding one server to a 10-server cluster means moving roughly 90% of your data. 
   2. With consistent hashing, you only move about 10% (the keys that belonged to the affected range). 
   3. This makes it practical to add and remove servers dynamically without causing a massive data migration.
4. This pattern shows up in several places. 
   1. Distributed caches like Memcached and Redis Cluster use it to distribute keys across cache nodes. 
   2. Distributed databases like Cassandra and DynamoDB use it for sharding. 
   3. Some load balancers use it to assign requests to backend servers in a way that's stable when servers come and go. 
   4. CDNs use it to route requests to edge servers.
5. The main time to bring it up is when you're discussing elastic scaling. 
   1. If your system needs to add or remove cache nodes or database shards based on load, mention consistent hashing as the mechanism that makes this practical without massive data movement.