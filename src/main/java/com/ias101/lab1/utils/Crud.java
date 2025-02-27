package com.ias101.lab1.utils;

import com.ias101.lab1.database.util.DBUtil;
import com.ias101.lab1.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class for performing CRUD operations on user data in the database.
 */
public class Crud {
    private static final String DB_URL = "jdbc:sqlite:src/main/resources/database/sample.db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    // SQL queries as constants
    private static final String SELECT_ALL_USERS = "SELECT * FROM user_data";

    // Regex pattern to allow only alphanumeric usernames
    private static final Pattern VALID_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    // Disallowed usernames (e.g., admin, root, system)
    private static final Pattern DISALLOWED_USERNAMES = Pattern.compile("^(admin|root|system)$", Pattern.CASE_INSENSITIVE);

    /**
     * Retrieves <b>all users</b> from the database.
     *
     * @return List of all User objects in the database
     * @throws RuntimeException if there is an error fetching users from database
     */
    public static List<User> getAll() {
        ResultSet rs = null;
        List<User> users = new ArrayList<>();

        try (var connection = DBUtil.connect(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = connection.createStatement()) {

            rs = stmt.executeQuery(SELECT_ALL_USERS);
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
            rs.close();
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all users", e);
        }
    }

    /**
     * Searches for a user by their <b>username</b>.
     *
     * @param username The username to search for
     * @return User object if found, null otherwise
     * @throws RuntimeException if there is an error searching the database or if the username is invalid
     */
    public static User searchByUsername(String username) {
        // Validate username using regex
        if (!isValidUsername(username)) {
            throw new RuntimeException("Invalid username: " + username);
        }

        ResultSet rs = null;
        User user = null;

        try (var connection = DBUtil.connect(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = connection.createStatement()) {

            // Deliberately vulnerable to SQL injection for demonstration
            rs = stmt.executeQuery(String.format("SELECT * FROM user_data WHERE username = '%s'", username));
            while (rs.next()) {
                user = extractUserFromResultSet(rs);
            }
            rs.close();
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Error searching for user: " + username, e);
        }
    }

    /**
     * Deletes a user from the database by their username.
     *
     * @param username The username of the user to delete
     * @throws RuntimeException if there is an error deleting the user or if the username is invalid
     */
    public static void deleteUserByUsername(String username) {
        // Validate username using regex
        if (!isValidUsername(username)) {
            throw new RuntimeException("Invalid username: " + username);
        }

        // Check if the username is disallowed
        if (isDisallowedUsername(username)) {
            throw new RuntimeException("Deletion of user '" + username + "' is not allowed.");
        }

        try (var connection = DBUtil.connect(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = connection.createStatement()) {

            // Deliberately vulnerable to SQL injection for demonstration
            stmt.execute(String.format("DELETE FROM user_data WHERE username = '%s'", username));

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user: " + username, e);
        }
    }

    /**
     * Extracts user data from a ResultSet and creates a User object.
     *
     * @param rs The ResultSet containing user data
     * @return User object created from the ResultSet data
     * @throws SQLException if there is an error reading from the ResultSet
     */
    private static User extractUserFromResultSet(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("username"),
                rs.getString("password")
        );
    }

    /**
     * Validates a username using a regex pattern.
     *
     * @param username The username to validate
     * @return true if the username is valid, false otherwise
     */
    private static boolean isValidUsername(String username) {
        return VALID_USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Checks if a username is disallowed (e.g., admin, root, system).
     *
     * @param username The username to check
     * @return true if the username is disallowed, false otherwise
     */
    private static boolean isDisallowedUsername(String username) {
        return DISALLOWED_USERNAMES.matcher(username).matches();
    }
}