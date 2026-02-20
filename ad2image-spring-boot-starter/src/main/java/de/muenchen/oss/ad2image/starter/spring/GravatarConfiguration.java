package de.muenchen.oss.ad2image.starter.spring;

import de.muenchen.oss.ad2image.starter.core.Ad2ImageConfigurationProperties;
import de.muenchen.oss.ad2image.starter.core.AdConfigurationProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ConditionalOnProperty(value = "de.muenchen.oss.ad2image.gravatar.enabled", havingValue = "true", matchIfMissing = false)
@EnableScheduling
public class GravatarConfiguration {

    @Bean
    @ConditionalOnMissingBean
    GravatarController gravatarController(AvatarService service, Ad2ImageConfigurationProperties ad2ImageProps, GravatarHashMapService gravatarHashMapService) {
        return new GravatarController(service, ad2ImageProps, gravatarHashMapService);
    }

    @Bean
    @ConditionalOnMissingBean
    GravatarHashMapService gravatarHashMapService(@Qualifier("ad2ImageLdapContextSource") LdapContextSource ad2ImageLdapContextSource,
            Ad2ImageConfigurationProperties ad2ImageConfigurationProperties) {
        return new GravatarHashMapService(ad2ImageLdapContextSource, ad2ImageConfigurationProperties);
    }
}
