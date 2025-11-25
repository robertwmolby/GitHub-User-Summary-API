package api.molby.githubSummary.api;

import api.molby.githubSummary.client.GitHubApiClient;
import api.molby.githubSummary.exception.GitHubApiAccessException;
import api.molby.githubSummary.client.GithubUserDTO;
import api.molby.githubSummary.client.GithubRepositoryDTO;
import api.molby.githubSummary.exception.GitHubUserNotFoundException;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import java.util.List;

/**
 * Service class for github user summary api.
 */
@Service
public class GitHubUserSummaryService {

    private static Logger log = org.slf4j.LoggerFactory.getLogger(GitHubUserSummaryService.class);

    private final GitHubUserSummaryCache gitHubUserSummaryCache;
    private final GitHubApiClient gitHubApiClient;

    public GitHubUserSummaryService(GitHubUserSummaryCache gitHubUserSummaryCache,
                                    GitHubApiClient gitHubApiClient) {
        this.gitHubUserSummaryCache = gitHubUserSummaryCache;
        this.gitHubApiClient = gitHubApiClient;
    }

    public GitHubUserSummaryDTO fetchUserSummary(String username) throws GitHubApiAccessException, GitHubUserNotFoundException {
        try {
            // convert username to lowercase for handling.  github itself is case insensitive but
            // this ensures consistency for caching and any other handling on this side of things
            username = username.toLowerCase();
            GithubUserDTO gitHubUserDTO = gitHubApiClient.fetchUser(username);
            List<GithubRepositoryDTO> gitHubRepositories = gitHubApiClient.fetchUserRepositories(username);
            GitHubUserSummaryDTO gitHubUserSummaryDTO = buildSummary(gitHubUserDTO, gitHubRepositories);
            // cache request in case it is needed later as a fallback
            gitHubUserSummaryCache.cacheResponse(username, gitHubUserSummaryDTO);
            return gitHubUserSummaryDTO;
        }
        catch (GitHubApiAccessException e) {
            RestClientResponseException restClientResponseException = e.getRootCause();
            if (restClientResponseException.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new GitHubUserNotFoundException(username);
            }
            log.warn("Error accessing github api for user {}.  message: {}. Attempting to fall back to " +
                    "cached version of response.", username, e.getMessage());
            GitHubUserSummaryDTO gitHubUserSummaryDTO = gitHubUserSummaryCache.getResponseFromCache(username);
            if (gitHubUserSummaryDTO != null) {
                log.warn("Returning cached response for user {}.", username);
                return gitHubUserSummaryDTO;
            }
            else {
                log.warn("Cached response not found for user {}.", username);
                throw e;
            }

        }
    }

    /**
     * Create a github summary response using previously fetched user and repositories infomration
     * @param githubUserDTO DTO with information about user
     * @param githubRepositoryDTOS DTO with list of repositories for user
     * @return Summary response object for return to client.
     */
    private GitHubUserSummaryDTO buildSummary(GithubUserDTO githubUserDTO, List<GithubRepositoryDTO> githubRepositoryDTOS) {
        return GitHubUserSummaryDTO.builder()
                .userName(githubUserDTO.getLogin())
                .displayName(githubUserDTO.getName())
                .email(githubUserDTO.getEmail())
                .url(githubUserDTO.getUrl())
                .avatar(githubUserDTO.getAvatarUrl())
                .email(githubUserDTO.getEmail())
                .geoLocation(githubUserDTO.getLocation())
                .createdAt(githubUserDTO.getCreatedAt())
                .repos(
                    githubRepositoryDTOS.stream().map(r ->
                        GithubRepositoryResponseDTO.builder()
                            .name(r.getName())
                            .url(r.getUrl())
                            .build())
                            .toList()
                )
                .build();
    }

}
