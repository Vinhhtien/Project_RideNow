<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Giỏ hàng | RideNow</title>
    <link rel="icon" href="${ctx}/images/favicon.ico">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        :root {
            --primary: #0b0b0d;
            --primary-dark: #060607;
            --primary-light: #606064;
            --secondary: #22242b;
            --secondary-light: #2e3038;
            --accent: #3b82f6;
            --accent-dark: #1e40af;
            --accent-light: #60a5fa;
            --dark: #323232;
            --dark-light: #171922;
            --light: #f5f7fb;
            --gray: #9aa2b2;
            --gray-light: #cbd5e1;
            --gray-dark: #666b78;
            --white: #fff;
            --shadow-sm: 0 2px 6px rgba(0, 0, 0, .35);
            --shadow-md: 0 6px 14px rgba(0, 0, 0, .5);
            --shadow-lg: 0 14px 30px rgba(0, 0, 0, .55);
            --radius: 8px;
            --radius-lg: 12px;
            --transition: all .3s ease;
            --error-bg: rgba(220, 38, 38, .15);
            --error-border: rgba(220, 38, 38, .4);
            --error-text: #fca5a5;
            --warning-bg: rgba(245, 158, 11, .15);
            --warning-border: rgba(245, 158, 11, .4);
            --warning-text: #fcd34d;
            --success-bg: rgba(34, 197, 94, .15);
            --success-border: rgba(34, 197, 94, .4);
            --success-text: #86efac;
        }

        * {
            box-sizing: border-box
        }

        body {
            font-family: 'Inter', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #0a0b0d 0%, #121318 100%);
            color: var(--light);
            line-height: 1.6;
            margin: 0;
            min-height: 100vh
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 40px 20px
        }

        header {
            background: rgba(11, 11, 13, .94);
            backdrop-filter: blur(10px);
            box-shadow: var(--shadow-md);
            position: sticky;
            top: 0;
            z-index: 100;
            border-bottom: 1px solid var(--primary-light)
        }

        .header-content {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 16px 20px;
            max-width: 1200px;
            margin: 0 auto
        }

        .brand {
            display: flex;
            align-items: center;
            gap: 12px;
            text-decoration: none;
            color: var(--accent);
            font-weight: 800;
            font-size: 26px;
            letter-spacing: -.5px;
            text-shadow: 0 0 10px rgba(59, 130, 246, .25)
        }

        .brand img {
            height: 40px;
            border-radius: 4px
        }

        .auth {
            display: flex;
            gap: 12px
        }

        .btn {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 8px 16px;
            border-radius: var(--radius);
            text-decoration: none;
            font-weight: 600;
            transition: var(--transition);
            border: 1px solid var(--primary-light);
            background: transparent;
            color: var(--light)
        }

        .btn--ghost:hover {
            background: var(--primary-light);
            color: var(--accent);
            border-color: var(--accent)
        }

        .btn-gray {
            background: var(--secondary);
            border: 1px solid var(--primary-light);
            border-radius: 8px;
            padding: 8px 12px;
            color: #e5e7eb;
            cursor: pointer;
            transition: var(--transition)
        }

        .btn-gray:hover {
            background: rgba(59, 130, 246, .15);
            color: #93c5fd;
            border-color: rgba(59, 130, 246, .3)
        }

        .btn-danger {
            background: transparent;
            border: 1px solid #ef4444;
            color: #fca5a5;
            border-radius: 8px;
            padding: 8px 12px;
            cursor: pointer
        }

        .btn-danger:hover {
            background: rgba(239, 68, 68, .15)
        }

        .page-title {
            font-size: 2rem;
            font-weight: 800;
            margin: 0 0 24px;
            color: var(--accent);
            text-shadow: 0 0 10px rgba(59, 130, 246, .25);
            display: flex;
            align-items: center;
            gap: 12px
        }

        .card {
            background: var(--dark-light);
            border-radius: 12px;
            box-shadow: var(--shadow-md);
            padding: 24px;
            margin-bottom: 20px;
            border: 1px solid var(--primary-light)
        }

        .table-container {
            overflow-x: auto;
            border-radius: 8px
        }

        .table {
            width: 100%;
            border-collapse: collapse;
            background: transparent
        }

        .table th, .table td {
            padding: 16px;
            text-align: left;
            border-bottom: 1px solid var(--primary-light);
            vertical-align: top
        }

        .table th {
            background: var(--secondary);
            font-weight: 600;
            color: var(--accent);
            font-size: .9rem;
            text-transform: uppercase;
            letter-spacing: .5px
        }

        .table tr:last-child td {
            border-bottom: none
        }

        .table tr:hover {
            background: rgba(59, 130, 246, .05)
        }

        .bike-name {
            font-weight: 600;
            color: #fff
        }

        .bike-type {
            color: #cbd5e1;
            font-size: .9rem
        }

        .date-row {
            display: flex;
            gap: 8px;
            align-items: center;
            flex-wrap: wrap
        }

        .date-row input[type="date"] {
            background: #6f6f6f;
            color: #fff;
            border: 1px solid var(--primary-light);
            border-radius: 8px;
            padding: 8px 10px;
            min-width: 150px
        }

        .field-hint {
            font-size: .85rem;
            color: #9aa2b2
        }

        .actions {
            display: flex;
            justify-content: flex-end;
            margin-top: 20px
        }

        .summary-card {
            width: 100%;
            max-width: 420px;
            background: var(--dark-light);
            border-radius: 12px;
            padding: 20px;
            box-shadow: var(--shadow-md);
            border: 1px solid var(--primary-light)
        }

        .summary-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 12px;
            padding-bottom: 12px;
            border-bottom: 1px solid var(--primary-light)
        }

        .summary-row:last-child {
            border-bottom: none;
            margin-bottom: 0;
            padding-bottom: 0
        }

        .summary-total {
            font-weight: 700;
            color: var(--accent)
        }

        .js-error {
            color: #f87171;
            margin-top: 6px;
            display: none
        }

        /* ====== EMPTY CART – Glass style đẹp ====== */
        .empty-wrap {
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 60px 20px
        }

        .empty-card {
            width: 100%;
            max-width: 900px;
            min-height: 260px;
            border-radius: 16px;
            padding: 48px 28px;
            text-align: center;
            background: linear-gradient(145deg, rgba(15, 23, 42, .7), rgba(2, 6, 23, .9));
            border: 1px solid rgba(148, 163, 184, .25);
            box-shadow: 0 10px 40px rgba(0, 0, 0, .35), inset 0 0 0 1px rgba(255, 255, 255, .05);
            backdrop-filter: blur(10px);
            transition: transform .25s ease, box-shadow .25s ease, border-color .25s ease;
        }

        .empty-card:hover {
            transform: translateY(-2px);
            border-color: rgba(59, 130, 246, .35);
            box-shadow: 0 16px 50px rgba(59, 130, 246, .25), 0 10px 30px rgba(0, 0, 0, .5);
        }

        .empty-icon {
            width: 92px;
            height: 92px;
            margin: 0 auto 16px;
            display: grid;
            place-items: center;
            border-radius: 20px;
            background: radial-gradient(90px 90px at 40% 40%, rgba(96, 165, 250, .35), rgba(96, 165, 250, .08));
            color: #60a5fa;
            border: 1px solid rgba(96, 165, 250, .35);
            box-shadow: inset 0 0 0 1px rgba(255, 255, 255, .05);
        }

        .empty-icon i {
            font-size: 42px
        }

        .empty-title {
            font-weight: 800;
            font-size: 28px;
            margin: 10px 0 6px;
            color: #93c5fd;
            text-shadow: 0 0 12px rgba(59, 130, 246, .25)
        }

        .empty-desc {
            color: #cbd5e1;
            max-width: 600px;
            margin: 0 auto 28px;
            font-size: 15.5px;
            line-height: 1.7
        }

        .empty-actions {
            display: flex;
            gap: 14px;
            justify-content: center;
            flex-wrap: wrap
        }

        .btn-cta {
            display: inline-flex;
            align-items: center;
            gap: 10px;
            padding: 12px 20px;
            border-radius: 12px;
            border: 1px solid rgba(59, 130, 246, .45);
            background: linear-gradient(180deg, #3b82f6, #1e40af);
            color: #fff;
            text-decoration: none;
            font-weight: 700;
            box-shadow: 0 10px 25px rgba(59, 130, 246, .35);
            transition: transform .2s ease, box-shadow .2s ease, filter .2s ease;
        }

        .btn-cta:hover {
            transform: translateY(-2px);
            filter: saturate(1.1);
            box-shadow: 0 16px 35px rgba(59, 130, 246, .4)
        }

        .btn-ghost {
            padding: 12px 18px;
            border-radius: 12px;
            border: 1px solid rgba(148, 163, 184, .35);
            color: #cbd5e1;
            text-decoration: none;
            background: rgba(2, 6, 23, .35);
            transition: all .2s ease;
        }

        .btn-ghost:hover {
            border-color: #60a5fa;
            color: #e2e8f0;
            background: rgba(59, 130, 246, .15)
        }

        @media (max-width: 768px) {
            .empty-card {
                padding: 32px 18px
            }

            .empty-title {
                font-size: 22px
            }

            .empty-icon {
                width: 72px;
                height: 72px
            }

            .empty-icon i {
                font-size: 34px
            }
        }

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
            <a href="${ctx}/motorbikesearch" class="btn btn--ghost"><i class="fas fa-magnifying-glass"></i> Tìm xe khác</a>
            <a href="${ctx}/customerorders" class="btn btn--ghost"><i class="fas fa-clipboard-list"></i> Đơn của tôi</a>
            <a href="${ctx}/wallet" class="btn btn--ghost"><i class="fas fa-wallet"></i> Ví của tôi</a>
            <a href="${ctx}/" class="btn btn--ghost"><i class="fas fa-house"></i> Trang chủ</a>
        </div>
    </div>
</header>

<main class="container">
    <h1 class="page-title"><i class="fas fa-shopping-cart"></i> Giỏ hàng</h1>

    <c:if test="${not empty sessionScope.error}">
        <div class="card" style="border-color:#ef4444;color:#fca5a5">
            <i class="fas fa-exclamation-circle"></i> ${sessionScope.error}
        </div>
        <c:remove var="error" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.success}">
        <div class="card" style="border-color:#22c55e;color:#86efac">
            <i class="fas fa-check-circle"></i> ${sessionScope.success}
        </div>
        <c:remove var="success" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.warning}">
        <div class="card" style="border-color:#f59e0b;color:#fcd34d">
            <i class="fas fa-exclamation-triangle"></i> ${sessionScope.warning}
        </div>
        <c:remove var="warning" scope="session"/>
    </c:if>

    <c:choose>
        <c:when test="${empty cartItems}">
            <div class="empty-wrap">
                <div class="empty-card">
                    <div class="empty-icon"><i class="fas fa-shopping-cart"></i></div>
                    <div class="empty-title">Giỏ hàng của bạn đang trống</div>
                    <p class="empty-desc">
                        Hãy tiếp tục tìm kiếm chiếc xe phù hợp với nhu cầu của bạn. RideNow có hàng trăm mẫu xe với giá
                        hấp dẫn, sẵn sàng cho chuyến đi của bạn!
                    </p>
                    <div class="empty-actions">
                        <a href="${ctx}/motorbikesearch" class="btn-cta">
                            <i class="fas fa-magnifying-glass"></i> Tiếp tục tìm xe
                        </a>
                        <a href="${ctx}/" class="btn-ghost">
                            <i class="fas fa-house"></i> Về trang chủ
                        </a>
                    </div>
                </div>
            </div>
        </c:when>


        <c:otherwise>
            <!-- Form checkout: mọi input date phía dưới có form="checkoutForm" để submit trực tiếp -->
            <form action="${ctx}/cart" method="post" id="checkoutForm" style="display:inline;">
                <input type="hidden" name="action" value="checkout"/>
            </form>

            <div class="card">
                <div class="table-container">
                    <table class="table" id="cartTable">
                        <thead>
                        <tr>
                            <th>Xe thuê</th>
                            <th>Thời gian thuê</th>
                            <th>Giá/ngày</th>
                            <th>Số ngày</th>
                            <th>Tạm tính</th>
                            <th>Tiền cọc</th>
                            <th>Thao tác</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="it" items="${cartItems}" varStatus="st">
                            <c:set var="startISO"><fmt:formatDate value="${it.startDate}" pattern="yyyy-MM-dd"/></c:set>
                            <c:set var="endISO"><fmt:formatDate value="${it.endDate}" pattern="yyyy-MM-dd"/></c:set>
                            <tr data-row="${st.index}">
                                <td>
                                    <div class="bike-name">${it.bikeName}</div>
                                    <div class="bike-type">${it.typeName}</div>
                                </td>
                                <td>
                                    <div class="date-row">
                                        <label>
                                            <span class="field-hint">Từ</span><br/>
                                            <input type="date"
                                                   class="js-start"
                                                   name="start_${st.index}"
                                                   form="checkoutForm"
                                                   min="${todayISO}"
                                                   value="${startISO}"/>
                                        </label>
                                        <label>
                                            <span class="field-hint">Đến</span><br/>
                                            <input type="date"
                                                   class="js-end"
                                                   name="end_${st.index}"
                                                   form="checkoutForm"
                                                   min="${todayISO}"
                                                   value="${endISO}"/>
                                        </label>
                                    </div>
                                    <div class="js-error"></div>

                                    <!-- Form ẩn để SAVE (đặt ngoài date-row, cùng <td>) -->
                                    <form class="js-save-form" action="${ctx}/cart" method="post" style="display:none">
                                        <input type="hidden" name="action" value="updateDates"/>
                                        <input type="hidden" name="index" value="${st.index}"/>
                                        <input type="hidden" name="start" value="${startISO}"/>
                                        <input type="hidden" name="end" value="${endISO}"/>
                                    </form>
                                </td>
                                <td><span class="js-price" data-price="${it.pricePerDay}"><fmt:formatNumber
                                        value="${it.pricePerDay}" type="number"/> đ</span></td>
                                <td><span class="js-days">${it.days}</span></td>
                                <td><span class="js-subtotal" data-sub="${it.subtotal}"><fmt:formatNumber
                                        value="${it.subtotal}" type="number"/> đ</span></td>
                                <td><span class="js-deposit" data-deposit="${it.deposit}"><fmt:formatNumber
                                        value="${it.deposit}" type="number"/> đ</span></td>
                                <td style="display:flex;gap:8px;align-items:center">
                                    <button type="button" class="btn-gray js-save" title="Lưu ngày thuê">
                                        <i class="fa-solid fa-floppy-disk"></i> Save
                                    </button>
                                    <form action="${ctx}/cart" method="post" style="display:inline">
                                        <input type="hidden" name="action" value="remove"/>
                                        <input type="hidden" name="index" value="${st.index}"/>
                                        <button class="btn-danger" type="submit" title="Xóa khỏi giỏ hàng">
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

            <div class="actions">
                <div class="summary-card" id="summary">
                    <div class="summary-row">
                        <span>Tổng tiền thuê xe</span>
                        <span><span id="sum-rent"><fmt:formatNumber value="${total}" type="number"/></span> đ</span>
                    </div>
                    <div class="summary-row">
                        <span>Tổng tiền cọc</span>
                        <span><span id="sum-deposit"><fmt:formatNumber value="${depositTotal}" type="number"/></span> đ</span>
                    </div>
                    <div class="summary-row summary-total">
                        <span>Thanh toán ngay (30% + cọc)</span>
                        <span><span id="sum-topay"><fmt:formatNumber value="${toPayNow}" type="number"/></span> đ</span>
                    </div>
                    <button type="submit" form="checkoutForm" class="btn-gray" style="width:100%;margin-top:12px">
                        <i class="fas fa-credit-card"></i> Tiến hành thanh toán
                    </button>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<script>
    function parseISO(d) {
        return new Date(d + "T00:00:00");
    }

    function fmtNumber(n) {
        return n.toLocaleString('vi-VN');
    }

    function diffDaysInclusive(startISO, endISO) {
        const ms = parseISO(endISO) - parseISO(startISO);
        return Math.floor(ms / (1000 * 60 * 60 * 24)) + 1;
    }

    function recalcSummary() {
        const subs = Array.from(document.querySelectorAll('.js-subtotal')).map(e => Number(e.dataset.sub || 0));
        const deposits = Array.from(document.querySelectorAll('.js-deposit')).map(e => Number(e.dataset.deposit || 0));
        const sumRent = subs.reduce((a, b) => a + b, 0);
        const sumDepo = deposits.reduce((a, b) => a + b, 0);
        const toPayNow = Math.round(sumRent * 0.3) + sumDepo;
        const elRent = document.getElementById('sum-rent');
        const elDepo = document.getElementById('sum-deposit');
        const elPay = document.getElementById('sum-topay');
        if (elRent) elRent.textContent = fmtNumber(sumRent);
        if (elDepo) elDepo.textContent = fmtNumber(sumDepo);
        if (elPay) elPay.textContent = fmtNumber(toPayNow);
    }

    function recalcRow(tr) {
        const start = tr.querySelector('.js-start').value;
        const end = tr.querySelector('.js-end').value;
        const price = Number(tr.querySelector('.js-price').dataset.price);
        const errEl = tr.querySelector('.js-error');
        const today = '${todayISO}';
        errEl.style.display = 'none';
        errEl.textContent = '';
        if (!start || !end) return;
        if (start < today) {
            errEl.textContent = 'Ngày nhận không được ở quá khứ.';
            errEl.style.display = 'block';
            return;
        }
        if (end < start) {
            errEl.textContent = 'Ngày trả phải sau hoặc bằng ngày nhận.';
            errEl.style.display = 'block';
            return;
        }
        const days = diffDaysInclusive(start, end);
        const sub = price * days;
        tr.querySelector('.js-days').textContent = String(days);
        tr.querySelector('.js-subtotal').dataset.sub = String(sub);
        tr.querySelector('.js-subtotal').textContent = fmtNumber(sub) + ' đ';
        recalcSummary();
    }

    document.addEventListener('DOMContentLoaded', function () {
        // lắng nghe thay đổi để tính lại UI
        document.querySelectorAll('#cartTable tbody tr').forEach(tr => {
            const start = tr.querySelector('.js-start');
            const end = tr.querySelector('.js-end');
            if (start && end) {
                ['input', 'change'].forEach(ev => {
                    start.addEventListener(ev, () => recalcRow(tr));
                    end.addEventListener(ev, () => recalcRow(tr));
                });
            }
        });

        // ====== HANDLER NÚT SAVE: event delegation + closest() + fallback ======
        const table = document.getElementById('cartTable');
        if (table) {
            table.addEventListener('click', (ev) => {
                const btn = ev.target.closest('.js-save');
                if (!btn) return;

                const tr = btn.closest('tr');
                if (!tr) {
                    console.warn('Không tìm thấy <tr> chứa nút Save.');
                    return;
                }

                const startEl = tr.querySelector('.js-start');
                const endEl = tr.querySelector('.js-end');
                const errEl = tr.querySelector('.js-error');

                if (!startEl || !endEl) {
                    console.warn('Không tìm thấy input date trong hàng.');
                    return;
                }

                const startVal = startEl.value;
                const endVal = endEl.value;

                // validate nhanh phía client
                const today = '${todayISO}';
                if (errEl) {
                    errEl.style.display = 'none';
                    errEl.textContent = '';
                }
                if (!startVal || !endVal) {
                    if (errEl) {
                        errEl.textContent = 'Vui lòng chọn đủ ngày.';
                        errEl.style.display = 'block';
                    }
                    return;
                }
                if (startVal < today) {
                    if (errEl) {
                        errEl.textContent = 'Ngày nhận không được ở quá khứ.';
                        errEl.style.display = 'block';
                    }
                    return;
                }
                if (endVal < startVal) {
                    if (errEl) {
                        errEl.textContent = 'Ngày trả phải sau hoặc bằng ngày nhận.';
                        errEl.style.display = 'block';
                    }
                    return;
                }

                // tìm form ẩn trong cùng <td>
                let form = tr.querySelector('.js-save-form');

                // Fallback tạo form tạm nếu không thấy
                if (!form) {
                    console.warn('Không tìm thấy form ẩn trong hàng. Tạo form tạm để submit.');
                    form = document.createElement('form');
                    form.method = 'post';
                    form.action = `${window.location.origin}${'${ctx}'}/cart`;
                    form.style.display = 'none';

                    const makeHidden = (name, value) => {
                        const input = document.createElement('input');
                        input.type = 'hidden';
                        input.name = name;
                        input.value = value;
                        form.appendChild(input);
                    };

                    // Lấy index từ thuộc tính data-row (fallback)
                    const idx = tr.getAttribute('data-row') || '0';
                    makeHidden('action', 'updateDates');
                    makeHidden('index', idx);
                    makeHidden('start', startVal);
                    makeHidden('end', endVal);

                    document.body.appendChild(form);
                    form.submit();
                    return;
                }

                // có form: set giá trị mới và submit
                const hiddenStart = form.querySelector('input[name="start"]');
                const hiddenEnd = form.querySelector('input[name="end"]');
                if (hiddenStart) hiddenStart.value = startVal;
                if (hiddenEnd) hiddenEnd.value = endVal;

                form.submit(); // POST /cart?action=updateDates&index=...&start=...&end=...
            });
        }
        // =======================================================================

        // chặn checkout nếu còn lỗi hiển thị
        const checkoutForm = document.getElementById('checkoutForm');
        if (checkoutForm) {
            checkoutForm.addEventListener('submit', function (e) {
                const hasError = Array.from(document.querySelectorAll('.js-error'))
                    .some(el => el.style.display !== 'none' && el.textContent.trim() !== '');
                if (hasError) {
                    e.preventDefault();
                    alert('Vui lòng sửa các lỗi ngày thuê trước khi thanh toán.');
                }
            });
        }
    });
</script>

</body>
</html>
