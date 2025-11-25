package api.molby.githubSummary.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Summary of github user information and the repositories associated with that user.
 * Response for API request.
 */
@Data
@Builder
@Schema(title = "Github Summary",
        description = "Summary information about a given user and a list of all their repositories.")
public class GitHubUserSummaryDTO {

    @Schema(title = "User Name", description = "Name used for Github Login", example = "octocat")
    private String userName;
    @Schema(title = "User Display Name", description = "Name displayed for the user in GitHub",
            example = "The Octocat")
    private String displayName;
    @Schema(title = "Avatar", description = "Link to the given users avatar image.",
            example = "https://avatars.githubusercontent.com/u/583231?v=4")
    private String avatar;
    @Schema(title = "Geographic Location", description = "Users location as specified in github.",
            example = "San Francisco")
    private String geoLocation;
    @Schema(title = "Email", description = "Users email address if specified.",
            example = "superman@metropolis.gov")
    private String email;
    @Schema(title = "API URL", description = "Link to Github API url for the given user.",
            example = "https://api.github.com/users/octocat")
    private String url;
    @Schema(title = "Creation Date/Time", description = "Timestamp of users creation",
            example = "Tue, 25 Jan 2011 18:44:36 GMT")
    @JsonFormat(pattern = "EEE, dd MMM yyyy HH:mm:ss 'GMT'", timezone = "GMT")
    private OffsetDateTime createdAt;
    @Schema(title = "User Repositories", description = "Users GitHub repositories")
    private List<GithubRepositoryResponseDTO> repos;
}
