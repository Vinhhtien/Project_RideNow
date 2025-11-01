<%-- Partners / Bike List - show images like detail.jsp; hide img if missing (null) --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Xe của tôi | RideNow</title>

    <style>
        :root {
            --bg: #0f172a;
            --card: #1e293b;
            --text: #f1f5f9;
            --muted: #94a3b8;
            --primary: #3b82f6;
            --primary-2: #2563eb;
            --shadow: 0 12px 36px rgba(2, 6, 23, .45);
            --radius: 14px
        }

        * {
            box-sizing: border-box
        }

        body {
            margin: 0;
            font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Arial;
            color: var(--text);
            background: radial-gradient(1200px 600px at -10% -10%, #213060 0%, transparent 60%),
            radial-gradient(900px 600px at 110% -20%, #361d56 0%, transparent 55%),
            linear-gradient(180deg, #111a36 0%, #0b1229 100%);
        }

        .topbar {
            display: flex;
            justify-content: center;
            margin: 16px 0
        }

        .btn {
            display: inline-flex;
            gap: 8px;
            text-decoration: none;
            padding: 10px 14px;
            border-radius: 10px;
            font-weight: 700
        }

        .btn-secondary {
            background: rgba(255, 255, 255, .06);
            color: #e2e8f0;
            border: 1px solid rgba(255, 255, 255, .1)
        }

        .page-title {
            display: flex;
            justify-content: center;
            gap: 8px;
            margin: 24px 0 10px;
            font-weight: 900;
            font-size: clamp(18px, 2.2vw, 24px);
            background: linear-gradient(90deg, #eaf2ff, #b7ceff, #7aa7ff);
            -webkit-background-clip: text;
            background-clip: text;
            color: transparent
        }

        .container {
            width: min(1200px, 92%);
            margin: 20px auto 40px;
            display: grid;
            grid-template-columns:repeat(auto-fill, minmax(280px, 1fr));
            gap: 22px
        }

        .card {
            background: linear-gradient(180deg, rgba(255, 255, 255, .05), rgba(255, 255, 255, .02)), var(--card);
            border: 1px solid rgba(255, 255, 255, .07);
            border-radius: var(--radius);
            overflow: hidden;
            box-shadow: var(--shadow)
        }

        .thumb-wrap {
            width: 100%;
            height: 180px;
            overflow: hidden;
            border-bottom: 1px solid rgba(255, 255, 255, .08);
            background: rgba(255, 255, 255, .03)
        }

        .card img {
            width: 100%;
            height: 180px;
            object-fit: cover;
            display: block
        }

        .card-body {
            text-align: center;
            padding: 14px
        }

        .card-body h3 {
            margin: 8px 0 12px;
            font-size: 16px;
            font-weight: 800;
            color: #cfe0ff
        }

        .btn-detail {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            background: linear-gradient(135deg, var(--primary), var(--primary-2));
            color: #fff;
            padding: 10px 16px;
            border-radius: 10px;
            text-decoration: none;
            font-weight: 700
        }

        .empty-message {
            text-align: center;
            margin: 40px 0;
            color: var(--muted)
        }
    </style>
</head>
<body>
<div class="topbar">
    <a class="btn btn-secondary" href="${ctx}/dashboard">← Quay lại Dashboard</a>
</div>

<h2 class="page-title">Danh sách xe đang đăng bán</h2>

<c:choose>
    <c:when test="${not empty motorbikes}">
        <div class="container">
            <c:forEach var="bike" items="${motorbikes}">
                <%-- 1) Xác định thư mục ảnh như detail.jsp --%>
                <c:set var="imgFolder" value="khac"/>
                <c:if test="${bike.typeId == 1}"><c:set var="imgFolder" value="xe-so"/></c:if>
                <c:if test="${bike.typeId == 2}"><c:set var="imgFolder" value="xe-ga"/></c:if>
                <c:if test="${bike.typeId == 3}"><c:set var="imgFolder" value="xe-pkl"/></c:if>
                <c:if test="${imgFolder == 'khac' and not empty bike.typeName}">
                    <c:set var="typeLower" value="${fn:toLowerCase(bike.typeName)}"/>
                    <c:if test="${fn:contains(typeLower,'số') or fn:contains(typeLower,'so')}"><c:set var="imgFolder"
                                                                                                      value="xe-so"/></c:if>
                    <c:if test="${fn:contains(typeLower,'ga')}"><c:set var="imgFolder" value="xe-ga"/></c:if>
                    <c:if test="${fn:contains(typeLower,'pkl') or fn:contains(typeLower,'phân khối') or fn:contains(typeLower,'phan khoi')}"><c:set
                            var="imgFolder" value="xe-pkl"/></c:if>
                </c:if>

                <%-- 2) Thẻ Card --%>
                <div class="card">
                    <div class="thumb-wrap">
                            <%-- GIỐNG detail.jsp: dùng trực tiếp 1.jpg; nếu lỗi => coi như NULL: ẩn ảnh --%>
                        <img
                                src="${ctx}/images/bike/${imgFolder}/${bike.bikeId}/1.jpg"
                                alt="${fn:escapeXml(bike.bikeName)}"
                                onerror="this.dataset.null='1'; this.removeAttribute('src'); this.style.display='none';"/>
                    </div>

                    <div class="card-body">
                        <h3>${fn:escapeXml(bike.bikeName)}</h3>
                        <a class="btn-detail" href="${ctx}/viewmotorbike?id=${bike.bikeId}">
                            Xem chi tiết
                            <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" fill="none"
                                 stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M4 12h14"></path>
                                <path d="M12 5l7 7-7 7"></path>
                            </svg>
                        </a>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:when>
    <c:otherwise>
        <p class="empty-message">Bạn chưa đăng bán xe nào.</p>
    </c:otherwise>
</c:choose>

</body>
</html>