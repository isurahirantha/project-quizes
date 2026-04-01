# Quiz App Backend - Spring Boot REST API

Production-ready backend for a mobile quiz application. Developed with Spring Boot 3, Java 17, and PostgreSQL.

## 🚀 Setup & Run Instructions

### 1. Prerequisites
- **Java 17** or higher
- **Maven** 3.8+
- **PostgreSQL** 14+
- (Optional) **SMTP Credentials** for email notifications (Gmail recommended)

### 2. Database Setup
1. Create a database named `quizapp_db` in PostgreSQL:
   ```sql
   CREATE DATABASE quizapp_db;
   ```
2. Update `src/main/resources/application.yml` with your PostgreSQL username and password (default is `postgres`/`postgres`).

### 3. Application Configuration
Open `src/main/resources/application.yml` and configure:
- `spring.mail.username` / `password`: Your SMTP credentials.
- `app.jwt.secret`: Use a long, random Base64 string for production.
- `app.file.upload-dir`: Where payment slips will be stored (default is `uploads` folder in project root).

### 4. Running the Application
Run the following command from the project root:
```bash
mvn spring-boot:run
```
The server will start on `http://localhost:8080`.

### 5. Accessing API Documentation
Once the app is running, visit:
**Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 🔑 Admin Credentials (Seed Data)
The system is pre-seeded with one administrator:
- **Email:** `admin@quizapp.com`
- **Password:** `Admin@123`
- **Note:** Admin MUST change password after the first login via `/api/auth/change-password`.

---

## 🛠 Features & Modules
1. **Admin Module:** JWT-based login, password change, and email-based reset.
2. **Category & Subcategory:** full tree management.
3. **Quiz Module:** Create quizzes with free/paid flags and pass marks.
4. **Question Module:** Support for Single and Multiple choice answers.
5. **Quiz Attempt:** Public flow for taking quizzes, tracking progress, and scoring.
6. **Payment System:** Public slip upload with Admin approval/rejection workflow.
7. **Access Control:** Generation of unique 8-digit codes bound to device ID.

---

## 📊 Database Design (ER Diagram)

```mermaid
erDiagram
    ADMIN {
        long id PK
        string email UK
        string password
        string full_name
        boolean must_change_password
    }
    CATEGORY {
        long id PK
        string name
        decimal price
        int validity_days
    }
    SUBCATEGORY {
        long id PK
        long category_id FK
        string name
    }
    QUIZ {
        long id PK
        long subcategory_id FK
        string title
        boolean is_free
        int pass_mark
    }
    QUESTION {
        long id PK
        long quiz_id FK
        string question_text
        string question_type
    }
    QUESTION_OPTION {
        long id PK
        long question_id FK
        string option_text
        boolean is_correct
    }
    PAYMENT {
        long id PK
        long category_id FK
        string user_email
        string status
        string payment_slip_url
    }
    ACCESS_CODE {
        long id PK
        string code UK
        long payment_id FK
        datetime expires_at
        string bound_device_id
    }
    QUIZ_ATTEMPT {
        long id PK
        long quiz_id FK
        string device_id
        decimal score_percent
        string status
    }

    CATEGORY ||--o{ SUBCATEGORY : "contains"
    SUBCATEGORY ||--o{ QUIZ : "contains"
    QUIZ ||--o{ QUESTION : "has"
    QUESTION ||--o{ QUESTION_OPTION : "has"
    PAYMENT ||--|| ACCESS_CODE : "generates"
    QUIZ ||--o{ QUIZ_ATTEMPT : "tracked by"
```
