# Ad hoc notes for API Design

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
3. 