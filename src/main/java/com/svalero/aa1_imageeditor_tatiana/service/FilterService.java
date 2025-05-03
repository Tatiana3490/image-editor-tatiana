package com.svalero.aa1_imageeditor_tatiana.service;

import com.svalero.aa1_imageeditor_tatiana.task.ImageFilter;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.awt.image.BufferedImage;

public class FilterService extends Service<BufferedImage> {

    private final BufferedImage inputImage;
    private final ImageFilter filter;

    public FilterService(BufferedImage inputImage, ImageFilter filter) {
        this.inputImage = inputImage;
        this.filter = filter;
    }

    @Override
    protected Task<BufferedImage> createTask() {
        return new Task<>() {
            @Override
            protected BufferedImage call() throws Exception {
                updateProgress(0.2, 1);
                BufferedImage result = filter.apply(inputImage);
                updateProgress(1, 1);
                return result;
            }
        };
    }
}
