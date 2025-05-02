package com.svalero.aa1_imageeditor_tatiana.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class AppController {

    @FXML
    private ListView<String> historyList;

    @FXML
    private ImageView imageViewOriginal;

    @FXML
    private ImageView imageViewModified;

    private final ObservableList<String> historyItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        historyList.setItems(historyItems);
    }

    public void addHistoryEntry(String entry) {
        historyItems.add(entry);
    }

    public void showOriginalImage(Image image) {
        imageViewOriginal.setImage(image);
    }

    public void showModifiedImage(Image image) {
        imageViewModified.setImage(image);
    }
}
