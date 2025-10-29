<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt"  prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <title>Order Detail | Admin</title>
  <link rel="stylesheet" href="${ctx}/css/admin.css"/>
  <style>
    .kv{display:grid;grid-template-columns:160px 1fr;gap:.5rem 1rem}
    .summary{display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:.75rem;margin:1rem 0}
    .card{background:var(--card-bg);border:1px solid var(--border-color);border-radius:8px;padding:1rem}
    .status-badge{padding:.2rem .5rem;border-radius:999px;text-transform:capitalize}
    .pending{background:#fff3cd;color:#8a6d3b}.confirmed{background:#e0f7fa}.cancelled{background:#fdecea;color:#b71c1c}.completed{background:#e8f5e9;color:#1b5e20}
  </style>
</head>
<body class="admin">
<main class="content">
  <header class="content-header">
    <div>
      <h1>Order Detail</h1>
      <div class="breadcrumb"><a href="${ctx}/admin/dashboard">Dashboard</a> › <a href="${ctx}/admin/orders">Orders</a> › #${order.orderId}</div>
    </div>
  </header>

  <c:if test="${notFound}">
    <section class="panel"><div class="panel-body">Order not found.</div></section>
  </c:if>

  <c:if test="${not empty order}">
    <section class="panel">
      <div class="panel-header"><h2>Summary</h2></div>
      <div class="panel-body">
        <div class="kv">
          <div>Order ID</div><div>#${order.orderId}</div>
          <div>Customer</div><div>${order.customerName}</div>
          <div>Status</div><div><span class="status-badge ${order.orderStatus}">${order.orderStatus}</span></div>
          <div>Period</div>
          <div><fmt:formatDate value="${order.startDate}" pattern="dd/MM/yyyy"/> – <fmt:formatDate value="${order.endDate}" pattern="dd/MM/yyyy"/></div>
          <div>Created</div><div><fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm"/></div>
        </div>

        <div class="summary">
          <div class="card"><div>Total</div><h3><fmt:formatNumber value="${order.orderTotal}" type="currency"/></h3></div>
          <div class="card"><div>Paid</div><h3><fmt:formatNumber value="${order.totalPaid}" type="currency"/></h3></div>
          <div class="card"><div>Due</div><h3><fmt:formatNumber value="${order.amountDue}" type="currency"/></h3></div>
          <div class="card"><div>Payments</div><h3>${order.paymentCount}</h3>
            <small>Last: <fmt:formatDate value="${order.lastPaidAt}" pattern="dd/MM/yyyy HH:mm"/></small></div>
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-header"><h2>Items</h2></div>
      <div class="panel-body">
        <table class="data-table">
          <thead><tr><th>#</th><th>Bike</th><th>Plate</th><th>Price/Day</th><th>Qty</th><th>Line Total</th></tr></thead>
          <tbody>
          <c:forEach var="it" items="${items}">
            <tr>
              <td>${it.detailId}</td><td>${it.bikeName}</td><td>${it.licensePlate}</td>
              <td><fmt:formatNumber value="${it.pricePerDay}" type="currency"/></td>
              <td>${it.quantity}</td>
              <td><fmt:formatNumber value="${it.lineTotal}" type="currency"/></td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
      </div>
    </section>

    <section class="panel">
      <div class="panel-header">
        <h2>Payments</h2>
        <a class="btn btn-outline" href="${ctx}/adminpaymentverify?order_id=${order.orderId}">Verify Payments</a>
      </div>
      <div class="panel-body">
        <table class="data-table">
          <thead><tr><th>ID</th><th>Date</th><th>Amount</th><th>Method</th><th>Status</th><th>Verified</th></tr></thead>
          <tbody>
          <c:forEach var="p" items="${payments}">
            <tr>
              <td>${p.paymentId}</td>
              <td><fmt:formatDate value="${p.paymentDate}" pattern="dd/MM/yyyy HH:mm"/></td>
              <td><fmt:formatNumber value="${p.amount}" type="currency"/></td>
              <td>${p.method}</td>
              <td>${p.status}</td>
              <td><c:choose>
                  <c:when test="${not empty p.verifiedBy}">Yes (<fmt:formatDate value="${p.verifiedAt}" pattern="dd/MM/yyyy HH:mm"/>)</c:when>
                  <c:otherwise>No</c:otherwise>
                </c:choose></td>
            </tr>
          </c:forEach>
          <c:if test="${empty payments}">
            <tr><td colspan="6" style="text-align:center;padding:1rem;">No payments</td></tr>
          </c:if>
          </tbody>
        </table>
      </div>
    </section>
  </c:if>
</main>
</body>
</html>
