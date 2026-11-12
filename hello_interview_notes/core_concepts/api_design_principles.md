# Ad hoc notes for API Design Principles

## High level gists
Protocols, resources, and endpoints are the mechanics of API design. But when an interviewer looks at your API and calls it "**clean**," they're really measuring it against a handful of principles that hold no matter which protocol you chose. 

You'll rarely be asked to recite API design principles directly. Instead, they're the standard your design gets judged against, and applying the right one at the right moment is how you sound like someone who has built real APIs before.

Nine principles cover most of what matters:

1. **Design around resources, not actions**. Expose the things in your system and let the methods express what clients do with them. If you catch yourself writing endpoints like /getUserBookings or /processPayment, stop and ask which resource you're actually manipulating (GET /users/{id}/bookings, POST /payments). This is the same advice from resource modeling, promoted to a principle because it's the first thing interviewers check.
2. **Be consistent everywhere**. Same naming conventions, same parameter names, same response shapes across every endpoint. If one endpoint filters with ?status=paid and another with ?state=paid, consumers can't predict your API, they have to memorize it. Consistency is boring, and that's exactly the point. Someone who has seen one of your endpoints should be able to guess the rest.
3. **Follow the principle of least surprise**. Use HTTP the way everyone else does. GET never changes data, DELETE deletes, and a 200 means success (not an error wrapped in a "success": false body). Every place your API deviates from what a reasonable developer would guess is a place where every consumer pays a small tax, forever.
4. **Keep the API stateless**. Each request should carry everything the server needs to process it: the auth token, the parameters, the payload. When no server has to remember anything about previous requests, any server can handle any request, and horizontal scaling becomes a load balancer problem instead of an application problem. This is a big part of why JWTs are so popular in distributed systems.
5. **Make retries safe**. Networks fail mid-request, and the client has no idea whether the server did the work before things fell apart. Its only option is to retry, so design your API so retries are harmless. GET, PUT, and DELETE are idempotent by nature. For POST endpoints that create things users care about, like bookings and payments, use idempotency keys (covered below).
6. **Paginate anything that can grow**. A list endpoint that returns "all bookings" works fine with 50 rows in development and falls over at 5 million in production. Every collection endpoint gets pagination from day one, along with a cap on page size so a client can't ask for ?limit=100000 and take down your database.
7. **Secure every endpoint by default**. Authentication should be opt-out, not opt-in: every endpoint requires a valid token unless you deliberately mark it public, and authorization confirms the caller can act on this specific resource (John can cancel his own bookings, not everyone's). Rate limiting backs it all up. 
8. **Evolve without breaking clients**. Once someone depends on your API, you no longer control when they update. Old mobile app versions stick around for years. Adding a new field is safe, but renaming, removing, or changing the meaning of an existing one is a breaking change that needs a versioning story.
9. **Make errors actionable**. A good error response tells the client what went wrong and what to do about it. That means the right status code class (4xx for "you messed up," 5xx for "we messed up") plus a machine-readable error code and a human-readable message in the body.


You don't get points for listing principles in the abstract. You get points for applying them out loud. "**I'll use an idempotency key on this endpoint so a retry can't double-book the seat**" lands far better than a lecture on idempotency. Use principles as the justification for your decisions and you'll demonstrate all nine without ever naming them.



-------------
Credit:
1. https://www.hellointerview.com/learn/system-design/core-concepts/api-design#api-design-principles