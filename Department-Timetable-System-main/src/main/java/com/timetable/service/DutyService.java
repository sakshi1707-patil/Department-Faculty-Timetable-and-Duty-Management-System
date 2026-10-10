package com.timetable.service;

import com.timetable.dao.DutyDAO;
import com.timetable.model.Duty;

import java.util.List;

public class DutyService {
    private final DutyDAO dutyDAO = new DutyDAO();

    public void assignDuty(int facultyId, String dutyType, String date) {
        Duty duty = new Duty();
        duty.setFacultyId(facultyId);
        duty.setDutyType(dutyType);
        duty.setDate(date);
        dutyDAO.assignDuty(duty);
    }

    public List<Duty> getAllDuties() {
        return dutyDAO.getAllDuties();
    }

    public List<Duty> getByFacultyId(int facultyId) {
        return dutyDAO.getByFacultyId(facultyId);
    }
}

