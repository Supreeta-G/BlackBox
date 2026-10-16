package com.example.blackbox.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1280, 850);
        scene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());

        primaryStage.setTitle("AI Agent Black Box — Flight Recorder & Cryptographic Evidence System");
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(750);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
