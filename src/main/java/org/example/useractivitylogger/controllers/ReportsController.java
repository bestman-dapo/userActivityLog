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

        // === Existing Controls ===
        @FXML private ComboBox<String> cmbStaffFilter;
        @FXML private Button btnApplyFilter, btnClearFilter;
        @FXML private TableView<AttendanceSummary> tblAttendanceSummary;
        @FXML private TableColumn<AttendanceSummary, String> colStaffName;
        @FXML private TableColumn<AttendanceSummary, Integer> colDaysWorked;
        @FXML private TableColumn<AttendanceSummary, Integer> colDaysMissed;
        @FXML private LineChart<String, Number> activityTrendChart;

        // === New Controls ===
        @FXML private TableView<LatecomerSummary> tblLatecomers;
        @FXML private TableColumn<LatecomerSummary, String> colLateStaffName;
        @FXML private TableColumn<LatecomerSummary, Integer> colLateDays;

        @FXML private TableView<TaskSummary> tblTaskSummary;
        @FXML private TableColumn<TaskSummary, String> colTaskStaffName;
        @FXML private TableColumn<TaskSummary, Integer> colTasksSubmitted;
        @FXML private TableColumn<TaskSummary, Integer> colUnreportedDays;

        @FXML private BarChart<String, Number> attendanceProductivityChart;

        private Connection connection;

        public ReportsController() {
            this.connection = DatabaseService.getConnection();
        }

        public void initialize() {
            connectDB();
            setupTable();
            loadStaffList();
            loadAttendanceSummary(null);
            loadActivityTrend(null);

            // === Call new reports ===
            loadLatecomers(null);
            loadTaskSummary(null);
            loadAttendanceVsProductivity(null);

            btnApplyFilter.setOnAction(e -> {
                String selectedStaff = cmbStaffFilter.getValue();
                if (selectedStaff != null && !selectedStaff.isEmpty()) {
                    loadAttendanceSummary(selectedStaff);
                    loadActivityTrend(selectedStaff);
                    loadLatecomers(selectedStaff);
                    loadTaskSummary(selectedStaff);
                    loadAttendanceVsProductivity(selectedStaff);
                }
            });

            btnClearFilter.setOnAction(e -> {
                cmbStaffFilter.setValue(null);
                loadAttendanceSummary(null);
                loadActivityTrend(null);
                loadLatecomers(null);
                loadTaskSummary(null);
                loadAttendanceVsProductivity(null);
            });
        }

        private void connectDB() {
            this.connection = DatabaseService.getConnection();
        }

        private void setupTable() {
            // Existing table
            colStaffName.setCellValueFactory(new PropertyValueFactory<>("staffName"));
            colDaysWorked.setCellValueFactory(new PropertyValueFactory<>("daysWorked"));
            colDaysMissed.setCellValueFactory(new PropertyValueFactory<>("daysMissed"));

            // New tables
            if (colLateStaffName != null) colLateStaffName.setCellValueFactory(new PropertyValueFactory<>("staffName"));
            if (colLateDays != null) colLateDays.setCellValueFactory(new PropertyValueFactory<>("lateDays"));

            if (colTaskStaffName != null) colTaskStaffName.setCellValueFactory(new PropertyValueFactory<>("staffName"));
            if (colTasksSubmitted != null) colTasksSubmitted.setCellValueFactory(new PropertyValueFactory<>("tasksSubmitted"));
            if (colUnreportedDays != null) colUnreportedDays.setCellValueFactory(new PropertyValueFactory<>("unreportedDays"));
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
                    ON u.id = l.user_id 
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

        // === New Reports ===

        private void loadLatecomers(String staffName) {
            if (tblLatecomers == null) return; // safety if FXML not linked
            ObservableList<LatecomerSummary> data = FXCollections.observableArrayList();
            try {
                String query = """
                    SELECT CONCAT(u.first_name, ' ', u.last_name) AS full_name,
                           COUNT(*) AS late_days
                    FROM user_activity_log l
                    JOIN users u ON u.id = l.user_id
                    WHERE TIME(l.clock_in_time) > '09:00:00'
                """;

                if (staffName != null && !staffName.isEmpty()) {
                    query += " AND CONCAT(u.first_name, ' ', u.last_name) = ?";
                }

                query += " GROUP BY u.id";

                PreparedStatement stmt = this.connection.prepareStatement(query);
                if (staffName != null && !staffName.isEmpty()) {
                    stmt.setString(1, staffName);
                }

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    data.add(new LatecomerSummary(rs.getString("full_name"), rs.getInt("late_days")));
                }

                tblLatecomers.setItems(data);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void loadTaskSummary(String staffName) {
            ObservableList<TaskSummary> data = FXCollections.observableArrayList();

            try {
                String query = """
        SELECT CONCAT(u.first_name, ' ', u.last_name) AS full_name,
               COUNT(DISTINCT DATE(l.clock_in_time)) AS days_present,
               COUNT(l.task_summary) AS tasks_done
        FROM users u
        LEFT JOIN user_activity_log l ON u.id = l.user_id
             AND (l.task_summary IS NOT NULL AND l.task_summary <> '')
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
                    int daysPresent = rs.getInt("days_present");
                    int tasks = rs.getInt("tasks_done");
                    int unreportedDays = Math.max(0, daysPresent - tasks);

                    data.add(new TaskSummary(name, tasks, unreportedDays));
                }

                tblTaskSummary.setItems(data);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void loadAttendanceVsProductivity(String staffName) {
            attendanceProductivityChart.getData().clear();
            XYChart.Series<String, Number> seriesAttendance = new XYChart.Series<>();
            seriesAttendance.setName("Days Present");
            XYChart.Series<String, Number> seriesTasks = new XYChart.Series<>();
            seriesTasks.setName("Tasks Submitted");

            try {
                String query = """
        SELECT CONCAT(u.first_name, ' ', u.last_name) AS full_name,
               COUNT(DISTINCT DATE(l.clock_in_time)) AS days_present,
               COUNT(l.task_summary) AS tasks_done
        FROM users u
        LEFT JOIN user_activity_log l ON u.id = l.user_id
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
                    int daysPresent = rs.getInt("days_present");
                    int tasksDone = rs.getInt("tasks_done");

                    seriesAttendance.getData().add(new XYChart.Data<>(name, daysPresent));
                    seriesTasks.getData().add(new XYChart.Data<>(name, tasksDone));
                }

                attendanceProductivityChart.getData().addAll(seriesAttendance, seriesTasks);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private int calculateWorkingDays(LocalDate date) {
            YearMonth yearMonth = YearMonth.from(date);
            int workingDays = 0;
            LocalDate today = LocalDate.now();

            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                LocalDate d = LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), day);

                if(d.isAfter(today)){
break;
                }
                if (!(d.getDayOfWeek().name().equals("SATURDAY") || d.getDayOfWeek().name().equals("SUNDAY"))) {
                    workingDays++;
                }
            }
            return workingDays;
        }

        // === Data Models ===
        public static class AttendanceSummary {
            private final SimpleStringProperty staffName;
            private final SimpleIntegerProperty daysWorked;
            private final SimpleIntegerProperty daysMissed;

            public AttendanceSummary(String staffName, int daysWorked, int daysMissed) {
                this.staffName = new SimpleStringProperty(staffName);
                this.daysWorked = new SimpleIntegerProperty(daysWorked);
                this.daysMissed = new SimpleIntegerProperty(daysMissed);
            }
            public String getStaffName() { return staffName.get(); }
            public int getDaysWorked() { return daysWorked.get(); }
            public int getDaysMissed() { return daysMissed.get(); }
        }

        public static class LatecomerSummary {
            private final SimpleStringProperty staffName;
            private final SimpleIntegerProperty lateDays;

            public LatecomerSummary(String staffName, int lateDays) {
                this.staffName = new SimpleStringProperty(staffName);
                this.lateDays = new SimpleIntegerProperty(lateDays);
            }
            public String getStaffName() { return staffName.get(); }
            public int getLateDays() { return lateDays.get(); }
        }

        public static class TaskSummary {
            private final SimpleStringProperty staffName;
            private final SimpleIntegerProperty tasksSubmitted;
            private final SimpleIntegerProperty unreportedDays;

            public TaskSummary(String staffName, int tasksSubmitted, int unreportedDays) {
                this.staffName = new SimpleStringProperty(staffName);
                this.tasksSubmitted = new SimpleIntegerProperty(tasksSubmitted);
                this.unreportedDays = new SimpleIntegerProperty(unreportedDays);
            }
            public String getStaffName() { return staffName.get(); }
            public int getTasksSubmitted() { return tasksSubmitted.get(); }
            public int getUnreportedDays() { return unreportedDays.get(); }
        }
    }
