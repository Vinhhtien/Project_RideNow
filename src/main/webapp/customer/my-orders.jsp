<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt"  prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="vi_VN"/>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Đơn thuê của tôi | RideNow</title>
  <link rel="stylesheet" href="${ctx}/css/homeStyle.css"/>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <style>
    :root{
      --primary:#0b0b0d; --primary-light:#606064;
      --secondary:#22242b; --secondary-light:#2e3038;
      --accent:#3b82f6; --accent-dark:#1e40af; --accent-light:#60a5fa;
      --dark:#323232; --dark-light:#171922; --light:#f5f7fb;
      --gray:#9aa2b2; --gray-light:#cbd5e1; --gray-dark:#666b78;
      --white:#fff; --shadow-sm:0 2px 6px rgba(0,0,0,.35); --shadow-md:0 6px 14px rgba(0,0,0,.5);
      --radius:8px; --radius-lg:12px; --transition:.3s ease;
    }
    *{box-sizing:border-box}
    body{font-family:'Inter','Segoe UI',Tahoma,sans-serif;background:linear-gradient(135deg,#0a0b0d 0%,#121318 100%);
         color:var(--light);margin:0;min-height:100vh;line-height:1.6}
    .wrap{max-width:1200px;margin:0 auto;padding:40px 20px}
    .page-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:32px;
                 padding-bottom:20px;border-bottom:1px solid var(--primary-light)}
    .page-title{font-size:2.2rem;font-weight:800;color:var(--accent);margin:0;text-shadow:0 0 10px rgba(59,130,246,.25)}
    .toolbar{display:flex;gap:12px;flex-wrap:wrap}
    .btn{display:inline-flex;align-items:center;gap:8px;padding:10px 20px;border-radius:var(--radius);
         text-decoration:none;font-weight:600;transition:var(--transition);
         border:1px solid var(--primary-light);background:var(--dark-light);color:var(--light)}
    .btn:hover{background:var(--primary-light);color:var(--accent);border-color:var(--accent);transform:translateY(-2px)}
    .btn-primary{background:var(--accent);color:var(--white);border-color:var(--accent)}
    .btn-primary[disabled]{opacity:.6;cursor:not-allowed}
    .btn-primary:hover{background:var(--accent-dark);border-color:var(--accent-dark);box-shadow:0 6px 20px rgba(59,130,246,.3)}
    .btn-danger{border-color:#ef4444;color:#ef4444;background:var(--dark-light)}
    .btn-danger:hover{background:rgba(239,68,68,.12);border-color:#ef4444}
    .alert{padding:16px 20px;border-radius:var(--radius);margin-bottom:24px;background:rgba(21,128,61,.15);
           color:#86efac;border:1px solid rgba(34,197,94,.3);border-left:4px solid #22c55e}
    .empty-state{text-align:center;padding:60px 40px;background:var(--dark-light);border-radius:var(--radius-lg);
                 box-shadow:var(--shadow-md);border:1px solid var(--primary-light)}
    .empty-state i{font-size:64px;color:var(--accent);margin-bottom:20px;opacity:.8}
    .table-container{background:var(--dark-light);border-radius:var(--radius-lg);overflow:hidden;box-shadow:var(--shadow-md);
                     margin-bottom:24px;border:1px solid var(--primary-light)}
    table{width:100%;border-collapse:collapse}
    th,td{padding:16px 20px;text-align:left;border-bottom:1px solid var(--primary-light)}
    th{background:var(--secondary);font-weight:600;color:var(--accent);font-size:.9rem;text-transform:uppercase;letter-spacing:.5px}
    tr:last-child td{border-bottom:none}
    tr:hover{background:rgba(59,130,246,.05)}
    .badge{display:inline-block;padding:6px 12px;border-radius:20px;font-size:.75rem;font-weight:600;text-transform:uppercase;letter-spacing:.5px}
    .badge.pending{background:rgba(245,158,11,.15);color:#f59e0b;border:1px solid rgba(245,158,11,.3)}
    .badge.confirmed{background:rgba(34,197,94,.15);color:#22c55e;border:1px solid rgba(34,197,94,.3)}
    .badge.completed{background:rgba(59,130,246,.15);color:var(--accent);border:1px solid rgba(59,130,246,.3)}
    .badge.cancelled{background:rgba(239,68,68,.15);color:#ef4444;border:1px solid rgba(239,68,68,.3)}
    .checkbox-cell{width:40px;text-align:center}
    .checkbox-cell input[type="checkbox"]{width:18px;height:18px;cursor:pointer;accent-color:var(--accent)}
    .checkbox-cell input[type="checkbox"]:disabled{opacity:.5;cursor:not-allowed}
    .hint{color:var(--gray-light);font-size:.9rem;margin-bottom:20px;padding:16px 20px;background:var(--secondary);
          border-radius:var(--radius);border-left:4px solid var(--accent);display:flex;align-items:center;gap:10px}
    .hint i{color:var(--accent);font-size:1.1rem}
    .actions{display:flex;gap:16px;margin-top:24px}
    .payment-method-badge{display:inline-block;padding:2px 6px;border-radius:4px;font-size:.7rem;margin-left:4px}
    .payment-wallet { background: rgba(34,197,94,.15); color:#22c55e; border:1px solid rgba(34,197,94,.3); }
    .payment-transfer { background: rgba(59,130,246,.15); color:var(--accent); border:1px solid rgba(59,130,246,.3); }

    /* DEBUG (tuỳ giữ) */
    .debug-info{background:#1a1a1a;padding:10px;margin:10px 0;border-radius:5px;font-size:12px;border-left:4px solid var(--accent)}
    .debug-info strong{color:var(--accent)}
    @media (max-width:768px){
      .wrap{padding:20px 16px}
      .page-header{flex-direction:column;align-items:flex-start;gap:20px}
      .page-title{font-size:1.8rem}
      .table-container{overflow-x:auto;border-radius:var(--radius)}
      table{min-width:920px}
      .actions{flex-direction:column}
      .btn{justify-content:center}
    }
  </style>
</head>
<body>
  <div class="wrap">
    <div class="page-header">
      <h1 class="page-title"><i class="fas fa-clipboard-list"></i> Đơn thuê của tôi</h1>
      <div class="toolbar">
        <a class="btn" href="${ctx}/motorbikesearch"><i class="fas fa-motorcycle"></i> Tìm xe</a>
        <a class="btn" href="${ctx}/cart"><i class="fas fa-cart-shopping"></i> Giỏ hàng</a>
        <a class="btn" href="${ctx}/wallet"><i class="fas fa-wallet"></i> Ví của tôi</a>
      </div>
    </div>

    <!-- DEBUG (tuỳ giữ) -->
<!--    <div class="debug-info">
      <strong>DEBUG:</strong>
      OrdersVm size: ${not empty ordersVm ? ordersVm.size() : 0}
    </div>-->

    <!-- Flash -->
    <c:if test="${not empty sessionScope.flash}">
      <div class="alert"><i class="fas fa-info-circle"></i> ${sessionScope.flash}</div>
      <c:remove var="flash" scope="session"/>
    </c:if>

    <c:set var="hasOrders" value="${not empty ordersVm and ordersVm.size() > 0}"/>

    <c:choose>
      <c:when test="${not hasOrders}">
        <div class="empty-state">
          <i class="fas fa-clipboard-list"></i>
          <h3>Bạn chưa có đơn thuê nào</h3>
          <a class="btn btn-primary" href="${ctx}/motorbikesearch">Tìm xe ngay</a>
        </div>
      </c:when>

      <c:otherwise>
        <div class="hint">
          <i class="fas fa-info-circle"></i>
          Chọn các đơn <b>pending</b> để thanh toán (30% + cọc). 
          Sau khi thanh toán xong, <b>đơn sẽ chuyển sang “Completed” ngay</b> và hệ thống gửi email xác nhận cho bạn.
        </div>

        <form id="payForm" method="get" action="${ctx}/paynow" onsubmit="return buildOrdersCsv()">
          <div class="table-container">
            <table>
              <thead>
                <tr>
                  <th class="checkbox-cell"><input type="checkbox" id="checkAll"/></th>
                  <th>Mã đơn</th>
                  <th>Xe</th>
                  <th>Ngày nhận</th>
                  <th>Ngày trả</th>
                  <th>Tổng tiền</th>
                  <th>Trạng thái</th>
                  <th>Thao tác</th>
                </tr>
              </thead>

              <tbody>
                <c:forEach var="o" items="${ordersVm}">
                  <%-- Cho phép tick nếu còn pending (bỏ các cờ chờ xác minh) --%>
                  <c:set var="canPay" value="${o.status == 'pending'}"/>
                  <c:set var="canCancel" value="${o.status == 'pending'}"/>

                  <tr>
                    <td class="checkbox-cell">
                      <c:choose>
                        <c:when test="${canPay}">
                          <input type="checkbox" class="chk" value="${o.orderId}" name="selectedOrder"/>
                        </c:when>
                        <c:otherwise>
                          <input type="checkbox" disabled title="Chỉ có thể thanh toán đơn đang pending"/>
                        </c:otherwise>
                      </c:choose>
                    </td>
                    <td>
                      <strong>#${o.orderId}</strong>
                      <c:if test="${not empty o.paymentMethod}">
                        <span class="payment-method-badge ${o.paymentMethod == 'wallet' ? 'payment-wallet' : 'payment-transfer'}">
                          <c:choose>
                            <c:when test="${o.paymentMethod == 'wallet'}">💳 Ví</c:when>
                            <c:when test="${o.paymentMethod == 'transfer'}">🏦 Chuyển khoản</c:when>
                            <c:otherwise>${o.paymentMethod}</c:otherwise>
                          </c:choose>
                        </span>
                      </c:if>
                    </td>
                    <td>${o.bikeName}</td>
                    <td><fmt:formatDate value="${o.start}" pattern="dd/MM/yyyy"/></td>
                    <td><fmt:formatDate value="${o.end}" pattern="dd/MM/yyyy"/></td>
                    <td><fmt:formatNumber value="${o.total}" type="number"/> đ</td>
                    <td>
                      <span class="badge ${o.status}">${o.status}</span>
                    </td>
                    <td>
                      <c:if test="${canCancel}">
                        <button type="button" class="btn btn-danger" onclick="confirmCancel(${o.orderId})">
                          <i class="fas fa-ban"></i> Hủy
                        </button>
                      </c:if>
                    </td>
                  </tr>
                </c:forEach>
              </tbody>

            </table>
          </div>

          <input type="hidden" name="orders" id="ordersCsv"/>
          <div class="actions">
            <button class="btn btn-primary" type="submit" id="submitBtn" disabled>
              <i class="fas fa-credit-card"></i> Thanh toán các đơn đã chọn
            </button>
            <a class="btn" href="${ctx}/home.jsp"><i class="fas fa-home"></i> Về trang chủ</a>
          </div>
        </form>

        <!-- Form hủy đơn ẩn -->
        <form id="cancelForm" method="post" action="${ctx}/customerorders" style="display:none">
          <input type="hidden" name="action" value="cancel"/>
          <input type="hidden" name="orderId" id="cancelOrderId"/>
        </form>
      </c:otherwise>
    </c:choose>
  </div>

  <script>
    const chkAll = document.getElementById('checkAll');
    const submitBtn = document.getElementById('submitBtn');
    const chks = () => Array.from(document.querySelectorAll('.chk'));

    function updateSubmitButton(){
      const selected = chks().filter(c => c.checked).length;
      submitBtn.disabled = selected === 0;
      submitBtn.innerHTML = selected > 0
        ? `<i class="fas fa-credit-card"></i> Thanh toán (${selected} đơn)`
        : `<i class="fas fa-credit-card"></i> Thanh toán các đơn đã chọn`;
    }

    function syncCheckAllDisabled(){
      const anyCheck = chks().length > 0;
      chkAll.disabled = !anyCheck;
      chkAll.title = anyCheck ? '' : 'Không có đơn nào đủ điều kiện thanh toán';
    }

    chkAll?.addEventListener('change', () => {
      if (!chkAll.disabled) {
        chks().forEach(c => c.checked = chkAll.checked);
        updateSubmitButton();
      }
    });

    document.addEventListener('change', e => {
      if (e.target.classList?.contains('chk')) updateSubmitButton();
    });

    function buildOrdersCsv(){
      const ids = chks().filter(c => c.checked).map(c => c.value);
      if (ids.length === 0){
        alert('Vui lòng chọn ít nhất 1 đơn pending để thanh toán.');
        return false;
      }
      if (!confirm(`Bạn sắp thanh toán cho ${ids.length} đơn hàng.\nSau khi thanh toán xong, đơn sẽ được xác nhận ngay. Tiếp tục?`)){
        return false;
      }
      document.getElementById('ordersCsv').value = ids.join(',');
      return true;
    }

    function confirmCancel(orderId){
      if (confirm('Bạn có chắc muốn hủy đơn #' + orderId + ' ?')) {
        document.getElementById('cancelOrderId').value = orderId;
        document.getElementById('cancelForm').submit();
      }
    }

    document.addEventListener('DOMContentLoaded', function(){
      if (document.querySelector('.alert')) window.scrollTo(0, 0);
      syncCheckAllDisabled();
      updateSubmitButton();
    });
  </script>
</body>
</html>
