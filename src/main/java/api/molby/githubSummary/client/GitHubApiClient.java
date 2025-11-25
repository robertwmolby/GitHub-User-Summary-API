package api.molby.githubSummary.client;

import api.molby.githubSummary.exception.GitHubApiAccessException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Client for calling GitHub APIs used by the GitHubUserSummary service.
 */
@Component
public class GitHubApiClient {

    private final RestClient gitHubRestClient;

    public GitHubApiClient(RestClient gitHubRestClient) {
        this.gitHubRestClient = gitHubRestClient;
    }

    /**
     * Construct a GitHubUserDTO by making a call to a given github api.
     * @param userName User to fetch details on.
     * @return GitHubUserDTO with information provided by github api.
     */
    public GithubUserDTO fetchUser(String userName) throws GitHubApiAccessException {
        GithubUserDTO githubUserDTO = null;
        try {
            githubUserDTO = gitHubRestClient.get()
                    .uri("/users/{username}", userName)
                    .retrieve()
                    .body(new ParameterizedTypeReference<GithubUserDTO>() {
                    });
        }
        catch (RestClientResponseException re) {
            throw new GitHubApiAccessException(re, userName, re.getMessage());
        }
        return githubUserDTO;
    }

    /**
     * Return a list of github repositories for a given user.  Note that this
     * takes into consideration the possibility of multiple pages of results in the
     * github repository api results.  This is unlikely in most situation since the default page
     * size is 100, but could still technically occur.
     * @param userName Name of user to fetch repositories for.
     * @return List of repositories for the given user.
     */
    public List<GithubRepositoryDTO> fetchUserRepositories(String userName) throws GitHubApiAccessException {
        // github user repositories can technically have multiple pages...
        List<GithubRepositoryDTO> githubRepositoryDTOS = new ArrayList<>();
        int pageNumber = 1;
        boolean morePages = true;
        try {
            while (morePages) {
                GithubUserRepositoryPage githubUserRepositoryPage = fetchRepositoryPage(userName, pageNumber++);
                githubRepositoryDTOS.addAll(githubUserRepositoryPage.githubRepositoryDTOS());
                morePages = githubUserRepositoryPage.hasNextPage();
            }
        }
        catch (RestClientResponseException e) {
            throw new GitHubApiAccessException(e, userName, e.getMessage());
        }
        return githubRepositoryDTOS;
    }

    /**
     * Fetch a given page number of repositories for a given user.
     * param username github user name
     * @param pageNumber page number to fetch
     * @return boolean indicating if more pages exists
     */
    private GithubUserRepositoryPage fetchRepositoryPage(String username, int pageNumber) {
        boolean nextPageExists = false;
        ResponseEntity<List<GithubRepositoryDTO>> githubUserRepositoryEntity = gitHubRestClient.get()
                .uri(
                        uriBuilder -> uriBuilder
                                .path("/users/{username}/repos")
                                .queryParam("sort","name")
                                .queryParam("page", pageNumber)
                                .build(username)
                )
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<GithubRepositoryDTO>>() {
                });
        List<GithubRepositoryDTO> githubRepositoryDTOS = githubUserRepositoryEntity.getBody();
        String linkHeader = githubUserRepositoryEntity.getHeaders().getFirst(HttpHeaders.LINK);
        if (Strings.isNotEmpty(linkHeader)) {
            String[] linkHeaderSegments = linkHeader.split(",");
            for (String linkHeaderSegment : linkHeaderSegments) {
                String linkRelation = linkHeaderSegment.split(";")[1].trim().split("=")[1].trim();
                // link relation may possibly include trailing/leading quotes in actual string so just look for keyword next
                if (linkRelation.contains("next")) {
                    nextPageExists = true;
                    break;
                }
            }
        }
        return new GithubUserRepositoryPage(githubRepositoryDTOS, nextPageExists);
    }

    /**
     * Inner record to store result for fetchRepositoryPage and avoid
     * a mutating signature.
     * @param githubRepositoryDTOS Repository dtos fetched for a page
     * @param hasNextPage boolean indicating if another page of results exist in github
     */
    private record GithubUserRepositoryPage(
            List<GithubRepositoryDTO> githubRepositoryDTOS,
            boolean hasNextPage
    ) {}
}
