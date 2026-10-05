# EduFrame — Learning Management System (LMS)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4%20%2F%204.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Gradle](https://img.shields.io/badge/Gradle-8.x-blue.svg)](https://gradle.org/)
[![Database](https://img.shields.io/badge/Database-MS%20SQL%20Server-red.svg)](https://www.microsoft.com/sql-server)

**Course:** SE2030 — Software Engineering  
**Academic Period:** Year 2, Semester 1  
**Group ID:** `Y2-S1-MLB-B4G1-06`  
**Institution:** Sri Lanka Institute of Information Technology (SLIIT)  

---

## 📋 Table of Contents
1. [Executive Summary](#-executive-summary)
2. [Software Architecture & Design Patterns](#-software-architecture--design-patterns)
3. [Core Subsystems & Features](#-core-subsystems--features)
4. [Technology Stack](#-technology-stack)
5. [Project Structure](#-project-structure)
6. [Database Schema & Configuration](#-database-schema--configuration)
7. [Prerequisites & Setup Guide](#-prerequisites--setup-guide)
8. [Running the Application](#-running-the-application)
9. [User Roles & Authentication](#-user-roles--authentication)
10. [Testing & Verification](#-testing--verification)
11. [Project Team & Contributions](#-project-team--contributions)

---

## 🚀 Executive Summary

**EduFrame** is an enterprise-grade Learning and Education Management Platform designed to streamline course delivery, student assessment, content management, technical support, and institutional communications. 

Built on Java 17 and Spring Boot, EduFrame emphasizes robust software architecture by adhering to Gang of Four (GoF) design patterns across six core functional subsystems. The platform provides tailored web dashboards for **Administrators**, **Teachers**, and **Students**.

---

## 🏗️ Software Architecture & Design Patterns

The EduFrame system architecture integrates six distinct GoF design patterns across its core subsystems:

| Subsystem | Component Name | Group Member & IT Number | Design Pattern | Pattern Category |
| :--- | :--- | :--- | :--- | :--- |
| **UC-01** | Announcement & Event System | De Silva L. C. A. (`IT25101841`) | **Observer Pattern** | Behavioral |
| **UC-02** | Support Ticketing System | Munasinghe M. H. V. S. (`IT25102695`) | **Chain of Responsibility Pattern** | Behavioral |
| **UC-03** | Advertisement Management | Bandara W. G. M. D. (`IT25103702`) | **Singleton Pattern** | Creational |
| **UC-04** | Course & Curriculum Management | Withana T. T. (`IT25101767`) | **Composite Pattern** | Structural |
| **UC-05** | Video Management System | Wickramathunga P. P. (`IT25103673`) | **Strategy Pattern** | Behavioral |
| **UC-06** | Quiz & Assessment Subsystem | Adikari A. M. T. D. (`IT25100859`) | **Factory Method Pattern** | Creational |

### Detailed Design Pattern Implementations

#### 1. Observer Pattern (UC-01: Announcements & Events)
- **Objective:** Real-time event notifications across student dashboards.
- **Implementation:** When an instructor publishes an announcement or schedules a course deadline, subject event publishers push updates to registered student observers automatically.

#### 2. Chain of Responsibility Pattern (UC-02: Support Ticketing)
- **Objective:** Automated and tiered technical support ticket escalation.
- **Implementation:** Incoming student support requests pass through a sequential handler chain:
  `AutoBotHandler` $\rightarrow$ `StaffHandler` $\rightarrow$ `AdminHandler`. Each handler either resolves the issue or escalates it to the next tier.

#### 3. Singleton Pattern (UC-03: Advertisement Management)
- **Objective:** Global state synchronization for promotional banners and announcements.
- **Implementation:** Maintains a single instance of the advertisement manager to control banner placement, impression counts, visibility schedules, and automated time-bound expiration.

#### 4. Composite Pattern (UC-04: Course & Curriculum Management)
- **Objective:** Hierarchical structured course tree representation.
- **Implementation:** Uniformly treats individual resource components (lecture slides, videos, external links) and container nodes (modules, sub-modules, chapters) under a single curriculum component interface.

#### 5. Strategy Pattern (UC-05: Video & Media Upload Management)
- **Objective:** Flexible and decoupled media processing pipelines.
- **Implementation:** Encapsulates media upload and validation logic into interchangeable strategies (`VideoUploadStrategy`, `ImageUploadStrategy`), allowing dynamic selection based on file type and bandwidth parameters.

#### 6. Factory Method Pattern (UC-06: Quiz & Assessment Subsystem)
- **Objective:** Extensible grading engine for dynamic evaluation.
- **Implementation:** Instantiates appropriate evaluation strategies (`McqGradingStrategy`, `TrueFalseGradingStrategy`, `ShortAnswerGradingStrategy`) based on question metadata submitted during quiz attempts. Automates PDF certificate generation via `iTextPDF` upon quiz completion.

---

## 📦 Core Subsystems & Features

- **Course Management:** Module creation, syllabus breakdown, file attachment management, and student enrollment tracking.
- **Quiz & Assessment Engine:** Multiple question types (MCQ, True/False, Short Answer), timed quiz attempts, automated grading, and instant PDF certificate generation.
- **Media Upload Manager:** Supports video and image uploads up to 500MB with progress tracking and media rendering.
- **Support Ticket Portal:** Multi-tiered ticket creation, status updates (Open, In-Progress, Resolved, Escalated), and resolution tracking.
- **Institutional Advertisements:** Banner management, active impression tracking, expiry dates, and audience targeting.
- **Announcements & Notifications:** Course-level alerts, system broadcasts, and student notification bell integration.

---

## 🛠️ Technology Stack

- **Language:** Java 17 (JDK 17)
- **Framework:** Spring Boot 4.1.0 / 3.4.x
- **Security:** Spring Security (Form-based Auth, Role-based Access Control)
- **Persistence & ORM:** Spring Data JPA / Hibernate
- **Database:** Microsoft SQL Server (Production/Lab) & H2 (In-memory Testing)
- **Templating Engine:** Thymeleaf with Spring Security 6 integration
- **PDF Generation:** iText PDF 5.5.13.3
- **Utilities & Tools:** Project Lombok, Gradle Wrapper

---

## 📁 Project Structure

```text
eduframe/
├── docs/                                  # Project documentation & design diagrams
│   └── EduFrame_Design_Patterns_Mapping.md
├── src/
│   ├── main/
│   │   ├── java/com/eduframepackage/eduframe/
│   │   │   ├── config/                    # SecurityConfig, WebConfig
│   │   │   ├── controller/                # Page Controllers, Admin APIs, Advertisements
│   │   │   ├── model/                     # Course, Module, User entities
│   │   │   ├── repository/                # Spring Data JPA Repositories
│   │   │   ├── service/                   # Business Logic & Services
│   │   │   ├── strategy/                  # Media Upload Strategies (Strategy Pattern)
│   │   │   └── quiz/                      # Quiz Subsystem (UC-06)
│   │   │       ├── controller/            # StudentQuizController, TeacherQuizController
│   │   │       ├── entity/                # Quiz, Question, Attempt, Certificate entities
│   │   │       ├── event/                 # QuizPassedEvent, CertificateEventListener
│   │   │       └── service/grading/       # Grading Strategy Factory & Implementations
│   │   └── resources/
│   │       ├── application.properties     # App configuration & DB settings
│   │       ├── schema.sql                 # SQL DDL Script
│   │       ├── static/                    # CSS, JS, Images, Media assets
│   │       └── templates/                 # Thymeleaf HTML Templates
│   └── test/                              # Unit & Integration Tests
├── build.gradle                           # Gradle dependencies & build configuration
├── schema.sql                             # Database schema backup
└── README.md                              # Main project documentation
```

---

## 🗄️ Database Schema & Configuration

EduFrame uses Microsoft SQL Server configured via `src/main/resources/application.properties`.

### Default Datasource Settings
```properties
server.port=8081

app.db.host=${DB_HOST:localhost}
app.db.port=${DB_PORT:1433}
app.db.name=${DB_NAME:EduFrame-db}
app.db.username=${DB_USERNAME:sa}
app.db.password=${DB_PASSWORD:Ashendra@200308}

spring.datasource.url=jdbc:sqlserver://${app.db.host}:${app.db.port};databaseName=${app.db.name};encrypt=true;trustServerCertificate=true;sendTimeAsDatetime=false;
spring.jpa.hibernate.ddl-auto=update
```

---

## ⚙️ Prerequisites & Setup Guide

### 1. Requirements
- **Java Development Kit (JDK):** Version 17 or higher
- **Database:** Microsoft SQL Server 2019+ or Azure SQL DB
- **IDE:** IntelliJ IDEA / Eclipse / VS Code with Java extension pack

### 2. Database Preparation
1. Create a database named `EduFrame-db` in your MSSQL Server instance.
2. Ensure SQL Server Authentication is enabled for user `sa` (or update credentials in `application.properties`).
3. Run `schema.sql` if manual table setup is desired (Spring Boot will auto-initialize schema on startup).

---

## 🏃 Running the Application

### Using Gradle Wrapper

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/ashendrade/EduFrame-SE2030-B4G1-06.git
   cd eduframe
   ```

2. **Build the Project:**
   ```bash
   ./gradlew build -x test
   ```

3. **Run the Application:**
   ```bash
   ./gradlew bootRun
   ```

4. **Access the Web Interface:**
   Open your browser and navigate to:
   ```text
   http://localhost:8081
   ```

---

## 👤 User Roles & Authentication

EduFrame includes default users seeded automatically on first startup:

| Role | Username / Email | Default Password | Permissions |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | Full system control, user management, banner allocation, ticket escalation |
| **Teacher** | `teacher` | `teacher123` | Course management, quiz creation, event publishing, certificate templates |
| **Student** | `student` | `student123` | Course enrollment, quiz attempts, certificate downloads, support tickets |

---

## 🧪 Testing & Verification

Run the full automated JUnit test suite using:

```bash
./gradlew test
```

Test reports will be generated in `build/reports/tests/test/index.html`.

---

## 👥 Project Team & Contributions

**Group ID:** `Y2-S1-MLB-B4G1-06`  
**Module:** SE2030 - Software Engineering  

| Student Name | Registration No / IT No | Assigned Subsystem & Pattern |
| :--- | :--- | :--- |
| **De Silva L. C. A.** | `IT25101841` | Announcement & Event System (**Observer Pattern**) |
| **Munasinghe M. H. V. S.** | `IT25102695` | Support Ticketing System (**Chain of Responsibility Pattern**) |
| **Bandara W. G. M. D.** | `IT25103702` | Advertisement Management (**Singleton Pattern**) |
| **Withana T. T.** | `IT25101767` | Course & Curriculum Management (**Composite Pattern**) |
| **Wickramathunga P. P.** | `IT25103673` | Video Management System (**Strategy Pattern**) |
| **Adikari A. M. T. D.** | `IT25100859` | Quiz & Assessment Subsystem (**Factory Method Pattern**) |

---
*Developed for SLIIT SE2030 Software Engineering Project — 2026.*

