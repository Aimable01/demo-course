-- Sample data for testing the Course Management System

-- Insert sample students
INSERT INTO students (first_name, last_name, email) VALUES 
('John', 'Doe', 'john.doe@example.com'),
('Jane', 'Smith', 'jane.smith@example.com'),
('Mike', 'Johnson', 'mike.johnson@example.com'),
('Sarah', 'Wilson', 'sarah.wilson@example.com'),
('David', 'Brown', 'david.brown@example.com')
ON CONFLICT (email) DO NOTHING;

-- Insert sample courses
INSERT INTO courses (name, code, credits) VALUES 
('Introduction to Programming', 'CS101', 3),
('Data Structures and Algorithms', 'CS201', 4),
('Database Systems', 'CS301', 3),
('Software Engineering', 'CS401', 4),
('Computer Networks', 'CS501', 3),
('Operating Systems', 'CS601', 4),
('Web Development', 'CS701', 3),
('Machine Learning', 'CS801', 4)
ON CONFLICT (code) DO NOTHING;

-- Insert sample grades
INSERT INTO grades (student_id, course_id, score, letter_grade) VALUES 
(1, 1, 85.5, 'B'),
(1, 2, 92.0, 'A'),
(1, 3, 78.5, 'C'),
(2, 1, 95.0, 'A'),
(2, 2, 88.0, 'B'),
(2, 4, 91.5, 'A'),
(3, 1, 72.0, 'C'),
(3, 3, 85.0, 'B'),
(3, 5, 79.5, 'C'),
(4, 2, 96.5, 'A'),
(4, 4, 89.0, 'B'),
(4, 6, 93.5, 'A'),
(5, 1, 68.0, 'D'),
(5, 3, 75.5, 'C'),
(5, 7, 82.0, 'B')
ON CONFLICT (student_id, course_id) DO NOTHING;

