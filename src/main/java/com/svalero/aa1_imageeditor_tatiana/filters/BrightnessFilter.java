package com.svalero.aa1_imageeditor_tatiana.filters;

import com.svalero.aa1_imageeditor_tatiana.task.ImageFilter;
import java.awt.image.BufferedImage;

public class BrightnessFilter implements ImageFilter {

    private final int brightnessIncrease;

    public BrightnessFilter(int brightnessIncrease) {
        this.brightnessIncrease = brightnessIncrease;
    }

    @Override
    public BufferedImage apply(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null.");
        }

        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage brightImage = new BufferedImage(width, height, image.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);

                int alpha = (pixel >> 24) & 0xFF;
                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;

                int brightRed = Math.min(red + brightnessIncrease, 255);
                int brightGreen = Math.min(green + brightnessIncrease, 255);
                int brightBlue = Math.min(blue + brightnessIncrease, 255);

                int newPixel = (alpha << 24) | (brightRed << 16) | (brightGreen << 8) | brightBlue;

                brightImage.setRGB(x, y, newPixel);
            }
        }

        return brightImage;
    }
}
