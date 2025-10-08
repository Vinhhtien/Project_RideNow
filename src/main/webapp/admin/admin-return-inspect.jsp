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
        .inspection-item { display:flex; align-items:center; gap:8px; }
        .damage-notes { width:100%; min-height:80px; padding:12px; border:1px solid #e5e7eb; border-radius:6px; resize:vertical; }
        .refund-options { display:grid; grid-template-columns:1fr 1fr; gap:16px; margin:16px 0; }
        .refund-option { border:2px solid #e5e7eb; border-radius:8px; padding:16px; cursor:pointer; transition:all .3s ease; }
        .refund-option.selected { border-color:#3b82f6; background:#f0f9ff; }
        .refund-option h4 { margin:0 0 8px 0; color:#1f2937; }
        .refund-option p { margin:0; color:#6b7280; font-size:.9rem; }
        .action-buttons { display:flex; gap:12px; margin-top:20px; }
        .fee-input { width:100%; padding:8px 12px; border:1px solid #e5e7eb; border-radius:4px; }
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
            <a class="active" href="${pageContext.request.contextPath}/adminreturns">Hoàn Cọc</a>
            <a href="${pageContext.request.contextPath}/adminwithdrawals">Rút Tiền</a>
            <a href="${pageContext.request.contextPath}/logout">Logout</a>
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
                    <div>
                        <strong>Mã đơn:</strong> #${order.orderId}<br>
                        <strong>Khách hàng:</strong> ${order.customerName}<br>
                        <strong>Điện thoại:</strong> ${order.customerPhone}
                    </div>
                    <div>
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
                                <span>Tốt (không hư hỏng)</span>
                            </label>
                            <label class="inspection-item">
                                <input type="radio" name="bikeCondition" value="good" required>
                                <span>Bình thường (hao mòn nhẹ)</span>
                            </label>
                            <label class="inspection-item">
                                <input type="radio" name="bikeCondition" value="damaged" required>
                                <span>Hư hỏng (cần tính phí)</span>
                            </label>
                        </div>

                        <div id="damageSection" style="display:none; margin-top:16px;">
                            <h4>Chi tiết hư hỏng & Phí</h4>
                            <textarea name="damageNotes" placeholder="Mô tả chi tiết hư hỏng..." class="damage-notes"></textarea>
                            <div style="margin-top:12px;">
                                <label>Phí sửa chữa (VNĐ):</label>
                                <input type="number" name="damageFee" min="0" max="${order.depositAmount}" 
                                       step="1000" class="fee-input" placeholder="0">
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
                        <button type="submit" class="btn btn-primary">✅ Xác Nhận Hoàn Cọc</button>
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
                document.getElementById('damageSection').style.display = 
                    this.value === 'damaged' ? 'block' : 'none';
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
    </script>
</body>
</html>