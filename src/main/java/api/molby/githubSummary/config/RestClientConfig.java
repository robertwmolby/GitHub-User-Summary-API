package api.molby.githubSummary.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuration for RestClients.
 */
@Configuration
public class RestClientConfig {

    // allow override of github api url if needed...
    @Value("${github.api.url:https://api.github.com}")
    private String gitHubAPIUrl;
    /**
     * Return rest client for usage in github api calls.
     * @return github api url.
     */
    @Bean
    public RestClient gethubRestClient() {
        return RestClient.builder()
                .baseUrl(gitHubAPIUrl)
                .build();
    }
}
