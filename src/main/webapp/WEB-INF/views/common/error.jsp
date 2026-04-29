<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>?ㅻ쪟 ?덈궡</title>
    <link rel="stylesheet" href="/css/app.css">
</head>
<body class="error-page">
<main>
    <%-- 공통 오류 화면: ControllerAdvice와 ErrorPageController가 전달한 메시지를 표시합니다. --%>
    <div class="status">${status}</div>
    <h1>${title}</h1>
    <p>${message}</p>
    <a href="${returnUrl}" class="primary-link">${returnText}</a>
</main>
</body>
</html>


