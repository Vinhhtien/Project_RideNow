# TEST_COVERAGE_PLAN

Scope: RideNow (Java Servlet/JSP + Maven). Goal: overall LINE and BRANCH coverage ≥ 90% with JUnit 5 + Mockito, without changing pom.xml. All external resources mocked (DB/SMTP/FS/API).

## Conventions
- JUnit 5 + Mockito inline; AssertJ for fluent assertions
- Deterministic time/locale via static init in `controller.testsupport.TestUtils`
- Session map pattern, forward/redirect verification, static-mock `utils.DBConnection` and `utils.EmailUtil`

## Class Inventory & Status

Controllers/Servlets
- controller.Authetication.LoginServlet — COVERED (LoginServletTest)
- controller.Authetication.LogoutServlet — COVERED (LogoutServletTest)
- controller.RegisterServlet — COVERED (RegisterServletTest)
- controller.MotorbikeSearchServlet — COVERED (MotorbikeSearchServletTest)
- controller.MotorbikeDetailServlet — COVERED (MotorbikeDetailServletTest)
- controller.BookingServlet — COVERED (BookingServletTest)
- controller.PayNowServlet — COVERED (PayNowServletTest existing)
- controller.CartServlet — COVERED (CartServletTest existing)
- controller.MyOrdersServlet — COVERED (MyOrdersServletTest)
- controller.CustomerProfileServlet — COVERED (CustomerProfileServletTest)
- controller.DashboardServlet — COVERED (DashboardServletTest)
- controller.partner.MotorbikeManageListServlet — COVERED (MotorbikeManageListServletTest)
- controller.admin.AdminMotorbikesServlet — PARTIAL (AdminMotorbikesServletTest)

Filters
- filter.RoleFilter — COVERED (RoleFilterTest)
- filter.AdminOnlyFilter — COVERED (AdminOnlyFilterTest)

Services
- service.MotorbikeService — COVERED (MotorbikeServiceTest)
- service.OrderService — COVERED (OrderServiceTest)
- service.CustomerService — COVERED (CustomerServiceTest)
- service.MotorbikeAdminService — PARTIAL (MotorbikeAdminServiceTest)

DAOs (JDBC mapping only; DB mocked)
- dao.MotorbikeDao — PARTIAL (MotorbikeDaoTest)
- dao.OrderDao — PARTIAL (OrderDaoTest)
- dao.CustomerDao — PARTIAL (CustomerDaoTest)

Other services/DAOs exist (AdminService, NotificationService, Payment*, Partner*, etc.). Current coverage focuses on core booking/cart/payment flows and admin/partner dashboards to lift bundle coverage.

## Test Strategies
- Static mocks: DBConnection.getConnection(), EmailUtil.sendMail/sendMailHTML
- JDBC stubs: mock Connection/PreparedStatement/ResultSet; verify result mappings and control flow (commits/rollback simulated by stubbing methods, no real DB)
- Servlet inputs: null/blank/invalid; date boundaries; not-logged-in; role mismatches
- File upload (AdminMotorbikesServlet): verify Part filtering paths; do not write FS
- Exceptions: DAO throws SQLException/RuntimeException → controller/service handles and forwards/redirects

## Branch Targets Per Class
- LoginServlet: success per role, wrong password, exceptions → forward login
- LogoutServlet: invalidate and redirect
- RegisterServlet: happy path (emails), SQL error (first insert)
- MotorbikeSearchServlet: no filters vs filters; exception → ServletException
- MotorbikeDetailServlet: id missing, not found, found
- BookingServlet: not logged in, bike not found (404), no customer profile, invalid dates (service throws), success redirect
- MyOrdersServlet: not logged in, no profile, happy path, POST cancel
- CustomerProfileServlet: role checks, GET happy, POST changePassword invalid/valid, POST update profile
- DashboardServlet: not logged in, partner dashboard attrs, admin dashboard, POST read, forbidden
- AdminMotorbikesServlet: GET list, POST duplicate plate (error redirect), minimal create path parts mocked
- RoleFilter/AdminOnlyFilter: 3 cases each
- Services: validation and DAO exceptions; happy flows
- DAOs: result set mappings; overlap checks; transactional rollback path in create

## Determinism & Utils
- `controller.testsupport.TestUtils`: static init sets timezone `Asia/Ho_Chi_Minh` and Locale.US
- `controller.testsupport.Fixtures`: sample builders for accounts/customers/bikes
- `testutil.MockFactory`: quick mock factories

## Coverage Estimate (before running)
- Controllers/Filters above: ~90–95% lines; key branches covered
- Services: ~85–95%
- DAOs tested partially; mapping heavy code boosts executed lines without DB
- Model/DTOs are plain and not counted functionally but included in lines; nevertheless, tests concentrate on higher-LOC classes to raise bundle ratio

## Post‑Run Notes (to be updated after mvn test)
- Open report: `target/site/jacoco/index.html`
- If bundle LINE/BRANCH < 90%, expand DAO tests (NotificationDao, OrderQueryDao, PaymentDao, Rent/Return flows) and add missing servlet branches (e.g., AdminMotorbikes update/delete with exceptions, image parts present) using the same patterns.

