## CarRes - MySQL Setup

This project now persists data to MySQL using JDBC. Follow the steps below to run it locally.

### 1) Install MySQL and create database

Run these commands in MySQL:

```sql
CREATE DATABASE IF NOT EXISTS car_reservation CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Tables are auto-created on app start.

### 2) Add MySQL JDBC driver to classpath

Download MySQL Connector/J (e.g., `mysql-connector-j-8.x.x.jar`). Place it in `lib/` and include it on the classpath when compiling/running.

Example compile/run:

```bash
javac -cp lib/mysql-connector-j-8.4.0.jar -d bin src/essentials/*.java
java -cp bin;lib/mysql-connector-j-8.4.0.jar essentials.App
```

On Unix/macOS replace `;` with `:` in the classpath.

### 3) Configure database credentials

The app supports multiple configuration methods with priority order:

#### Option A: .env file (Recommended)
Create a `.env` file in your project root:

```env
# Database Configuration
DB_URL=jdbc:mysql://localhost:3306/car_reservation?useSSL=false&serverTimezone=UTC
DB_USER=root
DB_PASS=your_password_here

# Optional: Database Name, Host, and Port
DB_NAME=car_reservation
DB_HOST=localhost
DB_PORT=3306
```

#### Option B: Environment variables
Set these environment variables (overrides .env file):

**Windows PowerShell:**
```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/car_reservation?useSSL=false&serverTimezone=UTC"
$env:DB_USER = "root"
$env:DB_PASS = "your_password"
```

**Windows Command Prompt:**
```cmd
set DB_URL=jdbc:mysql://localhost:3306/car_reservation?useSSL=false&serverTimezone=UTC
set DB_USER=root
set DB_PASS=your_password
```

**Unix/Linux/macOS:**
```bash
export DB_URL="jdbc:mysql://localhost:3306/car_reservation?useSSL=false&serverTimezone=UTC"
export DB_USER="root"
export DB_PASS="your_password"
```

#### Option C: Default values
If neither .env file nor environment variables are set, the app uses these defaults:
- `DB_URL`: `jdbc:mysql://localhost:3306/car_reservation?useSSL=false&serverTimezone=UTC`
- `DB_USER`: `root`
- `DB_PASS`: `Harish_1234`

### 4) Seed cars

`App.main` seeds three cars in the database at startup if they don't exist. You can adjust or remove that logic in `src/essentials/App.java`.

### 5) Configuration debugging

The app will show which configuration source it's using:
- "Configuration loaded from .env file" - if .env file is found
- "No .env file found, using default configuration" - if using defaults

You can also call `Database.getCurrentConfig()` to see the current configuration being used.

### Notes

- Tables created:
  - `cars(car_id, brand, model, base_price_per_day, is_available)`
  - `customers(customer_id, name)`
  - `rentals(id, car_id, customer_id, days, returned, created_at)`
- The in-memory lists are kept for runtime operations but are synchronized with the database on writes.
- Configuration priority: Environment Variables > .env file > Default values
