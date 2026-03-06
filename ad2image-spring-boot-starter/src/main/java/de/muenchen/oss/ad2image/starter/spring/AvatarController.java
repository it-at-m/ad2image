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

import de.muenchen.oss.ad2image.starter.core.Ad2ImageConfigurationProperties;
import de.muenchen.oss.ad2image.starter.core.Mode;
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

    private final Ad2ImageConfigurationProperties confProps;

    public AvatarController(AvatarService avatarService, Ad2ImageConfigurationProperties confProps) {
        super();
        this.avatarService = avatarService;
        this.confProps = confProps;
    }

    @Operation(summary = "Retrieve a users avatar image", description = "Retrieve a users avatar image")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200", description = "Successful operation",
                            content = { @Content(mediaType = "image/png") }
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "User not found or user has no avatar image",
                            content = @Content(schema = @Schema(implementation = Void.class))
                    )
            }
    )
    @GetMapping(value = "avatar", produces = { MediaType.IMAGE_PNG_VALUE })
    public ResponseEntity<byte[]> avatar(
            @Parameter(description = "uid of the user", example = "john.doe", required = true) @RequestParam final String uid,
            @Parameter(
                    description = """
                            retrieval mode - possible values:

                            - `404`: 404 Response, if the user has no photo stored in AD/Exchange
                            - `identicon`: renders an [Identicon](https://en.wikipedia.org/wiki/Identicon), if the user has no photo stored in
                              AD/Exchange
                            - `fallbackIdenticon`: identical to `identicon`, but also responds with an Identicon if the user itself does not exist
                              in AD/Exchange
                            - `generic`: renders an [generic placeholder icon](ad2image-spring-boot-starter/src/main/resources/account_64.png), if
                              the user has no photo stored in AD/Exchange
                            - `fallbackGeneric`: **default** - identical to `generic`, but also responds with an generic placeholder icon if the
                              user itself does not exist in AD/Exchange
                            - `genericDark`: renders a [generic placeholder icon with white foreground](ad2image-spring-boot-starter/src/main/resources/account_dark.png) (for dark mode applications), if
                              the user has no photo stored in AD/Exchange
                            - `fallbackGenericDark`: identical to `genericDark`, but also responds with a generic placeholder icon with white foreground if the
                              user itself does not exist in AD/Exchange
                            - `triangle`: renders
                              an [randomly generated Avatar based on triangles](https://raw.githubusercontent.com/gabrie-allaigre/avatar-generator/master/doc/triangle1.png),
                              if the user has no photo stored in AD/Exchange
                            - `fallbackTriangle`: identical to `triangle`, but also responds correspondingly if the user itself does not exist in
                              AD/Exchange
                            - `square`: renders
                              an [randomly generated Avatar based on squares](https://raw.githubusercontent.com/gabrie-allaigre/avatar-generator/master/doc/square1.png),
                              if the user has no photo stored in AD/Exchange
                            - `fallbackSquare`: identical to `square`, but also responds correspondingly if the user itself does not exist in
                              AD/Exchange
                            - `github`: renders
                              an [randomly generated Avatar based Github avatar style](https://raw.githubusercontent.com/gabrie-allaigre/avatar-generator/master/doc/github2.png),
                              if the user has no photo stored in AD/Exchange
                            - `fallbackGithub`: identical to `github`, but also responds correspondingly if the user itself does not exist in
                              AD/Exchange
                            """,
                    example = "fallbackGeneric", schema = @Schema(
                            allowableValues = { "404", "identicon", "fallbackIdenticon", "generic", "fallbackGeneric", "genericDark", "fallbackGenericDark",
                                    "triangle",
                                    "fallbackTriangle", "square", "fallbackSquare", "github", "fallbackGithub" },
                            defaultValue = "fallbackGeneric"
                    )
            ) @RequestParam(
                    name = "m", required = false
            ) final String mode,
            @Parameter(
                    description = "image size", schema = @Schema(
                            defaultValue = "64",
                            example = "64",
                            minimum = "1",
                            maximum = "2048"
                    )
            ) @RequestParam(name = "size", required = false, defaultValue = "64") final int requestedSize) {
        log.info("Incoming avatar request for uid='{}', m='{}', size='{}'", uid, mode, requestedSize);
        int size = ControllerUtils.getSizeInBounds(requestedSize, ImageSize.getAdDefaultImageSize().getSizePixels(), 2048);
        Mode resolvedMode = resolveMode(mode);
        byte[] jpegThumbnail = avatarService.get(uid, resolvedMode, size);
        if (jpegThumbnail != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE);
            return ResponseEntity.ok()
                    // let the browser cache the avatar
                    .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
                    .headers(headers)
                    .body(jpegThumbnail);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private Mode resolveMode(String mode) {
        Mode resolvedMode = confProps.getDefaultMode();
        EnumSet<Mode> allPossibleModes = EnumSet.allOf(Mode.class);
        for (Mode possibleMode : allPossibleModes) {
            if (possibleMode.getParameterValue().equalsIgnoreCase(mode)) {
                return possibleMode;
            }
        }
        return resolvedMode;
    }

}
