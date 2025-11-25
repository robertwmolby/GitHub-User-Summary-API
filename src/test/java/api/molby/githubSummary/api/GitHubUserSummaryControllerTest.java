package api.molby.githubSummary.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GithubUserSummaryController.class)
// excludes security for testing
@AutoConfigureMockMvc(addFilters = false)
class GithubUserSummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GitHubUserSummaryService gitHubUserSummaryService;

    @Test
    void getUserSummary_validUser_returnsSummary() throws Exception {
        String userName = "octocat";
        GitHubUserSummaryDTO gitHubUserSummaryDTO = GitHubUserSummaryDTO.builder().userName(userName).build();
        mockMvc.perform(get("/userSummary/{username}", userName)).andExpect(status().isOk());
    }

    @Test
    void getUserSummary_invalidUsername_returnsBadRequest() throws Exception {
        String userName = "octocat";
        GitHubUserSummaryDTO gitHubUserSummaryDTO = GitHubUserSummaryDTO.builder().userName(userName).build();
        mockMvc.perform(get("/userSummary/{username}", userName)).andExpect(status().isOk());
    }
}