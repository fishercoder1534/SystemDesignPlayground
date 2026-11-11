# Ad hoc notes for Core Databases

## High level gists
1. Almost all system design problems will require you to store some data and you're most likely going to be storing it in a database (or Blob Storage). 
   1. While there are many different types of databases, the most common are relational databases (e.g. Postgres) and NoSQL databases (e.g. DynamoDB) - we recommend you pick one of these for your interview. 
   2. If you are taking predominantly product design interviews, we recommend you pick a relational database. 
   3. If you are taking predominantly infrastructure design interviews, we recommend you pick a NoSQL database.
2. Many candidates trip themselves up by trying to insert a comparison of relational and NoSQL databases into their answer. 
   1. The reality is that these two technologies are **highly overlapping** and broad statements like "I need to use a relational database because I have relationships in my data" (NoSQL databases can work great for this) or "I've gotta use NoSQL because I need scale and performance" (relational databases, used correctly, perform and scale incredibly well) are often yellow flags that reveal inexperience. 
   2. Here's the truth: most interviewers don't need an explicit comparison of SQL and NoSQL databases in your session and it's a pothole you should completely avoid. 
   3. Instead, talk about what you know about the database you're using and how it will help you solve the problem at hand. 
   4. If you're asked to compare, focus on the **differences** in the databases you're familiar with and how they would impact your design. 
   5. So "I'm using Postgres here because its ACID properties will allow me to maintain data integrity" is a great opener.


## Relational Databases
1. Relational databases (sometimes called RDBMS or Relational Database Management Systems) are the most common type of database. 
   1. They're often used for transactional data (e.g. user records, order records, etc) and are typically the default choice for a product design interview. 
   2. Relational databases store your data in tables, which are composed of rows and columns. Each row represents a single record, and each column represents a single field on that record. 
   3. For example, a users table might have a name column and an email column. 
   4. Relational databases are often queried using SQL, a declarative language for querying data.
2. Things you should know about relational databases 
   1. Beyond simply storing data, relational databases come equipped with several features which are useful for system design interviews. The most important of these are:
      1. **SQL Joins**: Joins are a way of combining data from multiple tables. 
         1. For example, if you have a users table and a posts table, you might want to query for all posts by a particular user. This is important for querying data and SQL databases can support arbitrary joins between tables. Note that joins can be also be a major performance bottleneck in your system so minimize them where possible.
      2. **Indexes**: Indexes are a way of storing data in a way that makes it faster to query. 
         1. For example, if you have a users table with a name column, you might create an index on the name column. This would allow you to query for users by name much faster than if you didn't have an index. 
         2. Indexes are often implemented using a B-Tree or a Hash Table. The great thing about relational databases is (a) their support for **arbitrarily many indexes**, which allows you to optimize for different queries and (b) their support for **multi-column and specialized indexes** (e.g. geospatial indexes, full-text indexes).
      3. RDBMS **Transactions**: Transactions are a way of **grouping multiple operations together into a single atomic operation**. 
         1. For example, if you have a users table and a posts table, you might want to create a new user and a new post for that user at the same time. 
         2. If you do this in a transaction, either both operations will succeed or both will fail. This ensures you don't have invalid data like a post from a user who doesn't exist!
3. The most common relational databases are Postgres and MySQL. If you don't already have a favorite, we recommend you pick Postgres, but either one is fine.

## NoSQL Databases