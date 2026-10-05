# EduFrame Subsystems & Design Pattern Specification

## Overview
This document specifies the software design pattern assigned to each of the 6 main EduFrame system components based on the lecture series (**Design Patterns Part 01 & Part 02**) and the **EduFrame System Architecture Design Document**.

---

## Component & Design Pattern Mapping Table

| Subsystem | Component Name | Group Member & IT Number | Design Pattern | Pattern Category |
| :--- | :--- | :--- | :--- | :--- |
| **UC-01** | Announcement & Event System | De Silva L. C. A. (`IT25101841`) | **Observer Pattern** | Behavioral |
| **UC-02** | Support Ticketing System | Munasinghe M. H. V. S. (`IT25102695`) | **Chain of Responsibility Pattern** | Behavioral |
| **UC-03** | Advertisement Management | Bandara W. G. M. D. (`IT25103702`) | **Singleton Pattern** | Creational |
| **UC-04** | Course & Curriculum Management | Withana T. T. (`IT25101767`) | **Composite Pattern** | Structural |
| **UC-05** | Video Management System | Wickramathunga P. P. (`IT25103673`) | **Strategy Pattern** | Behavioral |
| **UC-06** | Quiz & Assessment Subsystem | Adikari A. M. T. D. (`IT25100859`) | **Factory Method Pattern** | Creational |

---

## Subsystem Details & Technical Implementation

### 1. Announcement & Event System (UC-01)
- **Assigned Member:** De Silva L. C. A. (`IT25101841`)
- **Design Pattern:** **Observer Pattern**
- **Type:** Behavioral Pattern
- **Description:** Whenever a teacher creates or updates a time-sensitive course event or announcement, the system notifies all subscribed student dashboards automatically.

---

### 2. Support Ticketing System (UC-02)
- **Assigned Member:** Munasinghe M. H. V. S. (`IT25102695`)
- **Design Pattern:** **Chain of Responsibility Pattern**
- **Type:** Behavioral Pattern
- **Description:** Incoming technical tickets are passed sequentially down a handler chain (`AutoBotHandler` -> `StaffHandler` -> `AdminHandler`) until a handler resolves the issue or escalates it.

---

### 3. Advertisement Management System (UC-03)
- **Assigned Member:** Bandara W. G. M. D. (`IT25103702`)
- **Design Pattern:** **Singleton Pattern**
- **Type:** Creational Pattern
- **Description:** Ensures a single global manager manages banner allocation, rotation schedules, visibility triggers, and time-bound advertisement expiration.

---

### 4. Course & Curriculum Management System (UC-04)
- **Assigned Member:** Withana T. T. (`IT25101767`)
- **Design Pattern:** **Composite Pattern**
- **Type:** Structural Pattern
- **Description:** Treats individual resources (videos, syllabus PDFs) and groups of resources (modules, sub-modules) uniformly under a single component hierarchy interface.

---

### 5. Video Management System (UC-05)
- **Assigned Member:** Wickramathunga P. P. (`IT25103673`)
- **Design Pattern:** **Strategy Pattern**
- **Type:** Behavioral Pattern
- **Description:** Encapsulates media upload, file validation, and format processing logic (`VideoUploadStrategy`, `ImageUploadStrategy`) into interchangeable strategy implementations.

---

### 6. Quiz & Assessment Subsystem (UC-06)
- **Assigned Member:** Adikari A. M. T. D. (`IT25100859`)
- **Design Pattern:** **Factory Method Pattern**
- **Type:** Creational Pattern
- **Description:** Dynamically instantiates the appropriate evaluation/grading handler (`McqGradingStrategy`, `TrueFalseGradingStrategy`, `ShortAnswerGradingStrategy`) based on the submitted question type.
