<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Kết quả tìm xe | RideNow</title>

  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
  <!-- Thứ tự: homeStyle trước, searchStyle sau -->
  <link rel="stylesheet" href="${ctx}/css/homeStyle.css?v=6" />
  <link rel="stylesheet" href="${ctx}/css/searchStyle.css?v=6" />
</head>
<body>

  <!-- ===== HEADER ===== -->
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

          <div class="auth">
            <a href="${ctx}/cart" class="btn btn--ghost"><i class="fas fa-shopping-cart"></i> Giỏ hàng</a>  
            <c:choose>
              <c:when test="${not empty sessionScope.account}">
                <a href="${ctx}/customer/profile" class="btn btn--ghost">
                  <i class="fas fa-user-circle"></i> Xin chào, <strong>${sessionScope.account.username}</strong>
                </a>
                <a href="${ctx}/logout" class="btn btn--ghost">
                  <i class="fas fa-right-from-bracket"></i> Đăng xuất
                </a>
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

  <!-- ===== HERO / FILTER SUMMARY ===== -->
  <section class="search-hero">
    <div class="container">
      <h1>Kết quả tìm kiếm xe</h1>
      <p>Điều chỉnh bộ lọc bên trái để thu hẹp kết quả.</p>

      <div class="filter-pills">
        <c:if test="${not empty type_id}">
          <span class="pill"><i class="fa-solid fa-tag"></i>
            <c:choose>
              <c:when test="${type_id == 1}">Xe số</c:when>
              <c:when test="${type_id == 2}">Xe ga</c:when>
              <c:when test="${type_id == 3}">Xe PKL</c:when>
              <c:otherwise>Loại #${type_id}</c:otherwise>
            </c:choose>
          </span>
        </c:if>
        <c:if test="${not empty start_date}">
          <span class="pill"><i class="fa-regular fa-calendar"></i> Bắt đầu: ${start_date}</span>
        </c:if>
        <c:if test="${not empty end_date}">
          <span class="pill"><i class="fa-regular fa-calendar-check"></i> Trả: ${end_date}</span>
        </c:if>
        <c:if test="${not empty max_price}">
          <span class="pill"><i class="fa-solid fa-money-bill"></i>
            Giá &le; <fmt:formatNumber value="${max_price}" type="number" /> đ/ngày
          </span>
        </c:if>
        <c:if test="${not empty keyword}">
          <span class="pill"><i class="fa-solid fa-magnifying-glass"></i> "${fn:escapeXml(keyword)}"</span>
        </c:if>

        <c:if test="${not empty type_id || not empty start_date || not empty end_date || not empty max_price || not empty keyword}">
          <a class="pill pill-clear" href="${ctx}/motorbikesearch"><i class="fa-solid fa-xmark"></i> Xóa bộ lọc</a>
        </c:if>
      </div>
    </div>
  </section>

  <!-- ===== MAIN LAYOUT (CÓ NỀN VIDEO) ===== -->
  <main class="container search-layout">

    <!-- NỀN VIDEO -->
    <div class="search-bg">
      <video autoplay muted loop playsinline preload="metadata"
             poster="${ctx}/images/demo_bike/back-PKL2.jpg">
        <source src="${ctx}/images/video/vid1.mp4" type="video/mp4"/>
      </video>
      <div class="bg-overlay"></div>
    </div>

    <!-- Sidebar filters -->
    <aside class="sidebar">
      <form class="card filter-card" id="filters" action="${ctx}/motorbikesearch" method="get">
        <input type="hidden" name="page" id="pageHidden" value="1"/>
        <input type="hidden" name="size" value="${size}"/>

        <h3><i class="fa-solid fa-sliders"></i> Bộ lọc</h3>

        <label for="type_id">Loại xe</label>
        <select id="type_id" name="type_id" class="control">
          <option value="">-- Tất cả --</option>
          <option value="1" <c:if test="${type_id==1}">selected</c:if>>Xe số</option>
          <option value="2" <c:if test="${type_id==2}">selected</c:if>>Xe ga</option>
          <option value="3" <c:if test="${type_id==3}">selected</c:if>>Xe PKL</option>
        </select>

        <label for="start_date">Bắt đầu</label>
        <input id="start_date" name="start_date" type="date" class="control" value="${start_date}"/>

        <label for="end_date">Trả xe</label>
        <input id="end_date" name="end_date" type="date" class="control" value="${end_date}"/>

        <label for="max_price">Giá tối đa (VND/ngày)</label>
        <input id="max_price" name="max_price" type="number" min="0" step="1000" class="control" value="${max_price}"/>

        <label for="keyword">Từ khóa</label>
        <input id="keyword" name="keyword" type="text" class="control" value="${fn:escapeXml(keyword)}" placeholder="Tên xe / biển số"/>

        <div style="display:flex; gap:8px; margin-top:12px">
          <button type="submit" class="btn-search" style="flex:1"><i class="fas fa-search"></i> Áp dụng</button>
          <a class="btn-gray" href="${ctx}/motorbikesearch" title="Xóa lọc"><i class="fas fa-times"></i></a>
        </div>
      </form>
    </aside>

    <!-- Results -->
    <section>
      <div class="results-top">
        <h2>
          <c:choose>
            <c:when test="${total > 0}">
              Tìm thấy <strong>${total}</strong> xe
            </c:when>
            <c:otherwise>
              Không có xe phù hợp
            </c:otherwise>
          </c:choose>
        </h2>
      </div>

      <c:choose>
        <c:when test="${empty items}">
          <div class="card no-results">
            <i class="fa-regular fa-face-frown"></i>
            Không tìm thấy xe nào theo bộ lọc hiện tại. Hãy thử nới lỏng điều kiện.
          </div>
        </c:when>
        <c:otherwise>
          <div class="bike-grid">
            <c:forEach var="b" items="${items}">

              <!-- Map thư mục ảnh theo loại -->
              <c:choose>
                <c:when test="${not empty b.typeName && fn:contains(fn:toLowerCase(b.typeName), 'số')}">
                  <c:set var="imgFolder" value="xe-so"/>
                </c:when>
                <c:when test="${not empty b.typeName && fn:contains(fn:toLowerCase(b.typeName), 'ga')}">
                  <c:set var="imgFolder" value="xe-ga"/>
                </c:when>
                <c:when test="${not empty b.typeName && (fn:contains(fn:toLowerCase(b.typeName), 'pkl') 
                                || fn:contains(fn:toLowerCase(b.typeName), 'phân khối'))}">
                  <c:set var="imgFolder" value="xe-pkl"/>
                </c:when>
                <c:when test="${b.typeId == 1}"><c:set var="imgFolder" value="xe-so"/></c:when>
                <c:when test="${b.typeId == 2}"><c:set var="imgFolder" value="xe-ga"/></c:when>
                <c:when test="${b.typeId == 3}"><c:set var="imgFolder" value="xe-pkl"/></c:when>
                <c:otherwise><c:set var="imgFolder" value="khac"/></c:otherwise>
              </c:choose>

              <div class="bike-card">
                <div class="bike-image">
                  <img src="${ctx}/images/bike/${imgFolder}/${b.bikeId}/1.jpg"
                       alt="${fn:escapeXml(b.bikeName)}"
                       onerror="this.onerror=null;this.src='${ctx}/images/bike_placeholder.jpg';">
                  <div class="bike-badge">
                    <c:choose>
                      <c:when test="${b.status == 'available'}">Sẵn sàng</c:when>
                      <c:when test="${b.status == 'rented'}">Đang thuê</c:when>
                      <c:otherwise>Bảo dưỡng</c:otherwise>
                    </c:choose>
                  </div>
                </div>
                <div class="bike-details">
                  <div class="bike-title">
                    <h3><a href="${ctx}/motorbikedetail?id=${b.bikeId}">${fn:escapeXml(b.bikeName)}</a></h3>
                    <div class="bike-price">
                      <fmt:formatNumber value="${b.pricePerDay}" type="number"/> đ/ngày
                    </div>
                  </div>
                  <div class="bike-specs">
                    <span><i class="fas fa-tag"></i>
                      <c:choose>
                        <c:when test="${not empty b.typeName}">${fn:escapeXml(b.typeName)}</c:when>
                        <c:otherwise>Không rõ loại</c:otherwise>
                      </c:choose>
                    </span>
                    <span><i class="fas fa-id-card"></i> ${fn:escapeXml(b.licensePlate)}</span>
                  </div>
                  <a class="bike-action" href="${ctx}/motorbikedetail?id=${b.bikeId}">Xem chi tiết</a>
                </div>
              </div>
            </c:forEach>
          </div>
        </c:otherwise>
      </c:choose>

      <!-- Pagination -->
      <c:if test="${totalPages > 1}">
        <div class="pagination">
          <c:forEach var="p" begin="1" end="${totalPages}">
            <c:url var="pageUrl" value="${ctx}/motorbikesearch">
              <c:param name="type_id" value="${type_id}" />
              <c:param name="start_date" value="${start_date}" />
              <c:param name="end_date" value="${end_date}" />
              <c:param name="max_price" value="${max_price}" />
              <c:param name="keyword" value="${keyword}" />
              <c:param name="page" value="${p}" />
              <c:param name="size" value="${size}" />
            </c:url>
            <a class="page-link ${p==page?'active':''}" href="${pageUrl}">${p}</a>
          </c:forEach>
        </div>
      </c:if>
    </section>
  </main>

  <!-- ===== FOOTER ===== -->
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
            <li><a href="${ctx}/#home"><i class="fas fa-chevron-right"></i> Trang chủ</a></li>
            <li><a href="#filters"><i class="fas fa-chevron-right"></i> Bộ lọc</a></li>
            <li><a href="${ctx}/#how-it-works"><i class="fas fa-chevron-right"></i> Cách thuê</a></li>
            <li><a href="${ctx}/#testimonials"><i class="fas fa-chevron-right"></i> Đánh giá</a></li>
            <li><a href="${ctx}/#contact"><i class="fas fa-chevron-right"></i> Liên hệ</a></li>
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

  <!-- ===== JS ===== -->
  <script>
    // Header shadow
    window.addEventListener('scroll', () => {
      const header = document.getElementById('header');
      if (window.scrollY > 50) header.classList.add('scrolled');
      else header.classList.remove('scrolled');
    });

    // Reset page=1 khi thay đổi filter
    (function(){
      const f = document.getElementById('filters');
      const pageHidden = document.getElementById('pageHidden');
      Array.from(f.elements).forEach(el => {
        if (el.name !== 'page') {
          el.addEventListener('change', () => { pageHidden.value = '1'; });
        }
      });
    })();

    // Ràng buộc ngày
    const today = new Date().toISOString().split('T')[0];
    const sd = document.getElementById('start_date');
    const ed = document.getElementById('end_date');
    [sd, ed].forEach(el => el && el.setAttribute('min', today));
    sd?.addEventListener('change', () => ed?.setAttribute('min', sd.value || today));
  </script>
</body>
</html>
