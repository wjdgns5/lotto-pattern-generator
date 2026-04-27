<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>濡쒓렇??/title>
    <link rel="stylesheet" href="/css/app.css">
</head>
<body class="auth-page">
<main>
    <%-- 로그인 화면: 실제 인증 처리는 Spring Security가 담당합니다. --%>
    <h1>濡쒓렇??/h1>
    <p>湲곕낯 愿由ъ옄 怨꾩젙? <strong>admin / admin1234</strong> ?낅땲?? 諛고룷 ?꾩뿉??諛섎뱶??蹂寃쏀븯?몄슂.</p>
    <c:if test="${param.error != null}"><p class="error-text">?꾩씠???먮뒗 鍮꾨?踰덊샇瑜??뺤씤??二쇱꽭??</p></c:if>
    <c:if test="${param.registered != null}"><p class="notice-text">?뚯썝媛?낆씠 ?꾨즺?섏뿀?듬땲??</p></c:if>
    <c:if test="${param.logout != null}"><p class="notice-text">濡쒓렇?꾩썐?섏뿀?듬땲??</p></c:if>
    <form method="post" action="/login">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <label>?꾩씠??<input type="text" name="username" autocomplete="username" required></label>
        <label>鍮꾨?踰덊샇 <input type="password" name="password" autocomplete="current-password" required></label>
        <button type="submit">濡쒓렇??/button>
    </form>
    <p class="auth-link"><a href="/register">?뚯썝媛??/a></p>
</main>
</body>
</html>


