<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Partner Dashboard - Bike Rental System</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }

        .container {
            max-width: 1400px;
            margin: 0 auto;
        }

        .header {
            background: white;
            padding: 25px 30px;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.1);
            margin-bottom: 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .header h1 {
            color: #667eea;
            font-size: 28px;
        }

        .user-info {
            display: flex;
            align-items: center;
            gap: 15px;
        }

        .avatar {
            width: 50px;
            height: 50px;
            border-radius: 50%;
            background: linear-gradient(135deg, #667eea, #764ba2);
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-weight: bold;
            font-size: 20px;
        }

        .logout-btn {
            background: #ff4757;
            color: white;
            border: none;
            padding: 10px 20px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 14px;
            transition: all 0.3s;
        }

        .logout-btn:hover {
            background: #ff3838;
            transform: translateY(-2px);
        }

        .dashboard-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 25px;
            margin-bottom: 30px;
        }

        .card {
            background: white;
            padding: 25px;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.1);
            transition: all 0.3s;
            cursor: pointer;
        }

        .card:hover {
            transform: translateY(-5px);
            box-shadow: 0 8px 30px rgba(0,0,0,0.15);
        }

        .card-icon {
            width: 60px;
            height: 60px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 30px;
            margin-bottom: 15px;
        }

        .card-title {
            font-size: 20px;
            color: #2c3e50;
            margin-bottom: 10px;
            font-weight: 600;
        }

        .card-description {
            color: #7f8c8d;
            font-size: 14px;
            line-height: 1.6;
        }

        .icon-purple { background: linear-gradient(135deg, #667eea, #764ba2); }
        .icon-blue { background: linear-gradient(135deg, #4facfe, #00f2fe); }
        .icon-green { background: linear-gradient(135deg, #43e97b, #38f9d7); }
        .icon-orange { background: linear-gradient(135deg, #fa709a, #fee140); }
        .icon-red { background: linear-gradient(135deg, #ff6b6b, #ee5a6f); }
        .icon-teal { background: linear-gradient(135deg, #2ecc71, #1abc9c); }

        .stats-section {
            background: white;
            padding: 30px;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.1);
            margin-bottom: 30px;
        }

        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-top: 20px;
        }

        .stat-box {
            padding: 20px;
            border-radius: 10px;
            text-align: center;
        }

        .stat-box h3 {
            font-size: 32px;
            margin-bottom: 5px;
        }

        .stat-box p {
            color: #7f8c8d;
            font-size: 14px;
        }

        .notifications {
            background: white;
            padding: 25px;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.1);
        }

        .notification-item {
            padding: 15px;
            border-left: 4px solid #667eea;
            background: #f8f9fa;
            margin-bottom: 15px;
            border-radius: 5px;
        }

        .notification-item:last-child {
            margin-bottom: 0;
        }

        .notification-time {
            color: #7f8c8d;
            font-size: 12px;
            margin-top: 5px;
        }

        @media (max-width: 768px) {
            .header {
                flex-direction: column;
                gap: 15px;
                text-align: center;
            }

            .dashboard-grid {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="header">
            <div>
                <h1>🚲 Partner Dashboard</h1>
                <p style="color: #7f8c8d; margin-top: 5px;">Chào mừng trở lại, ${sessionScope.partnerName != null ? sessionScope.partnerName : 'Partner'}</p>
            </div>
            <div class="user-info">
                <div class="avatar">P</div>
                <div>
                    <strong>${sessionScope.partnerName != null ? sessionScope.partnerName : 'Partner Name'}</strong>
                    <p style="color: #7f8c8d; font-size: 12px;">ID: ${sessionScope.partnerId != null ? sessionScope.partnerId : 'P001'}</p>
                </div>
                <button class="logout-btn" onclick="logout()">Đăng xuất</button>
            </div>
        </div>

        <!-- Statistics Section -->
        <div class="stats-section">
            <h2 style="color: #2c3e50; margin-bottom: 10px;">📊 Thống kê tổng quan</h2>
            <div class="stats-grid">
                <div class="stat-box icon-purple">
                    <h3>${totalBikes != null ? totalBikes : 12}</h3>
                    <p>Tổng số xe</p>
                </div>
                <div class="stat-box icon-blue">
                    <h3>${bikesRented != null ? bikesRented : 5}</h3>
                    <p>Xe đang cho thuê</p>
                </div>
                <div class="stat-box icon-green">
                    <h3>${bikesAvailable != null ? bikesAvailable : 7}</h3>
                    <p>Xe khả dụng</p>
                </div>
                <div class="stat-box icon-orange">
                    <h3>${totalReviews != null ? totalReviews : 24}</h3>
                    <p>Đánh giá</p>
                </div>
            </div>
        </div>

        <!-- Main Dashboard Grid -->
        <div class="dashboard-grid">
            <!-- Update Account Information -->
            <div class="card" onclick="location.href='${pageContext.request.contextPath}/partner/update-account'">
                <div class="card-icon icon-purple">👤</div>
                <h3 class="card-title">Cập nhật thông tin tài khoản</h3>
                <p class="card-description">Chỉnh sửa thông tin cá nhân, địa chỉ, số điện thoại và các thông tin liên hệ</p>
            </div>

            <!-- View Bike Details -->
            <div class="card" onclick="location.href='${pageContext.request.contextPath}/partner/bike-details'">
                <div class="card-icon icon-blue">🔍</div>
                <h3 class="card-title">Xem chi tiết xe</h3>
                <p class="card-description">Xem thông tin chi tiết về từng xe đạp trong hệ thống của bạn</p>
            </div>

            <div class="card" onclick="location.href='${pageContext.request.contextPath}/viewmotorbike'">
    <div class="card-icon icon-green">🚴</div>
    <h3 class="card-title">Danh sách xe của tôi</h3>
    <p class="card-description">Quản lý tất cả xe, thêm xe mới hoặc cập nhật thông tin xe</p>
</div>


            <!-- View Rental History -->
            <div class="card" onclick="location.href='${pageContext.request.contextPath}/partner/rental-history'">
                <div class="card-icon icon-orange">📜</div>
                <h3 class="card-title">Lịch sử cho thuê</h3>
                <p class="card-description">Xem lịch sử tất cả các giao dịch cho thuê xe và doanh thu</p>
            </div>

            <!-- View Reviews -->
            <div class="card" onclick="location.href='${pageContext.request.contextPath}/partner/reviews'">
                <div class="card-icon icon-red">⭐</div>
                <h3 class="card-title">Xem đánh giá xe</h3>
                <p class="card-description">Xem và phản hồi các đánh giá của khách hàng về xe của bạn</p>
            </div>

            <!-- Notifications -->
            <div class="card" onclick="location.href='${pageContext.request.contextPath}/partner/notifications'">
                <div class="card-icon icon-teal">🔔</div>
                <h3 class="card-title">Thông báo</h3>
                <p class="card-description">Nhận thông báo về đơn thuê mới, đánh giá và cập nhật hệ thống</p>
            </div>
        </div>

        <!-- Recent Notifications -->
        <div class="notifications">
            <h2 style="color: #2c3e50; margin-bottom: 15px;">🔔 Thông báo mới nhất</h2>
            <c:choose>
                <c:when test="${not empty notifications}">
                    <c:forEach items="${notifications}" var="notification">
                        <div class="notification-item">
                            <strong>${notification.title}</strong>
                            <p>${notification.message}</p>
                            <div class="notification-time">${notification.time}</div>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <div class="notification-item">
                        <strong>Đơn thuê mới</strong>
                        <p>Xe "Mountain Bike Pro" đã được thuê bởi Nguyễn Văn A</p>
                        <div class="notification-time">2 giờ trước</div>
                    </div>
                    <div class="notification-item">
                        <strong>Đánh giá mới</strong>
                        <p>Xe "City Cruiser" nhận được đánh giá 5 sao từ khách hàng</p>
                        <div class="notification-time">5 giờ trước</div>
                    </div>
                    <div class="notification-item">
                        <strong>Hoàn thành thuê xe</strong>
                        <p>Xe "Road Racer" đã được trả lại thành công</p>
                        <div class="notification-time">1 ngày trước</div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <script>
        function logout() {
            if (confirm('Bạn có chắc chắn muốn đăng xuất?')) {
                window.location.href = '${pageContext.request.contextPath}/logout';
            }
        }

        // Add smooth animations
        document.addEventListener('DOMContentLoaded', function() {
            const cards = document.querySelectorAll('.card');
            cards.forEach((card, index) => {
                setTimeout(() => {
                    card.style.opacity = '0';
                    card.style.transform = 'translateY(20px)';
                    card.style.transition = 'all 0.5s ease';
                    
                    setTimeout(() => {
                        card.style.opacity = '1';
                        card.style.transform = 'translateY(0)';
                    }, 50);
                }, index * 100);
            });
        });
    </script>
</body>
</html>