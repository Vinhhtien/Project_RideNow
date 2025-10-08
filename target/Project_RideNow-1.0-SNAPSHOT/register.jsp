<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Đăng ký | RideNow</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
  <style>
    :root {
      --primary: #5bc0be;
      --primary-dark: #4aa8a6;
      --dark: #0b132b;
      --darker: #111827;
      --dark-light: #1f2937;
      --text: #e2e8f0;
      --text-light: #cbd5e1;
      --text-lighter: #94a3b8;
      --error: #ef4444;
      --success: #10b981;
      --warning: #f59e0b;
      --border: #334155;
      --shadow: 0 10px 25px rgba(0, 0, 0, 0.35);
    }

    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }

    body {
      font-family: 'Inter', system-ui, -apple-system, sans-serif;
      background: linear-gradient(135deg, var(--dark) 0%, #1e293b 100%);
      color: var(--text);
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 20px;
      line-height: 1.6;
    }

    .register-container {
      width: 100%;
      max-width: 480px;
      margin: 0 auto;
    }

    .register-card {
      background: var(--darker);
      padding: 2.5rem;
      border-radius: 20px;
      box-shadow: var(--shadow);
      border: 1px solid var(--border);
      position: relative;
      overflow: hidden;
    }

    .register-card::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 4px;
      background: linear-gradient(90deg, var(--primary), var(--primary-dark));
    }

    .logo-header {
      text-align: center;
      margin-bottom: 2rem;
    }

    .logo {
      display: inline-flex;
      align-items: center;
      gap: 0.75rem;
      color: var(--text);
      text-decoration: none;
      font-size: 1.5rem;
      font-weight: 700;
      margin-bottom: 0.5rem;
    }

    .logo i {
      color: var(--primary);
      font-size: 1.75rem;
    }

    .tagline {
      color: var(--text-lighter);
      font-size: 0.9rem;
      margin-bottom: 0.5rem;
    }

    h1 {
      font-size: 1.75rem;
      font-weight: 700;
      margin-bottom: 0.5rem;
      text-align: center;
      background: linear-gradient(135deg, var(--primary), var(--text-light));
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .form-group {
      margin-bottom: 1.25rem;
      position: relative;
    }

    label {
      display: block;
      margin-bottom: 0.5rem;
      font-weight: 500;
      color: var(--text-light);
      font-size: 0.9rem;
    }

    .input-with-icon {
      position: relative;
    }

    .input-with-icon i {
      position: absolute;
      left: 1rem;
      top: 50%;
      transform: translateY(-50%);
      color: var(--text-lighter);
      font-size: 1rem;
      z-index: 2;
    }

    input, textarea {
      width: 100%;
      padding: 0.875rem 1rem 0.875rem 3rem;
      border: 1px solid var(--border);
      border-radius: 12px;
      background: var(--dark-light);
      color: var(--text);
      font-size: 0.95rem;
      transition: all 0.3s ease;
      position: relative;
    }

    textarea {
      padding-left: 1rem;
      min-height: 80px;
      resize: vertical;
    }

    input:focus, textarea:focus {
      outline: none;
      border-color: var(--primary);
      box-shadow: 0 0 0 3px rgba(91, 192, 190, 0.1);
      transform: translateY(-1px);
    }

    input::placeholder, textarea::placeholder {
      color: var(--text-lighter);
    }

    /* Validation Styles */
    input:valid:not(:placeholder-shown) {
      border-color: var(--success);
    }

    input:invalid:not(:placeholder-shown) {
      border-color: var(--error);
    }

    input:invalid:not(:placeholder-shown) + .validation-icon {
      color: var(--error);
    }

    input:valid:not(:placeholder-shown) + .validation-icon {
      color: var(--success);
    }

    .validation-icon {
      position: absolute;
      right: 1rem;
      top: 50%;
      transform: translateY(-50%);
      font-size: 0.9rem;
      z-index: 2;
    }

    .error-message {
      color: var(--error);
      font-size: 0.8rem;
      margin-top: 0.25rem;
      display: none;
    }

    input:invalid:not(:placeholder-shown) ~ .error-message {
      display: block;
    }

    .password-requirements {
      font-size: 0.8rem;
      color: var(--text-lighter);
      margin-top: 0.25rem;
      line-height: 1.4;
    }

    .requirements-list {
      list-style: none;
      margin-top: 0.5rem;
    }

    .requirements-list li {
      font-size: 0.75rem;
      margin-bottom: 0.25rem;
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .requirements-list li.valid {
      color: var(--success);
    }

    .requirements-list li.invalid {
      color: var(--text-lighter);
    }

    .requirements-list li i {
      font-size: 0.7rem;
    }

    .btn {
      width: 100%;
      padding: 1rem;
      border: none;
      border-radius: 12px;
      background: linear-gradient(135deg, var(--primary), var(--primary-dark));
      color: #001219;
      font-weight: 700;
      font-size: 1rem;
      cursor: pointer;
      transition: all 0.3s ease;
      margin-top: 0.5rem;
      position: relative;
    }

    .btn:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 5px 15px rgba(91, 192, 190, 0.3);
    }

    .btn:active:not(:disabled) {
      transform: translateY(0);
    }

    .btn:disabled {
      opacity: 0.6;
      cursor: not-allowed;
      transform: none;
    }

    .form-error {
      background: rgba(239, 68, 68, 0.1);
      border: 1px solid var(--error);
      color: var(--error);
      padding: 0.875rem 1rem;
      border-radius: 8px;
      margin-top: 1rem;
      font-size: 0.9rem;
      text-align: center;
    }

    .form-success {
      background: rgba(16, 185, 129, 0.1);
      border: 1px solid var(--success);
      color: var(--success);
      padding: 0.875rem 1rem;
      border-radius: 8px;
      margin-top: 1rem;
      font-size: 0.9rem;
      text-align: center;
    }

    .login-link {
      text-align: center;
      margin-top: 1.5rem;
      padding-top: 1.5rem;
      border-top: 1px solid var(--border);
    }

    .login-link a {
      color: var(--primary);
      text-decoration: none;
      font-weight: 500;
      transition: color 0.3s ease;
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
    }

    .login-link a:hover {
      color: var(--text-light);
    }

    @media (max-width: 480px) {
      .register-card {
        padding: 2rem 1.5rem;
      }
      
      h1 {
        font-size: 1.5rem;
      }
      
      input, textarea {
        padding: 0.75rem 0.875rem 0.75rem 2.5rem;
      }
      
      .input-with-icon i {
        left: 0.875rem;
      }
      
      .validation-icon {
        right: 0.875rem;
      }
    }

    /* Animation */
    @keyframes fadeIn {
      from {
        opacity: 0;
        transform: translateY(20px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    .register-card {
      animation: fadeIn 0.6s ease-out;
    }

    .form-group {
      animation: fadeIn 0.6s ease-out;
      animation-fill-mode: both;
    }

    .form-group:nth-child(1) { animation-delay: 0.1s; }
    .form-group:nth-child(2) { animation-delay: 0.2s; }
    .form-group:nth-child(3) { animation-delay: 0.3s; }
    .form-group:nth-child(4) { animation-delay: 0.4s; }
    .form-group:nth-child(5) { animation-delay: 0.5s; }
    .form-group:nth-child(6) { animation-delay: 0.6s; }
    .form-group:nth-child(7) { animation-delay: 0.7s; }
  </style>
</head>
<body>
  <div class="register-container">
    <div class="register-card">
      <div class="logo-header">
        <a href="${ctx}/" class="logo">
          <i class="fas fa-motorcycle"></i>
          <span>RideNow</span>
        </a>
        <div class="tagline">Đặt xe máy dễ dàng, nhanh chóng</div>
        <h1>Đăng ký tài khoản</h1>
      </div>

      <form id="registerForm" method="post" action="${ctx}/register" novalidate>
        <div class="form-group">
          <label for="username">Tài khoản *</label>
          <div class="input-with-icon">
            <i class="fas fa-user"></i>
            <input type="text" id="username" name="username" 
                   placeholder="Nhập tên tài khoản" 
                   pattern="[a-zA-Z0-9]{3,20}"
                   title="Tài khoản phải từ 3-20 ký tự, chỉ chứa chữ cái và số"
                   required>
            <i class="validation-icon fas fa-check" style="display: none;"></i>
            <i class="validation-icon fas fa-times" style="display: none;"></i>
          </div>
          <div class="error-message">Tài khoản phải từ 3-20 ký tự, chỉ chứa chữ cái và số</div>
        </div>

        <div class="form-group">
          <label for="password">Mật khẩu *</label>
          <div class="input-with-icon">
            <i class="fas fa-lock"></i>
            <input type="password" id="password" name="password" 
                   placeholder="Nhập mật khẩu" 
                   minlength="6"
                   pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{6,}$"
                   title="Mật khẩu phải có ít nhất 6 ký tự, bao gồm chữ hoa, chữ thường và số"
                   required>
            <i class="validation-icon fas fa-check" style="display: none;"></i>
            <i class="validation-icon fas fa-times" style="display: none;"></i>
          </div>
          <div class="password-requirements">
            <ul class="requirements-list">
              <li id="req-length" class="invalid"><i class="fas fa-circle"></i> Ít nhất 6 ký tự</li>
              <li id="req-uppercase" class="invalid"><i class="fas fa-circle"></i> Chứa ít nhất 1 chữ hoa</li>
              <li id="req-lowercase" class="invalid"><i class="fas fa-circle"></i> Chứa ít nhất 1 chữ thường</li>
              <li id="req-number" class="invalid"><i class="fas fa-circle"></i> Chứa ít nhất 1 số</li>
            </ul>
          </div>
        </div>

        <div class="form-group">
          <label for="confirm_password">Xác nhận mật khẩu *</label>
          <div class="input-with-icon">
            <i class="fas fa-lock"></i>
            <input type="password" id="confirm_password" name="confirm_password" 
                   placeholder="Nhập lại mật khẩu" 
                   required>
            <i class="validation-icon fas fa-check" style="display: none;"></i>
            <i class="validation-icon fas fa-times" style="display: none;"></i>
          </div>
          <div class="error-message">Mật khẩu xác nhận không khớp</div>
        </div>

        <div class="form-group">
          <label for="full_name">Họ tên *</label>
          <div class="input-with-icon">
            <i class="fas fa-id-card"></i>
            <input type="text" id="full_name" name="full_name" 
                   placeholder="Nhập họ tên đầy đủ" 
                   pattern="[a-zA-ZÀ-ỹ\s]{2,50}"
                   title="Họ tên phải từ 2-50 ký tự, chỉ chứa chữ cái và khoảng trắng"
                   required>
            <i class="validation-icon fas fa-check" style="display: none;"></i>
            <i class="validation-icon fas fa-times" style="display: none;"></i>
          </div>
          <div class="error-message">Họ tên phải từ 2-50 ký tự, chỉ chứa chữ cái</div>
        </div>

        <div class="form-group">
          <label for="email">Email *</label>
          <div class="input-with-icon">
            <i class="fas fa-envelope"></i>
            <input type="email" id="email" name="email" 
                   placeholder="Nhập địa chỉ email" 
                   pattern="[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,}$"
                   title="Vui lòng nhập địa chỉ email hợp lệ"
                   required>
            <i class="validation-icon fas fa-check" style="display: none;"></i>
            <i class="validation-icon fas fa-times" style="display: none;"></i>
          </div>
          <div class="error-message">Vui lòng nhập địa chỉ email hợp lệ</div>
        </div>

        <div class="form-group">
          <label for="phone">Điện thoại</label>
          <div class="input-with-icon">
            <i class="fas fa-phone"></i>
            <input type="tel" id="phone" name="phone" 
                   placeholder="Nhập số điện thoại"
                   pattern="[0-9]{10,11}"
                   title="Số điện thoại phải có 10-11 chữ số">
            <i class="validation-icon fas fa-check" style="display: none;"></i>
            <i class="validation-icon fas fa-times" style="display: none;"></i>
          </div>
          <div class="error-message">Số điện thoại phải có 10-11 chữ số</div>
        </div>

        <div class="form-group">
          <label for="address">Địa chỉ</label>
          <textarea id="address" name="address" 
                    placeholder="Nhập địa chỉ của bạn" 
                    rows="3"
                    maxlength="200"></textarea>
        </div>

        <button type="submit" class="btn" id="submitBtn" disabled>
          <i class="fas fa-user-plus"></i> Tạo tài khoản
        </button>

        <c:if test="${not empty error}">
          <div class="form-error">
            <i class="fas fa-exclamation-circle"></i> ${error}
          </div>
        </c:if>

        <c:if test="${not empty flash}">
          <div class="form-success">
            <i class="fas fa-check-circle"></i> ${flash}
          </div>
        </c:if>
      </form>

      <div class="login-link">
        <a href="${ctx}/login">
          <i class="fas fa-arrow-left"></i> Đã có tài khoản? Đăng nhập ngay
        </a>
      </div>
    </div>
  </div>

  <script>
    document.addEventListener('DOMContentLoaded', function() {
      const form = document.getElementById('registerForm');
      const submitBtn = document.getElementById('submitBtn');
      const inputs = form.querySelectorAll('input[required]');
      const passwordInput = document.getElementById('password');
      const confirmPasswordInput = document.getElementById('confirm_password');

      // Hiển thị icon validation
      function showValidationIcon(input, isValid) {
        const checkIcon = input.parentElement.querySelector('.fa-check');
        const timesIcon = input.parentElement.querySelector('.fa-times');
        
        if (isValid) {
          checkIcon.style.display = 'block';
          timesIcon.style.display = 'none';
          input.style.borderColor = 'var(--success)';
        } else {
          checkIcon.style.display = 'none';
          timesIcon.style.display = 'block';
          input.style.borderColor = 'var(--error)';
        }
      }

      // Kiểm tra password requirements
      function checkPasswordRequirements(password) {
        const requirements = {
          length: password.length >= 6,
          uppercase: /[A-Z]/.test(password),
          lowercase: /[a-z]/.test(password),
          number: /\d/.test(password)
        };

        // Update UI
        document.getElementById('req-length').className = requirements.length ? 'valid' : 'invalid';
        document.getElementById('req-uppercase').className = requirements.uppercase ? 'valid' : 'invalid';
        document.getElementById('req-lowercase').className = requirements.lowercase ? 'valid' : 'invalid';
        document.getElementById('req-number').className = requirements.number ? 'valid' : 'invalid';

        return Object.values(requirements).every(req => req);
      }

      // Kiểm tra confirm password
      function checkConfirmPassword() {
        const password = passwordInput.value;
        const confirmPassword = confirmPasswordInput.value;
        const isValid = password === confirmPassword && password.length > 0;
        
        showValidationIcon(confirmPasswordInput, isValid);
        return isValid;
      }

      // Kiểm tra form validity
      function checkFormValidity() {
        let allValid = true;

        // Kiểm tra các input required
        inputs.forEach(input => {
          if (!input.checkValidity()) {
            allValid = false;
          }
        });

        // Kiểm tra confirm password
        if (!checkConfirmPassword()) {
          allValid = false;
        }

        // Kiểm tra password requirements
        if (!checkPasswordRequirements(passwordInput.value)) {
          allValid = false;
        }

        submitBtn.disabled = !allValid;
        return allValid;
      }

      // Xử lý sự kiện input
      inputs.forEach(input => {
        input.addEventListener('input', function() {
          const isValid = this.checkValidity();
          showValidationIcon(this, isValid);
          
          // Xử lý đặc biệt cho password
          if (this.id === 'password') {
            checkPasswordRequirements(this.value);
            if (confirmPasswordInput.value) {
              checkConfirmPassword();
            }
          }
          
          if (this.id === 'confirm_password') {
            checkConfirmPassword();
          }

          checkFormValidity();
        });

        input.addEventListener('blur', function() {
          if (this.value) {
            showValidationIcon(this, this.checkValidity());
          }
        });
      });

      // Xử lý submit form
      form.addEventListener('submit', function(e) {
        if (!checkFormValidity()) {
          e.preventDefault();
          alert('Vui lòng kiểm tra lại thông tin đăng ký!');
          return;
        }

        const btn = this.querySelector('.btn');
        btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
        btn.disabled = true;
      });

      // Kiểm tra ban đầu
      checkFormValidity();
    });
  </script>
</body>
</html>