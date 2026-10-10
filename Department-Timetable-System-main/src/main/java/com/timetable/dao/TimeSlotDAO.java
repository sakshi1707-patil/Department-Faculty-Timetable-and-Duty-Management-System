package com.timetable.dao;

import com.timetable.model.TimeSlot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TimeSlotDAO {
    public List<TimeSlot> getAllSlots() {
        List<TimeSlot> slots = new ArrayList<>();
        String sql = "SELECT slot_id, slot_label, sort_order, is_break, break_name FROM time_slot ORDER BY sort_order";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                TimeSlot slot = new TimeSlot();
                slot.setSlotId(resultSet.getInt("slot_id"));
                slot.setSlotLabel(resultSet.getString("slot_label"));
                slot.setSortOrder(resultSet.getInt("sort_order"));
                slot.setBreakSlot(resultSet.getBoolean("is_break"));
                slot.setBreakName(resultSet.getString("break_name"));
                slots.add(slot);
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch time slots", exception);
        }

        return slots;
    }
}

