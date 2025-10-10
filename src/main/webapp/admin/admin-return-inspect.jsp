<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kiểm Tra Xe & Hoàn Cọc - RideNow Admin</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
    <style>
  /* ===== Design tokens (Professional Light theme) ===== */
  :root{
    --bg-0:#f6f8fb;              /* page background */
    --bg-1:#ffffff;              /* surfaces/card */
    --line:#e6eaf0;              /* subtle borders */
    --text:#0f172a;              /* primary text (slate-900) */
    --muted:#5b6472;            /* secondary text */
    --muted-2:#8a93a3;          /* helper text */

    --accent:#2563eb;           /* brand blue */
    --accent-600:#1d4ed8;
    --accent-700:#1e40af;
    --accent-50:#eff6ff;

    --success:#16a34a;
    --success-50:#f0fdf4;
    --success-200:#bbf7d0;

    --warning:#f59e0b;
    --warning-50:#fff7ed;

    --danger:#ef4444;

    --radius:12px;
    --shadow-sm:0 1px 2px rgba(16,24,40,.06);
    --shadow-md:0 8px 24px rgba(16,24,40,.08);
    --shadow-lg:0 16px 40px rgba(16,24,40,.12);
    --focus:0 0 0 4px rgba(37,99,235,.15);
    --trans:.22s ease;
  }

  /* Page canvas harmonize (doesn't break admin.css) */
  body.admin{
    background:var(--bg-0);
    color:var(--text);
    font-family:'Inter','Segoe UI',system-ui,-apple-system,Roboto,Arial,sans-serif;
  }

  /* Panels (wrapper components already in HTML) */
  .panel{
    background:var(--bg-1);
    border:1px solid var(--line);
    border-radius:var(--radius);
    box-shadow:var(--shadow-sm);
    overflow:hidden;
  }
  .panel-head{
    padding:18px 20px;
    border-bottom:1px solid var(--line);
    background:linear-gradient(0deg, rgba(37,99,235,.04), rgba(37,99,235,.04));
  }
  .panel-head h2{ margin:0; font-size:1.05rem; letter-spacing:.2px; color:var(--accent-700) }
  .panel-body{ padding:20px; }

  /* Headings */
  h1{ margin:0 0 18px; font-size:1.6rem; font-weight:800; color:var(--accent-700) }
  h3{ margin:0 0 10px; font-size:1.05rem; color:var(--text) }
  h4{ margin:0 0 8px; font-size:1rem; color:var(--text) }

  /* Utility grid */
  .grid-2{ display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:16px }
  @media (max-width: 860px){ .grid-2{ grid-template-columns:1fr } }

  /* Info cards */
  .info-card{
    background:var(--bg-1);
    border:1px solid var(--line);
    border-radius:10px;
    padding:14px 16px;
  }
  .info-card strong{ color:var(--text) }

  /* Notice / flash */
  .notice{
    background:var(--warning-50);
    border:1px solid rgba(245,158,11,.25);
    color:#8a5a08;
    padding:12px 14px;
    border-radius:10px;
    margin-bottom:14px;
  }

  /* ===== Inspection section ===== */
  .inspection-section{
    background:var(--bg-1);
    border-radius:12px;
    padding:18px;
    margin:16px 0;
    border:1px solid var(--line);
  }
  .inspection-grid{
    display:grid; gap:12px; margin:14px 0;
    grid-template-columns:repeat(auto-fit,minmax(240px,1fr));
  }
  .inspection-item{
    position:relative;
    display:flex; gap:12px; align-items:flex-start;
    padding:14px;
    border:1px solid var(--line);
    border-radius:10px;
    background:#fff;
    cursor:pointer;
    transition:transform var(--trans), box-shadow var(--trans), border-color var(--trans), background var(--trans);
  }
  .inspection-item input[type="radio"]{ margin-top:2px }
  .inspection-item:hover{ box-shadow:var(--shadow-md); transform:translateY(-1px) }
  .inspection-item.selected{
    border-color:var(--accent);
    background:var(--accent-50);
    box-shadow:var(--shadow-md);
  }
  .inspection-item strong{ color:var(--text) }
  .inspection-item small{ color:var(--muted) }

  /* Damage notes & fee input */
  .damage-notes, .fee-input{
    width:100%;
    padding:10px 12px;
    border:1px solid var(--line);
    border-radius:10px;
    background:#fff;
    color:var(--text);
    transition:border-color var(--trans), box-shadow var(--trans);
  }
  .damage-notes{ min-height:92px; resize:vertical }
  .damage-notes:focus, .fee-input:focus{
    outline:none; border-color:var(--accent); box-shadow:var(--focus);
  }

  /* ===== Refund options ===== */
  .refund-options{
    display:grid; gap:14px; grid-template-columns:repeat(2,minmax(0,1fr)); margin:12px 0 4px;
  }
  @media (max-width: 720px){ .refund-options{ grid-template-columns:1fr } }
  .refund-option{
    border:2px solid var(--line);
    border-radius:14px;
    padding:16px 18px;
    background:#fff;
    cursor:pointer;
    transition:transform var(--trans), box-shadow var(--trans), border-color var(--trans), background var(--trans);
  }
  .refund-option:hover{ border-color:#c9d3e3; box-shadow:var(--shadow-md); transform:translateY(-1px) }
  .refund-option.selected{
    border-color:var(--accent);
    background:var(--accent-50);
    box-shadow:var(--shadow-md);
  }
  .refund-option h4{ margin:0 0 6px; color:var(--text); font-weight:700 }
  .refund-option p{ margin:0; color:var(--muted) }
  .refund-option input[type="radio"]{ display:none }

  /* ===== Calculation box ===== */
  .calculation-section{
    background:var(--success-50);
    border:1px solid var(--success-200);
    border-radius:12px;
    padding:16px;
    margin:16px 0;
    box-shadow:var(--shadow-sm);
  }
  #displayRefundAmount{
    font-size:1.6em; font-weight:800; color:var(--success);
  }

  /* Action buttons – add nicer defaults in case admin.css misses */
  .action-buttons{ display:flex; gap:12px; margin-top:20px; flex-wrap:wrap }
  .btn{
    appearance:none; border:1px solid var(--line); border-radius:12px;
    padding:12px 16px; background:#fff; color:var(--text);
    font-weight:700; cursor:pointer; transition:transform var(--trans), box-shadow var(--trans), background var(--trans), border-color var(--trans);
  }
  .btn:hover{ transform:translateY(-1px); box-shadow:var(--shadow-md) }
  .btn:focus{ outline:none; box-shadow:var(--focus) }
  .btn.btn-primary{
    background:var(--accent); color:#fff; border-color:var(--accent);
  }
  .btn.btn-primary:hover{ background:var(--accent-600) }
  .btn.secondary{
    background:#f8fafc; color:var(--accent-700); border-color:#dbe2ee;
  }
  .btn.secondary:hover{ background:#eef2f8 }

  /* Sidebar (subtle polish; keeps your structure) */
  .sidebar{
    background:#0f172a;          /* slate-900 */
    border-right:1px solid #0b1220;
  }
  .sidebar .brand{
    font-weight:800; letter-spacing:.3px; color:#e5eefc;
    background:linear-gradient(90deg, rgba(37,99,235,.15), transparent);
  }
  .sidebar nav a{
    color:#cbd5e1; border-left:3px solid transparent; transition:color var(--trans), background var(--trans), border-color var(--trans);
  }
  .sidebar nav a:hover{ color:#fff; background:rgba(255,255,255,.04) }
  .sidebar nav a.active{
    color:#fff; background:rgba(37,99,235,.12); 
  }

  /* Main content spacing */
  .content{ padding:24px; }
  @media (max-width: 860px){ .content{ padding:18px } }

  /* Subtle helper text */
  .text-muted{ color:var(--muted-2) }

  /* Accessibility: focus ring for clickable cards */
  .inspection-item:focus-within,
  .refund-option:focus-within{
    border-color:var(--accent);
    box-shadow:var(--focus);
  }
</style>

</head>
<body class="admin">
    <fmt:setLocale value="vi_VN"/>
    <aside class="sidebar">
        <div class="brand">RideNow Admin</div>
        <nav>
            <a href="${pageContext.request.contextPath}/admin/dashboard">Dashboard</a>
            <a href="${pageContext.request.contextPath}/adminpaymentverify">Xác Minh Thanh Toán</a>
            <a href="${pageContext.request.contextPath}/adminpickup">Giao Nhận Xe</a>
            <a href="${pageContext.request.contextPath}/adminreturn">Trả Xe</a>
            <a href="${pageContext.request.contextPath}/adminreturns" class="active">Kiểm tra và Hoàn Cọc</a>
            <!--<a href="${pageContext.request.contextPath}/adminwithdrawals">Rút Tiền</a>-->
            <a href="${pageContext.request.contextPath}/logout">Đăng xuất</a>
        </nav>
    </aside>

    <main class="content">
        <h1>Kiểm Tra Xe & Hoàn Cọc</h1>

        <c:if test="${not empty sessionScope.flash}">
            <div class="notice">${sessionScope.flash}</div>
            <c:remove var="flash" scope="session"/>
        </c:if>

        <div class="panel">
            <div class="panel-head">
                <h2>Thông Tin Đơn Hàng</h2>
            </div>
            <div class="panel-body">
                <div class="grid-2">
                    <div class="info-card">
                        <strong>Mã đơn:</strong> #${order.orderId}<br>
                        <strong>Khách hàng:</strong> ${order.customerName}<br>
                        <strong>Điện thoại:</strong> ${order.customerPhone}
                    </div>
                    <div class="info-card">
                        <strong>Xe thuê:</strong> ${order.bikeName}<br>
                        <strong>Tiền cọc:</strong> <fmt:formatNumber value="${order.depositAmount}" type="currency"/><br>
                        <strong>Ngày trả:</strong> 
                        <c:if test="${not empty order.returnedAt}">
                            <fmt:formatDate value="${order.returnedAt}" pattern="dd/MM/yyyy HH:mm"/>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>

        <form method="post" action="${pageContext.request.contextPath}/adminreturninspect">
            <input type="hidden" name="orderId" value="${order.orderId}">
            
            <div class="panel">
                <div class="panel-head">
                    <h2>Kiểm Tra Tình Trạng Xe</h2>
                </div>
                <div class="panel-body">
                    <div class="inspection-section">
                        <h3>Tình trạng xe</h3>
                        <div class="inspection-grid">
                            <label class="inspection-item">
                                <input type="radio" name="bikeCondition" value="excellent" required>
                                <span>
                                    <strong>Tốt</strong><br>
                                    <small class="text-muted">Không có hư hỏng, hoàn trả đầy đủ cọc</small>
                                </span>
                            </label>
                            <label class="inspection-item">
                                <input type="radio" name="bikeCondition" value="good" required>
                                <span>
                                    <strong>Bình thường</strong><br>
                                    <small class="text-muted">Hao mòn nhẹ do thời gian</small>
                                </span>
                            </label>
                            <label class="inspection-item">
                                <input type="radio" name="bikeCondition" value="damaged" required>
                                <span>
                                    <strong>Hư hỏng</strong><br>
                                    <small class="text-muted">Có hư hỏng cần sửa chữa, tính phí</small>
                                </span>
                            </label>
                        </div>

                        <div id="damageSection" style="display:none; margin-top:16px;">
                            <h4>Chi tiết hư hỏng & Phí</h4>
                            <textarea name="damageNotes" placeholder="Mô tả chi tiết hư hỏng (vị trí, mức độ hư hỏng, ước tính chi phí sửa chữa)..." class="damage-notes"></textarea>
                            <div style="margin-top:12px;">
                                <label>Phí sửa chữa (VNĐ):</label>
                                <input type="number" name="damageFee" min="0" max="${order.depositAmount}" 
                                       step="1000" class="fee-input" placeholder="0" value="0">
                                <small class="text-muted">Số tiền tối đa: <fmt:formatNumber value="${order.depositAmount}" type="currency"/></small>
                            </div>
                        </div>

                        <!-- Hiển thị tính toán tiền hoàn -->
                        <div class="calculation-section" id="refundCalculation" style="display:none;">
                            <h4>Kết Quả Tính Toán</h4>
                            <div class="grid-2">
                                <div>
                                    <strong>Tiền cọc:</strong> <fmt:formatNumber value="${order.depositAmount}" type="currency"/><br>
                                    <strong>Phí hư hỏng:</strong> <span id="displayDamageFee">0 VNĐ</span>
                                </div>
                                <div>
                                    <strong style="font-size: 1.2em;">Tiền hoàn cọc:</strong><br>
                                    <span id="displayRefundAmount" style="font-size: 1.5em; font-weight: bold; color: #059669;">0 VNĐ</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="inspection-section">
                        <h3>Phương Thức Hoàn Cọc</h3>
                        <div class="refund-options">
                            <label class="refund-option">
                                <input type="radio" name="refundMethod" value="cash" required>
                                <h4>💵 Tiền Mặt</h4>
                                <p>Hoàn trả trực tiếp bằng tiền mặt tại cửa hàng</p>
                            </label>
                            <label class="refund-option">
                                <input type="radio" name="refundMethod" value="wallet" required>
                                <h4>💰 Ví Điện Tử</h4>
                                <p>Hoàn vào ví để sử dụng cho lần thuê tiếp theo</p>
                            </label>
                        </div>
                    </div>

                    <div class="action-buttons">
                        <button type="submit" class="btn btn-primary">✅ Xác Nhận Kiểm Tra & Tạo Yêu Cầu Hoàn Cọc</button>
                        <a href="${pageContext.request.contextPath}/adminreturns" class="btn secondary">↩ Quay Lại</a>
                    </div>
                </div>
            </div>
        </form>
    </main>

    <script>
        // Hiển thị section hư hỏng khi chọn "damaged"
        document.querySelectorAll('input[name="bikeCondition"]').forEach(radio => {
            radio.addEventListener('change', function() {
                const damageSection = document.getElementById('damageSection');
                const refundCalculation = document.getElementById('refundCalculation');
                
                if (this.value === 'damaged') {
                    damageSection.style.display = 'block';
                    refundCalculation.style.display = 'block';
                } else {
                    damageSection.style.display = 'none';
                    refundCalculation.style.display = 'block';
                }
                calculateRefund();
            });
        });

        // Highlight selected refund option
        document.querySelectorAll('.refund-option').forEach(option => {
            const radio = option.querySelector('input[type="radio"]');
            option.addEventListener('click', () => {
                document.querySelectorAll('.refund-option').forEach(o => o.classList.remove('selected'));
                option.classList.add('selected');
                radio.checked = true;
            });
        });

        // Highlight selected inspection item
        document.querySelectorAll('.inspection-item').forEach(item => {
            const radio = item.querySelector('input[type="radio"]');
            item.addEventListener('click', () => {
                document.querySelectorAll('.inspection-item').forEach(i => i.classList.remove('selected'));
                item.classList.add('selected');
                radio.checked = true;
                
                // Trigger change event để hiển thị damage section nếu cần
                radio.dispatchEvent(new Event('change'));
            });
        });

        // Tính toán tiền hoàn
        function calculateRefund() {
            const depositAmount = ${order.depositAmount};
            const damageFeeInput = document.querySelector('input[name="damageFee"]');
            let damageFee = 0;
            
            if (damageFeeInput) {
                damageFee = parseFloat(damageFeeInput.value) || 0;
            }
            
            const refundAmount = Math.max(0, depositAmount - damageFee);
            
            // Cập nhật hiển thị
            document.getElementById('displayDamageFee').textContent = new Intl.NumberFormat('vi-VN', {
                style: 'currency',
                currency: 'VND'
            }).format(damageFee);
            
            document.getElementById('displayRefundAmount').textContent = new Intl.NumberFormat('vi-VN', {
                style: 'currency',
                currency: 'VND'
            }).format(refundAmount);
            
            // Đổi màu nếu không hoàn cọc
            const refundElement = document.getElementById('displayRefundAmount');
            if (refundAmount === 0) {
                refundElement.style.color = '#dc2626';
            } else {
                refundElement.style.color = '#059669';
            }
        }

        // Lắng nghe sự kiện thay đổi phí hư hỏng
        document.querySelector('input[name="damageFee"]')?.addEventListener('input', calculateRefund);

        // Khởi tạo tính toán ban đầu
        calculateRefund();
    </script>
</body>
</html>