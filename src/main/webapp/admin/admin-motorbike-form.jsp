<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${empty motorbike ? 'Thêm Xe Mới' : 'Sửa Thông Tin Xe'} - RideNow Admin</title>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
  <style>
    :root{
      --mb-primary:#3b82f6; --mb-primary-600:#2563eb; --mb-primary-50:#eef2ff;
      --mb-success:#10b981; --mb-success-50:#ecfdf5;
      --mb-warn:#f59e0b; --mb-warn-50:#fffbeb;
      --mb-danger:#ef4444; --mb-danger-50:#fff1f2;
      --mb-ink:#0f172a; --mb-muted:#64748b; --mb-bg:#f8fafc; --mb-card:#ffffff; --mb-line:#e5e7eb;
      --mb-radius:12px; --mb-shadow:0 8px 20px rgba(2,6,23,.06);
    }
    *{box-sizing:border-box}
    html,body{height:100%}
    body.admin{margin:0;background:var(--mb-bg);color:var(--mb-ink);
      font-family:Inter,system-ui,-apple-system,"Segoe UI",Roboto,Arial,sans-serif;
      display:flex;min-height:100vh;}
    .mb-form-container{max-width:1000px;margin:18px 22px}
    .mb-form-panel{background:var(--mb-card);border:1px solid var(--mb-line);border-radius:var(--mb-radius);box-shadow:var(--mb-shadow);overflow:hidden}
    .mb-form-header{padding:16px 18px;border-bottom:1px solid var(--mb-line);background:#f8fafc}
    .mb-form-header h2{margin:0;font-size:18px;font-weight:800}
    .mb-form-body{padding:24px}
    .mb-form-grid{display:grid;grid-template-columns:1fr 1fr;gap:20px}
    .mb-form-group{display:flex;flex-direction:column;gap:8px}
    .mb-form-group.full{grid-column:1/-1}
    .mb-label{font-size:14px;font-weight:700;color:#334155}
    .mb-label.required:after{content:" *";color:var(--mb-danger)}
    .mb-input,.mb-select,.mb-textarea{padding:12px 14px;border:1px solid var(--mb-line);border-radius:10px;background:#fff;font-size:14px;outline:none;transition:all .15s}
    .mb-input:focus,.mb-select:focus,.mb-textarea:focus{border-color:var(--mb-primary);box-shadow:0 0 0 4px rgba(59,130,246,.12)}
    .mb-textarea{min-height:100px;resize:vertical;font-family:inherit}
    .mb-radio-group{display:flex;gap:24px;margin-top:4px}
    .mb-radio-option{display:flex;align-items:center;gap:8px;cursor:pointer}
    .mb-radio-option input[type="radio"]{width:18px;height:18px;accent-color:var(--mb-primary)}
    .mb-owner-selection{margin-top:12px;padding:16px;background:#f8fafc;border-radius:8px;border:1px solid var(--mb-line);display:none}
    .mb-owner-selection.active{display:block}
    .mb-static-field{display:flex;align-items:center;gap:10px}
    .mb-pill{display:inline-flex;align-items:center;gap:8px;padding:8px 12px;border-radius:999px;background:var(--mb-primary-50);color:#1e3a8a;font-weight:600;font-size:13px;border:1px solid #c7d2fe}
    .mb-hint{font-size:12px;color:#64748b;margin-top:6px}
    .gallery { display:grid; gap:12px; }
    .gallery-main{ position:relative; border-radius:12px; overflow:hidden; background:#0b1224; border:1px solid #334155; }
    .gallery-main img{ width:100%; height:300px; object-fit:cover; display:block; }
    .gallery-nav{ position:absolute; inset:0; display:flex; align-items:center; justify-content:space-between; pointer-events:none; }
    .gallery-btn{ pointer-events:auto; width:40px; height:40px; border:none; border-radius:999px; background:rgba(0,0,0,.45); color:#fff; display:flex; align-items:center; justify-content:center; margin:0 8px; cursor:pointer; transition:.2s; }
    .gallery-btn:hover{ background:rgba(0,0,0,.7); transform:translateY(-1px); }
    .thumbs{ display:none; grid-template-columns:repeat(6,1fr); gap:10px; }
    .thumbs.show{ display:grid; }
    .thumb{ position:relative; border:2px solid transparent; border-radius:10px; overflow:hidden; cursor:pointer; transition:.2s; height:74px; background:#0b1224; }
    .thumb img{ width:100%; height:100%; object-fit:cover; display:block; }
    .thumb.active, .thumb:hover{ border-color:#3b82f6; box-shadow:0 0 0 3px rgba(59,130,246,.15); }
    .thumb-delete{ position:absolute; top:4px; right:4px; width:20px; height:20px; border:none; border-radius:50%; background:rgba(239,68,68,0.9); color:white; display:flex; align-items:center; justify-content:center; cursor:pointer; opacity:0; transition:opacity 0.2s; font-size:10px; }
    .thumb:hover .thumb-delete{ opacity:1; }
    .thumb-delete:hover{ background:rgb(220,38,38); transform:scale(1.1); }
    .upload-area{ border:2px dashed #d1d5db; border-radius:10px; padding:30px; text-align:center; background:#fafafa; transition:all 0.3s; cursor:pointer; margin-bottom:16px; }
    .upload-area:hover{ border-color:#3b82f6; background:#f0f9ff; }
    .upload-area.dragover{ border-color:#3b82f6; background:#dbeafe; }
    .upload-icon{ font-size:48px; color:#6b7280; margin-bottom:12px; }
    .upload-text{ font-size:16px; color:#374151; margin-bottom:8px; }
    .upload-hint{ font-size:14px; color:#6b7280; }
    .upload-preview{ display:grid; grid-template-columns:repeat(auto-fill, minmax(120px, 1fr)); gap:12px; margin-top:16px; }
    .upload-preview-item{ position:relative; border-radius:8px; overflow:hidden; height:100px; border:1px solid #e5e7eb; }
    .upload-preview-item img{ width:100%; height:100%; object-fit:cover; }
    .upload-preview-remove{ position:absolute; top:4px; right:4px; width:20px; height:20px; border:none; border-radius:50%; background:rgba(0,0,0,0.6); color:white; display:flex; align-items:center; justify-content:center; cursor:pointer; font-size:10px; }
    .upload-preview-info{ padding:8px; background:#f8fafc; border-top:1px solid #e5e7eb; font-size:12px; color:#64748b; }
    .mb-btn-group{display:flex;gap:12px;justify-content:flex-end;margin-top:32px;padding-top:20px;border-top:1px solid var(--mb-line)}
    .mb-btn{display:inline-flex;align-items:center;gap:8px;padding:12px 20px;border-radius:10px;font-weight:700;border:1px solid transparent;cursor:pointer;transition:.15s;text-decoration:none;font-size:14px}
    .mb-btn-primary{background:var(--mb-primary);color:#fff}
    .mb-btn-primary:hover{background:var(--mb-primary-600);transform:translateY(-1px)}
    .mb-btn-secondary{background:#64748b;color:#fff}
    .mb-btn-secondary:hover{background:#475569;transform:translateY(-1px)}
    .mb-btn-outline{background:transparent;border-color:var(--mb-line);color:var(--mb-muted)}
    .mb-btn-outline:hover{background:#f8fafc;color:var(--mb-ink)}
    .mb-btn-danger{background:var(--mb-danger);color:#fff}
    .mb-btn-danger:hover{background:#dc2626;transform:translateY(-1px)}
    .image-actions{ display:flex; gap:12px; margin-top:16px; }
    @media (max-width:768px){
      .mb-form-grid{grid-template-columns:1fr}
      .mb-form-container{margin:14px}
      .content-header{padding:12px 16px}
      .mb-btn-group{flex-direction:column}
      .thumbs{ grid-template-columns:repeat(4,1fr); }
      .gallery-main img{ height:250px; }
    }
    @media (max-width:1100px){
      .sidebar-nav a span,.brand h1 { display: none; }
      .sidebar { width: 80px; }
      .sidebar-nav a { justify-content: center; padding: 1rem; }
      .brand { justify-content: center; padding: 1rem 0.5rem; }
    }
  </style>
</head>

<script>
  // --- set selected cho type & status an toàn ---
  (function initSelectDefaults(){
    // typeId
    <%-- nếu có motorbike thì gán selected bằng JS (tránh truy cập motorbike.typeId khi null) --%>
    <c:if test="${not empty motorbike}">
      const typeSel = document.getElementById('typeId');
      if (typeSel) {
        const currentType = '${motorbike.typeId}';
        if (currentType) typeSel.value = String(currentType);
      }
      const statusSel = document.getElementById('status');
      if (statusSel) {
        const currentStatus = '${motorbike.status}';
        if (currentStatus) statusSel.value = currentStatus;
      }
    </c:if>
    <c:if test="${empty motorbike}">
      // trang thêm mới: default trạng thái 'available'
      const statusSel = document.getElementById('status');
      if (statusSel) statusSel.value = 'available';
    </c:if>
  })();
</script>


<body class="admin">

<!-- Sidebar -->
<aside class="sidebar">
  <div class="brand"><div class="brand-logo"><i class="fas fa-motorcycle"></i></div><h1>RideNow Admin</h1></div>
  <nav class="sidebar-nav">
    <a href="${pageContext.request.contextPath}/admin/dashboard" class="nav-item"><i class="fas fa-tachometer-alt"></i><span>Dashboard</span></a>
    <a href="${pageContext.request.contextPath}/admin/partners" class="nav-item"><i class="fas fa-handshake"></i><span>Partners</span></a>
    <a href="${pageContext.request.contextPath}/admin/customers" class="nav-item"><i class="fas fa-users"></i><span>Customers</span></a>
    <a href="${pageContext.request.contextPath}/admin/bikes" class="nav-item active"><i class="fas fa-motorcycle"></i><span>Motorbikes</span></a>
    <a href="${pageContext.request.contextPath}/admin/orders" class="nav-item"><i class="fas fa-clipboard-list"></i><span>Orders</span></a>
    <a href="${pageContext.request.contextPath}/adminpickup" class="nav-item"><i class="fas fa-shipping-fast"></i><span>Vehicle Pickup</span></a>
    <a href="${pageContext.request.contextPath}/adminreturn" class="nav-item"><i class="fas fa-undo-alt"></i><span>Vehicle Return</span></a>
    <a href="${pageContext.request.contextPath}/adminreturns" class="nav-item"><i class="fas fa-clipboard-check"></i><span>Verify & Refund</span></a>
    <a href="${pageContext.request.contextPath}/admin/reports" class="nav-item"><i class="fas fa-chart-bar"></i><span>Reports</span></a>
    <a href="${pageContext.request.contextPath}/admin/feedback" class="nav-item"><i class="fas fa-comment-alt"></i><span>Feedback</span></a>
    <a href="${pageContext.request.contextPath}/logout" class="nav-item logout"><i class="fas fa-sign-out-alt"></i><span>Logout</span></a>
  </nav>
</aside>

<!-- Main -->
<main class="content">
  <header class="content-header">
    <div class="header-left">
      <h1>${empty motorbike ? 'Thêm Xe Mới' : 'Sửa Thông Tin Xe'}</h1>
      <div class="breadcrumb">
        <span>Admin</span><i class="fas fa-chevron-right"></i>
        <span><a href="${pageContext.request.contextPath}/admin/bikes" style="color:inherit;text-decoration:none">Motorbikes</a></span>
        <i class="fas fa-chevron-right"></i><span class="active">${empty motorbike ? 'Thêm mới' : 'Sửa'}</span>
      </div>
    </div>
    <div class="header-right"><div class="user-profile"><div class="user-avatar"><i class="fas fa-user-circle"></i></div><span>Administrator</span></div></div>
  </header>
      
      <!-- THÊM DEBUG SECTION ẨN -->
<div style="display: none; background: #f0f0f0; padding: 10px; margin: 10px 0; border: 1px solid #ccc;">
    <h3>DEBUG INFO</h3>
    <p>Partners count: ${partners.size()}</p>
    <p>BikeTypes count: ${bikeTypes.size()}</p>
    <c:forEach var="partner" items="${partners}">
        <p>Partner: ID=${partner.partnerId}, Name=${partner.fullname}</p>
    </c:forEach>
    <c:if test="${not empty motorbike}">
        <p>Editing Bike ID: ${motorbike.bikeId}</p>
    </c:if>
</div>
      
  <div class="mb-form-container">
    <c:set var="ctx" value="${pageContext.request.contextPath}" />

    <!-- Gallery -->
    <div class="mb-form-panel" style="margin-bottom:20px;">
      <div class="mb-form-header"><h2>Hình ảnh xe</h2></div>
      <div class="mb-form-body">
        <div class="upload-area" id="uploadArea">
          <div class="upload-icon"><i class="fas fa-cloud-upload-alt"></i></div>
          <div class="upload-text">Kéo thả ảnh vào đây hoặc click để chọn</div>
          <div class="upload-hint">Hỗ trợ JPG (tối đa 6 ảnh)</div>
          <input type="file" id="fileInput" multiple accept=".jpg,.jpeg" style="display:none">
        </div>
        <div class="upload-preview" id="uploadPreview"></div>

        <c:if test="${not empty motorbike}">
          <c:choose>
            <c:when test="${not empty motorbike.typeName && fn:contains(fn:toLowerCase(motorbike.typeName), 'số')}">
              <c:set var="imgFolder" value="xe-so"/>
            </c:when>
            <c:when test="${not empty motorbike.typeName && fn:contains(fn:toLowerCase(motorbike.typeName), 'ga')}">
              <c:set var="imgFolder" value="xe-ga"/>
            </c:when>
            <c:when test="${not empty motorbike.typeName && (fn:contains(fn:toLowerCase(motorbike.typeName), 'pkl') || fn:contains(fn:toLowerCase(motorbike.typeName), 'phân khối'))}">
              <c:set var="imgFolder" value="xe-pkl"/>
            </c:when>
            <c:when test="${motorbike.typeId == 1}"><c:set var="imgFolder" value="xe-so"/></c:when>
            <c:when test="${motorbike.typeId == 2}"><c:set var="imgFolder" value="xe-ga"/></c:when>
            <c:when test="${motorbike.typeId == 3}"><c:set var="imgFolder" value="xe-pkl"/></c:when>
            <c:otherwise><c:set var="imgFolder" value="khac"/></c:otherwise>
          </c:choose>

          <div class="gallery" id="gallery">
            <div class="gallery-main">
              <img id="mainImg"
                   src="${ctx}/images/bike/${imgFolder}/${motorbike.bikeId}/1.jpg"
                   alt="${fn:escapeXml(motorbike.bikeName)}"
                   loading="eager"
                   onerror="this.onerror=null;this.src='${ctx}/images/bike_placeholder.jpg';">
              <div class="gallery-nav" id="navBtns" style="display:none">
                <button type="button" class="gallery-btn" id="prevBtn" aria-label="Ảnh trước"><i class="fa-solid fa-chevron-left"></i></button>
                <button type="button" class="gallery-btn" id="nextBtn" aria-label="Ảnh sau"><i class="fa-solid fa-chevron-right"></i></button>
              </div>
            </div>
            <div class="thumbs" id="thumbs"></div>
          </div>

          <div class="image-actions">
            <button type="button" class="mb-btn mb-btn-danger" id="deleteAllBtn" style="display:none">
              <i class="fas fa-trash"></i> Xóa tất cả ảnh
            </button>
          </div>
        </c:if>
      </div>
    </div>

    <!-- Form -->
    <div class="mb-form-panel">
      <div class="mb-form-header"><h2>Thông tin Xe Máy</h2></div>
      <div class="mb-form-body">
        <form method="post" id="mainForm"
              action="${pageContext.request.contextPath}/admin/bikes?action=${empty motorbike ? 'create' : 'update'}"
              enctype="multipart/form-data">
          <c:if test="${not empty motorbike}">
            <input type="hidden" name="bikeId" value="${motorbike.bikeId}">
          </c:if>
          <input type="hidden" name="deletedImages" id="deletedImages" value="">

          <div class="mb-form-grid">
            <div class="mb-form-group">
              <label for="bikeName" class="mb-label required">Tên Xe</label>
              <input type="text" id="bikeName" name="bikeName" class="mb-input" value="${motorbike.bikeName}" required placeholder="Nhập tên xe">
            </div>

            <div class="mb-form-group">
                <label for="licensePlate" class="mb-label required">Biển Số</label>
                <input
                    type="text"
                    id="licensePlate"
                    name="licensePlate"
                    class="mb-input"
                    value="${motorbike.licensePlate}"
                    required
                    placeholder="VD: 43E1-68932"
                    inputmode="latin"
                    maxlength="10"
                    pattern="^\d{2}[A-Z]\d-\d{5}$"
                    title="Định dạng: 2 số + 1 chữ cái (A-Z) + 1 số + dấu '-' + 5 số. Ví dụ: 43E1-68932"
                >
                <div class="mb-hint">Định dạng bắt buộc: <b>NNLN-NNNNN</b> (N=số, L=chữ A-Z). Ví dụ: <code>43E1-68932</code></div>
                <small id="plateError" style="color:#ef4444; display:none;">Biển số không đúng định dạng. Ví dụ hợp lệ: 43E1-68932</small>
              </div>


            <div class="mb-form-group">
                <label for="typeId" class="mb-label required">Loại Xe</label>
                <select id="typeId" name="typeId" class="mb-select" required>
                    <option value="">Chọn loại xe</option>
                    <c:forEach var="type" items="${bikeTypes}">
                        <option value="${type.typeId}" 
                            <c:if test="${not empty motorbike && motorbike.typeId == type.typeId}">selected</c:if>>
                            ${type.typeName}
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="mb-form-group">
              <label for="pricePerDay" class="mb-label required">Giá Thuê/ngày (VND)</label>
              <input type="number" id="pricePerDay" name="pricePerDay" class="mb-input"
                     value="${motorbike.pricePerDay}" step="1000" min="0" required placeholder="Nhập giá thuê">
            </div>

            <div class="mb-form-group">
                <label for="status" class="mb-label required">Trạng thái</label>
                <select id="status" name="status" class="mb-select" required>
                    <option value="available" 
                        <c:if test="${not empty motorbike && motorbike.status == 'available'}">selected</c:if>>
                        Có sẵn
                    </option>
                    <option value="rented" 
                        <c:if test="${not empty motorbike && motorbike.status == 'rented'}">selected</c:if>>
                        Đã thuê
                    </option>
                    <option value="maintenance" 
                        <c:if test="${not empty motorbike && motorbike.status == 'maintenance'}">selected</c:if>>
                        Bảo trì
                    </option>
                </select>
            </div>

            <!-- Chủ sở hữu: chỉ khi thêm mới -->
            <c:if test="${empty motorbike}">
              <div class="mb-form-group full">
                <label class="mb-label required">Chủ Sở Hữu</label>
                <div class="mb-radio-group">
                  <label class="mb-radio-option">
                    <input type="radio" name="ownerType" value="partner" id="ownerPartner" checked>
                    <span>Đối tác</span>
                  </label>
                  <label class="mb-radio-option">
                    <input type="radio" name="ownerType" value="admin" id="ownerAdmin">
                    <span>Cửa hàng Admin</span>
                  </label>
                </div>

                <!-- Đối tác -->
            
                <div id="partnerSelection" class="mb-owner-selection ${(empty prefill_ownerType or prefill_ownerType eq 'partner') ? 'active' : ''}">
                    <label for="partnerId" class="mb-label required">Chọn Đối tác</label>
                    <select id="partnerId" name="partnerId" class="mb-select" 
                            ${(empty prefill_ownerType or prefill_ownerType eq 'partner') ? 'required' : ''}>
                        <option value="">Chọn đối tác</option>
                        <c:forEach var="partner" items="${partners}">
                            <option value="${partner.partnerId}"
                                ${prefill_partnerId eq partner.partnerId ? 'selected' : ''}>
                                ${partner.fullname} <!-- GIỮ NGUYÊN fullname -->
                            </option>
                        </c:forEach>
                    </select>
                    <c:if test="${empty partners}">
                        <p style="color: red; font-size: 12px;">⚠️ Không có đối tác nào trong hệ thống</p>
                    </c:if>
                </div>

                <!-- Cửa hàng Admin (cố định) -->
                <div id="storeSelection" class="mb-owner-selection">
                  <label class="mb-label">Cửa hàng</label>
                  <div class="mb-static-field">
                    <span class="mb-pill"><i class="fa-solid fa-store"></i> Cửa hàng Admin (mặc định)</span>
                  </div>
                  <input type="hidden" name="storeId" id="storeId" value="1" />
                  <div class="mb-hint">Hệ thống chỉ có 1 cửa hàng. Không cần chọn.</div>
                </div>
              </div>
            </c:if>

            <div class="mb-form-group full">
              <label for="description" class="mb-label">Mô tả</label>
              <textarea id="description" name="description" class="mb-textarea" placeholder="Nhập mô tả về xe...">${motorbike.description}</textarea>
            </div>
          </div>

          <div class="mb-btn-group">
            <a href="${pageContext.request.contextPath}/admin/bikes" class="mb-btn mb-btn-outline"><i class="fas fa-arrow-left"></i> Quay lại</a>
            <button type="submit" class="mb-btn mb-btn-primary"><i class="fas fa-save"></i> ${empty motorbike ? 'Thêm Xe' : 'Cập nhật'}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</main>

<script>
  // Biến toàn cục
  let uploadedFiles = [];
  let deletedImageIndexes = [];
  let currentImages = [];

  document.addEventListener('DOMContentLoaded', function() {
    // Toggle chủ sở hữu (chỉ khi thêm mới)
    const ownerTypeRadios = document.querySelectorAll('input[name="ownerType"]');
    const partnerSelection = document.getElementById('partnerSelection');
    const storeSelection = document.getElementById('storeSelection');
    const partnerIdEl = document.getElementById('partnerId');

    function toggleOwnerSelection() {
      const selectedOwner = document.querySelector('input[name="ownerType"]:checked').value;
      if (selectedOwner === 'partner') {
        partnerSelection?.classList.add('active');
        storeSelection?.classList.remove('active');
        if (partnerIdEl) partnerIdEl.required = true;
      } else {
        partnerSelection?.classList.remove('active');
        storeSelection?.classList.add('active');
        if (partnerIdEl) { partnerIdEl.required = false; partnerIdEl.value = ''; }
      }
    }
    if (ownerTypeRadios.length > 0) {
      ownerTypeRadios.forEach(radio => radio.addEventListener('change', toggleOwnerSelection));
      toggleOwnerSelection();
    }

    // Làm tròn giá bội 1.000
    const priceInput = document.getElementById('pricePerDay');
    if (priceInput) {
      priceInput.addEventListener('blur', function() {
        const value = parseInt(this.value);
        if (!isNaN(value) && value >= 0) this.value = Math.round(value / 1000) * 1000;
      });
    }

    <c:if test="${not empty motorbike}">initGallery();</c:if>
    initUploadHandler();
  });

  // ===== Upload Handler =====
  function initUploadHandler() {
    const uploadArea = document.getElementById('uploadArea');
    const fileInput = document.getElementById('fileInput');
    const uploadPreview = document.getElementById('uploadPreview');
    const mainForm = document.getElementById('mainForm');

    uploadArea.addEventListener('click', () => fileInput.click());
    uploadArea.addEventListener('dragover', (e) => { e.preventDefault(); uploadArea.classList.add('dragover'); });
    uploadArea.addEventListener('dragleave', () => uploadArea.classList.remove('dragover'));
    uploadArea.addEventListener('drop', (e) => { e.preventDefault(); uploadArea.classList.remove('dragover'); handleFiles(e.dataTransfer.files); });
    fileInput.addEventListener('change', (e) => handleFiles(e.target.files));

    function handleFiles(files) {
      const maxFiles = 6;
      const totalSlots = maxFiles - deletedImageIndexes.length;
      const remainingSlots = totalSlots - uploadedFiles.length;
      if (files.length > remainingSlots) {
        alert(`Chỉ có thể upload tối đa ${maxFiles} ảnh. Bạn đã chọn ${files.length} ảnh nhưng chỉ còn ${remainingSlots} slot trống.`);
        return;
      }
      for (let file of files) {
        if (!file.type.startsWith('image/jpeg') && !file.name.toLowerCase().endsWith('.jpg')) { alert('Chỉ chấp nhận file ảnh JPG!'); continue; }
        if (file.size > 5 * 1024 * 1024) { alert('File ảnh không được vượt quá 5MB!'); continue; }
        const reader = new FileReader();
        reader.onload = (e) => { uploadedFiles.push({ file, url: e.target.result, name: file.name, size: (file.size/1024/1024).toFixed(2)+' MB' }); updatePreview(); };
        reader.readAsDataURL(file);
      }
      fileInput.value = '';
    }

    function updatePreview() {
      uploadPreview.innerHTML = '';
      uploadedFiles.forEach((fileData, index) => {
        const el = document.createElement('div');
        el.className = 'upload-preview-item';
        el.innerHTML = `
          <img src="${fileData.url}" alt="Preview">
          <button type="button" class="upload-preview-remove" data-index="${index}"><i class="fas fa-times"></i></button>
          <div class="upload-preview-info"><div>${fileData.name}</div><div>${fileData.size}</div></div>`;
        uploadPreview.appendChild(el);
      });
      document.querySelectorAll('.upload-preview-remove').forEach(btn => {
        btn.addEventListener('click', (e) => { const idx = parseInt(e.currentTarget.getAttribute('data-index')); uploadedFiles.splice(idx, 1); updatePreview(); });
      });
    }

    mainForm.addEventListener('submit', function(e) {
      e.preventDefault();
      const formData = new FormData();
      for (let element of mainForm.elements) {
        if (element.name && element.type !== 'file') {
          if ((element.type === 'checkbox' || element.type === 'radio')) {
            if (element.checked) formData.append(element.name, element.value);
          } else {
            formData.append(element.name, element.value);
          }
        }
      }
      uploadedFiles.forEach(fd => formData.append('images', fd.file));
      <c:if test="${not empty motorbike}">formData.append('deletedImages', deletedImageIndexes.join(','));</c:if>
      const action = '${empty motorbike ? "create" : "update"}';
      formData.append('action', action);

      fetch(mainForm.action, { method: 'POST', body: formData })
        .then(res => { if (res.ok) window.location.href='${pageContext.request.contextPath}/admin/bikes?success='+action; else alert('Có lỗi xảy ra khi lưu thông tin xe!'); })
        .catch(err => { console.error(err); alert('Có lỗi xảy ra khi lưu thông tin xe!'); });
    });
  }

  // ===== Gallery (khi sửa) =====
  function initGallery(){
    const mainImg = document.getElementById('mainImg');
    const thumbsWrap = document.getElementById('thumbs');
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');
    const navBtns = document.getElementById('navBtns');
    const deleteAllBtn = document.getElementById('deleteAllBtn');

    const base = "${pageContext.request.contextPath}/images/bike/${imgFolder}/${motorbike.bikeId}";
    const validImages = [];

    function checkImageExists(url, index) {
      return new Promise((resolve) => {
        const img = new Image();
        img.onload = () => resolve({url, index, exists: true});
        img.onerror = () => resolve({url, index, exists: false});
        img.src = url;
      });
    }

    async function loadImages() {
      const promises = [];
      for (let i = 1; i <= 6; i++) promises.push(checkImageExists(base + '/' + i + '.jpg', i));
      const results = await Promise.all(promises);
      results.forEach(r => { if (r.exists) { validImages.push({url:r.url, index:r.index}); currentImages.push(r.index); }});
      validImages.sort((a,b)=>a.index-b.index); currentImages.sort((a,b)=>a-b);
      setupGallery();
    }

    function setupGallery() {
      if (validImages.length === 0) {
        mainImg.src = "${pageContext.request.contextPath}/images/bike_placeholder.jpg";
        navBtns.style.display='none'; thumbsWrap.style.display='none'; deleteAllBtn.style.display='none'; return;
      }
      mainImg.src = validImages[0].url;
      if (validImages.length === 1){ navBtns.style.display='none'; thumbsWrap.style.display='none'; }
      else { navBtns.style.display='flex'; thumbsWrap.style.display='grid'; }
      if (validImages.length > 0) deleteAllBtn.style.display='inline-flex';

      let html='';
      for (let i=0;i<validImages.length;i++){
        const active = i===0?'active':'';
        html += '<div class="thumb '+active+'" data-index="'+i+'">'+
                '<button type="button" class="thumb-delete" data-index="'+validImages[i].index+'"><i class="fas fa-times"></i></button>'+
                '<img src="'+validImages[i].url+'" alt="Ảnh '+(i+1)+'"></div>';
      }
      thumbsWrap.innerHTML = html;

      let currentIndex = 0;

      document.querySelectorAll('.thumb-delete').forEach(btn=>{
        btn.addEventListener('click', function(e){
          e.stopPropagation();
          const imageIndex = parseInt(this.getAttribute('data-index'));
          if (confirm('Bạn có chắc muốn xóa ảnh này?')) deleteImage(imageIndex);
        });
      });

      thumbsWrap.addEventListener('click', function(e){
        const thumb = e.target.closest('.thumb');
        if (thumb && !e.target.closest('.thumb-delete')) {
          currentIndex = parseInt(thumb.getAttribute('data-index'));
          showImage(currentIndex);
        }
      });

      prevBtn.addEventListener('click', function(){ currentIndex = (currentIndex - 1 + validImages.length) % validImages.length; showImage(currentIndex); });
      nextBtn.addEventListener('click', function(){ currentIndex = (currentIndex + 1) % validImages.length; showImage(currentIndex); });

      deleteAllBtn.addEventListener('click', function(){
        if (confirm('Bạn có chắc muốn xóa TẤT CẢ ảnh của xe này?')) {
          const allIndexes = validImages.map(img => img.index);
          deletedImageIndexes = [...deletedImageIndexes, ...allIndexes];
          validImages.length = 0; currentImages.length = 0;
          setupGallery();
          mainImg.src = "${pageContext.request.contextPath}/images/bike_placeholder.jpg";
          deleteAllBtn.style.display = 'none';
        }
      });

      function showImage(index){
        mainImg.src = validImages[index].url;
        const allThumbs = thumbsWrap.querySelectorAll('.thumb');
        allThumbs.forEach((t,j)=>{ if(j===index) t.classList.add('active'); else t.classList.remove('active'); });
      }
    }

    function deleteImage(imageIndex){
      deletedImageIndexes.push(imageIndex);
      const pos = validImages.findIndex(img => img.index === imageIndex);
      if (pos !== -1){
        validImages.splice(pos,1);
        const ci = currentImages.indexOf(imageIndex);
        if (ci !== -1) currentImages.splice(ci,1);
        setupGallery();
        if (validImages.length === 0){
          mainImg.src = "${pageContext.request.contextPath}/images/bike_placeholder.jpg";
          deleteAllBtn.style.display='none';
        }
      }
    }

    loadImages();
  }
</script>
</body>
</html>
