<%-- RideNow - Motorbike Detail (auto-pick images per folder, safe date format) --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <title>Chi tiết xe</title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <style>
        :root {
            --bg: #0f1221;
            --card: #121735;
            --card-2: #10152e;
            --text: #e8eaf6;
            --muted: #a7b0d6;
            --primary: #6c8cff;
            --primary-2: #9aaeff;
            --danger: #ff6b6b;
            --success: #34d399;
            --border: #1c244c;
            --shadow: 0 12px 36px rgba(3, 6, 22, .45);
            --radius: 18px;
            --ring: #9ab3ff;
            --ease: cubic-bezier(.22, .98, .24, .99);
            --glass: linear-gradient(180deg, rgba(255, 255, 255, .06), rgba(255, 255, 255, .025));
            --border-grad: linear-gradient(180deg, rgba(157, 172, 255, .55), rgba(157, 172, 255, 0)) border-box;
        }

        * {
            box-sizing: border-box
        }

        html, body {
            margin: 0
        }

        body {
            font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Arial, "Apple Color Emoji", "Segoe UI Emoji";
            background: radial-gradient(1200px 600px at -10% -10%, #24306b 0%, transparent 60%),
            radial-gradient(900px 600px at 110% -20%, #391d5f 0%, transparent 55%),
            linear-gradient(180deg, #0b0f1f 0%, #0a0d1c 100%);
            min-height: 100vh;
            color: var(--text);
            padding: 24px;
            -webkit-font-smoothing: antialiased;
            -moz-osx-font-smoothing: grayscale;
        }

        .wrap {
            max-width: 1080px;
            margin: 0 auto
        }

        .topbar {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
            margin-bottom: 18px
        }

        .crumbs a {
            color: var(--muted);
            text-decoration: none;
            font-size: 14px;
            display: inline-flex;
            gap: 6px;
            align-items: center;
            transition: color .18s var(--ease), transform .18s var(--ease);
        }

        .crumbs a:hover {
            color: var(--primary);
            transform: translateX(-1px)
        }

        .title {
            font-size: clamp(22px, 2.2vw, 30px);
            font-weight: 900;
            letter-spacing: .3px;
            margin: 6px 0 0;
            background: linear-gradient(90deg, #eef3ff, #c8d2ff, #7fa0ff 70%);
            -webkit-background-clip: text;
            background-clip: text;
            color: transparent;
            text-shadow: 0 2px 22px rgba(124, 152, 255, .16);
        }

        .grid {
            display: grid;
            grid-template-columns:1.1fr .9fr;
            gap: 18px
        }

        @media (max-width: 1080px) {
            .grid {
                grid-template-columns:1fr
            }
        }

        @media (max-width: 560px) {
            body {
                padding: 16px
            }
        }

        .card {
            background: var(--glass), var(--card);
            border: 1px solid rgba(255, 255, 255, .06);
            border-radius: var(--radius);
            box-shadow: var(--shadow);
            padding: 18px 18px 16px;
            position: relative;
            transition: transform .22s var(--ease), border-color .22s var(--ease), box-shadow .22s var(--ease);
        }

        .card::after {
            content: "";
            position: absolute;
            inset: 0;
            border-radius: inherit;
            padding: 1px;
            pointer-events: none;
            background: var(--border-grad);
            -webkit-mask: linear-gradient(#000 0 0) content-box, linear-gradient(#000 0 0);
            -webkit-mask-composite: xor;
            mask-composite: exclude;
        }

        .card:hover {
            transform: translateY(-2px);
            box-shadow: 0 16px 40px rgba(3, 6, 22, .55)
        }

        .card h3 {
            margin: 2px 0 12px;
            font-size: 16px;
            font-weight: 800;
            letter-spacing: .2px;
            color: #dbe3ff
        }

        .hero {
            border-radius: 14px;
            overflow: hidden;
            border: 1px solid rgba(255, 255, 255, .06);
            background: #0c1228
        }

        .hero img {
            width: 100%;
            aspect-ratio: 16/9;
            min-height: 260px;
            object-fit: cover;
            display: block;
            transition: transform .35s var(--ease), filter .35s var(--ease);
        }

        .hero:hover img {
            transform: scale(1.01);
            filter: saturate(1.05)
        }

        .kv {
            display: flex;
            gap: 12px;
            align-items: flex-start;
            background: rgba(255, 255, 255, .03);
            border: 1px solid rgba(255, 255, 255, .08);
            border-radius: 12px;
            padding: 12px 14px;
            margin-top: 10px;
        }

        .kv b {
            flex: 0 0 clamp(120px, 22%, 180px);
            color: #d8e1ff;
            font-weight: 800;
            letter-spacing: .2px;
            opacity: .95
        }

        .kv > div {
            flex: 1;
            overflow-wrap: anywhere;
            line-height: 1.55
        }

        .price {
            font-size: clamp(22px, 2.2vw, 30px);
            font-weight: 900;
            letter-spacing: .3px;
            margin: 0;
            background: linear-gradient(180deg, #8db1ff, #6c8cff 65%);
            -webkit-background-clip: text;
            background-clip: text;
            color: transparent;
            text-shadow: 0 2px 22px rgba(124, 152, 255, .25);
        }

        .badge {
            display: inline-block;
            border-radius: 999px;
            padding: 5px 10px;
            font-size: 11px;
            font-weight: 900;
            letter-spacing: .35px;
            border: 1px solid #3a4477;
            text-transform: uppercase;
            background: rgba(255, 255, 255, .03);
        }

        .ok {
            background: rgba(52, 211, 153, .18);
            color: #a7ffd8;
            border-color: rgba(52, 211, 153, .4)
        }

        .warn {
            background: rgba(245, 158, 11, .18);
            color: #ffe5b5;
            border-color: rgba(245, 158, 11, .4)
        }

        .bad {
            background: rgba(255, 107, 107, .18);
            color: #ffd0d0;
            border-color: rgba(255, 107, 107, .4)
        }

        .btn {
            appearance: none;
            border-radius: 12px;
            padding: 10px 14px;
            border: 1px solid rgba(255, 255, 255, .1);
            background: rgba(255, 255, 255, .02);
            color: var(--text);
            cursor: pointer;
            font-weight: 700;
            transition: transform .18s var(--ease), background .18s var(--ease), box-shadow .18s var(--ease), border-color .18s var(--ease);
            outline: none;
            position: relative;
        }

        .btn:hover {
            transform: translateY(-1px);
            background: rgba(255, 255, 255, .04)
        }

        .btn:focus-visible {
            box-shadow: 0 0 0 3px rgba(154, 174, 255, .35)
        }

        .btn-primary {
            background: linear-gradient(180deg, #9fb7ff, #6c8cff);
            color: #081028;
            border-color: #90adff;
            text-shadow: 0 1px 0 rgba(255, 255, 255, .4)
        }

        .btn-ghost {
            border: 1px dashed #3a4477
        }

        .rv-grid {
            display: grid;
            grid-template-columns:repeat(auto-fill, minmax(300px, 1fr));
            gap: 14px
        }

        .rv {
            display: grid;
            grid-template-columns:88px 1fr;
            gap: 12px;
            border: 1px solid rgba(255, 255, 255, .08);
            border-radius: 12px;
            padding: 12px;
            background: rgba(255, 255, 255, .03)
        }

        .rv .thumb {
            width: 88px;
            height: 88px;
            border-radius: 10px;
            overflow: hidden;
            background: #0c1228;
            border: 1px solid rgba(255, 255, 255, .06)
        }

        .rv .thumb img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block
        }

        .rv .title {
            font-weight: 900;
            font-size: 15px;
            margin: 0 0 4px;
            color: #eaf0ff
        }

        .rv .meta {
            font-size: 12px;
            color: var(--muted)
        }

        .rv .comment {
            margin-top: 8px;
            font-size: 14px;
            line-height: 1.55
        }

        .stars {
            display: flex;
            gap: 2px;
            margin: 4px 0
        }

        .star {
            font-size: 16px;
            line-height: 1;
            text-shadow: 0 1px 6px rgba(255, 215, 107, .25)
        }

        .star.filled {
            color: #ffd76b
        }

        .star.empty {
            opacity: .5
        }

        .empty {
            padding: 16px;
            border: 1px dashed rgba(255, 255, 255, .12);
            border-radius: 12px;
            color: var(--muted);
            text-align: center;
            background: rgba(255, 255, 255, .02)
        }

        @media (prefers-reduced-motion: reduce) {
            .card, .btn, .hero img, .rv {
                transition: none
            }
        }
    </style>
</head>
<body>
<div class="wrap">
    <div class="topbar">
        <div>
            <div class="crumbs">
                <a href="${ctx}/viewmotorbike">← Quay lại danh sách</a>
            </div>
            <h1 class="title">Chi tiết xe</h1>
        </div>
    </div>

    <%-- DỮ LIỆU & MAP FOLDER ẢNH --%>
    <c:set var="bd" value="${bikeDetail}"/>
    <c:set var="statusLower" value="${empty bd.status ? '' : fn:toLowerCase(bd.status)}"/>

    <c:set var="imgFolder" value="khac"/>
    <c:if test="${bd.typeId == 1}"><c:set var="imgFolder" value="xe-so"/></c:if>
    <c:if test="${bd.typeId == 2}"><c:set var="imgFolder" value="xe-ga"/></c:if>
    <c:if test="${bd.typeId == 3}"><c:set var="imgFolder" value="xe-pkl"/></c:if>
    <c:if test="${imgFolder == 'khac' and not empty bd.typeName}">
        <c:set var="__t" value="${fn:toLowerCase(bd.typeName)}"/>
        <c:if test="${fn:contains(__t,'số') or fn:contains(__t,'so')}"><c:set var="imgFolder" value="xe-so"/></c:if>
        <c:if test="${fn:contains(__t,'ga')}"><c:set var="imgFolder" value="xe-ga"/></c:if>
        <c:if test="${fn:contains(__t,'pkl') or fn:contains(__t,'phân khối') or fn:contains(__t,'phan khoi')}">
            <c:set var="imgFolder" value="xe-pkl"/>
        </c:if>
    </c:if>

    <c:choose>
        <c:when test="${fn:contains(statusLower,'available')}"><c:set var="statusCls" value="ok"/></c:when>
        <c:when test="${fn:contains(statusLower,'rent') or fn:contains(statusLower,'hold')}"><c:set var="statusCls"
                                                                                                    value="warn"/></c:when>
        <c:when test="${fn:contains(statusLower,'maintenance') or fn:contains(statusLower,'maintain')}"><c:set
                var="statusCls" value="bad"/></c:when>
        <c:otherwise><c:set var="statusCls" value="warn"/></c:otherwise>
    </c:choose>

    <div class="grid">
        <div class="card">
            <div class="hero">
                <%-- ẢNH CHÍNH: bắt đầu bằng wave-110.jpg, JS sẽ dò 1/2/3.jpg và thay nếu tồn tại --%>
                <img
                        class="probe-img"
                        src="${ctx}/images/bike/wave-110.jpg"
                        data-base="${ctx}/images/bike/${imgFolder}/${bd.bikeId}"
                        alt="${fn:escapeXml(bd.bikeName)}"
                        onerror="this.onerror=null;this.src='${ctx}/images/bike_placeholder.jpg';">
            </div>

            <h3 style="margin-top:12px">Thông tin xe</h3>

            <c:choose>
                <c:when test="${empty bd}">
                    <div class="empty">Không có dữ liệu xe.</div>
                </c:when>

                <%-- MotorbikeListItem (có typeName) --%>
                <c:when test="${not empty bd.typeName}">
                    <div class="kv"><b>Tên xe</b>
                        <div><c:out value="${bd.bikeName}"/></div>
                    </div>
                    <div class="kv"><b>Biển số</b>
                        <div><c:out value="${bd.licensePlate}"/></div>
                    </div>
                    <div class="kv"><b>Loại xe</b>
                        <div><c:out value="${bd.typeName}"/></div>
                    </div>
                    <div class="kv"><b>Giá/ngày</b>
                        <div><span class="price"><fmt:formatNumber value="${bd.pricePerDay}" type="number"
                                                                   minFractionDigits="0"/></span> ₫
                        </div>
                    </div>
                    <div class="kv"><b>Trạng thái</b>
                        <div><span class="badge ${statusCls}"><c:out value="${bd.status}"/></span></div>
                    </div>
                    <c:if test="${not empty bd.description}">
                        <div class="kv"><b>Mô tả</b>
                            <div><c:out value="${bd.description}"/></div>
                        </div>
                    </c:if>
                </c:when>

                <%-- Motorbike model (chỉ có typeId) --%>
                <c:otherwise>
                    <div class="kv"><b>Tên xe</b>
                        <div><c:out value="${bd.bikeName}"/></div>
                    </div>
                    <div class="kv"><b>Biển số</b>
                        <div><c:out value="${bd.licensePlate}"/></div>
                    </div>
                    <div class="kv"><b>Loại xe ID</b>
                        <div><c:out value="${bd.typeId}"/></div>
                    </div>
                    <div class="kv"><b>Giá/ngày</b>
                        <div><span class="price"><fmt:formatNumber value="${bd.pricePerDay}" type="number"
                                                                   minFractionDigits="0"/></span> ₫
                        </div>
                    </div>
                    <div class="kv"><b>Trạng thái</b>
                        <div><span class="badge ${statusCls}"><c:out value="${bd.status}"/></span></div>
                    </div>
                    <c:if test="${not empty bd.description}">
                        <div class="kv"><b>Mô tả</b>
                            <div><c:out value="${bd.description}"/></div>
                        </div>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="card">
            <h3>Thông tin nhanh</h3>
            <div class="kv"><b>Giá/ngày</b>
                <div><span class="price"><fmt:formatNumber value="${bd.pricePerDay}" type="number"
                                                           minFractionDigits="0"/></span> ₫
                </div>
            </div>
            <div class="kv"><b>Trạng thái</b>
                <div><span class="badge ${statusCls}"><c:out value="${bd.status != null ? bd.status : '—'}"/></span>
                </div>
            </div>
            <div class="kv"><b>Biển số</b>
                <div><c:out value="${bd.licensePlate}"/></div>
            </div>
            <div class="kv"><b>Mã xe</b>
                <div>#<c:out value="${bd.bikeId}"/></div>
            </div>
        </div>
    </div>

    <div class="card" style="margin-top:18px">
        <h3>Đánh giá từ khách hàng</h3>
        <c:set var="REVIEWS" value="${reviews}"/>
        <c:choose>
            <c:when test="${empty REVIEWS}">
                <div class="empty">Chưa có đánh giá nào cho xe này.</div>
            </c:when>
            <c:otherwise>
                <div class="rv-grid">
                    <c:forEach var="rv" items="${REVIEWS}">
                        <div class="rv">
                            <div class="thumb">
                                    <%-- Ảnh thumbnail: dùng cùng base với ảnh chính, cũng dò 1→2→3.jpg --%>
                                <img
                                        class="probe-img"
                                        src="${ctx}/images/bike/wave-110.jpg"
                                        data-base="${ctx}/images/bike/${imgFolder}/${bd.bikeId}"
                                        alt="${fn:escapeXml(bd.bikeName)}"
                                        onerror="this.onerror=null;this.src='${ctx}/images/bike_placeholder.jpg';">
                            </div>
                            <div>
                                <div class="title">Khách #<c:out value="${rv.customerId}"/></div>
                                <div class="stars">
                                    <c:set var="ratingVal" value="${rv.rating != null ? rv.rating : 0}"/>
                                    <c:forEach var="i" begin="1" end="5">
                                        <span class="star ${i <= ratingVal ? 'filled' : 'empty'}">★</span>
                                    </c:forEach>
                                    <span class="badge" style="margin-left:6px">★ <b><c:out value="${ratingVal}"/></b>/5</span>
                                </div>

                                <div class="meta">
                                    <c:choose>
                                        <c:when test="${not empty rv.createdAt}">
                                            <c:catch var="__fmtErr"><fmt:formatDate value="${rv.createdAt}"
                                                                                    pattern="HH:mm dd/MM/yyyy"
                                                                                    var="__fmtOut"/></c:catch>
                                            <c:choose>
                                                <c:when test="${empty __fmtErr and not empty __fmtOut}"><c:out
                                                        value="${__fmtOut}"/></c:when>
                                                <c:otherwise>
                                                    <c:set var="__printed" value="false"/>
                                                    <c:catch var="__p1err"><fmt:parseDate value="${rv.createdAt}"
                                                                                          pattern="yyyy-MM-dd'T'HH:mm:ssX"
                                                                                          var="__p1"/></c:catch>
                                                    <c:if test="${empty __p1err and not empty __p1}"><fmt:formatDate
                                                            value="${__p1}" pattern="HH:mm dd/MM/yyyy"/><c:set
                                                            var="__printed" value="true"/></c:if>
                                                    <c:if test="${not __printed}">
                                                        <c:catch var="__p2err"><fmt:parseDate value="${rv.createdAt}"
                                                                                              pattern="yyyy-MM-dd'T'HH:mm:ss"
                                                                                              var="__p2"/></c:catch>
                                                        <c:if test="${empty __p2err and not empty __p2}"><fmt:formatDate
                                                                value="${__p2}" pattern="HH:mm dd/MM/yyyy"/><c:set
                                                                var="__printed" value="true"/></c:if>
                                                    </c:if>
                                                    <c:if test="${not __printed}">
                                                        <c:catch var="__p3err"><fmt:parseDate value="${rv.createdAt}"
                                                                                              pattern="yyyy-MM-dd HH:mm:ss"
                                                                                              var="__p3"/></c:catch>
                                                        <c:if test="${empty __p3err and not empty __p3}"><fmt:formatDate
                                                                value="${__p3}" pattern="HH:mm dd/MM/yyyy"/><c:set
                                                                var="__printed" value="true"/></c:if>
                                                    </c:if>
                                                    <c:if test="${not __printed}"><c:out
                                                            value="${rv.createdAt}"/></c:if>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:when>
                                        <c:otherwise>—</c:otherwise>
                                    </c:choose>
                                </div>

                                <div class="comment"><c:out value="${rv.comment}"/></div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

</div>

<script>
    // Dò ảnh: với mỗi .probe-img, thử base/1.jpg -> base/2.jpg -> base/3.jpg
    document.addEventListener('DOMContentLoaded', () => {
        const probes = document.querySelectorAll('.probe-img');
        probes.forEach(el => {
            const base = el.getAttribute('data-base'); // .../images/bike/{imgFolder}/{bikeId}
            if (!base) return;
            const candidates = [base + '/1.jpg', base + '/2.jpg', base + '/3.jpg'];

            (async () => {
                for (const url of candidates) {
                    const ok = await check(url);
                    if (ok) {
                        el.src = url;
                        return;
                    }
                }
                // nếu không ảnh nào có: giữ nguyên wave-110.jpg (src hiện tại)
                // nếu wave-110.jpg không tồn tại: onerror sẽ rơi về bike_placeholder.jpg
            })();

            function check(url) {
                return new Promise(res => {
                    const img = new Image();
                    img.onload = () => res(true);
                    img.onerror = () => res(false);
                    img.src = url;
                });
            }
        });
    });
</script>
</body>
</html>