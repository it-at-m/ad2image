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

import de.muenchen.oss.ad2image.starter.core.ImageSize;
import de.muenchen.oss.ad2image.starter.core.Mode;
import de.muenchen.oss.ad2image.starter.spring.AvatarService;
import de.muenchen.oss.ad2image.starter.spring.GravatarHashMapService;
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

@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
                "de.muenchen.oss.ad2image.ews.username=dummyUser",
                "de.muenchen.oss.ad2image.ews.password=dummyPassword",
                "de.muenchen.oss.ad2image.ews.ews-service-url=http://localhost/Exchange.asmx",
                "de.muenchen.oss.ad2image.ad.url=ldap://localhost:389",
                "de.muenchen.oss.ad2image.ad.user-dn=CN=user,DC=dummy,DC=domain",
                "de.muenchen.oss.ad2image.ad.password=dummyPassword",
                "de.muenchen.oss.ad2image.ad.user-search-base=DC=dummy,DC=domain",
                "de.muenchen.oss.ad2image.gravatar.enabled=true",
        }
)
@AutoConfigureTestRestTemplate
class AvatarApplicationTest {

    @MockitoBean(name = "ad2ImageLdapTemplate")
    private LdapTemplate ldapTemplate;

    @MockitoBean
    private AvatarService service;

    @MockitoBean
    private GravatarHashMapService gravatarHashMapService;

    @LocalServerPort
    private Integer port;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void avatar_request_ok() throws IOException {
        Mockito.when(service.get(Mockito.anyString(), Mockito.any(), Mockito.anyInt()))
                .thenReturn(Files.readAllBytes(new ClassPathResource("account_dummy.png").getFile().toPath()));

        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/avatar?uid=dummy.user", String.class);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);

        Mockito.verify(service).get("dummy.user", Mode.M_FALLBACK_GENERIC, ImageSize.HR64.getSizePixels());
    }

    @Test
    void gravatar_request_ok() throws IOException {
        Mockito.when(gravatarHashMapService.getUidForSha256MailHash(Mockito.anyString())).thenReturn("dummy.user");
        Mockito.when(service.get(Mockito.anyString(), Mockito.any(), Mockito.anyInt()))
                .thenReturn(Files.readAllBytes(new ClassPathResource("account_dummy.png").getFile().toPath()));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/gravatar/963dd12f8d2f181ee9bef66a67f7b3bd87f47e9e3ecc5b534c85766b227daa28", String.class);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);

        Mockito.verify(service).get("dummy.user", Mode.M_FALLBACK_GENERIC, 80);
    }

    @Test
    void gravatar_request_md5_ok() throws IOException {
        Mockito.when(gravatarHashMapService.getUidForMd5MailHash(Mockito.anyString())).thenReturn("dummy.user");
        Mockito.when(service.get(Mockito.anyString(), Mockito.any(), Mockito.anyInt()))
                .thenReturn(Files.readAllBytes(new ClassPathResource("account_dummy.png").getFile().toPath()));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/gravatar/54119127076b6ef4cc7653dbac39350f", String.class);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);

        Mockito.verify(service).get("dummy.user", Mode.M_FALLBACK_GENERIC, 80);
    }

    @Test
    void gravatar_request_with_mp_parameter() throws IOException {
        Mockito.when(gravatarHashMapService.getUidForSha256MailHash(Mockito.anyString())).thenReturn("dummy.user");
        Mockito.when(service.get(Mockito.anyString(), Mockito.any(), Mockito.anyInt()))
                .thenReturn(Files.readAllBytes(new ClassPathResource("account_dummy.png").getFile().toPath()));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/gravatar/963dd12f8d2f181ee9bef66a67f7b3bd87f47e9e3ecc5b534c85766b227daa28?d=mp", String.class);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);

        Mockito.verify(service).get("dummy.user", Mode.M_FALLBACK_GENERIC, 80);
    }

    @Test
    void gravatar_request_with_identicon_parameter() throws IOException {
        Mockito.when(gravatarHashMapService.getUidForSha256MailHash(Mockito.anyString())).thenReturn("dummy.user");
        Mockito.when(service.get(Mockito.anyString(), Mockito.any(), Mockito.anyInt()))
                .thenReturn(Files.readAllBytes(new ClassPathResource("account_dummy.png").getFile().toPath()));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/gravatar/963dd12f8d2f181ee9bef66a67f7b3bd87f47e9e3ecc5b534c85766b227daa28?d=identicon", String.class);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);

        Mockito.verify(service).get("dummy.user", Mode.M_FALLBACK_IDENTICON, 80);
    }

    @Test
    void gravatar_request_with_unsupported_default_parameter() throws IOException {
        Mockito.when(gravatarHashMapService.getUidForSha256MailHash(Mockito.anyString())).thenReturn("dummy.user");
        Mockito.when(service.get(Mockito.anyString(), Mockito.any(), Mockito.anyInt()))
                .thenReturn(Files.readAllBytes(new ClassPathResource("account_dummy.png").getFile().toPath()));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/gravatar/963dd12f8d2f181ee9bef66a67f7b3bd87f47e9e3ecc5b534c85766b227daa28?d=monsterid", String.class);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);

        // Should fall back to gravatar default mode (M_FALLBACK_GENERIC)
        Mockito.verify(service).get("dummy.user", Mode.M_FALLBACK_GENERIC, 80);
    }

}
