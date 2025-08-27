package essentials;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


//Represents a car rental transaction in the system.
public class Rental {
    private Car car;           
    private Customer customer;  
    private int days;          
    private boolean returned;  

    public Rental(Car car, Customer customer, int days) {
        this.car = car;
        this.customer = customer;
        this.days = days;
        this.returned = false;  
    }

    public Car getCar() {
        return car;
    }

    public Customer getCustomer() {
        return customer;
    }

    public int getDays() {
        return days;
    }

    public boolean isReturned() {
        return returned;
    }

    public void setReturned(boolean returned) {
        this.returned = returned;
    }

    // ==== JDBC helpers for Rental ====
    public static void insert(Rental rental) throws SQLException {
        String sql = "INSERT INTO rentals (car_id, customer_id, days, returned) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rental.getCar().getCarId());
            ps.setString(2, rental.getCustomer().getCustomerId());
            ps.setInt(3, rental.getDays());
            ps.setBoolean(4, rental.isReturned());
            ps.executeUpdate();
        }
    }

    public static void markReturned(String carId) throws SQLException {
        String sql = "UPDATE rentals SET returned = 1 WHERE car_id = ? AND returned = 0 ORDER BY id DESC LIMIT 1";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, carId);
            ps.executeUpdate();
        }
    }

    public static List<Rental> listAll() throws SQLException {
        String sql = "SELECT car_id, customer_id, days, returned FROM rentals ORDER BY id DESC";
        List<Rental> list = new ArrayList<>();
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Car car = Car.findById(rs.getString("car_id"));
                    Customer customer = Customer.findById(rs.getString("customer_id"));
                    Rental rental = new Rental(car, customer, rs.getInt("days"));
                    if (rs.getBoolean("returned")) {
                        rental.setReturned(true);
                    }
                    list.add(rental);
                }
            }
        }
        return list;
    }

    public static Rental findActiveByCarId(String carId) throws SQLException {
        String sql = "SELECT car_id, customer_id, days, returned FROM rentals WHERE car_id=? AND returned=0 ORDER BY id DESC LIMIT 1";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, carId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Car car = Car.findById(rs.getString("car_id"));
                    Customer customer = Customer.findById(rs.getString("customer_id"));
                    Rental rental = new Rental(car, customer, rs.getInt("days"));
                    if (rs.getBoolean("returned")) {
                        rental.setReturned(true);
                    }
                    return rental;
                }
            }
        }
        return null;
    }

    // Performs a full rental flow: validate availability, insert rental, and set car unavailable
    public static boolean rentCar(Car car, Customer customer, int days) throws SQLException {
        if (car == null || customer == null) {
            return false;
        }
        if (!car.isAvailable()) {
            return false;
        }
        car.rent();
        Rental rental = new Rental(car, customer, days);
        insert(rental);
        Car.setAvailability(car.getCarId(), false);
        return true;
    }
}