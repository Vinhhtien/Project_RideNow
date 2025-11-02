<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Đánh Giá Cửa Hàng - RideNow</title>
    
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${ctx}/css/homeStyle.css"/>
    
    <style>
        /* Đảm bảo font chữ Inter được áp dụng */
        body, h1, h2, h3, h4, h5, h6, p, span, div, a, button, input, textarea, select, label {
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif !important;
        }
        
        /* LOẠI BỎ GẠCH CHÂN CHO TẤT CẢ CÁC LIÊN KẾT */
        a, a:hover, a:focus, a:visited {
            text-decoration: none !important;
        }
        
        /* Đảm bảo các nút và liên kết không có gạch chân */
        .btn, .btn:hover, .btn:focus,
        .btn--ghost, .btn--ghost:hover,
        .btn--solid, .btn--solid:hover,
        .btn-primary-custom, .btn-primary-custom:hover,
        .btn-secondary-custom, .btn-secondary-custom:hover,
        .nav-link, .nav-link:hover,
        .dropdown a, .dropdown a:hover,
        .mobile-nav a, .mobile-nav a:hover,
        .mobile-actions a, .mobile-actions a:hover,
        .footer-links a, .footer-links a:hover,
        .social-links a, .social-links a:hover,
        .review-form-container a, .review-form-container a:hover,
        .login-prompt a, .login-prompt a:hover {
            text-decoration: none !important;
        }

        .review-section {
            padding: 120px 0 80px;
            background: linear-gradient(135deg, var(--primary-dark) 0%, var(--dark) 100%);
            min-height: 100vh;
            color: var(--light);
        }
        
        .review-form-container {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(20px);
            -webkit-backdrop-filter: blur(20px);
            border-radius: 20px;
            padding: 40px;
            margin-bottom: 50px;
            border: 1px solid rgba(59, 130, 246, 0.2);
            box-shadow: 0 15px 35px rgba(0, 0, 0, 0.5);
        }
        
        .review-form-container h2 {
            color: var(--accent);
            text-shadow: 0 0 15px rgba(59, 130, 246, 0.3);
        }
        
        .user-review-badge {
            background: linear-gradient(135deg, var(--accent), var(--accent-dark));
            color: white;
            padding: 8px 16px;
            border-radius: 20px;
            font-size: 14px;
            font-weight: 600;
            display: inline-block;
            margin-bottom: 20px;
        }
        
        .rating-stars {
            font-size: 32px;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        
        .star {
            color: var(--gray-dark);
            cursor: pointer;
            transition: all 0.3s ease;
            text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
        }
        
        .star:hover,
        .star.filled {
            color: #FFD700;
            text-shadow: 0 0 15px rgba(255, 215, 0, 0.5);
            transform: scale(1.2);
        }
        
        .form-label {
            color: var(--accent-light);
            font-weight: 600;
            margin-bottom: 10px;
        }
        
        .form-control, .form-select {
            background: rgba(11, 11, 13, 0.8) !important;
            border: 1px solid var(--primary-light) !important;
            color: var(--light) !important;
            border-radius: 10px !important;
            padding: 12px 16px !important;
            transition: all 0.3s ease !important;
        }
        
        .form-control:focus, .form-select:focus {
            border-color: var(--accent) !important;
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2) !important;
            background: rgba(11, 11, 13, 0.9) !important;
        }
        
        .form-control::placeholder {
            color: var(--gray) !important;
        }
        
        .form-text {
            color: var(--gray-light) !important;
        }
        
        .existing-reviews {
            margin-top: 60px;
        }
        
        .existing-reviews h3 {
            color: var(--accent);
            text-shadow: 0 0 15px rgba(59, 130, 246, 0.3);
            margin-bottom: 30px;
        }
        
        .review-card {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(15px);
            -webkit-backdrop-filter: blur(15px);
            border-radius: 15px;
            padding: 30px;
            margin-bottom: 25px;
            border: 1px solid rgba(59, 130, 246, 0.2);
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
            transition: all 0.3s ease;
            position: relative;
        }
        
        .review-card:hover {
            transform: translateY(-5px);
            border-color: rgba(59, 130, 246, 0.4);
            box-shadow: 0 15px 35px rgba(0, 0, 0, 0.4);
        }
        
        .user-review-indicator {
            position: absolute;
            top: 15px;
            right: 15px;
            background: var(--accent);
            color: white;
            padding: 4px 8px;
            border-radius: 10px;
            font-size: 12px;
            font-weight: 600;
        }
        
        .review-comment {
            color: var(--light);
            font-style: italic;
            line-height: 1.6;
            margin-bottom: 20px;
        }
        
        .customer-info {
            border-top: 1px solid rgba(255, 255, 255, 0.1);
            padding-top: 15px;
        }
        
        .customer-name {
            color: var(--accent-light);
            font-weight: 600;
        }
        
        .no-reviews-content {
            background: rgba(255, 255, 255, 0.05);
            border-radius: 15px;
            padding: 50px 30px;
            border: 1px solid rgba(255, 255, 255, 0.1);
            text-align: center;
        }
        
        .no-reviews-content i {
            color: var(--accent);
            font-size: 4rem;
            margin-bottom: 20px;
            opacity: 0.7;
        }
        
        .no-reviews-content h5 {
            color: var(--accent-light);
            margin-bottom: 10px;
        }
        
        .no-reviews-content p {
            color: var(--gray-light);
            margin-bottom: 0;
        }
        
        /* Button styles matching home.jsp */
        .btn-primary-custom {
            background: linear-gradient(135deg, var(--accent), var(--accent-dark));
            color: var(--white);
            border: none;
            border-radius: 50px;
            padding: 12px 30px;
            font-weight: 600;
            transition: all 0.3s ease;
            box-shadow: var(--shadow-md);
        }
        
        .btn-primary-custom:hover {
            background: linear-gradient(135deg, var(--accent-dark), var(--accent));
            transform: translateY(-2px);
            box-shadow: var(--shadow-lg);
        }
        
        .btn-secondary-custom {
            background: transparent;
            color: var(--light);
            border: 1px solid var(--primary-light);
            border-radius: 50px;
            padding: 12px 30px;
            font-weight: 600;
            transition: all 0.3s ease;
        }
        
        .btn-secondary-custom:hover {
            background: var(--primary-light);
            color: var(--accent);
            border-color: var(--accent);
            transform: translateY(-2px);
        }
        
        .btn-edit-custom {
            background: transparent;
            color: var(--accent-light);
            border: 1px solid var(--accent-light);
            border-radius: 50px;
            padding: 8px 20px;
            font-weight: 600;
            transition: all 0.3s ease;
            font-size: 14px;
        }
        
        .btn-edit-custom:hover {
            background: var(--accent-light);
            color: var(--white);
            transform: translateY(-2px);
        }
        
        /* Login prompt styles */
        .login-prompt {
            background: rgba(255, 255, 255, 0.05);
            border-radius: 15px;
            padding: 50px 30px;
            border: 1px solid rgba(255, 255, 255, 0.1);
            text-align: center;
        }
        
        .login-prompt i {
            color: var(--accent);
            font-size: 4rem;
            margin-bottom: 20px;
        }
        
        .login-prompt h4 {
            color: var(--accent-light);
            margin-bottom: 15px;
        }
        
        .login-prompt p {
            color: var(--gray-light);
            margin-bottom: 25px;
        }
        
        /* Alert customization */
        .alert-success {
            background: rgba(34, 197, 94, 0.1);
            border: 1px solid rgba(34, 197, 94, 0.3);
            color: var(--light);
            backdrop-filter: blur(10px);
            border-radius: 10px;
        }
        
        .alert-info {
            background: rgba(59, 130, 246, 0.1);
            border: 1px solid rgba(59, 130, 246, 0.3);
            color: var(--light);
            backdrop-filter: blur(10px);
            border-radius: 10px;
        }
        
        /* Responsive */
        @media (max-width: 768px) {
            .review-section {
                padding: 100px 0 60px;
            }
            
            .review-form-container {
                padding: 30px 20px;
            }
            
            .rating-stars {
                font-size: 28px;
            }
            
            .review-card {
                padding: 20px 15px;
            }
        }
    </style>
</head>
<body>
<%@ include file="/chatbox.jsp" %>

<!-- ===== HEADER ===== -->
<header id="header">
    <div class="header-top">
        <div class="container">
            <div class="header-content">

                <!-- Logo -->
                <a href="${ctx}/" class="brand">
                    <img src="${ctx}/images/ridenow_Logo.jpg" alt="RideNow Logo">
                    <span class="brand-name">RideNow</span>
                </a>

                <!-- NAV -->
                <nav id="mainNav" aria-label="Điều hướng chính">
                    <ul>
                        <li><a href="${ctx}/home" class="nav-link">Trang chủ</a></li>
                        <li class="nav-item">
                            <a href="${ctx}/home#categories" class="nav-link">Loại xe <i class="fa-solid fa-chevron-down" style="font-size:12px"></i></a>
                            <div class="dropdown" aria-label="Chọn loại xe">
                                <a href="${ctx}/motorbikesearch?type_id=1">Xe số</a>
                                <a href="${ctx}/motorbikesearch?type_id=2">Xe ga</a>
                                <a href="${ctx}/motorbikesearch?type_id=3">Xe PKL</a>
                            </div>
                        </li>
                        <li><a href="${ctx}/home#how-it-works" class="nav-link">Cách thuê</a></li>
                        <li><a href="${ctx}/home#testimonials" class="nav-link active">Đánh giá</a></li>
                        <li><a href="${ctx}/home#contact" class="nav-link">Liên hệ</a></li>
                    </ul>
                </nav>

                <!-- AUTH -->
                <div class="auth" id="authDesktop">
                    <a href="${ctx}/cart" class="btn btn--ghost"><i class="fas fa-shopping-cart"></i> Giỏ hàng</a>

                    <c:choose>
                        <c:when test="${not empty sessionScope.account}">
                            <c:choose>
                                <c:when test="${sessionScope.account.role == 'customer'}">
                                    <a href="${ctx}/customer/profile" class="btn btn--ghost" title="Chỉnh sửa hồ sơ">
                                        <i class="fas fa-user-circle"></i> Xin chào,
                                        <strong>${sessionScope.account.username}</strong>
                                    </a>
                                </c:when>
                            </c:choose>

                            <a href="${ctx}/logout" class="btn btn--ghost">
                                <i class="fas fa-right-from-bracket"></i> Đăng xuất
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a href="${ctx}/login" class="btn btn--ghost">
                                <i class="fas fa-user"></i> Đăng nhập
                            </a>
                            <a href="${ctx}/register" class="btn btn--solid">Đăng ký</a>
                        </c:otherwise>
                    </c:choose>
                </div>

                <button class="mobile-toggle" id="mobileToggle" aria-label="Mở menu" aria-expanded="false">
                    <i class="fas fa-bars"></i>
                </button>
            </div>
        </div>
    </div>

    <!-- Mobile panel -->
    <div class="mobile-panel" id="mobilePanel">
        <div class="mobile-actions">
            <a href="${ctx}/cart"><i class="fas fa-shopping-cart"></i> Giỏ hàng</a>
            <div style="display:flex;gap:10px">
                <c:choose>
                    <c:when test="${not empty sessionScope.account}">
                        <c:if test="${sessionScope.account.role == 'admin' || sessionScope.account.role == 'partner'}">
                            <a href="${ctx}/motorbikemanagelist"><i class="fas fa-screwdriver-wrench"></i> Quản lý xe</a>
                        </c:if>
                        <a href="${ctx}/logout"><i class="fas fa-right-from-bracket"></i> Đăng xuất</a>
                    </c:when>
                    <c:otherwise>
                        <a href="${ctx}/login"><i class="fas fa-user"></i> Đăng nhập</a>
                        <a href="${ctx}/register" style="background:var(--primary);padding:8px 14px;border-radius:6px;color:#fff">Đăng ký</a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div class="mobile-search">
            <form id="mobileSearch" action="${ctx}/motorbikesearch" method="get" style="display:grid;gap:12px">
                <div>
                    <label for="type_id_m" style="font-size:12px;color:#cbd5e1;margin-left:2px">Loại xe</label>
                    <select id="type_id_m" name="type_id" class="control" style="height:44px;background:#0b1224;color:#e2e8f0;border:1px solid #334155;">
                        <option value="">-- Tất cả --</option>
                        <option value="1">Xe số</option>
                        <option value="2">Xe ga</option>
                        <option value="3">Phân khối lớn</option>
                    </select>
                </div>
            </form>
        </div>

        <div class="mobile-nav">
            <a href="${ctx}/home"><i class="fas fa-home"></i> Trang chủ</a>
            <a href="${ctx}/motorbikesearch?type_id=1"><i class="fas fa-motorcycle"></i> Xe số</a>
            <a href="${ctx}/motorbikesearch?type_id=2"><i class="fas fa-motorcycle"></i> Xe ga</a>
            <a href="${ctx}/motorbikesearch?type_id=3"><i class="fas fa-motorcycle"></i> Xe PKL</a>
            <a href="${ctx}/home#how-it-works"><i class="fas fa-question-circle"></i> Cách thuê</a>
            <a href="${ctx}/home#testimonials"><i class="fas fa-star"></i> Đánh giá</a>
            <a href="${ctx}/home#contact"><i class="fas fa-phone"></i> Liên hệ</a>
        </div>
    </div>
</header>

<!-- ===== REVIEW SECTION ===== -->
<section class="review-section">
    <div class="container">
        <!-- Thông báo -->
        <c:if test="${not empty sessionScope.message}">
            <div class="alert alert-success alert-dismissible fade show mb-5" role="alert">
                ${sessionScope.message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <c:remove var="message" scope="session"/>
        </c:if>

        <!-- Form đánh giá -->
        <div class="review-form-container">
            <h2 class="text-center mb-4">Đánh Giá Cửa Hàng</h2>
            
            <c:choose>
                <c:when test="${not empty sessionScope.account and sessionScope.account.role == 'customer'}">
                    <c:choose>
                        <c:when test="${not empty userReview}">
                            <!-- Hiển thị form chỉnh sửa nếu user đã có đánh giá -->
                            <div class="user-review-badge">
                                <i class="fas fa-edit me-2"></i>Bạn đã đánh giá cửa hàng
                            </div>
                            
                            <form action="${ctx}/storereview" method="post" id="reviewForm">
                                <input type="hidden" name="action" value="update">
                                <input type="hidden" name="storeReviewId" value="${userReview.storeReviewId}">
                                
                                <div class="mb-4">
                                    <label class="form-label">Đánh giá sao:</label>
                                    <div class="rating-stars mb-3" id="ratingStars">
                                        <c:forEach begin="1" end="5" var="star">
                                            <span class="star ${star <= userReview.rating ? 'filled' : ''}" 
                                                  data-rating="${star}">★</span>
                                        </c:forEach>
                                    </div>
                                    <input type="hidden" name="rating" id="ratingInput" value="${userReview.rating}" required>
                                    <small class="form-text">Chọn số sao để cập nhật đánh giá (1-5 sao)</small>
                                </div>
                                
                                <div class="mb-4">
                                    <label for="comment" class="form-label">Nhận xét của bạn:</label>
                                    <textarea class="form-control" id="comment" name="comment" rows="5" 
                                              placeholder="Chia sẻ trải nghiệm của bạn với cửa hàng..." 
                                              maxlength="500" required>${userReview.comment}</textarea>
<!--                                    <div class="form-text">
                                        <span id="charCount">${fn:length(userReview.comment)}</span>/500 ký tự
                                    </div>-->
                                </div>
                                
                                <div class="text-center">
                                    <button type="submit" class="btn-primary-custom me-3">
                                        <i class="fas fa-sync-alt me-2"></i>Cập Nhật Đánh Giá
                                    </button>
                                    <a href="${ctx}/home" class="btn-secondary-custom">
                                        <i class="fas fa-home me-2"></i>Về Trang Chủ
                                    </a>
                                </div>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <!-- Hiển thị form tạo mới nếu user chưa có đánh giá -->
                            <form action="${ctx}/storereview" method="post" id="reviewForm">
                                <input type="hidden" name="action" value="create">
                                
                                <div class="mb-4">
                                    <label class="form-label">Đánh giá sao:</label>
                                    <div class="rating-stars mb-3" id="ratingStars">
                                        <span class="star" data-rating="1">★</span>
                                        <span class="star" data-rating="2">★</span>
                                        <span class="star" data-rating="3">★</span>
                                        <span class="star" data-rating="4">★</span>
                                        <span class="star" data-rating="5">★</span>
                                    </div>
                                    <input type="hidden" name="rating" id="ratingInput" required>
                                    <small class="form-text">Chọn số sao để đánh giá (1-5 sao)</small>
                                </div>
                                
                                <div class="mb-4">
                                    <label for="comment" class="form-label">Nhận xét của bạn:</label>
                                    <textarea class="form-control" id="comment" name="comment" rows="5" 
                                              placeholder="Chia sẻ trải nghiệm của bạn với cửa hàng..." 
                                              maxlength="500" required></textarea>
                                    <div class="form-text">
                                        <span id="charCount">0</span>/500 ký tự
                                    </div>
                                </div>
                                
                                <div class="text-center">
                                    <button type="submit" class="btn-primary-custom me-3">
                                        <i class="fas fa-paper-plane me-2"></i>Gửi Đánh Giá
                                    </button>
                                    <a href="${ctx}/home" class="btn-secondary-custom">
                                        <i class="fas fa-home me-2"></i>Về Trang Chủ
                                    </a>
                                </div>
                            </form>
                        </c:otherwise>
                    </c:choose>
                </c:when>
                
                <c:otherwise>
                    <div class="login-prompt">
                        <div class="mb-4">
                            <i class="fas fa-user-lock"></i>
                        </div>
                        <h4 class="mb-3">Vui lòng đăng nhập để đánh giá</h4>
                        <p class="mb-4">Bạn cần đăng nhập bằng tài khoản khách hàng để gửi đánh giá.</p>
                        <a href="${ctx}/login" class="btn-primary-custom me-3">
                            <i class="fas fa-sign-in-alt me-2"></i>Đăng Nhập
                        </a>
                        <a href="${ctx}/home" class="btn-secondary-custom">
                            <i class="fas fa-home me-2"></i>Về Trang Chủ
                        </a>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- Hiển thị đánh giá hiện có -->
        <div class="existing-reviews">
            <h3 class="text-center mb-4">Đánh Giá Từ Khách Hàng</h3>
            
            <c:choose>
                <c:when test="${not empty reviews}">
                    <c:forEach items="${reviews}" var="review">
                        <div class="review-card">
                            <!-- Chỉ báo đánh giá của user hiện tại -->
                            <c:if test="${not empty sessionScope.account and sessionScope.account.accountId == review.customerId}">
                                <div class="user-review-indicator">
                                    <i class="fas fa-user me-1"></i>Đánh giá của bạn
                                </div>
                            </c:if>
                            
                            <div class="d-flex justify-content-between align-items-start mb-3">
                                <div class="rating-stars">
                                    <c:forEach begin="1" end="5" var="star">
                                        <c:choose>
                                            <c:when test="${star <= review.rating}">
                                                <span class="star filled">★</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="star">★</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                    <span class="ms-2 form-text">(${review.rating}/5)</span>
                                </div>
                                <c:if test="${not empty review.createdAt}">
                                    <small class="form-text">
                                        <fmt:formatDate value="${review.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                                    </small>
                                </c:if>
                            </div>
                            
                            <p class="review-comment">"${review.comment}"</p>
                            
                            <div class="customer-info">
                                <strong class="customer-name">
                                    <c:choose>
                                        <c:when test="${not empty review.customerName}">
                                            ${review.customerName}
                                        </c:when>
                                        <c:otherwise>
                                            Khách hàng #${review.customerId}
                                        </c:otherwise>
                                    </c:choose>
                                </strong>
                            </div>
                        </div>
                    </c:forEach>
                </c:when>
                
                <c:otherwise>
                    <div class="no-reviews-content">
                        <i class="fas fa-comments mb-3"></i>
                        <h5>Chưa có đánh giá nào</h5>
                        <p>Hãy là người đầu tiên đánh giá cửa hàng!</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</section>

<!-- ===== FOOTER ===== -->
<footer id="contact" class="site-footer" role="contentinfo">
    <div class="container">
        <div class="footer-content">
            <div class="footer-about fade-in">
                <div class="footer-logo"><i class="fas fa-motorcycle"></i> RideNow</div>
                <p>Dịch vụ cho thuê xe máy hàng đầu Việt Nam với chất lượng tốt và giá cạnh tranh. Cam kết mang đến trải nghiệm thuê xe tuyệt vời nhất.</p>
                <div class="social-links">
                    <a href="#"><i class="fab fa-facebook-f"></i></a>
                    <a href="#"><i class="fab fa-instagram"></i></a>
                    <a href="#"><i class="fab fa-twitter"></i></a>
                    <a href="#"><i class="fab fa-youtube"></i></a>
                </div>
            </div>

            <div class="footer-links fade-in delay-1">
                <h3>Liên Kết Nhanh</h3>
                <ul>
                    <li><a href="${ctx}/home"><i class="fas fa-chevron-right"></i> Trang chủ</a></li>
                    <li><a href="${ctx}/home#categories"><i class="fas fa-chevron-right"></i> Loại xe</a></li>
                    <li><a href="${ctx}/home#how-it-works"><i class="fas fa-chevron-right"></i> Cách thuê</a></li>
                    <li><a href="${ctx}/home#testimonials"><i class="fas fa-chevron-right"></i> Đánh giá</a></li>
                    <li><a href="${ctx}/home#contact"><i class="fas fa-chevron-right"></i> Liên hệ</a></li>
                </ul>
            </div>

            <div class="footer-links fade-in delay-2">
                <h3>Hỗ Trợ</h3>
                <ul>
                    <li><a href="#"><i class="fas fa-chevron-right"></i> Câu hỏi thường gặp</a></li>
                    <li><a href="#"><i class="fas fa-chevron-right"></i> Chính sách bảo mật</a></li>
                    <li><a href="#"><i class="fas fa-chevron-right"></i> Điều khoản sử dụng</a></li>
                    <li><a href="#"><i class="fas fa-chevron-right"></i> Chính sách hoàn tiền</a></li>
                    <li><a href="#"><i class="fas fa-chevron-right"></i> Trung tâm hỗ trợ</a></li>
                </ul>
            </div>

            <div class="footer-newsletter fade-in delay-3">
                <h3>Đăng Ký Nhận Tin</h3>
                <p>Đăng ký để nhận thông tin khuyến mãi và ưu đãi đặc biệt từ RideNow.</p>
                <form class="newsletter-form">
                    <input type="email" placeholder="Email của bạn" required>
                    <button type="submit"><i class="fas fa-paper-plane"></i> Đăng ký</button>
                </form>
            </div>
        </div>
        <div class="footer-bottom"><p>© 2025 RideNow. Tất cả quyền được bảo lưu.</p></div>
    </div>
</footer>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

<script>
document.addEventListener('DOMContentLoaded', function() {
    // Xử lý rating stars
    const stars = document.querySelectorAll('.star[data-rating]');
    const ratingInput = document.getElementById('ratingInput');
    
    stars.forEach(star => {
        star.addEventListener('click', function() {
            const rating = this.getAttribute('data-rating');
            ratingInput.value = rating;
            
            // Cập nhật hiển thị sao
            stars.forEach(s => {
                if (s.getAttribute('data-rating') <= rating) {
                    s.classList.add('filled');
                } else {
                    s.classList.remove('filled');
                }
            });
        });
        
        // Thêm hiệu ứng hover
        star.addEventListener('mouseenter', function() {
            const rating = this.getAttribute('data-rating');
            stars.forEach(s => {
                if (s.getAttribute('data-rating') <= rating) {
                    s.style.transform = 'scale(1.1)';
                    s.style.color = '#FFD700';
                }
            });
        });
        
        star.addEventListener('mouseleave', function() {
            const currentRating = ratingInput.value;
            stars.forEach(s => {
                if (!currentRating || s.getAttribute('data-rating') > currentRating) {
                    s.style.transform = 'scale(1)';
                    if (!s.classList.contains('filled')) {
                        s.style.color = 'var(--gray-dark)';
                    }
                }
            });
        });
    });
    
    // Đếm ký tự comment
    const commentTextarea = document.getElementById('comment');
    const charCount = document.getElementById('charCount');
    
    if (commentTextarea && charCount) {
        commentTextarea.addEventListener('input', function() {
            charCount.textContent = this.value.length;
            
            // Thêm cảnh báo khi gần đạt giới hạn
            if (this.value.length > 450) {
                charCount.style.color = '#ef4444';
            } else if (this.value.length > 400) {
                charCount.style.color = '#f59e0b';
            } else {
                charCount.style.color = 'var(--gray-light)';
            }
        });
        
        // Khởi tạo đếm ký tự nếu có giá trị ban đầu (trong trường hợp chỉnh sửa)
        if (commentTextarea.value) {
            charCount.textContent = commentTextarea.value.length;
            if (commentTextarea.value.length > 450) {
                charCount.style.color = '#ef4444';
            } else if (commentTextarea.value.length > 400) {
                charCount.style.color = '#f59e0b';
            }
        }
    }
    
    // Validate form
    const reviewForm = document.getElementById('reviewForm');
    if (reviewForm) {
        reviewForm.addEventListener('submit', function(e) {
            if (!ratingInput.value) {
                e.preventDefault();
                alert('Vui lòng chọn số sao đánh giá!');
                return false;
            }
            
            if (!commentTextarea.value.trim()) {
                e.preventDefault();
                alert('Vui lòng nhập nội dung đánh giá!');
                return false;
            }
        });
    }
    
    // Auto close alert after 5 seconds
    const alert = document.querySelector('.alert');
    if (alert) {
        setTimeout(() => {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    }
    
    // Mobile menu functionality (từ home.jsp)
    const $ = (sel) => document.querySelector(sel);
    const $$ = (sel) => document.querySelectorAll(sel);

    const mobileToggle = $('#mobileToggle');
    const mobilePanel = $('#mobilePanel');
    if (mobileToggle && mobilePanel) {
        mobileToggle.addEventListener('click', () => {
            const open = mobilePanel.style.display === 'block';
            mobilePanel.style.display = open ? 'none' : 'block';
            mobileToggle.setAttribute('aria-expanded', String(!open));
        });
    }

    // Smooth scroll cho internal links
    $$('a[href^="#"]').forEach(a => {
        a.addEventListener('click', e => {
            const id = a.getAttribute('href');
            if (!id || id === '#') return;
            const target = $(id);
            if (target) {
                e.preventDefault();
                window.scrollTo({top: target.offsetTop - 90, behavior: 'smooth'});
                if (mobilePanel) {
                    mobilePanel.style.display = 'none';
                    if (mobileToggle) {
                        mobileToggle.setAttribute('aria-expanded', 'false');
                    }
                }
            }
        });
    });
});
</script>
</body>
</html>