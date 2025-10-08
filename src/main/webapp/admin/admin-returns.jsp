<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt"  prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Quản lý hoàn cọc - RideNow Admin</title>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;800&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
  <style>
    .badge.returned { background:#dcfce7; color:#166534; }
    .badge.dang_cho { background:#fef3c7; color:#92400e; }
    .badge.dang_xu_ly { background:#e0e7ff; color:#3730a3; }
    .badge.da_hoan_thanh { background:#dcfce7; color:#166534; }
    .badge.da_huy { background:#fecaca; color:#dc2626; }
    .debug-info { background: #f3f4f6; padding: 10px; margin: 10px 0; border-radius: 5px; }
  </style>
</head>
<body class="admin">
  <fmt:setLocale value="vi_VN"/>
  <aside class="sidebar">
    <div class="brand">RideNow Admin</div>
    <nav>
      <a href="${pageContext.request.contextPath}/admindashboard">Dashboard</a>
      <a href="${pageContext.request.contextPath}/adminpaymentverify">Xác Minh Thanh Toán</a>
      <a href="${pageContext.request.contextPath}/adminpickup">Giao Nhận Xe</a>
      <a href="${pageContext.request.contextPath}/adminreturn">Trả Xe</a>
      <a class="active" href="${pageContext.request.contextPath}/adminreturns">Hoàn Cọc</a>
      <a href="${pageContext.request.contextPath}/adminwithdrawals">Rút Tiền</a>
      <a href="${pageContext.request.contextPath}/logout">Đăng xuất</a>
    </nav>
  </aside>

  <main class="content">
    <h1>Quản lý Hoàn Cọc</h1>

    <!-- DEBUG INFO -->
    <div class="debug-info">
      <strong>DEBUG:</strong> 
      Số đơn chờ hoàn cọc: ${empty refundOrders ? 0 : refundOrders.size()} |
      Số yêu cầu rút tiền: ${empty pendingWithdrawals ? 0 : pendingWithdrawals.size()}
    </div>

    <c:if test="${not empty sessionScope.flash}">
      <div class="notice">${sessionScope.flash}</div>
      <c:remove var="flash" scope="session"/>
    </c:if>

    <!-- Danh sách đơn hàng chờ hoàn cọc -->
    <section class="panel">
      <div class="panel-head">
        <h2>Đơn Hàng Chờ Hoàn Cọc</h2>
      </div>
      <c:choose>
        <c:when test="${empty refundOrders}">
          <div class="empty-state">
            <p>📭 Không có đơn hàng nào chờ hoàn cọc</p>
            <small>Các đơn hàng đã trả xe sẽ xuất hiện ở đây</small>
          </div>
        </c:when>
        <c:otherwise>
          <table class="table">
            <thead>
              <tr>
                <th>Mã đơn</th>
                <th>Khách hàng</th>
                <th>Xe thuê</th>
                <th>Ngày trả</th>
                <th>Tiền cọc</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="order" items="${refundOrders}">
                <tr>
                  <td>#${order.orderId}</td>
                  <td>
                    ${order.customerName}<br>
                    <small class="text-muted">${order.customerPhone}</small>
                  </td>
                  <td>${order.bikeName}</td>
                  <td>
                    <c:choose>
                      <c:when test="${order.returnedAt != null}">
                        <fmt:formatDate value="${order.returnedAt}" pattern="dd/MM/yyyy HH:mm"/>
                      </c:when>
                      <c:otherwise>-</c:otherwise>
                    </c:choose>
                  </td>
                  <td><fmt:formatNumber value="${order.depositAmount}" type="currency"/></td>
                  <td><span class="badge returned">Đã trả xe</span></td>
                  <td>
                    <a class="btn btn-sm btn-primary"
                       href="${pageContext.request.contextPath}/adminreturninspect?orderId=${order.orderId}">
                      🔍 Kiểm tra & Hoàn cọc
                    </a>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </c:otherwise>
      </c:choose>
    </section>

    <!-- Yêu cầu rút tiền -->
    <section class="panel">
      <div class="panel-head">
        <h2>Yêu Cầu Rút Tiền</h2>
      </div>
      <c:choose>
        <c:when test="${empty pendingWithdrawals}">
          <div class="empty-state">
            <p>💳 Không có yêu cầu rút tiền nào</p>
          </div>
        </c:when>
        <c:otherwise>
          <table class="table">
            <thead>
              <tr>
                <th>Mã yêu cầu</th>
                <th>Khách hàng</th>
                <th>Số tiền</th>
                <th>Ngày yêu cầu</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="withdrawal" items="${pendingWithdrawals}">
                <tr>
                  <td>#${withdrawal.withdrawalId}</td>
                  <td>
                    ${withdrawal.customerName}<br>
                    <small class="text-muted">${withdrawal.customerPhone}</small>
                  </td>
                  <td><fmt:formatNumber value="${withdrawal.amount}" type="currency"/></td>
                  <td>
                    <fmt:formatDate value="${withdrawal.requestDate}" pattern="dd/MM/yyyy HH:mm"/>
                  </td>
                  <td>
                    <span class="badge ${withdrawal.status}">
                      ${withdrawal.status}
                    </span>
                  </td>
                  <td>
                    <c:if test="${withdrawal.status == 'đang chờ'}">
                      <form method="post" action="${pageContext.request.contextPath}/adminwithdrawals" style="display:inline;">
                        <input type="hidden" name="withdrawalId" value="${withdrawal.withdrawalId}"/>
                        <input type="hidden" name="action" value="confirm"/>
                        <button type="submit" class="btn btn-sm btn-success">✅ Duyệt</button>
                      </form>
                      <form method="post" action="${pageContext.request.contextPath}/adminwithdrawals" style="display:inline;">
                        <input type="hidden" name="withdrawalId" value="${withdrawal.withdrawalId}"/>
                        <input type="hidden" name="action" value="cancel"/>
                        <button type="submit" class="btn btn-sm btn-danger">❌ Từ chối</button>
                      </form>
                    </c:if>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </c:otherwise>
      </c:choose>
    </section>
  </main>
</body>
</html>