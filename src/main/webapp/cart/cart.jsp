<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Giỏ hàng | RideNow</title>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

  <style>
    :root{
      --primary:#0b0b0d; --primary-dark:#060607; --primary-light:#606064;
      --secondary:#22242b; --secondary-light:#2e3038;
      --accent:#3b82f6; --accent-dark:#1e40af; --accent-light:#60a5fa;
      --dark:#323232; --dark-light:#171922;
      --light:#f5f7fb; --gray:#9aa2b2; --gray-light:#cbd5e1; --gray-dark:#666b78;
      --white:#fff; --shadow-sm:0 2px 6px rgba(0,0,0,.35);
      --shadow-md:0 6px 14px rgba(0,0,0,.5); --shadow-lg:0 14px 30px rgba(0,0,0,.55);
      --radius:8px; --radius-lg:12px; --transition:all .3s ease;
    }
    *{box-sizing:border-box}
    body{
      font-family:'Inter','Segoe UI',Tahoma,Geneva,Verdana,sans-serif;
      background:linear-gradient(135deg,#0a0b0d 0%,#121318 100%);
      color:var(--light); line-height:1.6; margin:0; min-height:100vh;
    }
    .container{max-width:1200px;margin:0 auto;padding:40px 20px}

    header{
      background:rgba(11,11,13,.94); backdrop-filter:blur(10px);
      box-shadow:var(--shadow-md); position:sticky; top:0; z-index:100;
      border-bottom:1px solid var(--primary-light);
    }
    .header-content{
      display:flex; justify-content:space-between; align-items:center;
      padding:16px 20px; max-width:1200px; margin:0 auto;
    }
    .brand{display:flex;align-items:center;gap:12px;text-decoration:none;color:var(--accent);
      font-weight:800;font-size:26px;letter-spacing:-.5px;text-shadow:0 0 10px rgba(59,130,246,.25)}
    .brand img{height:40px;border-radius:4px}
    .auth{display:flex;gap:12px}

    /* Nút cơ bản */
    .btn{display:inline-flex;align-items:center;gap:8px;padding:8px 16px;border-radius:var(--radius);
      text-decoration:none;font-weight:600;transition:var(--transition);border:1px solid var(--primary-light);
      background:transparent;color:var(--light)}
    .btn--ghost:hover{background:var(--primary-light);color:var(--accent);border-color:var(--accent)}
    .btn-gray{
      background:var(--secondary);border:1px solid var(--primary-light);border-radius:var(--radius);
      padding:8px 12px;color:var(--gray-light);cursor:pointer;transition:var(--transition)
    }
    .btn-gray:hover{background:rgba(239,68,68,.2);color:#ef4444;border-color:rgba(239,68,68,.3)}

    /* Tiêu đề trang */
    .page-title{
      font-size:2.2rem;font-weight:800;margin:0 0 32px;color:var(--accent);
      text-shadow:0 0 10px rgba(59,130,246,.25);display:flex;align-items:center;gap:12px
    }

    /* Card */
    .card{
      background:var(--dark-light);border-radius:var(--radius-lg);box-shadow:var(--shadow-md);
      padding:32px;margin-bottom:24px;border:1px solid var(--primary-light);position:relative;overflow:hidden
    }

    /* Empty Cart */
    .empty-cart{
      text-align:center; padding:80px 40px; min-height:360px;
      display:flex;flex-direction:column;align-items:center;justify-content:center; position:relative;
      background:none !important;
    }
    .empty-cart::after{ content:none !important; }
    .empty-cart-bg{
      position:absolute; inset:20px; border-radius:var(--radius);
      background:linear-gradient(135deg, rgba(59,130,246,.06) 0%, rgba(59,130,246,.03) 100%);
      z-index:0; pointer-events:none;
    }
    .empty-cart-content{
      position:relative; z-index:5; display:flex; flex-direction:column; align-items:center; width:100%;
    }
    .empty-cart i{font-size:80px;color:var(--accent);margin-bottom:24px;opacity:.9}
    .empty-cart h3{margin:0 0 16px;color:var(--accent);font-size:1.8rem}
    .empty-cart p{color:var(--gray-light);margin-bottom:28px;font-size:1.05rem;max-width:420px}

    /* Bảng xe */
    .table-container{overflow-x:auto;border-radius:var(--radius)}
    .table{width:100%;border-collapse:collapse;background:var(--dark-light)}
    .table th,.table td{padding:20px;text-align:left;border-bottom:1px solid var(--primary-light)}
    .table th{
      background:var(--secondary);font-weight:600;color:var(--accent);
      font-size:.9rem;text-transform:uppercase;letter-spacing:.5px
    }
    .table tr:last-child td{border-bottom:none}
    .table tr:hover{background:rgba(59,130,246,.05)}
    .bike-info{display:flex;flex-direction:column}
    .bike-name{font-weight:600;margin-bottom:6px;color:var(--light);font-size:1.1rem}
    .bike-type{color:var(--gray-light);font-size:.9rem}

    /* Thanh toán */
    .actions{display:flex;justify-content:flex-end;margin-top:32px}
    .summary-card{width:100%;max-width:400px;background:var(--dark-light);border-radius:var(--radius-lg);
      padding:28px;box-shadow:var(--shadow-md);border:1px solid var(--primary-light)}
    .summary-row{display:flex;justify-content:space-between;margin-bottom:16px;padding-bottom:16px;border-bottom:1px solid var(--primary-light)}
    .summary-row:last-child{border-bottom:none;margin-bottom:0;padding-bottom:0}
    .summary-total{font-weight:700;font-size:1.3rem;color:var(--accent);padding-top:16px;border-top:2px solid var(--accent)}
    .summary-label{color:var(--gray-light)} .summary-value{font-weight:600;color:var(--light)}

    /* Responsive */
    @media (max-width:768px){
      .container{padding:20px 16px}
      .header-content{flex-direction:column;gap:16px;padding:12px 16px}
      .auth{width:100%;justify-content:space-between;flex-wrap:wrap}
      .page-title{font-size:1.8rem}
      .card{padding:24px 20px}
      .empty-cart{padding:60px 24px;min-height:300px}
      .empty-cart i{font-size:64px}
      .empty-cart h3{font-size:1.5rem}
      .empty-cart p{font-size:1rem}
      .table-container{border:1px solid var(--primary-light);border-radius:var(--radius)}
      .table{min-width:700px}
      .table th,.table td{padding:16px}
      .actions{justify-content:center}
      .summary-card{max-width:100%}
    }

    /* --- FIX NÚT "TIẾP TỤC TÌM XE" BỊ NHẢY KHI HOVER --- */
    .btn-search{
      background:var(--accent);
      color:var(--white);
      border:none;
      padding:14px 28px !important;
      border-radius:12px;
      font-weight:700;
      text-decoration:none;
      display:inline-flex;
      align-items:center;
      justify-content:center;
      gap:8px;
      transition:background .25s ease, box-shadow .25s ease, transform .2s ease;
      font-size:1.1rem;
      line-height:1 !important;
      min-height:56px;
      position:relative;
      z-index:6;
    }
    .btn-search::before,
    .btn-search::after { content:none !important; }
    .btn-search i {
      display:inline-block !important;
      opacity:1 !important;
      width:1.25em;
      height:1.25em;
      font-size:1.1em;
      margin-right:6px;
      transform:none !important;
    }
    .btn-search:hover {
      background:var(--accent-dark);
      transform:translateY(-2px);
      box-shadow:0 10px 28px rgba(59,130,246,.3);
    }

    .btn:focus,.btn-search:focus,.btn-gray:focus{
      outline:2px solid var(--accent-light);outline-offset:2px
    }
    @media (prefers-reduced-motion: reduce){*{transition:none !important}}
  </style>
</head>

<body>
<header>
  <div class="header-content">
    <a href="${ctx}/" class="brand">
      <img src="${ctx}/images/ridenow_Logo.jpg" alt="RideNow">
      <span class="brand-name">RideNow</span>
    </a>
    <div class="auth">
      <a href="${ctx}/motorbikesearch" class="btn btn--ghost">
        <i class="fas fa-magnifying-glass"></i> Tìm xe khác
      </a>
      <a href="${ctx}/customerorders" class="btn btn--ghost">
        <i class="fas fa-clipboard-list"></i> Đơn của tôi
      </a>
      <a href="${ctx}/wallet" class="btn btn--ghost">
        <i class="fas fa-wallet"></i> Ví của tôi
      </a>
      <a href="${ctx}/" class="btn btn--ghost"><i class="fas fa-house"></i> Trang chủ</a>
    </div>
  </div>
</header>

<main class="container">
  <h1 class="page-title"><i class="fas fa-shopping-cart"></i> Giỏ hàng</h1>
  
  <%-- Thêm đoạn này sau thẻ <h1> --%>
<div class="process-notice" style="background: #f0f9ff; border: 1px solid #3b82f6; border-radius: 8px; padding: 16px; margin-bottom: 20px;">
    <h4 style="margin: 0 0 8px 0; color: #1e40af;">
        <i class="fas fa-info-circle"></i> Quy trình thuê xe mới
    </h4>
    <ol style="margin: 0; padding-left: 20px; color: #374151;">
        <li>Thanh toán 30% + cọc qua chuyển khoản</li>
        <li>Admin xác minh thanh toán (trong vòng 30 phút)</li>
        <li>Đến cửa hàng nhận xe khi được xác nhận</li>
        <li>Trả xe và nhận lại tiền cọc</li>
    </ol>
</div>
  
  <c:choose>
    <c:when test="${empty cartItems}">
      <div class="card empty-cart">
        <div class="empty-cart-bg"></div>
        <div class="empty-cart-content">
          <i class="fas fa-shopping-cart"></i>
          <h3>Giỏ hàng trống</h3>
          <p>Bạn chưa có sản phẩm nào trong giỏ hàng</p>
          <a class="btn-search" href="${ctx}/motorbikesearch">
            Tiếp tục tìm xe
          </a>
        </div>
      </div>
    </c:when>

    <c:otherwise>
      <div class="card">
        <div class="table-container">
          <table class="table">
            <thead>
              <tr>
                <th style="text-align:left">Xe</th>
                <th>Ngày thuê</th>
                <th>Giá/ngày</th>
                <th>Số ngày</th>
                <th>Tạm tính</th>
                <th>Cọc</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="it" items="${cartItems}" varStatus="st">
                <tr>
                  <td style="text-align:left">
                    <div class="bike-info">
                      <span class="bike-name">${it.bikeName}</span>
                      <span class="bike-type">${it.typeName}</span>
                    </div>
                  </td>
                  <td>
                    <fmt:formatDate value="${it.startDate}" pattern="dd/MM/yyyy"/> –
                    <fmt:formatDate value="${it.endDate}" pattern="dd/MM/yyyy"/>
                  </td>
                  <td><fmt:formatNumber value="${it.pricePerDay}" type="number"/> đ</td>
                  <td>${it.days}</td>
                  <td><fmt:formatNumber value="${it.subtotal}" type="number"/> đ</td>
                  <td><fmt:formatNumber value="${it.deposit}" type="number"/> đ</td>
                  <td>
                    <form action="${ctx}/cart" method="post">
                      <input type="hidden" name="action" value="remove"/>
                      <input type="hidden" name="index" value="${st.index}"/>
                      <button class="btn-gray" type="submit" title="Xóa">
                        <i class="fas fa-trash"></i>
                      </button>
                    </form>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </div>

      <%-- THAY THẾ ĐOẠN NÀY --%>
<div class="actions">
  <div class="summary-card">
    <div class="summary-row">
      <span class="summary-label">Tổng tiền xe</span>
      <span class="summary-value"><fmt:formatNumber value="${total}" type="number"/> đ</span>
    </div>
    <div class="summary-row">
      <span class="summary-label">Cọc</span>
      <span class="summary-value"><fmt:formatNumber value="${depositTotal}" type="number"/> đ</span>
    </div>
    <div class="summary-row summary-total">
      <span>Trả ngay (30% + cọc)</span>
      <span><fmt:formatNumber value="${toPayNow}" type="number"/> đ</span>
    </div>
    
    <%-- SỬA NÚT THÀNH FORM --%>
    <form action="${ctx}/cart" method="post" style="display: inline; width: 100%;">
      <input type="hidden" name="action" value="checkout"/>
      <button type="submit" class="btn-search" style="border: none; background: none; cursor: pointer; width: 100%; font: inherit; color: inherit;">
        Tiếp tục thanh toán <i class="fas fa-arrow-right"></i>
      </button>
    </form>
  </div>
</div>
    </c:otherwise>
  </c:choose>
</main>
</body>
</html>
