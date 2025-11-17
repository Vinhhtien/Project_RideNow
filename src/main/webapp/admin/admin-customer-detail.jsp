<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết Khách hàng - RideNow Admin</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap"
          rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <!-- Bootstrap cho toast -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/css/bootstrap.min.css">
    <!-- Giữ admin.css để đồng bộ layout/side bar -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">

    <style>
        /* ===== Minimal, clean, readable ===== */
        :root {
            /* Surface */
            --bg: #f6f7fb;
            --panel: #ffffff;
            --card: #ffffff;
            --card-2: #ffffff;

            /* Text & border */
            --text: #0f172a;
            --muted: #475569;
            --line: #e5e7eb;
            --ring: rgba(37, 99, 235, .35);

            /* Accent */
            --primary: #2563eb;
            --primary-2: #2563eb;

            /* State */
            --success: #16a34a;
            --warning: #d97706;
            --danger: #dc2626;
            --info: #0ea5e9;

            /* Radius & shadow */
            --radius: 14px;
            --radius-sm: 10px;
            --shadow-1: 0 6px 16px rgba(2, 6, 23, .08);
            --shadow-2: 0 10px 24px rgba(2, 6, 23, .10);
            --blur: none;
        }

        body.admin {
            background: var(--bg);
            color: var(--text);
            font-family: 'Inter', system-ui, -apple-system, Segoe UI, Roboto, 'Helvetica Neue', Arial, "Noto Sans";
            letter-spacing: .1px;
        }

        .customer-detail-container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 1rem;
        }

        .customer-header {
            position: relative;
            border-radius: var(--radius);
            padding: 20px 24px;
            margin: 1.25rem 0 1.5rem;
            background: var(--panel);
            border: 1px solid var(--line);
            box-shadow: var(--shadow-1);
            overflow: hidden;
        }

        .customer-header:before {
            content: none;
        }

        .header-content {
            display: flex;
            justify-content: space-between;
            gap: 1rem;
            align-items: flex-start;
            flex-wrap: wrap
        }

        .customer-identity {
            display: flex;
            align-items: center;
            gap: 1rem
        }

        .avatar-xl {
            width: 72px;
            height: 72px;
            border-radius: 50%;
            display: grid;
            place-items: center;
            font-size: 1.5rem;
            font-weight: 800;
            color: #fff;
            background: var(--primary);
            border: none;
            box-shadow: none;
        }

        .customer-titles h1 {
            margin: 0 0 .25rem 0;
            font-size: clamp(20px, 2.1vw, 26px);
            font-weight: 800;
            color: var(--text)
        }

        .customer-subtitle {
            display: flex;
            align-items: center;
            gap: .5rem;
            flex-wrap: wrap;
            opacity: .95
        }

        .customer-id {
            background: #eef2ff;
            border: 1px solid #e0e7ff;
            color: #334155;
            padding: .28rem .65rem;
            border-radius: 999px;
            font-size: .82rem;
            font-weight: 600
        }

        .header-actions {
            display: flex;
            gap: .6rem;
            flex-wrap: wrap
        }

        .customer-grid {
            display: grid;
            gap: 1.25rem;
            grid-template-columns:minmax(0, 1fr) 340px;
        }

        @media (max-width: 1100px) {
            .customer-grid {
                grid-template-columns:1fr;
            }
        }

        .card {
            background: var(--card);
            border: 1px solid var(--line);
            border-radius: var(--radius);
            box-shadow: var(--shadow-1);
            overflow: hidden;
            transition: transform .12s ease, box-shadow .12s ease;
        }

        .card:hover {
            transform: translateY(-1px);
            box-shadow: var(--shadow-2);
        }

        .card-header {
            padding: 14px 16px;
            border-bottom: 1px solid var(--line);
            background: #fafafa;
        }

        .card-header h2 {
            margin: 0;
            font-size: 1rem;
            font-weight: 700;
            color: #111827;
            display: flex;
            align-items: center;
            gap: .6rem;
        }

        .card-body {
            padding: 14px 16px;
        }

        .info-grid {
            display: grid;
            gap: .75rem
        }

        .info-row {
            display: flex;
            gap: .75rem;
            align-items: flex-start;
            padding: .75rem .8rem;
            border-radius: var(--radius-sm);
            background: #ffffff;
            border: 1px solid var(--line);
            transition: background .1s ease, border-color .1s ease;
        }

        .info-row:hover {
            background: #f8fafc;
            border-color: #e2e8f0;
        }

        .info-icon {
            width: 18px;
            color: var(--primary);
            margin-top: .1rem
        }

        .info-content {
            flex: 1;
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            gap: 1rem
        }

        .info-label {
            min-width: 140px;
            font-weight: 700;
            color: #111827
        }

        .info-value {
            color: var(--muted)
        }

        .status-badge {
            display: inline-flex;
            align-items: center;
            gap: .4rem;
            padding: .35rem .7rem;
            border-radius: 999px;
            font-size: .72rem;
            font-weight: 800;
            letter-spacing: .3px;
            text-transform: uppercase;
            white-space: nowrap;
            border: 1px solid transparent;
        }

        .status-badge.success {
            background: #e9fbef;
            border-color: #b7f0c7;
            color: #166534;
        }

        .status-badge.danger {
            background: #fee2e2;
            border-color: #fecaca;
            color: #991b1b;
        }

        .status-badge.warning {
            background: #fef3c7;
            border-color: #fde68a;
            color: #92400e;
        }

        .status-badge.info {
            background: #e0f2fe;
            border-color: #bae6fd;
            color: #0c4a6e;
        }

        .btn {
            appearance: none;
            border: none;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: .5rem;
            padding: .7rem .95rem;
            border-radius: 10px;
            font-weight: 700;
            font-size: .9rem;
            transition: transform .1s ease, box-shadow .1s ease, background .1s ease;
            outline: none;
        }

        .btn:focus-visible {
            box-shadow: 0 0 0 3px var(--ring);
        }

        .btn:hover {
            transform: translateY(-1px);
        }

        .btn-warning {
            color: #fff;
            background: #f59e0b;
        }

        .btn-warning:hover {
            box-shadow: 0 8px 18px rgba(245, 158, 11, .25);
        }

        .btn-success {
            color: #fff;
            background: #16a34a;
        }

        .btn-success:hover {
            box-shadow: 0 8px 18px rgba(22, 163, 74, .25);
        }

        .btn-secondary {
            color: #0f172a;
            background: #e5e7eb;
        }

        .btn-secondary:hover {
            background: #dfe3e8;
            box-shadow: 0 6px 14px rgba(2, 6, 23, .08);
        }

        .wallet-card {
            position: relative;
            border-radius: 12px;
            padding: 14px 12px;
            background: #0f172a;
            color: #f8fafc;
            border: 1px solid #0f172a;
            box-shadow: var(--shadow-1);
        }

        .wallet-card:after {
            content: none;
        }

        .wallet-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: .4rem
        }

        .wallet-title {
            font-weight: 700
        }

        .wallet-balance {
            font-size: clamp(26px, 3vw, 36px);
            font-weight: 900;
            margin: .2rem 0 .8rem
        }

        .wallet-details {
            display: grid;
            gap: .45rem
        }

        .wallet-item {
            display: flex;
            justify-content: space-between;
            padding: .55rem 0;
            border-bottom: 1px dashed rgba(255, 255, 255, .15)
        }

        .wallet-item:last-child {
            border-bottom: none
        }

        .wallet-amount {
            font-weight: 800
        }

        .wallet-amount.positive {
            color: #86efac
        }

        .wallet-amount.negative {
            color: #fecaca
        }

        .wallet-amount.zero {
            color: #e5e7eb
        }

        .stats-grid {
            display: grid;
            grid-template-columns:repeat(2, minmax(0, 1fr));
            gap: .9rem;
            margin-top: .2rem
        }

        .stat-card {
            text-align: center;
            border-radius: 12px;
            padding: 1rem;
            background: #ffffff;
            border: 1px solid var(--line);
            transition: transform .1s ease, border-color .1s ease;
        }

        .stat-card:hover {
            transform: translateY(-1px);
            border-color: #dbe1e8;
        }

        .stat-icon {
            width: 44px;
            height: 44px;
            border-radius: 10px;
            display: grid;
            place-items: center;
            margin: 0 auto .6rem;
            font-size: 1.05rem
        }

        .stat-icon.wallet {
            background: #e0f2fe;
            color: #0369a1
        }

        .stat-icon.orders {
            background: #eafbea;
            color: #166534
        }

        .stat-icon.spent {
            background: #fff7ed;
            color: #9a3412
        }

        .stat-icon.last-order {
            background: #eef2ff;
            color: #3730a3
        }

        .stat-value {
            font-weight: 900;
            font-size: 1.15rem;
            color: #111827
        }

        .stat-label {
            color: #6b7280;
            font-size: .88rem;
            font-weight: 600
        }

        .table-container {
            border: 1px solid var(--line);
            border-radius: 12px;
            overflow: hidden;
            background: #ffffff;
            box-shadow: var(--shadow-1);
        }

        .data-table {
            width: 100%;
            border-collapse: separate;
            border-spacing: 0
        }

        .data-table thead th {
            background: #f8fafc;
            color: #111827;
            text-align: left;
            font-weight: 800;
            font-size: .86rem;
            padding: .85rem 1rem;
            border-bottom: 1px solid var(--line);
            letter-spacing: .2px;
        }

        .data-table td {
            padding: .8rem 1rem;
            border-bottom: 1px solid #f1f5f9;
            color: #111827;
        }

        .data-table tr:last-child td {
            border-bottom: none
        }

        .data-table tbody tr:hover {
            background: #f8fafc;
        }

        .empty-state {
            padding: 2rem;
            text-align: center;
            color: #6b7280
        }

        .empty-state i {
            font-size: 2.4rem;
            margin-bottom: .5rem;
            color: #cbd5e1
        }

        .empty-state h3 {
            margin-bottom: .3rem;
            color: #111827
        }

        .flash-message {
            display: flex;
            align-items: center;
            gap: .55rem;
            padding: .85rem 1rem;
            border-radius: 10px;
            border: 1px solid var(--line);
            background: #ffffff;
            font-weight: 700;
            color: #111827;
        }

        .flash-message.success {
            border-color: #bbf7d0;
            background: #ecfdf5;
            color: #065f46
        }

        .flash-message.error {
            border-color: #fecaca;
            background: #fef2f2;
            color: #7f1d1d
        }

        .panel {
            margin-bottom: 1.25rem
        }

        .panel + .panel {
            margin-top: 1.25rem
        }

        .table-container {
            margin-top: .8rem
        }

        .text-center {
            text-align: center
        }

        .breadcrumb {
            color: #64748b
        }

        .breadcrumb a {
            color: #1d4ed8;
            text-decoration: none
        }

        .breadcrumb a:hover {
            text-decoration: underline
        }

        @media (max-width: 820px) {
            .info-content {
                flex-direction: column;
                gap: .4rem
            }

            .info-label {
                min-width: auto
            }

            .header-actions {
                width: 100%
            }

            .stats-grid {
                grid-template-columns:1fr
            }
        }
    </style>

</head>
<body class="admin">
<fmt:setLocale value="vi_VN"/>

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
        <a href="${pageContext.request.contextPath}/admin/customers" class="nav-item active"><i
                class="fas fa-users"></i><span>Customers</span></a>
        <a href="${pageContext.request.contextPath}/admin/bikes" class="nav-item"><i
                class="fas fa-motorcycle"></i><span>Motorbikes</span></a>
        <a href="${pageContext.request.contextPath}/admin/orders" class="nav-item"><i class="fas fa-clipboard-list"></i><span>Orders</span></a>
        <a href="${pageContext.request.contextPath}/admin/schedule" class="nav-item">
            <i class="fas fa-calendar-alt"></i><span>View Schedule</span>
        </a>
        <a href="${pageContext.request.contextPath}/adminpickup" class="nav-item"><i
                class="fas fa-shipping-fast"></i><span>Vehicle Pickup</span></a>
        <a href="${pageContext.request.contextPath}/adminreturn" class="nav-item"><i class="fas fa-undo-alt"></i><span>Vehicle Return</span></a>
        <a href="${pageContext.request.contextPath}/adminreturns" class="nav-item"><i
                class="fas fa-clipboard-check"></i><span>Verify & Refund</span></a>
        <a href="${pageContext.request.contextPath}/admin/reports" class="nav-item"><i
                class="fas fa-chart-bar"></i><span>Reports</span></a>
        <a href="${pageContext.request.contextPath}/admin/feedback" class="nav-item"><i
                class="fas fa-comment-alt"></i><span>Feedback</span></a>
        <a href="${pageContext.request.contextPath}/logout" class="nav-item logout"><i
                class="fas fa-sign-out-alt"></i><span>Logout</span></a>
    </nav>
</aside>

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

    <!-- Flash -> toast -->
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

    <div class="customer-detail-container">
        <c:if test="${empty detail}">
            <div class="flash-message error"><i class="fas fa-exclamation-triangle"></i>Không tìm thấy thông tin khách
                hàng hoặc có lỗi xảy ra.
            </div>
            <div class="text-center" style="padding: 2rem;">
                <a href="${pageContext.request.contextPath}/admin/customers" class="btn btn-secondary"><i
                        class="fas fa-arrow-left"></i> Quay lại danh sách khách hàng</a>
            </div>
        </c:if>

        <c:if test="${not empty detail}">
            <!-- Header -->
            <section class="customer-header">
                <div class="header-content">
                    <div class="customer-identity">
                        <div class="avatar-xl">${detail.fullName.charAt(0)}</div>
                        <div class="customer-titles">
                            <h1>${detail.fullName}</h1>
                            <div class="customer-subtitle">
                                <span class="customer-id">ID: #${detail.id}</span>
                                <span class="status-badge ${detail.banned ? 'danger' : 'success'}">
                                    <i class="fas ${detail.banned ? 'fa-ban' : 'fa-check'}"></i>
                                    ${detail.banned ? 'BANNED' : 'ACTIVE'}
                                </span>
                            </div>
                        </div>
                    </div>
                    <div class="header-actions">
                        <form method="post"
                              action="${pageContext.request.contextPath}/admin/customers"
                              style="display: inline;"
                              class="needs-confirm"
                              data-confirm-message="${detail.banned ? 'Bạn có chắc chắn muốn mở khóa tài khoản này?' : 'Bạn có chắc chắn muốn khóa tài khoản này?'}">
                            <input type="hidden" name="action" value="toggle">
                            <input type="hidden" name="customerId" value="${detail.id}">
                            <button type="submit" class="btn ${detail.banned ? 'btn-success' : 'btn-warning'}">
                                <i class="fas ${detail.banned ? 'fa-unlock' : 'fa-lock'}"></i>
                                ${detail.banned ? 'UNBAN TÀI KHOẢN' : 'BAN TÀI KHOẢN'}
                            </button>
                        </form>
                        <a href="${pageContext.request.contextPath}/admin/customers" class="btn btn-secondary">
                            <i class="fas fa-arrow-left"></i> Quay lại
                        </a>
                    </div>
                </div>
            </section>

            <!-- Grid -->
            <div class="customer-grid">
                <!-- Left -->
                <div class="left-column">
                    <!-- Personal info -->
                    <section class="card panel">
                        <div class="card-header">
                            <h2><i class="fas fa-user-circle"></i> Thông tin cá nhân</h2>
                        </div>
                        <div class="card-body">
                            <div class="info-grid">
                                <div class="info-row">
                                    <i class="fas fa-envelope info-icon"></i>
                                    <div class="info-content">
                                        <span class="info-label">Email:</span>
                                        <span class="info-value">${detail.email}</span>
                                        <span class="status-badge ${detail.emailVerified ? 'success' : 'warning'}">
                                            <i class="fas ${detail.emailVerified ? 'fa-check' : 'fa-exclamation'}"></i>
                                            ${detail.emailVerified ? 'Đã xác minh' : 'Chưa xác minh'}
                                        </span>
                                    </div>
                                </div>

                                <div class="info-row">
                                    <i class="fas fa-phone info-icon"></i>
                                    <div class="info-content">
                                        <span class="info-label">SĐT:</span>
                                        <span class="info-value">${not empty detail.phone ? detail.phone : 'N/A'}</span>
                                    </div>
                                </div>

                                <div class="info-row">
                                    <i class="fas fa-map-marker-alt info-icon"></i>
                                    <div class="info-content">
                                        <span class="info-label">Địa chỉ:</span>
                                        <span class="info-value">${not empty detail.address ? detail.address : 'N/A'}</span>
                                    </div>
                                </div>

                                <div class="info-row">
                                    <i class="fas fa-calendar info-icon"></i>
                                    <div class="info-content">
                                        <span class="info-label">Ngày sinh:</span>
                                        <span class="info-value">
                                            <c:if test="${not empty detail.dob}">
                                                <fmt:formatDate value="${detail.dob}" pattern="dd/MM/yyyy"/>
                                            </c:if>
                                            <c:if test="${empty detail.dob}">N/A</c:if>
                                        </span>
                                    </div>
                                </div>

                                <div class="info-row">
                                    <i class="fas fa-calendar-plus info-icon"></i>
                                    <div class="info-content">
                                        <span class="info-label">Ngày tạo:</span>
                                        <span class="info-value">
                                            <fmt:formatDate value="${detail.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                                        </span>
                                    </div>
                                </div>

                                <div class="info-row">
                                    <i class="fas fa-sign-in-alt info-icon"></i>
                                    <div class="info-content">
                                        <span class="info-label">Đăng nhập cuối:</span>
                                        <span class="info-value">
                                            <c:if test="${not empty detail.lastLogin}">
                                                <fmt:formatDate value="${detail.lastLogin}" pattern="dd/MM/yyyy HH:mm"/>
                                            </c:if>
                                            <c:if test="${empty detail.lastLogin}">N/A</c:if>
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </section>

                    <!-- Recent orders -->
                    <section class="card panel">
                        <div class="card-header">
                            <h2><i class="fas fa-history"></i> 5 Đơn hàng gần nhất</h2>
                        </div>
                        <div class="card-body">
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
                                                    <span style="font-weight:800;">
                                                        <fmt:formatNumber value="${order.total}" type="currency" currencyCode="VND"/>
                                                    </span>
                                                </td>
                                                <td>
                                                    <span class="status-badge info">${order.status}</span>
                                                </td>
                                                <td>
                                                    <fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
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
                </div>

                <!-- Right -->
                <div class="right-column">
                    <!-- Wallet -->
                    <section class="card panel">
                        <div class="card-header">
                            <h2><i class="fas fa-wallet"></i> Thông tin Ví</h2>
                        </div>
                        <div class="card-body">
                            <div class="wallet-card">
                                <div class="wallet-header">
                                    <div class="wallet-title">Số dư ví</div>
                                </div>
                                <div class="wallet-balance">
                                    <fmt:formatNumber value="${detail.wallet}" type="currency" currencyCode="VND"/>
                                </div>
                                <div class="wallet-details">
                                    <div class="wallet-item">
                                        <span>Số dư hiện tại:</span>
                                        <span class="wallet-amount ${detail.wallet > 0 ? 'positive' : detail.wallet < 0 ? 'negative' : 'zero'}">
                                            <fmt:formatNumber value="${detail.wallet}" type="currency" currencyCode="VND"/>
                                        </span>
                                    </div>
                                    <div class="wallet-item">
                                        <span>Tổng chi tiêu:</span>
                                        <span class="wallet-amount">
                                            <fmt:formatNumber value="${detail.totalSpent}" type="currency" currencyCode="VND"/>
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
                                                <fmt:formatDate value="${detail.lastOrderAt}" pattern="dd/MM/yyyy HH:mm"/>
                                            </c:if>
                                            <c:if test="${empty detail.lastOrderAt}">N/A</c:if>
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </section>

                    <!-- Stats -->
                    <section class="card panel">
                        <div class="card-header">
                            <h2><i class="fas fa-chart-bar"></i> Thống kê</h2>
                        </div>
                        <div class="card-body">
                            <div class="stats-grid">
                                <div class="stat-card">
                                    <div class="stat-icon wallet"><i class="fas fa-wallet"></i></div>
                                    <div class="stat-value">
                                        <fmt:formatNumber value="${detail.wallet}" type="currency" currencyCode="VND"/>
                                    </div>
                                    <div class="stat-label">Số dư ví</div>
                                </div>

                                <div class="stat-card">
                                    <div class="stat-icon orders"><i class="fas fa-shopping-bag"></i></div>
                                    <div class="stat-value">${detail.orders}</div>
                                    <div class="stat-label">Tổng đơn hàng</div>
                                </div>

                                <div class="stat-card">
                                    <div class="stat-icon spent"><i class="fas fa-money-bill-wave"></i></div>
                                    <div class="stat-value">
                                        <fmt:formatNumber value="${detail.totalSpent}" type="currency" currencyCode="VND"/>
                                    </div>
                                    <div class="stat-label">Tổng chi tiêu</div>
                                </div>

                                <div class="stat-card">
                                    <div class="stat-icon last-order"><i class="fas fa-clock"></i></div>
                                    <div class="stat-value">
                                        <c:if test="${not empty detail.lastOrderAt}">
                                            <fmt:formatDate value="${detail.lastOrderAt}" pattern="dd/MM/yy"/>
                                        </c:if>
                                        <c:if test="${empty detail.lastOrderAt}">N/A</c:if>
                                    </div>
                                    <div class="stat-label">Đơn cuối</div>
                                </div>
                            </div>
                        </div>
                    </section>
                </div>
            </div>
        </c:if>
    </div>
</main>

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
        // Flash toast
        const flashToastEl = document.getElementById('flashToast');
        if (flashToastEl && typeof bootstrap !== 'undefined') {
            const flashToast = new bootstrap.Toast(flashToastEl);
            flashToast.show();
        }

        // Confirm toast
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
