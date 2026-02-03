package org.example.useractivitylogger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/useractivitylogger/login.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setTitle("Login");
        stage.setScene(scene);

        // ✅ allow resizing
        stage.setMaximized(true);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
