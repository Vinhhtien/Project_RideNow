<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt"  prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý Partners - RideNow</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
</head>
<body class="admin">
    <!-- Sidebar Navigation -->
    <aside class="sidebar">
        <div class="brand">
            <div class="brand-logo">
                <i class="fas fa-motorcycle"></i>
            </div>
            <h1>RideNow Admin</h1>
        </div>
        
        <nav class="sidebar-nav">
            <a href="${pageContext.request.contextPath}/admin/dashboard" class="nav-item">
                <i class="fas fa-tachometer-alt"></i>
                <span>Dashboard</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/partners" class="nav-item active">
                <i class="fas fa-handshake"></i>
                <span>Partners</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/customers" class="nav-item">
                <i class="fas fa-users"></i>
                <span>Customers</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/bikes" class="nav-item">
                <i class="fas fa-motorcycle"></i>
                <span>Motorbikes</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/orders" class="nav-item">
                <i class="fas fa-clipboard-list"></i>
                <span>Orders</span>
            </a>
            <a href="${pageContext.request.contextPath}/adminpaymentverify" class="nav-item">
                <i class="fas fa-money-check-alt"></i>
                <span>Verify Payments</span>
            </a>
            <a href="${pageContext.request.contextPath}/adminpickup" class="nav-item">
                <i class="fas fa-shipping-fast"></i>
                <span>Vehicle Pickup</span>
            </a>
            <a href="${pageContext.request.contextPath}/adminreturn" class="nav-item">
                <i class="fas fa-undo-alt"></i>
                <span>Vehicle Return</span>
            </a>
            <a href="${pageContext.request.contextPath}/adminreturns" class="nav-item">
                <i class="fas fa-clipboard-check"></i>
                <span>Verify & Refund</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/reports" class="nav-item">
                <i class="fas fa-chart-bar"></i>
                <span>Reports</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/feedback" class="nav-item">
                <i class="fas fa-comment-alt"></i>
                <span>Feedback</span>
            </a>
            <a href="${pageContext.request.contextPath}/logout" class="nav-item logout">
                <i class="fas fa-sign-out-alt"></i>
                <span>Logout</span>
            </a>
        </nav>
    </aside>

    <!-- Main Content -->
    <main class="content partners-page">
        <header class="content-header">
            <div class="header-left">
                <h1>Quản lý Partners</h1>
                <div class="breadcrumb">
                    <span>Admin</span>
                    <i class="fas fa-chevron-right"></i>
                    <span class="active">Partners</span>
                </div>
            </div>
            <div class="header-right">
                <div class="user-profile">
                    <div class="user-avatar">
                        <i class="fas fa-user-circle"></i>
                    </div>
                    <span>Administrator</span>
                </div>
            </div>
        </header>

        <div class="container">
            <!-- Hiển thị thông báo -->
            <c:if test="${not empty success}">
                <div class="alert alert-success" role="alert" aria-live="assertive">
                    <i class="fas fa-check-circle"></i> ${success}
                </div>
            </c:if>
            <c:if test="${not empty error}">
                <div class="alert alert-error" role="alert" aria-live="assertive">
                    <i class="fas fa-exclamation-circle"></i> ${error}
                </div>
            </c:if>

            <!-- KPI Cards for Partner Management -->
            <section class="kpi-grid">
                <div class="kpi-card">
                    <div class="kpi-icon partners">
                        <i class="fas fa-handshake"></i>
                    </div>
                    <div class="kpi-content">
                        <div class="kpi-value">${not empty partners ? partners.size() : 0}</div>
                        <div class="kpi-label">Tổng số Partners</div>
                    </div>
                </div>
                <div class="kpi-card">
                    <div class="kpi-icon active">
                        <i class="fas fa-user-check"></i>
                    </div>
                    <div class="kpi-content">
                        <div class="kpi-value">${not empty partners ? partners.size() : 0}</div>
                        <div class="kpi-label">Đang hoạt động</div>
                    </div>
                </div>
                <div class="kpi-card">
                    <div class="kpi-icon growth">
                        <i class="fas fa-chart-line"></i>
                    </div>
                    <div class="kpi-content">
                        <div class="kpi-value">+0%</div>
                        <div class="kpi-label">Tăng trưởng</div>
                    </div>
                </div>
            </section>

            <!-- Action Toolbar -->
            <div class="action-toolbar">
                <div class="toolbar-left">
                    <a href="${pageContext.request.contextPath}/adminpartnercreate" class="btn btn-primary">
                        <i class="fas fa-user-plus"></i> Thêm Partner Mới
                    </a>
                    <button type="button" id="exportCsvBtn" class="btn btn-secondary">
                        <i class="fas fa-file-export"></i> Xuất CSV
                    </button>
                </div>
                <div class="toolbar-right">
                    <div class="search-box">
                        <i class="fas fa-search"></i>
                        <input id="partnerSearch" type="text" placeholder="Tìm kiếm partners..." aria-label="Tìm kiếm partners">
                    </div>
                </div>
            </div>

            <!-- Section Danh sách Partners -->
            <section class="card">
                <div class="card-header">
                    <div class="card-title">
                        <i class="fas fa-list"></i>
                        <span>Danh sách Partners</span>
                    </div>
                    <div class="card-actions">
                        <span class="badge badge-primary">Tổng: ${not empty partners ? partners.size() : 0}</span>
                        <div class="view-controls">
                            <span id="visibleCount">0</span>/<span id="totalCount">0</span> hiển thị
                        </div>
                    </div>
                </div>
                
                <div class="card-body">
                    <c:choose>
                        <c:when test="${not empty partners}">
                            <div class="table-container">
                                <table class="data-table" id="partnersTable">
                                    <thead>
                                        <tr>
                                            <th class="sortable" data-sort-key="id">
                                                <span>ID</span>
                                                <i class="sort-indicator fas fa-sort"></i>
                                            </th>
                                            <th class="sortable" data-sort-key="name">
                                                <span>Tên công ty</span>
                                                <i class="sort-indicator fas fa-sort"></i>
                                            </th>
                                            <th class="sortable" data-sort-key="address">
                                                <span>Địa chỉ</span>
                                                <i class="sort-indicator fas fa-sort"></i>
                                            </th>
                                            <th class="sortable" data-sort-key="phone">
                                                <span>Điện thoại</span>
                                                <i class="sort-indicator fas fa-sort"></i>
                                            </th>
                                            <th>Trạng thái</th>
                                            <th class="text-center">Hành động</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach var="partner" items="${partners}">
                                        <tr data-id="${partner.partnerId}">
                                            <td data-label="ID">
                                                <span class="id-badge">#${partner.partnerId}</span>
                                            </td>
                                            <td data-label="Tên công ty">
                                                <div class="partner-info">
                                                    <div class="partner-name">${partner.fullname}</div>
                                                </div>
                                            </td>
                                            <td data-label="Địa chỉ">
                                                <div class="text-truncate" style="max-width: 200px;">${partner.address}</div>
                                            </td>
                                            <td data-label="Điện thoại">
                                                <div class="contact-info">
                                                    <i class="fas fa-phone"></i>
                                                    <span>${partner.phone}</span>
                                                </div>
                                            </td>
                                            <td data-label="Trạng thái">
                                                <span class="status-badge status-active">
                                                    <i class="status-dot"></i>
                                                    Đang hoạt động
                                                </span>
                                            </td>
                                            <td data-label="Hành động" class="actions">
                                                <div class="action-buttons">
                                                    <form method="post" action="${pageContext.request.contextPath}/admin/partners" class="delete-form">
                                                        <input type="hidden" name="partnerId" value="${partner.partnerId}">
                                                        <button type="submit" class="btn btn-danger btn-sm" 
                                                                onclick="return confirm('Bạn có chắc muốn xóa partner &quot;${partner.fullname}&quot;?')">
                                                            <i class="fas fa-trash"></i>
                                                            <span>Xóa</span>
                                                        </button>
                                                    </form>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-state" role="status" aria-live="polite">
                                <div class="empty-icon">
                                    <i class="fas fa-users"></i>
                                </div>
                                <h3>Chưa có partner nào</h3>
                                <p>Hãy tạo partner đầu tiên bằng cách nhấn nút "Thêm Partner Mới"</p>
                                <a href="${pageContext.request.contextPath}/adminpartnercreate" class="btn btn-primary">
                                    <i class="fas fa-user-plus"></i> Thêm Partner Đầu Tiên
                                </a>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </section>
        </div>
    </main>

    <script>
        // Hiển thị thông báo tự động ẩn sau 5 giây
        setTimeout(function() {
            const alerts = document.querySelectorAll('.alert');
            alerts.forEach(alert => {
                alert.style.transition = 'opacity 0.5s ease';
                alert.style.opacity = '0';
                setTimeout(() => alert.remove(), 500);
            });
        }, 5000);

        // Client-side utilities: search, sort, count, CSV export
        (function() {
            const table = document.getElementById('partnersTable');
            const searchInput = document.getElementById('partnerSearch');
            const exportBtn = document.getElementById('exportCsvBtn');
            const visibleCountEl = document.getElementById('visibleCount');
            const totalCountEl = document.getElementById('totalCount');
            
            if (!table) {
                // Handle empty state
                if (totalCountEl) totalCountEl.textContent = '0';
                if (visibleCountEl) visibleCountEl.textContent = '0';
                return;
            }

            const tbody = table.querySelector('tbody');
            const rows = Array.from(tbody.querySelectorAll('tr'));
            const getCellValue = (row, key) => {
                switch (key) {
                    case 'id': return ((row.children[0] && row.children[0].textContent) || '').trim().replace('#', '');
                    case 'name': return ((row.children[1] && row.children[1].textContent) || '').trim().toLowerCase();
                    case 'address': return ((row.children[2] && row.children[2].textContent) || '').trim().toLowerCase();
                    case 'phone': return ((row.children[3] && row.children[3].textContent) || '').trim();
                    default: return '';
                }
            };

            // Initialize counts
            totalCountEl.textContent = rows.length.toString();
            visibleCountEl.textContent = rows.length.toString();

            const updateVisibleCount = () => {
                const visible = rows.filter(r => r.style.display !== 'none').length;
                visibleCountEl.textContent = visible.toString();
            };

            // Search filter
            if (searchInput) {
                searchInput.addEventListener('input', function() {
                    const q = (this.value || '').trim().toLowerCase();
                    rows.forEach(row => {
                        const hay = [getCellValue(row, 'name'), getCellValue(row, 'address'), getCellValue(row, 'phone')].join(' ');
                        row.style.display = hay.indexOf(q) > -1 ? '' : 'none';
                    });
                    updateVisibleCount();
                });
            }

            // Sorting
            let currentSortKey = null;
            let currentSortDir = 'asc';
            const headerCells = table.querySelectorAll('thead th.sortable');
            
            headerCells.forEach(th => {
                th.addEventListener('click', function() {
                    const key = this.getAttribute('data-sort-key');
                    if (!key) return;
                    
                    if (currentSortKey === key) {
                        currentSortDir = currentSortDir === 'asc' ? 'desc' : 'asc';
                    } else {
                        currentSortKey = key;
                        currentSortDir = 'asc';
                    }

                    // Update sort indicators
                    headerCells.forEach(h => {
                        h.classList.remove('sort-asc', 'sort-desc');
                        const icon = h.querySelector('.sort-indicator');
                        if (icon) {
                            icon.className = 'sort-indicator fas fa-sort';
                        }
                    });
                    
                    this.classList.add(currentSortDir === 'asc' ? 'sort-asc' : 'sort-desc');
                    const icon = this.querySelector('.sort-indicator');
                    if (icon) {
                        // Sửa lỗi: thay vì dùng EL, dùng JavaScript thuần
                        const direction = currentSortDir === 'asc' ? 'up' : 'down';
                        icon.className = 'sort-indicator fas fa-sort-' + direction;
                    }

                    const visibleRows = rows.filter(r => r.style.display !== 'none');
                    visibleRows.sort((a, b) => {
                        const av = getCellValue(a, key);
                        const bv = getCellValue(b, key);
                        const na = Number(av);
                        const nb = Number(bv);
                        const bothNumeric = !Number.isNaN(na) && !Number.isNaN(nb);
                        let cmp = 0;
                        if (bothNumeric) {
                            cmp = na - nb;
                        } else {
                            cmp = av.localeCompare(bv);
                        }
                        return currentSortDir === 'asc' ? cmp : -cmp;
                    });

                    // Re-append in sorted order
                    const fragment = document.createDocumentFragment();
                    visibleRows.forEach(r => fragment.appendChild(r));
                    tbody.appendChild(fragment);
                    updateVisibleCount();
                });
            });

            // CSV export
            if (exportBtn) {
                exportBtn.addEventListener('click', function() {
                    const header = ['ID','Tên công ty','Địa chỉ','Điện thoại','Trạng thái'];
                    const visibleRows = rows.filter(r => r.style.display !== 'none');
                    const data = visibleRows.map(r => {
                        const cols = Array.from(r.children).slice(0, 5).map(td => {
                            // Extract clean text content without icons or badges
                            const content = td.cloneNode(true);
                            const icons = content.querySelectorAll('i, .status-dot, .id-badge');
                            icons.forEach(el => el.remove());
                            return (content.textContent || '').trim();
                        });
                        return cols;
                    });
                    const csvRows = [header].concat(data).map(cols => cols.map(v => '"' + v.replace(/"/g, '""') + '"').join(','));
                    const csv = csvRows.join('\n');
                    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
                    const url = URL.createObjectURL(blob);
                    const a = document.createElement('a');
                    a.href = url;
                    const now = new Date();
                    const ts = now.toISOString().slice(0,19).replace(/[:T]/g,'-');
                    a.download = `partners-${ts}.csv`;
                    document.body.appendChild(a);
                    a.click();
                    document.body.removeChild(a);
                    URL.revokeObjectURL(url);
                });
            }
        })();
    </script>
</body>
</html>