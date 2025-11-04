<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>
    <c:choose>
      <c:when test="${not empty bike}">${fn:escapeXml(bike.bikeName)} | Chi tiết xe</c:when>
      <c:otherwise>Chi tiết xe | RideNow</c:otherwise>
    </c:choose>
  </title>

  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">

  <!-- CSS -->
  <link rel="stylesheet" href="${ctx}/css/homeStyle.css?v=11">
  <link rel="stylesheet" href="${ctx}/css/detailStyle.css?v=11">

  <!-- Flatpickr -->
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css">
  <script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>
  <script src="https://cdn.jsdelivr.net/npm/flatpickr/dist/l10n/vn.js"></script>

  <!-- Gallery & Reviews CSS -->
  <style>
    .gallery { display:grid; gap:12px; }
    .gallery-main{ position:relative; border-radius:12px; overflow:hidden; background:#0b1224; border:1px solid #334155; }
    .gallery-main img{ width:100%; height:420px; object-fit:cover; display:block; }
    .gallery-nav{ position:absolute; inset:0; display:flex; align-items:center; justify-content:space-between; pointer-events:none; }
    .gallery-btn{ pointer-events:auto; width:40px; height:40px; border:none; border-radius:999px; background:rgba(0,0,0,.45); color:#fff; display:flex; align-items:center; justify-content:center; margin:0 8px; cursor:pointer; transition:.2s; }
    .gallery-btn:hover{ background:rgba(0,0,0,.7); transform:translateY(-1px); }
    .thumbs{ display:none; grid-template-columns:repeat(6,1fr); gap:10px; }
    .thumbs.show{ display:grid; }
    .thumb{ border:2px solid transparent; border-radius:10px; overflow:hidden; cursor:pointer; transition:.2s; height:74px; background:#0b1224; }
    .thumb img{ width:100%; height:100%; object-fit:cover; display:block; }
    .thumb.active, .thumb:hover{ border-color:#3b82f6; box-shadow:0 0 0 3px rgba(59,130,246,.15); }
    @media (max-width:768px){ .gallery-main img{ height:300px; } .thumbs{ grid-template-columns:repeat(4,1fr); } }
  
    /* === Review Section - Compact & Beautiful Design === */
    .reviews-section {
      margin-top: 1.5rem;
    }

    .review-card {
      background: rgba(15, 23, 42, 0.6);
      border: 1px solid #334155;
      border-radius: 8px;
      padding: 0.75rem 1rem;
      margin-bottom: 0.75rem;
      backdrop-filter: blur(10px);
      transition: all 0.2s ease;
      position: relative;
    }

    .review-card:hover {
      border-color: #4b5563;
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    .review-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 0.5rem;
      gap: 0.75rem;
    }

    .reviewer-name {
      font-weight: 600;
      color: #e2e8f0;
      font-size: 0.875rem;
      line-height: 1.2;
    }

    .review-rating {
      display: flex;
      gap: 2px;
      flex-shrink: 0;
    }

    .review-rating .star-filled {
      color: #fbbf24;
      font-size: 13px;
    }

    .review-rating .star-empty {
      color: #475569;
      font-size: 13px;
    }

    .review-content {
      margin-bottom: 0.375rem;
    }

    .review-comment {
      color: #cbd5e1;
      line-height: 1.4;
      font-size: 0.8125rem;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .review-date {
      font-size: 0.75rem;
      color: #94a3b8;
      text-align: right;
    }

    .no-reviews {
      text-align: center;
      color: #64748b;
      padding: 2rem 1rem;
      background: rgba(15, 23, 42, 0.6);
      border-radius: 8px;
      border: 1px solid #334155;
    }

    .no-reviews i {
      font-size: 2rem;
      margin-bottom: 0.75rem;
      color: #475569;
      opacity: 0.7;
    }

    .no-reviews p {
      margin: 0.25rem 0;
      font-size: 0.875rem;
    }

    /* Review Stats - Compact Layout */
    .review-stats {
      display: flex;
      align-items: center;
      gap: 1.25rem;
      margin-bottom: 1.25rem;
      padding: 1rem 1.25rem;
      background: rgba(15, 23, 42, 0.6);
      border-radius: 8px;
      border: 1px solid #334155;
    }

    .average-rating {
      text-align: center;
      min-width: 70px;
    }

    .average-score {
      font-size: 1.75rem;
      font-weight: 700;
      color: #fbbf24;
      display: block;
      line-height: 1;
    }

    .rating-text {
      font-size: 0.75rem;
      color: #94a3b8;
      margin-top: 0.25rem;
    }

    .rating-breakdown {
      flex: 1;
      min-width: 0;
    }

    .rating-bar {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-bottom: 0.375rem;
    }

    .rating-bar .stars {
      width: 40px;
      color: #fbbf24;
      font-size: 0.75rem;
      flex-shrink: 0;
    }

    .rating-bar .bar {
      flex: 1;
      height: 6px;
      background: #334155;
      border-radius: 3px;
      overflow: hidden;
      min-width: 0;
    }

    .rating-bar .fill {
      height: 100%;
      background: #fbbf24;
      border-radius: 3px;
      transition: width 0.3s ease;
    }

    .rating-bar .count {
      width: 25px;
      font-size: 0.75rem;
      color: #94a3b8;
      text-align: right;
      flex-shrink: 0;
    }

    .total-reviews {
      text-align: center;
      min-width: 60px;
    }

    .total-count {
      font-size: 1.125rem;
      font-weight: 600;
      color: #e2e8f0;
      display: block;
    }

    .total-text {
      font-size: 0.75rem;
      color: #94a3b8;
    }

    /* Animation */
    .review-card {
      animation: fadeInUp 0.3s ease-out;
    }

    @keyframes fadeInUp {
      from {
        opacity: 0;
        transform: translateY(8px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    /* Responsive Design */
    @media (max-width: 768px) {
      .review-stats {
        flex-direction: column;
        gap: 1rem;
        padding: 1rem;
      }
      
      .rating-breakdown {
        width: 100%;
      }
      
      .average-rating, .total-reviews {
        width: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 1rem;
      }
      
      .review-header {
        flex-direction: row;
        align-items: center;
      }
      
      .review-comment {
        -webkit-line-clamp: 3;
      }
    }

    @media (max-width: 480px) {
      .review-card {
        padding: 0.625rem 0.875rem;
      }
      
      .review-stats {
        gap: 0.875rem;
        padding: 0.875rem;
      }
      
      .rating-bar {
        gap: 0.375rem;
      }
      
      .rating-bar .stars {
        width: 35px;
      }
      
      .review-comment {
        -webkit-line-clamp: 2;
      }
      
      .average-score {
        font-size: 1.5rem;
      }
      
      .total-count {
        font-size: 1rem;
      }
    }

    /* Ultra Compact Mode for High Density */
    .reviews-section.compact .review-card {
      padding: 0.625rem 0.75rem;
      margin-bottom: 0.5rem;
    }

    .reviews-section.compact .review-header {
      margin-bottom: 0.375rem;
      gap: 0.5rem;
    }

    .reviews-section.compact .reviewer-name {
      font-size: 0.8125rem;
    }

    .reviews-section.compact .review-rating .star-filled,
    .reviews-section.compact .review-rating .star-empty {
      font-size: 11px;
    }

    .reviews-section.compact .review-comment {
      font-size: 0.75rem;
      -webkit-line-clamp: 2;
      margin-bottom: 0.125rem;
    }

    .reviews-section.compact .review-date {
      font-size: 0.6875rem;
    }

    .reviews-section.compact .review-stats {
      padding: 0.875rem;
      gap: 1rem;
      margin-bottom: 1rem;
    }

    .reviews-section.compact .average-score {
      font-size: 1.5rem;
    }

    .reviews-section.compact .rating-bar {
      margin-bottom: 0.25rem;
    }

    .reviews-section.compact .rating-bar .stars {
      width: 35px;
      font-size: 0.6875rem;
    }

    .reviews-section.compact .rating-bar .bar {
      height: 4px;
    }

    .reviews-section.compact .total-count {
      font-size: 1rem;
    }
  </style>
</head>
<body>

<header id="header">
  <div class="header-top">
    <div class="container">
      <div class="header-content">
        <a href="${ctx}/" class="brand" aria-label="Về trang chủ">
          <img src="${ctx}/images/ridenow_Logo.jpg" alt="RideNow Logo">
          <span class="brand-name">RideNow</span>
        </a>

        <nav id="mainNav" aria-label="Điều hướng chính">
          <ul>
            <li><a href="${ctx}/#home" class="nav-link">Trang chủ</a></li>
            <li class="nav-item">
              <a href="#" class="nav-link">Loại xe <i class="fa-solid fa-chevron-down" style="font-size:12px"></i></a>
              <div class="dropdown" aria-label="Chọn loại xe">
                <a href="${ctx}/motorbikesearch?type_id=1">Xe số</a>
                <a href="${ctx}/motorbikesearch?type_id=2">Xe ga</a>
                <a href="${ctx}/motorbikesearch?type_id=3">Xe PKL</a>
              </div>
            </li>
            <li><a href="${ctx}/#how-it-works" class="nav-link">Cách thuê</a></li>
            <li><a href="${ctx}/#testimonials" class="nav-link">Đánh giá</a></li>
            <li><a href="${ctx}/#contact" class="nav-link">Liên hệ</a></li>
          </ul>
        </nav>

        <div class="auth" id="authDesktop">
          <a href="${ctx}/cart" class="btn btn--ghost"><i class="fas fa-shopping-cart"></i> Giỏ hàng</a>
          <c:choose>
            <c:when test="${not empty sessionScope.account}">
              <a href="${ctx}/customer/profile" class="btn btn--ghost" title="Hồ sơ">
                <i class="fas fa-user-circle"></i> Xin chào, <strong>${sessionScope.account.username}</strong>
              </a>
              <a href="${ctx}/logout" class="btn btn--ghost"><i class="fas fa-right-from-bracket"></i> Đăng xuất</a>
            </c:when>
            <c:otherwise>
              <a href="${ctx}/login" class="btn btn--ghost"><i class="fas fa-user"></i> Đăng nhập</a>
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
</header>

<!-- Video nền -->
<div class="page-bg">
  <video autoplay muted loop playsinline preload="metadata" poster="${ctx}/images/demo_bike/back-PKL2.jpg">
    <source src="${ctx}/images/video/vid1.mp4" type="video/mp4"/>
  </video>
  <div class="bg-overlay"></div>
</div>

<section class="detail-hero">
  <div class="container">
    <nav class="breadcrumbs" aria-label="breadcrumb">
      <a href="${ctx}/"><i class="fa-solid fa-house"></i> Trang chủ</a>
      <i class="fa-solid fa-angle-right"></i>
      <a href="${ctx}/motorbikesearch">Tìm xe</a>
      <i class="fa-solid fa-angle-right"></i>
      <span>Chi tiết</span>
    </nav>
    <h1>
      <c:choose>
        <c:when test="${not empty bike}">${fn:escapeXml(bike.bikeName)}</c:when>
        <c:otherwise>Chi tiết xe</c:otherwise>
      </c:choose>
    </h1>
  </div>
</section>

<!-- Map thư mục ảnh theo loại -->
<c:if test="${not empty bike}">
  <c:choose>
    <c:when test="${not empty bike.typeName && fn:contains(fn:toLowerCase(bike.typeName), 'số')}">
      <c:set var="imgFolder" value="xe-so"/>
    </c:when>
    <c:when test="${not empty bike.typeName && fn:contains(fn:toLowerCase(bike.typeName), 'ga')}">
      <c:set var="imgFolder" value="xe-ga"/>
    </c:when>
    <c:when test="${not empty bike.typeName && (fn:contains(fn:toLowerCase(bike.typeName), 'pkl') || fn:contains(fn:toLowerCase(bike.typeName), 'phân khối'))}">
      <c:set var="imgFolder" value="xe-pkl"/>
    </c:when>
    <c:when test="${bike.typeId == 1}"><c:set var="imgFolder" value="xe-so"/></c:when>
    <c:when test="${bike.typeId == 2}"><c:set var="imgFolder" value="xe-ga"/></c:when>
    <c:when test="${bike.typeId == 3}"><c:set var="imgFolder" value="xe-pkl"/></c:when>
    <c:otherwise><c:set var="imgFolder" value="khac"/></c:otherwise>
  </c:choose>
</c:if>

<main class="container detail-layout">

  <c:if test="${not empty error}">
    <div class="card callout callout--error">
      <i class="fa-solid fa-triangle-exclamation"></i> ${error}
    </div>
  </c:if>

  <c:choose>
    <c:when test="${empty bike}">
      <div class="card callout">
        Không tìm thấy xe. <a href="${ctx}/motorbikesearch" class="btn btn--ghost" style="margin-left:8px">Quay lại tìm kiếm</a>
      </div>
    </c:when>

    <c:otherwise>
      <div class="grid">
        <!-- Trái: Gallery -->
        <section class="card">
          <div class="gallery" id="gallery">
            <div class="gallery-main">
              <img id="mainImg"
                   src="${ctx}/images/bike/${imgFolder}/${bike.bikeId}/1.jpg"
                   alt="${fn:escapeXml(bike.bikeName)}"
                   loading="eager"
                   onerror="this.onerror=null;this.src='${ctx}/images/bike_placeholder.jpg';">
              <div class="gallery-nav" id="navBtns" style="display:none">
                <button type="button" class="gallery-btn" id="prevBtn" aria-label="Ảnh trước">
                  <i class="fa-solid fa-chevron-left"></i>
                </button>
                <button type="button" class="gallery-btn" id="nextBtn" aria-label="Ảnh sau">
                  <i class="fa-solid fa-chevron-right"></i>
                </button>
              </div>
            </div>
            <div class="thumbs" id="thumbs"></div>
          </div>
        </section>

        <!-- Phải: Thông tin + giỏ -->
        <section class="card">
          <h3 class="card-title">Thông tin chính</h3>

          <div class="price-row">
            <div class="price"><fmt:formatNumber value="${bike.pricePerDay}" type="number"/> đ/ngày</div>
            <div class="status">
              <c:choose>
                <c:when test="${bike.status == 'available'}"><span class="status-badge ok"><i class="fa-solid fa-circle-check"></i> Sẵn sàng</span></c:when>
                <c:when test="${bike.status == 'rented'}"><span class="status-badge warn"><i class="fa-solid fa-clock"></i> Đang thuê</span></c:when>
                <c:otherwise><span class="status-badge danger"><i class="fa-solid fa-screwdriver-wrench"></i> Bảo dưỡng</span></c:otherwise>
              </c:choose>
            </div>
          </div>

          <div class="meta">
            <span><i class="fa-solid fa-id-card"></i> Biển số: <strong>${fn:escapeXml(bike.licensePlate)}</strong></span>
            <span><i class="fa-solid fa-tag"></i> Loại: <strong><c:out value="${empty bike.typeName ? 'Không rõ' : bike.typeName}"/></strong></span>
          </div>

          <div class="owner-box">
            <div class="owner-avatar"><i class="fa-solid fa-user-tie"></i></div>
            <div>
              <div class="owner-name">${fn:escapeXml(bike.ownerName)}</div>
              <div class="owner-type">
                <span>Chủ sở hữu: </span>
                <c:choose>
                  <c:when test="${bike.ownerType=='partner'}">Đối tác</c:when>
                  <c:when test="${bike.ownerType=='store'}">Cửa hàng</c:when>
                  <c:otherwise>${fn:escapeXml(bike.ownerType)}</c:otherwise>
                </c:choose>
              </div>
            </div>
          </div>

          <div class="desc">
            <div class="desc-title">Mô tả</div>
            <c:choose>
              <c:when test="${not empty bike.description}">
                <p>${fn:escapeXml(bike.description)}</p>
              </c:when>
              <c:otherwise>
                <p>Xe đang trong tình trạng tốt, sẵn sàng cho chuyến đi của bạn.</p>
              </c:otherwise>
            </c:choose>
          </div>

          <!-- Đặt xe nhanh / thêm giỏ -->
          <div class="book card--inner">
            <div class="book-title"><i class="fa-solid fa-bolt"></i> Đặt xe nhanh</div>

            <c:choose>
              <c:when test="${bike.status == 'maintenance'}">
                <!-- Hiển thị thông báo khi xe đang bảo dưỡng -->
                <div class="card callout callout--error" style="margin-top:12px">
                  <i class="fa-solid fa-screwdriver-wrench"></i>
                  Xe đang trong chế độ bảo dưỡng, tạm thời không thể thuê. Vui lòng quay lại sau.
                </div>
                <div class="extra-actions">
                  <a href="${ctx}/motorbikesearch" class="btn-ghost"><i class="fa-solid fa-arrow-left"></i> Quay lại danh sách</a>
                </div>
              </c:when>
              <c:otherwise>
                <!-- Hiển thị form đặt xe khi xe khả dụng -->
                
                <!-- ✅ Chỉ một block lỗi, kèm danh sách khoảng ngày trùng -->
                <c:if test="${not empty sessionScope.book_error}">
                  <div class="card callout callout--error" style="margin-top:12px">
                    <i class="fa-solid fa-triangle-exclamation"></i>
                    ${sessionScope.book_error}
                    <c:if test="${not empty sessionScope.book_conflicts}">
                      <ul style="margin:8px 0 0 24px; list-style:disc">
                        <c:forEach var="cf" items="${sessionScope.book_conflicts}">
                          <li>
                            <span class="badge" style="background:#fee2e2;color:#991b1b;padding:2px 8px;border-radius:999px">
                              ${cf}
                            </span>
                          </li>
                        </c:forEach>
                      </ul>
                    </c:if>
                  </div>
                  <c:remove var="book_error" scope="session"/>
                  <c:remove var="book_conflicts" scope="session"/>
                </c:if>

                <div id="previewBox" class="card preview">
                  <div><b>Giá/ngày:</b> <fmt:formatNumber value="${bike.pricePerDay}" type="number"/> đ</div>
                  <div><b>Số ngày:</b> <span id="pvDays">0</span></div>
                  <div><b>Tạm tính:</b> <span id="pvSubtotal">0</span> đ</div>
                  <div><b>Cọc dự kiến:</b>
                    <c:choose>
                      <c:when test="${bike.typeName == 'Phân khối lớn'}">1,000,000</c:when>
                      <c:otherwise>500,000</c:otherwise>
                    </c:choose> đ
                  </div>
                </div>

                <form action="${ctx}/cart" method="post" class="book-form" id="addToCartForm" novalidate>
                  <input type="hidden" name="action" value="add"/>
                  <input type="hidden" name="bikeId" value="${bike.bikeId}" />
                  <label>
                    Ngày nhận
                    <input type="text" name="start" id="startDate" class="control" placeholder="Chọn ngày nhận" required>
                  </label>
                  <label>
                    Ngày trả
                    <input type="text" name="end" id="endDate" class="control" placeholder="Chọn ngày trả" required>
                  </label>
                  <button type="submit" class="btn-primary">
                    <i class="fa-solid fa-cart-plus"></i> Thêm vào giỏ
                  </button>
                </form>
                <small class="hint">* Chọn ngày để xem tạm tính. Bạn có thể thêm nhiều xe vào giỏ.</small>

                <div class="extra-actions">
                  <a href="${ctx}/cart" class="btn-ghost"><i class="fa-solid fa-bag-shopping"></i> Xem giỏ hàng</a>
                  <a href="${ctx}/motorbikesearch" class="btn-ghost"><i class="fa-solid fa-arrow-left"></i> Quay lại danh sách</a>
                </div>
              </c:otherwise>
            </c:choose>
          </div>
        </section>
      </div>

      <!-- Reviews Section - ĐÃ ĐƯỢC CẢI THIỆN -->
      <section class="reviews-section">
        <div class="card">
          <h3 class="card-title">Đánh giá từ khách hàng</h3>
          
          <!-- Review Statistics - Sử dụng reviewDisplayStats từ servlet -->
          <c:if test="${not empty reviewDisplayStats and reviewDisplayStats.totalReviews > 0}">
            <div class="review-stats">
              <div class="average-rating">
                <span class="average-score"><fmt:formatNumber value="${reviewDisplayStats.averageRating}" pattern="0.0"/></span>
                <div class="rating-text">/5</div>
              </div>
              <div class="rating-breakdown">
                <!-- Sửa lỗi: step không thể âm, dùng biến currentRating để đảo ngược thứ tự -->
                <c:forEach var="i" begin="1" end="5" step="1">
                  <c:set var="currentRating" value="${6 - i}"/>
                  <div class="rating-bar">
                    <span class="stars">${currentRating} ★</span>
                    <div class="bar">
                      <div class="fill" style="width: ${reviewDisplayStats.ratingPercentages[currentRating] * 100}%"></div>
                    </div>
                    <span class="count">${reviewDisplayStats.ratingCounts[currentRating]}</span>
                  </div>
                </c:forEach>
              </div>
              <div class="total-reviews">
                <span class="total-count">${reviewDisplayStats.totalReviews}</span>
                <div class="total-text">đánh giá</div>
              </div>
            </div>
          </c:if>

          <!-- Reviews List - Sử dụng publicReviews từ servlet -->
          <c:choose>
            <c:when test="${not empty publicReviews}">
              <c:forEach var="review" items="${publicReviews}">
                <div class="review-card">
                  <div class="review-header">
                    <div class="reviewer-name">${fn:escapeXml(review.customerName)}</div>
                    <div class="review-rating">
                      <c:forEach begin="1" end="5" var="star">
                        <c:choose>
                          <c:when test="${star <= review.rating}">
                            <span class="star-filled">★</span>
                          </c:when>
                          <c:otherwise>
                            <span class="star-empty">★</span>
                          </c:otherwise>
                        </c:choose>
                      </c:forEach>
                    </div>
                  </div>
                  <div class="review-content">
                    <div class="review-comment">
                      ${fn:escapeXml(review.comment)}
                    </div>
                  </div>
                  <div class="review-date">
                    <!-- SỬA LỖI: Xử lý LocalDateTime -->
                    <c:set var="dateTimeString" value="${review.createdAt}"/>
                    <c:set var="datePart" value="${fn:substring(dateTimeString, 0, 10)}"/>
                    <c:set var="timePart" value="${fn:substring(dateTimeString, 11, 16)}"/>
                    ${datePart} ${timePart}
                  </div>
                </div>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <div class="no-reviews">
                <i class="fa-solid fa-comment-dots"></i>
                <p>Chưa có đánh giá nào cho xe này.</p>
                <p>Hãy là người đầu tiên đánh giá!</p>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
      </section>
    </c:otherwise>
  </c:choose>
</main>

<footer id="contact" class="site-footer" role="contentinfo">
    <div class="container">
      <div class="footer-content">
        <div class="footer-about fade-in">
          <div class="footer-logo"><i class="fas fa-motorcycle"></i> MotoRent</div>
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
  // header shadow
  window.addEventListener('scroll', () => {
    const header = document.getElementById('header');
    if (window.scrollY > 50) header.classList.add('scrolled'); else header.classList.remove('scrolled');
  });

  // Datepicker + preview
  let fpStart, fpEnd;
  document.addEventListener('DOMContentLoaded', () => {
    fpStart = flatpickr("#startDate", {
      locale: "vn",
      dateFormat: "Y-m-d",
      minDate: "today",
      onChange: (d) => {
        if (d.length) {
          fpEnd.set("minDate", d[0]);
          if (!fpEnd.input.value) fpEnd.setDate(d[0], true); // mặc định thuê 1 ngày
        }
        calcPreview();
      }
    });
    fpEnd = flatpickr("#endDate", {
      locale: "vn",
      dateFormat: "Y-m-d",
      minDate: "today",
      onChange: calcPreview
    });

    // init gallery
    initGallery();

    // ✅ GUARD TRƯỚC KHI SUBMIT: luôn gửi đủ start & end
    const form = document.getElementById('addToCartForm');
    if (form) {
      form.addEventListener('submit', function(e){
        const s = document.getElementById('startDate');
        const t = document.getElementById('endDate');

        // Nếu người dùng chỉ chọn 1 bên, tự đồng bộ sang bên kia (thuê 1 ngày)
        if (!s.value && t.value) s.value = t.value;
        if (!t.value && s.value) t.value = s.value;

        // Nếu vẫn thiếu -> chặn submit
        if (!s.value || !t.value) {
          e.preventDefault();
          alert('Vui lòng chọn ngày nhận và ngày trả.');
          return;
        }

        // Nếu end < start -> chặn và báo lỗi tại chỗ
        const sd = new Date(s.value);
        const ed = new Date(t.value);
        if (ed < sd) {
          e.preventDefault();
          alert('Ngày trả phải sau hoặc bằng ngày nhận.');
          t.focus();
          return;
        }
      });
    }
  });

  function calcPreview(){
    const pvDays = document.getElementById('pvDays');
    const pvSubtotal = document.getElementById('pvSubtotal');
    const pricePerDay = Number('${bike.pricePerDay}');
    const sVal = document.getElementById('startDate')?.value;
    const eVal = document.getElementById('endDate')?.value;
    if(!sVal || !eVal){ 
      if(pvDays) pvDays.textContent='0'; 
      if(pvSubtotal) pvSubtotal.textContent='0'; 
      return; 
    }
    const s = new Date(sVal), e = new Date(eVal);
    if(isNaN(s)||isNaN(e)||e<s){ 
      if(pvDays) pvDays.textContent='0'; 
      if(pvSubtotal) pvSubtotal.textContent='0'; 
      return; 
    }
    const days = Math.floor((e - s) / (1000*60*60*24)) + 1;
    if(pvDays) pvDays.textContent = String(days);
    if(pvSubtotal) pvSubtotal.textContent = (pricePerDay * days).toLocaleString('vi-VN');
  }

  // ===== Gallery đơn giản & chính xác (tự dò 1..6.jpg) =====
  function initGallery(){
    const mainImg = document.getElementById('mainImg');
    const thumbsWrap = document.getElementById('thumbs');
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');
    const navBtns = document.getElementById('navBtns');

    if (!mainImg) return;

    const base = "${ctx}/images/bike/${imgFolder}/${bike.bikeId}";
    const validImages = [];

    function checkImageExists(url, index) {
      return new Promise((resolve) => {
        const img = new Image();
        img.onload = () => resolve({url, index, exists: true});
        img.onerror = () => resolve({url, index, exists: false});
        img.src = url;
      });
    }

    async function loadImages() {
      const promises = [];
      for (let i = 1; i <= 6; i++) promises.push(checkImageExists(base + '/' + i + '.jpg', i));
      const results = await Promise.all(promises);
      results.forEach(r => { if (r.exists) validImages.push(r.url); });

      validImages.sort((a, b) => {
        const na = parseInt(a.match(/\/(\d+)\.jpg$/)[1]);
        const nb = parseInt(b.match(/\/(\d+)\.jpg$/)[1]);
        return na - nb;
      });

      setupGallery();
    }

    function setupGallery() {
      if (validImages.length === 0) {
        mainImg.src = "${ctx}/images/bike_placeholder.jpg";
        mainImg.alt = "Ảnh không khả dụng";
        if (navBtns) navBtns.style.display = 'none';
        if (thumbsWrap) thumbsWrap.style.display = 'none';
        return;
      }

      mainImg.src = validImages[0];

      if (validImages.length === 1) {
        if (navBtns) navBtns.style.display = 'none';
        if (thumbsWrap) thumbsWrap.style.display = 'none';
        return;
      }

      if (navBtns) navBtns.style.display = 'flex';
      if (thumbsWrap) thumbsWrap.style.display = 'grid';

      let thumbsHTML = '';
      for (let i = 0; i < validImages.length; i++) {
        const activeClass = i === 0 ? 'active' : '';
        thumbsHTML += '<div class="thumb ' + activeClass + '" data-index="' + i + '">' +
                      '<img src="' + validImages[i] + '" alt="Ảnh ' + (i + 1) + '">' +
                      '</div>';
      }
      if (thumbsWrap) thumbsWrap.innerHTML = thumbsHTML;

      let currentIndex = 0;

      if (thumbsWrap) {
        thumbsWrap.addEventListener('click', function(e) {
          const thumb = e.target.closest('.thumb');
          if (thumb) {
            currentIndex = parseInt(thumb.getAttribute('data-index'));
            showImage(currentIndex);
          }
        });
      }

      if (prevBtn) prevBtn.addEventListener('click', function() {
        currentIndex = (currentIndex - 1 + validImages.length) % validImages.length;
        showImage(currentIndex);
      });
      if (nextBtn) nextBtn.addEventListener('click', function() {
        currentIndex = (currentIndex + 1) % validImages.length;
        showImage(currentIndex);
      });

      function showImage(index) {
        mainImg.src = validImages[index];
        if (thumbsWrap) {
          const allThumbs = thumbsWrap.querySelectorAll('.thumb');
          for (let j = 0; j < allThumbs.length; j++) {
            if (j === index) allThumbs[j].classList.add('active'); else allThumbs[j].classList.remove('active');
          }
        }
      }
    }

    loadImages();
  }
</script>
<jsp:include page="/chatbox.jsp" />
</body>
</html>