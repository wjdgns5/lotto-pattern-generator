<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>통계 대시보드</title>
    <link rel="stylesheet" href="/css/app.css">
</head>
<body class="stats-page">
<main>
    <%-- 통계 화면: 저장된 당첨번호를 기반으로 번호 빈도, 홀짝 비율, 합계 구간을 시각화합니다. --%>
    <header>
        <div>
            <h1>통계 대시보드</h1>
            <p>저장된 ${stats.drawCount}회차 기준 번호 분포와 패턴을 시각화했습니다.</p>
        </div>
        <a href="/generate">사용자 화면</a>
    </header>

    <div class="kpis">
        <div class="kpi"><p>분석 회차</p><strong>${stats.drawCount}</strong></div>
        <div class="kpi"><p>최다 출현 번호</p><strong>${stats.topNumbers[0].number}번</strong></div>
        <div class="kpi"><p>최다 출현 횟수</p><strong>${stats.maxNumberCount}회</strong></div>
    </div>

    <section class="panel wide">
        <h2>번호별 출현 빈도</h2>
        <div class="number-chart">
            <c:forEach var="item" items="${stats.allNumbers}">
                <div class="bar-cell">
                    <div class="bar">
                        <div class="bar-fill" style="height:${item.count * 100 / stats.maxNumberCount}%"></div>
                    </div>
                    <div class="bar-label">${item.number}</div>
                    <div class="bar-count">${item.count}</div>
                </div>
            </c:forEach>
        </div>
    </section>

    <div class="grid">
        <section class="panel">
            <h2>상위 출현 번호</h2>
            <c:forEach var="item" items="${stats.topNumbers}">
                <div class="row">
                    <strong>${item.number}번</strong>
                    <div class="mini-track"><div class="mini-fill blue" style="width:${item.count * 100 / stats.maxNumberCount}%"></div></div>
                    <span>${item.count}회</span>
                </div>
            </c:forEach>
        </section>
        <section class="panel">
            <h2>하위 출현 번호</h2>
            <c:forEach var="item" items="${stats.bottomNumbers}">
                <div class="row">
                    <strong>${item.number}번</strong>
                    <div class="mini-track"><div class="mini-fill" style="width:${item.count * 100 / stats.maxNumberCount}%"></div></div>
                    <span>${item.count}회</span>
                </div>
            </c:forEach>
        </section>
        <section class="panel">
            <h2>홀짝 비율</h2>
            <c:forEach var="entry" items="${stats.oddEvenRatios}">
                <div class="row">
                    <strong>${entry.key}</strong>
                    <div class="mini-track"><div class="mini-fill" style="width:${entry.value * 100 / stats.drawCount}%"></div></div>
                    <span>${entry.value}회</span>
                </div>
            </c:forEach>
        </section>
        <section class="panel">
            <h2>합계 구간</h2>
            <c:forEach var="entry" items="${stats.sumRanges}">
                <div class="row">
                    <strong>${entry.key}</strong>
                    <div class="mini-track"><div class="mini-fill warn" style="width:${entry.value * 100 / stats.drawCount}%"></div></div>
                    <span>${entry.value}회</span>
                </div>
            </c:forEach>
        </section>
    </div>
</main>
</body>
</html>
