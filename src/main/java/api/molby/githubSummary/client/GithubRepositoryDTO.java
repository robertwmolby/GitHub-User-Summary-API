package api.molby.githubSummary.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;

/**
 * Information about a given github repository.  Just name and url.  Technically quite a bit
 * more information could be returned, but this is all we need for our purposes.
 */
@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubRepositoryDTO {

    private String name;
    private String url;
}
