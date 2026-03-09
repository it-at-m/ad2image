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
package de.muenchen.oss.ad2image.starter.spring;

import de.muenchen.oss.ad2image.starter.core.DirectoryLookupService;
import de.muenchen.oss.ad2image.starter.core.EwsUserPhotoService;
import de.muenchen.oss.ad2image.starter.core.ImageScaler;
import de.muenchen.oss.ad2image.starter.core.ImageSize;
import de.muenchen.oss.ad2image.starter.core.Mode;
import de.muenchen.oss.ad2image.starter.core.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;

import de.muenchen.oss.ad2image.starter.core.AvatarGenerator;
import de.muenchen.oss.ad2image.starter.core.InitialsAvatarGenerator;

import java.io.IOException;
import java.util.Optional;

import static de.muenchen.oss.ad2image.starter.core.Mode.M_FALLBACK_GENERIC;
import static de.muenchen.oss.ad2image.starter.core.Mode.M_FALLBACK_GENERIC_DARK;
import static de.muenchen.oss.ad2image.starter.core.Mode.M_FALLBACK_GITHUB;
import static de.muenchen.oss.ad2image.starter.core.Mode.M_FALLBACK_IDENTICON;
import static de.muenchen.oss.ad2image.starter.core.Mode.M_FALLBACK_SQUARE;
import static de.muenchen.oss.ad2image.starter.core.Mode.M_FALLBACK_TRIANGLE;
import static de.muenchen.oss.ad2image.starter.core.Mode.M_GITHUB;
import static de.muenchen.oss.ad2image.starter.core.Mode.M_SQUARE;

public class AvatarService {

    private static final Logger log = LoggerFactory.getLogger(AvatarService.class);

    private final AvatarGenerator avatarGenerator;
    private final DirectoryLookupService directoryLookupService;
    private final EwsUserPhotoService ewsUserPhotoService;

    public AvatarService(AvatarGenerator avatarGenerator, DirectoryLookupService directoryLookupService, EwsUserPhotoService ewsUserPhotoService) {
        this.avatarGenerator = avatarGenerator;
        this.directoryLookupService = directoryLookupService;
        this.ewsUserPhotoService = ewsUserPhotoService;
    }

    /**
     * Produces an avatar image for the given user identifier using directory data, Exchange photos, or
     * fallback generators.
     *
     * The method returns the user's stored AD thumbnail if available (scaling it when necessary),
     * requests a larger
     * photo from Exchange when a larger size is requested, or generates a fallback image when no photo
     * exists. For
     * non-existent users the method will generate a fallback image only for explicit fallback modes;
     * otherwise it
     * returns null.
     *
     * @param uid the user identifier to resolve and generate an avatar for
     * @param mode the avatar selection mode that controls fallback behavior and special modes (e.g.,
     *            initials, 404)
     * @param size the requested avatar edge length in pixels
     * @return a byte array containing the avatar image data, or `null` when no avatar is available for
     *         the given mode
     * @throws RuntimeException if an image scaling operation fails
     */
    @Cacheable("avatars")
    public byte[] get(String uid, Mode mode, int size) {
        byte[] avatarBytes = null;
        Optional<User> userInDirectory = directoryLookupService.findUserInDirectory(uid);
        if (userInDirectory.isPresent()) {
            User user = userInDirectory.get();
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
                    ImageSize nearestSize = ImageSize.findNearestSize(size);
                    avatarBytes = ewsUserPhotoService.getUserPhotoFromExchange(user.getEmail(), nearestSize);
                    if (nearestSize.getSizePixels() != size) {
                        try {
                            avatarBytes = ImageScaler.scaleImage(avatarBytes, size, size);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to scale image", e);
                        }
                    }
                }
            } else {
                if (mode == Mode.M_404) {
                    log.debug("User '{}' has no photo stored and mode=404, returning null as photo", uid);
                    return null;
                } else if (mode == Mode.M_INITIALS) {
                    log.debug("Generating initials avatar for '{}'.", uid);
                    avatarBytes = InitialsAvatarGenerator.generate(uid, buildInitials(user), size);
                } else {
                    AvatarGenerator.AvatarType avatarType = getAvatarType(mode);
                    log.debug("User '{}' has no photo stored, computing fallback avatar with type {}...", uid, avatarType);
                    avatarBytes = avatarGenerator.generateAvatar(uid, avatarType, size);
                }
            }
        } else {
            if (mode == M_FALLBACK_GITHUB
                    || mode == M_FALLBACK_SQUARE
                    || mode == M_FALLBACK_GENERIC
                    || mode == M_FALLBACK_GENERIC_DARK
                    || mode == M_FALLBACK_IDENTICON
                    || mode == M_FALLBACK_TRIANGLE) {
                AvatarGenerator.AvatarType avatarType = getAvatarType(mode);
                log.debug("User '{} does not exist in AD, computing fallback avatar with type {}...'", uid, avatarType);
                avatarBytes = avatarGenerator.generateAvatar(uid, avatarType, size);
            } else {
                log.debug("User '{}' does not exist in AD and no fallback mode requested, returning null as photo", uid);
                return null;
            }
        }
        return avatarBytes;
    }

    /**
     * Builds uppercase initials from the user's given name and surname.
     *
     * If the user's given name and/or surname (sn) are present and not blank, this returns
     * the first character of each converted to uppercase and concatenated (given name first).
     *
     * @param user the user whose initials to build
     * @return the concatenated uppercase initials (one or two characters), or an empty string if
     *         neither name is available
     */
    private static String buildInitials(User user) {
        String given = user.getGivenName();
        String sn = user.getSn();
        return (given != null && !given.isBlank() ? String.valueOf(Character.toUpperCase(given.charAt(0))) : "")
                + (sn != null && !sn.isBlank() ? String.valueOf(Character.toUpperCase(sn.charAt(0))) : "");
    }

    /**
     * Map a Mode value to the corresponding AvatarGenerator.AvatarType.
     *
     * @param mode the requested avatar Mode
     * @return the corresponding AvatarGenerator.AvatarType; returns GENERIC when the mode has no
     *         specific mapping
     */
    private AvatarGenerator.AvatarType getAvatarType(Mode mode) {
        AvatarGenerator.AvatarType type = switch (mode) {
        case M_GENERIC_DARK, M_FALLBACK_GENERIC_DARK -> AvatarGenerator.AvatarType.GENERIC_DARK;
        case M_IDENTICON, M_FALLBACK_IDENTICON -> AvatarGenerator.AvatarType.IDENTICON;
        case M_SQUARE, M_FALLBACK_SQUARE -> AvatarGenerator.AvatarType.SQUARE;
        case M_TRIANGLE, M_FALLBACK_TRIANGLE -> AvatarGenerator.AvatarType.TRIANGLE;
        case M_GITHUB, M_FALLBACK_GITHUB -> AvatarGenerator.AvatarType.GITHUB;
        default -> AvatarGenerator.AvatarType.GENERIC;
        };
        return type;
    }

}
