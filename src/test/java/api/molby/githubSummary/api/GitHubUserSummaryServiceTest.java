package api.molby.githubSummary.api;

import api.molby.githubSummary.client.GitHubApiClient;
import api.molby.githubSummary.client.GithubRepositoryDTO;
import api.molby.githubSummary.client.GithubUserDTO;
import api.molby.githubSummary.exception.GitHubApiAccessException;
import api.molby.githubSummary.exception.GitHubUserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientResponseException;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitHubUserSummaryServiceTest {

    private static final String LOGIN = "test_login";
    private static final String NAME = "Mr. Test Login";
    private static final String URL = "http://testlogin";
    private static final String EMAIL = "testlogin@test.org";
    private static final String AVATAR_URL = "http://ilooklikeafrog";
    private static final String LOCATION = "Metropolis";
    private static final OffsetDateTime CREATED_AT = OffsetDateTime.now();
    private static final String REPO_NAME_1 = "Test Repo";
    private static final String REPO_URL_1  = "http://testurl";
    private static final String REPO_NAME_2 = "Test Repo 2";
    private static final String REPO_URL_2  = "http://testurl2";


    @Mock
    private GitHubUserSummaryCache gitHubUserSummaryCache;

    @Mock
    private GitHubApiClient gitHubApiClient;

    private GitHubUserSummaryService gitHubUserSummaryService;
    private GithubUserDTO githubUserDTO;
    private List<GithubRepositoryDTO> githubRepositoryDTOS;
    private GitHubUserSummaryDTO gitHubUserSummaryDTO;

    @BeforeEach
    void setUp() {
        gitHubUserSummaryService = new GitHubUserSummaryService(gitHubUserSummaryCache, gitHubApiClient);
        githubUserDTO = GithubUserDTO.builder()
                .login(LOGIN)
                .name(NAME)
                .url(URL)
                .email(EMAIL)
                .avatarUrl(AVATAR_URL)
                .createdAt(CREATED_AT)
                .location(LOCATION)
                .build();
        githubRepositoryDTOS = List.of(
                GithubRepositoryDTO.builder().name(REPO_NAME_1).url(REPO_URL_1).build(),
                GithubRepositoryDTO.builder().name(REPO_NAME_2).url(REPO_URL_2).build()
        );
        gitHubUserSummaryDTO = GitHubUserSummaryDTO.builder()
                .userName(LOGIN)
                .displayName(NAME)
                .avatar(AVATAR_URL)
                .url(URL)
                .createdAt(CREATED_AT)
                .email(EMAIL)
                .geoLocation(LOCATION)
                .repos(
                        List.of(
                                GithubRepositoryResponseDTO.builder().name(REPO_NAME_1).url(REPO_URL_1).build(),
                                GithubRepositoryResponseDTO.builder().name(REPO_NAME_2).url(REPO_URL_2).build()
                        )
                )
                .build();
    }

    @Test
    void getUserSummary_validUser_returnsSummary() throws Exception {
        when(gitHubApiClient.fetchUser(LOGIN)).thenReturn(githubUserDTO);
        when(gitHubApiClient.fetchUserRepositories(LOGIN)).thenReturn(githubRepositoryDTOS);
        GitHubUserSummaryDTO actualGitHubUserSummaryDTO = gitHubUserSummaryService.fetchUserSummary(LOGIN);
        assertEquals(gitHubUserSummaryDTO,actualGitHubUserSummaryDTO);
    }

    @Test
    void getUserSummary_userNotFound_throwsException() throws Exception {
        RestClientResponseException restClientResponseException =
            new RestClientResponseException(
                    "Not Found",   // message
                    404,           // statusCode
                    "Not Found",   // statusText
                    null,          // headers
                    null,          // responseBody
                    null           // charset
            );
        GitHubApiAccessException gitHubApiAccessException = new GitHubApiAccessException(restClientResponseException, LOGIN, "unknown user");
        when(gitHubApiClient.fetchUser(LOGIN)).thenThrow(gitHubApiAccessException);
        assertThrows(GitHubUserNotFoundException.class, () -> gitHubUserSummaryService.fetchUserSummary(LOGIN));
    }

    @Test
    void getUserSummary_apiError_fallsBackToCache() throws Exception {
        RestClientResponseException restClientResponseException =
                new RestClientResponseException(
                        "Error",   // message
                        500,           // statusCode
                        "Error",   // statusText
                        null,          // headers
                        null,          // responseBody
                        null           // charset
                );
        GitHubApiAccessException gitHubApiAccessException = new GitHubApiAccessException(
                restClientResponseException, LOGIN, "Error accessing github api"
        );
        when(gitHubApiClient.fetchUser(LOGIN)).thenThrow(gitHubApiAccessException);
        when(gitHubUserSummaryCache.getResponseFromCache(LOGIN)).thenReturn(gitHubUserSummaryDTO);
        GitHubUserSummaryDTO actualGitHubUserSummaryDTO = gitHubUserSummaryService.fetchUserSummary(LOGIN);
        assertEquals(gitHubUserSummaryDTO,actualGitHubUserSummaryDTO);
    }

    @Test
    void getUserSummary_apiErrorAndCacheMiss_throwsException() throws Exception {
        RestClientResponseException restClientResponseException =
                new RestClientResponseException(
                        "Error",   // message
                        500,           // statusCode
                        "Error",   // statusText
                        null,          // headers
                        null,          // responseBody
                        null           // charset
                );
        GitHubApiAccessException gitHubApiAccessException = new GitHubApiAccessException(
                restClientResponseException, LOGIN, "Error accessing github api"
        );
        when(gitHubApiClient.fetchUser(LOGIN)).thenThrow(gitHubApiAccessException);
        when(gitHubUserSummaryCache.getResponseFromCache(LOGIN)).thenReturn(null);
        assertThrows(GitHubApiAccessException.class, () -> gitHubUserSummaryService.fetchUserSummary(LOGIN));
    }
}