<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>로그인</title>
    <link rel="stylesheet" href="/css/app.css">
</head>
<body class="auth-page">
<main>
    <%-- 로그인 화면: 실제 인증 처리는 Spring Security가 담당합니다. --%>
    <h1>로그인</h1>
    <p>기본 관리자 계정은 <strong>admin / admin1234</strong> 입니다. 배포 전에는 반드시 변경하세요.</p>
    <c:if test="${param.error != null}"><p class="error-text">아이디 또는 비밀번호를 확인해 주세요.</p></c:if>
    <c:if test="${param.registered != null}"><p class="notice-text">회원가입이 완료되었습니다.</p></c:if>
    <c:if test="${param.logout != null}"><p class="notice-text">로그아웃되었습니다.</p></c:if>
    <form method="post" action="/login">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <label>아이디 <input type="text" name="username" autocomplete="username" required></label>
        <label>비밀번호 <input type="password" name="password" autocomplete="current-password" required></label>
        <button type="submit">로그인</button>
    </form>
    <p class="auth-link"><a href="/register">회원가입</a></p>
</main>
</body>
</html>
