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
      <a href="${pageContext.request.contextPath}/admin/dashboard" class="nav-item active">
        <i class="fas fa-tachometer-alt"></i>
        <span>Dashboard</span>
      </a>
      <a href="${pageContext.request.contextPath}/admin/partners" class="nav-item">
        <i class="fas fa-handshake"></i>
        <span>Partners</span>
      </a>
      <a href="${pageContext.request.contextPath}/admin/customers" class="nav-item">
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
      <a href="${pageContext.request.contextPath}/adminpaymentverify" class="nav-item">
        <i class="fas fa-money-check-alt"></i>
        <span>Verify Payments</span>
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
        <h1>Dashboard</h1>
        <div class="breadcrumb">
          <span>Admin</span>
          <i class="fas fa-chevron-right"></i>
          <span class="active">Dashboard</span>
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

    <c:set var="k" value="${kpi}"/>

    <!-- KPI Cards -->
    <section class="kpi-grid">
      <div class="kpi-card">
        <div class="kpi-icon customers">
          <i class="fas fa-users"></i>
        </div>
        <div class="kpi-content">
          <div class="kpi-value">${k.totalCustomers}</div>
          <div class="kpi-label">Total Customers</div>
        </div>
      </div>
      
      <div class="kpi-card">
        <div class="kpi-icon partners">
          <i class="fas fa-handshake"></i>
        </div>
        <div class="kpi-content">
          <div class="kpi-value">${k.totalPartners}</div>
          <div class="kpi-label">Total Partners</div>
        </div>
      </div>
      
      <div class="kpi-card">
        <div class="kpi-icon bikes">
          <i class="fas fa-motorcycle"></i>
        </div>
        <div class="kpi-content">
          <div class="kpi-value">${k.totalBikes}</div>
          <div class="kpi-label">Total Motorbikes</div>
        </div>
      </div>
      
      <div class="kpi-card">
        <div class="kpi-icon orders">
          <i class="fas fa-clipboard-list"></i>
        </div>
        <div class="kpi-content">
          <div class="kpi-value">${k.totalOrders}</div>
          <div class="kpi-label">Total Orders</div>
        </div>
      </div>
      
      <div class="kpi-card wide">
        <div class="kpi-icon revenue">
          <i class="fas fa-money-bill-wave"></i>
        </div>
        <div class="kpi-content">
          <div class="kpi-value"><fmt:formatNumber value="${k.revenueToday}" type="currency"/></div>
          <div class="kpi-label">Today's Revenue</div>
        </div>
      </div>
      
      <div class="kpi-card wide">
        <div class="kpi-icon revenue">
          <i class="fas fa-chart-line"></i>
        </div>
        <div class="kpi-content">
          <div class="kpi-value"><fmt:formatNumber value="${k.revenueThisMonth}" type="currency"/></div>
          <div class="kpi-label">Monthly Revenue</div>
        </div>
      </div>
    </section>

    <!-- Main Content Grid -->
    <div class="content-grid">
      <!-- Latest Orders -->
      <section class="panel">
        <div class="panel-header">
          <h2>Latest Orders</h2>
          <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/orders">
            View All
            <i class="fas fa-arrow-right"></i>
          </a>
        </div>
        <div class="panel-body">
          <table class="data-table">
            <thead>
              <tr>
                <th>Order ID</th>
                <th>Customer</th>
                <th>Status</th>
                <th>Total</th>
                <th>Created Date</th>
                <th>Action</th>
              </tr>
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
                    Details
                    <i class="fas fa-external-link-alt"></i>
                  </a>
                </td>
              </tr>
            </c:forEach>
            </tbody>
          </table>
        </div>
      </section>

      <!-- Bikes under maintenance -->
      <section class="panel">
        <div class="panel-header">
          <h2>Bikes Under Maintenance</h2>
          <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/bikes?status=maintenance">
            Manage
            <i class="fas fa-cog"></i>
          </a>
        </div>
        <div class="panel-body">
          <div class="maintenance-list">
            <c:forEach var="b" items="${maintenanceBikes}">
              <div class="maintenance-item">
                <div class="bike-info">
                  <div class="bike-name">${b[0]}</div>
                  <div class="bike-model">${b[1]}</div>
                </div>
                <div class="maintenance-status">
                  <i class="fas fa-tools"></i>
                  <span>Under Maintenance</span>
                </div>
              </div>
            </c:forEach>
          </div>
        </div>
      </section>
      
      <!-- Recent Feedback -->
      <section class="panel">
        <div class="panel-header">
          <h2>Recent Feedback</h2>
          <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/feedback">
            View All
            <i class="fas fa-arrow-right"></i>
          </a>
        </div>
        <div class="panel-body">
          <div class="feedback-list">
            <div class="feedback-item">
              <div class="feedback-header">
                <div class="customer-name">Nguyễn Văn A</div>
                <div class="rating">
                  <i class="fas fa-star"></i>
                  <i class="fas fa-star"></i>
                  <i class="fas fa-star"></i>
                  <i class="fas fa-star"></i>
                  <i class="fas fa-star-half-alt"></i>
                </div>
              </div>
              <div class="feedback-content">
                Dịch vụ thuê xe rất tốt, xe mới và vận hành ổn định. Nhân viên hỗ trợ nhiệt tình.
              </div>
              <div class="feedback-date">2 hours ago</div>
            </div>
            <div class="feedback-item">
              <div class="feedback-header">
                <div class="customer-name">Trần Thị B</div>
                <div class="rating">
                  <i class="fas fa-star"></i>
                  <i class="fas fa-star"></i>
                  <i class="fas fa-star"></i>
                  <i class="fas fa-star"></i>
                  <i class="far fa-star"></i>
                </div>
              </div>
              <div class="feedback-content">
                Xe hơi cũ một chút nhưng giá cả hợp lý. Quy trình thuê xe nhanh chóng.
              </div>
              <div class="feedback-date">5 hours ago</div>
            </div>
          </div>
        </div>
      </section>
      
      <!-- Revenue Chart Placeholder -->
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
          <div class="chart-placeholder">
            <i class="fas fa-chart-line"></i>
            <p>Revenue chart will be displayed here</p>
          </div>
        </div>
      </section>
    </div>
  </main>
</body>
</html>