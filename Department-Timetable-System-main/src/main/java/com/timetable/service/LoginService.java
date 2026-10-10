package com.timetable.service;

import com.timetable.dao.LoginDAO;
import com.timetable.model.User;

public class LoginService {
    private final LoginDAO loginDAO = new LoginDAO();

    public User login(String username, String password) {
        return loginDAO.findByCredentials(username, password);
    }
}

