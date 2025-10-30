<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1"/>
  <title>Chi Tiết Đơn Hàng | RideNow Admin</title>

  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css"/>
  <link rel="stylesheet" href="${ctx}/css/admin.css">
  <style>
    /* Order Detail Specific Styles */
    .order-header {
      background: white;
      border-radius: 12px;
      padding: 1.5rem;
      box-shadow: 0 2px 8px rgba(0,0,0,0.08);
      margin-bottom: 1.5rem;
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
    }

    .order-title h1 {
      margin: 0 0 0.5rem 0;
      font-size: 1.5rem;
      font-weight: 700;
      color: #1f2937;
    }

    .breadcrumb {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 0.875rem;
      color: #6b7280;
    }

    .breadcrumb a {
      color: #3b82f6;
      text-decoration: none;
      transition: color 0.2s;
    }

    .breadcrumb a:hover {
      color: #2563eb;
    }

    .breadcrumb i {
      font-size: 0.75rem;
    }

    .order-actions {
      display: flex;
      gap: 0.75rem;
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

    /* Main Content Grid */
    .order-content {
      display: grid;
      grid-template-columns: 2fr 1fr;
      gap: 1.5rem;
      margin-bottom: 1.5rem;
    }

    @media (max-width: 1024px) {
      .order-content {
        grid-template-columns: 1fr;
      }
    }

    .panel {
      background: white;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.08);
      overflow: hidden;
    }

    .panel-header {
      padding: 1.25rem 1.5rem;
      border-bottom: 1px solid #e5e7eb;
      background: #f8fafc;
    }

    .panel-header h2 {
      margin: 0;
      font-size: 1.125rem;
      font-weight: 600;
      color: #1f2937;
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .panel-body {
      padding: 1.5rem;
    }

    /* Order Information */
    .order-info-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 1rem;
      margin-bottom: 1.5rem;
    }

    .info-item {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .info-label {
      font-size: 0.875rem;
      font-weight: 500;
      color: #6b7280;
    }

    .info-value {
      font-size: 0.95rem;
      font-weight: 600;
      color: #1f2937;
    }

    .status-badge {
      padding: 0.375rem 0.75rem;
      border-radius: 6px;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: capitalize;
      letter-spacing: 0.025em;
      display: inline-flex;
      align-items: center;
      gap: 0.375rem;
    }

    .status-badge.pending { background: #fef3c7; color: #92400e; }
    .status-badge.confirmed { background: #dbeafe; color: #1e40af; }
    .status-badge.completed { background: #d1fae5; color: #065f46; }
    .status-badge.cancelled { background: #fee2e2; color: #991b1b; }

    /* Financial Summary */
    .financial-summary {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 1rem;
      margin-bottom: 1rem;
    }

    @media (max-width: 768px) {
      .financial-summary {
        grid-template-columns: repeat(2, 1fr);
      }
    }

    @media (max-width: 480px) {
      .financial-summary {
        grid-template-columns: 1fr;
      }
    }

    .summary-card {
      background: #f8fafc;
      border: 1px solid #e5e7eb;
      border-radius: 8px;
      padding: 1rem;
      text-align: center;
    }

    .summary-card .label {
      font-size: 0.75rem;
      color: #6b7280;
      font-weight: 500;
      margin-bottom: 0.5rem;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }

    .summary-card .value {
      font-size: 1.125rem;
      font-weight: 700;
      color: #1f2937;
      margin-bottom: 0.25rem;
    }

    .summary-card .sub {
      font-size: 0.7rem;
      color: #9ca3af;
    }

    .amount-positive { color: #065f46; }
    .amount-negative { color: #dc2626; }
    .amount-zero { color: #6b7280; }

    .info-chip {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.5rem 0.875rem;
      background: #f0f9ff;
      color: #0369a1;
      border-radius: 6px;
      font-size: 0.75rem;
      font-weight: 500;
    }

    /* Payment Breakdown */
    .payment-breakdown {
      background: #f8fafc;
      border: 1px solid #e5e7eb;
      border-radius: 8px;
      padding: 1rem;
      margin-bottom: 1rem;
    }

    .breakdown-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0.5rem 0;
      border-bottom: 1px solid #e5e7eb;
    }

    .breakdown-item:last-child {
      border-bottom: none;
      font-weight: 600;
      font-size: 1.1rem;
    }

    .breakdown-label {
      color: #6b7280;
      font-size: 0.875rem;
    }

    .breakdown-value {
      font-weight: 600;
      color: #1f2937;
    }

    /* Refund Information */
    .refund-info {
      background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
      border: 1px solid #bae6fd;
      border-radius: 8px;
      padding: 1.25rem;
      margin-bottom: 1rem;
    }

    .refund-header {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-bottom: 1rem;
    }

    .refund-header h3 {
      margin: 0;
      font-size: 1rem;
      font-weight: 600;
      color: #0369a1;
    }

    .refund-details {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 1rem;
    }

    .refund-detail {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .refund-label {
      font-size: 0.75rem;
      color: #6b7280;
      font-weight: 500;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }

    .refund-value {
      font-size: 0.95rem;
      font-weight: 600;
      color: #1f2937;
    }

    .refund-method {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.5rem 1rem;
      border-radius: 6px;
      font-weight: 500;
      font-size: 0.875rem;
    }

    .refund-method.cash {
      background: #d1fae5;
      color: #065f46;
    }

    .refund-method.wallet {
      background: #dbeafe;
      color: #1e40af;
    }

    .condition-badge {
      padding: 0.375rem 0.75rem;
      border-radius: 6px;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: capitalize;
    }

    .condition-excellent { background: #d1fae5; color: #065f46; }
    .condition-good { background: #fef3c7; color: #92400e; }
    .condition-damaged { background: #fee2e2; color: #991b1b; }

    /* Table Styles */
    .data-table {
      width: 100%;
      border-collapse: collapse;
    }

    .data-table th {
      background: #f8fafc;
      padding: 0.875rem 0.75rem;
      text-align: left;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      color: #6b7280;
      border-bottom: 1px solid #e5e7eb;
    }

    .data-table td {
      padding: 0.875rem 0.75rem;
      border-bottom: 1px solid #f1f5f9;
      font-size: 0.875rem;
    }

    .data-table tbody tr {
      transition: background-color 0.2s;
    }

    .data-table tbody tr:hover {
      background-color: #f8fafc;
    }

    .data-table tbody tr:last-child td {
      border-bottom: none;
    }

    .payment-status {
      display: inline-flex;
      align-items: center;
      gap: 0.375rem;
      padding: 0.375rem 0.75rem;
      border-radius: 6px;
      font-size: 0.75rem;
      font-weight: 500;
    }

    .payment-status.verified {
      background: #d1fae5;
      color: #065f46;
    }

    .payment-status.pending {
      background: #fef3c7;
      color: #92400e;
    }

    .empty-state {
      text-align: center;
      padding: 2rem 1rem;
      color: #6b7280;
    }

    .empty-state i {
      font-size: 2.5rem;
      margin-bottom: 0.75rem;
      opacity: 0.5;
      color: #cbd5e1;
    }

    .empty-state h3 {
      font-size: 1.125rem;
      margin: 0 0 0.5rem 0;
      color: #374151;
    }

    .empty-state p {
      margin: 0;
      font-size: 0.875rem;
    }

    /* Order Timeline */
    .timeline {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .timeline-item {
      display: flex;
      align-items: flex-start;
      gap: 0.75rem;
      padding: 0.75rem;
      border-radius: 8px;
      transition: background-color 0.2s;
    }

    .timeline-item:hover {
      background: #f8fafc;
    }

    .timeline-icon {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.875rem;
      flex-shrink: 0;
    }

    .timeline-icon.completed {
      background: #10b981;
      color: white;
    }

    .timeline-icon.pending {
      background: #f59e0b;
      color: white;
    }

    .timeline-icon.cancelled {
      background: #ef4444;
      color: white;
    }

    .timeline-content {
      flex: 1;
    }

    .timeline-title {
      font-weight: 600;
      color: #1f2937;
      font-size: 0.875rem;
      margin-bottom: 0.25rem;
    }

    .timeline-description {
      color: #6b7280;
      font-size: 0.75rem;
    }

    /* Responsive */
    @media (max-width: 768px) {
      .order-header {
        flex-direction: column;
        gap: 1rem;
      }

      .order-actions {
        width: 100%;
        justify-content: flex-end;
      }

      .order-info-grid {
        grid-template-columns: 1fr;
      }

      .refund-details {
        grid-template-columns: 1fr;
      }

      .data-table {
        display: block;
        overflow-x: auto;
      }
    }
  </style>
</head>

<body class="admin order-detail-page">
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
    <!-- Order Header -->
    <header class="order-header">
      <div class="order-title">
        <h1>Đơn Hàng #${order.orderId}</h1>
        <div class="breadcrumb">
          <a href="${ctx}/admin/dashboard">Admin</a>
          <i class="fa-solid fa-angle-right"></i>
          <a href="${ctx}/admin/orders">Orders</a>
          <i class="fa-solid fa-angle-right"></i>
          <span>Chi tiết</span>
        </div>
      </div>
      <div class="order-actions">
        <a class="btn btn-outline" href="${ctx}/admin/orders">
          <i class="fa-solid fa-arrow-left"></i>
          Quay lại
        </a>
      </div>
    </header>

    <c:if test="${notFound}">
      <section class="panel">
        <div class="panel-body">
          <div class="empty-state">
            <i class="fa-solid fa-receipt"></i>
            <h3>Không tìm thấy đơn hàng</h3>
            <p>Đơn hàng bạn đang tìm kiếm không tồn tại</p>
          </div>
        </div>
      </section>
    </c:if>

    <c:if test="${not empty order}">
      <div class="order-content">
        <!-- Left Column - Main Content -->
        <div style="display: flex; flex-direction: column; gap: 1.5rem;">
          <!-- Order Information -->
          <section class="panel">
            <div class="panel-header">
              <h2><i class="fa-solid fa-circle-info"></i> Thông Tin Đơn Hàng</h2>
            </div>
            <div class="panel-body">
              <div class="order-info-grid">
                <div class="info-item">
                  <div class="info-label">Mã đơn hàng</div>
                  <div class="info-value">#${order.orderId}</div>
                </div>
                <div class="info-item">
                  <div class="info-label">Khách hàng</div>
                  <div class="info-value">${order.customerName}</div>
                </div>
                <div class="info-item">
                  <div class="info-label">Trạng thái</div>
                  <div class="info-value">
                    <span class="status-badge ${order.orderStatus}">
                      <c:choose>
                        <c:when test="${order.orderStatus == 'pending'}">Đang chờ</c:when>
                        <c:when test="${order.orderStatus == 'confirmed'}">Đã xác nhận</c:when>
                        <c:when test="${order.orderStatus == 'completed'}">Hoàn thành</c:when>
                        <c:when test="${order.orderStatus == 'cancelled'}">Đã hủy</c:when>
                      </c:choose>
                    </span>
                  </div>
                </div>
                <div class="info-item">
                  <div class="info-label">Thời gian thuê</div>
                  <div class="info-value">
                    <fmt:formatDate value="${order.startDate}" pattern="dd/MM/yyyy"/> - 
                    <fmt:formatDate value="${order.endDate}" pattern="dd/MM/yyyy"/>
                  </div>
                </div>
                <div class="info-item">
                  <div class="info-label">Ngày tạo</div>
                  <div class="info-value">
                    <fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                  </div>
                </div>
              </div>

              <!-- Payment Breakdown -->
              <div class="payment-breakdown">
                <div class="breakdown-item">
                  <div class="breakdown-label">Tiền thuê xe</div>
                  <div class="breakdown-value"><fmt:formatNumber value="${order.orderTotal}" type="currency"/></div>
                </div>
                <div class="breakdown-item">
                  <div class="breakdown-label">Tiền cọc</div>
                  <div class="breakdown-value">500.000 ₫</div>
                </div>
                <div class="breakdown-item">
                  <div class="breakdown-label">Tổng cần thanh toán</div>
                  <div class="breakdown-value"><fmt:formatNumber value="${order.orderTotal + 500000}" type="currency"/></div>
                </div>
              </div>

              <div class="financial-summary">
                <div class="summary-card">
                  <div class="label">Tổng tiền</div>
                  <div class="value"><fmt:formatNumber value="${order.orderTotal + 500000}" type="currency"/></div>
                </div>
                <div class="summary-card">
                  <div class="label">Đã thanh toán</div>
                  <div class="value"><fmt:formatNumber value="${order.totalPaid}" type="currency"/></div>
                </div>
                <div class="summary-card">
                  <div class="label">Còn nợ</div>
                  <div class="value 
                    <c:choose>
                      <c:when test="${order.orderStatus == 'completed'}">amount-zero</c:when>
                      <c:when test="${(order.orderTotal + 500000) - order.totalPaid > 0}">amount-negative</c:when>
                      <c:otherwise>amount-zero</c:otherwise>
                    </c:choose>">
                    <c:choose>
                      <c:when test="${order.orderStatus == 'completed'}">0 ₫</c:when>
                      <c:otherwise>
                        <fmt:formatNumber value="${(order.orderTotal + 500000) - order.totalPaid}" type="currency"/>
                      </c:otherwise>
                    </c:choose>
                  </div>
                </div>
                <div class="summary-card">
                  <div class="label">Thanh toán</div>
                  <div class="value">${order.paymentCount}</div>
                  <c:if test="${not empty order.lastPaidAt}">
                    <div class="sub">Lần cuối: <fmt:formatDate value="${order.lastPaidAt}" pattern="dd/MM/yyyy HH:mm"/></div>
                  </c:if>
                </div>
              </div>

              <div class="info-chip">
                <i class="fa-solid fa-clock-rotate-left"></i>
                Cập nhật gần đây
              </div>
            </div>
          </section>

          <!-- ... phần đầu giữ nguyên ... -->

<!-- Refund Information (Only for completed orders) -->
<c:if test="${order.orderStatus == 'completed'}">
  <section class="panel">
    <div class="panel-header">
      <h2><i class="fa-solid fa-money-bill-wave"></i> Thông Tin Hoàn Tiền</h2>
    </div>
    <div class="panel-body">
      <c:choose>
        <c:when test="${not empty refund}">
          <div class="refund-info">
            <div class="refund-header">
              <i class="fa-solid fa-rotate-left" style="color:#0369a1;"></i>
              <h3>Đã ghi nhận hoàn cọc</h3>
            </div>

            <div class="refund-details">
              <!-- Số tiền hoàn -->
              <div class="refund-detail">
                <div class="refund-label">Số tiền hoàn</div>
                <div class="refund-value" style="color:#065f46;font-size:1.1rem;">
                  <strong>
                    <fmt:formatNumber value="${empty refund.refundAmount ? 0 : refund.refundAmount}" type="currency"/>
                  </strong>
                </div>
              </div>

              <!-- Phương thức -->
              <div class="refund-detail">
                <div class="refund-label">Phương thức</div>
                <div class="refund-value">
                  <span class="refund-method ${refund.refundMethod}">
                    <c:choose>
                      <c:when test="${refund.refundMethod == 'cash'}">
                        <i class="fa-solid fa-money-bill-wave"></i> Tiền mặt
                      </c:when>
                      <c:when test="${refund.refundMethod == 'wallet'}">
                        <i class="fa-solid fa-wallet"></i> Ví điện tử
                      </c:when>
                      <c:otherwise>
                        ${refund.refundMethod}
                      </c:otherwise>
                    </c:choose>
                  </span>
                </div>
              </div>

              <!-- Tình trạng xe -->
              <div class="refund-detail">
                <div class="refund-label">Tình trạng xe</div>
                <div class="refund-value">
                  <span class="condition-badge condition-${refund.bikeCondition}">
                    <c:choose>
                      <c:when test="${refund.bikeCondition == 'excellent'}">
                        <i class="fa-solid fa-circle-check"></i> Xuất sắc
                      </c:when>
                      <c:when test="${refund.bikeCondition == 'good'}">
                        <i class="fa-solid fa-thumbs-up"></i> Tốt
                      </c:when>
                      <c:when test="${refund.bikeCondition == 'damaged'}">
                        <i class="fa-solid fa-triangle-exclamation"></i> Hư hỏng
                      </c:when>
                      <c:otherwise>${refund.bikeCondition}</c:otherwise>
                    </c:choose>
                  </span>
                </div>
              </div>

              <!-- Phí hư hỏng -->
              <div class="refund-detail">
                <div class="refund-label">Phí hư hỏng</div>
                <div class="refund-value">
                  <c:choose>
                    <c:when test="${not empty refund.damageFee && refund.damageFee > 0}">
                      <span style="color:#dc2626;">
                        -<fmt:formatNumber value="${refund.damageFee}" type="currency"/>
                      </span>
                    </c:when>
                    <c:otherwise>
                      <span style="color:#065f46;">0 ₫</span>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>
            </div>

            <!-- Ghi chú hư hỏng (nếu có) -->
            <c:if test="${not empty refund.damageNotes}">
              <div style="margin-top:1rem;padding-top:1rem;border-top:1px solid #bae6fd;">
                <div class="refund-label">Ghi chú hư hỏng</div>
                <div style="font-size:0.875rem;color:#6b7280;margin-top:0.25rem;">
                  ${refund.damageNotes}
                </div>
              </div>
            </c:if>

            <!-- Ghi chú admin (nếu có) -->
            <c:if test="${not empty refund.adminNotes}">
              <div style="margin-top:1rem;padding-top:1rem;border-top:1px solid #bae6fd;">
                <div class="refund-label">Ghi chú của admin</div>
                <div style="font-size:0.875rem;color:#6b7280;margin-top:0.25rem;">
                  ${refund.adminNotes}
                </div>
              </div>
            </c:if>

            <!-- Ngày hoàn -->
            <div style="margin-top:1rem;padding-top:1rem;border-top:1px solid #bae6fd;">
              <div class="refund-label">Ngày hoàn tiền</div>
              <div style="font-size:0.875rem;color:#6b7280;margin-top:0.25rem;">
                <fmt:formatDate value="${refund.inspectedAt}" pattern="dd/MM/yyyy HH:mm"/>
              </div>
            </div>
          </div>
        </c:when>

        <c:otherwise>
          <div class="empty-state">
            <i class="fa-solid fa-rotate-left"></i>
            <h3>Chưa có dữ liệu hoàn tiền</h3>
            <p>Đơn đã hoàn thành nhưng chưa ghi nhận thông tin hoàn cọc</p>
          </div>
        </c:otherwise>
      </c:choose>
    </div>
  </section>
</c:if>



          <!-- Items -->
          <section class="panel">
            <div class="panel-header">
              <h2><i class="fa-solid fa-motorcycle"></i> Danh Sách Xe Thuê</h2>
            </div>
            <div class="panel-body">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Tên Xe</th>
                    <th>Biển Số</th>
                    <th>Giá/ngày</th>
                    <th>SL</th>
                    <th>Thành tiền</th>
                  </tr>
                </thead>
                <tbody>
                <c:forEach var="it" items="${items}" varStatus="status">
                  <tr>
                    <td>${status.index + 1}</td>
                    <td><strong>${it.bikeName}</strong></td>
                    <td>${it.licensePlate}</td>
                    <td><fmt:formatNumber value="${it.pricePerDay}" type="currency"/></td>
                    <td>${it.quantity}</td>
                    <td><strong><fmt:formatNumber value="${it.lineTotal}" type="currency"/></strong></td>
                  </tr>
                </c:forEach>
                </tbody>
              </table>
            </div>
          </section>

          <!-- Payments -->
          <section class="panel">
            <div class="panel-header">
              <h2><i class="fa-solid fa-credit-card"></i> Lịch Sử Thanh Toán</h2>
            </div>
            <div class="panel-body">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>Mã</th>
                    <th>Ngày</th>
                    <th>Số tiền</th>
                    <th>Phương thức</th>
                    <th>Trạng thái</th>
                    <th>Xác minh</th>
                  </tr>
                </thead>
                <tbody>
                <c:forEach var="p" items="${payments}">
                  <tr>
                    <td><strong>#${p.paymentId}</strong></td>
                    <td><fmt:formatDate value="${p.paymentDate}" pattern="dd/MM/yyyy HH:mm"/></td>
                    <td><strong><fmt:formatNumber value="${p.amount}" type="currency"/></strong></td>
                    <td>
                      <c:choose>
                        <c:when test="${p.method == 'cash'}">
                          <span style="display: inline-flex; align-items: center; gap: 0.25rem;">
                            <i class="fa-solid fa-money-bill-wave"></i> Tiền mặt
                          </span>
                        </c:when>
                        <c:when test="${p.method == 'bank_transfer'}">
                          <span style="display: inline-flex; align-items: center; gap: 0.25rem;">
                            <i class="fa-solid fa-building-columns"></i> Chuyển khoản
                          </span>
                        </c:when>
                        <c:otherwise>${p.method}</c:otherwise>
                      </c:choose>
                    </td>
                    <td>
                      <span class="payment-status ${p.status == 'paid' ? 'verified' : 'pending'}">
                        <c:choose>
                          <c:when test="${p.status == 'paid'}">Đã thanh toán</c:when>
                          <c:when test="${p.status == 'pending'}">Đang chờ</c:when>
                          <c:when test="${p.status == 'failed'}">Thất bại</c:when>
                          <c:when test="${p.status == 'refunded'}">Đã hoàn tiền</c:when>
                          <c:otherwise>${p.status}</c:otherwise>
                        </c:choose>
                      </span>
                    </td>
                    <td>
                      <c:choose>
                        <c:when test="${not empty p.verifiedBy}">
                          <span class="payment-status verified">
                            <i class="fa-solid fa-check"></i> Đã xác minh
                          </span>
                        </c:when>
                        <c:otherwise>
                          <span class="payment-status pending">
                            <i class="fa-solid fa-clock"></i> Chờ xác minh
                          </span>
                        </c:otherwise>
                      </c:choose>
                    </td>
                  </tr>
                </c:forEach>
                <c:if test="${empty payments}">
                  <tr>
                    <td colspan="6">
                      <div class="empty-state">
                        <i class="fa-solid fa-credit-card"></i>
                        <h3>Chưa có thanh toán</h3>
                        <p>Chưa có giao dịch thanh toán nào</p>
                      </div>
                    </td>
                  </tr>
                </c:if>
                </tbody>
              </table>
            </div>
          </section>
        </div>

        <!-- Right Column - Sidebar -->
        <div style="display: flex; flex-direction: column; gap: 1.5rem;">
          <!-- Order Timeline -->
          <section class="panel">
            <div class="panel-header">
              <h2><i class="fa-solid fa-list-check"></i> Trạng Thái</h2>
            </div>
            <div class="panel-body">
              <div class="timeline">
                <div class="timeline-item">
                  <div class="timeline-icon completed">
                    <i class="fa-solid fa-check"></i>
                  </div>
                  <div class="timeline-content">
                    <div class="timeline-title">Đơn hàng được tạo</div>
                    <div class="timeline-description">
                      <fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                    </div>
                  </div>
                </div>
                
                <c:if test="${order.orderStatus == 'confirmed' || order.orderStatus == 'completed'}">
                  <div class="timeline-item">
                    <div class="timeline-icon completed">
                      <i class="fa-solid fa-check"></i>
                    </div>
                    <div class="timeline-content">
                      <div class="timeline-title">Đã xác nhận</div>
                      <div class="timeline-description">Đã xác nhận bởi hệ thống</div>
                    </div>
                  </div>
                </c:if>
                
                <c:if test="${order.orderStatus == 'completed'}">
                  <div class="timeline-item">
                    <div class="timeline-icon completed">
                      <i class="fa-solid fa-check"></i>
                    </div>
                    <div class="timeline-content">
                      <div class="timeline-title">Hoàn thành</div>
                      <div class="timeline-description">Xe đã được trả và xác nhận</div>
                    </div>
                  </div>
                </c:if>
                
                <c:if test="${order.orderStatus == 'cancelled'}">
                  <div class="timeline-item">
                    <div class="timeline-icon cancelled">
                      <i class="fa-solid fa-xmark"></i>
                    </div>
                    <div class="timeline-content">
                      <div class="timeline-title">Đã hủy</div>
                      <div class="timeline-description">Đơn hàng đã bị hủy</div>
                    </div>
                  </div>
                </c:if>
              </div>
            </div>
          </section>

          <!-- Quick Actions -->
          <section class="panel">
            <div class="panel-header">
              <h2><i class="fa-solid fa-bolt"></i> Hành Động</h2>
            </div>
            <div class="panel-body">
              <div style="display: flex; flex-direction: column; gap: 0.75rem;">
                <button class="btn btn-outline" style="justify-content: start;">
                  <i class="fa-solid fa-print"></i> In hóa đơn
                </button>
                <button class="btn btn-outline" style="justify-content: start;">
                  <i class="fa-solid fa-envelope"></i> Gửi email
                </button>
                <c:if test="${order.orderStatus == 'confirmed'}">
                  <button class="btn btn-primary" style="justify-content: start;">
                    <i class="fa-solid fa-flag-checkered"></i> Đánh dấu hoàn thành
                  </button>
                </c:if>
              </div>
            </div>
          </section>
        </div>
      </div>
    </c:if>
  </main>
</body>
</html>