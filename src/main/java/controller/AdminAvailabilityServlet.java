package controller; // đặt ở controller cho client (detail.jsp) gọi được dễ hiểu hơn
// nếu bạn muốn giữ ở controller.admin vẫn OK vì urlPatterns mới là yếu tố quyết định.

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import utils.DBConnection;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;

/**
 * AdminAvailabilityServlet
 * Kiểm tra xe có bị trùng lịch với ĐƠN ĐÃ CONFIRMED trong [start, end] hay không.
 * Trả JSON: { ok, available, message }
 */
@WebServlet(name = "AdminAvailabilityServlet", urlPatterns = {"/availability"})
public class AdminAvailabilityServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");

        String bikeIdStr = req.getParameter("bikeId");
        String startStr = req.getParameter("start");
        String endStr = req.getParameter("end");

        try (PrintWriter out = resp.getWriter()) {
            // Validate params
            if (isBlank(bikeIdStr) || isBlank(startStr) || isBlank(endStr)) {
                out.print("{\"ok\":false,\"available\":false,\"message\":\"Thiếu tham số đầu vào.\"}");
                return;
            }

            int bikeId;
            Date start, end;
            try {
                bikeId = Integer.parseInt(bikeIdStr);
                start = Date.valueOf(startStr); // yyyy-MM-dd
                end = Date.valueOf(endStr);
            } catch (Exception ex) {
                out.print("{\"ok\":false,\"available\":false,\"message\":\"Định dạng tham số không hợp lệ.\"}");
                return;
            }

            if (end.before(start)) {
                out.print("{\"ok\":true,\"available\":false,\"message\":\"Ngày trả phải ≥ ngày nhận.\"}");
                return;
            }
            if (start.toLocalDate().isBefore(LocalDate.now())) {
                out.print("{\"ok\":true,\"available\":false,\"message\":\"Ngày nhận không được ở quá khứ.\"}");
                return;
            }

            String sql =
                    "SELECT 1 " +
                            "FROM RentalOrders r " +
                            "JOIN OrderDetails d ON d.order_id = r.order_id " +
                            "WHERE d.bike_id = ? " +
                            "  AND r.status = 'confirmed' " +
                            "  AND NOT (r.end_date < ? OR r.start_date > ?)";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, bikeId);
                ps.setDate(2, start);
                ps.setDate(3, end);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        out.print("{\"ok\":true,\"available\":false,\"message\":\"Xe đã được đặt trong khoảng ngày này.\"}");
                    } else {
                        out.print("{\"ok\":true,\"available\":true,\"message\":\"Xe còn trống.\"}");
                    }
                }
            } catch (Exception dbEx) {
                dbEx.printStackTrace();
                out.print("{\"ok\":false,\"available\":false,\"message\":\"Lỗi hệ thống.\"}");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
