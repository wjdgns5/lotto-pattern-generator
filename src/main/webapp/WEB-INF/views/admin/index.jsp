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
            <h1>관리자</h1>
            <p>당첨번호 저장, CSV 업로드, 외부 API 업데이트와 관리자 감사 로그를 관리합니다.</p>
        </div>
        <a href="/generate" class="nav-button">사용자 화면</a>
    </header>

    <div class="summary">
        <p>저장된 당첨번호: ${winningDrawCount}회차</p>
        <c:if test="${not empty latestWinningDraw}">
            <p>최신 저장 회차: ${latestWinningDraw.drawNumber}회차 ${latestWinningDraw.drawDate}</p>
        </c:if>
    </div>
    <c:if test="${not empty winningNumberMessage}"><div class="notice">${winningNumberMessage}</div></c:if>
    <c:if test="${not empty winningNumberErrorMessage}"><div class="error">${winningNumberErrorMessage}</div></c:if>

    <h2>CSV 일괄 업로드</h2>
    <form method="post" action="/admin/winning-numbers/upload" enctype="multipart/form-data">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <label class="full-row">CSV 파일 <input type="file" name="file" accept=".csv" required></label>
        <div class="actions"><button type="submit">CSV 업로드</button></div>
    </form>

    <h2>외부 API 업데이트</h2>
    <form method="post" action="/admin/winning-numbers/external-update">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <div class="actions"><button type="submit">최신 회차 가져오기</button></div>
    </form>

    <h2>당첨번호 수동 저장</h2>
    <form method="post" action="/admin/winning-numbers">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <label>회차 <input type="number" name="drawNumber" min="1" required></label>
        <label>추첨일 <input type="date" name="drawDate"></label>
        <label>번호 1 <input type="number" name="number1" min="1" max="45" required></label>
        <label>번호 2 <input type="number" name="number2" min="1" max="45" required></label>
        <label>번호 3 <input type="number" name="number3" min="1" max="45" required></label>
        <label>번호 4 <input type="number" name="number4" min="1" max="45" required></label>
        <label>번호 5 <input type="number" name="number5" min="1" max="45" required></label>
        <label>번호 6 <input type="number" name="number6" min="1" max="45" required></label>
        <label>보너스 <input type="number" name="bonusNumber" min="1" max="45"></label>
        <div class="actions"><button type="submit">저장</button></div>
    </form>

    <h2>관리자 감사 로그</h2>
    <c:choose>
        <c:when test="${empty auditLogs}">
            <div class="audit-log"><p>아직 기록된 관리자 작업이 없습니다.</p></div>
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
        <h2>최근 저장 회차</h2>
        <div class="history">
            <c:forEach var="draw" items="${latestWinningDraws}">
                <div class="draw">
                    <div class="meta"><span>${draw.drawNumber}회차</span><span>${empty draw.drawDate ? '-' : draw.drawDate}</span><span>보너스 ${empty draw.bonusNumber ? '-' : draw.bonusNumber}</span></div>
                    <div class="balls"><c:forEach var="number" items="${draw.numbers}"><span class="ball">${number}</span></c:forEach></div>
                </div>
            </c:forEach>
        </div>
    </c:if>
</main>
</body>
</html>
