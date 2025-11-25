package api.molby.githubSummary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class GitHubSummaryApplication {

    public static void main(String[] args) {
        SpringApplication.run(GitHubSummaryApplication.class, args);
    }

}
