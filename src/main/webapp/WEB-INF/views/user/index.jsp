<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Lotto Pattern Generator</title>
    <link rel="stylesheet" href="/css/app.css">
</head>
<body class="main-page">
<main>
    <%-- 사용자 메인 화면: 번호 생성, 프리셋 관리, 최근 생성 이력을 한 페이지에서 처리합니다. --%>
    <header>
        <div>
            <h1>Lotto Pattern Generator</h1>
            <p>${username}님 조건에 맞는 패턴 기반 후보 조합을 생성합니다. 이 결과는 예측이나 당첨 보장이 아닙니다.</p>
        </div>
        <nav>
            <a href="/stats">통계</a>
            <c:if test="${isAdmin}">
                <a href="/admin">관리자</a>
            </c:if>
            <form method="post" action="/logout" class="logout-form">
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
        <label class="wide">제외 번호 <input type="text" name="excludedNumbers" placeholder="예: 4, 10, 22" value="${generationRequest.excludedNumbers}"></label>
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
                    <div class="result-head preset-result-head">
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
<div class="toast" id="toast">복사되었습니다</div>
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
        showToast(copied ? "복사되었습니다" : "복사하지 못했습니다");
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
