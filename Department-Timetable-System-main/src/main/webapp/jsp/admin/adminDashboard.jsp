<%@ include file="/jsp/common/header.jsp" %>
<div class="page-shell">
    <%@ include file="/jsp/common/navbar.jsp" %>

    <c:if test="${not empty param.success}">
        <div class="message success">${param.success}</div>
    </c:if>

    <div class="grid">
        <div class="card">
            <h2>Add Faculty</h2>
            <form method="post" action="${pageContext.request.contextPath}/admin">
                <input type="hidden" name="action" value="addFaculty">
                <label>Name</label>
                <input type="text" name="name" required>
                <label>Department</label>
                <input type="text" name="department" required>
                <label>Email</label>
                <input type="email" name="email" required>
                <button type="submit">Save Faculty</button>
            </form>
        </div>

        <div class="card">
            <h2>Add Subject / Activity</h2>
            <form method="post" action="${pageContext.request.contextPath}/admin">
                <input type="hidden" name="action" value="addSubject">
                <label>Division</label>
                <select name="divisionId" required>
                    <c:forEach items="${divisionList}" var="division">
                        <option value="${division.divisionId}">${division.divisionCode}</option>
                    </c:forEach>
                </select>
                <label>Subject Name</label>
                <input type="text" name="subjectName" required>
                <label>Hours Per Week</label>
                <input type="number" name="hoursPerWeek" min="1" required>
                <label>Faculty</label>
                <select name="facultyId" required>
                    <c:forEach items="${facultyList}" var="faculty">
                        <option value="${faculty.facultyId}">${faculty.name}</option>
                    </c:forEach>
                </select>
                <label>Activity Type</label>
                <select name="activityType" required>
                    <option value="THEORY">Theory</option>
                    <option value="PRACTICAL">Practical</option>
                    <option value="MENTORING">Mentoring</option>
                    <option value="TNP">T&amp;P</option>
                </select>
                <label>Preferred Room Type</label>
                <select name="preferredRoomType" required>
                    <option value="CLASSROOM">Classroom</option>
                    <option value="LAB">Lab</option>
                </select>
                <label>Batch Label</label>
                <input type="text" name="batchLabel" placeholder="A1/A2 or leave blank">
                <button type="submit">Save Subject</button>
            </form>
        </div>
    </div>

    <div class="card">
        <h2>Generate Weekly Timetable</h2>
        <form method="post" action="${pageContext.request.contextPath}/admin">
            <input type="hidden" name="action" value="autoGenerate">
            <label>Week Start Date</label>
            <input type="date" name="weekStartDate" value="${defaultWeekStart}" required>
            <button type="submit">Auto Generate Timetable</button>
        </form>
        <p>Generator uses division-wise subjects, fixed slots, room type matching, and approved leave dates.</p>
    </div>

    <div class="card">
        <h2>Admin Alerts</h2>
        <table>
            <thead>
            <tr>
                <th>Division</th>
                <th>Date</th>
                <th>Time Slot</th>
                <th>Subject</th>
                <th>Current Faculty</th>
                <th>Note</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${empty attentionEntries}">
                    <tr>
                        <td>SYAIML-A</td>
                        <td>2026-05-19</td>
                        <td>09:15-10:15</td>
                        <td>OOP</td>
                        <td>Prof. S. D. Jadhav</td>
                        <td><span class="cell-note">Example: Replacement required: Prof. S. D. Jadhav on leave</span></td>
                    </tr>
                    <tr>
                        <td>SYAIML-B</td>
                        <td>2026-05-20</td>
                        <td>11:45-12:45</td>
                        <td>DAA</td>
                        <td>Prof. K. M. Thorat</td>
                        <td><span class="cell-note">Example: Replacement: Prof. M. M. Gaikwad for Prof. K. M. Thorat</span></td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${attentionEntries}" var="entry">
                        <tr>
                            <td>${entry.divisionCode}</td>
                            <td>${entry.scheduleDate}</td>
                            <td>${entry.timeSlot}</td>
                            <td>${entry.subjectName}</td>
                            <td>${entry.facultyName}</td>
                            <td>${entry.notes}</td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
    </div>

    <div class="card">
        <h2>Divisions</h2>
        <table>
            <thead>
            <tr>
                <th>Code</th>
                <th>Year</th>
                <th>Section</th>
                <th>Classroom</th>
                <th>Coordinator</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${divisionList}" var="division">
                <tr>
                    <td>${division.divisionCode}</td>
                    <td>${division.yearName}</td>
                    <td>${division.sectionName}</td>
                    <td>${division.classroom}</td>
                    <td>${division.coordinatorName}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>

    <div class="card">
        <h2>Subject List</h2>
        <table>
            <thead>
            <tr>
                <th>Division</th>
                <th>Subject</th>
                <th>Hours/Week</th>
                <th>Faculty</th>
                <th>Type</th>
                <th>Room Type</th>
                <th>Batch</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${subjectList}" var="subject">
                <tr>
                    <td>${subject.divisionCode}</td>
                    <td>${subject.subjectName}</td>
                    <td>${subject.hoursPerWeek}</td>
                    <td>${subject.facultyName}</td>
                    <td>${subject.activityType}</td>
                    <td>${subject.preferredRoomType}</td>
                    <td>${subject.batchLabel}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>

    <div class="card">
        <h2>Recent Leave Requests</h2>
        <table>
            <thead>
            <tr>
                <th>Faculty</th>
                <th>Date</th>
                <th>Status</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${leaveList}" var="leave">
                <tr>
                    <td>${leave.facultyName}</td>
                    <td>${leave.date}</td>
                    <td>${leave.status}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</div>
<%@ include file="/jsp/common/footer.jsp" %>
