# Ad hoc notes for API Gateway

## High level gists
1. Especially in a microservice architecture, an API gateway sits in front of your system and is responsible for routing incoming requests to the appropriate backend service. 
   1. For example, if the system receives a request to GET /users/123, the API gateway would route that request to the users service and return the response to the client. 
   2. The gateway is also typically responsible for handling cross-cutting concerns like authentication, rate limiting, and logging.
2. In nearly all product design style system design interviews, it is a good idea to include an API gateway in your design as the first point of contact for your clients.
3. What are the most common API gateways?
   1. The most common API gateways are **AWS API Gateway**, Kong, and Apigee. 
   2. It's also not uncommon to have an **nginx** or **Apache webserver** as your API gateway (in the early days of Amazon, a gigantic fleet of Apache webservers served this purpose).