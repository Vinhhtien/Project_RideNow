<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt"  prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%
  response.setCharacterEncoding("UTF-8");
%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="vi_VN"/>
<fmt:requestEncoding value="UTF-8"/>

<c:set var="view" value="${empty param.view ? (empty requestScope.view ? 'overview' : requestScope.view) : param.view}"/>
<c:set var="page" value="${empty param.page ? (empty requestScope.page ? 1 : requestScope.page) : param.page}"/>
<c:set var="size" value="${empty param.size ? (empty requestScope.size ? 20 : requestScope.size) : param.size}"/>
<c:set var="type" value="${empty param.type ? (empty filters.type ? '' : filters.type) : param.type}"/>
<c:set var="star" value="${empty param.star ? (empty filters.star ? '' : filters.star) : param.star}"/>

<c:set var="fromStr"><fmt:formatDate value="${filters.from}" pattern="yyyy-MM-dd"/></c:set>
<c:set var="toStr"><fmt:formatDate value="${filters.to}" pattern="yyyy-MM-dd"/></c:set>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Feedback • RideNow Admin</title>

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
      --radius-sm:10px; --radius-md:12px; --radius-lg:14px; --radius-full:9999px;
      --shadow-sm:0 1px 2px rgba(16,24,40,.04); --shadow-sm-alt:0 1px 2px rgba(0,0,0,.03);
      --transition-fast:all .12s ease;
    }
    body.admin{font-family:var(--font-family-sans);color:var(--color-text-primary);background:var(--color-bg-body);}
    .sidebar{position:fixed;left:0;top:0;bottom:0;width:var(--sidebar-width);background:linear-gradient(180deg,#0b0f1f 0%,#141a36 60%,#1b234a 100%);color:var(--color-sidebar-text);}
    .content.report{margin-left:var(--sidebar-width);padding:24px;}

    .content.report .content-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;border-bottom:1px solid var(--color-border-light);padding-bottom:8px;}
    .content.report .header-right{display:flex;align-items:center;gap:12px;}
    .content.report .header-right .icon-btn{order:0;position:relative;}
    .content.report .header-right .user-profile{order:1;display:flex;align-items:center;gap:8px;}

    .content.report .breadcrumb{display:flex;gap:8px;align-items:center;color:var(--color-text-muted-dark);}
    .content.report .breadcrumb .active{color:var(--color-text-primary);}
    .content.report .icon-btn{display:flex;align-items:center;justify-content:center;width:40px;height:40px;border-radius:10px;background:var(--color-primary-bg);color:var(--color-primary);border:1px solid var(--color-border);box-shadow:var(--shadow-sm);}

    .content.report .panel{background:var(--color-bg-content);border:1px solid var(--color-border);border-radius:12px;margin:16px 0;box-shadow:var(--shadow-sm);}
    .content.report .panel-header{padding:16px 20px;border-bottom:1px solid var(--color-border);display:flex;justify-content:space-between;align-items:center;}
    .content.report .panel-body{padding:16px 20px;}

    .content.report .tabs{display:flex;gap:10px;flex-wrap:wrap;}
    .content.report .tab{display:inline-flex;align-items:center;justify-content:center;height:40px;padding:0 14px;border-radius:12px;font-weight:600;font-size:14px;background:#fff;border:1px solid var(--color-border);color:var(--color-text-primary);text-decoration:none;cursor:pointer;}
    .content.report .tab:hover:not(.active){background:var(--color-bg-hover-light);border-color:#cbd5e1;transform:translateY(-2px);}
    .content.report .tab.active{background-image:linear-gradient(135deg,#ff8a00,#ff3d81,#7c4dff,#1fa2ff);background-size:200% 200%;background-position:0% 50%;color:#fff;border:none;box-shadow:0 10px 22px rgba(37,99,235,.22);text-decoration:none;}

    .content.report .btn{height:40px;padding:0 12px;border:1px solid var(--color-border-input);border-radius:10px;background:#fff;text-decoration:none;color:var(--color-text-primary);font-size:14px;font-weight:600;display:inline-flex;align-items:center;justify-content:center;}
    .content.report .btn.primary{border-color:var(--color-primary);background:linear-gradient(180deg,var(--color-primary-light) 0%,var(--color-primary) 100%);color:#fff;}

    .content.report .filters{display:flex;gap:12px;flex-wrap:wrap;align-items:flex-end;}
    .content.report .filters label{font-size:14px;color:#334155;font-weight:600;}
    .content.report .filters input[type="date"], .content.report .filters select{height:40px;padding:0 12px;font-size:14px;border:1px solid var(--color-border-input);border-radius:10px;background:#fff;}
    .content.report .filters .filter-actions{margin-left:auto;display:flex;gap:12px;flex-wrap:wrap;}

    .content.report .grid4{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:14px;}
    .content.report .grid2{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:14px;}
    .content.report .card{
      position:relative;border:1px solid transparent;
      background-image:linear-gradient(#fff,#fff), linear-gradient(135deg,#60a5fa,#a78bfa,#f472b6,#f59e0b);
      background-origin:border-box;background-clip:padding-box,border-box;border-radius:14px;padding:14px;box-shadow:0 1px 2px rgba(0,0,0,.03);
      transition:transform .18s ease, box-shadow .18s ease, background-position .3s ease;background-size:auto,200% 200%;background-position:center,0% 50%;
    }
    .content.report .card:hover{transform:translateY(-3px);box-shadow:0 14px 28px rgba(31,98,235,.15);background-position:center,100% 50%;}
    .content.report .card .muted{color:var(--color-text-muted);font-size:12px;margin-bottom:6px;}

    .content.report .data-table{width:100%;border-collapse:collapse;}
    .content.report .data-table thead th{background:var(--color-bg-header);color:var(--color-text-header);font-weight:700;font-size:14px;border-bottom:1px solid var(--color-border);padding:12px 10px;text-align:left;white-space:nowrap;}
    .content.report .data-table tbody td{font-size:14px;padding:10px;border-bottom:1px solid var(--color-border-light);}
    .content.report .data-table tbody tr:hover{background:linear-gradient(90deg,rgba(37,99,235,.06),rgba(37,99,235,0));}

    .badge{padding:4px 10px;border-radius:9999px;font-weight:700;font-size:12px;letter-spacing:.2px;display:inline-block;}
    .badge.store{background:#eef2ff;color:#1e40af;border:1px solid #c7d2fe;}
    .badge.bike{background:#ecfeff;color:#0c4a6e;border:1px solid #bae6fd;}
    .stars{color:#f59e0b;}
    .num{text-align:right;}
    
    /* ===== Feedback tabs: style như nút "Lọc" ===== */
    .feedback-tabs{
        display:flex;
        gap:10px;
        margin-bottom:14px;
        flex-wrap:wrap;
    }

    .feedback-tab{
        display:inline-flex;
        align-items:center;
        justify-content:center;
        height:40px;
        padding:0 12px;
        border:1px solid var(--color-border-input);
        border-radius:10px;
        background:#fff;
        color:var(--color-text-primary);
        font-size:14px;
        font-weight:600;
        cursor:pointer;
        user-select:none;
        text-decoration:none;
        transition:transform .12s ease, box-shadow .12s ease, background-color .12s ease;
    }

    .feedback-tab:hover{
        transform:translateY(-1px);
        box-shadow:var(--shadow-sm);
    }

    .feedback-tab.active{
        border-color:var(--color-primary);
        background:linear-gradient(180deg,var(--color-primary-light) 0%,var(--color-primary) 100%);
        color:#fff;
        box-shadow:0 6px 14px var(--color-primary-shadow);
    }

    .feedback-content{
        margin-top:10px;
    }

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
      <a href="${ctx}/adminpickup"     class="nav-item"><i class="fas fa-shipping-fast"></i><span>Vehicle Pickup</span></a>
      <a href="${ctx}/adminreturn"     class="nav-item"><i class="fas fa-undo-alt"></i><span>Vehicle Return</span></a>
      <a href="${ctx}/adminreturns"    class="nav-item"><i class="fas fa-clipboard-check"></i><span>Verify & Refund</span></a>
      <a href="${ctx}/admin/reports"   class="nav-item"><i class="fas fa-chart-bar"></i><span>Reports</span></a>
      <a href="${ctx}/admin/feedback"  class="nav-item active"><i class="fas fa-comment-alt"></i><span>Feedback</span></a>
      <a href="${ctx}/logout"          class="nav-item logout"><i class="fas fa-sign-out-alt"></i><span>Logout</span></a>
    </nav>
  </aside>

  <main class="content report">
    <header class="content-header">
      <div class="header-left">
        <h1>Feedback</h1>
        <div class="breadcrumb"><span>Admin</span><i class="fas fa-chevron-right"></i><span class="active">Feedback</span></div>
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
          <a class="tab ${view=='overview'?'active':''}" href="${ctx}/admin/feedback?view=overview&from=${fromStr}&to=${toStr}">Tổng quan</a>
          <a class="tab ${view=='details'?'active':''}"  href="${ctx}/admin/feedback?view=details&from=${fromStr}&to=${toStr}&type=${type}&star=${star}&size=${size}&page=${page}">Chi tiết</a>
        </div>
      </div>
      <div class="panel-body">
        <form method="get" class="filters">
          <input type="hidden" name="view" value="${view}"/>
          <label>Từ ngày<br><input type="date" name="from" value="${fromStr}"/></label>
          <label>Đến ngày<br><input type="date" name="to" value="${toStr}"/></label>

          <c:if test="${view == 'details'}">
            <label>Loại<br>
              <select name="type">
                <option value=""      ${empty type?'selected':''}>Tất cả</option>
                <option value="STORE" ${fn:toUpperCase(fn:trim(type))=='STORE'?'selected':''}>Cửa hàng</option>
                <option value="BIKE"  ${fn:toUpperCase(fn:trim(type))=='BIKE'?'selected':''}>Xe</option>
              </select>
            </label>
            <label>Điểm sao<br>
              <select name="star">
                <option value="" ${empty star?'selected':''}>Tất cả</option>
                <option value="5" ${star==5?'selected':''}>5</option>
                <option value="4" ${star==4?'selected':''}>4</option>
                <option value="3" ${star==3?'selected':''}>3</option>
                <option value="2" ${star==2?'selected':''}>2</option>
                <option value="1" ${star==1?'selected':''}>1</option>
              </select>
            </label>
            <label>Kích thước trang<br>
              <select name="size">
                <option ${size==10?'selected':''}>10</option>
                <option ${size==20?'selected':''}>20</option>
                <option ${size==50?'selected':''}>50</option>
                <option ${size==100?'selected':''}>100</option>
              </select>
            </label>
          </c:if>

          <button type="submit" class="btn primary">Lọc</button>
          <div class="filter-actions">
            <c:if test="${view=='details'}">
              <a class="btn" href="${ctx}/admin/feedback?action=export&type=details&from=${fromStr}&to=${toStr}&type=${type}&star=${star}">Xuất CSV</a>
            </c:if>
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
              <div class="card"><div class="muted">Tổng số feedback</div><h3><fmt:formatNumber value="${summary.countOverall}" type="number" groupingUsed="true"/></h3></div>
              <div class="card"><div class="muted">Feedback cửa hàng</div><h3><fmt:formatNumber value="${summary.countStore}" type="number" groupingUsed="true"/></h3></div>
              <div class="card"><div class="muted">Feedback xe</div><h3><fmt:formatNumber value="${summary.countBike}" type="number" groupingUsed="true"/></h3></div>
              <div class="card"><div class="muted">Điểm TB chung</div><h3><fmt:formatNumber value="${summary.avgOverall}" type="number" maxFractionDigits="2"/></h3></div>
            </div>

            <div class="grid2" style="margin-top:14px">
              <div class="card">
                <div class="muted">TB cửa hàng</div>
                <h3><fmt:formatNumber value="${summary.avgStore}" type="number" maxFractionDigits="2"/></h3>
                <table class="data-table" style="margin-top:10px">
                  <thead><tr><th>Sao</th><th class="num">Số lượng</th></tr></thead>
                  <tbody>
                    <c:forEach var="i" begin="1" end="5">
                      <c:set var="s" value="${6 - i}"/>
                      <tr>
                        <td><span class="stars"><i class="fa fa-star"></i></span> ${s}</td>
                        <td class="num"><c:out value="${summary.histStore[s]}" default="0"/></td>
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
              </div>
              <div class="card">
                <div class="muted">TB xe</div>
                <h3><fmt:formatNumber value="${summary.avgBike}" type="number" maxFractionDigits="2"/></h3>
                <table class="data-table" style="margin-top:10px">
                  <thead><tr><th>Sao</th><th class="num">Số lượng</th></tr></thead>
                  <tbody>
                    <c:forEach var="i" begin="1" end="5">
                      <c:set var="s" value="${6 - i}"/>
                      <tr>
                        <td><span class="stars"><i class="fa fa-star"></i></span> ${s}</td>
                        <td class="num"><c:out value="${summary.histBike[s]}" default="0"/></td>
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
              </div>
            </div>

            <div class="grid2" style="margin-top:14px">
              <div class="card">
                <div class="muted">Top cửa hàng</div>
                <table class="data-table">
                  <thead><tr><th>#</th><th>Tên</th><th class="num">TB</th><th class="num">SL</th></tr></thead>
                  <tbody>
                    <c:set var="rk" value="1"/>
                    <c:forEach var="t" items="${summary.topStores}">
                      <tr>
                        <td class="num">${rk}</td>
                        <td>${t.name}</td>
                        <td class="num"><fmt:formatNumber value="${t.avgRating}" type="number" maxFractionDigits="2"/></td>
                        <td class="num"><fmt:formatNumber value="${t.count}" type="number"/></td>
                      </tr>
                      <c:set var="rk" value="${rk+1}"/>
                    </c:forEach>
                    <c:if test="${empty summary.topStores}">
                      <tr><td colspan="4" class="num">Không có dữ liệu.</td></tr>
                    </c:if>
                  </tbody>
                </table>
              </div>
              <div class="card">
                <div class="muted">Top xe</div>
                <table class="data-table">
                  <thead><tr><th>#</th><th>Tên</th><th class="num">TB</th><th class="num">SL</th></tr></thead>
                  <tbody>
                    <c:set var="rk2" value="1"/>
                    <c:forEach var="t" items="${summary.topBikes}">
                      <tr>
                        <td class="num">${rk2}</td>
                        <td>${t.name}</td>
                        <td class="num"><fmt:formatNumber value="${t.avgRating}" type="number" maxFractionDigits="2"/></td>
                        <td class="num"><fmt:formatNumber value="${t.count}" type="number"/></td>
                      </tr>
                      <c:set var="rk2" value="${rk2+1}"/>
                    </c:forEach>
                    <c:if test="${empty summary.topBikes}">
                      <tr><td colspan="4" class="num">Không có dữ liệu.</td></tr>
                    </c:if>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </section>
      </c:when>

      <c:when test="${view=='details'}">
        <section class="panel">
          <div class="panel-header"><h2>Chi tiết Feedback</h2></div>
          <div class="panel-body">

            <!-- Tabs: chỉ đổi khu vực này -->
            <div class="feedback-tabs" style="display:flex;gap:8px;margin-bottom:12px;">
              <a class="feedback-tab ${empty type or fn:toUpperCase(fn:trim(type))=='STORE' ? 'active' : ''}"
                 href="${ctx}/admin/feedback?view=details&from=${fromStr}&to=${toStr}&type=STORE&star=${star}&size=${size}&page=1">
                Feedback Cửa Hàng
              </a>
              <a class="feedback-tab ${fn:toUpperCase(fn:trim(type))=='BIKE' ? 'active' : ''}"
                 href="${ctx}/admin/feedback?view=details&from=${fromStr}&to=${toStr}&type=BIKE&star=${star}&size=${size}&page=1">
                Feedback Xe
              </a>
            </div>

            <!-- Chỉ render 1 bảng theo type để không lệch layout -->
            <c:choose>
              <c:when test="${fn:toUpperCase(fn:trim(type))=='BIKE'}">
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>Loại</th><th>Mục tiêu</th><th>Order</th><th>Khách</th><th>Sao</th><th>Tiêu đề</th><th>Nội dung</th><th>Thời gian</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:set var="hasBike" value="false"/>
                    <c:forEach var="it" items="${items}">
                      <c:if test="${fn:toUpperCase(fn:trim(it.type))=='BIKE'}">
                        <tr>
                          <td><span class="badge bike">BIKE</span></td>
                          <td>
                            <div style="display:flex;gap:8px;align-items:center">
                              <i class="fas fa-motorcycle"></i>
                              <div>
                                <div><strong>${empty it.targetName ? it.targetCode : it.targetName}</strong></div>
                                <div class="muted">${it.targetCode}</div>
                              </div>
                            </div>
                          </td>
                          <td>
                            <c:choose>
                              <c:when test="${empty it.orderId}">—</c:when>
                              <c:otherwise><a class="badge" href="${ctx}/admin/reports?view=order-detail&id=${it.orderId}">#${it.orderId}</a></c:otherwise>
                            </c:choose>
                          </td>
                          <td>
                            <c:choose>
                              <c:when test="${empty it.customerName}">#${it.customerId}</c:when>
                              <c:otherwise>${it.customerName}</c:otherwise>
                            </c:choose>
                          </td>
                          <td>
                            <span class="stars">
                              <c:forEach var="i" begin="1" end="5">
                                <i class="${i <= (empty it.rating ? 0 : it.rating) ? 'fas fa-star' : 'far fa-star'}"></i>
                              </c:forEach>
                            </span>
                          </td>
                          <td>${empty it.title ? '-' : it.title}</td>
                          <td>${empty it.content ? '-' : it.content}</td>
                          <td><fmt:formatDate value="${it.createdAt}" pattern="yyyy-MM-dd HH:mm"/></td>
                        </tr>
                        <c:set var="hasBike" value="true"/>
                      </c:if>
                    </c:forEach>
                    <c:if test="${not hasBike}">
                      <tr><td colspan="8">Không có dữ liệu.</td></tr>
                    </c:if>
                  </tbody>
                </table>
              </c:when>

              <c:otherwise>
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>Loại</th><th>Mục tiêu</th><th>Order</th><th>Khách</th><th>Sao</th><th>Tiêu đề</th><th>Nội dung</th><th>Thời gian</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:set var="hasStore" value="false"/>
                    <c:forEach var="it" items="${items}">
                      <c:if test="${fn:toUpperCase(fn:trim(it.type))=='STORE'}">
                        <tr>
                          <td><span class="badge store">STORE</span></td>
                          <td>
                            <div style="display:flex;gap:8px;align-items:center">
                              <i class="fa fa-store"></i>
                              <div>
                                <div><strong>${empty it.targetName ? it.targetCode : it.targetName}</strong></div>
                                <div class="muted">${it.targetCode}</div>
                              </div>
                            </div>
                          </td>
                          <td>
                            <c:choose>
                              <c:when test="${empty it.orderId}">—</c:when>
                              <c:otherwise><a class="badge" href="${ctx}/admin/reports?view=order-detail&id=${it.orderId}">#${it.orderId}</a></c:otherwise>
                            </c:choose>
                          </td>
                          <td>
                            <c:choose>
                              <c:when test="${empty it.customerName}">#${it.customerId}</c:when>
                              <c:otherwise>${it.customerName}</c:otherwise>
                            </c:choose>
                          </td>
                          <td>
                            <span class="stars">
                              <c:forEach var="i" begin="1" end="5">
                                <i class="${i <= (empty it.rating ? 0 : it.rating) ? 'fas fa-star' : 'far fa-star'}"></i>
                              </c:forEach>
                            </span>
                          </td>
                          <td>${empty it.title ? '-' : it.title}</td>
                          <td>${empty it.content ? '-' : it.content}</td>
                          <td><fmt:formatDate value="${it.createdAt}" pattern="yyyy-MM-dd HH:mm"/></td>
                        </tr>
                        <c:set var="hasStore" value="true"/>
                      </c:if>
                    </c:forEach>
                    <c:if test="${not hasStore}">
                      <tr><td colspan="8">Không có dữ liệu.</td></tr>
                    </c:if>
                  </tbody>
                </table>
              </c:otherwise>
            </c:choose>

            <c:set var="hasPrev" value="${page > 1}"/>
            <c:set var="hasNext" value="${total > page * size}"/>
            <c:set var="totalPages" value="${(total + size - 1) / size}"/>

            <c:if test="${hasPrev || hasNext}">
              <div class="pager" style="margin-top:14px;display:flex;gap:12px;align-items:center;">
                <span>Trang <fmt:formatNumber value="${page}" maxFractionDigits="0"/> / <fmt:formatNumber value="${totalPages}" maxFractionDigits="0"/></span>
                <c:if test="${hasPrev}">
                  <a class="btn" href="${ctx}/admin/feedback?view=details&from=${fromStr}&to=${toStr}&type=${type}&star=${star}&size=${size}&page=${page-1}">« Trước</a>
                </c:if>
                <c:if test="${hasNext}">
                  <a class="btn" href="${ctx}/admin/feedback?view=details&from=${fromStr}&to=${toStr}&type=${type}&star=${star}&size=${size}&page=${page+1}">Tiếp »</a>
                </c:if>
              </div>
            </c:if>
          </div>
        </section>
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
    });

    // animated gradient for active tab in header
    document.querySelectorAll('.content.report .tabs .tab.active').forEach(tab=>{
      let pos=0, speed=.25;
      (function step(){ pos+=speed; if(pos>100) pos=0; tab.style.backgroundPosition=pos+'% 50%'; tab._raf=requestAnimationFrame(step); })();
    });

    // giữ nguyên JS; tab chi tiết hiện đang điều hướng bằng link nên không cần preventDefault
  });
  </script>
</body>
</html>
