package com.timetable.dao;

import com.timetable.model.Duty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DutyDAO {
    public void assignDuty(Duty duty) {
        String sql = "INSERT INTO duty(faculty_id, duty_type, date) VALUES (?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, duty.getFacultyId());
            statement.setString(2, duty.getDutyType());
            statement.setString(3, duty.getDate());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to assign duty", exception);
        }
    }

    public List<Duty> getAllDuties() {
        return getDuties(null);
    }

    public List<Duty> getByFacultyId(int facultyId) {
        return getDuties(facultyId);
    }

    private List<Duty> getDuties(Integer facultyId) {
        List<Duty> dutyList = new ArrayList<>();
        String sql = "SELECT d.duty_id, d.faculty_id, d.duty_type, d.date, f.name AS faculty_name " +
                "FROM duty d JOIN faculty f ON d.faculty_id = f.faculty_id " +
                (facultyId == null ? "" : "WHERE d.faculty_id = ? ") +
                "ORDER BY d.date DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (facultyId != null) {
                statement.setInt(1, facultyId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Duty duty = new Duty();
                    duty.setDutyId(resultSet.getInt("duty_id"));
                    duty.setFacultyId(resultSet.getInt("faculty_id"));
                    duty.setFacultyName(resultSet.getString("faculty_name"));
                    duty.setDutyType(resultSet.getString("duty_type"));
                    duty.setDate(resultSet.getString("date"));
                    dutyList.add(duty);
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch duties", exception);
        }

        return dutyList;
    }
}

