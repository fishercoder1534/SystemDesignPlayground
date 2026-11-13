## Understanding the Problem

What is Bit.ly?

Bit.ly is a URL shortening service that converts long URLs into shorter, manageable links. It also provides analytics for the shortened URLs.

Designing a URL shortener is a very common beginner system design interview question. 

### Functional Requirements
The first thing you'll want to do when starting a system design interview is to get a clear understanding of the requirements of the system. 

Functional requirements are the features that the system must have to satisfy the needs of the user.
In some interviews, the interviewer will provide you with the core functional requirements upfront. In other cases, you'll need to determine these requirements yourself. 
If you're familiar with the product, this task should be relatively straightforward. However, if you're not, it's advisable to ask your interviewer some clarifying questions to gain a better understanding of the system.

The most important thing is that you zero in on the top **3-4 features** of the system and **don't get distracted** by the bells and whistles.

We'll concentrate on the following set of functional requirements:

Core Requirements
1. Users should be able to submit a long URL and receive a shortened version. 
   1. Optionally, users should be able to specify a custom alias for their shortened URL (ie. "www.short.ly/my-custom-alias")
   2. Optionally, users should be able to specify an expiration date for their shortened URL.
2. Users should be able to access the original URL by using the shortened URL.


Below the line (out of scope):
* User authentication and account management.
* Analytics on link clicks (e.g., click counts, geographic data).
  
These features are considered "below the line" because they add complexity to the system without being core to the basic functionality of a URL shortener. In a real interview, you might discuss these with your interviewer to determine if they should be included in your design.

### Non-Functional Requirements
1. Ensure uniqueness of short URLs;
2. Availability > Consistency;
2. low latency in redirects, under 100 ms;
3. Scalable to 1B urls and 100M DAU

## The Set Up

### Defining the Core Entities

### The API

## High-Level Design

### 1) Users should be able to submit a long URL and receive a shortened version

### 2) Users should be able to access the original URL by using the shortened URL

## Potential Deep Dives

### 1) How can we ensure short urls are unique?

### 2) How can we ensure that redirects are fast?

### 3) How can we scale to support 1B shortened urls and 100M DAU?

### Final Design

## What is Expected at Each Level?

### Mid-level

### Senior

### Staff+