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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import de.muenchen.oss.ad2image.starter.core.Ad2ImageConfigurationProperties;
import de.muenchen.oss.ad2image.starter.core.AdConfigurationProperties;
import de.muenchen.oss.ad2image.starter.core.AvatarLoader;
import de.muenchen.oss.ad2image.starter.core.ExchangeConfigurationProperties;

@AutoConfiguration
@ConditionalOnProperty(value = "de.muenchen.oss.ad2image.enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(value = { WebMvcAutoConfiguration.class })
public class Ad2ImageAutoConfiguration {

    @Configuration
    @EnableConfigurationProperties({ Ad2ImageConfigurationProperties.class, ExchangeConfigurationProperties.class, AdConfigurationProperties.class })
    static class ConfigPropertiesConfiguration {
    }

    @Bean
    @ConditionalOnMissingBean
    AvatarService avatarService(AvatarLoader avatarLoader) {
        return new AvatarService(avatarLoader);
    }

    @Bean
    @ConditionalOnMissingBean
    AvatarLoader avatarLoader(@Qualifier("ad2ImageLdapTemplate") LdapTemplate ad2ImageLdapTemplate, RestTemplateBuilder restTemplateBuilder,
            Ad2ImageConfigurationProperties ad2ImageProps) {
        return new AvatarLoader(ad2ImageLdapTemplate, restTemplateBuilder, ad2ImageProps.getAd(), ad2ImageProps.getEws());
    }

    @Bean
    @ConditionalOnMissingBean
    AvatarController avatarController(AvatarService service) {
        return new AvatarController(service);
    }

    @Bean("ad2ImageLdapTemplate")
    @ConditionalOnMissingBean(name = "ad2ImageLdapTemplate")
    LdapTemplate ad2ImageLdapTemplate(@Qualifier("ad2ImageLdapContextSource") LdapContextSource ad2ImageLdapContextSource) {
        return new LdapTemplate(ad2ImageLdapContextSource);
    }

    @Bean("ad2ImageLdapContextSource")
    @ConditionalOnMissingBean(name = "ad2ImageLdapContextSource")
    LdapContextSource ad2ImageLdapContextSource(AdConfigurationProperties adConfProps) {
        LdapContextSource ldapContextSource = new LdapContextSource();
        ldapContextSource.setUrl(adConfProps.getUrl());
        ldapContextSource.setUserDn(adConfProps.getUserDn());
        ldapContextSource.setPassword(adConfProps.getPassword());
        return ldapContextSource;
    }

}
