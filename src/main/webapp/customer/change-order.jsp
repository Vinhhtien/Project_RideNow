<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <title>Đổi/Hủy đơn #${vm.orderId}</title>
  <link rel="stylesheet" href="${ctx}/css/homeStyle.css"/>
  <style>
    .panel {background:#171922;border:1px solid #2e3038;border-radius:12px;padding:20px;margin-top:20px;}
    .row {display:flex;gap:16px;align-items:center;margin:12px 0;}
    label {min-width:140px;opacity:.8;}
    input[type="date"]{padding:8px 12px;border-radius:8px;border:1px solid #3a3d46;background:#0b0b0d;color:#f5f7fb}
    .btn {display:inline-flex;gap:8px;align-items:center;padding:10px 16px;border-radius:10px;border:1px solid #2e3038;background:#1a1d26;color:#fff;text-decoration:none}
    .btn-primary{background:#3b82f6;border-color:#3b82f6}
    .btn-danger{background:#ef4444;border-color:#ef4444}
    .hint{margin-top:8px;opacity:.8}
  </style>
</head>
<body>
<div class="wrap">
  <h2>Đổi/Hủy đơn #${vm.orderId}</h2>
  <div class="panel">
    <div class="row"><label>Trạng thái:</label><b>${vm.status}</b></div>
    <div class="row"><label>Còn lại:</label><b>${vm.remainingMinutes}</b> phút để đổi/hủy</div>
    <div class="row"><label>Ngày hiện tại:</label>${vm.start} → ${vm.end}</div>
  </div>

  <div class="panel">
    <h3>Đổi thời gian</h3>
    <form method="post" action="${ctx}/change-order">
      <input type="hidden" name="orderId" value="${vm.orderId}"/>
      <input type="hidden" name="action" value="update_dates"/>
      <div class="row">
        <label>Ngày nhận mới:</label>
        <input type="date" name="start" value="${vm.start}" required/>
      </div>
      <div class="row">
        <label>Ngày trả mới:</label>
        <input type="date" name="end" value="${vm.end}" required/>
      </div>
      <button class="btn btn-primary" type="submit"><i class="fas fa-save"></i> Lưu thay đổi</button>
      <div class="hint">* Hệ thống sẽ từ chối nếu trùng lịch với xe đã đặt trong cùng khoảng thời gian.</div>
    </form>
  </div>

  <div class="panel">
    <h3>Hủy đơn</h3>
    <form method="post" action="${ctx}/change-order" onsubmit="return confirm('Bạn chắc chắn muốn hủy đơn #${vm.orderId}?')">
      <input type="hidden" name="orderId" value="${vm.orderId}"/>
      <input type="hidden" name="action" value="cancel"/>
      <button class="btn btn-danger" type="submit"><i class="fas fa-ban"></i> Hủy đơn</button>
    </form>
  </div>

  <p style="margin-top:20px;">
    <a class="btn" href="${ctx}/customerorders">← Quay lại “Đơn thuê của tôi”</a>
  </p>
</div>
</body>
</html>
