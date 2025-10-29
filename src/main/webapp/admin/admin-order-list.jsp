<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt"  prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Orders | Admin</title>
  <link rel="stylesheet" href="${ctx}/css/admin.css"/>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <style>
    /* Enhanced Order List Styles */
    .filters {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1rem;
      margin-bottom: 1.5rem;
      align-items: end;
    }
    
    .filters input, .filters select {
      padding: 0.75rem;
      border: 1px solid var(--border-color);
      border-radius: 8px;
      background: var(--card-bg);
      color: inherit;
      font-size: 0.9rem;
      transition: all 0.3s ease;
    }
    
    .filters input:focus, .filters select:focus {
      outline: none;
      border-color: var(--primary-color);
      box-shadow: 0 0 0 3px rgba(25, 118, 210, 0.1);
    }
    
    .filter-buttons {
      display: flex;
      gap: 0.75rem;
    }
    
    .btn {
      padding: 0.75rem 1.5rem;
      border-radius: 8px;
      text-decoration: none;
      font-weight: 500;
      font-size: 0.9rem;
      transition: all 0.3s ease;
      border: none;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
    }
    
    .btn-primary {
      background: var(--primary-color);
      color: white;
    }
    
    .btn-primary:hover {
      background: #1565c0;
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(25, 118, 210, 0.3);
    }
    
    .btn-outline {
      background: transparent;
      border: 1px solid var(--border-color);
      color: var(--text-color);
    }
    
    .btn-outline:hover {
      background: var(--hover-color);
      border-color: var(--primary-color);
      color: var(--primary-color);
    }
    
    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1.5rem;
      border-bottom: 1px solid var(--border-color);
    }
    
    .panel-meta {
      color: var(--text-light);
      font-size: 0.9rem;
    }
    
    .panel-body {
      padding: 1.5rem;
    }
    
    /* Enhanced Data Table */
    .data-table {
      width: 100%;
      border-collapse: collapse;
      background: var(--card-bg);
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1);
    }
    
    .data-table th {
      background: var(--header-bg);
      padding: 1rem;
      text-align: left;
      font-weight: 600;
      font-size: 0.85rem;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      color: var(--text-light);
      border-bottom: 1px solid var(--border-color);
    }
    
    .data-table td {
      padding: 1rem;
      border-bottom: 1px solid var(--border-color);
      font-size: 0.9rem;
    }
    
    .data-table tbody tr {
      transition: all 0.3s ease;
    }
    
    .data-table tbody tr:hover {
      background: var(--hover-color);
    }
    
    /* Enhanced Status Badges */
    .status-badge {
      padding: 0.4rem 0.8rem;
      border-radius: 999px;
      text-transform: capitalize;
      font-size: 0.8rem;
      font-weight: 500;
      display: inline-flex;
      align-items: center;
      gap: 0.3rem;
    }
    
    .status-badge::before {
      content: "";
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: currentColor;
    }
    
    .pending { background: #fff3cd; color: #8a6d3b; }
    .confirmed { background: #e0f7fa; color: #006064; }
    .cancelled { background: #fdecea; color: #b71c1c; }
    .completed { background: #e8f5e9; color: #1b5e20; }
    
    /* Enhanced Pagination */
    .pagination {
      display: flex;
      gap: 0.5rem;
      justify-content: center;
      align-items: center;
      margin-top: 2rem;
      flex-wrap: wrap;
    }
    
    .pagination a, .pagination span {
      padding: 0.6rem 0.9rem;
      border: 1px solid var(--border-color);
      border-radius: 6px;
      text-decoration: none;
      color: var(--text-color);
      font-size: 0.9rem;
      transition: all 0.3s ease;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-width: 2.5rem;
    }
    
    .pagination a:hover {
      background: var(--primary-color);
      color: white;
      border-color: var(--primary-color);
    }
    
    .pagination span {
      background: var(--primary-color);
      color: white;
      border-color: var(--primary-color);
    }
    
    /* Action Links */
    .action-link {
      color: var(--primary-color);
      text-decoration: none;
      font-weight: 500;
      font-size: 0.85rem;
      display: inline-flex;
      align-items: center;
      gap: 0.3rem;
      transition: all 0.3s ease;
    }
    
    .action-link:hover {
      color: #1565c0;
      gap: 0.5rem;
    }
    
    .action-link i {
      font-size: 0.7rem;
    }
    
    /* Empty State */
    .empty-state {
      text-align: center;
      padding: 3rem 1rem;
      color: var(--text-light);
    }
    
    .empty-state i {
      font-size: 3rem;
      margin-bottom: 1rem;
      color: var(--border-color);
    }
    
    /* Responsive Design */
    @media (max-width: 768px) {
      .filters {
        grid-template-columns: 1fr;
      }
      
      .filter-buttons {
        grid-column: 1;
      }
      
      .panel-header {
        flex-direction: column;
        gap: 1rem;
        align-items: flex-start;
      }
      
      .data-table {
        font-size: 0.8rem;
      }
      
      .data-table th,
      .data-table td {
        padding: 0.75rem 0.5rem;
      }
    }
    
    @media (max-width: 480px) {
      .data-table {
        display: block;
        overflow-x: auto;
        white-space: nowrap;
      }
    }
  </style>
</head>
<body class="admin">
<main class="content">
  <header class="content-header">
    <div class="header-left">
      <h1>Orders</h1>
      <div class="breadcrumb">
        <a href="${ctx}/admin/dashboard">Dashboard</a>
        <i class="fas fa-chevron-right"></i>
        <span class="active">Orders</span>
      </div>
    </div>
    <div class="header-right">
      <a class="icon-btn" href="${ctx}/admin/notify" title="Send notification">
        <i class="fas fa-paper-plane"></i>
      </a>
      <div class="user-profile">
        <div class="user-avatar"><i class="fas fa-user-circle"></i></div>
        <span>Administrator</span>
      </div>
    </div>
  </header>

  <section class="panel">
    <div class="panel-header">
      <h2><i class="fas fa-filter"></i> Filter Orders</h2>
    </div>
    <div class="panel-body">
      <form class="filters" method="get" action="${ctx}/admin/orders">
        <div class="filter-group">
          <label for="search" style="display: block; margin-bottom: 0.5rem; font-weight: 500; font-size: 0.9rem;">Search Customer</label>
          <input type="text" id="search" name="q" value="${fn:escapeXml(q)}" placeholder="Enter customer name..."/>
        </div>
        
        <div class="filter-group">
          <label for="status" style="display: block; margin-bottom: 0.5rem; font-weight: 500; font-size: 0.9rem;">Order Status</label>
          <select id="status" name="status">
            <option value="">All statuses</option>
            <c:forEach var="st" items="${['pending','confirmed','cancelled','completed']}">
              <option value="${st}" <c:if test="${st==status}">selected</c:if>>${fn:toUpperCase(st)}</option>
            </c:forEach>
          </select>
        </div>
        
        <div class="filter-group">
          <label for="from" style="display: block; margin-bottom: 0.5rem; font-weight: 500; font-size: 0.9rem;">From Date</label>
          <input type="date" id="from" name="from" value="${from}"/>
        </div>
        
        <div class="filter-group">
          <label for="to" style="display: block; margin-bottom: 0.5rem; font-weight: 500; font-size: 0.9rem;">To Date</label>
          <input type="date" id="to" name="to" value="${to}"/>
        </div>
        
        <div class="filter-buttons">
          <button class="btn btn-primary" type="submit">
            <i class="fas fa-search"></i> Apply Filters
          </button>
          <a class="btn btn-outline" href="${ctx}/admin/orders">
            <i class="fas fa-refresh"></i> Reset
          </a>
        </div>
      </form>
    </div>
  </section>

  <section class="panel">
    <div class="panel-header">
      <h2><i class="fas fa-list"></i> Order List</h2>
      <div class="panel-meta">
        <i class="fas fa-chart-bar"></i> Total: ${total} orders
      </div>
    </div>
    <div class="panel-body">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Customer</th>
            <th>Status</th>
            <th>Total</th>
            <th>Paid</th>
            <th>Due</th>
            <th>Created</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
        <c:forEach var="o" items="${orders}">
          <tr>
            <td><strong>#${o.orderId}</strong></td>
            <td>${o.customerName}</td>
            <td>
              <span class="status-badge ${o.orderStatus}">
                ${o.orderStatus}
              </span>
            </td>
            <td><strong><fmt:formatNumber value="${o.orderTotal}" type="currency"/></strong></td>
            <td><fmt:formatNumber value="${o.totalPaid}" type="currency"/></td>
            <td>
              <c:choose>
                <c:when test="${o.amountDue > 0}">
                  <span style="color: #d32f2f; font-weight: 500;">
                    <fmt:formatNumber value="${o.amountDue}" type="currency"/>
                  </span>
                </c:when>
                <c:otherwise>
                  <span style="color: #1b5e20; font-weight: 500;">
                    <fmt:formatNumber value="${o.amountDue}" type="currency"/>
                  </span>
                </c:otherwise>
              </c:choose>
            </td>
            <td><fmt:formatDate value="${o.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
            <td>
              <a class="action-link" href="${ctx}/admin/orders/detail?id=${o.orderId}">
                Details <i class="fas fa-external-link-alt"></i>
              </a>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty orders}">
          <tr>
            <td colspan="8">
              <div class="empty-state">
                <i class="fas fa-clipboard-list"></i>
                <h3>No orders found</h3>
                <p>Try adjusting your search filters or create a new order</p>
              </div>
            </td>
          </tr>
        </c:if>
        </tbody>
      </table>

      <c:if test="${totalPages > 1}">
        <div class="pagination">
          <c:forEach var="p" begin="1" end="${totalPages}">
            <c:choose>
              <c:when test="${p==page}">
                <span><strong>${p}</strong></span>
              </c:when>
              <c:otherwise>
                <a href="${ctx}/admin/orders?page=${p}&status=${status}&q=${fn:escapeXml(q)}&from=${from}&to=${to}">
                  ${p}
                </a>
              </c:otherwise>
            </c:choose>
          </c:forEach>
        </div>
      </c:if>
    </div>
  </section>
</main>
</body>
</html>