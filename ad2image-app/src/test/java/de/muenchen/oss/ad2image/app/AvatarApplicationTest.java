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
package de.muenchen.oss.ad2image.app;

import de.muenchen.oss.ad2image.starter.core.AvatarLoader;
import de.muenchen.oss.ad2image.starter.core.ImageSize;
import de.muenchen.oss.ad2image.starter.core.Mode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.nio.file.Files;

import static org.mockito.Mockito.times;

/**
 * @author michael.prankl
 *
 */
@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
                "de.muenchen.oss.ad2image.ews.username=dummyUser",
                "de.muenchen.oss.ad2image.ews.password=dummyPassword",
                "de.muenchen.oss.ad2image.ews.domain=dummy.domain",
                "de.muenchen.oss.ad2image.ews.ews-service-url=http://localhost/Exchange.asmx",
                "de.muenchen.oss.ad2image.ad.url=ldap://localhost:389",
                "de.muenchen.oss.ad2image.ad.user-dn=CN=user,DC=dummy,DC=domain",
                "de.muenchen.oss.ad2image.ad.password=dummyPassword",
                "de.muenchen.oss.ad2image.ad.user-search-base=DC=dummy,DC=domain"
        }
)
@AutoConfigureTestRestTemplate
class AvatarApplicationTest {

    @MockitoBean(name = "ad2ImageLdapTemplate")
    private LdapTemplate ldapTemplate;

    @MockitoBean
    private AvatarLoader loader;

    @LocalServerPort
    private Integer port;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void avatar_request_ok() throws IOException {
        Mockito.when(loader.loadAvatar(Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenReturn(Files.readAllBytes(new ClassPathResource("account_dummy.png").getFile().toPath()));

        ResponseEntity<String> firstResponse = restTemplate.getForEntity("http://localhost:" + port + "/avatar?uid=dummy.user", String.class);
        Assertions.assertThat(firstResponse.getStatusCode().value()).isEqualTo(200);
        ResponseEntity<String> secondResponse = restTemplate.getForEntity("http://localhost:" + port + "/avatar?uid=dummy.user", String.class);
        Assertions.assertThat(secondResponse.getStatusCode().value()).isEqualTo(200);

        // second request should get cached - so just 1 call to loader
        Mockito.verify(loader, times(1)).loadAvatar("dummy.user", Mode.M_FALLBACK_GENERIC, ImageSize.HR64);
    }

}
