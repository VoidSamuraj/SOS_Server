# SOS_Server

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.0.10-purple" alt="Kotlin 2.0.10">
  <img src="https://img.shields.io/badge/Ktor-2.3.12-purple" alt="Ktor 2.3.12">
  <img src="https://img.shields.io/badge/Exposed-0.53.0-green" alt="Exposed 0.53.0">
  <img src="https://img.shields.io/badge/Docker-24.0.7-blue" alt="Docker 24.0.7">
  <img src="https://img.shields.io/badge/AWS-Cloud-orange" alt="AWS Cloud">
  <img src="https://img.shields.io/badge/HTTPS-Secure-green" alt="HTTPS Secure">
  <img src="https://img.shields.io/badge/WebSocket-WSS-blue" alt="WebSocket WSS">
  <img src="https://img.shields.io/badge/JWT-Secure-teal" alt="JWT Secure">
  <img src="https://img.shields.io/badge/PostgreSQL-16.3-blue" alt="PostgreSQL 16.3">
  <img src="https://img.shields.io/badge/React-18.3.1-skyblue" alt="React 18.3.1">
  <img src="https://img.shields.io/badge/Webpack-5.94.0-lightblue" alt="Webpack 5.94.0">
  <img src="https://img.shields.io/badge/Google_Maps-API-red" alt="Google Maps API">
  <img src="https://img.shields.io/badge/SimpleJavaMail-8.11.3-blue" alt="Simple Java Mail 8.11.3">
</p>

---

## Purpose
The SOS application is designed to support the operations of a security company by automating and accelerating emergency response processes.  
It ensures that users receive quick and effective assistance in critical situations.

It works together with:
- [Guard Client App](https://github.com/VoidSamuraj/SOS_Guard_App)
- [SOS Client App](https://github.com/VoidSamuraj/SOS_Client_App)

---

## Preview

<table>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/16ac1d6f-43f7-4441-a0a9-6963d1c387e9"></td>
    <td><img src="https://github.com/user-attachments/assets/7182689f-6d02-43bf-89ab-3711c1d03af6"></td>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/43120116-a355-49e0-a09c-a5f5468cfb18"></td>
    <td><img src="https://github.com/user-attachments/assets/92a175df-0863-43bb-8907-0830a1024b72"></td>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/cae01527-4343-4443-97e8-defa26684d14"></td>
    <td><img src="https://github.com/user-attachments/assets/ac9c0ad2-dc46-4b87-b08e-54bc8f1d34da"></td>
  </tr>
</table>

---

## Key Features
- **Real-Time Location Updates:** Continuously tracks guards' positions and provides frequent updates.
- **Status Management:** Guards can manually set themselves as "available" or "unavailable."
- **Task Assignment and Intervention:** Guards confirm emergency tasks. Their status updates automatically depending on their actions (intervention / no response).
- **Navigation Assistance:** After accepting a task, guards receive the client's location and directions for quick intervention.

---

## Technologies Used
- **Kotlin:** Statically typed, cross-platform language, fully compatible with Java.
- **Ktor:** Lightweight and modular Kotlin server framework.
- **Exposed:** Kotlin SQL framework for database interactions.
- **WebSocket:** Enables real-time server-client communication.
- **Google Maps API:** Maps integration for guards and emergencies.
- **React:** Frontend framework for responsive web apps.
- **PostgreSQL:** Primary production database.
- **Docker:** For containerizing and easily deploying the application.
- **JWT:** Secure authentication method.
- **Simple Java Mail:** For email notifications and password recovery.

---

## How It Works
1. **SOS Alerts:** Clients trigger an alert. The system notifies available guards in real-time.
2. **Task Confirmation:** Guards confirm tasks and proceed towards the client's location.
3. **Automatic Status Update:** If a guard does not respond within a timeframe, their status is marked as "not responding."
4. **Backup Requests:** Guards can request reinforcements if necessary.

---

## Requirements Before Running

### 1. Set Up Keys
You need to create the following files manually:

**`src/main/kotlin/security/Keys.kt`**
```kotlin
object Keys {
    val EncryptKey = hex("YOUR_ENCRYPTION_KEY_IN_HEX_FORMAT")
    val SignKey = hex("YOUR_SIGNING_KEY_IN_HEX_FORMAT")
    const val JWTSecret = "YOUR_JWT_SECRET_HERE"
    const val ISS = "ApplicationSupportingSecurityCompanyWork"
    const val emailAddress = "EMAIL_USED_FOR_PASSWORD_RECOVERY"
    const val emailPassword = "EMAIL_PASSWORD_FOR_RECOVERY_EMAIL"
    const val dbAddress = "DATABASE_CONNECTION_ADDRESS"
}
```

**`src/main/resources/react/src/keys.js`**
```javascript
const keys = {
  GOOGLE_API_KEY: 'YOUR_GOOGLE_API_KEY_HERE',
  MAP_ID: 'YOUR_MAP_ID_HERE',
  STATS_MAP_ID: 'YOUR_STATS_MAP_ID_HERE'
};
export default keys;
```

---

### 2. Install Frontend Packages
```bash
npm install
```
Run inside `src/main/resources/react`.

---

### 3. Build the Frontend
```bash
npm run webpackbuild
```
Also run inside `src/main/resources/react`.

---

### 4. Adjust Configuration (Optional)

You might want to update the database URL, ports, or SSL settings.

**Example of `src/main/resources/application.yaml`:**
```yaml
ktor:
  application:
    modules:
      - ApplicationKt.module
  deployment:
    host: "0.0.0.0"        # Server binds to all network interfaces
    sslPort: 8443           # SSL/TLS port
  security:
    ssl:
      keyStore: "your-keystore-file.jks"         # Path to your keystore
      keyAlias: "yourKeyAlias"                   # Alias for your key
      keyStorePassword: "yourKeyStorePassword"   # Password for keystore
      privateKeyPassword: "yourPrivateKeyPassword" # Password for the private key
```

> **Note:** Database credentials are read from `Keys.kt`.  
> You can switch between databases by editing `src/main/kotlin/Application.kt`:

```kotlin
DatabaseFactory.init("jdbc:h2:file:./build/db", "org.h2.Driver", "root", "password")

/* Example for PostgreSQL:
DatabaseFactory.init(
    jdbcURL = Keys.dbAddress,
    driverClassName = "org.postgresql.Driver",
    user = Keys.dbLogin,
    password = Keys.dbPassword
)
*/
```

---

### 5. Run with Docker (Optional)
You can also run the server inside Docker:

```bash
docker build -t sos-server .
docker run -p 3000:3000 sos-server
```

