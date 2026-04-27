<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Lotto Admin</title>
    <link rel="stylesheet" href="/css/app.css">
</head>
<body class="admin-page">
<main>
    <%-- 관리자 화면: 당첨번호 수동 저장, CSV 업로드, 외부 API 업데이트, 감사 로그를 관리합니다. --%>
    <header>
        <div>
            <h1>愿由ъ옄</h1>
            <p>?뱀꺼踰덊샇 ??? CSV ?낅줈?? ?몃? API ?낅뜲?댄듃? 愿由ъ옄 媛먯궗 濡쒓렇瑜?愿由ы빀?덈떎.</p>
        </div>
        <a href="/generate" class="nav-button">?ъ슜???붾㈃</a>
    </header>

    <div class="summary">
        <p>??λ맂 ?뱀꺼踰덊샇: ${winningDrawCount}?뚯감</p>
        <c:if test="${not empty latestWinningDraw}">
            <p>理쒖떊 ????뚯감: ${latestWinningDraw.drawNumber}?뚯감 ${latestWinningDraw.drawDate}</p>
        </c:if>
    </div>
    <c:if test="${not empty winningNumberMessage}"><div class="notice">${winningNumberMessage}</div></c:if>
    <c:if test="${not empty winningNumberErrorMessage}"><div class="error">${winningNumberErrorMessage}</div></c:if>

    <h2>CSV ?쇨큵 ?낅줈??/h2>
    <form method="post" action="/admin/winning-numbers/upload" enctype="multipart/form-data">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <label class="full-row">CSV ?뚯씪 <input type="file" name="file" accept=".csv" required></label>
        <div class="actions"><button type="submit">CSV ?낅줈??/button></div>
    </form>

    <h2>?몃? API ?낅뜲?댄듃</h2>
    <form method="post" action="/admin/winning-numbers/external-update">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <div class="actions"><button type="submit">理쒖떊 ?뚯감 媛?몄삤湲?/button></div>
    </form>

    <h2>?뱀꺼踰덊샇 ?섎룞 ???/h2>
    <form method="post" action="/admin/winning-numbers">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <label>?뚯감 <input type="number" name="drawNumber" min="1" required></label>
        <label>異붿꺼??<input type="date" name="drawDate"></label>
        <label>踰덊샇 1 <input type="number" name="number1" min="1" max="45" required></label>
        <label>踰덊샇 2 <input type="number" name="number2" min="1" max="45" required></label>
        <label>踰덊샇 3 <input type="number" name="number3" min="1" max="45" required></label>
        <label>踰덊샇 4 <input type="number" name="number4" min="1" max="45" required></label>
        <label>踰덊샇 5 <input type="number" name="number5" min="1" max="45" required></label>
        <label>踰덊샇 6 <input type="number" name="number6" min="1" max="45" required></label>
        <label>蹂대꼫??<input type="number" name="bonusNumber" min="1" max="45"></label>
        <div class="actions"><button type="submit">???/button></div>
    </form>

    <h2>愿由ъ옄 媛먯궗 濡쒓렇</h2>
    <c:choose>
        <c:when test="${empty auditLogs}">
            <div class="audit-log"><p>?꾩쭅 湲곕줉??愿由ъ옄 ?묒뾽???놁뒿?덈떎.</p></div>
        </c:when>
        <c:otherwise>
            <div class="audit-grid">
                <c:forEach var="log" items="${auditLogs}">
                    <div class="audit-log">
                        <div class="meta"><span>${log.actionType}</span><span>${log.username}</span></div>
                        <strong>${log.detail}</strong>
                        <p>${log.createdAt}</p>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>

    <c:if test="${not empty latestWinningDraws}">
        <h2>理쒓렐 ????뚯감</h2>
        <div class="history">
            <c:forEach var="draw" items="${latestWinningDraws}">
                <div class="draw">
                    <div class="meta"><span>${draw.drawNumber}?뚯감</span><span>${empty draw.drawDate ? '-' : draw.drawDate}</span><span>蹂대꼫??${empty draw.bonusNumber ? '-' : draw.bonusNumber}</span></div>
                    <div class="balls"><c:forEach var="number" items="${draw.numbers}"><span class="ball">${number}</span></c:forEach></div>
                </div>
            </c:forEach>
        </div>
    </c:if>
</main>
</body>
</html>


