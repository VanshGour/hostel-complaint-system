-- V2: Seed initial users and sample complaints
-- Password for all seed users is 'password123' (BCrypt hashed)
-- Hash: $2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxGQrvkWGyWm

INSERT INTO users (id, email, password_hash, full_name, phone_number, role, hostel_block, room_number, trade_specialty, is_active)
VALUES
(1, 'warden@campus.edu', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxGQrvkWGyWm', 'Dr. Ramesh Sharma (Chief Warden)', '+91-9876543210', 'ADMIN', 'Block A', 'Office-101', NULL, TRUE),
(2, 'electrician@campus.edu', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxGQrvkWGyWm', 'Suresh Kumar (Electrician)', '+91-9876543211', 'STAFF', 'Campus-Wide', 'Maint-01', 'ELECTRICAL', TRUE),
(3, 'plumber@campus.edu', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxGQrvkWGyWm', 'Mukesh Yadav (Plumber)', '+91-9876543212', 'STAFF', 'Campus-Wide', 'Maint-02', 'PLUMBING', TRUE),
(4, 'carpenter@campus.edu', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxGQrvkWGyWm', 'Rajesh Verma (Carpenter)', '+91-9876543213', 'STAFF', 'Campus-Wide', 'Maint-03', 'CARPENTRY', TRUE),
(5, 'rahul@campus.edu', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxGQrvkWGyWm', 'Rahul Verma', '+91-9876543214', 'STUDENT', 'Block A', '304', NULL, TRUE),
(6, 'priya@campus.edu', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxGQrvkWGyWm', 'Priya Patel', '+91-9876543215', 'STUDENT', 'Block B', '112', NULL, TRUE)
ON CONFLICT (email) DO NOTHING;

-- Reset sequence to avoid ID collisions on new inserts
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

-- Seed sample complaints
INSERT INTO complaints (id, title, description, category, hostel_block, room_or_area, priority, status, created_by_id, assigned_to_id, upvote_count, version, last_status_change_at, created_at)
VALUES
(1, 'Ceiling fan making screeching noise and wobbling', 'The ceiling fan regulator sparks occasionally and the blades wobble violently at speed 3 and above.', 'ELECTRICAL', 'Block A', 'Room 304', 'HIGH', 'ASSIGNED', 5, 2, 3, 1, NOW() - INTERVAL '6 hours', NOW() - INTERVAL '12 hours'),
(2, 'Common washroom geyser not heating water', 'The geyser on 2nd floor wing B turns on power LED but water remains ice cold.', 'ELECTRICAL', 'Block B', '2nd Floor Washroom', 'URGENT', 'OPEN', 6, NULL, 7, 0, NOW() - INTERVAL '50 hours', NOW() - INTERVAL '50 hours'),
(3, 'Drainage blockage causing water accumulation', 'Shower drain is clogged with debris, causing water to flood the corridor.', 'PLUMBING', 'Block A', '1st Floor Washroom', 'HIGH', 'IN_PROGRESS', 5, 3, 2, 2, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '24 hours'),
(4, 'Study table leg broken and drawer stuck', 'One leg of the double study table collapsed under books.', 'CARPENTRY', 'Block B', 'Room 112', 'MEDIUM', 'RESOLVED', 6, 4, 1, 3, NOW() - INTERVAL '3 hours', NOW() - INTERVAL '48 hours')
ON CONFLICT (id) DO NOTHING;

SELECT setval('complaints_id_seq', (SELECT MAX(id) FROM complaints));
