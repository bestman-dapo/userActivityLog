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
import org.example.useractivitylogger.StageUtils;
import org.example.useractivitylogger.services.AuthService;
import org.example.useractivitylogger.services.AuthService.LoginResult;
import org.example.useractivitylogger.sessions.UserSession;

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
        try {
            authService = new AuthService();

            // Load logo image (put logo.png in resources/images/)
            InputStream logoStream = getClass().getResourceAsStream("/logo_new.png");
            if (logoStream != null) {
                logoImageView.setImage(new Image(logoStream));
            } else {
                System.err.println("Logo image not found!");
            }

            loginButton.setOnAction(e -> handleLogin());

        } catch (Exception ex) {
            System.err.println("Error during initialization: " + ex.getMessage());
            ex.printStackTrace();
        }
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
            UserSession.initSession(result.id, result.username);
            messageLabel.setText("Login successful as " + result.role);
            openDashboard(result.role);
        } else {
            System.out.println(result.message);
            messageLabel.setText("Login failed: " + result.message);
        }
    }

    private void openDashboard(String role) {
        String fxmlFile = role.equals("admin")
                ? "/org/example/useractivitylogger/admin/admin_dashboard.fxml"
                : "/org/example/useractivitylogger/staff_dashboard.fxml";

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(role.substring(0, 1).toUpperCase() + role.substring(1) + " Dashboard");

            // ✅ Always maximize window
            stage.setMaximized(true);

            StageUtils.applyMinimumSize(stage);

            // OR for true fullscreen (optional)
            // stage.setFullScreen(true);
            // stage.setFullScreenExitHint(""); // removes the "press ESC to exit" hint

        } catch (IOException e) {
            messageLabel.setText("Failed to load dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void shutdown() {
        authService.close();
    }
}