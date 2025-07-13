## 🏢 ** Central Control Server (Permission + Access Policy Core)**

### 🔹 Use:

* **Spring Boot** – main backend framework (RESTful services)
* **Spring Security** – for OAuth2, JWT-based session management
* **gRPC with Protobuf** (optional) – for efficient communication with clients
* **Open Policy Agent (OPA)** – as an external policy engine (or you can roll your own DSL engine in Java)

### 🔹 APIs Handle:

* Capsule lease issuing
* Access validation
* Trust score checking
* TTL & revocation sync
* Admin dashboard access

---

## 🗄️ **Database Layer**

| Data Type                     | Database       | Reason                     |
| ----------------------------- | -------------- | -------------------------- |
| Users, Tokens, Capsules Index | **PostgreSQL** | Relational, secure, ACID   |
| Shred Logs, Access Logs       | **MongoDB**    | Flexible schema, scalable  |
| Real-Time TTL, Trust Score    | **Redis**      | Fast, TTL support, caching |

---

## 🔐 Security Stack

* **JWT + Spring Security** – stateless authentication
* **Bouncy Castle or Tink** – for strong encryption
* **App-Scoped Key Management**:

  * Derive encryption key using app install hash + session token (from control server)
* **Capsule File**:

  * Custom file format (`.cpsx`) with:

    * Manifest
    * Encrypted data blob
    * Signature + policy metadata

---

## 🌐 DevOps & Deployment

| Layer         | Stack                                    |
| ------------- | ---------------------------------------- |
| API Hosting   | Spring Boot on Docker                    |
| DB            | PostgreSQL, MongoDB, Redis               |
| Orchestration | Docker Compose (dev) / Kubernetes (prod) |
| Logging       | ELK Stack or Prometheus + Grafana        |

---

## ✅ Summary: Java 3-Tier Capsule System Stack

| Layer       | Technology Stack                                             |
| ----------- | ------------------------------------------------------------ |
| Client App  | JavaFX + JCE/Tink + SQLite (optional)                        |
| Backend API | Spring Boot + Spring Security + PostgreSQL + Redis + MongoDB |
| Admin Panel | Spring Boot + Thymeleaf (or React + REST)                    |
| Encryption  | AES-256 (JCE / Bouncy Castle) + Capsule KDF                  |
| Policy      | JSON rules or Open Policy Agent (OPA)                        |
| DevOps      | Docker + GitHub Actions + Kubernetes (optional scaling)      |

---

Would you like a working folder structure or template Maven project to start development with JavaFX (client) + Spring Boot (server)?
