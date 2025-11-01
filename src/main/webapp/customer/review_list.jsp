<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Đánh giá xe máy</title>
        <style>
            body {
                font-family: 'Segoe UI', sans-serif;
                margin: 40px auto;
                max-width: 800px;
                background: #121212;
                color: #f0f0f0;
            }

            h2, h3 {
                text-align: center;
                color: #00bfff;
            }

            p {
                color: #ccc;
            }

            form {
                background: #1e1e1e;
                border-radius: 10px;
                box-shadow: 0 2px 6px rgba(0,0,0,0.3);
                padding: 20px;
                margin-top: 20px;
            }

            textarea {
                width: 100%;
                height: 80px;
                padding: 10px;
                border: 1px solid #444;
                border-radius: 6px;
                resize: none;
                background: #2a2a2a;
                color: #f0f0f0;
            }

            button {
                background: #00bfff;
                color: white;
                border: none;
                padding: 10px 20px;
                border-radius: 6px;
                cursor: pointer;
                font-size: 15px;
                transition: background 0.3s ease;
            }

            button:hover {
                background: #008ecc;
            }

            .stars {
                display: flex;
                gap: 5px;
                cursor: pointer;
                justify-content: center;
                margin-bottom: 10px;
            }

            .star {
                font-size: 26px;
                color: #555;
            }

            .review-card {
                background: #1e1e1e;
                margin-top: 15px;
                border-radius: 8px;
                padding: 15px;
                box-shadow: 0 1px 4px rgba(0,0,0,0.3);
            }

            .review-card .stars {
                justify-content: flex-start;
                gap: 2px;
            }

            .review-card .star-filled {
                color: orange;
                font-size: 20px;
            }

            .review-card .star-empty {
                color: #555;
                font-size: 20px;
            }

            .msg-success {
                text-align: center;
                color: #4caf50;
                font-weight: bold;
            }

            .msg-error {
                text-align: center;
                color: #f44336;
                font-weight: bold;
            }

            hr {
                border: none;
                border-top: 1px solid #333;
                margin: 30px 0;
            }

        </style>
    </head>
    <body>

        <h2>Đánh giá xe máy #${bikeId}</h2>
        <p style="text-align:center;color:#666;">(Đơn hàng #${orderId})</p>

        <c:if test="${not empty sessionScope.message}">
            <div class="msg-success">${sessionScope.message}</div>
            <c:remove var="message" scope="session"/>
        </c:if>
        <c:if test="${not empty error}">
            <div class="msg-error">${error}</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/review" onsubmit="return validateReviewForm();">
            <input type="hidden" name="orderId" value="${orderId}">
            <input type="hidden" name="bikeId" value="${bikeId}">
            <input type="hidden" name="rating" id="ratingValue" value="0">

            <label><b>Chọn số sao:</b></label>
            <div class="stars" id="starContainer">
                <span class="star" data-value="1">★</span>
                <span class="star" data-value="2">★</span>
                <span class="star" data-value="3">★</span>
                <span class="star" data-value="4">★</span>
                <span class="star" data-value="5">★</span>
            </div>

            <div id="ratingDisplay" style="text-align:center;font-weight:bold;margin-bottom:10px;"></div>

            <textarea name="comment" maxlength="500" placeholder="Nhập nhận xét của bạn..."></textarea><br><br>
            <button type="submit">Gửi đánh giá</button>
        </form>

        <hr>
        <h3>Các đánh giá gần đây về xe này</h3>

        <c:choose>
            <c:when test="${empty reviews}">
                <p>Chưa có đánh giá nào cho xe này.</p>
            </c:when>
            <c:otherwise>
                <c:forEach var="rwc" items="${reviews}">
                    <div class="review-card">
                        <div class="stars">
                            <c:forEach var="i" begin="1" end="${rwc.review.rating}">
                                <span class="star-filled">★</span>
                            </c:forEach>
                            <c:forEach var="j" begin="${rwc.review.rating + 1}" end="5">
                                <span class="star-empty">★</span>
                            </c:forEach>
                        </div>
                        <p><strong>${rwc.customerName}</strong></p> 
                        <p>${rwc.review.comment}</p>
                        <p><small>Ngày: ${rwc.review.createdAt} </small></p>
                    </div>
                </c:forEach>
            </c:otherwise>
        </c:choose>

        <script>
            const stars = document.querySelectorAll(".star");
            const ratingInput = document.getElementById("ratingValue");
            const ratingDisplay = document.getElementById("ratingDisplay");

            stars.forEach(star => {
                star.addEventListener("click", () => {
                    const value = parseInt(star.dataset.value);
                    ratingInput.value = value;

                    // Đặt màu cho sao trong form
                    stars.forEach(s => s.style.color = "#ccc");
                    for (let i = 0; i < value; i++) {
                        stars[i].style.color = "orange";
                    }

                    ratingDisplay.textContent = `Bạn đã chọn ${value} sao`;
                });
            });

            function validateReviewForm() {
                const rating = parseInt(ratingInput.value);
                if (isNaN(rating) || rating < 1 || rating > 5) {
                    alert("Vui lòng chọn số sao từ 1 đến 5.");
                    return false;
                }
                return true;
            }
        </script>

    </body>
</html>