<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Quản lý Xe Máy - RideNow Admin</title>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

  <!-- ========== Standalone CSS cho trang Motorbikes (không phụ thuộc admin.css) ========== -->
  <style>
    :root{
      --mb-primary:#3b82f6; --mb-primary-600:#2563eb; --mb-primary-50:#eef2ff;
      --mb-success:#10b981; --mb-success-50:#ecfdf5;
      --mb-warn:#f59e0b; --mb-warn-50:#fffbeb;
      --mb-danger:#ef4444; --mb-danger-50:#fff1f2;
      --mb-ink:#0f172a; --mb-muted:#64748b;
      --mb-bg:#f8fafc; --mb-card:#ffffff; --mb-line:#e5e7eb;
      --mb-radius:12px; --mb-shadow:0 8px 20px rgba(2,6,23,.06);
    }
    *{box-sizing:border-box}
    html,body{height:100%}
    body.mb-admin{
      margin:0;background:var(--mb-bg);color:var(--mb-ink);
      font-family:Inter,system-ui,-apple-system,"Segoe UI",Roboto,Arial,sans-serif;
      display:flex;min-height:100vh;
    }

    /* ===== Sidebar ===== */
    .mb-sidebar{width:260px;background:#1e293b;color:#e5e7eb;display:flex;flex-direction:column;box-shadow:var(--mb-shadow)}
    .mb-brand{display:flex;align-items:center;padding:18px;border-bottom:1px solid rgba(255,255,255,.06)}
    .mb-brand-logo{width:42px;height:42px;border-radius:10px;display:grid;place-items:center;color:#fff;
      background:linear-gradient(135deg,var(--mb-primary),#60a5fa);margin-right:12px}
    .mb-brand h1{margin:0;font-size:18px;font-weight:800}
    .mb-nav{padding:12px 8px;display:flex;flex-direction:column}
    .mb-nav a{display:flex;align-items:center;gap:12px;padding:12px 14px;border-radius:10px;
      color:#cbd5e1;text-decoration:none;transition:.15s}
    .mb-nav a i{width:20px;text-align:center}
    .mb-nav a:hover{background:rgba(255,255,255,.06);color:#fff}
    .mb-nav a.active{background:rgba(59,130,246,.16);color:#fff;outline:1px solid rgba(59,130,246,.25)}

    /* ===== Content / Header ===== */
    .mb-content{flex:1;display:flex;flex-direction:column}
    .mb-header{background:var(--mb-card);padding:14px 22px;display:flex;justify-content:space-between;align-items:center;box-shadow:0 1px 0 var(--mb-line);position:sticky;top:0;z-index:5}
    .mb-header h1{margin:2px 0;font-size:22px;font-weight:800}
    .mb-breadcrumb{display:flex;align-items:center;gap:8px;color:#6b7280;font-size:13px}
    .mb-breadcrumb .active{color:var(--mb-primary);font-weight:600}
    .mb-user{display:flex;align-items:center;gap:10px}
    .mb-avatar{width:40px;height:40px;border-radius:50%;display:grid;place-items:center;color:#4f46e5;background:#eef2ff}

    /* ===== Filters ===== */
    .mb-filters{margin:18px 22px 0;background:var(--mb-card);border:1px solid var(--mb-line);
      border-radius:var(--mb-radius);box-shadow:var(--mb-shadow);padding:14px 16px;display:flex;gap:16px;align-items:end}
    .mb-filter{display:flex;flex-direction:column;gap:6px}
    .mb-filter label{font-size:12px;font-weight:700;color:#334155;letter-spacing:.4px;text-transform:uppercase}
    .mb-select{min-width:180px;padding:10px 12px;border:1px solid var(--mb-line);border-radius:10px;background:#fff;font-size:14px;outline:none}
    .mb-select:focus{border-color:var(--mb-primary);box-shadow:0 0 0 4px rgba(59,130,246,.12)}
    .mb-btn{display:inline-flex;align-items:center;gap:8px;padding:10px 14px;border-radius:10px;font-weight:700;
      border:1px solid transparent;background:var(--mb-primary);color:#fff;cursor:pointer;transition:.15s}
    .mb-btn:hover{background:var(--mb-primary-600);transform:translateY(-1px)}

    /* ===== Panel ===== */
    .mb-panel{background:var(--mb-card);border:1px solid var(--mb-line);border-radius:var(--mb-radius);box-shadow:var(--mb-shadow);margin:18px 22px;overflow:hidden}
    .mb-panel-head{padding:16px 18px;border-bottom:1px solid var(--mb-line);display:flex;justify-content:space-between;align-items:center}
    .mb-panel-head h2{margin:0;font-size:18px;font-weight:800}
    .mb-panel-body{padding:0}

    /* ===== Alerts ===== */
    .mb-alert{margin:16px 22px;padding:12px 14px;border-radius:12px;border:1px solid transparent;font-size:14px}
    .mb-alert.success{background:var(--mb-success-50);color:var(--mb-success);border-color:#bbf7d0}
    .mb-alert.danger{background:var(--mb-danger-50);color:var(--mb-danger);border-color:#fecdd3}

    /* ===== Table ===== */
    .mb-table{width:100%;border-collapse:separate;border-spacing:0}
    .mb-table thead th{
      position:sticky;top:0;z-index:1;background:#f8fafc;border-bottom:1px solid var(--mb-line);
      padding:14px;text-align:left;font-size:12px;text-transform:uppercase;letter-spacing:.55px;color:#334155;font-weight:800
    }
    .mb-table td{padding:16px 14px;border-bottom:1px solid var(--mb-line);vertical-align:middle}
    .mb-table tbody tr:hover{background:#fafbff}
    .mb-table tbody tr:nth-child(odd){background:#fff}
    .mb-table tbody tr:nth-child(even){background:#fcfcff}
    .mb-currency{text-align:right;font-variant-numeric:tabular-nums;font-weight:800}

    /* ===== Owner chip (fix cột “Cửa Hàng Xe ABC” phình to) ===== */
    .mb-owner-chip{
      display:inline-flex;align-items:center;gap:.4rem;padding:.38rem .62rem;border-radius:999px;
      font-size:.78rem;font-weight:700;line-height:1.2;white-space:nowrap;height:auto;
      background:#e8f0ff;color:#1d4ed8;
    }
    .mb-owner-chip.admin{background:#ecfdf5;color:#0f766e}
    .mb-owner-chip.unknown{background:#f3f4f6;color:#6b7280}
    /* nếu còn CSS từ nơi khác đè lên, reset cứng */
    .mb-table td:nth-child(5) .mb-owner-chip{all:revert-layer;display:inline-flex!important;align-items:center;gap:.4rem;padding:.38rem .62rem;border-radius:999px;font-size:.78rem;font-weight:700;line-height:1.2;white-space:nowrap;height:auto!important}

    /* ===== Status badges ===== */
    .mb-status{display:inline-block;padding:.38rem .62rem;border-radius:999px;font-size:.78rem;font-weight:800;white-space:nowrap}
    .mb-status.available{background:var(--mb-success-50);color:var(--mb-success)}
    .mb-status.rented{background:var(--mb-primary-50);color:var(--mb-primary-600)}
    .mb-status.maintenance{background:var(--mb-warn-50);color:var(--mb-warn)}

    /* ===== Row actions ===== */
    .mb-actions{display:flex;gap:8px}
    .mb-btn-edit{background:var(--mb-warn);color:#111827;border-radius:10px;padding:8px 12px;font-weight:700}
    .mb-btn-delete{background:var(--mb-danger);color:#fff;border-radius:10px;padding:8px 12px;font-weight:700}
    .mb-btn-edit:hover{filter:brightness(.97)}
    .mb-btn-delete:hover{filter:brightness(.95)}

    /* ===== Responsive ===== */
    @media (max-width:1100px){ .mb-sidebar{width:86px}.mb-brand h1,.mb-nav a span{display:none} .mb-nav a{justify-content:center}}
    @media (max-width:768px){
      .mb-filters{flex-direction:column;align-items:stretch}
      .mb-panel{margin:14px}
      .mb-header{padding:12px 16px}
      .mb-table{display:block;overflow-x:auto}
    }
  </style>
</head>
<body class="mb-admin">
<fmt:setLocale value="vi_VN"/>

<!-- Sidebar -->
<aside class="mb-sidebar">
  <div class="mb-brand">
    <div class="mb-brand-logo"><i class="fas fa-motorcycle"></i></div>
    <h1>RideNow Admin</h1>
  </div>
  <nav class="mb-nav">
    <a href="${pageContext.request.contextPath}/admin/dashboard"><i class="fas fa-tachometer-alt"></i><span>Dashboard</span></a>
    <a href="${pageContext.request.contextPath}/admin/partners"><i class="fas fa-handshake"></i><span>Partners</span></a>
    <a href="${pageContext.request.contextPath}/admin/customers"><i class="fas fa-users"></i><span>Customers</span></a>
    <a class="active" href="${pageContext.request.contextPath}/admin/bikes"><i class="fas fa-motorcycle"></i><span>Motorbikes</span></a>
    <a href="${pageContext.request.contextPath}/admin/orders"><i class="fas fa-clipboard-list"></i><span>Orders</span></a>
  </nav>
</aside>

<!-- Main -->
<main class="mb-content">
  <header class="mb-header">
    <div>
      <h1>Quản lý Xe Máy</h1>
      <div class="mb-breadcrumb">
        <span>Admin</span><i class="fas fa-chevron-right"></i><span class="active">Motorbikes</span>
      </div>
    </div>
    <div class="mb-user">
      <div class="mb-avatar"><i class="fas fa-user"></i></div>
      <span>Administrator</span>
    </div>
  </header>

  <!-- Filters -->
  <div class="mb-filters">
    <div class="mb-filter">
      <label for="filterOwner">Chủ sở hữu</label>
      <select id="filterOwner" class="mb-select">
        <option value="all">Tất cả</option>
        <option value="partner">Đối tác</option>
        <option value="admin">Admin</option>
      </select>
    </div>
    <div class="mb-filter">
      <label for="filterStatus">Trạng thái</label>
      <select id="filterStatus" class="mb-select">
        <option value="all">Tất cả</option>
        <option value="available">Có sẵn</option>
        <option value="rented">Đã thuê</option>
        <option value="maintenance">Bảo trì</option>
      </select>
    </div>
    <button id="btnFilter" class="mb-btn"><i class="fas fa-filter"></i> Lọc</button>
  </div>

  <!-- Alerts -->
  <c:if test="${not empty param.success}">
    <div class="mb-alert success">
      <c:choose>
        <c:when test="${param.success == 'created'}">✅ Thêm xe thành công!</c:when>
        <c:when test="${param.success == 'updated'}">✅ Cập nhật xe thành công!</c:when>
        <c:when test="${param.success == 'deleted'}">✅ Xóa xe thành công!</c:when>
        <c:otherwise>✅ Thao tác thành công!</c:otherwise>
      </c:choose>
    </div>
  </c:if>
  <c:if test="${not empty param.error}">
    <div class="mb-alert danger">
      <c:choose>
        <c:when test="${param.error == 'create_failed'}">❌ Thêm xe thất bại!</c:when>
        <c:when test="${param.error == 'update_failed'}">❌ Cập nhật xe thất bại!</c:when>
        <c:when test="${param.error == 'delete_failed'}">❌ Xóa xe thất bại!</c:when>
        <c:when test="${param.error == 'not_found'}">❌ Không tìm thấy xe!</c:when>
        <c:otherwise>❌ Có lỗi xảy ra!</c:otherwise>
      </c:choose>
    </div>
  </c:if>

  <!-- List -->
  <section class="mb-panel">
    <div class="mb-panel-head">
      <h2>Danh sách Xe Máy</h2>
      <a class="mb-btn" href="${pageContext.request.contextPath}/admin/bikes?action=new"><i class="fas fa-plus"></i> Thêm Xe Mới</a>
    </div>

    <div class="mb-panel-body">
      <table class="mb-table">
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
            <td><strong>${bike.bikeName}</strong></td>
            <td>${bike.licensePlate}</td>
            <td>${bike.typeName}</td>
            <td>
              <c:choose>
                <c:when test="${bike.ownerType == 'Partner' && not empty bike.ownerName}">
                  <span class="mb-owner-chip partner">${bike.ownerName}</span>
                </c:when>
                <c:when test="${bike.ownerType == 'Admin' && not empty bike.ownerName}">
                  <span class="mb-owner-chip admin">${bike.ownerName}</span>
                </c:when>
                <c:otherwise>
                  <span class="mb-owner-chip unknown">—</span>
                </c:otherwise>
              </c:choose>
            </td>
            <td class="mb-currency"><fmt:formatNumber value="${bike.pricePerDay}" type="currency"/></td>
            <td>
              <span class="mb-status ${bike.status}">
                <c:choose>
                  <c:when test="${bike.status == 'available'}">Có sẵn</c:when>
                  <c:when test="${bike.status == 'rented'}">Đã thuê</c:when>
                  <c:when test="${bike.status == 'maintenance'}">Bảo trì</c:when>
                  <c:otherwise>${bike.status}</c:otherwise>
                </c:choose>
              </span>
            </td>
            <td>
              <div class="mb-actions">
                <a class="mb-btn-edit" href="${pageContext.request.contextPath}/admin/bikes?action=edit&id=${bike.bikeId}"><i class="fas fa-edit"></i> Sửa</a>
                <a class="mb-btn-delete" href="${pageContext.request.contextPath}/admin/bikes?action=delete&id=${bike.bikeId}"
                   onclick="return confirm('Bạn có chắc chắn muốn xóa xe ${bike.bikeName} (${bike.licensePlate})?')"><i class="fas fa-trash"></i> Xóa</a>
              </div>
            </td>
          </tr>
        </c:forEach>

        <c:if test="${empty motorbikes}">
          <tr>
            <td colspan="8" style="text-align:center;padding:48px;color:#64748b">
              <i class="fas fa-motorcycle" style="font-size:42px;margin-bottom:10px;display:block;color:#cbd5e1;"></i>
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
  document.getElementById('btnFilter').addEventListener('click', function () {
    const owner = document.getElementById('filterOwner').value;
    const status = document.getElementById('filterStatus').value;
    let url = '${pageContext.request.contextPath}/admin/bikes?action=filter';
    if (owner !== 'all') url += '&ownerType=' + owner;
    if (status !== 'all') url += '&status=' + status;
    window.location.href = url;
  });

  // Khôi phục filter
  document.addEventListener('DOMContentLoaded', function () {
    const q = new URLSearchParams(window.location.search);
    if (q.get('ownerType')) document.getElementById('filterOwner').value = q.get('ownerType');
    if (q.get('status')) document.getElementById('filterStatus').value = q.get('status');
  });
</script>
</body>
</html>
