package com.timetable.dao;

import com.timetable.model.Timetable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TimetableDAO {
    public void addEntry(Timetable timetable) {
        if (hasCollision(timetable)) {
            throw new RuntimeException("Timetable collision detected for division, faculty, or room.");
        }
        String sql = "INSERT INTO timetable(division_id, faculty_id, subject_id, room_id, schedule_date, day_name, slot_id, batch_label, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, timetable.getDivisionId());
            statement.setInt(2, timetable.getFacultyId());
            statement.setInt(3, timetable.getSubjectId());
            statement.setInt(4, timetable.getRoomId());
            statement.setString(5, timetable.getScheduleDate());
            statement.setString(6, timetable.getDayName());
            statement.setInt(7, timetable.getSlotId());
            statement.setString(8, timetable.getBatchLabel());
            statement.setString(9, timetable.getNotes());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to add timetable entry", exception);
        }
    }

    public void deleteEntriesForWeek(LocalDate weekStartDate) {
        LocalDate normalizedWeekStart = com.timetable.service.AutoGenerateTimetable.normalizeAcademicWeekStart(weekStartDate);
        String sql = "DELETE FROM timetable WHERE schedule_date BETWEEN ? AND ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalizedWeekStart.toString());
            statement.setString(2, normalizedWeekStart.plusDays(4).toString());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to clear timetable week", exception);
        }
    }

    public List<Timetable> getAllEntries() {
        return getEntries(null, null);
    }

    public List<Timetable> getByFacultyId(int facultyId) {
        return getEntries("t.faculty_id = ?", statement -> statement.setInt(1, facultyId));
    }

    public List<Timetable> getByFacultyAndWeek(int facultyId, LocalDate weekStartDate) {
        LocalDate normalizedWeekStart = com.timetable.service.AutoGenerateTimetable.normalizeAcademicWeekStart(weekStartDate);
        return getEntries("t.faculty_id = ? AND t.schedule_date BETWEEN ? AND ?", statement -> {
            statement.setInt(1, facultyId);
            statement.setString(2, normalizedWeekStart.toString());
            statement.setString(3, normalizedWeekStart.plusDays(4).toString());
        });
    }

    public List<Timetable> getByFacultyAndDate(int facultyId, String scheduleDate) {
        return getEntries("t.faculty_id = ? AND t.schedule_date = ?", statement -> {
            statement.setInt(1, facultyId);
            statement.setString(2, scheduleDate);
        });
    }

    public List<Timetable> getByDivisionAndWeek(Integer divisionId, LocalDate weekStartDate) {
        LocalDate normalizedWeekStart = com.timetable.service.AutoGenerateTimetable.normalizeAcademicWeekStart(weekStartDate);
        StringBuilder where = new StringBuilder("t.schedule_date BETWEEN ? AND ?");
        if (divisionId != null && divisionId > 0) {
            where.append(" AND t.division_id = ?");
        }

        return getEntries(where.toString(), statement -> {
            statement.setString(1, normalizedWeekStart.toString());
            statement.setString(2, normalizedWeekStart.plusDays(4).toString());
            if (divisionId != null && divisionId > 0) {
                statement.setInt(3, divisionId);
            }
        });
    }

    public List<Timetable> getEntriesRequiringAttention() {
        return getEntries("(t.notes LIKE 'Replacement required%' OR t.notes LIKE 'Replacement:%')", null);
    }

    private List<Timetable> getEntries(String whereClause, StatementConfigurer configurer) {
        List<Timetable> timetableList = new ArrayList<>();
        String sql = "SELECT t.timetable_id, t.division_id, t.faculty_id, t.subject_id, t.room_id, t.schedule_date, t.day_name, " +
                "t.slot_id, t.batch_label, t.notes, d.division_code, d.coordinator_name, f.name AS faculty_name, " +
                "s.subject_name, r.room_number, ts.slot_label " +
                "FROM timetable t " +
                "JOIN division d ON t.division_id = d.division_id " +
                "JOIN faculty f ON t.faculty_id = f.faculty_id " +
                "JOIN subject s ON t.subject_id = s.subject_id " +
                "JOIN room r ON t.room_id = r.room_id " +
                "JOIN time_slot ts ON t.slot_id = ts.slot_id " +
                (whereClause == null ? "" : "WHERE " + whereClause + " ") +
                "ORDER BY t.schedule_date, ts.sort_order, d.division_code";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (configurer != null) {
                configurer.configure(statement);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    timetableList.add(mapTimetable(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch timetable", exception);
        }

        return timetableList;
    }

    private Timetable mapTimetable(ResultSet resultSet) throws SQLException {
        Timetable timetable = new Timetable();
        timetable.setTimetableId(resultSet.getInt("timetable_id"));
        timetable.setDivisionId(resultSet.getInt("division_id"));
        timetable.setFacultyId(resultSet.getInt("faculty_id"));
        timetable.setSubjectId(resultSet.getInt("subject_id"));
        timetable.setRoomId(resultSet.getInt("room_id"));
        timetable.setScheduleDate(resultSet.getString("schedule_date"));
        timetable.setDayName(resultSet.getString("day_name"));
        timetable.setSlotId(resultSet.getInt("slot_id"));
        timetable.setBatchLabel(resultSet.getString("batch_label"));
        timetable.setNotes(resultSet.getString("notes"));
        timetable.setDivisionCode(resultSet.getString("division_code"));
        timetable.setCoordinatorName(resultSet.getString("coordinator_name"));
        timetable.setFacultyName(resultSet.getString("faculty_name"));
        timetable.setSubjectName(resultSet.getString("subject_name"));
        timetable.setRoomNumber(resultSet.getString("room_number"));
        timetable.setTimeSlot(resultSet.getString("slot_label"));
        return timetable;
    }

    public void updateFacultyAssignment(int timetableId, int newFacultyId, String notes) {
        String sql = "UPDATE timetable SET faculty_id = ?, notes = ? WHERE timetable_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, newFacultyId);
            statement.setString(2, notes);
            statement.setInt(3, timetableId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to update timetable faculty assignment", exception);
        }
    }

    public void updateNotes(int timetableId, String notes) {
        String sql = "UPDATE timetable SET notes = ? WHERE timetable_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, notes);
            statement.setInt(2, timetableId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to update timetable notes", exception);
        }
    }

    private interface StatementConfigurer {
        void configure(PreparedStatement statement) throws SQLException;
    }

    private boolean hasCollision(Timetable timetable) {
        String sql = "SELECT COUNT(*) AS cnt FROM timetable " +
                "WHERE schedule_date = ? AND slot_id = ? AND (" +
                "(division_id = ? AND batch_label = ?) OR faculty_id = ? OR (room_id = ? AND batch_label = ?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, timetable.getScheduleDate());
            statement.setInt(2, timetable.getSlotId());
            statement.setInt(3, timetable.getDivisionId());
            statement.setString(4, timetable.getBatchLabel());
            statement.setInt(5, timetable.getFacultyId());
            statement.setInt(6, timetable.getRoomId());
            statement.setString(7, timetable.getBatchLabel());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("cnt") > 0;
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to validate timetable collision", exception);
        }
        return false;
    }
}
