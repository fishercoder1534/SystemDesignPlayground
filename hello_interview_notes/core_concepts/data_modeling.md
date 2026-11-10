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