package com.timetable.service;

import com.timetable.dao.DivisionDAO;
import com.timetable.model.Division;

import java.util.List;

public class DivisionService {
    private final DivisionDAO divisionDAO = new DivisionDAO();

    public List<Division> getAllDivisions() {
        return divisionDAO.getAllDivisions();
    }
}

