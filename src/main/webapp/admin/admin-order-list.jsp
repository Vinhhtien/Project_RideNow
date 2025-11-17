<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>Quản lý Đơn Hàng | RideNow Admin</title>

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css"/>
    <link rel="stylesheet" href="${ctx}/css/admin.css">
    <style>
        /* Order Page Specific Styles */
        .order-toolbar {
            background: white;
            border-radius: 12px;
            padding: 1.5rem;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
            margin-bottom: 1.5rem;
        }

        .filter-tabs {
            display: flex;
            gap: 0.5rem;
            margin-bottom: 1.5rem;
            flex-wrap: wrap;
        }

        .filter-tab {
            padding: 0.75rem 1.5rem;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            background: white;
            color: #6b7280;
            font-size: 0.875rem;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.2s;
            text-decoration: none;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }

        .filter-tab:hover {
            background: #f8fafc;
            border-color: #d1d5db;
        }

        .filter-tab.active {
            background: #3b82f6;
            color: white;
            border-color: #3b82f6;
        }

        .filter-tab .count {
            background: rgba(255, 255, 255, 0.2);
            padding: 0.125rem 0.5rem;
            border-radius: 12px;
            font-size: 0.75rem;
            font-weight: 600;
        }

        .filter-form {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 1rem;
            padding-top: 1.5rem;
            border-top: 1px solid #e5e7eb;
        }

        .filter-form > div {
            display: flex;
            flex-direction: column;
            gap: 0.5rem;
        }

        .filter-form label {
            font-size: 0.875rem;
            font-weight: 600;
            color: #374151;
        }

        .filter-form select,
        .filter-form input {
            padding: 0.75rem;
            border: 1px solid #d1d5db;
            border-radius: 8px;
            font-size: 0.875rem;
            transition: border-color 0.2s;
        }

        .filter-form select:focus,
        .filter-form input:focus {
            outline: none;
            border-color: #3b82f6;
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }

        .filter-actions {
            display: flex;
            gap: 0.75rem;
            align-items: flex-end;
        }

        .btn {
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            padding: 0.75rem 1.5rem;
            border-radius: 8px;
            font-size: 0.875rem;
            font-weight: 500;
            text-decoration: none;
            transition: all 0.2s;
            border: 1px solid transparent;
            cursor: pointer;
            white-space: nowrap;
        }

        .btn-primary {
            background: #3b82f6;
            color: white;
            border-color: #3b82f6;
        }

        .btn-primary:hover {
            background: #2563eb;
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
        }

        .btn-outline {
            background: transparent;
            color: #6b7280;
            border-color: #d1d5db;
        }

        .btn-outline:hover {
            background: #f9fafb;
            color: #374151;
        }

        .order-table-section {
            background: white;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
            overflow: hidden;
        }

        .table-header {
            padding: 1.5rem;
            border-bottom: 1px solid #e5e7eb;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .table-header h2 {
            margin: 0;
            font-size: 1.25rem;
            font-weight: 600;
            color: #1f2937;
        }

        .meta {
            color: #6b7280;
            font-size: 0.875rem;
            font-weight: 500;
        }

        .order-table {
            width: 100%;
            border-collapse: collapse;
        }

        .order-table th {
            background: #f8fafc;
            padding: 1rem 0.75rem;
            text-align: left;
            font-size: 0.75rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            color: #6b7280;
            border-bottom: 1px solid #e5e7eb;
        }

        .order-table td {
            padding: 1rem 0.75rem;
            border-bottom: 1px solid #f1f5f9;
            font-size: 0.875rem;
        }

        .order-table tbody tr {
            transition: background-color 0.2s;
        }

        .order-table tbody tr:hover {
            background-color: #f8fafc;
        }

        .order-table tbody tr:last-child td {
            border-bottom: none;
        }

        .status-badge {
            padding: 0.375rem 0.75rem;
            border-radius: 6px;
            font-size: 0.75rem;
            font-weight: 600;
            text-transform: capitalize;
            letter-spacing: 0.025em;
        }

        .status-badge.pending {
            background: #fef3c7;
            color: #92400e;
        }

        .status-badge.confirmed {
            background: #dbeafe;
            color: #1e40af;
        }

        .status-badge.completed {
            background: #d1fae5;
            color: #065f46;
        }

        .status-badge.cancelled {
            background: #fee2e2;
            color: #991b1b;
        }

        .amount-positive {
            color: #065f46;
            font-weight: 600;
        }

        .amount-negative {
            color: #dc2626;
            font-weight: 600;
        }

        .amount-zero {
            color: #6b7280;
        }

        .action-buttons {
            display: flex;
            gap: 0.5rem;
        }

        .btn-sm {
            padding: 0.5rem 1rem;
            font-size: 0.75rem;
        }

        .pagination {
            display: flex;
            gap: 0.5rem;
            align-items: center;
            justify-content: center;
            padding: 1.5rem;
            border-top: 1px solid #e5e7eb;
        }

        .pagination a,
        .pagination span {
            display: flex;
            align-items: center;
            justify-content: center;
            min-width: 2.5rem;
            height: 2.5rem;
            padding: 0 0.5rem;
            border: 1px solid #d1d5db;
            border-radius: 8px;
            text-decoration: none;
            font-size: 0.875rem;
            font-weight: 500;
            transition: all 0.2s;
        }

        .pagination a {
            color: #374151;
        }

        .pagination a:hover {
            background: #3b82f6;
            border-color: #3b82f6;
            color: white;
        }

        .pagination span {
            background: #3b82f6;
            color: white;
            border-color: #3b82f6;
        }

        .empty-state {
            text-align: center;
            padding: 3rem 1rem;
            color: #6b7280;
        }

        .empty-state i {
            font-size: 3rem;
            margin-bottom: 1rem;
            opacity: 0.5;
            color: #cbd5e1;
        }

        .empty-state h3 {
            font-size: 1.25rem;
            margin: 0 0 0.5rem 0;
            color: #374151;
        }

        .empty-state p {
            margin: 0;
            font-size: 0.875rem;
        }

        /* Responsive */
        @media (max-width: 1024px) {
            .filter-form {
                grid-template-columns: repeat(2, 1fr);
            }
        }

        @media (max-width: 768px) {
            .filter-tabs {
                flex-direction: column;
            }

            .filter-form {
                grid-template-columns: 1fr;
            }

            .order-table {
                display: block;
                overflow-x: auto;
            }

            .table-header {
                flex-direction: column;
                gap: 1rem;
                align-items: flex-start;
            }

            .action-buttons {
                flex-direction: column;
            }
        }
    </style>
</head>

<body class="admin order-page">
<!-- SIDEBAR -->
<aside class="sidebar">
    <div class="brand">
        <div class="brand-logo"><i class="fa-solid fa-motorcycle"></i></div>
        <h1>RideNow Admin</h1>
    </div>
    <nav class="sidebar-nav">
        <a href="${ctx}/admin/dashboard" class="nav-item"><i class="fa-solid fa-gauge"></i>Dashboard</a>
        <a href="${ctx}/admin/partners" class="nav-item"><i class="fa-solid fa-handshake"></i>Partners</a>
        <a href="${ctx}/admin/customers" class="nav-item"><i class="fa-solid fa-users"></i>Customers</a>
        <a href="${ctx}/admin/bikes" class="nav-item"><i class="fa-solid fa-motorcycle"></i>Motorbikes</a>
        <a href="${ctx}/admin/orders" class="nav-item active"><i class="fa-solid fa-receipt"></i>Orders</a>
        <a href="${pageContext.request.contextPath}/admin/schedule" class="nav-item">
            <i class="fas fa-calendar-alt"></i><span>View Schedule</span>
        </a>
        <a href="${ctx}/adminpickup" class="nav-item"><i class="fa-solid fa-truck"></i>Vehicle Pickup</a>
        <a href="${ctx}/adminreturn" class="nav-item"><i class="fa-solid fa-rotate-left"></i>Vehicle Return</a>
        <a href="${ctx}/adminreturns" class="nav-item"><i class="fa-solid fa-clipboard-check"></i>Verify & Refund</a>
        <a href="${ctx}/admin/reports" class="nav-item"><i class="fa-solid fa-chart-line"></i>Reports</a>
        <a href="${ctx}/admin/feedback" class="nav-item"><i class="fa-solid fa-comment-dots"></i>Feedback</a>
        <a href="${ctx}/logout" class="nav-item logout"><i class="fa-solid fa-arrow-right-from-bracket"></i>Logout</a>
    </nav>
</aside>

<!-- MAIN -->
<main class="content">
    <header class="content-header">
        <div class="header-left">
            <h1>Quản lý Đơn Hàng</h1>
            <div class="breadcrumb">
                <span>Admin</span><i class="fa-solid fa-angle-right"></i><span class="active">Orders</span>
            </div>
        </div>
        <div class="user-profile">
            <div class="user-avatar"><i class="fa-solid fa-user"></i></div>
            <span>Administrator</span>
        </div>
    </header>

    <fmt:setLocale value="vi_VN"/>

    <!-- Đếm trên trang hiện tại (pageCount) để hiển thị phụ nếu cần -->
    <c:set var="pageCount" value="${fn:length(orders)}"/>

    <!-- Nếu backend có đưa các số đếm theo status, dùng nó; nếu không, fallback -->
    <c:set var="pendingCount" value="${empty pendingCount   ? 0 : pendingCount}"/>
    <c:set var="confirmedCount" value="${empty confirmedCount ? 0 : confirmedCount}"/>
    <c:set var="completedCount" value="${empty completedCount ? 0 : completedCount}"/>
    <c:set var="cancelledCount" value="${empty cancelledCount ? 0 : cancelledCount}"/>

    <c:if test="${pendingCount == 0 && confirmedCount == 0 && completedCount == 0 && cancelledCount == 0}">
        <!-- Fallback: đếm tạm theo danh sách của trang hiện tại -->
        <c:forEach var="o" items="${orders}">
            <c:if test="${o.orderStatus == 'pending'}"><c:set var="pendingCount" value="${pendingCount + 1}"/></c:if>
            <c:if test="${o.orderStatus == 'confirmed'}"><c:set var="confirmedCount"
                                                                value="${confirmedCount + 1}"/></c:if>
            <c:if test="${o.orderStatus == 'completed'}"><c:set var="completedCount"
                                                                value="${completedCount + 1}"/></c:if>
            <c:if test="${o.orderStatus == 'cancelled'}"><c:set var="cancelledCount"
                                                                value="${cancelledCount + 1}"/></c:if>
        </c:forEach>
    </c:if>

    <!-- FILTER TOOLBAR -->
    <section class="order-toolbar">
        <!-- Quick Filter Tabs -->
        <div class="filter-tabs">
            <a href="${ctx}/admin/orders" class="filter-tab ${empty status ? 'active' : ''}">
                <i class="fa-solid fa-layer-group"></i>
                Tất cả đơn
                <span class="count">${total}</span>
            </a>
            <a href="${ctx}/admin/orders?status=pending&q=${fn:escapeXml(q)}&from=${from}&to=${to}"
               class="filter-tab ${status == 'pending' ? 'active' : ''}">
                <i class="fa-solid fa-clock"></i>
                Đang chờ
                <span class="count">${pendingCount}</span>
            </a>
            <a href="${ctx}/admin/orders?status=confirmed&q=${fn:escapeXml(q)}&from=${from}&to=${to}"
               class="filter-tab ${status == 'confirmed' ? 'active' : ''}">
                <i class="fa-solid fa-check-circle"></i>
                Đã xác nhận
                <span class="count">${confirmedCount}</span>
            </a>
            <a href="${ctx}/admin/orders?status=completed&q=${fn:escapeXml(q)}&from=${from}&to=${to}"
               class="filter-tab ${status == 'completed' ? 'active' : ''}">
                <i class="fa-solid fa-flag-checkered"></i>
                Hoàn thành
                <span class="count">${completedCount}</span>
            </a>
            <a href="${ctx}/admin/orders?status=cancelled&q=${fn:escapeXml(q)}&from=${from}&to=${to}"
               class="filter-tab ${status == 'cancelled' ? 'active' : ''}">
                <i class="fa-solid fa-ban"></i>
                Đã hủy
                <span class="count">${cancelledCount}</span>
            </a>
        </div>

        <!-- Advanced Filter Form -->
        <form class="filter-form" method="get" action="${ctx}/admin/orders">
            <input type="hidden" name="status" value="${status}">

            <div>
                <label for="customer-search">Tên khách hàng</label>
                <input type="text" id="customer-search" name="q" value="${fn:escapeXml(q)}"
                       placeholder="Nhập tên khách hàng..."/>
            </div>

            <div>
                <label for="from-date">Từ ngày</label>
                <fmt:formatDate value="${fromDate}" pattern="yyyy-MM-dd" var="fromStr"/>
                <input type="date" id="from-date" name="from" value="${empty fromStr ? from : fromStr}"/>
            </div>

            <div>
                <label for="to-date">Đến ngày</label>
                <fmt:formatDate value="${toDate}" pattern="yyyy-MM-dd" var="toStr"/>
                <input type="date" id="to-date" name="to" value="${empty toStr ? to : toStr}"/>
            </div>

            <div class="filter-actions">
                <button class="btn btn-primary" type="submit">
                    <i class="fa-solid fa-filter"></i> Lọc
                </button>
                <a class="btn btn-outline" href="${ctx}/admin/orders">
                    <i class="fa-solid fa-rotate-right"></i> Reset
                </a>
            </div>
        </form>
    </section>

    <!-- ORDER TABLE -->
    <section class="order-table-section">
        <div class="table-header">
            <h2>Danh sách đơn hàng</h2>
            <div class="meta">
                Trang ${page} / ${totalPages} • Tổng ${total} kết quả
                <c:if test="${pageCount lt pageSize}">
                    • Trên trang này: ${pageCount}
                </c:if>
            </div>
        </div>

        <div class="table-wrapper">
            <table class="order-table">
                <thead> <tr>
        <th>Mã đơn</th>
        <th>Khách hàng</th>
        <th>Trạng thái</th>
        <th>Tổng đơn</th>
        <th>Đã thanh toán</th>
        <th>Còn phải thu</th>
        <th>Ngày tạo</th>
        <th>Thao tác</th>
    </tr> </thead>
                <tbody>
                <c:forEach var="o" items="${orders}">
                    <!-- giữ nguyên row như của bạn -->
                    <tr>
                        <td><strong>#${o.orderId}</strong></td>
                        <td>${o.customerName}</td>
                        <td>
                <span class="status-badge ${o.orderStatus}">
                  <c:choose>
                      <c:when test="${o.orderStatus == 'pending'}">Đang chờ</c:when>
                      <c:when test="${o.orderStatus == 'confirmed'}">Đã xác nhận</c:when>
                      <c:when test="${o.orderStatus == 'completed'}">Hoàn thành</c:when>
                      <c:when test="${o.orderStatus == 'cancelled'}">Đã hủy</c:when>
                      <c:otherwise>${o.orderStatus}</c:otherwise>
                  </c:choose>
                </span>
                        </td>
                        <td><strong><fmt:formatNumber value="${o.orderTotal}" type="currency"/></strong></td>
                        <td><fmt:formatNumber value="${o.totalPaid}" type="currency"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${o.amountDue > 0}">
                                    <span class="amount-negative"><fmt:formatNumber value="${o.amountDue}"
                                                                                    type="currency"/></span>
                                </c:when>
                                <c:when test="${o.amountDue < 0}">
                                    <span class="amount-positive"><fmt:formatNumber value="${-o.amountDue}"
                                                                                    type="currency"/></span>
                                </c:when>
                                <c:otherwise>
                                    <span class="amount-zero"><fmt:formatNumber value="${o.amountDue}"
                                                                                type="currency"/></span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td><fmt:formatDate value="${o.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                        <td>
                            <div class="action-buttons">
                                <a class="btn btn-primary btn-sm" href="${ctx}/admin/orders/detail?id=${o.orderId}">
                                    <i class="fa-solid fa-eye"></i> Chi tiết
                                </a>
                            </div>
                        </td>
                    </tr>
                </c:forEach>

                <c:if test="${empty orders}">
                    <tr>
                        <td colspan="8">
                            <div class="empty-state">
                                <i class="fa-solid fa-receipt"></i>
                                <h3>Không tìm thấy đơn hàng nào</h3>
                                <p>Hãy thử điều chỉnh bộ lọc tìm kiếm của bạn</p>
                            </div>
                        </td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>

        <c:if test="${totalPages > 1}">
            <div class="pagination">
                <c:forEach var="p" begin="1" end="${totalPages}">
                    <c:choose>
                        <c:when test="${p==page}">
                            <span>${p}</span>
                        </c:when>
                        <c:otherwise>
                            <a href="${ctx}/admin/orders?page=${p}&status=${status}&q=${fn:escapeXml(q)}&from=${from}&to=${to}">
                                    ${p}
                            </a>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </div>
        </c:if>
    </section>
</main>
</body>
</html>
