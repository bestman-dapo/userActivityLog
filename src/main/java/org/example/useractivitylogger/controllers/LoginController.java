//package org.example.useractivitylogger.controllers;
//
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.stage.Stage;
//import org.example.useractivitylogger.services.AuthService;
//import org.example.useractivitylogger.services.AuthService.LoginResult;
//
//import java.io.IOException;
//
//public class LoginController {
//
//    @FXML
//    private TextField usernameField;
//
//    @FXML
//    private PasswordField passwordField;
//
//    @FXML
//    private Button loginButton;
//
//    @FXML
//    private Label messageLabel;
//
//    private AuthService authService;
//
//    public void initialize() {
//        authService = new AuthService();
//        loginButton.setOnAction(e -> handleLogin());
//    }
//
//    private void handleLogin() {
//        String username = usernameField.getText().trim();
//        String password = passwordField.getText().trim();
//
//        if (username.isEmpty() || password.isEmpty()) {
//            messageLabel.setText("Please enter both username and password.");
//            return;
//        }
//
//        LoginResult result = authService.login(username, password);
//
//        if (result.success) {
//            messageLabel.setText("Login successful as " + result.role);
//            openDashboard(result.role);
//        } else {
//            messageLabel.setText("Login failed: " + result.message);
//        }
//    }
//
//    private void openDashboard(String role) {
//        String fxmlFile = role.equals("admin") ? "/fxml/admin_dashboard.fxml" : "/fxml/staff_dashboard.fxml";
//
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
//            Scene scene = new Scene(loader.load());
//            Stage stage = (Stage) loginButton.getScene().getWindow();
//            stage.setScene(scene);
//            stage.setTitle(role.substring(0, 1).toUpperCase() + role.substring(1) + " Dashboard");
//        } catch (IOException e) {
//            messageLabel.setText("Failed to load dashboard.");
//            e.printStackTrace();
//        }
//    }
//
//    public void shutdown() {
//        authService.close();
//    }
//}
package org.example.useractivitylogger.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.useractivitylogger.services.AuthService;
import org.example.useractivitylogger.services.AuthService.LoginResult;

import java.io.IOException;
import java.io.InputStream;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label messageLabel;

    @FXML
    private ImageView logoImageView;

    private AuthService authService;

    public void initialize() {
        authService = new AuthService();

        // Load logo image (put logo.png in resources/images/)
        InputStream logoStream = getClass().getResourceAsStream("/images/logo.png");
        if (logoStream != null) {
            logoImageView.setImage(new Image(logoStream));
        }

        loginButton.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password.");
            return;
        }

        LoginResult result = authService.login(username, password);

        if (result.success) {
            messageLabel.setText("Login successful as " + result.role);
            openDashboard(result.role);
        } else {
            messageLabel.setText("Login failed: " + result.message);
        }
    }

    private void openDashboard(String role) {
        String fxmlFile = role.equals("admin") ? "/fxml/admin_dashboard.fxml" : "/fxml/staff_dashboard.fxml";

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(role.substring(0, 1).toUpperCase() + role.substring(1) + " Dashboard");
        } catch (IOException e) {
            messageLabel.setText("Failed to load dashboard.");
            e.printStackTrace();
        }
    }

    public void shutdown() {
        authService.close();
    }
}
