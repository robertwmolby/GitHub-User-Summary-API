# GitHub User Summary API

## Overview
This is an endpoint designed to consolidate 2 public github endpoints that return user details and user repository list information. These github endpoints are:
- https://api.github.com/users/octocat
- https://api.github.com/users/octocat/repos

The summary of these endpoints can be found here:  https://docs.github.com/en/rest/using-the-rest-api/getting-started-with-the-rest-api?apiVersion=2022-11-28

## High Level Design Considerations
The driving design considerations when deciding between libraries and components are as follows:
- Flow Simplicity
- Well documented leveraging inline documentation (javadocs and swagger/open ai)
- Monitoring for the purpose of health checks and performance in a containerized environment.
- Security of the application. Security configuration has been setup but is wide open at this time.  Actuator endpoints have been placed on alternate ports in order to make securing them easier. In addition, usenames in the path are filtered via regular expressions to ensure they both match GitHub username requirements and do not contain any potential injection attacks.

## Design
This is a spring boot application with a single restful endpoint.  Core classes within it:
- UserSummaryController.java - handles the restful endpoint
- UserSummaryService.java - handles the business logic for the endpoint.  Calls GitHubApiClient to fetch data.  Falls back to GitHubUserSummaryCache contents if an exception occurs while attempting to fetch the information via the GitHubApiClient.
- GitHubApiClient.java - handles the API calls to github.  Uses RestClient.  Note that logic exists in this for repository fetches to address that GitHub technically does page on these requests and could return multiple pages of results.

Beyond simply the general springboot/service handler, the following is included:
- Exception handling
- Logging
- Actuator endpoints
- Swagger/Open AI documentation

## Assumptions
The assumptions for this appliction are as follows:
- Although nothing will initially be secured, the application should be ready for security if it needs to be added at a later point
- The application will be deployed as a container in an orchestration environment such as Kubernetes
- Documentation provided will be in-line (java doc plus swagger/open ai)
- The application will not need to scale beyond a point where a non-blocking implementation is needed
- The application is built in a "silo" as far as techology decisions are concerned.  In general, specific libraries and design aspects used would be dependent on common libraries used within the organization as a whole.
- Application will be built in SpringBoot 3+ and Java 21

## Libraries
In general standard springboot libraries and classes are used.  Specifically:
### SpringBoot Libraries
- Spring Boot Web (basic api construction)
- RestClient (API fetches).  Used over WebClient since there is no need for non-blocking aspect and performance.  Other alternative (personal preferred) would be Feign but that does bring in Spring Cloud dependencies.
- Validation.  Used for controller request validation.
- Spring Boot Actuator - for monitoring and health checks.  Will be exposed on separate port than the main application port in order to make securing it easier.  Will only expose health check endpoints and not metrics (could easily open those open if needed for tools like Prometheus)
- Spring docs (for swagger/open ai documentation).
- Springboot test libraries

### Non-SpringBoot Libraries
- Lombok for pojo handling.  Given pojos in this library are immutable Java records could be used but Lombok provides some additional options if later needed
- Caffeine for caching.  Simple, in memory cache.  Does have limitations since it is in memory but unless there is a large nuymber of potential users in the request this is a non-issue.  Will configure time based eviction for purposes of this project.  
- Junit5 for unit tests
- Mockito for mocking during unit tests

## Structure
Although not heavily pertinent due to the small size, general package structure will be domain based with the exception of cross-cutting concerns.  

## Notes
- Just a standard single property file used in yaml format. Only infomration specified is that related to management endpoints and cache setup.
- Gradle is used versus Maven for simplicity.

## Startup
As a standard springboot application, startup is just a matter of starting the main @SpringBootApplication class.  In this case that is GitHubUserSummaryAPIApplication.java.  Once started by defaul the application will be available on port 8080 (actuator ports are on 9080).  At that point you can use api calls similar to those in the postman collection GitHubUserSummaryAPI.postman_collection.json.  The base url is "http://localhost:8080/api/v1/users/octocat
 This collection and environment file can be found in the "/postman" folder. 

## Testing
The following unit tests have been created:
- UserSummaryControllerTest.java
- UserSummaryServiceTest.java
- GitHubApiClientTest.java
Other classes are either general SpringBoot configuration classes or pojos and not being considered for unit testing at this point.
