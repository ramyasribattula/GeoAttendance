# 📍 GeoAttendance – Geo-Fenced Student Attendance System

A location-based Android application developed using Kotlin and XML that enables students to mark attendance only when they are within a predefined campus radius of 100 metres.

## 🚀 Project Overview

GeoAttendance is designed to simplify student attendance management using GPS location verification, Firebase Authentication and Firebase Realtime Database. It helps prevent unauthorized attendance marking by checking the student's current location before allowing attendance.

## ✨ Features

- **User Authentication:** Student registration and login using Firebase Authentication.
- **Google Sign-In:** Supports Google account authentication.
- **GPS Location Tracking:** Retrieves the student's current geographical coordinates.
- **Geo-Fencing:** Allows attendance marking only within a 100-metre campus radius.
- **Barcode / QR Scanning:** Supports scanning using Google Code Scanner and ML Kit.
- **Attendance Management:** Stores attendance records in Firebase Realtime Database.
- **Duplicate Prevention:** Prevents multiple attendance entries for the same day.
- **Attendance History:** Displays previously recorded attendance using RecyclerView.
- **Product Information:** Integrates the Open Food Facts API for product information.

## 🛠️ Technologies Used

| Category | Technology |
|---|---|
| Programming Language | Kotlin |
| UI Design | XML |
| IDE | Android Studio |
| Backend | Firebase |
| Authentication | Firebase Authentication |
| Database | Firebase Realtime Database |
| Location Services | Google Play Services Location |
| Barcode Scanning | Google Code Scanner, ML Kit |
| API Integration | Retrofit, Gson |
| Image Loading | Coil |
| UI Components | RecyclerView, CardView, Material Components |

## 📚 Libraries and Dependencies

- AndroidX Core KTX
- AndroidX AppCompat
- AndroidX Activity KTX
- ConstraintLayout
- Material Components
- Firebase BOM
- Firebase Authentication
- Firebase Realtime Database
- Google Play Services Location
- Google Play Services Code Scanner
- ML Kit Barcode Scanning
- Retrofit
- Gson Converter
- Coil
- RecyclerView
- CardView
- Credential Manager
- Google ID
- Lifecycle Runtime KTX

## ⚙️ Application Workflow

1. Student opens the application.
2. Student registers or logs in.
3. Firebase Authentication verifies the user.
4. The dashboard retrieves the current GPS location.
5. The application calculates the distance from the campus.
6. If the student is within 100 metres, attendance can be marked.
7. Attendance information is saved in Firebase Realtime Database.
8. The student can view attendance history.

## 📱 Application Screens

Add your application screenshots here after capturing them.

| Login Screen | Attendance Dashboard | Attendance History |
|---|---|---|
| Add login screenshot | Add dashboard screenshot | Add history screenshot |

## 🔧 Installation and Setup

1. Clone this repository:

   ```bash
   git clone https://github.com/ramyasribattula/GeoAttendance.git
   ```

2. Open the project in Android Studio.

3. Create a Firebase project in the Firebase Console.

4. Register an Android application using the package name:

   `com.cutmap.geoattendance`

5. Enable Firebase Authentication and Realtime Database.

6. Download your own `google-services.json` file and place it inside the `app/` directory.

7. Configure the campus latitude and longitude in `CampusConfig.kt`.

8. Sync Gradle and run the application.

## 🔐 Permissions Required

- Internet Access
- Fine Location
- Coarse Location
- Camera permission, if required by the scanning implementation

## 📋 Project Configuration

- Minimum SDK: API 24
- Target SDK: API 37
- Package Name: `com.cutmap.geoattendance`
- Version: 1.0

## 🎯 Expected Outcome

A location-based student attendance application that verifies campus proximity before allowing attendance marking, stores attendance records securely in Firebase and provides attendance history.

## 👩‍💻 Developer

**Ramyasri Battula
