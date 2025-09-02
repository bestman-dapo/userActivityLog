package org.example.useractivitylogger.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.useractivitylogger.services.DatabaseService;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;

public class ReportsController {

    @FXML private ComboBox<String> cmbStaffFilter;
    @FXML private Button btnApplyFilter, btnClearFilter;
    @FXML private TableView<AttendanceSummary> tblAttendanceSummary;
    @FXML private TableColumn<AttendanceSummary, String> colStaffName;
    @FXML private TableColumn<AttendanceSummary, Integer> colDaysWorked;
    @FXML private TableColumn<AttendanceSummary, Integer> colDaysMissed;
    @FXML private LineChart<String, Number> activityTrendChart;

    private Connection connection;

    public void initialize() {
        connectDB();
        setupTable();
        loadStaffList();
        loadAttendanceSummary(null);
        loadActivityTrend(null);

        btnApplyFilter.setOnAction(e -> {
            String selectedStaff = cmbStaffFilter.getValue();
            if (selectedStaff != null && !selectedStaff.isEmpty()) {
                loadAttendanceSummary(selectedStaff);
                loadActivityTrend(selectedStaff);
            }
        });

        btnClearFilter.setOnAction(e -> {
            cmbStaffFilter.setValue(null);
            loadAttendanceSummary(null);
            loadActivityTrend(null);
        });
    }

    public ReportsController (){
        this.connection = DatabaseService.getConnection();
    }

    private void connectDB() {
        Connection connection = DatabaseService.getConnection();
    }

    private void setupTable() {
        colStaffName.setCellValueFactory(new PropertyValueFactory<>("staffName"));
        colDaysWorked.setCellValueFactory(new PropertyValueFactory<>("daysWorked"));
        colDaysMissed.setCellValueFactory(new PropertyValueFactory<>("daysMissed"));
    }

    private void loadStaffList() {

        try {
            ObservableList<String> staffNames = FXCollections.observableArrayList();

            String query = "SELECT CONCAT(first_name, ' ', last_name) AS full_name FROM users";
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                staffNames.add(rs.getString("full_name"));
            }
            cmbStaffFilter.setItems(staffNames);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAttendanceSummary(String staffName) {
        ObservableList<AttendanceSummary> data = FXCollections.observableArrayList();
        int totalWorkingDays = calculateWorkingDays(LocalDate.now());

        try {
            String query = """
            SELECT CONCAT(u.first_name, ' ', u.last_name) AS full_name,
                   COALESCE(COUNT(DISTINCT DATE(l.clock_in_time)), 0) AS days_worked
            FROM users u
            LEFT JOIN user_activity_log l 
                ON u.id = l.user_id AND MONTH(l.clock_in_time) = MONTH(CURRENT_DATE())
        """;

            if (staffName != null && !staffName.isEmpty()) {
                query += " WHERE CONCAT(u.first_name, ' ', u.last_name) = ?";
            }

            query += " GROUP BY u.id";

            PreparedStatement stmt = this.connection.prepareStatement(query);

            if (staffName != null && !staffName.isEmpty()) {
                stmt.setString(1, staffName);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("full_name");
                int daysWorked = rs.getInt("days_worked");
                int daysMissed = Math.max(0, totalWorkingDays - daysWorked);
                data.add(new AttendanceSummary(name, daysWorked, daysMissed));
            }

            tblAttendanceSummary.setItems(data);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void loadActivityTrend(String staffName) {
        activityTrendChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Activity Logs");

        try {
            String query = """
                SELECT DATE(clock_in_time) AS log_date, COUNT(*) AS total_logs
                FROM user_activity_log l
                JOIN users u ON u.id = l.user_id
            """;

            if (staffName != null) {
                query += " WHERE CONCAT(u.first_name, ' ', u.last_name) = ?";
            }

            query += " GROUP BY DATE(clock_in_time) ORDER BY log_date";

            PreparedStatement stmt = this.connection.prepareStatement(query);
            if (staffName != null) {
                stmt.setString(1, staffName);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String date = rs.getString("log_date");
                int logs = rs.getInt("total_logs");
                series.getData().add(new XYChart.Data<>(date, logs));
            }

            activityTrendChart.getData().add(series);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int calculateWorkingDays(LocalDate date) {
        YearMonth yearMonth = YearMonth.from(date);
        int workingDays = 0;
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate d = LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), day);
            if (!(d.getDayOfWeek().name().equals("SATURDAY") || d.getDayOfWeek().name().equals("SUNDAY"))) {
                workingDays++;
            }
        }
        return workingDays;
    }

    // Data Model for TableView
    public static class AttendanceSummary {
        private final SimpleStringProperty staffName;
        private final SimpleIntegerProperty daysWorked;
        private final SimpleIntegerProperty daysMissed;

        public AttendanceSummary(String staffName, int daysWorked, int daysMissed) {
            this.staffName = new SimpleStringProperty(staffName);
            this.daysWorked = new SimpleIntegerProperty(daysWorked);
            this.daysMissed = new SimpleIntegerProperty(daysMissed);
        }

        public String getStaffName() {
            return staffName.get();
        }

        public int getDaysWorked() {
            return daysWorked.get();
        }

        public int getDaysMissed() {
            return daysMissed.get();
        }
    }
}
