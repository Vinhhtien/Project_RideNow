<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Trả Xe - RideNow Admin</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${ctx}/css/admin.css">
    <style>
        .empty-state { text-align: center; padding: 40px; color: #666; }
        .empty-state i { font-size: 48px; margin-bottom: 16px; color: #ccc; }
    </style>
</head>
<body class="admin">
    <aside class="sidebar">
        <div class="brand">RideNow Admin</div>
        <nav>
            <a href="${ctx}/admindashboard">Dashboard</a>
            <a href="${ctx}/adminpaymentverify">Xác Minh Thanh Toán</a>
            <a href="${ctx}/adminpickup">Giao Nhận Xe</a>
            <a class="active" href="${ctx}/adminreturn">Trả Xe</a>
            <a href="${ctx}/adminreturns">Hoàn Cọc</a>
            <a href="${ctx}/adminwithdrawals">Rút Tiền</a>
            <a href="${ctx}/logout">Logout</a>
        </nav>
    </aside>

    <main class="content">
        <h1>Trả Xe</h1>

        <c:if test="${not empty sessionScope.flash}">
            <div class="notice">${sessionScope.flash}</div>
            <c:remove var="flash" scope="session"/>
        </c:if>

        <div class="panel">
            <div class="panel-head">
                <h2>Đơn Hàng Đang Thuê</h2>
            </div>

            <c:choose>
                <c:when test="${empty activeOrders}">
                    <div class="empty-state">
                        <i class="fas fa-clipboard-check"></i>
                        <h3>Không có đơn hàng nào đang thuê</h3>
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
                                <th>Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="o" items="${activeOrders}">
                                <tr>
                                    <td>#${o[0]}</td>
                                    <td>
                                        ${o[1]}<br>
                                        <small class="text-muted">${o[2]}</small>
                                    </td>
                                    <td>${o[4]}</td>
                                    <td>
                                        <fmt:formatDate value="${o[5]}" pattern="dd/MM/yyyy"/> - 
                                        <fmt:formatDate value="${o[6]}" pattern="dd/MM/yyyy"/>
                                    </td>
                                    <td><fmt:formatNumber value="${o[7]}" type="currency"/></td>
                                    <td><fmt:formatNumber value="${o[8]}" type="currency"/></td>
                                    <td>
                                        <form method="post" action="${ctx}/adminreturn" 
                                              onsubmit="return confirm('Xác nhận khách đã trả xe?');">
                                            <input type="hidden" name="orderId" value="${o[0]}">
                                            <button type="submit" class="btn btn-sm btn-primary">
                                                <i class="fas fa-check"></i> Đã Trả Xe
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