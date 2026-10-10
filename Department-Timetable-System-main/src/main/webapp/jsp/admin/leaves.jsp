<%@ include file="/jsp/common/header.jsp" %>
<div class="page-shell">
    <%@ include file="/jsp/common/navbar.jsp" %>

    <c:if test="${not empty param.success}">
        <div class="message success">${param.success}</div>
    </c:if>

    <div class="card">
        <h2>Leave Requests</h2>
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Faculty</th>
                <th>Date</th>
                <th>Status</th>
                <th>Action</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${leaveList}" var="leave">
                <tr>
                    <td>${leave.leaveId}</td>
                    <td>${leave.facultyName}</td>
                    <td>${leave.date}</td>
                    <td>${leave.status}</td>
                    <td>
                        <form method="post" action="${pageContext.request.contextPath}/leave">
                            <input type="hidden" name="action" value="update">
                            <input type="hidden" name="leaveId" value="${leave.leaveId}">
                            <select name="status">
                                <option value="Approved" ${leave.status == 'Approved' ? 'selected' : ''}>Approved</option>
                                <option value="Rejected" ${leave.status == 'Rejected' ? 'selected' : ''}>Rejected</option>
                                <option value="Pending" ${leave.status == 'Pending' ? 'selected' : ''}>Pending</option>
                            </select>
                            <button type="submit">Update</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</div>
<%@ include file="/jsp/common/footer.jsp" %>
