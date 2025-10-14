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
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 1.5rem;
            margin-bottom: 2rem;
        }
        
        .info-card {
            background: white;
            padding: 1.5rem;
            border-radius: var(--border-radius);
            border-left: 4px solid var(--primary);
            box-shadow: var(--shadow);
        }
        
        .inspection-section {
            background: white;
            border-radius: var(--border-radius);
            padding: 2rem;
            margin: 2rem 0;
            box-shadow: var(--shadow);
        }
        
        .inspection-grid {
            display: grid;
            gap: 1rem;
            margin: 1.5rem 0;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
        }
        
        .inspection-item {
            position: relative;
            display: flex;
            gap: 1rem;
            align-items: flex-start;
            padding: 1.5rem;
            border: 2px solid var(--gray-200);
            border-radius: var(--border-radius);
            background: white;
            cursor: pointer;
            transition: var(--transition);
        }
        
        .inspection-item input[type="radio"] {
            margin-top: 0.25rem;
        }
        
        .inspection-item:hover {
            border-color: var(--primary);
            transform: translateY(-2px);
        }
        
        .inspection-item.selected {
            border-color: var(--primary);
            background: var(--primary-light);
        }
        
        .refund-options {
            display: grid;
            gap: 1.5rem;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            margin: 1.5rem 0;
        }
        
        .refund-option {
            border: 2px solid var(--gray-200);
            border-radius: var(--border-radius);
            padding: 1.5rem;
            background: white;
            cursor: pointer;
            transition: var(--transition);
        }
        
        .refund-option:hover {
            border-color: var(--primary);
            transform: translateY(-2px);
        }
        
        .refund-option.selected {
            border-color: var(--primary);
            background: var(--primary-light);
        }
        
        .refund-option h4 {
            margin: 0 0 0.5rem;
            color: var(--gray-900);
            font-weight: 600;
        }
        
        .calculation-section {
            background: #f0fdf4;
            border: 1px solid #bbf7d0;
            border-radius: var(--border-radius);
            padding: 2rem;
            margin: 2rem 0;
        }
        
        #displayRefundAmount {
            font-size: 2rem;
            font-weight: 800;
            color: #059669;
        }
        
        .action-buttons {
            display: flex;
            gap: 1rem;
            margin-top: 2rem;
            flex-wrap: wrap;
        }
        
        .damage-notes, .fee-input {
            width: 100%;
            padding: 1rem;
            border: 1px solid var(--gray-300);
            border-radius: 8px;
            background: white;
            color: var(--gray-900);
            font-family: inherit;
            font-size: 0.875rem;
            transition: var(--transition);
        }
        
        .damage-notes {
            min-height: 120px;
            resize: vertical;
        }
        
        .damage-notes:focus, .fee-input:focus {
            outline: none;
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }
        
        @media (max-width: 768px) {
            .info-grid {
                grid-template-columns: 1fr;
            }
            
            .inspection-grid {
                grid-template-columns: 1fr;
            }
            
            .refund-options {
                grid-template-columns: 1fr;
            }
            
            .action-buttons {
                flex-direction: column;
            }
        }
    </style>
</head>
<body class="admin">
    <fmt:setLocale value="vi_VN"/>
    
    <!-- Sidebar Navigation -->
    <aside class="sidebar">
        <div class="brand">
            <div class="brand-logo">
                <i class="fas fa-motorcycle"></i>
            </div>
            <h1>RideNow Admin</h1>
        </div>
        
        <nav class="sidebar-nav">
            <a href="${pageContext.request.contextPath}/admin/dashboard" class="nav-item">
                <i class="fas fa-tachometer-alt"></i>
                <span>Dashboard</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/partners" class="nav-item">
                <i class="fas fa-handshake"></i>
                <span>Partners</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/customers" class="nav-item">
                <i class="fas fa-users"></i>
                <span>Customers</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/bikes" class="nav-item">
                <i class="fas fa-motorcycle"></i>
                <span>Motorbikes</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/orders" class="nav-item">
                <i class="fas fa-clipboard-list"></i>
                <span>Orders</span>
            </a>
            <a href="${pageContext.request.contextPath}/adminpaymentverify" class="nav-item">
                <i class="fas fa-money-check-alt"></i>
                <span>Verify Payments</span>
            </a>
            <a href="${pageContext.request.contextPath}/adminpickup" class="nav-item">
                <i class="fas fa-shipping-fast"></i>
                <span>Vehicle Pickup</span>
            </a>
            <a href="${pageContext.request.contextPath}/adminreturn" class="nav-item">
                <i class="fas fa-undo-alt"></i>
                <span>Vehicle Return</span>
            </a>
            <a href="${pageContext.request.contextPath}/adminreturns" class="nav-item active">
                <i class="fas fa-clipboard-check"></i>
                <span>Verify & Refund</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/reports" class="nav-item">
                <i class="fas fa-chart-bar"></i>
                <span>Reports</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/feedback" class="nav-item">
                <i class="fas fa-comment-alt"></i>
                <span>Feedback</span>
            </a>
            <a href="${pageContext.request.contextPath}/logout" class="nav-item logout">
                <i class="fas fa-sign-out-alt"></i>
                <span>Logout</span>
            </a>
        </nav>
    </aside>

    <!-- Main Content -->
    <main class="content">
        <header class="content-header">
            <div class="header-left">
                <h1>Kiểm Tra Xe & Hoàn Cọc</h1>
                <div class="breadcrumb">
                    <span>Admin</span>
                    <i class="fas fa-chevron-right"></i>
                    <span>Quản lý Đơn hàng</span>
                    <i class="fas fa-chevron-right"></i>
                    <span class="active">Kiểm Tra & Hoàn Cọc</span>
                </div>
            </div>
            <div class="header-right">
                <div class="user-profile">
                    <div class="user-avatar">
                        <i class="fas fa-user-circle"></i>
                    </div>
                    <span>Administrator</span>
                </div>
            </div>
        </header>

        <c:if test="${not empty sessionScope.flash}">
            <div class="notice">
                <i class="fas fa-info-circle"></i>
                ${sessionScope.flash}
            </div>
            <c:remove var="flash" scope="session"/>
        </c:if>

        <!-- Order Information -->
        <section class="panel">
            <div class="panel-header">
                <h2>
                    <i class="fas fa-info-circle"></i>
                    Thông Tin Đơn Hàng
                </h2>
            </div>
            <div class="panel-body">
                <div class="info-grid">
                    <div class="info-card">
                        <div class="info-item">
                            <strong>Mã đơn:</strong> #${order.orderId}
                        </div>
                        <div class="info-item">
                            <strong>Khách hàng:</strong> ${order.customerName}
                        </div>
                        <div class="info-item">
                            <strong>Điện thoại:</strong> ${order.customerPhone}
                        </div>
                    </div>
                    <div class="info-card">
                        <div class="info-item">
                            <strong>Xe thuê:</strong> ${order.bikeName}
                        </div>
                        <div class="info-item">
                            <strong>Tiền cọc:</strong> 
                            <span style="color: #059669; font-weight: 600;">
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

        <!-- Vehicle Inspection Form -->
        <form method="post" action="${pageContext.request.contextPath}/adminreturninspect">
            <input type="hidden" name="orderId" value="${order.orderId}">
            
            <section class="panel">
                <div class="panel-header">
                    <h2>
                        <i class="fas fa-clipboard-check"></i>
                        Kiểm Tra Tình Trạng Xe
                    </h2>
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

                        <div id="damageSection" style="display:none; margin-top:2rem;">
                            <h4>Chi tiết hư hỏng & Phí</h4>
                            <textarea name="damageNotes" placeholder="Mô tả chi tiết hư hỏng (vị trí, mức độ hư hỏng, ước tính chi phí sửa chữa)..." class="damage-notes"></textarea>
                            <div style="margin-top:1rem;">
                                <label>Phí sửa chữa (VNĐ):</label>
                                <input type="number" name="damageFee" min="0" max="${order.depositAmount}" 
                                       step="1000" class="fee-input" placeholder="0" value="0">
                                <small class="text-muted">Số tiền tối đa: <fmt:formatNumber value="${order.depositAmount}" type="currency"/></small>
                            </div>
                        </div>

                        <!-- Refund Calculation -->
                        <div class="calculation-section" id="refundCalculation" style="display:none;">
                            <h4>Kết Quả Tính Toán</h4>
                            <div class="info-grid">
                                <div>
                                    <div class="info-item">
                                        <strong>Tiền cọc:</strong> 
                                        <span style="color: #3b82f6;">
                                            <fmt:formatNumber value="${order.depositAmount}" type="currency"/>
                                        </span>
                                    </div>
                                    <div class="info-item">
                                        <strong>Phí hư hỏng:</strong> 
                                        <span id="displayDamageFee" style="color: #dc2626;">0 VNĐ</span>
                                    </div>
                                </div>
                                <div>
                                    <strong style="font-size: 1.1em;">Tiền hoàn cọc:</strong><br>
                                    <span id="displayRefundAmount">0 VNĐ</span>
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
                        <button type="submit" class="btn btn-primary">
                            <i class="fas fa-check-circle"></i>
                            Xác Nhận Kiểm Tra & Tạo Yêu Cầu Hoàn Cọc
                        </button>
                        <a href="${pageContext.request.contextPath}/adminreturns" class="btn" style="background: var(--gray-100); color: var(--gray-700);">
                            <i class="fas fa-arrow-left"></i>
                            Quay Lại
                        </a>
                    </div>
                </div>
            </section>
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
                refundElement.style.color = '#ef4444';
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