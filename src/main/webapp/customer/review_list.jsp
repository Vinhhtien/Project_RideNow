<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đánh giá xe máy | RideNow</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        :root {
            --primary: #0b0b0d;
            --primary-light: #606064;
            --secondary: #22242b;
            --secondary-light: #2e3038;
            --accent: #3b82f6;
            --accent-dark: #1e40af;
            --accent-light: #60a5fa;
            --success: #10b981;
            --warning: #f59e0b;
            --error: #ef4444;
            --dark: #323232;
            --dark-light: #171922;
            --light: #f5f7fb;
            --gray: #9aa2b2;
            --gray-light: #cbd5e1;
            --gray-dark: #666b78;
            --white: #fff;
            --shadow-sm: 0 2px 6px rgba(0,0,0,.35);
            --shadow-md: 0 6px 14px rgba(0,0,0,.5);
            --radius: 8px;
            --radius-lg: 12px;
            --transition: .3s ease;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: 'Inter', 'Segoe UI', Tahoma, sans-serif;
            background: linear-gradient(135deg, #0a0b0d 0%, #121318 100%);
            color: var(--light);
            line-height: 1.6;
            min-height: 100vh;
            padding: 20px;
        }

        .container {
            max-width: 800px;
            margin: 0 auto;
        }

        /* Toolbar Styles */
        .toolbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
            padding-bottom: 15px;
            border-bottom: 1px solid var(--primary-light);
        }

        .back-btn {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 10px 20px;
            border-radius: var(--radius);
            text-decoration: none;
            font-weight: 600;
            transition: var(--transition);
            border: 1px solid var(--primary-light);
            background: var(--dark-light);
            color: var(--light);
        }

        .back-btn:hover {
            background: var(--primary-light);
            color: var(--accent);
            border-color: var(--accent);
            transform: translateY(-2px);
        }

        .page-title {
            font-size: 1.5rem;
            font-weight: 700;
            color: var(--accent);
            text-shadow: 0 0 10px rgba(59, 130, 246, 0.25);
        }

        .header {
            text-align: center;
            margin-bottom: 40px;
            padding: 30px;
            background: var(--dark-light);
            border-radius: var(--radius-lg);
            box-shadow: var(--shadow-md);
            border: 1px solid var(--primary-light);
        }

        .header h1 {
            font-size: 2.2rem;
            font-weight: 800;
            color: var(--accent);
            margin-bottom: 8px;
            text-shadow: 0 0 10px rgba(59, 130, 246, 0.25);
        }

        .header .subtitle {
            color: var(--gray-light);
            font-size: 1.1rem;
        }

        .bike-info {
            display: inline-flex;
            align-items: center;
            gap: 12px;
            background: var(--secondary);
            padding: 10px 20px;
            border-radius: 30px;
            margin-top: 15px;
            border: 1px solid var(--primary-light);
        }

        .review-form {
            background: var(--dark-light);
            border-radius: var(--radius-lg);
            padding: 30px;
            margin-bottom: 40px;
            box-shadow: var(--shadow-md);
            border: 1px solid var(--primary-light);
        }

        .form-title {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 24px;
            font-size: 1.4rem;
            font-weight: 700;
            color: var(--accent);
        }

        .form-title i {
            color: var(--accent);
        }

        .rating-section {
            margin-bottom: 24px;
        }

        .rating-label {
            display: block;
            margin-bottom: 12px;
            font-weight: 600;
            color: var(--gray-light);
        }

        .stars {
            display: flex;
            gap: 8px;
            margin-bottom: 12px;
        }

        .star {
            font-size: 32px;
            color: var(--gray-dark);
            cursor: pointer;
            transition: var(--transition);
            text-shadow: 0 2px 4px rgba(0,0,0,0.3);
        }

        .star:hover {
            transform: scale(1.2);
            color: var(--warning);
        }

        .star.active {
            color: var(--warning);
            text-shadow: 0 0 12px rgba(245, 158, 11, 0.5);
        }

        .rating-display {
            text-align: center;
            font-weight: 600;
            color: var(--warning);
            margin: 10px 0;
            min-height: 24px;
        }

        .comment-section {
            margin-bottom: 24px;
        }

        .comment-label {
            display: block;
            margin-bottom: 8px;
            font-weight: 600;
            color: var(--gray-light);
        }

        textarea {
            width: 100%;
            height: 120px;
            padding: 16px;
            border: 1px solid var(--primary-light);
            border-radius: var(--radius);
            background: var(--secondary);
            color: var(--light);
            font-family: inherit;
            font-size: 1rem;
            resize: vertical;
            transition: var(--transition);
        }

        textarea:focus {
            outline: none;
            border-color: var(--accent);
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }

        .char-count {
            text-align: right;
            color: var(--gray);
            font-size: 0.9rem;
            margin-top: 4px;
        }

        .submit-btn {
            background: var(--accent);
            color: var(--white);
            border: none;
            padding: 14px 28px;
            border-radius: var(--radius);
            font-size: 1rem;
            font-weight: 600;
            cursor: pointer;
            transition: var(--transition);
            display: inline-flex;
            align-items: center;
            gap: 8px;
            width: 100%;
            justify-content: center;
        }

        .submit-btn:hover:not(:disabled) {
            background: var(--accent-dark);
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(59, 130, 246, 0.3);
        }

        .submit-btn:disabled {
            opacity: 0.6;
            cursor: not-allowed;
            transform: none;
        }

        .reviews-section {
            background: var(--dark-light);
            border-radius: var(--radius-lg);
            padding: 30px;
            box-shadow: var(--shadow-md);
            border: 1px solid var(--primary-light);
        }

        .section-title {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 24px;
            font-size: 1.4rem;
            font-weight: 700;
            color: var(--accent);
        }

        .section-title i {
            color: var(--accent);
        }

        .review-card {
            background: var(--secondary);
            border-radius: var(--radius);
            padding: 24px;
            margin-bottom: 20px;
            border: 1px solid var(--primary-light);
            transition: var(--transition);
        }

        .review-card:hover {
            transform: translateY(-2px);
            box-shadow: var(--shadow-md);
        }

        .review-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 16px;
        }

        .reviewer-info {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background: linear-gradient(135deg, var(--accent), var(--accent-dark));
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-weight: 600;
            font-size: 1.1rem;
        }

        .reviewer-name {
            font-weight: 600;
            color: var(--light);
        }

        .review-date {
            color: var(--gray);
            font-size: 0.9rem;
        }

        .review-rating {
            display: flex;
            gap: 2px;
        }

        .review-star {
            color: var(--warning);
            font-size: 18px;
        }

        .review-comment {
            color: var(--gray-light);
            line-height: 1.6;
            margin-bottom: 16px;
        }

        .no-reviews {
            text-align: center;
            padding: 40px;
            color: var(--gray);
            background: var(--secondary);
            border-radius: var(--radius);
            border: 1px solid var(--primary-light);
        }

        .no-reviews i {
            font-size: 3rem;
            margin-bottom: 16px;
            color: var(--gray-dark);
        }

        .alert {
            padding: 16px 20px;
            border-radius: var(--radius);
            margin-bottom: 24px;
            display: flex;
            align-items: center;
            gap: 12px;
            border-left: 4px solid;
        }

        .alert-success {
            background: rgba(16, 185, 129, 0.15);
            color: #10b981;
            border-left-color: #10b981;
        }

        .alert-error {
            background: rgba(239, 68, 68, 0.15);
            color: #ef4444;
            border-left-color: #ef4444;
        }

        @media (max-width: 768px) {
            body {
                padding: 16px;
            }
            
            .toolbar {
                flex-direction: column;
                gap: 15px;
                align-items: flex-start;
            }
            
            .header h1 {
                font-size: 1.8rem;
            }
            
            .review-form,
            .reviews-section {
                padding: 20px;
            }
            
            .review-header {
                flex-direction: column;
                gap: 12px;
            }
            
            .stars {
                gap: 4px;
            }
            
            .star {
                font-size: 28px;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Toolbar với nút quay lại -->
        <div class="toolbar">
            <a class="back-btn" href="${pageContext.request.contextPath}/customerorders">
                <i class="fas fa-arrow-left"></i> Quay lại đơn hàng
            </a>
            <div class="page-title">
                <i class="fas fa-star"></i> Đánh giá xe máy
            </div>
        </div>

        <!-- Header Section -->
        <div class="header">
            <h1><i class="fas fa-comment-dots"></i> Chia sẻ trải nghiệm</h1>
            <p class="subtitle">Đánh giá giúp cộng đồng lựa chọn tốt hơn</p>
            <div class="bike-info">
                <i class="fas fa-motorcycle"></i>
                <span>Xe #${bikeId} • Đơn hàng #${orderId}</span>
            </div>
        </div>

        <!-- Flash Messages -->
        <c:if test="${not empty sessionScope.message}">
            <div class="alert alert-success">
                <i class="fas fa-check-circle"></i>
                ${sessionScope.message}
            </div>
            <c:remove var="message" scope="session"/>
        </c:if>
        
        <c:if test="${not empty error}">
            <div class="alert alert-error">
                <i class="fas fa-exclamation-circle"></i>
                ${error}
            </div>
        </c:if>

        <!-- Review Form -->
        <div class="review-form">
            <div class="form-title">
                <i class="fas fa-edit"></i>
                <span>Viết đánh giá của bạn</span>
            </div>

            <form method="post" action="${pageContext.request.contextPath}/review" onsubmit="return validateReviewForm()">
                <input type="hidden" name="orderId" value="${orderId}">
                <input type="hidden" name="bikeId" value="${bikeId}">
                <input type="hidden" name="rating" id="ratingValue" value="0">

                <div class="rating-section">
                    <label class="rating-label">Đánh giá sao:</label>
                    <div class="stars" id="starContainer">
                        <span class="star" data-value="1">★</span>
                        <span class="star" data-value="2">★</span>
                        <span class="star" data-value="3">★</span>
                        <span class="star" data-value="4">★</span>
                        <span class="star" data-value="5">★</span>
                    </div>
                    <div id="ratingDisplay" class="rating-display"></div>
                </div>

                <div class="comment-section">
                    <label class="comment-label">Nhận xét:</label>
                    <textarea name="comment" maxlength="500" placeholder="Hãy chia sẻ trải nghiệm của bạn với chiếc xe này..." id="commentField"></textarea>
                    <div class="char-count"><span id="charCount">0</span>/500 ký tự</div>
                </div>

                <button type="submit" class="submit-btn" id="submitBtn">
                    <i class="fas fa-paper-plane"></i> Gửi đánh giá
                </button>
            </form>
        </div>

        <!-- Reviews List -->
        <div class="reviews-section">
            <div class="section-title">
                <i class="fas fa-comments"></i>
                <span>Đánh giá từ khách hàng</span>
                <span style="color: var(--gray); margin-left: auto;">(<c:out value="${fn:length(reviews)}" /> đánh giá)</span>
            </div>

            <c:choose>
                <c:when test="${empty reviews}">
                    <div class="no-reviews">
                        <i class="fas fa-comment-slash"></i>
                        <h3>Chưa có đánh giá nào</h3>
                        <p>Hãy là người đầu tiên đánh giá chiếc xe này!</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <c:forEach var="rwc" items="${reviews}">
                        <div class="review-card">
                            <div class="review-header">
                                <div class="reviewer-info">
                                    <div class="avatar">
                                        <c:out value="${fn:substring(rwc.customerName, 0, 1)}" />
                                    </div>
                                    <div>
                                        <div class="reviewer-name"><c:out value="${rwc.customerName}" /></div>
                                        <div class="review-date">
                                            <i class="far fa-clock"></i> 
                                            <!-- Sửa lỗi: Xử lý LocalDateTime trực tiếp -->
                                            <c:set var="createdAt" value="${rwc.review.createdAt}" />
                                            <c:set var="datePart" value="${fn:substring(createdAt, 0, 10)}" />
                                            <c:set var="timePart" value="${fn:substring(createdAt, 11, 16)}" />
                                            <c:set var="day" value="${fn:substring(datePart, 8, 10)}" />
                                            <c:set var="month" value="${fn:substring(datePart, 5, 7)}" />
                                            <c:set var="year" value="${fn:substring(datePart, 0, 4)}" />
                                            ${day}/${month}/${year} ${timePart}
                                        </div>
                                    </div>
                                </div>
                                <div class="review-rating">
                                    <c:forEach var="i" begin="1" end="5">
                                        <c:choose>
                                            <c:when test="${i <= rwc.review.rating}">
                                                <span class="review-star fas">★</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="review-star far">★</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </div>
                            </div>
                            <div class="review-comment">
                                <c:out value="${rwc.review.comment}" />
                            </div>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <script>
        // Star Rating System
        const stars = document.querySelectorAll(".star");
        const ratingInput = document.getElementById("ratingValue");
        const ratingDisplay = document.getElementById("ratingDisplay");
        const commentField = document.getElementById("commentField");
        const charCount = document.getElementById("charCount");
        const submitBtn = document.getElementById("submitBtn");

        let currentRating = 0;

        stars.forEach(star => {
            star.addEventListener("click", () => {
                const value = parseInt(star.dataset.value);
                currentRating = value;
                ratingInput.value = value;

                // Update star appearance
                stars.forEach((s, index) => {
                    if (index < value) {
                        s.classList.add("active");
                    } else {
                        s.classList.remove("active");
                    }
                });

                // Update rating text
                const ratingTexts = [
                    "",
                    "Rất tệ",
                    "Tệ",
                    "Bình thường",
                    "Tốt",
                    "Tuyệt vời"
                ];
                ratingDisplay.textContent = `${value} sao • ${ratingTexts[value]}`;
                ratingDisplay.style.color = getRatingColor(value);
            });

            star.addEventListener("mouseenter", () => {
                const value = parseInt(star.dataset.value);
                stars.forEach((s, index) => {
                    if (index < value) {
                        s.style.color = "var(--warning)";
                    }
                });
            });

            star.addEventListener("mouseleave", () => {
                stars.forEach((s, index) => {
                    if (index >= currentRating) {
                        s.style.color = "var(--gray-dark)";
                    }
                });
            });
        });

        function getRatingColor(rating) {
            const colors = {
                1: "var(--error)",
                2: "#f97316",
                3: "#eab308",
                4: "#84cc16",
                5: "var(--success)"
            };
            return colors[rating] || "var(--warning)";
        }

        // Character counter
        commentField.addEventListener("input", () => {
            const count = commentField.value.length;
            charCount.textContent = count;
            
            if (count > 450) {
                charCount.style.color = "var(--warning)";
            } else {
                charCount.style.color = "var(--gray)";
            }
        });

        // Form validation
        function validateReviewForm() {
            const rating = parseInt(ratingInput.value);
            
            if (isNaN(rating) || rating < 1 || rating > 5) {
                alert("Vui lòng chọn số sao từ 1 đến 5.");
                return false;
            }

            // Show loading state
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang gửi...';
            submitBtn.disabled = true;

            return true;
        }

        // Initialize character count
        charCount.textContent = commentField.value.length;
    </script>
</body>
</html>