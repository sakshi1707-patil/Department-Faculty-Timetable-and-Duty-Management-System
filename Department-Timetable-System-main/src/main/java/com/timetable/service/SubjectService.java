package com.timetable.service;

import com.timetable.dao.SubjectDAO;
import com.timetable.model.Subject;

import java.util.List;

public class SubjectService {
    private final SubjectDAO subjectDAO = new SubjectDAO();

    public void addSubject(String subjectName,
                           int hoursPerWeek,
                           int facultyId,
                           int divisionId,
                           String activityType,
                           String preferredRoomType,
                           String batchLabel) {
        Subject subject = new Subject();
        subject.setSubjectName(subjectName);
        subject.setHoursPerWeek(hoursPerWeek);
        subject.setFacultyId(facultyId);
        subject.setDivisionId(divisionId);
        subject.setActivityType(activityType);
        subject.setPreferredRoomType(preferredRoomType);
        subject.setBatchLabel(batchLabel);
        subjectDAO.addSubject(subject);
    }

    public List<Subject> getAllSubjects() {
        return subjectDAO.getAllSubjects();
    }

    public List<Subject> getSubjectsByDivision(int divisionId) {
        return subjectDAO.getSubjectsByDivision(divisionId);
    }
}
