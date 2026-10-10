<%@ include file="/jsp/common/header.jsp" %>
<div class="page-shell">
    <%@ include file="/jsp/common/navbar.jsp" %>

    <c:if test="${not empty param.success}">
        <div class="message success">${param.success}</div>
    </c:if>

    <div class="grid">
        <div class="card">
            <h2>Apply Leave</h2>
            <form method="post" action="${pageContext.request.contextPath}/leave">
                <input type="hidden" name="action" value="apply">
                <label>Date</label>
                <input type="date" name="date" required>
                <button type="submit">Apply</button>
            </form>
        </div>

        <div class="card">
            <h2>My Duties</h2>
            <table>
                <thead>
                <tr>
                    <th>Type</th>
                    <th>Date</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${dutyList}" var="duty">
                    <tr>
                        <td>${duty.dutyType}</td>
                        <td>${duty.date}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <div class="card">
        <h2>My Weekly Timetable</h2>
        <form method="get" action="${pageContext.request.contextPath}/faculty">
            <label>Week Start Date</label>
            <input type="date" name="weekStartDate" value="${weekStartDate}" required>
            <button type="submit">View My Slots</button>
        </form>
        <table>
            <thead>
            <tr>
                <th>Division</th>
                <th>Date</th>
                <th>Day</th>
                <th>Time Slot</th>
                <th>Subject</th>
                <th>Room</th>
                <th>Batch</th>
                <th>Notes</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${timetableList}" var="entry">
                <tr>
                    <td>${entry.divisionCode}</td>
                    <td>${entry.scheduleDate}</td>
                    <td>${entry.dayName}</td>
                    <td>${entry.timeSlot}</td>
                    <td>${entry.subjectName}</td>
                    <td>${entry.roomNumber}</td>
                    <td>${entry.batchLabel}</td>
                    <td>${entry.notes}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>

    <div class="card">
        <h2>My Leave Status</h2>
        <table>
            <thead>
            <tr>
                <th>Date</th>
                <th>Status</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${leaveList}" var="leave">
                <tr>
                    <td>${leave.date}</td>
                    <td>${leave.status}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</div>
<%@ include file="/jsp/common/footer.jsp" %>
