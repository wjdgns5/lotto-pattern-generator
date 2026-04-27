<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>로그인</title>
    <style>
        body { margin:0; min-height:100vh; display:grid; place-items:center; font-family:Arial,"Malgun Gothic",sans-serif; background:#f5f7f9; color:#20242b; }
        main { width:min(420px, calc(100% - 28px)); padding:24px; border:1px solid #d8dde3; border-radius:8px; background:#fff; }
        h1 { margin:0 0 8px; } p { color:#66707c; line-height:1.6; } form { display:grid; gap:14px; margin-top:18px; }
        label { display:grid; gap:7px; font-weight:700; } input { min-height:44px; padding:8px 10px; border:1px solid #d8dde3; border-radius:6px; font:inherit; }
        button { min-height:42px; border:0; border-radius:6px; color:#fff; background:#0f766e; font-weight:700; cursor:pointer; }
        a { color:#115e59; font-weight:700; text-decoration:none; } .notice { color:#115e59; } .error { color:#b42318; }
    </style>
</head>
<body>
<main>
    <h1>로그인</h1>
    <p>기본 관리자 계정은 <strong>admin / admin1234</strong> 입니다. 배포 전에는 반드시 변경하세요.</p>
    <c:if test="${param.error != null}"><p class="error">아이디 또는 비밀번호를 확인해 주세요.</p></c:if>
    <c:if test="${param.registered != null}"><p class="notice">회원가입이 완료되었습니다.</p></c:if>
    <c:if test="${param.logout != null}"><p class="notice">로그아웃되었습니다.</p></c:if>
    <form method="post" action="/login">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <label>아이디 <input type="text" name="username" autocomplete="username" required></label>
        <label>비밀번호 <input type="password" name="password" autocomplete="current-password" required></label>
        <button type="submit">로그인</button>
    </form>
    <p style="margin-top:16px;"><a href="/register">회원가입</a></p>
</main>
</body>
</html>
