# RideNow — Hệ thống quản lý thuê xe máy trực tuyến

Nền tảng web quản lý thuê xe máy nhiều vai trò (Khách hàng, Đối tác, Quản trị), hỗ trợ tìm kiếm, đặt xe, thanh toán, duyệt xác minh, hoàn trả, quản lý đối tác và phân tích vận hành. Dự án thuộc học phần SWP391.

- Kiến trúc: MVC (Servlets/JSP/DAO/Service)
- Máy chủ ứng dụng: Tomcat 10.1 (Jakarta EE 10)
- CSDL: SQL Server
- Build & Test: Maven, JUnit 5, Mockito, JaCoCo
- Tích hợp: Đăng nhập Google (OAuth), Email SMTP, AI Gemini (hỏi đáp dữ liệu)

## Tính năng chính

- Xác thực & phân quyền
  - Đăng ký, đăng nhập, quên mật khẩu (mã xác minh email)
  - Băm mật khẩu bằng BCrypt
  - Phân quyền theo vai trò (customer/partner/admin) bằng Servlet Filters
- Tìm kiếm & đặt xe
  - Tìm theo loại xe, giá, tình trạng
  - Kiểm tra sẵn sàng theo khoảng ngày, tính tổng chi phí
  - Lịch sử đặt xe, theo dõi trạng thái đơn
- Quản lý xe & Đối tác
  - Thêm/sửa/xóa xe, tải ảnh, phân loại (xe số, xe ga, PKL)
  - Đối tác quản lý danh sách xe, xem lịch đặt
- Thanh toán & Ví
  - Mô phỏng thanh toán (PayNow), xác minh giao dịch bởi Admin
  - Quản lý hoàn/refund, phí trễ hạn
- Admin Dashboard & Báo cáo
  - Tổng quan doanh thu, số đơn, số xe, người dùng
  - Quản lý khách hàng, đối tác, đơn thuê, lịch nhận/trả
- Hỗ trợ AI (tùy chọn)
  - Chat “small talk” và hỏi đáp dựa trên dữ liệu (đọc-only)
  - Gemini sinh SQL an toàn (SELECT + tham số), diễn giải kết quả

## Kiến trúc & công nghệ

- Ngôn ngữ: Java 17
- Jakarta EE 10: Servlets, JSP, JSTL
- Build: Apache Maven (đóng gói WAR)
- CSDL: Microsoft SQL Server (JDBC driver mssql-jdbc)
- Thư viện:
  - Bảo mật: jBCrypt
  - Email: Jakarta Mail (SMTP Gmail)
  - JSON/HTTP: Gson, OkHttp
  - Kiểm thử: JUnit 5, Mockito, AssertJ
  - Coverage: JaCoCo

## Cấu trúc thư mục

- `src/main/java`
  - `controller/` — Servlets (ví dụ: `LoginServlet`, `BookingServlet`, `CartServlet`, `admin/...`)
  - `service/` — Lớp nghiệp vụ + interface (`IOrderService`, `MotorbikeService`, …), `service/AI/...`
  - `dao/` — Data Access Object (DAO + interface) truy vấn SQL
  - `model/` — Thực thể (Account, Customer, Motorbike, …)
  - `filter/` — `RoleFilter`, `AdminOnlyFilter` (chặn truy cập theo vai trò)
  - `utils/` — `DBConnection`, `EmailUtil`, `GoogleConstants`, `PasswordUtil`, `AI/Gemini*.java`
- `src/main/webapp`
  - JSP trang chủ/đăng nhập/đăng ký/giỏ/chi tiết, thư mục `customer/`, `partner/`, `admin/`
  - Tài nguyên tĩnh: `css/`, `images/`
  - `WEB-INF/web.xml` — cấu hình web, `home.jsp` là welcome file
- `database/RideNow_DATABASE.sql` — Schema + đối tượng DB (table, view, function)
- `pom.xml` — Cấu hình Maven (Jakarta EE, MSSQL JDBC, JUnit, Mockito, JaCoCo)
- `src/test/java/...` — Test JUnit/Mockito các lớp DAO/Service/Filter/Controller

## Yêu cầu môi trường

- JDK 17+
- Apache Maven 3.9+
- Microsoft SQL Server 2019+
- Apache Tomcat 10.1.x
- Git (tùy chọn)

## Cài đặt & chạy

1) Clone mã nguồn

```bash
git clone <repo-url>
cd Project_RideNow
```

2) Khởi tạo cơ sở dữ liệu

- Mở SQL Server Management Studio (SSMS)
- Chạy script `database/RideNow_DATABASE.sql` để tạo schema/đối tượng
- Kiểm tra tên DB khớp với cấu hình trong code (mặc định: `MotorbikeRentalDB`)

3) Cấu hình kết nối CSDL

- Sửa `src/main/java/utils/DBConnection.java` cho phù hợp môi trường:

```java
private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=MotorbikeRentalDB;encrypt=false;trustServerCertificate=true";
private static final String USER = "sa";
private static final String PASS = "<your_password>";
```

- Lưu ý bảo mật: không để mật khẩu cứng trong mã khi triển khai thật.

4) Cấu hình Email SMTP (tùy chọn cho verify/OTP)

- Sửa `src/main/java/utils/EmailUtil.java`:
  - `SMTP_USER` (gmail), `SMTP_PASS` (App Password của Gmail)
- Bật App Password (2FA) trong Google Account; dùng `session.setDebug(true)` để theo dõi log khi cần.

5) Cấu hình Đăng nhập Google (tùy chọn)

- Sửa `src/main/java/utils/GoogleConstants.java`:
  - `CLIENT_ID`, `CLIENT_SECRET` từ Google Cloud Console
  - `REDIRECT_URI` trỏ về `http://localhost:8080/Project_RideNow/logingoogle` (hoặc context bạn dùng)
- Cập nhật Authorized redirect URI trong Google Cloud cho khớp.

6) Cấu hình AI Gemini (tùy chọn)

- Đặt API key trong:
  - `src/main/java/utils/AI/GeminiClient.java`
  - `src/main/java/utils/AI/GeminiToolClient.java`
- Khuyến nghị đọc key từ biến môi trường/secret manager khi triển khai thật.

7) Build dự án

```bash
mvn clean package
```

- WAR sẽ ở: `target/Project_RideNow-1.0-SNAPSHOT.war`

8) Triển khai lên Tomcat

- Copy WAR vào `TOMCAT_HOME/webapps/`
- Khởi động Tomcat, truy cập:
  - `http://localhost:8080/Project_RideNow-1.0-SNAPSHOT/` (hoặc context do Tomcat cấu hình)
- Ứng dụng khai báo `home.jsp` là trang welcome.

## URL quan trọng

- Đăng nhập: `/login`
- Đăng ký: `/register`
- Quên mật khẩu: `/forgot`
- Tìm kiếm xe: `/motorbikesearch`
- Chi tiết xe: `/motorbikedetail?id=...`
- Giỏ/Thanh toán: `/cart`, `/checkout`
- Hồ sơ khách: `/customer/profile`
- Đối tác: quản lý qua `/motorbikes/manage` và dashboard liên quan
- Quản trị: `/admin/*` (dashboard, quản lý xe/khách/đối tác/đơn/lịch/kiểm định/hoàn trả…)
- AI Chat: `/ai/chat` (nếu bật)

Lưu ý: Truy cập `/admin/*` yêu cầu vai trò admin (lọc bởi `AdminOnlyFilter`).

## Kiểm thử & coverage

- Chạy test: `mvn test`
- Báo cáo JaCoCo: mở `target/site/jacoco/index.html`
- Kế hoạch coverage: `TEST_COVERAGE_PLAN.md`

## Gợi ý tài khoản mẫu

- Nếu script DB có seed, sử dụng tài khoản sẵn có trong `database/RideNow_DATABASE.sql`.
- Hoặc đăng ký mới ở `/register` (cần cấu hình SMTP để xác minh email).

## Ghi chú bảo mật & triển khai

- Không commit khóa API, mật khẩu DB/SMTP vào repo.
- Dùng biến môi trường/secret manager thay vì hằng số trong code.
- Bật HTTPS cho môi trường production.
- Cấu hình CORS và headers bảo mật nếu tích hợp frontend/bên thứ ba.

## Khắc phục lỗi thường gặp

- Không kết nối DB: kiểm tra `utils/DBConnection.java`, SQL Server (port 1433), `encrypt=false`/`trustServerCertificate=true` cho môi trường dev.
- Lỗi Google OAuth: kiểm tra `REDIRECT_URI` trùng với cấu hình Google Cloud.
- Không gửi được email: dùng App Password, kiểm tra log debug trong `EmailUtil`.
- Lỗi AI: kiểm tra API key Gemini, quyền mạng và hạn mức.

## Bản quyền & giấy phép

Dự án học thuật SWP391 — sử dụng cho mục đích học tập/trình diễn nếu không ghi khác.

