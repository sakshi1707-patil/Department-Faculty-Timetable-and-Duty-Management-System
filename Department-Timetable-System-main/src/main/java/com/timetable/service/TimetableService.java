package com.timetable.service;

import com.timetable.dao.LeaveDAO;
import com.timetable.dao.TimetableDAO;
import com.timetable.model.Leave;
import com.timetable.model.Timetable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TimetableService {
    private final TimetableDAO timetableDAO = new TimetableDAO();
    private final LeaveDAO leaveDAO = new LeaveDAO();

    public void addEntry(int divisionId, int facultyId, int subjectId, int roomId,
                         String scheduleDate, String dayName, int slotId,
                         String batchLabel, String notes) {
        Timetable timetable = new Timetable();
        timetable.setDivisionId(divisionId);
        timetable.setFacultyId(facultyId);
        timetable.setSubjectId(subjectId);
        timetable.setRoomId(roomId);
        timetable.setScheduleDate(scheduleDate);
        timetable.setDayName(dayName);
        timetable.setSlotId(slotId);
        timetable.setBatchLabel(batchLabel);
        timetable.setNotes(notes);
        timetableDAO.addEntry(timetable);
    }

    public List<Timetable> getAllEntries() {
        return timetableDAO.getAllEntries();
    }

    public List<Timetable> getByFacultyId(int facultyId) {
        return timetableDAO.getByFacultyId(facultyId);
    }

    public List<Timetable> getByFacultyAndWeek(int facultyId, LocalDate weekStartDate) {
        return timetableDAO.getByFacultyAndWeek(facultyId, weekStartDate);
    }

    public List<Timetable> getByDivisionAndWeek(Integer divisionId, LocalDate weekStartDate) {
        return timetableDAO.getByDivisionAndWeek(divisionId, weekStartDate);
    }

    public List<Timetable> getEntriesRequiringAttention() {
        List<Timetable> attentionEntries = new ArrayList<>(timetableDAO.getEntriesRequiringAttention());
        for (Leave leave : leaveDAO.getApprovedLeaves()) {
            if (hasAlertForLeave(attentionEntries, leave)) {
                continue;
            }
            Timetable alert = new Timetable();
            alert.setDivisionCode("All assigned classes");
            alert.setScheduleDate(leave.getDate());
            alert.setTimeSlot("-");
            alert.setSubjectName("Approved Leave");
            alert.setFacultyName(leave.getFacultyName());
            alert.setNotes("Replacement required: " + leave.getFacultyName() + " on leave");
            attentionEntries.add(alert);
        }
        return attentionEntries;
    }

    private boolean hasAlertForLeave(List<Timetable> attentionEntries, Leave leave) {
        for (Timetable entry : attentionEntries) {
            if (leave.getDate().equals(entry.getScheduleDate())
                    && entry.getNotes() != null
                    && entry.getNotes().contains(leave.getFacultyName())) {
                return true;
            }
        }
        return false;
    }
}
