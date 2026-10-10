<%@ include file="/jsp/common/header.jsp" %>
<div class="page-shell">
    <%@ include file="/jsp/common/navbar.jsp" %>

    <c:if test="${not empty param.success}">
        <div class="message success">${param.success}</div>
    </c:if>

    <div class="card">
        <h2>Weekly Timetable View</h2>
        <form method="get" action="${pageContext.request.contextPath}/timetable">
            <div class="grid">
                <div>
                    <label>Week Start Date</label>
                    <input type="date" name="weekStartDate" value="${weekStartDate}" required>
                </div>
                <div>
                    <label>Division</label>
                    <select name="divisionId">
                        <option value="">All Divisions</option>
                        <c:forEach items="${divisionList}" var="division">
                            <c:if test="${division.divisionCode ne 'TYDS'}">
                                <option value="${division.divisionId}"
                                    ${selectedDivisionId == division.divisionId ? 'selected' : ''}>
                                    ${division.divisionCode}
                                </option>
                            </c:if>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <button type="submit">View Timetable</button>
            <c:if test="${not empty selectedDivisionId}">
                <a class="btn-link" href="${pageContext.request.contextPath}/timetable/pdf?divisionId=${selectedDivisionId}&weekStartDate=${weekStartDate}">
                    Download PDF
                </a>
            </c:if>
        </form>
    </div>

    <c:forEach items="${divisionList}" var="division">
        <c:if test="${division.divisionCode ne 'TYDS'}">
        <c:if test="${empty selectedDivisionId or selectedDivisionId == division.divisionId}">
            <div class="card timetable-card">
                <div class="timetable-title">
                    ${division.divisionCode}
                    <span>(${division.classroom})</span>
                </div>
                <table class="official-timetable">
                    <thead>
                    <tr>
                        <th class="time-col">Time</th>
                        <c:forEach items="${dateKeys}" var="dateKey">
                            <th>${dayLabels[dateKey]}<br>
                                <span class="date-sub">${dateKey}</span>
                            </th>
                        </c:forEach>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${slotList}" var="slot">
                        <c:choose>
                            <c:when test="${slot.breakSlot}">
                                <tr class="break-row">
                                    <td class="time-col">${slot.slotLabel}</td>
                                    <td colspan="5">${slot.breakName}</td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td class="time-col">${slot.slotLabel}</td>
                                    <c:forEach items="${dateKeys}" var="dateKey">
                                        <c:set var="cell"
                                               value="${divisionGrid[division.divisionCode][slot.slotId][dateKey]}"/>
                                        <c:if test="${empty cell or not cell.hidden}">
                                            <td class="timetable-cell"
                                                rowspan="${empty cell ? 1 : cell.rowSpan}">
                                                <c:if test="${not empty cell}">
                                                    <c:forEach items="${cell.entries}" var="entry">
                                                        <div class="cell-entry">
                                                            <div class="entry-line">
                                                                <strong>${entry.subjectName}</strong>
                                                                <c:if test="${not empty entry.facultyName}">
                                                                    <span class="faculty-tag">(${entry.facultyName})</span>
                                                                </c:if>
                                                            </div>
                                                            <c:if test="${not empty entry.batchLabel}">
                                                                <div class="batch-tag">${entry.batchLabel}</div>
                                                            </c:if>
                                                            <c:if test="${not empty entry.roomNumber}">
                                                                <div class="room-tag">${entry.roomNumber}</div>
                                                            </c:if>
                                                            <c:if test="${not empty entry.notes
                                                                          and entry.notes ne 'THEORY'
                                                                          and entry.notes ne 'PRACTICAL'}">
                                                                <div class="cell-note">${entry.notes}</div>
                                                            </c:if>
                                                        </div>
                                                    </c:forEach>
                                                </c:if>
                                            </td>
                                        </c:if>
                                    </c:forEach>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:if>
        </c:if>
    </c:forEach>
</div>
<%@ include file="/jsp/common/footer.jsp" %>
