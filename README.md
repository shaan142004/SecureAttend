# SecureAttend

A multi-factor, anti-proxy attendance verification system for colleges — Android app (Kotlin) + web Faculty Dashboard, sharing one Firebase backend.

SecureAttend supports a real college timetable: any teacher can independently create and manage their own lectures (subject, day, time window, classroom Wi-Fi/GPS), and students only ever see lectures that are happening right now. Before attendance is recorded, the system runs three checks — fingerprint, Wi-Fi network, and GPS radius — so attendance can't be marked by the wrong person or from outside the classroom.

**Live Faculty Dashboard:** https://attend-ccce4.web.app

## How it works

**Student flow:**
1. Log in → lands on "Select Your Lecture," showing only lectures whose day and time window match right now, across all teachers.
2. Pick a lecture → runs the verification flow:
   1. **Fingerprint match** — via the phone's own biometric sensor (`BiometricAuthHelper`), no fingerprint data ever leaves the device.
   2. **Wi-Fi BSSID match** — confirms the phone is connected to that lecture's registered classroom router (`WifiUtils`).
   3. **GPS radius check** — confirms the phone is within the lecture's allowed radius (default 5m) of the classroom's registered coordinates (`LocationUtils`).
3. Only when all three pass is an attendance record written, tagged to that lecture, subject, and teacher.

**Teacher flow (app or website):**
- Create/edit/delete their own lectures — subject, day of week, start/end time, classroom name, Wi-Fi BSSID, GPS coordinates, allowed radius.
- "Use Current Wi-Fi" / "Use Current Location" buttons let a teacher configure a room just by standing in it.
- View live attendance per lecture: present count, average distance from router, verification status per student, CSV export.

## Project structure

```
app/    → Android app (Kotlin)
web/    → Faculty Dashboard (static HTML/JS, deployed to Firebase Hosting)
```

### Key Android files
- `LoginActivity` / `RegisterActivity` — Firebase email/password auth; registration blocked if the device has no fingerprint enrolled.
- `SelectLectureActivity` — shown to students after login; lists only currently-active lectures (`LectureTimeUtils`).
- `MarkAttendanceActivity` — runs the fingerprint → Wi-Fi → GPS check for the selected lecture.
- `FacultyDashboardActivity` — "My Lectures," lists lectures owned by the signed-in teacher.
- `LectureFormActivity` — add/edit a lecture, with "Use Current Wi-Fi"/"Use Current Location."
- `LectureAttendanceActivity` — per-lecture attendance view for the owning teacher.
- `utils/FirestoreRepository.kt` — all Firestore reads/writes (students, teachers, lectures, attendance).

### Web dashboard (`web/`)
- Separate faculty account system (Firebase Authentication); every new registration is marked as a teacher in Firestore.
- "My Lectures" panel — add/edit/delete lectures, with a "Use My Location" button using the browser's Geolocation API.
- Per-lecture attendance view with stats and CSV export.

## Firestore data model

| Collection | Purpose |
|---|---|
| `students/{uid}` | Student profile |
| `teachers/{uid}` | Marks an account as faculty (created automatically at website registration) |
| `lectures/{lectureId}` | One lecture slot — subject, teacher, day, time window, classroom Wi-Fi BSSID, GPS, radius |
| `attendance/{autoId}` | One attendance event — student, lecture reference, timestamp, verification flags, distance from router |

Security rules (`firestore.rules`) enforce that only a lecture's owning teacher can create/edit/delete it, and attendance records can never be edited or deleted once written.

## Setup steps

### 1. Open the Android app
- Android Studio → Open → select this repo's root folder (or just the project — the `app/` module is standard)
- Let Gradle sync

### 2. Firebase project
- Create a project at console.firebase.google.com
- Add an Android app with package name `com.secureattend.app`, download `google-services.json` into `app/`
- Add a Web app too (for the dashboard), copy its config into `web/index.html`'s `firebaseConfig`
- Enable **Authentication → Email/Password**
- Enable **Firestore Database**, then publish the rules in `firestore.rules`

### 3. Run the Android app
- Connect a **physical Android phone** (fingerprint, real GPS, and real Wi-Fi BSSID don't work reliably on emulators)
- Register a teacher account on the web dashboard first, create a test lecture with your real classroom Wi-Fi/GPS
- Register a student account in the app, log in, select that lecture, mark attendance

### 4. Deploy the web dashboard (optional — already live at the link above)
```
cd web
firebase deploy --only hosting
```

## Common issues
- **BSSID reads as `02:00:00:00:00:00`** → grant Location permission to the app (required by Android to read Wi-Fi BSSID)
- **"No fingerprint enrolled" on Register** → add a fingerprint in the phone's own Settings first
- **No lectures showing on Select Lecture screen** → check the lecture's day/time window actually covers right now, and that Firestore rules are published
- **Firestore permission denied** → confirm `firestore.rules` has been published in the Firebase Console

## Tech stack
Kotlin, Android Jetpack, AndroidX Biometric Library, Google Fused Location Provider API, Firebase Authentication, Cloud Firestore, Firebase Hosting, HTML/CSS/JavaScript (ES modules, Firebase Web SDK).
