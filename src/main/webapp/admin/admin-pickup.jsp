<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Giao Nhận Xe - RideNow Admin</title>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/css/bootstrap.min.css">
        <link rel="stylesheet" href="${ctx}/css/admin.css">
        <style>
            .btn:disabled {
                opacity: 0.6;
                cursor: not-allowed;
            }

            .btn-secondary {
                background-color: #6c757d;
                border-color: #6c757d;
            }

            .btn-overdue {
                background-color: #dc3545;
                border-color: #dc3545;
                color: white;
            }

            .btn-overdue:hover {
                background-color: #c82333;
                border-color: #bd2130;
            }

            .text-muted {
                color: #6c757d !important;
            }

            .small {
                font-size: 0.875em;
            }

            .mt-1 {
                margin-top: 0.25rem;
            }

            .pickup-info {
                font-size: 0.8rem;
                color: #6c757d;
                margin-top: 0.25rem;
            }

            .overdue-info {
                font-size: 0.8rem;
                color: #dc3545;
                margin-top: 0.25rem;
                font-weight: bold;
            }

            .modal-overdue .modal-header {
                background-color: #fff3cd;
                border-bottom: 2px solid #ffc107;
            }

            .modal-overdue .modal-title {
                color: #856404;
                font-weight: bold;
            }
        </style>
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
                <a href="${ctx}/adminpickup" class="nav-item active">
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
                    <h1>Giao Nhận Xe</h1>
                    <div class="breadcrumb">
                        <span>Admin</span>
                        <i class="fas fa-chevron-right"></i>
                        <span class="active">Giao Nhận Xe</span>
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
                        <div class="kpi-value">${not empty orders ? orders.size() : 0}</div>
                        <div class="kpi-label">Chờ Giao Nhận</div>
                    </div>
                </div>

                <div class="kpi-card">
                    <div class="kpi-icon" style="background: linear-gradient(135deg, #3b82f6, #2563eb);">
                        <i class="fas fa-calendar-day"></i>
                    </div>
                    <div class="kpi-content">
                        <div class="kpi-value">
                            <c:set var="todayPickups" value="0"/>
                            <c:forEach var="o" items="${orders}">
                                <c:if test="${o[4] eq today}">
                                    <c:set var="todayPickups" value="${todayPickups + 1}"/>
                                </c:if>
                            </c:forEach>
                            ${todayPickups}
                        </div>
                        <div class="kpi-label">Giao Hôm Nay</div>
                    </div>
                </div>

                <div class="kpi-card">
                    <div class="kpi-icon" style="background: linear-gradient(135deg, #10b981, #059669);">
                        <i class="fas fa-money-bill-wave"></i>
                    </div>
                    <div class="kpi-content">
                        <div class="kpi-value">
                            <c:set var="totalDeposit" value="0"/>
                            <c:forEach var="o" items="${orders}">
                                <c:set var="totalDeposit" value="${totalDeposit + o[7]}"/>
                            </c:forEach>
                            <fmt:formatNumber value="${totalDeposit}" type="currency"/>
                        </div>
                        <div class="kpi-label">Tổng Tiền Cọc</div>
                    </div>
                </div>
            </section>

            <!-- Orders Panel -->
            <section class="panel">
                <div class="panel-header">
                    <h2>
                        <i class="fas fa-truck-loading"></i>
                        Đơn Hàng Chờ Giao Xe
                    </h2>
                    <div class="panel-stats">
                        <span class="stat-badge">
                            <i class="fas fa-list"></i>
                            Tổng số: ${not empty orders ? orders.size() : 0}
                        </span>
                    </div>
                </div>

                <div class="panel-body">
                    <c:choose>
                        <c:when test="${empty orders}">
                            <div class="empty-state">
                                <i class="fas fa-motorcycle"></i>
                                <h3>Không có đơn hàng nào chờ giao xe</h3>
                                <p>Tất cả các đơn hàng đã được xử lý</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <!-- ===== BẮT ĐẦU PHÂN TRANG TRONG JSP (10 items/page) ===== -->
                            <c:set var="pageSize" value="10"/>
                            <c:set var="page" value="${param.page != null ? param.page : 1}"/>
                            <c:set var="totalOrders" value="${not empty orders ? orders.size() : 0}"/>
                            <c:set var="totalPages" value="${(totalOrders / pageSize) + (totalOrders % pageSize > 0 ? 1 : 0)}"/>
                            <c:set var="start" value="${(page - 1) * pageSize}"/>
                            <c:set var="end" value="${page * pageSize - 1}"/>
                            <c:if test="${end >= totalOrders}">
                                <c:set var="end" value="${totalOrders - 1}"/>
                            </c:if>
                            <!-- ===== KẾT THÚC PHẦN TÍNH TOÁN ===== -->

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
                                            <th>Trạng Thái</th>
                                            <th>Thao Tác</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="o" items="${orders}" varStatus="status">
                                            <c:if test="${status.index >= start && status.index <= end}">
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
                                                            <strong>Từ:</strong> <fmt:formatDate value="${o[4]}" pattern="dd/MM/yyyy"/><br>
                                                            <strong>Đến:</strong> <fmt:formatDate value="${o[5]}" pattern="dd/MM/yyyy"/>
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <strong style="color: #3b82f6;">
                                                            <fmt:formatNumber value="${o[6]}" type="currency"/>
                                                        </strong>
                                                    </td>
                                                    <td>
                                                        <strong style="color: #059669;">
                                                            <fmt:formatNumber value="${o[7]}" type="currency"/>
                                                        </strong>
                                                    </td>
                                                    <td>
                                                        <span class="status-badge pending">
                                                            <i class="fas fa-clock"></i>
                                                            ${o[8]}
                                                        </span>
                                                    </td>
                                                    <td>
                                                        <!-- GIỮ NGUYÊN PHẦN XỬ LÝ (NGÀY, FORM, MODAL) -->
                                                        <jsp:useBean id="now" class="java.util.Date"/>
                                                        <fmt:formatDate value="${now}" pattern="yyyyMMdd" var="todayNumber"/>
                                                        <fmt:formatDate value="${o[4]}" pattern="yyyyMMdd" var="rentalDateNumber"/>

                                                        <c:set var="canPickup" value="${todayNumber >= rentalDateNumber}"/>
                                                        <c:set var="isOverdue" value="${todayNumber > rentalDateNumber}"/>

                                                        <c:choose>
                                                            <c:when test="${!canPickup}">
                                                                <button type="button" class="btn btn-secondary btn-sm" disabled
                                                                        title="Không thể nhận xe trước ngày thuê: <fmt:formatDate value='${o[4]}' pattern='dd/MM/yyyy'/>">
                                                                    <i class="fas fa-clock"></i> Chưa đến ngày
                                                                </button>
                                                                <div class="pickup-info">
                                                                    Có thể nhận từ: <strong><fmt:formatDate value="${o[4]}" pattern="dd/MM/yyyy"/></strong>
                                                                </div>
                                                            </c:when>
                                                            <c:when test="${canPickup && !isOverdue}">
                                                                <form method="post" action="${ctx}/adminpickup" onsubmit="return confirm('Xác nhận khách đã nhận xe?');">
                                                                    <input type="hidden" name="orderId" value="${o[0]}"/>
                                                                    <input type="hidden" name="actionType" value="normal_pickup"/>
                                                                    <button type="submit" class="btn btn-primary btn-sm">
                                                                        <i class="fas fa-check"></i> Đã Nhận Xe
                                                                    </button>
                                                                </form>
                                                                <div class="pickup-info">Có thể nhận xe</div>
                                                            </c:when>
                                                            <c:when test="${isOverdue}">
                                                                <button type="button" class="btn btn-overdue btn-sm" data-bs-toggle="modal" data-bs-target="#overdueModal${o[0]}">
                                                                    <i class="fas fa-exclamation-triangle"></i> Quá Hạn
                                                                </button>
                                                                <div class="overdue-info">
                                                                    Quá hạn từ: <strong><fmt:formatDate value="${o[4]}" pattern="dd/MM/yyyy"/></strong>
                                                                </div>

                                                                <!-- Modal (nguyên như trước) -->
                                                                <div class="modal fade" id="overdueModal${o[0]}" tabindex="-1" aria-hidden="true">
                                                                    <div class="modal-dialog modal-dialog-centered">
                                                                        <div class="modal-content modal-overdue">
                                                                            <div class="modal-header">
                                                                                <h5 class="modal-title">
                                                                                    <i class="fas fa-exclamation-triangle text-warning me-2"></i>
                                                                                    Xác Nhận Đơn Quá Hạn #${o[0]}
                                                                                </h5>
                                                                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                                            </div>
                                                                            <div class="modal-body">
                                                                                <p><strong>Đơn hàng này đã quá ngày thuê!</strong></p>
                                                                                <p>Ngày thuê: <fmt:formatDate value="${o[4]}" pattern="dd/MM/yyyy"/></p>
                                                                                <p>Vui lòng xác nhận tình trạng:</p>

                                                                                <div class="mb-3">
                                                                                    <label class="form-label">Ghi chú (tùy chọn):</label>
                                                                                    <textarea class="form-control" name="notes" rows="2" placeholder="Nhập ghi chú về tình trạng giao xe..."></textarea>
                                                                                </div>
                                                                            </div>
                                                                            <div class="modal-footer">
                                                                                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>

                                                                                <form method="post" action="${ctx}/adminpickup" class="d-inline">
                                                                                    <input type="hidden" name="orderId" value="${o[0]}"/>
                                                                                    <input type="hidden" name="actionType" value="overdue_pickup"/>
                                                                                    <input type="hidden" name="notes" value=""/>
                                                                                    <button type="submit" class="btn btn-success"
                                                                                            onclick="this.form.notes.value = this.form.parentElement.parentElement.querySelector('textarea').value;">
                                                                                        <i class="fas fa-check"></i> Đã Giao Xe
                                                                                    </button>
                                                                                </form>

                                                                                <form method="post" action="${ctx}/adminpickup" class="d-inline">
                                                                                    <input type="hidden" name="orderId" value="${o[0]}"/>
                                                                                    <input type="hidden" name="actionType" value="mark_not_given"/>
                                                                                    <input type="hidden" name="notes" value=""/>
<!--                                                                                    <button type="submit" class="btn btn-warning"
                                                                                            onclick="this.form.notes.value = this.form.parentElement.parentElement.querySelector('textarea').value;">
                                                                                        <i class="fas fa-times"></i> Chưa Giao Xe
                                                                                    </button>-->
                                                                                </form>
                                                                            </div>
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                            </c:when>
                                                        </c:choose>
                                                    </td>
                                                </tr>
                                            </c:if>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>

                            <!-- Pagination -->
                            <div class="d-flex justify-content-center align-items-center mt-3">
                                <c:if test="${totalPages > 1}">
                                    <nav>
                                        <ul class="pagination">
                                            <li class="page-item ${page == 1 ? 'disabled' : ''}">
                                                <a class="page-link" href="?page=${page - 1}" aria-label="Previous">&laquo;</a>
                                            </li>

                                            <c:forEach var="i" begin="1" end="${totalPages}">
                                                <li class="page-item ${i == page ? 'active' : ''}">
                                                    <a class="page-link" href="?page=${i}">${i}</a>
                                                </li>
                                            </c:forEach>

                                            <li class="page-item ${page == totalPages ? 'disabled' : ''}">
                                                <a class="page-link" href="?page=${page + 1}" aria-label="Next">&raquo;</a>
                                            </li>
                                        </ul>
                                    </nav>
                                </c:if>
                            </div>

                        </c:otherwise>
                    </c:choose>
                </div>
            </section>
        </main>

        <!-- Bootstrap JS -->
        <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/js/bootstrap.bundle.min.js"></script>

        <script>
                                                                                                // JavaScript để xử lý ghi chú trong modal
                                                                                                document.addEventListener('DOMContentLoaded', function () {
                                                                                                    var modals = document.querySelectorAll('.modal');
                                                                                                    modals.forEach(function (modal) {
                                                                                                        modal.addEventListener('show.bs.modal', function () {
                                                                                                            var textarea = this.querySelector('textarea');
                                                                                                            var forms = this.querySelectorAll('form');
                                                                                                            forms.forEach(function (form) {
                                                                                                                var hiddenNotes = form.querySelector('input[name="notes"]');
                                                                                                                form.addEventListener('submit', function () {
                                                                                                                    hiddenNotes.value = textarea.value;
                                                                                                                });
                                                                                                            });
                                                                                                        });
                                                                                                    });
                                                                                                });
        </script>
    </body>

</html>