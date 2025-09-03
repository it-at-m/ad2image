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

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.NTCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.support.LdapEncoder;
import org.springframework.util.StreamUtils;

import com.talanlabs.avatargenerator.Avatar;
import com.talanlabs.avatargenerator.Avatar.AvatarBuilder;
import com.talanlabs.avatargenerator.GitHubAvatar;
import com.talanlabs.avatargenerator.IdenticonAvatar;
import com.talanlabs.avatargenerator.SquareAvatar;
import com.talanlabs.avatargenerator.TriangleAvatar;

public class AvatarLoader {

    private static final Logger log = LoggerFactory.getLogger(AvatarLoader.class);

    private final LdapTemplate ldapTemplate;
    private final CloseableHttpClient httpClient;
    private final String exchangeBaseUrl;
    private final AdConfigurationProperties adConfigurationProps;
    private final Map<ImageSize, Avatar> identiconAvatarBuilders;
    private final Map<ImageSize, Avatar> triangleAvatarBuilders;
    private final Map<ImageSize, Avatar> squareAvatarBuilders;
    private final Map<ImageSize, Avatar> githubAvatarBuilders;

    public AvatarLoader(LdapTemplate ldapTemplate, RestTemplateBuilder restTemplateBuilder,
            AdConfigurationProperties adConfigurationProps, ExchangeConfigurationProperties exchangeConfigurationProps) {
        log.debug("Initializing AvatarLoader instance...");
        this.adConfigurationProps = adConfigurationProps;
        this.ldapTemplate = ldapTemplate;

        // setup HTTP Client for EWS Api calls
        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                // TODO probabably not the securest default
                new AuthScope(null, -1),
                new NTCredentials(exchangeConfigurationProps.getUsername(), exchangeConfigurationProps.getPassword().toCharArray(), "ad2image",
                        exchangeConfigurationProps.getDomain()));
        this.httpClient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();
        this.exchangeBaseUrl = exchangeConfigurationProps.getEwsServiceUrl();

        // setup avatarBuilders for each supported size
        this.identiconAvatarBuilders = buildAvatarBuilders(IdenticonAvatar.newAvatarBuilder());
        this.triangleAvatarBuilders = buildAvatarBuilders(TriangleAvatar.newAvatarBuilder());
        this.squareAvatarBuilders = buildAvatarBuilders(SquareAvatar.newAvatarBuilder());
        this.githubAvatarBuilders = buildAvatarBuilders(GitHubAvatar.newAvatarBuilder());
        log.debug("Initialized AvatarLoader instance.");
    }

    private Map<ImageSize, Avatar> buildAvatarBuilders(AvatarBuilder avatarBuilder) {
        EnumMap<ImageSize, Avatar> avatarBuilders = new EnumMap<>(ImageSize.class);
        for (ImageSize size : ImageSize.values()) {
            avatarBuilders.put(size, avatarBuilder.size(size.getSizePixels(), size.getSizePixels()).build());
        }
        return avatarBuilders;
    }

    public byte[] loadAvatar(String uid, Mode mode, ImageSize size) {
        log.info("Looking up '{}' in AD (mode='{}', size='{}')...", uid, mode, size);
        List<User> users = findPersonInAD(uid);
        if (users.size() == 1) {
            User user = users.get(0);
            if (user.getThumbnailPhoto() != null) {
                // user has stored a picture
                if (size.equals(ImageSize.getAdDefaultImageSize())) {
                    log.debug("Using AD thumbnailPhoto as avatar for '{}'.", uid);
                    return users.get(0).getThumbnailPhoto();
                } else {
                    log.debug("Calling Exchange EWS API GetUserPhoto for '{}' with size '{}'.", uid, size);
                    return getUserPhotoFromExchange(user.getEmail(), size.getSizeRequestedValue());
                }
            } else {
                return generateFallbackAvatar(uid, mode, size);
            }
        } else {
            return generateFallbackAvatar(uid, mode, size);
        }
    }

    private byte[] getUserPhotoFromExchange(String email, String size) {
        String url = this.exchangeBaseUrl + "/s/GetUserPhoto?email=" + email + "&size=" + size;
        HttpGet httpGet = new HttpGet(url);
        try {
            return this.httpClient.execute(httpGet, response -> {
                // Check if the response is successful
                if (response.getCode() == 200) {
                    // Convert the response entity to byte array
                    return EntityUtils.toByteArray(response.getEntity());
                } else {
                    log.warn("Failed to retrieve user photo from Exchange, HTTP status code: {}", response.getCode());
                    return null;
                }
            });
        } catch (IOException e) {
            log.error("Exception while fetching user photo from Exchange.", e);
            return null;
        }
    }

    private byte[] generateFallbackAvatar(String uid, Mode mode, ImageSize size) {
        switch (mode) {
        case M_IDENTICON, M_FALLBACK_IDENTICON:
            log.debug("Generating identicon fallback avatar for '{}'.", uid);
            return identiconAvatarBuilders.get(size).createAsPngBytes(uid.hashCode());
        case M_GENERIC, M_FALLBACK_GENERIC:
            log.debug("Using generic fallback avatar for '{}'.", uid);
            return getGenericPhoto(size);
        case M_TRIANGLE, M_FALLBACK_TRIANGLE:
            log.debug("Generating triangle fallback avatar for '{}'.", uid);
            return triangleAvatarBuilders.get(size).createAsPngBytes(uid.hashCode());
        case M_SQUARE, M_FALLBACK_SQUARE:
            log.debug("Generating square fallback avatar for '{}'.", uid);
            return squareAvatarBuilders.get(size).createAsPngBytes(uid.hashCode());
        case M_GITHUB, M_FALLBACK_GITHUB:
            log.debug("Generating github fallback avatar for '{}'.", uid);
            return githubAvatarBuilders.get(size).createAsPngBytes(uid.hashCode());
        default:
            log.debug("No thumbnailPhoto for '{}' found and no fallback specified - returning null.", uid);
            return null;
        }

    }

    /**
     * @param uid die UID des Users
     * @return Trefferliste
     */
    private List<User> findPersonInAD(String uid) {
        String userSearchFilter = this.adConfigurationProps.getUserSearchFilter();
        String safeUid = LdapEncoder.filterEncode(uid);
        String finalFilter = userSearchFilter.replace("{uid}", safeUid);
        String uidAttribute = this.adConfigurationProps.getUidAttribute();
        String mailAttribute = this.adConfigurationProps.getMailAttribute();
        String thumbnailPhotoAttribute = this.adConfigurationProps.getThumbnailPhotoAttribute();
        return ldapTemplate.search(
                LdapQueryBuilder.query().base(this.adConfigurationProps.getUserSearchBase())
                        .filter(finalFilter),
                new AttributesMapper<User>() {
                    @Override
                    public User mapFromAttributes(Attributes attributes) throws NamingException {
                        User u = new User();
                        u.setUid((String) attributes.get(uidAttribute).get());
                        u.setEmail((String) attributes.get(mailAttribute).get());
                        Attribute thumbnailAttribute = attributes.get(thumbnailPhotoAttribute);
                        if (thumbnailAttribute != null) {
                            u.setThumbnailPhoto((byte[]) thumbnailAttribute.get());
                        }
                        return u;
                    }
                });
    }

    private byte[] getGenericPhoto(ImageSize size) {
        try {
            return StreamUtils.copyToByteArray(new ClassPathResource("account_" + size.getSizePixels() + ".png").getInputStream());
        } catch (IOException e) {
            log.error("IOException while reading generic image.", e);
            throw new RuntimeException("IOException while reading generic image.", e);
        }
    }

}
