# Ad hoc notes for Consistent Hashing

## High level gists
1. The CAP theorem comes up when you're designing **distributed** systems and need to make tradeoffs about how your data behaves during failures. 
   1. It states you can only have two of three properties at once. 
   2. Consistency (all nodes see the same data), Availability (every request gets a response), and Partition tolerance (system works even when network connections fail between nodes). 
   3. Since network partitions are unavoidable in distributed systems, you're really choosing between consistency and availability.
2. Here's what that means in practice. 
   1. If you choose consistency, when a network partition happens, some nodes will refuse to serve requests rather than return potentially stale data. Your system might go down, but when it's up, the data is always correct. 
   2. If you choose availability, every node keeps serving requests even during a partition. Users always get a response, but different nodes might temporarily have different data until the partition heals.
3. For most systems, availability is the right default. 
   1. Users can tolerate seeing slightly stale data (your Instagram feed being 2 seconds old), but they can't tolerate the app being down. 
   2. Social media feeds, recommendation systems, and analytics dashboards all work fine with eventual consistency, where the system guarantees that all nodes will converge to the same state given enough time without new updates. 
   3. This is different from weak consistency, which makes no such guarantee about convergence.
4. Strong consistency matters when stale data causes actual business problems. 
   1. Inventory systems need accurate stock counts or you'll oversell products. 
   2. Banking systems need correct account balances or you'll allow fraud. 
   3. Booking systems like Ticketmaster need to prevent double-booking the same seat. 
   4. These are systems where reading stale data for even a few seconds can cost real money or create bad user experiences.
5. You don't have to pick one model for your entire system. 
   1. It's common to have different consistency requirements for different parts of the same application. 
   2. In an e-commerce system, product descriptions and reviews can be eventually consistent (who cares if a review takes 5 seconds to appear), 
   3. but inventory counts and order processing need strong consistency to prevent overselling.
6. It's worth knowing that the CAP theorem only describes behavior during network partitions, which are relatively rare. 
   1. In normal operation, the real tradeoff is between consistency and latency. 
   2. This is captured by the PACELC theorem: during a Partition, choose Availability or Consistency; Else, choose Latency or Consistency. 
   3. In practice this means that even when your network is healthy, choosing strong consistency adds latency because nodes need to coordinate before responding.
7. In interviews, when you mention replication or distributed data, your interviewer might ask about consistency. 
   1. The safe answer is eventual consistency unless the problem specifically involves money, inventory, or booking limited resources.
