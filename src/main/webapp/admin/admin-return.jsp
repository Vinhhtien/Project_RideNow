<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Trả Xe - RideNow Admin</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${ctx}/css/admin.css">
    <style>
        .empty-state { 
            text-align: center; 
            padding: 3rem 2rem; 
            color: #64748b; 
        }
        .empty-state i { 
            font-size: 4rem; 
            margin-bottom: 1.5rem; 
            color: #cbd5e1; 
        }
        .empty-state h3 {
            font-size: 1.5rem;
            margin-bottom: 0.5rem;
            color: #475569;
        }
        .empty-state p {
            color: #64748b;
            font-size: 1rem;
        }
    </style>
</head>
<body class="admin">
    <fmt:setLocale value="vi_VN" scope="session"/>
    
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
<!--            <a href="${ctx}/adminpaymentverify" class="nav-item">
                <i class="fas fa-money-check-alt"></i>
                <span>Verify Payments</span>
            </a>-->
            <a href="${ctx}/adminpickup" class="nav-item">
                <i class="fas fa-shipping-fast"></i>
                <span>Vehicle Pickup</span>
            </a>
            <a href="${ctx}/adminreturn" class="nav-item active">
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
                <h1>Trả Xe</h1>
                <div class="breadcrumb">
                    <span>Admin</span>
                    <i class="fas fa-chevron-right"></i>
                    <span>Quản lý Đơn hàng</span>
                    <i class="fas fa-chevron-right"></i>
                    <span class="active">Trả Xe</span>
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
                    <div class="kpi-value">${not empty activeOrders ? activeOrders.size() : 0}</div>
                    <div class="kpi-label">Đang Thuê</div>
                </div>
            </div>
            
            <div class="kpi-card">
                <div class="kpi-icon" style="background: linear-gradient(135deg, #3b82f6, #2563eb);">
                    <i class="fas fa-calendar-day"></i>
                </div>
                <div class="kpi-content">
                    <div class="kpi-value">
                        <c:set var="todayReturns" value="0" />
                        <c:forEach var="o" items="${activeOrders}">
                            <fmt:parseDate value="${o[5]}" pattern="yyyy-MM-dd" var="endDate"/>
                            <c:if test="${endDate eq today}">
                                <c:set var="todayReturns" value="${todayReturns + 1}" />
                            </c:if>
                        </c:forEach>
                        ${todayReturns}
                    </div>
                    <div class="kpi-label">Hết Hạn Hôm Nay</div>
                </div>
            </div>
            
            <div class="kpi-card">
                <div class="kpi-icon" style="background: linear-gradient(135deg, #10b981, #059669);">
                    <i class="fas fa-money-bill-wave"></i>
                </div>
                <div class="kpi-content">
                    <div class="kpi-value">
                        <c:set var="totalDeposit" value="0" />
                        <c:forEach var="o" items="${activeOrders}">
                            <c:set var="totalDeposit" value="${totalDeposit + o[7]}" />
                        </c:forEach>
                        <fmt:formatNumber value="${totalDeposit}" type="currency"/>
                    </div>
                    <div class="kpi-label">Tổng Tiền Cọc</div>
                </div>
            </div>
        </section>

        <!-- Active Orders Panel -->
        <section class="panel">
            <div class="panel-header">
                <h2>
                    <i class="fas fa-clipboard-list"></i>
                    Đơn Hàng Đang Thuê
                </h2>
                <div class="panel-stats">
                    <span class="stat-badge">
                        <i class="fas fa-list"></i>
                        Tổng số: ${not empty activeOrders ? activeOrders.size() : 0}
                    </span>
                </div>
            </div>

            <div class="panel-body">
                <c:choose>
                    <c:when test="${empty activeOrders}">
                        <div class="empty-state">
                            <i class="fas fa-clipboard-check"></i>
                            <h3>Không có đơn hàng nào đang thuê</h3>
                            <p>Tất cả các đơn hàng đã được xử lý</p>
                            <a href="${ctx}/admin/dashboard" class="btn btn-primary" style="margin-top: 1rem;">
                                <i class="fas fa-arrow-left"></i>
                                Quay lại Dashboard
                            </a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-container">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Mã Đơn</th>
                                        <th>Khách Hàng</th>
                                        <th>Xe Thuê</th>
                                        <th>Ngày Thuê</th>
                                        <th>Tổng Tiền</th>
                                        <th>Tiền Cọc</th>
                                        <th>Thao Tác</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="o" items="${activeOrders}">
                                        <tr>
                                            <td><strong>#${o[0]}</strong></td>
                                            <td>
                                                <div class="customer-info">
                                                    <div class="customer-name">${o[1]}</div>
                                                    <div class="text-muted">${o[2]}</div>
                                                </div>
                                            </td>
                                            <td>
                                                <div class="bike-info">
                                                    <i class="fas fa-motorcycle"></i>
                                                    ${o[3]}
                                                </div>
                                            </td>
                                            <td>
                                                <div class="date-range">
                                                    <fmt:parseDate value="${o[4]}" pattern="yyyy-MM-dd" var="startDate"/>
                                                    <fmt:parseDate value="${o[5]}" pattern="yyyy-MM-dd" var="endDate"/>
                                                    <strong>Từ:</strong> <fmt:formatDate value="${startDate}" pattern="dd/MM/yyyy"/><br>
                                                    <strong>Đến:</strong> <fmt:formatDate value="${endDate}" pattern="dd/MM/yyyy"/>
                                                </div>
                                            </td>
                                            <td>
                                                <strong style="color: #3b82f6;">
                                                    <fmt:formatNumber value="${o[6]}" type="currency" currencyCode="VND"/>
                                                </strong>
                                            </td>
                                            <td>
                                                <strong style="color: #059669;">
                                                    <fmt:formatNumber value="${o[7]}" type="currency" currencyCode="VND"/>
                                                </strong>
                                            </td>
                                            <td>
                                                <!-- GIỮ NGUYÊN FORM - KHÔNG THAY ĐỔI CHỨC NĂNG -->
                                                <form method="post" action="${ctx}/adminreturn" 
                                                      onsubmit="return confirm('Xác nhận khách đã trả xe? Đơn hàng sẽ chuyển sang trạng thái chờ kiểm tra để hoàn cọc.');">
                                                    <input type="hidden" name="orderId" value="${o[0]}">
                                                    <button type="submit" class="btn btn-primary btn-sm">
                                                        <i class="fas fa-check"></i> Đã Trả Xe
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

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const notice = document.querySelector('.notice');
            if (notice) {
                setTimeout(() => {
                    notice.style.opacity = '0';
                    notice.style.transition = 'opacity 0.5s ease';
                    setTimeout(() => notice.remove(), 500);
                }, 5000);
            }
        });
    </script>
</body>
</html>