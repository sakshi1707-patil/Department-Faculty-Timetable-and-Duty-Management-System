package com.timetable.service;

import com.timetable.dao.LeaveDAO;
import com.timetable.dao.FacultyDAO;
import com.timetable.dao.TimetableDAO;
import com.timetable.model.Faculty;
import com.timetable.model.Leave;
import com.timetable.model.Timetable;

import java.util.List;

public class LeaveService {
    private final LeaveDAO leaveDAO = new LeaveDAO();
    private final FacultyDAO facultyDAO = new FacultyDAO();
    private final TimetableDAO timetableDAO = new TimetableDAO();

    public void applyLeave(int facultyId, String date) {
        leaveDAO.applyLeave(facultyId, date);
    }

    public String updateLeaveStatus(int leaveId, String status) {
        leaveDAO.updateStatus(leaveId, status);
        Leave leave = leaveDAO.getLeaveById(leaveId);
        if (leave == null) {
            return "Leave updated.";
        }

        if (!"Approved".equalsIgnoreCase(status)) {
            return "Leave status updated to " + status + ".";
        }

        List<Timetable> impactedEntries = timetableDAO.getByFacultyAndDate(leave.getFacultyId(), leave.getDate());
        if (impactedEntries.isEmpty()) {
            return "Leave approved. No timetable entries were scheduled on that date.";
        }

        int reassigned = 0;
        int flagged = 0;
        for (Timetable entry : impactedEntries) {
            Faculty replacement = facultyDAO.findAvailableReplacement(
                    leave.getFacultyId(),
                    entry.getScheduleDate(),
                    entry.getSlotId()
            );

            if (replacement != null) {
                String note = "Replacement: " + replacement.getName() + " for " + leave.getFacultyName();
                timetableDAO.updateFacultyAssignment(entry.getTimetableId(), replacement.getFacultyId(), note);
                reassigned++;
            } else {
                timetableDAO.updateNotes(entry.getTimetableId(), "Replacement required: " + leave.getFacultyName() + " on leave");
                flagged++;
            }
        }

        return "Leave approved. Reassigned " + reassigned + " slot(s)" +
                (flagged > 0 ? ", flagged " + flagged + " slot(s) for admin follow-up." : ".");
    }

    public List<Leave> getAllLeaves() {
        return leaveDAO.getAllLeaves();
    }

    public List<Leave> getLeavesByFaculty(int facultyId) {
        return leaveDAO.getLeavesByFaculty(facultyId);
    }
}
