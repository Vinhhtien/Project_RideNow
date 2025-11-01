🚀 RideNow - Motorbike Rental Management System

A complete online motorbike rental platform that connects customers, partners, and administrators through a secure and efficient web-based ecosystem.

📘 Overview

RideNow is a multi-role Motorbike Rental Management System developed under the SWP391 Software Project at FPT University.
The platform streamlines end-to-end motorbike rental processes — from browsing and booking to payment, verification, and returns, supporting both customers and rental partners under a unified admin supervision.

RideNow’s MVC architecture ensures clean separation of concerns, maintainable business logic, and robust database interaction with SQL Server.

🎯 Core Objectives

Simplify the process of renting and managing motorbikes online.

Provide a secure and user-friendly experience for all roles.

Support partner collaboration and admin oversight.

Improve operational transparency through analytics dashboards.

💡 Key Concept

RideNow integrates all core rental operations (searching, booking, payments, management, verification) into one cohesive ecosystem — optimizing efficiency for both customers and administrators.

⚙️ Tech Stack
🧩 Backend
Component	Technology
Language	Java 17
Framework	Jakarta EE (Servlets, JSP, JSTL)
Architecture	MVC (Model–View–Controller)
Build Tool	Apache Maven 3.9+
Application Server	Apache Tomcat 10.1.x
🗄️ Database & Persistence
Layer	Technology
Database	Microsoft SQL Server 2019+
Connection	JDBC (MSSQL JDBC Driver 12.6.1)
DAO Pattern	Custom DAO + Interface-based abstraction
Connection Utils	DBConnection.java
🔐 Security & Session
Feature	Library / Mechanism
Password Hashing	BCrypt (jBCrypt 0.4)
Session Control	HttpSession-based login tracking
Access Control	Role-based filter system (Guest, Customer, Partner, Admin)
Input Validation	Server-side + client-side (Regex, JSTL tags)
🧰 Additional Libraries

Jakarta Mail – Email verification & password recovery

Jackson Databind – JSON parsing

Apache Commons FileUpload / IO – Image uploads

Flatpickr JS – Date picker for booking UI

🧪 Testing & QA
Tool	Purpose
JUnit 5.10.0	Unit testing
Mockito 5.5.0	Mocking dependencies
AssertJ	Fluent assertions
JaCoCo 0.8.10	Code coverage reports
🧠 Key Features
👤 Authentication & User Management

Secure login / registration with BCrypt password hashing

Session management for role-based navigation

Forgot password via email verification (OTP)

Role-based access control (guest → customer / partner / admin)

🛵 Motorbike Management

Admin/Partner can add, edit, delete motorbikes

Supports image upload and categorization by bike type (scooter, manual, big bike)

License plate validation (e.g., 43E1-68932 format)

Dynamic pricing & availability management

📅 Booking & Rental Process

Customers can search bikes by type, location, price

Real-time booking calendar with start/end date validation

Automatic cost calculation (per day × duration)

Booking history (My Orders) with order status tracking

💳 Payment & Wallet

Payment gateway simulation via PayNow page

Admin verifies transactions and updates status

Option to send confirmation email upon admin approval

Refund & late fee management handled by Admin

🧾 Partner Management

Partners can manage their listed bikes and view bookings

Profit tracking per rental

Admin oversight on partner activities

🧭 Dashboard & Analytics

Admin dashboard showing total bookings, revenue, bikes, and users

Partner dashboard showing personal performance

Visual analytics integrated with JSP

🧩 Project Structure
RideNow/
├── src/
│   ├── main/java/
│   │   ├── controller/            # Servlets (BookingServlet, LoginServlet, etc.)
│   │   ├── service/               # Business logic layer + interfaces
│   │   ├── dao/                   # Data access layer (DAO interfaces + impl)
│   │   ├── model/                 # Entity models (Account, Customer, Motorbike, etc.)
│   │   ├── filter/                # Role-based access filters
│   │   ├── utils/                 # Helpers (DBConnection, EmailUtil, ValidationUtil)
│   │   └── ai/                    # AIService & Gemini integration (optional feature)
│   ├── main/webapp/
│   │   ├── auth/                  # login.jsp, register.jsp, forgot.jsp
│   │   ├── customer/              # booking.jsp, myorders.jsp, profile.jsp
│   │   ├── admin/                 # dashboard.jsp, bikes.jsp, payments.jsp
│   │   ├── partner/               # partner-dashboard.jsp, manage-bikes.jsp
│   │   ├── css/                   # admin.css, homeStyle.css, global.css
│   │   ├── js/                    # validation.js, flatpickr.js
│   │   ├── includes/              # header.jsp, footer.jsp
│   │   └── WEB-INF/web.xml        # Servlet configuration
│   └── test/java/com/ridenow/     # JUnit + Mockito test cases
├── database/
│   ├── RideNow_Schema.sql         # Database structure
│   ├── RideNow_Data.sql           # Sample seed data
│   └── RideNow_TestData.sql       # Test dataset for unit tests
├── target/
│   ├── RideNow.war                # Deployable WAR file
│   └── site/jacoco/               # Coverage reports
├── pom.xml                        # Maven configuration
└── README.md                      # This file

🚀 How to Run
Prerequisites

Ensure you have:

JDK 17+

Apache Maven 3.9+

Microsoft SQL Server 2019+

Apache Tomcat 10.1+

Git (for cloning)

Step 1: Clone the Repository
git clone https://github.com/your-username/RideNow.git
cd RideNow

Step 2: Set Up the Database

Open SQL Server Management Studio (SSMS)

Run the schema and data scripts:

source database/RideNow_Schema.sql;
source database/RideNow_Data.sql;


Configure the database connection inside DBConnection.java:

private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=RideNow;encrypt=false";
private static final String USER = "sa";
private static final String PASSWORD = "your_password";

Step 3: Build the Project
mvn clean install


Expected Output:

[INFO] BUILD SUCCESS

Step 4: Deploy to Tomcat

Copy target/RideNow.war to Tomcat/webapps/

Start Tomcat and open:
👉 http://localhost:8080/RideNow

Default Test Accounts
Role	Username	Password
Admin	admin1	1
Partner	partner1	partner123
Customer	customer1	customer123
Guest	—	Browsing only
Step 5: Run Unit Tests
mvn test


Generate coverage:

mvn jacoco:report


View report at:
target/site/jacoco/index.html

🧑‍💻 Development Team (FPT University SWP391 - Fall 2025)
Member	Student ID	Role	Responsibilities
Lê Vĩnh Tiến	SE190123	Project Lead & Backend Developer	Architecture, servlet, service & DAO design

📞 Contact & Support

GitHub Repository: RideNow Project

Email: ridenow.team@fpt.edu.vn

Instructor: traltb@fe.edu.vn
 (Course Supervisor – SWP391)

🧾 License

Educational Use Only
Developed as part of FPT University’s SWP391 Course.

✅ Free for academic and learning purposes
❌ Not for commercial distribution

🙏 Acknowledgments

FPT University for guidance and infrastructure

Mentor Team (SWP391) for valuable feedback

Open Source Community for libraries & documentation

AI Assistant (ChatGPT) for documentation and code generation support

📚 Additional Documents

📖 [API Documentation (JavaDoc)]

🧪 [Test Plan (ISTQB format)]

🗄️ [Database ERD & Schema]

🎨 [UI/UX Screens & Design Tokens]

📝 [Development Logs & AI Prompts]

🔄 Version History
Version	Date	Changes
1.0.0	Nov 2025	Final release with booking, payment, dashboard
0.9.0	Oct 2025	Added wallet & return management
0.8.0	Sep 2025	Completed customer booking flow
🌟 Roadmap

✅ Completed:

Authentication & Roles

Motorbike CRUD + Partner management

Booking & Payment workflow

Dashboard analytics

🚧 In Progress:

Email confirmation for verified payments

AI Chatbox integration for customer support



PDF invoice generation

Made with ❤️ by the RideNow Team – FPT University (SWP391)
⭐ Star this repository if you find it helpful!
