package org.example.useractivitylogger.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.useractivitylogger.models.Staff;
import org.example.useractivitylogger.services.DatabaseService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ManageStaffController {

    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtUsername;
    @FXML private TextField txtPosition;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnAddStaff;
    @FXML private TableView<Staff> tblStaff;
    @FXML private TableColumn<Staff, String> colFirstName;
    @FXML private TableColumn<Staff, String> colLastName;
    @FXML private TableColumn<Staff, String> colPosition;
    @FXML private TableColumn<Staff, String> colEmail;
//    @FXML private TableColumn<Staff, String> colPassword;
    @FXML private TableColumn<Staff, Integer> colPresentDays;

    private final ObservableList<Staff> staffList = FXCollections.observableArrayList();

    private static final int ROWS_PER_PAGE = 10;
    private int currentPage = 0;

//    public ManageStaffController(TableColumn<Staff, String> colPassword) {
//        this.colPassword = colPassword;
//    }

    @FXML
    public void initialize() {
        colFirstName.setCellValueFactory(data -> data.getValue().firstNameProperty());
        colLastName.setCellValueFactory(data -> data.getValue().lastNameProperty());
//        colPassword.setCellValueFactory(data -> data.getValue().passwordProperty());
        colPosition.setCellValueFactory(data -> data.getValue().positionProperty());
        colEmail.setCellValueFactory(data -> data.getValue().emailProperty());
        colPresentDays.setCellValueFactory(data -> data.getValue().presentDaysProperty().asObject());

        btnAddStaff.setOnAction(e -> addStaff());
        loadStaffData(currentPage);
    }

    private void addStaff() {
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String username = txtUsername.getText().trim();
        String position = txtPosition.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();
        String bcryptHash = BCrypt.withDefaults().hashToString(12, password.toCharArray());

        if (firstName.isEmpty() || lastName.isEmpty() || position.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Validation Error", "Please fill all fields.");
            return;
        }

        String query = "INSERT INTO users (first_name, last_name, username, position, email, password, role) VALUES (?, ?, ?, ?, ?, ?, 'staff')";

        Connection conn = DatabaseService.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, username);
            stmt.setString(4, position);
            stmt.setString(5, email);
            stmt.setString(6, bcryptHash);
            stmt.executeUpdate();

            showAlert("Success", "Staff added successfully.");
            clearForm();
            loadStaffData(currentPage);

        } catch (SQLException ex) {
            showAlert("Error", "Failed to add staff: " + ex.getMessage());
        }
    }

    private void loadStaffData(int page) {
        staffList.clear();

        String sql = """
        SELECT u.id,
               u.first_name,
               u.last_name,
               COALESCE(u.position, '') AS position,
               COALESCE(u.email, '') AS email,
               COALESCE(p.present_days, 0) AS present_days
        FROM users u
        LEFT JOIN (
            SELECT user_id,
                   COUNT(DISTINCT DATE(clock_in_time)) AS present_days
            FROM user_activity_log
            WHERE YEAR(clock_in_time) = YEAR(CURDATE())
              AND MONTH(clock_in_time) = MONTH(CURDATE())
            GROUP BY user_id
        ) p ON p.user_id = u.id
        WHERE u.role = 'staff'
        ORDER BY u.first_name
        LIMIT ? OFFSET ?;
    """;

        Connection conn = DatabaseService.getConnection();
        try (
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ROWS_PER_PAGE);
            ps.setInt(2, page * ROWS_PER_PAGE);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    staffList.add(new Staff(
                            rs.getInt("id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("position"),
                            rs.getString("email"),
                            null,
                            rs.getInt("present_days")
                    ));
                }
            }
            tblStaff.setItems(staffList);

        } catch (SQLException ex) {
            showAlert("Error", "Failed to load staff: " + ex.getMessage());
        }
    }

    private void clearForm() {
        txtFirstName.clear();
        txtLastName.clear();
        txtPosition.clear();
        txtEmail.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
