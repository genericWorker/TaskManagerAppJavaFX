package edu.dccc.taskmanagerapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class TaskManagerApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TaskManagerApplication.class.getResource("taskmanager-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 920, 700);
        stage.setTitle("Task Manager");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}