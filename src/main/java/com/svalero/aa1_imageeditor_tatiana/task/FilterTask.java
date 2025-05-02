package com.svalero.aa1_imageeditor_tatiana.task;

import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;

public class FilterTask implements Callable<BufferedImage> {

    private final BufferedImage image;
    private final ImageFilter filter;

    public FilterTask(BufferedImage image, ImageFilter filter) {
        this.image = image;
        this.filter = filter;
    }

    @Override
    public BufferedImage call() throws Exception {
        Thread.sleep(500);  // Para simular la carga
        return filter.apply(image);
    }
}
