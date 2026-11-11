# Ad hoc notes for Load Balancer

## High level gists
1. Most system design problems will require you to design a system that can handle a large amount of traffic. 
   1. When you have a large amount of traffic, you will need to distribute that traffic across multiple machines (called horizontal scaling) to avoid overloading any single machine or creating a hotspot. 
   2. This is where a load balancer comes in. 
   3. For the purposes of an interview, you can assume that your load balancer is a black box that will distribute work across your system.
2. The reality is that you need a load balancer wherever you have multiple machines capable of handling the same request. 
   1. However, in an interview, it can be redundant to draw a load balancer in front of every service. 
   2. Instead, either omit a load balancer from your design altogether (and just mention that the services are horizontally scaled) or add one only to the front of the design as an abstraction.
3. Note that sometimes you'll need to have specific features from your load balancer, like sticky sessions or persistent connections. 
   1. The most common decision to make is whether to use an L4 (layer 4) or L7 (layer 7) load balancer.
   2. You can somewhat shortcut this decision with a simple rule of thumb:
      1. if you have **persistent** connections like **websockets**, you'll likely want to use an L4 load balancer. 
      2. Otherwise, an L7 load balancer offers great flexibility in routing traffic to different services while minimizing the connection load downstream.
4. What are the most common load balancers? 
   1. AWS Elastic Load Balancer (a hosted offering from AWS), 
   2. NGINX (an open-source webserver frequently used as a load balancer), and 
   3. HAProxy (a popular open-source load balancer). 
   4. Note that for problems with extremely high traffic, specialized hardware load balancers will outperform software load balancers you'd host yourself - you'll quickly be pulled into the crazy world of network engineering.






----------
Credit:
1. https://www.hellointerview.com/learn/system-design/in-a-hurry/key-technologies