package org.example.useractivitylogger.models;

import javafx.beans.property.*;

public class Staff {
    private final IntegerProperty id;
    private final StringProperty firstName;
    private final StringProperty lastName;
    private final StringProperty position;
    private final StringProperty password;
    private StringProperty email = null;
    private final IntegerProperty presentDays;

//    public Staff(int id, String firstName, String lastName, String position, String email, int presentDays) {
//        this.id = new SimpleIntegerProperty(id);
//        this.firstName = new SimpleStringProperty(firstName);
//        this.lastName = new SimpleStringProperty(lastName);
//        this.position = new SimpleStringProperty(position);
//        this.email = new SimpleStringProperty();
//        this.presentDays = new SimpleIntegerProperty(presentDays);
//        password = null;
//    }

    public Staff(int id, String firstName, String lastName, String position, String email, String password, int presentDays) {
        this.id = new SimpleIntegerProperty(id);
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);
        this.position = new SimpleStringProperty(position);
        this.password = new SimpleStringProperty(password);
        this.email = new SimpleStringProperty();
        this.presentDays = new SimpleIntegerProperty(presentDays);
    }

    // Properties
    public IntegerProperty idProperty() { return id; }
    public StringProperty firstNameProperty() { return firstName; }
    public StringProperty lastNameProperty() { return lastName; }
    public StringProperty positionProperty() { return position; }
    public StringProperty emailProperty() { return email; }
    public StringProperty passwordProperty() { return password; }
    public IntegerProperty presentDaysProperty() { return presentDays; }

    // Getters
    public int getId() { return id.get(); }
    public String getFirstName() { return firstName.get(); }
    public String getLastName() { return lastName.get(); }
    public String getPosition() { return position.get(); }
    public String getEmail() { return email.get(); }
    public int getPresentDays() { return presentDays.get(); }
    public String getPassword() { return password.get(); }

    // Full name computed property
    public String getFullName() {
        return (getFirstName() + " " + getLastName()).trim();
    }
}
