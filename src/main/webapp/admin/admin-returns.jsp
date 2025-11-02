<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8"/>
        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        <title>Quản lý hoàn cọc - RideNow Admin</title>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
        <style>
            /* ... CSS Styles (giữ nguyên) ... */
            .badge {
                padding: 0.375rem 0.75rem;
                border-radius: 20px;
                font-size: 0.75rem;
                font-weight: 600;
                display: inline-flex;
                align-items: center;
                gap: 0.25rem;
            }

            .badge.returned {
                background: #dcfce7;
                color: #166534;
            }

            .badge.pending {
                background: #fef3c7;
                color: #92400e;
            }

            .badge.processing {
                background: #e0e7ff;
                color: #3730a3;
            }

            .badge.completed {
                background: #dcfce7;
                color: #166534;
            }

            .badge.cancelled {
                background: #fecaca;
                color: #dc2626;
            }

            .badge.refunded {
                background: #ecfdf5;
                color: #059669;
            }

            .badge.held {
                background: #f3f4f6;
                color: #374151;
            }

            .badge.excellent {
                background: #dcfce7;
                color: #166534;
            }

            .badge.good {
                background: #e0e7ff;
                color: #3730a3;
            }

            .badge.damaged {
                background: #fef3c7;
                color: #92400e;
            }

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

            .customer-info {
                line-height: 1.4;
            }

            .customer-name {
                font-weight: 600;
                color: var(--gray-900);
            }

            .action-buttons {
                display: flex;
                gap: 0.5rem;
                flex-wrap: wrap;
            }

            .btn-sm {
                padding: 0.5rem 1rem;
                font-size: 0.875rem;
                display: inline-flex;
                align-items: center;
                gap: 0.375rem;
            }

            .stats-grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
                gap: 1.5rem;
                margin: 2rem 0;
            }

            .stat-card {
                background: white;
                padding: 1.5rem;
                border-radius: var(--border-radius);
                box-shadow: var(--shadow);
                border-left: 4px solid var(--primary);
            }

            .stat-value {
                font-size: 2rem;
                font-weight: 700;
                color: var(--gray-900);
                line-height: 1;
                margin-bottom: 0.5rem;
            }

            .stat-label {
                font-size: 0.875rem;
                color: var(--gray-500);
                font-weight: 500;
            }

            @media (max-width: 768px) {
                .action-buttons {
                    flex-direction: column;
                }

                .stats-grid {
                    grid-template-columns: repeat(2, 1fr);
                    gap: 1rem;
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
                <a href="${pageContext.request.contextPath}/admin/customers" class="nav-item"><i class="fas fa-users"></i><span>Customers</span></a>
                <a href="${pageContext.request.contextPath}/admin/bikes" class="nav-item"><i
                        class="fas fa-motorcycle"></i><span>Motorbikes</span></a>
                <a href="${pageContext.request.contextPath}/admin/orders" class="nav-item"><i class="fas fa-clipboard-list"></i><span>Orders</span></a>
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

        <main class="content">
            <header class="content-header">
                <div class="header-left">
                    <h1>Quản lý Hoàn Cọc</h1>
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

            <section class="stats-grid">
                <div class="stat-card">
                    <div class="stat-value">${empty refundOrders ? 0 : refundOrders.size()}</div>
                    <div class="stat-label">Chờ Kiểm Tra</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value">
                        <c:set var="pendingCount" value="0"/>
                        <c:forEach var="req" items="${refundRequests}">
                            <c:if test="${req.status eq 'pending'}">
                                <c:set var="pendingCount" value="${pendingCount + 1}"/>
                            </c:if>
                        </c:forEach>
                        ${pendingCount}
                    </div>
                    <div class="stat-label">Chờ Duyệt</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value">
                        <c:set var="processingCount" value="0"/>
                        <c:forEach var="req" items="${refundRequests}">
                            <c:if test="${req.status eq 'processing'}">
                                <c:set var="processingCount" value="${processingCount + 1}"/>
                            </c:if>
                        </c:forEach>
                        ${processingCount}
                    </div>
                    <div class="stat-label">Đang Xử Lý</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value"><fmt:formatNumber value="${totalPendingAmount}" type="currency"/></div>
                    <div class="stat-label">Tổng Tiền Chờ</div>
                </div>
            </section>

            <section class="panel">
                <div class="panel-header">
                    <h2><i class="fas fa-clock"></i> Đơn Hàng Chờ Kiểm Tra</h2>
                    <div class="panel-stats"><span class="stat-badge"><i
                                class="fas fa-list"></i> Tổng số: ${empty refundOrders ? 0 : refundOrders.size()}</span></div>
                </div>
                <div class="panel-body">
                    <c:choose>
                        <c:when test="${empty refundOrders}">
                            <div class="empty-state">
                                <i class="fas fa-clipboard-check"></i>
                                <h3>Không có đơn hàng nào chờ kiểm tra</h3>
                                <p>Các đơn hàng đã trả xe sẽ xuất hiện ở đây</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-container">
                                <table class="data-table">
                                    <thead>
                                        <tr>
                                            <th>Mã đơn</th>
                                            <th>Khách hàng</th>
                                            <th>Xe thuê</th>
                                            <th>Ngày trả</th>
                                            <th>Tiền cọc</th>
                                            <th>Trạng thái</th>
                                            <th>Thao tác</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="order" items="${refundOrders}">
                                            <tr>
                                                <td><strong>#${order.orderId}</strong></td>
                                                <td>
                                                    <div class="customer-info">
                                                        <div class="customer-name">${order.customerName}</div>
                                                        <div class="text-muted">${order.customerPhone}</div>
                                                    </div>
                                                </td>
                                                <td>${order.bikeName}</td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${order.returnedAt != null}">
                                                            <fmt:formatDate value="${order.returnedAt}" pattern="dd/MM/yyyy HH:mm"/>
                                                        </c:when>
                                                        <c:otherwise>-</c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td><strong style="color: #059669;"><fmt:formatNumber value="${order.depositAmount}"
                                                                  type="currency"/></strong>
                                                </td>
                                                <td><span class="badge returned"><i
                                                            class="fas fa-check-circle"></i> Đã trả xe</span></td>
                                                <td>
                                                    <a class="btn btn-primary btn-sm"
                                                       href="${pageContext.request.contextPath}/adminreturninspect?orderId=${order.orderId}">
                                                        <i class="fas fa-search"></i> Kiểm tra & Hoàn cọc
                                                    </a>
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

            <section class="panel">
                <div class="panel-header">
                    <h2><i class="fas fa-money-bill-wave"></i> Yêu Cầu Hoàn Cọc Đang Chờ Xử Lý</h2>
                    <div class="panel-stats"><span class="stat-badge"><i
                                class="fas fa-list"></i> Tổng số: ${empty refundRequests ? 0 : refundRequests.size()}</span></div>
                </div>
                <div class="panel-body">
                    <c:choose>
                        <c:when test="${empty refundRequests}">
                            <div class="empty-state">
                                <i class="fas fa-receipt"></i>
                                <h3>Không có yêu cầu hoàn cọc nào đang chờ xử lý</h3>
                                <p>Các yêu cầu hoàn cọc sau khi kiểm tra xe sẽ xuất hiện ở đây</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-container">
                                <table class="data-table">
                                    <thead>
                                        <tr>
                                            <th>Mã kiểm tra</th>
                                            <th>Mã đơn</th>
                                            <th>Khách hàng</th>
                                            <th>Tiền cọc</th>
                                            <th>Tiền hoàn</th>
                                            <th>Phí hư hỏng</th>
                                            <th>Tình trạng xe</th>
                                            <th>Ngày kiểm tra</th>
                                            <th>Trạng thái</th>
                                            <th>Thao tác</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="req" items="${refundRequests}">
                                            <tr>
                                                <td><strong>#${req.inspectionId}</strong></td>
                                                <td>#${req.orderId}</td>
                                                <td>
                                                    <div class="customer-info">
                                                        <div class="customer-name">${req.customerName}</div>
                                                        <div class="text-muted">${req.customerPhone}</div>
                                                    </div>
                                                </td>
                                                <td><strong style="color: #3b82f6;"><fmt:formatNumber value="${req.depositAmount}"
                                                                  type="currency"/></strong>
                                                </td>
                                                <td><strong style="color: #059669;"><fmt:formatNumber value="${req.refundAmount}"
                                                                  type="currency"/></strong>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${req.damageFee ne null and req.damageFee gt 0}">
                                                            <span style="color: #dc2626; font-weight: 600;">
                                                                <fmt:formatNumber value="${req.damageFee}" type="currency"/>
                                                            </span>
                                                        </c:when>
                                                        <c:otherwise>-</c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${req.bikeCondition eq 'excellent'}"><span
                                                                class="badge excellent"><i
                                                                    class="fas fa-star"></i>Tốt</span></c:when>
                                                        <c:when test="${req.bikeCondition eq 'good'}"><span class="badge good"><i
                                                                    class="fas fa-check"></i>Bình thường</span></c:when>
                                                        <c:when test="${req.bikeCondition eq 'damaged'}"><span
                                                                class="badge damaged"><i
                                                                    class="fas fa-tools"></i>Hư hỏng</span></c:when>
                                                            <c:otherwise>${req.bikeCondition}</c:otherwise>
                                                        </c:choose>
                                                </td>
                                                <td><fmt:formatDate value="${req.inspectedAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${req.status eq 'pending'}"><span class="badge pending"><i
                                                                    class="fas fa-clock"></i> Đang chờ</span></c:when>
                                                        <c:when test="${req.status eq 'processing'}"><span class="badge processing"><i
                                                                    class="fas fa-sync-alt"></i> Đang xử lý</span></c:when>
                                                        <c:otherwise><span class="badge">${req.status}</span></c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <div class="action-buttons">

                                                        <c:if test="${req.status eq 'pending'}">
                                                            <form method="post"
                                                                  action="${pageContext.request.contextPath}/adminreturns"
                                                                  style="display:inline;">
                                                                <input type="hidden" name="inspectionId"
                                                                       value="${req.inspectionId}"/>
                                                                <input type="hidden" name="action" value="mark_processing"/>
                                                                <button type="submit" class="btn btn-success btn-sm"
                                                                        title="Duyệt yêu cầu">
                                                                    <i class="fas fa-check"></i> Duyệt
                                                                </button>
                                                            </form>

                                                            <form method="post"
                                                                  action="${pageContext.request.contextPath}/adminreturns"
                                                                  style="display:inline;">
                                                                <input type="hidden" name="inspectionId"
                                                                       value="${req.inspectionId}"/>
                                                                <input type="hidden" name="action" value="cancel"/>
                                                                <button type="submit" class="btn btn-danger btn-sm"
                                                                        onclick="return confirm('Xác nhận từ chối yêu cầu hoàn cọc này?')">
                                                                    <i class="fas fa-times"></i> Từ chối
                                                                </button>
                                                            </form>
                                                        </c:if>

                                                        <c:if test="${req.status eq 'processing'}">
                                                            <form method="post"
                                                                  action="${pageContext.request.contextPath}/adminreturns"
                                                                  style="display:inline;">
                                                                <input type="hidden" name="orderId" value="${req.orderId}"/>
                                                                <input type="hidden" name="inspectionId"
                                                                       value="${req.inspectionId}"/>
                                                                <input type="hidden" name="action" value="complete_refund"/>
                                                                <input type="hidden" name="refundMethod" value="wallet"/>
                                                                <button type="submit" class="btn btn-primary btn-sm"
                                                                        onclick="return confirm('Xác nhận hoàn ${req.refundAmount} VNĐ về ví khách hàng?')">
                                                                    <i class="fas fa-wallet"></i> Về ví
                                                                </button>
                                                            </form>

                                                            <form method="post"
                                                                  action="${pageContext.request.contextPath}/adminreturns"
                                                                  style="display:inline;">
                                                                <input type="hidden" name="orderId" value="${req.orderId}"/>
                                                                <input type="hidden" name="inspectionId"
                                                                       value="${req.inspectionId}"/>
                                                                <input type="hidden" name="action" value="complete_refund"/>
                                                                <input type="hidden" name="refundMethod" value="cash"/>
                                                                <button type="submit" class="btn btn-secondary btn-sm"
                                                                        onclick="return confirm('Xác nhận đã hoàn ${req.refundAmount} VNĐ tiền mặt cho khách?')">
                                                                    <i class="fas fa-money-bill"></i> Tiền mặt
                                                                </button>
                                                            </form>
                                                        </c:if>
                                                    </div>
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