# PGFinder

PGFinder is a premium Java desktop application designed for finding and booking paying-guest (PG) accommodations. Built using JavaFX, JDBC, and MySQL, it supports role-based views for **Students** (browsing, filtering, requesting bookings, reviewing PGs) and **Owners** (managing PGs, rooms, beds, and approving/rejecting booking requests).

---

## Technical Stack
- **Language**: Java 17 or higher (tested on JDK 25)
- **GUI Framework**: JavaFX 21.0.2 (Controls & FXML)
- **Database**: MySQL Server 8.0+
- **Security**: jBCrypt 0.4 (Password hashing)
- **Testing**: JUnit 5 (Jupiter 5.10.2)
- **Build Tool**: Maven

---

## Directory & Package Layout
The project follows standard Maven directories (`src/main/java`, `src/main/resources`, `src/test/java`):

```text
com.pgfinder/
├── App.java                   # Main Application Entry Point
├── config/
│   └── DBConnection.java      # Database Connection manager (loads classpath resources)
├── model/                     # Plain Java Objects representing database entities
│   ├── User.java, PG.java, Room.java, Bed.java, BookingRequest.java, Review.java, Verification.java
├── dao/                       # Data Access Objects (parameterized queries, try-with-resources)
│   ├── UserDAO.java, PGDAO.java, RoomDAO.java, BedDAO.java, BookingDAO.java, ReviewDAO.java, VerificationDAO.java
├── service/                   # Business Logic services (Auth, Booking, reviews, etc.)
├── controller/                # JavaFX Controllers mapping FXML widgets to services
├── util/                      # Utilities (SceneManager, SessionManager, BCryptUtil, AlertUtil)
└── dsa/                       # Custom Data Structures & Algorithms
    ├── Trie.java, TrieNode.java, AreaTrie.java   # Prefix-matching autocomplete structure
    ├── BookingQueue.java                          # FIFO Booking Request Queue
    └── RatingPriorityQueue.java                   # PG Sorting by Rating Priority Queue
```

---

## Installation & Setup

### 1. Database Configuration
1. Start your local MySQL server instance.
2. Run the database schema initialization script:
   ```bash
   mysql -u <username> -p < src/main/resources/db/schema.sql
   ```
3. Seed the database with sample demo data (realistic Indian student PG prices, rooms, and hashed passwords):
   ```bash
   mysql -u <username> -p < src/main/resources/db/seed.sql
   ```

### 2. Properties Setup
Copy the sample database properties file and add your credentials:
```bash
cp src/main/resources/config.properties.example src/main/resources/config.properties
```
Edit the actual `src/main/resources/config.properties` file with your local MySQL credentials:
```properties
db.url=jdbc:mysql://localhost:3306/pgfinder
db.username=your_mysql_username
db.password=your_mysql_password
```
*(Note: `config.properties` is configured in `.gitignore` and will never be committed to Git).*

### 3. Build & Test
Verify that the configuration is working by running the unit tests:
```bash
mvn clean test
```

### 4. Running the Application
Launch the JavaFX user interface:
```bash
mvn javafx:run
```

---

## Custom Algorithms
- **Trie (Prefix Autocomplete)**: A custom implementation of a retrieval tree (`Trie`/`TrieNode`) wrapped by `AreaTrie`. It queries the database for distinct area names (e.g., Kothrud, Viman Nagar, Hinjewadi) and builds prefix suggestion lists in real-time as the user types in search filters.
- **BookingQueue**: A queue managing incoming bookings under a first-in, first-out workflow.
- **RatingPriorityQueue**: Priority queue sorting PGs based on reviews/ratings.

---

## Seeded Demo Accounts
To test the application, use these pre-seeded logins:
*   **Student Role**:
    *   **Email**: `student1@pgfinder.com`
    *   **Password**: `password123`
*   **Owner Role**:
    *   **Email**: `owner1@pgfinder.com`
    *   **Password**: `password123`
