package com.timetable.controller;

import com.timetable.model.User;
import com.timetable.model.FixedActivity;
import com.timetable.model.Timetable;
import com.timetable.service.DivisionService;
import com.timetable.service.FacultyService;
import com.timetable.service.TimeSlotService;
import com.timetable.service.AutoGenerateTimetable;
import com.timetable.service.FixedActivityService;
import com.timetable.service.TimetableService;
import com.timetable.util.Constants;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet("/timetable")
public class TimetableServlet extends BaseServlet {
    private final TimetableService timetableService = new TimetableService();
    private final FacultyService facultyService = new FacultyService();
    private final DivisionService divisionService = new DivisionService();
    private final TimeSlotService timeSlotService = new TimeSlotService();
    private final FixedActivityService fixedActivityService = new FixedActivityService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = getLoggedInUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        LocalDate weekStartDate = AutoGenerateTimetable.normalizeAcademicWeekStart(parseWeekStart(request.getParameter("weekStartDate")));
        request.setAttribute("weekStartDate", weekStartDate);
        request.setAttribute("divisionList", divisionService.getAllDivisions());
        request.setAttribute("slotList", timeSlotService.getAllSlots());
        List<LocalDate> instructionalDates = AutoGenerateTimetable.getInstructionalDates(weekStartDate);
        request.setAttribute("dateKeys", toDateKeys(instructionalDates));
        request.setAttribute("dayLabels", toDayLabels(instructionalDates));
        Map<String, String> dateByDay = toDateByDay(instructionalDates);

        if (Constants.ROLE_FACULTY.equalsIgnoreCase(user.getRole()) && user.getFacultyId() != null) {
            request.setAttribute("timetableList", timetableService.getByFacultyId(user.getFacultyId()));
        } else {
            Integer divisionId = parseDivision(request.getParameter("divisionId"));
            request.setAttribute("selectedDivisionId", divisionId);
            List<Timetable> timetableList = timetableService.getByDivisionAndWeek(divisionId, weekStartDate);
            request.setAttribute("timetableList", timetableList);
            request.setAttribute("divisionGrid", buildGrid(
                    timetableList,
                    fixedActivityService.getAllActivities(),
                    dateByDay,
                    divisionId,
                    timeSlotService.getAllSlots(),
                    instructionalDates
            ));
        }

        request.setAttribute("facultyList", facultyService.getAllFaculty());
        request.getRequestDispatcher("/jsp/admin/timetable.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = getLoggedInUser(request);
        if (user == null || !Constants.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        timetableService.addEntry(
                Integer.parseInt(request.getParameter("divisionId")),
                Integer.parseInt(request.getParameter("facultyId")),
                Integer.parseInt(request.getParameter("subjectId")),
                Integer.parseInt(request.getParameter("roomId")),
                request.getParameter("scheduleDate"),
                request.getParameter("dayName"),
                Integer.parseInt(request.getParameter("slotId")),
                blankToNull(request.getParameter("batchLabel")),
                blankToNull(request.getParameter("notes"))
        );

        response.sendRedirect(request.getContextPath() + "/timetable?success=" +
                URLEncoder.encode("Timetable entry added", StandardCharsets.UTF_8));
    }

    private LocalDate parseWeekStart(String value) {
        return value == null || value.isBlank() ? LocalDate.now() : LocalDate.parse(value);
    }

    private Integer parseDivision(String value) {
        return value == null || value.isBlank() ? null : Integer.parseInt(value);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? "" : value.trim();
    }

    private Map<String, Map<Integer, Map<String, TimetableCellView>>> buildGrid(List<Timetable> timetableList,
                                                                                 List<FixedActivity> fixedActivities,
                                                                                 Map<String, String> dateByDay,
                                                                                 Integer selectedDivisionId,
                                                                                 List<com.timetable.model.TimeSlot> slotList,
                                                                                 List<LocalDate> instructionalDates) {
        Map<String, Map<Integer, Map<String, List<Timetable>>>> rawGrid = new LinkedHashMap<>();

        for (FixedActivity activity : fixedActivities) {
            if (selectedDivisionId != null && selectedDivisionId > 0 && activity.getDivisionId() != selectedDivisionId) {
                continue;
            }

            String scheduleDate = dateByDay.get(activity.getActivityDay().toLowerCase(Locale.ENGLISH));
            if (scheduleDate == null) {
                continue;
            }

            addCellEntry(rawGrid, activity.getDivisionCode(), activity.getSlotId(), scheduleDate, toSyntheticEntry(activity, scheduleDate));
        }

        for (Timetable entry : timetableList) {
            mergeActualEntry(rawGrid, entry);
        }
        return toCellViewGrid(rawGrid, slotList, toDateKeys(instructionalDates));
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

    private void addCellEntry(Map<String, Map<Integer, Map<String, List<Timetable>>>> grid,
                              String divisionCode,
                              int slotId,
                              String scheduleDate,
                              Timetable entry) {
        grid.computeIfAbsent(divisionCode, key -> new LinkedHashMap<>())
                .computeIfAbsent(slotId, key -> new LinkedHashMap<>())
                .computeIfAbsent(scheduleDate, key -> new ArrayList<>())
                .add(entry);
    }

    private void mergeActualEntry(Map<String, Map<Integer, Map<String, List<Timetable>>>> grid, Timetable actualEntry) {
        List<Timetable> cellEntries = grid
                .computeIfAbsent(actualEntry.getDivisionCode(), key -> new LinkedHashMap<>())
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
            matchingEntry.setTimetableId(actualEntry.getTimetableId());
            matchingEntry.setFacultyId(actualEntry.getFacultyId());
            matchingEntry.setSubjectId(actualEntry.getSubjectId());
            matchingEntry.setRoomId(actualEntry.getRoomId());
            matchingEntry.setDayName(actualEntry.getDayName());
            matchingEntry.setTimeSlot(actualEntry.getTimeSlot());
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

    private Map<String, Map<Integer, Map<String, TimetableCellView>>> toCellViewGrid(
            Map<String, Map<Integer, Map<String, List<Timetable>>>> rawGrid,
            List<com.timetable.model.TimeSlot> slotList,
            List<String> dateKeys) {
        Map<String, Map<Integer, Map<String, TimetableCellView>>> grid = new LinkedHashMap<>();

        for (Map.Entry<String, Map<Integer, Map<String, List<Timetable>>>> divisionEntry : rawGrid.entrySet()) {
            Map<Integer, Map<String, TimetableCellView>> slotMap = new LinkedHashMap<>();
            grid.put(divisionEntry.getKey(), slotMap);

            for (Map.Entry<Integer, Map<String, List<Timetable>>> slotEntry : divisionEntry.getValue().entrySet()) {
                Map<String, TimetableCellView> dateMap = new LinkedHashMap<>();
                slotMap.put(slotEntry.getKey(), dateMap);
                for (Map.Entry<String, List<Timetable>> dateEntry : slotEntry.getValue().entrySet()) {
                    TimetableCellView view = new TimetableCellView();
                    view.setEntries(deduplicateEntries(dateEntry.getValue()));
                    dateMap.put(dateEntry.getKey(), view);
                }
            }

            for (String dateKey : dateKeys) {
                int index = 0;
                while (index < slotList.size()) {
                    com.timetable.model.TimeSlot slot = slotList.get(index);
                    if (slot.isBreakSlot()) {
                        index++;
                        continue;
                    }

                    TimetableCellView currentCell = getCell(slotMap, slot.getSlotId(), dateKey);
                    if (currentCell == null || currentCell.isHidden() || currentCell.getEntries().isEmpty()) {
                        index++;
                        continue;
                    }

                    int rowSpan = 1;
                    int nextIndex = index + 1;
                    while (nextIndex < slotList.size()) {
                        com.timetable.model.TimeSlot nextSlot = slotList.get(nextIndex);
                        if (nextSlot.isBreakSlot()) {
                            break;
                        }

                        TimetableCellView nextCell = getCell(slotMap, nextSlot.getSlotId(), dateKey);
                        if (!canMergeCell(currentCell, nextCell)) {
                            break;
                        }

                        nextCell.setHidden(true);
                        rowSpan++;
                        nextIndex++;
                    }

                    currentCell.setRowSpan(rowSpan);
                    index = nextIndex;
                }
            }
        }

        return grid;
    }

    private List<Timetable> deduplicateEntries(List<Timetable> entries) {
        Map<String, Timetable> uniqueEntries = new LinkedHashMap<>();
        for (Timetable entry : entries) {
            String key = normalize(entry.getSubjectName())
                    + "#" + normalize(entry.getFacultyName())
                    + "#" + normalize(entry.getRoomNumber())
                    + "#" + normalize(entry.getBatchLabel())
                    + "#" + normalize(entry.getNotes());
            uniqueEntries.putIfAbsent(key, entry);
        }
        return new ArrayList<>(uniqueEntries.values());
    }

    private TimetableCellView getCell(Map<Integer, Map<String, TimetableCellView>> slotMap, int slotId, String dateKey) {
        Map<String, TimetableCellView> dateMap = slotMap.get(slotId);
        return dateMap == null ? null : dateMap.get(dateKey);
    }

    private boolean canMergeCell(TimetableCellView currentCell, TimetableCellView nextCell) {
        if (currentCell == null || nextCell == null || currentCell.getEntries().isEmpty() || nextCell.getEntries().isEmpty()) {
            return false;
        }
        if (currentCell.getEntries().size() != nextCell.getEntries().size()) {
            return false;
        }

        for (int i = 0; i < currentCell.getEntries().size(); i++) {
            Timetable left = currentCell.getEntries().get(i);
            Timetable right = nextCell.getEntries().get(i);
            if (!sameActivity(left, right)) {
                return false;
            }
        }
        return true;
    }

    public static class TimetableCellView {
        private List<Timetable> entries = new ArrayList<>();
        private int rowSpan = 1;
        private boolean hidden;

        public List<Timetable> getEntries() { return entries; }
        public void setEntries(List<Timetable> entries) { this.entries = entries; }
        public int getRowSpan() { return rowSpan; }
        public void setRowSpan(int rowSpan) { this.rowSpan = rowSpan; }
        public boolean isHidden() { return hidden; }
        public void setHidden(boolean hidden) { this.hidden = hidden; }
    }
}
