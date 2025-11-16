<%-- an --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Gửi thông báo tới đối tác | RideNow Admin</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
    <style>
        /* Layout */
        .broadcast-wrap {
            display: grid;
            grid-template-columns: 1.4fr .9fr;
            gap: 1rem
        }

        @media (max-width: 992px) {
            .broadcast-wrap {
                grid-template-columns:1fr
            }
        }

        /* Form */
        .form-row {
            display: flex;
            flex-direction: column;
            gap: .5rem;
            margin: .75rem 0
        }

        .form-row input, .form-row textarea {
            width: 100%;
            padding: .8rem .9rem;
            border: 1px solid var(--border-color);
            border-radius: 10px;
            background: #fff;
            color: #111;
            font: inherit
        }

        .form-row textarea {
            min-height: 180px;
            resize: vertical
        }

        .row-tools {
            display: flex;
            align-items: center;
            gap: .5rem;
            justify-content: space-between
        }

        .hint {
            color: var(--text-light);
            font-size: .9rem
        }

        /* Counters */
        .counter {
            font-size: .85rem;
            color: var(--text-light)
        }

        .counter .ok {
            color: #16a34a
        }

        .counter .warn {
            color: #f59e0b
        }

        .counter .err {
            color: #dc2626
        }

        /* Chips */
        .chips {
            display: flex;
            flex-wrap: wrap;
            gap: .5rem;
            margin-top: .25rem
        }

        .chip {
            border: 1px solid var(--border-color);
            background: var(--card-bg);
            color: var(--text);
            padding: .35rem .6rem;
            border-radius: 999px;
            cursor: pointer;
            font-size: .85rem;
            transition: .15s
        }

        .chip:hover {
            transform: translateY(-1px);
            border-color: var(--primary-color);
            color: var(--primary-color)
        }

        /* Actions */
        .actions {
            display: flex;
            gap: .6rem;
            justify-content: flex-end;
            margin-top: .75rem
        }

        .btn-ghost {
            background: transparent;
            border: 1px dashed var(--border-color);
            color: var(--text)
        }

        .btn-ghost:hover {
            border-color: var(--primary-color);
            color: var(--primary-color)
        }

        /* Preview card */
        .preview {
            border: 1px solid var(--border-color);
            border-radius: 12px;
            background: var(--card-bg);
            padding: 1rem
        }

        .pv-head {
            display: flex;
            align-items: center;
            gap: .6rem;
            margin-bottom: .35rem
        }

        .pv-ic {
            width: 36px;
            height: 36px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            background: #e3f2fd;
            color: #1976d2
        }

        .pv-title {
            font-weight: 700
        }

        .pv-time {
            font-size: .85rem;
            color: var(--text-light)
        }

        .pv-body {
            white-space: pre-wrap;
            color: var(--text)
        }

        /* Recipient (details) */
        details.target {
            border: 1px solid var(--border-color);
            border-radius: 10px;
            background: var(--card-bg);
            padding: .75rem 1rem;
            margin-top: .6rem
        }

        details.target > summary {
            list-style: none;
            cursor: pointer;
            font-weight: 700;
            display: flex;
            align-items: center;
            gap: .5rem
        }

        details.target > summary::before {
            content: "\f1d8";
            font-family: "Font Awesome 6 Free";
            font-weight: 900;
            color: var(--primary-color)
        }

        details.target[open] {
            box-shadow: 0 1px 0 rgba(0, 0, 0, .02) inset
        }

        .label-sub {
            font-size: .85rem;
            color: var(--text-light);
            margin-top: -.25rem
        }
    </style>
</head>
<body class="admin">
<fmt:setLocale value="vi_VN"/>

<!-- Sidebar -->
<aside class="sidebar">
    <div class="brand">
        <div class="brand-logo"><i class="fas fa-motorcycle"></i></div>
        <h1>RideNow Admin</h1>
    </div>
    <nav class="sidebar-nav">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="nav-item"><i
                class="fas fa-tachometer-alt"></i><span>Dashboard</span></a>
        <a href="${pageContext.request.contextPath}/admin/partners" class="nav-item"><i
                class="fas fa-handshake"></i><span>Partners</span></a>
        <a href="${pageContext.request.contextPath}/admin/customers" class="nav-item"><i class="fas fa-users"></i><span>Customers</span></a>
        <a href="${pageContext.request.contextPath}/admin/bikes" class="nav-item"><i
                class="fas fa-motorcycle"></i><span>Motorbikes</span></a>
        <a href="${pageContext.request.contextPath}/admin/orders" class="nav-item"><i class="fas fa-clipboard-list"></i><span>Orders</span></a>
        <a href="${pageContext.request.contextPath}/admin/schedule" class="nav-item">
            <i class="fas fa-calendar-alt"></i><span>View Schedule</span>
        </a>
        <a href="${pageContext.request.contextPath}/adminpaymentverify" class="nav-item"><i
                class="fas fa-money-check-alt"></i><span>Verify Payments</span></a>
        <a href="${pageContext.request.contextPath}/adminpickup" class="nav-item"><i
                class="fas fa-shipping-fast"></i><span>Vehicle Pickup</span></a>
        <a href="${pageContext.request.contextPath}/adminreturn" class="nav-item"><i class="fas fa-undo-alt"></i><span>Vehicle Return</span></a>
        <a href="${pageContext.request.contextPath}/adminreturns" class="nav-item"><i
                class="fas fa-clipboard-check"></i><span>Verify & Refund</span></a>
        <a href="${pageContext.request.contextPath}/admin/reports" class="nav-item"><i
                class="fas fa-chart-bar"></i><span>Reports</span></a>
        <a href="${pageContext.request.contextPath}/admin/feedback" class="nav-item"><i
                class="fas fa-comment-alt"></i><span>Feedback</span></a>
        <a href="${pageContext.request.contextPath}/logout" class="nav-item logout"><i
                class="fas fa-sign-out-alt"></i><span>Logout</span></a>
    </nav>
</aside>

<main class="content">
    <header class="content-header">
        <div class="header-left">
            <h1>Thông báo diện rộng</h1>
            <div class="breadcrumb">
                <span>Admin</span>
                <i class="fas fa-chevron-right"></i>
                <span class="active">Thông báo diện rộng</span>
            </div>
        </div>
        <div class="header-right">
            <div class="user-profile">
                <div class="user-avatar"><i class="fas fa-user-circle"></i></div>
                <span>Administrator</span>
            </div>
        </div>
    </header>

    <!-- Alerts từ servlet -->
    <c:if test="${param.broadcast eq 'ok'}">
        <div class="alert alert-success" role="alert"><i class="fas fa-check-circle"></i> Đã gửi thành công
            cho ${param.sent} đối tác.
        </div>
    </c:if>
    <c:if test="${param.broadcast eq 'fail'}">
        <div class="alert alert-error" role="alert"><i class="fas fa-times-circle"></i> Gửi thất bại. Vui lòng thử lại.
        </div>
    </c:if>
    <c:if test="${param.broadcast eq 'invalid'}">
        <div class="alert alert-warning" role="alert"><i class="fas fa-exclamation-triangle"></i> Vui lòng nhập đủ tiêu
            đề và nội dung.
        </div>
    </c:if>
    <c:if test="${param.broadcast eq 'notfound'}">
        <div class="alert alert-warning" role="alert"><i class="fas fa-exclamation-triangle"></i> Không tìm thấy đối tác
            khớp với tiêu chí người nhận.
        </div>
    </c:if>

    <section class="panel">
        <div class="panel-header">
            <h2>Soạn thông báo gửi đối tác</h2>
        </div>
        <div class="panel-body">
            <div class="broadcast-wrap">
                <!-- LEFT: Form -->
                <form id="bcForm" method="post" action="${pageContext.request.contextPath}/admin/notify"
                      autocomplete="off" novalidate>
                    <!-- Title -->
                    <div class="form-row">
                        <div class="row-tools">
                            <label for="title" style="font-weight:600">Tiêu đề (tối đa 200 ký tự)</label>
                        </div>
                        <input id="title" name="title" maxlength="200" required
                               placeholder="VD: Bảo trì hệ thống từ 23:00–24:00 đêm nay">
                        <div class="chips" aria-label="Mẫu nội dung nhanh">
                            <button type="button" class="chip" data-tpl-title="Bảo trì hệ thống"
                                    data-tpl-msg="Hệ thống sẽ bảo trì từ 23:00–24:00. Trong thời gian này có thể có gián đoạn tạm thời. Cảm ơn bạn đã phối hợp.">
                                Bảo trì hệ thống
                            </button>
                            <button type="button"
                                    class="chip"
                                    data-tpl-title="Thông báo vận hành"
                                    data-tpl-msg="Vui lòng cập nhật thông tin cửa hàng nếu thay đổi địa chỉ hoặc SĐT để khách liên hệ thuận tiện.">
                                Thông báo vận hành
                            </button>
                        </div>
                    </div>

                    <!-- Message -->
                    <div class="form-row">
                        <div class="row-tools">
                            <label for="message" style="font-weight:600">Nội dung</label>
                        </div>
                        <textarea id="message" name="message" required
                                  placeholder="Nhập nội dung gửi tới đối tác…"></textarea>

                        <!-- Recipient -->
                        <details class="target">
                            <summary>Người nhận (tuỳ chọn)</summary>
                            <div class="form-row" style="margin-top:.5rem">
                                <label for="username" style="font-weight:600">Người nhận cụ thể</label>
                                <div class="label-sub">Nhập <strong>tên người dùng</strong> hoặc <strong>tên công
                                    ty</strong>. Để trống để gửi tới tất cả đối tác.
                                </div>
                                <input id="username" name="username" placeholder="VD: suzuki | Honda Đà Nẵng">
                            </div>
                        </details>


                    </div>

                    <div class="actions">
                        <button type="button" class="btn btn-ghost" id="clearDraft"><i class="fas fa-eraser"></i> Xoá
                            nháp
                        </button>
                        <a class="btn btn-outline" href="${pageContext.request.contextPath}/admin/dashboard">Huỷ</a>
                        <button class="btn btn-primary" type="submit"><i class="fas fa-paper-plane"></i> Gửi thông báo
                        </button>
                    </div>
                </form>

                <!-- RIGHT: Preview -->
                <aside class="preview" aria-label="Xem trước">
                    <div class="pv-head">
                        <div class="pv-ic"><i class="fas fa-bell"></i></div>
                        <div>
                            <div class="pv-title" id="pvTitle">Tiêu đề thông báo</div>
                            <div class="pv-time" id="pvTime"></div>
                        </div>
                    </div>
                    <div class="pv-body" id="pvMsg">Nội dung sẽ hiển thị tại đây…</div>
                </aside>
            </div>
        </div>
    </section>
</main>

<script>
    (function () {
        const titleMax = 200;
        const titleEl = document.getElementById('title');
        const msgEl = document.getElementById('message');
        const titleCounter = document.getElementById('titleCounter'); // có thể không tồn tại
        const msgCounter = document.getElementById('msgCounter');   // có thể không tồn tại
        const pvTitle = document.getElementById('pvTitle');
        const pvMsg = document.getElementById('pvMsg');
        const pvTime = document.getElementById('pvTime');
        const clearBtn = document.getElementById('clearDraft');

        function fmt2(n) {
            return (n < 10 ? '0' : '') + n;
        }

        function setNow() {
            const d = new Date();
            pvTime.textContent = fmt2(d.getHours()) + ":" + fmt2(d.getMinutes()) + " " + fmt2(d.getDate()) + "/" + fmt2(d.getMonth() + 1) + "/" + d.getFullYear();
        }

        setNow();

        function updateTitleCounter() {
            if (!titleCounter) return;
            const len = (titleEl.value || '').length;
            let cls = 'ok';
            if (len > titleMax) cls = 'err';
            else if (len > titleMax - 20) cls = 'warn';
            titleCounter.innerHTML = `<span class="${cls}">${len}</span>/${titleMax}`;
        }

        function updateMsgCounter() {
            if (!msgCounter) return;
            const len = (msgEl.value || '').length;
            let cls = 'ok';
            if (len > 1000) cls = 'warn';
            if (len > 3000) cls = 'err';
            msgCounter.innerHTML = `<span class="${cls}">${len}</span> ký tự`;
        }

        function escapeHTML(s) {
            return s.replace(/[&<>"']/g, m => ({
                '&': '&amp;',
                '<': '&lt;',
                '>': '&gt;',
                '"': '&quot;',
                "'": '&#39;'
            }[m]));
        }

        function updatePreview() {
            pvTitle.textContent = (titleEl.value || '').trim() || 'Tiêu đề thông báo';
            const raw = msgEl.value || 'Nội dung sẽ hiển thị tại đây…';
            pvMsg.innerHTML = escapeHTML(raw); // văn bản thuần, không highlight token
        }

        const KEY_T = 'bc_title', KEY_M = 'bc_message';
        try {
            const t = localStorage.getItem(KEY_T);
            if (t) titleEl.value = t;
            const m = localStorage.getItem(KEY_M);
            if (m) msgEl.value = m;
        } catch (e) {
        }

        function saveDraft() {
            try {
                localStorage.setItem(KEY_T, titleEl.value || '');
                localStorage.setItem(KEY_M, msgEl.value || '');
            } catch (e) {
            }
        }

        titleEl.addEventListener('input', () => {
            updateTitleCounter();
            updatePreview();
            saveDraft();
        });
        msgEl.addEventListener('input', () => {
            updateMsgCounter();
            updatePreview();
            saveDraft();
        });

        // Mẫu nhanh tiêu đề + nội dung
        document.querySelectorAll('.chip[data-tpl-title]').forEach(ch => {
            ch.addEventListener('click', () => {
                titleEl.value = ch.getAttribute('data-tpl-title') || '';
                msgEl.value = ch.getAttribute('data-tpl-msg') || '';
                updateTitleCounter();
                updateMsgCounter();
                updatePreview();
                saveDraft();
            });
        });

        // Không hỗ trợ chip chèn URL/ORDER/BIKE → bỏ hoàn toàn

        clearBtn?.addEventListener('click', () => {
            if (confirm('Xoá nháp hiện tại?')) {
                titleEl.value = '';
                msgEl.value = '';
                updateTitleCounter();
                updateMsgCounter();
                updatePreview();
                try {
                    localStorage.removeItem(KEY_T);
                    localStorage.removeItem(KEY_M);
                } catch (e) {
                }
            }
        });

        // Khởi tạo lần đầu
        updateTitleCounter();
        updateMsgCounter();
        updatePreview();

        // Ẩn alert tự động
        setTimeout(() => document.querySelectorAll('.alert').forEach(a => {
            a.style.transition = 'opacity .4s';
            a.style.opacity = '0';
            setTimeout(() => a.remove(), 400);
        }), 5000);
    })();

    (function () {
        const form = document.getElementById('bcForm');
        const uEl = document.getElementById('username');
        form?.addEventListener('submit', function (e) {
            const u = (uEl?.value || '').trim();
            if (!u) {
                if (!confirm('Bạn sắp gửi cho tất cả đối tác. Xác nhận gửi?')) {
                    e.preventDefault();
                    return;
                }
            } else {
                if (!confirm('Gửi thông báo cho: ' + u + ' (khớp theo username hoặc company_name). Xác nhận gửi?')) {
                    e.preventDefault();
                    return;
                }
            }
        });
    })();
</script>

</body>
</html>
