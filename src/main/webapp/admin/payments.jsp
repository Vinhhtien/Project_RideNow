<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <title>Pending Payments | Admin</title>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;800&display=swap" rel="stylesheet">
  <style>
    body{font-family:Inter,system-ui;margin:24px}
    a.btn{display:inline-block;padding:8px 12px;border:1px solid #e5e7eb;border-radius:8px;text-decoration:none;color:#111}
    table{width:100%;border-collapse:collapse;margin-top:14px}
    th,td{padding:10px;border:1px solid #e5e7eb;text-align:left}
    th{background:#f8fafc}
    .badge{padding:4px 8px;border-radius:999px;background:#fff3cd;color:#7a5c00}
    form{display:inline}
  </style>
</head>
<body>
  <h2>Thanh toán đang chờ xác minh</h2>

  <c:if test="${not empty sessionScope.flash}">
    <div style="color:green;margin:8px 0">${sessionScope.flash}</div>
    <c:remove var="flash" scope="session"/>
  </c:if>

  <div>
    <a class="btn" href="${ctx}/admin/dashboard">← Về Dashboard</a>
  </div>

  <c:choose>
    <c:when test="${empty rows}">
      <p>Không có khoản thanh toán nào đang chờ.</p>
    </c:when>
    <c:otherwise>
      <table>
        <thead>
          <tr>
            <th>ID</th><th>Order</th><th>Khách</th><th>Số tiền</th><th>Phương thức</th><th>Trạng thái</th><th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="r" items="${rows}">
            <tr>
              <td>${r.paymentId}</td>
              <td>#${r.orderId}</td>
              <td>${r.customerName}</td>
              <td><fmt:formatNumber value="${r.amount}" type="number"/> đ</td>
              <td>${r.method}</td>
              <td><span class="badge">${r.status}</span></td>
              <td>
                <form method="post" action="${ctx}/admin/payments"
                      onsubmit="return confirm('Xác nhận đã nhận tiền?');">
                  <input type="hidden" name="action" value="markPaid"/>
                  <input type="hidden" name="paymentId" value="${r.paymentId}"/>
                  <button type="submit">Đã nhận tiền</button>
                </form>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </c:otherwise>
  </c:choose>
</body>
</html>
