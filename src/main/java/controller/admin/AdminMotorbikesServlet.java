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
import java.util.*;
import java.util.stream.Collectors;

import model.Motorbike;
import model.BikeType;
import model.Partner;
import service.IMotorbikeAdminService;
import service.MotorbikeAdminService;

@WebServlet("/admin/bikes")
@MultipartConfig(
        maxFileSize = 1024 * 1024 * 5,     // 5MB / file
        maxRequestSize = 1024 * 1024 * 10  // 10MB / request
)
public class AdminMotorbikesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private IMotorbikeAdminService motorbikeAdminService;

    @Override
    public void init() {
        motorbikeAdminService = new MotorbikeAdminService();
    }

    /* ------------------------ Helpers ------------------------ */

    private String str(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? null : v.trim();
    }

    private Integer intOrNull(HttpServletRequest req, String name) {
        try {
            String v = str(req, name);
            if (v == null || v.isBlank()) return null;
            return Integer.valueOf(v);
        } catch (Exception e) {
            return null;
        }
    }

    private int intRequired(HttpServletRequest req, String name) {
        Integer v = intOrNull(req, name);
        if (v == null) throw new IllegalArgumentException("Missing/invalid int param: " + name);
        return v;
    }

    private BigDecimal bigDecimalRequired(HttpServletRequest req, String name) {
        try {
            String v = str(req, name);
            if (v == null || v.isBlank()) throw new IllegalArgumentException();
            return new BigDecimal(v);
        } catch (Exception e) {
            throw new IllegalArgumentException("Missing/invalid decimal param: " + name);
        }
    }

    private boolean licensePlateExists(String plate) {
        if (plate == null || plate.isBlank()) return false;
        List<Motorbike> all = motorbikeAdminService.getAllMotorbikes();
        return all.stream().anyMatch(b -> plate.equalsIgnoreCase(b.getLicensePlate()));
    }

    /* ------------------------ GET/POST ------------------------ */

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            
        request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");
    response.setContentType("text/html; charset=UTF-8");

        System.out.println("=== DEBUG doGet START ===");
        System.out.println("Request URL: " + request.getRequestURL());
        System.out.println("Query String: " + request.getQueryString());
            
        String action = request.getParameter("action");
        System.out.println("Action parameter: " + action);

        if (action == null) {
            listMotorbikes(request, response);
        } else {
            switch (action) {
                case "new":
                    showNewForm(request, response);
                    break;
                case "edit":
                    showEditForm(request, response);
                    break;
                case "delete":
                    deleteMotorbike(request, response);
                    break;
                case "filter":
                    filterMotorbikes(request, response);
                    break;
                default:
                    listMotorbikes(request, response);
                    break;
            }
        }
        System.out.println("=== DEBUG doGet END ===");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        System.out.println("=== DEBUG doPost START ===");
        System.out.println("Content-Type: " + request.getContentType());
        System.out.println("Request URL: " + request.getRequestURL());
        System.out.println("Query String: " + request.getQueryString());

        // log params
        request.getParameterMap().forEach((k, v) -> System.out.println("  " + k + ": " + String.join(", ", v)));

        String action = request.getParameter("action");
        System.out.println("Action parameter: " + action);

        try {
            if ("create".equals(action)) {
                createMotorbike(request, response);
            } else if ("update".equals(action)) {
                updateMotorbike(request, response);
            } else {
                listMotorbikes(request, response);
            }
        } finally {
            System.out.println("=== DEBUG doPost END ===");
        }
    }

    /* ------------------------ Pages ------------------------ */

    private void listMotorbikes(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("=== DEBUG listMotorbikes START ===");
        List<Motorbike> motorbikes = motorbikeAdminService.getAllMotorbikes();
        System.out.println("Retrieved " + motorbikes.size() + " motorbikes");
        request.setAttribute("motorbikes", motorbikes);
        request.getRequestDispatcher("/admin/admin-motorbikes-management.jsp").forward(request, response);
        System.out.println("=== DEBUG listMotorbikes END ===");
    }

    private void filterMotorbikes(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("=== DEBUG filterMotorbikes START ===");

        String ownerType = str(request, "ownerType"); // all | partner | admin | null
        String status = str(request, "status");       // all | available | rented | maintenance | null
        System.out.println("Filter params - ownerType: " + ownerType + ", status: " + status);

        List<Motorbike> list = motorbikeAdminService.getAllMotorbikes();

        if (ownerType != null && !"all".equalsIgnoreCase(ownerType)) {
            list = list.stream()
                    .filter(b -> ownerType.equalsIgnoreCase(
                            b.getPartnerId() != null ? "partner" : "admin"))
                    .collect(Collectors.toList());
        }
        if (status != null && !"all".equalsIgnoreCase(status)) {
            list = list.stream()
                    .filter(b -> status.equalsIgnoreCase(b.getStatus()))
                    .collect(Collectors.toList());
        }

        System.out.println("Filtered result: " + list.size() + " motorbikes");
        request.setAttribute("motorbikes", list);
        request.getRequestDispatcher("/admin/admin-motorbikes-management.jsp").forward(request, response);
        System.out.println("=== DEBUG filterMotorbikes END ===");
    }

    private void showNewForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("=== DEBUG showNewForm START ===");
        List<BikeType> bikeTypes = motorbikeAdminService.getAllBikeTypes();
        List<Partner> partners = motorbikeAdminService.getAllPartners();
        System.out.println("Retrieved " + bikeTypes.size() + " bike types");
        System.out.println("Retrieved " + partners.size() + " partners");
        request.setAttribute("bikeTypes", bikeTypes);
        request.setAttribute("partners", partners);
        request.getRequestDispatcher("/admin/admin-motorbike-form.jsp").forward(request, response);
        System.out.println("=== DEBUG showNewForm END ===");
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("=== DEBUG showEditForm START ===");
        int id = intRequired(request, "id");
        Motorbike motorbike = motorbikeAdminService.getMotorbikeById(id);
        if (motorbike == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        System.out.println("Editing motorbike ID: " + id + " (" + motorbike.getBikeName() + ")");
        request.setAttribute("motorbike", motorbike);
        request.setAttribute("bikeTypes", motorbikeAdminService.getAllBikeTypes());
        request.setAttribute("partners", motorbikeAdminService.getAllPartners());
        request.getRequestDispatcher("/admin/admin-motorbike-form.jsp").forward(request, response);
        System.out.println("=== DEBUG showEditForm END ===");
    }

    /* ------------------------ Create/Update/Delete ------------------------ */

    private void createMotorbike(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
    System.out.println("=== DEBUG createMotorbike START ===");
    
    try {
        // DEBUG: In tất cả parameters
        System.out.println("=== ALL PARAMETERS ===");
        request.getParameterMap().forEach((k, v) -> 
            System.out.println("  " + k + ": " + String.join(", ", v)));
        
        String bikeName = str(request, "bikeName");
        String licensePlate = str(request, "licensePlate");
        BigDecimal pricePerDay = bigDecimalRequired(request, "pricePerDay");
        String status = Optional.ofNullable(str(request, "status")).orElse("available");
        String description = str(request, "description");
        int typeId = intRequired(request, "typeId");

        System.out.println("=== PARSED VALUES ===");
        System.out.println("  bikeName: " + bikeName);
        System.out.println("  licensePlate: " + licensePlate);
        System.out.println("  pricePerDay: " + pricePerDay);
        System.out.println("  status: " + status);
        System.out.println("  description: " + description);
        System.out.println("  typeId: " + typeId);

        String ownerType = Optional.ofNullable(str(request, "ownerType")).orElse("admin");
        Integer partnerId = null, storeId = null;
        
        System.out.println("=== OWNER TYPE: " + ownerType + " ===");
        
        if ("partner".equalsIgnoreCase(ownerType)) {
            partnerId = intOrNull(request, "partnerId");
            System.out.println("  partnerId: " + partnerId);
            
            if (partnerId == null) {
                System.err.println("ERROR: partnerId is null for partner ownerType");
                request.setAttribute("formError", "Vui lòng chọn Đối tác cho chủ sở hữu = Đối tác.");
                request.setAttribute("bikeTypes", motorbikeAdminService.getAllBikeTypes());
                request.setAttribute("partners", motorbikeAdminService.getAllPartners());
                request.getRequestDispatcher("/admin/admin-motorbike-form.jsp").forward(request, response);
                return;
            }
        } else {
            storeId = 1; // default store
            System.out.println("  storeId: " + storeId);
        }

        // Kiểm tra biển số trùng
        System.out.println("=== CHECKING LICENSE PLATE: " + licensePlate + " ===");
        if (licensePlateExists(licensePlate)) {
            System.err.println("ERROR: Duplicate license plate: " + licensePlate);
            request.setAttribute("formError", "Biển số đã tồn tại: " + licensePlate);
            request.setAttribute("prefill_ownerType", ownerType);
            request.setAttribute("prefill_partnerId", partnerId);
            request.setAttribute("prefill_storeId", storeId);
            request.setAttribute("prefill", Map.of(
                    "bikeName", bikeName,
                    "licensePlate", licensePlate,
                    "pricePerDay", pricePerDay.toPlainString(),
                    "status", status,
                    "description", description,
                    "typeId", String.valueOf(typeId)
            ));
            request.setAttribute("bikeTypes", motorbikeAdminService.getAllBikeTypes());
            request.setAttribute("partners", motorbikeAdminService.getAllPartners());
            request.getRequestDispatcher("/admin/admin-motorbike-form.jsp").forward(request, response);
            return;
        }

        // Tạo motorbike object
        Motorbike nb = new Motorbike();
        nb.setBikeName(bikeName);
        nb.setLicensePlate(licensePlate);
        nb.setPricePerDay(pricePerDay);
        nb.setStatus(status);
        nb.setDescription(description);
        nb.setTypeId(typeId);
        nb.setPartnerId(partnerId);
        nb.setStoreId(storeId);

        System.out.println("=== BEFORE SERVICE CALL ===");
        System.out.println("  Motorbike: " + nb.getBikeName() + ", License: " + nb.getLicensePlate());
        System.out.println("  PartnerId: " + nb.getPartnerId() + ", StoreId: " + nb.getStoreId());

        boolean ok = motorbikeAdminService.addMotorbike(nb);
        System.out.println("=== SERVICE RESULT: " + ok + " ===");
        System.out.println("  New Bike ID: " + nb.getBikeId());

        if (!ok) {
            System.err.println("ERROR: Service returned false");
            response.sendRedirect(request.getContextPath() + "/admin/bikes?error=create_failed");
            return;
        }

        // Xử lý ảnh
        if (nb.getBikeId() > 0) {
            try {
                handleImageUpload(request, nb.getBikeId(), typeId, false);
            } catch (Exception e) {
                System.err.println("Image upload error: " + e.getMessage());
            }
        }

        System.out.println("=== SUCCESS - REDIRECTING ===");
        response.sendRedirect(request.getContextPath() + "/admin/bikes?success=created");
        
    } catch (Exception e) {
        System.err.println("ERROR in createMotorbike: " + e.getMessage());
        e.printStackTrace();
        response.sendRedirect(request.getContextPath() + "/admin/bikes?error=create_failed");
    }
}

    private void updateMotorbike(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        System.out.println("=== DEBUG updateMotorbike START ===");

        int bikeId = intRequired(request, "bikeId");
        String bikeName = str(request, "bikeName");
        String licensePlate = str(request, "licensePlate");
        BigDecimal pricePerDay = bigDecimalRequired(request, "pricePerDay");
        String status = Optional.ofNullable(str(request, "status")).orElse("available");
        String description = str(request, "description");
        int typeId = intRequired(request, "typeId");

        Motorbike existing = motorbikeAdminService.getMotorbikeById(bikeId);
        if (existing == null) {
            response.sendRedirect(request.getContextPath() + "/admin/bikes?error=not_found");
            System.out.println("=== DEBUG updateMotorbike END (not found) ===");
            return;
        }

        // nếu đổi biển số, kiểm tra trùng với xe khác
        if (licensePlate != null && !licensePlate.equalsIgnoreCase(existing.getLicensePlate())
                && licensePlateExists(licensePlate)) {
            request.setAttribute("formError", "Biển số đã tồn tại: " + licensePlate);
            request.setAttribute("motorbike", existing);
            request.setAttribute("bikeTypes", motorbikeAdminService.getAllBikeTypes());
            request.setAttribute("partners", motorbikeAdminService.getAllPartners());
            request.getRequestDispatcher("/admin/admin-motorbike-form.jsp").forward(request, response);
            System.out.println("=== DEBUG updateMotorbike END (duplicate) ===");
            return;
        }

        existing.setBikeName(bikeName);
        existing.setLicensePlate(licensePlate);
        existing.setPricePerDay(pricePerDay);
        existing.setStatus(status);
        existing.setDescription(description);
        existing.setTypeId(typeId);

        boolean ok = motorbikeAdminService.updateMotorbike(existing);
        System.out.println("Service result: " + ok);

        if (!ok) {
            response.sendRedirect(request.getContextPath() + "/admin/bikes?error=update_failed");
            System.out.println("=== DEBUG updateMotorbike END (service failed) ===");
            return;
        }

        // ảnh
        try {
            handleImageUpdate(request, bikeId, typeId);
        } catch (Exception e) {
            System.err.println("Image update error: " + e.getMessage());
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/admin/bikes?success=updated");
        System.out.println("=== DEBUG updateMotorbike END ===");
    }

    private void deleteMotorbike(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        System.out.println("=== DEBUG deleteMotorbike START ===");
        int id = intRequired(request, "id");
        Motorbike mb = motorbikeAdminService.getMotorbikeById(id);
        if (mb != null) {
            try {
                deleteImageFolder(mb.getBikeId(), mb.getTypeId());
            } catch (Exception e) {
                System.err.println("Delete image folder error: " + e.getMessage());
            }
        }
        boolean ok = motorbikeAdminService.deleteMotorbike(id);
        response.sendRedirect(request.getContextPath() + "/admin/bikes?" + (ok ? "success=deleted" : "error=delete_failed"));
        System.out.println("=== DEBUG deleteMotorbike END ===");
    }

    /* ------------------------ Image Handling ------------------------ */

    private String getSourceImageBasePath() {
        String currentPath = getServletContext().getRealPath("/");
        File currentDir = new File(currentPath);
        if (currentPath != null && currentPath.contains("target")) {
            File projectRoot = currentDir.getParentFile() != null ? currentDir.getParentFile().getParentFile() : null;
            if (projectRoot != null) {
                File srcWebapp = new File(projectRoot, "src" + File.separator + "main" + File.separator + "webapp");
                if (srcWebapp.exists()) return srcWebapp.getAbsolutePath();
            }
        }
        return currentPath;
    }

    private void handleImageUpload(HttpServletRequest request, int bikeId, int typeId, boolean isUpdate)
            throws Exception {
        System.out.println("=== DEBUG handleImageUpload START ===");
        Collection<Part> parts = request.getParts();
        List<Part> imageParts = parts.stream()
                .filter(p -> "images".equals(p.getName()) && p.getSize() > 0 && p.getContentType() != null && p.getContentType().startsWith("image/"))
                .collect(Collectors.toList());
        System.out.println("Total image parts: " + imageParts.size());
        if (imageParts.isEmpty()) {
            System.out.println("No image parts found");
            System.out.println("=== DEBUG handleImageUpload END ===");
            return;
        }

        String typeFolder = getTypeFolder(typeId);
        String base = getSourceImageBasePath();
        String uploadPath = base + File.separator + "images" + File.separator + "bike"
                + File.separator + typeFolder + File.separator + bikeId;
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        // tìm số tiếp theo
        File[] existing = uploadDir.listFiles((dir, name) -> name.matches("\\d+\\.jpg"));
        int next = (existing == null) ? 1 : (int) Arrays.stream(existing).count() + 1;

        for (Part part : imageParts) {
            if (next > 6) break; // tối đa 6 ảnh
            String filePath = uploadPath + File.separator + (next++) + ".jpg";
            try (InputStream in = part.getInputStream(); OutputStream out = new FileOutputStream(filePath)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
            }
        }
        System.out.println("=== DEBUG handleImageUpload END ===");
    }

    private void handleImageUpdate(HttpServletRequest request, int bikeId, int typeId) {
        System.out.println("=== DEBUG handleImageUpdate START ===");
        handleImageDeletion(request, bikeId, typeId);
        reorganizeImages(bikeId, typeId);
        try {
            handleImageUpload(request, bikeId, typeId, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("=== DEBUG handleImageUpdate END ===");
    }

    private void handleImageDeletion(HttpServletRequest request, int bikeId, int typeId) {
        System.out.println("=== DEBUG handleImageDeletion START ===");
        String[] arr = request.getParameterValues("deletedImages");
        if (arr == null || arr.length == 0) {
            System.out.println("No deletedImages parameter");
            System.out.println("=== DEBUG handleImageDeletion END ===");
            return;
        }
        List<String> indexes = new ArrayList<>();
        for (String s : arr) {
            if (s != null && !s.isBlank()) {
                for (String p : s.split(",")) {
                    String t = p.trim();
                    if (!t.isEmpty()) indexes.add(t);
                }
            }
        }
        if (indexes.isEmpty()) {
            System.out.println("No indexes to delete");
            System.out.println("=== DEBUG handleImageDeletion END ===");
            return;
        }

        String typeFolder = getTypeFolder(typeId);
        String path = getSourceImageBasePath() + File.separator + "images" + File.separator + "bike"
                + File.separator + typeFolder + File.separator + bikeId;
        for (String idx : indexes) {
            File f = new File(path, idx + ".jpg");
            if (f.exists()) {
                boolean ok = f.delete();
                System.out.println("Delete " + f.getName() + ": " + ok);
            }
        }
        System.out.println("=== DEBUG handleImageDeletion END ===");
    }

    private void reorganizeImages(int bikeId, int typeId) {
        System.out.println("=== DEBUG reorganizeImages START ===");
        String typeFolder = getTypeFolder(typeId);
        String dirPath = getSourceImageBasePath() + File.separator + "images" + File.separator + "bike"
                + File.separator + typeFolder + File.separator + bikeId;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            System.out.println("No image dir to reorganize");
            System.out.println("=== DEBUG reorganizeImages END ===");
            return;
        }
        File[] files = dir.listFiles((d, name) -> name.matches("\\d+\\.jpg"));
        if (files == null || files.length == 0) {
            System.out.println("No files to reorganize");
            System.out.println("=== DEBUG reorganizeImages END ===");
            return;
        }
        Arrays.sort(files, Comparator.comparingInt(f -> Integer.parseInt(f.getName().replace(".jpg", ""))));

        // copy qua temp để rename an toàn
        File temp = new File(dirPath + "_temp");
        if (temp.exists()) deleteFolder(temp);
        temp.mkdirs();

        for (int i = 0; i < files.length; i++) {
            File oldF = files[i];
            File newF = new File(temp, (i + 1) + ".jpg");
            try (InputStream in = new java.io.FileInputStream(oldF);
                 OutputStream out = new FileOutputStream(newF)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        deleteFolder(dir);
        boolean renamed = temp.renameTo(dir);
        System.out.println("Rename temp -> original: " + renamed);
        System.out.println("=== DEBUG reorganizeImages END ===");
    }

    private void deleteImageFolder(int bikeId, int typeId) {
        String typeFolder = getTypeFolder(typeId);
        String path = getSourceImageBasePath() + File.separator + "images" + File.separator + "bike"
                + File.separator + typeFolder + File.separator + bikeId;
        deleteFolder(new File(path));
    }

    private void deleteFolder(File folder) {
        if (folder != null && folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) for (File f : files) {
                if (f.isDirectory()) deleteFolder(f);
                else f.delete();
            }
            folder.delete();
        }
    }

    private String getTypeFolder(int typeId) {
        switch (typeId) {
            case 1: return "xe-so";
            case 2: return "xe-ga";
            case 3: return "xe-pkl";
            default: return "khac";
        }
    }
}
