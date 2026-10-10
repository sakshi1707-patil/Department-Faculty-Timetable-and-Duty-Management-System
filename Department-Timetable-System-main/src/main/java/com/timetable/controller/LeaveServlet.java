package com.timetable.controller;

import com.timetable.model.User;
import com.timetable.service.LeaveService;
import com.timetable.util.Constants;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/leave")
public class LeaveServlet extends BaseServlet {
    private final LeaveService leaveService = new LeaveService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = getLoggedInUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (Constants.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
            request.setAttribute("leaveList", leaveService.getAllLeaves());
            request.getRequestDispatcher("/jsp/admin/leaves.jsp").forward(request, response);
            return;
        }

        if (Constants.ROLE_FACULTY.equalsIgnoreCase(user.getRole()) && user.getFacultyId() != null) {
            request.setAttribute("leaveList", leaveService.getLeavesByFaculty(user.getFacultyId()));
            request.getRequestDispatcher("/jsp/faculty/leaveStatus.jsp").forward(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/student");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = getLoggedInUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");

        if ("apply".equals(action) && user.getFacultyId() != null) {
            leaveService.applyLeave(user.getFacultyId(), request.getParameter("date"));
            response.sendRedirect(request.getContextPath() + "/faculty");
            return;
        }

        if ("update".equals(action) && Constants.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
            String message = leaveService.updateLeaveStatus(
                    Integer.parseInt(request.getParameter("leaveId")),
                    request.getParameter("status")
            );
            response.sendRedirect(request.getContextPath() + "/leave?success=" +
                    java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8));
            return;
        }

        response.sendRedirect(request.getContextPath() + "/login");
    }
}
