package org.example.useractivitylogger.services;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import at.favre.lib.crypto.bcrypt.BCrypt;

public class AuthService {

    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> usersCollection;

    public AuthService() {
        // Connect to MongoDB
        System.setProperty("jdk.tls.client.protocols", "TLSv1.2");

        mongoClient = MongoClients.create("mongodb+srv://USER1:simplecity@great9it.miwhuju.mongodb.net/?retryWrites=true&w=majority&appName=Great9it");
        database = mongoClient.getDatabase("UserLogger");
        usersCollection = database.getCollection("users");
    }

    public LoginResult login(String username, String password) {
        // Fetch user document from DB
        Document userDoc = usersCollection.find(Filters.eq("email", username)).first();

        if (userDoc == null) {
            return new LoginResult(false, null, "User not found");
        }
        String storedHashedPassword = userDoc.getString("password");
        String role = userDoc.getString("role");

        // Check password
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), storedHashedPassword);

        if (!result.verified) {
            return new LoginResult(false, null, "Incorrect password");
        }

        // Successful login
        return new LoginResult(true, role, "Login successful");
    }

    public void close() {
        mongoClient.close();
    }

    // Helper class to represent login outcome
    public static class LoginResult {
        public final boolean success;
        public final String role; // "staff" or "admin"
        public final String message;

        public LoginResult(boolean success, String role, String message) {
            this.success = success;
            this.role = role;
            this.message = message;
        }
    }
}
