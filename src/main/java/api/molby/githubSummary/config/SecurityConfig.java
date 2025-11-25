package api.molby.githubSummary.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SpringBoot security configuration.  Currently wide open.
 */
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        // no real security in place at this point, all pages are permitted.
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        )
                        .permitAll()
                        .anyRequest()
                        .permitAll()
                );
        return (SecurityFilterChain) http.build();
    }
}
