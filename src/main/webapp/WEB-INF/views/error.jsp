<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>오류 안내</title>
    <style>
        :root { --ink:#20242b; --muted:#66707c; --line:#d8dde3; --paper:#f5f7f9; --accent:#0f766e; --white:#fff; }
        * { box-sizing:border-box; }
        body { margin:0; min-height:100vh; display:grid; place-items:center; font-family:Arial,"Malgun Gothic",sans-serif; color:var(--ink); background:var(--paper); }
        main { width:min(460px, calc(100% - 28px)); padding:28px; border:1px solid var(--line); border-radius:8px; background:var(--white); }
        .status { display:inline-grid; place-items:center; min-width:64px; min-height:38px; margin-bottom:18px; border-radius:999px; color:#115e59; background:#eef6f5; font-weight:700; }
        h1 { margin:0 0 10px; font-size:28px; letter-spacing:0; }
        p { margin:0; color:var(--muted); line-height:1.6; }
        a { display:inline-grid; place-items:center; min-height:42px; margin-top:22px; padding:0 16px; border-radius:6px; color:#fff; background:var(--accent); font-weight:700; text-decoration:none; }
    </style>
</head>
<body>
<main>
    <div class="status">${status}</div>
    <h1>${title}</h1>
    <p>${message}</p>
    <a href="${returnUrl}">${returnText}</a>
</main>
</body>
</html>
