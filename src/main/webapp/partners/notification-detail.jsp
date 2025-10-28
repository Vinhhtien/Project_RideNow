<%-- an: Partner Notification Detail --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
  model.Account acc = (model.Account) session.getAttribute("account");
  if (acc == null || !"partner".equalsIgnoreCase(acc.getRole())) {
      response.sendRedirect(request.getContextPath() + "/login");
      return;
  }

  String nidStr = request.getParameter("nid");
  model.Notification notif = null;
  try {
      int nid = Integer.parseInt(nidStr);
      service.NotificationService ns = new service.NotificationService();
      notif = ns.findByIdForAccount(nid, acc.getAccountId());
  } catch (Exception ignore) {}
  request.setAttribute("notif", notif);

  // Auto mark read khi mở nếu ?markRead=1|true
  String markRead = request.getParameter("markRead");
  if (notif != null && ( "1".equals(markRead) || "true".equalsIgnoreCase(markRead) )) {
      try {
          service.NotificationService ns2 = new service.NotificationService();
          ns2.readOne(notif.getNotificationId(), acc.getAccountId());
          notif.setRead(true);
      } catch (Exception ignore) {}
  }

  // Suy luận deepLink theo nghiệp vụ:
  // - Admin xác nhận đơn / xác nhận thanh toán / khách đã trả xe / (tuỳ chọn: khách đã nhận xe) -> /rentalhistory
  // - Thông báo vận hành / cập nhật hồ sơ -> /partner?action=editProfile
  // - Khác -> không có nút Xem chi tiết
  String deepLink = null;
  if (notif != null) {
      String t = notif.getTitle()   != null ? notif.getTitle().toLowerCase()   : "";
      String m = notif.getMessage() != null ? notif.getMessage().toLowerCase() : "";

      boolean toHistory =
          t.contains("xác nhận đơn") || m.contains("xác nhận đơn") ||
          t.contains("xác nhận đặt xe") || m.contains("xác nhận đặt xe") ||
          t.contains("xác nhận thanh toán") || m.contains("xác nhận thanh toán") ||
          t.contains("khách đã trả xe") || m.contains("khách đã trả xe") ||
          t.contains("đã trả xe") || m.contains("đã trả xe") ||
          t.contains("đơn hàng đã hoàn thành") || m.contains("đơn hàng đã hoàn thành") ||
          t.contains("hoàn tất") || m.contains("hoàn tất")
          // nếu muốn kèm cả "đã nhận xe" thì giữ hai dòng dưới, không muốn thì xoá
          || t.contains("đã nhận xe") || m.contains("đã nhận xe");

      boolean toProfile =
          t.contains("vận hành") || m.contains("vận hành") ||
          t.contains("hồ sơ")    || m.contains("hồ sơ")    ||
          t.contains("cập nhật hồ sơ") || m.contains("cập nhật hồ sơ");

      if (toHistory) {
          deepLink = request.getContextPath() + "/rentalhistory";
      } else if (toProfile) {
          deepLink = request.getContextPath() + "/partner?action=editProfile";
      }
  }
  request.setAttribute("deepLink", deepLink);
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <title>Chi tiết thông báo</title>
  <meta name="viewport" content="width=device-width, initial-scale=1"/>
  <style>
    :root{--bg:#0b0f1f;--bg2:#0a0d1c;--card:#10152e;--text:#e8eaf6;--muted:#a7b0d6;--border:#1c244c;--radius:16px}
    *{box-sizing:border-box} html,body{margin:0}
    body{font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Arial;background: linear-gradient(180deg, var(--bg) 0%, var(--bg2) 100%);min-height:100vh; color:var(--text); padding:24px;}
    .wrap{max-width:900px;margin:0 auto}
    .row{display:flex;gap:10px;align-items:center;justify-content:space-between;margin-bottom:12px}
    .btn{appearance:none;border:1px solid #3a4477;background:transparent;color:var(--text);padding:8px 12px;border-radius:10px;cursor:pointer;text-decoration:none;font-size:14px}
    .btn.primary{background:#22d3ee;color:#0f172a;border-color:#24cde0}
    .btn:disabled{opacity:.6;cursor:not-allowed}
    .card{background:var(--card);border:1px solid var(--border);border-radius:var(--radius);padding:16px;box-shadow:0 10px 30px rgba(0,0,0,.35)}
    .muted{color:var(--muted)}
    .title{margin:0 0 6px 0;font-size:22px;font-weight:800}
    .meta{font-size:13px;margin-bottom:8px}
    .content{white-space:pre-wrap;line-height:1.6}
    .badge{display:inline-block;border:1px solid #22d3ee;color:#22d3ee;border-radius:999px;padding:2px 8px;font-size:12px;margin-left:6px}
    .badge.done{border-color:#9ab6ff;color:#0f172a;background:#9ab6ff}
    .foot{display:flex;gap:8px;justify-content:flex-end;margin-top:12px}
  </style>
</head>
<body>
  <div class="wrap">
    <div class="row">
      <a class="btn" href="<%=request.getContextPath()%>/dashboard?tab=notifications">← Về Dashboard</a>
      <c:if test="${notif ne null && !notif.read}">
        <form method="post" action="<%=request.getContextPath()%>/dashboard" style="margin:0">
          <input type="hidden" name="action" value="read"/>
          <input type="hidden" name="id" value="${notif.notificationId}"/>
          <button class="btn primary" type="submit">Đánh dấu đã đọc</button>
        </form>
      </c:if>
    </div>

    <c:choose>
      <c:when test="${notif == null}">
        <div class="card">❌ Không tìm thấy thông báo hoặc bạn không có quyền xem.</div>
      </c:when>
      <c:otherwise>
        <div class="card">
          <h2 class="title">${notif.title}
            <c:choose>
              <c:when test="${notif.read}"><span class="badge done">đã đọc</span></c:when>
              <c:otherwise><span class="badge">chưa đọc</span></c:otherwise>
            </c:choose>
          </h2>
          <div class="meta muted">
            <c:choose>
              <c:when test="${not empty notif.createdAt}">
                <fmt:formatDate value="${notif.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
              </c:when>
              <c:otherwise>—</c:otherwise>
            </c:choose>
          </div>

          <div class="content">
            <c:out value="${notif.message}" escapeXml="false"/>
          </div>

          <c:if test="${not empty deepLink}">
            <div class="foot">
              <a class="btn" href="${deepLink}">Xem chi tiết →</a>
            </div>
          </c:if>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
</body>
</html>
