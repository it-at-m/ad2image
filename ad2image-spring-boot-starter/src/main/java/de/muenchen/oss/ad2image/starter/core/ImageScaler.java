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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageScaler {

    private static final Logger log = LoggerFactory.getLogger(ImageScaler.class);

    public static byte[] scaleImage(byte[] originalImageBytes, int targetWidth, int targetHeight) throws IOException {
        log.debug("Scaling image to {}x{} px / png format...", targetWidth, targetHeight);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(originalImageBytes); ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
            BufferedImage originalImage = ImageIO.read(bais);

            Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = outputImage.createGraphics();

            // Fill the background with white
            //            g2d.setColor(Color.WHITE);
            //            g2d.fillRect(0, 0, targetWidth, targetHeight);

            g2d.drawImage(scaledImage, 0, 0, null);
            g2d.dispose();

            ImageIO.write(outputImage, "png", baos);
            return baos.toByteArray();
        }
    }
}
