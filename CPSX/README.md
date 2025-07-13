# CPSX - CAPSULE SECURE EXCHANGE
Creating a **new secure file type** (let’s call it `.cpsx` for “Capsule Secure Exchange”) involves defining a **standard structure**, **serialization format**, **encryption strategy**, and **validation rules** so only your app can decrypt, manage, and enforce the policies inside it.

Here’s a senior-level guide to help you **design and implement a secure, app-bound file format** in Java:

---

## 🧱 1. Define the `.cpsx` File Structure

Design it like a **container file** (similar to `.zip` or `.jar`), but with strong encryption and a signed manifest.

### 📂 Internal Layout:

```
my_capsule.cpsx
├── manifest.json       # Metadata: creator, expiry, policy
├── policy.json         # Optional OPA-style rules
├── payload/            # Encrypted user files
│   └── file1.pdf.enc
│   └── notes.txt.enc
├── access.sig          # Signature file for verification
└── checksums.sha256    # Hashes of encrypted parts
```

---

## 🔐 2. Encryption Strategy

### 📌 Key Design Decisions:

* Use **AES-256-GCM** (authenticated encryption)
* Key derived from:

  * App fingerprint (install ID or signed hash)
  * User token (from your central service)
  * Salt and timestamp

### ✅ Recommendation:

* Use **PBKDF2 + HMAC-SHA512** to derive key:

```java
SecretKey key = deriveKey(appInstallId, sessionToken, salt);
```

* Encrypt each file inside `/payload/` individually.
* Encrypt the manifest with a different key (admin/master access).
* Store encrypted files in `.enc` format (binary).

---

## 🧾 3. Manifest & Policy Format

### `manifest.json` Example:

```json
{
  "capsule_id": "abc123",
  "created_by": "admin@bank.com",
  "created_at": "2025-07-13T10:00:00Z",
  "expires_at": "2025-08-01T00:00:00Z",
  "access_level": "L3",
  "allowed_apps": ["CapsuleViewer-v1.0"],
  "user_id": 567,
  "policy_file": "policy.json"
}
```

### `policy.json` (OPA or JSON Logic Style):

```json
{
  "rules": [
    { "if": { "app_version": "<1.1" }, "deny": "Upgrade required" },
    { "if": { "location": "outside-India" }, "deny": "Geo restriction" }
  ]
}
```

---

## 🛡️ 4. Signature & Integrity

* Sign the entire archive using SHA256 + RSA private key (admin/private key).
* Generate and validate:

  * `access.sig` → signature of `manifest.json + checksums.sha256`
  * `checksums.sha256` → for each payload file

---

## 🧰 5. Implementation Tools (Java)

| Purpose                | Java Tool / Library                        |
| ---------------------- | ------------------------------------------ |
| Encryption (AES, GCM)  | `javax.crypto` or Bouncy Castle            |
| File Packaging         | `java.util.zip` or Apache Commons Compress |
| Signature Verification | `java.security.Signature`                  |
| JSON Parsing           | Jackson or Gson                            |
| Integrity Check        | `MessageDigest` (SHA-256)                  |

---

## 🖥️ 6. App-Binding Techniques

Ensure that only your app can open `.cpsx`:

* Derive part of the encryption key from:

  * App install hash or fingerprint
  * Hardware UUID or system info
* Prevent side-channel decryption using:

  * Encapsulated runtime (JavaFX + encrypted JAR class loader)
  * Time-based token (request ephemeral key from server)

---

## 🔁 7. Opening & Closing Lifecycle

### On Open:

1. Validate `.cpsx` file signature
2. Decrypt `manifest.json` & validate policy
3. Request decryption key from **Central Server**
4. Decrypt contents into memory or temp disk (if allowed)
5. Register session lease with TTL in Redis/backend

### On Close/Exit/Expiry:

* Re-encrypt capsule contents
* Remove temp files
* If expired or revoked: **overwrite with garbage** (shred)
* Log action to MongoDB via secure API

---

## 🧪 8. File Format Extension & MIME

| Format    | Value                       |
| --------- | --------------------------- |
| File Ext  | `.cpsx`                     |
| MIME Type | `application/x-capsule`     |
| Icon      | Custom OS icon for branding |

---

## 🚀 Optional Add-ons

* **Compression** (before encryption): GZIP or LZ4
* **Stealth Mode**: Make capsule look like another format (e.g., `.pdf`, but opens with your app)
* **Capsule Signing Chain**: Verifiable signatures from Admin or Bank

---

## ✅ Final Tips

* Write automated test suite for integrity + permission failure cases.
* Include metadata viewer/debugger tool for `.cpsx` files (admin-only).
* Build a “Capsule SDK” to read/write this format for internal devs.

---

Would you like a base `.cpsx` creator Java CLI tool scaffold (with manifest writing + AES encryption)?
