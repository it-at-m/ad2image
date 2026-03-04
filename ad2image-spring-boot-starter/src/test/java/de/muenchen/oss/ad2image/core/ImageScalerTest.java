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

import com.talanlabs.avatargenerator.Avatar;
import com.talanlabs.avatargenerator.GitHubAvatar;
import com.talanlabs.avatargenerator.IdenticonAvatar;
import de.muenchen.oss.ad2image.starter.core.ImageScaler;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ImageScalerTest {

    @Test
    void scale_png_with_transparency() throws IOException {
        byte[] inputPng = StreamUtils.copyToByteArray(new ClassPathResource("account.png").getInputStream());
        byte[] scaledImage = ImageScaler.scaleImage(inputPng, 256, 256);

        Files.write(new File("target/scale_256.png").toPath(), scaledImage);
    }

    @Test
    void scale_png_with_transparency_dark() throws IOException {
        byte[] inputPng = StreamUtils.copyToByteArray(new ClassPathResource("account_dark.png").getInputStream());
        byte[] scaledImage = ImageScaler.scaleImage(inputPng, 256, 256);

        Files.write(new File("target/scale_256_dark.png").toPath(), scaledImage);
    }

    @Test
    void scale_png_avatar() throws IOException {
        Avatar avatar = IdenticonAvatar.newAvatarBuilder().size(64, 64).build();
        byte[] inputPng = avatar.createAsPngBytes("tmpuser".hashCode());
        byte[] scaledImage = ImageScaler.scaleImage(inputPng, 256, 256);

        Files.write(new File("target/identicon_256.png").toPath(), scaledImage);
    }

    @Test
    void scale_png_avatar_github() throws IOException {
        Avatar avatar = GitHubAvatar.newAvatarBuilder().size(64, 64).build();
        byte[] inputPng = avatar.createAsPngBytes("tmpuser".hashCode());
        byte[] scaledImage = ImageScaler.scaleImage(inputPng, 256, 256);

        Files.write(new File("target/github_256.png").toPath(), scaledImage);
    }
}
