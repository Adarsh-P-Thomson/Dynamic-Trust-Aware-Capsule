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
