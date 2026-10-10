USE college_timetable_system;

ALTER TABLE users
    ADD COLUMN division_id INT NULL AFTER faculty_id,
    ADD CONSTRAINT fk_users_division
        FOREIGN KEY (division_id) REFERENCES division(division_id)
        ON DELETE SET NULL;

UPDATE users
SET division_id = 1
WHERE role = 'STUDENT'
  AND username = 'student1';
