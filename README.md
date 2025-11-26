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

## Response Shape and Field Mapping

In order to conform to the desired response format, the fields needed to be mapped.  Mapping is as follows (github field -> result field):
- login -> userName
- name -> displayName
- afatar_url -> avatar
- location -> geoLocation
- url -> url
- created_at -> createdAt
  - Created at date format also needed to be changed from
    - GitHub format using ISO-8601 timestamp (e.g. 2011-01-25T18:44:36Z).  
  - To
    - Format "Tue, 25 Jan 2011 18:44:36 GMT"
  - This is done by using an OffsetDateTime in conjunction with formatters
- repos[] is derived from the user’s repository list, exposing only name and the GitHub repo url.

This mapping logic is encapsulated in DTOs and mapping code so the controller layer stays clean.

Note that only fields that are required in response are mapped inside of the GitHub access dtos.  There are a large number of other fields returned by the github api that are not needed for this application.

## Design

This is a Spring Boot application exposing a single RESTful endpoint:
- UserSummaryController
  - Exposes a versioned endpoint: GET /api/v1/users/{username}.
    - Versioning used to ensure backwards compatible.  Path versioning selected here although other options could be considered.
  - Validates the incoming username using a regular expression Bean Validation.  This expression uses GitHub username rules and avoids injection patterns.
  - Delegates to UserSummaryService.
- UserSummaryService
  - Delegates calls for github information to GitHubApiClient.
  - Performs mapping and formatting
  - If github access works
    - Aggregates user and repository information into a single summary DTO.
    - Stores resultant dto to cache for potential later usage.
    - Returns summary dto
  - If github access fails
    - Attempts to fetch summary dto from GitHubUserSummaryCache.  If it exists, it is returned.  If not, a GithubApiAccessException is thrown (handled by GlobalExceptionHandler)  
- GitHubApiClient
  - Uses Spring’s RestClient to call GitHub’s REST API.
  - Handles both the user and repository endpoints.
  - Implements pagination for repository retrieval to ensure all repos are returned.  GitHub APIS are paginated by default with a default page size of 30.  This ensures that if the number of repositories exceeds this that they will still all be returned.
  - Wraps remote call failures in custom exceptions that are handled via a global exception handler.

#### Additional cross-cutting concerns:

- Exception handling
  - A @ControllerAdvice layer converts known exceptions (e.g., user not found, downstream failures) into structured JSON error responses with appropriate HTTP status codes.
- Logging
  - Request/response start/stop and paths and error paths are logged for debugging purposes
  - Issues resulting from user request issues are treated as info level
  - Issues resulting from failure to access github are logged as wares with detail about whether or not cache fall back was successful.
- Actuator endpoints
  - Exposed on an alternate port.  This will make securing them easier.

## Defensive Coding & Error Handling
The service is designed to behave predictably for bad input and upstream issues:
- Username validation
  - @Valid and regex ensure usernames align with GitHub username rules. This also serves the dual purpose of preventing injection attacks.
- Error handling
  - User not found (404)
    - 404's received from Github are encapsulated in custom exception GitHubUserNotFoundException.
    - The controller advice maps this exception to a custom 404 response
  - GitHub failures
    - In the event of a failure accessing github where cache fall back also failed, the service will use a custom excption GitHubApiAccessException.  This is handled by the global exception handler as follows:
      - Returns a suitable error response without any sensitive data.      - 
    - Generic unexpected failures
      - Logged and returned as 500 respons with generic body having only non-sensitive data.
  - All logging of errors is handled within the global exception handler.

## Assumptions
The assumptions for this appliction are as follows:
- Although nothing will initially be secured, the application should be ready for security if it needs to be added at a later point
- The application will be deployed as a container in an orchestration environment such as Kubernetes
- Documentation provided will be in-line (java doc plus swagger/open ai)
- The application will not need to scale beyond a point where a non-blocking implementation is needed
- The application is built in a "silo" as far as techology decisions are concerned.  In general, specific libraries and design aspects used would be dependent on common libraries used within the organization as a whole.
- Application will be built in SpringBoot 3+ and Java 21

## Libraries & Framework Choices
### Spring Boot
- spring-boot-starter-web
  - For the REST endpoint and MVC infrastructure.
- RestClient
  - Simple, modern HTTP client for calling GitHub. Chosen over WebClient because non-blocking I/O is not required here.  An alternative would be Feign but this was not used in order to avoid additional Spring Clould dependencies.
- spring-boot-starter-validation 
  - Used for request validation
- spring-boot-starter-actuator
  - Health and readiness checks on a separate port.  Only exposing health and info points for container monitoring. Additional endpoints could be added for use in monitoring by tools such as Prometheus.
- springdoc-openapi
  - Auto-generates OpenAPI/Swagger docs for the endpoint.
- spring-boot-starter-test
  - Spring Boot testing library

### Non-Spring libraries
- Lombok
  - Reduces boilerplate for DTOs and configuration objects. Used
    - @Value for immutable objects
    - @Builder for simplicity in constructing dtos throughout application
  - Java records could also be used but lacked the optional Builder pattern.  Java records were still used however for a few inner classes.
- Caffeine
  - Simple, efficient in-memory cache implementation.
- JUnit 5
  - Primary test framework.
- Mockito
  - For mocking any objects needed in unit tests.

## Package Structure
The package layout is domain-oriented, with cross-cutting concerns separated:
- api.molby.githubSummary.api – classes related directly to consuming request and creating response objects.  This includes the controller, service, cache, and associated dtos.
- api.molby.githubSummary.client – classes associated to any calls made to GitHub.  This includes the actual GitHub API client and associated dto objects.
- api.molby.githubSummary.exception – Custom exceptions and global exception handler
- api.molby.githubSummary.config – Configuration (cache, security, openapi, restclient)


## Startup & Running Locally
### Prerequisites
- Java 21
- Gradle.  Used instead of Maven for simplicity.
- No spring profiles are used, single application.yml is included wit basic defaults.  Additional values for things like github url use @Value annotations to allow overrides while having a default.
- 
### Running the application
From the project root:
```
   ./gradlew bootRun
```

By default, the application starts on:
- App port: 8080
- Actuator port: 9080 (health/info only)

### Example request
```
curl "http://localhost:8080/api/v1/users/octocat"
```

### Example response
```
{
    "user_name": "octocat",
    "display_name": "The Octocat",
    "avatar": "https://avatars.githubusercontent.com/u/583231?v=4",
    "geo_location": "San Francisco",
    "email": null,
    "url": "https://api.github.com/users/octocat",
    "created_at": "Tue, 25 Jan 2011 18:44:36 GMT",
    "repos": [
        {
            "name": "boysenberry-repo-1",
            "url": "https://api.github.com/repos/octocat/boysenberry-repo-1"
        },
        ...
    ]
}
```

## Testing
Testing
The following test classes are included:
- GitHubUserSummaryControllerTest
  - @WebMvcTest of the controller, with the service mocked.
  - Verifies:
    - Successful request returns the expected JSON structure.
    - Invalid usernames are rejected at the validation layer.
- GitHubUserSummaryServiceTest
  - Tests orchestration logic and mapping from GitHub DTOs to the summary DTO.
  - Covers:
    - Successful request
    - Unknown user (404)
    - Github API failure with successful cache fallback.
    - Github API failure with unsuccessful cache fallback.
- GitHubApiClientTest
  - Covers
    - Successfull request of both user
    - Fetch of git hub user that does not exist
    - Successful request of repositories where all repositories are in the firat page
    - Successful request of repositories where multiple page requests are needed.
    - Failed user repository request

Boilerplate classes (simple configuration and DTOs) are not unit tested.

To run the tests:
```
./gradlew test
```

A Postman collection (GitHubUserSummaryAPI.postman_collection.json) and environment file are available under the /postman folder.


