package org.example.useractivitylogger.services;

import org.example.useractivitylogger.models.ActivityLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityService {
    private final Connection conn;

    public ActivityService() {
        conn = DatabaseService.getConnection();
    }

    public void clockIn(int userId) {
        if (hasClockedInToday(userId)) {
            System.out.println("User has already clocked in today.");
            return;
        }

        String sql = "INSERT INTO user_activity_log (user_id, clock_in_time) VALUES (?, NOW())";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clockOut(int userId) {
        String sql = "UPDATE user_activity_log SET clock_out_time = NOW() WHERE user_id = ? AND clock_out_time IS NULL";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ Check if user has clocked in today
    public boolean hasClockedInToday(int userId) {
        String sql = "SELECT COUNT(*) FROM user_activity_log WHERE user_id = ? AND DATE(clock_in_time) = CURDATE()";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ✅ Check if user has clocked out today
    public boolean hasClockedOutToday(int userId) {
        String sql = "SELECT COUNT(*) FROM user_activity_log WHERE user_id = ? AND DATE(clock_in_time) = CURDATE() AND clock_out_time IS NOT NULL";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ✅ Check if user has submitted task today
    public boolean hasSubmittedTaskToday(int userId) {
        String sql = "SELECT COUNT(*) FROM user_activity_log WHERE user_id = ? AND DATE(clock_in_time) = CURDATE() AND task_summary IS NOT NULL";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ✅ Submit Task Summary
    public void submitTask(int userId, String taskSummary) {
        String sql = "UPDATE user_activity_log SET task_summary = ? WHERE user_id = ? AND DATE(clock_in_time) = CURDATE()";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, taskSummary);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ Fetch logs for table
    public List<ActivityLog> getLogsForUser(int userId) {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT clock_in_time, clock_out_time, task_summary FROM user_activity_log WHERE user_id = ? ORDER BY clock_in_time DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ActivityLog log = new ActivityLog();
                log.setClockIn(rs.getTimestamp("clock_in_time") != null ? rs.getTimestamp("clock_in_time").toString() : "");
                log.setClockOut(rs.getTimestamp("clock_out_time") != null ? rs.getTimestamp("clock_out_time").toString() : "");
                log.setTaskSummary(rs.getString("task_summary") != null ? rs.getString("task_summary") : "");
                logs.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
}
