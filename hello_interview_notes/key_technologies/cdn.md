# Ad hoc notes for CDN

## High level gists
1. Modern systems often serve users globally, which makes it challenging to deliver content quickly to users all over the world. 
   1. Users (and interviewers) expect fast load times, and delays can lead to a poor user experience and loss of traffic. 
   2. A content delivery network (CDN) is a type of cache that uses distributed servers to deliver content to users based on their geographic location. 
   3. CDNs are often used to deliver static content like images, videos, and HTML files, but they can also be used to deliver dynamic content like API responses.
2. They work by caching content on servers that are close to users. 
   1. When a user requests content, the CDN routes the request to the closest server. 
   2. If the content is cached on that server, the CDN will return the cached content. 
   3. If the content is not cached on that server, the CDN will fetch the content from the origin server, cache it on the server, and then return the content to the user.
3. The most common application of a CDN in an interview is to cache static media assets like images and videos. 
   1. For example, if you have a social media platform like Instagram, you might use a CDN to cache user profile pictures. 
   2. This would allow you to serve profile pictures quickly to users all over the world.


## Things you should know about CDNs
1. CDNs are not just for static assets. 
   1. While CDNs are often used to cache static assets like images, videos, and javascript files, they can also be used to cache dynamic content. 
   2. This is especially useful for content that is accessed frequently, but changes infrequently. 
   3. For example, a blog post that is updated once a day can be cached by a CDN.
2. CDNs can be used to cache API responses. 
   1. If you have an API that is accessed frequently, you can use a CDN to cache the responses. 
   2. This can help reduce the load on your servers and improve the performance of your API.
3. Eviction policies. 
   1. Like other caches, CDNs have eviction policies that determine when cached content is removed. 
   2. For example, you can set a time-to-live (TTL) for cached content, or you can use a cache invalidation mechanism to remove content from the cache when it changes.
4. Some of the most popular CDNs are **Cloudflare**, **Akamai**, and **Amazon CloudFront**. 
   1. These CDNs offer a range of features, including caching, DDoS protection, and web application firewalls. 
   2. They also have a global network of edge locations, which means that they can deliver content to users around the world with low latency.


------------
Credit:

1. https://www.hellointerview.com/learn/system-design/in-a-hurry/key-technologies#cdn
