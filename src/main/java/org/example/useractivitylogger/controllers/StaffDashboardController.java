package org.example.useractivitylogger.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableRow;
import javafx.stage.Stage;
import org.example.useractivitylogger.models.ActivityLog;
import org.example.useractivitylogger.services.ActivityService;
import org.example.useractivitylogger.sessions.UserSession;

import java.io.IOException;

public class StaffDashboardController {

    @FXML private Button clockInButton, clockOutButton, submitTaskButton;
    @FXML private TextArea taskSummaryArea;
    @FXML private TableView<ActivityLog> activityTable;
    @FXML private TableColumn<ActivityLog, String> colClockIn, colClockOut, colTaskSummary;
    @FXML private Label loggedInUserLabel;
    @FXML private Button logoutButton;

    private final ActivityService activityService = new ActivityService();
    private final ObservableList<ActivityLog> activityLogs = FXCollections.observableArrayList();

    private int userId = UserSession.getInstance().getUserId();

    @FXML
    public void initialize() {
        logoutButton.setOnAction(event -> handleLogout());
        logoutButton.setCursor(Cursor.HAND);

        String email = UserSession.getInstance().getUsername();
        loggedInUserLabel.setText(email);

        colClockIn.setCellValueFactory(data -> data.getValue().clockInProperty());
        colClockOut.setCellValueFactory(data -> data.getValue().clockOutProperty());
        colTaskSummary.setCellValueFactory(data -> data.getValue().taskSummaryProperty());

        activityTable.setItems(activityLogs);

        // Load logs
        loadActivityLogs();

        // Button actions
        clockInButton.setOnAction(e -> handleClockIn());
        clockOutButton.setOnAction(e -> handleClockOut());
        submitTaskButton.setOnAction(e -> handleTaskSubmit());

        updateButtonStates();

        // Row factory to add hover + click interactivity
        activityTable.setRowFactory(tv -> {
            TableRow<ActivityLog> row = new TableRow<>();

            // Hover effects
            row.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
                if (!row.isEmpty()) {
                    row.setCursor(isNowHovered ? Cursor.HAND : Cursor.DEFAULT);
                    row.setStyle(isNowHovered ? "-fx-background-color: #e0f7fa;" : "");
                }
            });

            // On click -> show modal
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    ActivityLog selectedLog = row.getItem();
                    showActivityDetailsModal(selectedLog);
                }
            });

            return row;
        });
    }

    private void showActivityDetailsModal(ActivityLog log) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Attendance Details");

        // Content
        StringBuilder content = new StringBuilder();
        content.append("Clock In: ").append(log.getClockIn()).append("\n");
        content.append("Clock Out: ").append(
                log.getClockOut() != null ? log.getClockOut() : "Not clocked out"
        ).append("\n\n");
        content.append("Task Submitted:\n").append(
                log.getTaskSummary() != null && !log.getTaskSummary().isEmpty()
                        ? log.getTaskSummary()
                        : "No task submitted."
        );

        TextArea detailsArea = new TextArea(content.toString());
        detailsArea.setEditable(false);
        detailsArea.setWrapText(true);

        dialog.getDialogPane().setContent(detailsArea);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    private void loadActivityLogs() {
        activityLogs.setAll(activityService.getLogsForUser(userId));
    }

    private void handleClockIn() {
        if (!activityService.hasClockedInToday(userId)) {
            activityService.clockIn(userId);
            loadActivityLogs();
        } else {
            showAlert("You have already clocked in today.");
        }
        updateButtonStates();
    }

    private void handleClockOut() {
        if (!activityService.hasClockedInToday(userId)) {
            showAlert("You must clock in first.");
            return;
        }
        if (activityService.hasClockedOutToday(userId)) {
            showAlert("You have already clocked out today.");
            return;
        }
        if (!activityService.hasSubmittedTaskToday(userId)) {
            showAlert("Please submit your task summary before clocking out.");
            return;
        }
        activityService.clockOut(userId);
        loadActivityLogs();
        updateButtonStates();
    }

    private void handleTaskSubmit() {
        if (!activityService.hasClockedInToday(userId)) {
            showAlert("You need to clock in first.");
            return;
        }
        String task = taskSummaryArea.getText().trim();
        if (!task.isEmpty()) {
            activityService.submitTask(userId, task);
            taskSummaryArea.clear();
            loadActivityLogs();
        } else {
            showAlert("Task summary cannot be empty.");
        }
        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean clockedIn = activityService.hasClockedInToday(userId);
        boolean clockedOut = activityService.hasClockedOutToday(userId);
        boolean taskSubmitted = activityService.hasSubmittedTaskToday(userId);

        clockInButton.setDisable(clockedIn);
        submitTaskButton.setDisable(!clockedIn || taskSubmitted);
        clockOutButton.setDisable(!clockedIn || clockedOut || !taskSubmitted);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleLogout() {
        UserSession.clearSession();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/useractivitylogger/login.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
