package com.timetable.dao;

import com.timetable.model.Subject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SubjectDAO {
    public void addSubject(Subject subject) {
        String sql = "INSERT INTO subject(subject_name, hours_per_week, faculty_id, division_id, activity_type, preferred_room_type, batch_label) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, subject.getSubjectName());
            statement.setInt(2, subject.getHoursPerWeek());
            statement.setInt(3, subject.getFacultyId());
            statement.setInt(4, subject.getDivisionId());
            statement.setString(5, subject.getActivityType());
            statement.setString(6, subject.getPreferredRoomType());
            statement.setString(7, subject.getBatchLabel());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to add subject", exception);
        }
    }

    public List<Subject> getAllSubjects() {
        List<Subject> subjectList = new ArrayList<>();
        String sql = "SELECT s.subject_id, s.subject_name, s.hours_per_week, s.faculty_id, s.division_id, " +
                "s.activity_type, s.preferred_room_type, s.batch_label, f.name AS faculty_name, d.division_code " +
                "FROM subject s " +
                "JOIN faculty f ON s.faculty_id = f.faculty_id " +
                "JOIN division d ON s.division_id = d.division_id " +
                "ORDER BY d.division_code, s.subject_name";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                subjectList.add(mapSubject(resultSet));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch subjects", exception);
        }

        return subjectList;
    }

    public List<Subject> getSubjectsByDivision(int divisionId) {
        List<Subject> subjectList = new ArrayList<>();
        String sql = "SELECT s.subject_id, s.subject_name, s.hours_per_week, s.faculty_id, s.division_id, " +
                "s.activity_type, s.preferred_room_type, s.batch_label, f.name AS faculty_name, d.division_code " +
                "FROM subject s " +
                "JOIN faculty f ON s.faculty_id = f.faculty_id " +
                "JOIN division d ON s.division_id = d.division_id " +
                "WHERE s.division_id = ? ORDER BY s.subject_name";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, divisionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    subjectList.add(mapSubject(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch subjects by division", exception);
        }

        return subjectList;
    }

    private Subject mapSubject(ResultSet resultSet) throws SQLException {
        Subject subject = new Subject();
        subject.setSubjectId(resultSet.getInt("subject_id"));
        subject.setSubjectName(resultSet.getString("subject_name"));
        subject.setHoursPerWeek(resultSet.getInt("hours_per_week"));
        subject.setFacultyId(resultSet.getInt("faculty_id"));
        subject.setDivisionId(resultSet.getInt("division_id"));
        subject.setActivityType(resultSet.getString("activity_type"));
        subject.setPreferredRoomType(resultSet.getString("preferred_room_type"));
        subject.setBatchLabel(resultSet.getString("batch_label"));
        subject.setFacultyName(resultSet.getString("faculty_name"));
        subject.setDivisionCode(resultSet.getString("division_code"));
        return subject;
    }
}

