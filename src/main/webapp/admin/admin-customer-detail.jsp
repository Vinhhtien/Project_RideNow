<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết Khách hàng - RideNow Admin</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
    <style>
        /* Enhanced Styles for Customer Detail */
        .customer-profile {
            display: flex;
            gap: 2rem;
            align-items: flex-start;
            background: white;
            border-radius: 12px;
            padding: 2rem;
            border: 1px solid var(--border-color);
        }
        
        .profile-avatar {
            flex-shrink: 0;
        }
        
        .avatar-placeholder {
            width: 100px;
            height: 100px;
            border-radius: 50%;
            background: linear-gradient(135deg, var(--primary-color), #3b82f6);
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 2.5rem;
            font-weight: bold;
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
        }
        
        .profile-info {
            flex: 1;
        }
        
        .customer-header {
            display: flex;
            justify-content: between;
            align-items: flex-start;
            margin-bottom: 1.5rem;
        }
        
        .customer-name {
            font-size: 1.5rem;
            font-weight: 700;
            color: var(--text-color);
            margin-bottom: 0.5rem;
        }
        
        .customer-id {
            color: var(--text-light);
            font-size: 0.875rem;
        }
        
        .info-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: 1rem;
        }
        
        .info-item {
            display: flex;
            align-items: flex-start;
            gap: 0.75rem;
            padding: 0.75rem;
            background: #f8fafc;
            border-radius: 8px;
            border: 1px solid #e2e8f0;
        }
        
        .info-item label {
            font-weight: 600;
            min-width: 120px;
            color: var(--text-color);
            font-size: 0.875rem;
        }
        
        .info-item span:not(.status-badge) {
            color: var(--text-color);
            flex: 1;
        }
        
        .info-icon {
            width: 16px;
            color: var(--primary-color);
        }
        
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 1.5rem;
            margin-top: 1rem;
        }
        
        .stat-card {
            background: white;
            border: 1px solid var(--border-color);
            border-radius: 12px;
            padding: 1.5rem;
            text-align: center;
            transition: transform 0.3s ease, box-shadow 0.3s ease;
            position: relative;
            overflow: hidden;
        }
        
        .stat-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(0,0,0,0.1);
        }
        
        .stat-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 4px;
            background: linear-gradient(90deg, var(--primary-color), #3b82f6);
        }
        
        .stat-icon {
            width: 48px;
            height: 48px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 1rem;
            font-size: 1.25rem;
        }
        
        .stat-icon.wallet { background: #dbeafe; color: #1e40af; }
        .stat-icon.orders { background: #dcfce7; color: #166534; }
        .stat-icon.spent { background: #fef3c7; color: #92400e; }
        .stat-icon.last-order { background: #e0e7ff; color: #3730a3; }
        
        .stat-value {
            font-size: 1.75rem;
            font-weight: 800;
            color: var(--text-color);
            margin-bottom: 0.5rem;
        }
        
        .stat-label {
            color: var(--text-light);
            font-size: 0.875rem;
            font-weight: 500;
        }
        
        .action-buttons {
            display: flex;
            gap: 1rem;
            margin-top: 1.5rem;
        }
        
        .btn {
            padding: 0.75rem 1.5rem;
            border-radius: 8px;
            font-size: 0.875rem;
            font-weight: 600;
            text-decoration: none;
            border: none;
            cursor: pointer;
            transition: all 0.3s ease;
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
        }
        
        .btn-warning {
            background: linear-gradient(135deg, #f59e0b, #d97706);
            color: white;
        }
        
        .btn-warning:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
        }
        
        .btn-success {
            background: linear-gradient(135deg, #10b981, #059669);
            color: white;
        }
        
        .btn-success:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
        }
        
        .btn-secondary {
            background: #6b7280;
            color: white;
        }
        
        .btn-secondary:hover {
            background: #4b5563;
            transform: translateY(-2px);
        }
        
        .btn-wallet {
            background: linear-gradient(135deg, #3b82f6, #1e40af);
            color: white;
        }
        
        .btn-wallet:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
        }
        
        .status-badge {
            padding: 0.375rem 0.75rem;
            border-radius: 20px;
            font-size: 0.75rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        
        .status-badge.success {
            background: #dcfce7;
            color: #166534;
            border: 1px solid #bbf7d0;
        }
        
        .status-badge.danger {
            background: #fee2e2;
            color: #991b1b;
            border: 1px solid #fecaca;
        }
        
        .status-badge.warning {
            background: #fef3c7;
            color: #92400e;
            border: 1px solid #fde68a;
        }
        
        .status-badge.info {
            background: #dbeafe;
            color: #1e40af;
            border: 1px solid #bfdbfe;
        }
        
        .table-container {
            background: white;
            border-radius: 12px;
            border: 1px solid var(--border-color);
            overflow: hidden;
        }
        
        .data-table {
            width: 100%;
            border-collapse: collapse;
        }
        
        .data-table th {
            background: #f8fafc;
            padding: 1rem;
            text-align: left;
            font-weight: 600;
            color: var(--text-color);
            border-bottom: 1px solid var(--border-color);
            font-size: 0.875rem;
        }
        
        .data-table td {
            padding: 1rem;
            border-bottom: 1px solid #f1f5f9;
            font-size: 0.875rem;
        }
        
        .data-table tr:hover {
            background: #f8fafc;
        }
        
        .data-table tr:last-child td {
            border-bottom: none;
        }
        
        .empty-state {
            padding: 3rem;
            text-align: center;
            color: var(--text-light);
        }
        
        .empty-state i {
            margin-bottom: 1rem;
            color: #cbd5e1;
            font-size: 3rem;
        }
        
        .empty-state h3 {
            margin-bottom: 0.5rem;
            color: var(--text-color);
        }
        
        .flash-message {
            padding: 1rem 1.5rem;
            border-radius: 8px;
            margin-bottom: 1.5rem;
            font-weight: 500;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }
        
        .flash-message.success {
            background: #dcfce7;
            color: #166534;
            border: 1px solid #bbf7d0;
        }
        
        .flash-message.error {
            background: #fee2e2;
            color: #991b1b;
            border: 1px solid #fecaca;
        }
        
        /* Wallet specific styles */
        .wallet-info {
            background: linear-gradient(135deg, #1a237e, #3949ab);
            color: white;
            border-radius: 10px;
            padding: 20px;
            margin: 15px 0;
            box-shadow: 0 4px 12px rgba(26, 35, 126, 0.2);
        }
        
        .wallet-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 15px;
        }
        
        .wallet-title {
            font-size: 1.2rem;
            font-weight: 600;
        }
        
        .wallet-balance {
            font-size: 2rem;
            font-weight: 700;
            margin: 10px 0;
        }
        
        .wallet-details {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
            margin-top: 15px;
        }
        
        .wallet-item {
            display: flex;
            justify-content: space-between;
            padding: 10px 0;
            border-bottom: 1px solid rgba(255,255,255,0.1);
        }
        
        .wallet-amount {
            font-weight: 600;
        }
        
        .wallet-amount.positive {
            color: #10b981;
        }
        
        .wallet-amount.negative {
            color: #ef4444;
        }
        
        .wallet-amount.zero {
            color: #6b7280;
        }
        
        /* Responsive Design */
        @media (max-width: 768px) {
            .customer-profile {
                flex-direction: column;
                text-align: center;
            }
            
            .info-grid {
                grid-template-columns: 1fr;
            }
            
            .stats-grid {
                grid-template-columns: repeat(2, 1fr);
            }
            
            .action-buttons {
                flex-direction: column;
            }
            
            .wallet-details {
                grid-template-columns: 1fr;
            }
        }
        
        @media (max-width: 480px) {
            .stats-grid {
                grid-template-columns: 1fr;
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
            <a href="${pageContext.request.contextPath}/admin/customers" class="nav-item active">
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
                <h1>Chi tiết Khách hàng</h1>
                <div class="breadcrumb">
                    <span>Admin</span>
                    <i class="fas fa-chevron-right"></i>
                    <a href="${pageContext.request.contextPath}/admin/customers">Customers</a>
                    <i class="fas fa-chevron-right"></i>
                    <span class="active">Chi tiết</span>
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

        <!-- Flash Message -->
        <c:if test="${not empty flash}">
            <div class="flash-message success">
                <i class="fas fa-check-circle"></i>
                ${flash}
            </div>
        </c:if>

        <c:if test="${empty detail}">
            <div class="flash-message error">
                <i class="fas fa-exclamation-triangle"></i>
                Không tìm thấy thông tin khách hàng hoặc có lỗi xảy ra.
            </div>
            <div class="text-center" style="padding: 2rem;">
                <a href="${pageContext.request.contextPath}/admin/customers" class="btn btn-secondary">
                    <i class="fas fa-arrow-left"></i> Quay lại danh sách khách hàng
                </a>
            </div>
        </c:if>

        <c:if test="${not empty detail}">
            <!-- Customer Info Card -->
            <section class="panel">
                <div class="panel-header">
                    <h2><i class="fas fa-user"></i> Thông tin cá nhân</h2>
                    <div style="display: flex; gap: 1rem;">
                        <form method="post" action="${pageContext.request.contextPath}/admin/customers" style="display: inline;">
                            <input type="hidden" name="action" value="toggle">
                            <input type="hidden" name="customerId" value="${detail.id}">
                            <button type="submit" class="btn ${detail.banned ? 'btn-success' : 'btn-warning'}">
                                <i class="fas ${detail.banned ? 'fa-unlock' : 'fa-lock'}"></i>
                                ${detail.banned ? 'UNBAN TÀI KHOẢN' : 'BAN TÀI KHOẢN'}
                            </button>
                        </form>
<!--                        <a href="${pageContext.request.contextPath}/admin/customers/wallet?id=${detail.id}" class="btn btn-wallet">
                            <i class="fas fa-wallet"></i> QUẢN LÝ VÍ
                        </a>-->
                    </div>
                </div>
                <div class="panel-body">
                    <div class="customer-profile">
                        <div class="profile-avatar">
                            <div class="avatar-placeholder">
                                ${detail.fullName.charAt(0)}
                            </div>
                        </div>
                        <div class="profile-info">
                            <div class="customer-header">
                                <div>
                                    <div class="customer-name">${detail.fullName}</div>
                                    <div class="customer-id">ID: #${detail.id}</div>
                                </div>
                            </div>
                            
                            <div class="info-grid">
                                <div class="info-item">
                                    <i class="fas fa-envelope info-icon"></i>
                                    <label>Email:</label>
                                    <span>${detail.email}</span>
                                    <span class="status-badge ${detail.emailVerified ? 'success' : 'warning'}">
                                        <i class="fas ${detail.emailVerified ? 'fa-check' : 'fa-exclamation'}"></i>
                                        ${detail.emailVerified ? 'Đã xác minh' : 'Chưa xác minh'}
                                    </span>
                                </div>
                                
                                <div class="info-item">
                                    <i class="fas fa-phone info-icon"></i>
                                    <label>SĐT:</label>
                                    <span>${not empty detail.phone ? detail.phone : 'N/A'}</span>
                                </div>
                                
                                <div class="info-item">
                                    <i class="fas fa-map-marker-alt info-icon"></i>
                                    <label>Địa chỉ:</label>
                                    <span>${not empty detail.address ? detail.address : 'N/A'}</span>
                                </div>
                                
                                <div class="info-item">
                                    <i class="fas fa-calendar info-icon"></i>
                                    <label>Ngày sinh:</label>
                                    <span>
                                        <c:if test="${not empty detail.dob}">
                                            <fmt:formatDate value="${detail.dob}" pattern="dd/MM/yyyy" />
                                        </c:if>
                                        <c:if test="${empty detail.dob}">N/A</c:if>
                                    </span>
                                </div>
                                
                                <div class="info-item">
                                    <i class="fas fa-calendar-plus info-icon"></i>
                                    <label>Ngày tạo:</label>
                                    <span><fmt:formatDate value="${detail.createdAt}" pattern="dd/MM/yyyy HH:mm" /></span>
                                </div>
                                
                                <div class="info-item">
                                    <i class="fas fa-sign-in-alt info-icon"></i>
                                    <label>Đăng nhập cuối:</label>
                                    <span>
                                        <c:if test="${not empty detail.lastLogin}">
                                            <fmt:formatDate value="${detail.lastLogin}" pattern="dd/MM/yyyy HH:mm" />
                                        </c:if>
                                        <c:if test="${empty detail.lastLogin}">N/A</c:if>
                                    </span>
                                </div>
                                
                                <div class="info-item">
                                    <i class="fas fa-flag info-icon"></i>
                                    <label>Trạng thái:</label>
                                    <span class="status-badge ${detail.banned ? 'danger' : 'success'}">
                                        <i class="fas ${detail.banned ? 'fa-ban' : 'fa-check'}"></i>
                                        ${detail.banned ? 'BANNED' : 'ACTIVE'}
                                    </span>
                                </div>
                            </div>
                            
                            <div class="action-buttons">
                                <a href="${pageContext.request.contextPath}/admin/customers" class="btn btn-secondary">
                                    <i class="fas fa-arrow-left"></i> Quay lại
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <!-- Wallet Information -->
            <section class="panel">
                <div class="panel-header">
                    <h2><i class="fas fa-wallet"></i> Thông tin Ví</h2>
                </div>
                <div class="panel-body">
                    <div class="wallet-info">
                        <div class="wallet-header">
                            <div class="wallet-title">Số dư ví</div>
                            <div class="wallet-balance">
                                <fmt:formatNumber value="${detail.wallet}" type="currency" currencyCode="VND" />
                            </div>
                        </div>
                        <div class="wallet-details">
                            <div class="wallet-item">
                                <span>Số dư hiện tại:</span>
                                <span class="wallet-amount ${detail.wallet > 0 ? 'positive' : detail.wallet < 0 ? 'negative' : 'zero'}">
                                    <fmt:formatNumber value="${detail.wallet}" type="currency" currencyCode="VND" />
                                </span>
                            </div>
                            <div class="wallet-item">
                                <span>Tổng chi tiêu:</span>
                                <span class="wallet-amount">
                                    <fmt:formatNumber value="${detail.totalSpent}" type="currency" currencyCode="VND" />
                                </span>
                            </div>
                            <div class="wallet-item">
                                <span>Số đơn hàng:</span>
                                <span>${detail.orders}</span>
                            </div>
                            <div class="wallet-item">
                                <span>Đơn hàng cuối:</span>
                                <span>
                                    <c:if test="${not empty detail.lastOrderAt}">
                                        <fmt:formatDate value="${detail.lastOrderAt}" pattern="dd/MM/yyyy HH:mm" />
                                    </c:if>
                                    <c:if test="${empty detail.lastOrderAt}">N/A</c:if>
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <!-- Statistics Card -->
            <section class="panel">
                <div class="panel-header">
                    <h2><i class="fas fa-chart-bar"></i> Thống kê</h2>
                </div>
                <div class="panel-body">
                    <div class="stats-grid">
                        <div class="stat-card">
                            <div class="stat-icon wallet">
                                <i class="fas fa-wallet"></i>
                            </div>
                            <div class="stat-value">
                                <fmt:formatNumber value="${detail.wallet}" type="currency" currencyCode="VND" />
                            </div>
                            <div class="stat-label">Số dư ví</div>
                        </div>
                        
                        <div class="stat-card">
                            <div class="stat-icon orders">
                                <i class="fas fa-shopping-bag"></i>
                            </div>
                            <div class="stat-value">${detail.orders}</div>
                            <div class="stat-label">Tổng đơn hàng</div>
                        </div>
                        
                        <div class="stat-card">
                            <div class="stat-icon spent">
                                <i class="fas fa-money-bill-wave"></i>
                            </div>
                            <div class="stat-value">
                                <fmt:formatNumber value="${detail.totalSpent}" type="currency" currencyCode="VND" />
                            </div>
                            <div class="stat-label">Tổng chi tiêu</div>
                        </div>
                        
                        <div class="stat-card">
                            <div class="stat-icon last-order">
                                <i class="fas fa-clock"></i>
                            </div>
                            <div class="stat-value">
                                <c:if test="${not empty detail.lastOrderAt}">
                                    <fmt:formatDate value="${detail.lastOrderAt}" pattern="dd/MM/yy" />
                                </c:if>
                                <c:if test="${empty detail.lastOrderAt}">N/A</c:if>
                            </div>
                            <div class="stat-label">Đơn cuối</div>
                        </div>
                    </div>
                </div>
            </section>

            <!-- Recent Orders -->
            <section class="panel">
                <div class="panel-header">
                    <h2><i class="fas fa-history"></i> 5 Đơn hàng gần nhất</h2>
                </div>
                <div class="panel-body">
                    <c:if test="${not empty detail.recentOrders}">
                        <div class="table-container">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Mã đơn</th>
                                        <th>Xe</th>
                                        <th>Tổng tiền</th>
                                        <th>Trạng thái</th>
                                        <th>Ngày tạo</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="order" items="${detail.recentOrders}">
                                        <tr>
                                            <td><strong>#${order.orderId}</strong></td>
                                            <td>${order.bikeName}</td>
                                            <td>
                                                <span style="font-weight: 600; color: #059669;">
                                                    <fmt:formatNumber value="${order.total}" type="currency" currencyCode="VND" />
                                                </span>
                                            </td>
                                            <td>
                                                <span class="status-badge info">
                                                    ${order.status}
                                                </span>
                                            </td>
                                            <td>
                                                <fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:if>
                    <c:if test="${empty detail.recentOrders}">
                        <div class="empty-state">
                            <i class="fas fa-clipboard-list"></i>
                            <h3>Không có đơn hàng nào</h3>
                            <p>Khách hàng này chưa có đơn hàng nào</p>
                        </div>
                    </c:if>
                </div>
            </section>
        </c:if>
    </main>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            console.log('Customer detail page loaded');
            
            // Add confirmation for ban/unban action
            const banButton = document.querySelector('button[class*="btn-warning"], button[class*="btn-success"]');
            if (banButton) {
                banButton.addEventListener('click', function(e) {
                    const isBan = this.classList.contains('btn-warning');
                    const message = isBan 
                        ? 'Bạn có chắc chắn muốn khóa tài khoản này?'
                        : 'Bạn có chắc chắn muốn mở khóa tài khoản này?';
                    
                    if (!confirm(message)) {
                        e.preventDefault();
                    }
                });
            }
        });
    </script>
</body>
</html>