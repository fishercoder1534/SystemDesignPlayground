# Ad hoc notes for Proximity-Based Services

## High level gists
1. Several systems like Design Uber will require you to search for entities by location. 
   1. Geospatial indexes are the key to efficiently querying and retrieving entities based on geographical proximity. 
   2. These services often rely on extensions to commodity databases like PostgreSQL with PostGIS extensions or Redis' geospatial data type, or dedicated solutions like Elasticsearch with geo-queries enabled.
2. The architecture typically involves dividing the geographical area into manageable regions and indexing entities within these regions. 
   1. This allows the system to quickly exclude vast areas that don't contain relevant entities, thereby reducing the search space significantly.
3. While geospatial indexes are great, they're only really necessary when you need to index hundreds of thousands or millions of items. 
   1. If you need to search through a map of 1,000 items, you're better off scanning all of the items than the overhead of a purpose-built index or service.
