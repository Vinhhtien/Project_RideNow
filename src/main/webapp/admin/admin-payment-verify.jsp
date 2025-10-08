<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Xác Minh Thanh Toán - RideNow Admin</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${ctx}/css/admin.css">
    <style>
        .badge.pending { background: #fef3c7; color: #92400e; }
        .badge.paid { background: #dcfce7; color: #166534; }
        .empty-state { text-align: center; padding: 40px; color: #666; }
        .empty-state i { font-size: 48px; margin-bottom: 16px; color: #ccc; }
    </style>
</head>
<body class="admin">
    <aside class="sidebar">
        <div class="brand">RideNow Admin</div>
        <nav>
            <a href="${ctx}/admin/dashboard">Dashboard</a>
            <a class="active" href="${ctx}/adminpaymentverify">Xác Minh Thanh Toán</a>
            <a href="${ctx}/adminpickup">Giao Nhận Xe</a>
            <a href="${ctx}/adminreturn">Trả Xe</a>
            <a href="${ctx}/adminreturns">Hoàn Cọc</a>
            <a href="${ctx}/adminwithdrawals">Rút Tiền</a>
            <a href="${ctx}/logout">Logout</a>
        </nav>
    </aside>

    <main class="content">
        <h1>Xác Minh Thanh Toán</h1>

        <c:if test="${not empty sessionScope.flash}">
            <div class="notice">${sessionScope.flash}</div>
            <c:remove var="flash" scope="session"/>
        </c:if>

        <div class="panel">
            <div class="panel-head">
                <h2>Thanh Toán Đang Chờ Xác Minh</h2>
            </div>

            <c:choose>
                <c:when test="${empty payments}">
                    <div class="empty-state">
                        <i class="fas fa-check-circle"></i>
                        <h3>Không có thanh toán nào đang chờ</h3>
                        <p>Tất cả các thanh toán đã được xử lý</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <table class="table">
                        <thead>
                            <tr>
                                <th>Mã TT</th>
                                <th>Mã Đơn</th>
                                <th>Khách Hàng</th>
                                <th>Số Tiền</th>
                                <th>Phương Thức</th>
                                <th>Ngày TT</th>
                                <th>Trạng Thái</th>
                                <th>Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="p" items="${payments}">
                                <tr>
                                    <td>#${p[0]}</td>
                                    <td>#${p[1]}</td>
                                    <td>
                                        ${p[2]}<br>
                                        <small class="text-muted">${p[3]}</small>
                                    </td>
                                    <td><fmt:formatNumber value="${p[4]}" type="currency"/></td>
                                    <td>${p[5]}</td>
                                    <td><fmt:formatDate value="${p[6]}" pattern="dd/MM/yyyy HH:mm"/></td>
                                    <td><span class="badge pending">${p[7]}</span></td>
                                    <td>
                                        <form method="post" action="${pageContext.request.contextPath}/adminpaymentverify" 
                                              onsubmit="return confirm('Xác nhận đã kiểm tra và thanh toán thành công?');">
                                            <input type="hidden" name="paymentId" value="${p[0]}">
                                            <button type="submit" class="btn btn-sm btn-primary">
                                                <i class="fas fa-check"></i> Xác Nhận
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