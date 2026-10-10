package com.timetable.controller;

import com.timetable.model.User;
import com.timetable.model.Faculty;
import com.timetable.model.FixedActivity;
import com.timetable.model.TimeSlot;
import com.timetable.model.Timetable;
import com.timetable.service.AutoGenerateTimetable;
import com.timetable.service.DutyService;
import com.timetable.service.FixedActivityService;
import com.timetable.service.FacultyService;
import com.timetable.service.LeaveService;
import com.timetable.service.TimeSlotService;
import com.timetable.service.TimetableService;
import com.timetable.util.Constants;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

@WebServlet("/faculty")
public class FacultyServlet extends BaseServlet {
    private final TimetableService timetableService = new TimetableService();
    private final DutyService dutyService = new DutyService();
    private final LeaveService leaveService = new LeaveService();
    private final FacultyService facultyService = new FacultyService();
    private final FixedActivityService fixedActivityService = new FixedActivityService();
    private final TimeSlotService timeSlotService = new TimeSlotService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = getLoggedInUser(request);
        if (user == null || !Constants.ROLE_FACULTY.equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int facultyId = user.getFacultyId() == null ? 0 : user.getFacultyId();
        LocalDate weekStartDate = AutoGenerateTimetable.normalizeAcademicWeekStart(parseWeekStart(request.getParameter("weekStartDate")));
        request.setAttribute("weekStartDate", weekStartDate);
        request.setAttribute("timetableList", buildFacultyWeekView(facultyId, weekStartDate));
        request.setAttribute("dutyList", dutyService.getByFacultyId(facultyId));
        request.setAttribute("leaveList", leaveService.getLeavesByFaculty(facultyId));
        request.getRequestDispatcher("/jsp/faculty/facultyDashboard.jsp").forward(request, response);
    }

    private LocalDate parseWeekStart(String value) {
        return value == null || value.isBlank() ? LocalDate.now() : LocalDate.parse(value);
    }

    private List<Timetable> compactFacultyEntries(List<Timetable> entries) {
        List<Timetable> compacted = new ArrayList<>();
        for (Timetable entry : entries) {
            if (!compacted.isEmpty() && canMerge(compacted.get(compacted.size() - 1), entry)) {
                Timetable previous = compacted.get(compacted.size() - 1);
                previous.setTimeSlot(mergeTimeRange(previous.getTimeSlot(), entry.getTimeSlot()));
                previous.setNotes("PRACTICAL (2 Hr Lab)");
                continue;
            }
            compacted.add(copyEntry(entry));
        }
        return compacted;
    }

    private boolean canMerge(Timetable previous, Timetable current) {
        return "PRACTICAL".equalsIgnoreCase(previous.getNotes())
                && "PRACTICAL".equalsIgnoreCase(current.getNotes())
                && previous.getDivisionId() == current.getDivisionId()
                && previous.getSubjectId() == current.getSubjectId()
                && previous.getRoomId() == current.getRoomId()
                && previous.getScheduleDate().equals(current.getScheduleDate())
                && safe(previous.getBatchLabel()).equals(safe(current.getBatchLabel()))
                && current.getSlotId() == previous.getSlotId() + 1;
    }

    private Timetable copyEntry(Timetable source) {
        Timetable copy = new Timetable();
        copy.setTimetableId(source.getTimetableId());
        copy.setDivisionId(source.getDivisionId());
        copy.setFacultyId(source.getFacultyId());
        copy.setSubjectId(source.getSubjectId());
        copy.setRoomId(source.getRoomId());
        copy.setSlotId(source.getSlotId());
        copy.setScheduleDate(source.getScheduleDate());
        copy.setDayName(source.getDayName());
        copy.setTimeSlot(source.getTimeSlot());
        copy.setBatchLabel(source.getBatchLabel());
        copy.setNotes(source.getNotes());
        copy.setDivisionCode(source.getDivisionCode());
        copy.setCoordinatorName(source.getCoordinatorName());
        copy.setFacultyName(source.getFacultyName());
        copy.setSubjectName(source.getSubjectName());
        copy.setRoomNumber(source.getRoomNumber());
        return copy;
    }

    private String mergeTimeRange(String firstSlot, String secondSlot) {
        if (firstSlot == null || secondSlot == null || !firstSlot.contains("-") || !secondSlot.contains("-")) {
            return firstSlot;
        }
        String start = firstSlot.substring(0, firstSlot.indexOf('-')).trim();
        String end = secondSlot.substring(secondSlot.indexOf('-') + 1).trim();
        return start + " - " + end;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private List<Timetable> buildFacultyWeekView(int facultyId, LocalDate weekStartDate) {
        List<Timetable> actualEntries = timetableService.getByFacultyAndWeek(facultyId, weekStartDate);
        Faculty faculty = facultyService.getFacultyById(facultyId);
        if (faculty == null) {
            return compactFacultyEntries(actualEntries);
        }

        List<LocalDate> instructionalDates = AutoGenerateTimetable.getInstructionalDates(weekStartDate);
        Map<String, String> dateByDay = new HashMap<>();
        for (LocalDate date : instructionalDates) {
            dateByDay.put(
                    date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH).toLowerCase(Locale.ENGLISH),
                    date.toString()
            );
        }
        Map<Integer, String> slotLabels = new HashMap<>();
        for (TimeSlot slot : timeSlotService.getAllSlots()) {
            slotLabels.put(slot.getSlotId(), slot.getSlotLabel());
        }

        Set<String> overriddenTemplateKeys = new HashSet<>();
        for (Timetable actualEntry : timetableService.getByDivisionAndWeek(null, weekStartDate)) {
            overriddenTemplateKeys.add(templateKey(actualEntry));
        }

        String facultyCode = toFacultyCode(faculty.getName());
        List<Timetable> mergedEntries = new ArrayList<>(actualEntries);
        for (FixedActivity activity : fixedActivityService.getAllActivities()) {
            if (!matchesFaculty(activity.getFacultyText(), facultyCode)) {
                continue;
            }

            String scheduleDate = dateByDay.get(activity.getActivityDay().toLowerCase(Locale.ENGLISH));
            if (scheduleDate == null) {
                continue;
            }

            Timetable syntheticEntry = toSyntheticEntry(activity, scheduleDate, slotLabels.get(activity.getSlotId()));
            if (overriddenTemplateKeys.contains(templateKey(syntheticEntry))) {
                continue;
            }
            mergedEntries.add(syntheticEntry);
        }

        mergedEntries.sort((left, right) -> {
            int dateCompare = safe(left.getScheduleDate()).compareTo(safe(right.getScheduleDate()));
            if (dateCompare != 0) {
                return dateCompare;
            }
            return Integer.compare(left.getSlotId(), right.getSlotId());
        });
        return compactFacultyEntries(mergedEntries);
    }

    private Timetable toSyntheticEntry(FixedActivity activity, String scheduleDate, String slotLabel) {
        Timetable timetable = new Timetable();
        timetable.setDivisionId(activity.getDivisionId());
        timetable.setDivisionCode(activity.getDivisionCode());
        timetable.setScheduleDate(scheduleDate);
        timetable.setDayName(activity.getActivityDay());
        timetable.setSlotId(activity.getSlotId());
        timetable.setTimeSlot(slotLabel == null ? "" : slotLabel);
        timetable.setSubjectName(activity.getActivityTitle());
        timetable.setFacultyName(activity.getFacultyText() == null ? "" : activity.getFacultyText());
        timetable.setRoomNumber(activity.getRoomText() == null ? "" : activity.getRoomText());
        timetable.setBatchLabel(activity.getBatchLabel());
        timetable.setNotes(activity.getNotes() == null || activity.getNotes().isBlank() ? "TEMPLATE" : activity.getNotes());
        return timetable;
    }

    private boolean matchesFaculty(String facultyText, String facultyCode) {
        if (facultyText == null || facultyText.isBlank() || facultyCode.isBlank()) {
            return false;
        }

        String[] parts = facultyText.toUpperCase(Locale.ENGLISH).split("[/,]");
        for (String part : parts) {
            if (part.trim().equals(facultyCode)) {
                return true;
            }
        }
        return false;
    }

    private String toFacultyCode(String facultyName) {
        if (facultyName == null || facultyName.isBlank()) {
            return "";
        }

        String cleaned = facultyName.replace("Dr.", "")
                .replace("Prof.", "")
                .replace("Mrs.", "")
                .replace("Mr.", "")
                .replace("Ms.", "")
                .trim();
        String[] parts = cleaned.split("\\s+");
        StringBuilder code = new StringBuilder();
        for (String part : parts) {
            String lettersOnly = part.replaceAll("[^A-Za-z]", "");
            if (!lettersOnly.isEmpty()) {
                code.append(Character.toUpperCase(lettersOnly.charAt(0)));
            }
        }
        return code.toString();
    }

    private String templateKey(Timetable entry) {
        return entry.getDivisionId()
                + "#" + safe(entry.getScheduleDate())
                + "#" + entry.getSlotId()
                + "#" + safe(entry.getSubjectName()).toLowerCase(Locale.ENGLISH)
                + "#" + safe(entry.getBatchLabel()).toLowerCase(Locale.ENGLISH);
    }
}
