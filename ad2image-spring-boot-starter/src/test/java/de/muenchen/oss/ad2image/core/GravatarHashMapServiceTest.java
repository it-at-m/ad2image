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
package de.muenchen.oss.ad2image.core;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import de.muenchen.oss.ad2image.starter.core.Ad2ImageConfigurationProperties;
import de.muenchen.oss.ad2image.starter.core.AdConfigurationProperties;
import de.muenchen.oss.ad2image.starter.core.GravatarConfigurationProperties;
import de.muenchen.oss.ad2image.starter.spring.GravatarHashMapService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ldap.core.support.LdapContextSource;

/**
 * @author michael.prankl
 *
 */
class GravatarHashMapServiceTest {

    private static InMemoryDirectoryServer server;
    private GravatarHashMapService sut;

    @Test
    void initializes_from_ad() {
        Assertions.assertThat(sut.getUidForMailHash("abc")).isNull();
        // maxi.mustermann.email
        Assertions.assertThat(sut.getUidForMailHash("4acd4226a47683e8500b832633246a231726686714965da16a0d2aabffffad3f")).isEqualTo("maxi.mustermann");
        sut.updateCache();
        Assertions.assertThat(sut.getUidForMailHash("abc")).isNull();
        Assertions.assertThat(sut.getUidForMailHash("4acd4226a47683e8500b832633246a231726686714965da16a0d2aabffffad3f")).isEqualTo("maxi.mustermann");
    }

    @BeforeEach
    public void setup() {
        Ad2ImageConfigurationProperties props = new Ad2ImageConfigurationProperties();
        AdConfigurationProperties adConf = new AdConfigurationProperties();
        adConf.setUrl("ldap://localhost:" + server.getListenPort());
        adConf.setUserDn("");
        adConf.setPassword("");
        adConf.setUserSearchBase("ou=Users,dc=example,dc=com");
        props.setAd(adConf);
        GravatarConfigurationProperties gravatarConfigurationProperties = new GravatarConfigurationProperties();
        props.setGravatar(gravatarConfigurationProperties);
        LdapContextSource source = new LdapContextSource();
        source.setUrl(adConf.getUrl());
        source.setUserDn(adConf.getUserDn());
        source.setPassword(adConf.getPassword());
        source.afterPropertiesSet();

        sut = new GravatarHashMapService(source, props);
    }

    @BeforeAll
    public static void startup() throws Exception {
        server = AvatarLoaderTest.ldapServer();
    }

}
