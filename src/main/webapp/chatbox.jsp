<%-- partials/ai-chatbox.jsp (include vào bất kỳ trang nào) --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>

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

  .ai-header{display:flex;align-items:center;justify-content:space-between;gap:10px;padding:14px;background:linear-gradient(180deg,#131c31,#0f172a);border-bottom:1px solid var(--ai-border);border-radius:16px 16px 0 0;position:relative}
  .ai-title{font-weight:700;letter-spacing:.2px}
  .ai-sub{font-size:12px;color:var(--ai-muted);margin-top:2px}
  .ai-close{width:36px;height:36px;border-radius:10px;background:#0b1224;color:#e5e7eb;border:1px solid var(--ai-border);cursor:pointer}
  
  .ai-clear-history{width:36px;height:36px;border-radius:10px;background:#0b1224;color:var(--ai-muted);border:1px solid var(--ai-border);cursor:pointer;font-size:14px}
  .ai-clear-history:hover{background:rgba(239,68,68,0.1);color:#ef4444}

  .ai-messages{height:360px;overflow:auto;padding:14px 12px 8px;background:var(--ai-bg)}
  .msg{display:flex;margin:10px 0;animation:fadeIn 0.3s ease-in}
  .msg.user{justify-content:flex-end}
  .bubble{max-width:85%;padding:10px 12px;border-radius:12px;line-height:1.45;background:var(--ai-card);border:1px solid var(--ai-border);white-space:pre-wrap;word-break:break-word}
  .msg.user .bubble{background:#1b2540;border-color:#2b3a55}
  .msg.bot .bubble a{color:var(--ai-accent);text-decoration:none;font-weight:500}
  .msg.bot .bubble a:hover{text-decoration:underline}
  .msg.bot .bubble ul{margin:6px 0;padding-left:16px}
  .msg.bot .bubble li{margin:4px 0;padding:4px 0}

  .ai-input{display:flex;gap:8px;padding:10px;border-top:1px solid var(--ai-border);background:linear-gradient(180deg,#0f172a,#0b1224);border-radius:0 0 16px 16px}
  .ai-input input{flex:1;height:40px;border-radius:10px;background:#0a1328;color:#e5e7eb;border:1px solid #22314a;padding:0 12px;outline:none;font-family:inherit}
  .ai-input input:focus{border-color:#3b82f6}
  .ai-send{padding:0 14px;border-radius:10px;border:1px solid #28406b;cursor:pointer;color:#fff;background:linear-gradient(180deg,#2563eb,#1d4ed8);font-family:inherit}
  .ai-send:hover{background:linear-gradient(180deg,#1d4ed8,#1e40af)}
  .ai-send:disabled{opacity:0.6;cursor:not-allowed}

  .typing-indicator{display:none;padding:8px 12px;color:var(--ai-muted);font-size:12px;font-style:italic}
  .typing-indicator.show{display:block}

  @keyframes fadeIn {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
  }

  /* Quick suggestions */
  .ai-suggestions{margin:10px 0;padding:12px;background:rgba(59,130,246,0.1);border-radius:12px;border:1px solid rgba(59,130,246,0.3)}
  .suggestion-title{font-size:12px;color:#93c5fd;margin-bottom:8px;font-weight:600}
  .suggestion-btn{display:block;width:100%;padding:8px 12px;margin:4px 0;background:rgba(255,255,255,0.05);border:1px solid #334155;border-radius:8px;color:#e5e7eb;cursor:pointer;text-align:left;font-size:13px;transition:all 0.2s;font-family:inherit}
  .suggestion-btn:hover{background:rgba(59,130,246,0.2);border-color:#3b82f6}

  /* Status colors */
  .status-available{color:#10b981}
  .status-rented{color:#ef4444}
  .status-maintenance{color:#f59e0b}
</style>

<button id="ai-fab" class="ai-fab" aria-controls="ai-panel" aria-expanded="false" title="Mở trợ lý AI">🤖</button>

<div id="ai-panel" class="ai-panel" role="dialog" aria-modal="true" aria-labelledby="ai-title" hidden>
  <div class="ai-header">
    <div>
      <div id="ai-title" class="ai-title">Trợ lý AI · RideNow</div>
      <div class="ai-sub">Hỏi dữ liệu hoặc trò chuyện tự nhiên</div>
    </div>
    <div style="display:flex;gap:6px">
      <button id="ai-clear-history" class="ai-clear-history" title="Xóa lịch sử trò chuyện" aria-label="Xóa lịch sử">🗑️</button>
      <button id="ai-close" class="ai-close" aria-label="Đóng (Esc)">✕</button>
    </div>
  </div>

  <div id="ai-messages" class="ai-messages" aria-live="polite">
    <!-- Messages will be loaded from localStorage -->
  </div>

  <div id="typing-indicator" class="typing-indicator">
    <div class="bubble">Trợ lý đang trả lời...</div>
  </div>

  <form id="ai-form" class="ai-input" autocomplete="off">
    <input id="ai-q" type="text" placeholder="Nhập câu hỏi… (vd: Top 5 xe số / xe ga dưới 180000)" aria-label="Nội dung câu hỏi"/>
    <button type="submit" class="ai-send" aria-label="Gửi" id="ai-send-btn">Gửi</button>
  </form>
</div>
 <script>
  (() => {
    // SỬA LỖI: Thay thế request.getContextPath() bằng pageContext.request.contextPath
    const ctx = '${pageContext.request.contextPath}';
    const CHAT_HISTORY_KEY = 'ridenow_chat_history';

    const $fab = document.getElementById('ai-fab');
    const $panel = document.getElementById('ai-panel');
    const $close = document.getElementById('ai-close');
    const $clear = document.getElementById('ai-clear-history');
    const $form = document.getElementById('ai-form');
    const $q = document.getElementById('ai-q');
    const $msgs = document.getElementById('ai-messages');
    const $typing = document.getElementById('typing-indicator');
    const $sendBtn = document.getElementById('ai-send-btn');

    // ========= LỊCH SỬ CHAT =========
    function loadChatHistory() {
      try {
        const history = localStorage.getItem(CHAT_HISTORY_KEY);
        if (history) {
          const messages = JSON.parse(history);
          $msgs.innerHTML = '';
          messages.forEach(msg => {
            addMsg(msg.role, msg.html, false);
          });
          console.log('Đã tải lịch sử:', messages.length, 'tin nhắn');
        } else {
          showWelcomeMessage();
        }
      } catch (e) {
        console.error('Lỗi tải lịch sử:', e);
        showWelcomeMessage();
      }
    }

    function saveChatHistory() {
      try {
        const messages = [];
        $msgs.querySelectorAll('.msg').forEach(msgEl => {
          const role = msgEl.classList.contains('user') ? 'user' : 'bot';
          const bubble = msgEl.querySelector('.bubble');
          if (bubble) {
            messages.push({
              role: role,
              html: bubble.innerHTML
            });
          }
        });
        localStorage.setItem(CHAT_HISTORY_KEY, JSON.stringify(messages));
      } catch (e) {
        console.error('Lỗi lưu lịch sử:', e);
      }
    }

    function clearChatHistory() {
      if (confirm('Bạn có chắc muốn xóa toàn bộ lịch sử trò chuyện?')) {
        localStorage.removeItem(CHAT_HISTORY_KEY);
        $msgs.innerHTML = '';
        showWelcomeMessage();
        console.log('Đã xóa lịch sử trò chuyện');
      }
    }

    function showWelcomeMessage() {
      const welcomeMsg = 'Xin chào! 👋 Tôi là trợ lý AI của RideNow.<br/>Bạn có thể hỏi tôi về:<br/>• <strong>Xe số, xe ga, xe PKL</strong><br/>• <strong>Giá thuê xe</strong><br/>• <strong>Xe có sẵn</strong><br/>• <strong>Thông tin chi tiết xe</strong>';
      addMsg('bot', welcomeMsg, false);
      showQuickSuggestions();
    }

    function showQuickSuggestions() {
      const suggestions = [
        "Xe số có những loại nào?",
        "Xe ga giá dưới 150k?",
        "Xe PKL nào đang có sẵn?",
        "Tìm xe theo giá từ thấp đến cao"
      ];
      
      let html = '<div class="ai-suggestions">';
      html += '<div class="suggestion-title">💡 Gợi ý câu hỏi:</div>';
      
      suggestions.forEach(function(q) {
        html += '<button class="suggestion-btn" onclick="setQuestion(\'' + q + '\')">' + q + '</button>';
      });
      
      html += '</div>';
      $msgs.innerHTML += html;
      saveChatHistory();
    }

    // ========= XỬ LÝ GIAO DIỆN =========
    function openPanel() { 
      $panel.hidden = false; 
      requestAnimationFrame(() => $panel.classList.add('open')); 
      $fab.setAttribute('aria-expanded', 'true'); 
      setTimeout(() => $q.focus(), 120); 
      loadChatHistory();
    }

    function closePanel() { 
      $panel.classList.remove('open'); 
      $fab.setAttribute('aria-expanded', 'false'); 
      setTimeout(() => { $panel.hidden = true; }, 180); 
    }

    function escapeHtml(s) { 
      return (s || '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;'); 
    }

    function addMsg(role, html, saveToHistory = true) {
      const w = document.createElement('div');
      w.className = 'msg ' + (role === 'user' ? 'user' : 'bot');
      const b = document.createElement('div');
      b.className = 'bubble';
      b.innerHTML = html;
      w.appendChild(b);
      $msgs.appendChild(w);
      $msgs.scrollTop = $msgs.scrollHeight;
      
      if (saveToHistory) {
        saveChatHistory();
      }
    }

    function showTyping() {
      $typing.classList.add('show');
      $msgs.scrollTop = $msgs.scrollHeight;
    }

    function hideTyping() {
      $typing.classList.remove('show');
    }

    function setQuestion(text) {
      $q.value = text;
      $form.dispatchEvent(new Event('submit'));
    }

    // ========= EVENT LISTENERS =========
    $fab.addEventListener('click', () => $panel.hidden ? openPanel() : closePanel());
    $close.addEventListener('click', closePanel);
    $clear.addEventListener('click', clearChatHistory);
    
    document.addEventListener('keydown', e => { 
      if(e.key === 'Escape' && !$panel.hidden) closePanel(); 
    });

    $form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const q = ($q.value || '').trim();
      if(!q) return;
      
      // Disable form while processing
      $sendBtn.disabled = true;
      $q.disabled = true;
      
      addMsg('user', escapeHtml(q));
      $q.value = '';
      showTyping();

      try {
        const res = await fetch(ctx + '/ai/chat', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json; charset=UTF-8' },
          body: JSON.stringify({ question: q })
        });
        
        const data = await res.json();
        hideTyping();
        
        if (data.answer) {
          // Replace context path and handle newlines
          const html = String(data.answer)
            .replace(/__CTX__/g, ctx)
            .replace(/\n/g, '<br/>')
            .replace(/available/g, '<span class="status-available">available</span>')
            .replace(/rented/g, '<span class="status-rented">rented</span>')
            .replace(/maintenance/g, '<span class="status-maintenance">maintenance</span>');
          addMsg('bot', html);
        } else if (data.error) {
          addMsg('bot', '❌ ' + escapeHtml(data.error));
        } else {
          addMsg('bot', '⚠️ Không có phản hồi từ hệ thống.');
        }
      } catch(err) {
        hideTyping();
        addMsg('bot', '❌ Lỗi kết nối: ' + escapeHtml(err.message));
        console.error('Lỗi gửi câu hỏi:', err);
      } finally {
        // Re-enable form
        $sendBtn.disabled = false;
        $q.disabled = false;
        $q.focus();
      }
    });

    // Load initial history when page loads
    loadChatHistory();

    // Make setQuestion available globally for suggestion buttons
    window.setQuestion = setQuestion;
  })();
</script>
