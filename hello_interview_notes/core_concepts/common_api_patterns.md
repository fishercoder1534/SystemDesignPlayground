# Ad hoc notes for Common API Patterns

## High level gists
The principles above describe what a good API looks like. The patterns below are how those principles show up in production systems, and they apply regardless of whether you choose REST, GraphQL, or RPC. 

These are worth knowing since they come up in most real-world systems.

1. **Pagination**:
   1. When you're dealing with large datasets, you can't return everything at once. 
   2. Imagine an API that returns all events ever created, that could be millions of records which would be many gigabytes of data. 
   3. Instead, you need pagination to break large result sets into manageable chunks. 
   4. There are two main approaches to pagination: **offset**-based and **cursor**-based.
      1. Offset-based pagination is the simplest approach and used by most websites. You specify how many records to skip and how many to return: /events?offset=20&limit=10 gets records 21-30. This is intuitive and easy to implement, but it has problems with large datasets. If someone adds a new event while you're paginating through results, you might see duplicates or miss records as the data shifts.
      2. Cursor-based pagination solves this by using a pointer to a specific record instead of counting from the beginning. Here's how it works in practice:
         
         First request: /events?limit=10
         Response includes the events plus a cursor pointing to the last record:
      ```json
        {
        "events": [...],
        "next_cursor": "cmd9atj3p000007ky19w1dpy2"
        }
      ```
      Next request: /events?cursor=cmd9atj3p000007ky19w1dpy2&limit=10
      The cursor is typically an encoded reference to a specific record (like an ID or timestamp). 
      This is more stable because it's not affected by new records being added, but it's harder to implement features like "jump to page 5." In the example, cmd9atj3p000007ky19w1dpy2 is the id of the last event in the first page.
      For interviews, offset-based pagination is usually fine unless you're dealing with real-time data or the interviewer specifically asks about high-volume scenarios. 
      Most interviewers care more about whether you remembered to include pagination than which specific approach you choose.
2. **Filtering and Sorting**
   1. Once you're paginating, clients will want to control which slice of the data they get and in what order. 
   2. The convention is query parameters: /events?city=NYC&status=upcoming for filtering and /events?sort=date for sorting, with a leading minus for descending order (?sort=-date for newest first).
   3. The design work here isn't inventing the parameters, it's keeping them consistent. 
      1. If events filter by status, bookings should too, not state. 
      2. Once consumers learn the filter shape on one endpoint, they'll assume it works everywhere, and mismatched names across endpoints is one of the most common consistency violations in real API reviews. 
      3. In an interview, you don't need to enumerate every filter. Showing one filtered endpoint and saying the same conventions apply across the API is enough.
 
