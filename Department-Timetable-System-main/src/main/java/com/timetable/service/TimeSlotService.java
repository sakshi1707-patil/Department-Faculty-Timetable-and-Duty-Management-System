package com.timetable.service;

import com.timetable.dao.TimeSlotDAO;
import com.timetable.model.TimeSlot;

import java.util.List;

public class TimeSlotService {
    private final TimeSlotDAO timeSlotDAO = new TimeSlotDAO();

    public List<TimeSlot> getAllSlots() {
        return timeSlotDAO.getAllSlots();
    }
}

