<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Tạo Partner - RideNow</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
    <style>
        /* ==========================================
           CSS cho Trang Tạo Partner - MÀU SẮC HÀI HÒA
           Scope: .partner-create-page
           ========================================== */

        .partner-create-page .container {
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
        }

        .partner-create-page .card {
            background: #fff;
            border-radius: 16px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
            border: 1px solid #f1f5f9;
            overflow: hidden;
            margin-bottom: 2rem;
            transition: transform 0.3s ease, box-shadow 0.3s ease;
        }

        .partner-create-page .card:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
        }

        .partner-create-page .card-header {
            padding: 1.5rem 2rem;
            border-bottom: 1px solid #f1f5f9;
            background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
        }

        .partner-create-page .card-title {
            display: flex;
            align-items: center;
            gap: 0.75rem;
            font-size: 1.25rem;
            font-weight: 700;
            color: #1e293b;
            margin: 0;
        }

        .partner-create-page .card-title i {
            color: #6366f1;
            font-size: 1.5rem;
        }

        .partner-create-page .card-body {
            padding: 2rem;
        }

        .partner-create-page .partner-form {
            width: 100%;
        }

        .partner-create-page .form-grid {
            display: grid;
            grid-template-columns: 1fr;
            gap: 1.75rem;
            margin-bottom: 2rem;
        }

        .partner-create-page .form-group {
            display: flex;
            flex-direction: column;
        }

        .partner-create-page .form-label {
            font-weight: 600;
            color: #374151;
            margin-bottom: 0.75rem;
            font-size: 0.9rem;
            display: flex;
            align-items: center;
            gap: 0.25rem;
        }

        .partner-create-page .form-label .required {
            color: #ef4444;
            font-size: 1.2rem;
        }

        .partner-create-page .form-input {
            padding: 1rem 1.25rem;
            border: 2px solid #e5e7eb;
            border-radius: 12px;
            font-size: 1rem;
            transition: all 0.3s ease;
            background: #fff;
            font-family: 'Inter', sans-serif;
        }

        .partner-create-page .form-input:focus {
            outline: none;
            border-color: #6366f1;
            box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.15);
            background: #fafbff;
        }

        .partner-create-page .form-input.valid {
            border-color: #10b981;
            box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.15);
        }

        .partner-create-page .form-input.invalid {
            border-color: #ef4444;
            box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.15);
        }

        .partner-create-page .form-hint {
            font-size: 0.8rem;
            color: #6b7280;
            margin-top: 0.5rem;
            font-style: italic;
        }

        .partner-create-page .form-actions {
            display: flex;
            gap: 1rem;
            align-items: center;
            flex-wrap: wrap;
            padding-top: 1.5rem;
            border-top: 1px solid #f1f5f9;
        }

        .partner-create-page .btn {
            display: inline-flex;
            align-items: center;
            gap: 0.75rem;
            padding: 1rem 2rem;
            border: none;
            border-radius: 12px;
            font-size: 1rem;
            font-weight: 600;
            text-decoration: none;
            cursor: pointer;
            transition: all 0.3s ease;
            line-height: 1;
            font-family: 'Inter', sans-serif;
        }

        .partner-create-page .btn:focus {
            outline: 2px solid #6366f1;
            outline-offset: 2px;
        }

        .partner-create-page .btn-primary {
            background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
            color: white;
            box-shadow: 0 4px 15px rgba(99, 102, 241, 0.3);
        }

        .partner-create-page .btn-primary:hover {
            background: linear-gradient(135deg, #4f46e5 0%, #4338ca 100%);
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(79, 70, 229, 0.4);
        }

        .partner-create-page .btn-secondary {
            background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
            color: white;
            box-shadow: 0 4px 15px rgba(107, 114, 128, 0.2);
        }

        .partner-create-page .btn-secondary:hover {
            background: linear-gradient(135deg, #4b5563 0%, #374151 100%);
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(75, 85, 99, 0.3);
        }

        /* Information Card - MÀU SẮC HÀI HÒA TINH TẾ */
        .partner-create-page .info-card {
            background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
            border: 2px solid #e2e8f0;
            border-radius: 20px;
            overflow: hidden;
        }

        .partner-create-page .info-card .card-header {
            background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
            border-bottom: 2px solid #cbd5e1;
            padding: 1.25rem 2rem;
        }

        .partner-create-page .info-card .card-title {
            color: #334155;
        }

        .partner-create-page .info-card .card-title i {
            color: #6366f1;
        }

        .partner-create-page .info-list {
            display: flex;
            flex-direction: column;
            gap: 1.25rem;
        }

        .partner-create-page .info-item {
            display: flex;
            align-items: center;
            gap: 1.25rem;
            padding: 1.5rem;
            background: rgba(255, 255, 255, 0.8);
            border-radius: 16px;
            border: 1px solid #e2e8f0;
            transition: all 0.3s ease;
            position: relative;
            overflow: hidden;
        }

        .partner-create-page .info-item::before {
            content: '';
            position: absolute;
            left: 0;
            top: 0;
            height: 100%;
            width: 4px;
            background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
            border-radius: 4px 0 0 4px;
        }

        .partner-create-page .info-item:hover {
            transform: translateX(8px);
            background: rgba(255, 255, 255, 0.95);
            box-shadow: 0 4px 15px rgba(99, 102, 241, 0.1);
            border-color: #c7d2fe;
        }

        .partner-create-page .info-item i {
            color: #6366f1;
            font-size: 1.75rem;
            flex-shrink: 0;
            background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
            padding: 0.75rem;
            border-radius: 12px;
            width: 60px;
            height: 60px;
            display: flex;
            align-items: center;
            justify-content: center;
            box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
        }

        .partner-create-page .info-content {
            flex: 1;
        }

        .partner-create-page .info-content strong {
            color: #374151;
            display: block;
            margin-bottom: 0.5rem;
            font-size: 1rem;
            font-weight: 700;
        }

        .partner-create-page .info-content span {
            color: #4b5563;
            font-size: 0.95rem;
            line-height: 1.5;
            font-weight: 500;
        }

        /* Alerts tinh tế */
        .partner-create-page .alert {
            padding: 1.25rem 1.75rem;
            margin-bottom: 2rem;
            border-radius: 16px;
            border: 2px solid transparent;
            display: flex;
            align-items: center;
            gap: 1rem;
            animation: slideIn 0.4s ease;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
        }

        .partner-create-page .alert-success {
            background: linear-gradient(135deg, #f0fdf4 0%, #ecfdf5 100%);
            color: #065f46;
            border-color: #d1fae5;
        }

        .partner-create-page .alert-error {
            background: linear-gradient(135deg, #fef2f2 0%, #fef2f2 100%);
            color: #991b1b;
            border-color: #fecaca;
        }

        .partner-create-page .alert i {
            font-size: 1.5rem;
        }

        .partner-create-page .alert-success i {
            color: #10b981;
        }

        .partner-create-page .alert-error i {
            color: #ef4444;
        }

        @keyframes slideIn {
            from {
                opacity: 0;
                transform: translateY(-15px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        /* Responsive Design */
        @media (max-width: 768px) {
            .partner-create-page .container {
                padding: 1rem;
            }
            
            .partner-create-page .card-body {
                padding: 1.5rem;
            }
            
            .partner-create-page .card-header {
                padding: 1.25rem 1.5rem;
            }
            
            .partner-create-page .form-actions {
                flex-direction: column;
                align-items: stretch;
            }
            
            .partner-create-page .btn {
                justify-content: center;
                padding: 0.875rem 1.5rem;
            }
            
            .partner-create-page .info-item {
                flex-direction: column;
                text-align: center;
                gap: 1rem;
                padding: 1.25rem;
            }
            
            .partner-create-page .info-item::before {
                width: 100%;
                height: 4px;
                top: 0;
                left: 0;
                border-radius: 4px 4px 0 0;
            }
            
            .partner-create-page .info-item i {
                width: 50px;
                height: 50px;
                font-size: 1.5rem;
            }
        }

        /* Dark Mode Support - Hài hòa */
        @media (prefers-color-scheme: dark) {
            .partner-create-page .card {
                background: #1f2937;
                border-color: #374151;
                box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
            }
            
            .partner-create-page .card-header {
                background: linear-gradient(135deg, #1f2937 0%, #374151 100%);
                border-color: #4b5563;
            }
            
            .partner-create-page .card-title {
                color: #f9fafb;
            }
            
            .partner-create-page .card-title i {
                color: #818cf8;
            }
            
            .partner-create-page .form-label {
                color: #e5e7eb;
            }
            
            .partner-create-page .form-input {
                background: #374151;
                border-color: #4b5563;
                color: #f9fafb;
            }
            
            .partner-create-page .form-input:focus {
                border-color: #818cf8;
                background: #404a5c;
            }
            
            .partner-create-page .form-hint {
                color: #9ca3af;
            }
            
            .partner-create-page .info-card {
                background: linear-gradient(135deg, #1f2937 0%, #2d3748 100%);
                border-color: #4b5563;
            }
            
            .partner-create-page .info-card .card-header {
                background: linear-gradient(135deg, #2d3748 0%, #374151 100%);
                border-color: #4b5563;
            }
            
            .partner-create-page .info-card .card-title {
                color: #e5e7eb;
            }
            
            .partner-create-page .info-card .card-title i {
                color: #818cf8;
            }
            
            .partner-create-page .info-item {
                background: rgba(55, 65, 81, 0.8);
                border-color: #4b5563;
            }
            
            .partner-create-page .info-item::before {
                background: linear-gradient(135deg, #818cf8 0%, #6366f1 100%);
            }
            
            .partner-create-page .info-item:hover {
                background: rgba(55, 65, 81, 0.95);
                border-color: #6366f1;
            }
            
            .partner-create-page .info-item i {
                color: #818cf8;
                background: linear-gradient(135deg, #3730a3 0%, #312e81 100%);
                box-shadow: 0 4px 12px rgba(129, 140, 248, 0.2);
            }
            
            .partner-create-page .info-content strong {
                color: #e5e7eb;
            }
            
            .partner-create-page .info-content span {
                color: #d1d5db;
            }
            
            .partner-create-page .alert-success {
                background: linear-gradient(135deg, #064e3b 0%, #065f46 100%);
                color: #d1fae5;
                border-color: #047857;
            }
            
            .partner-create-page .alert-error {
                background: linear-gradient(135deg, #7f1d1d 0%, #991b1b 100%);
                color: #fecaca;
                border-color: #dc2626;
            }
        }
    </style>
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
            <a href="${pageContext.request.contextPath}/admin/partners" class="nav-item">
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
<!--            <a href="${pageContext.request.contextPath}/adminpaymentverify" class="nav-item">
                <i class="fas fa-money-check-alt"></i>
                <span>Verify Payments</span>
            </a>-->
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
    <main class="content partner-create-page">
        <header class="content-header">
            <div class="header-left">
                <h1>Tạo Partner Mới</h1>
                <div class="breadcrumb">
                    <a href="${pageContext.request.contextPath}/admin/dashboard">Admin</a>
                    <i class="fas fa-chevron-right"></i>
                    <a href="${pageContext.request.contextPath}/admin/partners">Partners</a>
                    <i class="fas fa-chevron-right"></i>
                    <span class="active">Tạo mới</span>
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

            <section class="card">
                <div class="card-header">
                    <div class="card-title">
                        <i class="fas fa-user-plus"></i>
                        <span>Thông tin Partner</span>
                    </div>
                </div>
                <div class="card-body">
                    <form method="post" class="partner-form">
                        <div class="form-grid">
                            <div class="form-group">
                                <label for="username" class="form-label">
                                    Username <span class="required">*</span>
                                </label>
                                <input type="text" id="username" name="username" value="${param.username}" 
                                       class="form-input" required 
                                       pattern="[a-zA-Z0-9._]{3,50}" 
                                       title="3-50 ký tự, chỉ cho phép chữ, số, dấu chấm và gạch dưới">
                                <div class="form-hint">3-50 ký tự, chỉ cho phép chữ, số, dấu chấm và gạch dưới</div>
                            </div>

                            <div class="form-group">
                                <label for="companyName" class="form-label">
                                    Tên công ty <span class="required">*</span>
                                </label>
                                <input type="text" id="companyName" name="companyName" value="${param.companyName}" 
                                       class="form-input" required>
                            </div>

                            <div class="form-group">
                                <label for="address" class="form-label">Địa chỉ</label>
                                <input type="text" id="address" name="address" value="${param.address}" 
                                       class="form-input">
                            </div>

                            <div class="form-group">
                                <label for="phone" class="form-label">Số điện thoại</label>
                                <input type="text" id="phone" name="phone" value="${param.phone}" 
                                       class="form-input"
                                       pattern="^[0-9 +()\-]{7,20}$" 
                                       title="7-20 ký tự số, khoảng trắng, +, (), -">
                                <div class="form-hint">7-20 ký tự số, khoảng trắng, +, (), -</div>
                            </div>
                        </div>

                        <div class="form-actions">
                            <button type="submit" class="btn btn-primary">
                                <i class="fas fa-save"></i> Tạo Partner
                            </button>
                            <a href="${pageContext.request.contextPath}/admin/partners" class="btn btn-secondary">
                                <i class="fas fa-arrow-left"></i> Quay lại
                            </a>
                        </div>
                    </form>
                </div>
            </section>

            <!-- Information Card - MÀU SẮC HÀI HÒA -->
            <section class="card info-card">
                <div class="card-header">
                    <div class="card-title">
                        <i class="fas fa-info-circle"></i>
                        <span>Thông tin quan trọng</span>
                    </div>
                </div>
                <div class="card-body">
                    <div class="info-list">
                        <div class="info-item">
                            <i class="fas fa-key"></i>
                            <div class="info-content">
                                <strong>Mật khẩu mặc định:</strong>
                                <span>1 (Partner nên đổi mật khẩu ngay sau khi đăng nhập)</span>
                            </div>
                        </div>
                        <div class="info-item">
                            <i class="fas fa-user-shield"></i>
                            <div class="info-content">
                                <strong>Quyền hạn:</strong>
                                <span>Partner có thể quản lý xe, đơn hàng và doanh thu của họ</span>
                            </div>
                        </div>
                        <div class="info-item">
                            <i class="fas fa-bell"></i>
                            <div class="info-content">
                                <strong>Lưu ý quan trọng:</strong>
                                <span>Partner sẽ nhận được email hướng dẫn sử dụng hệ thống</span>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        </div>
    </main>

    <script>
        // Auto hide alerts after 5 seconds
        setTimeout(function() {
            const alerts = document.querySelectorAll('.alert');
            alerts.forEach(alert => {
                alert.style.transition = 'opacity 0.5s ease';
                alert.style.opacity = '0';
                setTimeout(() => alert.remove(), 500);
            });
        }, 5000);

        // Form validation and enhancement
        document.addEventListener('DOMContentLoaded', function() {
            const form = document.querySelector('.partner-form');
            const inputs = form.querySelectorAll('.form-input');
            
            // Real-time validation
            inputs.forEach(input => {
                input.addEventListener('blur', function() {
                    validateField(this);
                });
                
                input.addEventListener('input', function() {
                    if (this.classList.contains('invalid')) {
                        validateField(this);
                    }
                });
            });
            
            function validateField(field) {
                const isValid = field.checkValidity();
                
                if (field.value.trim() === '') {
                    field.classList.remove('valid', 'invalid');
                    return;
                }
                
                if (isValid) {
                    field.classList.remove('invalid');
                    field.classList.add('valid');
                } else {
                    field.classList.remove('valid');
                    field.classList.add('invalid');
                }
            }
            
            // Form submission enhancement
            form.addEventListener('submit', function(e) {
                let isValid = true;
                
                inputs.forEach(input => {
                    validateField(input);
                    if (input.classList.contains('invalid')) {
                        isValid = false;
                    }
                });
                
                if (!isValid) {
                    e.preventDefault();
                    // Scroll to first invalid field
                    const firstInvalid = form.querySelector('.invalid');
                    if (firstInvalid) {
                        firstInvalid.scrollIntoView({ 
                            behavior: 'smooth', 
                            block: 'center' 
                        });
                    }
                }
            });
        });
    </script>
</body>
</html>