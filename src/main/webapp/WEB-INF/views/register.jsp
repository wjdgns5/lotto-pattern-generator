<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>?뚯썝媛??/title>
    <link rel="stylesheet" href="/css/app.css">
</head>
<body class="auth-page">
<main>
    <%-- 회원가입 화면: 입력값은 AuthController를 거쳐 UserAccountService에서 검증/저장됩니다. --%>
    <h1>?뚯썝媛??/h1>
    <p>媛??怨꾩젙? USER 沅뚰븳?쇰줈 ?앹꽦?⑸땲??</p>
    <c:if test="${not empty errorMessage}"><p class="error-text">${errorMessage}</p></c:if>
    <form method="post" action="/register">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <label>?꾩씠??<input type="text" name="username" value="${registerRequest.username}" autocomplete="username" required></label>
        <label>鍮꾨?踰덊샇 <input type="password" name="password" autocomplete="new-password" required></label>
        <label>鍮꾨?踰덊샇 ?뺤씤 <input type="password" name="passwordConfirm" autocomplete="new-password" required></label>
        <button type="submit">媛?낇븯湲?/button>
    </form>
    <p class="auth-link"><a href="/login">濡쒓렇?몄쑝濡??뚯븘媛湲?/a></p>
</main>
</body>
</html>


