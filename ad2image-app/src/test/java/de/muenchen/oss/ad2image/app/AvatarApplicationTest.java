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

import static org.mockito.Mockito.times;

import java.io.IOException;
import java.nio.file.Files;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import de.muenchen.oss.ad2image.starter.core.AvatarLoader;
import de.muenchen.oss.ad2image.starter.core.ImageSize;

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
class AvatarApplicationTest {

    @MockitoBean(name = "ad2ImageLdapTemplate")
    private LdapTemplate ldapTemplate;

    @MockitoBean
    private AvatarLoader loader;

    // just a needed hack to disable (Test)RestTemplate auto-configuration of @SpringBootTest
    @MockitoBean
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private Integer port;

    private CloseableHttpClient httpClient = HttpClients.createDefault();

    @Test
    void avatar_request_ok() throws IOException {
        Mockito.when(loader.loadAvatar(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenReturn(Files.readAllBytes(new ClassPathResource("account_dummy.png").getFile().toPath()));

        CloseableHttpResponse firstResponse = httpClient.execute(new HttpGet("http://localhost:" + port + "/avatar?uid=dummy.user"));
        Assertions.assertThat(firstResponse.getCode()).isEqualTo(200);

        CloseableHttpResponse secondResponse = httpClient.execute(new HttpGet("http://localhost:" + port + "/avatar?uid=dummy.user"));
        Assertions.assertThat(secondResponse.getCode()).isEqualTo(200);

        // second request should get cached - so just 1 call to loader
        Mockito.verify(loader, times(1)).loadAvatar("dummy.user", "identicon", ImageSize.HR64);
    }

}
