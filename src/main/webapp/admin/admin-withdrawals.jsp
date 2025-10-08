<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hoàn Tiền Cọc - RideNow Admin</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${ctx}/css/admin.css">
    <style>
        .badge{padding:4px 8px;border-radius:999px;font-size:12px;border:1px solid #e5e7eb}
        .pending{background:#fff7ed;color:#d97706;border-color:#fdba74}
        .processing{background:#eff6ff;color:#2563eb;border-color:#93c5fd}
        .completed{background:#ecfdf5;color:#059669;border-color:#6ee7b7}
        .cancelled{background:#fef2f2;color:#b91c1c;border-color:#fecaca}
        .refunded{background:#ecfdf5;color:#059669;border-color:#6ee7b7}
        .held{background:#f3f4f6;color:#374151;border-color:#d1d5db}
        .actions form{display:inline; margin: 2px;}
        .empty-state { text-align: center; padding: 40px; color: #666; }
        .empty-state i { font-size: 48px; margin-bottom: 16px; color: #ccc; }
        .filters{margin-bottom:20px;display:flex;gap:10px;align-items:center;flex-wrap: wrap;}
        .filter-select{padding:8px 12px;border:1px solid #d1d5db;border-radius:6px;min-width: 150px;}
        .stats-cards{display:flex;gap:15px;margin-bottom:20px;flex-wrap: wrap;}
        .stat-card{flex:1;min-width: 200px;padding:15px;background:white;border-radius:8px;border:1px solid #e5e7eb;text-align:center;}
        .stat-card .number{font-size:24px;font-weight:bold;color:#2563eb;}
        .stat-card .label{font-size:14px;color:#6b7280;}
        .text-muted{color:#6b7280!important;}
        .table-small{font-size: 0.875rem;}
        .table-small td{padding: 8px 12px;}
        
        /* Debug style */
        .debug-info {
            background: #f3f4f6;
            padding: 10px;
            border-radius: 6px;
            margin-bottom: 10px;
            font-size: 12px;
            color: #6b7280;
        }
        .debug-success { background: #ecfdf5; color: #059669; }
        .debug-warning { background: #fff7ed; color: #d97706; }
        .debug-error { background: #fef2f2; color: #b91c1c; }
    </style>
</head>
<body class="admin">
    <aside class="sidebar">
        <div class="brand">RideNow Admin</div>
        <nav>
            <a href="${pageContext.request.contextPath}/admin/dashboard">Dashboard</a>
            <a href="${pageContext.request.contextPath}/adminpaymentverify">Xác Minh Thanh Toán</a>
            <a href="${pageContext.request.contextPath}/adminpickup">Giao Nhận Xe</a>
            <a href="${pageContext.request.contextPath}/adminreturn">Trả Xe</a>
            <a href="${pageContext.request.contextPath}/adminreturns" class="active">Hoàn Cọc</a>
            <a href="${pageContext.request.contextPath}/adminwithdrawals">Rút Tiền</a>
            <a href="${pageContext.request.contextPath}/logout">Logout</a>
        </nav>
    </aside>

    <main class="content">
        <h1>Hoàn Tiền Cọc</h1>

        <!-- Debug info -->
        <div class="debug-info ${empty rows ? 'debug-warning' : 'debug-success'}">
            <strong>DEBUG INFO:</strong><br>
            - totalRecords: ${totalRecords}<br>
            - rows size: ${rows != null ? rows.size() : 0}<br>
            - totalPending: ${totalPending != null ? totalPending : 0}<br>
            - totalProcessing: ${totalProcessing != null ? totalProcessing : 0}<br>
            - totalPendingAmount: <fmt:formatNumber value="${totalPendingAmount != null ? totalPendingAmount : 0}" type="currency"/><br>
            - Filter: ${param.status != null ? param.status : 'all'}
        </div>

        <c:if test="${not empty sessionScope.flash}">
            <div class="notice ${sessionScope.flash.contains('✅') ? 'notice-success' : 'notice-error'}">
                ${sessionScope.flash}
            </div>
            <c:remove var="flash" scope="session"/>
        </c:if>

        <!-- Thống kê nhanh -->
        <div class="stats-cards">
            <div class="stat-card">
                <div class="number">${totalPending != null ? totalPending : 0}</div>
                <div class="label">Đang Chờ Xử Lý</div>
            </div>
            <div class="stat-card">
                <div class="number">${totalProcessing != null ? totalProcessing : 0}</div>
                <div class="label">Đang Xử Lý</div>
            </div>
            <div class="stat-card">
                <div class="number">
                    <fmt:formatNumber value="${totalPendingAmount != null ? totalPendingAmount : 0}" type="currency"/>
                </div>
                <div class="label">Tổng Tiền Đang Chờ</div>
            </div>
        </div>

        <!-- Bộ lọc -->
        <div class="filters">
            <label><strong>Lọc theo trạng thái:</strong></label>
            <select class="filter-select" id="statusFilter">
                <option value="all">Tất Cả Trạng Thái</option>
                <option value="pending">Đang Chờ</option>
                <option value="processing">Đang Xử Lý</option>
                <option value="completed">Đã Hoàn Thành</option>
                <option value="refunded">Đã Hoàn Tiền</option>
                <option value="held">Đang Giữ Cọc</option>
                <option value="cancelled">Đã Hủy</option>
            </select>
            <button class="btn btn-sm" onclick="clearFilter()">
                <i class="fas fa-times"></i> Xóa Bộ Lọc
            </button>
        </div>

        <div class="panel">
            <div class="panel-head">
                <h2>Yêu Cầu Hoàn Tiền Cọc</h2>
                <span class="text-muted">Tổng: ${totalRecords != null ? totalRecords : 0} yêu cầu</span>
            </div>

            <c:choose>
                <c:when test="${empty rows}">
                    <div class="empty-state">
                        <i class="fas fa-clipboard-check"></i>
                        <h3>Không có yêu cầu hoàn tiền nào</h3>
                        <p>Tất cả các yêu cầu đã được xử lý</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <table class="table table-small" id="withdrawalsTable">
                        <thead>
                            <tr>
                                <th>Mã Đơn</th>
                                <th>Mã Kiểm Tra</th>
                                <th>Khách Hàng</th>
                                <th>Số Tiền</th>
                                <th>Phương Thức</th>
                                <th>Thời Gian</th>
                                <th>Trạng Thái</th>
                                <th>Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="w" items="${rows}" varStatus="loop">
                                <tr data-status="${w.status}" data-order-id="${w.orderId}" data-withdrawal-id="${w.withdrawalId}">
                                    <td>
                                        <strong>#${w.orderId}</strong>
                                        <br><small class="text-muted">WID: ${w.withdrawalId}</small>
                                    </td>
                                    <td>
                                        <c:if test="${not empty w.inspectionId}">
                                            #${w.inspectionId}
                                        </c:if>
                                        <c:if test="${empty w.inspectionId}">
                                            <span class="text-muted">Chưa KT</span>
                                        </c:if>
                                    </td>
                                    <td>
                                        <strong>${w.customerName}</strong><br>
                                        <small class="text-muted">${w.customerPhone}</small>
                                    </td>
                                    <td>
                                        <strong>
                                            <fmt:formatNumber value="${w.refundAmount != null ? w.refundAmount : w.amount}" type="currency"/>
                                        </strong>
                                        <c:if test="${w.refundAmount != null && w.refundAmount.compareTo(w.amount) != 0}">
                                            <br><small class="text-muted">Gốc: <fmt:formatNumber value="${w.amount}" type="currency"/></small>
                                        </c:if>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${w.refundMethod == 'cash'}">
                                                <span class="badge" style="background:#f3f4f6;color:#374151;">
                                                    <i class="fas fa-money-bill-wave"></i> Tiền mặt
                                                </span>
                                            </c:when>
                                            <c:when test="${w.refundMethod == 'bank_transfer'}">
                                                <span class="badge" style="background:#eff6ff;color:#2563eb;">
                                                    <i class="fas fa-university"></i> Chuyển khoản
                                                </span>
                                            </c:when>
                                            <c:when test="${not empty w.refundMethod}">
                                                <span class="badge">${w.refundMethod}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted">Chưa xác định</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <fmt:formatDate value="${w.requestDate}" pattern="dd/MM/yyyy"/><br>
                                        <small class="text-muted">
                                            <fmt:formatDate value="${w.requestDate}" pattern="HH:mm"/>
                                        </small>
                                        <c:if test="${not empty w.returnedAt}">
                                            <br><small class="text-muted">
                                                Trả: <fmt:formatDate value="${w.returnedAt}" pattern="dd/MM HH:mm"/>
                                            </small>
                                        </c:if>
                                    </td>
                                    <td>
                                        <span class="badge ${w.status}">
                                            <c:choose>
                                                <c:when test="${w.status == 'pending'}">
                                                    <i class="fas fa-clock"></i> Đang chờ
                                                </c:when>
                                                <c:when test="${w.status == 'processing'}">
                                                    <i class="fas fa-sync-alt"></i> Đang xử lý
                                                </c:when>
                                                <c:when test="${w.status == 'completed'}">
                                                    <i class="fas fa-check"></i> Đã hoàn thành
                                                </c:when>
                                                <c:when test="${w.status == 'refunded'}">
                                                    <i class="fas fa-check-circle"></i> Đã hoàn tiền
                                                </c:when>
                                                <c:when test="${w.status == 'held'}">
                                                    <i class="fas fa-lock"></i> Đang giữ cọc
                                                </c:when>
                                                <c:when test="${w.status == 'cancelled'}">
                                                    <i class="fas fa-times"></i> Đã hủy
                                                </c:when>
                                                <c:otherwise>${w.status}</c:otherwise>
                                            </c:choose>
                                        </span>
                                        <c:if test="${not empty w.bikeCondition}">
                                            <br><small class="text-muted">T.trạng: ${w.bikeCondition}</small>
                                        </c:if>
                                    </td>
                                    <td class="actions">
                                        <!-- Trạng thái HELD: Xe chưa trả -->
                                        <c:if test="${w.status == 'held'}">
                                            <form method="post" action="${ctx}/adminwithdrawals"
                                                  onsubmit="return confirmAction('process_refund', ${w.orderId}, ${w.withdrawalId})">
                                                <input type="hidden" name="orderId" value="${w.orderId}">
                                                <input type="hidden" name="withdrawalId" value="${w.withdrawalId}">
                                                <input type="hidden" name="action" value="process_refund">
                                                <button class="btn btn-sm btn-primary" type="submit" title="Xác nhận xe đã trả">
                                                    <i class="fas fa-car"></i> Xe đã trả
                                                </button>
                                            </form>
                                        </c:if>

                                        <!-- Trạng thái PENDING: Bắt đầu xử lý -->
                                        <c:if test="${w.status == 'pending'}">
                                            <form method="post" action="${ctx}/adminwithdrawals"
                                                  onsubmit="return confirmAction('mark_processing', ${w.orderId}, ${w.withdrawalId})">
                                                <input type="hidden" name="orderId" value="${w.orderId}">
                                                <input type="hidden" name="withdrawalId" value="${w.withdrawalId}">
                                                <input type="hidden" name="action" value="mark_processing">
                                                <button class="btn btn-sm" type="submit" title="Bắt đầu xử lý hoàn tiền">
                                                    <i class="fas fa-play"></i> Bắt đầu
                                                </button>
                                            </form>
                                        </c:if>

                                        <!-- Trạng thái PENDING hoặc PROCESSING: Hoàn tất -->
                                        <c:if test="${w.status == 'pending' || w.status == 'processing'}">
                                            <form method="post" action="${ctx}/adminwithdrawals"
                                                  onsubmit="return confirmAction('complete_refund', ${w.orderId}, ${w.withdrawalId})">
                                                <input type="hidden" name="orderId" value="${w.orderId}">
                                                <input type="hidden" name="withdrawalId" value="${w.withdrawalId}">
                                                <input type="hidden" name="action" value="complete_refund">
                                                <button class="btn btn-sm btn-primary" type="submit" title="Xác nhận đã hoàn tiền">
                                                    <i class="fas fa-check"></i> Hoàn tất
                                                </button>
                                            </form>
                                        </c:if>

                                        <!-- Hủy cho các trạng thái chưa hoàn thành -->
                                        <c:if test="${w.status != 'completed' && w.status != 'refunded' && w.status != 'cancelled'}">
                                            <form method="post" action="${ctx}/adminwithdrawals"
                                                  onsubmit="return confirmAction('cancel', ${w.orderId}, ${w.withdrawalId})">
                                                <input type="hidden" name="orderId" value="${w.orderId}">
                                                <input type="hidden" name="withdrawalId" value="${w.withdrawalId}">
                                                <input type="hidden" name="action" value="cancel">
                                                <button class="btn btn-sm btn-danger" type="submit" title="Hủy yêu cầu">
                                                    <i class="fas fa-times"></i> Hủy
                                                </button>
                                            </form>
                                        </c:if>

                                        <!-- Đã hoàn thành -->
                                        <c:if test="${w.status == 'completed' || w.status == 'refunded' || w.status == 'cancelled'}">
                                            <span class="text-muted">Đã hoàn tất</span>
                                            <c:if test="${w.status == 'cancelled'}">
                                                <br><small class="text-muted">(Đã hủy)</small>
                                            </c:if>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </div>
    </main>

    <script src="https://kit.fontawesome.com/a076d05399.js" crossorigin="anonymous"></script>
    <script>
        // Debug info
        console.log('🔄 JSP Loaded - Debug Info:');
        console.log('- totalRecords:', ${totalRecords});
        console.log('- rows size:', ${rows != null ? rows.size() : 0});
        console.log('- filter:', '${param.status != null ? param.status : 'all'}');

        document.addEventListener('DOMContentLoaded', function() {
            console.log('🔄 Khởi tạo bộ lọc...');
            initFilter();
        });

        function initFilter() {
            const filterSelect = document.getElementById('statusFilter');
            if (!filterSelect) {
                console.error('❌ Không tìm thấy bộ lọc');
                return;
            }

            const urlParams = new URLSearchParams(window.location.search);
            const filterStatus = urlParams.get('status') || 'all';
            filterSelect.value = filterStatus;
            
            applyFilter(filterStatus);

            filterSelect.addEventListener('change', function() {
                console.log('🎯 Filter changed to:', this.value);
                applyFilter(this.value);
            });

            console.log('✅ Filter initialized with:', filterStatus);
        }

        function applyFilter(status) {
            console.log('🔍 Áp dụng filter:', status);
            
            const rows = document.querySelectorAll('#withdrawalsTable tbody tr');
            let visibleCount = 0;

            rows.forEach((row, index) => {
                const rowStatus = row.getAttribute('data-status');
                const orderId = row.getAttribute('data-order-id');
                
                if (status === 'all' || rowStatus === status) {
                    row.style.display = '';
                    visibleCount++;
                    console.log(`✅ Showing row ${index} - Order#${orderId} (${rowStatus})`);
                } else {
                    row.style.display = 'none';
                    console.log(`❌ Hiding row ${index} - Order#${orderId} (${rowStatus})`);
                }
            });

            updateURL(status);
            showEmptyMessage(visibleCount === 0, status);
            
            console.log(`📊 Filter result: ${visibleCount}/${rows.length} visible rows`);
        }

        function showEmptyMessage(isEmpty, status) {
            const oldMessage = document.getElementById('filterEmptyMessage');
            if (oldMessage) oldMessage.remove();

            if (isEmpty) {
                const statusText = getStatusText(status);
                const message = document.createElement('div');
                message.id = 'filterEmptyMessage';
                message.className = 'empty-state';
                message.innerHTML = `
                    <i class="fas fa-search"></i>
                    <h3>Không tìm thấy yêu cầu nào</h3>
                    <p>Không có yêu cầu hoàn tiền nào ở trạng thái "${statusText}"</p>
                    <button class="btn btn-sm" onclick="clearFilter()">Hiển thị tất cả</button>
                `;
                
                const table = document.getElementById('withdrawalsTable');
                if (table) {
                    table.parentNode.insertBefore(message, table);
                }
                console.log('📝 Showed empty message for status:', status);
            }
        }

        function getStatusText(status) {
            const statusMap = {
                'all': 'Tất cả',
                'pending': 'Đang chờ',
                'processing': 'Đang xử lý',
                'completed': 'Đã hoàn thành',
                'refunded': 'Đã hoàn tiền',
                'held': 'Đang giữ cọc',
                'cancelled': 'Đã hủy'
            };
            return statusMap[status] || status;
        }

        function clearFilter() {
            console.log('🔄 Clearing filter');
            const filterSelect = document.getElementById('statusFilter');
            if (filterSelect) {
                filterSelect.value = 'all';
                applyFilter('all');
            }
        }

        function updateURL(status) {
            const url = new URL(window.location);
            if (status && status !== 'all') {
                url.searchParams.set('status', status);
            } else {
                url.searchParams.delete('status');
            }
            window.history.replaceState({}, '', url);
            console.log('🔗 Updated URL:', url.toString());
        }

        function confirmAction(action, orderId, withdrawalId) {
            const actionTexts = {
                'process_refund': 'Xác nhận xe đã được trả và bắt đầu xử lý hoàn tiền',
                'mark_processing': 'Bắt đầu xử lý hoàn tiền',
                'complete_refund': 'Xác nhận đã hoàn tiền thành công',
                'cancel': 'Hủy yêu cầu hoàn tiền'
            };
            
            const message = `${actionTexts[action]} cho đơn hàng #${orderId}?`;
            console.log(`🔄 Confirming action: ${action} for Order#${orderId}, WID: ${withdrawalId}`);
            return confirm(message);
        }

        // Debug function to check form data
        window.debugForms = function() {
            console.log('🔍 Debugging forms...');
            const forms = document.querySelectorAll('form');
            forms.forEach((form, index) => {
                const inputs = form.querySelectorAll('input[type="hidden"]');
                console.log(`Form ${index}:`);
                inputs.forEach(input => {
                    console.log(`  - ${input.name}: ${input.value}`);
                });
            });
        };
    </script>
</body>
</html>