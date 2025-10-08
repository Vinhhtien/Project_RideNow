<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt"  prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Thanh toán chuyển khoản | RideNow</title>

  <!-- CSS dùng chung -->
  <link rel="stylesheet" href="${ctx}/css/homeStyle.css?v=10">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

  <style>
    /* ===== Tokens (dark theme) ===== */
    :root{
      --pay-accent:#3b82f6;
      --pay-accent-dark:#1e40af;
      --pay-bg-0:#0a0b0d;
      --pay-bg-1:#111319;
      --pay-bg-2:#171922;
      --pay-line:#2b3140;
      --pay-text:#e7ecf5;
      --pay-muted:#b7c0d4;
      --pay-muted-2:#8b93a7;
      --pay-warn:#f59e0b;
      --pay-radius:12px;
      --pay-shadow-md:0 6px 14px rgba(0,0,0,.5);
      --pay-shadow-lg:0 14px 30px rgba(0,0,0,.55);
    }

    /* ===== Base ===== */
    html,body{height:100%}
    body{
      background:linear-gradient(135deg,var(--pay-bg-0),var(--pay-bg-1));
      color:var(--pay-text);
      font-family:'Inter','Segoe UI',Tahoma,Geneva,Verdana,sans-serif;
      -webkit-font-smoothing:antialiased;
      -moz-osx-font-smoothing:grayscale;
    }

    /* Header sticky */
    #header{
      position:sticky; top:0; z-index:50;
      background:rgba(17,19,25,.9);
      backdrop-filter:blur(10px);
      border-bottom:1px solid var(--pay-line);
    }

    /* Container */
    .pay-wrap{max-width:1040px; margin:2.2rem auto; padding:0 20px}

    /* ===== Buttons (local) ===== */
    .pay-btn-ghost{
      display:inline-flex; align-items:center; gap:8px;
      background:transparent; color:var(--pay-text);
      border:1px solid var(--pay-line); padding:8px 16px; border-radius:10px;
      transition:.2s;
      text-decoration:none;
    }
    .pay-btn-ghost:hover{color:var(--pay-accent); border-color:var(--pay-accent); background:rgba(59,130,246,.08)}

    .pay-btn-primary{
      display:flex; align-items:center; justify-content:center; gap:8px;
      width:100%; padding:14px 22px; border:0; border-radius:var(--pay-radius);
      background:var(--pay-accent); color:#fff; font-weight:700;
      transition:transform .15s, box-shadow .15s, background .15s;
      cursor:pointer;
    }
    .pay-btn-primary:hover:not(.is-disabled){background:var(--pay-accent-dark); transform:translateY(-2px); box-shadow:var(--pay-shadow-lg)}
    .pay-btn-primary.is-disabled{background:#3a3f4d; color:#97a0b6; cursor:not-allowed; opacity:.85}

    /* ===== Title ===== */
    .pay-title{display:flex; align-items:center; gap:12px; font-size:2.2rem; margin:1.2rem 0 1.6rem}

    /* ===== Steps (prefix tránh xung đột) ===== */
    .pay-steps{display:flex; justify-content:center; gap:3.2rem; margin:1.6rem 0 2.2rem}
    .pay-step{display:flex; flex-direction:column; align-items:center; gap:8px; opacity:.6; transition:.2s}
    .pay-step.is-active{opacity:1}
    .pay-step__num{
      width:44px; height:44px; border-radius:999px;
      display:flex; align-items:center; justify-content:center;
      font-weight:800; font-size:1.3rem;
      color:#eaf0ff; background:#394154; box-shadow:inset 0 0 0 1px var(--pay-line);
    }
    .pay-step.is-active .pay-step__num{background:var(--pay-accent); color:#fff; box-shadow:0 0 12px rgba(59,130,246,.35)}
    .pay-step__text{font-size:.95rem; font-weight:700; color:var(--pay-muted)}
    .pay-step.is-active .pay-step__text{color:var(--pay-accent)}

    /* ===== Card ===== */
    .pay-card{
      background:var(--pay-bg-2); border:1px solid var(--pay-line); border-radius:var(--pay-radius);
      padding:22px; margin-bottom:22px; box-shadow:var(--pay-shadow-md);
    }
    .pay-card__title{display:flex; align-items:center; gap:10px; margin:0 0 12px; color:var(--pay-accent); font-size:1.3rem; font-weight:800}
    .pay-note{color:var(--pay-muted); margin-bottom:1.5rem}

    /* ===== Table ===== */
    .pay-table{width:100%; border-collapse:separate; border-spacing:0; background:#151823; border-radius:10px; overflow:hidden}
    .pay-table th, .pay-table td{padding:14px 12px; border-bottom:1px solid var(--pay-line)}
    .pay-table th{
      background:linear-gradient(0deg, rgba(59,130,246,.14), rgba(59,130,246,.14));
      color:#cfe3ff; text-transform:uppercase; letter-spacing:.35px; font-size:.78rem; font-weight:700;
    }
    .pay-table td{color:var(--pay-text)}
    .pay-table tr:nth-child(even){background:rgba(255,255,255,.02)}
    .pay-table tr:hover{background:rgba(59,130,246,.06)}
    .pay-table td:nth-last-child(2), .pay-table td:nth-last-child(3){text-align:right}
    .pay-text-accent{color:var(--pay-accent)}
    .pay-text-muted{color:var(--pay-muted-2)}

    /* ===== Alert ===== */
    .pay-alert{
      display:flex; gap:12px; align-items:flex-start;
      background:rgba(245,158,11,.12); color:#ffd089;
      border:1px solid rgba(245,158,11,.35); border-left:4px solid var(--pay-warn);
      padding:14px 16px; border-radius:10px; margin:0 0 16px;
    }

    /* ===== Bank / QR ===== */
    .pay-bank{display:grid; grid-template-columns:260px 1fr; gap:22px; align-items:start; margin-top:12px}
    @media (max-width:768px){.pay-bank{grid-template-columns:1fr}}
    .pay-qr{text-align:center}
    .pay-qr__box{background:#fff; border:1px solid #e5e7eb; border-radius:12px; padding:16px; display:inline-block}
    .pay-qr__img{width:220px; height:220px; display:block; border-radius:8px}
    .pay-bank__details{background:#141825; border:1px solid var(--pay-line); border-radius:12px; padding:16px}
    .pay-bank__head{display:flex; gap:12px; align-items:center; margin-bottom:12px; padding-bottom:10px; border-bottom:1px solid var(--pay-line)}
    .pay-bank__logo{width:42px; height:42px; border-radius:10px; background:var(--pay-accent); color:#fff; display:flex; align-items:center; justify-content:center; font-weight:800}
    .pay-bank__title{margin:0; font-size:1.1rem}
    .pay-bank__row{display:flex; justify-content:space-between; align-items:center; padding:10px 0; border-bottom:1px solid rgba(255,255,255,.05)}
    @media (max-width:520px){.pay-bank__row{flex-direction:column; align-items:flex-start; gap:6px}}
    .pay-bank__label{color:var(--pay-muted); font-weight:600}
    .pay-bank__value{color:var(--pay-text); font-weight:700}
    .pay-copy{
      margin-left:10px; background:transparent; border:1px solid var(--pay-line); color:var(--pay-muted);
      padding:6px 10px; border-radius:10px; font-size:.8rem; display:inline-flex; gap:6px; align-items:center; transition:.2s; cursor:pointer;
    }
    .pay-copy:hover{border-color:var(--pay-accent); color:var(--pay-accent); background:rgba(59,130,246,.08)}

    /* ===== Total ===== */
    .pay-total{
      background:linear-gradient(135deg,var(--pay-accent),var(--pay-accent-dark));
      color:#fff; border:1px solid rgba(255,255,255,.25);
      padding:20px; margin:18px 0; border-radius:16px; text-align:center;
    }
    .pay-total__sub{font-size:16px; opacity:.9; margin-bottom:8px}
    .pay-total__num{font-size:28px; font-weight:800}
    .pay-total__hint{font-size:14px; opacity:.8; margin-top:8px}

    /* ===== Instructions ===== */
    .pay-instructions{background:#151823; border:1px solid var(--pay-line); border-radius:12px; padding:16px; margin-top:6px}
    .pay-instructions h4{margin:0 0 10px; color:var(--pay-accent); font-size:16px}
    .pay-instructions ol{margin:0 0 0 18px; color:var(--pay-muted)}
    .pay-instructions li{margin-bottom:8px; line-height:1.5}
    .pay-instructions li strong{color:var(--pay-text)}

    /* ===== Actions ===== */
    .pay-actions{display:flex; gap:14px; margin-top:18px; flex-wrap:wrap}
    .pay-actions .pay-btn-ghost, .pay-actions .pay-btn-primary{min-width:280px}
    @media (max-width:768px){.pay-actions .pay-btn-ghost, .pay-actions .pay-btn-primary{min-width:100%}}

    img{max-width:100%; height:auto}
  </style>
</head>

<body>
<header id="header" class="scrolled">
  <div class="header-top">
    <div class="container">
      <div class="header-content">
        <a href="${ctx}/" class="brand">
          <img src="${ctx}/images/ridenow_Logo.jpg" alt="RideNow">
          <span class="brand-name">RideNow</span>
        </a>
        <a href="${ctx}/customerorders" class="pay-btn-ghost">
          <i class="fas fa-clipboard-list"></i> Đơn của tôi
        </a>
      </div>
    </div>
  </div>
</header>

<main class="pay-wrap">

  <h1 class="pay-title">
    <i class="fas fa-credit-card"></i> Thanh toán chuyển khoản
  </h1>
<!--
  <c:if test="${hasPendingPayment}">
    <div class="pay-alert">
      <i class="fas fa-exclamation-triangle"></i>
      <div>
        <strong>Lưu ý quan trọng:</strong> Một số đơn hàng đã có thanh toán đang chờ xác minh.
        Nếu bạn đã chuyển khoản, vui lòng không thực hiện lại. Hệ thống đang xử lý yêu cầu của bạn.
      </div>
    </div>
  </c:if> 
    -->
    <c:if test="${not empty warningMessage}">
    <div class="pay-alert" style="background: rgba(245, 158, 11, 0.12); border-color: #f59e0b;">
        <i class="fas fa-exclamation-triangle"></i>
        <div>${warningMessage}</div>
    </div>
</c:if>
    <c:if test="${hasPendingPayment}">
    <div class="pay-alert">
        <i class="fas fa-clock"></i>
        <div>
            <strong>Thanh toán đang chờ xác minh:</strong> Bạn đã có thanh toán đang chờ Admin xác minh.
            Vui lòng không thực hiện lại thanh toán. Hệ thống sẽ thông báo khi được xác nhận.
        </div>
    </div>
</c:if>
    
    <div class="pay-process-info" style="background: rgba(59, 130, 246, 0.1); border: 1px solid #3b82f6; border-radius: 10px; padding: 16px; margin: 16px 0;">
    <h4 style="margin: 0 0 8px 0; color: #1e40af; display: flex; align-items: center; gap: 8px;">
        <i class="fas fa-sync-alt"></i> Quy trình xác minh
    </h4>
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px;">
        <div style="text-align: center;">
            <div style="background: #3b82f6; color: white; width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 8px; font-weight: bold;">1</div>
  
            
            
            
            
            <!-- Steps -->
  <div class="pay-steps">
    <div class="pay-step is-active">
      <div class="pay-step__num">1</div>
      <div class="pay-step__text">Chuyển khoản</div>
    </div>
    <div class="pay-step">
      <div class="pay-step__num">2</div>
      <div class="pay-step__text">Xác nhận thanh toán</div>
    </div>
    <div class="pay-step">
      <div class="pay-step__num">3</div>
      <div class="pay-step__text">Chờ xác minh</div>
    </div>
  </div>

  <!-- Order info -->
  <div class="pay-card">
    <h2 class="pay-card__title"><i class="fas fa-receipt"></i> Thông tin đơn hàng</h2>
    <p class="pay-note">
      Vui lòng chuyển khoản <b class="pay-text-accent">30% tiền thuê</b> + <b class="pay-text-accent">tiền cọc</b>
      cho các đơn hàng dưới đây. Bạn có thể chuyển gộp một lần.
    </p>

    <div style="overflow-x:auto">
      <table class="pay-table">
        <thead>
          <tr>
            <th>Mã đơn</th>
            <th>Xe thuê</th>
            <th>Thời gian thuê</th>
            <th>30% tiền thuê</th>
            <th>Tiền cọc</th>
            <th>Tổng thanh toán</th>
            <th>Trạng thái TT</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="r" items="${rows}">
            <tr>
              <td><strong>#${r.orderId}</strong></td>
              <td>${r.bikeName}</td>
              <td>
                <fmt:formatDate value="${r.start}" pattern="dd/MM/yyyy"/>
                →
                <fmt:formatDate value="${r.end}" pattern="dd/MM/yyyy"/>
              </td>
              <td><fmt:formatNumber value="${r.thirtyPct}" type="number"/> đ</td>
              <td><fmt:formatNumber value="${r.deposit}" type="number"/> đ</td>
              <td><strong class="pay-text-accent"><fmt:formatNumber value="${r.toPayNow}" type="number"/> đ</strong></td>
              <td>
                <c:choose>
                  <c:when test="${r.paymentStatus == 'pending'}">
                    <span style="color:#f59e0b;font-weight:600"><i class="fas fa-clock"></i> Đang chờ</span>
                  </c:when>
                  <c:otherwise>
                    <span class="pay-text-muted"><i class="fas fa-circle"></i> Chưa TT</span>
                  </c:otherwise>
                </c:choose>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </div>

  <!-- Bank / QR -->
  <div class="pay-card">
    <h2 class="pay-card__title"><i class="fas fa-university"></i> Thông tin chuyển khoản</h2>

    <div class="pay-bank">
      <!-- QR -->
      <div class="pay-qr">
        <div class="pay-qr__box">
          <img id="qrImg" class="pay-qr__img" alt="VietQR Code">
        </div>
        <div class="pay-text-muted" style="margin-top:12px; font-size:14px">
          <i class="fas fa-qrcode"></i> Quét mã QR bằng ứng dụng ngân hàng
        </div>
      </div>

      <!-- Details -->
      <div class="pay-bank__details">
        <div class="pay-bank__head">
          <div class="pay-bank__logo">VTB</div>
          <h3 class="pay-bank__title">VietinBank</h3>
        </div>

        <div class="pay-bank__row">
          <span class="pay-bank__label">Số tài khoản:</span>
          <div style="display:flex;align-items:center;flex-wrap:wrap">
            <span class="pay-bank__value" id="accNo">${qrAccountNo}</span>
            <button class="pay-copy" type="button" onclick="copyText('${qrAccountNo}')"><i class="far fa-copy"></i> Copy</button>
          </div>
        </div>

        <div class="pay-bank__row">
          <span class="pay-bank__label">Chủ tài khoản:</span>
          <span class="pay-bank__value" id="accName">${qrAccountName}</span>
        </div>

        <div class="pay-bank__row">
          <span class="pay-bank__label">Chi nhánh:</span>
          <span class="pay-bank__value">Hội sở chính</span>
        </div>

        <div class="pay-bank__row">
          <span class="pay-bank__label">Số tiền:</span>
          <span class="pay-bank__value pay-text-accent" style="font-size:16px">
            <fmt:formatNumber value="${grandTotal}" type="number"/> đ
          </span>
        </div>

        <div class="pay-bank__row">
          <span class="pay-bank__label">Nội dung chuyển khoản:</span>
          <div style="display:flex;align-items:center;flex-wrap:wrap">
            <span class="pay-bank__value" id="addInfo">${qrAddInfo}</span>
            <button class="pay-copy" type="button" onclick="copyText('${qrAddInfo}')"><i class="far fa-copy"></i> Copy</button>
          </div>
        </div>

        <div style="margin-top:20px; padding:16px; background:var(--pay-bg-2); border-radius:10px; border:1px solid var(--pay-line)">
          <div class="pay-text-muted" style="font-size:14px; text-align:center; line-height:1.5">
            <i class="fas fa-exclamation-circle"></i>
            <c:choose>
              <c:when test="${hasPendingPayment}">
                Bạn đã có thanh toán đang chờ xác minh. Chỉ bấm "Tôi đã chuyển khoản" nếu đây là lần chuyển khoản đầu tiên.
              </c:when>
              <c:otherwise>
                Vui lòng chuyển khoản chính xác nội dung để hệ thống tự động xác minh.
              </c:otherwise>
            </c:choose>
          </div>
        </div>
      </div>
    </div>

    <!-- Total -->
    <div class="pay-total">
      <div class="pay-total__sub">Tổng số tiền cần chuyển</div>
      <div class="pay-total__num"><fmt:formatNumber value="${grandTotal}" type="number"/> đ</div>
      <div class="pay-total__hint">(Có thể chuyển gộp một lần cho tất cả đơn hàng)</div>
    </div>

    <!-- Instructions -->
    <div class="pay-instructions">
      <h4><i class="fas fa-info-circle"></i> Hướng dẫn thanh toán</h4>
      <ol>
        <li>Quét mã QR hoặc chuyển khoản đến tài khoản VietinBank ở trên.</li>
        <li>Sao chép chính xác nội dung chuyển khoản: <strong>${qrAddInfo}</strong>.</li>
        <li>Chuyển đúng số tiền: <strong><fmt:formatNumber value="${grandTotal}" type="number"/> đ</strong>.</li>
        <li>Bấm "Tôi đã chuyển khoản" sau khi hoàn tất giao dịch.</li>
        <li><strong class="pay-text-accent">Chỉ bấm một lần</strong> – Hệ thống ghi nhận và chờ xác minh.</li>
      </ol>
    </div>

    <!-- Actions -->
    <div class="pay-actions">
      <form method="post" action="${ctx}/paynow" style="flex:1; min-width:300px">
        <input type="hidden" name="orders" value="${ordersCsv}">
        <button
          class="pay-btn-primary ${hasPendingPayment ? 'is-disabled' : ''}"
          type="submit"
          ${hasPendingPayment ? 'disabled' : ''}>
          <i class="fas fa-check-circle"></i>
          <c:choose>
            <c:when test="${hasPendingPayment}">Đang chờ xác minh...</c:when>
            <c:otherwise>Tôi đã chuyển khoản</c:otherwise>
          </c:choose>
        </button>
      </form>

      <a href="${ctx}/customerorders" class="pay-btn-ghost">
        <i class="fas fa-arrow-left"></i> Quay lại đơn hàng
      </a>
    </div>
  </div>
</main>

<script>
(function(){
  const accNo   = '${qrAccountNo}';
  const accName = '${qrAccountName}';
  const addInfo = '${qrAddInfo}';
  const raw     = '${grandTotal}'; // có thể là 1060000, "1,060,000", "1.060.000", "1060000.00", v.v.

  // Chuẩn hoá: giữ lại chỉ chữ số và dấu chấm thập phân
  const cleanAmount = String(raw).replace(/[^\d.]/g, '');
  const parsed  = parseFloat(cleanAmount) || 0;

  // Format số tiền cho QR (chỉ số, không có dấu phân cách)
  const qrAmount = Math.round(parsed).toString();

  // DÙNG BIN ĐÚNG CỦA VIETINBANK (970415)
  const bankBin = '970415'; // VietinBank

  // Tạo URL QR với số tiền chính xác
  const url = "https://img.vietqr.io/image/" + bankBin + "-" + accNo + "-qr_only.png"
            + "?amount=" + encodeURIComponent(qrAmount)
            + "&addInfo=" + encodeURIComponent(addInfo)
            + "&accountName=" + encodeURIComponent(accName);

  const qrImg = document.getElementById('qrImg');
  qrImg.src = url;

  // Log để debug
  console.log('Số tiền gốc:', raw);
  console.log('Số tiền đã làm sạch:', cleanAmount);
  console.log('Số tiền cho QR:', qrAmount);
  console.log('URL QR:', url);

  window.copyText = function(text){
    navigator.clipboard.writeText(text).then(() => {
      const tip = document.createElement('div');
      tip.textContent = 'Đã sao chép!';
      tip.style.cssText =
        "position:fixed;bottom:20px;left:50%;transform:translateX(-50%);"+
        "background:var(--pay-accent);color:#fff;padding:12px 20px;border-radius:12px;"+
        "z-index:10000;font-weight:700;box-shadow:var(--pay-shadow-lg);font-size:14px;";
      document.body.appendChild(tip);
      setTimeout(() => tip.remove(), 1800);
    });
  };

  // Chặn double submit
  const form = document.querySelector('form');
  form?.addEventListener('submit', function(){
    const btn = this.querySelector('button[type=\"submit\"]');
    if (btn && !btn.classList.contains('is-disabled')) {
      btn.disabled = true;
      btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';
    }
  });
})();
</script>

</body>
</html> 