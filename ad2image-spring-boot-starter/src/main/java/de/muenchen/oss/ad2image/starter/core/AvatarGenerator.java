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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class AvatarGenerator {

    private static final Logger log = LoggerFactory.getLogger(AvatarGenerator.class);

    private final Map<Integer, Avatar> identiconAvatarBuilders;
    private final Map<Integer, Avatar> triangleAvatarBuilders;
    private final Map<Integer, Avatar> squareAvatarBuilders;
    private final Map<Integer, Avatar> githubAvatarBuilders;

    public enum AvatarType {
        IDENTICON,
        SQUARE,
        TRIANGLE,
        GITHUB,
        GENERIC,
        GENERIC_DARK
    }

    public AvatarGenerator() {
        log.debug("Initializing AvatarGenerator...");
        // setup avatarBuilders for standard ad size
        this.identiconAvatarBuilders = buildAvatarBuilders(IdenticonAvatar.newAvatarBuilder());
        this.triangleAvatarBuilders = buildAvatarBuilders(TriangleAvatar.newAvatarBuilder());
        this.squareAvatarBuilders = buildAvatarBuilders(SquareAvatar.newAvatarBuilder());
        this.githubAvatarBuilders = buildAvatarBuilders(GitHubAvatar.newAvatarBuilder());
        log.debug("Initialized AvatarGenerator instance.");
    }

    private Map<Integer, Avatar> buildAvatarBuilders(AvatarBuilder avatarBuilder) {
        Map<Integer, Avatar> avatarBuilders = new HashMap<>();
        avatarBuilders.put(ImageSize.getAdDefaultImageSize().getSizePixels(),
                avatarBuilder.size(ImageSize.getAdDefaultImageSize().getSizePixels(), ImageSize.getAdDefaultImageSize().getSizePixels()).build());
        return avatarBuilders;
    }

    /**
     * Generates an Avatar as requested by type for the given UID.
     *
     * @param uid an UID
     * @param type a {@link AvatarType}
     * @param size requested image size
     * @return generated avatar as PNG
     */
    public byte[] generateAvatar(String uid, AvatarType type, int size) {
        byte[] imageBytes;
        switch (type) {
        case IDENTICON:
            log.debug("Generating identicon fallback avatar for '{}'.", uid);
            imageBytes = identiconAvatarBuilders.computeIfAbsent(size, newSize -> IdenticonAvatar.newAvatarBuilder().size(newSize, newSize).build())
                    .createAsPngBytes(uid.hashCode());
            break;
        case GENERIC:
            log.debug("Using generic fallback avatar for '{}'.", uid);
            imageBytes = getGenericPhoto(false, size);
            break;
        case GENERIC_DARK:
            log.debug("Using generic dark fallback avatar for '{}'.", uid);
            imageBytes = getGenericPhoto(true, size);
            break;
        case TRIANGLE:
            log.debug("Generating triangle fallback avatar for '{}'.", uid);
            imageBytes = triangleAvatarBuilders.computeIfAbsent(size, newSize -> TriangleAvatar.newAvatarBuilder().size(newSize, newSize).build())
                    .createAsPngBytes(uid.hashCode());
            break;
        case SQUARE:
            log.debug("Generating square fallback avatar for '{}'.", uid);
            imageBytes = squareAvatarBuilders.computeIfAbsent(size, newSize -> SquareAvatar.newAvatarBuilder().size(newSize, newSize).build())
                    .createAsPngBytes(uid.hashCode());
            break;
        case GITHUB:
            log.debug("Generating github fallback avatar for '{}'.", uid);
            imageBytes = githubAvatarBuilders.computeIfAbsent(size, newSize -> GitHubAvatar.newAvatarBuilder().size(newSize, newSize).build())
                    .createAsPngBytes(uid.hashCode());
            break;
        default:
            throw new IllegalArgumentException("Invalid type '" + type + "'.");
        }

        return imageBytes;
    }

    private byte[] getGenericPhoto(boolean dark, int size) {
        try {
            String file = dark ? "account_dark.png" : "account.png";
            return ImageScaler.scaleImage(StreamUtils.copyToByteArray(new ClassPathResource(file).getInputStream()), size, size);
        } catch (IOException e) {
            log.error("IOException while reading generic image.", e);
            throw new RuntimeException("IOException while reading generic image.", e);
        }
    }

}
