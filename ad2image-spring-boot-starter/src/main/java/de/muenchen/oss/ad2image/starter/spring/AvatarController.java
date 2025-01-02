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

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.muenchen.oss.ad2image.starter.core.AvatarLoader;
import de.muenchen.oss.ad2image.starter.core.ImageSize;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@Tag(name = "avatar", description = "the avatar API")
@OpenAPIDefinition(
        info = @Info(
                title = "ad2image API", description = "easy avatars powered by AD / Exchange & Identicons", version = "v1",
                contact = @Contact(name = "Munich Open Source", url = "https://opensource.muenchen.de/", email = "opensource@muenchen.de")
        ),
        externalDocs = @ExternalDocumentation(
                description = "More documentation and examples", url = "https://github.com/it-at-m/ad2image?tab=readme-ov-file#documentation"
        )
)
public class AvatarController {

    private static final Logger log = LoggerFactory.getLogger(AvatarController.class);

    private final AvatarService avatarService;

    public AvatarController(AvatarService avatarService) {
        super();
        this.avatarService = avatarService;
    }

    @Operation(summary = "Retrieve a users avatar image", description = "Retrieve a users avatar image")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200", description = "Successful operation",
                            content = { @Content(mediaType = "image/jpeg"), @Content(mediaType = "image/png") }
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "User not found or user has no avatar image",
                            content = @Content(schema = @Schema(implementation = Void.class))
                    )
            }
    )
    @GetMapping(value = "avatar", produces = { MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE })
    public ResponseEntity<byte[]> avatar(@Parameter(description = "uid of the user") @RequestParam(required = true) final String uid,
            @Parameter(description = "retrieval mode") @RequestParam(
                    name = "m", required = false, defaultValue = AvatarLoader.MODE_IDENTICON
            ) final String mode,
            @Parameter(description = "image size") @RequestParam(name = "size", required = false, defaultValue = "64") final String requestedSize) {
        log.info("Incoming avatar request for uid='{}', m='{}', size='{}'", uid, mode, requestedSize);
        ImageSize resolvedSize = resolveSize(requestedSize);
        byte[] jpegThumbnail = avatarService.get(uid, mode, resolvedSize);
        if (jpegThumbnail != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
            return ResponseEntity.ok()
                    // let the browser cache the avatar
                    .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
                    .headers(headers)
                    .body(jpegThumbnail);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private ImageSize resolveSize(String requestedSize) {
        ImageSize resolvedSize = ImageSize.getAdDefaultImageSize();
        try {
            Integer requestedSizeInteger = Integer.valueOf(requestedSize);
            EnumSet<ImageSize> allPossibleSizes = EnumSet.allOf(ImageSize.class);
            for (ImageSize imageSize : allPossibleSizes) {
                if (imageSize.getSizePixels() == requestedSizeInteger) {
                    return imageSize;
                }
            }
        } catch (NumberFormatException e) {
            log.warn("Could not resolve size parameter value '{}' to a valid ImageSize enum constant, using default size {}.", requestedSize, resolvedSize);
        }
        return resolvedSize;
    }

}
