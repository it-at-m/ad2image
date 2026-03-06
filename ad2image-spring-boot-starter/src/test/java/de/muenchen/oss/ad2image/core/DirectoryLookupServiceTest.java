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
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldif.LDIFReader;
import de.muenchen.oss.ad2image.starter.core.AdConfigurationProperties;
import de.muenchen.oss.ad2image.starter.core.DirectoryLookupService;
import de.muenchen.oss.ad2image.starter.core.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class DirectoryLookupServiceTest {

    private static InMemoryDirectoryServer server;

    private DirectoryLookupService sut;

    @BeforeEach
    public void setup() {
        AdConfigurationProperties adConf = new AdConfigurationProperties();
        adConf.setUrl("ldap://localhost:" + server.getListenPort());
        adConf.setUserDn("");
        adConf.setPassword("");
        adConf.setUserSearchBase("ou=Users,dc=example,dc=com");
        LdapContextSource source = new LdapContextSource();
        source.setUrl(adConf.getUrl());
        source.setUserDn(adConf.getUserDn());
        source.setPassword(adConf.getPassword());
        source.afterPropertiesSet();
        LdapTemplate ldapTemplate = new LdapTemplate(source);
        sut = new DirectoryLookupService(ldapTemplate, adConf);
    }

    @BeforeAll
    public static void startup() throws Exception {
        server = ldapServer();
    }

    @Test
    void user_found() {
        Optional<User> optionalUser = sut.findUserInDirectory("maxi.mustermann");
        assertThat(optionalUser).isPresent();
        assertThat(optionalUser.get().getEmail()).isEqualTo("maxi.mustermann.email");
        assertThat(optionalUser.get().getThumbnailPhoto()).isNotNull();
    }

    @Test
    void user_found_no_photo() {
        Optional<User> optionalUser = sut.findUserInDirectory("nophoto.user");
        assertThat(optionalUser).isPresent();
        assertThat(optionalUser.get().getThumbnailPhoto()).isNull();
    }

    @Test
    void user_not_found() {
        Optional<User> optionalUser = sut.findUserInDirectory("ham.ma.ned");
        assertThat(optionalUser).isNotPresent();
    }

    public static InMemoryDirectoryServer ldapServer() throws LDAPException, IOException {
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
