package com.smarttaskscheduler.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainWindow mainWindow = new MainWindow();
        Scene scene = new Scene(mainWindow.getView(), 800, 600);
        
        primaryStage.setTitle("Smart Task Scheduler");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> mainWindow.onShutdown());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
