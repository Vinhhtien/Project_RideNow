<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page pageEncoding="UTF-8" %>
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
    <fmt:setLocale value="vi_VN" scope="session"/>
    
    <aside class="sidebar">
        <div class="brand">RideNow Admin</div>
        <nav>
            <a href="${pageContext.request.contextPath}/admin/dashboard">Dashboard</a>
            <a href="${pageContext.request.contextPath}/adminpaymentverify">Xác Minh Thanh Toán</a>
            <a href="${pageContext.request.contextPath}/adminpickup">Giao Nhận Xe</a>
            <a href="${pageContext.request.contextPath}/adminreturn" class="active">Trả Xe</a>
            <a href="${pageContext.request.contextPath}/adminreturns">Kiểm tra và Hoàn Cọc</a>
            <a href="${pageContext.request.contextPath}/logout">Đăng xuất</a>
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
                <p class="text-muted">Xác nhận khách hàng đã trả xe để bắt đầu quy trình hoàn cọc</p>
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
                                        <c:out value="${o[1]}" /><br>
                                        <small class="text-muted"><c:out value="${o[2]}" /></small>
                                    </td>
                                    <td><c:out value="${o[3]}" /></td>
                                    <td>
                                        <fmt:parseDate value="${o[4]}" pattern="yyyy-MM-dd" var="startDate"/>
                                        <fmt:parseDate value="${o[5]}" pattern="yyyy-MM-dd" var="endDate"/>
                                        <fmt:formatDate value="${startDate}" pattern="dd/MM/yyyy"/> - 
                                        <fmt:formatDate value="${endDate}" pattern="dd/MM/yyyy"/>
                                    </td>
                                    <td>
                                        <fmt:formatNumber value="${o[6]}" type="currency" currencyCode="VND"/>
                                    </td>
                                    <td>
                                        <fmt:formatNumber value="${o[7]}" type="currency" currencyCode="VND"/>
                                    </td>
                                    <td>
                                        <form method="post" action="${ctx}/adminreturn" 
                                              onsubmit="return confirm('Xác nhận khách đã trả xe? Đơn hàng sẽ chuyển sang trạng thái chờ kiểm tra để hoàn cọc.');">
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
    
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const notice = document.querySelector('.notice');
            if (notice) {
                setTimeout(() => {
                    notice.style.opacity = '0';
                    notice.style.transition = 'opacity 0.5s ease';
                    setTimeout(() => notice.remove(), 500);
                }, 5000);
            }
        });
    </script>
</body>
</html>