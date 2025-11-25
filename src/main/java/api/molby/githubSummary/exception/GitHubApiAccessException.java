package api.molby.githubSummary.exception;

import org.springframework.web.client.RestClientResponseException;

/**
 * General exception that has occurred when attempting to access github api's.
 * Wraps a RestClientResponseException to make it explicit since RestClientResponseExxception is a RunTimeException
 */
public class GitHubApiAccessException extends Exception {

    private final RestClientResponseException rootCause;
    private final String userName;
    public GitHubApiAccessException(RestClientResponseException rootCause, String userName, String message) {
        super(message);
        this.rootCause = rootCause;
        this.userName = userName;
    }

    public RestClientResponseException getRootCause() {
        return rootCause;
    }

    public String getUserName() {
        return userName;
    }
}
