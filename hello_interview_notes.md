# Ad hoc notes for System Design interview prep

## 5 Keys to Staff-Level System Design Interviews
1. Match your communication to the interviewer's level
   1. A hallmark of a good staff-level engineer is how rapidly they can assimilate context and how little they need to be told to get to the heart of the problem. They're able to explain a complex situation with less words and they are able to give deeper insights;
   2. Don't over-explain;
   3. Leverage commonly used patterns and concepts to shortcut the conversation;
2. Identify the crux of the problem 
   1. Great staff-level engineers know intuitively which parts are straightforward and gravitate towards the *most difficult parts*. When they execute, they're typically mentoring more junior engineers in how to solve the easy parts while they focus on the hard stuff with the greatest technical risk (remember this for the behavioral!);
   2. Avoid rabbit-holing on the easy parts.
   3. Earmark the tough parts of the problem to acknowledge them and so you can come back to them later. "Maintaining fault-tolerance over these hour-long transactions is a bit tricky, I'll come back to this in a bit."
   4. Shortcut the simple parts. "This is a pretty straightforward CRUD service, let's move on to the more interesting parts."
3. Ruthlessly cut complexity 
   1. Start with something as basic as possible that solves the functional requirements. Acknowledge this might be deficient in places and you'll solve those pinch points as they come. 
   2. YAGNI (you aren't gonna need it). Justify every complexity relative to other priorities in the design.
4. Demonstrate your depth
   1. While the question asked of you may not be a system you've built before, you should still be able to pull useful pieces from your past experience to inform the design. You haven't built Uber (obviously!) but you have worked with a workflow-heavy system. Make sure that shines through!
   2. Staff-level engineers operate differently than senior engineers and part of the premium in compensation is to pay you for your experiences. Demonstrating how you are able to take those experiences and apply them to a wholly new problem is a great way to show that you're earning this higher pay scale.
5. Show off your instincts
   1. The benefit of hiring a staff-level engineer is that they have a wealth of experience they can draw on when they make the most important decisions for your company or team. The biggest mistake candidates make is they shy away from doing this.
   2. Make the decision. Don't just outline options.
   3. Justify your decisions, but don't attempt to make an airtight case (you won't be able to!). You have less than an hour and your interviewer knows this.
   4.  "I'm going to go with Postgres here because I need transactions across tables and durability. I don't think this is going to be a scaling bottleneck but we can come back to this later if needed." is a great response.


## Delivery Framework
1. Functional Requirements:
   1. e.g. 
      1. Users should be able to post tweets 
      2. Users should be able to follow other users
2. Non-functional Requirements
   1. The system should be highly available, prioritizing availability over consistency 
   2. The system should be able to scale to support 100M+ DAU (Daily Active Users)
   3. The system should be low latency, rendering feeds in under 200ms
   4. Here is a checklist of things to consider that might help you identify the most important non-functional requirements for your system. You'll want to identify the top 3-5 that are most relevant to your system. 
      1. CAP Theorem: Should your system prioritize consistency or availability? Note, partition tolerance is a given in distributed systems. 
      2. Environment Constraints: Are there any constraints on the environment in which your system will run? For example, are you running on a mobile device with limited battery life? Running on devices with limited memory or limited bandwidth (e.g. streaming video on 3G)? 
      3. Scalability: All systems need to scale, but does this system have unique scaling requirements? For example, does it have bursty traffic at a specific time of day? Are there events, like holidays, that will cause a significant increase in traffic? Also consider the read vs write ratio here. Does your system need to scale reads or writes more? 
      4. Latency: How quickly does the system need to respond to user requests? Specifically consider any requests that require meaningful computation. For example, low latency search when designing Yelp. 
      5. Durability: How important is it that the data in your system is not lost? For example, a social network might be able to tolerate some data loss, but a banking system cannot. 
      6. Security: How secure does the system need to be? Consider data protection, access control, and compliance with regulations. 
      7. Fault Tolerance: How well does the system need to handle failures? Consider redundancy, failover, and recovery mechanisms. 
      8. Compliance: Are there legal or regulatory requirements the system needs to meet? Consider industry standards, data protection laws, and other regulations.
3. Capacity Estimation
   1. In most scenarios, you're dealing with a large, distributed system – and it's reasonable to assume as much. Many candidates will calculate storage, DAU, and QPS, only to conclude, "ok, so it's a lot. Got it." As interviewers, we gain nothing from this except that you can perform basic arithmetic.
   2. Our suggestion is to explain to the interviewer that you would like to skip on estimations upfront and that you will do math while designing when/if necessary.
4. Core Entities
   1. This helps you to define terms, understand the data central to your design, and gives you a foundation to build on. These are the core entities that your API will exchange and that your system will persist in a Data Model.
   2. Why not list the entire data model at this point? Because you don't know what you don't know. As you design your system, you'll discover new entities and relationships that you didn't anticipate. By starting with a small list, you can quickly iterate and add to it as you go.
5. API or System Interface
   1. You have a quick decision to make here -- which API protocol should you use? 
      1. REST (Representational State Transfer): Uses HTTP verbs (GET, POST, PUT, DELETE) to perform CRUD operations on resources. This should be your default choice for most interviews.
      2. GraphQL: Allows clients to specify exactly what data they want to receive, avoiding over-fetching and under-fetching. Choose this when you have diverse clients with different data needs. 
      3. RPC (Remote Procedure Call): Action-oriented protocol (like gRPC) that's faster than REST for service-to-service communication. Use for internal APIs when performance is critical.
      4. Don't overthink this. Default to REST unless you have a specific reason not to. For real-time features, you'll also need WebSockets or Server-Sent Events, but design your core API first.
      ```json
      POST /v1/tweets
      body: {
      "text": string
      }
      
      GET /v1/tweets/{tweetId} -> Tweet
      
      POST /v1/follows
      body: {
      "followee_id": string
      }
      
      GET /v1/feed -> Tweet[]
      ```
      Notice how we use plural resource names (tweets, not tweet). The current user is derived from the authentication token in the request header, not from request bodies or path parameters.
   
      Never rely on sensitive information like user IDs from request bodies when they should come from authentication. Always authenticate requests and derive the current user from the auth token, not from user input.



-------
Credit: 
1. https://www.hellointerview.com/blog/staff-level-system-design
2. https://www.hellointerview.com/learn/system-design/in-a-hurry/delivery