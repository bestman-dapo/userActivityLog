package org.example.useractivitylogger;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoConnection {

    // Change this to your own MongoDB Atlas connection string
    private static final String URI = "mongodb+srv://USER1:simplecity@great9it.miwhuju.mongodb.net/?retryWrites=true&w=majority&appName=Great9it";
    // Change this to your database name in MongoDB Atlas
    private static final String DATABASE_NAME = "UserLogger";

    private static MongoClient mongoClient;
    private static MongoDatabase database;

    // Connect to MongoDB Atlas
    public static MongoDatabase getDatabase() {
        if (database == null) {
            try {
                mongoClient = MongoClients.create(URI);
                database = mongoClient.getDatabase(DATABASE_NAME);
                System.out.println("Connected to database: " + database.getName());
            } catch (Exception e) {
                System.err.println(" MongoDB connection failed!");
                e.printStackTrace();
            }
        }
        return database;
    }

    // Close the connection
    public static void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("🔌 MongoDB connection closed.");
        }
    }
}