package com.timetable.dao;

import com.timetable.model.Division;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DivisionDAO {
    public List<Division> getAllDivisions() {
        List<Division> divisions = new ArrayList<>();
        String sql = "SELECT division_id, division_code, year_name, section_name, classroom, coordinator_name " +
                "FROM division ORDER BY division_code";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Division division = new Division();
                division.setDivisionId(resultSet.getInt("division_id"));
                division.setDivisionCode(resultSet.getString("division_code"));
                division.setYearName(resultSet.getString("year_name"));
                division.setSectionName(resultSet.getString("section_name"));
                division.setClassroom(resultSet.getString("classroom"));
                division.setCoordinatorName(resultSet.getString("coordinator_name"));
                divisions.add(division);
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch divisions", exception);
        }

        return divisions;
    }
}

