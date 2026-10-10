package com.timetable.service;

import com.timetable.dao.LeaveDAO;
import com.timetable.dao.RoomDAO;
import com.timetable.dao.SubjectDAO;
import com.timetable.dao.TimetableDAO;
import com.timetable.dao.TimeSlotDAO;
import com.timetable.dao.FixedActivityDAO;
import com.timetable.model.FixedActivity;
import com.timetable.model.Room;
import com.timetable.model.Subject;
import com.timetable.model.TimeSlot;
import com.timetable.model.Timetable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

public final class AutoGenerateTimetable {

    private AutoGenerateTimetable() {}

    // =========================================================================
    // SYNC KEYWORDS
    // =========================================================================
    private static final String[] MDM_KEYWORDS  = { "mdm", "multidisciplinary" };
    private static final String[] MINI_KEYWORDS = { "mini project" };
    private static final String[] BE_KEYWORDS   = { "be project", "project phase" };

    private static final String[] SY_PREFIXES = { "SYAIML", "SYDS" };
    private static final String[] TY_PREFIXES = { "TYAIML" };
    private static final String[] FY_PREFIXES = { "FYAIML", "FYDS" };

    // =========================================================================
    // BATCH GROUP LABELS
    // Group-1 picks slot first, Group-2 forced to same slot in different lab
    // =========================================================================
    private static final String[] GROUP1_LABELS = {
        "A1-A2", "B1-B2", "C1-C2", "D1-D2"
    };
    private static final String[] GROUP2_LABELS = {
        "A3-A4", "B3-B4", "C3-C4", "D3-D4"
    };

    // Valid 2-hour lab block start sort_orders
    private static final int[] LAB_BLOCK_STARTS = { 1, 4, 7, 8 };

    // =========================================================================
    // MAIN ENTRY POINT
    // =========================================================================
    public static String generate(LocalDate weekStartDate) {
        if (weekStartDate == null) return "Select a valid week start date.";
        weekStartDate = normalizeAcademicWeekStart(weekStartDate);

        SubjectDAO       subjectDAO       = new SubjectDAO();
        RoomDAO          roomDAO          = new RoomDAO();
        TimeSlotDAO      timeSlotDAO      = new TimeSlotDAO();
        TimetableDAO     timetableDAO     = new TimetableDAO();
        LeaveDAO         leaveDAO         = new LeaveDAO();
        FixedActivityDAO fixedActivityDAO = new FixedActivityDAO();

        List<Subject>       subjects        = subjectDAO.getAllSubjects();
        List<Room>          rooms           = roomDAO.getAllRooms();
        List<TimeSlot>      slots           = timeSlotDAO.getAllSlots();
        Set<String>         leaveKeys       = leaveDAO.getApprovedLeaveKeys();
        List<FixedActivity> fixedActivities = fixedActivityDAO.getAllActivities();

        if (subjects.isEmpty()) return "No subjects found.";

        timetableDAO.deleteEntriesForWeek(weekStartDate);

        // Occupancy tracking
        Set<String>               occupiedDivisionSlots = new HashSet<>();
        Set<String>               occupiedFacultySlots  = new HashSet<>();
        Set<String>               occupiedRoomSlots     = new HashSet<>();
        Map<Integer, Integer>     scheduledHours        = new HashMap<>();
        Map<String, Integer>      subjectDayUsage       = new HashMap<>();
        Map<String, Integer>      divisionDayLoad       = new HashMap<>();
        Map<Integer, Set<String>> subjectScheduledDates = new HashMap<>();

        // One lab per division per day
        Set<String> divisionDayHasLab = new HashSet<>();

        // Synchronized slot locks: "SY_MDM" -> "2026-07-01#4"
        Map<String, String> syncedSlotLock = new HashMap<>();

        // Paired lab lock: "divisionId#subjectBaseName" -> "date#firstSlotId"
        Map<String, String> pairedLabLock = new HashMap<>();

        int           generatedCount = 0;
        StringBuilder unscheduled   = new StringBuilder();

        List<LocalDate> instructionalDates = getInstructionalDates(weekStartDate);
        reserveFixedActivitySlots(fixedActivities, instructionalDates, occupiedDivisionSlots);

        // PRIORITY ORDER:
        // 1. Labs (practicals) first
        // 2. MDM synchronized theory
        // 3. Mini Project / BE Project
        // 4. Regular theory last
        subjects.sort(Comparator.comparingInt(AutoGenerateTimetable::subjectPriority));

        for (Subject subject : subjects) {
            int remaining = subject.getHoursPerWeek()
                            - scheduledHours.getOrDefault(subject.getSubjectId(), 0);

            while (remaining > 0) {
                List<Timetable> entries;
                String syncKey = getSyncKey(subject);

                if (isPractical(subject)) {
                    entries = findLabSlots(subject, rooms, slots, instructionalDates,
                            occupiedDivisionSlots, occupiedFacultySlots, occupiedRoomSlots,
                            leaveKeys, subjectDayUsage, divisionDayLoad,
                            subjectScheduledDates, divisionDayHasLab, pairedLabLock);
                } else if (syncKey != null) {
                    entries = findSyncedSlots(subject, rooms, slots, instructionalDates,
                            occupiedDivisionSlots, occupiedFacultySlots, occupiedRoomSlots,
                            leaveKeys, subjectDayUsage, divisionDayLoad,
                            subjectScheduledDates, 1, syncKey, syncedSlotLock);
                } else {
                    entries = findSlotsForSubject(subject, rooms, slots, instructionalDates,
                            occupiedDivisionSlots, occupiedFacultySlots, occupiedRoomSlots,
                            leaveKeys, subjectDayUsage, divisionDayLoad,
                            subjectScheduledDates, 1);
                }

                if (entries.isEmpty()) {
                    if (unscheduled.length() > 0) unscheduled.append(", ");
                    unscheduled.append(subject.getDivisionCode())
                               .append("-").append(subject.getSubjectName())
                               .append("(").append(remaining).append("hr)");
                    break;
                }

                for (Timetable entry : entries) {
                    timetableDAO.addEntry(entry);
                    scheduledHours.merge(subject.getSubjectId(), 1, Integer::sum);
                    occupiedDivisionSlots.add(slotKey(subject.getDivisionId(),
                            entry.getScheduleDate(), entry.getSlotId()));
                    occupiedFacultySlots.add(slotKey(subject.getFacultyId(),
                            entry.getScheduleDate(), entry.getSlotId()));
                    occupiedRoomSlots.add(slotKey(entry.getRoomId(),
                            entry.getScheduleDate(), entry.getSlotId()));
                    generatedCount++;
                }

                String firstDate = entries.get(0).getScheduleDate();
                subjectDayUsage.merge(subject.getSubjectId() + "#" + firstDate, 1, Integer::sum);
                divisionDayLoad.merge(subject.getDivisionId() + "#" + firstDate,
                        entries.size(), Integer::sum);
                subjectScheduledDates
                        .computeIfAbsent(subject.getSubjectId(), k -> new HashSet<>())
                        .add(firstDate);
                if (isPractical(subject)) {
                    divisionDayHasLab.add(subject.getDivisionId() + "#" + firstDate);
                }
                remaining -= entries.size();
            }
        }

        if (generatedCount == 0) return "No timetable entries generated.";
        if (unscheduled.length() == 0)
            return "Generated " + generatedCount + " entries from " + weekStartDate + ".";
        return "Generated " + generatedCount + " entries. Could not schedule: " + unscheduled;
    }

    // =========================================================================
    // LAB SCHEDULING
    // Group-1 (A1-A2) picks slot and lab first
    // Group-2 (A3-A4) forced to SAME slot but DIFFERENT lab
    // Same subject, same faculty for both groups
    // Both run simultaneously for 2 hours
    // Only ONE lab session per division per day
    // =========================================================================
    private static List<Timetable> findLabSlots(Subject subject,
                                                 List<Room> rooms,
                                                 List<TimeSlot> slots,
                                                 List<LocalDate> instructionalDates,
                                                 Set<String> occupiedDivisionSlots,
                                                 Set<String> occupiedFacultySlots,
                                                 Set<String> occupiedRoomSlots,
                                                 Set<String> leaveKeys,
                                                 Map<String, Integer> subjectDayUsage,
                                                 Map<String, Integer> divisionDayLoad,
                                                 Map<Integer, Set<String>> subjectScheduledDates,
                                                 Set<String> divisionDayHasLab,
                                                 Map<String, String> pairedLabLock) {

        String  batchLabel = subject.getBatchLabel() == null ? "" : subject.getBatchLabel().trim();
        boolean isGroup1   = isGroup1Batch(batchLabel);
        boolean isGroup2   = isGroup2Batch(batchLabel);

        // Pairing key: same subject, same division
        String pairedKey = subject.getDivisionId() + "#" + baseName(subject.getSubjectName());

        // Group-2: must run at same time as group-1 in a different lab
        if (isGroup2 && pairedLabLock.containsKey(pairedKey)) {
            return scheduleGroup2Lab(subject, rooms, slots,
                    occupiedFacultySlots, occupiedRoomSlots,
                    leaveKeys, pairedLabLock.get(pairedKey));
        }

        // Group-1 or unpaired: pick freely
        List<LocalDate> orderedDates = sortedDates(instructionalDates, subject,
                subjectDayUsage, divisionDayLoad);

        for (LocalDate scheduleDate : orderedDates) {
            String dateKey = scheduleDate.toString();

            // One lab per division per day
            if (divisionDayHasLab.contains(subject.getDivisionId() + "#" + dateKey)) continue;
            if (leaveKeys.contains(subject.getFacultyId() + "#" + dateKey)) continue;
            if (!canUseDateForSubject(subject, dateKey, subjectDayUsage,
                    subjectScheduledDates, instructionalDates)) continue;

            // Try valid 2-hour lab blocks only
            for (int startOrder : LAB_BLOCK_STARTS) {
                int slotIndex = findSlotIndexBySortOrder(slots, startOrder);
                if (slotIndex == -1) continue;

                List<TimeSlot> candidateSlots = getStrictLabBlock(slots, slotIndex);
                if (candidateSlots == null) continue;

                // Check division free for both slots
                boolean divFree = true;
                for (TimeSlot cs : candidateSlots) {
                    if (occupiedDivisionSlots.contains(
                            slotKey(subject.getDivisionId(), dateKey, cs.getSlotId()))) {
                        divFree = false; break;
                    }
                }
                if (!divFree) continue;

                // Check faculty free for both slots
                boolean facFree = true;
                for (TimeSlot cs : candidateSlots) {
                    if (occupiedFacultySlots.contains(
                            slotKey(subject.getFacultyId(), dateKey, cs.getSlotId()))) {
                        facFree = false; break;
                    }
                }
                if (!facFree) continue;

                // Find a free lab
                for (Room room : rooms) {
                    if (!"LAB".equalsIgnoreCase(room.getRoomType())) continue;
                    boolean roomFree = true;
                    for (TimeSlot cs : candidateSlots) {
                        if (occupiedRoomSlots.contains(
                                slotKey(room.getRoomId(), dateKey, cs.getSlotId()))) {
                            roomFree = false; break;
                        }
                    }
                    if (!roomFree) continue;

                    List<Timetable> entries = buildEntries(subject, room,
                            scheduleDate, candidateSlots);

                    // Lock for group-2 pairing
                    if (isGroup1) {
                        pairedLabLock.put(pairedKey,
                                dateKey + "#" + candidateSlots.get(0).getSlotId());
                    }
                    return entries;
                }
            }
        }
        return new ArrayList<>();
    }

    // Group-2 scheduled at SAME date+slot as group-1 but DIFFERENT lab
    private static List<Timetable> scheduleGroup2Lab(Subject subject,
                                                      List<Room> rooms,
                                                      List<TimeSlot> slots,
                                                      Set<String> occupiedFacultySlots,
                                                      Set<String> occupiedRoomSlots,
                                                      Set<String> leaveKeys,
                                                      String lockValue) {
        String[] parts       = lockValue.split("#");
        String   lockedDate  = parts[0];
        int      lockedSlotId = Integer.parseInt(parts[1]);

        int startIndex = -1;
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).getSlotId() == lockedSlotId) { startIndex = i; break; }
        }
        if (startIndex == -1) return new ArrayList<>();

        List<TimeSlot> candidateSlots = getStrictLabBlock(slots, startIndex);
        if (candidateSlots == null) return new ArrayList<>();

        if (leaveKeys.contains(subject.getFacultyId() + "#" + lockedDate))
            return new ArrayList<>();

        // Check faculty free
        for (TimeSlot cs : candidateSlots) {
            if (occupiedFacultySlots.contains(
                    slotKey(subject.getFacultyId(), lockedDate, cs.getSlotId())))
                return new ArrayList<>();
        }

        // Find a DIFFERENT free lab (group-1's lab already in occupiedRoomSlots)
        for (Room room : rooms) {
            if (!"LAB".equalsIgnoreCase(room.getRoomType())) continue;
            boolean roomFree = true;
            for (TimeSlot cs : candidateSlots) {
                if (occupiedRoomSlots.contains(
                        slotKey(room.getRoomId(), lockedDate, cs.getSlotId()))) {
                    roomFree = false; break;
                }
            }
            if (!roomFree) continue;
            return buildEntries(subject, room, LocalDate.parse(lockedDate), candidateSlots);
        }
        return new ArrayList<>();
    }

    // =========================================================================
    // SYNCHRONIZED THEORY SLOT SCHEDULING (MDM)
    // =========================================================================
    private static List<Timetable> findSyncedSlots(Subject subject,
                                                    List<Room> rooms,
                                                    List<TimeSlot> slots,
                                                    List<LocalDate> instructionalDates,
                                                    Set<String> occupiedDivisionSlots,
                                                    Set<String> occupiedFacultySlots,
                                                    Set<String> occupiedRoomSlots,
                                                    Set<String> leaveKeys,
                                                    Map<String, Integer> subjectDayUsage,
                                                    Map<String, Integer> divisionDayLoad,
                                                    Map<Integer, Set<String>> subjectScheduledDates,
                                                    int blockSize,
                                                    String syncKey,
                                                    Map<String, String> syncedSlotLock) {

        if (syncedSlotLock.containsKey(syncKey)) {
            // Forced to same slot as first division
            String[] parts       = syncedSlotLock.get(syncKey).split("#");
            String   lockedDate  = parts[0];
            int      lockedSlotId = Integer.parseInt(parts[1]);

            int startIndex = -1;
            for (int i = 0; i < slots.size(); i++) {
                if (slots.get(i).getSlotId() == lockedSlotId) { startIndex = i; break; }
            }
            if (startIndex == -1) return new ArrayList<>();

            List<TimeSlot> candidateSlots = getCandidateSlots(slots, startIndex, blockSize);
            if (candidateSlots.size() != blockSize) return new ArrayList<>();

            if (leaveKeys.contains(subject.getFacultyId() + "#" + lockedDate))
                return new ArrayList<>();
            for (TimeSlot cs : candidateSlots) {
                if (occupiedFacultySlots.contains(
                        slotKey(subject.getFacultyId(), lockedDate, cs.getSlotId())))
                    return new ArrayList<>();
                if (occupiedDivisionSlots.contains(
                        slotKey(subject.getDivisionId(), lockedDate, cs.getSlotId())))
                    return new ArrayList<>();
            }

            for (Room room : rooms) {
                if (!room.getRoomType().equalsIgnoreCase(subject.getPreferredRoomType())) continue;
                boolean roomFree = true;
                for (TimeSlot cs : candidateSlots) {
                    if (occupiedRoomSlots.contains(
                            slotKey(room.getRoomId(), lockedDate, cs.getSlotId()))) {
                        roomFree = false; break;
                    }
                }
                if (!roomFree) continue;
                return buildEntries(subject, room, LocalDate.parse(lockedDate), candidateSlots);
            }
            return new ArrayList<>();

        } else {
            // First division picks freely and locks
            List<Timetable> entries = findSlotsForSubject(subject, rooms, slots,
                    instructionalDates, occupiedDivisionSlots, occupiedFacultySlots,
                    occupiedRoomSlots, leaveKeys, subjectDayUsage, divisionDayLoad,
                    subjectScheduledDates, blockSize);
            if (!entries.isEmpty()) {
                syncedSlotLock.put(syncKey,
                        entries.get(0).getScheduleDate() + "#" + entries.get(0).getSlotId());
            }
            return entries;
        }
    }

    // =========================================================================
    // REGULAR THEORY SLOT SCHEDULING
    // =========================================================================
    private static List<Timetable> findSlotsForSubject(Subject subject,
                                                        List<Room> rooms,
                                                        List<TimeSlot> slots,
                                                        List<LocalDate> instructionalDates,
                                                        Set<String> occupiedDivisionSlots,
                                                        Set<String> occupiedFacultySlots,
                                                        Set<String> occupiedRoomSlots,
                                                        Set<String> leaveKeys,
                                                        Map<String, Integer> subjectDayUsage,
                                                        Map<String, Integer> divisionDayLoad,
                                                        Map<Integer, Set<String>> subjectScheduledDates,
                                                        int blockSize) {
        List<LocalDate> orderedDates = sortedDates(instructionalDates, subject,
                subjectDayUsage, divisionDayLoad);

        for (LocalDate scheduleDate : orderedDates) {
            String dateKey = scheduleDate.toString();
            if (leaveKeys.contains(subject.getFacultyId() + "#" + scheduleDate)) continue;
            if (!canUseDateForSubject(subject, dateKey, subjectDayUsage,
                    subjectScheduledDates, instructionalDates)) continue;

            for (int slotIndex : getPreferredSlotIndexes(slots)) {
                if (slots.get(slotIndex).isBreakSlot()) continue;
                List<TimeSlot> candidateSlots = getCandidateSlots(slots, slotIndex, blockSize);
                if (candidateSlots.size() != blockSize) continue;

                for (Room room : rooms) {
                    if (!room.getRoomType().equalsIgnoreCase(subject.getPreferredRoomType()))
                        continue;
                    if (!areSlotsAvailable(subject, room, dateKey, candidateSlots,
                            occupiedDivisionSlots, occupiedFacultySlots, occupiedRoomSlots))
                        continue;
                    return buildEntries(subject, room, scheduleDate, candidateSlots);
                }
            }
        }
        return new ArrayList<>();
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static List<TimeSlot> getStrictLabBlock(List<TimeSlot> slots, int startIndex) {
        if (startIndex + 1 >= slots.size()) return null;
        TimeSlot s1 = slots.get(startIndex);
        TimeSlot s2 = slots.get(startIndex + 1);
        if (s1.isBreakSlot() || s2.isBreakSlot()) return null;
        if (s2.getSortOrder() != s1.getSortOrder() + 1) return null;
        List<TimeSlot> block = new ArrayList<>();
        block.add(s1);
        block.add(s2);
        return block;
    }

    private static int findSlotIndexBySortOrder(List<TimeSlot> slots, int sortOrder) {
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).getSortOrder() == sortOrder) return i;
        }
        return -1;
    }

    private static List<Integer> getPreferredSlotIndexes(List<TimeSlot> slots) {
        List<Integer> preferred = new ArrayList<>();
        List<Integer> fallback  = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).isBreakSlot()) continue;
            int order = slots.get(i).getSortOrder();
            if (order == 2 || order == 4 || order == 5 || order == 7) preferred.add(i);
            else fallback.add(i);
        }
        preferred.addAll(fallback);
        return preferred;
    }

    private static String baseName(String subjectName) {
        return subjectName == null ? "" : subjectName.trim().toLowerCase(Locale.ENGLISH);
    }

    private static boolean isGroup1Batch(String label) {
        for (String g : GROUP1_LABELS)
            if (g.equalsIgnoreCase(label)) return true;
        return false;
    }

    private static boolean isGroup2Batch(String label) {
        for (String g : GROUP2_LABELS)
            if (g.equalsIgnoreCase(label)) return true;
        return false;
    }

    private static List<Timetable> buildEntries(Subject subject, Room room,
                                                 LocalDate date,
                                                 List<TimeSlot> candidateSlots) {
        String dateStr = date.toString();
        String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        List<Timetable> list = new ArrayList<>();
        for (TimeSlot cs : candidateSlots) {
            Timetable t = new Timetable();
            t.setDivisionId(subject.getDivisionId());
            t.setFacultyId(subject.getFacultyId());
            t.setSubjectId(subject.getSubjectId());
            t.setRoomId(room.getRoomId());
            t.setScheduleDate(dateStr);
            t.setDayName(dayName);
            t.setSlotId(cs.getSlotId());
            t.setBatchLabel(subject.getBatchLabel());
            t.setNotes(subject.getActivityType());
            list.add(t);
        }
        return list;
    }

    private static List<LocalDate> sortedDates(List<LocalDate> dates, Subject subject,
                                                Map<String, Integer> subjectDayUsage,
                                                Map<String, Integer> divisionDayLoad) {
        List<LocalDate> ordered = new ArrayList<>(dates);
        ordered.sort(
            Comparator.comparingInt((LocalDate d) ->
                    subjectDayUsage.getOrDefault(subject.getSubjectId() + "#" + d, 0))
                .thenComparingInt(d ->
                    divisionDayLoad.getOrDefault(subject.getDivisionId() + "#" + d, 0))
                .thenComparing(LocalDate::toString));
        return ordered;
    }

    private static void reserveFixedActivitySlots(List<FixedActivity> fixedActivities,
                                                   List<LocalDate> instructionalDates,
                                                   Set<String> occupiedDivisionSlots) {
        Map<String, String> dateByDay = new HashMap<>();
        for (LocalDate date : instructionalDates) {
            dateByDay.put(date.getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                    .toLowerCase(Locale.ENGLISH), date.toString());
        }
        for (FixedActivity fa : fixedActivities) {
            String sd = dateByDay.get(fa.getActivityDay().toLowerCase(Locale.ENGLISH));
            if (sd == null) continue;
            occupiedDivisionSlots.add(slotKey(fa.getDivisionId(), sd, fa.getSlotId()));
        }
    }

    private static String getSyncKey(Subject subject) {
        String name = subject.getSubjectName().toLowerCase(Locale.ENGLISH);
        String year = getYearPrefix(subject.getDivisionCode().toUpperCase(Locale.ENGLISH));
        if (year == null) return null;
        if (containsAny(name, MDM_KEYWORDS))  return year + "_MDM";
        if (containsAny(name, MINI_KEYWORDS)) return year + "_MINI";
        if (containsAny(name, BE_KEYWORDS))   return year + "_BE";
        return null;
    }

    private static String getYearPrefix(String divCode) {
        for (String p : SY_PREFIXES) if (divCode.startsWith(p)) return "SY";
        for (String p : TY_PREFIXES) if (divCode.startsWith(p)) return "TY";
        for (String p : FY_PREFIXES) if (divCode.startsWith(p)) return "FY";
        return null;
    }

    private static boolean containsAny(String text, String[] keywords) {
        for (String kw : keywords) if (text.contains(kw)) return true;
        return false;
    }

    private static int subjectPriority(Subject s) {
        // Labs first (priority 1), then MDM (2), then Mini/BE Project (3), then theory (10)
        if (isPractical(s)) return 1;
        String name = s.getSubjectName().toLowerCase(Locale.ENGLISH);
        if (containsAny(name, MDM_KEYWORDS))  return 2;
        if (containsAny(name, MINI_KEYWORDS)) return 3;
        if (containsAny(name, BE_KEYWORDS))   return 3;
        return 10;
    }

    private static String slotKey(int id, String date, int slotId) {
        return id + "#" + date + "#" + slotId;
    }

    public static LocalDate normalizeAcademicWeekStart(LocalDate d) {
        switch (d.getDayOfWeek()) {
            case MONDAY:    return d.plusDays(1);
            case TUESDAY:   return d;
            case WEDNESDAY: return d.minusDays(1);
            case THURSDAY:  return d.minusDays(2);
            case FRIDAY:    return d.minusDays(3);
            case SATURDAY:  return d.minusDays(4);
            default:        return d.minusDays(5);
        }
    }

    public static List<LocalDate> getInstructionalDates(LocalDate start) {
        List<LocalDate> dates = new ArrayList<>();
        for (int i = 0; i < 5; i++) dates.add(start.plusDays(i));
        return dates;
    }

    private static boolean isPractical(Subject subject) {
        return "PRACTICAL".equalsIgnoreCase(subject.getActivityType());
    }

    private static List<TimeSlot> getCandidateSlots(List<TimeSlot> slots,
                                                     int startIndex, int blockSize) {
        List<TimeSlot> result = new ArrayList<>();
        for (int offset = 0; offset < blockSize; offset++) {
            int idx = startIndex + offset;
            if (idx >= slots.size()) return new ArrayList<>();
            TimeSlot ts = slots.get(idx);
            if (ts.isBreakSlot()) return new ArrayList<>();
            if (offset > 0 && ts.getSortOrder()
                    != result.get(offset - 1).getSortOrder() + 1) return new ArrayList<>();
            result.add(ts);
        }
        return result;
    }

    private static boolean areSlotsAvailable(Subject subject, Room room, String dateKey,
                                              List<TimeSlot> candidateSlots,
                                              Set<String> occupiedDivisionSlots,
                                              Set<String> occupiedFacultySlots,
                                              Set<String> occupiedRoomSlots) {
        for (TimeSlot cs : candidateSlots) {
            if (occupiedDivisionSlots.contains(
                    slotKey(subject.getDivisionId(), dateKey, cs.getSlotId()))) return false;
            if (occupiedFacultySlots.contains(
                    slotKey(subject.getFacultyId(), dateKey, cs.getSlotId()))) return false;
            if (occupiedRoomSlots.contains(
                    slotKey(room.getRoomId(), dateKey, cs.getSlotId()))) return false;
        }
        return true;
    }

    private static boolean canUseDateForSubject(Subject subject, String dateKey,
                                                 Map<String, Integer> subjectDayUsage,
                                                 Map<Integer, Set<String>> subjectScheduledDates,
                                                 List<LocalDate> instructionalDates) {
        int usageToday = subjectDayUsage.getOrDefault(
                subject.getSubjectId() + "#" + dateKey, 0);
        if (isPractical(subject)) return usageToday == 0;
        if (usageToday > 0) return false;
        Set<String> usedDates = subjectScheduledDates.getOrDefault(
                subject.getSubjectId(), new HashSet<>());
        int remaining = instructionalDates.size() - usedDates.size();
        return remaining <= subject.getHoursPerWeek() || !usedDates.contains(dateKey);
    }
}
