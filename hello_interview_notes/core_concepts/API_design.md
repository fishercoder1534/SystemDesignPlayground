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
Once you've identified your resources, you need to decide how clients interact with them. HTTP provides a set of methods (verbs) that map naturally to common operations, and understanding when to use each one is crucial for interviews.

1. **GET** is for retrieving data without changing anything. Use GET for /events/{id} to fetch event details or /events to list all events.
2. **POST** creates new resources. When a user books tickets, you'd POST to /events/{id}/bookings with the booking details in the request body. The server assigns an ID and returns the newly created booking. POST is neither safe nor idempotent. In other words, calling it multiple times creates multiple bookings.
3. **PUT** replaces an entire resource with what you send, or creates it if it doesn't exist. If you're updating a user's profile completely, PUT to /users/{id} with the full user object. Unlike POST, PUT is idempotent, so sending the same data multiple times results in the same final state.
4. **PATCH** updates part of a resource. When a user changes just their email address, PATCH to /users/{id} with only the email field. Unlike PUT, PATCH is not guaranteed to be idempotent - the result depends on how you implement it. A PATCH that says "set email to X" is idempotent, but a PATCH that says "append to list" is not.
5. **DELETE** removes a resource. DELETE /bookings/{id} cancels a booking. It's idempotent because repeated calls leave the server in the same state (the resource stays deleted), even if the response codes differ (first call might return 204, subsequent calls might return 404).

6. The key here is idempotency, meaning whether repeated requests leave the server in **the same state**. 
   1. GET, PUT, and DELETE are idempotent because calling them multiple times doesn't change the final result. 
   2. POST and PATCH are not guaranteed to be idempotent, which matters when networks fail and clients retry requests. You don't want duplicate bookings from a retry.


### Passing Data to APIs

API endpoints need input to tell the server what to do. 
1. This can be which resources to fetch, what data to update in the database, or how to filter results. 
2. Understanding where to put different types of input is crucial for designing clean, intuitive APIs. 
3. You have three main options for passing data to your REST API, and each serves a different purpose.
   1. **Path parameters** identify which specific resource you're working with. 
      1. When you want to get event details, you put the event ID in the path: /events/123. 
      2. The ID is part of the URL structure itself, making it clear that you're asking for a specific event, not a collection of events. 
      3. Use path parameters when the value is required to identify the resource - without it, the request doesn't make sense.
   2. **Query parameters** filter, sort, or modify how you retrieve resources. 
      1. When you want to search for events in a specific city or date range, you use query parameters: /events?city=NYC&date=2024-01-01. 
      2. These are optional - you could ask for all events without any filters, or apply multiple filters together. 
      3. Query parameters work well for pagination too: /events?page=2&limit=20. 
      4. Note that the first option is separated via a ? and all subsequent parameters are separated by &.
   3. **Request body** contains the actual data you're sending to create or update resources. 
      1. When a user books tickets, you POST to /events/{id}/bookings with the booking details in the request body. 
      2. Things like how many tickets, seating preferences, and so on. 
      3. The request body is where you put complex data structures and anything that might be too large or sensitive for a URL.

Each type of input serves a different role in the API's contract. 
1. **Path parameters** are structural, they determine which endpoint you're hitting. 
2. **Query parameters** are modifiers, they change how the endpoint behaves. 
3. **Request body** is payload, it's the data you're actually working with.

4. Here's a practical example. If you're building a booking system and a user wants to book VIP tickets for a specific event, you'd structure it like this:

The event ID (123) is in the path because you need to specify which event you're booking. The notification preference is a query parameter because it's optional behavior. The actual booking details go in the request body because they're the core data you're creating.

```json
POST /events/123/bookings?notify=true
{
  "tickets": [
    {"section": "VIP", "quantity": 2},
    {"section": "General", "quantity": 1}
  ],
  "payment_method": "credit_card"
}
```


### Returning Data
An API response is made up of two parts:
1. The status code, which indicates whether the request was successful or not.
2. The response body, which contains the data you're returning to the client (typically JSON)

For status codes, stick to the common ones: 200 for success, 201 for created resources, 400 for bad requests, 401 for authentication required, 404 for not found, and 500 for server errors.

Don't overthink this in interviews - interviewers care more about whether you understand the difference between client errors (4xx) and server errors (5xx) than memorizing every status code. Even using X's to obscure the status code is typically totally fine.








## GraphQL
GraphQL emerged from Facebook in 2012 to solve a specific problem: their mobile app needed different data than their web app, but they were stuck with REST endpoints that returned fixed data structures. 
The mobile team kept asking for new endpoints or modifications to existing ones and this was slowing down development on both sides.

With REST, you typically have two unpleasant choices when different clients need different data. You can create multiple endpoints for different use cases, leading to endpoint proliferation and maintenance headaches. 
Or you can make your endpoints return everything any client might need, leading to over-fetching where mobile clients download megabytes of data they don't use.

GraphQL consolidates resource endpoints into a single endpoint that accepts queries describing exactly what data you want. 
The client specifies the shape of the response, and the server returns data in that exact format.

### How GraphQL Works
Here's a simple example using our Ticketmaster scenario. Instead of separate REST endpoints for events, venues, and tickets, you'd have a single GraphQL endpoint that can handle queries like this:
```json
query {
  event(id: "123") {
    name
    date
    venue {
      name
      address
    }
    tickets {
      section
      price
      available
    }
  }
}
```

The server returns exactly what you asked for, nothing more, nothing less. If the mobile app only needs event names and dates, it can request just those fields. If the web dashboard needs comprehensive event details with venue information and ticket availability, it can request all of that in a single query.

### When to Use GraphQL in Interviews

GraphQL is the right choice when you have diverse clients with different data needs. 
1. If your interviewer mentions scenarios like "the mobile app needs different data than the web app" or asks about "avoiding over-fetching and under-fetching," they're likely looking for you to bring up GraphQL.
2. It's also a good choice when frontend teams need to iterate quickly without backend changes. 
3. With REST, adding a new field to a mobile screen often requires backend changes and API deployments. 
4. With GraphQL, the frontend team can request additional fields as long as they exist in the schema. 
5. However, GraphQL adds complexity. You need to implement query parsing, schema validation, and often sophisticated caching strategies. 
6. For most system design interviews, REST is simpler and more straightforward unless the problem specifically calls for GraphQL's flexibility.


### GraphQL Schema Design
If you do choose GraphQL, you'll need to think differently about design. 

Instead of REST's resource endpoints, you design a schema that defines your data types and their relationships.

For our Ticketmaster example, you'd start by modeling your core entities as GraphQL types:
```json
type Event {
  id: ID!
  name: String!
  date: DateTime!
  venue: Venue!
  tickets: [Ticket!]!
}

type Venue {
  id: ID!
  name: String!
  address: String!
}

type Query {
  event(id: ID!): Event
  events(limit: Int, after: String): [Event!]!
}
```

The key difference from REST is that you define **relationships** directly in the **schema**. 
1. An Event has a Venue, and clients can traverse that relationship in a single query.
2. But this flexibility creates the N+1 problem, the biggest GraphQL gotcha. 
3. When a client queries events with their venues, you might execute one query for events, then N separate queries for each venue. With 100 events, that's 101 database queries instead of 2. 
4. The solution is **batching/dataloader patterns** that group related queries together, but it adds complexity you don't have with REST.

GraphQL also handles authorization differently. 
1. Instead of securing entire endpoints like REST, you secure individual fields. A user might see an event's name and date but not the venue data. 
2. You can control this at the field level in your schema resolvers.

In interviews, mention GraphQL when you see clear over-fetching or under-fetching problems, but don't default to it.
Most interviewers appreciate that you know about GraphQL, but they usually prefer to see you solve the core architectural challenges with simpler tools first.
 


## RPC
**RPC** (Remote Procedure Call) is a communication paradigm that allows a client to call a procedure on a server and wait for a response without the client having to understand the underlying network details. 
Protocols like gRPC use **binary** serialization and **HTTP/2**, making them faster than traditional **JSON-over-HTTP REST APIs** for service communication.

### How RPC Works
Unlike REST's resource-oriented approach, RPC is action-oriented. You're essentially calling functions across a network as if they were local functions in your codebase. Here's how the same Ticketmaster operations might look with RPC:
```json
// Instead of GET /events/123
getEvent(eventId: "123")

// Instead of POST /events/123/bookings
createBooking(eventId: "123", userId: "456", tickets: [...])

// Instead of GET /events/123/tickets
getAvailableTickets(eventId: "123", section: "VIP")
```
The most popular RPC protocol today is **gRPC**, which uses Protocol Buffers for serialization and HTTP/2 for transport. 
1. This combo is **much** faster than REST's JSON-over-HTTP approach, especially for service-to-service communication. 
2. Another notable RPC framework is **Apache Thrift**, originally developed at Facebook and now open source, which supports multiple programming languages and serialization formats.


### Protocol Buffers and Type Safety

gRPC uses Protocol Buffers (protobuf) to define service contracts. You write a .proto file that describes your service methods and data structures:
```json
service TicketService {
  rpc GetEvent(GetEventRequest) returns (Event);
  rpc CreateBooking(CreateBookingRequest) returns (Booking);
  rpc GetAvailableTickets(GetTicketsRequest) returns (TicketList);
}

message GetEventRequest {
  string event_id = 1;
}

message Event {
  string id = 1;
  string name = 2;
  int64 date = 3;
  Venue venue = 4;
}
```

From this single definition, gRPC generates client and server code in multiple programming languages. This means your Go backend service and your Java payment service can communicate with compile-time type safety, catching mismatches before deployment.

### When to Use RPC in Interviews
RPC shines in microservice architectures where services need to communicate frequently and efficiently. If your interviewer mentions internal service communication, high-performance requirements, or polyglot environments (different services in different languages), RPC is likely a good choice.

Consider RPC when:
1. Performance is critical: Binary serialization and HTTP/2 make RPC **significantly** faster than JSON REST
2. Type safety matters: Generated client code prevents many runtime errors
3. Service-to-service communication: Internal APIs between your own services don't need REST's resource semantics
4. Streaming is needed: gRPC supports bidirectional streaming for real-time features

For our Ticketmaster example, you might use REST APIs for your public endpoints that mobile apps and web clients consume, but use gRPC for internal communication between your booking service, payment service, and inventory service.

Unless explicitly asked, you won't typically outline your internal APIs during the API step of the interview. 

Instead, focus on just the user facing APIs here. At most, you'll call out that internal services communicate over RPC during your high-level design.


-----------------------
Credit:
1. https://www.hellointerview.com/learn/system-design/core-concepts/api-design
