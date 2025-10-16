// AN
package controller.partner;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

import model.Account;
import model.Partner;
import model.Motorbike;
import model.MotorbikeListItem;
import service.PartnerService;
import service.IPartnerService;
import service.IMotorbikeService;
import service.MotorbikeService;


@WebServlet(name = "PartnerServlet", urlPatterns = {"/partner"})
public class PartnerServlet extends HttpServlet {

    private final IPartnerService partnerService = new PartnerService();
    private final IMotorbikeService motorbikeService = new MotorbikeService(); // hiện chưa dùng, giữ nguyên

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null || !"partner".equalsIgnoreCase(acc.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        if (action == null) action = "profile";

        try {
            switch (action) {
                case "bikes": {
                    Partner p = partnerService.getByAccountId(acc.getAccountId());
                    if (p == null) {
                        resp.sendRedirect(req.getContextPath() + "/login");
                        return;
                    }
                    List<Motorbike> bikes = partnerService.getMyBikes(p.getPartnerId());
                    // set cả 2 tên attribute để tương thích mọi JSP
                    req.setAttribute("bikes", bikes);
                    req.setAttribute("motorbikes", bikes);
                    req.getRequestDispatcher("/partners/bikelist.jsp").forward(req, resp);
                    break;
                }
                case "bikeDetail": {
                    int bikeId = Integer.parseInt(req.getParameter("id"));
                    MotorbikeListItem bike = partnerService.getBikeDetails(bikeId);
                    req.setAttribute("bike", bike);
                    req.getRequestDispatcher("/partners/bikelist.jsp").forward(req, resp);
                    break;
                }
                case "profile": {
                    Partner p = partnerService.getByAccountId(acc.getAccountId());
                    req.setAttribute("partner", p);
                    req.getRequestDispatcher("/partners/dashboard.jsp").forward(req, resp);
                    break;
                }
                case "editProfile": {
                    Partner p = partnerService.getByAccountId(acc.getAccountId());
                    req.setAttribute("partner", p);
                    req.getRequestDispatcher("/partners/profile_edit.jsp").forward(req, resp);
                    break;
                }
                case "reviews": {
                    // nếu card trên dashboard trỏ về /partner?action=reviews
                    resp.sendRedirect(req.getContextPath() + "/viewreviewservlet");
                    break;
                }
                default: {
                    Partner p = partnerService.getByAccountId(acc.getAccountId());
                    req.setAttribute("partner", p);
                    req.getRequestDispatcher("/partners/dashboard.jsp").forward(req, resp);
                    break;
                }
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null || !"partner".equalsIgnoreCase(acc.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        if ("updateProfile".equals(action)) {
            try {
                req.setCharacterEncoding("UTF-8"); // đọc tiếng Việt an toàn

                // --- Parse partnerId chịu lỗi + fallback theo accountId ---
                int partnerId = 0;
                String raw = req.getParameter("partnerId");
                if (raw != null && !raw.trim().isEmpty()) {
                    try { partnerId = Integer.parseInt(raw.trim()); } catch (NumberFormatException ignored) {}
                }
                if (partnerId == 0) {
                    Partner cur = partnerService.getByAccountId(acc.getAccountId());
                    if (cur != null) {
                        partnerId = cur.getPartnerId();
                    }
                }

                // --- Validate SĐT: 10 số, bắt đầu bằng 0 ---
                String phoneRaw = req.getParameter("phone");
                String phoneDigits = phoneRaw == null ? "" : phoneRaw.replaceAll("\\D", "").trim();
                if (!phoneDigits.matches("^0\\d{9}$")) {
                    req.setAttribute("error", "Số điện thoại không hợp lệ: phải đủ 10 số và bắt đầu bằng 0.");

                    Partner back = new Partner();
                    back.setPartnerId(partnerId);
                    back.setAccountId(acc.getAccountId());
                    back.setFullname(req.getParameter("fullName"));
                    back.setPhone(phoneRaw);
                    back.setAddress(req.getParameter("address"));
                    req.setAttribute("partner", back);

                    req.getRequestDispatcher("/partners/profile_edit.jsp").forward(req, resp);
                    return;
                }

                // --- Update Partner info ---
                Partner p = new Partner();
                p.setPartnerId(partnerId);
                p.setAccountId(acc.getAccountId());
                p.setFullname(safeTrim(req.getParameter("fullName")));
                p.setPhone(phoneDigits);
                p.setAddress(safeTrim(req.getParameter("address")));

                boolean okInfo = partnerService.updateAccountInfo(p);

                // --- Update username (nếu có nhập) ---
                String accountName = safeTrim(req.getParameter("accountName"));
                boolean okName = true;
                if (accountName != null && !accountName.isEmpty()) {
                    boolean updated = partnerService.updateAccountName(acc.getAccountId(), accountName);
                    okName = updated;
                    if (updated) {
                        req.getSession().setAttribute("partnerName", accountName);
                        Object obj = req.getSession().getAttribute("account");
                        if (obj instanceof Account) {
                            try {
                                ((Account) obj).setUsername(accountName);
                                req.getSession().setAttribute("account", obj);
                            } catch (Throwable ignore) { /* no-op */ }
                        }
                    } else {
                        req.setAttribute("error", "Cập nhật tên tài khoản thất bại.");
                    }
                }

                if (okInfo && okName) {
                    req.setAttribute("msg", "Cập nhật thông tin thành công");
                } else if (!okInfo) {
                    req.setAttribute("error", "Cập nhật thông tin cửa hàng thất bại.");
                }

                Partner updatedPartner = partnerService.getByAccountId(acc.getAccountId());
                req.setAttribute("partner", updatedPartner);

                req.getRequestDispatcher("/partners/profile_edit.jsp").forward(req, resp);
            } catch (Exception e) {
                throw new ServletException(e);
            }

        } else if ("updatePassword".equals(action)) {
            try {
                req.setCharacterEncoding("UTF-8");

                String newPwd = req.getParameter("newPassword");
                String confirm = req.getParameter("confirmPassword");
                if (newPwd == null) newPwd = "";
                if (confirm == null) confirm = "";

                boolean strong = newPwd.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");
                if (!strong) {
                    req.setAttribute("pwd_error", "Mật khẩu chưa đạt yêu cầu (≥8 ký tự, có chữ in hoa, số và ký tự đặc biệt).");
                } else if (!newPwd.equals(confirm)) {
                    req.setAttribute("pwd_error", "Xác nhận mật khẩu không khớp.");
                } else {
                    boolean ok = partnerService.updatePassword(acc.getAccountId(), newPwd);
                    if (ok) req.setAttribute("pwd_msg", "Cập nhật mật khẩu thành công.");
                    else     req.setAttribute("pwd_error", "Cập nhật mật khẩu thất bại.");
                }

                Partner p = partnerService.getByAccountId(acc.getAccountId());
                req.setAttribute("partner", p);

                req.getRequestDispatcher("/partners/profile_edit.jsp").forward(req, resp);
            } catch (Exception e) {
                throw new ServletException(e);
            }

        } else {
            doGet(req, resp);
        }
    }

    private String safeTrim(String s) {
        return s == null ? null : s.trim();
    }
}
