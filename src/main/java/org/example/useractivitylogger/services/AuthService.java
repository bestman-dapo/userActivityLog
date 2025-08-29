package org.example.useractivitylogger.services;

import at.favre.lib.crypto.bcrypt.BCrypt;

import java.sql.*;

public class AuthService {

    private final Connection connection;

    public AuthService() {
        this.connection = DatabaseService.getConnection();
    }

    public LoginResult login(String username, String password) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return new LoginResult(false, null, "User not found", 0, "");
                }

                String storedHashedPassword = rs.getString("password");
                String role = rs.getString("role");

                // Verify password using BCrypt
                BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), storedHashedPassword);

                if (!result.verified) {
                    return new LoginResult(false, null, "Incorrect password", 0, "");
                }

                return new LoginResult(true, role, "Login successful", rs.getInt("id"), rs.getString("username"));
            }
        } catch (SQLException e) {
            return new LoginResult(false, null, "Error: " + e.getMessage(), 0, "");
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ MySQL connection closed.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error closing MySQL connection: " + e.getMessage());
        }
    }

    // Inner class for login results
    public static class LoginResult {
        public final boolean success;
        public final String role;
        public final String message;
        public int id;
        public final String username;

        public LoginResult(boolean success, String role, String message, int id, String username) {
            this.success = success;
            this.role = role;
            this.message = message;
            this.id = id;
            this.username = username;
        }
    }
}
