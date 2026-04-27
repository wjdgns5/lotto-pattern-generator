<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>회원가입</title>
    <style>
        body { margin:0; min-height:100vh; display:grid; place-items:center; font-family:Arial,"Malgun Gothic",sans-serif; background:#f5f7f9; color:#20242b; }
        main { width:min(420px, calc(100% - 28px)); padding:24px; border:1px solid #d8dde3; border-radius:8px; background:#fff; }
        h1 { margin:0 0 8px; } p { color:#66707c; line-height:1.6; } form { display:grid; gap:14px; margin-top:18px; }
        label { display:grid; gap:7px; font-weight:700; } input { min-height:44px; padding:8px 10px; border:1px solid #d8dde3; border-radius:6px; font:inherit; }
        button { min-height:42px; border:0; border-radius:6px; color:#fff; background:#0f766e; font-weight:700; cursor:pointer; }
        a { color:#115e59; font-weight:700; text-decoration:none; } .error { color:#b42318; }
    </style>
</head>
<body>
<main>
    <h1>회원가입</h1>
    <p>가입 계정은 USER 권한으로 생성됩니다.</p>
    <c:if test="${not empty errorMessage}"><p class="error">${errorMessage}</p></c:if>
    <form method="post" action="/register">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <label>아이디 <input type="text" name="username" value="${registerRequest.username}" autocomplete="username" required></label>
        <label>비밀번호 <input type="password" name="password" autocomplete="new-password" required></label>
        <label>비밀번호 확인 <input type="password" name="passwordConfirm" autocomplete="new-password" required></label>
        <button type="submit">가입하기</button>
    </form>
    <p style="margin-top:16px;"><a href="/login">로그인으로 돌아가기</a></p>
</main>
</body>
</html>
