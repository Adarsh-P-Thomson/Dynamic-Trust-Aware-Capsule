## 🔷 **Client App (Capsule Viewer/Handler)**

### 🔹 Use:

* **JavaFX** – for the GUI (modern desktop apps)
* **JCE (Java Cryptography Extension)** – for AES-256 encryption/decryption
* **Tink (by Google)** – optional, for easier cross-platform crypto APIs
* **JNA or JNI** – for advanced system-level operations (e.g., file shredding)
* **SQLite (embedded)** – if local storage of temp capsule metadata is needed

### 🔹 Key Features:

* Auto decrypt capsule on launch (from central token + key material)
* Auto encrypt on close
* File watcher to trigger cleanup/shred if capsule is idle/expired

---