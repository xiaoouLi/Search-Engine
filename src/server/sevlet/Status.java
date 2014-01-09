// Xiaoou, Dec,2013
// xli65@usfca.edu

package server.sevlet;

/**
 * 
 * Modified based on https://github.com/cs212/demos/blob/master/Login%20Demo/Status.java
 * 
 * Creates a Status enum type for tracking errors. Each Status enum type will use the ordinal as the error code, and
 * store a message describing the error.
 * 
 */
public enum Status {

    OK("No errors occured."),
    ERROR("Unknown error occurred."),
    MISSING_CONFIG("Unable to find configuration file."),
    MISSING_VALUES("Missing values in configuration file."),
    CONNECTION_FAILED("Failed to establish a database connection."),
    CREATE_FAILED("Failed to create necessary tables."),
    INVALID_LOGIN("Invalid username and/or password."),
    INVALID_USER("User does not exist."),
    DUPLICATE_USER("User with that username already exists."),
    SQL_EXCEPTION("Unable to query the database."),
    INVALID_CONFIG("Invalid configuration");

    private final String message;

    private Status(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return this.message;
    }
}
