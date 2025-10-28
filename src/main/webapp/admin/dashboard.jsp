<%-- an --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt"  prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Admin Dashboard - RideNow</title>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
  <style>
    /* Additional Styles for Enhanced Dashboard */
    .quick-actions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1rem;
      margin-bottom: 1rem;
    }
    .action-card {
      display: flex;
      align-items: center;
      padding: 1.5rem;
      background: var(--card-bg);
      border: 1px solid var(--border-color);
      border-radius: 8px;
      text-decoration: none;
      color: inherit;
      transition: all 0.3s ease;
    }
    .action-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); border-color: var(--primary-color); }
    .action-icon { width: 48px; height: 48px; border-radius: 8px; display: flex; align-items: center; justify-content: center; margin-right: 1rem; font-size: 1.25rem; }
    .action-icon.primary { background: #e3f2fd; color: #1976d2; }
    .action-icon.warning { background: #fff3e0; color: #f57c00; }
    .action-icon.danger  { background: #ffebee; color: #d32f2f; }
    .action-icon.info    { background: #e8f5e8; color: #388e3c; }
    .action-icon.success { background: #e8f5e8; color: #388e3c; }
    .action-content { flex: 1; }
    .action-title { font-weight: 600; margin-bottom: 0.25rem; }
    .action-desc { font-size: 0.875rem; color: var(--text-light); }

    /* Header Actions */
    .header-actions { display: flex; gap: 0.5rem; }
    .btn-outline {
      background: transparent; border: 1px solid var(--primary-color); color: var(--primary-color);
      padding: 0.5rem 1rem; border-radius: 4px; text-decoration: none; font-size: 0.875rem; transition: all 0.3s ease;
    }
    .btn-outline:hover { background: var(--primary-color); color: white; }

    /* KPI Icon Colors */
    .kpi-icon.warning { background: #fff3e0; color: #f57c00; }
    .kpi-icon.maintenance { background: #ffebee; color: #d32f2f; }

   /* ==== Notify icon (đơn giản, an toàn, không keyframes) ==== */
.content-header .header-right { display:flex; align-items:center; gap:.75rem; }

.icon-btn{
  position: relative;
  display:inline-flex; align-items:center; justify-content:center;
  width:36px; height:36px; border-radius:50%;
  border:1px solid var(--border-color, #2b334d);
  background:var(--card-bg, #0f172a);
  color:var(--primary-color, #1976d2);
  text-decoration:none;
  margin-right:.25rem;
  transition: transform .15s ease, box-shadow .2s ease, background-color .2s ease, color .2s ease;
  -webkit-tap-highlight-color: transparent; /* tránh flash xanh trên mobile */
}
.icon-btn i{
  font-size:16px; line-height:1;
  position: relative; z-index: 1;  /* icon luôn ở trên */
}

/* hover: nâng nhẹ + viền sáng tinh tế */
.icon-btn:hover{
  transform: translateY(-1px);
  background: var(--primary-color, #1976d2);
  color: #fff;
  box-shadow:
    0 6px 18px rgba(0,0,0,.12),
    0 0 0 3px rgba(25, 118, 210, .12) inset;
}

/* active: nhấn nhẹ, KHÔNG làm mờ icon */
.icon-btn:active{
  transform: scale(.96);
  box-shadow: 0 2px 8px rgba(0,0,0,.18);
  opacity: 1; /* nếu global css có giảm opacity khi active thì dòng này sẽ chặn lại */
}

/* focus (bằng phím tab): vòng sáng truy cập */
.icon-btn:focus-visible{
  outline: none;
  box-shadow: 0 0 0 3px rgba(25, 118, 210, .35);
}
  </style>
</head>
<body class="admin">
  <fmt:setLocale value="vi_VN"/>

  <!-- Sidebar Navigation -->
  <aside class="sidebar">
    <div class="brand">
      <div class="brand-logo"><i class="fas fa-motorcycle"></i></div>
      <h1>RideNow Admin</h1>
    </div>
    <nav class="sidebar-nav">
      <a href="${pageContext.request.contextPath}/admin/dashboard" class="nav-item active"><i class="fas fa-tachometer-alt"></i><span>Dashboard</span></a>
      <a href="${pageContext.request.contextPath}/admin/partners" class="nav-item"><i class="fas fa-handshake"></i><span>Partners</span></a>
      <a href="${pageContext.request.contextPath}/admin/customers" class="nav-item"><i class="fas fa-users"></i><span>Customers</span></a>
      <a href="${pageContext.request.contextPath}/admin/bikes" class="nav-item"><i class="fas fa-motorcycle"></i><span>Motorbikes</span></a>
      <a href="${pageContext.request.contextPath}/admin/orders" class="nav-item"><i class="fas fa-clipboard-list"></i><span>Orders</span></a>
      <a href="${pageContext.request.contextPath}/adminpaymentverify" class="nav-item"><i class="fas fa-money-check-alt"></i><span>Verify Payments</span></a>
      <a href="${pageContext.request.contextPath}/adminpickup" class="nav-item"><i class="fas fa-shipping-fast"></i><span>Vehicle Pickup</span></a>
      <a href="${pageContext.request.contextPath}/adminreturn" class="nav-item"><i class="fas fa-undo-alt"></i><span>Vehicle Return</span></a>
      <a href="${pageContext.request.contextPath}/adminreturns" class="nav-item"><i class="fas fa-clipboard-check"></i><span>Verify & Refund</span></a>
      <a href="${pageContext.request.contextPath}/admin/reports" class="nav-item"><i class="fas fa-chart-bar"></i><span>Reports</span></a>
      <a href="${pageContext.request.contextPath}/admin/feedback" class="nav-item"><i class="fas fa-comment-alt"></i><span>Feedback</span></a>
      <a href="${pageContext.request.contextPath}/logout" class="nav-item logout"><i class="fas fa-sign-out-alt"></i><span>Logout</span></a>
    </nav>
  </aside>

  <!-- Main Content -->
  <main class="content">
    <header class="content-header">
      <div class="header-left">
        <h1>Dashboard</h1>
        <div class="breadcrumb">
          <span>Admin</span>
          <i class="fas fa-chevron-right"></i>
          <span class="active">Dashboard</span>
        </div>
      </div>
      <div class="header-right">
        <!-- NEW: Paper-plane icon → mở trang soạn thông báo trong khu admin -->
        <a class="icon-btn"
              href="${pageContext.request.contextPath}/admin/notify"
           title="Gửi thông báo tới tất cả Partner" aria-label="Gửi thông báo">
          <i class="fas fa-paper-plane"></i>
        </a>
        <div class="user-profile">
          <div class="user-avatar"><i class="fas fa-user-circle"></i></div>
          <span>Administrator</span>
        </div>
      </div>
    </header>

    <c:set var="k" value="${kpi}"/>

    <!-- KPI Cards -->
    <section class="kpi-grid">
      <div class="kpi-card">
        <div class="kpi-icon customers"><i class="fas fa-users"></i></div>
        <div class="kpi-content"><div class="kpi-value">${k.totalCustomers}</div><div class="kpi-label">Total Customers</div></div>
      </div>
      <div class="kpi-card">
        <div class="kpi-icon partners"><i class="fas fa-handshake"></i></div>
        <div class="kpi-content"><div class="kpi-value">${k.totalPartners}</div><div class="kpi-label">Total Partners</div></div>
      </div>
      <div class="kpi-card">
        <div class="kpi-icon bikes"><i class="fas fa-motorcycle"></i></div>
        <div class="kpi-content"><div class="kpi-value">${k.totalBikes}</div><div class="kpi-label">Total Motorbikes</div></div>
      </div>
      <div class="kpi-card">
        <div class="kpi-icon orders"><i class="fas fa-clipboard-list"></i></div>
        <div class="kpi-content"><div class="kpi-value">${k.totalOrders}</div><div class="kpi-label">Total Orders</div></div>
      </div>

      <!-- Extra KPI -->
      <div class="kpi-card">
        <div class="kpi-icon warning"><i class="fas fa-exclamation-triangle"></i></div>
        <div class="kpi-content">
          <div class="kpi-value">
            <c:choose><c:when test="${not empty k.pendingVerifications}">${k.pendingVerifications}</c:when><c:otherwise>5</c:otherwise></c:choose>
          </div>
          <div class="kpi-label">Pending Verifications</div>
        </div>
      </div>
      <div class="kpi-card">
        <div class="kpi-icon maintenance"><i class="fas fa-tools"></i></div>
        <div class="kpi-content">
          <div class="kpi-value">
            <c:choose><c:when test="${not empty k.bikesInMaintenance}">${k.bikesInMaintenance}</c:when><c:otherwise>3</c:otherwise></c:choose>
          </div>
          <div class="kpi-label">Bikes in Maintenance</div>
        </div>
      </div>
      <div class="kpi-card wide">
        <div class="kpi-icon revenue"><i class="fas fa-money-bill-wave"></i></div>
        <div class="kpi-content"><div class="kpi-value"><fmt:formatNumber value="${k.revenueToday}" type="currency"/></div><div class="kpi-label">Today's Revenue</div></div>
      </div>
      <div class="kpi-card wide">
        <div class="kpi-icon revenue"><i class="fas fa-chart-line"></i></div>
        <div class="kpi-content"><div class="kpi-value"><fmt:formatNumber value="${k.revenueThisMonth}" type="currency"/></div><div class="kpi-label">Monthly Revenue</div></div>
      </div>
    </section>

    <!-- Quick Actions -->
    <section class="panel">
      <div class="panel-header"><h2>Quick Actions</h2></div>
      <div class="panel-body">
        <div class="quick-actions-grid">
          <a href="${pageContext.request.contextPath}/admin/partners" class="action-card">
            <div class="action-icon primary"><i class="fas fa-handshake"></i></div>
            <div class="action-content"><div class="action-title">Manage Partners</div><div class="action-desc">Create, view and manage all partners</div></div>
          </a>
          <a href="${pageContext.request.contextPath}/adminpaymentverify" class="action-card">
            <div class="action-icon warning"><i class="fas fa-money-check-alt"></i></div>
            <div class="action-content"><div class="action-title">Verify Payments</div><div class="action-desc">Process pending payments</div></div>
          </a>
          <a href="${pageContext.request.contextPath}/admin/bikes" class="action-card">
            <div class="action-icon success"><i class="fas fa-motorcycle"></i></div>
            <div class="action-content"><div class="action-title">Manage Bikes</div><div class="action-desc">View and update bike inventory</div></div>
          </a>
          <a href="${pageContext.request.contextPath}/admin/orders" class="action-card">
            <div class="action-icon info"><i class="fas fa-clipboard-list"></i></div>
            <div class="action-content"><div class="action-title">View Orders</div><div class="action-desc">Manage customer orders</div></div>
          </a>
        </div>
      </div>
    </section>

    <!-- Main Content Grid -->
    <div class="content-grid">
      <section class="panel">
        <div class="panel-header">
          <h2>Latest Orders</h2>
          <div class="header-actions">
            <a class="btn btn-outline" href="${pageContext.request.contextPath}/admin/orders">View All Orders</a>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/partners">
              <i class="fas fa-handshake"></i> Manage Partners
            </a>
          </div>
        </div>
        <div class="panel-body">
          <table class="data-table">
            <thead>
              <tr><th>Order ID</th><th>Customer</th><th>Status</th><th>Total</th><th>Created Date</th><th>Action</th></tr>
            </thead>
            <tbody>
            <c:forEach var="o" items="${latestOrders}">
              <tr>
                <td>#${o.orderId}</td>
                <td>${o.customerName}</td>
                <td><span class="status-badge ${o.status}">${o.status}</span></td>
                <td><fmt:formatNumber value="${o.totalPrice}" type="currency"/></td>
                <td><fmt:formatDate value="${o.createdAtDate}" pattern="dd/MM/yyyy HH:mm"/></td>
                <td>
                  <a class="action-link" href="${pageContext.request.contextPath}/admin/orders/detail?id=${o.orderId}">
                    Details <i class="fas fa-external-link-alt"></i>
                  </a>
                </td>
              </tr>
            </c:forEach>
            </tbody>
          </table>
        </div>
      </section>

      <section class="panel">
        <div class="panel-header">
          <h2>Partner Management</h2>
          <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/partners">
            <i class="fas fa-cog"></i> Manage All Partners
          </a>
        </div>
        <div class="panel-body">
          <div class="quick-actions-grid" style="grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));">
            <div class="action-card">
              <div class="action-icon primary"><i class="fas fa-user-plus"></i></div>
              <div class="action-content">
                <div class="action-title">Create Partner</div>
                <div class="action-desc">Set up new partner account with default password</div>
                <a href="${pageContext.request.contextPath}/admin/partners" class="btn btn-outline" style="margin-top: .5rem; padding: .25rem .75rem; font-size: .75rem;">Get Started</a>
              </div>
            </div>
            <div class="action-card">
              <div class="action-icon info"><i class="fas fa-list"></i></div>
              <div class="action-content">
                <div class="action-title">View All Partners</div>
                <div class="action-desc">Manage existing partner accounts and permissions</div>
                <a href="${pageContext.request.contextPath}/admin/partners" class="btn btn-outline" style="margin-top: .5rem; padding: .25rem .75rem; font-size: .75rem;">View List</a>
              </div>
            </div>
            <div class="action-card">
              <div class="action-icon warning"><i class="fas fa-key"></i></div>
              <div class="action-content">
                <div class="action-title">Password Reset</div>
                <div class="action-desc">Partners using default password:
                  <strong><c:choose><c:when test="${not empty k.partnersWithDefaultPassword}">${k.partnersWithDefaultPassword}</c:when><c:otherwise>2</c:otherwise></c:choose></strong>
                </div>
                <span class="status-badge warning" style="margin-top:.5rem;">Action Required</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="panel">
        <div class="panel-header">
          <h2>Bikes Under Maintenance</h2>
          <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/bikes?status=maintenance">Manage <i class="fas fa-cog"></i></a>
        </div>
        <div class="panel-body">
          <div class="maintenance-list">
            <c:forEach var="b" items="${maintenanceBikes}">
              <div class="maintenance-item">
                <div class="bike-info"><div class="bike-name">${b[0]}</div><div class="bike-model">${b[1]}</div></div>
                <div class="maintenance-status"><i class="fas fa-tools"></i><span>Under Maintenance</span></div>
              </div>
            </c:forEach>
            <c:if test="${empty maintenanceBikes}">
              <div style="text-align:center; padding:2rem; color:var(--text-light);">
                <i class="fas fa-check-circle" style="font-size:2rem; margin-bottom:1rem; color:var(--success-color);"></i>
                <p>No bikes currently under maintenance</p>
              </div>
            </c:if>
          </div>
        </div>
      </section>

      <section class="panel">
        <div class="panel-header">
          <h2>Recent Feedback</h2>
          <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/feedback">View All <i class="fas fa-arrow-right"></i></a>
        </div>
        <div class="panel-body">
          <div class="feedback-list">
            <div class="feedback-item">
              <div class="feedback-header"><div class="customer-name">Nguyễn Văn A</div>
                <div class="rating"><i class="fas fa-star"></i><i class="fas fa-star"></i><i class="fas fa-star"></i><i class="fas fa-star"></i><i class="fas fa-star-half-alt"></i></div>
              </div>
              <div class="feedback-content">Dịch vụ thuê xe rất tốt, xe mới và vận hành ổn định. Nhân viên hỗ trợ nhiệt tình.</div>
              <div class="feedback-date">2 hours ago</div>
            </div>
            <div class="feedback-item">
              <div class="feedback-header"><div class="customer-name">Trần Thị B</div>
                <div class="rating"><i class="fas fa-star"></i><i class="fas fa-star"></i><i class="fas fa-star"></i><i class="fas fa-star"></i><i class="far fa-star"></i></div>
              </div>
              <div class="feedback-content">Xe hơi cũ một chút nhưng giá cả hợp lý. Quy trình thuê xe nhanh chóng.</div>
              <div class="feedback-date">5 hours ago</div>
            </div>
          </div>
        </div>
      </section>

      <section class="panel full-width">
        <div class="panel-header">
          <h2>Revenue Overview</h2>
          <div class="time-filter">
            <button class="filter-btn active">Today</button>
            <button class="filter-btn">Week</button>
            <button class="filter-btn">Month</button>
            <button class="filter-btn">Year</button>
          </div>
        </div>
        <div class="panel-body">
          <div class="chart-placeholder"><i class="fas fa-chart-line"></i><p>Revenue chart will be displayed here</p></div>
        </div>
      </section>
    </div>
  </main>

  <script>
    document.addEventListener('DOMContentLoaded', function() {
      console.log('Admin Dashboard initialized');
    });
  </script>
</body>
</html>
