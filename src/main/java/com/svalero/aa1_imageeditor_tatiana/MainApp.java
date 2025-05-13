package com.svalero.aa1_imageeditor_tatiana;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.io.IOException;


public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Splash screen
        FXMLLoader splashLoader = new FXMLLoader(getClass().getResource("splash.fxml"));
        Scene splashScene = new Scene(splashLoader.load());
        Stage splashStage = new Stage();
        splashStage.setScene(splashScene);
        splashStage.setTitle("Cargando...");
        splashStage.show();

        // Espera 2 segundos y lanza la app principal
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> {
            try {
                FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("main_layout.fxml"));
                Scene mainScene = new Scene(mainLoader.load());
                primaryStage.setScene(mainScene);
                primaryStage.setTitle("Editor de Imágenes Multihilo");
                primaryStage.show();
                splashStage.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        pause.play();
    }


    public static void main(String[] args) {
        launch();
    }
}
