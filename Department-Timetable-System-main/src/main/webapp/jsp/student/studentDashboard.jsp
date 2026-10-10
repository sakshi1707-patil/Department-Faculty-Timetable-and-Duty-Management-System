<%@ include file="/jsp/common/header.jsp" %>
<div class="page-shell">
    <%@ include file="/jsp/common/navbar.jsp" %>

    <div class="card">
        <c:choose>
            <c:when test="${empty division}">
                <h2>Student Timetable</h2>
                <div class="message error">
                    Your student account is not assigned to any division. Ask admin to set division_id for your username.
                </div>
            </c:when>
            <c:otherwise>
                <h2>${division.divisionCode} Weekly Timetable</h2>
                <p>Classroom: ${division.classroom} | Coordinator: ${division.coordinatorName}</p>
            </c:otherwise>
        </c:choose>
        <form method="get" action="${pageContext.request.contextPath}/student">
            <div class="grid">
                <div>
                    <label>Division</label>
                    <select name="divisionId" required>
                        <c:forEach items="${divisionList}" var="divisionOption">
                            <option value="${divisionOption.divisionId}" ${selectedDivisionId == divisionOption.divisionId ? 'selected' : ''}>
                                ${divisionOption.divisionCode}
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div>
                    <label>Week Start Date</label>
                    <input type="date" name="weekStartDate" value="${weekStartDate}" required>
                </div>
            </div>
            <button type="submit">View Timetable</button>
        </form>
        <c:if test="${empty slotList}">
            <div class="message error">No time slots found. Please load the timetable database schema first.</div>
        </c:if>
        <table>
            <thead>
            <tr>
                <th>Time</th>
                <c:forEach items="${dateKeys}" var="dateKey">
                    <th>${dayLabels[dateKey]}<br>${dateKey}</th>
                </c:forEach>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${slotList}" var="slot">
                <c:choose>
                    <c:when test="${slot.breakSlot}">
                        <tr>
                            <td>${slot.slotLabel}</td>
                            <td colspan="5">${slot.breakName}</td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td>${slot.slotLabel}</td>
                            <c:forEach items="${dateKeys}" var="dateKey">
                                <c:set var="entries" value="${timetableGrid[slot.slotId][dateKey]}"/>
                                <c:set var="splitCell" value="${not empty entries and fn:length(entries) gt 1}"/>
                                <td class="timetable-cell ${splitCell ? 'split-cell' : ''}">
                                    <c:forEach items="${entries}" var="entry">
                                        <div class="cell-entry">
                                            <strong>${entry.subjectName}</strong>
                                            <c:if test="${not empty entry.facultyName}">
                                                <br>${entry.facultyName}
                                            </c:if>
                                            <c:if test="${not empty entry.roomNumber}">
                                                <br>${entry.roomNumber}
                                            </c:if>
                                            <c:if test="${not empty entry.batchLabel}">
                                                <br><span class="batch-label">${entry.batchLabel}</span>
                                            </c:if>
                                            <c:if test="${not empty entry.notes and entry.notes ne 'THEORY' and entry.notes ne 'PRACTICAL'}">
                                                <br><span class="cell-note">${entry.notes}</span>
                                            </c:if>
                                        </div>
                                    </c:forEach>
                                </td>
                            </c:forEach>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
            </tbody>
        </table>
    </div>
</div>
<%@ include file="/jsp/common/footer.jsp" %>
