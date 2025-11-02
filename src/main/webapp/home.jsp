<%--<%@ page contentType="text/html;charset=UTF-8" language="java" %>--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<c:if test="${empty requestScope.reviews}">
    <jsp:forward page="/home"/>
</c:if>


<!DOCTYPE html>
<html lang="vi">
    <head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>RideNow - Đặt Xe Máy Dễ Dàng</title>
    
    <!-- Bootstrap CSS - ĐẶT TRƯỚC custom CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    
    <!-- Custom CSS - ĐẶT SAU Bootstrap -->
    <link rel="stylesheet" href="css/homeStyle.css"/>
    
    <style>
        /* ===== Customer Reviews Improved Styles ===== */
        .customer-reviews {
            background: linear-gradient(135deg, var(--primary-dark) 0%, var(--dark) 100%);
            padding: 80px 0;
            color: var(--light);
            position: relative;
            border-top: 1px solid var(--primary-light);
            border-bottom: 1px solid var(--primary-light);
        }

        .review-card {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(20px);
            -webkit-backdrop-filter: blur(20px);
            border-radius: 20px;
            padding: 50px 40px;
            margin: 20px 60px;
            border: 1px solid rgba(59, 130, 246, 0.2);
            min-height: 280px;
            display: flex;
            flex-direction: column;
            justify-content: center;
            box-shadow: 0 15px 35px rgba(0, 0, 0, 0.5);
            transition: var(--transition);
            position: relative;
            overflow: hidden;
        }

        .review-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 3px;
            background: linear-gradient(90deg, var(--accent), var(--accent-light));
            transform: scaleX(0);
            transition: transform 0.3s ease;
        }

        .review-card:hover::before {
            transform: scaleX(1);
        }

        .review-card:hover {
            transform: translateY(-5px);
            border-color: rgba(59, 130, 246, 0.4);
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.6);
        }

        .rating-stars {
            font-size: 28px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 25px;
        }

        .star {
            color: var(--gray-dark);
            margin: 0 4px;
            text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
            transition: var(--transition);
        }

        .star.filled {
            color: #FFD700;
            text-shadow: 0 0 15px rgba(255, 215, 0, 0.5);
            transform: scale(1.1);
        }

        .rating-text {
            font-size: 16px;
            color: var(--gray-light);
            margin-left: 12px;
            font-weight: 600;
        }

        .review-comment {
            font-size: 20px;
            font-style: italic;
            line-height: 1.7;
            margin-bottom: 30px;
            color: var(--light);
            text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
        }

        .customer-info {
            border-top: 1px solid rgba(255, 255, 255, 0.1);
            padding-top: 20px;
        }

        .customer-name {
            display: block;
            font-size: 16px;
            margin-bottom: 5px;
            color: var(--accent-light);
            font-weight: 600;
        }

        .review-date {
            font-size: 14px;
            color: var(--gray);
        }

        .no-reviews {
            padding: 60px 40px;
            background: rgba(255, 255, 255, 0.05);
            border-radius: 20px;
            backdrop-filter: blur(10px);
            border: 1px solid rgba(255, 255, 255, 0.1);
        }

        .no-reviews i {
            color: var(--accent);
            margin-bottom: 20px;
        }

        .no-reviews h5 {
            color: var(--accent-light);
            margin-bottom: 10px;
        }

        .no-reviews p {
            font-size: 16px;
            margin-bottom: 0;
            color: var(--gray-light);
        }

        /* Carousel Customization - Improved */
        .carousel-indicators {
            bottom: -50px;
        }

        .carousel-indicators button {
            width: 10px !important;
            height: 10px !important;
            border-radius: 50% !important;
            margin: 0 8px !important;
            background-color: var(--gray-dark) !important;
            border: 2px solid transparent !important;
            transition: var(--transition);
        }

        .carousel-indicators button.active {
            background-color: var(--accent) !important;
            transform: scale(1.3);
            border-color: var(--accent-light) !important;
        }

        .carousel-control-prev,
        .carousel-control-next {
            width: 60px !important;
            height: 60px !important;
            top: 50% !important;
            transform: translateY(-50%);
            background: rgba(59, 130, 246, 0.1);
            border-radius: 50%;
            margin: 0 25px;
            border: 2px solid rgba(59, 130, 246, 0.3);
            transition: var(--transition);
        }

        .carousel-control-prev:hover,
        .carousel-control-next:hover {
            background: rgba(59, 130, 246, 0.2);
            border-color: var(--accent);
            transform: translateY(-50%) scale(1.1);
        }

        .carousel-control-prev {
            left: 10px !important;
        }

        .carousel-control-next {
            right: 10px !important;
        }

        .carousel-control-prev-icon,
        .carousel-control-next-icon {
            background-size: 18px 18px;
            background-color: var(--accent);
            border-radius: 50%;
            width: 30px;
            height: 30px;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        /* Nút đánh giá */
        .customer-reviews .btn--solid {
            background: linear-gradient(135deg, var(--accent), var(--accent-dark));
            color: var(--white);
            padding: 12px 30px;
            border-radius: 50px;
            font-weight: 600;
            text-decoration: none;
            transition: var(--transition);
            box-shadow: var(--shadow-md);
            border: none;
        }

        .customer-reviews .btn--solid:hover {
            background: linear-gradient(135deg, var(--accent-dark), var(--accent));
            transform: translateY(-2px);
            box-shadow: var(--shadow-lg);
        }

        /* Alert customization */
        .customer-reviews .alert {
            background: rgba(34, 197, 94, 0.1);
            border: 1px solid rgba(34, 197, 94, 0.3);
            color: var(--light);
            backdrop-filter: blur(10px);
        }

        /* Responsive */
        @media (max-width: 768px) {
            .customer-reviews {
                padding: 60px 0;
            }

            .review-card {
                padding: 30px 20px;
                margin: 10px 20px;
                min-height: 250px;
            }

            .rating-stars {
                font-size: 24px;
                margin-bottom: 20px;
            }

            .review-comment {
                font-size: 16px;
                margin-bottom: 20px;
            }

            .carousel-control-prev,
            .carousel-control-next {
                width: 50px !important;
                height: 50px !important;
                margin: 0 15px;
            }

            .carousel-indicators {
                bottom: -50px;
            }
        }

        @media (max-width: 576px) {
            .review-card {
                padding: 25px 15px;
                margin: 5px 10px;
                min-height: 220px;
            }

            .rating-stars {
                font-size: 20px;
            }

            .review-comment {
                font-size: 14px;
            }

            .carousel-control-prev,
            .carousel-control-next {
                width: 40px !important;
                height: 40px !important;
                margin: 0 10px;
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
                    <img src="images/ridenow_Logo.jpg" alt="RideNow Logo">
                    <span class="brand-name">RideNow</span>
                </a>

                <!-- NAV -->
                <nav id="mainNav" aria-label="Điều hướng chính">
                    <ul>
                        <li><a href="#home" class="nav-link active">Trang chủ</a></li>
                        <li class="nav-item">
                            <a href="#categories" class="nav-link">Loại xe <i class="fa-solid fa-chevron-down"
                                                                              style="font-size:12px"></i></a>
                            <div class="dropdown" aria-label="Chọn loại xe">
                                <a href="${pageContext.request.contextPath}/motorbikesearch?type_id=1">Xe số</a>
                                <a href="${pageContext.request.contextPath}/motorbikesearch?type_id=2">Xe ga</a>
                                <a href="${pageContext.request.contextPath}/motorbikesearch?type_id=3">Xe PKL</a>
                            </div>
                        </li>
                        <li><a href="#how-it-works" class="nav-link">Cách thuê</a></li>
                        <li><a href="#testimonials" class="nav-link">Đánh giá</a></li>
                        <li><a href="#contact" class="nav-link">Liên hệ</a></li>
                    </ul>
                </nav>

                <!-- AUTH -->
                <div class="auth" id="authDesktop">
                    <a href="${ctx}/cart" class="btn btn--ghost"><i class="fas fa-shopping-cart"></i> Giỏ hàng</a>

                    <c:choose>
                        <c:when test="${not empty sessionScope.account}">
                            <c:choose>
                                <c:when test="${sessionScope.account.role == 'customer'}">
                                    <a href="${pageContext.request.contextPath}/customer/profile" class="btn btn--ghost"
                                       title="Chỉnh sửa hồ sơ">
                                        <i class="fas fa-user-circle"></i> Xin chào,
                                        <strong>${sessionScope.account.username}</strong>
                                    </a>
                                </c:when>
                            </c:choose>

                            <a href="${pageContext.request.contextPath}/logout" class="btn btn--ghost">
                                <i class="fas fa-right-from-bracket"></i> Đăng xuất
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/login" class="btn btn--ghost">
                                <i class="fas fa-user"></i> Đăng nhập
                            </a>
                            <a href="${pageContext.request.contextPath}/register" class="btn btn--solid">Đăng ký</a>
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
            <a href="#"><i class="fas fa-shopping-cart"></i> Giỏ hàng</a>
            <div style="display:flex;gap:10px">
                <c:choose>
                    <c:when test="${not empty sessionScope.account}">
                        <c:if test="${sessionScope.account.role == 'admin' || sessionScope.account.role == 'partner'}">
                            <a href="${pageContext.request.contextPath}/motorbikemanagelist"><i
                                    class="fas fa-screwdriver-wrench"></i> Quản lý xe</a>
                        </c:if>
                        <a href="${pageContext.request.contextPath}/logout"><i class="fas fa-right-from-bracket"></i>
                            Đăng xuất</a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/login"><i class="fas fa-user"></i> Đăng nhập</a>
                        <a href="${pageContext.request.contextPath}/register"
                           style="background:var(--primary);padding:8px 14px;border-radius:6px;color:#fff">Đăng ký</a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div class="mobile-search">
            <form id="mobileSearch" action="${pageContext.request.contextPath}/motorbikesearch" method="get"
                  style="display:grid;gap:12px">
                <div>
                    <label for="type_id_m" style="font-size:12px;color:#cbd5e1;margin-left:2px">Loại xe</label>
                    <select id="type_id_m" name="type_id" class="control"
                            style="height:44px;background:#0b1224;color:#e2e8f0;border:1px solid #334155;">
                        <option value="">-- Tất cả --</option>
                        <option value="1">Xe số</option>
                        <option value="2">Xe ga</option>
                        <option value="3">Phân khối lớn</option>
                    </select>
                </div>
            </form>
        </div>

        <div class="mobile-nav">
            <a href="#home"><i class="fas fa-home"></i> Trang chủ</a>
            <a href="${pageContext.request.contextPath}/motorbikesearch?type_id=1"><i class="fas fa-motorcycle"></i> Xe
                số</a>
            <a href="${pageContext.request.contextPath}/motorbikesearch?type_id=2"><i class="fas fa-motorcycle"></i> Xe
                ga</a>
            <a href="${pageContext.request.contextPath}/motorbikesearch?type_id=3"><i class="fas fa-motorcycle"></i> Xe
                PKL</a>
            <a href="#how-it-works"><i class="fas fa-question-circle"></i> Cách thuê</a>
            <a href="#testimonials"><i class="fas fa-star"></i> Đánh giá</a>
            <a href="#contact"><i class="fas fa-phone"></i> Liên hệ</a>
        </div>
    </div>
</header>

<!-- ===== HERO ===== -->
<section class="hero hero--with-search" id="home">
    <div class="container">
        <div class="hero-content fade-in">
            <h2>Thuê Xe Máy Dễ Dàng, Nhanh Chóng</h2>
            <p>Hơn 1000+ xe máy các loại với chất lượng cao, giá cả hợp lý và dịch vụ chuyên nghiệp</p>
            <a href="#categories" class="btn-hero">Khám phá xe</a>
        </div>

        <!-- SEARCH CARD OVERLAY -->
        <div class="search-card fade-in">
            <form class="search-inner" action="${pageContext.request.contextPath}/motorbikesearch" method="get">
                <div class="field field--type">
                    <label for="type_id">Loại xe</label>
                    <select id="type_id" name="type_id" class="control">
                        <option value="">-- Tất cả --</option>
                        <option value="1">Xe số</option>
                        <option value="2">Xe ga</option>
                        <option value="3">Phân khối lớn</option>
                    </select>
                </div>
                <div class="field field--start">
                    <label for="start_date">Bắt đầu</label>
                    <input id="start_date" name="start_date" type="date" class="control">
                </div>
                <div class="field field--end">
                    <label for="end_date">Trả xe</label>
                    <input id="end_date" name="end_date" type="date" class="control">
                </div>
                <div class="field field--price">
                    <label for="max_price">Giá tối đa (VND/ngày)</label>
                    <input id="max_price" name="max_price" type="number" min="0" step="1000" placeholder="VD: 200000"
                           class="control">
                </div>
                <div class="field field--kw">
                    <label for="keyword">Từ khóa</label>
                    <input id="keyword" name="keyword" type="text" placeholder="Tên xe / biển số" class="control">
                </div>
                <div class="field field--actions">
                    <button type="submit" class="btn-search"><i class="fas fa-search"></i> Tìm xe</button>
                </div>
            </form>
        </div>
        <!-- /SEARCH CARD OVERLAY -->
    </div>
</section>


<!-- ===== CATEGORIES ===== -->
<section class="container" id="categories">
    <div class="section-title fade-in">
        <h2>Loại Xe Cho Thuê</h2>
        <p>Lựa chọn từ nhiều dòng xe đa dạng phù hợp với nhu cầu của bạn</p>
    </div>
    <div class="categories">
        <div class="category-card fade-in delay-1">
            <div class="category-image">
                <img src="images/demo_bike/xe-ga.jpg" alt="Xe ga">
            </div>
            <div class="category-content">
                <h3>Xe Ga</h3>
                <p>Xe ga tự động, dễ sử dụng, tiết kiệm nhiên liệu. Phù hợp di chuyển trong thành phố.</p>
                <a href="${pageContext.request.contextPath}/motorbikesearch?type_id=2" class="btn-category">Xem thêm <i
                        class="fas fa-arrow-right"></i></a>
            </div>
        </div>
        <div class="category-card fade-in delay-2">
            <div class="category-image">
                <img src="images/demo_bike/yaz1.jpg" alt="Xe số cổ điển">
            </div>
            <div class="category-content">
                <h3>Xe Số</h3>
                <p>Vận hành bền bỉ, tiết kiệm. Lựa chọn kinh tế cho mọi nhu cầu.</p>
                <a href="${pageContext.request.contextPath}/motorbikesearch?type_id=1" class="btn-category">Xem thêm <i
                        class="fas fa-arrow-right"></i></a>
            </div>
        </div>
        <div class="category-card fade-in delay-3">
            <div class="category-image">
                <img src="images/demo_bike/r1.jpg" alt="Xe PKL thể thao">
            </div>
            <div class="category-content">
                <h3>Xe PKL</h3>
                <p>Mạnh mẽ, phấn khích cho hành trình dài.</p>
                <a href="${pageContext.request.contextPath}/motorbikesearch?type_id=3" class="btn-category">Xem thêm <i
                        class="fas fa-arrow-right"></i></a>
            </div>
        </div>
    </div>
</section>

<!-- ===== Featured ===== -->
<section class="container featured-bikes">
    <div class="section-title fade-in">
        <h2>Xe Nổi Bật</h2>
        <p>Những mẫu xe được yêu thích và đánh giá cao nhất</p>
    </div>
    <div class="bike-grid">
        <div class="bike-card fade-in delay-1">
            <div class="bike-image">
                <img src="images/demo_bike/future.jpg" alt="Honda Future 125cc">
                <div class="bike-badge">Phổ biến</div>
            </div>
            <div class="bike-details">
                <div class="bike-title">
                    <h3>Honda Future</h3>
                    <div class="bike-price">200k/ngày</div>
                </div>
                <div class="bike-specs"><span><i class="fas fa-gas-pump"></i> 125cc</span><span><i
                        class="fas fa-cog"></i> Xe số</span></div>
                <a href="${pageContext.request.contextPath}/motorbikesearch?keyword=Future" class="bike-action">Thuê
                    ngay</a>
            </div>
        </div>
        <div class="bike-card fade-in delay-2">
            <div class="bike-image">
                <img src="images/demo_bike/nvx.jpg" alt="Yamaha NVX 155cc">
                <div class="bike-badge">Mới</div>
            </div>
            <div class="bike-details">
                <div class="bike-title">
                    <h3>Yamaha NVX</h3>
                    <div class="bike-price">250k/ngày</div>
                </div>
                <div class="bike-specs"><span><i class="fas fa-gas-pump"></i> 155cc</span><span><i
                        class="fas fa-cog"></i> Xe ga</span></div>
                <a href="${pageContext.request.contextPath}/motorbikesearch?keyword=NVX" class="bike-action">Thuê
                    ngay</a>
            </div>
        </div>
        <div class="bike-card fade-in delay-3">
            <div class="bike-image">
                <img src="images/demo_bike/ninja300.jpg" alt="Kawasaki Ninja 300cc">
                <div class="bike-badge">Cao cấp</div>
            </div>
            <div class="bike-details">
                <div class="bike-title">
                    <h3>Kawasaki Ninja</h3>
                    <div class="bike-price">500k/ngày</div>
                </div>
                <div class="bike-specs"><span><i class="fas fa-gas-pump"></i> 300cc</span><span><i
                        class="fas fa-cog"></i> Xe PKL</span></div>
                <a href="${pageContext.request.contextPath}/motorbikesearch?keyword=Ninja" class="bike-action">Thuê
                    ngay</a>
            </div>
        </div>
    </div>
</section>

<!-- ===== How it works ===== -->
<section class="how-it-works" id="how-it-works">
    <div class="container">
        <div class="section-title fade-in">
            <h2 style="color:#fff">Cách Thuê Xe</h2>
            <p>Chỉ với 4 bước đơn giản để sở hữu chiếc xe ưng ý</p>
        </div>
        <div class="steps">
            <div class="step fade-in delay-1">
                <div class="step-number">1</div>
                <div class="step-icon"><i class="fas fa-search"></i></div>
                <h3>Tìm Kiếm</h3>
                <p>Chọn thời gian và loại xe bạn muốn</p>
            </div>
            <div class="step fade-in delay-2">
                <div class="step-number">2</div>
                <div class="step-icon"><i class="fas fa-calendar-check"></i></div>
                <h3>Đặt Xe</h3>
                <p>Điền thông tin và xác nhận đơn</p>
            </div>
            <div class="step fade-in delay-1">
                <div class="step-number">3</div>
                <div class="step-icon"><i class="fas fa-key"></i></div>
                <h3>Nhận Xe</h3>
                <p>Nhận xe tại điểm hẹn</p>
            </div>
            <div class="step fade-in delay-2">
                <div class="step-number">4</div>
                <div class="step-icon"><i class="fas fa-road"></i></div>
                <h3>Trải Nghiệm</h3>
                <p>Tận hưởng chuyến đi an toàn</p>
            </div>
        </div>
    </div>
</section>

<!-- ===== Customer Reviews ===== -->
<!-- ===== Customer Reviews ===== -->
<section class="customer-reviews" id="testimonials">
    <div class="container">
        <div class="section-title fade-in">
            <h2>Đánh giá từ khách hàng</h2>
            <p>Những trải nghiệm thực tế từ khách hàng đã sử dụng dịch vụ của chúng tôi</p>
        </div>

        <!-- Thông báo -->
        <c:if test="${not empty sessionScope.message}">
            <div class="alert alert-success alert-dismissible fade show mx-auto" style="max-width: 600px; font-family: 'Inter', sans-serif;" role="alert">
                ${sessionScope.message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <c:remove var="message" scope="session"/>
        </c:if>

        <c:choose>
            <c:when test="${not empty reviews}">
                <!-- Carousel Container -->
                <div id="reviewCarousel" class="carousel slide" data-bs-ride="carousel">
                    <!-- Indicators -->
                    <div class="carousel-indicators">
                        <c:forEach items="${reviews}" var="review" varStatus="loop">
                            <button type="button" data-bs-target="#reviewCarousel"
                                    data-bs-slide-to="${loop.index}"
                                    class="<c:if test='${loop.index == 0}'>active</c:if>"
                                    aria-label="Review ${loop.index + 1}"></button>
                        </c:forEach>
                    </div>

                    <!-- Carousel Items -->
                    <div class="carousel-inner">
                        <c:forEach items="${reviews}" var="review" varStatus="loop">
                            <div class="carousel-item <c:if test='${loop.index == 0}'>active</c:if>">
                                <div class="review-card text-center">
                                    <!-- Hiển thị sao đánh giá -->
                                    <div class="rating-stars mb-4">
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
                                        <span class="rating-text">(${review.rating}/5)</span>
                                    </div>

                                    <!-- Nội dung đánh giá -->
                                    <p class="review-comment">"${review.comment}"</p>

                                    <!-- Thông tin khách hàng -->
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
                                        <c:if test="${not empty review.createdAt}">
                                            <span class="review-date">
                                                <fmt:formatDate value="${review.createdAt}" pattern="dd/MM/yyyy"/>
                                            </span>
                                        </c:if>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>

                    <!-- Navigation Controls -->
                    <button class="carousel-control-prev" type="button" data-bs-target="#reviewCarousel" data-bs-slide="prev">
                        <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                        <span class="visually-hidden">Previous</span>
                    </button>
                    <button class="carousel-control-next" type="button" data-bs-target="#reviewCarousel" data-bs-slide="next">
                        <span class="carousel-control-next-icon" aria-hidden="true"></span>
                        <span class="visually-hidden">Next</span>
                    </button>
                </div>
            </c:when>

            <c:otherwise>
                <div class="text-center no-reviews">
                    <i class="fas fa-comments mb-3" style="font-size: 3rem; opacity: 0.7;"></i>
                    <h5>Chưa có đánh giá nào</h5>
                    <p>Hãy là người đầu tiên đánh giá dịch vụ của chúng tôi!</p>
                </div>
            </c:otherwise>
        </c:choose>

        <!-- Nút đánh giá -->
        <div class="text-center mt-5">
            <a href="${ctx}/storereview?view=page" class="btn btn--solid btn-lg">
                <i class="fas fa-star me-2"></i>Đánh giá ngay
            </a>
        </div>
    </div>
</section>


<!-- ===== App download & Footer giữ nguyên ===== -->
<section class="app-download">
    <div class="container">
        <div class="app-content">
            <div class="app-text fade-in">
                <h2>Tải Ứng Dụng MotoRent</h2>
                <p>Đặt xe mọi lúc, mọi nơi với ứng dụng di động. Nhận ưu đãi đặc biệt khi đặt qua app.</p>
                <div class="app-buttons">
                    <a href="#" class="app-btn"><i class="fab fa-apple"></i>
                        <div><span>Tải trên</span><strong>App Store</strong></div>
                    </a>
                    <a href="#" class="app-btn"><i class="fab fa-google-play"></i>
                        <div><span>Tải trên</span><strong>Google Play</strong></div>
                    </a>
                </div>
            </div>
            <div class="app-image fade-in delay-1">
                <img src="https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?auto=format&fit=crop&w=1000&q=80"
                     alt="Ứng dụng MotoRent hiển thị trên điện thoại">
            </div>
        </div>
    </div>
</section>

<footer id="contact" class="site-footer" role="contentinfo">
    <div class="container">
        <div class="footer-content">
            <div class="footer-about fade-in">
                <div class="footer-logo"><i class="fas fa-motorcycle"></i> MotoRent</div>
                <p>Dịch vụ cho thuê xe máy hàng đầu Việt Nam với chất lượng tốt và giá cạnh tranh. Cam kết mang đến trải
                    nghiệm thuê xe tuyệt vời nhất.</p>
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
                    <li><a href="#home"><i class="fas fa-chevron-right"></i> Trang chủ</a></li>
                    <li><a href="#categories"><i class="fas fa-chevron-right"></i> Loại xe</a></li>
                    <li><a href="#how-it-works"><i class="fas fa-chevron-right"></i> Cách thuê</a></li>
                    <li><a href="#testimonials"><i class="fas fa-chevron-right"></i> Đánh giá</a></li>
                    <li><a href="#contact"><i class="fas fa-chevron-right"></i> Liên hệ</a></li>
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
                <p>Đăng ký để nhận thông tin khuyến mãi và ưu đãi đặc biệt từ MotoRent.</p>
                <form class="newsletter-form">
                    <input type="email" placeholder="Email của bạn" required>
                    <button type="submit"><i class="fas fa-paper-plane"></i> Đăng ký</button>
                </form>
            </div>
        </div>
        <div class="footer-bottom"><p>© 2025 MotoRent. Tất cả quyền được bảo lưu.</p></div>
    </div>
</footer>

<script>
    const $ = (sel) => document.querySelector(sel);
    const $$ = (sel) => document.querySelectorAll(sel);

    window.addEventListener('scroll', () => {
        const header = $('#header');
        if (window.scrollY > 50) header.classList.add('scrolled'); else header.classList.remove('scrolled');
    });

    const mobileToggle = $('#mobileToggle');
    const mobilePanel = $('#mobilePanel');
    mobileToggle?.addEventListener('click', () => {
        const open = mobilePanel.style.display === 'block';
        mobilePanel.style.display = open ? 'none' : 'block';
        mobileToggle.setAttribute('aria-expanded', String(!open));
    });

    $$('a[href^="#"]').forEach(a => {
        a.addEventListener('click', e => {
            const id = a.getAttribute('href');
            if (!id || id === '#') return;
            const target = $(id);
            if (target) {
                e.preventDefault();
                window.scrollTo({top: target.offsetTop - 90, behavior: 'smooth'});
                mobilePanel.style.display = 'none';
                mobileToggle.setAttribute('aria-expanded', 'false');
            }
        });
    });

    const sections = $$('section');
    const navLinks = $$('.nav-link');
    window.addEventListener('scroll', () => {
        let current = '';
        sections.forEach(section => {
            const sectionTop = section.offsetTop;
            if (scrollY >= (sectionTop - 100)) current = section.getAttribute('id');
        });
        navLinks.forEach(link => {
            link.classList.remove('active');
            if (link.getAttribute('href') === `#${current}`) link.classList.add('active');
        });
    });

    const today = new Date().toISOString().split('T')[0];
    const sdTop = $('#start_date'), edTop = $('#end_date');
    [sdTop, edTop].forEach(el => el && el.setAttribute('min', today));
    sdTop?.addEventListener('change', () => edTop?.setAttribute('min', sdTop.value || today));

    const sdM = $('#start_date_m'), edM = $('#end_date_m');
    [sdM, edM].forEach(el => el && el.setAttribute('min', today));
    sdM?.addEventListener('change', () => edM?.setAttribute('min', sdM.value || today));

    const STORAGE_KEY = 'bbSearch';
    const toast = (msg, isError = false) => {
        const t = document.createElement('div');
        t.textContent = msg;
        t.style.cssText = `position:fixed;bottom:20px;left:50%;transform:translateX(-50%);background:${isError ? '#EF4444' : '#111827'};color:#fff;padding:12px 20px;border-radius:8px;z-index:9999;opacity:0;transition:opacity .3s;font-weight:500;`;
        document.body.appendChild(t);
        setTimeout(() => t.style.opacity = '1', 10);
        setTimeout(() => {
            t.style.opacity = '0';
            setTimeout(() => t.remove(), 300);
        }, 3000);
    };

    function getHeaderData() {
        return {
            type_id: $('#type_id')?.value || '',
            start_date: sdTop?.value || '',
            end_date: edTop?.value || '',
            max_price: $('#max_price')?.value || '',
            keyword: $('#keyword')?.value || ''
        };
    }

    function setHeaderData(d) {
        if (!d) return;
        if ($('#type_id')) $('#type_id').value = d.type_id || '';
        if (sdTop) sdTop.value = d.start_date || '';
        if (edTop) {
            if (d.start_date) edTop.setAttribute('min', d.start_date);
            edTop.value = d.end_date || '';
        }
        if ($('#max_price')) $('#max_price').value = d.max_price || '';
        if ($('#keyword')) $('#keyword').value = d.keyword || '';
    }

    function getMobileData() {
        return {
            type_id: $('#type_id_m')?.value || '',
            start_date: sdM?.value || '',
            end_date: edM?.value || '',
            max_price: $('#max_price_m')?.value || '',
            keyword: $('#keyword_m')?.value || ''
        };
    }

    function setMobileData(d) {
        if (!d) return;
        if ($('#type_id_m')) $('#type_id_m').value = d.type_id || '';
        if (sdM) sdM.value = d.start_date || '';
        if (edM) {
            if (d.start_date) edM.setAttribute('min', d.start_date);
            edM.value = d.end_date || '';
        }
        if ($('#max_price_m')) $('#max_price_m').value = d.max_price || '';
        if ($('#keyword_m')) $('#keyword_m').value = d.keyword || '';
    }

    function saveSearch(data) {
        try {
            localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
            toast('Đã lưu tìm kiếm thành công!');
        } catch (e) {
            toast('Có lỗi xảy ra khi lưu tìm kiếm.', true);
        }
    }

    function loadSearch() {
        try {
            const raw = localStorage.getItem(STORAGE_KEY);
            return raw ? JSON.parse(raw) : null;
        } catch (e) {
            return null;
        }
    }

    function clearSaved() {
        try {
            localStorage.removeItem(STORAGE_KEY);
            toast('Đã xóa tìm kiếm đã lưu.');
        } catch (e) {
            toast('Có lỗi xảy ra khi xóa tìm kiếm.', true);
        }
    }

    $('#saveSearch')?.addEventListener('click', () => saveSearch(getHeaderData()));
    $('#saveSearchM')?.addEventListener('click', () => saveSearch(getMobileData()));
    $('#resetHeader')?.addEventListener('click', clearSaved);
    $('#resetMobile')?.addEventListener('click', clearSaved);

    const saved = loadSearch();
    setHeaderData(saved);
    setMobileData(saved);
</script>
<script>
document.addEventListener('DOMContentLoaded', function() {
    const reviewCarousel = document.getElementById('reviewCarousel');
    
    if (reviewCarousel) {
        // Khởi tạo carousel với autoplay
        const carousel = new bootstrap.Carousel(reviewCarousel, {
            interval: 4000, // 4 giây
            pause: 'hover', // Dừng khi hover
            wrap: true,     // Quay vòng
            touch: true     // Cho phép touch trên mobile
        });
        
        // Thêm hiệu ứng smooth transition
        reviewCarousel.addEventListener('slide.bs.carousel', function(event) {
            const items = this.querySelectorAll('.carousel-item');
            items.forEach(item => {
                item.style.transition = 'transform 0.6s ease-in-out';
            });
        });
    }
});
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
