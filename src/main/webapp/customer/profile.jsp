<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <title>Hồ sơ khách hàng | RideNow</title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <!-- Icons + Fonts + Global style -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap"
          rel="stylesheet">
    <link rel="stylesheet" href="${ctx}/css/homeStyle.css"/>

    <style>
        :root {
            --primary: #5bc0be;
            --surface: #0f172a;
            --border: #1f2937;
            --text: #e2e8f0;
            --text-dim: #94a3b8;
            --ok: #10b981;
            --err: #ef4444;
        }

        body {
            background: #0b132b;
            color: var(--text);
            font-family: Inter, system-ui;
        }

        .profile-wrap {
            max-width: 1100px;
            margin: 120px auto 40px;
            padding: 0 16px;
        }

        .profile-header {
            display: flex;
            align-items: center;
            gap: 14px;
            margin-bottom: 18px;
        }

        .profile-header i {
            color: var(--primary);
        }

        .grid {
            display: grid;
            grid-template-columns: 1fr;
            gap: 18px;
        }

        @media (min-width: 980px) {
            .grid {
                grid-template-columns: 1.2fr 0.8fr;
            }
        }

        .card {
            background: rgba(17, 24, 39, 0.9);
            border: 1px solid var(--border);
            border-radius: 16px;
            padding: 18px;
            box-shadow: 0 10px 25px rgba(0, 0, 0, .25);
            backdrop-filter: blur(8px);
        }

        .card h2 {
            font-size: 20px;
            margin: 0 0 14px;
        }

        .card p.helper {
            color: var(--text-dim);
            margin-top: -6px;
            margin-bottom: 14px;
            font-size: 14px;
        }

        label {
            display: block;
            margin: 10px 0 6px;
            color: #cbd5e1;
            font-weight: 500;
        }

        input, textarea {
            width: 100%;
            padding: 12px 14px;
            border-radius: 12px;
            border: 1px solid #334155;
            background: #0f172a;
            color: #fff;
            outline: none;
        }

        input:focus, textarea:focus {
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgba(91, 192, 190, .18);
        }

        .btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            padding: 12px 16px;
            border: 0;
            border-radius: 12px;
            background: linear-gradient(135deg, #6a11cb 0%, #5bc0be 100%);
            color: #fff;
            font-weight: 700;
            cursor: pointer;
            transition: .25s;
        }

        .btn:hover {
            transform: translateY(-1px);
            filter: brightness(1.05);
        }

        .btn.ghost {
            background: transparent;
            border: 1px solid #334155;
            color: #e2e8f0;
            font-weight: 600;
        }

        .note {
            padding: 12px 14px;
            border-radius: 10px;
            margin: 10px 0 14px;
            border-left: 4px solid var(--ok);
            background: rgba(16, 185, 129, 0.1);
            color: var(--ok);
        }

        .note.err {
            border-left-color: var(--err);
            background: rgba(239, 68, 68, .12);
            color: #fecaca;
        }

        .row-two {
            display: grid;
            grid-template-columns:1fr;
            gap: 12px;
        }

        @media (min-width: 680px) {
            .row-two {
                grid-template-columns:1fr 1fr;
            }
        }

        .divider {
            height: 1px;
            background: #1f2937;
            margin: 12px 0;
        }

        #header {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            z-index: 100;
        }

        .profile-actions {
            display: flex;
            gap: 10px;
            align-items: center;
            flex-wrap: wrap;
            margin-top: 10px;
        }
    </style>
</head>
<body>

<!-- ===== HEADER (copy từ home.jsp) ===== -->
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
                <nav id="mainNav">
                    <ul>
                        <li><a href="${ctx}/home.jsp" class="nav-link">Trang chủ</a></li>
                        <li><a href="${ctx}/motorbikesearch" class="nav-link">Tìm xe</a></li>
                        <li><a href="#profile" class="nav-link active">Hồ sơ</a></li>
                    </ul>
                </nav>

                <!-- AUTH -->
                <div class="auth" id="authDesktop">
                    <a href="${ctx}/cart" class="btn btn--ghost"><i class="fas fa-shopping-cart"></i> Giỏ hàng</a>

                    <c:choose>
                        <c:when test="${not empty sessionScope.account}">
                            <a href="${ctx}/customer/profile" class="btn btn--ghost">
                                <i class="fas fa-user-circle"></i>
                                Xin chào,
                                <strong>${sessionScope.customerName != null ? sessionScope.customerName : sessionScope.account.username}</strong>
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
            </div>
        </div>
    </div>
</header>

<!-- ===== PROFILE ===== -->
<main class="profile-wrap" id="profile">
    <div class="profile-header">
        <i class="fas fa-user-circle fa-2x"></i>
        <div>
            <h1 style="margin:0;font-size:24px">Hồ sơ khách hàng</h1>
            <div class="profile-actions">
                <a class="btn ghost" href="${ctx}/home.jsp"><i class="fas fa-arrow-left"></i> Về trang chủ</a>
            </div>
        </div>
    </div>

    <!-- Flash -->
    <c:if test="${not empty sessionScope.flash}">
        <div class="note"><i class="fas fa-check-circle"></i> ${sessionScope.flash}</div>
        <c:remove var="flash" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.flash_err}">
        <div class="note err"><i class="fas fa-triangle-exclamation"></i> ${sessionScope.flash_err}</div>
        <c:remove var="flash_err" scope="session"/>
    </c:if>

    <div class="grid">
        <!-- Thông tin cá nhân -->
        <section class="card">
            <h2><i class="fas fa-id-card-clip"></i> Thông tin cá nhân</h2>
            <p class="helper">Cập nhật họ tên, email và thông tin liên hệ.</p>

            <form method="post" action="${ctx}/customer/profile" autocomplete="on">
                <div class="row-two">
                    <div>
                        <label for="full_name">Họ tên</label>
                        <input id="full_name" name="full_name" type="text"
                               value="${profile != null ? profile.fullName : ''}" required>
                    </div>
                    <div>
                        <label for="email">Email</label>
                        <input id="email" name="email" type="email" value="${profile != null ? profile.email : ''}"
                               required>
                    </div>
                </div>

                <div class="row-two">
                    <div>
                        <label for="phone">Điện thoại</label>
                        <input id="phone" name="phone" type="text" value="${profile != null ? profile.phone : ''}">
                    </div>
                    <div>
                        <label for="address">Địa chỉ</label>
                        <input id="address" name="address" type="text"
                               value="${profile != null ? profile.address : ''}">
                    </div>
                </div>

                <div class="divider"></div>
                <button class="btn" type="submit"><i class="fas fa-floppy-disk"></i> Lưu thay đổi</button>
            </form>
        </section>

        <!-- Đổi mật khẩu -->
        <section class="card" id="security">
            <h2><i class="fas fa-key"></i> Đổi mật khẩu</h2>
            <p class="helper">Vì lý do bảo mật, hãy dùng mật khẩu mạnh.</p>

            <form method="post" action="${ctx}/customer/profile" autocomplete="off">
                <input type="hidden" name="action" value="changePassword"/>

                <label for="current_password">Mật khẩu hiện tại</label>
                <input id="current_password" name="current_pw" type="password" required>

                <label for="new_password" style="margin-top:10px">Mật khẩu mới</label>
                <input id="new_password" name="new_pw" type="password" required>

                <label for="confirm_password" style="margin-top:10px">Xác nhận mật khẩu mới</label>
                <input id="confirm_password" name="confirm_pw" type="password" required>

                <div class="divider"></div>
                <button class="btn" type="submit"><i class="fas fa-rotate"></i> Cập nhật mật khẩu</button>
            </form>
        </section>
    </div>
</main>

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
    window.addEventListener('scroll', () => {
        const h = $('#header');
        if (window.scrollY > 50) h.classList.add('scrolled'); else h.classList.remove('scrolled');
    });
</script>
</body>
</html>
