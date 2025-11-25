package api.molby.githubSummary.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

/**
 * Information about a github user. Many other properties are available but this
 * is only returning those necessary for our purposes.
 */
@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubUserDTO {

    private String login;
    private String name;
    @JsonProperty("avatar_url")
    private String avatarUrl;
    private String email;
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
    private String location;
    @JsonProperty("url")
    private String url;
}
