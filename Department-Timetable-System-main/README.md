# Department Timetable System

Java dynamic web application for IntelliJ IDEA + Tomcat using JSP, Servlets, JDBC, and MySQL.

## Stack

- Java 11
- Maven WAR project
- Tomcat 9+
- MySQL 8
- JSP + JSTL + Servlets

## Import In IntelliJ

1. Open the project folder as a Maven project.
2. Make sure Tomcat is configured in IntelliJ.
3. Create the MySQL database using [`sql/schema.sql`](/C:/Users/VAISHNAVI/OneDrive/Desktop/文档/New%20project/sql/schema.sql).
4. Update DB values in [`src/main/java/com/timetable/util/Constants.java`](/C:/Users/VAISHNAVI/OneDrive/Desktop/文档/New%20project/src/main/java/com/timetable/util/Constants.java).
5. Add a Tomcat run configuration and deploy the generated WAR exploded artifact.

## Default Login

- Admin: `admin1 / admin123`
- Faculty: `faculty1 / fac123`
- Student: `student1 / stud123`

## Notes

- Your package folders in Java should be `com/timetable/...`, not `com.timetable...` as folder names.
- This starter converts the console menu flow into servlet routes and JSP pages.

