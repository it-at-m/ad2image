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

import de.muenchen.oss.ad2image.starter.core.Ad2ImageConfigurationProperties;
import de.muenchen.oss.ad2image.starter.core.ImageSize;
import de.muenchen.oss.ad2image.starter.core.Mode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
@Tag(name = "gravatar", description = "gravatar compatible API")
public class GravatarController {

    private static final Logger log = LoggerFactory.getLogger(GravatarController.class);

    private final AvatarService avatarService;
    private final Ad2ImageConfigurationProperties confProps;
    private final GravatarHashMapService gravatarHashMapService;

    public GravatarController(AvatarService avatarService, Ad2ImageConfigurationProperties confProps, GravatarHashMapService gravatarHashMapService) {
        super();
        this.avatarService = avatarService;
        this.confProps = confProps;
        this.gravatarHashMapService = gravatarHashMapService;
    }

    @Operation(summary = "Retrieve a users avatar image", description = "Retrieve a users avatar image based on their email hash")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200", description = "Successful operation",
                            content = { @Content(mediaType = "image/jpeg"), @Content(mediaType = "image/png") }
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "User not found or user has no image",
                            content = @Content(schema = @Schema(implementation = Void.class))
                    )
            }
    )
    @GetMapping(value = "gravatar/{mailhash}", produces = { MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE })
    public ResponseEntity<byte[]> avatar(
            @Parameter(
                    description = "SHA256 hash of users email address", example = "27205e5c51cb03f862138b22bcb5dc20f94a342e744ff6df1b8dc8af3c865109",
                    required = true
            ) @PathVariable(name = "mailhash") String sha256MailHash,
            @Parameter(
                    description = "d", schema = @Schema(
                            example = "identicon",
                            allowableValues = { "404", "identicon" }
                    )
            ) @RequestParam(
                    name = "d", required = false
            ) final String dParam,
            @Parameter(hidden = true) @RequestParam(
                    name = "default", required = false
            ) final String defaultParam,
            @Parameter(
                    schema = @Schema(
                            name = "s",
                            description = "image size",
                            defaultValue = "80",
                            example = "80",
                            minimum = "1",
                            maximum = "2048"
                    )
            ) @RequestParam(name = "size", required = false) final Integer requestedSParam,
            @RequestParam(name = "size", required = false) final Integer requestedSizeParam) {
        String requestedDefault = dParam != null ? dParam : defaultParam;
        Integer requestedSize = requestedSParam != null ? requestedSParam : requestedSizeParam;
        if (requestedSize == null) {
            requestedSize = 80;
        }
        if (requestedSize <= 0) {
            requestedSize = 80;
        }
        if (requestedSize > 2048) {
            requestedSize = 2048;
        }
        log.debug("Incoming gravatar request for sha256MailHash='{}', d='{}', s='{}'", sha256MailHash, requestedDefault, requestedSize);
        ImageSize resolvedSize = resolveSize(requestedSize);
        Mode resolvedMode = resolveMode(requestedDefault);
        String uid = gravatarHashMapService.getUidForMailHash(sha256MailHash.toLowerCase());
        if (uid != null) {
            log.info("Incoming gravatar request for sha256MailHash='{}', d='{}', size='{}' - resolved to uid='{}'", sha256MailHash, requestedDefault,
                    resolvedSize,
                    uid);
            byte[] jpegThumbnail = avatarService.get(uid, resolvedMode, resolvedSize);
            if (jpegThumbnail != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
                return ResponseEntity.ok()
                        // let the browser cache the avatar
                        .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
                        .headers(headers)
                        .body(jpegThumbnail);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);

    }

    private Mode resolveMode(String defaultParam) {
        Mode resolvedMode = confProps.getDefaultMode();
        if (defaultParam == null) {
            return resolvedMode;
        }
        if (defaultParam.equals(Mode.M_404.getParameterValue())) {
            return Mode.M_404;
        } else if (defaultParam.equals(Mode.M_IDENTICON.getParameterValue())) {
            return Mode.M_FALLBACK_IDENTICON;
        }
        return resolvedMode;
    }

    private ImageSize resolveSize(Integer requestedSizeInteger) {
        ImageSize adDefaultImageSize = ImageSize.getAdDefaultImageSize();
        EnumSet<ImageSize> allPossibleSizes = EnumSet.allOf(ImageSize.class);
        List<Integer> allPossibleSizesList = allPossibleSizes.stream().map(ImageSize::getSizePixels).toList();
        int nearestSize = findNearestSize(allPossibleSizesList, requestedSizeInteger);
        for (ImageSize imageSize : allPossibleSizes) {
            if (imageSize.getSizePixels() == nearestSize) {
                return imageSize;
            }
        }
        return adDefaultImageSize;
    }

    private static int findNearestSize(List<Integer> sizes, int targetSize) {
        int nearestSize = sizes.getFirst();
        int minDifference = Math.abs(targetSize - nearestSize);

        for (int size : sizes) {
            int difference = Math.abs(targetSize - size);
            if (difference < minDifference) {
                minDifference = difference;
                nearestSize = size;
            }
        }

        return nearestSize;
    }

}
