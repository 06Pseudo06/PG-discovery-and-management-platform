CREATE DATABASE IF NOT EXISTS pgfinder;
USE pgfinder;

-- Drop tables if they exist (in reverse dependency order)
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS verifications;
DROP TABLE IF EXISTS booking_requests;
DROP TABLE IF EXISTS beds;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS pgs;
DROP TABLE IF EXISTS users;

-- 1. Users Table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('STUDENT', 'OWNER') NOT NULL,
    phone VARCHAR(20) NOT NULL
);

-- 2. PGs Table
CREATE TABLE pgs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    owner_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(50) NOT NULL,
    area VARCHAR(50) NOT NULL,
    description TEXT,
    gender_preference ENUM('male', 'female', 'any') NOT NULL DEFAULT 'any',
    food_available BOOLEAN NOT NULL DEFAULT FALSE,
    wifi_available BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_pgs_city (city),
    INDEX idx_pgs_city_gender (city, gender_preference)
);

-- 3. Rooms Table (Part of cascade chain PG -> Room -> Bed)
CREATE TABLE rooms (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pg_id INT NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    room_type VARCHAR(50) NOT NULL,
    rent DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (pg_id) REFERENCES pgs(id) ON DELETE CASCADE
);

-- 4. Beds Table (Part of cascade chain PG -> Room -> Bed)
CREATE TABLE beds (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_id INT NOT NULL,
    bed_label VARCHAR(20) NOT NULL,
    status ENUM('vacant', 'occupied') NOT NULL DEFAULT 'vacant',
    deposit DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
);

-- 5. Booking Requests Table
-- ON DELETE RESTRICT on student_id and bed_id to prevent loss of transaction history
CREATE TABLE booking_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    bed_id INT NOT NULL,
    status ENUM('pending', 'approved', 'rejected') NOT NULL DEFAULT 'pending',
    requested_move_in_date DATE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    decided_at DATETIME NULL,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (bed_id) REFERENCES beds(id) ON DELETE RESTRICT
);

-- 6. Verifications Table
-- ON DELETE RESTRICT on booking_id to preserve verification history
CREATE TABLE verifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    code VARCHAR(20) NOT NULL,
    generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME NOT NULL,
    confirmed_at DATETIME NULL,
    status ENUM('pending', 'confirmed', 'expired') NOT NULL DEFAULT 'pending',
    FOREIGN KEY (booking_id) REFERENCES booking_requests(id) ON DELETE RESTRICT
);

-- 7. Reviews Table
-- ON DELETE RESTRICT on verification_id and pg_id to prevent deleting feedback history
CREATE TABLE reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    verification_id INT NOT NULL,
    pg_id INT NOT NULL,
    food_rating INT NOT NULL,
    cleanliness_rating INT NOT NULL,
    wifi_rating INT NOT NULL,
    owner_behavior_rating INT NOT NULL,
    comment TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (verification_id) REFERENCES verifications(id) ON DELETE RESTRICT,
    FOREIGN KEY (pg_id) REFERENCES pgs(id) ON DELETE RESTRICT
);
