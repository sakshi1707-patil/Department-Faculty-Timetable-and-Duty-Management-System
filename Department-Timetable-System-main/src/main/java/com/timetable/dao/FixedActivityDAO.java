package com.timetable.dao;

import com.timetable.model.FixedActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FixedActivityDAO {
    public List<FixedActivity> getAllActivities() {
        List<FixedActivity> activities = new ArrayList<>();
        String sql = "SELECT fa.fixed_activity_id, fa.division_id, d.division_code, fa.activity_title, fa.faculty_text, " +
                "fa.room_text, fa.activity_day, fa.slot_id, fa.batch_label, fa.notes " +
                "FROM fixed_activity fa JOIN division d ON fa.division_id = d.division_id " +
                "ORDER BY d.division_code, fa.activity_day, fa.slot_id";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                FixedActivity activity = new FixedActivity();
                activity.setFixedActivityId(resultSet.getInt("fixed_activity_id"));
                activity.setDivisionId(resultSet.getInt("division_id"));
                activity.setDivisionCode(resultSet.getString("division_code"));
                activity.setActivityTitle(resultSet.getString("activity_title"));
                activity.setFacultyText(resultSet.getString("faculty_text"));
                activity.setRoomText(resultSet.getString("room_text"));
                activity.setActivityDay(resultSet.getString("activity_day"));
                activity.setSlotId(resultSet.getInt("slot_id"));
                activity.setBatchLabel(resultSet.getString("batch_label"));
                activity.setNotes(resultSet.getString("notes"));
                activities.add(activity);
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch fixed activities", exception);
        }

        return activities;
    }
}

