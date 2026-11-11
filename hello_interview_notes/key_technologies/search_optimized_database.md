# Ad hoc notes for Search Optimized Database

## High level gists
1. Sometimes you're tasked with implementing **full-text** search as a feature of your design. 
   1. Full-text search is the ability to search through a large amount of text data and find relevant results. 
   2. This is different from a traditional database query, which is usually based on exact matches or ranges. 
   3. Without a search optimized database, you would need to run a query that looks something like this:
   ```sql
   SELECT * FROM documents WHERE document_text LIKE '%search_term%'
   ```
     * This query is slow and inefficient, and it doesn't scale well because it requires a full table scan. 
     * That means the database has to grab each record and test it against your predicate rather than relying on an index or lookup. Slow!
2. Search optimized databases, on the other hand, are specifically designed to handle full-text search. 
   1. They use techniques like indexing, tokenization, and stemming to make search queries fast and efficient. 
   2. In short, they work by building what are called inverted indexes. 
   3. Inverted indexes are a data structure that maps from words to the documents that contain them. 
   4. This allows you to quickly find documents that contain a given word. 
   5. A simple example of an inverted index might look like this:
   ```json
    {
    "word1": [doc1, doc2, doc3],
    "word2": [doc2, doc3, doc4],
    "word3": [doc1, doc3, doc4]
    }
    ```
   * Now, instead of scanning the entire table, the database can quickly look up the word in the query and find all the matching documents. Fast!
   * Examples of search optimized databases are straightforward, consider an application like Ticketmaster that needs to search through a large number of events to find relevant results. 
   * Or a social media platform like Twitter that needs to search through a large number of tweets to find relevant results. 
   * In either case, a search optimized database would be an optimal choice.


## Things you should know about search optimized databases
1. **Inverted Indexes**: As just mentioned, search optimized databases use inverted indexes to make search queries fast and efficient. 
   1. An inverted index is a data structure that maps from words to the documents that contain them. 
   2. This allows you to quickly find documents that contain a given word.
2. **Tokenization**: Tokenization is the process of breaking a piece of text into individual words. 
   1. This allows you to map from words to documents in the inverted index.
3. **Stemming**: Stemming is the process of reducing words to their root form. 
   1. This allows you to match different forms of the same word. 
   2. For example, "running" and "runs" would both be reduced to "run".
4. **Fuzzy Search**: Fuzzy search is the ability to find results that are similar to a given search term. 
   1. Most search optimized databases support fuzzy search out of the box as a configuration option. 
   2. In short, this works by using algorithms that can tolerate slight misspellings or variations in the search term. 
   3. This is achieved through techniques like edit distance calculation, which measures how many letters need to be changed, added, or removed to transform one word into another.
5. **Scaling**: Just like traditional databases, search optimized databases scale by adding more nodes to a cluster and sharding data across those nodes.



### Examples of search optimized databases
1. The clear leader in this space is Elasticsearch. 
   1. In short: Elasticsearch is a distributed, RESTful search and analytics engine that is built on top of Apache Lucene. 
   2. It is designed to be fast, scalable, and easy to use, and is the most popular search optimized database and is used by companies like Netflix, Uber, and Yelp.