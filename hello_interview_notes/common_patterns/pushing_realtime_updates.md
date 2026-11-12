# Ad hoc notes for Pushing Realtime Updates

## High level gists
1. In many systems, you'll need to be able to make updates to the user in **real-time**. 
   1. For synchronous APIs, this is as simple as returning a response once the request is completed. 
   2. For other systems like chat applications, notifications, or live dashboards, you'll need to be able to push updates to the user as they happen.
2. There are a lot of decisions to make when implementing realtime updates. 
   1. First, you'll need to choose a protocol. 
   2. Simple HTTP polling is the simplest option, but it's not the most efficient. 
   3. Server-sent events (SSE) and websockets are purpose-built for realtime updates, but the infrastructure can be tricky to get right. 
   4. We generally recommend starting with HTTP polling until it no longer serves your needs. 
   5. Once you're there, you can consider SSE or websockets.
3. For the server side of realtime updates, you again have more options! 
   1. Pub/Sub services are a common way to decouple the publisher and subscriber (used in our Whatsapp breakdown), 
   2. while stateful servers in a consistent hash ring or other configuration can be used for situations where processing is heavier (used in our Google Docs breakdown).
4. More depth to be continued.. 