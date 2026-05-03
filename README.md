# Smart Queue Management System

Smart Queue Management System is a full-stack Java web application for managing service queues using role-based access, token generation, staff counter operations, live queue displays, and admin reports.


## Features

- Role-based authentication and authorization
- Admin, Staff, and User modules
- Dynamic category management
- Counter management with open, pause, and close status
- User registration and login
- Category-wise token generation
- Smart token calling based on priority and issue time
- Priority support: Emergency, Senior Citizen, VIP, Normal
- Estimated wait time calculation
- Queue logs for generated, called, recalled, skipped, and completed actions
- Live public queue dashboard with auto-refresh
- Reports and analytics dashboard with Chart.js
- Date and category filters for reports
- Printable reports
- Responsive white, gray, and black professional UI

## Tech Stack

- Java 21
- Maven
- Spring Boot
- Spring MVC
- Spring Security
- Spring Data JPA
- Hibernate
- MySQL
- Thymeleaf
- Bootstrap
- JavaScript
- Chart.js

## Project Structure

```text
src/main/java/com/smartqueue
  config        Spring Security and startup seed data
  controller    MVC controllers for admin, staff, user, auth, and public pages
  dto           Form DTOs, dashboard DTOs, and report DTOs
  entity        JPA entities and enums
  exception     Global exception handling
  repository    Spring Data JPA repositories
  security      Custom user details and login success handling
  service       Business logic for queue, reports, users, and live dashboard
```

## Database Setup

1. Install and start MySQL.
2. Create the database:

```sql
CREATE DATABASE smart_queue_db;
```

3. Update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smart_queue_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```

4. Sample categories and counters are loaded from:

```text
src/main/resources/data.sql
```

## How To Run

Use Java 21 and Maven:

```bash
mvn spring-boot:run
```

Open the application:

```text
http://localhost:8080
```

## Default Credentials

Admin:

```text
Email: admin@smartqueue.local
Password: admin123
```

Staff:

```text
Email: staff@smartqueue.local
Password: staff123
```

Users can register from:

```text
http://localhost:8080/register
```

## Main URLs

- Home: `http://localhost:8080/`
- Login: `http://localhost:8080/login`
- User Dashboard: `http://localhost:8080/user/dashboard`
- My Token: `http://localhost:8080/user/my-token`
- Staff Dashboard: `http://localhost:8080/staff/dashboard`
- Admin Dashboard: `http://localhost:8080/admin/dashboard`
- Categories: `http://localhost:8080/admin/categories`
- Counters: `http://localhost:8080/admin/counters`
- Tokens: `http://localhost:8080/admin/tokens`
- Reports: `http://localhost:8080/admin/reports`
- Live Queue: `http://localhost:8080/live`

## Screenshot Placeholders

Add screenshots here before final submission:

### Home Page

`screenshots/home-page.png`

### Login Page

`screenshots/login-page.png`

### User Token Generation

`screenshots/user-dashboard.png`

### Staff Queue Operations

`screenshots/staff-dashboard.png`

### Admin Dashboard

`screenshots/admin-dashboard.png`

### Reports Dashboard

`screenshots/reports-dashboard.png`

### Live Queue Display

`screenshots/live-queue.png`

## Notes For Submission

- The project uses DTOs for form input and dashboard/report views.
- Validation is handled through Jakarta Bean Validation annotations.
- Global exception handling is implemented in `GlobalExceptionHandler`.
- Queue actions are logged in `QueueLog`.
- The UI follows a minimal white, gray, and black professional theme.
- Change default credentials before deploying outside local development.
