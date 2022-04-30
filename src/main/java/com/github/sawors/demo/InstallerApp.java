package com.github.sawors.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class InstallerApp extends Application {
    
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(InstallerApp.class.getResource("installer-home.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 390);
        stage.setTitle("Simple Modpack Installer v"+version);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        stage.getIcons().add(new Image(Objects.requireNonNull(InstallerApp.class.getResourceAsStream("resource_img.png"))));
    }
    
    // DONT'T FORGET TO CHANGE THE VERSION IN THE POM.XML TOO !!! :P
    private static final String version = "1.3";
    
    public static void main(String[] args) {
        launch();
    }
    
    public static String getVersion(){
        return version;
    }
}