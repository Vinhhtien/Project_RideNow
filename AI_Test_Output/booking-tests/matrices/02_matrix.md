| Test Case ID | Hàm | Loại (Happy/Edge/Error/State) | Given (Setup) | When (Hành động) | Then (Kết quả mong đợi) | Ưu tiên |
|---|---|---|---|---|---|---|
| TC-SVC-BOOK-001 | IOrderService.bookOneBike | Happy | Mock trả pricePerDay; không overlap | Gọi bookOneBike(1,10,2025-01-10,2025-01-12) | Trả orderId > 0 | P1 |
| TC-SVC-BOOK-002 | IOrderService.bookOneBike | Edge | start=end hợp lệ 1 ngày | Gọi bookOneBike(1,10,2025-01-10,2025-01-10) | Trả orderId > 0 | P2 |
| TC-SVC-BOOK-003 | IOrderService.bookOneBike | Error | start > end | Gọi bookOneBike(1,10,2025-01-12,2025-01-10) | Ném IllegalArgumentException | P1 |
| TC-SVC-BOOK-004 | IOrderService.bookOneBike | Error | DAO mock trả null price hoặc overlap=true | Gọi bookOneBike | Ném IllegalStateException (không bookable/overlap) | P1 |
| TC-SVC-AVAIL-001 | IOrderService.isBikeAvailable | Happy | Mock query trả 0 overlap | isBikeAvailable(10,2025-01-10,2025-01-12) | true | P1 |
| TC-SVC-AVAIL-002 | IOrderService.isBikeAvailable | Edge | Biên trùng ngày (end==start) | isBikeAvailable(10,2025-01-10,2025-01-10) | Kỳ vọng theo rule: true nếu không giao | P2 |
| TC-SVC-AVAIL-003 | IOrderService.isBikeAvailable | Error | Lỗi SQLException từ DAO | isBikeAvailable(...) | Ném SQLException | P1 |
| TC-SVC-OVERLAP-001 | IOrderService.getOverlappingRanges | Happy | Mock trả 2 khoảng hợp lệ, đã sort | getOverlappingRanges(10,2025-01-01,2025-01-31) | List size=2; phần tử đúng orderId/start/end | P2 |
| TC-SVC-OVERLAP-002 | IOrderService.getOverlappingRanges | Edge | Không có overlap | getOverlappingRanges(...) | Trả danh sách rỗng | P2 |
| TC-SVC-OVERLAP-003 | IOrderService.getOverlappingRanges | Error | Lỗi truy vấn | getOverlappingRanges(...) | Ném SQLException | P1 |
| TC-SVC-FINDALL-001 | IMotorbikeService.findAll | Happy | Mock DAO trả danh sách 3 xe | findAll() | Trả List size=3 | P3 |
| TC-SVC-FINDALL-002 | IMotorbikeService.findAll | Edge | DAO trả rỗng | findAll() | Trả List rỗng | P3 |
| TC-SVC-FINDALL-003 | IMotorbikeService.findAll | Error | DAO ném Exception | findAll() | Ném Exception | P3 |
| TC-SVC-FINDTYPE-001 | IMotorbikeService.findByTypeId | Happy | typeId=2 có dữ liệu | findByTypeId(2) | List size>0 | P3 |
| TC-SVC-FINDTYPE-002 | IMotorbikeService.findByTypeId | Edge | typeId không tồn tại | findByTypeId(999) | List rỗng | P3 |
| TC-SVC-FINDTYPE-003 | IMotorbikeService.findByTypeId | Error | typeId âm | findByTypeId(-1) | Ném Exception/validate lỗi | P3 |
| TC-SVC-SRCH-001 | IMotorbikeService.search | Happy | typeId=null, từ khóa “x”, page=0,size=10 | search(null,s,e,100,"x","price",0,10) | Trả list; đúng phân trang | P2 |
| TC-SVC-SRCH-002 | IMotorbikeService.search | Edge | start>end | search(..., start=2025-01-10, end=2025-01-01, ...) | Trả rỗng hoặc lỗi được xử lý | P2 |
| TC-SVC-SRCH-003 | IMotorbikeService.search | Error | sort không hợp lệ | search(..., sort="DROP TABLE", ...) | Fallback sort/ném lỗi (theo spec mock) | P2 |
| TC-SVC-COUNT-001 | IMotorbikeService.count | Happy | Đồng bộ tiêu chí với search | count(params giống SRCH-001) | Trả số nguyên >=0 | P2 |
| TC-SVC-COUNT-002 | IMotorbikeService.count | Edge | keyword rỗng/trắng | count(..., keyword="  ") | Không lỗi; trả số | P3 |
| TC-SVC-COUNT-003 | IMotorbikeService.count | Error | maxPrice âm | count(..., maxPrice=-1) | Ném Exception/0 (theo mock) | P3 |
| TC-SVC-OWNER-001 | IMotorbikeService.findAllByOwnerAccount | Happy | accountId=5, role="partner" | findAllByOwnerAccount(5,"partner") | List size>=0 | P3 |
| TC-SVC-OWNER-002 | IMotorbikeService.findAllByOwnerAccount | Edge | role null/không hợp lệ | findAllByOwnerAccount(5,null) | List rỗng | P3 |
| TC-SVC-OWNER-003 | IMotorbikeService.findAllByOwnerAccount | Error | DAO ném Exception | findAllByOwnerAccount(...) | Ném Exception | P3 |
| TC-SVC-DETAIL-001 | IMotorbikeService.getDetail | Happy | bikeId=10 có dữ liệu | getDetail(10) | Trả MotorbikeListItem | P2 |
| TC-SVC-DETAIL-002 | IMotorbikeService.getDetail | Edge | bikeId không tồn tại | getDetail(999) | Trả null | P2 |
| TC-SVC-DETAIL-003 | IMotorbikeService.getDetail | Error | DAO lỗi | getDetail(10) | Ném Exception | P2 |
| TC-SVC-PARTNER-001 | IMotorbikeService.getByPartnerId | Happy | partnerId=3 có xe | getByPartnerId(3) | List size>0 | P3 |
| TC-SVC-PARTNER-002 | IMotorbikeService.getByPartnerId | Edge | partnerId không có xe | getByPartnerId(99) | List rỗng | P3 |
| TC-SVC-PARTNER-003 | IMotorbikeService.getByPartnerId | Error | partnerId âm | getByPartnerId(-1) | Ném Exception | P3 |
| TC-CTL-CART-001 | CartServlet.doPost | Happy | session có cart; mock isBikeAvailable=true; getDetail trả item | action=add; cung cấp bikeId,start,end hợp lệ | Redirect /cart; item thêm vào cart | P1 |
| TC-CTL-CART-002 | CartServlet.doPost | Error | Thiếu bikeId hoặc ngày sai định dạng | action=add; thiếu param/format | session.book_error set; redirect /motorbikedetail?id=... | P1 |
| TC-CTL-CART-003 | CartServlet.doPost | Error | Xe bận (isBikeAvailable=false) + overlaps>0 | action=add | set book_error + book_conflicts; redirect về detail | P1 |
| TC-CTL-CART-004 | CartServlet.doPost | Edge | remove với index ngoài biên | action=remove; index=999 | Không ném lỗi; redirect /cart | P3 |
| TC-CTL-CART-005 | CartServlet.doPost | Edge | updateDates: end < start | action=saveDates; index=0; ngày đảo | session.error set; redirect /cart | P2 |
| TC-CTL-CART-006 | CartServlet.doPost | Error | checkout chưa đăng nhập | action=checkout; session.account=null | session.error; redirect /login | P1 |
| TC-CTL-CART-007 | CartServlet.doPost | Edge | checkout giỏ rỗng | action=checkout; cart rỗng | session.error; redirect /cart | P2 |
| TC-CTL-CART-008 | CartServlet.doPost | Happy | checkout thành công 100% | action=checkout; mock bookOneBike trả id>0 | redirect /paynow?orders=...; clear cart | P1 |
| TC-CTL-CART-009 | CartServlet.doPost | State | checkout thành công một phần | action=checkout; 1 success, 1 fail | session.warning set; redirect /paynow?orders=... | P2 |
| TC-CTL-CART-010 | CartServlet.doGet | Happy | cart có 2 items | doGet | Forward /cart/cart.jsp; set totals, todayISO | P2 |
| TC-CTL-CART-011 | CartServlet.doGet | Edge | cart null → khởi tạo rỗng | doGet | Forward JSP; list rỗng, totals=0 | P3 |
| TC-CTL-BOOKING-001 | BookingServlet.doPost | Error | Chưa login | doPost | Redirect /login | P1 |
| TC-CTL-BOOKING-002 | BookingServlet.doPost | Error | Bike không tồn tại | getDetail=null | resp.sendError(404) | P1 |
| TC-CTL-BOOKING-003 | BookingServlet.doPost | Error | Profile null | getProfile=null | Redirect /customer/profile?need=1 | P1 |
| TC-CTL-BOOKING-004 | BookingServlet.doPost | Happy | Hợp lệ; bookOneBike=123 | doPost | Redirect /customerorders?justCreated=123 | P1 |
| TC-CTL-BOOKING-005 | BookingServlet.doPost | Edge | Ngày sai định dạng/thiếu param | parse Date ném Exception | session.book_error set; redirect detail | P2 |
| TC-CTL-DETAIL-001 | MotorbikeDetailServlet.doGet | Edge | Thiếu cả bike_id và id | doGet | set error; forward /motorbikes/detail.jsp | P2 |
| TC-CTL-DETAIL-002 | MotorbikeDetailServlet.doGet | Error | getDetail ném Exception | doGet | set error; forward JSP | P2 |
| TC-CTL-DETAIL-003 | MotorbikeDetailServlet.doGet | Happy | Tìm thấy xe | doGet?id=10 | setAttribute("bike"); forward JSP | P3 |
| TC-CTL-ORDERS-001 | MyOrdersServlet.doGet | Error | Chưa login | doGet | Redirect /login.jsp | P1 |
| TC-CTL-ORDERS-002 | MyOrdersServlet.doGet | Error | Profile null | doGet | Redirect /customer/profile.jsp?need=1 | P1 |
| TC-CTL-ORDERS-003 | MyOrdersServlet.doGet | Happy | DAO trả rows hợp lệ | doGet | Map thành ordersVm; forward JSP | P2 |
| TC-CTL-ORDERS-004 | MyOrdersServlet.doGet | Edge | DAO ném lỗi | doGet | Xử lý an toàn (forward với list rỗng) | P2 |
| TC-CTL-ORDERS-005 | MyOrdersServlet.doGet | Edge | rows null/thiếu cột | doGet | Bỏ qua dòng lỗi; forward JSP | P3 |

**Tổng kiểm tra**

- IOrderService.bookOneBike: 4 case (TC-SVC-BOOK-001..004)
- IOrderService.isBikeAvailable: 3 case (TC-SVC-AVAIL-001..003)
- IOrderService.getOverlappingRanges: 3 case (TC-SVC-OVERLAP-001..003)
- IMotorbikeService.findAll: 3 case (TC-SVC-FINDALL-001..003)
- IMotorbikeService.findByTypeId: 3 case (TC-SVC-FINDTYPE-001..003)
- IMotorbikeService.search: 3 case (TC-SVC-SRCH-001..003)
- IMotorbikeService.count: 3 case (TC-SVC-COUNT-001..003)
- IMotorbikeService.findAllByOwnerAccount: 3 case (TC-SVC-OWNER-001..003)
- IMotorbikeService.getDetail: 3 case (TC-SVC-DETAIL-001..003)
- IMotorbikeService.getByPartnerId: 3 case (TC-SVC-PARTNER-001..003)
- CartServlet.doPost: 9 case (TC-CTL-CART-001..009)
- CartServlet.doGet: 2 case (TC-CTL-CART-010..011)
- BookingServlet.doPost: 5 case (TC-CTL-BOOKING-001..005)
- MotorbikeDetailServlet.doGet: 3 case (TC-CTL-DETAIL-001..003)
- MyOrdersServlet.doGet: 5 case (TC-CTL-ORDERS-001..005)

Tổng cộng: Service = 34 case; Servlet = 24 case; Tổng = 58 case.

