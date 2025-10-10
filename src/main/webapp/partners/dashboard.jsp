<%-- an --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    // Không logic phức tạp ở scriptlet — chỉ đảm bảo contextPath đã có
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8" />
  <title>Partner Dashboard</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <style>
    :root{
      --bg:#0b0f1f; --bg2:#0a0d1c;
      --card:#121735; --card2:#10152e;
      --text:#e8eaf6; --muted:#a7b0d6;
      --primary:#6c8cff; --primary2:#9aaeff;
      --danger:#ff6b6b; --success:#34d399;
      --border:#1c244c; --shadow:0 10px 30px rgba(0,0,0,.35);
      --radius:16px;
    }
    *{box-sizing:border-box} html,body{margin:0}
    body{
      font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Arial;
      background:
        radial-gradient(1200px 600px at -10% -10%, #24306b 0%, transparent 60%),
        radial-gradient(900px 600px at 110% -20%, #391d5f 0%, transparent 55%),
        linear-gradient(180deg, var(--bg) 0%, var(--bg2) 100%);
      min-height:100vh; color:var(--text); padding:24px;
    }
    .wrap{max-width:1200px;margin:0 auto}

    /* HEADER */
    .header{display:flex;align-items:center;justify-content:space-between;gap:16px;margin-bottom:18px}
    .left{display:flex;align-items:center;gap:14px}
    .avatar{
      width:46px;height:46px;border-radius:50%;background:#2a3a8a;border:1px solid #3c4aa3;
      display:flex;align-items:center;justify-content:center;font-weight:900;color:#eaf0ff;letter-spacing:.5px
    }
    .title{
      font-size:26px;font-weight:800;letter-spacing:.3px;margin:0;
      background:linear-gradient(90deg,#eaf0ff,#b9c7ff,#6c8cff);
      -webkit-background-clip:text;background-clip:text;color:transparent;
    }
    .sub{color:var(--muted);margin:4px 0 0}
    .hello b{color:#fff}
    .header-actions{display:flex;gap:12px;align-items:center}

    /* Icons */
    .logout-btn{background:transparent;border:1px dashed #3a4477;color:var(--text);padding:10px 14px;border-radius:12px;cursor:pointer}
    .logout-btn:hover{filter:brightness(1.08)}

    /* Bell */
    .notification-bell{
      position:relative;background:#1e40af;color:#fff;border:1px solid #243a9a;
      width:42px;height:42px;border-radius:50%;cursor:pointer;font-size:0;
      display:flex;align-items:center;justify-content:center;transition:.2s;
    }
    .notification-bell:hover{transform:translateY(-1px);filter:brightness(1.05)}
    .notification-badge{
      position:absolute;top:-6px;right:-6px;background:#ef4444;color:#fff;border-radius:999px;
      min-width:20px;height:20px;padding:0 6px;font-size:11px;font-weight:800;display:flex;align-items:center;justify-content:center
    }
    .notification-bell svg{width:22px;height:22px}

    /* GRID CARDS */
    .grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(260px,1fr));gap:16px}
    .card{
      background:linear-gradient(180deg, rgba(255,255,255,.03), rgba(255,255,255,.015)), var(--card);
      border:1px solid var(--border);border-radius:var(--radius);box-shadow:var(--shadow);
      padding:18px;cursor:pointer;transition:transform .08s ease, box-shadow .15s;
    }
    .card:hover{transform:translateY(-3px);box-shadow:0 16px 40px rgba(0,0,0,.45)}
    .icon{
      width:52px;height:52px;border-radius:12px;background:#2a3a8a;color:#fff;
      display:flex;align-items:center;justify-content:center;margin-bottom:10px
    }
    .icon svg{width:26px;height:26px}
    .card h3{margin:0 0 6px}
    .muted{color:var(--muted);font-size:14px}

    /* DRAWER (Notifications) */
    .drawer{position:fixed;inset:0;display:none}
    .drawer.open{display:block}
    .drawer .backdrop{
      position:absolute;inset:0;background:rgba(0,0,0,.45);
      opacity:0;transition:opacity .2s ease;
    }
    .drawer.open .backdrop{opacity:1}
    .drawer .panel{
      position:absolute;right:0;top:0;height:100%;width:min(520px, 92%);
      background:var(--card2);border-left:1px solid var(--border);box-shadow:var(--shadow);
      transform:translateX(100%);transition:transform .25s ease;display:flex;flex-direction:column;
    }
    .drawer.open .panel{transform:translateX(0)}
    .panel-head{
      display:flex;align-items:center;justify-content:space-between;padding:14px 16px;border-bottom:1px solid var(--border);
      background:linear-gradient(180deg, rgba(255,255,255,.03), rgba(255,255,255,.015));
    }
    .panel-head h3{margin:0;font-size:18px}
    .panel-tools{display:flex;gap:8px;align-items:center}
    .chip{border:1px solid #3a4477;background:transparent;color:var(--text);padding:6px 10px;border-radius:999px;cursor:pointer;font-size:13px}
    .chip.active{background:#1e3a8a;border-color:#1f3fb5}
    .search{display:flex;gap:8px;padding:10px 12px;border-bottom:1px solid var(--border)}
    .search input{flex:1;background:#111827;border:1px solid #2a335e;color:var(--text);border-radius:10px;padding:10px 12px}
    .list{overflow:auto;padding:10px 12px;display:flex;flex-direction:column;gap:10px}
    .nitem{
      border:1px solid var(--border);border-radius:12px;background:#111827;padding:10px 12px;
      display:grid;grid-template-columns:auto 1fr auto;gap:10px;align-items:center;
    }
    .nitem.unread{outline:2px solid #22d3ee}
    .n-ic{width:36px;height:36px;border-radius:999px;background:#1b2450;display:flex;align-items:center;justify-content:center}
    .n-ic svg{width:18px;height:18px}
    .n-title{font-weight:700;margin:0}
    .n-time{font-size:12px;color:var(--muted)}
    .n-msg{grid-column:2/4;color:#cfd7ff;font-size:14px;white-space:pre-wrap;margin:.2rem 0 0}
    .n-actions{display:flex;gap:8px}
    .btn{appearance:none;border:1px solid #3a4477;background:transparent;color:var(--text);padding:6px 10px;border-radius:10px;cursor:pointer;font-size:13px}
    .btn.primary{background:#22d3ee;color:#0f172a;border-color:#24cde0}
    .btn.warn{border-color:#7a2c2c;color:#ffd7d7}
    .empty{color:var(--muted);text-align:center;padding:24px}

    /* MODAL (detail) & LOGOUT */
    .modal{position:fixed;inset:0;display:none}
    .modal.open{display:block}
    .modal .backdrop{position:absolute;inset:0;background:rgba(0,0,0,.5)}
    .modal .dialog{
      position:absolute;left:50%;top:10%;transform:translateX(-50%);
      width:min(560px, 94%);background:var(--card);border:1px solid var(--border);border-radius:16px;box-shadow:var(--shadow);
      padding:16px;
    }
    .dialog h3{margin:4px 0 8px}
    .dialog .meta{color:var(--muted);font-size:13px;margin-bottom:10px}
    .dialog .content{white-space:pre-wrap;color:#e8eaf6;margin-bottom:12px}
    .dialog .foot{display:flex;gap:10px;justify-content:flex-end}
    a.link{color:#22d3ee;text-decoration:none}
    a.link:hover{text-decoration:underline}
    .row{display:flex;gap:10px;align-items:center}
    .note{color:var(--muted);font-size:13px}
  </style>
</head>
<body>
  <c:set var="greetName"
         value="${not empty sessionScope.partnerName ? sessionScope.partnerName
                 : (not empty sessionScope.account ? sessionScope.account.username : 'đối tác')}" />
  <div class="wrap">
    <div class="header">
      <div class="left">
        <div class="avatar">
          <c:out value="${fn:toUpperCase(fn:substring(greetName,0,1))}"/>
        </div>
        <div>
          <h1 class="title">Dashboard</h1>
          <p class="sub hello">Xin chào, <b><c:out value="${greetName}"/></b> — quản lý xe, đơn thuê, đánh giá & thông báo</p>
        </div>
      </div>
      <div class="header-actions">
        <!-- Bell -->
        <button id="bell" class="notification-bell" type="button" title="Thông báo" onclick="openDrawer()">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path d="M6 8a6 6 0 1 1 12 0v4l1.5 3H4.5L6 12V8Z"/><path d="M10 18a2 2 0 1 0 4 0"/>
          </svg>
          <span id="badge" class="notification-badge">
            <c:choose>
              <c:when test="${not empty unreadCount}">${unreadCount}</c:when>
              <c:when test="${empty allNotifications}">0</c:when>
              <c:otherwise>
                <c:set var="u" value="0" scope="page"/>
                <c:forEach var="n" items="${allNotifications}">
                  <c:if test="${!n.read}"><c:set var="u" value="${u+1}" scope="page"/></c:if>
                </c:forEach>
                ${u}
              </c:otherwise>
            </c:choose>
          </span>
        </button>
        <button class="logout-btn" type="button" onclick="openLogout()">Đăng xuất</button>
      </div>
    </div>

    <!-- QUICK ACTIONS -->
    <div class="grid" style="margin-bottom:18px">
      <div class="card" onclick="location.href='${pageContext.request.contextPath}/partner?action=editProfile'">
        <div class="icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <circle cx="12" cy="7" r="3.5"/><path d="M4 20c1.5-4.5 14.5-4.5 16 0"/>
          </svg>
        </div>
        <h3>Hồ sơ cửa hàng</h3><div class="muted">Cập nhật tên, SĐT, địa chỉ</div>
      </div>

      <!-- ICON MOTORBIKE mới, nét đẹp -->
      <div class="card" onclick="location.href='${pageContext.request.contextPath}/viewmotorbike'">
        <div class="icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <circle cx="6.5" cy="15.5" r="3.0"></circle>
            <circle cx="17.5" cy="15.5" r="3.0"></circle>
            <path d="M9 15.5h5"></path>
            <path d="M9.2 12l1.6-2.2c.3-.4.7-.8 1.2-.8H14l2.6 2.6"></path>
            <path d="M14.5 9.0h2.2c.8 0 1.3.3 1.8.9l1.2 1.6"></path>
            <path d="M7.4 9.8l1.2 1.2"></path>
          </svg>
        </div>
        <h3>Xe của tôi</h3><div class="muted">Quản lý danh sách xe</div>
      </div>

      <div class="card" onclick="location.href='${pageContext.request.contextPath}/rentalhistory'">
        <div class="icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path d="M7 3h10v4H7zM4 7h16v14H4z"/><path d="M8 11h8M8 15h8"/>
          </svg>
        </div>
        <h3>Lịch sử cho thuê</h3><div class="muted">Tất cả giao dịch</div>
      </div>

      <div class="card" onclick="location.href='${pageContext.request.contextPath}/viewreviewservlet'">
        <div class="icon">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2 9.5 8H3.5l5 3.8L6.9 18 12 14.6 17.1 18 15.5 11.8l5-3.8h-6L12 2z"/>
          </svg>
        </div>
        <h3>Đánh giá</h3><div class="muted">Theo dõi phản hồi</div>
      </div>
    </div>
  </div>

  <!-- ============ NOTIFICATION DRAWER ============ -->
  <div id="drawer" class="drawer" aria-hidden="true">
    <div class="backdrop" onclick="closeDrawer()"></div>
    <aside class="panel" role="dialog" aria-label="Tất cả thông báo">
      <div class="panel-head">
        <h3>Thông báo</h3>
        <div class="panel-tools">
          <button id="chipAll" class="chip active" type="button">Tất cả</button>
          <button id="chipUnread" class="chip" type="button">Chưa đọc</button>
          <form method="post" action="${pageContext.request.contextPath}/dashboard" onsubmit="setTimeout(closeDrawer,60)">
            <input type="hidden" name="action" value="readAll"/>
            <button class="chip" type="submit" title="Đánh dấu đọc tất cả">Đọc tất cả</button>
          </form>
        </div>
      </div>
      <div class="search">
        <input id="q" type="search" placeholder="Tìm theo tiêu đề hoặc nội dung..."/>
      </div>
      <div id="nList" class="list">
        <c:choose>
          <c:when test="${empty allNotifications}">
            <div class="empty">Chưa có thông báo nào.</div>
          </c:when>
          <c:otherwise>
            <c:forEach var="n" items="${allNotifications}">
              <div class="nitem ${n.read ? '' : 'unread'}"
                   data-id="${n.notificationId}"
                   data-read="${n.read ? '1' : '0'}"
                   data-title="${fn:escapeXml(n.title)}"
                   data-message="${fn:escapeXml(n.message)}">
                <div class="n-ic">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <path d="M6 8a6 6 0 1 1 12 0v4l1.5 3H4.5L6 12V8Z"/><path d="M10 18a2 2 0 1 0 4 0"/>
                  </svg>
                </div>
                <div>
                  <p class="n-title"><c:out value="${n.title}"/></p>
                  <div class="n-time">
                    <fmt:formatDate value="${n.createdAt}" pattern="HH:mm dd/MM/yyyy"/>
                    <c:if test="${!n.read}"> · <span style="color:#22d3ee">MỚI</span></c:if>
                  </div>
                </div>
                <div class="n-actions">
                  <button class="btn" type="button" onclick="openDetail(this)">Xem</button>
                  <button class="btn primary" type="button" onclick="markReadAndFade(this)">Đã đọc</button>
                </div>
                <div class="n-msg"><c:out value="${n.message}"/></div>
              </div>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </div>
    </aside>
  </div>

  <!-- ============ MODAL DETAIL ============ -->
  <div id="modal" class="modal" aria-hidden="true">
    <div class="backdrop" onclick="closeModal()"></div>
    <div class="dialog" role="dialog" aria-label="Chi tiết thông báo">
      <h3 id="mdTitle">Chi tiết thông báo</h3>
      <div class="meta" id="mdTime"></div>
      <div class="content" id="mdMsg"></div>
      <div class="foot">
        <a id="mdLink" class="btn" href="#" style="display:none">Mở chi tiết →</a>
        <button class="btn primary" type="button" onclick="markCurrentAsRead()">Đánh dấu đã đọc</button>
        <button class="btn" type="button" onclick="closeModal()">Đóng</button>
      </div>
    </div>
  </div>

  <!-- ============ LOGOUT MODAL (form UI, không dùng confirm) ============ -->
  <div id="logoutModal" class="modal" aria-hidden="true">
    <div class="backdrop" onclick="closeLogout()"></div>
    <div class="dialog" role="dialog" aria-label="Đăng xuất">
      <h3>Đăng xuất tài khoản</h3>
      <p class="note">Bạn chắc chắn muốn đăng xuất khỏi phiên làm việc hiện tại?</p>
      <label class="row" style="margin:8px 0 14px">
        <input id="logoutAll" type="checkbox" style="accent-color:#22d3ee; width:16px; height:16px"/>
        <span>Đăng xuất khỏi tất cả thiết bị</span>
      </label>
      <div class="foot">
        <button class="btn" type="button" onclick="closeLogout()">Huỷ</button>
        <button class="btn primary" type="button" onclick="confirmLogout()">Đăng xuất</button>
      </div>
    </div>
  </div>

  <script>
    const ctx = '<%=request.getContextPath()%>';
    const drawer = document.getElementById('drawer');
    const list = document.getElementById('nList');
    const q = document.getElementById('q');
    const chipAll = document.getElementById('chipAll');
    const chipUnread = document.getElementById('chipUnread');
    const badge = document.getElementById('badge');

    // Modal state
    let currentId = null;
    const modal = document.getElementById('modal');
    const mdTitle = document.getElementById('mdTitle');
    const mdTime  = document.getElementById('mdTime');
    const mdMsg   = document.getElementById('mdMsg');
    const mdLink  = document.getElementById('mdLink');

    // Logout modal
    const logoutModal = document.getElementById('logoutModal');
    function openLogout(){ logoutModal.classList.add('open'); logoutModal.setAttribute('aria-hidden','false'); }
    function closeLogout(){ logoutModal.classList.remove('open'); logoutModal.setAttribute('aria-hidden','true'); }
    function confirmLogout(){
      const all = document.getElementById('logoutAll')?.checked;
      const url = all ? (ctx + '/logout?all=1') : (ctx + '/logout');
      location.href = url;
    }

    function openDrawer(){ drawer.classList.add('open'); drawer.setAttribute('aria-hidden','false'); }
    function closeDrawer(){ drawer.classList.remove('open'); drawer.setAttribute('aria-hidden','true'); }

    function openModal(){ modal.classList.add('open'); modal.setAttribute('aria-hidden','false'); }
    function closeModal(){ modal.classList.remove('open'); modal.setAttribute('aria-hidden','true'); currentId=null; }

    // Filter (search + unread)
    function applyFilter(){
      const kw = (q.value||'').toLowerCase();
      const unreadOnly = chipUnread.classList.contains('active');
      let shown = 0;
      [...list.querySelectorAll('.nitem')].forEach(it=>{
        const title = (it.dataset.title||'').toLowerCase();
        const msg   = (it.dataset.message||'').toLowerCase();
        const isUnread = it.dataset.read === '0';
        const match = (!kw || title.includes(kw) || msg.includes(kw)) && (!unreadOnly || isUnread);
        it.style.display = match ? '' : 'none';
        if(match) shown++;
      });
      if(shown===0){
        if(!list.querySelector('.empty')) {
          const dv=document.createElement('div'); dv.className='empty'; dv.textContent='Không có thông báo phù hợp.'; list.appendChild(dv);
        }
      } else {
        const em = list.querySelector('.empty'); if(em) em.remove();
      }
    }
    q?.addEventListener('input', applyFilter);
    chipAll?.addEventListener('click', ()=>{chipAll.classList.add('active');chipUnread.classList.remove('active');applyFilter();});
    chipUnread?.addEventListener('click', ()=>{chipUnread.classList.add('active');chipAll.classList.remove('active');applyFilter();});

    // Detail modal
    function openDetail(btn){
      const item = btn.closest('.nitem');
      if(!item) return;
      currentId = item.dataset.id;
      mdTitle.textContent = item.dataset.title || 'Thông báo';
      mdTime.textContent = item.querySelector('.n-time')?.textContent || '';
      mdMsg.textContent  = item.dataset.message || '';
      // derive link từ token [URL:], [ORDER:], [BIKE:]
      let link = deriveLink(mdMsg.textContent);
      if(link){
        // Nếu là trang maintenance thì tự gắn ?nid=<id> để hiển thị động
        if(link.startsWith('/status/maintenance') && !/[?&]nid=\d+/.test(link)){
          const sep = link.includes('?') ? '&' : '?';
          link = link + sep + 'nid=' + currentId;
        }
        mdLink.href = ctx + link;
        mdLink.style.display = 'inline-flex';
      } else {
        mdLink.style.display = 'none';
      }
      openModal();
    }

    function markCurrentAsRead(){
      if(!currentId) return;
      fetch(ctx+'/dashboard', {
        method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'},
        body:new URLSearchParams({action:'read', id: currentId})
      }).catch(()=>{});
      const item = list.querySelector(`.nitem[data-id="${currentId}"]`);
      if(item){
        item.classList.remove('unread'); item.dataset.read='1';
        decBadge();
      }
      closeModal();
    }

    function markReadAndFade(btn){
      const item = btn.closest('.nitem'); if(!item) return;
      const id = item.dataset.id;
      fetch(ctx+'/dashboard', {
        method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'},
        body:new URLSearchParams({action:'read', id})
      }).catch(()=>{});
      item.style.opacity='.5'; item.classList.remove('unread'); item.dataset.read='1';
      decBadge();
    }

    function deriveLink(text){
      // Ưu tiên [URL:/path]
      const mUrl = text.match(/\[URL:([^\]]+)]/i);
      if(mUrl) return mUrl[1].trim();
      const mOrder = text.match(/\[ORDER:(\d+)]/i);
      if(mOrder) return '/rentalhistory?orderId=' + mOrder[1];
      const mBike  = text.match(/\[BIKE:(\d+)]/i);
      if(mBike) return '/viewmotorbike?bikeId=' + mBike[1];
      return null;
    }

    function decBadge(){
      if(!badge) return;
      const n = Math.max(0, (+badge.textContent||0) - 1);
      badge.textContent = n;
    }

    // Close with ESC
    document.addEventListener('keydown', (e)=>{
      if(e.key==='Escape'){
        if(document.getElementById('logoutModal').classList.contains('open')) { closeLogout(); return; }
        if(modal.classList.contains('open')) closeModal();
        else if(drawer.classList.contains('open')) closeDrawer();
      }
    });
  </script>
</body>
</html>
