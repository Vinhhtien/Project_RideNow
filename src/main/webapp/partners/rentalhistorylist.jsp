<%-- an --%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <title>Lịch sử thuê xe</title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <style>
        :root {
            --bg: #0b0f1f;
            --bg2: #0a0d1c;
            --card: #121735;
            --card2: #10152e;
            --text: #e8eaf6;
            --muted: #a7b0d6;
            --border: #1c244c;
            --shadow: 0 10px 30px rgba(0, 0, 0, .35);
            --primary: #6c8cff;
            --accent: #22d3ee;
            --danger: #ff6b6b;
            --success: #34d399;
            --warn: #f59e0b;
            --radius: 16px;
        }

        * {
            box-sizing: border-box
        }

        html, body {
            margin: 0
        }

        body {
            font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Arial;
            background: radial-gradient(1200px 600px at -10% -10%, #24306b 0%, transparent 60%),
            radial-gradient(900px 600px at 110% -20%, #391d5f 0%, transparent 55%),
            linear-gradient(180deg, var(--bg) 0%, var(--bg2) 100%);
            min-height: 100vh;
            color: var(--text);
            padding: 24px;
        }

        .wrap {
            max-width: 1100px;
            margin: 0 auto
        }

        .top {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 10px;
            margin-bottom: 16px
        }

        .title {
            font-size: 26px;
            font-weight: 800;
            margin: 0;
            background: linear-gradient(90deg, #eaf0ff, #b9c7ff, #6c8cff);
            -webkit-background-clip: text;
            background-clip: text;
            color: transparent
        }

        .btn {
            appearance: none;
            border: 1px solid #3a4477;
            background: transparent;
            color: var(--text);
            padding: 10px 14px;
            border-radius: 12px;
            cursor: pointer
        }

        .btn:hover {
            filter: brightness(1.05)
        }

        .btn.primary {
            background: var(--accent);
            color: #0f172a;
            border-color: #24cde0
        }

        .card {
            background: linear-gradient(180deg, rgba(255, 255, 255, .03), rgba(255, 255, 255, .015)), var(--card);
            border: 1px solid var(--border);
            border-radius: var(--radius);
            box-shadow: var(--shadow);
            padding: 16px;
        }

        /* Toolbar */
        .toolbar {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
            margin-bottom: 12px;
            align-items: center
        }

        .search {
            flex: 1;
            display: flex;
            gap: 8px
        }

        .search input {
            flex: 1;
            background: #111827;
            border: 1px solid #2a335e;
            color: var(--text);
            border-radius: 10px;
            padding: 10px 12px
        }

        .chip {
            border: 1px solid #3a4477;
            background: transparent;
            color: var(--text);
            padding: 6px 10px;
            border-radius: 999px;
            cursor: pointer;
            font-size: 13px
        }

        .chip.active {
            background: #1e3a8a;
            border-color: #1f3fb5
        }

        /* Table */
        table {
            width: 100%;
            border-collapse: collapse;
            background: var(--card2);
            border: 1px solid var(--border);
            border-radius: 12px;
            overflow: hidden
        }

        thead th {
            background: #0f142e;
            color: #b9c7ff;
            font-weight: 800;
            text-align: left;
            padding: 12px;
            border-bottom: 1px solid var(--border);
            font-size: 13px;
            letter-spacing: .3px
        }

        tbody td {
            padding: 12px;
            border-bottom: 1px solid #222b57;
            vertical-align: top
        }

        tbody tr:hover {
            background: #101735
        }

        .right {
            text-align: right
        }

        .muted {
            color: var(--muted)
        }

        .badge {
            display: inline-block;
            border-radius: 999px;
            padding: 4px 10px;
            font-size: 11px;
            font-weight: 800;
            letter-spacing: .3px;
            border: 1px solid #3a4477;
        }

        .ok {
            background: rgba(52, 211, 153, .15);
            color: #b9f9de;
            border-color: rgba(52, 211, 153, .35)
        }

        .bad {
            background: rgba(255, 107, 107, .15);
            color: #ffd0d0;
            border-color: rgba(255, 107, 107, .35)
        }

        .warn {
            background: rgba(245, 158, 11, .18);
            color: #ffe4b5;
            border-color: rgba(245, 158, 11, .35)
        }

        /* Highlight when coming from ?orderId= */
        tr.hl {
            outline: 2px solid var(--accent)
        }

        /* Modal */
        .modal {
            position: fixed;
            inset: 0;
            display: none;
            z-index: 50
        }

        .modal.open {
            display: block
        }

        .modal .backdrop {
            position: absolute;
            inset: 0;
            background: rgba(0, 0, 0, .55)
        }

        .modal .dialog {
            position: absolute;
            left: 50%;
            top: 8%;
            transform: translateX(-50%);
            width: min(680px, 94%);
            background: var(--card);
            border: 1px solid var(--border);
            border-radius: 16px;
            box-shadow: var(--shadow);
            padding: 16px 16px 12px;
        }

        .dialog h3 {
            margin: 2px 0 2px
        }

        .dialog .meta {
            color: var(--muted);
            font-size: 13px;
            margin-bottom: 8px
        }

        .dialog .table {
            border: 1px solid var(--border);
            border-radius: 12px;
            overflow: hidden
        }

        .dialog thead th {
            background: #0f142e
        }

        .dialog .foot {
            display: flex;
            gap: 10px;
            justify-content: flex-end;
            margin-top: 12px
        }
    </style>
</head>
<body>
<c:set var="orders"
       value="${not empty displayOrders ? displayOrders : (not empty rentalList ? rentalList : histories)}"/>
<div class="wrap">
    <div class="top">
        <h1 class="title">📜 Lịch sử thuê xe</h1>
        <div class="actions">
            <button class="btn" onclick="location.href='${pageContext.request.contextPath}/dashboard'">← Về Dashboard
            </button>
        </div>
    </div>

    <div class="card">
        <div class="toolbar">
            <div class="search">
                <input id="q" type="search" placeholder="Tìm theo mã đơn, khách, trạng thái..."/>
                <button class="btn" onclick="filterRows()">Tìm</button>
            </div>
            <button id="chipAll" class="chip active" type="button" onclick="setFilter('all')">Tất cả</button>
            <button id="chipUnread" class="chip" type="button" onclick="setFilter('pending')">Chưa hoàn tất</button>
            <button id="chipDone" class="chip" type="button" onclick="setFilter('done')">Đã hoàn tất</button>
            <button id="chipCancel" class="chip" type="button" onclick="setFilter('cancel')">Đã huỷ</button>
        </div>

        <c:choose>
            <c:when test="${empty orders}">
                <div class="muted" style="padding:8px 4px 0">Không có dữ liệu lịch sử cho thuê.</div>
            </c:when>
            <c:otherwise>
                <table id="tbl">
                    <thead>
                    <tr>
                        <th style="width:120px">Mã đơn</th>
                        <th>Khách hàng</th>
                        <th style="width:160px">Thời gian</th>
                        <th class="right" style="width:140px">Tổng tiền</th>
                        <th style="width:140px">Trạng thái</th>
                        <th style="width:110px">Chi tiết</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="o" items="${orders}">
                        <%-- Chuẩn hoá status, map sang badgeClass an toàn (không dùng matches/3-ngôi) --%>
                        <c:set var="statusLower" value="${empty o.status ? '' : fn:toLowerCase(o.status)}"/>
                        <c:choose>
                            <c:when test=" ${ fn:contains(statusLower,'done')
                                  or fn:contains(statusLower,'completed')
                                  or fn:contains(statusLower,'returned')
                                  or fn:contains(statusLower,'paid') } ">
                                <c:set var="badgeClass" value="ok"/>
                            </c:when>
                            <c:when test=" ${ fn:contains(statusLower,'cancel')
                                  or fn:contains(statusLower,'rejected')
                                  or fn:contains(statusLower,'failed')
                                  or fn:contains(statusLower,'deny') } ">
                                <c:set var="badgeClass" value="bad"/>
                            </c:when>
                            <c:otherwise>
                                <c:set var="badgeClass" value="warn"/>
                            </c:otherwise>
                        </c:choose>

                        <c:set var="rowClass" value=""/>
                        <c:if test="${param.orderId == o.orderId}">
                            <c:set var="rowClass" value="hl"/>
                        </c:if>

                        <tr class="${rowClass}"
                            data-text="${o.orderId} ${o.customerName} ${o.status}"
                            data-status="${statusLower}">
                            <td>#<c:out value="${o.orderId}"/></td>
                            <td>
                                <div><c:out
                                        value="${o.customerName != null ? o.customerName : 'Khách #'+o.customerId}"/></div>
                                <div class="muted">Xe:
                                    <c:out value="${o.bikeSummary != null ? o.bikeSummary : (o.bikeCount != null ? (o.bikeCount + ' xe') : '—')}"/>
                                </div>
                            </td>
                            <td>
                                <div>
                                    <fmt:formatDate value="${o.startDate}" pattern="HH:mm dd/MM/yyyy"/>
                                    <span class="muted">→</span>
                                </div>
                                <div><fmt:formatDate value="${o.endDate}" pattern="HH:mm dd/MM/yyyy"/></div>
                            </td>
                            <td class="right">
                                <fmt:formatNumber value="${o.totalPrice}" type="currency" currencySymbol="₫"/>
                            </td>
                            <td>
                    <span class="badge ${badgeClass}">
                      <c:out value="${o.status != null ? o.status : '—'}"/>
                    </span>
                            </td>
                            <td>
                                <button class="btn primary" onclick="showDetail(<c:out value='${o.orderId}'/>)">Xem
                                </button>
                            </td>
                        </tr>

                        <%-- Chi tiết đơn: nhúng JSON an toàn để modal đọc --%>
                        <script type="application/json" id="od-<c:out value='${o.orderId}'/>">
                            [
                            <c:forEach var="d" items="${o.details}" varStatus="s">
                                {
                                "bikeId": ${d.bikeId != null ? d.bikeId : 'null'},
                                "pricePerDay": ${d.pricePerDay != null ? d.pricePerDay : 'null'},
                                "quantity": ${d.quantity != null ? d.quantity : 'null'},
                                "lineTotal": ${d.lineTotal != null ? d.lineTotal : 'null'}
                                }<c:if test="${!s.last}">,</c:if>
                            </c:forEach>
                            ]
                        </script>
                    </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<!-- Modal chi tiết -->
<div id="popup" class="modal" aria-hidden="true">
    <div class="backdrop" onclick="closePopup()"></div>
    <div class="dialog" role="dialog" aria-label="Chi tiết đơn hàng">
        <h3 id="mdTitle">Chi tiết đơn</h3>
        <div class="meta" id="mdSub">Mã đơn: —</div>

        <div class="table">
            <table>
                <thead>
                <tr>
                    <th style="width:120px">Mã xe</th>
                    <th class="right">Giá/ngày</th>
                    <th class="right">Số lượng</th>
                    <th class="right">Tổng dòng</th>
                </tr>
                </thead>
                <tbody id="popupBody">
                <tr>
                    <td colspan="4" class="muted" style="padding:12px">Không có chi tiết.</td>
                </tr>
                </tbody>
            </table>
        </div>

        <div class="foot">
            <button class="btn" onclick="closePopup()">Đóng</button>
        </div>
    </div>
</div>

<script>
    // ======== Filter/Search ========
    let statusFilter = 'all';

    function setFilter(type) {
        statusFilter = type;
        document.getElementById('chipAll').classList.toggle('active', type === 'all');
        document.getElementById('chipUnread').classList.toggle('active', type === 'pending');
        document.getElementById('chipDone').classList.toggle('active', type === 'done');
        document.getElementById('chipCancel').classList.toggle('active', type === 'cancel');
        filterRows();
    }

    function filterRows() {
        var kw = (document.getElementById('q').value || '').toLowerCase();
        var rows = document.querySelectorAll('#tbl tbody tr');
        rows.forEach(function (tr) {
            var text = (tr.getAttribute('data-text') || '').toLowerCase();
            var st = tr.getAttribute('data-status') || '';
            var matchKw = !kw || text.indexOf(kw) >= 0;
            var matchSt =
                statusFilter === 'all' ? true :
                    statusFilter === 'pending' ? !(st.indexOf('done') >= 0 || st.indexOf('completed') >= 0 || st.indexOf('returned') >= 0 || st.indexOf('paid') >= 0) && st.indexOf('cancel') < 0 :
                        statusFilter === 'done' ? (st.indexOf('done') >= 0 || st.indexOf('completed') >= 0 || st.indexOf('returned') >= 0 || st.indexOf('paid') >= 0) :
                            statusFilter === 'cancel' ? st.indexOf('cancel') >= 0 : true;
            tr.style.display = (matchKw && matchSt) ? '' : 'none';
        });
    }

    // Gõ search là lọc ngay
    (function () {
        var q = document.getElementById('q');
        if (q) {
            q.addEventListener('input', filterRows);
        }
    })();

    // ======== Modal detail ========
    function showDetail(orderId) {
        var sc = document.getElementById('od-' + orderId);
        var tbody = document.getElementById('popupBody');
        var mdTitle = document.getElementById('mdTitle');
        var mdSub = document.getElementById('mdSub');
        mdTitle.textContent = 'Chi tiết đơn';
        mdSub.textContent = 'Mã đơn: #' + orderId;

        var items = [];
        if (sc) {
            try {
                items = JSON.parse(sc.textContent);
            } catch (e) {
                items = [];
            }
        }

        tbody.innerHTML = '';
        if (!items || !items.length) {
            tbody.innerHTML = '<tr><td colspan="4" class="muted" style="padding:12px">Không có chi tiết.</td></tr>';
        } else {
            items.forEach(function (d) {
                var tr = document.createElement('tr');
                tr.innerHTML =
                    '<td>' + (d.bikeId != null ? d.bikeId : '—') + '</td>' +
                    '<td class="right">' + fmtCurrency(d.pricePerDay) + '</td>' +
                    '<td class="right">' + (d.quantity != null ? d.quantity : '—') + '</td>' +
                    '<td class="right">' + fmtCurrency(d.lineTotal) + '</td>';
                tbody.appendChild(tr);
            });
        }
        openPopup();
    }

    function fmtCurrency(n) {
        if (n == null || isNaN(n)) return '—';
        try {
            return new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'}).format(Number(n));
        } catch (e) {
            return Number(n).toLocaleString('vi-VN') + ' ₫';
        }
    }

    function openPopup() {
        var m = document.getElementById('popup');
        m.classList.add('open');
        m.setAttribute('aria-hidden', 'false');
    }

    function closePopup() {
        var m = document.getElementById('popup');
        m.classList.remove('open');
        m.setAttribute('aria-hidden', 'true');
    }

    // ESC close + auto highlight nếu có ?orderId
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && document.getElementById('popup').classList.contains('open')) closePopup();
    });
    (function () {
        var params = new URLSearchParams(location.search);
        var oid = params.get('orderId');
        if (!oid) return;
        var rows = document.querySelectorAll('#tbl tbody tr');
        for (var i = 0; i < rows.length; i++) {
            var firstCell = rows[i].querySelector('td');
            if (firstCell && firstCell.textContent && firstCell.textContent.replace('#', '').trim() === String(oid)) {
                rows[i].classList.add('hl');
                break;
            }
        }
    })();
</script>
</body>
</html>
