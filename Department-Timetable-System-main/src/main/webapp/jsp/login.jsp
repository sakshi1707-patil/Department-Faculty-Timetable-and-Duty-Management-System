<%@ include file="/jsp/common/header.jsp" %>
<div class="page-shell">
    <div class="card login-card">
        <h2>Login</h2>
        <p>Use your admin, faculty, or student account to continue.</p>

        <c:if test="${not empty error}">
            <div class="message error">${error}</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/login">
            <label for="username">Username</label>
            <input type="text" id="username" name="username" required>

            <label for="password">Password</label>
            <input type="password" id="password" name="password" required>

            <button type="submit">Login</button>
        </form>
    </div>
</div>
<%@ include file="/jsp/common/footer.jsp" %>

