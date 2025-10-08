<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Đặt lại mật khẩu | RideNow</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">

  <style>
    :root{
      --primary:#5bc0be; --primary-dark:#4aa8a6; --primary-light:rgba(91,192,190,.1);
      --dark:#0b132b; --darker:#111827; --dark-light:#1f2937;
      --text:#e2e8f0; --text-light:#cbd5e1; --text-lighter:#94a3b8;
      --error:#ef4444; --error-light:rgba(239,68,68,.1);
      --success:#10b981; --success-light:rgba(16,185,129,.1);
      --border:#334155; --border-light:#475569;
      --shadow:0 20px 40px rgba(0,0,0,.4); --shadow-small:0 5px 15px rgba(0,0,0,.2);
    }
    *{margin:0;padding:0;box-sizing:border-box}
    body{
      font-family:'Inter',system-ui,-apple-system,Segoe UI,Roboto,Arial,sans-serif;
      background:linear-gradient(135deg,var(--dark) 0%,#1e293b 50%,#0f172a 100%);
      background-size:200% 200%;
      animation:gradientShift 10s ease infinite;
      color:var(--text);min-height:100vh;display:flex;align-items:center;justify-content:center;
      padding:20px;line-height:1.6
    }
    .reset-container{width:100%;max-width:440px;margin:0 auto}
    .reset-card{
      background:var(--darker);padding:2.5rem;border-radius:24px;box-shadow:var(--shadow);
      border:1px solid var(--border);position:relative;overflow:hidden;backdrop-filter:blur(10px);
      animation:fadeInUp .6s ease-out
    }
    .reset-card::before{
      content:'';position:absolute;top:0;left:0;right:0;height:4px;
      background:linear-gradient(90deg,var(--primary),#60a5fa,var(--primary-dark));
      background-size:200% 100%;animation:shimmer 3s ease infinite
    }

    .logo-header{text-align:center;margin-bottom:2rem}
    .logo{
      display:inline-flex;align-items:center;gap:.75rem;color:var(--text);text-decoration:none;
      font-size:1.5rem;font-weight:700;margin-bottom:.5rem;transition:.3s ease
    }
    .logo:hover{transform:translateY(-2px)}
    .logo i{color:var(--primary);font-size:1.75rem}
    .tagline{color:var(--text-lighter);font-size:.9rem;margin-bottom:.5rem}

    h1{
      font-size:1.75rem;font-weight:700;margin-bottom:.5rem;text-align:center;
      background:linear-gradient(135deg,var(--primary),#93c5fd,var(--primary-dark));
      -webkit-background-clip:text;-webkit-text-fill-color:transparent;background-clip:text;
      background-size:200% 200%;animation:textShimmer 3s ease infinite
    }

    .security-indicator{
      display:flex;align-items:center;justify-content:center;gap:1rem;margin-top:1rem;
      padding:1rem;background:var(--primary-light);border-radius:12px;border:1px solid var(--border-light)
    }
    .security-indicator i{color:var(--primary);font-size:1.2rem}
    .security-text{font-size:.85rem;color:var(--text-light)}

    .form-group{margin-bottom:1.5rem;position:relative;animation:fadeInUp .6s ease-out both}
    .form-group:nth-child(1){animation-delay:.1s}
    .form-group:nth-child(2){animation-delay:.2s}
    .form-group:nth-child(3){animation-delay:.3s}

    label{
      display:flex;align-items:center;gap:.5rem;margin-bottom:.75rem;
      font-weight:600;color:var(--text-light);font-size:.95rem
    }
    label i{color:var(--primary);font-size:.9rem}

    .input-with-icon{position:relative}
    .input-with-icon > i.fa-key,
    .input-with-icon > i.fa-check-double{
      position:absolute;left:1rem;top:50%;transform:translateY(-50%);
      color:var(--text-lighter);font-size:1.1rem;z-index:2;transition:color .3s ease
    }

    /* Tăng padding-right để đủ chỗ cho icon validate + nút con mắt */
    input{
      width:100%;padding:1rem 5rem 1rem 3rem;border:2px solid var(--border);border-radius:12px;
      background:var(--dark-light);color:var(--text);font-size:1rem;transition:all .3s ease;position:relative
    }
    input:focus{
      outline:none;border-color:var(--primary);
      box-shadow:0 0 0 4px var(--primary-light);transform:translateY(-2px)
    }
    input::placeholder{color:var(--text-lighter);font-size:.9rem}

    /* Icon validate: check/times – JS sẽ bật/tắt bằng class .show */
    .validation-icon{
      position:absolute;right:3rem;top:50%;transform:translateY(-50%);
      font-size:.95rem;z-index:2;opacity:0;transition:opacity .2s ease
    }
    .validation-icon.show{opacity:1}
    .validation-icon.ok{color:var(--success)}
    .validation-icon.err{color:var(--error)}

    /* Nút hiện/ẩn mật khẩu (con mắt) */
    .input-with-icon .toggle-visibility{
      position:absolute;right:.75rem;top:50%;transform:translateY(-50%);
      background:transparent;border:0;color:var(--text-lighter);cursor:pointer;
      padding:6px 8px;border-radius:8px;line-height:0;transition:color .2s ease,background .2s ease,transform .05s ease
    }
    .input-with-icon .toggle-visibility:hover{color:var(--text-light);background:rgba(255,255,255,.06)}
    .input-with-icon .toggle-visibility:active{transform:translateY(-50%) scale(.98)}

    .password-strength{margin-top:.5rem;height:4px;background:var(--border);border-radius:2px;overflow:hidden;position:relative}
    .strength-bar{height:100%;width:0%;border-radius:2px;transition:all .3s ease}
    .strength-weak{background:var(--error);width:33%}
    .strength-medium{background:#f59e0b;width:66%}
    .strength-strong{background:var(--success);width:100%}
    .strength-text{font-size:.75rem;margin-top:.25rem;text-align:right;color:var(--text-lighter)}

    .btn{
      width:100%;padding:1.1rem;border:none;border-radius:12px;
      background:linear-gradient(135deg,var(--primary),var(--primary-dark));
      color:#001219;font-weight:700;font-size:1.05rem;cursor:pointer;transition:all .3s ease;
      margin-top:.5rem;position:relative;overflow:hidden
    }
    .btn::before{
      content:'';position:absolute;top:0;left:-100%;width:100%;height:100%;
      background:linear-gradient(90deg,transparent,rgba(255,255,255,.2),transparent);transition:left .5s
    }
    .btn:hover::before{left:100%}
    .btn:hover:not(:disabled){transform:translateY(-3px);box-shadow:var(--shadow-small)}
    .btn:active:not(:disabled){transform:translateY(-1px)}
    .btn:disabled{opacity:.6;cursor:not-allowed;transform:none!important}
    .btn i{margin-right:.5rem}

    .message{padding:1rem;border-radius:8px;margin-top:1rem;font-size:.9rem;text-align:center;animation:slideIn .3s ease}
    .error-message{background:var(--error-light);border:1px solid var(--error);color:var(--error)}
    .success-message{background:var(--success-light);border:1px solid var(--success);color:var(--success)}
    .info-message{background:rgba(59,130,246,.1);border:1px solid #3b82f6;color:#93c5fd}

    .back-link{
      text-align:center;margin-top:1.5rem;padding-top:1.5rem;border-top:1px solid var(--border)
    }
    .back-link a{
      color:var(--primary);text-decoration:none;font-weight:500;transition:.3s ease;
      display:inline-flex;align-items:center;gap:.5rem
    }
    .back-link a:hover{color:var(--text-light);transform:translateX(-5px)}

    @media (max-width:480px){
      .reset-card{padding:2rem 1.5rem;border-radius:20px}
      h1{font-size:1.5rem}
      input{padding:.875rem 4.75rem .875rem 2.8rem}
      .input-with-icon > i.fa-key,
      .input-with-icon > i.fa-check-double{left:.875rem}
      .validation-icon{right:2.75rem}
      .input-with-icon .toggle-visibility{right:.6rem}
    }

   </style>
</head>
<body>
  <div class="reset-container">
    <div class="reset-card">
      <div class="logo-header">
        <a href="${ctx}/" class="logo">
          <i class="fas fa-motorcycle"></i><span>RideNow</span>
        </a>
        <div class="tagline">Đặt xe máy dễ dàng, nhanh chóng</div>
        <h1><i class="fas fa-key"></i> Đặt lại mật khẩu</h1>
      </div>

      <div class="security-indicator" aria-live="polite">
        <i class="fas fa-shield-alt"></i>
        <span class="security-text">Phiên bảo mật • Mật khẩu được mã hóa</span>
      </div>

      <form id="resetForm" method="post" action="${ctx}/resetpassword" novalidate>
        <input type="hidden" name="token" value="${param.token != null ? param.token : token}">

        <!-- Mật khẩu mới -->
        <div class="form-group">
          <label for="password"><i class="fas fa-lock"></i> Mật khẩu mới</label>
          <div class="input-with-icon">
            <i class="fas fa-key" aria-hidden="true"></i>
            <input type="password" id="password" name="password"
                   placeholder="Nhập mật khẩu mới"
                   minlength="6"
                   pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{6,}$"
                   required
                   autocomplete="new-password"
                   aria-describedby="strengthText">
            <!-- icon validate -->
            <i class="validation-icon ok fas fa-check" data-for="password" aria-hidden="true"></i>
            <i class="validation-icon err fas fa-times" data-for="password" aria-hidden="true"></i>
            <!-- nút hiện/ẩn -->
            <button type="button" class="toggle-visibility" data-target="password" aria-label="Hiện mật khẩu">
              <i class="far fa-eye"></i>
            </button>
          </div>

          <div class="password-strength" aria-hidden="true">
            <div class="strength-bar" id="strengthBar"></div>
          </div>
          <div class="strength-text" id="strengthText">Độ mạnh mật khẩu</div>
        </div>

        <!-- Xác nhận mật khẩu -->
        <div class="form-group">
          <label for="confirm"><i class="fas fa-lock"></i> Xác nhận mật khẩu</label>
          <div class="input-with-icon">
            <i class="fas fa-check-double" aria-hidden="true"></i>
            <input type="password" id="confirm" name="confirm"
                   placeholder="Nhập lại mật khẩu mới"
                   required
                   autocomplete="new-password">
            <!-- icon validate -->
            <i class="validation-icon ok fas fa-check" data-for="confirm" aria-hidden="true"></i>
            <i class="validation-icon err fas fa-times" data-for="confirm" aria-hidden="true"></i>
            <!-- nút hiện/ẩn -->
            <button type="button" class="toggle-visibility" data-target="confirm" aria-label="Hiện mật khẩu">
              <i class="far fa-eye"></i>
            </button>
          </div>
        </div>

        <button type="submit" class="btn" id="submitBtn" disabled>
          <i class="fas fa-save"></i> Cập nhật mật khẩu
        </button>

        <c:if test="${not empty err}">
          <div class="message error-message">
            <i class="fas fa-exclamation-circle"></i> ${err}
          </div>
        </c:if>

        <c:if test="${not empty success}">
          <div class="message success-message">
            <i class="fas fa-check-circle"></i> ${success}
          </div>
        </c:if>
      </form>

      <div class="back-link">
        <a href="${ctx}/login"><i class="fas fa-arrow-left"></i> Quay lại đăng nhập</a>
      </div>
    </div>
  </div>

  <script>
    document.addEventListener('DOMContentLoaded', function () {
      const form = document.getElementById('resetForm');
      const passwordInput = document.getElementById('password');
      const confirmInput  = document.getElementById('confirm');
      const submitBtn     = document.getElementById('submitBtn');
      const strengthBar   = document.getElementById('strengthBar');
      const strengthText  = document.getElementById('strengthText');

      // Hiện/ẩn mật khẩu
      document.querySelectorAll('.toggle-visibility').forEach(btn => {
        const input = document.getElementById(btn.dataset.target);
        const icon  = btn.querySelector('i');
        btn.addEventListener('click', () => {
          const hidden = input.type === 'password';
          input.type = hidden ? 'text' : 'password';
          icon.classList.toggle('fa-eye', !hidden);
          icon.classList.toggle('fa-eye-slash', hidden);
          btn.setAttribute('aria-label', hidden ? 'Ẩn mật khẩu' : 'Hiện mật khẩu');
          input.focus();
        });
      });

      // Tính độ mạnh mật khẩu
      function checkPasswordStrength(password) {
        let strength = 0;
        const req = {
          length: password.length >= 6,
          uppercase: /[A-Z]/.test(password),
          lowercase: /[a-z]/.test(password),
          number: /\d/.test(password),
          special: /[!@#$%^&*(),.?":{}|<>]/.test(password)
        };
        if (req.length) strength += 20;
        if (req.uppercase) strength += 20;
        if (req.lowercase) strength += 20;
        if (req.number) strength += 20;
        if (req.special) strength += 20;

        if (password.length === 0) {
          strengthBar.className = 'strength-bar';
          strengthBar.style.width = '0%';
          strengthText.textContent = 'Độ mạnh mật khẩu';
          strengthText.style.color = 'var(--text-lighter)';
        } else if (strength < 40) {
          strengthBar.className = 'strength-bar strength-weak';
          strengthText.textContent = 'Yếu';
          strengthText.style.color = 'var(--error)';
        } else if (strength < 80) {
          strengthBar.className = 'strength-bar strength-medium';
          strengthText.textContent = 'Trung bình';
          strengthText.style.color = '#f59e0b';
        } else {
          strengthBar.className = 'strength-bar strength-strong';
          strengthText.textContent = 'Mạnh';
          strengthText.style.color = 'var(--success)';
        }
        return strength >= 60;
      }

      // Cập nhật icon validate cho một input
      function updateValidationIcons(inputEl, isValid) {
        const okIcon  = inputEl.parentElement.querySelector('.validation-icon.ok');
        const errIcon = inputEl.parentElement.querySelector('.validation-icon.err');
        if (okIcon && errIcon) {
          okIcon.classList.toggle('show', isValid);
          errIcon.classList.toggle('show', !isValid && inputEl.value.length > 0);
        }
      }

      // Xác nhận khớp mật khẩu
      function checkConfirmPassword() {
        const isValid = confirmInput.value.length > 0 && confirmInput.value === passwordInput.value;
        updateValidationIcons(confirmInput, isValid);
        return isValid;
      }

      // Kiểm tra form tổng thể
      function checkFormValidity() {
        const passStrongEnough = checkPasswordStrength(passwordInput.value);
        const passPatternOK    = passwordInput.checkValidity(); // pattern + minlength
        const isPasswordValid  = passStrongEnough && passPatternOK;

        updateValidationIcons(passwordInput, isPasswordValid);

        const isConfirmValid   = checkConfirmPassword();
        submitBtn.disabled     = !(isPasswordValid && isConfirmValid);
        return !submitBtn.disabled;
      }

      // Lắng nghe thay đổi
      passwordInput.addEventListener('input', () => {
        checkPasswordStrength(passwordInput.value);
        checkFormValidity();
      });
      confirmInput.addEventListener('input', () => {
        checkFormValidity();
      });

      // Submit
      form.addEventListener('submit', function (e) {
        if (!checkFormValidity()) {
          e.preventDefault();
          return;
        }
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang cập nhật...';
        submitBtn.disabled = true;
      });

      // Khởi tạo
      checkFormValidity();
    });
  </script>
</body>
</html>
