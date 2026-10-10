package com.timetable.service;

import com.timetable.dao.FixedActivityDAO;
import com.timetable.model.FixedActivity;

import java.util.List;

public class FixedActivityService {
    private final FixedActivityDAO fixedActivityDAO = new FixedActivityDAO();

    public List<FixedActivity> getAllActivities() {
        return fixedActivityDAO.getAllActivities();
    }
}
