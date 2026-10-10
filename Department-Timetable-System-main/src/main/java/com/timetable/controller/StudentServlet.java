package com.timetable.controller;

import com.timetable.model.Division;
import com.timetable.model.FixedActivity;
import com.timetable.model.TimeSlot;
import com.timetable.model.Timetable;
import com.timetable.model.User;
import com.timetable.service.AutoGenerateTimetable;
import com.timetable.service.DivisionService;
import com.timetable.service.FixedActivityService;
import com.timetable.service.TimeSlotService;
import com.timetable.service.TimetableService;
import com.timetable.util.Constants;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet("/student")
public class StudentServlet extends BaseServlet {
    private final TimetableService timetableService = new TimetableService();
    private final DivisionService divisionService = new DivisionService();
    private final TimeSlotService timeSlotService = new TimeSlotService();
    private final FixedActivityService fixedActivityService = new FixedActivityService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = getLoggedInUser(request);
        if (user == null || !Constants.ROLE_STUDENT.equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        LocalDate weekStartDate = AutoGenerateTimetable.normalizeAcademicWeekStart(parseWeekStart(request.getParameter("weekStartDate")));
        List<Division> divisions = divisionService.getAllDivisions();
        Integer divisionId = parseDivision(request.getParameter("divisionId"));
        if (divisionId == null) {
            divisionId = user.getDivisionId();
        }
        if (divisionId == null && !divisions.isEmpty()) {
            divisionId = divisions.get(0).getDivisionId();
        }

        Division studentDivision = findDivision(divisions, divisionId);
        List<TimeSlot> slotList = timeSlotService.getAllSlots();
        List<LocalDate> instructionalDates = AutoGenerateTimetable.getInstructionalDates(weekStartDate);
        List<Timetable> timetableList = timetableService.getByDivisionAndWeek(divisionId, weekStartDate);

        request.setAttribute("divisionList", divisions);
        request.setAttribute("division", studentDivision);
        request.setAttribute("selectedDivisionId", divisionId);
        request.setAttribute("weekStartDate", weekStartDate);
        request.setAttribute("slotList", slotList);
        request.setAttribute("dateKeys", toDateKeys(instructionalDates));
        request.setAttribute("dayLabels", toDayLabels(instructionalDates));
        request.setAttribute("timetableGrid", buildGrid(
                timetableList,
                fixedActivityService.getAllActivities(),
                toDateByDay(instructionalDates),
                divisionId
        ));
        request.getRequestDispatcher("/jsp/student/studentDashboard.jsp").forward(request, response);
    }

    private LocalDate parseWeekStart(String value) {
        return value == null || value.isBlank() ? LocalDate.now() : LocalDate.parse(value);
    }

    private Integer parseDivision(String value) {
        return value == null || value.isBlank() ? null : Integer.parseInt(value);
    }

    private Division findDivision(List<Division> divisions, Integer divisionId) {
        if (divisionId == null) {
            return null;
        }
        for (Division division : divisions) {
            if (division.getDivisionId() == divisionId) {
                return division;
            }
        }
        return null;
    }

    private List<String> toDateKeys(List<LocalDate> instructionalDates) {
        List<String> dateKeys = new ArrayList<>();
        for (LocalDate instructionalDate : instructionalDates) {
            dateKeys.add(instructionalDate.toString());
        }
        return dateKeys;
    }

    private Map<String, String> toDayLabels(List<LocalDate> instructionalDates) {
        Map<String, String> dayLabels = new LinkedHashMap<>();
        for (LocalDate instructionalDate : instructionalDates) {
            dayLabels.put(
                    instructionalDate.toString(),
                    instructionalDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            );
        }
        return dayLabels;
    }

    private Map<String, String> toDateByDay(List<LocalDate> instructionalDates) {
        Map<String, String> dateByDay = new HashMap<>();
        for (LocalDate instructionalDate : instructionalDates) {
            dateByDay.put(
                    instructionalDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toLowerCase(Locale.ENGLISH),
                    instructionalDate.toString()
            );
        }
        return dateByDay;
    }

    private Map<Integer, Map<String, List<Timetable>>> buildGrid(List<Timetable> timetableList,
                                                                 List<FixedActivity> fixedActivities,
                                                                 Map<String, String> dateByDay,
                                                                 Integer divisionId) {
        Map<Integer, Map<String, List<Timetable>>> grid = new LinkedHashMap<>();

        for (FixedActivity activity : fixedActivities) {
            if (divisionId == null || activity.getDivisionId() != divisionId) {
                continue;
            }

            String scheduleDate = dateByDay.get(activity.getActivityDay().toLowerCase(Locale.ENGLISH));
            if (scheduleDate == null) {
                continue;
            }

            addCellEntry(grid, activity.getSlotId(), scheduleDate, toSyntheticEntry(activity, scheduleDate));
        }

        for (Timetable entry : timetableList) {
            mergeActualEntry(grid, entry);
        }
        return grid;
    }

    private Timetable toSyntheticEntry(FixedActivity activity, String scheduleDate) {
        Timetable timetable = new Timetable();
        timetable.setDivisionId(activity.getDivisionId());
        timetable.setDivisionCode(activity.getDivisionCode());
        timetable.setScheduleDate(scheduleDate);
        timetable.setDayName(activity.getActivityDay());
        timetable.setSlotId(activity.getSlotId());
        timetable.setSubjectName(activity.getActivityTitle());
        timetable.setFacultyName(activity.getFacultyText() == null ? "" : activity.getFacultyText());
        timetable.setRoomNumber(activity.getRoomText() == null ? "" : activity.getRoomText());
        timetable.setBatchLabel(activity.getBatchLabel());
        timetable.setNotes(activity.getNotes());
        return timetable;
    }

    private void addCellEntry(Map<Integer, Map<String, List<Timetable>>> grid,
                              int slotId,
                              String scheduleDate,
                              Timetable entry) {
        grid.computeIfAbsent(slotId, key -> new LinkedHashMap<>())
                .computeIfAbsent(scheduleDate, key -> new ArrayList<>())
                .add(entry);
    }

    private void mergeActualEntry(Map<Integer, Map<String, List<Timetable>>> grid, Timetable actualEntry) {
        List<Timetable> cellEntries = grid
                .computeIfAbsent(actualEntry.getSlotId(), key -> new LinkedHashMap<>())
                .computeIfAbsent(actualEntry.getScheduleDate(), key -> new ArrayList<>());

        Timetable matchingEntry = null;
        for (Timetable existingEntry : cellEntries) {
            if (sameActivity(existingEntry, actualEntry)) {
                matchingEntry = existingEntry;
                break;
            }
        }

        if (matchingEntry == null) {
            cellEntries.add(actualEntry);
        } else {
            matchingEntry.setFacultyName(actualEntry.getFacultyName());
            matchingEntry.setRoomNumber(actualEntry.getRoomNumber());
            matchingEntry.setBatchLabel(actualEntry.getBatchLabel());
            matchingEntry.setNotes(actualEntry.getNotes());
            matchingEntry.setSubjectName(actualEntry.getSubjectName());
        }

        cellEntries.sort(
                Comparator.comparing((Timetable cellEntry) -> normalize(cellEntry.getBatchLabel()))
                        .thenComparing(cellEntry -> normalize(cellEntry.getSubjectName()))
        );
    }

    private boolean sameActivity(Timetable left, Timetable right) {
        return normalize(left.getSubjectName()).equals(normalize(right.getSubjectName()))
                && normalize(left.getBatchLabel()).equals(normalize(right.getBatchLabel()));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ENGLISH);
    }
}
