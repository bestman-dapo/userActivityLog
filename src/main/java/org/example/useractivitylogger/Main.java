package org.example.useractivitylogger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import at.favre.lib.crypto.bcrypt.BCrypt;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/useractivitylogger/login.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.show();
    }

//    public static void main(String[] args) {
//        String password = "MySecurePassword123!";
//
//        // Hash the password with a cost factor of 12 (default)
//        String bcryptHash = BCrypt.withDefaults().hashToString(12, password.toCharArray());
//
//        System.out.println("Generated Hash: " + bcryptHash);
//    }

    public static void main(String[] args) {
        launch();
    }
}
