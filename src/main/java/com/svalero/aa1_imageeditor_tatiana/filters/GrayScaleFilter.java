package com.svalero.aa1_imageeditor_tatiana.filters;

import com.svalero.aa1_imageeditor_tatiana.task.ImageFilter;

import java.awt.image.BufferedImage;

public class GrayScaleFilter implements ImageFilter {
    @Override
    public BufferedImage apply(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null.");
        }

        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage grayImage = new BufferedImage(width, height, image.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);

                int alpha = (pixel >> 24) & 0xFF;
                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;

                // Luminance formula: más fiel a cómo percibimos el gris
                int gray = (int)(0.299 * red + 0.587 * green + 0.114 * blue);

                int newPixel = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
                grayImage.setRGB(x, y, newPixel);
            }
        }

        return grayImage;
    }
}
