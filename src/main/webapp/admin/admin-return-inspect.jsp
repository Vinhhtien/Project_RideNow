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
        .inspection-section { background:#f8f9fa; border-radius:8px; padding:20px; margin:16px 0; border-left:4px solid #3b82f6; }
        .inspection-grid { display:grid; grid-template-columns:repeat(auto-fit,minmax(200px,1fr)); gap:16px; margin:16px 0; }
        .inspection-item { display:flex; align-items:center; gap:8px; padding: 12px; border: 1px solid #e5e7eb; border-radius: 6px; cursor: pointer; }
        .inspection-item:hover { background: #f3f4f6; }
        .inspection-item.selected { border-color: #3b82f6; background: #eff6ff; }
        .damage-notes { width:100%; min-height:80px; padding:12px; border:1px solid #e5e7eb; border-radius:6px; resize:vertical; }
        .refund-options { display:grid; grid-template-columns:1fr 1fr; gap:16px; margin:16px 0; }
        .refund-option { border:2px solid #e5e7eb; border-radius:8px; padding:16px; cursor:pointer; transition:all .3s ease; }
        .refund-option:hover { border-color: #9ca3af; }
        .refund-option.selected { border-color:#3b82f6; background:#f0f9ff; }
        .refund-option h4 { margin:0 0 8px 0; color:#1f2937; }
        .refund-option p { margin:0; color:#6b7280; font-size:.9rem; }
        .action-buttons { display:flex; gap:12px; margin-top:20px; }
        .fee-input { width:100%; padding:8px 12px; border:1px solid #e5e7eb; border-radius:4px; }
        .info-card { background: white; padding: 16px; border-radius: 8px; border: 1px solid #e5e7eb; margin-bottom: 16px; }
        .calculation-section { background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 8px; padding: 16px; margin: 16px 0; }
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
                                    <small class="text-muted">Hao mòn nhẹ, có thể trừ phí nhỏ</small>
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