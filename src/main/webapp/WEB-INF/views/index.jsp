<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Lotto Pattern Generator</title>
    <style>
        :root { --ink:#20242b; --muted:#66707c; --line:#d8dde3; --paper:#f5f7f9; --accent:#0f766e; --accent-dark:#115e59; --danger:#b42318; --soft:#eef6f5; --white:#fff; }
        * { box-sizing: border-box; }
        body { margin:0; font-family: Arial, "Malgun Gothic", sans-serif; color:var(--ink); background:var(--paper); }
        main { width:min(1080px, calc(100% - 28px)); margin:0 auto; padding:28px 0 48px; }
        header { display:flex; align-items:flex-start; justify-content:space-between; gap:16px; margin-bottom:18px; }
        h1 { margin:0 0 8px; font-size:clamp(28px, 8vw, 42px); letter-spacing:0; }
        h2 { margin:26px 0 12px; font-size:22px; letter-spacing:0; }
        h3 { margin:0 0 10px; font-size:18px; letter-spacing:0; }
        p { margin:0; color:var(--muted); line-height:1.6; }
        a { color:var(--accent-dark); font-weight:700; text-decoration:none; }
        nav { display:flex; flex-wrap:wrap; gap:12px; justify-content:flex-end; }
        form { display:grid; grid-template-columns:repeat(2, minmax(0, 1fr)); gap:14px; margin:18px 0; padding:18px; border:1px solid var(--line); border-radius:8px; background:var(--white); }
        label { display:grid; gap:7px; font-weight:700; font-size:14px; }
        input { width:100%; min-height:44px; padding:8px 10px; border:1px solid var(--line); border-radius:6px; font:inherit; }
        button { min-height:42px; border:0; border-radius:6px; color:var(--white); background:var(--accent); font-weight:700; cursor:pointer; }
        button:hover { background:var(--accent-dark); }
        .secondary { color:var(--accent-dark); border:1px solid var(--accent); background:var(--white); }
        .secondary:hover { color:var(--white); }
        .wide, .actions { grid-column:1 / -1; }
        .actions { display:grid; grid-template-columns:1fr; }
        .summary, .notice, .error, .panel, .candidate, .history-item { margin:14px 0; padding:14px 16px; border:1px solid var(--line); border-radius:8px; background:var(--white); }
        .error { color:var(--danger); border-color:#f1b8b2; }
        .grid { display:grid; gap:16px; }
        .preset-list { display:grid; gap:10px; }
        .preset-row { display:grid; grid-template-columns:minmax(0, 1fr) auto; align-items:center; gap:10px; padding:10px 0; border-bottom:1px solid #eef0f2; }
        .preset-row:last-child { border-bottom:0; }
        .preset-delete-form { display:block; margin:0; padding:0; border:0; background:transparent; }
        .danger-button { min-height:34px; padding:0 12px; color:#b42318; border:1px solid #f1b8b2; background:#fff; }
        .danger-button:hover { color:#fff; background:#b42318; }
        .result-head { display:flex; align-items:center; justify-content:space-between; gap:12px; margin-top:24px; }
        .result-head h2 { margin:0; }
        .candidate-top { display:flex; align-items:flex-start; justify-content:space-between; gap:12px; }
        .balls { display:flex; flex-wrap:wrap; gap:8px; margin:2px 0 12px; }
        .ball { display:inline-grid; place-items:center; width:38px; height:38px; border-radius:50%; color:var(--white); background:var(--accent); font-weight:700; }
        .meta { display:flex; flex-wrap:wrap; gap:8px; color:var(--muted); font-size:14px; }
        .meta span { padding:4px 8px; border-radius:999px; background:var(--soft); }
        .toast { position:fixed; left:50%; bottom:18px; transform:translateX(-50%); padding:10px 14px; border-radius:999px; color:var(--white); background:var(--ink); opacity:0; pointer-events:none; transition:opacity .18s ease; }
        .toast.visible { opacity:1; }
        @media (min-width:860px) { form { grid-template-columns:repeat(6, minmax(0, 1fr)); } .wide { grid-column:span 2; } .actions { grid-column:span 1; } .grid { grid-template-columns:repeat(2, minmax(0, 1fr)); } }
        @media (max-width:560px) { main { width:min(100% - 20px, 1080px); padding-top:20px; } header, nav, .candidate-top, .result-head { display:grid; justify-content:stretch; } .secondary { width:100%; } .preset-row { grid-template-columns:1fr; } }
    </style>
</head>
<body>
<main>
    <header>
        <div>
            <h1>Lotto Pattern Generator</h1>
            <p>${username}님, 조건을 만족하는 패턴 기반 후보 조합을 생성합니다. 이 결과는 예측이나 당첨 보장이 아닙니다.</p>
        </div>
        <nav>
            <a href="/stats">통계</a>
            <c:if test="${isAdmin}">
                <a href="/admin">관리자</a>
            </c:if>
            <form method="post" action="/logout" style="display:inline; margin:0; padding:0; border:0; background:transparent;">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button type="submit" class="secondary">로그아웃</button>
            </form>
        </nav>
    </header>

    <div class="summary">
        <p>저장된 당첨번호: ${winningDrawCount}회차</p>
        <c:if test="${not empty latestWinningDraw}">
            <p>최신 저장 회차: ${latestWinningDraw.drawNumber}회차</p>
        </c:if>
    </div>

    <h2>후보 생성</h2>
    <form method="post" action="/generate">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <label>3:3 개수 <input type="number" name="threeOddThreeEvenCount" min="1" max="5" value="${generationRequest.threeOddThreeEvenCount}"></label>
        <label>4:2 개수 <input type="number" name="fourOddTwoEvenCount" min="1" max="5" value="${generationRequest.fourOddTwoEvenCount}"></label>
        <label>최소 합계 <input type="number" name="minSum" min="21" max="255" value="${generationRequest.minSum}"></label>
        <label>최대 합계 <input type="number" name="maxSum" min="21" max="255" value="${generationRequest.maxSum}"></label>
        <label class="wide">제외수 <input type="text" name="excludedNumbers" placeholder="예: 4, 10, 22" value="${generationRequest.excludedNumbers}"></label>
        <div class="actions"><button type="submit">후보 생성</button></div>
    </form>

    <form method="post" action="/presets">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <label class="wide">프리셋 이름 <input type="text" name="name" placeholder="예: 기본 10게임"></label>
        <input type="hidden" name="threeOddThreeEvenCount" value="${generationRequest.threeOddThreeEvenCount}">
        <input type="hidden" name="fourOddTwoEvenCount" value="${generationRequest.fourOddTwoEvenCount}">
        <input type="hidden" name="minSum" value="${generationRequest.minSum}">
        <input type="hidden" name="maxSum" value="${generationRequest.maxSum}">
        <input type="hidden" name="excludedNumbers" value="${generationRequest.excludedNumbers}">
        <div class="actions"><button type="submit" class="secondary">현재 조건 프리셋 저장</button></div>
    </form>

    <c:if test="${not empty errorMessage}"><div class="error">${errorMessage}</div></c:if>
    <c:if test="${not empty presetMessage}"><div class="notice">${presetMessage}</div></c:if>
    <c:if test="${not empty presetErrorMessage}"><div class="error">${presetErrorMessage}</div></c:if>

    <div class="grid">
        <section class="panel">
            <h3>내 프리셋</h3>
            <c:choose>
                <c:when test="${empty presets}"><p>저장된 프리셋이 없습니다.</p></c:when>
                <c:otherwise>
                    <div class="preset-list">
                        <c:forEach var="preset" items="${presets}">
                            <div class="preset-row">
                                <p><a href="/presets/${preset.id}/apply">${preset.name}</a> · 합계 ${preset.minSum}~${preset.maxSum}</p>
                                <form method="post" action="/presets/${preset.id}/delete" class="preset-delete-form">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                    <button type="submit" class="danger-button">삭제</button>
                                </form>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
        <section class="panel">
            <c:choose>
                <c:when test="${not empty selectedPresetName and not empty result}">
                    <div class="result-head" style="margin-top:0;">
                        <div>
                            <h3>선택한 프리셋 게임 번호</h3>
                            <p>${selectedPresetName}</p>
                        </div>
                        <button type="button" class="secondary" id="copy-all">전체 복사</button>
                    </div>
                    <h3>3:3 후보</h3>
                    <c:forEach var="candidate" items="${result.threeOddThreeEvenCandidates}">
                        <div class="candidate">
                            <div class="candidate-top">
                                <div>
                                    <div class="balls"><c:forEach var="number" items="${candidate.numbers}"><span class="ball">${number}</span></c:forEach></div>
                                    <div class="meta"><span>합계 ${candidate.sum}</span><span>패턴 ${candidate.oddEvenPattern}</span></div>
                                </div>
                                <button type="button" class="secondary candidate-copy" data-copy="${candidate.copyText}">개별 복사</button>
                            </div>
                        </div>
                    </c:forEach>
                    <h3>4:2 후보</h3>
                    <c:forEach var="candidate" items="${result.fourOddTwoEvenCandidates}">
                        <div class="candidate">
                            <div class="candidate-top">
                                <div>
                                    <div class="balls"><c:forEach var="number" items="${candidate.numbers}"><span class="ball">${number}</span></c:forEach></div>
                                    <div class="meta"><span>합계 ${candidate.sum}</span><span>패턴 ${candidate.oddEvenPattern}</span></div>
                                </div>
                                <button type="button" class="secondary candidate-copy" data-copy="${candidate.copyText}">개별 복사</button>
                            </div>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <h3>최근 생성 이력</h3>
                    <c:choose>
                        <c:when test="${empty histories}"><p>생성 이력이 없습니다.</p></c:when>
                        <c:otherwise>
                            <c:forEach var="history" items="${histories}">
                                <div class="history-item">
                                    <p>${history.requestSummary}</p>
                                    <p>${history.createdAt}</p>
                                </div>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </c:otherwise>
            </c:choose>
        </section>
    </div>

    <c:if test="${not empty result and empty selectedPresetName}">
        <div class="notice">
            <c:forEach var="message" items="${result.messages}"><p>${message}</p></c:forEach>
        </div>
        <div class="result-head">
            <h2>생성 결과</h2>
            <button type="button" class="secondary" id="copy-all">전체 복사</button>
        </div>
        <div class="grid">
            <section>
                <h2>3:3 후보</h2>
                <c:forEach var="candidate" items="${result.threeOddThreeEvenCandidates}">
                    <div class="candidate">
                        <div class="candidate-top">
                            <div>
                                <div class="balls"><c:forEach var="number" items="${candidate.numbers}"><span class="ball">${number}</span></c:forEach></div>
                                <div class="meta"><span>합계 ${candidate.sum}</span><span>패턴 ${candidate.oddEvenPattern}</span><span>저빈도 ${empty candidate.lowFrequencyNumbers ? '없음' : candidate.lowFrequencyNumbers}</span></div>
                            </div>
                            <button type="button" class="secondary candidate-copy" data-copy="${candidate.copyText}">개별 복사</button>
                        </div>
                    </div>
                </c:forEach>
            </section>
            <section>
                <h2>4:2 후보</h2>
                <c:forEach var="candidate" items="${result.fourOddTwoEvenCandidates}">
                    <div class="candidate">
                        <div class="candidate-top">
                            <div>
                                <div class="balls"><c:forEach var="number" items="${candidate.numbers}"><span class="ball">${number}</span></c:forEach></div>
                                <div class="meta"><span>합계 ${candidate.sum}</span><span>패턴 ${candidate.oddEvenPattern}</span><span>저빈도 ${empty candidate.lowFrequencyNumbers ? '없음' : candidate.lowFrequencyNumbers}</span></div>
                            </div>
                            <button type="button" class="secondary candidate-copy" data-copy="${candidate.copyText}">개별 복사</button>
                        </div>
                    </div>
                </c:forEach>
            </section>
        </div>
    </c:if>
</main>
<div class="toast" id="toast">복사했습니다</div>
<script>
    const toast = document.getElementById("toast");
    function showToast(message) { toast.textContent = message; toast.classList.add("visible"); window.setTimeout(() => toast.classList.remove("visible"), 1400); }
    function fallbackCopyText(text) {
        const textarea = document.createElement("textarea");
        textarea.value = text;
        textarea.setAttribute("readonly", "");
        textarea.style.position = "fixed";
        textarea.style.top = "0";
        textarea.style.left = "0";
        textarea.style.opacity = "0";
        document.body.appendChild(textarea);
        textarea.focus();
        textarea.select();
        textarea.setSelectionRange(0, textarea.value.length);
        let copied = false;
        try { copied = document.execCommand("copy"); } finally { textarea.remove(); }
        return copied;
    }
    async function copyText(text) {
        if (!text) { showToast("복사할 번호가 없습니다"); return; }
        let copied = false;
        if (navigator.clipboard && window.isSecureContext) {
            try { await navigator.clipboard.writeText(text); copied = true; } catch (error) { copied = fallbackCopyText(text); }
        } else {
            copied = fallbackCopyText(text);
        }
        showToast(copied ? "복사했습니다" : "복사하지 못했습니다");
    }
    document.addEventListener("click", (event) => {
        const button = event.target.closest(".candidate-copy");
        if (button) copyText(button.dataset.copy || "");
    });
    const copyAllButton = document.getElementById("copy-all");
    if (copyAllButton) {
        copyAllButton.addEventListener("click", () => {
            const text = Array.from(document.querySelectorAll(".candidate-copy")).map((button, index) => (index + 1) + ". " + (button.dataset.copy || "")).join("\n");
            copyText(text);
        });
    }
</script>
</body>
</html>
