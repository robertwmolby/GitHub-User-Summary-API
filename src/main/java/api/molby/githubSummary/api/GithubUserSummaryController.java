package api.molby.githubSummary.api;

import api.molby.githubSummary.exception.GitHubApiAccessException;
import api.molby.githubSummary.exception.GitHubUserNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for github user summary endpoints.
 */
@RestController
@RequestMapping("/userSummary/v1")
@Validated
@Tag(
        name = "GitHub User Summary",
        description = "Endpoints for fetching GitHub user detail and repository information and summarizing it."
)
public class GithubUserSummaryController {

    private static Logger log = LoggerFactory.getLogger(GithubUserSummaryController.class);

    private final GitHubUserSummaryService gitHubUserSummaryService;

    public GithubUserSummaryController(GitHubUserSummaryService gitHubUserSummaryService) {
        this.gitHubUserSummaryService = gitHubUserSummaryService;
    }

    @Operation(
            summary="Access github summary information for specified user.",
            description = "Provides a api to provide both user details and " +
                    "a list of user repositories for a provided github user. Note that " +
                    "validation on user format is made prior to execution of request."
    )
    @GetMapping("/{username}")
    public GitHubUserSummaryDTO getUserSummary(
            @Parameter(
                description="Github user name",
                required = true,
                example="octocat")
            @Pattern(
                // regular expression for github usernames
                regexp = "^[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,37}[a-zA-Z0-9])?$",
                message = "Username provided was invalid."
            )
            @PathVariable
            String username)
            throws GitHubApiAccessException, GitHubUserNotFoundException {
        log.debug("Received github summary API request for user {}.", username);
        GitHubUserSummaryDTO gitHubUserSummaryDTO = gitHubUserSummaryService.fetchUserSummary(username);
        log.debug("Returning github summary response for user {}.", username);
        return gitHubUserSummaryDTO;
    }


}
