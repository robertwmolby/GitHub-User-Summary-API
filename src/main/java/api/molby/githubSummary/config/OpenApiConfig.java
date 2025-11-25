package api.molby.githubSummary.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gitHubUserSummaryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GitHub User Summary API")
                        .description("Service that leverages GitHub apis to create a consolidated view of user information with " +
                                "their corresponding repository lists.")
                        .version("1.0.0")
                );
    }
}