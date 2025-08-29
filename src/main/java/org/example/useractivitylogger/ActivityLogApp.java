package org.example.useractivitylogger;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.useractivitylogger.models.ActivityLog;

public class ActivityLogApp extends Application {

    private final ObservableList<ActivityLog> logs = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {

        TableView<ActivityLog> table = new TableView<>();
        table.setItems(logs);

        TableColumn<ActivityLog, String> clockInCol = new TableColumn<>("Clock In");
        clockInCol.setCellValueFactory(cell -> cell.getValue().clockInProperty());

        TableColumn<ActivityLog, String> clockOutCol = new TableColumn<>("Clock Out");
        clockOutCol.setCellValueFactory(cell -> cell.getValue().clockOutProperty());

        TableColumn<ActivityLog, String> taskCol = new TableColumn<>("Task Summary");
        taskCol.setCellValueFactory(cell -> cell.getValue().taskSummaryProperty());

        TableColumn<ActivityLog, String> hoursCol = new TableColumn<>("Hours Worked");
        hoursCol.setCellValueFactory(cell -> cell.getValue().hoursWorkedProperty());

        table.getColumns().addAll(clockInCol, clockOutCol, taskCol, hoursCol);


        VBox root = new VBox(table);
        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.setTitle("Staff Activity Log");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
