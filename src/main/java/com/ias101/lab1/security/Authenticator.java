package com.ias101.lab1.security;

import com.ias101.lab1.database.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Authentication class for user validation
 */
public class Authenticator {

    // Define regular expressions for username and password validation
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]{5,20}$"; // Allows underscores in usernames
    private static final String PASSWORD_PATTERN = "^(pass|password|userpass)\\d{1,3}$"; // Matches passwords like pass123, password456, userpass123
    private static final String SPECIAL_CHAR_PASSWORD_PATTERN = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*+]).{5,20}$"; // Allows letters, digits, and special characters

    /**
     * Authenticates a user by checking username and password against database
     *
     * @param username The username to authenticate
     * @param password The password to authenticate
     * @return boolean Returns true if authentication successful, false otherwise
     * @throws RuntimeException if there is a SQL error during authentication
     */
    public static boolean authenticateUser(String username, String password) {
        // Validate username against its pattern
        if (!isValid(username, USERNAME_PATTERN)) {
            System.out.println("Invalid username format.");
            return false;
        }

        // Validate password against either the original pattern or the special character pattern
        if (!isValid(password, PASSWORD_PATTERN) && !isValid(password, SPECIAL_CHAR_PASSWORD_PATTERN)) {
            System.out.println("Invalid password format. Password must contain at least one letter, one digit, one special character, and be 8-20 characters long.");
            return false;
        }

        try (var conn = DBUtil.connect("jdbc:sqlite:src/main/resources/database/sample.db",
                "root", "root")) {
            try (var statement = conn.createStatement()) {
                var query = """
                        SELECT * FROM user_data
                        WHERE username =\s""" + "'" + username + "'"
                        + "AND password = " + "'" + password + "'";
                System.out.println(query);
                ResultSet rs = statement.executeQuery(query);

                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Validates a given string against a regular expression pattern
     *
     * @param input   The string to validate
     * @param pattern The regular expression pattern to match against
     * @return boolean Returns true if the input matches the pattern, false otherwise
     */
    private static boolean isValid(String input, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(input);
        return m.matches();
    }
}