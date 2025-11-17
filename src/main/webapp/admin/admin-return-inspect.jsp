<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kiểm Tra Xe & Hoàn Cọc - RideNow Admin</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
    <style>
        .info-grid {
            display: grid;
            grid-template-columns:repeat(auto-fit, minmax(300px, 1fr));
            gap: 1.5rem;
            margin-bottom: 2rem
        }

        .info-card {
            background: #fff;
            padding: 1.5rem;
            border-radius: var(--border-radius);
            border-left: 4px solid var(--primary);
            box-shadow: var(--shadow)
        }

        .inspection-section {
            background: #fff;
            border-radius: var(--border-radius);
            padding: 2rem;
            margin: 2rem 0;
            box-shadow: var(--shadow)
        }

        .inspection-grid {
            display: grid;
            gap: 1rem;
            margin: 1.5rem 0;
            grid-template-columns:repeat(auto-fit, minmax(280px, 1fr))
        }

        .inspection-item {
            position: relative;
            display: flex;
            gap: 1rem;
            align-items: flex-start;
            padding: 1.25rem;
            border: 2px solid var(--gray-200);
            border-radius: var(--border-radius);
            background: #fff;
            cursor: pointer;
            transition: var(--transition)
        }

        .inspection-item input[type="radio"] {
            margin-top: .2rem
        }

        .inspection-item:hover {
            border-color: var(--primary);
            transform: translateY(-2px)
        }

        .inspection-item.selected {
            border-color: var(--primary);
            background: var(--primary-light)
        }

        .refund-options {
            display: grid;
            gap: 1.5rem;
            grid-template-columns:repeat(auto-fit, minmax(250px, 1fr));
            margin: 1.5rem 0
        }

        .refund-option {
            border: 2px solid var(--gray-200);
            border-radius: var(--border-radius);
            padding: 1.25rem;
            background: #fff;
            cursor: pointer;
            transition: var(--transition)
        }

        .refund-option:hover {
            border-color: var(--primary);
            transform: translateY(-2px)
        }

        .refund-option.selected {
            border-color: var(--primary);
            background: var(--primary-light)
        }

        .refund-option h4 {
            margin: 0 0 .5rem;
            color: var(--gray-900);
            font-weight: 600
        }

        .calculation-section {
            background: #f0fdf4;
            border: 1px solid #bbf7d0;
            border-radius: var(--border-radius);
            padding: 1.5rem;
            margin: 1.5rem 0
        }

        #displayRefundAmount {
            font-size: 2rem;
            font-weight: 800;
            color: #059669
        }

        .action-buttons {
            display: flex;
            gap: 1rem;
            margin-top: 2rem;
            flex-wrap: wrap
        }

        .damage-notes, .fee-input {
            width: 100%;
            padding: 1rem;
            border: 1px solid var(--gray-300);
            border-radius: 8px;
            background: #fff;
            color: var(--gray-900);
            font-family: inherit;
            font-size: .9rem;
            transition: var(--transition)
        }

        .damage-notes {
            min-height: 120px;
            resize: vertical
        }

        .damage-notes:focus, .fee-input:focus {
            outline: none;
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgba(59, 130, 246, .1)
        }

        .hint {
            font-size: .8rem;
            color: var(--gray-500)
        }

        .warn {
            color: #dc2626;
            font-weight: 600
        }

        .btn[disabled] {
            opacity: .7;
            cursor: not-allowed
        }

        @media (max-width: 768px) {
            .info-grid {
                grid-template-columns:1fr
            }

            .inspection-grid {
                grid-template-columns:1fr
            }

            .refund-options {
                grid-template-columns:1fr
            }

            .action-buttons {
                flex-direction: column
            }
        }

        /* Toast styles */
        .toast-container {
            position: fixed;
            top: 1.25rem;
            right: 1.25rem;
            z-index: 9999;
            display: flex;
            flex-direction: column;
            gap: .5rem;
        }

        .toast {
            min-width: 260px;
            max-width: 360px;
            background: #111827;
            color: #f9fafb;
            padding: .75rem 1rem;
            border-radius: .5rem;
            box-shadow: 0 10px 15px rgba(0,0,0,.2);
            font-size: .85rem;
            display: flex;
            align-items: flex-start;
            justify-content: space-between;
            gap: .75rem;
            opacity: 0;
            transform: translateY(-10px);
            transition: opacity .2s ease, transform .2s ease;
            border-left: 4px solid #f97316;
        }

        .toast--error {
            border-left-color: #f97373;
        }

        .toast--show {
            opacity: 1;
            transform: translateY(0);
        }

        .toast__message {
            flex: 1;
        }

        .toast__close {
            background: none;
            border: none;
            color: inherit;
            cursor: pointer;
            font-size: 1rem;
            line-height: 1;
            padding: 0;
        }
    </style>
</head>
<body class="admin">
<fmt:setLocale value="vi_VN"/>

<!-- Sidebar -->
<aside class="sidebar">
    <div class="brand">
        <div class="brand-logo"><i class="fas fa-motorcycle"></i></div>
        <h1>RideNow Admin</h1>
    </div>
    <nav class="sidebar-nav">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="nav-item"><i
                class="fas fa-tachometer-alt"></i><span>Dashboard</span></a>
        <a href="${pageContext.request.contextPath}/admin/partners" class="nav-item"><i
                class="fas fa-handshake"></i><span>Partners</span></a>
        <a href="${pageContext.request.contextPath}/admin/customers" class="nav-item"><i class="fas fa-users"></i><span>Customers</span></a>
        <a href="${pageContext.request.contextPath}/admin/bikes" class="nav-item"><i
                class="fas fa-motorcycle"></i><span>Motorbikes</span></a>
        <a href="${pageContext.request.contextPath}/admin/orders" class="nav-item"><i class="fas fa-clipboard-list"></i><span>Orders</span></a>
        <a href="${pageContext.request.contextPath}/admin/schedule" class="nav-item">
            <i class="fas fa-calendar-alt"></i><span>View Schedule</span>
        </a>
        <%-- <a href="${pageContext.request.contextPath}/adminpaymentverify" class="nav-item"><i class="fas fa-money-check-alt"></i><span>Verify Payments</span></a> --%>
        <a href="${pageContext.request.contextPath}/adminpickup" class="nav-item"><i
                class="fas fa-shipping-fast"></i><span>Vehicle Pickup</span></a>
        <a href="${pageContext.request.contextPath}/adminreturn" class="nav-item"><i class="fas fa-undo-alt"></i><span>Vehicle Return</span></a>
        <a href="${pageContext.request.contextPath}/adminreturns" class="nav-item active"><i
                class="fas fa-clipboard-check"></i><span>Verify & Refund</span></a>
        <a href="${pageContext.request.contextPath}/admin/reports" class="nav-item"><i
                class="fas fa-chart-bar"></i><span>Reports</span></a>
        <a href="${pageContext.request.contextPath}/admin/feedback" class="nav-item"><i
                class="fas fa-comment-alt"></i><span>Feedback</span></a>
        <a href="${pageContext.request.contextPath}/logout" class="nav-item logout"><i
                class="fas fa-sign-out-alt"></i><span>Logout</span></a>
    </nav>
</aside>

<!-- Main -->
<main class="content">
    <header class="content-header">
        <div class="header-left">
            <h1>Kiểm Tra Xe & Hoàn Cọc</h1>
            <div class="breadcrumb">
                <span>Admin</span><i class="fas fa-chevron-right"></i>
                <span>Quản lý Đơn hàng</span><i class="fas fa-chevron-right"></i>
                <span class="active">Kiểm Tra & Hoàn Cọc</span>
            </div>
        </div>
        <div class="header-right">
            <div class="user-profile">
                <div class="user-avatar"><i class="fas fa-user-circle"></i></div>
                <span>Administrator</span></div>
        </div>
    </header>

    <c:if test="${not empty sessionScope.flash}">
        <div class="notice"><i class="fas fa-info-circle"></i>${sessionScope.flash}</div>
        <c:remove var="flash" scope="session"/>
    </c:if>

    <!-- Thông tin đơn -->
    <section class="panel">
        <div class="panel-header">
            <h2><i class="fas fa-info-circle"></i> Thông Tin Đơn Hàng</h2>
        </div>
        <div class="panel-body">
            <div class="info-grid">
                <div class="info-card">
                    <div class="info-item"><strong>Mã đơn:</strong> #${order.orderId}</div>
                    <div class="info-item"><strong>Khách hàng:</strong> ${order.customerName}</div>
                    <div class="info-item"><strong>Điện thoại:</strong> ${order.customerPhone}</div>
                </div>
                <div class="info-card">
                    <div class="info-item"><strong>Xe thuê:</strong> ${order.bikeName}</div>
                    <div class="info-item">
                        <strong>Tiền cọc:</strong>
                        <span style="color:#059669;font-weight:600">
              <fmt:formatNumber value="${order.depositAmount}" type="currency"/>
            </span>
                    </div>
                    <div class="info-item">
                        <strong>Ngày trả:</strong>
                        <c:if test="${not empty order.returnedAt}">
                            <fmt:formatDate value="${order.returnedAt}" pattern="dd/MM/yyyy HH:mm"/>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- Form kiểm tra -->
    <form id="inspectForm" method="post" action="${pageContext.request.contextPath}/adminreturninspect"
          onsubmit="return handleSubmit()">
        <input type="hidden" name="orderId" value="${order.orderId}"/>

        <section class="panel">
            <div class="panel-header">
                <h2><i class="fas fa-clipboard-check"></i> Kiểm Tra Tình Trạng Xe</h2>
            </div>
            <div class="panel-body">
                <div class="inspection-section">
                    <h3>Tình trạng xe</h3>
                    <div class="inspection-grid" id="conditionGroup">
                        <label class="inspection-item">
                            <input type="radio" name="bikeCondition" value="excellent" required>
                            <span><strong>Tốt</strong><br><small class="text-muted">Không có hư hỏng, hoàn trả toàn bộ cọc</small></span>
                        </label>
                        <label class="inspection-item">
                            <input type="radio" name="bikeCondition" value="good" required>
                            <span><strong>Bình thường</strong><br><small
                                    class="text-muted">Hao mòn nhẹ do sử dụng</small></span>
                        </label>
                        <label class="inspection-item">
                            <input type="radio" name="bikeCondition" value="damaged" required>
                            <span><strong>Hư hỏng</strong><br><small
                                    class="text-muted">Có hư hỏng cần sửa chữa, trừ phí</small></span>
                        </label>
                    </div>

                    <div id="damageSection" style="display:none;margin-top:1.25rem">
                        <h4>Chi tiết hư hỏng & Phí</h4>
                        <textarea name="damageNotes" class="damage-notes"
                                  placeholder="Mô tả vị trí, mức độ hư hỏng, chi phí dự kiến..."></textarea>
                        <div style="margin-top:1rem">
                            <label>Phí sửa chữa (VNĐ):</label>
                            <input
                                    type="number"
                                    name="damageFee"
                                    min="0"
                                    max="${order.depositAmount}"
                                    step="1000"
                                    class="fee-input"
                                    placeholder="0"
                                    value="0"/>
                            <div class="hint">Tối đa: <fmt:formatNumber value="${order.depositAmount}"
                                                                        type="currency"/></div>
                            <div id="feeWarn" class="hint warn" style="display:none">Phí hư hỏng không được vượt quá
                                tiền cọc.
                            </div>
                        </div>
                    </div>

                    <!-- Tính toán -->
                    <div class="calculation-section" id="refundCalculation" style="display:none">
                        <h4>Kết quả tính toán</h4>
                        <div class="info-grid">
                            <div>
                                <div class="info-item"><strong>Tiền cọc:</strong>
                                    <span style="color:#3b82f6">
                    <fmt:formatNumber value="${order.depositAmount}" type="currency"/>
                  </span>
                                </div>
                                <div class="info-item"><strong>Phí hư hỏng:</strong> <span id="displayDamageFee"
                                                                                           style="color:#dc2626">0 VNĐ</span>
                                </div>
                            </div>
                            <div>
                                <strong style="font-size:1.05rem">Tiền hoàn cọc:</strong><br>
                                <span id="displayRefundAmount">0 VNĐ</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="inspection-section">
                    <h3>Phương thức hoàn cọc</h3>
                    <div class="refund-options" id="refundMethodGroup">
                        <label class="refund-option">
                            <input type="radio" name="refundMethod" value="cash" required>
                            <h4>💵 Tiền mặt</h4>
                            <p>Hoàn trực tiếp tại cửa hàng</p>
                        </label>
                        <label class="refund-option">
                            <input type="radio" name="refundMethod" value="wallet" required>
                            <h4>💰 Ví điện tử</h4>
                            <p>Hoàn vào ví để dùng cho lần thuê tiếp theo</p>
                        </label>
                    </div>
                </div>

                <div class="action-buttons">
                    <button id="submitBtn" type="submit" class="btn btn-primary">
                        <i class="fas fa-check-circle"></i>
                        <span class="btn-text">Xác Nhận Kiểm Tra & Tạo Yêu Cầu Hoàn Cọc</span>
                    </button>
                    <a href="${pageContext.request.contextPath}/adminreturns" class="btn"
                       style="background:var(--gray-100);color:var(--gray-700)">
                        <i class="fas fa-arrow-left"></i> Quay Lại
                    </a>
                </div>
            </div>
        </section>
    </form>
</main>

<script>
    // ===== Toast helper =====
    function ensureToastContainer() {
        let container = document.getElementById('toastContainer');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toastContainer';
            container.className = 'toast-container';
            document.body.appendChild(container);
        }
        return container;
    }

    function showToast(message, type) {
        const container = ensureToastContainer();
        const toast = document.createElement('div');
        toast.className = 'toast toast--show' + (type ? (' toast--' + type) : '');
        toast.innerHTML =
            '<div class="toast__message">' + message + '</div>' +
            '<button type="button" class="toast__close">&times;</button>';

        container.appendChild(toast);

        const closeBtn = toast.querySelector('.toast__close');

        function close() {
            toast.classList.remove('toast--show');
            setTimeout(function () {
                toast.remove();
            }, 200);
        }

        if (closeBtn) {
            closeBtn.addEventListener('click', close);
        }

        setTimeout(close, 4000);
    }

    // ===== Helpers =====
    const depositAmount = Number('${order.depositAmount}');
    const damageSection = document.getElementById('damageSection');
    const refundCalculation = document.getElementById('refundCalculation');
    const feeInput = document.querySelector('input[name="damageFee"]');
    const feeWarn = document.getElementById('feeWarn');
    const displayDamageFee = document.getElementById('displayDamageFee');
    const displayRefundAmount = document.getElementById('displayRefundAmount');

    const fmt = new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'});

    function setSelected(containerSelector, labelEl) {
        document.querySelectorAll(containerSelector).forEach(function (el) {
            el.classList.remove('selected');
        });
        labelEl.classList.add('selected');
    }

    function calculateRefund() {
        let fee = 0;
        if (feeInput) {
            fee = Number(feeInput.value || 0);
            if (fee < 0) fee = 0;
            if (fee > depositAmount) {
                feeWarn.style.display = 'block';
                fee = depositAmount;
                feeInput.value = String(depositAmount);
            } else {
                feeWarn.style.display = 'none';
            }
        }
        const refund = Math.max(0, depositAmount - fee);

        displayDamageFee.textContent = fmt.format(fee);
        displayRefundAmount.textContent = fmt.format(refund);
        displayRefundAmount.style.color = (refund === 0 ? '#ef4444' : '#059669');
    }

    function toggleSectionsByCondition(val) {
        if (val === 'damaged') {
            damageSection.style.display = 'block';
            refundCalculation.style.display = 'block';
        } else if (val === 'excellent' || val === 'good') {
            damageSection.style.display = 'none';
            refundCalculation.style.display = 'block';
            if (feeInput) feeInput.value = '0';
        }
        calculateRefund();
    }

    // ===== UI bindings =====
    // Highlight for condition cards
    document.querySelectorAll('.inspection-item').forEach(function (label) {
        const radio = label.querySelector('input[type="radio"]');
        label.addEventListener('click', function () {
            radio.checked = true;
            setSelected('.inspection-item', label);
            toggleSectionsByCondition(radio.value);
        });
    });

    // Highlight for refund method cards
    document.querySelectorAll('.refund-option').forEach(function (label) {
        const radio = label.querySelector('input[type="radio"]');
        label.addEventListener('click', function () {
            radio.checked = true;
            setSelected('.refund-option', label);
        });
    });

    // Fee change
    feeInput?.addEventListener('input', calculateRefund);

    // Init view: show calc box ngay từ đầu để admin thấy tổng tiền
    (function init() {
        refundCalculation.style.display = 'block';
        calculateRefund();
    })();

    // Prevent double submit + basic guard
    function handleSubmit() {
        const conditionChecked = document.querySelector('input[name="bikeCondition"]:checked');
        const methodChecked = document.querySelector('input[name="refundMethod"]:checked');
        if (!conditionChecked) {
            showToast('Vui lòng chọn tình trạng xe.', 'error');
            return false;
        }
        if (!methodChecked) {
            showToast('Vui lòng chọn phương thức hoàn cọc.', 'error');
            return false;
        }

        // Hard guard damageFee when damaged
        if (conditionChecked.value === 'damaged' && feeInput) {
            const feeVal = Number(feeInput.value || 0);
            if (feeVal > depositAmount) {
                showToast('Phí hư hỏng không được vượt quá tiền cọc.', 'error');
                return false;
            }
            if (feeVal < 0) {
                showToast('Phí hư hỏng không hợp lệ.', 'error');
                return false;
            }
        }

        const btn = document.getElementById('submitBtn');
        const text = btn.querySelector('.btn-text');
        btn.setAttribute('disabled', 'disabled');
        if (text) text.textContent = 'Đang tạo yêu cầu...';
        btn.insertAdjacentHTML('afterbegin', '<i class="fas fa-circle-notch fa-spin" style="margin-right:.5rem"></i>');
        return true;
    }
</script>
</body>
</html>
