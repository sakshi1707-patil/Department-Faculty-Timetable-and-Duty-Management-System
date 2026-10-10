<%@ include file="/jsp/common/header.jsp" %>
<div class="page-shell">
    <%@ include file="/jsp/common/navbar.jsp" %>
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

