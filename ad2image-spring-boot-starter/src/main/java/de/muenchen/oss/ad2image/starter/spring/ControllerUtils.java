package de.muenchen.oss.ad2image.starter.spring;

public class ControllerUtils {

    public static int getSizeInBounds(int requestedSize, int defaultSize, int maxSize) {
        int size = defaultSize;
        if (requestedSize > 0 && requestedSize <= maxSize) {
            size = requestedSize;
        } else if (requestedSize > maxSize) {
            size = maxSize;
        }
        return size;
    }

}
