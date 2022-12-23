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

@Controller
public class AvatarController {

    private static final Logger log = LoggerFactory.getLogger(AvatarController.class);

    private final AvatarService avatarService;

    public AvatarController(AvatarService avatarService) {
        super();
        this.avatarService = avatarService;
    }

    @GetMapping(value = "avatar", produces = { MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE })
    public ResponseEntity<byte[]> avatar(@RequestParam(required = true) final String uid,
            @RequestParam(name = "m", required = false, defaultValue = AvatarLoader.MODE_IDENTICON) final String mode,
            @RequestParam(name = "size", required = false, defaultValue = "64") final String requestedSize) {
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