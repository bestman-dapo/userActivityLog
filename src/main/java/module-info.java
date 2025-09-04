module org.example.useractivitylogger {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.driver.core;
    requires org.mongodb.bson;
    requires bcrypt;
    requires io.github.cdimascio.dotenv.java;
    requires java.sql;

    exports org.example.useractivitylogger;
    exports org.example.useractivitylogger.controllers to javafx.fxml;
    opens org.example.useractivitylogger to javafx.fxml, javafx.graphics;
    opens org.example.useractivitylogger.controllers to javafx.fxml, javafx.base;
}
