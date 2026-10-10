package com.timetable.controller;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.Color;
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

@WebServlet("/timetable/pdf")
public class TimetablePdfServlet extends BaseServlet {
    private final TimetableService timetableService = new TimetableService();
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

        String divisionParam = request.getParameter("divisionId");
        if (divisionParam == null || divisionParam.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Select a division before downloading the PDF.");
            return;
        }

        int divisionId = Integer.parseInt(divisionParam);
        LocalDate weekStartDate = AutoGenerateTimetable.normalizeAcademicWeekStart(
                request.getParameter("weekStartDate") == null || request.getParameter("weekStartDate").isBlank()
                        ? LocalDate.now()
                        : LocalDate.parse(request.getParameter("weekStartDate"))
        );

        Division division = findDivision(divisionId);
        if (division == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Division not found.");
            return;
        }

        List<Timetable> timetableList = timetableService.getByDivisionAndWeek(divisionId, weekStartDate);
        List<TimeSlot> slotList = timeSlotService.getAllSlots();
        List<LocalDate> instructionalDates = AutoGenerateTimetable.getInstructionalDates(weekStartDate);
        Map<String, String> dateByDay = toDateByDay(instructionalDates);
        Map<Integer, Map<String, List<Timetable>>> grid = buildGrid(
                timetableList,
                fixedActivityService.getAllActivities(),
                dateByDay,
                divisionId
        );

        response.setContentType("application/pdf");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"" + division.getDivisionCode() + "-" + weekStartDate + ".pdf\""
        );

        try {
            Document document = new Document(PageSize.A4.rotate(), 10, 10, 10, 10);
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font subtitleFont = new Font(Font.HELVETICA, 7, Font.NORMAL);
            Font collegeFont = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(42, 73, 122));
            Font smallCapsFont = new Font(Font.HELVETICA, 7, Font.BOLD, new Color(42, 73, 122));
            Font headerFont = new Font(Font.HELVETICA, 7, Font.BOLD, Color.BLACK);
            Font cellFont = new Font(Font.HELVETICA, 6, Font.NORMAL);
            Font subjectFont = new Font(Font.HELVETICA, 6.5f, Font.BOLD);
            Font breakFont = new Font(Font.HELVETICA, 7, Font.BOLD);

            document.add(centered("KIT's College of Engineering Kolhapur", collegeFont));
            document.add(centered("(Empowered Autonomous)", smallCapsFont));
            document.add(centered("Department of CSE-AIML & CSE-DS", subtitleFont));
            document.add(centered("Time Table A.Y. 2025-26 Even Semester", subtitleFont));

            Paragraph title = new Paragraph(division.getDivisionCode() + " (" + division.getClassroom() + ")", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(1f);
            document.add(title);

            Paragraph subtitle = new Paragraph("Class Coordinator : " + division.getCoordinatorName(), subtitleFont);
            subtitle.setAlignment(Element.ALIGN_RIGHT);
            subtitle.setSpacingAfter(4f);
            document.add(subtitle);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.95f, 2.05f, 2.05f, 2.05f, 2.05f, 2.05f});
            table.setSplitLate(false);
            table.setSplitRows(false);
            table.setKeepTogether(true);

            addHeaderCell(table, "Time", headerFont);
            for (LocalDate date : instructionalDates) {
                addHeaderCell(
                        table,
                        date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + "\n" + date,
                        headerFont
                );
            }

            for (TimeSlot slot : slotList) {
                if (slot.isBreakSlot()) {
                    addNormalCell(table, slot.getSlotLabel(), breakFont, Color.LIGHT_GRAY);
                    PdfPCell breakCell = new PdfPCell(new Phrase(slot.getBreakName(), breakFont));
                    breakCell.setColspan(5);
                    breakCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    breakCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    breakCell.setBackgroundColor(new Color(245, 245, 245));
                    breakCell.setPadding(2f);
                    breakCell.setMinimumHeight(15f);
                    table.addCell(breakCell);
                    continue;
                }

                addNormalCell(table, slot.getSlotLabel(), cellFont, Color.WHITE);
                for (LocalDate date : instructionalDates) {
                    List<Timetable> cellEntries = grid
                            .getOrDefault(slot.getSlotId(), new LinkedHashMap<>())
                            .get(date.toString());
                    if (isRepeatedFromPreviousSlot(slotList, slot, date.toString(), grid)) {
                        cellEntries = new ArrayList<>();
                    }

                    PdfPCell cell = buildActivityCell(
                            cellEntries,
                            subjectFont,
                            cellFont
                    );
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();
        } catch (DocumentException exception) {
            throw new ServletException("Unable to generate timetable PDF", exception);
        }
    }

    private Paragraph centered(String text, Font font) {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingAfter(1f);
        return paragraph;
    }

    private Division findDivision(int divisionId) {
        for (Division division : divisionService.getAllDivisions()) {
            if (division.getDivisionId() == divisionId) {
                return division;
            }
        }
        return null;
    }

    private Map<Integer, Map<String, List<Timetable>>> buildGrid(List<Timetable> timetableList,
                                                                 List<FixedActivity> fixedActivities,
                                                                 Map<String, String> dateByDay,
                                                                 int divisionId) {
        Map<Integer, Map<String, List<Timetable>>> grid = new LinkedHashMap<>();
        for (FixedActivity activity : fixedActivities) {
            if (activity.getDivisionId() != divisionId) {
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
            matchingEntry.setTimetableId(actualEntry.getTimetableId());
            matchingEntry.setFacultyId(actualEntry.getFacultyId());
            matchingEntry.setSubjectId(actualEntry.getSubjectId());
            matchingEntry.setRoomId(actualEntry.getRoomId());
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

    private boolean isRepeatedFromPreviousSlot(List<TimeSlot> slotList,
                                               TimeSlot currentSlot,
                                               String scheduleDate,
                                               Map<Integer, Map<String, List<Timetable>>> grid) {
        TimeSlot previousSlot = null;
        for (TimeSlot slot : slotList) {
            if (slot.getSlotId() == currentSlot.getSlotId()) {
                break;
            }
            if (!slot.isBreakSlot()) {
                previousSlot = slot;
            }
        }

        if (previousSlot == null) {
            return false;
        }

        List<Timetable> currentEntries = grid
                .getOrDefault(currentSlot.getSlotId(), new LinkedHashMap<>())
                .get(scheduleDate);
        List<Timetable> previousEntries = grid
                .getOrDefault(previousSlot.getSlotId(), new LinkedHashMap<>())
                .get(scheduleDate);

        if (currentEntries == null || previousEntries == null || currentEntries.isEmpty()) {
            return false;
        }
        if (currentEntries.size() != previousEntries.size()) {
            return false;
        }

        for (int i = 0; i < currentEntries.size(); i++) {
            if (!sameActivity(currentEntries.get(i), previousEntries.get(i))) {
                return false;
            }
        }
        return true;
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(new Color(244, 244, 244));
        cell.setPadding(3f);
        cell.setMinimumHeight(22f);
        table.addCell(cell);
    }

    private PdfPCell buildActivityCell(List<Timetable> cellEntries, Font subjectFont, Font bodyFont) {
        PdfPCell cell = new PdfPCell();
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(2f);
        cell.setMinimumHeight(42f);
        cell.setBackgroundColor(Color.WHITE);

        if (cellEntries == null || cellEntries.isEmpty()) {
            return cell;
        }

        for (int index = 0; index < cellEntries.size(); index++) {
            Timetable entry = cellEntries.get(index);
            Paragraph paragraph = new Paragraph();
            paragraph.setAlignment(Element.ALIGN_CENTER);
            paragraph.setLeading(7f);
            paragraph.add(new Phrase(entry.getSubjectName() == null ? "" : entry.getSubjectName(), subjectFont));

            if (entry.getFacultyName() != null && !entry.getFacultyName().isBlank()) {
                paragraph.add(new Phrase("\n" + entry.getFacultyName(), bodyFont));
            }
            if (entry.getRoomNumber() != null && !entry.getRoomNumber().isBlank()) {
                paragraph.add(new Phrase("\n" + entry.getRoomNumber(), bodyFont));
            }
            if (entry.getBatchLabel() != null && !entry.getBatchLabel().isBlank()) {
                paragraph.add(new Phrase("\n" + entry.getBatchLabel(), bodyFont));
            }
            if (entry.getNotes() != null
                    && !entry.getNotes().isBlank()
                    && !"THEORY".equalsIgnoreCase(entry.getNotes())
                    && !"PRACTICAL".equalsIgnoreCase(entry.getNotes())) {
                paragraph.add(new Phrase("\n" + entry.getNotes(), bodyFont));
            }
            paragraph.setSpacingAfter(index == cellEntries.size() - 1 ? 0f : 2f);
            cell.addElement(paragraph);
        }

        return cell;
    }

    private void addNormalCell(PdfPTable table, String text, Font font, Color backgroundColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(2f);
        cell.setMinimumHeight(42f);
        cell.setBackgroundColor(backgroundColor);
        table.addCell(cell);
    }
}
