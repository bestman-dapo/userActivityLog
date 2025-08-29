package org.example.useractivitylogger.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.example.useractivitylogger.models.ActivityLog;
import org.example.useractivitylogger.services.DatabaseService;
import org.example.useractivitylogger.sessions.UserSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminDashboardController {

    @FXML
    private Button logoutButton;

    @FXML
    private Label loggedInUserLabel;

    @FXML
    private Button menuDashboard;

    @FXML
    private Button menuManageStaff;

    @FXML
    private Button menuReports;

    @FXML
    private Button menuSettings;

    @FXML
    private Button toggleSidebarBtn;

    @FXML
    private StackPane mainContent;

    @FXML
    private AnchorPane sidebar;
    @FXML
    private Label lblTotalStaff;

    @FXML
    private Label lblPresentToday;

    @FXML
    private Label lblAbsentToday;

    @FXML
    private TableView<ActivityLog> tblRecentAttendance;

    @FXML
    private TableColumn<ActivityLog, String> colStaffName;

    @FXML
    private TableColumn<ActivityLog, String> colClockIn;

    @FXML
    private TableColumn<ActivityLog, String> colClockOut;

    @FXML
    private PieChart attendancePieChart;

    @FXML
    private LineChart<String, Number> activityTrendChart;


    private boolean isSidebarCollapsed = false;

    @FXML
    public void initialize() {
        // ✅ Show logged-in user email
        String email = UserSession.getInstance().getUsername();
        loggedInUserLabel.setText(email);

        // ✅ Logout button handler
        logoutButton.setOnAction(event -> handleLogout());
        logoutButton.setCursor(Cursor.HAND);

        // ✅ Sidebar toggle button handler
        toggleSidebarBtn.setOnAction(event -> toggleSidebar());
        toggleSidebarBtn.setCursor(Cursor.HAND);

        // ✅ Menu button handlers
        menuDashboard.setOnAction(event -> loadView("/org/example/useractivitylogger/admin/dashboard.fxml"));
        menuManageStaff.setOnAction(event -> loadView("/org/example/useractivitylogger/admin/manage_staff.fxml"));
        menuReports.setOnAction(event -> loadView("/org/example/useractivitylogger/admin/reports.fxml"));
        menuSettings.setOnAction(event -> loadView("/org/example/useractivitylogger/admin/settings.fxml"));

        // ✅ Load default view (Dashboard)
        loadView("/org/example/useractivitylogger/admin/dashboard.fxml");

    }

    private void updateTotalStaff() {
        Connection connection = DatabaseService.getConnection();
        if (connection == null) return;

        String sql = "SELECT COUNT(*) AS total FROM users WHERE role = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "staff");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int totalStaff = rs.getInt("total");
                lblTotalStaff.setText(String.valueOf(totalStaff));
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblTotalStaff.setText("N/A");
        }
    }
    private void handleLogout() {
        // ✅ Clear session and go back to login
        UserSession.getInstance().clearSession();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/useractivitylogger/login.fxml"));
            Node root = loader.load();
            logoutButton.getScene().setRoot((Parent) root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            // Replace content
            mainContent.getChildren().setAll(view);

            // Access the controller of the loaded FXML
            Object controller = loader.getController();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void toggleSidebar() {
        if (isSidebarCollapsed) {
            sidebar.setPrefWidth(250);
            isSidebarCollapsed = false;
        } else {
            sidebar.setPrefWidth(60);
            isSidebarCollapsed = true;
        }
    }
}
