# Ad hoc notes for Data Modeling

## High level gists
1. Data modeling is one of those things that sounds simple but has massive downstream effects on your system. The decisions you make about what data to store and how to structure it directly affect performance, scalability, and how painful it is to build and maintain your system.
2. The first big choice is relational versus NoSQL. 
   1. Relational databases like Postgres work great when you have structured data with **clear relationships** and need **strong consistency**. 
      1. Things like user accounts linking to orders linking to products. 
      2. You can express complex queries with SQL, use transactions to keep data consistent, and enforce foreign key constraints. 
   2. NoSQL databases like DynamoDB or MongoDB shine when you need flexible schemas (your data structure changes frequently) or you need to scale horizontally across many servers without complex joins.
3. Within relational databases, you'll hear about normalization and denormalization. 
   1. **Normalization** means splitting data across tables to **avoid duplication**. 
      1. You have a users table, an orders table, and a products table. 
      2. Each order references a user ID and product ID instead of copying the full user and product data into every order record. 
      3. This keeps your data consistent (update a product name once and it's updated everywhere), but it means you need joins to get complete data. 
      4. Joins get expensive when your tables are huge or you're joining across multiple tables.
   2. **Denormalization** goes the other way. You **duplicate** data to avoid joins and make reads faster. 
      1. Instead of joining to the users table every time you display an order, you store the username directly in each order record. 
      2. Now you can fetch an order and display it without touching another table. 
      3. The downside is updates. If a user changes their name, you have to update it in the users table plus every order record that copied it. 
      4. For read-heavy systems where data rarely changes, this tradeoff is often worth it.
   3. In interviews, a safe default is to start with a normalized relational model and then denormalize specific hot paths if you identify read performance issues.
   4. NoSQL databases force you to think differently. 
      1. DynamoDB requires you to design your *partition key* and *sort key* based on your access patterns. 
      2. If you're building a social media app and your most common query is "get all posts for user X," you'd use *user_id* as the partition key. This makes that query a fast single-partition lookup. 
      3. But now queries like "get all posts mentioning hashtag Y" require scanning the entire table because you didn't design for that access pattern. 
      4. You have to know your queries upfront and design around them.


## Database Model Options
In interviews, the temptation is to show off by choosing exotic database types. **Resist** this. Most of the time, the right answer is a **relational database**. It's the default unless your requirements clearly signal a specialized model. Unless you have significant experience and, with it, strong opinions about another database, the recommendation is to stick with **PostgreSQL**.

That doesn’t mean other database models aren’t worth knowing. Showing you understand when they might be useful demonstrates that you’re thinking about trade-offs, not just parroting the default. Still, the star of the show is SQL, so we’ll start there before briefly touching on the alternatives.

### Relational Databases (SQL)
Relational databases organize data into tables with **fixed schemas**, where **rows represent entities** and **columns represent attributes**. They enforce relationships through **foreign keys** and provide **ACID guarantees** for transactions.

SQL is great at handling complex queries. 
1. If you need to fetch "all posts by users that a given user follows, ordered by recency," joins make that straightforward. 
2. However, be careful with multi-table joins like this - they can become performance traps at scale. 
3. In interviews, mentioning complex reporting-style queries often raises yellow flags about performance, so you'll want to think through whether they're actually performant enough or if you need denormalized views, caching, or pre-computed results. 
4. And when strong consistency is a non-functional requirement, like ensuring payments don't double-charge or inventory doesn't oversell, **SQL's ACID guarantees** are the right tool for the job.

The usual knock on relational databases is **scalability**, but this is often **exaggerated**. 
1. Modern SQL databases scale with techniques like **read replicas**, **sharding**, **connection pooling**, and **caching**. 
2. Some of the largest companies in the world (Facebook, Airbnb) rely on relational foundations. 
3. Scaling isn't just about the database you choose, but how you architect around it.

### Document Databases
Document databases store data as JSON-like documents with flexible schemas, making them good for rapidly evolving applications where you don't know all your data fields upfront. 
1. Your data modeling becomes more about nesting and embedding related information within documents rather than normalizing across tables. 
2. Notice how the same data from our SQL example gets restructured. 
3. Instead of separate tables, we embed posts directly within each user document. 
4. This eliminates joins but means updating a post requires finding and modifying the entire user document.

Users collection:
```json
{
  "_id": "507f1f77bcf86cd799439011",
  "username": "john_doe",
  "email": "john@example.com",
  "posts": [
    {
      "content": "Hello, world!",
      "created_at": "2024-01-01T10:00:00Z"
    },
    {
      "content": "My first post",
      "created_at": "2024-01-01T10:05:00Z"
    }
  ],
  "created_at": "2024-01-01T10:00:00Z"
}
```

System design interviews intentionally scope functional requirements to a **clear, concise** set. 
1. This means you're unlikely to have "evolving schemas" in the first place, which removes the main reason to choose document databases. 
2. Only consider them if your interviewer explicitly mentions rapidly changing data structures.

#### When to consider over SQL: 
When your schema changes frequently, when you have **deeply** nested data that would require **many joins** in SQL, or when different records have vastly different structures. A user profile system where some users have extensive work histories while others have minimal data fits this pattern.

#### Data modeling impact: 
You'll **de**normalize more aggressively, embedding related data within documents to avoid expensive lookups across collections. 
1. This trades storage space and update complexity for read performance. 
2. Example technologies include MongoDB, Firestore, and CouchDB.

### Key-Value Stores
Key-value stores provide simple lookups where you fetch values by exact key match. 
1. They're extremely fast but offer limited query capabilities beyond that basic operation.
2. When to consider this over SQL: For caching, session storage, feature flags, or any scenario where you only need to look up data by a single identifier. 
3. They're also good for high-write scenarios where you need maximum performance and don't need complex queries.

In practice, you'll often use both together. SQL as your source of truth with a key-value cache (like Redis) in front for hot data. This gives you fast access without sacrificing durability or complex queries.

Data modeling impact: 
1. Your schema becomes very flat. 
2. You'll denormalize heavily and duplicate data across multiple keys to support different access patterns, since you can't join or query across relationships. 
3. This is great for reads but terrible for consistency when data changes.

### Wide-Column Databases

Wide-column databases organize data into column families where rows can have different sets of columns. They're optimized for massive write-heavy workloads and time-series data.

When a user creates a new post, you add a new row keyed by (user_id, timestamp). Rows with the same partition key (user_id) are stored together, making writes fast (just append to the partition) and reads efficient (scan a contiguous range of a user's posts).

When to consider over SQL: 
1. When you have **enormous write volumes**, **time-series data**, or analytics workloads where you primarily **append data** and run aggregations. 
2. Think telemetry, event logging, or IoT sensor data.

Data modeling impact: 
1. You'll design around query patterns even more than with SQL, often duplicating data across different column families to support various access patterns. 
2. Time becomes a first-class citizen in your modeling.

Example technologies include Cassandra and HBase.

### Graph Databases
Graph databases store data as **nodes** and **edges**, optimizing for traversing **relationships** between **entities**.

When to consider over SQL: 
1. Honestly? **Almost never** in interviews. 
2. The classic examples are social networks and recommendation engines, but even **Facebook** models their **social graph** with **MySQL**. 
3. If it's good enough for the world's largest social network, it's probably good enough for your interview.

Example technologies include Neo4j and Amazon Neptune.

Graph databases are a **common mistake in interviews**:
1. They sound sophisticated but add unnecessary complexity. 
2. Even "graph-heavy" companies like LinkedIn and Twitter use **SQL** for their **core relationship** data. 
3. Other databases can handle the primary query patterns without the operational complexity that comes with specialized graph systems.


## Schema Design Fundamentals

### Start with Requirements

Everything flows from three key factors that require careful consideration and were likely already determined during the requirements gathering and api design phases.

1. **Data volume** determines where your data can physically live. 
   1. A social media app with millions of users might need data spread across multiple data stores, which drives schema design choices. 
   2. If user data and post data need to live on separate systems for performance or organizational reasons, they necessarily need distinct schemas with careful consideration of how they reference each other.
2. **Access patterns** are the most important factor and drive most of your design decisions. 
   1. How will your data be queried? 
   2. A news feed that loads "recent posts by followed users" suggests you'll want denormalized data or carefully designed indexes. 
   3. An analytics dashboard that aggregates data across time periods might need different table structures entirely. 
   4. This comes naturally from your APIs. Just ask what queries will I need to support each endpoint?
3. **Consistency** requirements determine how **tightly coupled** your data can be. 
   1. Financial transactions need **strong consistency** (no partial charges), which often means keeping related data in the same database with ACID guarantees. 
   2. But a user's activity feed can handle eventual consistency (it's okay if a like shows up a few seconds later), which allows you to distribute that data across separate systems with different schemas optimized for different access patterns.

In interviews, explicitly **tie** your schema choices back to these factors. 
1. For example, "Since we need to load feeds quickly and likes can be eventually consistent, I'll denormalize like counts into the posts table." 
2. That shows you're reasoning instead of memorizing patterns.

All of the schema design techniques that follow (entities, keys, normalization, indexes, sharding) are just tools to address these three factors.


### Entities, Keys & Relationships
Once you’ve identified your core entities, the next step is to map them into tables (or collections) with clear identifiers and relationships.

For a social media app, you might have _users_, _posts_, _comments_, and _likes_. Each entity needs a primary key to identify individual records. Use system-generated IDs like user_id or post_id rather than business data like email addresses. **System-generated keys stay stable** even when business rules change.

```json
users: id (PK), username, email
posts: id (PK), user_id (FK → users.id), content, created_at
comments: id (PK), post_id (FK → posts.id), user_id (FK → users.id), content
likes: user_id (FK → users.id), post_id (FK → posts.id)
```

This shows the core relationships: 
1. each post belongs to one user (posts.user_id), 
2. each comment belongs to one post and one user, 
3. and likes connect users to posts. 
4. The (PK) marks primary keys, (FK) marks foreign keys with arrows showing what they reference.

In interviews, just pick an obvious primary key and explain why. "post_id will be our primary key so we can identify each post and reference it from comments and likes."

With entities defined, connect them with relationships:
1. **One-to-many (1:N)**: a user has many posts, a post has many comments.
2. **Many-to-many (N:M)**: users like many posts, posts are liked by many users.
3. **One-to-one (1:1)**: **rare** in practice, often a sign that **two tables should just be merged**.

These relationships are enforced through **foreign keys** in SQL (e.g., posts.user_id → users.id) or by application logic in NoSQL. 
1. Foreign keys help ensure referential integrity - meaning they prevent _orphaned_ records like a post referencing a user that doesn't exist, or comments pointing to deleted posts. 
2. However, they come at a cost because the database has to validate each insert/update. 
3. At very large scale, some companies drop them for write performance and enforce integrity at the application level. 
4. In an interview, mentioning them shows you understand the trade-off.

Finally, layer in **constraints** like _NOT NULL_, _UNIQUE_, or _CHECK_. 
1. These enforce correctness at the database level (emails must be unique, prices must be positive). 
2. They protect data quality, though they also add write overhead.

Keep your schema grounded in the problem domain: users, tweets, follows if you're modeling Twitter, not abstract "entities" and "relationships." Then show how keys, foreign keys, and constraints keep that model correct and scalable.


### Indexing for Access Patterns

### Normalization vs Denormalization

### Scaling and Sharding

## Conclusion




---------
Credit:
1. https://www.hellointerview.com/learn/system-design/core-concepts/data-modeling