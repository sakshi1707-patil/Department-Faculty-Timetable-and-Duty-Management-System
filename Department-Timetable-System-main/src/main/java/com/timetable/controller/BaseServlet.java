package com.timetable.controller;

import com.timetable.model.User;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public abstract class BaseServlet extends HttpServlet {
    protected User getLoggedInUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (User) session.getAttribute("loggedInUser");
    }

    protected boolean isLoggedIn(HttpServletRequest request) {
        return getLoggedInUser(request) != null;
    }
}

