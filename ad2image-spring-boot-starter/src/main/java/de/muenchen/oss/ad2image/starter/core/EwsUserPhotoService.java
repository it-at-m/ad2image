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
package de.muenchen.oss.ad2image.starter.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class EwsUserPhotoService {

    private static final Logger log = LoggerFactory.getLogger(EwsUserPhotoService.class);

    private final String exchangeBaseUrl;
    private final RestTemplate restTemplate;

    public EwsUserPhotoService(ExchangeConfigurationProperties exchangeConfigurationProperties, RestTemplateBuilder restTemplateBuilder) {
        this.exchangeBaseUrl = exchangeConfigurationProperties.getEwsServiceUrl();
        this.restTemplate = restTemplateBuilder
                .basicAuthentication(exchangeConfigurationProperties.getUsername(), exchangeConfigurationProperties.getPassword())
                .build();
    }

    /**
     * @param email user's email
     * @param size requested {@link ImageSize}
     * @return the user's photo
     * @throws EwsUserPhotoLookupException when users photo could not be retrieved
     */
    public byte[] getUserPhotoFromExchange(String email, ImageSize size) {
        String url = this.exchangeBaseUrl + "/s/GetUserPhoto?email=" + email + "&size=" + size.getSizeRequestedValue();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                return responseEntity.getBody();
            } else {
                throw new EwsUserPhotoLookupException(
                        "Failed to retrieve user photo from Exchange, HTTP status code: %s".formatted(responseEntity.getStatusCode()));
            }
        } catch (RestClientException e) {
            throw new EwsUserPhotoLookupException("Exception while fetching user photo from Exchange.", e);
        }
    }

    public static class EwsUserPhotoLookupException extends RuntimeException {

        public EwsUserPhotoLookupException(String message, Throwable cause) {
            super(message, cause);
        }

        public EwsUserPhotoLookupException(String message) {
            super(message);
        }
    }
}
