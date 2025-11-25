package api.molby.githubSummary.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfiguration {

    @Value("${spring.cache.github.maximum-size:1000}")
    private int maximumSize;

    @Value("${spring.cache.github.expire-after-write:60m}")
    private String expireAfterWrite;  // parse manually or better: use seconds/minutes

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("githubUserSummary");
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .maximumSize(maximumSize)
                        .expireAfterWrite(DurationStyle.detectAndParse(expireAfterWrite))
        );
        return cacheManager;
    }
}
