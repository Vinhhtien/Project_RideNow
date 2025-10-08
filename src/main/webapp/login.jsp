<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Đăng nhập | RideNow</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">

  <!-- Styles -->
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
      margin:0;padding:20px;position:relative
    }
    body::before{
      content:"";position:absolute;inset:0;z-index:-1;
      background-image:
        radial-gradient(circle at 20% 80%, rgba(91,192,190,.1) 0%, transparent 50%),
        radial-gradient(circle at 80% 20%, rgba(106,17,203,.08) 0%, transparent 50%),
        radial-gradient(circle at 40% 40%, rgba(37,117,252,.06) 0%, transparent 50%)
    }
    .card{
      width:100%;max-width:420px;background:var(--card-bg);
      padding:40px 35px;border-radius:20px;
      box-shadow:0 20px 40px rgba(0,0,0,.3),0 0 0 1px rgba(255,255,255,.05);
      backdrop-filter:blur(10px);border:1px solid rgba(255,255,255,.1);
      position:relative;overflow:hidden;transition:.3s
    }
    .card:hover{transform:translateY(-5px);box-shadow:0 25px 50px rgba(0,0,0,.4),0 0 0 1px rgba(91,192,190,.2)}
    .card::before{content:"";position:absolute;left:0;right:0;top:0;height:4px;
      background:linear-gradient(90deg,var(--secondary),var(--primary),var(--secondary-light))}
    .logo{ text-align:center;margin-bottom:30px }
    .logo-text{ font-size:36px;font-weight:800;
      background:linear-gradient(135deg,var(--primary) 0%,var(--secondary-light) 100%);
      -webkit-background-clip:text;-webkit-text-fill-color:transparent;letter-spacing:-1px }
    h1{font-size:26px;margin:0 0 18px;text-align:center;font-weight:700;color:var(--text-light)}
    .sub{font-size:13px;color:var(--text-lighter);text-align:center;margin-bottom:16px}
    .badge{
      display:inline-flex;align-items:center;gap:8px;
      padding:8px 10px;border-radius:10px;font-size:12px;font-weight:700;margin-bottom:12px
    }
    .badge--ok{ background:rgba(16,185,129,.12); color:#34d399; border:1px solid rgba(16,185,129,.35) }
    .form-group{margin-bottom:18px}
    label{display:block;margin-bottom:8px;color:var(--text-light);font-size:14px;font-weight:600}
    .input-container{position:relative}
    input[type="text"],input[type="password"]{
      width:100%;padding:14px 46px 14px 14px;border-radius:12px;
      border:1px solid rgba(255,255,255,.1);background:rgba(15,23,42,.7);
      color:#fff;font-size:15px;transition:.25s
    }
    input[type="text"]:focus,input[type="password"]:focus{
      outline:none;border-color:var(--primary);
      box-shadow:0 0 0 3px rgba(91,192,190,.2);background:rgba(15,23,42,.9)
    }
    .input-icon{position:absolute;right:14px;top:50%;transform:translateY(-50%);color:var(--text-lighter)}
    .row{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-top:2px}
    .remember{display:flex;align-items:center;gap:10px;color:var(--text-lighter);font-size:14px;cursor:pointer}
    .remember input{width:18px;height:18px;accent-color:var(--primary)}
    .btn{
      margin-top:16px;width:100%;padding:14px;border:0;border-radius:12px;
      background:linear-gradient(135deg,var(--secondary) 0%,var(--primary) 100%);
      color:#fff;font-weight:700;font-size:16px;cursor:pointer;transition:.25s;position:relative;overflow:hidden
    }
    .btn:hover{transform:translateY(-1px);box-shadow:0 10px 20px rgba(106,17,203,.25)}
    .btn i{margin-right:8px}
    .btn-google{background:#fff;color:#111}
    .hint{font-size:12px;color:var(--text-lighter);margin-top:6px}
    .err{
      margin-top:12px;color:var(--error);min-height:20px;font-size:14px;text-align:center;
      padding:10px;border-radius:10px;background:rgba(255,107,107,.1);border-left:4px solid var(--error)
    }
    .success{
      background:rgba(16,185,129,.1);color:var(--success);padding:12px;border-radius:10px;
      margin-bottom:14px;font-size:14px;border-left:4px solid var(--success)
    }
    .link{color:var(--primary);text-decoration:none;font-size:14px;position:relative}
    .link:hover{color:var(--secondary-light)}
    .register-link{ text-align:center;margin-top:18px;color:var(--text-lighter);font-size:15px }
    .home-link{ text-align:center;margin-top:12px }
    @media (max-width:480px){ .card{padding:30px 24px} .logo-text{font-size:32px} h1{font-size:24px} .row{flex-direction:column;align-items:flex-start} }
  </style>

  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body>

  <div class="card">
    <div class="logo"><div class="logo-text">RideNow</div></div>
    <h1>Đăng nhập tài khoản</h1>

    <!-- Thông báo sau verify / reset password -->
    <c:if test="${not empty sessionScope.flash}">
      <div class="success">${sessionScope.flash}</div>
      <c:remove var="flash" scope="session"/>
    </c:if>

    <!-- Nếu đã có cookie rn_auth (đã bật ghi nhớ) thì hiển thị badge gợi ý -->
    <c:if test="${cookie.rn_auth ne null}">
      <div class="badge badge--ok">
        <i class="fa-solid fa-shield-halved"></i>
        Đã bật <b>Ghi nhớ đăng nhập</b> — Bạn có thể bấm Đăng nhập mà không cần nhập mật khẩu.
      </div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/login">
      <div class="form-group">
        <label for="username">Tài khoản hoặc Email</label>
        <div class="input-container">
          <input
            type="text"
            id="username"
            name="username"
            required
            placeholder="Nhập tài khoản hoặc email"
            value="${cookie.rn_user != null ? cookie.rn_user.value : ''}">
          <i class="fas fa-user input-icon"></i>
        </div>
      </div>

      <div class="form-group">
        <label for="password">Mật khẩu</label>
        <div class="input-container">
          <!-- KHÔNG đặt required: cho phép để trống nếu đã remember-me -->
          <input type="password" id="password" name="password" required
            placeholder="Nhập mật khẩu"
            value="${cookie.rn_pw != null ? cookie.rn_pw.value : ''}">

          <i class="fas fa-lock input-icon"></i>
        </div>
        <div class="hint">
          <c:choose>
            <c:when test="${cookie.rn_auth ne null}">
              Bạn đã bật ghi nhớ trước đó. Có thể để trống mật khẩu và bấm <b>Đăng nhập</b>.
            </c:when>
            <c:otherwise>
              Tick “Ghi nhớ đăng nhập” để lần sau không cần nhập mật khẩu.
            </c:otherwise>
          </c:choose>
        </div>
      </div>

      <div class="row">
        <label class="remember" style="user-select:none">
          <!-- Nếu có rn_auth hoặc rn_user thì mặc định check -->
          <input type="checkbox" name="remember"
                 <c:if test="${cookie.rn_auth ne null or cookie.rn_user ne null}">checked</c:if> />
          Ghi nhớ đăng nhập
        </label>
        <a class="link" href="${pageContext.request.contextPath}/forgot">Quên mật khẩu?</a>
      </div>

      <button class="btn" type="submit">
        <i class="fas fa-sign-in-alt"></i> Đăng nhập
      </button>

      <!-- Nút Google (placeholder OAuth) -->
      <button type="button" class="btn" style="margin-top:12px;background:#fff;color:#111"
        onclick="location.href='${pageContext.request.contextPath}/logingoogle'">
  <i class="fab fa-google" style="color:#DB4437"></i> Đăng nhập bằng Google
</button>


      <div class="err">${error}</div>

      <div class="register-link">
        Chưa có tài khoản?
        <a class="link" href="${pageContext.request.contextPath}/register">Đăng ký ngay</a>
      </div>

      <div class="home-link">
        <a class="link" href="${pageContext.request.contextPath}/home.jsp">
          <i class="fas fa-arrow-left"></i> Về trang chủ
        </a>
      </div>
    </form>
  </div>

</body>
</html>
