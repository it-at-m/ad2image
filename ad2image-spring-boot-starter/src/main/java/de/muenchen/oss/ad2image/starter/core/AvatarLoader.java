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

import com.talanlabs.avatargenerator.Avatar;
import com.talanlabs.avatargenerator.Avatar.AvatarBuilder;
import com.talanlabs.avatargenerator.GitHubAvatar;
import com.talanlabs.avatargenerator.IdenticonAvatar;
import com.talanlabs.avatargenerator.SquareAvatar;
import com.talanlabs.avatargenerator.TriangleAvatar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.support.LdapEncoder;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class AvatarLoader {

    private static final Logger log = LoggerFactory.getLogger(AvatarLoader.class);

    private final LdapTemplate ldapTemplate;
    private final String exchangeBaseUrl;
    private final RestTemplate restTemplate;
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

        this.restTemplate = restTemplateBuilder
                .basicAuthentication(exchangeConfigurationProps.getUsername(), exchangeConfigurationProps.getPassword())
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

    public byte[] loadAvatar(String uid, Mode mode, int size) {
        log.info("Looking up '{}' in AD (mode='{}', size='{}')...", uid, mode, size);
        List<User> users = findPersonInAD(uid);
        byte[] avatarBytes = null;
        if (users.size() == 1) {
            User user = users.getFirst();
            if (user.getThumbnailPhoto() != null) {
                // user has stored a picture
                if (size <= ImageSize.getAdDefaultImageSize().getSizePixels()) {
                    log.debug("Using AD thumbnailPhoto as avatar for '{}'.", uid);
                    if (size < ImageSize.getAdDefaultImageSize().getSizePixels()) {
                        try {
                            avatarBytes = ImageScaler.scaleImage(user.getThumbnailPhoto(), size, size);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to scale image", e);
                        }
                    } else {
                        avatarBytes = user.getThumbnailPhoto();
                    }
                } else {
                    log.debug("Calling Exchange EWS API GetUserPhoto for mail '{}' with size '{}'.", user.getEmail(),
                            size);
                    avatarBytes = getUserPhotoFromExchange(user.getEmail(), size);
                }
            } else {
                log.debug("User '{}' has no photo stored, computing fallback avatar.", uid);
                avatarBytes = generateFallbackAvatar(uid, mode, size);
            }
        } else {
            if (users.size() > 1) {
                log.warn("Found more than one users for '{}' in AD.", uid);
                avatarBytes = generateFallbackAvatar(uid, mode, size);
            } else {
                log.debug("User '{}' not found in AD.", uid);
                avatarBytes = generateFallbackAvatar(uid, mode, size);
            }
        }
        return avatarBytes;
    }

    private byte[] getUserPhotoFromExchange(String email, int size) {
        ImageSize nearestSize = ImageSize.findNearestSize(size);
        String url = this.exchangeBaseUrl + "/s/GetUserPhoto?email=" + email + "&size=" + nearestSize.getSizeRequestedValue();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                byte[] imageBytes = responseEntity.getBody();
                if (nearestSize.getSizePixels() != size) {
                    return ImageScaler.scaleImage(imageBytes, size, size);
                } else {
                    return imageBytes;
                }
            } else {
                log.warn("Failed to retrieve user photo from Exchange, HTTP status code: {}", responseEntity.getStatusCode());
                return null;
            }
        } catch (RestClientException e) {
            log.error("Exception while fetching user photo from Exchange.", e);
            return null;
        } catch (IOException e) {
            throw new RuntimeException("Failed to scale image", e);
        }
    }

    private byte[] generateFallbackAvatar(String uid, Mode mode, int size) {
        ImageSize nearestSize = ImageSize.findNearestSize(size);
        byte[] imageBytes = null;
        switch (mode) {
        case M_IDENTICON:
        case M_FALLBACK_IDENTICON:
            log.debug("Generating identicon fallback avatar for '{}'.", uid);
            imageBytes = identiconAvatarBuilders.get(nearestSize).createAsPngBytes(uid.hashCode());
            break;
        case M_GENERIC:
        case M_FALLBACK_GENERIC:
            log.debug("Using generic fallback avatar for '{}'.", uid);
            imageBytes = getGenericPhoto(nearestSize);
            break;
        case M_TRIANGLE:
        case M_FALLBACK_TRIANGLE:
            log.debug("Generating triangle fallback avatar for '{}'.", uid);
            imageBytes = triangleAvatarBuilders.get(nearestSize).createAsPngBytes(uid.hashCode());
            break;
        case M_SQUARE:
        case M_FALLBACK_SQUARE:
            log.debug("Generating square fallback avatar for '{}'.", uid);
            imageBytes = squareAvatarBuilders.get(nearestSize).createAsPngBytes(uid.hashCode());
            break;
        case M_GITHUB:
        case M_FALLBACK_GITHUB:
            log.debug("Generating github fallback avatar for '{}'.", uid);
            imageBytes = githubAvatarBuilders.get(nearestSize).createAsPngBytes(uid.hashCode());
            break;
        default:
            log.debug("No thumbnailPhoto for '{}' found and no fallback specified - returning null.", uid);
            break;
        }

        if (imageBytes == null) {
            return null;
        }

        if (nearestSize.getSizePixels() != size) {
            try {
                return ImageScaler.scaleImage(imageBytes, size, size);
            } catch (IOException e) {
                throw new RuntimeException("Failed to scale image", e);
            }
        } else {
            return imageBytes;
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
            return StreamUtils.copyToByteArray(new ClassPathResource("account.png").getInputStream());
        } catch (IOException e) {
            log.error("IOException while reading generic image.", e);
            throw new RuntimeException("IOException while reading generic image.", e);
        }
    }

}
