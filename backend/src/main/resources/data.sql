-- Technicians
INSERT INTO technicians (name, assigned_region, availability_start, availability_end, maximum_workload_minutes, status) VALUES 
('Alice (Plumber)', 'NORTH', '09:00:00', '17:00:00', 480, 'AVAILABLE'),
('Bob (Electrician)', 'SOUTH', '09:00:00', '17:00:00', 480, 'AVAILABLE'),
('Charlie (HVAC)', 'NORTH', '10:00:00', '18:00:00', 480, 'AVAILABLE'),
('Diana (Multi)', 'SOUTH', '08:00:00', '16:00:00', 480, 'AVAILABLE');

-- Technician Skills
INSERT INTO technician_skills (technician_id, skill) VALUES 
(1, 'PLUMBING'),
(2, 'ELECTRICAL'),
(3, 'HVAC'),
(4, 'PLUMBING'),
(4, 'ELECTRICAL');

-- Service Requests
INSERT INTO service_requests (title, region, required_skill, priority, estimated_duration_minutes, preferred_start_time, preferred_end_time, status, created_at) VALUES 
('Fix leaky pipe', 'NORTH', 'PLUMBING', 'HIGH', 120, '10:00:00', '12:00:00', 'PENDING', CURRENT_TIMESTAMP),
('Install ceiling fan', 'SOUTH', 'ELECTRICAL', 'MEDIUM', 90, '13:00:00', '15:00:00', 'PENDING', CURRENT_TIMESTAMP),
('AC Maintenance', 'NORTH', 'HVAC', 'LOW', 60, '11:00:00', '14:00:00', 'PENDING', CURRENT_TIMESTAMP),
('Emergency Wire Short', 'SOUTH', 'ELECTRICAL', 'EMERGENCY', 120, '09:00:00', '12:00:00', 'PENDING', CURRENT_TIMESTAMP),
('Skill Mismatch Demo', 'SOUTH', 'CARPENTRY', 'MEDIUM', 60, '10:00:00', '11:00:00', 'PENDING', CURRENT_TIMESTAMP),
('Region Mismatch Demo', 'WEST', 'PLUMBING', 'MEDIUM', 60, '10:00:00', '11:00:00', 'PENDING', CURRENT_TIMESTAMP),
('Unassignable Time', 'NORTH', 'PLUMBING', 'HIGH', 120, '19:00:00', '21:00:00', 'PENDING', CURRENT_TIMESTAMP),
('Overload Test', 'NORTH', 'PLUMBING', 'LOW', 400, '09:00:00', '17:00:00', 'PENDING', CURRENT_TIMESTAMP);

-- Active Schedule
INSERT INTO schedules (working_date, current_version, status) VALUES 
(CURRENT_DATE, 1, 'DRAFT');
