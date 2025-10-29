<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt"  prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Ví của tôi | RideNow</title>
  <link rel="stylesheet" href="${ctx}/css/homeStyle.css"/>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <style>
    :root {
      --primary: #0b0b0d; --primary-dark: #060607; --primary-light: #606064;
      --secondary: #22242b; --secondary-light: #2e3038;
      --accent: #3b82f6; --accent-dark: #1e40af; --accent-light: #60a5fa;
      --dark: #323232; --dark-light: #171922; --light: #f5f7fb;
      --gray: #9aa2b2; --gray-light: #cbd5e1; --gray-dark: #666b78;
      --white: #ffffff; --shadow-sm: 0 2px 6px rgba(0,0,0,0.35);
      --shadow-md: 0 6px 14px rgba(0,0,0,0.5); --shadow-lg: 0 14px 30px rgba(0,0,0,0.55);
      --radius: 8px; --radius-lg: 12px; --transition: all .3s ease;
    }
    * { box-sizing: border-box; }
    body {
      font-family: 'Inter','Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
      background: linear-gradient(135deg, #0a0b0d 0%, #121318 100%);
      color: var(--light); line-height: 1.6; margin: 0; padding: 0; min-height: 100vh;
    }
    .wrap { max-width: 1000px; margin: 0 auto; padding: 40px 20px; }
    .page-header {
      display:flex; justify-content:space-between; align-items:center;
      margin-bottom:32px; padding-bottom:20px; border-bottom:1px solid var(--primary-light);
    }
    .page-title {
      font-size:2.2rem; font-weight:800; color:var(--accent); margin:0;
      text-shadow:0 0 10px rgba(59,130,246,.25);
    }
    .toolbar { display:flex; gap:12px; margin-bottom:24px; flex-wrap:wrap; }
    .btn {
      display:inline-flex; align-items:center; gap:8px; padding:10px 20px; border-radius:var(--radius);
      text-decoration:none; font-weight:600; transition:var(--transition);
      border:1px solid var(--primary-light); background:var(--dark-light); color:var(--light);
    }
    .btn:hover {
      background:var(--primary-light); color:var(--accent); border-color:var(--accent);
      transform:translateY(-2px); box-shadow:var(--shadow-sm);
    }
    .btn-primary { background:var(--accent); color:var(--white); border-color:var(--accent); }
    .btn-primary:hover { background:var(--accent-dark); border-color:var(--accent-dark);
      box-shadow:0 6px 20px rgba(59,130,246,0.3); }
    .card {
      background:var(--dark-light); border-radius:var(--radius-lg); box-shadow:var(--shadow-md);
      padding:32px; margin-bottom:24px; border:1px solid var(--primary-light);
    }
    .balance-section {
      text-align:center; padding:40px 20px; background:linear-gradient(135deg, var(--accent), var(--accent-dark));
      border-radius:var(--radius-lg); color:var(--white); margin-bottom:32px;
    }
    .balance-label { font-size:1.1rem; margin-bottom:8px; opacity:0.9; }
    .balance-amount { font-size:3rem; font-weight:800; margin-bottom:16px; }
    .balance-subtitle { font-size:0.9rem; opacity:0.8; }
    .stats-grid {
      display:grid; grid-template-columns:repeat(auto-fit, minmax(200px, 1fr)); gap:20px; margin-bottom:32px;
    }
    .stat-card {
      background:var(--secondary); padding:24px; border-radius:var(--radius); text-align:center;
      border:1px solid var(--primary-light);
    }
    .stat-value { font-size:1.8rem; font-weight:700; color:var(--accent); margin-bottom:8px; }
    .stat-label { font-size:0.9rem; color:var(--gray-light); }
    .table-container {
      overflow-x:auto; border-radius:var(--radius); margin-bottom:24px;
    }
    table { width:100%; border-collapse:collapse; }
    th, td { padding:16px 20px; text-align:left; border-bottom:1px solid var(--primary-light); }
    th {
      background:var(--secondary); font-weight:600; color:var(--accent);
      font-size:.9rem; text-transform:uppercase; letter-spacing:.5px;
    }
    tr:last-child td { border-bottom:none; }
    tr:hover { background:rgba(59,130,246,.05); }
    .badge {
      display:inline-block; padding:6px 12px; border-radius:20px; font-size:.75rem;
      font-weight:600; text-transform:uppercase; letter-spacing:.5px;
    }
    .badge.success { background:rgba(34,197,94,.15); color:#22c55e; border:1px solid rgba(34,197,94,.3); }
    .badge.pending { background:rgba(245,158,11,.15); color:#f59e0b; border:1px solid rgba(245,158,11,.3); }
    .badge.refund { background:rgba(59,130,246,.15); color:var(--accent); border:1px solid rgba(59,130,246,.3); }
    .badge.withdraw { background:rgba(168,85,247,.15); color:#a855f7; border:1px solid rgba(168,85,247,.3); }
    .amount.positive { color:#22c55e; font-weight:600; }
    .amount.negative { color:#ef4444; font-weight:600; }
    .info-box {
      background:var(--secondary); padding:20px; border-radius:var(--radius);
      border-left:4px solid var(--accent); margin-bottom:24px;
    }
    .info-box h4 { margin:0 0 8px; color:var(--accent); }
    .info-box p { margin:0; color:var(--gray-light); font-size:0.9rem; }
    @media (max-width:768px){
      .wrap{padding:20px 16px}
      .page-header{flex-direction:column; align-items:flex-start; gap:20px}
      .page-title{font-size:1.8rem}
      .balance-amount{font-size:2.2rem}
      .stats-grid{grid-template-columns:1fr}
      .table-container{overflow-x:auto}
      table{min-width:700px}
    }
  </style>
</head>
<body>
<div class="wrap">
  <div class="page-header">
    <h1 class="page-title">
      <i class="fas fa-wallet"></i> Ví của tôi
    </h1>
    <div class="toolbar">
      <a class="btn" href="${ctx}/motorbikesearch"><i class="fas fa-motorcycle"></i> Tìm xe</a>
      <a class="btn" href="${ctx}/cart"><i class="fas fa-cart-shopping"></i> Giỏ hàng</a>
      <a href="${ctx}/customerorders" class="btn"><i class="fas fa-clipboard-list"></i> Đơn của tôi</a>
      <a href="${ctx}/" class="btn"><i class="fas fa-house"></i> Trang chủ</a>
    </div>
  </div>

  <!-- Flash message -->
  <c:if test="${not empty sessionScope.flash}">
    <div class="info-box" style="border-left-color:#22c55e;">
      <i class="fas fa-info-circle"></i> ${sessionScope.flash}
    </div>
    <c:remove var="flash" scope="session"/>
  </c:if>

  <!-- Balance Section -->
  <div class="balance-section">
    <div class="balance-label">Số dư hiện tại</div>
    <div class="balance-amount">
      <fmt:formatNumber value="${balance}" type="number"/> đ
    </div>
    <div class="balance-subtitle">
      <c:choose>
        <c:when test="${balance > 0}">
          <i class="fas fa-coins"></i> Số dư khả dụng trong ví
        </c:when>
        <c:otherwise>
          <i class="fas fa-info-circle"></i> Bạn chưa có số dư trong ví
        </c:otherwise>
      </c:choose>
    </div>
  </div>

  <!-- Simple Stats -->
  <div class="stats-grid">
    <div class="stat-card">
      <div class="stat-value"><fmt:formatNumber value="${balance}" type="number"/> đ</div>
      <div class="stat-label">Tổng số dư</div>
    </div>
    <div class="stat-card">
      <div class="stat-value">${txs.size()}</div>
      <div class="stat-label">Tổng giao dịch</div>
    </div>
  </div>

  <!-- Transaction History -->
  <div class="card">
    <h3 style="margin:0 0 20px;color:var(--accent);">
      <i class="fas fa-history"></i> Lịch sử giao dịch
    </h3>
    <c:choose>
      <c:when test="${empty txs}">
        <div style="text-align:center; padding:40px 20px; color:var(--gray-light);">
          <i class="fas fa-receipt" style="font-size:3rem; margin-bottom:16px; opacity:0.5;"></i>
          <p>Chưa có giao dịch nào</p>
        </div>
      </c:when>
      <c:otherwise>
        <div class="table-container">
          <table>
            <thead>
            <tr>
              <th>Mã GD</th>
              <th>Thời gian</th>
              <th>Loại giao dịch</th>
              <th>Mô tả</th>
              <th>Đơn hàng</th>
              <th>Số tiền</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="t" items="${txs}">
              <tr>
                <td><strong>#${t.id}</strong></td>
                <td>
                  <c:choose>
                    <c:when test="${t.date != null}">
                      <fmt:formatDate value="${t.date}" pattern="dd/MM/yyyy HH:mm"/>
                    </c:when>
                    <c:otherwise>N/A</c:otherwise>
                  </c:choose>
                </td>
                <td>
                  <c:choose>
                    <c:when test="${t.type == 'topup'}">
                      <span class="badge refund"><i class="fas fa-coins"></i> Nạp tiền</span>
                    </c:when>
                    <c:when test="${t.type == 'withdraw'}">
                      <span class="badge withdraw"><i class="fas fa-hand-holding-usd"></i> Rút tiền</span>
                    </c:when>
                    <c:when test="${t.type == 'refund'}">
                      <span class="badge success"><i class="fas fa-undo"></i> Hoàn cọc</span>
                    </c:when>
                    <c:when test="${t.type == 'payment'}">
                      <span class="badge pending"><i class="fas fa-credit-card"></i> Thanh toán</span>
                    </c:when>
                    <c:when test="${t.type == 'adjust'}">
                      <span class="badge"><i class="fas fa-adjust"></i> Điều chỉnh</span>
                    </c:when>
                    <c:otherwise>
                      <span class="badge">${t.type != null ? t.type : 'unknown'}</span>
                    </c:otherwise>
                  </c:choose>
                </td>
                <td>${t.description != null ? t.description : 'Không có mô tả'}</td>
                <td>
                  <c:if test="${t.orderId != null and t.orderId > 0}">
                    <a href="${ctx}/customerorders" style="color:var(--accent); text-decoration:none;">
                      #${t.orderId}
                    </a>
                  </c:if>
                </td>
                <td>
                  <c:choose>
                    <c:when test="${t.amount >= 0}">
                      <span class="amount positive">+<fmt:formatNumber value="${t.amount}" type="number"/> đ</span>
                    </c:when>
                    <c:otherwise>
                      <span class="amount negative"><fmt:formatNumber value="${t.amount}" type="number"/> đ</span>
                    </c:otherwise>
                  </c:choose>
                </td>
              </tr>
            </c:forEach>
            </tbody>
          </table>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
</div>
</body>
</html>