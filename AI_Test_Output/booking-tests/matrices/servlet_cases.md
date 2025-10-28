**Servlet Booking — Test Matrix**

| ID | Servlet | Scenario | Inputs/Session | Expected | Mocks |
|---|---|---|---|---|---|
| V1 | BookingServlet.doPost | Not logged in | session.account=null | Redirect to /login | HttpServlet*, no service calls |
| V2 | BookingServlet.doPost | Bike not found | bikeId present; service.getDetail=null | resp.sendError(404, ...) | Mock IMotorbikeService.getDetail |
| V3 | BookingServlet.doPost | No customer profile | account present; getProfile=null | Redirect /customer/profile?need=1 | Mock ICustomerService.getProfile |
| V4 | BookingServlet.doPost | Success booking | Valid inputs; bookOneBike=123 | Redirect /customerorders?justCreated=123 | Mock IOrderService.bookOneBike |
| V5 | BookingServlet.doPost | Business error | bookOneBike throws | session.book_error set; redirect detail | Mock IOrderService exception |
| V6 | MotorbikeDetailServlet.doGet | Missing id | No bike_id/id params | Forward to /motorbikes/detail.jsp with error | Mock RequestDispatcher |
| V7 | MotorbikeDetailServlet.doGet | Not found | service.getDetail returns null | error set; forward JSP | Mock IMotorbikeService.getDetail |
| V8 | MotorbikeDetailServlet.doGet | Service error | service throws | error set; forward JSP | Mock IMotorbikeService exception |
| V9 | MyOrdersServlet.doGet | Not logged in | session.account=null | Redirect /login.jsp | HttpServlet*, no DAO calls |
| V10 | MyOrdersServlet.doGet | Profile missing | getProfile=null | Redirect /customer/profile.jsp?need=1 | Mock ICustomerService.getProfile |
| V11 | MyOrdersServlet.doGet | Happy path | rows valid | Set attributes; forward JSP | Mock IOrderQueryDao returns rows |
| V12 | MyOrdersServlet.doGet | DAO error | qdao throws | Forward JSP with empty list (helper swallows), or adjust to redirect if surfacing error | Mock IOrderQueryDao exception |
