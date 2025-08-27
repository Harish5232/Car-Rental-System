package essentials;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a car in the rental system.
 */
public class Car {
    // Car identification and details
    private String carId;           
    private String brand;           
    private String model;         
    private double basePricePerDay; 
    private boolean isAvailable;    

    public Car(String carId, String brand, String model, double basePricePerDay) {
        this.carId = carId;
        this.brand = brand;
        this.model = model;
        this.basePricePerDay = basePricePerDay;
        this.isAvailable = true;  // New cars start as available
    }

    // Getter methods for car properties
    public String getCarId() {
        return carId;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public double getBasePricePerDay() {
        return basePricePerDay;
    }

     //Checks if the car is currently available for rent.     
    public boolean isAvailable() {
        return isAvailable;
    }

    public double calculatePrice(int rentalDays) {
        return basePricePerDay * rentalDays;
    }

     // Marks the car as rented     
    public void rent() {
        isAvailable = false;
    }

     // Marks the car as returned
    public void returnCar() {
        isAvailable = true;
    }

    // ==== JDBC helpers for Car ====
    public static void addCar(Car car) throws SQLException {
        String sql = "INSERT INTO cars (car_id, brand, model, base_price_per_day, is_available) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE brand=VALUES(brand), model=VALUES(model), base_price_per_day=VALUES(base_price_per_day)";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, car.getCarId());
            ps.setString(2, car.getBrand());
            ps.setString(3, car.getModel());
            ps.setDouble(4, car.getBasePricePerDay());
            ps.setBoolean(5, car.isAvailable());
            ps.executeUpdate();
        }
    }

    public static List<Car> findAvailableCars() throws SQLException {
        String sql = "SELECT car_id, brand, model, base_price_per_day, is_available FROM cars WHERE is_available = 1";
        List<Car> list = new ArrayList<>();
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Car car = new Car(
                        rs.getString("car_id"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getDouble("base_price_per_day")
                    );
                    if (!rs.getBoolean("is_available")) {
                        car.rent();
                    }
                    list.add(car);
                }
            }
        }
        return list;
    }

    public static Car findById(String carId) throws SQLException {
        String sql = "SELECT car_id, brand, model, base_price_per_day, is_available FROM cars WHERE car_id = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, carId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Car car = new Car(
                        rs.getString("car_id"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getDouble("base_price_per_day")
                    );
                    if (!rs.getBoolean("is_available")) {
                        car.rent();
                    }
                    return car;
                }
            }
        }
        return null;
    }

    public static void setAvailability(String carId, boolean available) throws SQLException {
        String sql = "UPDATE cars SET is_available=? WHERE car_id=?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, available);
            ps.setString(2, carId);
            ps.executeUpdate();
        }
    }
}


