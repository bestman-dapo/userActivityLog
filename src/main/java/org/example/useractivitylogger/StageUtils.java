package org.example.useractivitylogger;

import javafx.stage.Screen;
import javafx.stage.Stage;

public class StageUtils {

    public static void applyMinimumSize(Stage stage) {
        // Get primary screen bounds
        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();

        // Set minimum size to 3/4 of screen
        stage.setMinWidth(screenWidth * 0.75);
        stage.setMinHeight(screenHeight * 0.75);
    }
}
