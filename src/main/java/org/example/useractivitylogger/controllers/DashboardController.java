package org.example.useractivitylogger.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.useractivitylogger.services.DatabaseService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.example.useractivitylogger.models.ActivityLog;

public class DashboardController {

    public Connection connection = DatabaseService.getConnection();

    public Label lblPresentToday;
    public Label lblAbsentToday;
    @FXML
    private Label lblTotalStaff;

    private int lblTotalStaffPresent;
    private int totalStaff;

    @FXML
    private TableView<ActivityLog> tblRecentAttendance;
    @FXML
    private TableColumn<ActivityLog, String> colStaffName;
    @FXML
    private TableColumn<ActivityLog, String> colClockIn;
    @FXML
    private TableColumn<ActivityLog, String> colClockOut;
    @FXML
    private TableColumn<ActivityLog, String> colTaskSummary;

    @FXML
    public void initialize() {
        updateTotalStaff();
        updatePresentAbsentToday();

        // Link table columns to model properties
        colStaffName.setCellValueFactory(cellData -> cellData.getValue().staffNameProperty());
        colClockIn.setCellValueFactory(cellData -> cellData.getValue().clockInProperty());
        colClockOut.setCellValueFactory(cellData -> cellData.getValue().clockOutProperty());
        colTaskSummary.setCellValueFactory(cellData -> cellData.getValue().taskSummaryProperty());

        // Load data
        loadRecentAttendance();
    }

    public void updateTotalStaff() {
        String sql = "SELECT COUNT(*) AS total FROM users WHERE role = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "staff");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                lblTotalStaff.setText(String.valueOf(rs.getInt("total")));
                this.totalStaff = rs.getInt("total");
            }
        } catch (Exception e) {
            lblTotalStaff.setText("N/A");
            e.printStackTrace();
        }
    }

    public void updatePresentAbsentToday() {
        String sql = "SELECT COUNT(*) AS total FROM user_activity_log WHERE clock_in_time > CURDATE()";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                lblPresentToday.setText(String.valueOf(rs.getInt("total")));
                this.lblTotalStaffPresent = rs.getInt("total");
            }
        } catch (Exception e) {
            lblPresentToday.setText("N/A");
            e.printStackTrace();
        }

        lblAbsentToday.setText(String.valueOf(this.totalStaff - this.lblTotalStaffPresent));
    }

    private void loadRecentAttendance() {
        ObservableList<ActivityLog> data = FXCollections.observableArrayList();

        String query = "SELECT u.username, ual.clock_in_time, ual.clock_out_time, ual.task_summary " +
                "FROM user_activity_log ual " +
                "JOIN users u ON ual.user_id = u.id " +
                "ORDER BY ual.clock_in_time DESC " +
                "LIMIT 10";

        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("username");
                String clockIn = rs.getString("clock_in_time");
                String clockOut = rs.getString("clock_out_time");
                String taskSummary = rs.getString("task_summary");

                // Create a proper ActivityLog object
                ActivityLog log = new ActivityLog(name, clockIn, clockOut, taskSummary);
                data.add(log);
            }

            tblRecentAttendance.setItems(data);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
