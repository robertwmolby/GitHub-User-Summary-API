package api.molby.githubSummary.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GithubUserSummaryController.class)
// excludes security for testing
@AutoConfigureMockMvc(addFilters = false)
class GithubUserSummaryControllerTest {

    private static final String USER_NAME = "test_login";
    private static final String DISPLAY_NAME = "Mr. Test Login";
    private static final String URL = "http://testlogin";
    private static final String EMAIL = "testlogin@test.org";
    private static final String AVATAR = "http://ilooklikeafrog";
    private static final String GEO_LOCATION = "Metropolis";
    private static final OffsetDateTime CREATED_AT = OffsetDateTime.of(
            2011, 1, 2,       // year, month, day
            3, 4, 5,        // hour, minute, second
            0,                 // nanos
            ZoneOffset.UTC     // offset
);
    private static final String REPO_NAME_1 = "Test Repo";
    private static final String REPO_URL_1  = "http://testurl";
    private static final String REPO_NAME_2 = "Test Repo 2";
    private static final String REPO_URL_2  = "http://testurl2";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GitHubUserSummaryService gitHubUserSummaryService;

    @Test
    void getUserSummary_validUser_returnsSummary() throws Exception {
        String userName = "octocat";

        GitHubUserSummaryDTO gitHubUserSummaryDTO = GitHubUserSummaryDTO.builder()
                .userName(USER_NAME)
                .displayName(DISPLAY_NAME)
                .avatar(AVATAR)
                .url(URL)
                .createdAt(CREATED_AT)
                .email(EMAIL)
                .geoLocation(GEO_LOCATION)
                .repos(
                        List.of(
                                GithubRepositoryResponseDTO.builder().name(REPO_NAME_1).url(REPO_URL_1).build(),
                                GithubRepositoryResponseDTO.builder().name(REPO_NAME_2).url(REPO_URL_2).build()
                        )
                )
                .build();
        when(gitHubUserSummaryService.fetchUserSummary(userName)).thenReturn(gitHubUserSummaryDTO);

        MvcResult result = mockMvc.perform(get("/userSummary/v1/{username}", userName))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        System.out.println(json);
        mockMvc.perform(get("/userSummary/v1/{username}", userName))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value(USER_NAME))
                .andExpect(jsonPath("$.displayName").value(DISPLAY_NAME))
                .andExpect(jsonPath("$.avatar").value(AVATAR))
                .andExpect(jsonPath("$.geoLocation").value(GEO_LOCATION))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.createdAt").value("Sun, 02 Jan 2011 03:04:05 GMT"))
                .andExpect(jsonPath("$.repos.length()").value(2))
                .andExpect(jsonPath("$.repos[0].name").value(REPO_NAME_1))
                .andExpect(jsonPath("$.repos[0].url").value(REPO_URL_1))
                .andExpect(jsonPath("$.repos[1].name").value(REPO_NAME_2))
                .andExpect(jsonPath("$.repos[1].url").value(REPO_URL_2));
    }

    @Test
    void getUserSummary_invalidUsername_returnsBadRequest() throws Exception {
        String userName = "~octocat";
        GitHubUserSummaryDTO gitHubUserSummaryDTO = GitHubUserSummaryDTO.builder().userName(userName).build();
        mockMvc.perform(get("/userSummary/v1/{username}", userName)).andExpect(status().isBadRequest());
    }
}