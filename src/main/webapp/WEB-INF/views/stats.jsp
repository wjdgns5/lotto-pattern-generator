<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>통계 대시보드</title>
    <style>
        :root { --ink:#20242b; --muted:#66707c; --line:#d8dde3; --paper:#f5f7f9; --accent:#0f766e; --accent-2:#2563eb; --warn:#b45309; --white:#fff; }
        * { box-sizing:border-box; }
        body { margin:0; font-family:Arial,"Malgun Gothic",sans-serif; background:var(--paper); color:var(--ink); }
        main { width:min(1120px, calc(100% - 28px)); margin:0 auto; padding:28px 0 48px; }
        header { display:flex; align-items:flex-start; justify-content:space-between; gap:16px; margin-bottom:18px; }
        h1 { margin:0 0 8px; font-size:clamp(28px,8vw,40px); letter-spacing:0; }
        h2 { margin:0 0 12px; font-size:20px; letter-spacing:0; }
        p { margin:0; color:var(--muted); line-height:1.6; }
        a { color:#115e59; font-weight:700; text-decoration:none; }
        .grid { display:grid; gap:16px; }
        .panel { padding:16px; border:1px solid var(--line); border-radius:8px; background:var(--white); }
        .kpis { display:grid; gap:12px; margin:18px 0; }
        .kpi { padding:14px 16px; border:1px solid var(--line); border-radius:8px; background:var(--white); }
        .kpi strong { display:block; font-size:28px; }
        .number-chart { display:grid; grid-template-columns:repeat(9, minmax(0, 1fr)); gap:8px; align-items:end; min-height:220px; padding-top:10px; }
        .bar-cell { display:grid; gap:6px; align-items:end; min-width:0; }
        .bar { display:grid; align-items:end; height:160px; border-radius:6px 6px 3px 3px; background:#e7eef7; overflow:hidden; }
        .bar-fill { min-height:4px; border-radius:6px 6px 0 0; background:linear-gradient(180deg, var(--accent-2), var(--accent)); }
        .bar-label, .bar-count { text-align:center; font-size:12px; color:var(--muted); }
        .bar-label { color:var(--ink); font-weight:700; }
        .row { display:grid; grid-template-columns:56px minmax(0,1fr) 54px; align-items:center; gap:10px; padding:8px 0; border-bottom:1px solid #eef0f2; }
        .row:last-child { border-bottom:0; }
        .mini-track { height:10px; border-radius:999px; background:#eef0f2; overflow:hidden; }
        .mini-fill { height:100%; border-radius:999px; background:var(--accent); }
        .mini-fill.blue { background:var(--accent-2); }
        .mini-fill.warn { background:var(--warn); }
        @media (min-width:760px) { .kpis { grid-template-columns:repeat(3,minmax(0,1fr)); } .grid { grid-template-columns:1.2fr .8fr; } .wide { grid-column:1 / -1; } }
        @media (max-width:680px) { header { display:grid; } .number-chart { grid-template-columns:repeat(5,minmax(0,1fr)); } }
    </style>
</head>
<body>
<main>
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
