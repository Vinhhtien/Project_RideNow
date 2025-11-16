<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt"  prefix="fmt" %>
<%
  response.setCharacterEncoding("UTF-8");
%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="vi_VN"/>
<fmt:requestEncoding value="UTF-8"/>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Báo cáo • RideNow Admin</title>

  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <link rel="stylesheet" href="${ctx}/css/admin.css"/>

  <style>
    :root{
      --sidebar-width:260px;
      --font-family-sans:Inter,system-ui,Arial,sans-serif;
      --color-text-primary:#0f172a; --color-text-muted:#64748b; --color-text-muted-dark:#6b7280; --color-text-header:#1f2937; --color-text-link:#1f6feb;
      --color-bg-body:#f8fafc; --color-bg-content:#fff; --color-bg-header:#f8fafc; --color-bg-hover:#f1f5f9; --color-bg-hover-light:#f9fafb;
      --color-border:#e5e7eb; --color-border-light:#eef2f7; --color-border-input:#d1d5db;
      --color-primary:#2563eb; --color-primary-light:#3b82f6; --color-primary-text:#fff; --color-primary-bg:#eef2ff; --color-primary-bg-hover:#e0e7ff;
      --color-primary-shadow:rgba(37,99,235,.25); --color-primary-shadow-hover:rgba(37,99,235,.18);
      --color-sidebar-bg:#0b0f1f; --color-sidebar-text:#e8eaf6;
      --color-status-paid-bg:#dcfce7; --color-status-paid-text:#166534;
      --color-status-refunded-bg:#ffe4e6; --color-status-refunded-text:#9f1239;
      --color-status-pending-bg:#fef9c3; --color-status-pending-text:#854d0e;
      --color-status-due-bg:#fee2e2; --color-status-due-text:#b91c1c;
      --radius-sm:10px; --radius-md:12px; --radius-lg:14px; --radius-full:9999px;
      --shadow-sm:0 1px 2px rgba(16,24,40,.04); --shadow-sm-alt:0 1px 2px rgba(0,0,0,.03);
      --transition-fast:all .12s ease;
    }
    body.admin{font-family:var(--font-family-sans);color:var(--color-text-primary);background:var(--color-bg-body);}
    .sidebar{position:fixed;left:0;top:0;bottom:0;width:var(--sidebar-width);background:linear-gradient(180deg,#0b0f1f 0%,#141a36 60%,#1b234a 100%);color:var(--color-sidebar-text);}
    .content.report{margin-left:var(--sidebar-width);padding:24px;}

    .content.report .content-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;border-bottom:1px solid var(--color-border-light);padding-bottom:8px;}
    .content.report .breadcrumb{display:flex;gap:8px;align-items:center;color:var(--color-text-muted-dark);}
    .content.report .breadcrumb .active{color:var(--color-text-primary);}
    .content.report .icon-btn{display:flex;align-items:center;justify-content:center;width:40px;height:40px;border-radius:var(--radius-sm);background:var(--color-primary-bg);color:var(--color-primary);border:1px solid var(--color-border);box-shadow:var(--shadow-sm);}
    .content.report .icon-btn:hover{transform:translateY(-1px);box-shadow:0 6px 16px var(--color-primary-shadow-hover);}

    .content.report .panel{background:var(--color-bg-content);border:1px solid var(--color-border);border-radius:var(--radius-md);margin:16px 0;box-shadow:var(--shadow-sm);}
    .content.report .panel-header{padding:16px 20px;border-bottom:1px solid var(--color-border);display:flex;justify-content:space-between;align-items:center;}
    .content.report .panel-body{padding:16px 20px;}

    .content.report .tabs{display:flex;gap:10px;flex-wrap:wrap;}
    .content.report .tab{display:inline-flex;align-items:center;justify-content:center;height:40px;padding:0 14px;border-radius:var(--radius-md);font-weight:600;font-size:14px;background:#fff;border:1px solid var(--color-border);color:var(--color-text-primary);transition:transform .14s ease,box-shadow .14s ease,background .3s ease,border-color .3s ease;}
    .content.report .tab:hover:not(.active){background:var(--color-bg-hover-light);border-color:#cbd5e1;transform:translateY(-2px);}
    .content.report .tab.active{background-image:linear-gradient(135deg,#ff8a00,#ff3d81,#7c4dff,#1fa2ff);background-size:200% 200%;background-position:0% 50%;color:#fff;border:none;box-shadow:0 10px 22px rgba(37,99,235,.22);}

    .content.report .btn{height:40px;padding:0 12px;border:1px solid var(--color-border-input);border-radius:var(--radius-sm);background:#fff;text-decoration:none;color:var(--color-text-primary);font-size:14px;font-weight:600;display:inline-flex;align-items:center;justify-content:center;transition:transform .14s ease,box-shadow .14s ease,background .3s ease;}
    .content.report .btn.primary{border-color:var(--color-primary);background:linear-gradient(180deg,var(--color-primary-light) 0%,var(--color-primary) 100%);color:#fff;}
    .content.report .btn:hover{transform:translateY(-2px) scale(1.02);box-shadow:0 10px 18px var(--color-primary-shadow-hover);}
    .content.report .btn:active{transform:scale(.98);}
    .pressing{transform:scale(.98)!important;}

    .content.report .filters{display:flex;gap:12px;flex-wrap:wrap;align-items:flex-end;}
    .content.report .filters label{font-size:14px;color:#334155;font-weight:600;}
    .content.report .filters input[type="date"], .content.report .filters input[type="number"], .content.report .filters select{height:40px;padding:0 12px;font-size:14px;border:1px solid var(--color-border-input);border-radius:var(--radius-sm);background:#fff;}
    .content.report .filters .filter-actions{margin-left:auto;display:flex;gap:12px;flex-wrap:wrap;}

    .content.report .grid4{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:14px;}
    .content.report .card{
      position:relative;border:1px solid transparent;
      background-image:linear-gradient(#fff,#fff), linear-gradient(135deg,#60a5fa,#a78bfa,#f472b6,#f59e0b);
      background-origin:border-box; background-clip:padding-box, border-box;
      border-radius:var(--radius-lg); padding:14px; box-shadow:var(--shadow-sm-alt);
      transition:transform .18s ease, box-shadow .18s ease, background-position .3s ease;
      background-size:auto, 200% 200%; background-position:center, 0% 50%;
    }
    .content.report .card:hover{transform:translateY(-3px);box-shadow:0 14px 28px rgba(31,98,235,.15);background-position:center,100% 50%;}
    .content.report .card .muted{color:var(--color-text-muted);font-size:12px;margin-bottom:6px;}

    .content.report .data-table{width:100%;border-collapse:collapse;}
    .content.report .data-table thead th{background:var(--color-bg-header);color:var(--color-text-header);font-weight:700;font-size:14px;border-bottom:1px solid var(--color-border);padding:12px 10px;text-align:left;white-space:nowrap;}
    .content.report .data-table tbody td{font-size:14px;padding:10px;border-bottom:1px solid var(--color-border-light);transition:background-color .18s ease;}
    .content.report .data-table tbody tr:hover{background:linear-gradient(90deg,rgba(37,99,235,.06),rgba(37,99,235,0));}
    .content.report a{text-decoration:none!important;}
    .content.report a:hover{opacity:.92;}

    .content.report .header-right{display:flex;align-items:center;gap:12px;}
    .content.report .header-right .icon-btn:first-child{order:0;position:relative;}
    .content.report .header-right .user-profile{order:1;display:flex;align-items:center;gap:8px;}

    .badge{padding:4px 10px;border-radius:var(--radius-full);font-weight:700;font-size:12px;letter-spacing:.2px;display:inline-block;}
    .status-paid{background:var(--color-status-paid-bg);color:var(--color-status-paid-text);}
    .status-refunded{background:var(--color-status-refunded-bg);color:var(--color-status-refunded-text);}
    .status-pending{background:var(--color-status-pending-bg);color:var(--color-status-pending-text);}
    .status-due{background:var(--color-status-due-bg);color:var(--color-status-due-text);}

    .chip{display:inline-block;padding:4px 10px;border:1px solid var(--color-border);border-radius:var(--radius-full);background:#fff;box-shadow:var(--shadow-sm);}
    .chip.soft{background:#f9fafb;border-color:#e5e7eb;}
    .chip-link{display:inline-block;padding:4px 10px;border:1px solid var(--color-border);border-radius:var(--radius-full);background:#fff;color:#1f6feb;box-shadow:var(--shadow-sm);}
    .chip-link:hover{box-shadow:0 8px 18px rgba(37,99,235,.15);transform:translateY(-1px);}
    .btn-chip{display:inline-block;padding:6px 12px;border-radius:var(--radius-full);font-weight:700;border:1px solid #dbeafe;background:linear-gradient(135deg,#eff6ff,#e0e7ff);color:#1e40af;box-shadow:0 6px 14px rgba(37,99,235,.15);}

    .payments-table td:nth-child(3) a{
      display:inline-block;padding:4px 10px;border-radius:var(--radius-full);
      border:1px solid #e0e7ff;background:linear-gradient(135deg,#eef2ff,#e0f2fe);
      color:#1f6feb;box-shadow:0 6px 14px rgba(37,99,235,.12);
    }
    .payments-table td:last-child a{
      display:inline-block;padding:6px 12px;border-radius:var(--radius-full);
      border:1px solid #dbeafe;background:linear-gradient(135deg,#eff6ff,#e0e7ff);
      color:#1e40af;font-weight:700;box-shadow:0 6px 14px rgba(37,99,235,.15);
    }
    .payments-table tbody tr:hover td:last-child a{box-shadow:0 10px 20px rgba(37,99,235,.22);}

    .refunds-table .chip.cash{
      background:linear-gradient(135deg,#34d399,#10b981);color:#062e23;border-color:rgba(16,185,129,.25);box-shadow:0 6px 14px rgba(16,185,129,.20);
    }
    .refunds-table .chip.wallet{
      background:linear-gradient(135deg,#60a5fa,#2563eb);color:#fff;border-color:rgba(37,99,235,.25);box-shadow:0 6px 14px rgba(37,99,235,.22);
    }
    .refunds-table td:nth-child(3) .chip-link{
      background:linear-gradient(135deg,#eef2ff,#e0f2fe);color:#1f6feb;border-color:#e0e7ff;box-shadow:0 6px 14px rgba(37,99,235,.12);
    }
    .refunds-table td:last-child .chip-link{
      background:linear-gradient(135deg,#f0f9ff,#ecfeff);color:#0c4a6e;border-color:#bae6fd;font-weight:700;box-shadow:0 6px 14px rgba(14,165,233,.18);
    }
    .refunds-table tbody tr:hover .chip.cash{box-shadow:0 10px 20px rgba(16,185,129,.28);}
    .refunds-table tbody tr:hover .chip.wallet{box-shadow:0 10px 20px rgba(37,99,235,.28);}

    .content.report .pager{margin-top:14px;display:flex;gap:12px;align-items:center;}
    .content.report .pager .btn{background:linear-gradient(180deg,#ffffff,#f5f8ff);}
    .content.report .pager .btn:hover{background:linear-gradient(180deg,#fff9f0,#eaf2ff);}

    .content.report .panel-header h2, .content.report .panel-header h3{
      background:linear-gradient(90deg,#1fa2ff,#12d8fa,#a6ffcb);-webkit-background-clip:text;background-clip:text;color:transparent;
    }

    .sidebar .nav-item{transition:background .2s ease,transform .2s ease;}
    .sidebar .nav-item:hover{background:rgba(255,255,255,.06);transform:translateX(4px);}

    .content.report .data-table .num{ text-align:right; }
  </style>
</head>

<body class="admin">
  <aside class="sidebar">
    <div class="brand">
      <div class="brand-logo"><i class="fas fa-motorcycle"></i></div>
      <h1>RideNow Admin</h1>
    </div>
    <nav class="sidebar-nav">
      <a href="${ctx}/admin/dashboard" class="nav-item"><i class="fas fa-tachometer-alt"></i><span>Dashboard</span></a>
      <a href="${ctx}/admin/partners"  class="nav-item"><i class="fas fa-handshake"></i><span>Partners</span></a>
      <a href="${ctx}/admin/customers" class="nav-item"><i class="fas fa-users"></i><span>Customers</span></a>
      <a href="${ctx}/admin/bikes"     class="nav-item"><i class="fas fa-motorcycle"></i><span>Motorbikes</span></a>
      <a href="${ctx}/admin/orders"    class="nav-item"><i class="fas fa-clipboard-list"></i><span>Orders</span></a>
      <a href="${pageContext.request.contextPath}/admin/schedule" class="nav-item">
        <i class="fas fa-calendar-alt"></i><span>View Schedule</span>
      </a>
      <!-- FIXED ICON HERE -->
      <a href="${ctx}/adminpickup"     class="nav-item"><i class="fas fa-shipping-fast"></i><span>Vehicle Pickup</span></a>
      <a href="${ctx}/adminreturn"     class="nav-item"><i class="fas fa-undo-alt"></i><span>Vehicle Return</span></a>
      <a href="${ctx}/adminreturns"    class="nav-item"><i class="fas fa-clipboard-check"></i><span>Verify & Refund</span></a>
      <a href="${ctx}/admin/reports"   class="nav-item active"><i class="fas fa-chart-bar"></i><span>Reports</span></a>
      <a href="${ctx}/admin/feedback"  class="nav-item"><i class="fas fa-comment-alt"></i><span>Feedback</span></a>
      <a href="${ctx}/logout"          class="nav-item logout"><i class="fas fa-sign-out-alt"></i><span>Logout</span></a>
    </nav>
  </aside>

  <main class="content report">
    <header class="content-header">
      <div class="header-left">
        <h1>Báo cáo</h1>
        <div class="breadcrumb"><span>Admin</span><i class="fas fa-chevron-right"></i><span class="active">Báo cáo</span></div>
      </div>
      <div class="header-right">
        <a class="icon-btn" href="${ctx}/admin/notify" title="Gửi thông báo"><i class="fas fa-paper-plane"></i></a>
        <div class="user-profile"><div class="user-avatar"><i class="fas fa-user-circle"></i></div><span>Administrator</span></div>
      </div>
    </header>

    <section class="panel">
      <div class="panel-header">
        <h2>Bộ lọc</h2>
        <div class="tabs">
          <a class="tab ${view=='overview'?'active':''}"      href="${ctx}/admin/reports?view=overview&from=${from}&to=${to}">Tổng quan</a>
          <a class="tab ${view=='overview-net'?'active':''}" href="${ctx}/admin/reports?view=overview-net&from=${from}&to=${to}">Tổng thu ròng</a>
          <a class="tab ${view=='payments'?'active':''}"      href="${ctx}/admin/reports?view=payments&from=${from}&to=${to}&size=${size}">Thanh toán</a>
          <a class="tab ${view=='refunds'?'active':''}"       href="${ctx}/admin/reports?view=refunds&from=${from}&to=${to}&size=${size}">Hoàn tiền</a>
          <a class="tab ${view=='stores'?'active':''}"        href="${ctx}/admin/reports?view=stores&from=${from}&to=${to}">Cửa hàng</a>
          <a class="tab ${view=='top-customers'?'active':''}" href="${ctx}/admin/reports?view=top-customers&from=${from}&to=${to}&limit=${limit}">Khách hàng nổi bật</a>
        </div>
      </div>
      <div class="panel-body">
        <form method="get" class="filters">
          <input type="hidden" name="view" value="${view}"/>
          <label>Từ ngày<br><input type="date" name="from" value="${from}"/></label>
          <label>Đến ngày<br><input type="date" name="to" value="${to}"/></label>

          <c:if test="${view == 'payments' || view == 'refunds'}">
            <label>Kích thước trang<br>
              <select name="size">
                <option ${size==10?'selected':''}>10</option>
                <option ${size==20?'selected':''}>20</option>
                <option ${size==50?'selected':''}>50</option>
                <option ${size==100?'selected':''}>100</option>
                <option ${size==200?'selected':''}>200</option>
              </select>
            </label>
          </c:if>

          <c:if test="${view == 'top-customers'}">
            <label>Top N<br>
              <select name="limit">
                <option ${limit==5?'selected':''}>5</option>
                <option ${limit==10?'selected':''}>10</option>
                <option ${limit==20?'selected':''}>20</option>
                <option ${limit==50?'selected':''}>50</option>
                <option ${limit==100?'selected':''}>100</option>
              </select>
            </label>
          </c:if>

          <c:if test="${view == 'stores'}">
            <label>Partner ID (tuỳ chọn)<br>
              <input type="number" name="partnerId" value="${partnerId}" placeholder="Để trống = tất cả"/>
            </label>
          </c:if>

          <button type="submit" class="btn primary">Lọc</button>

          <div class="filter-actions">
            <c:choose>
              <c:when test="${view=='overview'}">
                <a class="btn" href="${ctx}/admin/reports?action=export&type=methods&from=${from}&to=${to}">Xuất CSV Phương thức</a>
                <a class="btn" href="${ctx}/admin/reports?action=export&type=daily&from=${from}&to=${to}">Xuất CSV Theo tháng</a>
                <a class="btn" href="${ctx}/admin/reports?action=export&type=overview&from=${from}&to=${to}">Xuất CSV Tổng quan</a>
              </c:when>
              <c:when test="${view=='payments'}">
                <a class="btn" href="${ctx}/admin/reports?action=export&type=payments&from=${from}&to=${to}&page=${page}&size=${size}">Xuất CSV Thanh toán</a>
              </c:when>
              <c:when test="${view=='refunds'}">
                <a class="btn" href="${ctx}/admin/reports?action=export&type=refunds&from=${from}&to=${to}&page=${page}&size=${size}">Xuất CSV Hoàn tiền</a>
              </c:when>
              <c:when test="${view=='stores'}">
                <a class="btn" href="${ctx}/admin/reports?action=export&type=stores&from=${from}&to=${to}&partnerId=${partnerId}">Xuất CSV Cửa hàng</a>
              </c:when>
              <c:when test="${view=='top-customers'}">
                <a class="btn" href="${ctx}/admin/reports?action=export&type=top-customers&from=${from}&to=${to}&limit=${limit}">Xuất CSV Khách hàng nổi bật</a>
              </c:when>
            </c:choose>
          </div>
        </form>
      </div>
    </section>

    <c:choose>
      <c:when test="${view=='overview'}">
        <section class="panel">
          <div class="panel-header"><h2>Tổng quan</h2></div>
          <div class="panel-body">
            <div class="grid4">
              <div class="card">
                <div class="muted">Tổng thu</div>
                <h3><fmt:formatNumber value="${totalCollected}" type="number" groupingUsed="true"/></h3>
              </div>
              <div class="card">
                <div class="muted">Hoàn tiền</div>
                <h3><fmt:formatNumber value="${totalRefunded}" type="number" groupingUsed="true"/></h3>
              </div>
              <div class="card">
                <div class="muted">Doanh thu ròng</div>
                <h3><fmt:formatNumber value="${netRevenue}" type="number" groupingUsed="true"/></h3>
              </div>
            </div>

            <h3 style="margin-top:16px">Phân bổ theo phương thức</h3>
            <table class="data-table">
              <thead>
                <tr><th>Phương thức</th><th class="num">Thu</th><th class="num">Hoàn</th><th class="num">Ròng</th></tr>
              </thead>
              <tbody>
                <c:forEach var="m" items="${methodStats}">
                  <tr>
                    <td>${m.method}</td>
                    <td class="num"><fmt:formatNumber value="${m.paidAmount}" type="number" groupingUsed="true"/></td>
                    <td class="num"><fmt:formatNumber value="${m.refundedAmount}" type="number" groupingUsed="true"/></td>
                    <td class="num"><fmt:formatNumber value="${m.paidAmount - m.refundedAmount}" type="number" groupingUsed="true"/></td>
                  </tr>
                </c:forEach>
                <c:if test="${empty methodStats}">
                  <tr><td colspan="4">Không có dữ liệu.</td></tr>
                </c:if>
              </tbody>
            </table>

            <h3 style="margin-top:16px">Theo tháng</h3>
            <table class="data-table">
              <thead>
                <tr><th>Tháng</th><th class="num">Thu</th><th class="num">Hoàn</th><th class="num">Ròng</th></tr>
              </thead>
              <tbody>
                <c:forEach var="d" items="${daily}">
                  <tr>
                    <td><fmt:formatDate value="${d.day}" pattern="yyyy-MM"/></td>
                    <td class="num"><fmt:formatNumber value="${d.paidAmount}" type="number" groupingUsed="true"/></td>
                    <td class="num"><fmt:formatNumber value="${d.refundedAmount}" type="number" groupingUsed="true"/></td>
                    <td class="num"><fmt:formatNumber value="${d.paidAmount - d.refundedAmount}" type="number" groupingUsed="true"/></td>
                  </tr>
                </c:forEach>
                <c:if test="${empty daily}">
                  <tr><td colspan="4">Không có dữ liệu.</td></tr>
                </c:if>
              </tbody>
            </table>
          </div>
        </section>
      </c:when>

      <c:when test="${view=='overview-net'}">
        <section class="panel">
          <div class="panel-header"><h2>Tổng thu ròng</h2></div>
          <div class="panel-body">
            <div class="grid4">
              <div class="card"><div class="muted">Tổng thu</div><h3><fmt:formatNumber value="${totalCollected}" type="number" groupingUsed="true"/></h3></div>
              <div class="card"><div class="muted">Hoàn tiền</div><h3><fmt:formatNumber value="${totalRefunded}" type="number" groupingUsed="true"/></h3></div>
              <div class="card"><div class="muted">Doanh thu ròng</div><h3><fmt:formatNumber value="${netRevenue}" type="number" groupingUsed="true"/></h3></div>
            </div>

            <h3 style="margin-top:16px">Theo ngày</h3>
            <table class="data-table">
              <thead>
                <tr><th>Ngày</th><th class="num">Thu</th><th class="num">Hoàn</th><th class="num">Ròng</th></tr>
              </thead>
              <tbody>
                <c:forEach var="r" items="${dailyNet}">
                  <tr>
                    <td><fmt:formatDate value="${r.paymentDate}" pattern="yyyy-MM-dd"/></td>
                    <td class="num"><fmt:formatNumber value="${r.totalPaid}" type="number" groupingUsed="true"/></td>
                    <td class="num"><fmt:formatNumber value="${r.refundedAmount}" type="number" groupingUsed="true"/></td>
                    <td class="num"><fmt:formatNumber value="${r.netRevenue}" type="number" groupingUsed="true"/></td>
                  </tr>
                </c:forEach>
                <c:if test="${empty dailyNet}">
                  <tr><td colspan="4">Không có dữ liệu.</td></tr>
                </c:if>
              </tbody>
            </table>

            <h3 style="margin-top:16px">Drill-down theo đơn</h3>
            <table class="data-table">
              <thead>
                <tr>
                  <th>Order</th>
                  <th>Khách</th>
                  <th class="num">Giá thuê</th>
                  <th class="num">Cọc</th>
                  <th class="num">Đã thu</th>
                  <th class="num">Đã hoàn</th>
                  <th class="num">Ròng</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="o" items="${ordersNet}">
                  <c:set var="tp"  value="${o.totalPrice}"/>
                  <c:set var="dep" value="${o.depositAmount}"/>
                  <c:set var="paid" value="${o.paidAmount}"/>
                  <c:set var="ref"  value="${o.refundedAmount}"/>
                  <c:set var="net"  value="${paid - ref}"/>

                  <tr>
                    <td><a class="chip-link" href="${ctx}/admin/reports?view=order-detail&id=${o.orderId}">#${o.orderId}</a></td>
                    <td>${o.customerName}</td>
                    <td class="num"><fmt:formatNumber value="${tp}"  type="number" groupingUsed="true"/></td>
                    <td class="num"><fmt:formatNumber value="${dep}" type="number" groupingUsed="true"/></td>
                    <td class="num"><fmt:formatNumber value="${paid}" type="number" groupingUsed="true"/></td>
                    <td class="num"><fmt:formatNumber value="${ref}"  type="number" groupingUsed="true"/></td>
                    <td class="num"><fmt:formatNumber value="${net}"  type="number" groupingUsed="true"/></td>
                  </tr>
                </c:forEach>
                <c:if test="${empty ordersNet}">
                  <tr><td colspan="7">Không có dữ liệu.</td></tr>
                </c:if>
              </tbody>
            </table>

          </div>
        </section>
      </c:when>

      <c:when test="${view=='payments'}">
        <section class="panel">
          <div class="panel-header"><h2>Thanh toán</h2></div>
          <div class="panel-body">
            <table class="data-table payments-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Khách</th>
                  <th>Order</th>
                  <th class="num">Số tiền</th>
                  <th>Phương thức</th>
                  <th>Trạng thái</th>
                  <th>Thanh toán</th>
                  <th>Xác minh</th>
                  <th>Ref</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="p" items="${items}">
                  <tr>
                    <td>${p.paymentId}</td>
                    <td>${p.customerName}</td>
                    <td><a href="${ctx}/admin/reports?view=order-detail&id=${p.orderId}">#${p.orderId}</a></td>
                    <td class="num"><fmt:formatNumber value="${p.amount}" type="number" groupingUsed="true"/></td>
                    <td>${p.method}</td>
                    <td>
                      <span class="badge ${p.status=='paid'?'status-paid':(p.status=='refunded'?'status-refunded':'status-pending')}">
                        ${p.status}
                      </span>
                    </td>
                    <td><fmt:formatDate value="${p.paymentDate}" pattern="yyyy-MM-dd HH:mm"/></td>
                    <td><fmt:formatDate value="${p.verifiedAt}" pattern="yyyy-MM-dd HH:mm"/></td>
                    <td>${p.reference}</td>
                  </tr>
                </c:forEach>
                <c:if test="${empty items}">
                  <tr><td colspan="9">Không có dữ liệu.</td></tr>
                </c:if>
              </tbody>
            </table>

            <c:if test="${totalPages > 1}">
              <div class="pager">
                <span>Trang ${page} / ${totalPages}</span>
                <c:if test="${page > 1}">
                  <a class="btn" href="${ctx}/admin/reports?view=payments&from=${from}&to=${to}&size=${size}&page=${page-1}">« Trước</a>
                </c:if>
                <c:if test="${page < totalPages}">
                  <a class="btn" href="${ctx}/admin/reports?view=payments&from=${from}&to=${to}&size=${size}&page=${page+1}">Tiếp »</a>
                </c:if>
              </div>
            </c:if>
          </div>
        </section>
      </c:when>

      <c:when test="${view=='refunds'}">
        <section class="panel">
          <div class="panel-header"><h2>Hoàn tiền</h2></div>
          <div class="panel-body">
            <table class="data-table refunds-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Khách</th>
                  <th>Order</th>
                  <th class="num">Số tiền</th>
                  <th>Inspected</th>
                  <th>Phương thức</th>
                  <th>Verified</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="r" items="${refunds}">
                  <tr>
                    <td>${r.paymentId}</td>
                    <td>${r.customerName}</td>
                    <td><a class="chip-link" href="${ctx}/admin/reports?view=order-detail&id=${r.orderId}">#${r.orderId}</a></td>
                    <td class="num"><fmt:formatNumber value="${r.amount}" type="number" groupingUsed="true"/></td>
                    <td><fmt:formatDate value="${r.paymentDate}" pattern="yyyy-MM-dd HH:mm"/></td>
                    <td><span class="chip">${r.method}</span></td>
                    <td><fmt:formatDate value="${r.verifiedAt}" pattern="yyyy-MM-dd HH:mm"/></td>
                  </tr>
                </c:forEach>
                <c:if test="${empty refunds}">
                  <tr><td colspan="7">Không có dữ liệu.</td></tr>
                </c:if>
              </tbody>
            </table>

            <c:if test="${totalPages > 1}">
              <div class="pager">
                <span>Trang ${page} / ${totalPages}</span>
                <c:if test="${page > 1}">
                  <a class="btn" href="${ctx}/admin/reports?view=refunds&from=${from}&to=${to}&size=${size}&page=${page-1}">« Trước</a>
                </c:if>
                <c:if test="${page < totalPages}">
                  <a class="btn" href="${ctx}/admin/reports?view=refunds&from=${from}&to=${to}&size=${size}&page=${page+1}">Tiếp »</a>
                </c:if>
              </div>
            </c:if>
          </div>
        </section>
      </c:when>

      <c:when test="${view=='stores'}">
        <section class="panel">
          <div class="panel-header"><h2>Doanh thu theo cửa hàng</h2></div>
          <div class="panel-body">

            <c:set var="platformTotal" value="${platformNetTotal}"/>
            <c:if test="${empty platformTotal}">
              <c:set var="sumStoreNet100" value="0"/>
              <c:set var="sumPartnerAdmin60" value="0"/>
              <c:forEach var="x" items="${storeRevenue}">
                <c:set var="netx" value="${empty x.netRevenue ? (x.totalPaid - x.refundedAmount) : x.netRevenue}"/>
                <c:choose>
                  <c:when test="${empty x.partnerId}">
                    <c:set var="sumStoreNet100" value="${sumStoreNet100 + netx}"/>
                  </c:when>
                  <c:otherwise>
                    <c:set var="admin60x" value="${not empty x.adminShare60 ? x.adminShare60 : netx * 0.6}"/>
                    <c:set var="sumPartnerAdmin60" value="${sumPartnerAdmin60 + admin60x}"/>
                  </c:otherwise>
                </c:choose>
              </c:forEach>
              <c:set var="platformTotal" value="${sumStoreNet100 + sumPartnerAdmin60}"/>
            </c:if>

            <table class="data-table">
              <thead>
                <tr>
                  <th>STT</th>
                  <th>NAME</th>
                  <th class="num">Số đơn</th>
                  <th class="num">Doanh thu gộp</th>
                  <th class="num">Hoàn tiền</th>
                  <th class="num">Doanh thu ròng</th>
                  <th class="num">TOTAL</th>
                </tr>
              </thead>
              <tbody>
                <c:set var="stt" value="1"/>
                <c:set var="renderedStore" value="0"/>

                <c:forEach var="it" items="${storeRevenue}">
                  <c:if test="${empty it.partnerId}">
                    <c:set var="net" value="${empty it.netRevenue ? (it.totalPaid - it.refundedAmount) : it.netRevenue}"/>
                    <c:set var="rowTotal" value="${platformTotal}"/>

                    <tr>
                      <td class="num">${stt}</td>
                      <td>${empty it.storeName ? 'Cửa hàng' : it.storeName}</td>
                      <td class="num">${it.orderCount}</td>
                      <td class="num"><fmt:formatNumber value="${it.totalPaid}" type="number" groupingUsed="true"/></td>
                      <td class="num"><fmt:formatNumber value="${it.refundedAmount}" type="number" groupingUsed="true"/></td>
                      <td class="num"><fmt:formatNumber value="${net}" type="number" groupingUsed="true"/></td>
                      <td class="num"><fmt:formatNumber value="${rowTotal}" type="number" groupingUsed="true"/></td>
                    </tr>

                    <c:set var="renderedStore" value="1"/>
                    <c:set var="stt" value="${stt + 1}"/>
                  </c:if>
                </c:forEach>

                <c:if test="${renderedStore == 0}">
                  <c:set var="storeOrderCount" value="0"/>
                  <c:set var="storeTotalPaid"  value="0"/>
                  <c:set var="storeRefunded"   value="0"/>

                  <c:forEach var="x" items="${storeRevenue}">
                    <c:if test="${empty x.partnerId}">
                      <c:set var="storeOrderCount" value="${storeOrderCount + (empty x.orderCount ? 0 : x.orderCount)}"/>
                      <c:set var="storeTotalPaid"  value="${storeTotalPaid  + (empty x.totalPaid ? 0 : x.totalPaid)}"/>
                      <c:set var="storeRefunded"   value="${storeRefunded   + (empty x.refundedAmount ? 0 : x.refundedAmount)}"/>
                    </c:if>
                  </c:forEach>

                  <c:set var="storeNet" value="${storeTotalPaid - storeRefunded}"/>

                  <tr>
                    <td class="num">${stt}</td>
                    <td>Cửa hàng</td>
                    <td class="num"><fmt:formatNumber value="${storeOrderCount}" type="number" groupingUsed="true"/></td>
                    <td class="num"><fmt:formatNumber value="${storeTotalPaid}"  type="number" groupingUsed="true"/></td>
                    <td class="num"><fmt:formatNumber value="${storeRefunded}"   type="number" groupingUsed="true"/></td>
                    <td class="num"><fmt:formatNumber value="${storeNet}"        type="number" groupingUsed="true"/></td>
                    <td class="num"><fmt:formatNumber value="${platformTotal}"   type="number" groupingUsed="true"/></td>
                  </tr>

                  <c:set var="stt" value="${stt + 1}"/>
                </c:if>

                <c:forEach var="it" items="${storeRevenue}">
                  <c:if test="${not empty it.partnerId}">
                    <c:set var="net" value="${empty it.netRevenue ? (it.totalPaid - it.refundedAmount) : it.netRevenue}"/>
                    <c:set var="rowTotal" value="${not empty it.partnerShare40 ? it.partnerShare40 : net * 0.4}"/>

                    <tr>
                      <td class="num">${stt}</td>
                      <td>${it.partnerName}</td>
                      <td class="num">${it.orderCount}</td>
                      <td class="num"><fmt:formatNumber value="${it.totalPaid}" type="number" groupingUsed="true"/></td>
                      <td class="num"><fmt:formatNumber value="${it.refundedAmount}" type="number" groupingUsed="true"/></td>
                      <td class="num"><fmt:formatNumber value="${net}" type="number" groupingUsed="true"/></td>
                      <td class="num"><fmt:formatNumber value="${rowTotal}" type="number" groupingUsed="true"/></td>
                    </tr>

                    <c:set var="stt" value="${stt + 1}"/>
                  </c:if>
                </c:forEach>

                <c:if test="${empty storeRevenue}">
                  <tr><td colspan="7">Không có dữ liệu trong khoảng thời gian đã chọn.</td></tr>
                </c:if>
              </tbody>
            </table>
          </div>
        </section>
      </c:when>

      <c:when test="${view=='top-customers'}">
        <section class="panel">
          <div class="panel-header"><h2>Khách hàng nổi bật</h2></div>
          <div class="panel-body">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Hạng</th>
                  <th>Khách hàng</th>
                  <th class="num">Tổng đã thanh toán</th>
                </tr>
              </thead>
              <tbody>
                <c:set var="rank" value="1"/>
                <c:forEach var="tc" items="${topCustomers}">
                  <tr>
                    <td class="num">${rank}</td>
                    <td>${tc.customerName}</td>
                    <td class="num"><fmt:formatNumber value="${tc.totalPaid}" type="number" groupingUsed="true"/></td>
                  </tr>
                  <c:set var="rank" value="${rank + 1}"/>
                </c:forEach>
                <c:if test="${empty topCustomers}">
                  <tr><td colspan="3">Không có dữ liệu.</td></tr>
                </c:if>
              </tbody>
            </table>
          </div>
        </section>
      </c:when>

      <c:when test="${view=='payment-detail'}">
        <%-- giữ nguyên --%>
      </c:when>

      <c:when test="${view=='order-detail'}">
        <%-- giữ nguyên --%>
      </c:when>
    </c:choose>
  </main>

  <script>
  document.addEventListener('DOMContentLoaded', () => {
    const clickables = document.querySelectorAll('.content.report .btn, .content.report .icon-btn, .content.report .tab');
    clickables.forEach(el => {
      el.style.position = 'relative'; el.style.overflow = 'hidden';
      el.addEventListener('click', e => {
        const rect = el.getBoundingClientRect(); const d = Math.max(rect.width, rect.height);
        const x = e.clientX - rect.left - d/2; const y = e.clientY - rect.top - d/2;
        const span = document.createElement('span');
        span.style.position='absolute'; span.style.left=x+'px'; span.style.top=y+'px';
        span.style.width=span.style.height=d+'px'; span.style.borderRadius='50%';
        span.style.background='radial-gradient(circle, rgba(37,99,235,.35) 0%, rgba(37,99,235,0) 60%)';
        span.style.transform='scale(0)'; span.style.opacity='.35';
        span.style.transition='transform .5s ease-out, opacity .5s ease-out';
        el.appendChild(span);
        requestAnimationFrame(()=>{span.style.transform='scale(3)'; span.style.opacity='0';});
        setTimeout(()=>span.remove(),520);
      });
      el.addEventListener('mousedown', ()=>el.classList.add('pressing'));
      ['mouseup','mouseleave','blur'].forEach(evt=>el.addEventListener(evt, ()=>el.classList.remove('pressing')));
    });

    document.querySelectorAll('.content.report .tabs .tab.active').forEach(tab=>{
      let pos=0, speed=.25;
      (function step(){ pos+=speed; if(pos>100) pos=0; tab.style.backgroundPosition=pos+'% 50%'; tab._raf=requestAnimationFrame(step); })();
    });

    const notifyBtn=document.querySelector('.content.report .header-right .icon-btn');
    if(notifyBtn){
      setInterval(()=>{
        const ring=document.createElement('span');
        ring.style.position='absolute'; ring.style.inset='0'; ring.style.borderRadius='10px';
        ring.style.pointerEvents='none'; ring.style.boxShadow='0 0 0 0 rgba(37,99,235,.35)'; ring.style.opacity='.35'; ring.style.transform='scale(1)';
        ring.style.transition='transform 1.2s ease-out, opacity 1.2s ease-out, box-shadow 1.2s ease-out';
        notifyBtn.appendChild(ring);
        requestAnimationFrame(()=>{ ring.style.transform='scale(2.2)'; ring.style.opacity='0'; ring.style.boxShadow='0 0 0 14px rgba(37,99,235,0)';});
        setTimeout(()=>ring.remove(),1300);
      },1800);
    }

    const params = new URLSearchParams(location.search);
    if (params.get('view') === 'payments') {
      const tbl = document.querySelector('.panel-body table.data-table');
      if (tbl) tbl.classList.add('payments-table');
    }

    document.querySelectorAll('.refunds-table td:nth-child(6) .chip').forEach(el=>{
      const t = (el.textContent || '').trim().toLowerCase();
      if (t === 'cash')   el.classList.add('cash');
      if (t === 'wallet') el.classList.add('wallet');
    });
  });
  </script>
</body>
</html>
