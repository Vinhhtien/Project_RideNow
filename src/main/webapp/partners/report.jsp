<%-- partners/report.jsp • Partner Revenue Report --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="vi_VN"/>
<fmt:requestEncoding value="UTF-8"/>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <title>Báo cáo doanh thu • Partner</title>
  <meta name="viewport" content="width=device-width, initial-scale=1"/>
  <style>
    :root{
      --bg:#0f1221; --card:#121735; --card-2:#10152e; --text:#e8eaf6; --muted:#a7b0d6;
      --primary:#6c8cff; --primary-2:#9aaeff; --danger:#ff6b6b; --success:#34d399;
      --border:#1c244c; --shadow:0 10px 30px rgba(0,0,0,.35); --radius:16px;
    }
    *{box-sizing:border-box} html,body{margin:0}
    body{
      font-family:ui-sans-serif,system-ui,-apple-system,Segoe UI,Roboto,Arial;
      background:
        radial-gradient(1200px 600px at -10% -10%, #24306b 0%, transparent 60%),
        radial-gradient(900px 600px at 110% -20%, #391d5f 0%, transparent 55%),
        linear-gradient(180deg, #0b0f1f 0%, #0a0d1c 100%);
      min-height:100vh; color:var(--text); padding:24px;
    }
    .wrap{max-width:1200px;margin:0 auto}

    .topbar{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-bottom:18px}
    .crumbs a{color:var(--muted);text-decoration:none;font-size:14px}
    .crumbs a:hover{color:var(--primary)}
    .title{
      font-size:28px;font-weight:800;letter-spacing:.3px;margin:6px 0 0;
      background:linear-gradient(90deg,#eaf0ff,#b9c7ff,#6c8cff);
      -webkit-background-clip:text;background-clip:text;color:transparent;
    }

    /* cân đối 2 cột */
    .grid{
      display:grid;
      grid-template-columns:1fr 1fr; /* = nhau */
      gap:18px;
      align-items:stretch;
    }
    @media (max-width:980px){.grid{grid-template-columns:1fr}}

    .card{
      background:linear-gradient(180deg,rgba(255,255,255,.03),rgba(255,255,255,.015)),var(--card);
      border:1px solid var(--border);border-radius:var(--radius);box-shadow:var(--shadow);padding:18px 18px 16px;
      height:100%;
    }
    .card h3{margin:2px 0 12px;font-size:18px;font-weight:700}

    /* filter row */
    .filters{display:grid;grid-template-columns:repeat(3,1fr);gap:10px;margin:0 0 12px}
    @media (max-width:640px){.filters{grid-template-columns:1fr}}
    .filters input{
      background:var(--card-2);border:1px solid var(--border);color:var(--text);
      padding:10px 12px;border-radius:12px;outline:none
    }
    /* icon lịch màu trắng */
    .filters input[type="date"]::-webkit-calendar-picker-indicator{
      filter: invert(1) brightness(1.6) contrast(1.1);
      opacity:.95;
    }

    .actions{display:flex;gap:10px;align-items:center}
    .btn{
      appearance:none;border:0;border-radius:12px;padding:10px 14px;font-weight:700;cursor:pointer;
      transition:transform .06s ease, filter .15s;
      text-decoration:none;display:inline-flex;align-items:center;gap:8px;
    }
    .btn:active{transform:translateY(1px)}
    .btn-primary{background:linear-gradient(180deg,#7da1ff,#6c8cff);color:#0a0f21;border:1px solid #90adff}
    .btn-ghost{background:transparent;color:var(--text);border:1px dashed #3a4477;text-decoration:none}
    .btn-ghost:hover{filter:brightness(1.1)}

    /* stats */
    .stats{display:grid;grid-template-columns:repeat(4,1fr);gap:12px;margin-top:8px}
    @media (max-width:1080px){.stats{grid-template-columns:repeat(2,1fr)}}
    @media (max-width:640px){.stats{grid-template-columns:1fr}}
    .stat{background:#111827;border:1px solid var(--border);border-radius:12px;padding:14px}
    .k{color:var(--muted);font-size:13px;margin:0 0 6px}
    .v{font-size:22px;font-weight:800;margin:0}
    .ok .v{color:var(--success)} .warn .v{color:var(--primary)} .dim .v{color:#cfd7ff}

    /* bảng: cố định cột, không tràn ngang */
    .table-wrap{border:1px solid var(--border);border-radius:12px;overflow:hidden}
    table{width:100%;border-collapse:collapse;table-layout:fixed}
    col.col-id{width:8ch}
    col.col-name{width:auto}
    col.col-num{width:12ch}
    th,td{padding:10px 12px;border-bottom:1px solid rgba(255,255,255,.06);text-align:left}
    thead th{background:rgba(255,255,255,.06)}
    th{font-size:12px;letter-spacing:.5px;text-transform:uppercase;color:#c7d2ff}
    tbody tr:nth-child(odd){background:rgba(255,255,255,.02)}
    tbody tr:hover{background:rgba(255,255,255,.05)}
    .right{text-align:right}
    .mono{font-variant-numeric:tabular-nums}
    .truncate{white-space:nowrap;overflow:hidden;text-overflow:ellipsis}

    .muted{color:var(--muted);font-size:13px}
    .footer-note{margin-top:10px;color:var(--muted);font-size:12px}
  </style>
</head>
<body>
<div class="wrap">

  <!-- Top -->
  <div class="topbar">
    <div>
      <div class="crumbs">
        <a href="${pageContext.request.contextPath}/dashboard">← Quay lại Dashboard</a>
      </div>
      <h1 class="title">Báo cáo doanh thu đối tác</h1>
    </div>
    <div class="actions">
      <a class="btn btn-ghost" href="${pageContext.request.contextPath}/partners/report">Làm mới</a>
    </div>
  </div>

  <c:set var="ctx" value="${pageContext.request.contextPath}" />
  <c:set var="partnerId" value="${requestScope.partnerId}" />

  <c:set var="orders" value="${summary != null ? summary.totalOrders : 0}" />
  <c:set var="collected" value="${summary != null ? summary.totalCollected : null}" />
  <c:set var="refunded"  value="${summary != null ? summary.totalRefunded : null}" />
  <c:set var="net"       value="${summary != null ? summary.netRevenue : null}" />
  <c:set var="payout"    value="${net != null ? (net * 0.40) : null}" />

  <div class="grid">

    <!-- Trái: bộ lọc + bảng -->
    <div class="card">
      <h3>Bộ lọc</h3>
      <form class="filters" method="get" action="${ctx}/partners/report">
        <input type="hidden" name="partnerId" value="${partnerId}"/>
        <input type="date" name="from" value="${param.from}"/>
        <input type="date" name="to"   value="${param.to}"/>
        <button class="btn btn-primary" type="submit">Áp dụng</button>
      </form>

      <h3>Doanh thu theo từng xe</h3>
      <c:choose>
        <c:when test="${empty bikes}">
          <p class="muted">Chưa có dữ liệu cho khoảng thời gian đã chọn.</p>
        </c:when>
        <c:otherwise>
          <div class="table-wrap">
            <table aria-label="Bảng doanh thu theo xe">
              <colgroup>
                <col class="col-id"/>
                <col class="col-name"/>
                <col class="col-num"/><col class="col-num"/><col class="col-num"/>
                <col class="col-num"/><col class="col-num"/>
              </colgroup>
              <thead>
              <tr>
                <th>Mã xe</th>
                <th>Tên xe</th>
                <th class="right">Đơn</th>
                <th class="right">Tổng thu</th>
                <th class="right">Hoàn</th>
                <th class="right">Ròng</th>
                <th class="right">Tổng</th>
              </tr>
              </thead>
              <tbody>
              <c:set var="t_orders" value="0" scope="page"/>
              <c:set var="t_collected" value="0" scope="page"/>
              <c:set var="t_refunded" value="0" scope="page"/>
              <c:set var="t_net" value="0" scope="page"/>
              <c:set var="t_payout" value="0" scope="page"/>

              <c:forEach var="b" items="${bikes}">
                <c:set var="b_payout" value="${b.netRevenue * 0.40}" />
                <tr>
                  <td class="mono">#<c:out value="${b.bikeId}"/></td>
                  <td class="truncate" title="${b.bikeName}"><c:out value="${b.bikeName}"/></td>
                  <td class="right mono"><c:out value="${b.orders}"/></td>
                  <td class="right mono"><fmt:formatNumber value="${b.collected}" type="number" groupingUsed="true"/></td>
                  <td class="right mono"><fmt:formatNumber value="${b.refunded}"  type="number" groupingUsed="true"/></td>
                  <td class="right mono"><fmt:formatNumber value="${b.netRevenue}" type="number" groupingUsed="true"/></td>
                  <td class="right mono"><fmt:formatNumber value="${b_payout}"     type="number" groupingUsed="true"/></td>
                </tr>

                <c:set var="t_orders"    value="${t_orders + b.orders}" />
                <c:set var="t_collected" value="${t_collected + b.collected}" />
                <c:set var="t_refunded"  value="${t_refunded + b.refunded}" />
                <c:set var="t_net"       value="${t_net + b.netRevenue}" />
                <c:set var="t_payout"    value="${t_payout + b_payout}" />
              </c:forEach>

              <tr>
                <th colspan="2" class="right">Tổng</th>
                <th class="right mono"><c:out value="${t_orders}"/></th>
                <th class="right mono"><fmt:formatNumber value="${t_collected}" type="number" groupingUsed="true"/></th>
                <th class="right mono"><fmt:formatNumber value="${t_refunded}"  type="number" groupingUsed="true"/></th>
                <th class="right mono"><fmt:formatNumber value="${t_net}"       type="number" groupingUsed="true"/></th>
                <th class="right mono"><fmt:formatNumber value="${t_payout}"    type="number" groupingUsed="true"/></th>
              </tr>
              </tbody>
            </table>
          </div>
        </c:otherwise>
      </c:choose>
    </div>

    <!-- Phải: Tổng quan -->
    <div class="card">
      <h3>Tổng quan</h3>

      <div class="filters" style="grid-template-columns:1fr;">
        <div class="muted">
          Khoảng thời gian:
          <b>
            <c:choose>
              <c:when test="${not empty param.from or not empty param.to}">
                <c:out value="${empty param.from ? '…' : param.from}"/> → <c:out value="${empty param.to ? '…' : param.to}"/>
              </c:when>
              <c:otherwise>Toàn bộ</c:otherwise>
            </c:choose>
          </b>
        </div>
      </div>

      <div class="stats">
        <div class="stat dim">
          <p class="k">Tổng đơn</p>
          <p class="v mono"><c:out value="${orders}"/></p>
        </div>
        <div class="stat warn">
          <p class="k">Tổng thu</p>
          <p class="v mono">
            <c:choose>
              <c:when test="${collected != null}">
                <fmt:formatNumber value="${collected}" type="number" groupingUsed="true"/> ₫
              </c:when>
              <c:otherwise>—</c:otherwise>
            </c:choose>
          </p>
        </div>
        <div class="stat dim">
          <p class="k">Hoàn</p>
          <p class="v mono">
            <c:choose>
              <c:when test="${refunded != null}">
                <fmt:formatNumber value="${refunded}" type="number" groupingUsed="true"/> ₫
              </c:when>
              <c:otherwise>—</c:otherwise>
            </c:choose>
          </p>
        </div>
        <div class="stat ok">
          <p class="k">Doanh thu ròng</p>
          <p class="v mono">
            <c:choose>
              <c:when test="${net != null}">
                <fmt:formatNumber value="${net}" type="number" groupingUsed="true"/> ₫
              </c:when>
              <c:otherwise>—</c:otherwise>
            </c:choose>
          </p>
        </div>
      </div>

      <div class="stats" style="grid-template-columns:1fr;">
        <div class="stat warn">
          <p class="k">Tổng doanh thu nhận được</p>
          <p class="v mono">
            <c:choose>
              <c:when test="${payout != null}">
                <fmt:formatNumber value="${payout}" type="number" groupingUsed="true"/> ₫
              </c:when>
              <c:otherwise>—</c:otherwise>
            </c:choose>
          </p>
        </div>
      </div>

      <p class="footer-note">“Tổng doanh thu (đối tác nhận được)” là 40% từ doanh thu ròng.</p>
    </div>
  </div>

</div>
</body>
</html>
