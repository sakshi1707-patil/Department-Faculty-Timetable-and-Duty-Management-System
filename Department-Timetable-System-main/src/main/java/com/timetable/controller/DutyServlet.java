package com.timetable.controller;

import com.timetable.model.User;
import com.timetable.service.DutyService;
import com.timetable.util.Constants;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/duty")
public class DutyServlet extends BaseServlet {
    private final DutyService dutyService = new DutyService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = getLoggedInUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (Constants.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
            request.setAttribute("dutyList", dutyService.getAllDuties());
            request.getRequestDispatcher("/jsp/admin/duties.jsp").forward(request, response);
            return;
        }

        if (Constants.ROLE_FACULTY.equalsIgnoreCase(user.getRole()) && user.getFacultyId() != null) {
            request.setAttribute("dutyList", dutyService.getByFacultyId(user.getFacultyId()));
            request.getRequestDispatcher("/jsp/faculty/duties.jsp").forward(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/student");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = getLoggedInUser(request);
        if (user == null || !Constants.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        dutyService.assignDuty(
                Integer.parseInt(request.getParameter("facultyId")),
                request.getParameter("dutyType"),
                request.getParameter("date")
        );

        response.sendRedirect(request.getContextPath() + "/duty");
    }
}
