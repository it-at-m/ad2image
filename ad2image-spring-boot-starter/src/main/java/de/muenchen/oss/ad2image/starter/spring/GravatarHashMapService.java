package de.muenchen.oss.ad2image.starter.spring;

import de.muenchen.oss.ad2image.starter.core.Ad2ImageConfigurationProperties;
import de.muenchen.oss.ad2image.starter.core.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.control.PagedResultsDirContextProcessor;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.core.support.SingleContextSource;
import org.springframework.scheduling.annotation.Scheduled;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GravatarHashMapService {

    private static final Logger log = LoggerFactory.getLogger(GravatarHashMapService.class);

    private final LdapContextSource contextSource;
    private final Ad2ImageConfigurationProperties ad2ImageConfigurationProperties;

    private final Map<String, String> emailSha256HashToUidCache = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;

    public GravatarHashMapService(LdapContextSource contextSource, Ad2ImageConfigurationProperties ad2ImageConfigurationProperties) {
        this.contextSource = contextSource;
        this.ad2ImageConfigurationProperties = ad2ImageConfigurationProperties;
        // Initialize the cache
        this.populateMap();
    }

    private void populateMap() {
        // Simulate time-consuming cache population
        log.info("Populating SHA256-hashed email-to-uid map...");
        updateMap();
        initialized = true;
        log.info("SHA256-hashed email-to-uid map populated.");
    }

    private void updateMap() {
        this.findAllPersons().stream().forEach(person -> {
            String sha256Hex = DigestUtils.sha256Hex(person.getEmail());
            emailSha256HashToUidCache.put(sha256Hex.toLowerCase(), person.getUid());
        });
    }

    private List<User> findAllPersons() {
        String searchFilter = this.ad2ImageConfigurationProperties.getGravatar().getMapPopulationFilter();
        String uidAttribute = this.ad2ImageConfigurationProperties.getAd().getUidAttribute();
        String mailAttribute = this.ad2ImageConfigurationProperties.getAd().getMailAttribute();
        Integer pageSize = this.ad2ImageConfigurationProperties.getGravatar().getPageSize();
        String userSearchBase = this.ad2ImageConfigurationProperties.getAd().getUserSearchBase();
        final SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(new String[] { uidAttribute, mailAttribute });

        final PagedResultsDirContextProcessor processor = new PagedResultsDirContextProcessor(this.ad2ImageConfigurationProperties.getGravatar().getPageSize());
        log.info("Looking up users for mail address hashing [search-base='{}', search-filter='{}', pageSize='{}'] - this could take a while...",
                ad2ImageConfigurationProperties.getAd().getUserSearchBase(), searchFilter, pageSize);
        return SingleContextSource.doWithSingleContext(contextSource, operations -> {
            List<User> result = new LinkedList<>();

            do {
                List<User> oneResult = operations.search(userSearchBase, searchFilter, searchControls, new AttributesMapper<User>() {
                    @Override
                    public User mapFromAttributes(Attributes attributes) throws NamingException {
                        User u = new User();
                        u.setUid((String) attributes.get(uidAttribute).get());
                        u.setEmail((String) attributes.get(mailAttribute).get());
                        return u;
                    }
                }, processor);

                result.addAll(oneResult);
            } while (processor.hasMore());

            return result;
        });

    }

    @Scheduled(cron = "${de.muenchen.oss.ad2image.gravatar.hash-cache-refresh-cron:-}")
    public void updateCache() {
        if (!initialized) {
            return; // Don't update if not initialized
        }
        log.info("Starting scheduled update of sha256-mail-addresses-hashes map...");
        updateMap();
    }

    public String getUidForMailHash(String sha256MailHash) {
        if (!initialized) {
            return null;
        }
        return emailSha256HashToUidCache.get(sha256MailHash);
    }
}
