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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

public class InitialsAvatarGenerator {

    private InitialsAvatarGenerator() {
    }

    /**
     * Generates a square JPEG avatar image for the given uid and initials.
     * The background color is deterministically derived from the uid's hash code using HSL
     * (hue = uid.hashCode() % 360, saturation = 60%, lightness = 45%).
     * If initials is blank or null, a plain colored rectangle is returned (no text).
     * Text is always white.
     *
     * @param uid the user's uid (used as color seed)
     * @param initials the initials to render (e.g. "JD"), or blank/null for a plain rectangle
     * @param size the image size in pixels (width = height)
     * @return JPEG bytes of the generated image
     */
    public static byte[] generate(String uid, String initials, int size) {
        int seed = uid.hashCode() & Integer.MAX_VALUE; // ensure non-negative
        float hue = (seed % 360) / 360.0f;
        Color bg = hslToRgb(hue, 0.60f, 0.45f);

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        try {
            g2.setColor(bg);
            g2.fillRect(0, 0, size, size);

            if (initials != null && !initials.isBlank()) {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(1, (int) (size * 0.4f))));
                FontMetrics fm = g2.getFontMetrics();
                int x = (size - fm.stringWidth(initials)) / 2;
                int y = (size - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(initials, x, y);
            }
        } finally {
            g2.dispose();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "JPEG", baos);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate initials avatar", e);
        }
        return baos.toByteArray();
    }

    private static Color hslToRgb(float h, float s, float l) {
        float c = (1 - Math.abs(2 * l - 1)) * s;
        float x = c * (1 - Math.abs((h * 6) % 2 - 1));
        float m = l - c / 2;

        float r, g, b;
        switch ((int) (h * 6) % 6) {
        case 0 -> {
            r = c;
            g = x;
            b = 0;
        }
        case 1 -> {
            r = x;
            g = c;
            b = 0;
        }
        case 2 -> {
            r = 0;
            g = c;
            b = x;
        }
        case 3 -> {
            r = 0;
            g = x;
            b = c;
        }
        case 4 -> {
            r = x;
            g = 0;
            b = c;
        }
        default -> {
            r = c;
            g = 0;
            b = x;
        }
        }

        return new Color(
                Math.clamp(Math.round((r + m) * 255), 0, 255),
                Math.clamp(Math.round((g + m) * 255), 0, 255),
                Math.clamp(Math.round((b + m) * 255), 0, 255));
    }

}
