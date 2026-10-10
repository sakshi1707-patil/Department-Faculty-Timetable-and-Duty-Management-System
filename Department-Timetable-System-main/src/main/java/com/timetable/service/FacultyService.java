package com.timetable.service;

import com.timetable.dao.FacultyDAO;
import com.timetable.model.Faculty;

import java.util.List;

public class FacultyService {
    private final FacultyDAO facultyDAO = new FacultyDAO();

    public void addFaculty(String name, String department, String email) {
        Faculty faculty = new Faculty();
        faculty.setName(name);
        faculty.setDepartment(department);
        faculty.setEmail(email);
        facultyDAO.addFaculty(faculty);
    }

    public List<Faculty> getAllFaculty() {
        return facultyDAO.getAllFaculty();
    }

    public Faculty getFacultyById(int facultyId) {
        return facultyDAO.getFacultyById(facultyId);
    }
}
