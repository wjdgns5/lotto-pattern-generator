<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>?듦퀎 ??쒕낫??/title>
    <link rel="stylesheet" href="/css/app.css">
</head>
<body class="stats-page">
<main>
    <%-- 통계 화면: 저장된 당첨번호를 기반으로 번호 빈도, 홀짝 비율, 합계 구간을 시각화합니다. --%>
    <header>
        <div>
            <h1>?듦퀎 ??쒕낫??/h1>
            <p>??λ맂 ${stats.drawCount}?뚯감 湲곗? 踰덊샇 遺꾪룷? ?⑦꽩???쒓컖?뷀뻽?듬땲??</p>
        </div>
        <a href="/generate">?ъ슜???붾㈃</a>
    </header>

    <div class="kpis">
        <div class="kpi"><p>遺꾩꽍 ?뚯감</p><strong>${stats.drawCount}</strong></div>
        <div class="kpi"><p>理쒕떎 異쒗쁽 踰덊샇</p><strong>${stats.topNumbers[0].number}踰?/strong></div>
        <div class="kpi"><p>理쒕떎 異쒗쁽 ?잛닔</p><strong>${stats.maxNumberCount}??/strong></div>
    </div>

    <section class="panel wide">
        <h2>踰덊샇蹂?異쒗쁽 鍮덈룄</h2>
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
            <h2>?곸쐞 異쒗쁽 踰덊샇</h2>
            <c:forEach var="item" items="${stats.topNumbers}">
                <div class="row">
                    <strong>${item.number}踰?/strong>
                    <div class="mini-track"><div class="mini-fill blue" style="width:${item.count * 100 / stats.maxNumberCount}%"></div></div>
                    <span>${item.count}??/span>
                </div>
            </c:forEach>
        </section>
        <section class="panel">
            <h2>?섏쐞 異쒗쁽 踰덊샇</h2>
            <c:forEach var="item" items="${stats.bottomNumbers}">
                <div class="row">
                    <strong>${item.number}踰?/strong>
                    <div class="mini-track"><div class="mini-fill" style="width:${item.count * 100 / stats.maxNumberCount}%"></div></div>
                    <span>${item.count}??/span>
                </div>
            </c:forEach>
        </section>
        <section class="panel">
            <h2>?吏?鍮꾩쑉</h2>
            <c:forEach var="entry" items="${stats.oddEvenRatios}">
                <div class="row">
                    <strong>${entry.key}</strong>
                    <div class="mini-track"><div class="mini-fill" style="width:${entry.value * 100 / stats.drawCount}%"></div></div>
                    <span>${entry.value}??/span>
                </div>
            </c:forEach>
        </section>
        <section class="panel">
            <h2>?⑷퀎 援ш컙</h2>
            <c:forEach var="entry" items="${stats.sumRanges}">
                <div class="row">
                    <strong>${entry.key}</strong>
                    <div class="mini-track"><div class="mini-fill warn" style="width:${entry.value * 100 / stats.drawCount}%"></div></div>
                    <span>${entry.value}??/span>
                </div>
            </c:forEach>
        </section>
    </div>
</main>
</body>
</html>

