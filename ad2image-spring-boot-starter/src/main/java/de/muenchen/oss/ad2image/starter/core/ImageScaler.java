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
        log.debug("Scaling image to {}x{} px / jpg format...", targetWidth, targetHeight);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(originalImageBytes); ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
            BufferedImage originalImage = ImageIO.read(bais);

            Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);

            Graphics2D g2d = outputImage.createGraphics();

            // Fill the background with white
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, targetWidth, targetHeight);

            g2d.drawImage(scaledImage, 0, 0, null);
            g2d.dispose();

            ImageIO.write(outputImage, "jpg", baos);
            return baos.toByteArray();
        }
    }
}
