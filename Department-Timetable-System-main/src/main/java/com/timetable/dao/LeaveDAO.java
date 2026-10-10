package com.timetable.dao;

import com.timetable.model.Leave;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LeaveDAO {
    public void applyLeave(int facultyId, String date) {
        String sql = "INSERT INTO leave_table(faculty_id, date, status) VALUES (?, ?, 'Pending')";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, facultyId);
            statement.setString(2, date);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to apply leave", exception);
        }
    }

    public void updateStatus(int leaveId, String status) {
        String sql = "UPDATE leave_table SET status = ? WHERE leave_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, leaveId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to update leave status", exception);
        }
    }

    public Leave getLeaveById(int leaveId) {
        String sql = "SELECT l.leave_id, l.faculty_id, l.date, l.status, f.name AS faculty_name " +
                "FROM leave_table l JOIN faculty f ON l.faculty_id = f.faculty_id WHERE l.leave_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, leaveId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Leave leave = new Leave();
                    leave.setLeaveId(resultSet.getInt("leave_id"));
                    leave.setFacultyId(resultSet.getInt("faculty_id"));
                    leave.setFacultyName(resultSet.getString("faculty_name"));
                    leave.setDate(resultSet.getString("date"));
                    leave.setStatus(resultSet.getString("status"));
                    return leave;
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch leave by id", exception);
        }

        return null;
    }

    public Set<String> getApprovedLeaveKeys() {
        Set<String> leaveKeys = new HashSet<>();
        String sql = "SELECT faculty_id, date FROM leave_table WHERE status = 'Approved'";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                leaveKeys.add(resultSet.getInt("faculty_id") + "#" + resultSet.getDate("date"));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch approved leaves", exception);
        }

        return leaveKeys;
    }

    public List<Leave> getAllLeaves() {
        return getLeaves(null);
    }

    public List<Leave> getApprovedLeaves() {
        List<Leave> approvedLeaves = new ArrayList<>();
        for (Leave leave : getAllLeaves()) {
            if ("Approved".equalsIgnoreCase(leave.getStatus())) {
                approvedLeaves.add(leave);
            }
        }
        return approvedLeaves;
    }

    public List<Leave> getLeavesByFaculty(int facultyId) {
        return getLeaves(facultyId);
    }

    private List<Leave> getLeaves(Integer facultyId) {
        List<Leave> leaveList = new ArrayList<>();
        String sql = "SELECT l.leave_id, l.faculty_id, l.date, l.status, f.name AS faculty_name " +
                "FROM leave_table l JOIN faculty f ON l.faculty_id = f.faculty_id " +
                (facultyId == null ? "" : "WHERE l.faculty_id = ? ") +
                "ORDER BY l.date DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (facultyId != null) {
                statement.setInt(1, facultyId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Leave leave = new Leave();
                    leave.setLeaveId(resultSet.getInt("leave_id"));
                    leave.setFacultyId(resultSet.getInt("faculty_id"));
                    leave.setFacultyName(resultSet.getString("faculty_name"));
                    leave.setDate(resultSet.getString("date"));
                    leave.setStatus(resultSet.getString("status"));
                    leaveList.add(leave);
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch leaves", exception);
        }

        return leaveList;
    }
}
