package org.example.useractivitylogger.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class ActivityLog {
    private StringProperty clockIn = new SimpleStringProperty();
    private StringProperty clockOut = new SimpleStringProperty();
    private StringProperty taskSummary = new SimpleStringProperty();
    private final StringProperty totalStaff = new SimpleStringProperty();
    private final StringProperty hoursWorked = new SimpleStringProperty();

    private SimpleStringProperty staffUsername;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    public ActivityLog(SimpleStringProperty staffUsername) {
        this.staffUsername = staffUsername;
    }

    public ActivityLog(String clockIn, String clockOut, String taskSummary, SimpleStringProperty staffUsername) {
        this.staffUsername = staffUsername;
        this.clockIn.set(clockIn);
        this.clockOut.set(clockOut);
        this.taskSummary.set(taskSummary);
        calculateHoursWorked();
    }

    public ActivityLog() { }

    public ActivityLog(String name, String clockIn, String clockOut, String taskSummary) {
        this.taskSummary = new SimpleStringProperty(taskSummary);
        this.staffUsername = new SimpleStringProperty(name);
        this.clockIn.set(clockIn);
        this.clockOut.set(clockOut);
        calculateHoursWorked();
    }

    public void calculateHoursWorked() {
        try {
            LocalTime in = LocalTime.parse(clockIn.get(), formatter);
            LocalTime out = LocalTime.parse(clockOut.get(), formatter);
            long hours = in.until(out, ChronoUnit.HOURS);
            long minutes = in.until(out, ChronoUnit.MINUTES) % 60;
            hoursWorked.set(String.format("%02d:%02d", hours, minutes));
        } catch (Exception e) {
            hoursWorked.set("N/A");
        }
    }

    // JavaFX properties (used by TableView)
    public StringProperty clockInProperty() { return clockIn; }
    public StringProperty clockOutProperty() { return clockOut; }
    public StringProperty taskSummaryProperty() { return taskSummary; }
    public StringProperty staffNameProperty() { return staffUsername; }
    public ObservableValue<String> hoursWorkedProperty() { return hoursWorked; }

    // Getters
    public String getClockIn() { return clockIn.get(); }
    public String getClockOut() { return clockOut.get(); }
    public String getStaffName() { return staffUsername.get(); }
    public String getTaskSummary() { return taskSummary.get(); }
    public String getTotalStaff() { return totalStaff.get(); }

    // Setters
    public void setClockIn(String value) { clockIn.set(value); }
    public void setClockOut(String value) { clockOut.set(value); }
    public void setTaskSummary(String value) { taskSummary.set(value); }

    // Fixed Attendance method
    public ActivityLog Attendance(String staffName, String clockIn, String clockOut, String taskSummary) {
        return new ActivityLog(staffName, clockIn, clockOut, taskSummary);
    }
}
