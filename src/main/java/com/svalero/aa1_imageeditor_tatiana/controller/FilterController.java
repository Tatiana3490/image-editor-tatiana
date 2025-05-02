package com.svalero.aa1_imageeditor_tatiana.controller;

import com.svalero.aa1_imageeditor_tatiana.filters.*;
import com.svalero.aa1_imageeditor_tatiana.task.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FilterController {

    // 1. Componentes de la interfaz (UI)
    @FXML private ImageView originalImageView;
    @FXML private ImageView processedImageView;
    @FXML private ProgressBar progressBar;
    @FXML private Label lbStatus;
    @FXML private ListView<String> historyList;
    @FXML private ComboBox<String> filterSelector;
    @FXML private Spinner<Integer> threadSpinner;

    // 2. Estado de la apliación
    private File currentImageFile;
    private File lastOutputDir;
    private BufferedImage currentImage;
    private BufferedImage originalImage;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private static final String HISTORY_FILE = "history.txt";
    private final Deque<BufferedImage> undoStack = new ArrayDeque<>();
    private final List<BufferedImage> historyImages = new ArrayList<>();

    // 3. Inicialización
    @FXML
    public void initialize() {
        historyList.setItems(javafx.collections.FXCollections.observableArrayList());
        loadHistory();
        if (filterSelector != null) filterSelector.getSelectionModel().selectFirst();
        threadSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 16, 4));
    }

    // 4. Gestión de imagen individual
    @FXML
    public void loadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                Image image = new Image(file.toURI().toString());
                originalImageView.setImage(image);
                originalImage = ImageIO.read(file);
                currentImage = originalImage;
                currentImageFile = file;
                updateStatus("Imagen cargada: " + file.getName());
                addHistoryEntry("Imagen cargada: " + file.getName());
            } catch (IOException e) {
                e.printStackTrace();
                updateStatus("Error al cargar la imagen.");
            }
        }
    }

    @FXML
    public void saveImage() {
        if (processedImageView.getImage() == null) {
            updateStatus("No hay imagen procesada para guardar.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpeg")
        );
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(processedImageView.getImage(), null);
                ImageIO.write(bufferedImage, getFileExtension(file), file);
                updateStatus("Imagen guardada en: " + file.getAbsolutePath());
                addHistoryEntry("Imagen guardada: " + file.getName());
            } catch (IOException e) {
                updateStatus("Error al guardar la imagen.");
            }
        }
    }

    // 5. Aplicación de filtros
    @FXML
    public void applySelectedFilter() {
        if (filterSelector.getValue() == null) {
            updateStatus("Selecciona un filtro.");
            return;
        }
        ImageFilter filter;
        switch (filterSelector.getValue()) {
            case "Inversión de Color" -> filter = new ColorInversionFilter();
            case "Aumento de Brillo" -> filter = new BrightnessFilter(50);
            case "Escala de Grises" -> filter = new GrayScaleFilter();
            default -> {
                updateStatus("Filtro no válido.");
                return;
            }
        }
        applyFilter(filter, filterSelector.getValue());
    }

    private void applyFilter(ImageFilter filter, String name) {
        if (currentImage == null) {
            updateStatus("Por favor, carga una imagen primero.");
            return;
        }

        // Guardar el estado actual antes de aplicar el nuevo filtro
        undoStack.push(currentImage);
        historyImages.add(currentImage);

        FilterTask taskLogic = new FilterTask(currentImage, filter);

        Task<BufferedImage> task = new Task<>() {
            @Override
            protected BufferedImage call() throws Exception {
                updateProgress(0.2, 1);
                BufferedImage result = taskLogic.call();
                updateProgress(1, 1);
                return result;
            }
        };

        task.setOnSucceeded(event -> {
            currentImage = task.getValue();
            processedImageView.setImage(SwingFXUtils.toFXImage(currentImage, null));
            updateStatus("Filtro aplicado: " + name);
            addHistoryEntry("Filtro aplicado: " + name);
        });

        task.setOnFailed(event -> {
            updateStatus("Error al aplicar el filtro.");
            addHistoryEntry("Error con filtro: " + name);
        });

        progressBar.progressProperty().bind(task.progressProperty());
        executorService.submit(task);
    }


    @FXML
    public void resetToOriginal() {
        if (originalImage != null) {
            currentImage = originalImage;
            processedImageView.setImage(null);
            updateStatus("Imagen restaurada al estado original.");
            addHistoryEntry("Imagen restaurada al estado original.");
        } else {
            updateStatus("No hay imagen original cargada.");
        }
    }

    // 6. Procesamiento por carpeta (Batch Processing)
    @FXML
    public void processBatchFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Seleccionar carpeta de imágenes");
        File folder = chooser.showDialog(null);
        if (folder == null || !folder.isDirectory()) {
            updateStatus("No se seleccionó ninguna carpeta válida.");
            return;
        }

        originalImage = null;
        currentImage = null;
        Platform.runLater(() -> {
            originalImageView.setImage(null);
            processedImageView.setImage(null);
        });

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().matches(".*\\.(png|jpg|jpeg)$"));
        if (files == null || files.length == 0) {
            updateStatus("No se encontraron imágenes en la carpeta.");
            return;
        }

        File outputDir = new File(folder, "procesadas");
        if (!outputDir.exists()) outputDir.mkdirs();
        lastOutputDir = outputDir;

        int threadCount = threadSpinner.getValue();
        ExecutorService customPool = Executors.newFixedThreadPool(threadCount);
        updateStatus("Procesando " + files.length + " imágenes con " + threadCount + " hilos...");

        for (File file : files) {
            customPool.submit(() -> {
                try {
                    BufferedImage image = ImageIO.read(file);
                    ImageFilter filter;
                    switch (filterSelector.getSelectionModel().getSelectedItem()) {
                        case "Inversión de Color" -> filter = new ColorInversionFilter();
                        case "Aumento de Brillo" -> filter = new BrightnessFilter(50);
                        default -> filter = new GrayScaleFilter();
                    }
                    BufferedImage result = filter.apply(image);
                    Thread.sleep(3000);
                    File outputFile = new File(outputDir, "filtro_" + file.getName());
                    ImageIO.write(result, getFileExtension(file), outputFile);
                    Platform.runLater(() -> {
                        processedImageView.setImage(SwingFXUtils.toFXImage(result, null));
                        addHistoryEntry("Procesada: " + file.getName());
                        updateStatus("Procesada: " + file.getName());
                    });
                } catch (IOException | InterruptedException e) {
                    Platform.runLater(() -> updateStatus("Error procesando: " + file.getName()));
                }
            });
        }

        new Thread(() -> {
            customPool.shutdown();
            try {
                if (customPool.awaitTermination(1, java.util.concurrent.TimeUnit.MINUTES)) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Procesamiento finalizado");
                        alert.setHeaderText(null);
                        alert.setContentText("Todas las imágenes han sido procesadas.");
                        alert.showAndWait();
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

    }

    @FXML
    public void openProcessedFolder() {
        if (lastOutputDir != null && lastOutputDir.exists()) {
            try {
                Desktop.getDesktop().open(lastOutputDir);
            } catch (IOException e) {
                updateStatus("Error al abrir la carpeta.");
            }
        } else {
            updateStatus("No se ha procesado ninguna carpeta todavía.");
        }
    }

    // 7. Historial
    private void addHistoryEntry(String entry) {
        Platform.runLater(() -> {
            historyList.getItems().add(entry);
            saveHistoryEntry(entry);
        });
    }

    private void saveHistoryEntry(String entry) {
        try {
            Files.write(Paths.get(HISTORY_FILE), (entry + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Error al guardar historial: " + e.getMessage());
        }
    }

    private void loadHistory() {
        try {
            Path path = Paths.get(HISTORY_FILE);
            if (Files.exists(path)) {
                List<String> entries = Files.readAllLines(path, StandardCharsets.UTF_8);
                historyList.getItems().addAll(entries);
            }
        } catch (IOException e) {
            System.err.println("Error al cargar historial: " + e.getMessage());
        }
    }

    @FXML
    public void clearHistory() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Borrado");
        confirm.setHeaderText("¿Estás seguro de que quieres borrar el historial?");
        confirm.setContentText("Esta acción no se puede deshacer.");
        ButtonType yes = new ButtonType("Sí, borrar", ButtonBar.ButtonData.OK_DONE);
        ButtonType no = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(yes, no);
        confirm.showAndWait().ifPresent(response -> {
            if (response == yes) {
                try {
                    Files.write(Paths.get(HISTORY_FILE), new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
                } catch (IOException e) {
                    System.err.println("Error al borrar historial: " + e.getMessage());
                }
                Platform.runLater(() -> {
                    historyList.getItems().clear();
                    updateStatus("Historial borrado.");
                });
            } else {
                updateStatus("Operación cancelada. El historial sigue intacto.");
            }
        });
    }

    // 8. Utilidades
    private void updateStatus(String message) {
        Platform.runLater(() -> lbStatus.setText(message));
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        return (dotIndex == -1) ? "png" : name.substring(dotIndex + 1);
    }

    // 9. Deshacer, rehacer y revertir a versiones
    @FXML
    public void undoLastFilter() {
        if (!undoStack.isEmpty()) {
            currentImage = undoStack.pop();
            processedImageView.setImage(SwingFXUtils.toFXImage(currentImage, null));
            updateStatus("Filtro deshecho. Imagen anterior restaurada.");
            addHistoryEntry("Deshacer filtro");
        } else {
            updateStatus("No hay filtro que deshacer.");
        }
    }

    @FXML
    public void revertToPreviousVersion() {
        if (historyImages.isEmpty()) {
            updateStatus("No hay versiones anteriores disponibles.");
            return;
        }

        List<String> opciones = new ArrayList<>();
        for (int i = 0; i < historyImages.size(); i++) {
            opciones.add("Versión #" + (i + 1));
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(opciones.get(0), opciones);
        dialog.setTitle("Revertir a versión");
        dialog.setHeaderText("Selecciona una versión anterior");
        dialog.setContentText("Versiones disponibles:");

        dialog.showAndWait().ifPresent(seleccion -> {
            int index = opciones.indexOf(seleccion);
            currentImage = historyImages.get(index);
            processedImageView.setImage(SwingFXUtils.toFXImage(currentImage, null));
            updateStatus("Revertido a " + seleccion);
            addHistoryEntry("Revertido a " + seleccion);
        });

        undoStack.push(currentImage);
        historyImages.add(currentImage);

    }
}
