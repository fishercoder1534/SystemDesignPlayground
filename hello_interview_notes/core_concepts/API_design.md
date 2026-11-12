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
2. Default to **REST** unless you have a specific reason not to. 
   1. It's well-understood, has great tooling, and works for 90% of use cases. 
   2. If you're unsure, just say "I'll use REST APIs" and move on.
3. For real-time features like notifications, chat, or live updates, you'll need different protocols like WebSockets or Server-Sent Events. 
   1. These aren't traditional APIs - they're persistent connections.

## REST
### Resource Modeling
1. The foundation of good REST API design is identifying your **resources** correctly. 
   1. If you've followed the delivery framework, resources are just your core entities.
   2. Take Ticketmaster as an example. Your core entities might be events, venues, tickets, and bookings. These naturally map to REST resources:
   3. Importantly, REST resources should represent things in your system, not actions. Instead of thinking about what users can do (like "book" or "purchase"), think about what exists in your system (events, venues, tickets, bookings).
   4. When handling relationships between resources, you have two main approaches. 
      1. You can nest resources when there's a clear parent-child relationship, like /events/{id}/tickets for all tickets belonging to a specific event. 
      2. Alternatively, you can keep resources flat and use query parameters for filtering, like /tickets?event_id=123.
      3. The key difference is whether the relationship is required or optional. 
         1. Use path parameters (or nested resources) when the value is required - like /events/{id}/tickets where you always need to specify which event's tickets you want. 
         2. Use query parameters when the filter is optional - like /tickets?event_id=123&section=VIP where you might want all tickets, or tickets filtered by event, or tickets filtered by both event and section.
      4. In interviews, this distinction helps you make the right choice quickly. 
         1. If the relationship is always required for the query to make sense, use a path parameter. 
         2. If it's an optional filter among many possible filters, use a query parameter. 
         3. The interviewer cares more about whether you can identify the right resources than perfect URL structure, but showing this understanding demonstrates good API design intuition.

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/events` | Get all events |
| `GET` | `/events/{id}` | Get a specific event |
| `GET` | `/venues/{id}` | Get a specific venue |
| `GET` | `/events/{id}/tickets` | Get available tickets for an event |
| `POST` | `/events/{id}/bookings` | Create a new booking for an event |
| `GET` | `/bookings/{id}` | Get a specific booking |


### HTTP Methods
### Passing Data to APIs
### Returning Data


-----------------------
Credit:
1. https://www.hellointerview.com/learn/system-design/core-concepts/api-design
