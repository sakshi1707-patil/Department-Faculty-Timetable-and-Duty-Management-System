<%@ include file="/jsp/common/header.jsp" %>
<div class="page-shell">
    <%@ include file="/jsp/common/navbar.jsp" %>

    <div class="card">
        <h2>Assign Duty</h2>
        <form method="post" action="${pageContext.request.contextPath}/duty">
            <div class="grid">
                <div>
                    <label>Faculty ID</label>
                    <input type="number" name="facultyId" required>
                </div>
                <div>
                    <label>Duty Type</label>
                    <input type="text" name="dutyType" required>
                </div>
                <div>
                    <label>Date</label>
                    <input type="date" name="date" required>
                </div>
            </div>
            <button type="submit">Assign Duty</button>
        </form>
    </div>

    <div class="card">
        <h2>Duty List</h2>
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Faculty</th>
                <th>Duty Type</th>
                <th>Date</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${dutyList}" var="duty">
                <tr>
                    <td>${duty.dutyId}</td>
                    <td>${duty.facultyName}</td>
                    <td>${duty.dutyType}</td>
                    <td>${duty.date}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</div>
<%@ include file="/jsp/common/footer.jsp" %>

