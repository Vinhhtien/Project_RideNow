<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <title><c:out value="${title}"/></title>
  <meta name="viewport" content="width=device-width, initial-scale=1"/>
  <style>
    :root{
      --bg:#0b0f1f; --bg2:#0a0d1c; --card:#121735; --text:#e8eaf6; --muted:#a7b0d6;
      --border:#1c244c; --shadow:0 10px 30px rgba(0,0,0,.35); --radius:16px;
      --primary:#6c8cff;
    }
    *{box-sizing:border-box} html,body{margin:0}
    body{
      font-family: ui-sans-serif,system-ui,-apple-system,Segoe UI,Roboto,Arial;
      background:
        radial-gradient(1200px 600px at -10% -10%, #24306b 0%, transparent 60%),
        radial-gradient(900px 600px at 110% -20%, #391d5f 0%, transparent 55%),
        linear-gradient(180deg, var(--bg) 0%, var(--bg2) 100%);
      min-height:100vh;color:var(--text);display:flex;align-items:center;justify-content:center;padding:24px;
    }
    .card{
      width:min(720px, 92%); background:linear-gradient(180deg, rgba(255,255,255,.03), rgba(255,255,255,.015)), var(--card);
      border:1px solid var(--border); border-radius:var(--radius); box-shadow:var(--shadow);
      padding:22px;
    }
    .title{
      font-size:26px;font-weight:800;margin:0 0 8px;
      background:linear-gradient(90deg,#eaf0ff,#b9c7ff,#6c8cff);-webkit-background-clip:text;background-clip:text;color:transparent;
    }
    .muted{color:var(--muted)}
    .row{display:flex;gap:14px;align-items:center;margin:10px 0;flex-wrap:wrap}
    .chip{border:1px solid #3a4477;padding:6px 10px;border-radius:999px}
    .content{white-space:pre-wrap;margin:10px 0 6px}
    .actions{display:flex;gap:10px;justify-content:flex-end;margin-top:14px}
    .btn{appearance:none;border:1px solid #3a4477;background:transparent;color:var(--text);padding:10px 14px;border-radius:12px;cursor:pointer}
    .btn.primary{background:#22d3ee;color:#0f172a;border-color:#24cde0}
    a.link{color:#22d3ee;text-decoration:none}
    a.link:hover{text-decoration:underline}
  </style>
</head>
<body>
  <div class="card">
    <h1 class="title"><c:out value="${title}"/></h1>

    <div class="row">
      <div class="chip">Thời điểm: 
        <strong>
          <fmt:formatDate value="${createdAt}" pattern="HH:mm dd/MM/yyyy"/>
        </strong>
      </div>
      <c:if test="${not empty nid}">
        <div class="chip">Mã TB: <strong>${nid}</strong></div>
      </c:if>
    </div>

    <div class="content"><c:out value="${message}"/></div>

    <div class="actions">
      <button class="btn" onclick="location.href='${pageContext.request.contextPath}/dashboard'">← Về Dashboard</button>
      <a class="btn primary" href="${pageContext.request.contextPath}/viewmotorbike">Xe của tôi</a>
    </div>
  </div>
</body>
</html>
