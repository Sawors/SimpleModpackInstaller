package com.github.sawors.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    
    private static Stage st2;
    private static TextArea outputmsg;
    public static TextArea installtextarea = new TextArea();
    
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("installer-home.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 390);
        stage.setTitle("Simple Modpack Installer v1.1");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        st2 = stage;
    }
    
    public static void main(String[] args) {
        launch();
    }
    
    public static Stage getStage(){
        return st2;
    }
    
    public static void setInstallTextArea(TextArea area){
        installtextarea = area;
    }
    
    public static TextArea getInstallTextArea(){
        return installtextarea;
    }
}