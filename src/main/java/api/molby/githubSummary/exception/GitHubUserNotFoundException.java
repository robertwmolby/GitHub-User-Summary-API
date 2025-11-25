package api.molby.githubSummary.exception;

/**
 * Exception thrown if a user was not found in GitHub
 */
public class GitHubUserNotFoundException extends Exception{

    private final String userName;

    public GitHubUserNotFoundException(String userName) {
        super("User not found in GitHub: " + userName);
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}
