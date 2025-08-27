package essentials;
import java.util.List;
import java.util.Scanner;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;

public class CarRentalSystem {
    
    public CarRentalSystem() {
        try {
            ensureTables();
        } catch (SQLException e) {
            System.out.println("Failed creating tables: " + e.getMessage());
        }
    }
    //Create tables if they do not exist
    private void ensureTables() throws SQLException {
        try (Connection conn = Database.getConnection(); Statement st = conn.createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS cars (" +
                "car_id VARCHAR(64) PRIMARY KEY, " +
                "brand VARCHAR(100) NOT NULL, " +
                "model VARCHAR(100) NOT NULL, " +
                "base_price_per_day DOUBLE NOT NULL, " +
                "is_available TINYINT(1) NOT NULL DEFAULT 1" +
                ")"
            );
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS customers (" +
                "customer_id VARCHAR(64) PRIMARY KEY, " +
                "name VARCHAR(200) NOT NULL" +
                ")"
            );
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS rentals (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "car_id VARCHAR(64) NOT NULL, " +
                "customer_id VARCHAR(64) NOT NULL, " +
                "days INT NOT NULL, " +
                "returned TINYINT(1) NOT NULL DEFAULT 0, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "CONSTRAINT fk_car FOREIGN KEY (car_id) REFERENCES cars(car_id), " +
                "CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id)" +
                ")"
            );
        }
    }
    // Rent a car
    public void rentCar(Car car, Customer customer, int days) {
        try {
            boolean ok = Rental.rentCar(car, customer, days);
            if (!ok) {
                System.out.println("Car is not available for rent.");
            }
        } catch (SQLException e) {
            System.out.println("Failed to persist rental: " + e.getMessage());
        }
    }
    // Return a car
    public void returnCar(Car car) {
        car.returnCar();
        try {
            Rental.markReturned(car.getCarId());
            Car.setAvailability(car.getCarId(), true);
        } catch (SQLException e) {
            System.out.println("Failed to persist return: " + e.getMessage());
        }
    }
    // Display rental history
    public void showRentalHistory() {
        System.out.println("\n===== Rental History =====");
        List<Rental> rentals;
        try {
            rentals = Rental.listAll();
        } catch (SQLException e) {
            System.out.println("Failed reading rental history: " + e.getMessage());
            return;
        }
        if (rentals.isEmpty()) {
            System.out.println("No rentals yet.");
            return;
        }
        for (Rental rental : rentals) {
            String status = rental.isReturned() ? "Returned" : "Not Returned";
            System.out.println("Car: " + rental.getCar().getBrand() + " " + rental.getCar().getModel()
                    + " | Customer: " + rental.getCustomer().getName()
                    + " | Days: " + rental.getDays()
                    + " | Status: " + status);
        }
    }
    //Displays main menu and handles user input
    public void menu() {
        Scanner scanner = new Scanner(System.in);
        //Loops until user chooses to exit
        while (true) {
            System.out.println("\n===== Car Rental System =====");
            System.out.println("1. Rent a Car");
            System.out.println("2. Return a Car");
            System.out.println("3. Show Rental History");
            System.out.println("4. Add New Car");
            System.out.println("5. Clear Database");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                handleCarRental(scanner);
            } else if (choice == 2) {
                handleCarReturn(scanner);
            } else if (choice == 3) {
                showRentalHistory();
            } else if (choice == 4) {
                addNewCar(scanner);
            } else if (choice == 5) {
                clearDatabase(scanner);
            } else if (choice == 6) {
                break;
            } else {
                System.out.println("Invalid choice. Please enter a valid option.");
            }
        }

        System.out.println("\nThank you for using the Car Rental System!");
        scanner.close();
    }

    private void handleCarRental(Scanner scanner) {
        System.out.println("\n== Rent a Car ==\n");
        List<Car> cars;
        try {
            cars = Car.findAvailableCars();
        } catch (SQLException e) {
            System.out.println("Failed loading available cars: " + e.getMessage());
            return;
        }
        if (cars.isEmpty()) {
            System.out.println("No cars available for rent at the moment.");
            return;
        }
        System.out.print("Enter your name: ");
        String customerName = scanner.nextLine();

        System.out.println("\nAvailable Cars:");
        for (Car car : cars) {
            System.out.println(car.getCarId() + " - " + car.getBrand() + " " + car.getModel());
        }

        System.out.print("\nEnter the car ID you want to rent: ");
        String carId = scanner.nextLine();

        System.out.print("Enter the number of days for rental: ");
        int rentalDays = scanner.nextInt();
        scanner.nextLine();

        Customer newCustomer = new Customer(generateCustomerId(), customerName);
        try {
            Customer.addCustomer(newCustomer);
        } catch (SQLException e) {
            System.out.println("Failed saving customer: " + e.getMessage());
            return;
        }

        Car selectedCar = null;
        try {
            selectedCar = Car.findById(carId);
        } catch (SQLException e) {
            System.out.println("Failed to look up car: " + e.getMessage());
        }

        if (selectedCar != null && selectedCar.isAvailable()) {
            double totalPrice = selectedCar.calculatePrice(rentalDays);
            System.out.println("\n== Rental Information ==\n");
            System.out.println("Customer ID: " + newCustomer.getCustomerId());
            System.out.println("Customer Name: " + newCustomer.getName());
            System.out.println("Car: " + selectedCar.getBrand() + " " + selectedCar.getModel());
            System.out.println("Rental Days: " + rentalDays);
            System.out.printf("Total Price: $%.2f%n", totalPrice);

            System.out.print("\nConfirm rental (Y/N): ");
            String confirm = scanner.nextLine();

            if (confirm.equalsIgnoreCase("Y")) {
                rentCar(selectedCar, newCustomer, rentalDays);
                System.out.println("\nCar rented successfully.");
            } else {
                System.out.println("\nRental canceled.");
            }
        } else {
            System.out.println("\nInvalid car selection or car not available for rent.");
        }
    }
    // Handle car return process
    private void handleCarReturn(Scanner scanner) {
        System.out.println("\n== Return a Car ==\n");
        System.out.print("Enter the car ID you want to return: ");
        String carId = scanner.nextLine();

        Car carToReturn = null;
        try {
            carToReturn = Car.findById(carId);
        } catch (SQLException e) {
            System.out.println("Failed to look up car: " + e.getMessage());
        }

        if (carToReturn != null && !carToReturn.isAvailable()) {
            Rental active;
            try {
                active = Rental.findActiveByCarId(carId);
            } catch (SQLException e) {
                System.out.println("Failed to look up active rental: " + e.getMessage());
                return;
            }

            returnCar(carToReturn);
            if (active != null && active.getCustomer() != null) {
                System.out.println("Car returned successfully by " + active.getCustomer().getName());
            } else {
                System.out.println("Car returned successfully.");
            }
        } else {
            System.out.println("Invalid car ID or car is not rented.");
        }
    }
    // Add a new car to the system
    private void addNewCar(Scanner scanner) {
        System.out.println("\n== Add New Car ==\n");
        System.out.print("Enter Car ID: ");
        String carId = scanner.nextLine();
        System.out.print("Enter Brand: ");
        String brand = scanner.nextLine();
        System.out.print("Enter Model: ");
        String model = scanner.nextLine();
        System.out.print("Enter Base Price Per Day: ");
        double basePrice = scanner.nextDouble();
        scanner.nextLine();
        Car newCar = new Car(carId, brand, model, basePrice);
        try {
            Car.addCar(newCar);
            System.out.println("\nCar added successfully!");
        } catch (SQLException e) {
            System.out.println("Failed saving car: " + e.getMessage());
        }
    }

    private void clearDatabase(Scanner scanner) {
        System.out.println("\n== Clear Database ==\n");
        System.out.println("WARNING: This will delete ALL cars, customers, and rentals!");
        System.out.print("Type 'CLEAR' to confirm: ");
        String confirm = scanner.nextLine();
        if ("CLEAR".equals(confirm)) {
            try (Connection conn = Database.getConnection(); Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM rentals");
                st.executeUpdate("DELETE FROM cars");
                st.executeUpdate("DELETE FROM customers");
                System.out.println("\nDatabase cleared successfully!");
            } catch (SQLException e) {
                System.out.println("Failed to clear database: " + e.getMessage());
            }
        } else {
            System.out.println("Database clear operation cancelled.");
        }
    }
    // Generate a simple unique customer ID
    private String generateCustomerId() {
        long now = System.currentTimeMillis();
        return "CUS" + now;
    }
}
