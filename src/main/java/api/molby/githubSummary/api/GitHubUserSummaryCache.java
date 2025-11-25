package api.molby.githubSummary.api;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Component to handle caching of GitHubSummaryResponseDTO in the event of
 * an access failure.
 */
@Component
public class GitHubUserSummaryCache {

    /**
     * Return information from the cache.  As set up this doese NOT
     * act as a write thorugh cache but is purely used for a read
     * operation.  If something is desired in the cache the cacheResponse
     * method should be called.
     * @param username User that is the key for the cache
     * @return Cached object or null if object does not exist in cache.
     */
    @Cacheable(cacheNames = "githubUserSummary", key="#username", unless = "#result == null")
    public GitHubUserSummaryDTO getResponseFromCache(String username) {
        return null;
    }

    @CachePut(value="githubUserSummary", key="#userName")
    public GitHubUserSummaryDTO cacheResponse(String userName, GitHubUserSummaryDTO gitHubUserSummaryDTO) {
        return gitHubUserSummaryDTO;

    }
}
