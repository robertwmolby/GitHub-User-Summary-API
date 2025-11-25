package api.molby.githubSummary.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * Segment of response summary sent to user with repository details
 */
@Value
@Builder
@Schema(title = "Github Repository Response", description = "Information about a github repository.")
public class GithubRepositoryResponseDTO {

    @Schema(
        description="Name of the repository", name = "Name", example = "boysenberry-repo-1"
    )
    private String name;
    @Schema(
            description="Link for the api with full repository details.", name = "URL",
            example = "https://api.github.com/repos/octocat/boysenberry-repo-1"
    )
    private String url;
}
