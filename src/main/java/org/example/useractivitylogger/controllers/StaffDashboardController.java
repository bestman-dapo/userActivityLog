package org.example.useractivitylogger.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.useractivitylogger.models.ActivityLog;
import org.example.useractivitylogger.services.ActivityService;

import java.util.Optional;

public class StaffDashboardController {

    @FXML private Button clockInButton, clockOutButton, submitTaskButton;
    @FXML private TextArea taskSummaryArea;
    @FXML private TableView<ActivityLog> activityTable;
    @FXML private TableColumn<ActivityLog, String> colClockIn, colClockOut, colTaskSummary;

    private final ActivityService activityService = new ActivityService();
    private final ObservableList<ActivityLog> activityLogs = FXCollections.observableArrayList();

    private int userId = 1; // Example, should be set from login

    @FXML
    public void initialize() {
        colClockIn.setCellValueFactory(data -> data.getValue().clockInProperty());
        colClockOut.setCellValueFactory(data -> data.getValue().clockOutProperty());
        colTaskSummary.setCellValueFactory(data -> data.getValue().taskSummaryProperty());

        activityTable.setItems(activityLogs);

        loadActivityLogs();

        clockInButton.setOnAction(e -> handleClockIn());
        clockOutButton.setOnAction(e -> handleClockOut());
        submitTaskButton.setOnAction(e -> handleTaskSubmit());

        updateButtonStates();
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


}
