<%-- quy + an (modernized, safe) --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8" />
  <title>Đánh giá xe</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <style>
    :root{
      --bg:#0f1221; --card:#121735; --card-2:#10152e; --text:#e8eaf6; --muted:#a7b0d6;
      --primary:#6c8cff; --primary-2:#9aaeff; --danger:#ff6b6b; --success:#34d399;
      --border:#1c244c; --shadow:0 10px 30px rgba(0,0,0,.35); --radius:16px;
    }
    *{box-sizing:border-box}
    html,body{margin:0}
    body{
      font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Arial, "Apple Color Emoji","Segoe UI Emoji";
      background:
        radial-gradient(1200px 600px at -10% -10%, #24306b 0%, transparent 60%),
        radial-gradient(900px 600px at 110% -20%, #391d5f 0%, transparent 55%),
        linear-gradient(180deg, #0b0f1f 0%, #0a0d1c 100%);
      min-height:100vh; color:var(--text); padding:24px;
    }
    .wrap{max-width:1100px;margin:0 auto}

    .topbar{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-bottom:18px}
    .crumbs a{color:var(--muted);text-decoration:none;font-size:14px}
    .crumbs a:hover{color:var(--primary)}
    .title{
      font-size:28px; font-weight:800; letter-spacing:.3px; margin:6px 0 0;
      background:linear-gradient(90deg,#eaf0ff,#b9c7ff,#6c8cff);
      -webkit-background-clip:text;background-clip:text;color:transparent;
    }

    .card{
      background:linear-gradient(180deg, rgba(255,255,255,.03), rgba(255,255,255,.015)), var(--card);
      border:1px solid var(--border); border-radius:var(--radius); box-shadow:var(--shadow);
      padding:18px 18px 16px;
    }
    .toolbar{display:flex;gap:10px;align-items:center;margin-bottom:12px;flex-wrap:wrap}
    .input,.btn{
      border-radius:12px;border:1px solid var(--border);background:var(--card-2);color:var(--text);
      padding:10px 12px;font-size:14px;outline:none;
    }
    .btn{cursor:pointer;transition:filter .15s,transform .06s}
    .btn:hover{filter:brightness(1.1)}
    .btn:active{transform:translateY(1px)}
    .btn-primary{background:linear-gradient(180deg,#7da1ff,#6c8cff);color:#0a0f21;border:1px solid #90adff}
    .btn-ghost{background:transparent;border:1px dashed #3a4477}

    .stats{display:flex;gap:12px;align-items:center;color:var(--muted);font-size:13px;margin-bottom:10px}
    .badge{display:inline-block;border-radius:999px;padding:4px 10px;font-size:11px;font-weight:800;border:1px solid #3a4477;letter-spacing:.3px}
    .badge-ok{background:rgba(52,211,153,.2);color:#a7ffd8;border-color:rgba(52,211,153,.45)}

    .grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(300px,1fr));gap:14px}
    .rv{display:grid;grid-template-columns:90px 1fr;gap:12px;border:1px solid var(--border);border-radius:14px;padding:12px;background:rgba(255,255,255,.03)}
    .thumb{width:90px;height:90px;border-radius:10px;overflow:hidden;background:#0c1228;border:1px solid var(--border)}
    .thumb img{width:100%;height:100%;object-fit:cover;display:block}
    .rv-title{font-weight:800;font-size:15px;margin:0 0 4px}
    .meta{color:var(--muted);font-size:12px}
    .comment{margin-top:8px;font-size:14px;line-height:1.5}
    .stars{display:flex;gap:2px;margin:4px 0}
    .star{font-size:16px;line-height:1}
    .star.filled{color:#ffd76b}
    .star.empty{opacity:.55}

    .empty-state{padding:18px;border:1px dashed var(--border);border-radius:12px;text-align:center;color:var(--muted)}
  </style>
</head>
<body>
  <div class="wrap">
    <div class="topbar">
      <div>
        <div class="crumbs">
          <a href="${pageContext.request.contextPath}/partner?action=profile">← Quay lại Dashboard</a>
        </div>
        <h1 class="title">Đánh giá xe</h1>
      </div>
    </div>

    <!-- Gom dữ liệu -->
    <c:set var="REVIEWS" value="${not empty reviews ? reviews : (not empty reviewList ? reviewList : null)}" />

    <div class="card">
      <form method="get" action="${pageContext.request.contextPath}/viewreviewservlet" class="toolbar" autocomplete="off">
        <input class="input" type="text" name="bikeId" placeholder="Lọc theo Bike ID (tuỳ chọn)" value="${param.bikeId}" inputmode="numeric" />
        <button class="btn btn-primary" type="submit">Lọc</button>
        <a class="btn btn-ghost" href="${pageContext.request.contextPath}/viewreviewservlet">Bỏ lọc</a>
      </form>

      <div class="stats">
        <span class="badge badge-ok">Tổng: <b><c:out value="${fn:length(REVIEWS)}"/></b></span>
        <c:if test="${not empty param.bikeId}">
          <span>• Bộ lọc: Bike #<b><c:out value="${param.bikeId}"/></b></span>
        </c:if>
      </div>

      <c:choose>
        <c:when test="${empty REVIEWS}">
          <div class="empty-state">
            Chưa có đánh giá nào để hiển thị.
            <div style="margin-top:10px">
              <a class="btn btn-ghost" href="${pageContext.request.contextPath}/viewmotorbike">→ Xe của tôi</a>
            </div>
          </div>
        </c:when>
        <c:otherwise>
          <div class="grid">
            <c:forEach var="rv" items="${REVIEWS}">
              <div class="rv">
                <div class="thumb">
                  <img src="<c:url value='/images/bike/wave-110.jpg'/>"
                       alt="Ảnh xe"
                       onerror="this.onerror=null;this.src='${pageContext.request.contextPath}/images/ridenow_Logo.jpg';">
                </div>
                <div>
                  <div class="rv-title">Xe #<c:out value="${rv.bikeId}"/></div>

                  <div class="stars">
                    <c:set var="ratingVal" value="${rv.rating != null ? rv.rating : 0}"/>
                    <c:forEach var="i" begin="1" end="5">
                      <span class="star ${i <= ratingVal ? 'filled' : 'empty'}">★</span>
                    </c:forEach>
                    <span class="badge" style="margin-left:6px">★ <b><c:out value="${ratingVal}"/></b>/5</span>
                  </div>

                  <div class="meta">
                    Bởi KH #<c:out value="${rv.customerId}"/>
                    <c:if test="${not empty rv.createdAt}"> • <c:out value="${rv.createdAt}"/></c:if>
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
</body>
</html>
