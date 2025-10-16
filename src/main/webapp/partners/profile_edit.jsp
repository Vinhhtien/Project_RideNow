<%-- tiến 16/10/2025 --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8" />
  <title>Chỉnh sửa thông tin Partner</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <style>
    :root{
      --bg:#0f1221;
      --card:#121735;
      --card-2:#10152e;
      --text:#e8eaf6;
      --muted:#a7b0d6;
      --primary:#6c8cff;
      --primary-2:#9aaeff;
      --danger:#ff6b6b;
      --success:#34d399;
      --border:#1c244c;
      --shadow:0 10px 30px rgba(0,0,0,.35);
      --radius:16px;
    }
    *{box-sizing:border-box}
    html,body{margin:0}
    body{
      font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Arial, "Apple Color Emoji","Segoe UI Emoji";
      background:
        radial-gradient(1200px 600px at -10% -10%, #24306b 0%, transparent 60%),
        radial-gradient(900px 600px at 110% -20%, #391d5f 0%, transparent 55%),
        linear-gradient(180deg, #0b0f1f 0%, #0a0d1c 100%);
      min-height:100vh;
      color:var(--text);
      padding:24px;
    }
    .wrap{max-width:1100px;margin:0 auto}
    .topbar{
      display:flex;align-items:center;justify-content:space-between;gap:12px;
      margin-bottom:18px;
    }
    .crumbs a{ color:var(--muted);text-decoration:none;font-size:14px; }
    .crumbs a:hover{color:var(--primary)}
    .title{
      font-size:28px; font-weight:800; letter-spacing:.3px;
      background:linear-gradient(90deg,#eaf0ff,#b9c7ff,#6c8cff);
      -webkit-background-clip:text;background-clip:text;color:transparent;
      margin:6px 0 0;
    }

    .grid{ display:grid;grid-template-columns: 1.25fr .9fr;gap:18px; }
    @media (max-width: 980px){ .grid{grid-template-columns:1fr;} }

    .card{
      background:linear-gradient(180deg, rgba(255,255,255,.03), rgba(255,255,255,.015)) , var(--card);
      border:1px solid var(--border);
      border-radius:var(--radius);
      box-shadow:var(--shadow);
      padding:18px 18px 16px;
    }
    .card h3{margin:2px 0 12px;font-size:18px;font-weight:700}

    .alert{border-radius:12px;padding:10px 12px;margin:0 0 12px;font-size:14px}
    .alert-success{background:rgba(52,211,153,.15); border:1px solid rgba(52,211,153,.35); color:#b9f9de}
    .alert-danger {background:rgba(255,107,107,.12); border:1px solid rgba(255,107,107,.35); color:#ffd1d1}

    .form-grid{display:grid;grid-template-columns:repeat(2,1fr);gap:14px}
    @media (max-width: 640px){ .form-grid{grid-template-columns:1fr;} }
    .fg{display:flex;flex-direction:column;gap:8px}
    label{font-weight:600;color:#d5dcff}
    input[type="text"],
    input[type="password"]{
      background:var(--card-2);
      border:1px solid var(--border);
      color:var(--text);
      padding:12px 12px;
      border-radius:12px;
      outline:none;
      transition:border-color .2s, box-shadow .2s, transform .05s;
      font-size:14px;
    }
    input[type="text"]:focus,
    input[type="password"]:focus{
      border-color:#7fa0ff;
      box-shadow:0 0 0 3px rgba(124,156,255,.2);
    }

    .help{font-size:12px;color:var(--muted);margin-top:-2px}
    .counter{font-size:12px;color:var(--muted)}

    .row-actions{display:flex;gap:10px;align-items:center;margin-top:12px;}
    .btn{
      appearance:none;border:0;border-radius:12px;padding:10px 14px;
      font-weight:700;cursor:pointer;transition:transform .06s ease, filter .15s;
    }
    .btn:active{transform:translateY(1px)}
    .btn-primary{background:linear-gradient(180deg,#7da1ff,#6c8cff);color:#0a0f21;border:1px solid #90adff}
    .btn-primary:disabled{filter:grayscale(25%);opacity:.6;cursor:not-allowed}
    .btn-ghost{background:transparent;color:var(--text);border:1px dashed #3a4477}
    .btn-ghost:hover{filter:brightness(1.1)}

    .preview{ display:grid;gap:10px;margin-top:6px; }
    .kv{
      display:flex;align-items:flex-start;gap:10px;
      background:rgba(255,255,255,.03);border:1px solid var(--border);
      border-radius:12px;padding:10px 12px;
    }
    .kv b{min-width:120px;color:#d8e1ff;font-weight:700}
    .badge{
      display:inline-block;border-radius:999px;padding:4px 10px;font-size:11px;font-weight:800;
      letter-spacing:.4px;text-transform:uppercase;
    }
    .badge-ok{background:rgba(52,211,153,.2);color:#a7ffd8;border:1px solid rgba(52,211,153,.45)}
    .badge-bad{background:rgba(255,107,107,.18);color:#ffd0d0;border:1px solid rgba(255,107,107,.4)}

    .split-head{display:flex;align-items:center;justify-content:space-between;margin-bottom:8px}
    .muted{color:var(--muted);font-size:13px}

    /* Password field with toggle (FULL-WIDTH + eye icon inside) */
    .pw-field{
        position:relative;
    }
    .pw-field input{
        width:100%;
        padding-right:44px;           /* chừa chỗ cho nút eye */
    }
    .eye-btn{
        position:absolute;
        right:10px;
        top:50%;
        transform:translateY(-50%);
        display:inline-flex;
        align-items:center;
        justify-content:center;
        width:36px;
        height:36px;
        border-radius:10px;
        background:transparent;
        border:1px solid #3a4477;
        color:#bcd0ff;
        cursor:pointer;
        outline:none;
    }
    .eye-btn:hover{
        filter:brightness(1.1);
    }
    .eye-btn svg{
        width:18px;
        height:18px;
        display:block;
    }

    /* icon state: open/closed */
    .eye-btn .icon-closed{
        display:none;
    }
    .eye-btn[data-show="1"] .icon-open{
        display:none;
    }
    .eye-btn[data-show="1"] .icon-closed{
        display:block;
    }


    /* Requirements list */
    .reqs{list-style:none; padding-left:0; margin:8px 0 0}
    .reqs li{
      font-size:12px; color:var(--muted);
      padding-left:20px; position:relative; line-height:1.6;
    }
    .reqs li::before{
      content:'✕'; color:#ff9b9b; position:absolute; left:0; top:0;
      font-weight:900;
    }
    .reqs li.ok{ color:#b9f9de; }
    .reqs li.ok::before{ content:'✓'; color:#34d399; }
  </style>
</head>
<body>
    
  <div class="wrap">
    <!-- Top -->
    <div class="topbar">
      <div>
        <div class="crumbs">
          <a href="${pageContext.request.contextPath}/partner?action=profile">← Quay lại Dashboard</a>
        </div>
        <h1 class="title">Cập nhật thông tin tài khoản</h1>
      </div>
    </div>

    <!-- Alerts (thông báo update profile) -->
    <c:if test="${not empty msg}">
      <div class="alert alert-success">${msg}</div>
    </c:if>
    <c:if test="${not empty error}">
      <div class="alert alert-danger">${error}</div>
    </c:if>
      
    <%-- Add this right before the password change form --%>
    <c:if test="${param.forcePwd == '1'}">
        <div class="alert alert-danger">
            Bạn đang dùng mật khẩu mặc định "1". Vui lòng đổi mật khẩu ngay để bảo mật.
        </div>
    </c:if>

    <script>
        (function(){
            const urlParams = new URLSearchParams(window.location.search);
            if (urlParams.get('forcePwd') === '1') {
                const pwdForm = document.getElementById('pwdForm');
                if (pwdForm) {
                    pwdForm.scrollIntoView({behavior: 'smooth'});
                    const newPwdInput = document.getElementById('newPassword');
                    if (newPwdInput) newPwdInput.focus();
                }
            }
        })();
    </script>

    <!-- Content -->
    <div class="grid">
      <!-- Left: Form thông tin -->
      <div class="card">
        <div class="split-head">
          <h3>Thông tin cửa hàng</h3>
          <span id="statusBadge" class="badge badge-ok" style="display:none">Hợp lệ</span>
        </div>
        <form id="editForm" method="post" action="${pageContext.request.contextPath}/partner?action=updateProfile" autocomplete="off" novalidate>
          <input type="hidden" name="partnerId" value="${partner.partnerId}" />

          <div class="form-grid">
              
              <div class="fg">
                  <label for="accountName">Tên tài khoản</label>
                  <input id="accountName" name="accountName" type="text"
                         value="${sessionScope.partnerName != null ? sessionScope.partnerName : ''}"
                         required minlength="3" maxlength="50"
                         placeholder="VD: tienthu_ridenow" />
                  <div class="help">Tên hiển thị/username dùng trong hệ thống.</div>
              </div>

            <div class="fg">
              <label for="fullName">Tên cửa hàng</label>
              <input id="fullName" name="fullName" type="text"
                     value="${partner.fullname}" required
                     placeholder="VD: RideNow Đà Nẵng" />
              <div class="help">Tên hiển thị cho khách hàng khi đặt xe.</div>
            </div>

            <div class="fg">
              <label for="phone">Số điện thoại (10 số, bắt đầu bằng 0)</label>
              <input id="phone" name="phone" type="text"
                     value="${partner.phone}" required
                     pattern="0[0-9]{9}" minlength="10" maxlength="10"
                     inputmode="numeric" placeholder="0xxxxxxxxx" />
              <div class="split-head" style="margin-top:4px">
                <span class="help">Hỗ trợ gọi nhanh & xác minh đơn thuê.</span>
                <span class="counter"><span id="len">0</span>/10</span>
              </div>
            </div>

            <div class="fg" style="grid-column:1/-1">
              <label for="address">Địa chỉ</label>
              <input id="address" name="address" type="text"
                     value="${partner.address}" required
                     placeholder="Số nhà, đường, quận/huyện, tỉnh/thành" />
              <div class="help">Địa chỉ nhận/trả xe chính của cửa hàng.</div>
            </div>
          </div>

          <div class="row-actions">
            <button id="saveBtn" type="submit" class="btn btn-primary">Lưu thay đổi</button>
            <a class="btn btn-ghost" href="${pageContext.request.contextPath}/partner?action=profile">Hủy</a>
          </div>
        </form>
      </div>

      <!-- Right: Live preview & tips -->
      <div class="card">
        <div class="split-head">
          <h3>Xem trước</h3>
          <span id="validityBadge" class="badge" style="display:none"></span>
        </div>
        <div class="preview">
            <div class="kv"><b>Tên tài khoản</b><div id="pvAccountName"><c:out value="${sessionScope.partnerName != null ? sessionScope.partnerName : ''}"/></div></div>
          <div class="kv"><b>Tên cửa hàng</b><div id="pvName"><c:out value="${partner.fullname}"/></div></div>
          <div class="kv"><b>Số điện thoại</b><div id="pvPhone"><c:out value="${partner.phone}"/></div></div>
          <div class="kv"><b>Địa chỉ</b><div id="pvAddress"><c:out value="${partner.address}"/></div></div>
        </div>

        <div class="card" style="margin-top:14px">
          <h3>Mẹo nhỏ</h3>
          <p class="muted">
            • Tên cửa hàng rõ ràng giúp khách nhớ thương hiệu.<br/>
            • Số điện thoại đúng định dạng giúp xác nhận đơn nhanh hơn.<br/>
            • Địa chỉ đủ chi tiết giúp giao/nhận xe thuận tiện.
          </p>
        </div>
      </div>

      <!-- Row tiếp theo (trong grid): Đổi mật khẩu -->
      <div class="card">
        <h3>Đổi mật khẩu</h3>
        <p class="muted">Yêu cầu: ≥ 8 ký tự, có ít nhất 1 chữ in hoa, 1 số và 1 ký tự đặc biệt.</p>

        <!-- Thông báo riêng của đổi mật khẩu -->
        <c:if test="${not empty pwd_msg}">
          <div class="alert alert-success">${pwd_msg}</div>
        </c:if>
        <c:if test="${not empty pwd_error}">
          <div class="alert alert-danger">${pwd_error}</div>
        </c:if>

        <form id="pwdForm" method="post" action="${pageContext.request.contextPath}/partner?action=updatePassword" autocomplete="off">
          <div class="form-grid" style="grid-template-columns:1fr;">
            <div class="fg">
              <label for="newPassword">Mật khẩu mới</label>
              <div class="pw-field">
                  <input id="newPassword" name="newPassword" type="password"
                         required minlength="8"
                         pattern="(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}"
                         placeholder="Ít nhất 8 ký tự, có A-Z, số, ký tự đặc biệt"
                         style="-webkit-text-security: disc; text-security: disc;" />
                  <button type="button" class="eye-btn" data-target="newPassword" data-show="0" aria-label="Hiện/Ẩn mật khẩu">
                      <!-- eye open -->
                      <svg class="icon-open" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7Z"/>
                      <circle cx="12" cy="12" r="3"/>
                      </svg>
                      <!-- eye closed -->
                      <svg class="icon-closed" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M3 3l18 18M10.5 10.7A3 3 0 0 0 15 15M6.1 6.6A14.6 14.6 0 0 0 1 12s4 7 11 7a12 12 0 0 0 6.4-2.1"/>
                      </svg>
                  </button>
              </div>


              <!-- Checklist yêu cầu -->
              <ul class="reqs">
                <li id="rqLen">Tối thiểu 8 ký tự</li>
                <li id="rqUpper">Có chữ in hoa (A–Z)</li>
                <li id="rqDigit">Có chữ số (0–9)</li>
                <li id="rqSpecial">Có ký tự đặc biệt (!@#...)</li>
              </ul>
            </div>

            <div class="fg">
              <label for="confirmPassword">Xác nhận mật khẩu</label>
              <div class="pw-field">
                  <input id="confirmPassword" name="confirmPassword" type="password"
                         required minlength="8"
                         style="-webkit-text-security: disc; text-security: disc;"
                         placeholder="Nhập lại mật khẩu mới" />
                  <button type="button" class="eye-btn" data-target="confirmPassword" data-show="0" aria-label="Hiện/Ẩn mật khẩu">
                      <svg class="icon-open" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7Z"/>
                      <circle cx="12" cy="12" r="3"/>
                      </svg>
                      <svg class="icon-closed" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M3 3l18 18M10.5 10.7A3 3 0 0 0 15 15M6.1 6.6A14.6 14.6 0 0 0 1 12s4 7 11 7a12 12 0 0 0 6.4-2.1"/>
                      </svg>
                  </button>
              </div>

              <div class="help" id="matchHint">• Vui lòng nhập lại trùng khớp.</div>
            </div>
          </div>

          <div class="row-actions">
            <button id="pwdSaveBtn" type="submit" class="btn btn-primary">Cập nhật mật khẩu</button>
          </div>
        </form>
      </div>
      <!-- end password card -->

    </div> <!-- /.grid -->
  </div>

  <script>
  (function(){
    // ----- Profile form logic -----
    const form = document.getElementById('editForm');
    const accountName = document.getElementById('accountName');   // NEW
    const phone = document.getElementById('phone');
    const fullName = document.getElementById('fullName');
    const address = document.getElementById('address');
    const len = document.getElementById('len');
    const saveBtn = document.getElementById('saveBtn');

    const pvAccountName = document.getElementById('pvAccountName'); // NEW
    const pvName = document.getElementById('pvName');
    const pvPhone = document.getElementById('pvPhone');
    const pvAddress = document.getElementById('pvAddress');

    const statusBadge = document.getElementById('statusBadge');
    const validityBadge = document.getElementById('validityBadge');

    const clamp10 = s => (s || '').replace(/\D/g,'').slice(0,10);

    const updatePreview = () => {
      pvAccountName.textContent = (accountName?.value || '').trim() || '—'; // NEW
      pvName.textContent = (fullName.value || '').trim() || '—';
      pvPhone.textContent = (phone.value || '').trim() || '—';
      pvAddress.textContent = (address.value || '').trim() || '—';
    };

    const validateAll = () => {
      const okAccName = (accountName?.value || '').trim().length >= 3; // NEW
      const okName = (fullName.value || '').trim().length > 0;
      const digits = clamp10(phone.value || '');
      const okPhone = /^0\d{9}$/.test(digits) && digits.length === 10;
      const okAddr = (address.value || '').trim().length > 0;

      phone.value = digits;
      if (len) len.textContent = digits.length;

      const allOk = okAccName && okName && okPhone && okAddr; // NEW
      statusBadge.style.display = 'inline-block';
      statusBadge.textContent = allOk ? 'Hợp lệ' : 'Chưa hợp lệ';
      statusBadge.className = 'badge ' + (allOk ? 'badge-ok' : 'badge-bad');

      validityBadge.style.display = 'inline-block';
      validityBadge.textContent = okPhone ? 'SĐT hợp lệ' : 'SĐT chưa hợp lệ';
      validityBadge.className = 'badge ' + (okPhone ? 'badge-ok' : 'badge-bad');

      saveBtn.disabled = !allOk;
    };

    [accountName, fullName, address].forEach(el =>
      el?.addEventListener('input', () => { updatePreview(); validateAll(); })
    );
    phone.addEventListener('input', () => { phone.value = clamp10(phone.value); updatePreview(); validateAll(); });

    updatePreview();
    validateAll();

    form.addEventListener('submit', (e) => {
      const digits = clamp10(phone.value);
      if (!/^0\d{9}$/.test(digits)) {
        e.preventDefault();
        alert('Số điện thoại không hợp lệ: phải đủ 10 số và bắt đầu bằng 0.');
      }
      if ((accountName?.value || '').trim().length < 3) {
        e.preventDefault();
        alert('Tên tài khoản tối thiểu 3 ký tự.');
      }
    });


      // ----- Password form logic -----
      const pwdForm = document.getElementById('pwdForm');
      const newPwd = document.getElementById('newPassword');
      const confirmPwd = document.getElementById('confirmPassword');
      const pwdBtn = document.getElementById('pwdSaveBtn');
      const matchHint = document.getElementById('matchHint');

      // checklist items
      const rqLen = document.getElementById('rqLen');
      const rqUpper = document.getElementById('rqUpper');
      const rqDigit = document.getElementById('rqDigit');
      const rqSpecial = document.getElementById('rqSpecial');

      const hasUpper = s => /[A-Z]/.test(s);
      const hasDigit = s => /\d/.test(s);
      const hasSpecial = s => /[^A-Za-z0-9]/.test(s);
      const hasLen = s => (s || '').length >= 8;
      const strong = s => hasUpper(s) && hasDigit(s) && hasSpecial(s) && hasLen(s);

      function updateChecklist(s){
        toggleReq(rqLen, hasLen(s));
        toggleReq(rqUpper, hasUpper(s));
        toggleReq(rqDigit, hasDigit(s));
        toggleReq(rqSpecial, hasSpecial(s));
      }
      function toggleReq(el, ok){
        if (!el) return;
        el.classList.toggle('ok', !!ok);
      }

      function validatePwd(){
        const s = newPwd.value || '';
        const ok = strong(s);
        const matched = s.length > 0 && s === (confirmPwd.value || '');
        pwdBtn.disabled = !(ok && matched);
        matchHint.textContent = matched ? '• Xác nhận khớp.' : '• Vui lòng nhập lại trùng khớp.';
        matchHint.style.color = matched ? '#b9f9de' : 'var(--muted)';
        updateChecklist(s);
      }

      [newPwd, confirmPwd].forEach(el => el.addEventListener('input', validatePwd));
      updateChecklist(newPwd.value || '');
      validatePwd();

      pwdForm.addEventListener('submit', (e) => {
        const s = newPwd.value || '';
        if (!strong(s)) {
          e.preventDefault();
          alert('Mật khẩu chưa đạt yêu cầu (≥8, có A-Z, số, ký tự đặc biệt).');
          return;
        }
        if (s !== (confirmPwd.value || '')) {
          e.preventDefault();
          alert('Xác nhận mật khẩu không khớp.');
        }
      });

      
     // Toggle show/hide password with icon
     document.querySelectorAll('.eye-btn').forEach(btn => {
        btn.addEventListener('click', () => {
        const id = btn.getAttribute('data-target');
        const inp = document.getElementById(id);
        const showing = inp.type === 'text';
      if (showing) {
        inp.type = 'password';
        inp.style.webkitTextSecurity = 'disc';
        inp.style.textSecurity = 'disc';
        btn.setAttribute('data-show','0'); // hiện icon "mắt mở"
      }else {
        inp.type = 'text';
        inp.style.webkitTextSecurity = 'none';
        inp.style.textSecurity = 'none';
        btn.setAttribute('data-show','1'); // hiện icon "mắt gạch"
        }
      });
     });

    })();
  </script>
</body>
</html>
