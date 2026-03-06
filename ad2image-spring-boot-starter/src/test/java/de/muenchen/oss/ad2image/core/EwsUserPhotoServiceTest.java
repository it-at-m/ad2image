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

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import de.muenchen.oss.ad2image.starter.core.EwsUserPhotoService;
import de.muenchen.oss.ad2image.starter.core.ExchangeConfigurationProperties;
import de.muenchen.oss.ad2image.starter.core.ImageSize;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.restclient.RestTemplateBuilder;

import static com.github.tomakehurst.wiremock.client.WireMock.get;

public class EwsUserPhotoServiceTest {

    @RegisterExtension
    static WireMockExtension wm1 = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort())
            .build();

    private EwsUserPhotoService sut;

    @BeforeEach
    void setUp() {
        ExchangeConfigurationProperties exchangeConfigurationProperties = new ExchangeConfigurationProperties();
        exchangeConfigurationProperties.setEwsServiceUrl(wm1.baseUrl());
        exchangeConfigurationProperties.setUsername("a");
        exchangeConfigurationProperties.setPassword("b");
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        sut = new EwsUserPhotoService(exchangeConfigurationProperties, restTemplateBuilder);
    }

    @Test
    void get_ok() {
        wm1.stubFor(WireMock.get(WireMock.urlPathEqualTo("/s/GetUserPhoto"))
                .withHeader("Authorization", WireMock.containing("Basic"))
                .willReturn(
                        WireMock.ok().withBodyFile("account_dummy.png")));

        byte[] userPhotoFromExchange = sut.getUserPhotoFromExchange("mail@example.com", ImageSize.HR648);
        Assertions.assertThat(userPhotoFromExchange).isNotEmpty();
    }

    @Test
    void get_nok() {
        wm1.stubFor(WireMock.get(WireMock.urlPathEqualTo("/s/GetUserPhoto"))
                .withHeader("Authorization", WireMock.containing("Basic"))
                .willReturn(WireMock.notFound()));

        org.junit.jupiter.api.Assertions.assertThrows(EwsUserPhotoService.EwsUserPhotoLookupException.class, () -> {
            sut.getUserPhotoFromExchange("mail@example.com", ImageSize.HR648);
        });
    }
}
