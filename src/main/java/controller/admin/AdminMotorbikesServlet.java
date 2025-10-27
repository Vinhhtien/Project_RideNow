package controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import model.Motorbike;
import model.BikeType;
import model.Partner;
import service.IMotorbikeAdminService;
import service.MotorbikeAdminService;

@WebServlet("/admin/bikes")
@MultipartConfig(
    maxFileSize = 1024 * 1024 * 5, // 5MB
    maxRequestSize = 1024 * 1024 * 10 // 10MB
)
public class AdminMotorbikesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private IMotorbikeAdminService motorbikeAdminService;

    @Override
    public void init() {
        motorbikeAdminService = new MotorbikeAdminService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("=== DEBUG doGet START ===");
        System.out.println("Request URL: " + request.getRequestURL());
        System.out.println("Query String: " + request.getQueryString());
        
        String action = request.getParameter("action");
        System.out.println("Action parameter: " + action);
        
        if (action == null) {
            System.out.println("Calling listMotorbikes");
            listMotorbikes(request, response);
        } else {
            switch (action) {
                case "new":
                    System.out.println("Calling showNewForm");
                    showNewForm(request, response);
                    break;
                case "edit":
                    System.out.println("Calling showEditForm");
                    showEditForm(request, response);
                    break;
                case "delete":
                    System.out.println("Calling deleteMotorbike");
                    deleteMotorbike(request, response);
                    break;
                case "filter":
                    System.out.println("Calling filterMotorbikes");
                    filterMotorbikes(request, response);
                    break;
                default:
                    System.out.println("Calling listMotorbikes (default)");
                    listMotorbikes(request, response);
                    break;
            }
        }
        System.out.println("=== DEBUG doGet END ===");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("=== DEBUG doPost START ===");
        System.out.println("Content-Type: " + request.getContentType());
        System.out.println("Request URL: " + request.getRequestURL());
        System.out.println("Query String: " + request.getQueryString());
        
        // Debug tất cả parameters
        System.out.println("All parameters:");
        request.getParameterMap().forEach((key, values) -> {
            System.out.println("  " + key + ": " + String.join(", ", values));
        });
        
        String action = request.getParameter("action");
        System.out.println("Action parameter: " + action);
        
        if ("create".equals(action)) {
            System.out.println("Calling createMotorbike");
            createMotorbike(request, response);
        } else if ("update".equals(action)) {
            System.out.println("Calling updateMotorbike");
            updateMotorbike(request, response);
        } else {
            System.out.println("Calling listMotorbikes");
            listMotorbikes(request, response);
        }
        System.out.println("=== DEBUG doPost END ===");
    }

    private void listMotorbikes(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("=== DEBUG listMotorbikes START ===");
        try {
            List<Motorbike> motorbikes = motorbikeAdminService.getAllMotorbikes();
            System.out.println("Retrieved " + motorbikes.size() + " motorbikes");
            
            request.setAttribute("motorbikes", motorbikes);
            request.getRequestDispatcher("/admin/admin-motorbikes-management.jsp").forward(request, response);
        } catch (Exception e) {
            System.err.println("Error in listMotorbikes: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        System.out.println("=== DEBUG listMotorbikes END ===");
    }

    private void filterMotorbikes(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("=== DEBUG filterMotorbikes START ===");
        try {
            String ownerType = request.getParameter("ownerType");
            String status = request.getParameter("status");
            System.out.println("Filter params - ownerType: " + ownerType + ", status: " + status);
            
            List<Motorbike> motorbikes;

            if (ownerType != null && !ownerType.equals("all")) {
                System.out.println("Filtering by owner: " + ownerType);
                motorbikes = motorbikeAdminService.getMotorbikesByOwner(ownerType);
            } else if (status != null && !status.equals("all")) {
                System.out.println("Filtering by status: " + status);
                motorbikes = motorbikeAdminService.getMotorbikesByStatus(status);
            } else {
                System.out.println("No filter applied, getting all motorbikes");
                motorbikes = motorbikeAdminService.getAllMotorbikes();
            }

            System.out.println("Filtered result: " + motorbikes.size() + " motorbikes");
            request.setAttribute("motorbikes", motorbikes);
            request.getRequestDispatcher("/admin/admin-motorbikes-management.jsp").forward(request, response);
        } catch (Exception e) {
            System.err.println("Error in filterMotorbikes: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        System.out.println("=== DEBUG filterMotorbikes END ===");
    }

    private void showNewForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("=== DEBUG showNewForm START ===");
        try {
            List<BikeType> bikeTypes = motorbikeAdminService.getAllBikeTypes();
            List<Partner> partners = motorbikeAdminService.getAllPartners();

            System.out.println("Retrieved " + bikeTypes.size() + " bike types");
            System.out.println("Retrieved " + partners.size() + " partners");

            request.setAttribute("bikeTypes", bikeTypes);
            request.setAttribute("partners", partners);
            request.getRequestDispatcher("/admin/admin-motorbike-form.jsp").forward(request, response);
        } catch (Exception e) {
            System.err.println("Error in showNewForm: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        System.out.println("=== DEBUG showNewForm END ===");
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("=== DEBUG showEditForm START ===");
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            System.out.println("Editing motorbike ID: " + id);
            
            Motorbike motorbike = motorbikeAdminService.getMotorbikeById(id);
            
            if (motorbike == null) {
                System.err.println("Motorbike not found with ID: " + id);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            System.out.println("Found motorbike: " + motorbike.getBikeName());
            
            List<BikeType> bikeTypes = motorbikeAdminService.getAllBikeTypes();
            List<Partner> partners = motorbikeAdminService.getAllPartners();

            System.out.println("Retrieved " + bikeTypes.size() + " bike types");
            System.out.println("Retrieved " + partners.size() + " partners");

            request.setAttribute("motorbike", motorbike);
            request.setAttribute("bikeTypes", bikeTypes);
            request.setAttribute("partners", partners);
            request.getRequestDispatcher("/admin/admin-motorbike-form.jsp").forward(request, response);
        } catch (Exception e) {
            System.err.println("Error in showEditForm: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        System.out.println("=== DEBUG showEditForm END ===");
    }

    private void createMotorbike(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("=== DEBUG createMotorbike START ===");
        try {
            // DEBUG: Log tất cả parameters
            System.out.println("All form parameters:");
            request.getParameterMap().forEach((key, value) -> {
                System.out.println("  " + key + ": " + String.join(", ", value));
            });

            // Lấy các tham số từ form
            String bikeName = request.getParameter("bikeName");
            String licensePlate = request.getParameter("licensePlate");
            String pricePerDay = request.getParameter("pricePerDay");
            String status = request.getParameter("status");
            String description = request.getParameter("description");
            int typeId = Integer.parseInt(request.getParameter("typeId"));
            
            System.out.println("Form data:");
            System.out.println("  bikeName: " + bikeName);
            System.out.println("  licensePlate: " + licensePlate);
            System.out.println("  pricePerDay: " + pricePerDay);
            System.out.println("  status: " + status);
            System.out.println("  description: " + description);
            System.out.println("  typeId: " + typeId);
            
            // Xác định chủ sở hữu
            Integer partnerId = null;
            Integer storeId = null;
            String ownerType = request.getParameter("ownerType");
            System.out.println("ownerType: " + ownerType);
            
            if (ownerType != null && "partner".equals(ownerType)) {
                String partnerIdParam = request.getParameter("partnerId");
                System.out.println("partnerId parameter: " + partnerIdParam);
                if (partnerIdParam != null && !partnerIdParam.isEmpty()) {
                    partnerId = Integer.parseInt(partnerIdParam);
                }
            } else {
                storeId = 1; // Cửa hàng mặc định của admin
            }
            
            System.out.println("Final owner - partnerId: " + partnerId + ", storeId: " + storeId);

            Motorbike newMotorbike = new Motorbike();
            newMotorbike.setBikeName(bikeName);
            newMotorbike.setLicensePlate(licensePlate);
            newMotorbike.setPricePerDay(new BigDecimal(pricePerDay));
            newMotorbike.setStatus(status);
            newMotorbike.setDescription(description);
            newMotorbike.setTypeId(typeId);
            newMotorbike.setPartnerId(partnerId);
            newMotorbike.setStoreId(storeId);

            System.out.println("Calling service to add motorbike...");
            boolean success = motorbikeAdminService.addMotorbike(newMotorbike);
            System.out.println("Service result: " + success);
            
            if (success) {
                // Lấy ID vừa tạo
                int bikeId = newMotorbike.getBikeId();
                System.out.println("New bike created with ID: " + bikeId);
                
                // Xử lý upload ảnh sau khi tạo xe thành công
                if (bikeId > 0) {
                    System.out.println("Calling handleImageUpload for new bike...");
                    handleImageUpload(request, bikeId, typeId, false);
                } else {
                    System.out.println("WARNING: bikeId is not set after creation!");
                }
                
                response.sendRedirect(request.getContextPath() + "/admin/bikes?success=created");
            } else {
                System.err.println("Failed to create motorbike in service");
                response.sendRedirect(request.getContextPath() + "/admin/bikes?error=create_failed");
            }
        } catch (Exception e) {
            System.err.println("Exception in createMotorbike: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/bikes?error=create_exception");
        }
        System.out.println("=== DEBUG createMotorbike END ===");
    }

    private void updateMotorbike(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("=== DEBUG updateMotorbike START ===");
        try {
            int bikeId = Integer.parseInt(request.getParameter("bikeId"));
            String bikeName = request.getParameter("bikeName");
            String licensePlate = request.getParameter("licensePlate");
            String pricePerDay = request.getParameter("pricePerDay");
            String status = request.getParameter("status");
            String description = request.getParameter("description");
            int typeId = Integer.parseInt(request.getParameter("typeId"));

            System.out.println("Update data for bikeId: " + bikeId);
            System.out.println("  bikeName: " + bikeName);
            System.out.println("  licensePlate: " + licensePlate);
            System.out.println("  pricePerDay: " + pricePerDay);
            System.out.println("  status: " + status);
            System.out.println("  description: " + description);
            System.out.println("  typeId: " + typeId);

            Motorbike existingMotorbike = motorbikeAdminService.getMotorbikeById(bikeId);
            if (existingMotorbike == null) {
                System.err.println("Motorbike not found with ID: " + bikeId);
                response.sendRedirect(request.getContextPath() + "/admin/bikes?error=not_found");
                return;
            }

            System.out.println("Found existing motorbike: " + existingMotorbike.getBikeName());
            
            existingMotorbike.setBikeName(bikeName);
            existingMotorbike.setLicensePlate(licensePlate);
            existingMotorbike.setPricePerDay(new BigDecimal(pricePerDay));
            existingMotorbike.setStatus(status);
            existingMotorbike.setDescription(description);
            existingMotorbike.setTypeId(typeId);

            System.out.println("Calling service to update motorbike...");
            boolean success = motorbikeAdminService.updateMotorbike(existingMotorbike);
            System.out.println("Service result: " + success);
            
            if (success) {
                // Xử lý upload ảnh và xóa ảnh
                System.out.println("Calling handleImageUpdate...");
                handleImageUpdate(request, bikeId, typeId);
                
                response.sendRedirect(request.getContextPath() + "/admin/bikes?success=updated");
            } else {
                System.err.println("Failed to update motorbike in service");
                response.sendRedirect(request.getContextPath() + "/admin/bikes?error=update_failed");
            }
        } catch (Exception e) {
            System.err.println("Exception in updateMotorbike: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/bikes?error=update_exception");
        }
        System.out.println("=== DEBUG updateMotorbike END ===");
    }

    private void deleteMotorbike(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        System.out.println("=== DEBUG deleteMotorbike START ===");
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            System.out.println("Deleting motorbike ID: " + id);
            
            // Xóa thư mục ảnh trước khi xóa xe
            Motorbike motorbike = motorbikeAdminService.getMotorbikeById(id);
            if (motorbike != null) {
                System.out.println("Found motorbike, deleting image folder...");
                deleteImageFolder(motorbike.getBikeId(), motorbike.getTypeId());
            } else {
                System.out.println("Motorbike not found, skipping image deletion");
            }
            
            System.out.println("Calling service to delete motorbike...");
            boolean success = motorbikeAdminService.deleteMotorbike(id);
            System.out.println("Service result: " + success);
            
            if (success) {
                response.sendRedirect(request.getContextPath() + "/admin/bikes?success=deleted");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/bikes?error=delete_failed");
            }
        } catch (Exception e) {
            System.err.println("Exception in deleteMotorbike: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/bikes?error=delete_exception");
        }
        System.out.println("=== DEBUG deleteMotorbike END ===");
    }

    // ===== PHƯƠNG THỨC MỚI: Lấy đường dẫn source code =====
    private String getSourceImageBasePath() {
        // Lấy đường dẫn hiện tại từ servlet context
        String currentPath = getServletContext().getRealPath("/");
        System.out.println("Current real path: " + currentPath);
        
        File currentDir = new File(currentPath);
        
        // Nếu đang chạy từ thư mục target, điều chỉnh về src/main/webapp
        if (currentPath.contains("target")) {
            // Đi từ target về thư mục gốc project, rồi vào src/main/webapp
            File projectRoot = currentDir.getParentFile().getParentFile(); // target -> project root
            if (projectRoot != null) {
                String sourcePath = projectRoot.getAbsolutePath() + File.separator + "src" + 
                                  File.separator + "main" + File.separator + "webapp";
                File sourceDir = new File(sourcePath);
                if (sourceDir.exists()) {
                    System.out.println("Using source directory: " + sourcePath);
                    return sourcePath;
                } else {
                    System.out.println("Source directory not found: " + sourcePath);
                }
            }
        }
        
        // Nếu không tìm thấy source directory, dùng đường dẫn hiện tại
        System.out.println("Using current directory: " + currentPath);
        return currentPath;
    }

    // ===== Xử lý ảnh - VỚI DEBUG CHI TIẾT =====
    
    private void handleImageUpload(HttpServletRequest request, int bikeId, int typeId, boolean isUpdate) 
            throws ServletException, IOException {
        
        System.out.println("=== DEBUG handleImageUpload START ===");
        System.out.println("Parameters - bikeId: " + bikeId + ", typeId: " + typeId + ", isUpdate: " + isUpdate);
        
        try {
            // DEBUG: Kiểm tra các đường dẫn
            String servletPath = getServletContext().getRealPath("/");
            String sourcePath = getSourceImageBasePath();
            
            System.out.println("Servlet context path: " + servletPath);
            System.out.println("Source image base path: " + sourcePath);
            
            // Lấy tất cả các parts
            Collection<Part> parts = request.getParts();
            System.out.println("Total parts in request: " + parts.size());
            
            List<Part> imageParts = new ArrayList<>();
            
            for (Part part : parts) {
                System.out.println("Part: name=" + part.getName() + 
                                 ", size=" + part.getSize() + 
                                 ", contentType=" + part.getContentType() +
                                 ", submittedFileName=" + part.getSubmittedFileName());
                
                if ("images".equals(part.getName()) && part.getSize() > 0 && part.getContentType() != null 
                    && part.getContentType().startsWith("image/")) {
                    imageParts.add(part);
                    System.out.println("✓ Added to imageParts: " + part.getSubmittedFileName());
                }
            }
            
            System.out.println("Total image parts found: " + imageParts.size());
            
            if (!imageParts.isEmpty()) {
                String typeFolder = getTypeFolder(typeId);
                System.out.println("Type folder: " + typeFolder);
                
                // Tạo đường dẫn đến thư mục images trong SOURCE CODE
                String uploadPath = getSourceImageBasePath() + File.separator + "images" + File.separator + "bike" 
                                  + File.separator + typeFolder + File.separator + bikeId;
                
                System.out.println("Full upload path: " + uploadPath);
                
                File uploadDir = new File(uploadPath);
                System.out.println("Upload directory exists: " + uploadDir.exists());
                
                if (!uploadDir.exists()) {
                    boolean created = uploadDir.mkdirs();
                    System.out.println("Directory created: " + created + " at " + uploadDir.getAbsolutePath());
                }
                
                // Lấy danh sách ảnh hiện tại (nếu có)
                File[] existingFiles = uploadDir.listFiles((dir, name) -> name.matches("\\d+\\.jpg"));
                int nextImageNumber = 1;
                
                if (existingFiles != null) {
                    System.out.println("Existing image files: " + existingFiles.length);
                    
                    // Sắp xếp files theo số thứ tự
                    Arrays.sort(existingFiles, Comparator.comparing(file -> {
                        String name = file.getName();
                        return Integer.parseInt(name.substring(0, name.lastIndexOf('.')));
                    }));
                    
                    // Tìm số tiếp theo để thêm ảnh mới
                    nextImageNumber = existingFiles.length + 1;
                    System.out.println("Next available image number: " + nextImageNumber);
                    
                    // Debug existing files
                    for (File file : existingFiles) {
                        System.out.println("  Existing: " + file.getName() + " (" + file.length() + " bytes)");
                    }
                } else {
                    System.out.println("No existing image files found");
                }
                
                // Lưu ảnh mới - đánh số tiếp theo
                int imageCount = nextImageNumber;
                for (Part filePart : imageParts) {
                    if (imageCount > 6) {
                        System.out.println("⚠ Reached maximum 6 images, stopping");
                        break; // Tối đa 6 ảnh
                    }
                    
                    String fileName = imageCount + ".jpg";
                    String filePath = uploadPath + File.separator + fileName;
                    
                    System.out.println("Saving image " + imageCount + ": " + filePath);
                    System.out.println("  File size: " + filePart.getSize() + " bytes");
                    System.out.println("  Content type: " + filePart.getContentType());
                    System.out.println("  Submitted name: " + filePart.getSubmittedFileName());
                    
                    // Ghi file với stream
                    try (InputStream input = filePart.getInputStream();
                         OutputStream output = new FileOutputStream(filePath)) {
                        
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        long totalBytes = 0;
                        while ((bytesRead = input.read(buffer)) != -1) {
                            output.write(buffer, 0, bytesRead);
                            totalBytes += bytesRead;
                        }
                        System.out.println("  ✓ Written " + totalBytes + " bytes");
                    } catch (Exception e) {
                        System.err.println("  ✗ Error writing file: " + e.getMessage());
                        throw e;
                    }
                    
                    System.out.println("  ✓ Image saved successfully: " + fileName);
                    imageCount++;
                }
                
                // Debug: kiểm tra file sau khi lưu
                File[] newFiles = uploadDir.listFiles();
                if (newFiles != null) {
                    System.out.println("Total files after upload: " + newFiles.length);
                    for (File file : newFiles) {
                        System.out.println("  - " + file.getName() + " (" + file.length() + " bytes)");
                    }
                } else {
                    System.out.println("No files found after upload (directory might be empty)");
                }
            } else {
                System.out.println("No image parts found to upload");
            }
        } catch (Exception e) {
            System.err.println("❌ Error in handleImageUpload: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        System.out.println("=== DEBUG handleImageUpload END ===");
    }
    
    // PHƯƠNG THỨC MỚI: Xử lý toàn bộ cập nhật ảnh (xóa + thêm mới + sắp xếp lại)
    private void handleImageUpdate(HttpServletRequest request, int bikeId, int typeId) {
        System.out.println("=== DEBUG handleImageUpdate START ===");
        System.out.println("Parameters - bikeId: " + bikeId + ", typeId: " + typeId);
        
        try {
            // 1. Xử lý xóa ảnh trước
            System.out.println("Step 1: Handling image deletion...");
            handleImageDeletion(request, bikeId, typeId);
            
            // 2. Sắp xếp lại ảnh hiện tại
            System.out.println("Step 2: Reorganizing images...");
            reorganizeImages(bikeId, typeId);
            
            // 3. Xử lý thêm ảnh mới
            System.out.println("Step 3: Handling new image upload...");
            handleImageUpload(request, bikeId, typeId, true);
            
            System.out.println("✓ Image update completed successfully");
        } catch (Exception e) {
            System.err.println("❌ Error in handleImageUpdate: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        System.out.println("=== DEBUG handleImageUpdate END ===");
    }
    
    // PHƯƠNG THỨC MỚI: Sắp xếp lại ảnh thành thứ tự liên tục (1.jpg, 2.jpg, 3.jpg...)
    private void reorganizeImages(int bikeId, int typeId) {
        System.out.println("=== DEBUG reorganizeImages START ===");
        System.out.println("Parameters - bikeId: " + bikeId + ", typeId: " + typeId);
        
        try {
            String typeFolder = getTypeFolder(typeId);
            
            // Sử dụng đường dẫn SOURCE CODE
            String imagePath = getSourceImageBasePath() + File.separator + "images" 
                             + File.separator + "bike" + File.separator + typeFolder 
                             + File.separator + bikeId;
            
            System.out.println("Image path: " + imagePath);
            
            File imageDir = new File(imagePath);
            if (!imageDir.exists()) {
                System.out.println("⚠ Image directory does not exist: " + imagePath);
                System.out.println("=== DEBUG reorganizeImages END (no directory) ===");
                return;
            }
            
            // Lấy tất cả file ảnh .jpg
            File[] imageFiles = imageDir.listFiles((dir, name) -> name.matches("\\d+\\.jpg"));
            
            if (imageFiles == null || imageFiles.length == 0) {
                System.out.println("⚠ No image files found to reorganize");
                System.out.println("=== DEBUG reorganizeImages END (no files) ===");
                return;
            }
            
            System.out.println("Found " + imageFiles.length + " image files to reorganize");
            
            // Debug hiển thị files trước khi sắp xếp
            System.out.println("Files before reorganization:");
            for (File file : imageFiles) {
                System.out.println("  - " + file.getName() + " (" + file.length() + " bytes)");
            }
            
            // Sắp xếp files theo số thứ tự
            Arrays.sort(imageFiles, Comparator.comparing(file -> {
                String name = file.getName();
                return Integer.parseInt(name.substring(0, name.lastIndexOf('.')));
            }));
            
            System.out.println("Files after sorting:");
            for (File file : imageFiles) {
                System.out.println("  - " + file.getName());
            }
            
            // Tạo thư mục tạm thời
            String tempPath = imagePath + "_temp";
            File tempDir = new File(tempPath);
            System.out.println("Temp directory: " + tempPath);
            
            if (tempDir.exists()) {
                System.out.println("Temp directory exists, deleting...");
                deleteFolder(tempDir);
            }
            
            boolean tempCreated = tempDir.mkdirs();
            System.out.println("Temp directory created: " + tempCreated);
            
            // Copy ảnh sang thư mục tạm với tên mới (sắp xếp lại từ 1)
            System.out.println("Copying files to temp directory...");
            for (int i = 0; i < imageFiles.length; i++) {
                File oldFile = imageFiles[i];
                String newFileName = (i + 1) + ".jpg";
                File newFile = new File(tempPath + File.separator + newFileName);
                
                System.out.println("  Copying: " + oldFile.getName() + " -> " + newFileName);
                
                // Copy file
                try (InputStream in = new java.io.FileInputStream(oldFile);
                     OutputStream out = new FileOutputStream(newFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    long totalBytes = 0;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                        totalBytes += length;
                    }
                    System.out.println("    ✓ Copied " + totalBytes + " bytes");
                } catch (Exception e) {
                    System.err.println("    ✗ Error copying file: " + e.getMessage());
                    throw e;
                }
            }
            
            // Xóa thư mục cũ và đổi tên thư mục tạm
            System.out.println("Deleting original directory...");
            deleteFolder(imageDir);
            
            System.out.println("Renaming temp directory...");
            boolean renamed = tempDir.renameTo(imageDir);
            System.out.println("Rename successful: " + renamed);
            
            // Kiểm tra kết quả
            File[] finalFiles = imageDir.listFiles((dir, name) -> name.matches("\\d+\\.jpg"));
            if (finalFiles != null) {
                System.out.println("Final files after reorganization (" + finalFiles.length + " files):");
                for (File file : finalFiles) {
                    System.out.println("  - " + file.getName() + " (" + file.length() + " bytes)");
                }
            }
            
            System.out.println("✓ Image reorganization completed successfully");
        } catch (Exception e) {
            System.err.println("❌ Error in reorganizeImages: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        System.out.println("=== DEBUG reorganizeImages END ===");
    }
    
    private void handleImageDeletion(HttpServletRequest request, int bikeId, int typeId) {
        System.out.println("=== DEBUG handleImageDeletion START ===");
        System.out.println("Parameters - bikeId: " + bikeId + ", typeId: " + typeId);
        
        try {
            // Sử dụng getParameterValues để lấy tất cả giá trị
            String[] deletedImagesArray = request.getParameterValues("deletedImages");
            
            if (deletedImagesArray != null) {
                System.out.println("deletedImages parameter values: " + Arrays.toString(deletedImagesArray));
                
                List<String> indexesToDelete = new ArrayList<>();
                for (String deletedImagesStr : deletedImagesArray) {
                    if (deletedImagesStr != null && !deletedImagesStr.trim().isEmpty()) {
                        // Split mỗi string bằng dấu phẩy và thêm vào list
                        String[] parts = deletedImagesStr.split(",");
                        for (String part : parts) {
                            String trimmed = part.trim();
                            if (!trimmed.isEmpty()) {
                                indexesToDelete.add(trimmed);
                            }
                        }
                    }
                }
                
                System.out.println("All indexes to delete: " + indexesToDelete);
                
                if (!indexesToDelete.isEmpty()) {
                    String typeFolder = getTypeFolder(typeId);
                    
                    // Sử dụng đường dẫn SOURCE CODE
                    String imagePath = getSourceImageBasePath() + File.separator + "images" 
                                     + File.separator + "bike" + File.separator + typeFolder 
                                     + File.separator + bikeId;
                    
                    System.out.println("Deleting images from: " + imagePath);
                    
                    File imageDir = new File(imagePath);
                    if (!imageDir.exists()) {
                        System.out.println("⚠ Image directory does not exist");
                        System.out.println("=== DEBUG handleImageDeletion END (no directory) ===");
                        return;
                    }
                    
                    for (String index : indexesToDelete) {
                        String fileName = index + ".jpg";
                        File fileToDelete = new File(imagePath + File.separator + fileName);
                        System.out.println("Attempting to delete: " + fileName);
                        System.out.println("  File exists: " + fileToDelete.exists());
                        System.out.println("  File path: " + fileToDelete.getAbsolutePath());
                        
                        if (fileToDelete.exists()) {
                            boolean deleted = fileToDelete.delete();
                            System.out.println("  Delete result: " + deleted);
                            if (!deleted) {
                                System.err.println("  ✗ Failed to delete: " + fileName);
                            } else {
                                System.out.println("  ✓ Successfully deleted: " + fileName);
                            }
                        } else {
                            System.out.println("  ⚠ File not found: " + fileName);
                        }
                    }
                    
                    // Debug: kiểm tra files còn lại sau khi xóa
                    File[] remainingFiles = imageDir.listFiles((dir, name) -> name.matches("\\d+\\.jpg"));
                    if (remainingFiles != null) {
                        System.out.println("Remaining files after deletion (" + remainingFiles.length + " files):");
                        for (File file : remainingFiles) {
                            System.out.println("  - " + file.getName());
                        }
                    }
                } else {
                    System.out.println("No images to delete after processing");
                }
            } else {
                System.out.println("No deletedImages parameter found");
            }
        } catch (Exception e) {
            System.err.println("❌ Error in handleImageDeletion: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        System.out.println("=== DEBUG handleImageDeletion END ===");
    }
    
    private void deleteImageFolder(int bikeId, int typeId) {
        System.out.println("=== DEBUG deleteImageFolder START ===");
        System.out.println("Parameters - bikeId: " + bikeId + ", typeId: " + typeId);
        
        try {
            String typeFolder = getTypeFolder(typeId);
            
            // Sử dụng đường dẫn SOURCE CODE
            String imagePath = getSourceImageBasePath() + File.separator + "images" 
                             + File.separator + "bike" + File.separator + typeFolder 
                             + File.separator + bikeId;
            
            System.out.println("Deleting image folder: " + imagePath);
            
            File folder = new File(imagePath);
            if (folder.exists() && folder.isDirectory()) {
                System.out.println("Folder exists, deleting...");
                deleteFolder(folder);
                System.out.println("✓ Image folder deleted: " + imagePath);
            } else {
                System.out.println("⚠ Image folder does not exist: " + imagePath);
            }
        } catch (Exception e) {
            System.err.println("❌ Error in deleteImageFolder: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        System.out.println("=== DEBUG deleteImageFolder END ===");
    }
    
    // Phương thức hỗ trợ: xóa folder và tất cả contents
    private void deleteFolder(File folder) {
        System.out.println("=== DEBUG deleteFolder START ===");
        System.out.println("Deleting folder: " + folder.getAbsolutePath());
        
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            System.out.println("Folder contains " + (files != null ? files.length : 0) + " items");
            
            if (files != null) {
                for (File file : files) {
                    System.out.println("  Deleting: " + file.getName());
                    if (file.isDirectory()) {
                        deleteFolder(file);
                    } else {
                        boolean deleted = file.delete();
                        System.out.println("    Delete result: " + deleted);
                    }
                }
            }
            boolean folderDeleted = folder.delete();
            System.out.println("Folder deleted: " + folderDeleted);
        } else {
            System.out.println("Folder does not exist or is not a directory");
        }
        System.out.println("=== DEBUG deleteFolder END ===");
    }
    
    private String getTypeFolder(int typeId) {
        String folder;
        switch (typeId) {
            case 1: folder = "xe-so"; break;
            case 2: folder = "xe-ga"; break;
            case 3: folder = "xe-pkl"; break;
            default: folder = "khac"; break;
        }
        System.out.println("getTypeFolder: typeId=" + typeId + " -> " + folder);
        return folder;
    }
}