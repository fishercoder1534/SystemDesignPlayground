# Ad hoc notes for Distributed Lock

## High level gists
1. When you're dealing with online systems like Ticketmaster, you might need a way to lock a resource - like a concert ticket - for a short time (~10 minutes in this case). 
   1. This is so while one user is in the middle of buying a ticket, no one else can grab it. 
   2. Traditional databases with ACID properties use transaction locks to keep data consistent, which is great for ensuring that while one user is updating a record, no one else can update it, but they're not designed for longer-term locking. 
   3. This is where distributed locks come in handy.
2. Distributed locks are perfect for situations where you need to lock something across different systems or processes for a reasonable period of time. 
   1. They're often implemented using a distributed key-value store like Redis or ZooKeeper. 
   2. The basic idea is that you can use a key-value store to store a lock and then use the atomicity of the key-value store to ensure that only one process can acquire the lock at a time. 
   3. For example, if you have a Redis instance with a key ticket-123 and you want to lock it, you can set the value of ticket-123 to locked. 
   4. If another process tries to set the value of ticket-123 to locked, it will fail because the value is already set to locked. 
   5. Once the first process is done with the lock, it can set the value of ticket-123 to unlocked and another process can acquire the lock.
3. Another handy feature of distributed locks is that they can be set to expire after a certain amount of time. 
   1. This is great for ensuring that locks don't get stuck in a locked state if a process crashes or is killed. 
   2. For example, if you set the value of ticket-123 to locked and then the process crashes, the lock will expire after a certain amount of time (like 10 minutes) and another process can acquire the lock at that point.

## Common examples of when to use a distributed lock in a system design interview:
1. **E-Commerce Checkout System**: Use a distributed lock to hold a high-demand item, like limited-edition sneakers, in a user's cart for a short duration (like 10 minutes) during checkout to ensure that while one user is completing the payment process, the item isn't sold to someone else.
2. **Ride-Sharing Matchmaking**: A distributed lock can be used to manage the assignment of drivers to riders. 
   1. When a rider requests a ride, the system can lock a nearby driver, preventing them from being matched with multiple riders simultaneously. 
   2. This lock can be held until the driver confirms or declines the ride or until a certain amount of time has passed.
3. **Distributed Cron Jobs**: 
   1. For systems that run scheduled tasks (cron jobs) across multiple servers, a distributed lock ensures that a task is executed by only one server at a time. 
   2. For instance, in a data analytics platform, a daily job aggregates user data for reports. 
   3. A distributed lock can prevent the duplication of this task across multiple servers to save compute resources.
4. **Online Auction Bidding System**: 
   1. In an online auction, a distributed lock can be used during the final moments of bidding to ensure that when a bid is placed in the last seconds, the system locks the item briefly to process the bid and update the current highest bid, preventing other users from placing a bid on the same item simultaneously.

## Things you should know about distributed locks for your interview
1. **Locking Mechanisms**: There are different ways to implement distributed locks. 
   1. One common implementation uses Redis and is called Redlock. 
   2. Redlock uses multiple Redis instances to ensure that a lock is acquired and released in a safe and consistent manner.
2. **Lock Expiry**: Distributed locks can be set to expire after a certain amount of time. 
   1. This is important for ensuring that locks don't get stuck in a locked state if a process crashes or is killed.
3. **Locking Granularity**: Distributed locks can be used to lock a single resource or a group of resources. 
   1. For example, you might want to lock a single ticket in a ticketing system or you might want to lock a group of tickets in a section of a stadium.
4. **Deadlocks**: Deadlocks can occur when two or more processes are waiting for each other to release a lock. 
   1. Think about a situation where two processes are trying to acquire two locks at the same time. 
   2. One process acquires lock A and then tries to acquire lock B, while the other process acquires lock B and then tries to acquire lock A. 
   3. This can lead to a situation where both processes are waiting for each other to release a lock, causing a deadlock. 
   4. You should be prepared to discuss how to prevent this - a common mistake is to have locks pulled from far-flung pieces of infrastructure or your code, this makes it hard to recognize and prevent deadlocks.