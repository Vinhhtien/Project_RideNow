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
    .badge { padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: 500; }
    .badge.returned { background:#dcfce7; color:#166534; }
    .badge.pending { background:#fef3c7; color:#92400e; }
    .badge.processing { background:#e0e7ff; color:#3730a3; }
    .badge.completed { background:#dcfce7; color:#166534; }
    .badge.cancelled { background:#fecaca; color:#dc2626; }
    .badge.refunded { background:#ecfdf5; color:#059669; }
    .badge.held { background:#f3f4f6; color:#374151; }
    .debug-info { background: #f3f4f6; padding: 10px; margin: 10px 0; border-radius: 5px; font-size: 14px; }
    .empty-state { text-align: center; padding: 40px; color: #666; }
    .text-muted { color: #6b7280 !important; }
    .dropdown { display: inline-block; position: relative; }
    .dropdown-menu { display: none; position: absolute; background: white; border: 1px solid #e5e7eb; border-radius: 6px; padding: 8px; z-index: 1000; min-width: 120px; }
    .dropdown:hover .dropdown-menu { display: block; }
    .dropdown-item { display: block; width: 100%; padding: 8px 12px; border: none; background: none; text-align: left; cursor: pointer; border-radius: 4px; }
    .dropdown-item:hover { background: #f3f4f6; }
  </style>
</head>
<body class="admin">
  <fmt:setLocale value="vi_VN"/>
  <aside class="sidebar">
    <div class="brand">RideNow Admin</div>
    <nav>
      <a href="${pageContext.request.contextPath}/admin/dashboard">Dashboard</a>
      <a href="${pageContext.request.contextPath}/adminpaymentverify">Xác Minh Thanh Toán</a>
      <a href="${pageContext.request.contextPath}/adminpickup">Giao Nhận Xe</a>
      <a href="${pageContext.request.contextPath}/adminreturn">Trả Xe</a>
      <a href="${pageContext.request.contextPath}/adminreturns" class="active">Kiểm tra và Hoàn Cọc</a>
      <!--<a href="${pageContext.request.contextPath}/adminwithdrawals">Rút Tiền</a>-->
      <a href="${pageContext.request.contextPath}/logout">Đăng xuất</a>
    </nav>
  </aside>

  <main class="content">
    <h1>Quản lý Hoàn Cọc</h1>

    <!-- DEBUG INFO -->
    <div class="debug-info">
      <strong>DEBUG:</strong> 
      Số đơn chờ kiểm tra: ${empty refundOrders ? 0 : refundOrders.size()} |
      Số yêu cầu hoàn cọc: ${empty refundRequests ? 0 : refundRequests.size()}
    </div>

    <c:if test="${not empty sessionScope.flash}">
      <div class="notice">${sessionScope.flash}</div>
      <c:remove var="flash" scope="session"/>
    </c:if>

    <!-- Danh sách đơn hàng chờ kiểm tra -->
    <section class="panel">
      <div class="panel-head">
        <h2>Đơn Hàng Chờ Kiểm Tra</h2>
        <p class="text-muted">Các đơn hàng đã trả xe, chờ kiểm tra tình trạng để hoàn cọc</p>
      </div>
      <c:choose>
        <c:when test="${empty refundOrders}">
          <div class="empty-state">
            <p>📭 Không có đơn hàng nào chờ kiểm tra</p>
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
                  <td>
                    <span class="badge returned">Đã trả xe</span><br>
                    <small class="text-muted">${order.depositStatus}</small>
                  </td>
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

    <!-- Yêu cầu hoàn cọc đang chờ xử lý -->
    <section class="panel">
      <div class="panel-head">
        <h2>Yêu Cầu Hoàn Cọc Đang Chờ Xử Lý</h2>
        <p class="text-muted">Các yêu cầu hoàn cọc sau khi kiểm tra xe</p>
      </div>
      <c:choose>
        <c:when test="${empty refundRequests}">
          <div class="empty-state">
            <p>💳 Không có yêu cầu hoàn cọc nào đang chờ xử lý</p>
          </div>
        </c:when>
        <c:otherwise>
          <table class="table">
            <thead>
              <tr>
                <th>Mã kiểm tra</th>
                <th>Mã đơn</th>
                <th>Khách hàng</th>
                <th>Xe thuê</th>
                <th>Tiền cọc</th>
                <th>Tiền hoàn</th>
                <th>Phí hư hỏng</th>
                <th>Tình trạng xe</th>
                <th>Ngày kiểm tra</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="req" items="${refundRequests}">
                <tr>
                  <td>#${req.inspectionId}</td>
                  <td>#${req.orderId}</td>
                  <td>
                    ${req.customerName}<br>
                    <small class="text-muted">${req.customerPhone}</small>
                  </td>
                  <td>${req.bikeName}</td>
                  <td><fmt:formatNumber value="${req.depositAmount}" type="currency"/></td>
                  <td><fmt:formatNumber value="${req.refundAmount}" type="currency"/></td>
                  <td>
                    <c:if test="${req.damageFee > 0}">
                      <span style="color: #dc2625;">
                        <fmt:formatNumber value="${req.damageFee}" type="currency"/>
                      </span>
                    </c:if>
                    <c:if test="${req.damageFee == 0}">-</c:if>
                  </td>
                  <td>
                    <c:choose>
                      <c:when test="${req.bikeCondition == 'excellent'}">
                        <span class="badge completed">Tốt</span>
                      </c:when>
                      <c:when test="${req.bikeCondition == 'good'}">
                        <span class="badge processing">Bình thường</span>
                      </c:when>
                      <c:when test="${req.bikeCondition == 'damaged'}">
                        <span class="badge cancelled">Hư hỏng</span>
                      </c:when>
                      <c:otherwise>${req.bikeCondition}</c:otherwise>
                    </c:choose>
                  </td>
                  <td><fmt:formatDate value="${req.inspectedAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                  <td>
                    <c:choose>
                      <c:when test="${req.status == 'pending'}">
                        <span class="badge pending">Đang chờ</span>
                      </c:when>
                      <c:when test="${req.status == 'processing'}">
                        <span class="badge processing">Đang xử lý</span>
                      </c:when>
                      <c:otherwise>
                        <span class="badge">${req.status}</span>
                      </c:otherwise>
                    </c:choose>
                  </td>
                  <td>
                    <!-- Duyệt = chuyển pending -> processing -->
                    <c:if test="${req.status == 'pending'}">
                      <form method="post" action="${pageContext.request.contextPath}/adminreturns" style="display:inline;">
                        <input type="hidden" name="inspectionId" value="${req.inspectionId}"/>
                        <input type="hidden" name="action" value="mark_processing"/>
                        <button type="submit" class="btn btn-sm btn-success" title="Duyệt yêu cầu">
                          ✅ Duyệt
                        </button>
                      </form>
                      <br/>
                    </c:if>
                    
                    <!-- Hoàn tất với lựa chọn phương thức -->
                    <c:if test="${req.status == 'pending' || req.status == 'processing'}">
                      <div class="dropdown" style="display:inline;">
                        <button class="btn btn-sm btn-primary dropdown-toggle" type="button">
                          ✔ Hoàn tất
                        </button>
                        <div class="dropdown-menu">
                          <form method="post" action="${pageContext.request.contextPath}/adminreturns" style="display:block;">
                            <input type="hidden" name="orderId" value="${req.orderId}"/>
                            <input type="hidden" name="action" value="complete_refund"/>
                            <input type="hidden" name="refundMethod" value="wallet"/>
                            <button type="submit" class="dropdown-item" onclick="return confirm('Xác nhận hoàn ${req.refundAmount} VNĐ về ví khách hàng?')">
                              💳 Về ví
                            </button>
                          </form>
                          <form method="post" action="${pageContext.request.contextPath}/adminreturns" style="display:block;">
                            <input type="hidden" name="orderId" value="${req.orderId}"/>
                            <input type="hidden" name="action" value="complete_refund"/>
                            <input type="hidden" name="refundMethod" value="cash"/>
                            <button type="submit" class="dropdown-item" onclick="return confirm('Xác nhận đã hoàn ${req.refundAmount} VNĐ tiền mặt cho khách?')">
                              💵 Tiền mặt
                            </button>
                          </form>
                        </div>
                      </div>
                      <br/>
                    </c:if>
                    
                    <!-- Từ chối = cancelled -->
                    <c:if test="${req.status == 'pending' || req.status == 'processing'}">
                      <form method="post" action="${pageContext.request.contextPath}/adminreturns" style="display:inline;">
                        <input type="hidden" name="inspectionId" value="${req.inspectionId}"/>
                        <input type="hidden" name="action" value="cancel"/>
                        <button type="submit" class="btn btn-sm btn-danger" onclick="return confirm('Xác nhận từ chối yêu cầu hoàn cọc này?')">
                          ❌ Từ chối
                        </button>
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

  <script>
    // Simple dropdown functionality
    document.addEventListener('DOMContentLoaded', function() {
      const dropdowns = document.querySelectorAll('.dropdown');
      dropdowns.forEach(dropdown => {
        dropdown.addEventListener('click', function(e) {
          e.stopPropagation();
        });
      });

      // Close dropdowns when clicking outside
      document.addEventListener('click', function() {
        dropdowns.forEach(dropdown => {
          const menu = dropdown.querySelector('.dropdown-menu');
          if (menu) menu.style.display = 'none';
        });
      });
    });
  </script>
</body>
</html>