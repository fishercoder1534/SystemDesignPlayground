# Ad hoc notes for Dealing with Contention

## High level gists
1. When multiple users try to access the same resource simultaneously, like booking the last concert ticket or bidding on an auction item, you need mechanisms to prevent race conditions and ensure data consistency. 
   1. This pattern addresses coordination challenges in distributed systems.
2. Solutions range from database-level approaches like pessimistic locking and optimistic concurrency control to distributed coordination mechanisms. 
   1. The key is understanding when to use atomicity and transactions versus explicit locking strategies. 
   2. For distributed systems, you might need distributed locks, two-phase commit protocols, or queue-based serialization.
3. Trade-offs include performance versus consistency guarantees, and simple database solutions versus complex distributed coordination. 
   1. Most problems start with single-database solutions before scaling to distributed approaches.
4. Databases are built around **problems of contention**. 
   1. When you separate your data into multiple databases, you're taking on all of the challenges that the database systems were originally designed to solve. 
   2. In some cases this can be completely appropriate, but be careful about doing it prematurely. 
   3. Interviewers are keen to dig in and see if you really understand all that you're giving up by breaking your data apart.