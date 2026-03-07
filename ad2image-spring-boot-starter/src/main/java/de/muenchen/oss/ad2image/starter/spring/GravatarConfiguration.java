/*
 * The MIT License
 * Copyright © 2022 Landeshauptstadt München | it@M
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.muenchen.oss.ad2image.starter.spring;

import de.muenchen.oss.ad2image.starter.core.Ad2ImageConfigurationProperties;
import de.muenchen.oss.ad2image.starter.core.AdConfigurationProperties;
import de.muenchen.oss.ad2image.starter.core.AvatarGenerator;
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

    /**
     * Creates a GravatarController configured with the provided avatar services and configuration properties.
     *
     * @param service the AvatarService used to fetch and manage avatar data
     * @param avatarGenerator the AvatarGenerator responsible for producing avatar images
     * @param gravatarHashMapService the GravatarHashMapService that resolves Gravatar hashes to entries
     * @param ad2ImageConfigurationProperties configuration properties for the ad2image module
     * @return a GravatarController instance
     */
    @Bean
    @ConditionalOnMissingBean
    GravatarController gravatarController(AvatarService service, AvatarGenerator avatarGenerator, GravatarHashMapService gravatarHashMapService,
            Ad2ImageConfigurationProperties ad2ImageConfigurationProperties) {
        return new GravatarController(service, avatarGenerator, gravatarHashMapService, ad2ImageConfigurationProperties);
    }

    /**
     * Create a GravatarHashMapService configured with the application's LDAP context and properties.
     *
     * @param ad2ImageLdapContextSource the LDAP context source used for Active Directory lookups
     * @param ad2ImageConfigurationProperties application configuration properties for AD-to-image behavior
     * @return a configured GravatarHashMapService instance
     */
    @Bean
    @ConditionalOnMissingBean
    GravatarHashMapService gravatarHashMapService(@Qualifier("ad2ImageLdapContextSource") LdapContextSource ad2ImageLdapContextSource,
            Ad2ImageConfigurationProperties ad2ImageConfigurationProperties) {
        return new GravatarHashMapService(ad2ImageLdapContextSource, ad2ImageConfigurationProperties);
    }
}
