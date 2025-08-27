package essentials;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//Represents a customer in the car rental system.
public class Customer {
    // Customer identification and details
    private String customerId;  
    private String name;        

    public Customer(String customerId, String name) {
        this.customerId = customerId;
        this.name = name;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    // ==== JDBC helpers for Customer ====
    public static void addCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (customer_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getCustomerId());
            ps.setString(2, customer.getName());
            ps.executeUpdate();
        }
    }

    public static Customer findById(String customerId) throws SQLException {
        String sql = "SELECT customer_id, name FROM customers WHERE customer_id = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Customer(rs.getString("customer_id"), rs.getString("name"));
                }
            }
        }
        return null;
    }
}
