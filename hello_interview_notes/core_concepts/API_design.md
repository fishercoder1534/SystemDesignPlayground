# Ad hoc notes for API Design

## High level gists
1. For 90% of interviews, you'll default to REST. 
   1. It maps resources to URLs and uses HTTP methods to manipulate them. 
   2. Think /users/{id} for getting a user, POST /events/{id}/bookings for creating a booking. 
   3. REST is well-understood, works everywhere, and your interviewer will assume this unless you propose something else.
2. You should be able to sketch out 4-5 key endpoints in **a couple minutes** and move on.
3. If you're returning large result sets, you'll need pagination. 
   1. Cursor-based works better for real-time data where new items get added frequently, 
   2. but offset-based is fine for most cases. 
   3. For authentication, use JWT tokens for user sessions and API keys for service-to-service calls. 
   4. And if your system could get hammered by bots or abuse, mention rate limiting. 
   5. But don't go deep on any of these unless the interviewer specifically asks.

## API Types
1. In an interview, you'll typically choose between three main API protocols:
   1. REST (Representational State Transfer) - REST uses standard HTTP methods (GET, POST, PUT, DELETE) to manipulate resources identified by URLs. 
      1. For standard CRUD operations in web and mobile applications, REST maps naturally to your database operations and HTTP semantics, making it the go-to protocol for most web services. 
      2. This should be your default choice.
   2. GraphQL - Unlike REST's fixed endpoints, GraphQL uses a single endpoint with a query language that lets clients specify exactly what data they need. 
      1. Think about a mobile app that needs only basic user information versus a web dashboard that displays comprehensive analytics - with REST, you'd either create multiple endpoints or force clients to fetch more data than they need, but GraphQL lets each client request exactly what it needs in a single query. 
      2. If your interviewer mentions "flexible data fetching" or talks about avoiding over-fetching and under-fetching, they're signaling you to consider GraphQL.
   3. RPC (Remote Procedure Call) - RPC protocols like gRPC use binary serialization and HTTP/2 for efficient communication between services. 
      1. While REST treats everything as resources, RPC lets you think in terms of actions and procedures - when your user service needs to quickly validate permissions with your auth service, an RPC call like checkPermission(userId, resource) is more natural than trying to model this as a REST resource. 
      2. If the interviewer specifically mentions microservices or internal APIs, consider RPC for those high-performance connections. 
      3. Use RPC when performance is critical.