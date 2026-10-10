package com.timetable.util;

public final class Constants {
    public static final String DB_URL = "jdbc:mysql://localhost:3306/college_timetable_system?useSSL=false&serverTimezone=UTC";
    public static final String DB_USERNAME = "root";
    public static final String DB_PASSWORD = "root123";

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_FACULTY = "FACULTY";
    public static final String ROLE_STUDENT = "STUDENT";

    private Constants() {
    }
}

