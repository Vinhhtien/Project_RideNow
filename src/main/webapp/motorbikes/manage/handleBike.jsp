<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<!--<html lang="vi">-->
<!--<head>
  <meta charset="UTF-8">
  <title>Quản lý xe | RideNow</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <style>
    body{font-family:Inter,system-ui;background:#0b132b;color:#e2e8f0;margin:0}
    .wrap{max-width:1100px;margin:28px auto;padding:0 16px}
    .panel{background:#111827;border:1px solid #1f2937;border-radius:16px;padding:16px}
    table{width:100%;border-collapse:collapse;margin-top:10px}
    th,td{border-bottom:1px solid #1f2937;padding:10px 8px;text-align:left}
    th{color:#cbd5e1}
    .toolbar{display:flex;gap:10px;justify-content:space-between;align-items:center}
    .btn{padding:8px 12px;border-radius:10px;border:1px solid #334155;background:#1f2937;color:#fff;text-decoration:none}
    select{height:36px;background:#0b1224;border:1px solid #334155;color:#e2e8f0;border-radius:8px;padding:0 8px}
    a{color:#93c5fd;text-decoration:none}
    .muted{color:#94a3b8}
  </style>
</head>
<body>
  <div class="wrap">
    <div class="toolbar">
      <h1>Xe của tôi</h1>
      <div>
        <a class="btn" href="${pageContext.request.contextPath}/motorbikesearch">Tìm xe (public)</a>
        <a class="btn" href="${pageContext.request.contextPath}/home.jsp">Trang chủ</a>
      </div>
    </div>

    <div class="panel">
      <form method="get" action="${pageContext.request.contextPath}/motorbikemanagelist">
        <label for="type_id" class="muted">Lọc theo loại xe</label>
        <select name="type_id" id="type_id" onchange="this.form.submit()">
          <option value="">-- Tất cả --</option>
          <option value="1" ${param.type_id == '1' ? 'selected' : ''}>Xe số</option>
          <option value="2" ${param.type_id == '2' ? 'selected' : ''}>Xe ga</option>
          <option value="3" ${param.type_id == '3' ? 'selected' : ''}>Phân khối lớn</option>
        </select>
      </form>

      <c:choose>
        <c:when test="${empty items}">
          <p class="muted" style="margin-top:12px">Chưa có xe nào.</p>
        </c:when>
        <c:otherwise>
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Tên xe</th>
                <th>Biển số</th>
                <th>Loại</th>
                <th>Giá/ngày</th>
                <th>Trạng thái</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="m" items="${items}">
                <tr>
                  <td>${m.bikeId}</td>
                  <td>${m.bikeName}</td>
                  <td>${m.licensePlate}</td>
                  <td>
                    <c:choose>
                      <c:when test="${m.typeId == 1}">Xe số</c:when>
                      <c:when test="${m.typeId == 2}">Xe ga</c:when>
                      <c:when test="${m.typeId == 3}">Phân khối lớn</c:when>
                      <c:otherwise>#${m.typeId}</c:otherwise>
                    </c:choose>
                  </td>
                  <td>${m.pricePerDay}</td>
                  <td>${m.status}</td>
                  <td>
                    <a href="${pageContext.request.contextPath}/motorbikedetail?id=${m.bikeId}">Xem</a>
                    <span class="muted">|</span>
                    <a href="#">Sửa</a>
                    <span class="muted">|</span>
                    <a href="#">Xóa</a>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</body>-->
<!--</html>-->
