<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đánh giá dịch vụ</title>
    <style>
        body {
            font-family: 'Segoe UI', sans-serif;
            background: radial-gradient(circle at top, #0f172a, #020617);
            color: #fff;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
        }

        .review-card {
            background: rgba(15, 23, 42, 0.95);
            padding: 2rem;
            border-radius: 20px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
            width: 380px;
            text-align: center;
            animation: fadeIn 0.6s ease;
        }

        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }

        h2 {
            margin-bottom: 1rem;
            color: #fff;
        }

        .stars {
            display: flex;
            justify-content: center;
            gap: 8px;
            margin-bottom: 1rem;
            flex-direction: row;
        }

        .stars input {
            display: none;
        }

        .stars label {
            font-size: 28px;
            color: #475569;
            cursor: pointer;
            transition: color 0.3s;
        }

        .stars input:checked ~ label,
        .stars label:hover,
        .stars label:hover ~ label {
            color: #FFD700;
        }

        textarea {
            width: 100%;
            height: 90px;
            border-radius: 10px;
            border: none;
            padding: 10px;
            resize: none;
            outline: none;
            background: #1e293b;
            color: #fff;
            margin-bottom: 1rem;
            font-family: 'Segoe UI', sans-serif;
        }

        .btn {
            display: block;
            width: 100%;
            padding: 10px;
            border-radius: 8px;
            border: none;
            cursor: pointer;
            font-weight: bold;
            transition: 0.3s;
            font-size: 15px;
        }

        .btn-submit {
            background-color: #2563eb;
            color: white;
        }

        .btn-submit:hover {
            background-color: #1d4ed8;
        }

        .btn-home {
            background-color: #475569;
            color: white;
            margin-top: 0.75rem;
        }

        .btn-home:hover {
            background-color: #334155;
        }

        .message {
            color: #22c55e;
            margin-top: 15px;
            font-weight: 500;
        }
    </style>
</head>
<body>
    <div class="review-card">
        <h2>Đánh giá dịch vụ</h2>

        <!-- Nếu chưa gửi đánh giá -->
        <c:if test="${empty message}">
            <form action="${ctx}/storereview" method="post">
                <div class="stars">
                    <input type="radio" id="star5" name="rating" value="5"><label for="star5">★</label>
                    <input type="radio" id="star4" name="rating" value="4"><label for="star4">★</label>
                    <input type="radio" id="star3" name="rating" value="3"><label for="star3">★</label>
                    <input type="radio" id="star2" name="rating" value="2"><label for="star2">★</label>
                    <input type="radio" id="star1" name="rating" value="1"><label for="star1">★</label>
                </div>
                <textarea name="comment" placeholder="Viết nhận xét của bạn..."></textarea>
                <button type="submit" class="btn btn-submit">Gửi đánh giá</button>
            </form>
        </c:if>

        <!-- Sau khi gửi đánh giá -->
        <c:if test="${not empty message}">
            <p class="message">${message}</p>
            <form action="${ctx}/home.jsp" method="get">
                <button type="submit" class="btn btn-home">🏠 Quay về trang chủ</button>
            </form>
        </c:if>
    </div>
</body>
</html>
