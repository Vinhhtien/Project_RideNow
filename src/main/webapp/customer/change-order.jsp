<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<%-- Đảm bảo cancelCount luôn có giá trị (nếu null -> 0) --%>
<c:if test="${empty cancelCount}">
  <c:set var="cancelCount" value="0"/>
</c:if>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Đổi/Hủy đơn #${vm.orderId} | RideNow</title>
  <link rel="stylesheet" href="${ctx}/css/homeStyle.css"/>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <style>
    :root{
      --primary:#0b0b0d;
      --primary-light:#606064;
      --secondary:#22242b;
      --secondary-light:#2e3038;
      --accent:#3b82f6;
      --accent-dark:#1e40af;
      --accent-light:#60a5fa;
      --dark:#323232;
      --dark-light:#171922;
      --light:#f5f7fb;
      --gray:#9aa2b2;
      --gray-light:#cbd5e1;
      --gray-dark:#666b78;
      --white:#fff;
      --shadow-sm:0 2px 6px rgba(0,0,0,.35);
      --shadow-md:0 6px 14px rgba(0,0,0,.5);
      --radius:8px;
      --radius-lg:12px;
      --transition:.3s ease;
    }
    *{box-sizing:border-box}
    body{
      font-family:'Inter','Segoe UI',Tahoma,sans-serif;
      background:linear-gradient(135deg,#0a0b0d 0%,#121318 100%);
      color:var(--light);
      margin:0;
      min-height:100vh;
      line-height:1.6
    }
    .wrap{
      max-width:1200px;
      margin:0 auto;
      padding:40px 20px
    }
    .page-header{
      display:flex;
      justify-content:space-between;
      align-items:center;
      margin-bottom:32px;
      padding-bottom:20px;
      border-bottom:1px solid var(--primary-light)
    }
    .page-title{
      font-size:2.2rem;
      font-weight:800;
      color:var(--accent);
      margin:0;
      text-shadow:0 0 10px rgba(59,130,246,.25)
    }
    .panel{
      background:var(--dark-light);
      border-radius:var(--radius-lg);
      padding:30px;
      margin-bottom:24px;
      box-shadow:var(--shadow-md);
      border:1px solid var(--primary-light)
    }
    .panel-header{
      display:flex;
      align-items:center;
      gap:12px;
      margin-bottom:24px;
      padding-bottom:16px;
      border-bottom:1px solid var(--primary-light)
    }
    .panel-header h3{
      font-size:1.4rem;
      font-weight:700;
      color:var(--accent);
      margin:0
    }
    .panel-header i{
      color:var(--accent);
      font-size:1.3rem
    }
    .row{
      display:flex;
      align-items:center;
      gap:20px;
      margin:16px 0;
      padding:12px 0
    }
    label{
      min-width:180px;
      font-weight:600;
      color:var(--gray-light)
    }
    .form-group{
      display:flex;
      flex-direction:column;
      gap:8px;
      flex:1
    }
    input[type="date"]{
      padding:12px 16px;
      border-radius:var(--radius);
      border:1px solid var(--primary-light);
      background:#676772;
      color:var(--light);
      font-size:1rem;
      transition:var(--transition)
    }
    input[type="date"]:focus{
      outline:none;
      border-color:var(--accent);
      box-shadow:0 0 0 3px rgba(59,130,246,.1)
    }
    .btn{
      display:inline-flex;
      align-items:center;
      gap:8px;
      padding:12px 24px;
      border-radius:var(--radius);
      text-decoration:none;
      font-weight:600;
      transition:var(--transition);
      border:1px solid var(--primary-light);
      background:var(--dark-light);
      color:var(--light);
      cursor:pointer;
      font-size:0.95rem
    }
    .btn:hover{
      background:var(--primary-light);
      color:var(--accent);
      border-color:var(--accent);
      transform:translateY(-2px)
    }
    .btn-primary{
      background:var(--accent);
      color:var(--white);
      border-color:var(--accent)
    }
    .btn-primary[disabled]{
      opacity:.6;
      cursor:not-allowed;
      transform:none
    }
    .btn-primary:hover:not([disabled]){
      background:var(--accent-dark);
      border-color:var(--accent-dark);
      box-shadow:0 6px 20px rgba(59,130,246,.3)
    }
    .btn-danger{
      border-color:#ef4444;
      color:#ef4444;
      background:var(--dark-light)
    }
    .btn-danger:hover{
      background:rgba(239,68,68,.12);
      border-color:#ef4444
    }
    .alert{
      padding:16px 20px;
      border-radius:var(--radius);
      margin-bottom:24px;
      background:rgba(21,128,61,.15);
      color:#86efac;
      border:1px solid rgba(34,197,94,.3);
      border-left:4px solid #22c55e;
      display:flex;
      align-items:center;
      gap:12px
    }
    .alert.warning{
      background:rgba(245,158,11,.15);
      color:#f59e0b;
      border-color:rgba(245,158,11,.3);
      border-left-color:#f59e0b
    }
    .alert.error{
      background:rgba(239,68,68,.15);
      color:#ef4444;
      border-color:rgba(239,68,68,.3);
      border-left-color:#ef4444
    }
    .info-box{
      background:rgba(59,130,246,.1);
      padding:20px;
      border-radius:var(--radius);
      margin:20px 0;
      border-left:4px solid var(--accent);
      display:flex;
      align-items:flex-start;
      gap:12px
    }
    .info-box i{
      color:var(--accent);
      font-size:1.2rem;
      margin-top:2px
    }
    .info-content{flex:1}
    .info-content strong{color:var(--accent-light)}
    .date-display{
      padding:12px 16px;
      background:var(--secondary);
      border-radius:var(--radius);
      border:1px solid var(--primary-light);
      font-weight:600;
      min-width:140px;
      text-align:center
    }
    .badge{
      display:inline-block;
      padding:6px 12px;
      border-radius:20px;
      font-size:.75rem;
      font-weight:600;
      text-transform:uppercase;
      letter-spacing:.5px
    }
    .badge.confirmed{
      background:rgba(34,197,94,.15);
      color:#22c55e;
      border:1px solid rgba(34,197,94,.3)
    }
    .badge.pending{
      background:rgba(245,158,11,.15);
      color:#f59e0b;
      border:1px solid rgba(245,158,11,.3)
    }
    .badge.completed{
      background:rgba(59,130,246,.15);
      color:var(--accent);
      border:1px solid rgba(59,130,246,.3)
    }
    .badge.cancelled{
      background:rgba(239,68,68,.15);
      color:#ef4444;
      border:1px solid rgba(239,68,68,.3)
    }
    .status-indicator{
      display:inline-flex;
      align-items:center;
      gap:8px;
      padding:8px 16px;
      border-radius:20px;
      font-weight:600
    }
    .status-indicator.success{
      background:rgba(34,197,94,.15);
      color:#22c55e;
      border:1px solid rgba(34,197,94,.3)
    }
    .status-indicator.warning{
      background:rgba(245,158,11,.15);
      color:#f59e0b;
      border:1px solid rgba(245,158,11,.3)
    }
    .status-indicator.error{
      background:rgba(239,68,68,.15);
      color:#ef4444;
      border:1px solid rgba(239,68,68,.3)
    }
    .form-actions{
      display:flex;
      gap:16px;
      margin-top:24px;
      padding-top:20px;
      border-top:1px solid var(--primary-light)
    }
    .toolbar{
      display:flex;
      gap:12px;
      flex-wrap:wrap
    }
    .hint{
      color:var(--gray-light);
      font-size:.9rem;
      margin-top:16px;
      padding:16px 20px;
      background:var(--secondary);
      border-radius:var(--radius);
      border-left:4px solid var(--accent);
      display:flex;
      align-items:center;
      gap:10px
    }
    .hint i{
      color:var(--accent);
      font-size:1.1rem
    }

    @media (max-width:768px){
      .wrap{padding:20px 16px}
      .page-header{
        flex-direction:column;
        align-items:flex-start;
        gap:20px
      }
      .page-title{font-size:1.8rem}
      .row{
        flex-direction:column;
        align-items:flex-start;
        gap:12px
      }
      label{min-width:auto}
      .form-actions{flex-direction:column}
      .btn{justify-content:center}
    }
  </style>
</head>
<body>
<div class="wrap">
  <div class="page-header">
    <h1 class="page-title">
      <i class="fas fa-exchange-alt"></i> Đổi/Hủy Đơn #${vm.orderId}
    </h1>
    <div class="toolbar">
      <a class="btn" href="${ctx}/customerorders">
        <i class="fas fa-arrow-left"></i> Quay lại
      </a>
    </div>
  </div>

  <!-- Flash Message -->
  <c:if test="${not empty sessionScope.flash}">
    <div class="alert">
      <i class="fas fa-info-circle"></i> ${sessionScope.flash}
    </div>
    <c:remove var="flash" scope="session"/>
  </c:if>

  <!-- ================== THÔNG TIN ĐƠN HÀNG ================== -->
  <div class="panel">
    <div class="panel-header">
      <i class="fas fa-info-circle"></i>
      <h3>Thông Tin Đơn Hàng</h3>
    </div>

    <div class="row">
      <label><i class="fas fa-tag"></i> Trạng thái:</label>
      <span class="badge ${vm.status}">${vm.status}</span>
    </div>

    <div class="row">
      <label><i class="fas fa-clock"></i> Thời gian còn lại đổi/hủy:</label>
      <c:choose>
        <c:when test="${vm.remainingMinutes > 0}">
          <span class="status-indicator success">
            <i class="fas fa-check-circle"></i>
            <b>${vm.remainingMinutes}</b> phút
          </span>
        </c:when>
        <c:otherwise>
          <span class="status-indicator error">
            <i class="fas fa-exclamation-circle"></i>
            <b>0</b> phút (đã hết hạn đổi/hủy)
          </span>
        </c:otherwise>
      </c:choose>
    </div>

    <div class="row">
      <label><i class="fas fa-calendar-day"></i> Ngày thuê hiện tại:</label>
      <div style="display:flex;align-items:center;gap:12px;">
        <span class="date-display">${vm.start} → ${vm.end}</span>
        <span style="color:var(--gray-light);">(${vm.originalRentalDays} ngày)</span>
      </div>
    </div>

    <div class="row">
      <label><i class="fas fa-motorcycle"></i> Xe thuê:</label>
      <span style="font-weight:600;color:var(--accent-light);">#${vm.bikeId}</span>
    </div>

    <div class="row">
      <label><i class="fas fa-receipt"></i> Thông tin thanh toán:</label>
      <div style="display:flex;flex-direction:column;gap:4px;">
        <span>Tổng tiền đơn:
          <strong><fmt:formatNumber value="${vm.totalAmount}" type="number"/> đ</strong>
        </span>
        <span>Tiền cọc ban đầu:
          <strong><fmt:formatNumber value="${vm.depositAmount}" type="number"/> đ</strong>
        </span>
        <span>Số tiền sẽ hoàn (hủy trong 30 phút: cọc + 30%):
          <strong style="color:var(--accent-light);">
            <fmt:formatNumber value="${vm.refundAmount}" type="number"/> đ
          </strong>
        </span>
      </div>
    </div>

    <!-- Lịch sử ĐỔI đơn -->
    <div class="row">
      <label><i class="fas fa-sync-alt"></i> Số lần đã đổi:</label>
      <div style="display:flex;align-items:center;gap:12px;">
        <span class="status-indicator ${vm.changeCount >= 3 ? 'error' : 'warning'}">
          <i class="fas fa-history"></i>
          <b>${vm.changeCount}</b> / 3 lần
        </span>
        <c:if test="${vm.changeCount >= 3}">
          <span style="color:#f97373;font-size:.9rem;">
            Bạn đã đạt giới hạn 3 lần đổi với đơn này. Không thể đổi thêm.
          </span>
        </c:if>
      </div>
    </div>

    <!-- Lịch sử HỦY đơn theo tài khoản (dùng cancel_in30_count) -->
    <div class="row">
      <label><i class="fas fa-ban"></i> Lịch sử hủy của tài khoản:</label>
      <div style="display:flex;align-items:center;gap:12px;">
        <c:set var="cc" value="${cancelCount}" />
        <c:choose>
          <c:when test="${cc == 0}">
            <span class="status-indicator success">
              <i class="fas fa-user-check"></i>
              Đã hủy <b>0</b> / 3 lần (trong các đơn gần đây)
            </span>
          </c:when>
          <c:when test="${cc == 1}">
            <span class="status-indicator warning">
              <i class="fas fa-user-clock"></i>
              Đã hủy <b>1</b> / 3 lần (trong các đơn gần đây)
            </span>
          </c:when>
          <c:when test="${cc == 2}">
            <span class="status-indicator warning">
              <i class="fas fa-user-clock"></i>
              Đã hủy <b>2</b> / 3 lần (Nếu tiếp tục hủy lần này, tài khoản sẽ bị khóa)
            </span>
          </c:when>
          <c:otherwise>
            <span class="status-indicator error">
              <i class="fas fa-user-slash"></i>
              Đã hủy >= <b>3</b> lần (tài khoản sẽ bị/đã bị khóa)
            </span>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </div>

  <!-- ================== ĐỔI THỜI GIAN THUÊ ================== -->
  <div class="panel">
    <div class="panel-header">
      <i class="fas fa-calendar-alt"></i>
      <h3>Đổi Thời Gian Thuê</h3>
    </div>

    <div class="info-box">
      <i class="fas fa-info-circle"></i>
      <div class="info-content">
        <strong>Lưu ý quan trọng:</strong>
        <ul style="margin:8px 0 0 18px;padding:0;color:var(--gray-light);font-size:.95rem;">
          <li>Chỉ được đổi trong vòng <b>30 phút</b> sau khi đơn được xác nhận.</li>
          <li>Phải giữ nguyên <b>${vm.originalRentalDays} ngày thuê</b>, chỉ dời sang khoảng ngày khác.</li>
          <li>Mỗi đơn được đổi tối đa <b>3 lần</b>. Lần đổi thứ 3 hệ thống sẽ trừ <b>10% tiền cọc</b>.</li>
        </ul>
      </div>
    </div>

    <form method="post" action="${ctx}/change-order" id="updateForm">
      <input type="hidden" name="orderId" value="${vm.orderId}"/>
      <input type="hidden" name="action" value="update_dates"/>

      <div class="row">
        <label for="newStart"><i class="fas fa-play-circle"></i> Ngày nhận mới:</label>
        <div class="form-group">
          <input type="date"
                 id="newStart"
                 name="start"
                 value="${vm.start}"
                 min="<%= new java.sql.Date(System.currentTimeMillis()) %>"
                 required />
        </div>
      </div>

      <div class="row">
        <label><i class="fas fa-stop-circle"></i> Ngày trả mới (tự tính):</label>
        <div style="display:flex;align-items:center;gap:12px;">
          <span id="newEndDisplay" class="date-display">${vm.end}</span>
          <span id="daysInfo" style="color:var(--gray-light);"></span>
        </div>
        <input type="hidden" id="newEnd" name="end" value="${vm.end}"/>
      </div>

      <div id="dateWarning" class="alert warning" style="display:none;">
        <i class="fas fa-exclamation-triangle"></i>
        <span id="warningText"></span>
      </div>

      <div class="form-actions">
        <button class="btn btn-primary" type="submit" id="submitBtn">
          <i class="fas fa-save"></i> Lưu Thay Đổi
        </button>
        <button type="button" class="btn" onclick="resetDates()">
          <i class="fas fa-undo"></i> Đặt Lại
        </button>
      </div>
    </form>

    <div class="hint">
      <i class="fas fa-shield-alt"></i>
      Hệ thống sẽ tự động kiểm tra trùng lịch cho đúng <b>xe hiện tại</b> và từ chối nếu khoảng thời gian mới đã có người đặt.
    </div>
  </div>

  <!-- ================== HỦY ĐƠN HÀNG ================== -->
  <div class="panel">
    <div class="panel-header">
      <i class="fas fa-ban"></i>
      <h3>Hủy Đơn Hàng</h3>
    </div>

    <div class="alert warning">
      <i class="fas fa-exclamation-triangle"></i>
      <div>
        <strong>Cảnh báo:</strong>
        Bạn chỉ được hủy trong vòng <b>30 phút</b> kể từ khi đơn được xác nhận.
        Khi hủy đơn, số tiền
        <strong style="color:var(--accent-light);">
          <fmt:formatNumber value="${vm.refundAmount}" type="number"/> đ
        </strong>
        (cọc + 30% tổng tiền thuê) sẽ được hoàn vào ví RideNow của bạn.
        <br/>
        <c:choose>
          <c:when test="${cancelCount >= 2}">
            <span style="color:#f97373;font-weight:600;">
              LƯU Ý: Bạn đã hủy <b>${cancelCount}</b> lần. Nếu tiếp tục hủy lần này (lần thứ 3),
              tài khoản của bạn sẽ bị <u>KHÓA</u> và bạn sẽ bị đăng xuất khỏi hệ thống.
            </span>
          </c:when>
          <c:otherwise>
            Nếu bạn hủy đủ <b>3 lần</b> trong thời gian ngắn, tài khoản sẽ bị khóa theo chính sách RideNow.
          </c:otherwise>
        </c:choose>
        <br/>Hành động này <b>không thể hoàn tác</b>.
      </div>
    </div>

    <form method="post"
          action="${ctx}/change-order"
          onsubmit="return confirmCancel()">
      <input type="hidden" name="orderId" value="${vm.orderId}"/>
      <input type="hidden" name="action" value="cancel"/>

      <div class="form-actions">
        <button class="btn btn-danger" type="submit" id="cancelBtn">
          <i class="fas fa-ban"></i> Hủy Đơn #${vm.orderId}
        </button>
      </div>
    </form>
  </div>
</div>

<script>
// ====== DỮ LIỆU TỪ SERVER ======
const originalStart     = new Date('${vm.start}');
const originalEnd       = new Date('${vm.end}');
const rentalDays        = ${vm.originalRentalDays};
const remainingMinutes  = ${vm.remainingMinutes};
const changeCount       = ${vm.changeCount};
// dùng c:out để luôn có số (nếu null -> 0) => tránh lỗi JS
const cancelCount       = <c:out value="${cancelCount}" default="0"/>;

// ====== INIT ======
document.addEventListener('DOMContentLoaded', function() {
  const newStartInput = document.getElementById('newStart');
  const newEndDisplay = document.getElementById('newEndDisplay');
  const newEndInput   = document.getElementById('newEnd');
  const submitBtn     = document.getElementById('submitBtn');
  const daysInfo      = document.getElementById('daysInfo');

  daysInfo.textContent = `(${rentalDays} ngày)`;

  // Hết 30 phút hoặc đã đổi >= 3 lần → khóa form đổi
  if (remainingMinutes <= 0 || changeCount >= 3) {
    disableForm();
  }

  newStartInput.addEventListener('change', function() {
    if (this.value) {
      const newStart = new Date(this.value);
      const newEnd   = calculateNewEndDate(newStart, rentalDays);

      const formattedEnd = newEnd.toISOString().split('T')[0];
      newEndDisplay.textContent = formattedEnd;
      newEndInput.value         = formattedEnd;

      checkDateValidity(newStart, newEnd);
    }
  });

  // Validate submit
  document.getElementById('updateForm').addEventListener('submit', function(e) {
    if (!validateForm()) {
      e.preventDefault();
      return false;
    }

    if (!confirm('Xác nhận đổi đơn hàng #${vm.orderId}?\n\nNgày mới: '
        + newStartInput.value + ' → ' + newEndInput.value
        + '\nSố ngày: ' + rentalDays + ' ngày')) {
      e.preventDefault();
      return false;
    }

    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
    submitBtn.disabled = true;
  });

  // Kiểm tra ngày hiện tại nếu đã có value
  if (newStartInput.value) {
    const currentStart = new Date(newStartInput.value);
    const currentEnd   = calculateNewEndDate(currentStart, rentalDays);
    checkDateValidity(currentStart, currentEnd);
  }

  // Hết thời gian đổi/hủy → disable nút hủy luôn cho rõ
  if (remainingMinutes <= 0) {
    const cancelBtn = document.getElementById('cancelBtn');
    if (cancelBtn) {
      cancelBtn.disabled = true;
      cancelBtn.innerHTML = '<i class="fas fa-clock"></i> Không thể hủy (hết 30 phút)';
    }
  }
});

function calculateNewEndDate(startDate, days) {
  const newEnd = new Date(startDate);
  newEnd.setDate(newEnd.getDate() + days - 1); // tính cả ngày đầu
  return newEnd;
}

function checkDateValidity(newStart, newEnd) {
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  if (newStart < today) {
    showWarning('Không thể chọn ngày trong quá khứ.', true);
    return;
  }

  if (newStart > newEnd) {
    showWarning('Ngày nhận không thể sau ngày trả.', true);
    return;
  }

  if (newStart.getTime() === originalStart.getTime() &&
      newEnd.getTime()   === originalEnd.getTime()) {
    showWarning('Ngày thuê mới giống với ngày hiện tại. Vui lòng chọn ngày khác.', true);
    return;
  }

  hideWarning();
}

function showWarning(message, disableSubmit = false) {
  const dateWarning = document.getElementById('dateWarning');
  const warningText = document.getElementById('warningText');
  const submitBtn   = document.getElementById('submitBtn');

  warningText.textContent = message;
  dateWarning.style.display = 'flex';

  if (disableSubmit) {
    submitBtn.disabled = true;
  }
}

function hideWarning() {
  const dateWarning = document.getElementById('dateWarning');
  const submitBtn   = document.getElementById('submitBtn');
  dateWarning.style.display = 'none';
  submitBtn.disabled = false;
}

function validateForm() {
  const newStart = document.getElementById('newStart').value;
  const newEnd   = document.getElementById('newEnd').value;

  if (!newStart || !newEnd) {
    alert('Vui lòng chọn đầy đủ ngày nhận và ngày trả.');
    return false;
  }

  const startDate = new Date(newStart);
  const endDate   = new Date(newEnd);
  const today     = new Date();
  today.setHours(0, 0, 0, 0);

  if (startDate < today) {
    alert('Không thể chọn ngày trong quá khứ.');
    return false;
  }
  if (startDate > endDate) {
    alert('Ngày nhận phải trước ngày trả.');
    return false;
  }

  return true;
}

function resetDates() {
  document.getElementById('newStart').value = '${vm.start}';
  document.getElementById('newEndDisplay').textContent = '${vm.end}';
  document.getElementById('newEnd').value = '${vm.end}';
  hideWarning();
}

// Confirm HỦY ĐƠN – cảnh báo BAN ACC ở lần thứ 3
function confirmCancel() {
  const refundAmount    = ${vm.refundAmount};
  const formattedAmount = new Intl.NumberFormat('vi-VN').format(refundAmount);

  let message = 'Bạn có CHẮC CHẮN muốn hủy đơn hàng #${vm.orderId}?\n\n'
      + 'Số tiền ' + formattedAmount + ' đ (cọc + 30% tổng tiền thuê) sẽ được hoàn vào ví.\n\n';

  if (cancelCount >= 2) {
    // Đây là lần hủy thứ 3 trở lên
    message += 'LƯU Ý QUAN TRỌNG: Đây là LẦN HỦY THỨ 3. '
             + 'Sau khi hủy, tài khoản của bạn sẽ bị KHÓA và bạn sẽ bị đăng xuất khỏi hệ thống.\n\n';
  } else {
    message += 'Nếu bạn hủy nhiều lần (từ 3 lần trở lên trong thời gian ngắn), '
             + 'tài khoản sẽ bị khóa theo chính sách RideNow.\n\n';
  }

  message += 'Hành động này KHÔNG THỂ hoàn tác. Đơn hàng sẽ bị hủy vĩnh viễn.';

  return confirm(message);
}

function disableForm() {
  const newStart = document.getElementById('newStart');
  const submitBtn = document.getElementById('submitBtn');

  if (newStart) newStart.disabled = true;
  if (submitBtn) {
    submitBtn.disabled = true;
    submitBtn.innerHTML =
      '<i class="fas fa-clock"></i> Không thể đổi (hết thời gian hoặc quá 3 lần)';
  }

  showWarning('Đã quá 30 phút hoặc đơn đã đạt giới hạn 3 lần đổi. Không thể đổi đơn nữa.', true);
}
</script>
</body>
</html>
