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

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.unauthorized;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldif.LDIFException;
import com.unboundid.ldif.LDIFReader;

import de.muenchen.oss.ad2image.starter.core.AdConfigurationProperties;
import de.muenchen.oss.ad2image.starter.core.AvatarLoader;
import de.muenchen.oss.ad2image.starter.core.ExchangeConfigurationProperties;
import de.muenchen.oss.ad2image.starter.core.ImageSize;

/**
 * @author michael.prankl
 *
 */
class AvatarLoaderTest {

    @RegisterExtension
    static WireMockExtension wm1 = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort())
            .build();

    private static InMemoryDirectoryServer server;
    private AvatarLoader sut;

    @Test
    void default_size_from_ad() throws IOException {
        String uid = "maxi.mustermann";
        byte[] loadAvatar = sut.loadAvatar(uid, "identicon", ImageSize.getAdDefaultImageSize());
        assertThat(loadAvatar).isNotEmpty();
        Files.write(new File("target/" + uid + "_64.jpg").toPath(), loadAvatar);
    }

    @Test
    void bigger_size_from_ews() throws IOException {
        // stub Non-Preemptive authentication flow (default of Apache HTTP Client) - first request is without auth
        // @formatter:off
        wm1.stubFor(get(urlPathEqualTo("/s/GetUserPhoto"))
                .inScenario("Non-Preemptive Auth")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(
                        unauthorized()
                            .withHeader("WWW-Authenticate", "Negotiate")
                            .withHeader("WWW-Authenticate", "NTLM")
                            )
                .willSetStateTo("Negotiated"));
        wm1.stubFor(get(urlPathEqualTo("/s/GetUserPhoto"))
                .inScenario("Non-Preemptive Auth")
                .whenScenarioStateIs("Negotiated")
                .withHeader("Authorization", WireMock.containing("NTLM"))
                .willReturn(
                        ok().withBodyFile("account_dummy.png")));
        // @formatter:on
        String uid = "maxi.mustermann";
        byte[] loadAvatar = sut.loadAvatar(uid, "identicon", ImageSize.HR648);
        assertThat(loadAvatar).isNotEmpty();
        Files.write(new File("target/" + uid + "_648.jpg").toPath(), loadAvatar);
    }

    @Test
    void no_user_fallbackIdenticon() throws IOException {
        String uid = "dengibtsned.imad";
        byte[] loadAvatar = sut.loadAvatar(uid, "fallbackIdenticon", ImageSize.HR648);
        assertThat(loadAvatar).isNotEmpty();
        Files.write(new File("target/" + uid + "_648.png").toPath(), loadAvatar);
    }

    @Test
    void user_without_picture_fallback() throws IOException {
        String uid = "nophoto.user";
        byte[] loadAvatar = sut.loadAvatar(uid, "fallbackIdenticon", ImageSize.HR648);
        assertThat(loadAvatar).isNotEmpty();
        Files.write(new File("target/" + uid + "_648.png").toPath(), loadAvatar);
    }

    @Test
    void user_without_picture_no_fallback() throws IOException {
        String uid = "nophoto.user";
        byte[] loadAvatar = sut.loadAvatar(uid, "404", ImageSize.HR648);
        assertThat(loadAvatar).isNull();
    }

    @BeforeEach
    public void setup() {
        AdConfigurationProperties adConf = new AdConfigurationProperties();
        adConf.setUrl("ldap://localhost:" + server.getListenPort());
        adConf.setUserDn("");
        adConf.setPassword("");
        adConf.setUserSearchBase("ou=Users,dc=example,dc=com");
        ExchangeConfigurationProperties exchangeConf = new ExchangeConfigurationProperties();
        exchangeConf.setEwsServiceUrl(wm1.getRuntimeInfo().getHttpBaseUrl());
        exchangeConf.setDomain("localhost");
        exchangeConf.setUsername("aDummyUsername");
        exchangeConf.setPassword("aDummyPassword");
        LdapContextSource source = new LdapContextSource();
        source.setUrl(adConf.getUrl());
        source.setUserDn(adConf.getUserDn());
        source.setPassword(adConf.getPassword());
        source.afterPropertiesSet();
        LdapTemplate ldapTemplate = new LdapTemplate(source);
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        sut = new AvatarLoader(ldapTemplate, restTemplateBuilder, adConf, exchangeConf);
    }

    @BeforeAll
    public static void startup() throws Exception {
        server = ldapServer();
    }

    private static InMemoryDirectoryServer ldapServer() throws LDAPException, IOException, LDIFException {
        final InMemoryListenerConfig listenerConfig = InMemoryListenerConfig.createLDAPConfig(
                "default", 0);
        final InMemoryDirectoryServerConfig c = new InMemoryDirectoryServerConfig(
                "dc=example,dc=com");
        c.setListenerConfigs(listenerConfig);
        c.setEnforceAttributeSyntaxCompliance(false);
        c.setEnforceSingleStructuralObjectClass(false);
        c.setSchema(null); // schema compliance not needed for testing
        final InMemoryDirectoryServer inMemoryDirectoryServer = new InMemoryDirectoryServer(c);
        inMemoryDirectoryServer.startListening();
        final ClassPathResource sf2LdifResource = new ClassPathResource("simple.ldif");
        final LDIFReader reader = new LDIFReader(sf2LdifResource.getFile());
        inMemoryDirectoryServer.importFromLDIF(true, reader);
        return inMemoryDirectoryServer;
    }

}
