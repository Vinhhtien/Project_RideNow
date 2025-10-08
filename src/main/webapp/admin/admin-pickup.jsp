<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Giao Nhận Xe - RideNow Admin</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${ctx}/css/admin.css">
    <style>
        .badge.not_picked_up { background: #fef3c7; color: #92400e; }
        .badge.picked_up { background: #dcfce7; color: #166534; }
        .empty-state { text-align: center; padding: 40px; color: #666; }
        .empty-state i { font-size: 48px; margin-bottom: 16px; color: #ccc; }
    </style>
</head>
<body class="admin">
    <aside class="sidebar">
        <div class="brand">RideNow Admin</div>
        <nav>
            <a href="${pageContext.request.contextPath}/admin/dashboard">Dashboard</a>
            <a href="${pageContext.request.contextPath}/adminpaymentverify">Xác Minh Thanh Toán</a>
            <a href="${pageContext.request.contextPath}/adminpickup" class="active">Giao Nhận Xe</a>
            <a href="${pageContext.request.contextPath}/adminreturn">Trả Xe</a>
            <a href="${pageContext.request.contextPath}/adminreturns">Kiểm tra và Hoàn Cọc</a>
            <!--<a href="${pageContext.request.contextPath}/adminwithdrawals">Rút Tiền</a>-->
            <a href="${pageContext.request.contextPath}/logout">Đăng xuất</a>
        </nav>
    </aside>

    <main class="content">
        <h1>Giao Nhận Xe</h1>

        <c:if test="${not empty sessionScope.flash}">
            <div class="notice">${sessionScope.flash}</div>
            <c:remove var="flash" scope="session"/>
        </c:if>

        <div class="panel">
            <div class="panel-head">
                <h2>Đơn Hàng Chờ Giao Xe</h2>
            </div>

            <c:choose>
                <c:when test="${empty orders}">
                    <div class="empty-state">
                        <i class="fas fa-motorcycle"></i>
                        <h3>Không có đơn hàng nào chờ giao xe</h3>
                        <p>Tất cả các đơn hàng đã được xử lý</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <table class="table">
                        <thead>
                            <tr>
                                <th>Mã Đơn</th>
                                <th>Khách Hàng</th>
                                <th>Xe Thuê</th>
                                <th>Ngày Thuê</th>
                                <th>Tổng Tiền</th>
                                <th>Tiền Cọc</th>
                                <th>Trạng Thái</th>
                                <th>Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="o" items="${orders}">
                                <tr>
                                    <td>#${o[0]}</td>
                                    <td>
                                        ${o[1]}<br>
                                        <small class="text-muted">${o[2]}</small>
                                    </td>
                                    <td>${o[3]}</td> <!-- ĐÃ SỬA: bike_name từ index 3 -->
                                    <td>
                                        <fmt:formatDate value="${o[4]}" pattern="dd/MM/yyyy"/> - <!-- ĐÃ SỬA: start_date từ index 4 -->
                                        <fmt:formatDate value="${o[5]}" pattern="dd/MM/yyyy"/> <!-- ĐÃ SỬA: end_date từ index 5 -->
                                    </td>
                                    <td><fmt:formatNumber value="${o[6]}" type="currency"/></td> <!-- ĐÃ SỬA: total_price từ index 6 -->
                                    <td><fmt:formatNumber value="${o[7]}" type="currency"/></td> <!-- ĐÃ SỬA: deposit_amount từ index 7 -->
                                    <td><span class="badge not_picked_up">${o[8]}</span></td> <!-- ĐÃ SỬA: pickup_status từ index 8 -->
                                    <td>
                                        <form method="post" action="${ctx}/adminpickup" 
                                              onsubmit="return confirm('Xác nhận khách đã nhận xe?');">
                                            <input type="hidden" name="orderId" value="${o[0]}">
                                            <button type="submit" class="btn btn-sm btn-primary">
                                                <i class="fas fa-check"></i> Đã Nhận Xe
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </div>
    </main>

    <script src="https://kit.fontawesome.com/a076d05399.js" crossorigin="anonymous"></script>
</body>
</html>