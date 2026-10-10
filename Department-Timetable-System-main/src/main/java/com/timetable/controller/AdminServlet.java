package com.timetable.controller;

import com.timetable.model.User;
import com.timetable.service.AutoGenerateTimetable;
import com.timetable.service.DivisionService;
import com.timetable.service.FacultyService;
import com.timetable.service.LeaveService;
import com.timetable.service.SubjectService;
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

@WebServlet("/admin")
public class AdminServlet extends BaseServlet {
    private final FacultyService facultyService = new FacultyService();
    private final SubjectService subjectService = new SubjectService();
    private final DivisionService divisionService = new DivisionService();
    private final LeaveService leaveService = new LeaveService();
    private final TimetableService timetableService = new TimetableService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = getLoggedInUser(request);
        if (user == null || !Constants.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        request.setAttribute("facultyList", facultyService.getAllFaculty());
        request.setAttribute("subjectList", subjectService.getAllSubjects());
        request.setAttribute("divisionList", divisionService.getAllDivisions());
        request.setAttribute("defaultWeekStart", LocalDate.now());
        request.setAttribute("leaveList", leaveService.getAllLeaves());
        request.setAttribute("attentionEntries", timetableService.getEntriesRequiringAttention());
        request.getRequestDispatcher("/jsp/admin/adminDashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = getLoggedInUser(request);
        if (user == null || !Constants.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");

        if ("addFaculty".equals(action)) {
            facultyService.addFaculty(
                    request.getParameter("name"),
                    request.getParameter("department"),
                    request.getParameter("email")
            );
            response.sendRedirect(request.getContextPath() + "/admin?success=" + encode("Faculty added"));
            return;
        }

        if ("addSubject".equals(action)) {
            subjectService.addSubject(
                    request.getParameter("subjectName"),
                    Integer.parseInt(request.getParameter("hoursPerWeek")),
                    Integer.parseInt(request.getParameter("facultyId")),
                    Integer.parseInt(request.getParameter("divisionId")),
                    request.getParameter("activityType"),
                    request.getParameter("preferredRoomType"),
                    blankToNull(request.getParameter("batchLabel"))
            );
            response.sendRedirect(request.getContextPath() + "/admin?success=" + encode("Subject added"));
            return;
        }

        if ("autoGenerate".equals(action)) {
            LocalDate weekStartDate = LocalDate.parse(request.getParameter("weekStartDate"));
            response.sendRedirect(request.getContextPath() + "/admin?success=" +
                    encode(AutoGenerateTimetable.generate(weekStartDate)));
            return;
        }

        response.sendRedirect(request.getContextPath() + "/admin");
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? "" : value.trim();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
