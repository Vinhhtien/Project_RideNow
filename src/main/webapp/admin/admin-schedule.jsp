<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Lịch Thuê Xe - RideNow Admin</title>

  <c:set var="ctx" value="${pageContext.request.contextPath}" />
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <link rel="stylesheet" href="${ctx}/css/admin.css">

  <style>
    :root {
      --primary: #3b82f6;
      --primary-dark: #2563eb;
      --primary-light: #60a5fa;
      --secondary: #64748b;
      --success: #10b981;
      --warning: #f59e0b;
      --info: #8b5cf6;
      --dark: #0f172a;
      --dark-light: #1e293b;
      --darker: #0b1222;
      --light: #f8fafc;
      --gray: #94a3b8;
      --border: #334155;
      --shadow: rgba(0, 0, 0, 0.1);
    }

    body.admin { 
      display:flex; 
      min-height:100vh; 
      background: var(--dark); 
      color: var(--light); 
      font-family: 'Inter', sans-serif; 
      line-height: 1.6;
    }
    
    main.content { 
      flex:1; 
      padding: 1.5rem; 
      overflow: auto; 
      background: var(--dark-light);
      border-radius: 12px;
      margin: 1rem;
      box-shadow: 0 4px 12px var(--shadow);
    }

    .page-header { 
      display:flex; 
      justify-content:space-between; 
      align-items:center; 
      margin-bottom: 2rem;
      padding-bottom: 1rem;
      border-bottom: 1px solid var(--border);
    }
    
    .page-title {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      font-size: 1.75rem;
      font-weight: 700;
      color: var(--light);
    }
    
    .page-title i {
      color: var(--primary);
      background: rgba(59, 130, 246, 0.1);
      padding: 0.75rem;
      border-radius: 10px;
    }
    
    .btn-outline {
      background: transparent; 
      border: 1px solid var(--primary); 
      color: var(--primary);
      padding: 0.75rem 1.25rem; 
      border-radius: 8px; 
      text-decoration: none;
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-weight: 500;
      transition: all 0.3s ease;
      white-space: nowrap;
    }
    
    .btn-outline:hover { 
      background: var(--primary); 
      color: var(--light);
      transform: translateY(-2px);
      box-shadow: 0 4px 8px rgba(59, 130, 246, 0.3);
    }

    .filters-container {
      background: var(--darker);
      border-radius: 10px;
      padding: 1.5rem;
      margin-bottom: 2rem;
      box-shadow: 0 2px 8px var(--shadow);
    }
    
    .filters { 
      display: flex; 
      align-items: flex-end; 
      gap: 1rem; 
      flex-wrap: wrap; 
    }
    
    .filter-group {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      flex: 1;
      min-width: 150px;
    }
    
    .filter-group label {
      font-size: 0.875rem;
      font-weight: 500;
      color: var(--gray);
    }
    
    .filters input, .filters select {
      background: var(--dark); 
      color: var(--light); 
      border: 1px solid var(--border); 
      border-radius: 8px; 
      padding: 0.75rem;
      font-family: 'Inter', sans-serif;
      transition: all 0.2s;
      width: 100%;
    }
    
    .filters input:focus, .filters select:focus {
      outline: none;
      border-color: var(--primary);
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
    }
    
    .filters button {
      background: var(--primary); 
      border: none; 
      color: white; 
      padding: 0.75rem 1.5rem; 
      border-radius: 8px; 
      cursor: pointer;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 0.5rem;
      transition: all 0.3s ease;
      height: fit-content;
      white-space: nowrap;
    }
    
    .filters button:hover { 
      background: var(--primary-dark);
      transform: translateY(-2px);
      box-shadow: 0 4px 8px rgba(59, 130, 246, 0.3);
    }

    /* QUAN TRỌNG: Container cho calendar với scroll ngang */
    .calendar-scroll-container {
      overflow-x: auto;
      border: 1px solid var(--border);
      border-radius: 10px;
      background: var(--darker);
      margin-bottom: 2rem;
    }

    .calendar-container {
      min-width: fit-content;
      width: 100%;
    }
    
    .calendar-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1.5rem;
      background: var(--dark);
      border-bottom: 1px solid var(--border);
      position: sticky;
      left: 0;
    }
    
    .calendar-header h2 {
      font-size: 1.25rem;
      font-weight: 600;
      white-space: nowrap;
    }
    
    .legend { 
      display: flex; 
      gap: 1.5rem; 
      align-items: center; 
      font-size: 0.875rem; 
      color: var(--gray); 
      flex-wrap: wrap;
    }
    
    .legend-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      white-space: nowrap;
    }
    
    .legend .dot { 
      width: 12px; 
      height: 12px; 
      border-radius: 50%; 
      display: inline-block; 
    }
    
    .dot.pending { background: var(--warning); } 
    .dot.confirmed { background: var(--success); } 
    .dot.completed { background: var(--info); }

    /* QUAN TRỌNG: Grid cố định chiều rộng cột */
    .calendar-grid {
      display: grid;
      background: var(--darker);
      overflow: hidden;
      min-width: max-content;
    }
    
    .day-header {
      background: var(--dark); 
      text-align: center; 
      padding: 1rem 0.5rem; 
      font-weight: 600; 
      border-bottom: 1px solid var(--border);
      border-right: 1px solid var(--border);
      min-width: 120px; /* Cố định chiều rộng tối thiểu cho cột ngày */
      position: sticky;
      top: 0;
      z-index: 10;
    }
    
    .day-header:first-child {
      border-left: none;
      position: sticky;
      left: 0;
      z-index: 20;
      background: var(--dark);
    }
    
    .row-label {
      background: var(--dark); 
      border-right: 1px solid var(--border); 
      padding: 1rem 0.75rem; 
      font-weight: 600;
      position: sticky;
      left: 0;
      z-index: 15;
      min-width: 200px; /* Cố định chiều rộng cho cột xe */
      background: var(--dark);
    }
    
    .row-label small { 
      color: var(--gray); 
      display: block; 
      margin-top: 4px; 
      font-weight: 500; 
      font-size: 0.875rem;
    }

    .cell { 
      border-right: 1px solid var(--border); 
      border-bottom: 1px solid var(--border); 
      min-height: 100px; 
      padding: 0.5rem; 
      position: relative; 
      transition: background 0.2s;
      min-width: 120px; /* Cùng chiều rộng với day-header */
    }
    
    .cell:hover {
      background: rgba(255, 255, 255, 0.02);
    }
    
    .bike-card {
      display: block; 
      background: rgba(59, 130, 246, 0.15); 
      border: 1px solid rgba(59, 130, 246, 0.4);
      border-left: 4px solid rgba(59, 130, 246, 0.8);
      border-radius: 8px; 
      margin-bottom: 0.5rem; 
      padding: 0.75rem; 
      font-size: 0.875rem; 
      color: var(--light); 
      cursor: pointer; 
      transition: all 0.3s ease;
      text-decoration: none;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      word-break: break-word;
    }
    
    .bike-card:hover { 
      transform: translateY(-2px); 
      background: rgba(59, 130, 246, 0.25); 
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
    }
    
    .bike-card.confirmed { 
      background: rgba(16, 185, 129, 0.15); 
      border-color: rgba(16, 185, 129, 0.4);
      border-left-color: rgba(16, 185, 129, 0.8);
    }
    
    .bike-card.pending { 
      background: rgba(245, 158, 11, 0.15); 
      border-color: rgba(245, 158, 11, 0.4);
      border-left-color: rgba(245, 158, 11, 0.8);
    }
    
    .bike-card.completed { 
      background: rgba(139, 92, 246, 0.15); 
      border-color: rgba(139, 92, 246, 0.4);
      border-left-color: rgba(139, 92, 246, 0.8);
    }
    
    .bike-card small { 
      display: block; 
      font-size: 0.75rem; 
      color: var(--gray); 
      margin-top: 4px; 
    }

    .no-data { 
      grid-column: 1 / -1; 
      text-align: center; 
      color: var(--gray); 
      padding: 3rem; 
      font-size: 1.125rem;
    }
    
    .status-badge {
      display: inline-block;
      padding: 0.25rem 0.5rem;
      border-radius: 20px;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: uppercase;
      margin-top: 0.25rem;
    }
    
    .status-pending {
      background: rgba(245, 158, 11, 0.2);
      color: var(--warning);
    }
    
    .status-confirmed {
      background: rgba(16, 185, 129, 0.2);
      color: var(--success);
    }
    
    .status-completed {
      background: rgba(139, 92, 246, 0.2);
      color: var(--info);
    }
    
    /* Hiển thị số ngày khi có quá nhiều */
    .days-count-badge {
      background: var(--primary);
      color: white;
      padding: 0.25rem 0.5rem;
      border-radius: 4px;
      font-size: 0.75rem;
      margin-left: 0.5rem;
    }
    
    /* Responsive adjustments */
    @media (max-width: 1200px) {
      .row-label {
        min-width: 180px;
      }
      .day-header, .cell {
        min-width: 100px;
      }
    }
    
    @media (max-width: 992px) {
      .filters {
        flex-direction: column;
        align-items: stretch;
      }
      
      .filter-group {
        width: 100%;
      }
      
      .filters button {
        align-self: stretch;
        justify-content: center;
      }
      
      .page-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
      }
      
      .legend {
        justify-content: flex-start;
      }
    }
    
    @media (max-width: 768px) {
      main.content {
        padding: 1rem;
        margin: 0.5rem;
      }
      
      .row-label {
        min-width: 150px;
        padding: 0.75rem 0.5rem;
      }
      
      .day-header, .cell {
        min-width: 90px;
        padding: 0.5rem 0.25rem;
      }
      
      .bike-card {
        padding: 0.5rem;
        font-size: 0.8rem;
      }
    }
  </style>
</head>

<body class="admin">
  <!-- Sidebar giống dashboard -->
  <aside class="sidebar">
    <div class="brand">
      <div class="brand-logo"><i class="fas fa-motorcycle"></i></div>
      <h1>RideNow Admin</h1>
    </div>
    <nav class="sidebar-nav">
      <a href="${ctx}/admin/dashboard" class="nav-item"><i class="fas fa-tachometer-alt"></i><span>Dashboard</span></a>
      <a href="${ctx}/admin/partners" class="nav-item"><i class="fas fa-handshake"></i><span>Partners</span></a>
      <a href="${ctx}/admin/customers" class="nav-item"><i class="fas fa-users"></i><span>Customers</span></a>
      <a href="${ctx}/admin/bikes" class="nav-item"><i class="fas fa-motorcycle"></i><span>Motorbikes</span></a>
      <a href="${ctx}/admin/orders" class="nav-item"><i class="fas fa-clipboard-list"></i><span>Orders</span></a>
      <a href="${ctx}/admin/schedule" class="nav-item active"><i class="fas fa-calendar-alt"></i><span>View Schedule</span></a>
      <a href="${ctx}/adminpickup" class="nav-item"><i class="fas fa-shipping-fast"></i><span>Vehicle Pickup</span></a>
      <a href="${ctx}/adminreturn" class="nav-item"><i class="fas fa-undo-alt"></i><span>Vehicle Return</span></a>
      <a href="${ctx}/adminreturns" class="nav-item"><i class="fas fa-clipboard-check"></i><span>Verify & Refund</span></a>
      <a href="${ctx}/logout" class="nav-item logout"><i class="fas fa-sign-out-alt"></i><span>Logout</span></a>
    </nav>
  </aside>

  <!-- Nội dung chính -->
  <main class="content">
    <%
      // CHUYỂN PHẦN XỬ LÝ DỮ LIỆU LÊN ĐẦU
      // Chuẩn bị dữ liệu ngày
      java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ofPattern("dd/MM");
      java.time.LocalDate fromDate = (java.time.LocalDate) request.getAttribute("from");
      java.time.LocalDate toDate   = (java.time.LocalDate) request.getAttribute("to");
      java.util.List<model.ScheduleItem> items = (java.util.List<model.ScheduleItem>) request.getAttribute("items");

      java.util.List<java.time.LocalDate> days = new java.util.ArrayList<>();
      if (fromDate != null && toDate != null) {
        java.time.LocalDate d = fromDate;
        while (!d.isAfter(toDate)) { days.add(d); d = d.plusDays(1); }
      }

      // Lấy danh sách xe (theo items)
      java.util.Set<String> bikeSet = new java.util.LinkedHashSet<>();
      if (items != null) {
        for (model.ScheduleItem si : items) bikeSet.add(si.getBikeName()+"#"+si.getLicensePlate());
      }
      java.util.List<String> bikeList = new java.util.ArrayList<>(bikeSet);
      
      // Tính số ngày để hiển thị warning
      int daysCount = days != null ? days.size() : 0;
      boolean showDaysWarning = daysCount > 14;
      boolean showDaysBadge = daysCount > 7;
    %>

    <div class="page-header">
      <h1 class="page-title"><i class="fas fa-calendar-alt"></i> Lịch Thuê Xe 
        <% if(showDaysBadge) { %>
          <span class="days-count-badge"><%= daysCount %> ngày</span>
        <% } %>
      </h1>
      <a href="${ctx}/admin/dashboard" class="btn-outline"><i class="fas fa-arrow-left"></i> Quay lại Dashboard</a>
    </div>

    <!-- Bộ lọc -->
    <div class="filters-container">
      <form class="filters" method="get" action="${ctx}/admin/schedule">
        <div class="filter-group">
          <label for="view">Chế độ xem</label>
          <select name="view" id="view">
            <option value="week" ${view=='week' ? 'selected' : ''}>Tuần này (7 ngày)</option>
            <option value="month" ${view=='month' ? 'selected' : ''}>Tháng này (~30 ngày)</option>
            <option value="custom" ${view=='custom' ? 'selected' : ''}>Tùy chỉnh</option>
          </select>
        </div>
        
        <div class="filter-group">
          <label for="from">Từ ngày</label>
          <input type="date" name="from" id="from" value="${from}">
        </div>
        
        <div class="filter-group">
          <label for="to">Đến ngày</label>
          <input type="date" name="to" id="to" value="${to}">
        </div>
        
        <button type="submit"><i class="fas fa-filter"></i> Áp dụng bộ lọc</button>
        
        <% if(showDaysWarning) { %>
          <div class="filter-group">
            <div style="color: var(--warning); font-size: 0.875rem;">
              <i class="fas fa-exclamation-triangle"></i> Đang hiển thị <%= daysCount %> ngày
            </div>
          </div>
        <% } %>
      </form>
    </div>

    <!-- QUAN TRỌNG: Scroll container cho calendar -->
    <div class="calendar-scroll-container">
      <div class="calendar-container">
        <div class="calendar-header">
          <h2>Lịch trình thuê xe 
            <% if(daysCount > 0) { %>
              <span style="font-size: 1rem; color: var(--gray); margin-left: 0.5rem;">
                (<%= daysCount %> ngày)
              </span>
            <% } %>
          </h2>
          <div class="legend">
            <div class="legend-item">
              <span class="dot pending"></span> Đang chờ
            </div>
            <div class="legend-item">
              <span class="dot confirmed"></span> Đã xác nhận
            </div>
            <div class="legend-item">
              <span class="dot completed"></span> Hoàn thành
            </div>
          </div>
        </div>
        
        <!-- QUAN TRỌNG: Grid với số cột động -->
        <div class="calendar-grid" 
          style="grid-template-columns: 200px repeat(<%= daysCount > 0 ? daysCount : 7 %>, 1fr);">
          
          <!-- Header cột -->
          <div class="day-header" style="border-right:1px solid var(--border);">Thông tin xe</div>
          <% 
            if (days != null && daysCount > 0) {
              for (java.time.LocalDate d : days) { 
          %>
            <div class="day-header"><%= d.format(df) %></div>
          <%   } 
            } %>
          
          <!-- Mỗi hàng = 1 xe -->
          <%
            if (bikeList != null && days != null && daysCount > 0) {
              for (String bike : bikeList) {
                String[] parts = bike.split("#");
                String bikeName = parts[0];
                String plate = parts.length>1 ? parts[1] : "";
          %>
            <div class="row-label">
              <%=bikeName%>
              <small><%=plate%></small>
            </div>

            <% for (java.time.LocalDate d : days) {
                 boolean found = false;
                 if (items != null) {
                   for (model.ScheduleItem si : items) {
                     boolean sameBike = (bikeName+"#"+plate).equals(si.getBikeName()+"#"+si.getLicensePlate());
                     if (sameBike && (!d.isBefore(si.getStartDate()) && !d.isAfter(si.getEndDate()))) {
                       found = true;
                       String cls = si.getOrderStatus(); // pending|confirmed|completed
                       String statusText = "";
                       switch(cls) {
                         case "pending": statusText = "Đang chờ"; break;
                         case "confirmed": statusText = "Đã xác nhận"; break;
                         case "completed": statusText = "Hoàn thành"; break;
                         default: statusText = cls;
                       }
            %>
              <div class="cell">
                <a class="bike-card <%=cls%>" href="${ctx}/admin/orders/detail?id=<%=si.getOrderId()%>">
                  <%= si.getBikeName() %>
                  <small>Mã đơn: #<%= si.getOrderId() %></small>
                  <small>Loại: <%= si.getOwnerType() %></small>
                  <span class="status-badge status-<%=cls%>"><%= statusText %></span>
                </a>
              </div>
          <%
                       break;
                     }
                   }
                 }
                 if (!found) { %><div class="cell"></div><% }
               } // for day
            } // for bike
            } // if bikeList và days không null
          %>

          <c:if test="${empty items}">
            <div class="no-data">
              <i class="fas fa-calendar-times" style="font-size: 3rem; margin-bottom: 1rem; opacity: 0.5;"></i>
              <p>Không có dữ liệu đặt xe trong khoảng thời gian này</p>
            </div>
          </c:if>
        </div>
      </div>
    </div>
  </main>
</body>
</html>