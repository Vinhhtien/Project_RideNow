<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt"  prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Admin Dashboard - RideNow</title>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;800&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
</head>
<body class="admin">
  <fmt:setLocale value="vi_VN"/>
  <aside class="sidebar">
    <div class="brand">RideNow Admin</div>
<!--    <nav>
      <a class="active" href="${pageContext.request.contextPath}/admin/dashboard">Dashboard</a>
      <a href="${pageContext.request.contextPath}/admin/partners">Partners</a>
      <a href="${pageContext.request.contextPath}/admin/customers">Customers</a>
      <a href="${pageContext.request.contextPath}/admin/bikes">Motorbikes</a>
      <a href="${pageContext.request.contextPath}/admin/orders">Orders</a> 
      <a href="${pageContext.request.contextPath}/adminreturns">Returns</a>
      <a href="${pageContext.request.contextPath}/adminwithdrawals">Withdrawals</a>

      <a href="${pageContext.request.contextPath}/admin/payments">Payments</a>
      <a href="${pageContext.request.contextPath}/admin/reports">Reports</a>
      <a href="${pageContext.request.contextPath}/logout">Logout</a>
    </nav>
    -->
        
    <nav>
    <a href="${pageContext.request.contextPath}/admin/dashboard">Dashboard</a>
    <a href="${pageContext.request.contextPath}/adminpaymentverify">Xác Minh Thanh Toán</a>
    <a href="${pageContext.request.contextPath}/adminpickup">Giao Nhận Xe</a>
    <a href="${pageContext.request.contextPath}/adminreturn">Trả Xe</a>
    <a href="${pageContext.request.contextPath}/adminreturns">Hoàn Cọc</a>
    <a href="${pageContext.request.contextPath}/adminwithdrawals">Rút Tiền</a>
    <a href="${pageContext.request.contextPath}/logout">Logout</a>
</nav>
  </aside>

  <main class="content">
    <h1>Dashboard</h1>

    <c:set var="k" value="${kpi}"/>

    <!-- KPI Cards -->
    <section class="kpi">
      <div class="card"><div class="label">Customers</div><div class="value">${k.totalCustomers}</div></div>
      <div class="card"><div class="label">Partners</div><div class="value">${k.totalPartners}</div></div>
      <div class="card"><div class="label">Motorbikes</div><div class="value">${k.totalBikes}</div></div>
      <div class="card"><div class="label">Orders</div><div class="value">${k.totalOrders}</div></div>
      <div class="card wide">
        <div class="label">Doanh thu hôm nay</div>
        <div class="value"><fmt:formatNumber value="${k.revenueToday}" type="currency"/></div>
      </div>
      <div class="card wide">
        <div class="label">Doanh thu tháng</div>
        <div class="value"><fmt:formatNumber value="${k.revenueThisMonth}" type="currency"/></div>
      </div>
    </section>

    <!-- Latest Orders -->
    <section class="panel">
      <div class="panel-head">
        <h2>Đơn hàng mới nhất</h2>
        <a class="btn" href="${pageContext.request.contextPath}/admin/orders">Xem tất cả</a>
      </div>
      <table class="table">
        <thead>
          <tr><th>Mã</th><th>Khách</th><th>Trạng thái</th><th>Tổng tiền</th><th>Ngày tạo</th><th></th></tr>
        </thead>
        <tbody>
        <c:forEach var="o" items="${latestOrders}">
          <tr>
            <td>#${o.orderId}</td>
            <td>${o.customerName}</td>
            <td><span class="badge ${o.status}">${o.status}</span></td>
            <td><fmt:formatNumber value="${o.totalPrice}" type="currency"/></td>
            <fmt:formatDate value="${o.createdAtDate}" pattern="dd/MM/yyyy HH:mm"/>
            <td><a class="link" href="${pageContext.request.contextPath}/admin/orders/detail?id=${o.orderId}">Chi tiết</a></td>
          </tr>
        </c:forEach>
        </tbody>
      </table>
    </section>

    <!-- Bikes under maintenance (thay cho 'chờ xác minh') -->
    <section class="panel">
      <div class="panel-head">
        <h2>Xe đang bảo trì</h2>
        <a class="btn" href="${pageContext.request.contextPath}/admin/bikes?status=maintenance">Quản lý</a>
      </div>
      <ul class="list">
        <c:forEach var="b" items="${maintenanceBikes}">
          <li>
            <strong>${b[0]}</strong> — ${b[1]}
          </li>
        </c:forEach>
      </ul>
    </section>
  </main>
</body>
</html>

