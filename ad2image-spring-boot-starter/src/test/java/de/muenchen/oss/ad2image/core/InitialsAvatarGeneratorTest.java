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

import de.muenchen.oss.ad2image.starter.core.InitialsAvatarGenerator;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

class InitialsAvatarGeneratorTest {

    @Test
    void nonBlankInitials_returnsValidJpegBytes() throws IOException {
        byte[] result = InitialsAvatarGenerator.generate("jdoe", "JD", 64);
        assertThat(result).isNotEmpty();
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(result));
        assertThat(img).isNotNull();
        Files.write(new File("target/initials_JD_64.jpg").toPath(), result);
    }

    @Test
    void blankInitials_returnsValidJpegBytes() throws IOException {
        byte[] result = InitialsAvatarGenerator.generate("jdoe", "", 64);
        assertThat(result).isNotEmpty();
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(result));
        assertThat(img).isNotNull();
        Files.write(new File("target/initials_blank_64.jpg").toPath(), result);
    }

    @Test
    void nullInitials_returnsValidJpegBytes() throws IOException {
        byte[] result = InitialsAvatarGenerator.generate("jdoe", null, 64);
        assertThat(result).isNotEmpty();
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(result));
        assertThat(img).isNotNull();
    }

    @Test
    void determinism_sameInputSameOutput() {
        byte[] first = InitialsAvatarGenerator.generate("jdoe", "JD", 64);
        byte[] second = InitialsAvatarGenerator.generate("jdoe", "JD", 64);
        assertThat(first).isEqualTo(second);
    }

}
