
package org.example.useractivitylogger.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ActivityLog {
    private final StringProperty clockIn = new SimpleStringProperty();
    private final StringProperty clockOut = new SimpleStringProperty();
    private final StringProperty taskSummary = new SimpleStringProperty();

    public ActivityLog() {
        // Default constructor
    }
    public ActivityLog(String clockIn, String clockOut, String taskSummary) {
        this.clockIn.set(clockIn);
        this.clockOut.set(clockOut);
        this.taskSummary.set(taskSummary);
    }

    // JavaFX properties (used by TableView)
    public StringProperty clockInProperty() { return clockIn; }
    public StringProperty clockOutProperty() { return clockOut; }
    public StringProperty taskSummaryProperty() { return taskSummary; }

    // Getters
    public String getClockIn() { return clockIn.get(); }
    public String getClockOut() { return clockOut.get(); }
    public String getTaskSummary() { return taskSummary.get(); }

    // Setters (optional)
    public void setClockIn(String value) { clockIn.set(value); }
    public void setClockOut(String value) { clockOut.set(value); }
    public void setTaskSummary(String value) { taskSummary.set(value); }
}
