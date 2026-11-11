# Ad hoc notes for Database Indexing

## High level gists
1. Indexes are used to make database queries fast. 
   1. Without an index, finding a user by email means scanning every single row in your users table. 
   2. If you have 10 million users, that's 10 million rows to check. With an index on the email column, the database can jump straight to the right row in milliseconds.
2. The most common index is a B-tree. 
   1. It keeps data sorted in a tree structure that supports both exact lookups (find user with email X) and range queries (find all orders between date A and date B). 
   2. Most relational databases create B-tree indexes by default. 
   3. Hash indexes are faster for exact matches but can't do range queries, so they're less common. 
   4. You'll also see specialized indexes like full-text indexes for search (finding documents containing specific words) and geospatial indexes for location queries (find restaurants within 5 miles).
3. In interviews, think about your query patterns and propose indexes on the fields you're querying frequently. 
   1. If you're looking up users by email for authentication, index the email column. 
   2. If you're fetching a user's orders, index the user_id column on the orders table. 
   3. For composite queries like "find events in San Francisco on December 25th," you might need a compound index on both city and date.
4. For specialized needs beyond what your primary database supports, you'll need external systems. 
   1. Elasticsearch is the go-to for **full-text** search (think searching tweets or documents). 
   2. For geospatial queries in Postgres, PostGIS is a popular extension. 
   3. These external indexes typically sync from your primary database via change data capture (CDC), meaning the search index will lag slightly behind the primary database. The data you read from the search index is going to be stale by some small amount, but for search use cases that's almost always acceptable. The tradeoff is worth it because it lets you search in ways your main database can't handle.