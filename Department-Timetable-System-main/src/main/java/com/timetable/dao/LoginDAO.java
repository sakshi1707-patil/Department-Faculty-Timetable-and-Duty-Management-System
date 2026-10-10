package com.timetable.dao;

import com.timetable.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginDAO {
    public User findByCredentials(String username, String password) {
        String sql = "SELECT user_id, username, password, role, faculty_id FROM users WHERE username = ? AND password = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User();
                    user.setUserId(resultSet.getInt("user_id"));
                    user.setUsername(resultSet.getString("username"));
                    user.setPassword(resultSet.getString("password"));
                    user.setRole(resultSet.getString("role"));
                    int facultyId = resultSet.getInt("faculty_id");
                    user.setFacultyId(resultSet.wasNull() ? null : facultyId);
                    return user;
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to authenticate user", exception);
        }

        return null;
    }
}

