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

import de.muenchen.oss.ad2image.starter.core.AvatarGenerator;
import de.muenchen.oss.ad2image.starter.core.ImageSize;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author michael.prankl
 *
 */
class AvatarGeneratorTest {

    private AvatarGenerator sut = new AvatarGenerator();

    @Test
    void default_size_identicon() throws IOException {
        String uid = "maxi.mustermann";
        byte[] loadAvatar = sut.generateAvatar(uid, AvatarGenerator.AvatarType.IDENTICON, ImageSize.getAdDefaultImageSize().getSizePixels());
        assertThat(loadAvatar).isNotEmpty();
        Files.write(new File("target/" + uid + "_64.jpg").toPath(), loadAvatar);
    }

    @Test
    void smaller_than_default_size_identicon() throws IOException {
        String uid = "maxi.mustermann";
        byte[] loadAvatar = sut.generateAvatar(uid, AvatarGenerator.AvatarType.IDENTICON, 32);
        assertThat(loadAvatar).isNotEmpty();
        Files.write(new File("target/" + uid + "_32.jpg").toPath(), loadAvatar);
    }

    @Test
    void bigger_size_identicon() throws IOException {
        String uid = "maxi.mustermann";
        byte[] loadAvatar = sut.generateAvatar(uid, AvatarGenerator.AvatarType.IDENTICON, 648);
        assertThat(loadAvatar).isNotEmpty();
        Files.write(new File("target/" + uid + "_648.jpg").toPath(), loadAvatar);
    }

    @Test
    void generic_dark_mode() throws IOException {
        String uid = "test.user";
        byte[] loadAvatar = sut.generateAvatar(uid, AvatarGenerator.AvatarType.GENERIC_DARK, ImageSize.HR648.getSizePixels());
        assertThat(loadAvatar).isNotEmpty();
        Files.write(new File("target/generic_dark_648.png").toPath(), loadAvatar);
    }

    @Test
    void generic() throws IOException {
        String uid = "test.user";
        byte[] loadAvatar = sut.generateAvatar(uid, AvatarGenerator.AvatarType.GENERIC, ImageSize.HR648.getSizePixels());
        assertThat(loadAvatar).isNotEmpty();
        Files.write(new File("target/generic_648.png").toPath(), loadAvatar);
    }

}
