<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Xác Minh Thanh Toán - RideNow Admin</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${ctx}/css/admin.css">
</head>
<body class="admin">
    <!-- Sidebar Navigation -->
    <aside class="sidebar">
        <div class="brand">
            <div class="brand-logo">
                <i class="fas fa-motorcycle"></i>
            </div>
            <h1>RideNow Admin</h1>
        </div>
        
        <nav class="sidebar-nav">
            <a href="${ctx}/admin/dashboard" class="nav-item">
                <i class="fas fa-tachometer-alt"></i>
                <span>Dashboard</span>
            </a>
            <a href="${ctx}/admin/partners" class="nav-item">
                <i class="fas fa-handshake"></i>
                <span>Partners</span>
            </a>
            <a href="${ctx}/admin/customers" class="nav-item">
                <i class="fas fa-users"></i>
                <span>Customers</span>
            </a>
            <a href="${ctx}/admin/bikes" class="nav-item">
                <i class="fas fa-motorcycle"></i>
                <span>Motorbikes</span>
            </a>
            <a href="${ctx}/admin/orders" class="nav-item">
                <i class="fas fa-clipboard-list"></i>
                <span>Orders</span>
            </a>
<!--            <a href="${ctx}/adminpaymentverify" class="nav-item active">
                <i class="fas fa-money-check-alt"></i>
                <span>Verify Payments</span>
            </a>-->
            <a href="${ctx}/adminpickup" class="nav-item">
                <i class="fas fa-shipping-fast"></i>
                <span>Vehicle Pickup</span>
            </a>
            <a href="${ctx}/adminreturn" class="nav-item">
                <i class="fas fa-undo-alt"></i>
                <span>Vehicle Return</span>
            </a>
            <a href="${ctx}/adminreturns" class="nav-item">
                <i class="fas fa-clipboard-check"></i>
                <span>Verify & Refund</span>
            </a>
            <a href="${ctx}/admin/reports" class="nav-item">
                <i class="fas fa-chart-bar"></i>
                <span>Reports</span>
            </a>
            <a href="${ctx}/admin/feedback" class="nav-item">
                <i class="fas fa-comment-alt"></i>
                <span>Feedback</span>
            </a>
            <a href="${ctx}/logout" class="nav-item logout">
                <i class="fas fa-sign-out-alt"></i>
                <span>Logout</span>
            </a>
        </nav>
    </aside>

    <!-- Main Content -->
    <main class="content">
        <header class="content-header">
            <div class="header-left">
                <h1>Xác Minh Thanh Toán</h1>
                <div class="breadcrumb">
                    <span>Admin</span>
                    <i class="fas fa-chevron-right"></i>
                    <span class="active">Xác Minh Thanh Toán</span>
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

        <!-- KPI Cards -->
        <section class="kpi-grid">
            <div class="kpi-card">
                <div class="kpi-icon" style="background: linear-gradient(135deg, #f59e0b, #d97706);">
                    <i class="fas fa-clock"></i>
                </div>
                <div class="kpi-content">
                    <div class="kpi-value">${not empty payments ? payments.size() : 0}</div>
                    <div class="kpi-label">Chờ Xác Minh</div>
                </div>
            </div>
            
            <div class="kpi-card">
                <div class="kpi-icon" style="background: linear-gradient(135deg, #10b981, #059669);">
                    <i class="fas fa-money-bill-wave"></i>
                </div>
                <div class="kpi-content">
                    <div class="kpi-value">
                        <c:set var="totalAmount" value="0" />
                        <c:forEach var="p" items="${payments}">
                            <c:set var="totalAmount" value="${totalAmount + p[4]}" />
                        </c:forEach>
                        <fmt:formatNumber value="${totalAmount}" type="currency"/>
                    </div>
                    <div class="kpi-label">Tổng Tiền Chờ</div>
                </div>
            </div>
        </section>

        <!-- Payments Panel -->
        <section class="panel">
            <div class="panel-header">
                <h2>
                    <i class="fas fa-money-check-alt"></i>
                    Thanh Toán Đang Chờ Xác Minh
                </h2>
                <div class="panel-stats">
                    <span class="stat-badge">
                        <i class="fas fa-list"></i>
                        Tổng số: ${not empty payments ? payments.size() : 0}
                    </span>
                </div>
            </div>

            <div class="panel-body">
                <c:choose>
                    <c:when test="${empty payments}">
                        <div class="empty-state">
                            <i class="fas fa-check-circle"></i>
                            <h3>Không có thanh toán nào đang chờ</h3>
                            <p>Tất cả các thanh toán đã được xử lý</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-container">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Mã TT</th>
                                        <th>Mã Đơn</th>
                                        <th>Khách Hàng</th>
                                        <th>Số Tiền</th>
                                        <th>Phương Thức</th>
                                        <th>Ngày TT</th>
                                        <th>Trạng Thái</th>
                                        <th>Thao Tác</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="p" items="${payments}">
                                        <tr>
                                            <td><strong>#${p[0]}</strong></td>
                                            <td>#${p[1]}</td>
                                            <td>
                                                <div class="customer-info">
                                                    <div class="customer-name">${p[2]}</div>
                                                    <div class="text-muted">${p[3]}</div>
                                                </div>
                                            </td>
                                            <td>
                                                <strong style="color: #059669;">
                                                    <fmt:formatNumber value="${p[4]}" type="currency"/>
                                                </strong>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${p[5] == 'BANK_TRANSFER'}">
                                                        <i class="fas fa-university"></i> Chuyển khoản
                                                    </c:when>
                                                    <c:when test="${p[5] == 'EWALLET'}">
                                                        <i class="fas fa-wallet"></i> Ví điện tử
                                                    </c:when>
                                                    <c:when test="${p[5] == 'CASH'}">
                                                        <i class="fas fa-money-bill"></i> Tiền mặt
                                                    </c:when>
                                                    <c:otherwise>
                                                        ${p[5]}
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td><fmt:formatDate value="${p[6]}" pattern="dd/MM/yyyy HH:mm"/></td>
                                            <td>
                                                <span class="status-badge pending">
                                                    <i class="fas fa-clock"></i>
                                                    ${p[7]}
                                                </span>
                                            </td>
                                            <td>
                                                <!-- GIỮ NGUYÊN FORM - KHÔNG THAY ĐỔI CHỨC NĂNG -->
                                                <form method="post" action="${pageContext.request.contextPath}/adminpaymentverify" 
                                                      onsubmit="return confirm('Xác nhận đã kiểm tra và thanh toán thành công?');">
                                                    <input type="hidden" name="paymentId" value="${p[0]}">
                                                    <button type="submit" class="btn btn-primary btn-sm">
                                                        <i class="fas fa-check"></i> Xác Nhận
                                                    </button>
                                                </form>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>
    </main>
</body>
</html>