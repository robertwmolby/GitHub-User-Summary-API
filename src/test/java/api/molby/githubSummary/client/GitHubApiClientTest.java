package api.molby.githubSummary.client;

import api.molby.githubSummary.exception.GitHubApiAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitHubApiClientTest {

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

    private GithubUserDTO githubUserDTO;
    private GithubRepositoryDTO githubRepositoryDTO1;
    private GithubRepositoryDTO githubRepositoryDTO2;
    private List<GithubRepositoryDTO> githubRepositoryDTOS;


    @Mock
    private RestClient gitHubRestClient;

    @Mock
    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private GitHubApiClient gitHubApiClient;

    @BeforeEach
    void setUp() {
        githubUserDTO = GithubUserDTO.builder()
                .login(LOGIN)
                .name(NAME)
                .url(URL)
                .email(EMAIL)
                .avatarUrl(AVATAR_URL)
                .createdAt(CREATED_AT)
                .location(LOCATION)
                .build();
        githubRepositoryDTO1 = GithubRepositoryDTO.builder().name(REPO_NAME_1).url(REPO_URL_1).build();
        githubRepositoryDTO2 = GithubRepositoryDTO.builder().name(REPO_NAME_2).url(REPO_URL_2).build();
        githubRepositoryDTOS = List.of(
                GithubRepositoryDTO.builder().name(REPO_NAME_1).url(REPO_URL_1).build(),
                GithubRepositoryDTO.builder().name(REPO_NAME_2).url(REPO_URL_2).build()
        );
    }

    // ----------------------------------------------------
    // fetchUser
    // ----------------------------------------------------

    @Test
    void fetchUser_success_returnsUserDto() throws Exception {
        when(gitHubRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(githubUserDTO);
        GithubUserDTO result = gitHubApiClient.fetchUser(LOGIN);
        assertEquals(githubUserDTO, result);
    }

    @Test
    void fetchUser_restClientResponseException_wrappedInGitHubApiAccessException() {
        RestClientResponseException restClientResponseException =
                new RestClientResponseException(
                        "Not Found",
                        HttpStatus.NOT_FOUND.value(),
                        "Not Found",
                        null,
                        null,
                        StandardCharsets.UTF_8
                );
        when(gitHubRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenThrow(restClientResponseException);
        GitHubApiAccessException gitHubApiAccessException = assertThrows(
                GitHubApiAccessException.class,
                () -> gitHubApiClient.fetchUser(LOGIN)
        );
        assertThat(gitHubApiAccessException.getMessage().equals("Not Found"));
    }

    @Test
    void fetchUserRepositories_singlePage_returnsAllRepos() throws Exception {
        // For repo calls, GitHubApiClient uses the uri(Function) variant
        when(gitHubRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        HttpHeaders headers = new HttpHeaders();
        // no link header.
        ResponseEntity<List<GithubRepositoryDTO>> entity =
                new ResponseEntity<>(githubRepositoryDTOS, headers, HttpStatus.OK);
        when(responseSpec.toEntity(any(ParameterizedTypeReference.class)))
                .thenReturn(entity);
        List<GithubRepositoryDTO> result =
                gitHubApiClient.fetchUserRepositories(LOGIN);
        assertThat(result).hasSize(2);
    }

    @Test
    void fetchUserRepositories_multiplePages_appendsAllRepos() throws Exception {
        // For repo calls, GitHubApiClient uses uri(Function)
        when(gitHubRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class)))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // First page: has Link header with rel="next"
        HttpHeaders headersPage1 = new HttpHeaders();
        headersPage1.add(HttpHeaders.LINK,
                "<https://api.github.com/users/testuser/repos?page=2>; rel=\"next\"");
        ResponseEntity<List<GithubRepositoryDTO>> entityPage1 =
                new ResponseEntity<>(List.of(githubRepositoryDTO1), headersPage1, HttpStatus.OK);

        // Second page: no next link
        HttpHeaders headersPage2 = new HttpHeaders();
        ResponseEntity<List<GithubRepositoryDTO>> entityPage2 =
                new ResponseEntity<>(List.of(githubRepositoryDTO2), headersPage2, HttpStatus.OK);

        // Sequence of calls to toEntity(...) as pages are fetched
        when(responseSpec.toEntity(any(ParameterizedTypeReference.class)))
                .thenReturn(entityPage1)
                .thenReturn(entityPage2);

        List<GithubRepositoryDTO> result =
                gitHubApiClient.fetchUserRepositories(LOGIN);

        assertThat(result).hasSize(2);
    }

    @Test
    void fetchUserRepositories_restClientResponseException_wrappedInGitHubApiAccessException() {
        RestClientResponseException restClientResponseException =
                new RestClientResponseException(
                        "Internal Server Error",
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        null,
                        null,
                        StandardCharsets.UTF_8
                );
        when(gitHubRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenThrow(restClientResponseException);
        GitHubApiAccessException gitHubApiAccessException = assertThrows(
                GitHubApiAccessException.class,
                () -> gitHubApiClient.fetchUserRepositories(LOGIN)
        );
        assertThat(gitHubApiAccessException.getMessage().equals("Internal Server Error"));
    }
}
