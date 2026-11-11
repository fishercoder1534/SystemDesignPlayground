# Ad hoc notes for Queues

## High level gists
1. Queues serve as buffers for bursty traffic or as a means of distributing work across a system. 
   1. A compute resource sends messages to a queue and forgets about them. 
   2. On the other end, a pool of workers (also compute resources) processes the messages at their own pace. 
   3. Messages can be anything from a simple string to a complex object.
2. The queue's function is to smooth out the load on the system. 
   1. If I get a spike of 1,000 requests but can only handle 200 requests per second, 800 requests will wait in the queue before being processed — but they are not dropped! 
   2. Queues also decouple the producer and consumer of a system, allowing you to scale them independently. 
   3. I can bring down and up services behind a queue with negligible impact.
3. Be careful of introducing queues into **synchronous** workloads. 
   1. If you have strong **latency** requirements (e.g. < 500ms), by adding a queue you're nearly guaranteeing you'll break that latency constraint.
4. A couple common use cases for queues:
   1. **Buffer for Bursty Traffic**: 
      1. In a ride-sharing application like Uber, queues can be used to manage sudden surges in ride requests. 
      2. During peak hours or special events, ride requests can spike massively. 
      3. A queue buffers these incoming requests, allowing the system to process them at a manageable rate without overloading the server or degrading the user experience.
   2. **Distribute Work Across a System**: 
      1. In a cloud-based photo processing service, queues can be used to distribute expensive image processing tasks. 
      2. When a user uploads photos for editing or filtering, these tasks are placed in a queue. 
      3. Different worker nodes then pull tasks from the queue, ensuring even distribution of workload and efficient use of computing resources.
      4. In a cloud-based photo processing service, queues can be used to distribute expensive image processing tasks. 
      5. When a user uploads photos for editing or filtering, these tasks are placed in a queue. 
      6. Different worker nodes then pull tasks from the queue, ensuring even distribution of workload and efficient use of computing resources.

## Things you should know about queues for your interview
1. Message Ordering: 
   1. Most queues are FIFO (first in, first out), meaning that messages are processed in the order they were received. 
   2. However, some queues (like Kafka) allow for more complex ordering guarantees, such as ordering based on a specified priority or time.
2. Retry Mechanisms: 
   1. Many queues have built-in retry mechanisms that attempt to redeliver a message a certain number of times before considering it a failure. 
   2. You can configure retries, including the delay between attempts, and the maximum number of attempts.
3. Dead Letter Queues: 
   1. Dead letter queues are used to store messages that cannot be processed. 
   2. They're useful for debugging and auditing, as it allows you to inspect messages that failed to be processed and understand why they failed.
4. Scaling with Partitions: 
   1. Queues can be partitioned across multiple servers so that they can scale to handle more messages. 
   2. Each partition can be processed by a different set of workers. 
   3. Just like databases, you will need to specify a partition key to ensure that related messages are stored in the same partition.
5. Backpressure: 
   1. The biggest problem with queues is they make it easy to overwhelm your system. 
   2. If my system supports 200 requests per second but I'm receiving 300 requests per second, I'll never finish them! 
   3. A queue is just obscuring the problem that I don't have enough capacity. 
   4. The answer is backpressure. Backpressure is a way of slowing down the production of messages when the queue is overwhelmed. 
   5. This helps prevent the queue from becoming a bottleneck in your system. 
   6. For example, if a queue is full, you might want to reject new messages or slow down the rate at which new messages are accepted, potentially returning an error to the user or producer.

### What are the most common queueing technologies?
1. The most common queueing technologies are Kafka and SQS. 
   1. Kafka is a distributed streaming platform that can be used as a queue, 
   2. while SQS is a fully managed queue service provided by AWS.