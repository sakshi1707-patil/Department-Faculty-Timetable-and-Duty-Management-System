package com.timetable.dao;

import com.timetable.model.Faculty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FacultyDAO {
    public void addFaculty(Faculty faculty) {
        String sql = "INSERT INTO faculty(name, department, email) VALUES (?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, faculty.getName());
            statement.setString(2, faculty.getDepartment());
            statement.setString(3, faculty.getEmail());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to add faculty", exception);
        }
    }

    public List<Faculty> getAllFaculty() {
        List<Faculty> facultyList = new ArrayList<>();
        String sql = "SELECT faculty_id, name, department, email FROM faculty ORDER BY faculty_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Faculty faculty = new Faculty();
                faculty.setFacultyId(resultSet.getInt("faculty_id"));
                faculty.setName(resultSet.getString("name"));
                faculty.setDepartment(resultSet.getString("department"));
                faculty.setEmail(resultSet.getString("email"));
                facultyList.add(faculty);
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch faculty", exception);
        }

        return facultyList;
    }

    public Faculty getFacultyById(int facultyId) {
        String sql = "SELECT faculty_id, name, department, email FROM faculty WHERE faculty_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, facultyId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Faculty faculty = new Faculty();
                    faculty.setFacultyId(resultSet.getInt("faculty_id"));
                    faculty.setName(resultSet.getString("name"));
                    faculty.setDepartment(resultSet.getString("department"));
                    faculty.setEmail(resultSet.getString("email"));
                    return faculty;
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch faculty by id", exception);
        }

        return null;
    }

    public Faculty findAvailableReplacement(int originalFacultyId, String scheduleDate, int slotId) {
        Faculty originalFaculty = getFacultyById(originalFacultyId);
        if (originalFaculty == null) {
            return null;
        }

        String sql = "SELECT f.faculty_id, f.name, f.department, f.email " +
                "FROM faculty f " +
                "WHERE f.department = ? AND f.faculty_id <> ? " +
                "AND NOT EXISTS (SELECT 1 FROM timetable t WHERE t.faculty_id = f.faculty_id AND t.schedule_date = ? AND t.slot_id = ?) " +
                "AND NOT EXISTS (SELECT 1 FROM leave_table l WHERE l.faculty_id = f.faculty_id AND l.date = ? AND l.status = 'Approved') " +
                "ORDER BY f.faculty_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, originalFaculty.getDepartment());
            statement.setInt(2, originalFacultyId);
            statement.setString(3, scheduleDate);
            statement.setInt(4, slotId);
            statement.setString(5, scheduleDate);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Faculty faculty = new Faculty();
                    faculty.setFacultyId(resultSet.getInt("faculty_id"));
                    faculty.setName(resultSet.getString("name"));
                    faculty.setDepartment(resultSet.getString("department"));
                    faculty.setEmail(resultSet.getString("email"));
                    return faculty;
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to find replacement faculty", exception);
        }

        return null;
    }
}
