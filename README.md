# Account Service

## Overview

Account Service is a Spring Boot-based user management system that provides user registration, authentication, role management, payment tracking, and access control. It enforces security policies such as password safety, account locking, and administrator restrictions.

## Features

- **User Registration & Authentication**: Email-based sign-up and secure login.
- **Role-Based Access Control (RBAC)**: Assign and manage user roles.
- **Administrator Role Management**: Admins can grant/revoke roles.
- **Account Locking**: Automatic locking after multiple failed login attempts.
- **Password Security**: Prevents use of compromised passwords.
- **Event Logging**: Tracks security operations and potential breaches.
- **Payment Processing**: Allows users to submit and track salary payments.

## Technologies Used

- **Java 17+**
- **Spring Boot**
- **Spring Security**
- **JPA (Hibernate)**
- **H2/PostgreSQL (Configurable Database)**
- **Lombok**
- **Jakarta Validation**
- **Caching (Spring Cache)**
- **Gradle (Build Tool)**

## Installation

### Prerequisites

- **Java 17+**
- **Gradle**

### Clone the Repository

```sh
git clone https://github.com/patorinaldi/account-service.git
cd account-service
```

### Build and Run

```sh
./gradlew clean build
./gradlew bootRun
```

## API Endpoints and Usage

### User Management

#### Register a New User

**Request**
```sh
POST /api/auth/signup
Content-Type: application/json

{
  "username": "user@acme.com",
  "password": "SecurePass123"
}
```

**Response**
```json
{
  "id": 1,
  "username": "user@acme.com",
  "roles": ["ROLE_USER"]
}
```

#### Get User Details

**Request**
```sh
GET /api/auth/user/user@acme.com
```

**Response**
```json
{
  "id": 1,
  "username": "user@acme.com",
  "roles": ["ROLE_USER"]
}
```

#### Update Password

**Request**
```sh
PATCH /api/auth/password
Content-Type: application/json

{
  "oldPassword": "SecurePass123",
  "newPassword": "NewSecurePass456"
}
```

**Response**
```json
{
  "message": "Password updated successfully."
}
```

---

### Admin Management

#### Retrieve All Users

**Request**
```sh
GET /api/admin/user
```

**Response**
```json
[
  {
    "id": 1,
    "username": "user@acme.com",
    "roles": ["ROLE_USER"]
  },
  {
    "id": 2,
    "username": "admin@acme.com",
    "roles": ["ROLE_ADMINISTRATOR"]
  }
]
```

#### Delete a User

**Request**
```sh
DELETE /api/admin/user/user@acme.com
```

**Response**
```json
{
  "message": "User deleted successfully."
}
```

#### Grant a Role to a User

**Request**
```sh
PUT /api/admin/role
Content-Type: application/json

{
  "user": "user@acme.com",
  "role": "ACCOUNTANT",
  "operation": "GRANT"
}
```

**Response**
```json
{
  "id": 1,
  "username": "user@acme.com",
  "roles": ["ROLE_USER", "ROLE_ACCOUNTANT"]
}
```

#### Lock or Unlock a User Account

**Lock a user:**
```sh
PUT /api/admin/access
Content-Type: application/json

{
  "user": "user@acme.com",
  "operation": "LOCK"
}
```

**Unlock a user:**
```sh
PUT /api/admin/access
Content-Type: application/json

{
  "user": "user@acme.com",
  "operation": "UNLOCK"
}
```

**Response**
```json
{
  "message": "User locked successfully."
}
```

---

### Payment Management

#### Submit a Salary Payment

**Request**
```sh
POST /api/acct/payments
Content-Type: application/json

{
  "employee": "user@acme.com",
  "period": "2024-02",
  "salary": 5000
}
```

**Response**
```json
{
  "id": 1,
  "employee": "user@acme.com",
  "period": "2024-02",
  "salary": 5000
}
```

#### Retrieve User Payment History

**Request**
```sh
GET /api/acct/payments?employee=user@acme.com
```

**Response**
```json
[
  {
    "id": 1,
    "employee": "user@acme.com",
    "period": "2024-02",
    "salary": 5000
  },
  {
    "id": 2,
    "employee": "user@acme.com",
    "period": "2024-03",
    "salary": 5200
  }
]
```

---

### Security Events

#### Retrieve Security Events Log

**Request**
```sh
GET /api/security/events
```

**Response**
```json
[
  {
    "id": 1,
    "event": "LOGIN_FAILED",
    "user": "user@acme.com",
    "description": "Failed login attempt",
    "timestamp": "2024-02-15T10:15:30Z"
  },
  {
    "id": 2,
    "event": "LOCK_USER",
    "user": "user@acme.com",
    "description": "User account locked after multiple failed attempts",
    "timestamp": "2024-02-15T10:20:00Z"
  }
]
```

---

## Security Measures

- **Password Protection**: Blocks the use of compromised passwords.
- **Brute Force Prevention**: Locks accounts after repeated failed logins.
- **Role Management**: Ensures only admins can modify roles.
- **Event Logging**: Tracks security actions for auditing.

## Running Tests

To execute the tests, run:

```sh
./gradlew test
```

## License

This project is licensed under the MIT License.

## Contact

For support, reach out via email or create an issue in the repository.

---
