# Ad hoc notes for Blob Storage

## High level gists
1. Sometimes you'll need to store large, unstructured blobs of data. 
   1. This could be images, videos, or other files. 
   2. Storing these large blobs in a traditional database is both expensive and inefficient and should be avoided when possible. 
   3. Instead, you should use a blob storage service like Amazon S3 or Google Cloud Storage. 
   4. These platforms are specifically designed for handling large blobs of data, and are much more cost-effective than a traditional database.
2. Blob storage services are simple. 
   1. You can upload a blob of data and that data is stored and get back a URL. 
   2. You can then use this URL to download the blob of data. 
   3. Often times blob storage services work in conjunction with CDNs, so you can get fast downloads from anywhere in the world. 
   4. Upload a file/blob to blob storage which will act as your origin, and then use a CDN to cache the file/blob in edge locations around the world.
3. Avoid using blob storage like S3 as your primary database unless you have a very good reason. 
   1. In a typical setup you will have a core database like Postgres or DynamoDB that has pointers (just a url) to the blobs stored in S3. 
   2. This allows you to use the database to query and index the data with very low latency, while still getting the benefits of cheap blob storage.
4. Here are some common examples of when to use blob storage:
   1. Design Youtube -> Store videos in blob storage, store metadata in a database.
   2. Design Instagram -> Store images & videos in blob storage, store metadata in a database.
   3. Design Dropbox -> Store files in blob storage, store metadata in a database.
5. To upload:
   1. When clients want to upload a file, they request a presigned URL from the server.
   2. The server returns a presigned URL to the client, recording it in the database.
   3. The client uploads the file to the presigned URL.
   4. The blob storage triggers a notification to the server that the upload is complete and the status is updated.
6. To download:
   1. The client requests a specific file from the server and are returned a presigned URL.
   2. The client uses the presigned URL to download the file via the CDN, which proxies the request to the underlying blob storage.

### Things you should know about blob storage
1. Durability: Blob storage services are designed to be incredibly durable. They use techniques like replication and erasure coding to ensure that your data is safe even if a disk or server fails.
2. Scalability: Hosted blob storage solutions like AWS S3 can be considered infinitely scalable. 
   1. They can store an unlimited amount of data and can handle an unlimited number of requests (obviously within the limits of your account). 
   2. As a result, in your interview, you don't need to explicitly consider the scalability of blob storage services -- consider this as a given.
3. Cost: Blob storage services are designed to be cost effective. 
   1. They are much cheaper than storing large blobs of data in a traditional database. 
   2. For example, AWS S3 charges $0.023 per GB per month for the first 50 TB of storage. 
   3. This is much cheaper than storing the same data in a database like DynamoDB, which charges $1.25 per GB per month for the first 10 TB of storage.
4. Security: Blob storage services have built-in security features like encryption at rest and in transit. 
   1. They also have access control features that allow you to control who can access your data.
5. Upload and Download Directly from the Client: Blob storage services allow you to upload and download blobs directly from the client. 
   1. This is useful for applications that need to store and retrieve large blobs of data, like images or videos. 
   2. Familiarize yourself with presigned URLs and how they can be used to grant temporary access to a blob -- either for upload or download.
6. Chunking: When uploading large files, it's common to use chunking to upload the file in smaller pieces. 
   1. This allows you to resume an upload if it fails partway through, and it also allows you to upload the file in parallel. 
   2. This is especially useful for large files, where uploading the entire file at once might take a long time. 
   3. Modern blob storage services like S3 support chunking out of the box via the multipart upload API.




--------
Credit:
1. https://www.hellointerview.com/learn/system-design/in-a-hurry/key-technologies

