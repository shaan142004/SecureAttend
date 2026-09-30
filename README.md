# SecureAttend

Fingerprint + Wi-Fi BSSID + 5-metre GPS-verified attendance app (Android, Kotlin).

SecureAttend is a secure attendance management system consisting of an Android mobile application and a web-based Faculty Dashboard. The Android application allows students to mark attendance for lectures created by teachers. Before attendance is recorded, the system performs fingerprint, Wi-Fi, and GPS-based verification to help ensure that attendance is being marked by the correct student at the required location. Attendance records are associated with the relevant lecture, subject, and teacher. The Faculty Dashboard provides teachers with a web interface to view attendance information.

## What's inside
- `LoginActivity` / `RegisterActivity` — Firebase email/password auth
- `MarkAttendanceActivity` — the core screen. Runs 3 checks in order:
  1. Fingerprint match (`BiometricAuthHelper`)
  2. Wi-Fi BSSID match to classroom router (`WifiUtils`)
  3. GPS within 5 metres of the router's coordinates (`LocationUtils`)
- `FacultyDashboardActivity` — lists today's attendance for the classroom
- `utils/Constants.kt` — hardcoded classroom Wi-Fi BSSID + GPS coordinates (MVP). Replace with your real lab's values before testing on campus.

## Setup steps (do these in order)

### 1. Open in Android Studio
- Android Studio → Open → select this `SecureAttend` folder
- Let Gradle sync (first sync can take a few minutes)

### 2. Create your Firebase project
- Go to console.firebase.google.com → Add project → name it `SecureAttend`
- Add an Android app with package name `com.secureattend.app`
- Download `google-services.json` and place it in `app/` (same folder as `app/build.gradle.kts`)
- In Firebase console: Build → Authentication → enable Email/Password
- In Firebase console: Build → Firestore Database → Create database → start in test mode

### 3. Set your real classroom Wi-Fi + GPS values
Open `app/src/main/java/com/secureattend/app/utils/Constants.kt` and replace:
- `routerBssid` — stand in the lab, connect to its Wi-Fi, and log `WifiUtils.getCurrentBssid(context)` to Logcat to get the real BSSID
- `latitude` / `longitude` — open Google Maps, long-press your exact spot in the lab to copy coordinates

### 4. Run it
- Connect a **physical Android phone** (fingerprint sensor + real GPS + real Wi-Fi don't work properly on most emulators)
- Enable Developer Options + USB debugging on the phone
- Run ▶ from Android Studio

### 5. Test order
1. Register a student (needs a fingerprint already enrolled in the phone's Settings)
2. Log in
3. Tap "Mark Attendance" while connected to the classroom Wi-Fi and physically in the room
4. Check Faculty Dashboard to see the record appear

## Common beginner issues
- **BSSID reads as `02:00:00:00:00:00`** → grant Location permission to the app (Settings > Apps > SecureAttend > Permissions)
- **"No fingerprint enrolled" on Register screen** → add a fingerprint in the phone's own Settings > Security first, the app can't do this for you
- **Firestore permission denied** → you're still in test mode grace period (30 days) or need to update security rules
- **Gradle sync fails on `google-services.json` missing** → you skipped step 2

## Next steps for the full project (before Sept 15)
- Move classroom config from `Constants.kt` into a Firestore `classrooms` collection so faculty can add rooms from the app
- Add Firestore security rules before final submission (test mode is open to anyone)
- Add PDF/Excel export for attendance reports (mentioned in your proposal's feature list)
- Add proxy-attempt logging (failed fingerprint/wifi/location attempts) for the "Proxy Attempt Alerts" feature
