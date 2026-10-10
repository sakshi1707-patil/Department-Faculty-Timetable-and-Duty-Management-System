<%@ include file="/jsp/common/header.jsp" %>
<div class="page-shell">
    <%@ include file="/jsp/common/navbar.jsp" %>
    <div class="card">
        <h2>My Duties</h2>
        <table>
            <thead>
            <tr>
                <th>Duty Type</th>
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
<%@ include file="/jsp/common/footer.jsp" %>

