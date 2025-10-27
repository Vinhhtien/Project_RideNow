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

  <style>
    :root{
      --mb-primary:#3b82f6; --mb-primary-600:#2563eb; --mb-primary-50:#eef2ff;
      --mb-success:#10b981; --mb-success-50:#ecfdf5;
      --mb-warn:#f59e0b; --mb-warn-50:#fffbeb;
      --mb-danger:#ef4444; --mb-danger-50:#fff1f2;
      --mb-ink:#0f172a; --mb-muted:#64748b;
      --mb-bg:#f8fafc; --mb-card:#ffffff; --mb-line:#e5e7eb;
      --mb-radius:12px; --mb-shadow:0 8px 20px rgba(2,6,23,.06);
    }
    *{box-sizing:border-box}
    html,body{height:100%}
    body.mb-admin{
      margin:0;background:var(--mb-bg);color:var(--mb-ink);
      font-family:Inter,system-ui,-apple-system,"Segoe UI",Roboto,Arial,sans-serif;
      display:flex;min-height:100vh;
    }

    /* ===== Sidebar ===== */
    .mb-sidebar{width:260px;background:#1e293b;color:#e5e7eb;display:flex;flex-direction:column;box-shadow:var(--mb-shadow)}
    .mb-brand{display:flex;align-items:center;padding:18px;border-bottom:1px solid rgba(255,255,255,.06)}
    .mb-brand-logo{width:42px;height:42px;border-radius:10px;display:grid;place-items:center;color:#fff;
      background:linear-gradient(135deg,var(--mb-primary),#60a5fa);margin-right:12px}
    .mb-brand h1{margin:0;font-size:18px;font-weight:800}
    .mb-nav{padding:12px 8px;display:flex;flex-direction:column}
    .mb-nav a{display:flex;align-items:center;gap:12px;padding:12px 14px;border-radius:10px;
      color:#cbd5e1;text-decoration:none;transition:.15s}
    .mb-nav a i{width:20px;text-align:center}
    .mb-nav a:hover{background:rgba(255,255,255,.06);color:#fff}
    .mb-nav a.active{background:rgba(59,130,246,.16);color:#fff;outline:1px solid rgba(59,130,246,.25)}

    /* ===== Content / Header ===== */
    .mb-content{flex:1;display:flex;flex-direction:column}
    .mb-header{background:var(--mb-card);padding:14px 22px;display:flex;justify-content:space-between;align-items:center;box-shadow:0 1px 0 var(--mb-line);position:sticky;top:0;z-index:5}
    .mb-header h1{margin:2px 0;font-size:22px;font-weight:800}
    .mb-breadcrumb{display:flex;align-items:center;gap:8px;color:#6b7280;font-size:13px}
    .mb-breadcrumb .active{color:var(--mb-primary);font-weight:600}
    .mb-user{display:flex;align-items:center;gap:10px}
    .mb-avatar{width:40px;height:40px;border-radius:50%;display:grid;place-items:center;color:#4f46e5;background:#eef2ff}

    /* ===== Form Container ===== */
    .mb-form-container{max-width:1000px;margin:18px 22px}
    .mb-form-panel{background:var(--mb-card);border:1px solid var(--mb-line);border-radius:var(--mb-radius);box-shadow:var(--mb-shadow);overflow:hidden}
    .mb-form-header{padding:16px 18px;border-bottom:1px solid var(--mb-line);background:#f8fafc}
    .mb-form-header h2{margin:0;font-size:18px;font-weight:800}
    .mb-form-body{padding:24px}

    /* ===== Form Elements ===== */
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

    /* ===== Gallery Styles ===== */
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

    /* ===== Upload Styles ===== */
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

    /* ===== Buttons ===== */
    .mb-btn-group{display:flex;gap:12px;justify-content:flex-end;margin-top:32px;padding-top:20px;border-top:1px solid var(--mb-line)}
    .mb-btn{display:inline-flex;align-items:center;gap:8px;padding:12px 20px;border-radius:10px;font-weight:700;
      border:1px solid transparent;cursor:pointer;transition:.15s;text-decoration:none;font-size:14px}
    .mb-btn-primary{background:var(--mb-primary);color:#fff}
    .mb-btn-primary:hover{background:var(--mb-primary-600);transform:translateY(-1px)}
    .mb-btn-secondary{background:#64748b;color:#fff}
    .mb-btn-secondary:hover{background:#475569;transform:translateY(-1px)}
    .mb-btn-outline{background:transparent;border-color:var(--mb-line);color:var(--mb-muted)}
    .mb-btn-outline:hover{background:#f8fafc;color:var(--mb-ink)}
    .mb-btn-danger{background:var(--mb-danger);color:#fff}
    .mb-btn-danger:hover{background:#dc2626;transform:translateY(-1px)}

    /* ===== Image Actions ===== */
    .image-actions{ display:flex; gap:12px; margin-top:16px; }
    
    /* ===== Responsive ===== */
    @media (max-width:1100px){ .mb-sidebar{width:86px}.mb-brand h1,.mb-nav a span{display:none} .mb-nav a{justify-content:center}}
    @media (max-width:768px){
      .mb-form-grid{grid-template-columns:1fr}
      .mb-form-container{margin:14px}
      .mb-header{padding:12px 16px}
      .mb-btn-group{flex-direction:column}
      .thumbs{ grid-template-columns:repeat(4,1fr); }
      .gallery-main img{ height:250px; }
    }
  </style>
</head>
<body class="mb-admin">

<!-- Sidebar -->
<aside class="mb-sidebar">
  <div class="mb-brand">
    <div class="mb-brand-logo"><i class="fas fa-motorcycle"></i></div>
    <h1>RideNow Admin</h1>
  </div>
  <nav class="mb-nav">
    <a href="${pageContext.request.contextPath}/admin/dashboard"><i class="fas fa-tachometer-alt"></i><span>Dashboard</span></a>
    <a href="${pageContext.request.contextPath}/admin/partners"><i class="fas fa-handshake"></i><span>Partners</span></a>
    <a href="${pageContext.request.contextPath}/admin/customers"><i class="fas fa-users"></i><span>Customers</span></a>
    <a class="active" href="${pageContext.request.contextPath}/admin/bikes"><i class="fas fa-motorcycle"></i><span>Motorbikes</span></a>
    <a href="${pageContext.request.contextPath}/admin/orders"><i class="fas fa-clipboard-list"></i><span>Orders</span></a>
  </nav>
</aside>

<!-- Main -->
<main class="mb-content">
  <header class="mb-header">
    <div>
      <h1>${empty motorbike ? 'Thêm Xe Mới' : 'Sửa Thông Tin Xe'}</h1>
      <div class="mb-breadcrumb">
        <span>Admin</span><i class="fas fa-chevron-right"></i>
        <span><a href="${pageContext.request.contextPath}/admin/bikes" style="color:inherit;text-decoration:none">Motorbikes</a></span>
        <i class="fas fa-chevron-right"></i>
        <span class="active">${empty motorbike ? 'Thêm mới' : 'Sửa'}</span>
      </div>
    </div>
    <div class="mb-user">
      <div class="mb-avatar"><i class="fas fa-user"></i></div>
      <span>Administrator</span>
    </div>
  </header>

  <!-- Form Container -->
  <div class="mb-form-container">
    <c:set var="ctx" value="${pageContext.request.contextPath}" />
    
    <!-- Gallery Section - Hiển thị cả khi thêm mới và sửa -->
    <div class="mb-form-panel" style="margin-bottom: 20px;">
      <div class="mb-form-header">
        <h2>Hình ảnh xe</h2>
      </div>
      <div class="mb-form-body">
        
        <!-- Upload Area - Hiển thị cả khi thêm mới và sửa -->
        <div class="upload-area" id="uploadArea">
          <div class="upload-icon">
            <i class="fas fa-cloud-upload-alt"></i>
          </div>
          <div class="upload-text">Kéo thả ảnh vào đây hoặc click để chọn</div>
          <div class="upload-hint">Hỗ trợ JPG (tối đa 6 ảnh)</div>
          <input type="file" id="fileInput" multiple accept=".jpg,.jpeg" style="display:none">
        </div>

        <!-- Preview Area cho ảnh mới upload -->
        <div class="upload-preview" id="uploadPreview"></div>

        <!-- Gallery hiện tại - Chỉ hiển thị khi sửa xe -->
        <c:if test="${not empty motorbike}">
          <!-- Xác định thư mục ảnh theo loại xe -->
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
                <button type="button" class="gallery-btn" id="prevBtn" aria-label="Ảnh trước">
                  <i class="fa-solid fa-chevron-left"></i>
                </button>
                <button type="button" class="gallery-btn" id="nextBtn" aria-label="Ảnh sau">
                  <i class="fa-solid fa-chevron-right"></i>
                </button>
              </div>
            </div>
            <div class="thumbs" id="thumbs"></div>
          </div>

          <!-- Nút xóa tất cả ảnh - Chỉ hiển thị khi sửa xe -->
          <div class="image-actions">
            <button type="button" class="mb-btn mb-btn-danger" id="deleteAllBtn" style="display:none">
              <i class="fas fa-trash"></i> Xóa tất cả ảnh
            </button>
          </div>
        </c:if>
      </div>
    </div>

    <!-- Form Section -->
    <div class="mb-form-panel">
      <div class="mb-form-header">
        <h2>Thông tin Xe Máy</h2>
      </div>
      
      <div class="mb-form-body">
        <form method="post" id="mainForm"
              action="${pageContext.request.contextPath}/admin/bikes?action=${empty motorbike ? 'create' : 'update'}"
              enctype="multipart/form-data">
          
          <c:if test="${not empty motorbike}">
            <input type="hidden" name="bikeId" value="${motorbike.bikeId}">
          </c:if>

          <!-- Hidden input để lưu danh sách ảnh cần xóa -->
          <input type="hidden" name="deletedImages" id="deletedImages" value="">

          <div class="mb-form-grid">
            <!-- Tên Xe -->
            <div class="mb-form-group">
              <label for="bikeName" class="mb-label required">Tên Xe</label>
              <input type="text" id="bikeName" name="bikeName" class="mb-input" 
                     value="${motorbike.bikeName}" required placeholder="Nhập tên xe">
            </div>

            <!-- Biển Số -->
            <div class="mb-form-group">
              <label for="licensePlate" class="mb-label required">Biển Số</label>
              <input type="text" id="licensePlate" name="licensePlate" class="mb-input" 
                     value="${motorbike.licensePlate}" required placeholder="Nhập biển số">
            </div>

            <!-- Loại Xe -->
            <div class="mb-form-group">
              <label for="typeId" class="mb-label required">Loại Xe</label>
              <select id="typeId" name="typeId" class="mb-select" required>
                <option value="">Chọn loại xe</option>
                <c:forEach var="type" items="${bikeTypes}">
                  <option value="${type.typeId}" 
                          ${motorbike.typeId == type.typeId ? 'selected' : ''}>
                    ${type.typeName}
                  </option>
                </c:forEach>
              </select>
            </div>

            <!-- Giá Thuê/ngày -->
            <div class="mb-form-group">
              <label for="pricePerDay" class="mb-label required">Giá Thuê/ngày (VND)</label>
              <input type="number" id="pricePerDay" name="pricePerDay" class="mb-input" 
                     value="${motorbike.pricePerDay}" step="1000" min="0" required 
                     placeholder="Nhập giá thuê">
            </div>

            <!-- Trạng thái -->
            <div class="mb-form-group">
              <label for="status" class="mb-label required">Trạng thái</label>
              <select id="status" name="status" class="mb-select" required>
                <option value="available" ${motorbike.status == 'available' ? 'selected' : ''}>Có sẵn</option>
                <option value="rented" ${motorbike.status == 'rented' ? 'selected' : ''}>Đã thuê</option>
                <option value="maintenance" ${motorbike.status == 'maintenance' ? 'selected' : ''}>Bảo trì</option>
              </select>
            </div>

            <!-- Chủ Sở Hữu (chỉ hiển thị khi thêm mới) -->
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

                <!-- Lựa chọn Đối tác -->
                <div id="partnerSelection" class="mb-owner-selection active">
                  <label for="partnerId" class="mb-label required">Chọn Đối tác</label>
                  <select id="partnerId" name="partnerId" class="mb-select">
                    <option value="">Chọn đối tác</option>
                    <c:forEach var="partner" items="${partners}">
                      <option value="${partner.partnerId}">${partner.fullname}</option>
                    </c:forEach>
                  </select>
                </div>

                <!-- Lựa chọn Cửa hàng -->
                <div id="storeSelection" class="mb-owner-selection">
                  <label for="storeId" class="mb-label required">Chọn Cửa hàng</label>
                  <select id="storeId" name="storeId" class="mb-select">
                    <option value="">Chọn cửa hàng</option>
                    <c:forEach var="store" items="${stores}">
                      <option value="${store.storeId}">${store.storeName}</option>
                    </c:forEach>
                  </select>
                </div>
              </div>
            </c:if>

            <!-- Mô tả -->
            <div class="mb-form-group full">
              <label for="description" class="mb-label">Mô tả</label>
              <textarea id="description" name="description" class="mb-textarea" 
                        placeholder="Nhập mô tả về xe...">${motorbike.description}</textarea>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="mb-btn-group">
            <a href="${pageContext.request.contextPath}/admin/bikes" class="mb-btn mb-btn-outline">
              <i class="fas fa-arrow-left"></i> Quay lại
            </a>
            <button type="submit" class="mb-btn mb-btn-primary">
              <i class="fas fa-save"></i> ${empty motorbike ? 'Thêm Xe' : 'Cập nhật'}
            </button>
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
  let currentImages = []; // Lưu trữ thông tin ảnh hiện tại

  document.addEventListener('DOMContentLoaded', function() {
    // Xử lý hiển thị form chọn chủ sở hữu (chỉ khi thêm mới)
    const ownerTypeRadios = document.querySelectorAll('input[name="ownerType"]');
    const partnerSelection = document.getElementById('partnerSelection');
    const storeSelection = document.getElementById('storeSelection');

    function toggleOwnerSelection() {
      const selectedOwner = document.querySelector('input[name="ownerType"]:checked').value;
      
      if (selectedOwner === 'partner') {
        partnerSelection.classList.add('active');
        storeSelection.classList.remove('active');
        document.getElementById('partnerId').required = true;
        document.getElementById('storeId').required = false;
      } else {
        partnerSelection.classList.remove('active');
        storeSelection.classList.add('active');
        document.getElementById('partnerId').required = false;
        document.getElementById('storeId').required = true;
      }
    }

    // Chỉ thêm event listener nếu có radio buttons (trường hợp thêm mới)
    if (ownerTypeRadios.length > 0) {
      ownerTypeRadios.forEach(radio => {
        radio.addEventListener('change', toggleOwnerSelection);
      });
      toggleOwnerSelection(); // Khởi tạo trạng thái ban đầu
    }

    // Format số tiền khi nhập
    const priceInput = document.getElementById('pricePerDay');
    if (priceInput) {
      priceInput.addEventListener('blur', function() {
        const value = parseInt(this.value);
        if (!isNaN(value) && value >= 0) {
          this.value = Math.round(value / 1000) * 1000; // Làm tròn đến nghìn
        }
      });
    }

    // Khởi tạo gallery nếu có xe (trang sửa)
    <c:if test="${not empty motorbike}">
      initGallery();
    </c:if>

    // Xử lý upload ảnh
    initUploadHandler();
  });

  // ===== Upload Handler =====
  function initUploadHandler() {
    const uploadArea = document.getElementById('uploadArea');
    const fileInput = document.getElementById('fileInput');
    const uploadPreview = document.getElementById('uploadPreview');
    const mainForm = document.getElementById('mainForm');

    // Click upload area
    uploadArea.addEventListener('click', () => fileInput.click());

    // Drag and drop
    uploadArea.addEventListener('dragover', (e) => {
      e.preventDefault();
      uploadArea.classList.add('dragover');
    });

    uploadArea.addEventListener('dragleave', () => {
      uploadArea.classList.remove('dragover');
    });

    uploadArea.addEventListener('drop', (e) => {
      e.preventDefault();
      uploadArea.classList.remove('dragover');
      const files = e.dataTransfer.files;
      handleFiles(files);
    });

    // File input change
    fileInput.addEventListener('change', (e) => {
      handleFiles(e.target.files);
    });

    function handleFiles(files) {
      const maxFiles = 6;
      const totalSlots = maxFiles - deletedImageIndexes.length;
      const remainingSlots = totalSlots - uploadedFiles.length;
      
      if (files.length > remainingSlots) {
        alert(`Chỉ có thể upload tối đa ${maxFiles} ảnh. Bạn đã chọn ${files.length} ảnh nhưng chỉ còn ${remainingSlots} slot trống.`);
        return;
      }

      for (let file of files) {
        // Chỉ chấp nhận file JPG
        if (!file.type.startsWith('image/jpeg') && !file.name.toLowerCase().endsWith('.jpg')) {
          alert('Chỉ chấp nhận file ảnh JPG!');
          continue;
        }

        if (file.size > 5 * 1024 * 1024) { // 5MB
          alert('File ảnh không được vượt quá 5MB!');
          continue;
        }

        const reader = new FileReader();
        reader.onload = (e) => {
          const fileData = {
            file: file,
            url: e.target.result,
            name: file.name,
            size: (file.size / 1024 / 1024).toFixed(2) + ' MB'
          };
          uploadedFiles.push(fileData);
          updatePreview();
        };
        reader.readAsDataURL(file);
      }
      
      fileInput.value = '';
    }

    function updatePreview() {
      uploadPreview.innerHTML = '';
      
      uploadedFiles.forEach((fileData, index) => {
        const previewItem = document.createElement('div');
        previewItem.className = 'upload-preview-item';
        previewItem.innerHTML = `
          <img src="${fileData.url}" alt="Preview">
          <button type="button" class="upload-preview-remove" data-index="${index}">
            <i class="fas fa-times"></i>
          </button>
          <div class="upload-preview-info">
            <div>${fileData.name}</div>
            <div>${fileData.size}</div>
          </div>
        `;
        uploadPreview.appendChild(previewItem);
      });

      // Thêm sự kiện xóa preview
      document.querySelectorAll('.upload-preview-remove').forEach(btn => {
        btn.addEventListener('click', (e) => {
          const index = parseInt(e.target.closest('button').getAttribute('data-index'));
          uploadedFiles.splice(index, 1);
          updatePreview();
        });
      });
    }

    // Xử lý submit form
    mainForm.addEventListener('submit', function(e) {
      e.preventDefault();
      
      // Tạo FormData mới
      const formData = new FormData();
      
      // Thêm các tham số form vào FormData
      const formElements = mainForm.elements;
      for (let element of formElements) {
        if (element.name && element.type !== 'file') {
          if (element.type === 'checkbox' || element.type === 'radio') {
            if (element.checked) {
              formData.append(element.name, element.value);
            }
          } else {
            formData.append(element.name, element.value);
          }
        }
      }

      // Thêm các file ảnh mới
      uploadedFiles.forEach((fileData, index) => {
        formData.append('images', fileData.file);
      });

      // Thêm danh sách ảnh cần xóa (chỉ khi sửa xe)
      <c:if test="${not empty motorbike}">
      formData.append('deletedImages', deletedImageIndexes.join(','));
      </c:if>

      // Thêm action parameter
      const action = '${empty motorbike ? "create" : "update"}';
      formData.append('action', action);

      // Debug: hiển thị thông tin FormData
      console.log('Sending FormData:');
      console.log('- Files to upload:', uploadedFiles.length);
      console.log('- Images to delete:', deletedImageIndexes);
      for (let [key, value] of formData.entries()) {
        console.log(key + ': ' + (value instanceof File ? value.name : value));
      }

      // Gửi request bằng fetch
      fetch(mainForm.action, {
        method: 'POST',
        body: formData
      })
      .then(response => {
        if (response.ok) {
          // Chuyển hướng sau khi thành công
          window.location.href = '${pageContext.request.contextPath}/admin/bikes?success=' + action;
        } else {
          alert('Có lỗi xảy ra khi lưu thông tin xe!');
        }
      })
      .catch(error => {
        console.error('Error:', error);
        alert('Có lỗi xảy ra khi lưu thông tin xe!');
      });
    });
  }

  // ===== Gallery với chức năng xóa ảnh =====
  function initGallery(){
    const mainImg = document.getElementById('mainImg');
    const thumbsWrap = document.getElementById('thumbs');
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');
    const navBtns = document.getElementById('navBtns');
    const deleteAllBtn = document.getElementById('deleteAllBtn');

    const base = "${ctx}/images/bike/${imgFolder}/${motorbike.bikeId}";
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
      
      // Lưu cả URL và index
      results.forEach(r => { 
        if (r.exists) {
          validImages.push({url: r.url, index: r.index});
          currentImages.push(r.index); // Lưu index hiện tại
        }
      });

      // Sắp xếp theo index
      validImages.sort((a, b) => a.index - b.index);
      currentImages.sort((a, b) => a - b);

      setupGallery();
    }

    function setupGallery() {
      if (validImages.length === 0) {
        mainImg.src = "${ctx}/images/bike_placeholder.jpg";
        mainImg.alt = "Ảnh không khả dụng";
        if (navBtns) navBtns.style.display = 'none';
        if (thumbsWrap) thumbsWrap.style.display = 'none';
        if (deleteAllBtn) deleteAllBtn.style.display = 'none';
        return;
      }

      mainImg.src = validImages[0].url;

      if (validImages.length === 1) {
        if (navBtns) navBtns.style.display = 'none';
        if (thumbsWrap) thumbsWrap.style.display = 'none';
      } else {
        if (navBtns) navBtns.style.display = 'flex';
        if (thumbsWrap) thumbsWrap.style.display = 'grid';
      }

      // Hiện nút xóa tất cả nếu có ảnh
      if (deleteAllBtn && validImages.length > 0) {
        deleteAllBtn.style.display = 'inline-flex';
      }

      let thumbsHTML = '';
      for (let i = 0; i < validImages.length; i++) {
        const activeClass = i === 0 ? 'active' : '';
        thumbsHTML += '<div class="thumb ' + activeClass + '" data-index="' + i + '">' +
                      '<button type="button" class="thumb-delete" data-index="' + validImages[i].index + '">' +
                      '<i class="fas fa-times"></i></button>' +
                      '<img src="' + validImages[i].url + '" alt="Ảnh ' + (i + 1) + '">' +
                      '</div>';
      }
      if (thumbsWrap) thumbsWrap.innerHTML = thumbsHTML;

      let currentIndex = 0;

      // Xử lý xóa ảnh
      document.querySelectorAll('.thumb-delete').forEach(btn => {
        btn.addEventListener('click', function(e) {
          e.stopPropagation();
          const imageIndex = parseInt(this.getAttribute('data-index'));
          if (confirm('Bạn có chắc muốn xóa ảnh này?')) {
            deleteImage(imageIndex);
          }
        });
      });

      if (thumbsWrap) {
        thumbsWrap.addEventListener('click', function(e) {
          const thumb = e.target.closest('.thumb');
          if (thumb && !e.target.closest('.thumb-delete')) {
            currentIndex = parseInt(thumb.getAttribute('data-index'));
            showImage(currentIndex);
          }
        });
      }

      if (prevBtn) prevBtn.addEventListener('click', function() {
        currentIndex = (currentIndex - 1 + validImages.length) % validImages.length;
        showImage(currentIndex);
      });
      
      if (nextBtn) nextBtn.addEventListener('click', function() {
        currentIndex = (currentIndex + 1) % validImages.length;
        showImage(currentIndex);
      });

      // Xử lý xóa tất cả ảnh
      if (deleteAllBtn) {
        deleteAllBtn.addEventListener('click', function() {
          if (confirm('Bạn có chắc muốn xóa TẤT CẢ ảnh của xe này?')) {
            const allIndexes = validImages.map(img => img.index);
            deletedImageIndexes = [...deletedImageIndexes, ...allIndexes];
            validImages.length = 0;
            currentImages.length = 0;
            setupGallery();
            mainImg.src = "${ctx}/images/bike_placeholder.jpg";
            deleteAllBtn.style.display = 'none';
          }
        });
      }

      function showImage(index) {
        mainImg.src = validImages[index].url;
        if (thumbsWrap) {
          const allThumbs = thumbsWrap.querySelectorAll('.thumb');
          for (let j = 0; j < allThumbs.length; j++) {
            if (j === index) allThumbs[j].classList.add('active'); 
            else allThumbs[j].classList.remove('active');
          }
        }
      }
    }

    function deleteImage(imageIndex) {
      // Thêm vào danh sách xóa
      deletedImageIndexes.push(imageIndex);
      
      // Xóa khỏi gallery hiện tại
      const imageToRemove = validImages.findIndex(img => img.index === imageIndex);
      if (imageToRemove !== -1) {
        validImages.splice(imageToRemove, 1);
        
        // Xóa khỏi currentImages
        const currentIndex = currentImages.indexOf(imageIndex);
        if (currentIndex !== -1) {
          currentImages.splice(currentIndex, 1);
        }
        
        setupGallery();
        
        // Nếu không còn ảnh nào, hiển thị placeholder
        if (validImages.length === 0) {
          mainImg.src = "${ctx}/images/bike_placeholder.jpg";
          if (deleteAllBtn) deleteAllBtn.style.display = 'none';
        }
      }
    }

    loadImages();
  }
</script>
</body>
</html>