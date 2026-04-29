<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>회원가입</title>
    <link rel="stylesheet" href="/css/app.css">
</head>
<body class="auth-page">
<main>
    <%-- 회원가입 화면: 입력값은 AuthController를 거쳐 UserAccountService에서 검증/저장됩니다. --%>
    <h1>회원가입</h1>
    <p>가입 계정은 USER 권한으로 생성됩니다.</p>
    <c:if test="${not empty errorMessage}"><p class="error-text">${errorMessage}</p></c:if>
    <form method="post" action="/register">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <label>아이디 <input type="text" name="username" value="${registerRequest.username}" autocomplete="username" required></label>
        <label>비밀번호 <input type="password" name="password" autocomplete="new-password" required></label>
        <label>비밀번호 확인 <input type="password" name="passwordConfirm" autocomplete="new-password" required></label>
        <button type="submit">가입하기</button>
    </form>
    <p class="auth-link"><a href="/login">로그인으로 돌아가기</a></p>
</main>
</body>
</html>
