# Ad hoc notes for Handling Large Blobs

## High level gists
1. Large files like videos, images, and documents need special handling in distributed systems. 
   1. Instead of routing gigabytes through your application servers, this pattern uses direct client-to-storage transfers with presigned URLs and CDN delivery.
2. Your application server generates temporary, scoped credentials (presigned URLs) that let clients upload directly to blob storage like S3. 
   1. Downloads come from CDNs with signed URLs for access control. 
   2. This eliminates your servers as bottlenecks while providing resumable uploads, progress tracking, and global distribution.
3. Key challenges include state synchronization between your database metadata and blob storage, handling upload failures, and managing the lifecycle of large files. 
   1. Event notifications from storage services help keep your application state consistent.


More to come...