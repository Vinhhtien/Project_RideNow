<%-- quy + an --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Motorbike" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>Xe của tôi</title>
  <style>
    :root{
      --bg:#0f172a; --card:#1e293b; --text:#f1f5f9; --muted:#94a3b8;
      --primary:#3b82f6; --primary-2:#2563eb; --ring:#93c5fd;
      --shadow:0 12px 36px rgba(2,6,23,.45); --radius:14px;
      --ease:cubic-bezier(.22,.98,.24,.99);
    }

    *{box-sizing:border-box}
    html,body{margin:0}
    body{
      font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Arial, "Apple Color Emoji","Segoe UI Emoji";
      color:var(--text);
      background:
        radial-gradient(1200px 600px at -10% -10%, #213060 0%, transparent 60%),
        radial-gradient(900px 600px at 110% -20%, #361d56 0%, transparent 55%),
        linear-gradient(180deg, #111a36 0%, #0b1229 100%);
      min-height:100vh;
    }

    /* Back link */
    .topbar{display:flex;justify-content:center;margin:16px 0 0}
    .btn,.btn-secondary,.btn-detail{
      appearance:none;border:0;cursor:pointer;text-decoration:none;
      display:inline-flex;align-items:center;gap:8px;
      padding:10px 14px;border-radius:10px;font-weight:700;line-height:1;
      transition:transform .18s var(--ease),filter .18s var(--ease),box-shadow .18s var(--ease);
      outline:none;
    }
    .btn-secondary{background:rgba(255,255,255,.06);color:#e2e8f0;border:1px solid rgba(255,255,255,.1)}
    .btn-secondary:hover{transform:translateY(-1px);filter:brightness(1.05)}
    .btn-secondary:focus-visible{box-shadow:0 0 0 3px rgba(147,197,253,.35)}

    /* ===== Professional page title (no emoji) ===== */
    .page-title{
      display:flex;align-items:center;justify-content:center;gap:10px;
      margin:28px 16px 12px;
      font-size:clamp(18px,2.2vw,24px);font-weight:900;letter-spacing:.3px;
      background:linear-gradient(90deg,#eaf2ff,#b7ceff,#7aa7ff 70%);
      -webkit-background-clip:text;background-clip:text;color:transparent;
      text-shadow:0 2px 22px rgba(124,152,255,.18);
    }

    /* ==== ICON BASE ==== */
    .icon{
      display:inline-flex;align-items:center;justify-content:center;
      width:24px;height:24px;
    }
    .icon svg{
      width:22px;height:22px;
      stroke:currentColor;fill:none;
      stroke-width:1.8;stroke-linecap:round;stroke-linejoin:round;opacity:.95;
    }
    /* Icon ở tiêu đề dùng màu cố định để không bị trong suốt vì gradient text */
    .page-title .icon{ color:#cfe0ff; margin-right:4px }

    /* Grid */
    .container{
      width:min(1200px, 92%);margin:22px auto 40px;
      display:grid;grid-template-columns:repeat(auto-fill, minmax(280px, 1fr));
      gap:22px;
    }

    /* Card */
    .card{
      background:linear-gradient(180deg, rgba(255,255,255,.05), rgba(255,255,255,.02)), var(--card);
      border:1px solid rgba(255,255,255,.07);border-radius:var(--radius);
      overflow:hidden;box-shadow:var(--shadow);
      display:flex;flex-direction:column;
      transition:transform .22s var(--ease),box-shadow .22s var(--ease);
    }
    .card:hover{transform:translateY(-3px);box-shadow:0 16px 42px rgba(2,6,23,.55)}
    .card img{
      width:100%;height:180px;object-fit:cover;display:block;
      border-bottom:1px solid rgba(255,255,255,.08);
      transition:transform .35s var(--ease),filter .35s var(--ease);
    }
    .card:hover img{transform:scale(1.02);filter:saturate(1.06)}
    .card-body{flex:1;display:flex;flex-direction:column;justify-content:space-between;padding:14px;text-align:center}
    .card-body h3{margin:8px 0 12px;font-size:16px;font-weight:800;letter-spacing:.2px;color:#cfe0ff}
    .card-footer{margin-top:6px;text-align:center}

    /* CTA button */
    .btn-detail{
      background:linear-gradient(135deg,var(--primary),var(--primary-2));
      color:#fff;box-shadow:0 8px 20px rgba(37,99,235,.25);
    }
    .btn-detail:hover{transform:translateY(-2px);filter:brightness(1.05)}
    .btn-detail:focus-visible{box-shadow:0 0 0 3px rgba(147,197,253,.45)}
    .btn-detail .icon{ margin-left:8px; transition:transform .18s var(--ease) }
    .btn-detail:hover .icon{ transform:translateX(3px) }
    .btn-detail svg{ width:16px;height:16px;stroke:currentColor;fill:none;stroke-width:2;stroke-linecap:round;stroke-linejoin:round }

    /* Empty */
    .empty-message{text-align:center;margin:42px 16px;font-size:15px;color:var(--muted)}

    @media (prefers-reduced-motion:reduce){
      .card,.card img,.btn,.btn-detail,.btn-detail .icon{transition:none}
    }
  </style>
</head>
<body>

  <!-- Back to dashboard -->
  <div class="topbar">
    <a href="<%= request.getContextPath() %>/partners/dashboard.jsp" class="btn btn-secondary" aria-label="Quay lại trang Dashboard">
      ← Quay lại Dashboard
    </a>
  </div>

  <!-- TIÊU ĐỀ: dùng SVG line-icon bạn cung cấp, không emoji -->
  <h2 class="page-title">
    <span class="icon" aria-hidden="true">
      <!-- SVG bạn gửi -->
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"
           stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="6.5" cy="15.5" r="3.0"></circle>
        <circle cx="17.5" cy="15.5" r="3.0"></circle>
        <path d="M9 15.5h5"></path>
        <path d="M9.2 12l1.6-2.2c.3-.4.7-.8 1.2-.8H14l2.6 2.6"></path>
        <path d="M14.5 9.0h2.2c.8 0 1.3.3 1.8.9l1.2 1.6"></path>
        <path d="M7.4 9.8l1.2 1.2"></path>
      </svg>
    </span>
    <span>Danh sách xe đang đăng bán</span>
  </h2>

  <%
    List<Motorbike> motorbikes = (List<Motorbike>) request.getAttribute("motorbikes");
    if (motorbikes != null && !motorbikes.isEmpty()) {
  %>
  <div class="container">
    <% for (Motorbike bike : motorbikes) { %>
      <div class="card">
        <img
          src="<%= request.getContextPath() %>/images/bike/wave-110.jpg"
          alt="<%= bike.getBikeName() %>"
          onerror="this.onerror=null;this.src='<%= request.getContextPath() %>/images/ridenow_Logo.jpg';"
        />
        <div class="card-body">
          <h3><%= bike.getBikeName() %></h3>
          <div class="card-footer">
            <a href="<%= request.getContextPath() %>/viewmotorbike?id=<%= bike.getBikeId() %>" class="btn-detail">
              <span class="label">Xem chi tiết</span>
              <span class="icon" aria-hidden="true">
                <!-- Arrow line icon cho hành động (rõ nghĩa hơn cho CTA) -->
                <svg viewBox="0 0 24 24">
                  <path d="M4 12h14"></path>
                  <path d="M12 5l7 7-7 7"></path>
                </svg>
              </span>
            </a>
          </div>
        </div>
      </div>
    <% } %>
  </div>
  <% } else { %>
    <p class="empty-message">Bạn chưa đăng bán xe nào.</p>
  <% } %>

</body>
</html>
