<%-- partials/ai-chatbox.jsp (include vào bất kỳ trang nào) --%>
<%@ page pageEncoding="UTF-8" %>

<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600&display=swap" rel="stylesheet">

<style>
  :root{
    --ai-bg:#0b1224; --ai-panel:#0f172a; --ai-card:#111827; --ai-border:#243244;
    --ai-text:#e5e7eb; --ai-muted:#9ca3af; --ai-accent:#3b82f6;
  }
  .ai-fab{
    position:fixed;right:22px;bottom:22px;z-index:9998;width:56px;height:56px;border-radius:50%;
    border:1px solid var(--ai-border);background:linear-gradient(180deg,#1f2937,#0b1224);color:#fff;
    font-size:22px;cursor:pointer;box-shadow:0 12px 30px rgba(0,0,0,.35);transition:transform .15s,box-shadow .2s;
    display:flex;align-items:center;justify-content:center;font-family:'Inter',system-ui,Segoe UI,Roboto,Arial;
  }
  .ai-fab:hover{transform:translateY(-2px);box-shadow:0 18px 40px rgba(0,0,0,.45)}

  .ai-panel{
    position:fixed;right:22px;bottom:90px;z-index:9999;width:min(380px,92vw);height:520px;border-radius:16px;
    background:var(--ai-panel);color:var(--ai-text);border:1px solid var(--ai-border);
    box-shadow:0 18px 60px rgba(0,0,0,.45);transform:translateY(8px) scale(.98);opacity:0;pointer-events:none;
    transition:transform .18s,opacity .18s;font-family:'Inter',system-ui,Segoe UI,Roboto,Arial;
  }
  .ai-panel.open{transform:translateY(0) scale(1);opacity:1;pointer-events:auto}

  .ai-header{display:flex;align-items:center;justify-content:space-between;gap:10px;padding:14px;background:linear-gradient(180deg,#131c31,#0f172a);border-bottom:1px solid var(--ai-border);border-radius:16px 16px 0 0}
  .ai-title{font-weight:700;letter-spacing:.2px}
  .ai-sub{font-size:12px;color:var(--ai-muted);margin-top:2px}
  .ai-close{width:36px;height:36px;border-radius:10px;background:#0b1224;color:#e5e7eb;border:1px solid var(--ai-border);cursor:pointer}

  .ai-messages{height:360px;overflow:auto;padding:14px 12px 8px;background:var(--ai-bg)}
  .msg{display:flex;margin:10px 0}
  .msg.user{justify-content:flex-end}
  .bubble{max-width:85%;padding:10px 12px;border-radius:12px;line-height:1.45;background:var(--ai-card);border:1px solid var(--ai-border);white-space:pre-wrap;word-break:break-word}
  .msg.user .bubble{background:#1b2540;border-color:#2b3a55}
  .msg.bot .bubble a{color:var(--ai-accent);text-decoration:none}
  .msg.bot .bubble a:hover{text-decoration:underline}

  .ai-input{display:flex;gap:8px;padding:10px;border-top:1px solid var(--ai-border);background:linear-gradient(180deg,#0f172a,#0b1224);border-radius:0 0 16px 16px}
  .ai-input input{flex:1;height:40px;border-radius:10px;background:#0a1328;color:#e5e7eb;border:1px solid #22314a;padding:0 12px;outline:none}
  .ai-send{padding:0 14px;border-radius:10px;border:1px solid #28406b;cursor:pointer;color:#fff;background:linear-gradient(180deg,#2563eb,#1d4ed8)}
</style>

<button id="ai-fab" class="ai-fab" aria-controls="ai-panel" aria-expanded="false" title="Mở trợ lý AI">🤖</button>

<div id="ai-panel" class="ai-panel" role="dialog" aria-modal="true" aria-labelledby="ai-title" hidden>
  <div class="ai-header">
    <div>
      <div id="ai-title" class="ai-title">Trợ lý AI · RideNow</div>
      <div class="ai-sub">Hỏi dữ liệu hoặc trò chuyện tự nhiên</div>
    </div>
    <button id="ai-close" class="ai-close" aria-label="Đóng (Esc)">✕</button>
  </div>

  <div id="ai-messages" class="ai-messages" aria-live="polite">
    <div class="msg bot">
      <div class="bubble">Xin chào! 👋 Tôi là trợ lý AI của RideNow.<br/>Bạn muốn hỏi gì hôm nay?</div>
    </div>
  </div>

  <form id="ai-form" class="ai-input" autocomplete="off">
    <input id="ai-q" type="text" placeholder="Nhập câu hỏi… (vd: Top 5 xe số / xe ga dưới 180000 ở Đà Nẵng)" aria-label="Nội dung câu hỏi"/>
    <button type="submit" class="ai-send" aria-label="Gửi">Gửi</button>
  </form>
</div>

<script>
  (() => {
    const ctx = '<%= request.getContextPath() %>';

    const $fab  = document.getElementById('ai-fab');
    const $panel= document.getElementById('ai-panel');
    const $close= document.getElementById('ai-close');
    const $form = document.getElementById('ai-form');
    const $q    = document.getElementById('ai-q');
    const $msgs = document.getElementById('ai-messages');

    function openPanel(){ $panel.hidden=false; requestAnimationFrame(()=> $panel.classList.add('open')); $fab.setAttribute('aria-expanded','true'); setTimeout(()=> $q.focus(),120); }
    function closePanel(){ $panel.classList.remove('open'); $fab.setAttribute('aria-expanded','false'); setTimeout(()=>{ $panel.hidden=true; },180); }
    function escapeHtml(s){ return (s||'').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;'); }
    function addMsg(role, html){ const w=document.createElement('div'); w.className='msg '+(role==='user'?'user':'bot'); const b=document.createElement('div'); b.className='bubble'; b.innerHTML=html; w.appendChild(b); $msgs.appendChild(w); $msgs.scrollTop=$msgs.scrollHeight; }

    $fab.addEventListener('click',()=> $panel.hidden?openPanel():closePanel());
    $close.addEventListener('click',closePanel);
    document.addEventListener('keydown',e=>{ if(e.key==='Escape' && !$panel.hidden) closePanel(); });

    $form.addEventListener('submit', async (e)=>{
      e.preventDefault();
      const q = ($q.value||'').trim();
      if(!q) return;
      addMsg('user', escapeHtml(q));
      $q.value='';

      try{
        const res = await fetch(ctx + '/ai/chat', {
          method:'POST',
          headers:{'Content-Type':'application/json; charset=UTF-8'},
          body: JSON.stringify({question:q})
        });
        const data = await res.json();
        if (data.answer){
          // Thay __CTX__ bằng context path & xuống dòng → <br/>
          const html = String(data.answer).replace(/__CTX__/g, ctx).replace(/\n/g,'<br/>');
          addMsg('bot', html);
        } else if (data.error){
          addMsg('bot', '❌ ' + escapeHtml(data.error));
        } else {
          addMsg('bot', '⚠️ Không có phản hồi.');
        }
      }catch(err){
        addMsg('bot', '❌ Lỗi mạng: ' + escapeHtml(err.message));
      }
    });
  })();
</script>
