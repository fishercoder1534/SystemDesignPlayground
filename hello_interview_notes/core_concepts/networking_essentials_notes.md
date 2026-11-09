# Ad hoc notes for Networking Essentials

## High level gists
1. The most important decision you'll make is choosing your communication protocol. 
   1. For most systems, you'll default to **HTTP over TCP**. It's well-understood, works everywhere, and handles 90% of use cases.
   2. Your interviewer will expect this unless you have a specific reason to use something else.
   3. HTTP is at Layer 7 and TCP is at Layer 4 of the OSI model;
   4. WebSockets and Server-Sent Events (SSE) come up when you need real-time updates. 
      1. The key difference: SSE is *uni*directional - the client makes an initial HTTP request to open the connection, and then the server pushes data down that connection (like live scores or notifications). The client can't send additional data over the same SSE connection. 
      2. WebSockets handle true *bi*directional communication where both sides send messages freely (like chat or live collaboration). 
      3. SSE is simpler to implement and works better with standard HTTP infrastructure, but WebSockets are necessary when clients need to push data back to the server frequently.
      4. Both are stateful connections, which means you can't just throw them behind a standard load balancer. You'll need to think about connection persistence and what happens when a server goes down with thousands of active connections.
      5. gRPC is worth mentioning for internal service-to-service communication when performance is critical. It uses binary serialization and HTTP/2, making it significantly faster than JSON over HTTP. But you won't use it for public-facing APIs because browsers don't natively support gRPC
      6. A common pattern is REST for external APIs and gRPC internally.
   5. Load balancing is another area interviewers love to probe. 
      1. Layer 7 load balancers operate at the application level and can route based on the actual HTTP request content. You can send API calls to one service and web page requests to another. 
      2. Layer 4 load balancers work at the TCP level and are faster but dumber. They just distribute connections without looking at the content. For WebSockets, you typically need Layer 4 balancing because you're maintaining a persistent TCP connection.
   6. Geography and latency matter more than most candidates realize. 
      1. A request from New York to London has a minimum latency of around 80ms just from the speed of light through fiber optic cables, before you even process anything. 
      2. If your system needs low latency globally, you'll need regional deployments with data replicated or partitioned by geography. 
      3. This is why CDNs exist - to serve static content from edge servers close to users.
