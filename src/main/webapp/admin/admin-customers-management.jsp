<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Khách hàng - RideNow Admin</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
    <style>
        /* Enhanced Styles for Customers Management */
        .customers-header {
            display: flex;
            justify-content: between;
            align-items: center;
            margin-bottom: 1.5rem;
        }

        .header-stats {
            display: flex;
            gap: 1rem;
            margin-bottom: 1.5rem;
        }

        .stat-badge {
            background: var(--card-bg);
            border: 1px solid var(--border-color);
            border-radius: 8px;
            padding: 0.75rem 1.5rem;
            display: flex;
            align-items: center;
            gap: 0.5rem;
            font-size: 0.875rem;
            font-weight: 500;
        }

        .stat-badge .count {
            font-weight: 700;
            color: var(--primary-color);
        }

        .filter-form {
            background: var(--card-bg);
            border-radius: 12px;
            padding: 1.5rem;
            border: 1px solid var(--border-color);
        }

        .form-row {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 1rem;
            margin-bottom: 1rem;
        }

        .form-group {
            display: flex;
            flex-direction: column;
        }

        .form-group label {
            font-weight: 600;
            margin-bottom: 0.5rem;
            color: var(--text-color);
            font-size: 0.875rem;
        }

        .form-group input,
        .form-group select {
            padding: 0.75rem;
            border: 1px solid var(--border-color);
            border-radius: 6px;
            background: white;
            font-size: 0.875rem;
            transition: all 0.3s ease;
        }

        .form-group input:focus,
        .form-group select:focus {
            outline: none;
            border-color: var(--primary-color);
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }

        .form-actions {
            display: flex;
            gap: 0.75rem;
            justify-content: flex-end;
            padding-top: 1rem;
            border-top: 1px solid var(--border-color);
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
            text-transform: uppercase;
            letter-spacing: 0.5px;
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

        .status-badge {
            padding: 0.25rem 0.75rem;
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

        .action-buttons {
            display: flex;
            gap: 0.5rem;
            flex-wrap: wrap;
        }

        .btn {
            padding: 0.5rem 1rem;
            border-radius: 6px;
            font-size: 0.75rem;
            font-weight: 600;
            text-decoration: none;
            border: none;
            cursor: pointer;
            transition: all 0.3s ease;
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
        }

        .btn-sm {
            padding: 0.375rem 0.75rem;
            font-size: 0.7rem;
        }

        .btn-info {
            background: #dbeafe;
            color: #1e40af;
            border: 1px solid #bfdbfe;
        }

        .btn-info:hover {
            background: #bfdbfe;
            transform: translateY(-1px);
        }

        .btn-warning {
            background: #fef3c7;
            color: #92400e;
            border: 1px solid #fde68a;
        }

        .btn-warning:hover {
            background: #fde68a;
            transform: translateY(-1px);
        }

        .btn-success {
            background: #dcfce7;
            color: #166534;
            border: 1px solid #bbf7d0;
        }

        .btn-success:hover {
            background: #bbf7d0;
            transform: translateY(-1px);
        }

        .btn-wallet {
            background: #f0f9ff;
            color: #0369a1;
            border: 1px solid #bae6fd;
        }

        .btn-wallet:hover {
            background: #e0f2fe;
            transform: translateY(-1px);
        }

        .pagination {
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 0.5rem;
            margin-top: 2rem;
            padding: 1rem;
        }

        .page-link {
            padding: 0.5rem 0.75rem;
            border: 1px solid var(--border-color);
            border-radius: 6px;
            text-decoration: none;
            color: var(--text-color);
            font-size: 0.875rem;
            transition: all 0.3s ease;
        }

        .page-link:hover {
            background: var(--primary-color);
            color: white;
            border-color: var(--primary-color);
        }

        .page-link.active {
            background: var(--primary-color);
            color: white;
            border-color: var(--primary-color);
        }

        .text-center {
            text-align: center;
        }

        .flash-message {
            padding: 1rem 1.5rem;
            border-radius: 8px;
            margin-bottom: 1.5rem;
            font-weight: 500;
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
            border-bottom: 1px solid rgba(255, 255, 255, 0.1);
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
            .form-row {
                grid-template-columns: 1fr;
            }

            .header-stats {
                flex-direction: column;
            }

            .action-buttons {
                flex-direction: column;
            }

            .data-table {
                font-size: 0.75rem;
            }

            .data-table th,
            .data-table td {
                padding: 0.5rem;
            }

            .wallet-details {
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
        <a href="${pageContext.request.contextPath}/admin/schedule" class="nav-item">
            <i class="fas fa-calendar-alt"></i><span>View Schedule</span>
        </a>
        <a href="${pageContext.request.contextPath}/adminpickup" class="nav-item">
            <i class="fas fa-shipping-fast"></i>
            <span>Vehicle Pickup</span>
        </a>
        <a href="${pageContext.request.contextPath}/adminreturn" class="nav-item">
            <i class="fas fa-undo-alt"></i>
            <span>Vehicle Return</span>
        </a>
        <a href="${pageContext.request.contextPath}/adminreturns" class="nav-item">
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
            <h1>Quản lý Khách hàng</h1>
            <div class="breadcrumb">
                <span>Admin</span>
                <i class="fas fa-chevron-right"></i>
                <span class="active">Customers</span>
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

    <!-- Flash -> Toast -->
    <c:if test="${not empty flash}">
        <div class="toast-container position-fixed top-0 end-0 p-3" style="z-index: 1080;">
            <div id="flashToast" class="toast align-items-center text-bg-success border-0" role="alert"
                 aria-live="assertive" aria-atomic="true" data-bs-delay="5000">
                <div class="d-flex">
                    <div class="toast-body">
                        <i class="fas fa-check-circle me-2"></i>${flash}
                    </div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto"
                            data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
            </div>
        </div>
    </c:if>

    <!-- Quick Stats -->
    <div class="header-stats">
        <div class="stat-badge">
            <i class="fas fa-users text-primary"></i>
            <span>Tổng khách hàng: <span class="count">${totalItems}</span></span>
        </div>
        <div class="stat-badge">
            <i class="fas fa-check-circle text-success"></i>
            <span>Đang hoạt động: <span class="count">
                    <c:set var="activeCount" value="${0}"/>
                    <c:forEach var="c" items="${customers}">
                        <c:if test="${!c.banned}">
                            <c:set var="activeCount" value="${activeCount + 1}"/>
                        </c:if>
                    </c:forEach>
                    ${activeCount}
                </span></span>
        </div>
        <div class="stat-badge">
            <i class="fas fa-ban text-danger"></i>
            <span>Đã khóa: <span class="count">${totalItems - activeCount}</span></span>
        </div>
        <div class="stat-badge">
            <i class="fas fa-wallet text-warning"></i>
            <span>Tổng ví: <span class="count">
                    <fmt:formatNumber value="${totalWalletBalance}" type="currency" currencyCode="VND"/>
                </span></span>
        </div>
    </div>

    <!-- Search and Filter -->
    <section class="panel">
        <div class="panel-header">
            <h2><i class="fas fa-search"></i> Tìm kiếm & Lọc</h2>
        </div>
        <div class="panel-body">
            <form method="get" class="filter-form">
                <div class="form-row">
                    <div class="form-group">
                        <label for="q"><i class="fas fa-search"></i> Tìm kiếm</label>
                        <input type="text" id="q" name="q" value="${param.q}"
                               placeholder="Tên, email hoặc số điện thoại...">
                    </div>
                    <div class="form-group">
                        <label for="status"><i class="fas fa-filter"></i> Trạng thái</label>
                        <select id="status" name="status">
                            <option value="all" ${param.status == 'all' ? 'selected' : ''}>Tất cả</option>
                            <option value="active" ${param.status == 'active' ? 'selected' : ''}>Active</option>
                            <option value="banned" ${param.status == 'banned' ? 'selected' : ''}>Banned</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="walletFilter"><i class="fas fa-wallet"></i> Số dư ví</label>
                        <select id="walletFilter" name="walletFilter">
                            <option value="all" ${param.walletFilter == 'all' ? 'selected' : ''}>Tất cả</option>
                            <option value="has_balance" ${param.walletFilter == 'has_balance' ? 'selected' : ''}>Có số dư</option>
                            <option value="no_balance" ${param.walletFilter == 'no_balance' ? 'selected' : ''}>Không có số dư</option>
                            <option value="high_balance" ${param.walletFilter == 'high_balance' ? 'selected' : ''}>Số dư cao (&gt; 1M)</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="sort"><i class="fas fa-sort"></i> Sắp xếp</label>
                        <select id="sort" name="sort">
                            <option value="createdAt" ${param.sort == 'createdAt' ? 'selected' : ''}>Ngày tạo</option>
                            <option value="name" ${param.sort == 'name' ? 'selected' : ''}>Tên</option>
                            <option value="orders" ${param.sort == 'orders' ? 'selected' : ''}>Số đơn</option>
                            <option value="wallet" ${param.sort == 'wallet' ? 'selected' : ''}>Số dư ví</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="dir"><i class="fas fa-sort-amount-down"></i> Thứ tự</label>
                        <select id="dir" name="dir">
                            <option value="desc" ${param.dir == 'desc' ? 'selected' : ''}>Giảm dần</option>
                            <option value="asc" ${param.dir == 'asc' ? 'selected' : ''}>Tăng dần</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="pageSize"><i class="fas fa-list"></i> Hiển thị</label>
                        <select id="pageSize" name="pageSize">
                            <option value="5" ${param.pageSize == 5 ? 'selected' : ''}>5</option>
                            <option value="10" ${param.pageSize == 10 ? 'selected' : ''}>10</option>
                            <option value="20" ${param.pageSize == 20 ? 'selected' : ''}>20</option>
                            <option value="50" ${param.pageSize == 50 ? 'selected' : ''}>50</option>
                        </select>
                    </div>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn btn-primary">
                        <i class="fas fa-search"></i> Tìm kiếm
                    </button>
                    <a href="${pageContext.request.contextPath}/admin/customers" class="btn btn-secondary">
                        <i class="fas fa-refresh"></i> Reset
                    </a>
                </div>
            </form>
        </div>
    </section>

    <!-- Customers Table -->
    <section class="panel">
        <div class="panel-header">
            <h2><i class="fas fa-list"></i> Danh sách Khách hàng</h2>
            <div class="total-badge">
                ${totalItems} kết quả
            </div>
        </div>
        <div class="panel-body">
            <div class="table-container">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Họ tên</th>
                        <th>Email</th>
                        <th>SĐT</th>
                        <th>Số đơn</th>
                        <th>Số dư ví</th>
                        <th>Trạng thái</th>
                        <th>Ngày tạo</th>
                        <th>Hành động</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="c" items="${customers}">
                        <tr>
                            <td><strong>#${c.id}</strong></td>
                            <td>
                                <div class="customer-name">
                                    <div class="avatar-small">
                                        ${c.fullName.charAt(0)}
                                    </div>
                                    ${c.fullName}
                                </div>
                            </td>
                            <td>${c.email}</td>
                            <td>${c.phone}</td>
                            <td>
                                <span class="order-count">${c.orders}</span>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${c.wallet > 0}">
                                        <span class="wallet-amount positive">
                                            <i class="fas fa-wallet"></i>
                                            <fmt:formatNumber value="${c.wallet}" type="currency" currencyCode="VND"/>
                                        </span>
                                    </c:when>
                                    <c:when test="${c.wallet < 0}">
                                        <span class="wallet-amount negative">
                                            <i class="fas fa-wallet"></i>
                                            <fmt:formatNumber value="${c.wallet}" type="currency" currencyCode="VND"/>
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="wallet-amount zero">
                                            <i class="fas fa-wallet"></i>
                                            <fmt:formatNumber value="${c.wallet}" type="currency" currencyCode="VND"/>
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <span class="status-badge ${c.banned ? 'danger' : 'success'}">
                                    <i class="fas ${c.banned ? 'fa-ban' : 'fa-check'}"></i>
                                    ${c.banned ? 'BANNED' : 'ACTIVE'}
                                </span>
                            </td>
                            <td>
                                <fmt:formatDate value="${c.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                            </td>
                            <td>
                                <div class="action-buttons">
                                    <a href="${pageContext.request.contextPath}/admin/customers/detail?id=${c.id}"
                                       class="btn btn-sm btn-info">
                                        <i class="fas fa-eye"></i> Chi tiết
                                    </a>
                                    <form method="post" style="display: inline;"
                                          class="needs-confirm"
                                          data-confirm-message="${c.banned ? 'Bạn có chắc chắn muốn mở khóa tài khoản này?' : 'Bạn có chắc chắn muốn khóa tài khoản này?'}">
                                        <input type="hidden" name="action" value="toggle">
                                        <input type="hidden" name="customerId" value="${c.id}">
                                        <button type="submit"
                                                class="btn btn-sm ${c.banned ? 'btn-success' : 'btn-warning'}">
                                            <i class="fas ${c.banned ? 'fa-unlock' : 'fa-lock'}"></i>
                                            ${c.banned ? 'Unban' : 'Ban'}
                                        </button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty customers}">
                        <tr>
                            <td colspan="9" class="text-center">
                                <div class="empty-state">
                                    <i class="fas fa-users fa-3x"></i>
                                    <h3>Không tìm thấy khách hàng nào</h3>
                                    <p>Hãy thử điều chỉnh bộ lọc tìm kiếm của bạn</p>
                                </div>
                            </td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>

            <!-- Pagination -->
            <c:if test="${totalPages > 1}">
                <div class="pagination">
                    <c:if test="${page > 1}">
                        <a href="?page=${page-1}&pageSize=${pageSize}&q=${param.q}&status=${param.status}&walletFilter=${param.walletFilter}&sort=${param.sort}&dir=${param.dir}"
                           class="page-link">
                            <i class="fas fa-chevron-left"></i>
                        </a>
                    </c:if>

                    <c:forEach begin="1" end="${totalPages}" var="i">
                        <c:choose>
                            <c:when test="${i == page}">
                                <span class="page-link active">${i}</span>
                            </c:when>
                            <c:otherwise>
                                <a href="?page=${i}&pageSize=${pageSize}&q=${param.q}&status=${param.status}&walletFilter=${param.walletFilter}&sort=${param.sort}&dir=${param.dir}"
                                   class="page-link">${i}</a>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>

                    <c:if test="${page < totalPages}">
                        <a href="?page=${page+1}&pageSize=${pageSize}&q=${param.q}&status=${param.status}&walletFilter=${param.walletFilter}&sort=${param.sort}&dir=${param.dir}"
                           class="page-link">
                            <i class="fas fa-chevron-right"></i>
                        </a>
                    </c:if>
                </div>
            </c:if>
        </div>
    </section>
</main>

<style>
    .customer-name {
        display: flex;
        align-items: center;
        gap: 0.5rem;
    }

    .avatar-small {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        background: var(--primary-color);
        color: white;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 0.75rem;
        font-weight: 600;
    }

    .order-count {
        background: #dbeafe;
        color: #1e40af;
        padding: 0.25rem 0.5rem;
        border-radius: 12px;
        font-weight: 600;
        font-size: 0.75rem;
    }

    .empty-state {
        padding: 3rem;
        text-align: center;
        color: var(--text-light);
    }

    .empty-state i {
        margin-bottom: 1rem;
        color: #cbd5e1;
    }

    .empty-state h3 {
        margin-bottom: 0.5rem;
        color: var(--text-color);
    }

    .total-badge {
        background: var(--primary-color);
        color: white;
        padding: 0.5rem 1rem;
        border-radius: 20px;
        font-size: 0.875rem;
        font-weight: 600;
    }

    .text-primary {
        color: var(--primary-color);
    }

    .text-success {
        color: #059669;
    }

    .text-danger {
        color: #dc2626;
    }

    .text-warning {
        color: #d97706;
    }
</style>

<!-- Toast confirm -->
<div class="toast-container position-fixed bottom-0 end-0 p-3" style="z-index: 1080;">
    <div id="confirmToast" class="toast" role="alert" aria-live="assertive" aria-atomic="true"
         data-bs-autohide="false">
        <div class="toast-header">
            <i class="fas fa-question-circle me-2 text-warning"></i>
            <strong class="me-auto">Xác nhận</strong>
            <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
        <div class="toast-body">
            <span id="confirmToastMessage"></span>
            <div class="mt-2 pt-2 border-top">
                <button type="button" class="btn btn-sm btn-primary me-2" id="confirmToastYes">Đồng ý</button>
                <button type="button" class="btn btn-sm btn-secondary" id="confirmToastNo" data-bs-dismiss="toast">Hủy</button>
            </div>
        </div>
    </div>
</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/js/bootstrap.bundle.min.js"></script>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        console.log('Customers management page loaded');

        // Flash toast
        const flashToastEl = document.getElementById('flashToast');
        if (flashToastEl && typeof bootstrap !== 'undefined') {
            const flashToast = new bootstrap.Toast(flashToastEl);
            flashToast.show();
        }

        // Confirm toast setup
        const confirmToastEl = document.getElementById('confirmToast');
        let confirmToastInstance = null;
        let confirmToastCallback = null;
        const msgSpan = document.getElementById('confirmToastMessage');
        const yesBtn = document.getElementById('confirmToastYes');
        const noBtn = document.getElementById('confirmToastNo');

        if (confirmToastEl && typeof bootstrap !== 'undefined') {
            confirmToastInstance = new bootstrap.Toast(confirmToastEl, { autohide: false });

            if (yesBtn) {
                yesBtn.addEventListener('click', function () {
                    if (typeof confirmToastCallback === 'function') {
                        confirmToastCallback();
                    }
                    confirmToastCallback = null;
                    confirmToastInstance.hide();
                });
            }

            if (noBtn) {
                noBtn.addEventListener('click', function () {
                    confirmToastCallback = null;
                });
            }

            // Attach to forms with class needs-confirm
            document.querySelectorAll('form.needs-confirm').forEach(function (form) {
                form.addEventListener('submit', function (e) {
                    if (form.dataset.confirmed === 'true') {
                        return;
                    }
                    e.preventDefault();
                    const msg = form.dataset.confirmMessage || 'Xác nhận thực hiện hành động này?';
                    if (msgSpan) {
                        msgSpan.textContent = msg;
                    }
                    confirmToastCallback = function () {
                        form.dataset.confirmed = 'true';
                        form.submit();
                    };
                    confirmToastInstance.show();
                });
            });
        }
    });
</script>
</body>
</html>
