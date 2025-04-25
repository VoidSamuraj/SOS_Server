# SOS_Server

<p>

<img src="https://img.shields.io/badge/Kotlin-2.0.10-purple" alt="Kotlin 2.0.10">

<img src="https://img.shields.io/badge/Ktor-2.3.12-purple?color=5300EB" alt="Ktor 2.3.12">

<img src="https://img.shields.io/badge/Exposed-0.53.0-green?color=008B02" alt="Exposed 0.53.0">

<img src="https://img.shields.io/badge/Docker-24.0.7-blue" alt="Docker 24.0.7">

<img src="https://img.shields.io/badge/AWS-Cloud-orange" alt="AWS Cloud">

<img src="https://img.shields.io/badge/HTTPS-Secure-green?color=008B02" alt="HTTPS Secure">

<img src="https://img.shields.io/badge/WebSocket-WSS-blue?color=1E90FF" alt="WebSocket WSS">

<img src="https://img.shields.io/badge/JWT-Secure-blue?color=008B8B" alt="JWT Secure">

<img src="https://img.shields.io/badge/PostgreSQL-16.3-blue?color=336791" alt="PostgreSQL 16.3">

<img src="https://img.shields.io/badge/React-18.3.1-blue?color=61DAFB" alt="React 18.3.1 ">

<img src="https://img.shields.io/badge/Webpack-5.94.0-grey?color=8DD6F9" alt="Webpack 5.94.0">

<img src="https://img.shields.io/badge/Google_Maps-API-red?color=EA4335" alt="Google Maps API">

<img src="https://img.shields.io/badge/SimpleJavaMail-8.11.3-blue?color=1E90FF" alt="Simple Java Mail 8.11.3">

</p>

## Purpose:
The SOS application is designed to support the operations of a security company by automating and speeding up processes related to handling emergency requests. It ensures that users get quick and effective help in critical situations.

It works with <a href="https://github.com/VoidSamuraj/SOS_Guard_App" target="_blank">Guard Client</a> and <a href="https://github.com/VoidSamuraj/SOS_Client_App" target="_blank">SOS Client</a>

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

## Key Features:
- Real-Time Location Updates: The app continuously tracks the security guard’s position and provides updates at short intervals.
- Status Update: Guards can change their availability status (available/unavailable).
- Task Assignment: Upon receiving an emergency request, the guard can confirm it and start intervention; in the system, the status will change to "Intervention" for the guard. If the guard will not respond (confirm or deny the request), his status will change to "not responding".
- Client Navigation: The app provides the client’s position and allows the guard to use navigation to reach the client after confirming the intervention.

## Technologies:
- Kotlin: A statically typed programming language compatible with Java, ideal for cross-platform development.
- Ktor: My favorite server so far, easy, written with Kotlin.
- WebSocket: For real-time communication with the server.
- Google Maps API: For displaying emergencies and guards.
- React: For simple and reactive websites.

## How it Works:
1. Receiving SOS Alerts: Upon receiving an alert, the guard's mobile app updates the system about their status.
2. Intervention Confirmation: After confirming the intervention, the guard navigates to the client’s location, assisting as needed.
3. Support Requests: If needed, guards can request additional backup or resources.

## Requirements
Before running the project, you need to make sure to set the keys file and build the website.
1. Create files with keys:
    src/main/kotlin/security/Keys.kt
    src/main/resources/react/src/keys.js
2. Install packages:
  `npm install` in `src/main/resources/react`.
3. Build website:
  `npm run webpackbuild` in `src/main/resources/react`
You can also run a server using Docker. Example of running it on port 3000:3000:

```
docker build -t sos-server .
docker run -p 3000:3000 sos-server
```
