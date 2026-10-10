CREATE DATABASE IF NOT EXISTS college_timetable_system;
USE college_timetable_system;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS timetable;
DROP TABLE IF EXISTS fixed_activity;
DROP TABLE IF EXISTS time_slot;
DROP TABLE IF EXISTS subject;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS leave_table;
DROP TABLE IF EXISTS duty;
DROP TABLE IF EXISTS room;
DROP TABLE IF EXISTS division;
DROP TABLE IF EXISTS faculty;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE faculty (
    faculty_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL
);

CREATE TABLE division (
    division_id INT PRIMARY KEY AUTO_INCREMENT,
    division_code VARCHAR(30) NOT NULL UNIQUE,
    year_name VARCHAR(30) NOT NULL,
    section_name VARCHAR(30) NOT NULL,
    classroom VARCHAR(50) NOT NULL,
    coordinator_name VARCHAR(100) NOT NULL
);

CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    faculty_id INT NULL,
    division_id INT NULL,
    CONSTRAINT fk_users_faculty
        FOREIGN KEY (faculty_id) REFERENCES faculty(faculty_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_users_division
        FOREIGN KEY (division_id) REFERENCES division(division_id)
        ON DELETE SET NULL
);

CREATE TABLE room (
    room_id INT PRIMARY KEY AUTO_INCREMENT,
    room_number VARCHAR(50) NOT NULL,
    room_type VARCHAR(20) NOT NULL DEFAULT 'CLASSROOM'
);

CREATE TABLE time_slot (
    slot_id INT PRIMARY KEY AUTO_INCREMENT,
    slot_label VARCHAR(30) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    sort_order INT NOT NULL,
    is_break BOOLEAN NOT NULL DEFAULT FALSE,
    break_name VARCHAR(50) NULL
);

CREATE TABLE subject (
    subject_id INT PRIMARY KEY AUTO_INCREMENT,
    subject_name VARCHAR(100) NOT NULL,
    hours_per_week INT NOT NULL,
    faculty_id INT NOT NULL,
    division_id INT NOT NULL,
    activity_type VARCHAR(20) NOT NULL DEFAULT 'THEORY',
    preferred_room_type VARCHAR(20) NOT NULL DEFAULT 'CLASSROOM',
    batch_label VARCHAR(20) NOT NULL DEFAULT '',
    CONSTRAINT fk_subject_faculty
        FOREIGN KEY (faculty_id) REFERENCES faculty(faculty_id),
    CONSTRAINT fk_subject_division
        FOREIGN KEY (division_id) REFERENCES division(division_id)
);

CREATE TABLE timetable (
    timetable_id INT PRIMARY KEY AUTO_INCREMENT,
    division_id INT NOT NULL,
    faculty_id INT NOT NULL,
    subject_id INT NOT NULL,
    room_id INT NOT NULL,
    schedule_date DATE NOT NULL,
    day_name VARCHAR(20) NOT NULL,
    slot_id INT NOT NULL,
    batch_label VARCHAR(20) NOT NULL DEFAULT '',
    notes VARCHAR(255) NULL,
    CONSTRAINT fk_timetable_division FOREIGN KEY (division_id) REFERENCES division(division_id),
    CONSTRAINT fk_timetable_faculty FOREIGN KEY (faculty_id) REFERENCES faculty(faculty_id),
    CONSTRAINT fk_timetable_subject FOREIGN KEY (subject_id) REFERENCES subject(subject_id),
    CONSTRAINT fk_timetable_room FOREIGN KEY (room_id) REFERENCES room(room_id),
    CONSTRAINT fk_timetable_slot FOREIGN KEY (slot_id) REFERENCES time_slot(slot_id),
    CONSTRAINT uq_division_schedule UNIQUE (division_id, schedule_date, slot_id, batch_label),
    CONSTRAINT uq_faculty_schedule UNIQUE (faculty_id, schedule_date, slot_id, batch_label),
    CONSTRAINT uq_room_schedule UNIQUE (room_id, schedule_date, slot_id, batch_label)
);

CREATE TABLE fixed_activity (
    fixed_activity_id INT PRIMARY KEY AUTO_INCREMENT,
    division_id INT NOT NULL,
    activity_title VARCHAR(100) NOT NULL,
    faculty_text VARCHAR(100) NULL,
    room_text VARCHAR(100) NULL,
    activity_day VARCHAR(20) NOT NULL,
    slot_id INT NOT NULL,
    batch_label VARCHAR(30) NOT NULL DEFAULT '',
    notes VARCHAR(255) NULL,
    CONSTRAINT fk_fixed_activity_division FOREIGN KEY (division_id) REFERENCES division(division_id),
    CONSTRAINT fk_fixed_activity_slot FOREIGN KEY (slot_id) REFERENCES time_slot(slot_id)
);

CREATE TABLE duty (
    duty_id INT PRIMARY KEY AUTO_INCREMENT,
    faculty_id INT NOT NULL,
    duty_type VARCHAR(100) NOT NULL,
    date DATE NOT NULL,
    CONSTRAINT fk_duty_faculty FOREIGN KEY (faculty_id) REFERENCES faculty(faculty_id)
);

CREATE TABLE leave_table (
    leave_id INT PRIMARY KEY AUTO_INCREMENT,
    faculty_id INT NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    CONSTRAINT fk_leave_faculty FOREIGN KEY (faculty_id) REFERENCES faculty(faculty_id)
);

INSERT INTO faculty (name, department, email) VALUES
('Dr. A. G. Patil', 'CSE-AIML', 'agpatil@college.com'),
('Dr. S. D. Jadhav', 'CSE-AIML', 'sdjadhav@college.com'),
('Prof. K. M. Thorat', 'CSE-AIML', 'kmthorat@college.com'),
('Prof. M. M. Gaikwad', 'CSE-AIML', 'mmgaikwad@college.com'),
('Prof. S. G. Bhosale', 'CSE-AIML', 'sgbhosale@college.com'),
('Prof. P. S. Kamble', 'CSE-AIML', 'pskamble@college.com'),
('Prof. N. B. Patil', 'CSE-AIML', 'nbpatil@college.com'),
('Prof. V. K. Kulkarni', 'CSE-AIML', 'vkkulkarni@college.com'),
('Prof. A. K. Khot', 'CSE-AIML', 'akkhot@college.com');

INSERT INTO division (division_code, year_name, section_name, classroom, coordinator_name) VALUES
('SYAIML-A', 'Second Year', 'A', 'CSAMCR01', 'Mrs. Sapana G. Buwa'),
('SYAIML-B', 'Second Year', 'B', 'CSAMCR02', 'Mrs. Sujeeta Shah'),
('SYAIML-C', 'Second Year', 'C', 'CSDSCR01', 'Mrs. Komal Jadhav'),
('TYAIML-A', 'Third Year', 'A', 'CSAMCR03', 'Ms. Vajreshwari Kulal'),
('TYAIML-B', 'Third Year', 'B', 'CSAMCR04', 'Mrs. Amruta Chougule'),
('TYDS', 'Third Year', 'DS', 'CSAMCR05', 'Mrs. Karishma M. Tamboli');

INSERT INTO users (username, password, role, faculty_id, division_id) VALUES
('admin1', 'admin123', 'ADMIN', NULL, NULL),
('agpatil', 'fac123', 'FACULTY', 1, NULL),
('sdjadhav', 'fac123', 'FACULTY', 2, NULL),
('kmthorat', 'fac123', 'FACULTY', 3, NULL),
('mmgaikwad', 'fac123', 'FACULTY', 4, NULL),
('sgbhosale', 'fac123', 'FACULTY', 5, NULL),
('pskamble', 'fac123', 'FACULTY', 6, NULL),
('nbpatil', 'fac123', 'FACULTY', 7, NULL),
('vkkulkarni', 'fac123', 'FACULTY', 8, NULL),
('akkhot', 'fac123', 'FACULTY', 9, NULL),
('student1', 'stud123', 'STUDENT', NULL, 1);

INSERT INTO room (room_number, room_type) VALUES
('CSAMCR01', 'CLASSROOM'),
('CSAMCR02', 'CLASSROOM'),
('CSDSCR01', 'CLASSROOM'),
('CSAMCR03', 'CLASSROOM'),
('CSAMCR04', 'CLASSROOM'),
('CSAMCR05', 'CLASSROOM'),
('Lab 1', 'LAB'),
('Lab 2', 'LAB'),
('Lab 3', 'LAB'),
('Lab 4', 'LAB'),
('Lab 5', 'LAB'),
('Lab 6', 'LAB');

INSERT INTO time_slot (slot_label, start_time, end_time, sort_order, is_break, break_name) VALUES
('09:15-10:15', '09:15:00', '10:15:00', 1, FALSE, NULL),
('10:15-11:15', '10:15:00', '11:15:00', 2, FALSE, NULL),
('11:15-11:45', '11:15:00', '11:45:00', 3, TRUE, 'Break'),
('11:45-12:45', '11:45:00', '12:45:00', 4, FALSE, NULL),
('12:45-01:45', '12:45:00', '13:45:00', 5, FALSE, NULL),
('01:45-02:45', '13:45:00', '14:45:00', 6, TRUE, 'Lunch Break'),
('02:45-03:45', '14:45:00', '15:45:00', 7, FALSE, NULL),
('03:45-04:45', '15:45:00', '16:45:00', 8, FALSE, NULL);

INSERT INTO subject (subject_name, hours_per_week, faculty_id, division_id, activity_type, preferred_room_type, batch_label) VALUES
('AT', 3, 5, 1, 'THEORY', 'CLASSROOM', ''),
('OOP', 4, 2, 1, 'THEORY', 'CLASSROOM', ''),
('DAA', 3, 3, 1, 'THEORY', 'CLASSROOM', ''),
('CN', 2, 1, 1, 'THEORY', 'CLASSROOM', ''),
('ES', 2, 4, 1, 'THEORY', 'CLASSROOM', ''),
('SP', 2, 6, 1, 'THEORY', 'CLASSROOM', ''),
('AT', 3, 6, 2, 'THEORY', 'CLASSROOM', ''),
('OOP', 4, 7, 2, 'THEORY', 'CLASSROOM', ''),
('DAA', 3, 3, 2, 'THEORY', 'CLASSROOM', ''),
('CN', 2, 8, 2, 'THEORY', 'CLASSROOM', ''),
('ES', 2, 4, 2, 'THEORY', 'CLASSROOM', ''),
('SP', 2, 5, 2, 'THEORY', 'CLASSROOM', ''),
('AT', 3, 5, 3, 'THEORY', 'CLASSROOM', ''),
('OOP', 4, 7, 3, 'THEORY', 'CLASSROOM', ''),
('DAA', 3, 3, 3, 'THEORY', 'CLASSROOM', ''),
('CN', 2, 1, 3, 'THEORY', 'CLASSROOM', ''),
('ES', 2, 4, 3, 'THEORY', 'CLASSROOM', ''),
('SP', 2, 6, 3, 'THEORY', 'CLASSROOM', ''),
('AIDS Lab', 2, 9, 1, 'PRACTICAL', 'LAB', 'A1/A2'),
('OOP Lab', 2, 2, 1, 'PRACTICAL', 'LAB', 'A1/A2'),
('AIDS Lab', 2, 9, 2, 'PRACTICAL', 'LAB', 'B1/B2'),
('OOP Lab', 2, 7, 2, 'PRACTICAL', 'LAB', 'B1/B2'),
('AIDS Lab', 2, 9, 3, 'PRACTICAL', 'LAB', 'C1/C2'),
('OOP Lab', 2, 7, 3, 'PRACTICAL', 'LAB', 'C1/C2'),
('ML', 3, 1, 4, 'THEORY', 'CLASSROOM', ''),
('DAA-II', 3, 3, 4, 'THEORY', 'CLASSROOM', ''),
('WT', 3, 2, 4, 'THEORY', 'CLASSROOM', ''),
('SE', 2, 4, 4, 'THEORY', 'CLASSROOM', ''),
('AI', 2, 9, 4, 'THEORY', 'CLASSROOM', ''),
('ML Lab', 2, 1, 4, 'PRACTICAL', 'LAB', 'A1/A2'),
('WT Lab', 2, 2, 4, 'PRACTICAL', 'LAB', 'A1/A2'),
('ML', 3, 1, 5, 'THEORY', 'CLASSROOM', ''),
('DAA-II', 3, 3, 5, 'THEORY', 'CLASSROOM', ''),
('WT', 3, 7, 5, 'THEORY', 'CLASSROOM', ''),
('SE', 2, 4, 5, 'THEORY', 'CLASSROOM', ''),
('AI', 2, 8, 5, 'THEORY', 'CLASSROOM', ''),
('ML Lab', 2, 1, 5, 'PRACTICAL', 'LAB', 'B1/B2'),
('WT Lab', 2, 7, 5, 'PRACTICAL', 'LAB', 'B1/B2'),
('ML', 3, 1, 6, 'THEORY', 'CLASSROOM', ''),
('DAA-II', 3, 3, 6, 'THEORY', 'CLASSROOM', ''),
('WT', 3, 2, 6, 'THEORY', 'CLASSROOM', ''),
('SE', 2, 4, 6, 'THEORY', 'CLASSROOM', ''),
('AI', 2, 9, 6, 'THEORY', 'CLASSROOM', ''),
('ML Lab', 2, 1, 6, 'PRACTICAL', 'LAB', 'C1/C2'),
('WT Lab', 2, 2, 6, 'PRACTICAL', 'LAB', 'C1/C2');

INSERT INTO fixed_activity (division_id, activity_title, faculty_text, room_text, activity_day, slot_id, batch_label, notes) VALUES
(1, 'MDM II', 'US / AK / NM', 'CSAMCR01 / CSDSCR01 / CSAMCR02', 'Tuesday', 1, '', ''),
(1, 'AT', 'SGB', 'CSAMCR01', 'Tuesday', 2, '', ''),
(1, 'OOP', 'SDJ', 'CSAMCR01', 'Tuesday', 4, '', ''),
(1, 'SP', 'GP', 'CSAMCR01', 'Tuesday', 5, '', ''),
(1, 'OOP', 'SDJ', 'Lab 5', 'Tuesday', 7, 'A1', ''),
(1, 'AIDS', 'AK', 'Lab 2', 'Tuesday', 7, 'A2', ''),
(1, 'DAA', 'SS', 'CSAMCR01', 'Tuesday', 8, '', ''),
(1, 'MDM II', 'US / AK / NM', 'CSAMCR01 / CSDSCR01 / CSAMCR02', 'Wednesday', 1, '', ''),
(1, 'DAA', 'SS', 'CSAMCR01', 'Wednesday', 2, '', ''),
(1, 'OOP', 'SDJ', 'Lab 5', 'Wednesday', 4, 'A2', ''),
(1, 'DAVT', 'SSS', 'Lab 2', 'Wednesday', 4, 'A1', ''),
(1, 'OOP', 'SDJ', 'Lab 5', 'Wednesday', 5, 'A2', ''),
(1, 'DAVT', 'SSS', 'Lab 2', 'Wednesday', 5, 'A1', ''),
(1, 'CCA', '', 'CSAMCR01', 'Wednesday', 7, '', ''),
(1, 'CN', 'SM', 'CSAMCR01', 'Wednesday', 8, '', ''),
(1, 'OOP', 'SDJ', 'CSAMCR01', 'Thursday', 1, '', ''),
(1, 'MDM II', 'US / AK / NM', 'CSAMCR01 / CSDSCR01 / CSAMCR02', 'Thursday', 2, '', ''),
(1, 'AT', 'SGB', 'CSAMCR01', 'Thursday', 4, '', ''),
(1, 'SP', 'GP', 'CSAMCR01', 'Thursday', 5, '', ''),
(1, 'B.TECH.(HONS)', '', 'CSAMCR01', 'Thursday', 7, '', ''),
(1, 'DAA', 'SS', 'CSAMCR01', 'Thursday', 8, '', ''),
(1, 'AT', 'SGB', 'CSAMCR01', 'Friday', 1, '', ''),
(1, 'ES', 'MMG', 'CSAMCR01', 'Friday', 2, '', ''),
(1, 'AIDS', 'AK', 'Lab 2', 'Friday', 4, 'A1', ''),
(1, 'DAVT', 'SSS', 'Lab 5', 'Friday', 4, 'A2', ''),
(1, 'AIDS', 'AK', 'Lab 2', 'Friday', 5, 'A1', ''),
(1, 'DAVT', 'SSS', 'Lab 5', 'Friday', 5, 'A2', ''),
(1, 'SP', 'GP', 'CSAMCR01', 'Friday', 7, '', ''),
(1, 'ES', 'MMG', 'CSAMCR01', 'Friday', 8, '', ''),
(1, 'T & P', '', 'CSAMCR01', 'Saturday', 1, '', ''),
(1, 'MP-II', 'PSK', 'Lab 3', 'Saturday', 4, 'A1', ''),
(1, 'MP-II', 'SGB', 'Lab 4', 'Saturday', 4, 'A2', ''),
(1, 'MP-II', 'PSK', 'Lab 3', 'Saturday', 5, 'A1', ''),
(1, 'MP-II', 'SGB', 'Lab 4', 'Saturday', 5, 'A2', ''),
(1, 'Mentoring', '', 'CSAMCR01', 'Saturday', 7, '', ''),
(1, 'Mentoring', '', 'CSAMCR01', 'Saturday', 8, '', ''),

(2, 'MDM II', 'US / AK / NM', 'CSAMCR01 / CSDSCR01 / CSAMCR02', 'Tuesday', 1, '', ''),
(2, 'AT', 'PSK', 'CSAMCR02', 'Tuesday', 2, '', ''),
(2, 'ES', 'MMG', 'CSAMCR02', 'Tuesday', 4, '', ''),
(2, 'DAA', 'SL', 'CSAMCR02', 'Tuesday', 5, '', ''),
(2, 'MP-II', 'NM', 'Lab 2', 'Tuesday', 7, 'B1', ''),
(2, 'MP-II', 'AAP', 'Lab 6', 'Tuesday', 7, 'B2', ''),
(2, 'AT', 'PSK', 'CSAMCR02', 'Tuesday', 8, '', ''),
(2, 'MDM II', 'US / AK / NM', 'CSAMCR01 / CSDSCR01 / CSAMCR02', 'Wednesday', 1, '', ''),
(2, 'SP', 'MUM', 'CSAMCR02', 'Wednesday', 2, '', ''),
(2, 'CN', 'VK', 'CSAMCR02', 'Wednesday', 4, '', ''),
(2, 'OOP', 'NB', 'CSAMCR02', 'Wednesday', 5, '', ''),
(2, 'LeetCode Practice Session', '', 'Lab 3 / Lab 4', 'Wednesday', 7, '', ''),
(2, 'DAA', 'SL', 'CSAMCR02', 'Wednesday', 8, '', ''),
(2, 'MDM II', 'US / AK / NM', 'CSAMCR01 / CSDSCR01 / CSAMCR02', 'Thursday', 2, '', ''),
(2, 'OOP', 'NB', 'Lab 2', 'Thursday', 4, 'B1', ''),
(2, 'AIDS', 'NS', 'Lab 6', 'Thursday', 4, 'B2', ''),
(2, 'AT', 'PSK', 'CSAMCR02', 'Thursday', 5, '', ''),
(2, 'DAVT', 'SSS', 'Lab 4', 'Thursday', 7, 'B1', ''),
(2, 'OOP', 'NB', 'Lab 3', 'Thursday', 7, 'B2', ''),
(2, 'CCA', '', 'CSAMCR02', 'Thursday', 8, '', ''),
(2, 'ES', 'MMG', 'CSAMCR02', 'Friday', 1, '', ''),
(2, 'DAA', 'SL', 'CSAMCR02', 'Friday', 2, '', ''),
(2, 'SP', 'MUM', 'CSAMCR02', 'Friday', 4, '', ''),
(2, 'OOP', 'NB', 'CSAMCR02', 'Friday', 7, '', ''),
(2, 'B.TECH.(HONS)', '', 'CSAMCR02', 'Friday', 8, '', ''),
(2, 'AIDS', 'NS', 'Lab 5', 'Saturday', 1, 'B1', ''),
(2, 'DAVT', 'SSS', 'Lab 2', 'Saturday', 1, 'B2', ''),
(2, 'CN', 'VK', 'CSAMCR02', 'Saturday', 2, '', ''),
(2, 'T & P', '', 'CSAMCR02', 'Saturday', 4, '', ''),
(2, 'SP', 'MUM', 'CSAMCR02', 'Saturday', 5, '', ''),
(2, 'Mentoring', '', 'CSAMCR02', 'Saturday', 7, '', ''),
(2, 'Mentoring', '', 'CSAMCR02', 'Saturday', 8, '', ''),

(3, 'MDM II', 'US / AK / NM', 'CSAMCR01 / CSDSCR01 / CSAMCR02', 'Tuesday', 1, '', ''),
(3, 'ES', 'MMG', 'CSDSCR01', 'Tuesday', 2, '', ''),
(3, 'MP-II', 'NS', 'Lab 5', 'Tuesday', 4, 'C2', ''),
(3, 'OOP', 'NM', 'Lab 4', 'Tuesday', 4, 'C1', ''),
(3, 'SP', 'GP', 'CSDSCR01', 'Tuesday', 5, '', ''),
(3, 'SP', 'GP', 'CSDSCR01', 'Tuesday', 7, '', ''),
(3, 'DAA', 'SS', 'CSDSCR01', 'Tuesday', 8, '', ''),
(3, 'MDM II', 'US / AK / NM', 'CSAMCR01 / CSDSCR01 / CSAMCR02', 'Wednesday', 1, '', ''),
(3, 'DAA', 'SS', 'CSDSCR01', 'Wednesday', 2, '', ''),
(3, 'AT', 'SGB', 'CSDSCR01', 'Wednesday', 4, '', ''),
(3, 'AT', 'SGB', 'CSDSCR01', 'Wednesday', 5, '', ''),
(3, 'OOP', 'NM', 'Lab 2', 'Wednesday', 7, 'C2', ''),
(3, 'AIDS', 'SL', 'Lab 5', 'Wednesday', 7, 'C1', ''),
(3, 'DAA', 'SS', 'CSDSCR01', 'Wednesday', 8, '', ''),
(3, 'AT', 'SGB', 'CSDSCR01', 'Thursday', 1, '', ''),
(3, 'MDM II', 'US / AK / NM', 'CSAMCR01 / CSDSCR01 / CSAMCR02', 'Thursday', 2, '', ''),
(3, 'ES', 'MMG', 'CSDSCR01', 'Thursday', 4, '', ''),
(3, 'OOP', 'NM', 'CSDSCR01', 'Thursday', 5, '', ''),
(3, 'B.TECH.(HONS)', '', 'CSDSCR01', 'Thursday', 7, '', ''),
(3, 'CCA', '', 'CSDSCR01', 'Thursday', 8, '', ''),
(3, 'OOP', 'NM', 'CSDSCR01', 'Friday', 1, '', ''),
(3, 'CN', 'VK', 'CSDSCR01', 'Friday', 2, '', ''),
(3, 'MP-II', 'SL', 'Lab 1', 'Friday', 4, 'C1', ''),
(3, 'DAVT', 'AAP', 'Lab 6', 'Friday', 4, 'C2', ''),
(3, 'SP', 'GP', 'CSDSCR01', 'Friday', 7, '', ''),
(3, 'T & P', '', 'CSDSCR01', 'Friday', 8, '', ''),
(3, 'DAVT', 'AAP', 'Lab 1', 'Saturday', 1, 'C1', ''),
(3, 'AIDS', 'SL', 'Lab 4', 'Saturday', 1, 'C2', ''),
(3, 'CN', 'VK', 'CSDSCR01', 'Saturday', 2, '', ''),
(3, 'CN', 'VK', 'CSDSCR01', 'Saturday', 4, '', ''),
(3, 'Mentoring', '', 'CSDSCR01', 'Saturday', 7, '', ''),
(3, 'Mentoring', '', 'CSDSCR01', 'Saturday', 8, '', ''),

(4, 'OE-II / Departmental Training Session', '', 'CSAMCR03', 'Tuesday', 1, '', ''),
(4, 'MDM-IV / Departmental Training Session', 'SG / AC', 'CSAMCR03 / CSAMCR04', 'Tuesday', 2, '', ''),
(4, 'NLP', 'PSK', 'CSAMCR03', 'Tuesday', 4, '', ''),
(4, 'SEPM', 'VSP', 'CSAMCR03', 'Tuesday', 5, '', ''),
(4, 'CCA', '', 'CSAMCR03', 'Tuesday', 7, '', ''),
(4, 'DL', 'UPG', 'CSAMCR03', 'Tuesday', 8, '', ''),
(4, 'OE-II / Departmental Training Session', '', 'CSAMCR03', 'Wednesday', 1, '', ''),
(4, 'DL', 'UPG', 'CSAMCR03', 'Wednesday', 2, '', ''),
(4, 'DL', 'UPG', 'Lab 1', 'Wednesday', 4, 'A1', ''),
(4, 'IPCV', 'RK', 'Lab 3', 'Wednesday', 4, 'A2', ''),
(4, 'IPCV', 'RK', 'CSAMCR03', 'Wednesday', 5, '', ''),
(4, 'PE-II', 'NS / AAP / APB', 'CSAMCR03 / CSAMCR04 / CSAMCR05', 'Wednesday', 7, '', ''),
(4, 'SEPM', 'VSP', 'CSAMCR03', 'Wednesday', 8, '', ''),
(4, 'MP-IV', 'NB', 'Lab 4', 'Thursday', 1, 'A1', ''),
(4, 'MP-IV', 'US', 'Lab 1', 'Thursday', 1, 'A2', ''),
(4, 'MDM-IV / Departmental Training Session', 'SG / AC', 'CSAMCR03 / CSAMCR04', 'Thursday', 2, '', ''),
(4, 'AWDL', 'SDJ', 'Lab 5', 'Thursday', 4, 'A1', ''),
(4, 'DL', 'UPG', 'Lab 1', 'Thursday', 4, 'A2', ''),
(4, 'DL', 'UPG', 'CSAMCR03', 'Thursday', 5, '', ''),
(4, 'IPCV', 'RK', 'CSAMCR03', 'Thursday', 7, '', ''),
(4, 'PE-II', 'NS / AAP / APB', 'CSAMCR03 / CSAMCR04 / CSAMCR05', 'Thursday', 8, '', ''),
(4, 'OE-II / Departmental Training Session', '', 'CSAMCR03', 'Friday', 1, '', ''),
(4, 'MDM-IV / Departmental Training Session', 'SG / AC', 'CSAMCR03 / CSAMCR04', 'Friday', 2, '', ''),
(4, 'DL', 'UPG', 'CSAMCR03', 'Friday', 4, '', ''),
(4, 'IPCV', 'RK', 'CSAMCR03', 'Friday', 5, '', ''),
(4, 'T & P', '', 'CSAMCR03', 'Friday', 7, '', ''),
(4, 'IPCV', 'RK', 'CSAMCR03', 'Friday', 8, '', ''),
(4, 'NLP', 'PSK', 'CSAMCR03', 'Saturday', 1, '', ''),
(4, 'IPCV', 'RK', 'Lab 2', 'Saturday', 4, 'A1', ''),
(4, 'AWDL', 'SDJ', 'Lab 5', 'Saturday', 4, 'A2', ''),
(4, 'IPCV', 'RK', 'CSAMCR03', 'Saturday', 5, '', ''),
(4, 'PE-II', 'NS / AAP / APB', 'CSAMCR03 / CSAMCR04 / CSAMCR05', 'Saturday', 7, '', ''),

(5, 'OE-II / Departmental Training Session', '', 'CSAMCR04', 'Tuesday', 1, '', ''),
(5, 'MDM-IV / Departmental Training Session', 'SG / AC', 'CSAMCR03 / CSAMCR04', 'Tuesday', 2, '', ''),
(5, 'DL', 'SGC', 'CSAMCR04', 'Tuesday', 4, '', ''),
(5, 'IPCV', 'RK', 'CSAMCR04', 'Tuesday', 5, '', ''),
(5, 'DL', 'SGC', 'Lab 1', 'Tuesday', 7, 'B2', ''),
(5, 'MP-IV', 'DDK', 'Lab 5', 'Tuesday', 7, 'B1', ''),
(5, 'CCA', '', 'CSAMCR04', 'Tuesday', 8, '', ''),
(5, 'OE-II / Departmental Training Session', '', 'CSAMCR04', 'Wednesday', 1, '', ''),
(5, 'DL', 'SGC', 'CSAMCR04', 'Wednesday', 2, '', ''),
(5, 'NLP', 'SG', 'CSAMCR04', 'Wednesday', 4, '', ''),
(5, 'SEPM', 'VSP', 'CSAMCR04', 'Wednesday', 5, '', ''),
(5, 'PE-II', 'NS / AAP / APB', 'CSAMCR03 / CSAMCR04 / CSAMCR05', 'Wednesday', 7, '', ''),
(5, 'DL', 'SGC', 'CSAMCR04', 'Wednesday', 8, '', ''),
(5, 'AWDL', 'AC', 'Lab 5', 'Thursday', 1, 'B1', ''),
(5, 'IPCV', 'RK', 'Lab 2', 'Thursday', 1, 'B2', ''),
(5, 'MDM-IV / Departmental Training Session', 'SG / AC', 'CSAMCR03 / CSAMCR04', 'Thursday', 2, '', ''),
(5, 'MP-IV', 'DDK', 'Lab 4', 'Thursday', 4, 'B2', ''),
(5, 'IPCV', 'RK', 'Lab 3', 'Thursday', 4, 'B1', ''),
(5, 'SEPM', 'VSP', 'CSAMCR04', 'Thursday', 5, '', ''),
(5, 'PE-II', 'NS / AAP / APB', 'CSAMCR03 / CSAMCR04 / CSAMCR05', 'Thursday', 7, '', ''),
(5, 'IPCV', 'RK', 'CSAMCR04', 'Thursday', 8, '', ''),
(5, 'OE-II / Departmental Training Session', '', 'CSAMCR04', 'Friday', 1, '', ''),
(5, 'MDM-IV / Departmental Training Session', 'SG / AC', 'CSAMCR03 / CSAMCR04', 'Friday', 2, '', ''),
(5, 'T & P', '', 'CSAMCR04', 'Friday', 4, '', ''),
(5, 'B.TECH.(HONS)', '', 'CSAMCR04', 'Friday', 7, '', ''),
(5, 'PE-II', 'NS / AAP / APB', 'CSAMCR03 / CSAMCR04 / CSAMCR05', 'Friday', 8, '', ''),
(5, 'IPCV', 'RK', 'CSAMCR04', 'Saturday', 1, '', ''),
(5, 'NLP', 'SG', 'CSAMCR04', 'Saturday', 4, '', ''),
(5, 'OOP', 'NB', 'CSAMCR04', 'Saturday', 5, '', ''),
(5, 'SP', 'MUM', 'CSAMCR04', 'Saturday', 7, '', ''),

(6, 'OE-II / Departmental Training Session', '', 'CSAMCR05', 'Tuesday', 1, '', ''),
(6, 'MDM-IV / Departmental Training Session', 'SG / AC', 'CSAMCR03 / CSAMCR04', 'Tuesday', 2, '', ''),
(6, 'IPCV', 'DDK', 'CSAMCR05', 'Tuesday', 4, '', ''),
(6, 'DL', 'SGC', 'CSAMCR05', 'Tuesday', 5, '', ''),
(6, 'AWDL', 'VSP', 'Lab 4', 'Tuesday', 7, 'D1', ''),
(6, 'DL', 'RK', 'Lab 3', 'Tuesday', 7, 'D2', ''),
(6, 'CCA', '', 'CSAMCR05', 'Tuesday', 8, '', ''),
(6, 'IPCV', 'DDK', 'CSAMCR05', 'Wednesday', 2, '', ''),
(6, 'MP-IV', 'KMT', 'Lab 6', 'Wednesday', 4, 'D1', ''),
(6, 'IPCV', 'DDK', 'Lab 4', 'Wednesday', 4, 'D2', ''),
(6, 'SEPM', 'SSS', 'CSAMCR05', 'Wednesday', 5, '', ''),
(6, 'PE-II', 'NS / AAP / APB', 'CSAMCR03 / CSAMCR04 / CSAMCR05', 'Wednesday', 7, '', ''),
(6, 'OE-II / Departmental Training Session', '', 'CSAMCR05', 'Thursday', 1, '', ''),
(6, 'MDM-IV / Departmental Training Session', 'SG / AC', 'CSAMCR03 / CSAMCR04', 'Thursday', 2, '', ''),
(6, 'DL', 'SGC', 'CSAMCR05', 'Thursday', 4, '', ''),
(6, 'IPCV', 'DDK', 'CSAMCR05', 'Thursday', 5, '', ''),
(6, 'NLP', 'SG', 'CSAMCR05', 'Thursday', 7, '', ''),
(6, 'PE-II', 'NS / AAP / APB', 'CSAMCR03 / CSAMCR04 / CSAMCR05', 'Thursday', 8, '', ''),
(6, 'IPCV', 'DDK', 'Lab 3', 'Friday', 1, 'D1', ''),
(6, 'AWDL', 'VSP', 'Lab 6', 'Friday', 1, 'D2', ''),
(6, 'MDM-IV / Departmental Training Session', 'SG / AC', 'CSAMCR03 / CSAMCR04', 'Friday', 2, '', ''),
(6, 'DL', 'RK', 'Lab 4', 'Friday', 4, 'D1', ''),
(6, 'MP-IV', 'KMT', 'Lab 3', 'Friday', 4, 'D2', ''),
(6, 'IPCV', 'DDK', 'CSAMCR05', 'Friday', 5, '', ''),
(6, 'T & P', '', 'CSAMCR05', 'Friday', 7, '', ''),
(6, 'DL', 'SGC', 'CSAMCR05', 'Friday', 8, '', ''),
(6, 'OE-II / Departmental Training Session', '', 'CSAMCR05', 'Saturday', 1, '', ''),
(6, 'NLP', 'SG', 'CSAMCR05', 'Saturday', 2, '', ''),
(6, 'SEPM', 'SSS', 'CSAMCR05', 'Saturday', 4, '', ''),
(6, 'DL', 'SGC', 'CSAMCR05', 'Saturday', 7, '', '');
