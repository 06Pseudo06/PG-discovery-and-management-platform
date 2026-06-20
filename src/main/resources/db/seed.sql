USE pgfinder;

-- 1. Seed Users (Password is 'password123')
INSERT INTO users (id, name, email, password_hash, role, phone) VALUES
(1, 'Rajesh Mehta', 'owner1@pgfinder.com', '$2b$12$4i1aPF5aY9rLjgjzXMQmFOk6b8jQJ3NJR7Sb3kOghvBf5BclZAP0G', 'OWNER', '9876543210'),
(2, 'Sunita Sharma', 'owner2@pgfinder.com', '$2b$12$4i1aPF5aY9rLjgjzXMQmFOk6b8jQJ3NJR7Sb3kOghvBf5BclZAP0G', 'OWNER', '9876543211'),
(3, 'Amit Patel', 'student1@pgfinder.com', '$2b$12$4i1aPF5aY9rLjgjzXMQmFOk6b8jQJ3NJR7Sb3kOghvBf5BclZAP0G', 'STUDENT', '9123456780'),
(4, 'Neha Gupta', 'student2@pgfinder.com', '$2b$12$4i1aPF5aY9rLjgjzXMQmFOk6b8jQJ3NJR7Sb3kOghvBf5BclZAP0G', 'STUDENT', '9123456781'),
(5, 'Rahul Verma', 'student3@pgfinder.com', '$2b$12$4i1aPF5aY9rLjgjzXMQmFOk6b8jQJ3NJR7Sb3kOghvBf5BclZAP0G', 'STUDENT', '9123456782');

-- 2. Seed PGs (9 PGs: 5 in Pune, 4 in Mumbai)
INSERT INTO pgs (id, owner_id, name, address, city, area, description, gender_preference, food_available, wifi_available) VALUES
(1, 1, 'Oxford PG', 'Near MIT College, Kothrud', 'Pune', 'Kothrud', 'Premium student PG near MIT. Single and double rooms.', 'any', TRUE, TRUE),
(2, 1, 'Skyline PG', 'Symbiosis Road, Viman Nagar', 'Pune', 'Viman Nagar', 'High-speed internet, food included. Girls only.', 'female', TRUE, TRUE),
(3, 1, 'TechPark Residency', 'Phase 1, Hinjewadi', 'Pune', 'Hinjewadi', 'Spacious rooms for techies and students near Phase 1.', 'male', FALSE, TRUE),
(4, 2, 'Elite PG', 'Pan Card Club Road, Baner', 'Pune', 'Baner', 'Luxury rooms with food, gym and parking.', 'any', TRUE, TRUE),
(5, 2, 'Deccan Heritage PG', 'FC Road, Deccan Gymkhana', 'Pune', 'Deccan', 'Historic area PG near Fergusson College.', 'any', TRUE, FALSE),
(6, 1, 'Metro Heights PG', 'Andheri East, Near Metro Stn', 'Mumbai', 'Andheri', 'Cozy rooms for students, close to metro.', 'male', FALSE, TRUE),
(7, 2, 'Sea View PG', 'Carter Road, Bandra West', 'Mumbai', 'Bandra', 'Beautiful sea facing rooms in Bandra.', 'female', TRUE, TRUE),
(8, 2, 'Dadar Central PG', 'Near Dadar Station, Dadar', 'Mumbai', 'Dadar', 'Centrally located, easy commute across Mumbai.', 'any', FALSE, FALSE),
(9, 1, 'Andheri Elite PG', 'Veera Desai Road, Andheri West', 'Mumbai', 'Andheri', 'Premium girls PG near cinemas and cafes.', 'female', TRUE, TRUE);

-- 3. Seed Rooms (2 rooms per PG, total 18 rooms)
INSERT INTO rooms (id, pg_id, room_number, room_type, rent) VALUES
-- Oxford PG (ID 1)
(1, 1, '101', 'Double Sharing', 8000.00),
(2, 1, '102', 'Single Sharing', 12000.00),
-- Skyline PG (ID 2)
(3, 2, '201', 'Double Sharing', 9500.00),
(4, 2, '202', 'Triple Sharing', 7500.00),
-- TechPark Residency (ID 3)
(5, 3, '301', 'Double Sharing', 7000.00),
(6, 3, '302', 'Single Sharing', 11000.00),
-- Elite PG (ID 4)
(7, 4, '401', 'Double Sharing', 10000.00),
(8, 4, '402', 'Single Sharing', 15000.00),
-- Deccan Heritage PG (ID 5)
(9, 5, '501', 'Double Sharing', 8500.00),
(10, 5, '502', 'Triple Sharing', 6500.00),
-- Metro Heights PG (ID 6)
(11, 6, '601', 'Double Sharing', 9000.00),
(12, 6, '602', 'Single Sharing', 14000.00),
-- Sea View PG (ID 7)
(13, 7, '701', 'Double Sharing', 12000.00),
(14, 7, '702', 'Single Sharing', 15000.00),
-- Dadar Central PG (ID 8)
(15, 8, '801', 'Double Sharing', 8500.00),
(16, 8, '802', 'Triple Sharing', 6000.00),
-- Andheri Elite PG (ID 9)
(17, 9, '901', 'Double Sharing', 11000.00),
(18, 9, '902', 'Single Sharing', 14500.00);

-- 4. Seed Beds (2-3 beds per room)
INSERT INTO beds (id, room_id, bed_label, status, deposit) VALUES
-- Room 101 (Double)
(1, 1, '101-A', 'occupied', 15000.00),
(2, 1, '101-B', 'vacant', 15000.00),
-- Room 102 (Single)
(3, 2, '102-A', 'vacant', 20000.00),
-- Room 201 (Double)
(4, 3, '201-A', 'vacant', 18000.00),
(5, 3, '201-B', 'vacant', 18000.00),
-- Room 202 (Triple)
(6, 4, '202-A', 'vacant', 12000.00),
(7, 4, '202-B', 'vacant', 12000.00),
(8, 4, '202-C', 'vacant', 12000.00),
-- Room 301 (Double)
(9, 5, '301-A', 'occupied', 10000.00),
(10, 5, '301-B', 'vacant', 10000.00),
-- Room 302 (Single)
(11, 6, '302-A', 'vacant', 20000.00),
-- Room 401 (Double)
(12, 7, '401-A', 'vacant', 20000.00),
(13, 7, '401-B', 'vacant', 20000.00),
-- Room 402 (Single)
(14, 8, '402-A', 'vacant', 30000.00),
-- Room 501 (Double)
(15, 9, '501-A', 'vacant', 15000.00),
(16, 9, '501-B', 'vacant', 15000.00),
-- Room 502 (Triple)
(17, 10, '502-A', 'vacant', 10000.00),
(18, 10, '502-B', 'vacant', 10000.00),
(19, 10, '502-C', 'vacant', 10000.00),
-- Room 601 (Double)
(20, 11, '601-A', 'vacant', 18000.00),
(21, 11, '601-B', 'vacant', 18000.00),
-- Room 602 (Single)
(22, 12, '602-A', 'vacant', 25000.00),
-- Room 701 (Double)
(23, 13, '701-A', 'vacant', 24000.00),
(24, 13, '701-B', 'vacant', 24000.00),
-- Room 702 (Single)
(25, 14, '702-A', 'occupied', 30000.00),
-- Room 801 (Double)
(26, 15, '801-A', 'vacant', 15000.00),
(27, 15, '801-B', 'vacant', 15000.00),
-- Room 802 (Triple)
(28, 16, '802-A', 'vacant', 10000.00),
(29, 16, '802-B', 'vacant', 10000.00),
(30, 16, '802-C', 'vacant', 10000.00),
-- Room 901 (Double)
(31, 17, '901-A', 'vacant', 22000.00),
(32, 17, '901-B', 'vacant', 22000.00),
-- Room 902 (Single)
(33, 18, '902-A', 'vacant', 25000.00);
