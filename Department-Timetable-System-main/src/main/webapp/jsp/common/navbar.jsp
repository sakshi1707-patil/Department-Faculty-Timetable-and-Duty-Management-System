<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="card hero">
    <h1>Department Timetable System</h1>
    <p>Manage faculty, subjects, timetable, duties, and leaves.</p>
    <div class="nav">
        <c:if test="${sessionScope.loggedInUser.role == 'ADMIN'}">
            <a class="btn-link" href="${pageContext.request.contextPath}/admin">Admin Dashboard</a>
            <a class="btn-link" href="${pageContext.request.contextPath}/timetable">Timetable</a>
            <a class="btn-link" href="${pageContext.request.contextPath}/duty">Duties</a>
            <a class="btn-link" href="${pageContext.request.contextPath}/leave">Leaves</a>
        </c:if>
        <c:if test="${sessionScope.loggedInUser.role == 'FACULTY'}">
            <a class="btn-link" href="${pageContext.request.contextPath}/faculty">Faculty Dashboard</a>
            <a class="btn-link" href="${pageContext.request.contextPath}/duty">My Duties</a>
            <a class="btn-link" href="${pageContext.request.contextPath}/leave">My Leaves</a>
        </c:if>
        <c:if test="${sessionScope.loggedInUser.role == 'STUDENT'}">
            <a class="btn-link" href="${pageContext.request.contextPath}/student">Student Dashboard</a>
        </c:if>
        <a class="btn-link btn-secondary" href="${pageContext.request.contextPath}/logout">Logout</a>
    </div>
</div>

