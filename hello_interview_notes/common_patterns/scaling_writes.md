# Ad hoc notes for Scaling Writes

## High level gists
1. As your application grows from hundreds to **millions** of writes per second, individual database servers and storage systems hit hard limits. 
   1. The Scaling Writes pattern addresses write bottlenecks through sharding, batching, and intelligent load management.
2. The core strategies are:
   1. **horizontal sharding** (distributing data across multiple servers), 
   2. vertical partitioning (separating different types of data), 
   3. and handling write bursts through queues and load shedding. 
   4. Key considerations include selecting good partition keys that distribute load evenly while keeping related data together.
3. For burst handling, you can use write queues to buffer temporary spikes or implement load shedding to prioritize important writes during overload. 
   1. Batching techniques help reduce per-operation overhead by grouping multiple writes together.