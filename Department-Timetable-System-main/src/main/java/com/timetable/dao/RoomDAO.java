package com.timetable.dao;

import com.timetable.model.Room;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {
    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT room_id, room_number, room_type FROM room ORDER BY room_number";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Room room = new Room();
                room.setRoomId(resultSet.getInt("room_id"));
                room.setRoomNumber(resultSet.getString("room_number"));
                room.setRoomType(resultSet.getString("room_type"));
                rooms.add(room);
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch rooms", exception);
        }

        return rooms;
    }
}

