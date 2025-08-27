package essentials;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//Database connection utility class for the Car Rental System.
public class Database {
    // Default database connection parameter
    private static final String dbUser = envLoader.get("DB_USER");
    private static final String dbPass = envLoader.get("DB_PASS");
    private static final String defaultUrl = envLoader.get("DEFAULT_URL");

    //Establishes a connection to the MySQL database.
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
        }
        // Get connection parameters from environment variables or use defaults
        String url = System.getenv().getOrDefault("DB_URL", defaultUrl);
        String user = System.getenv().getOrDefault("DB_USER", dbUser);
        String pass = System.getenv().getOrDefault("DB_PASS", dbPass);
        
        // Establish and return the database connection
        return DriverManager.getConnection(url, user, pass);
    }
}


