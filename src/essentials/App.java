package essentials;


 //Main application entry point for the Car Rental System.
public class App {
    
    public static void main(String[] args) {
        // Initialize the main car rental system
        CarRentalSystem rentalSystem = new CarRentalSystem();

        // Hard Coded input for the cars
        Car car1 = new Car("1", "Suzuki", "Breeza", 100.0);  // Economy car
        Car car2 = new Car("2", "Honda", "City", 150.0);     // Mid-range sedan
        Car car3 = new Car("3", "Mahindra", "3XO", 80.0);    // Budget option

        // Persist sample cars to the database
        try {
            Car.addCar(car1);
            Car.addCar(car2);
            Car.addCar(car3);
        } catch (java.sql.SQLException e) {
            System.out.println("Failed saving sample cars: " + e.getMessage());
        }

        // Start the interactive menu system
        rentalSystem.menu();
    }
}
