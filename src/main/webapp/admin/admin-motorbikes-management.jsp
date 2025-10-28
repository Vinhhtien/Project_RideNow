<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1"/>
  <title>Quản lý Xe Máy | RideNow Admin</title>

  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css"/>
  <!-- Giữ admin.css và thêm css riêng cho trang motor, không trùng selector -->
  <link rel="stylesheet" href="${ctx}/css/admin.css">
  <link rel="stylesheet" href="${ctx}/css/admin-motorbikes.css">
</head>

<body class="admin motor-page">
  <!-- SIDEBAR (giống dashboard) -->
  <aside class="sidebar">
    <div class="brand">
      <div class="brand-logo"><i class="fa-solid fa-motorcycle"></i></div>
      <h1>RideNow Admin</h1>
    </div>
    <nav class="sidebar-nav">
      <a href="${ctx}/admin/dashboard" class="nav-item"><i class="fa-solid fa-gauge"></i>Dashboard</a>
      <a href="${ctx}/admin/partners" class="nav-item"><i class="fa-solid fa-handshake"></i>Partners</a>
      <a href="${ctx}/admin/customers" class="nav-item"><i class="fa-solid fa-users"></i>Customers</a>
      <a href="${ctx}/admin/bikes" class="nav-item active"><i class="fa-solid fa-motorcycle"></i>Motorbikes</a>
      <a href="${ctx}/admin/orders" class="nav-item"><i class="fa-solid fa-receipt"></i>Orders</a>
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
        <h1>Quản lý Xe Máy</h1>
        <div class="breadcrumb">
          <span>Admin</span><i class="fa-solid fa-angle-right"></i><span class="active">Motorbikes</span>
        </div>
      </div>
      <div class="user-profile">
        <div class="user-avatar"><i class="fa-solid fa-user"></i></div>
        <span>Administrator</span>
      </div>
    </header>

    <fmt:setLocale value="vi_VN"/>

    <!-- TÍNH KPI TỪ DANH SÁCH motorbikes -->
    <c:set var="total" value="${fn:length(motorbikes)}"/>
    <c:set var="available" value="0"/>
    <c:set var="rented" value="0"/>
    <c:set var="maintenance" value="0"/>
    <c:set var="partnerCount" value="0"/>
    <c:set var="adminCount" value="0"/>

    <c:forEach var="b" items="${motorbikes}">
      <c:if test="${b.status == 'available'}"><c:set var="available" value="${available + 1}"/></c:if>
      <c:if test="${b.status == 'rented'}"><c:set var="rented" value="${rented + 1}"/></c:if>
      <c:if test="${b.status == 'maintenance'}"><c:set var="maintenance" value="${maintenance + 1}"/></c:if>
      <c:if test="${b.ownerType == 'Partner'}"><c:set var="partnerCount" value="${partnerCount + 1}"/></c:if>
      <c:if test="${b.ownerType == 'Admin'}"><c:set var="adminCount" value="${adminCount + 1}"/></c:if>
    </c:forEach>

    <!-- KPI GRID (scope .motor-page để không trùng css global) -->
    <section class="motor-kpi-grid">
      <div class="motor-kpi-card total"><div class="kpi-icon"><i class="fa-solid fa-list"></i></div><div><p>Tổng số xe</p><h2>${total}</h2></div></div>
      <div class="motor-kpi-card available"><div class="kpi-icon"><i class="fa-solid fa-circle-check"></i></div><div><p>Có sẵn</p><h2>${available}</h2></div></div>
      <div class="motor-kpi-card rented"><div class="kpi-icon"><i class="fa-solid fa-key"></i></div><div><p>Đã thuê</p><h2>${rented}</h2></div></div>
      <div class="motor-kpi-card maintenance"><div class="kpi-icon"><i class="fa-solid fa-wrench"></i></div><div><p>Bảo trì</p><h2>${maintenance}</h2></div></div>
      <div class="motor-kpi-card partners"><div class="kpi-icon"><i class="fa-solid fa-handshake"></i></div><div><p>Đối tác</p><h2>${partnerCount}</h2></div></div>
      <div class="motor-kpi-card admins"><div class="kpi-icon"><i class="fa-solid fa-building"></i></div><div><p>Admin</p><h2>${adminCount}</h2></div></div>
    </section>

    <!-- FILTERS -->
    <section class="motor-toolbar">
      <div>
        <label for="owner">Chủ sở hữu</label>
        <select id="owner">
          <option value="all">Tất cả</option>
          <option value="partner">Đối tác</option>
          <option value="admin">Admin</option>
        </select>
      </div>
      <div>
        <label for="status">Trạng thái</label>
        <select id="status">
          <option value="all">Tất cả</option>
          <option value="available">Có sẵn</option>
          <option value="rented">Đã thuê</option>
          <option value="maintenance">Bảo trì</option>
        </select>
      </div>
      <button id="filterBtn" class="btn btn-primary"><i class="fa-solid fa-filter"></i> Lọc</button>
      <a class="btn btn-primary" href="${ctx}/admin/bikes?action=new"><i class="fa-solid fa-plus"></i> Thêm Xe Mới</a>
    </section>

    <!-- BẢNG -->
    <section class="motor-table-section">
      <div class="table-wrapper">
        <table class="data-table motor-table">
          <thead>
          <tr>
            <th>ID</th>
            <th>Tên Xe</th>
            <th>Biển Số</th>
            <th>Loại Xe</th>
            <th>Chủ Sở Hữu</th>
            <th>Giá/ngày</th>
            <th>Trạng thái</th>
            <th>Hành động</th>
          </tr>
          </thead>
          <tbody>
          <c:forEach var="bike" items="${motorbikes}">
            <tr>
              <td>${bike.bikeId}</td>
              <td><strong><c:out value="${bike.bikeName}"/></strong></td>
              <td><c:out value="${bike.licensePlate}"/></td>
              <td><c:out value="${bike.typeName}"/></td>
              <td>
                <c:choose>
                  <c:when test="${bike.ownerType == 'Partner' && not empty bike.ownerName}">
                    <span class="badge badge-partner"><i class="fa-solid fa-store"></i> <c:out value="${bike.ownerName}"/></span>
                  </c:when>
                  <c:when test="${bike.ownerType == 'Admin' && not empty bike.ownerName}">
                    <span class="badge badge-admin"><i class="fa-solid fa-building"></i> <c:out value="${bike.ownerName}"/></span>
                  </c:when>
                  <c:otherwise><span class="badge"><i class="fa-regular fa-circle"></i> —</span></c:otherwise>
                </c:choose>
              </td>
              <td><fmt:formatNumber value="${bike.pricePerDay}" type="number" minFractionDigits="0" maxFractionDigits="0"/> ₫</td>
              <td>
                <span class="status-badge 
                  <c:choose>
                    <c:when test="${bike.status == 'available'}">confirmed</c:when>
                    <c:when test="${bike.status == 'rented'}">completed</c:when>
                    <c:otherwise>pending</c:otherwise>
                  </c:choose>">
                  <c:choose>
                    <c:when test="${bike.status == 'available'}">Có sẵn</c:when>
                    <c:when test="${bike.status == 'rented'}">Đã thuê</c:when>
                    <c:when test="${bike.status == 'maintenance'}">Bảo trì</c:when>
                    <c:otherwise><c:out value="${bike.status}"/></c:otherwise>
                  </c:choose>
                </span>
              </td>
              <td>
                <a class="btn btn-secondary btn-sm" href="${ctx}/admin/bikes?action=edit&id=${bike.bikeId}">
                  <i class="fa-solid fa-pen"></i> Sửa
                </a>
                <a class="btn btn-danger btn-sm" href="${ctx}/admin/bikes?action=delete&id=${bike.bikeId}"
                   onclick="return confirm('Xóa xe ${bike.bikeName} (${bike.licensePlate})?')">
                  <i class="fa-solid fa-trash"></i> Xóa
                </a>
              </td>
            </tr>
          </c:forEach>

          <c:if test="${empty motorbikes}">
            <tr>
              <td colspan="8" style="text-align:center;padding:48px;color:#64748b">
                <i class="fa-solid fa-motorcycle" style="font-size:42px;margin-bottom:10px;display:block;color:#cbd5e1;"></i>
                Không có xe nào trong hệ thống
              </td>
            </tr>
          </c:if>
          </tbody>
        </table>
      </div>
    </section>
  </main>

  <script>
    // Lọc
    document.getElementById('filterBtn').addEventListener('click', function () {
      const owner = document.getElementById('owner').value;
      const status = document.getElementById('status').value;
      let url = '${ctx}/admin/bikes?action=filter';
      if (owner !== 'all')  url += '&ownerType=' + owner;
      if (status !== 'all') url += '&status=' + status;
      window.location.href = url;
    });

    // Khôi phục filter từ querystring
    (function () {
      const q = new URLSearchParams(window.location.search);
      if (q.get('ownerType')) document.getElementById('owner').value  = q.get('ownerType');
      if (q.get('status'))    document.getElementById('status').value = q.get('status');
    })();
  </script>
</body>
</html>
