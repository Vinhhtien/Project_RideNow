<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Quên mật khẩu | RideNow</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">

  <style>
    :root{
      --primary:#5bc0be;--primary-dark:#3a9e9c;
      --secondary:#6a11cb;--secondary-light:#2575fc;
      --bg-dark:#0b132b;--card-bg:rgba(17,24,39,.95);
      --text-light:#e2e8f0;--text-lighter:#94a3b8;
      --error:#ff6b6b;--success:#10b981;
    }
    *{box-sizing:border-box;margin:0;padding:0}
    body{
      font-family:Inter,system-ui,Segoe UI,Roboto,Arial,sans-serif;
      background:linear-gradient(135deg,#0b132b 0%,#1a1f36 100%);
      color:#fff;display:flex;min-height:100vh;align-items:center;justify-content:center;
      padding:20px;position:relative
    }
    body::before{
      content:"";position:absolute;inset:0;z-index:-1;
      background-image:
        radial-gradient(circle at 20% 80%, rgba(91,192,190,.1) 0%, transparent 50%),
        radial-gradient(circle at 80% 20%, rgba(106,17,203,.08) 0%, transparent 50%),
        radial-gradient(circle at 40% 40%, rgba(37,117,252,.06) 0%, transparent 50%);
    }
    .card{
      width:100%;max-width:420px;background:var(--card-bg);
      padding:36px 32px;border-radius:20px;
      box-shadow:0 20px 40px rgba(0,0,0,.3),0 0 0 1px rgba(255,255,255,.05);
      backdrop-filter:blur(10px);border:1px solid rgba(255,255,255,.1);
      position:relative;overflow:hidden;transition:.3s
    }
    .card:hover{transform:translateY(-3px);box-shadow:0 25px 50px rgba(0,0,0,.4)}
    .card::before{content:"";position:absolute;left:0;right:0;top:0;height:4px;
      background:linear-gradient(90deg,var(--secondary),var(--primary),var(--secondary-light))}
    h2{
      font-size:24px;font-weight:700;text-align:center;color:var(--text-light);
      margin-bottom:20px
    }
    label{
      display:block;margin-bottom:8px;font-size:14px;font-weight:600;color:var(--text-light)
    }
    input[type="email"]{
      width:100%;padding:14px;border-radius:12px;
      border:1px solid rgba(255,255,255,.1);background:rgba(15,23,42,.7);
      color:#fff;font-size:15px;transition:.25s
    }
    input[type="email"]:focus{
      outline:none;border-color:var(--primary);
      box-shadow:0 0 0 3px rgba(91,192,190,.2);background:rgba(15,23,42,.9)
    }
    button{
      margin-top:16px;width:100%;padding:14px;border:0;border-radius:12px;
      background:linear-gradient(135deg,var(--secondary) 0%,var(--primary) 100%);
      color:#fff;font-weight:700;font-size:16px;cursor:pointer;transition:.25s
    }
    button:hover{transform:translateY(-1px);box-shadow:0 10px 20px rgba(106,17,203,.25)}
    .msg{
      margin-top:16px;font-size:14px;text-align:center;
      color:var(--success);padding:10px;border-radius:10px;
      background:rgba(16,185,129,.1);border-left:4px solid var(--success)
    }
    .back-link{
      margin-top:16px;text-align:center
    }
    .back-link a{
      color:var(--primary);text-decoration:none;font-size:14px
    }
    .back-link a:hover{color:var(--secondary-light)}
  </style>
</head>
<body>
  <div class="card">
    <h2>Quên mật khẩu</h2>
    <form method="post" action="${ctx}/forgot">
      <label>Email</label>
      <input type="email" name="email" required placeholder="Nhập email đã đăng ký">
      <button type="submit">Gửi liên kết đặt lại</button>
    </form>
    <div class="msg">${msg}</div>
    <div class="back-link"><a href="${ctx}/login">← Quay lại đăng nhập</a></div>
  </div>
</body>
</html>
