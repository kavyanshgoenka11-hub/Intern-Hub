# Intern Hub

**Intern Hub** is an Android application developed as a Mobile Application Development (MAD) mini-project for NMIMS Shirpur. It brings internship discovery and career preparation features into one student-focused platform, with separate experiences for students and the designated administrator.

> **Project status:** Academic mini-project. This repository is not an official NMIMS product or an institutionally maintained service.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [How It Works](#how-it-works)
- [Firestore Data Model](#firestore-data-model)
- [Requirements](#requirements)
- [Setup and Installation](#setup-and-installation)
- [Running the App](#running-the-app)
- [Project Structure](#project-structure)
- [Security Notes](#security-notes)
- [Limitations and Future Scope](#limitations-and-future-scope)
- [Contributors](#contributors)

## Overview

Students often have to look in different places for internship opportunities, preparation guidance, and faculty mentoring. Intern Hub provides a single Android interface for these activities. Its data-backed modules use Firebase Authentication for account access and Cloud Firestore for shared internship and mentorship data.

## Features

### Student features

- **Account registration and sign-in:** Email-and-password authentication with input validation and user-facing error messages.
- **Student dashboard:** Displays the signed-in user's name and provides navigation to the main modules.
- **Internship Explorer:** Displays internships published in Firestore, including available details such as company, domain, required skills, location, work mode, stipend, and deadline.
- **Career Roadmap:** Presents a four-stage preparation path: choose a career path, build technical skills, create projects and a resume, and prepare and apply for internships. Progress is stored locally using Android `SharedPreferences`.
- **Faculty Mentorship:** Students can view published faculty sessions and submit a request to attend. The request status reflects the administrator's decision.
- **Upcoming Mentorship:** Shows upcoming published faculty sessions on the student dashboard.
- **Industry Insights:** Summarizes the skills listed in approved internship documents so students can see which skills appear most frequently in the available listings.
- **Account menu:** Provides access to basic account details, help information, and sign-out.

### Administrator features

- **Internship management:** Add, edit, and delete internship records stored in Firestore.
- **Faculty session management:** Publish faculty mentorship sessions.
- **Mentorship request review:** Accept or decline student requests; the updated status is reflected for students.
- **Industry skill summary:** View a summary of required skills calculated from approved internship records.

## Technology Stack

| Component | Technology |
|---|---|
| Platform | Android |
| Language | Java |
| UI | XML layouts and Android views |
| Authentication | Firebase Authentication (email and password) |
| Shared application data | Cloud Firestore |
| Local roadmap progress | Android `SharedPreferences` |
| IDE / build | Android Studio / Gradle |
| Package name | `com.example.internhub` |

## How It Works

1. A user registers or signs in through Firebase Authentication.
2. The app checks the current Firebase user. Users who are not signed in are sent to the authentication screen.
3. The current implementation routes the designated administrator, identified by a configured Firebase UID, to the admin dashboard. Other authenticated users are routed to the student dashboard.
4. Internship listings, faculty sessions, and mentorship requests are read from and written to Cloud Firestore.
5. The Industry Insights screen counts skill mentions in approved internship documents and displays a ranked summary.

## Firestore Data Model

The app uses the following main collections. Field names should match the ones used in the application code.

### `internships`

Common fields include:

- `title`
- `company`
- `domain`
- `required_skills`
- `description`
- `location`
- `work_mode`
- `stipend`
- `deadline`
- `application_url`
- `status` (for example, `approved`)
- `created_at`
- `updated_at` (when an existing record is edited)

The student internship list and Industry Insights use approved internship documents.

### `facultySessions`

Published session records include fields such as `title`, `faculty_name`, `date`, `time`, `mode`, and `status`. The student-facing screens display sessions with a published status.

### `mentorshipRequests`

A mentorship request records the session identifier and title, faculty name, student UID, student name and email, request `status`, and timestamps. Requests begin as `pending`; the administrator can change the status to accepted or declined.

> The exact required fields and allowed status values should be kept consistent with the Java code and Firestore Security Rules in the repository.

## Requirements

- Android Studio with Android SDK and Gradle support.
- A configured Firebase project.
- Firebase Authentication enabled with the **Email/Password** sign-in provider.
- Cloud Firestore enabled.
- A connected Android device or an Android emulator.

## Setup and Installation

### 1. Clone the repository

```bash
git clone https://github.com/kavyanshgoenka11-hub/Intern-Hub.git
cd Intern-Hub
```

Open the project in Android Studio and allow Gradle sync to finish.

### 2. Configure Firebase

1. Create or open the Firebase project used for this application.
2. Register an Android app using the package name `com.example.internhub`.
3. Download the matching `google-services.json` file from Firebase Console.
4. Place it in the app module directory, normally `app/google-services.json`.
5. In Firebase Console, enable **Authentication → Sign-in method → Email/Password**.
6. Create a Cloud Firestore database and apply appropriate Firestore Security Rules before testing with real accounts.

Do not commit private service-account credentials or other secrets. Follow the repository's `.gitignore` rules for local configuration files.

### 3. Configure the administrator

The current app uses a configured Firebase Authentication UID to distinguish the designated administrator from other signed-in users. Check the admin UID configuration in `MainActivity.java` and the admin dashboard code, and make sure both use the intended administrator account. Do not treat client-side role routing as a security boundary; restrict privileged writes with Firestore Security Rules.

### 4. Build and run

In Android Studio, select an emulator or connected Android device and run the `app` configuration.

Alternatively, from the project root on Windows PowerShell, build a debug APK with:

```powershell
.\gradlew.bat :app:assembleDebug
```

The debug APK is normally generated under:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Running the App

1. Launch the app and register an account or sign in with an existing Firebase Authentication account.
2. Use a regular account to explore student features.
3. Sign in with the configured administrator account to manage internship listings and faculty sessions and review mentorship requests.
4. Add sample internship documents with `required_skills` values to populate the Industry Insights summary.

The app requires a valid Firebase configuration and appropriately configured Firestore rules for its cloud-backed features to work.

## Project Structure

The application follows the standard Android app-module layout. Key files include:

```text
app/
└── src/main/
    ├── java/com/example/internhub/
    │   ├── AuthActivity.java
    │   ├── MainActivity.java
    │   ├── InternshipActivity.java
    │   ├── IndustryInsightsActivity.java
    │   ├── AdminDashboardActivity.java
    │   └── ...
    ├── res/
    │   ├── layout/
    │   └── drawable/
    └── AndroidManifest.xml
```

This is a representative overview; the project contains additional activity classes and resources for mentorship, roadmap, and administrator workflows.

## Security Notes

- Firebase Authentication handles account credentials; the app should not store user passwords in Firestore or in local files.
- A UID check in Android code is useful for navigation but is **not sufficient authorization** on its own. Firestore Security Rules must independently enforce who can create, update, or delete internship records, publish sessions, and change mentorship request statuses.
- Avoid committing `google-services.json` if your repository policy requires keeping Firebase configuration out of version control, and never commit service-account private keys. Firebase client configuration is not a substitute for database security rules.
- Use test data for academic demonstrations and avoid adding sensitive student information to public repositories.

## Limitations and Future Scope

- Internship discovery depends on the data entered into the Firestore database; the skill summary is only as representative as the available approved listings.
- The Industry Insights module counts the skills recorded in internship documents. It is a descriptive summary, not a predictive job-market model.
- The Career Roadmap progress is stored locally using `SharedPreferences` and is not designed as cross-device progress synchronization.
- Alumni mentorship is outside the current submission scope and may be considered for future development.
- Further improvements could include stronger role management, additional input validation, automated tests, and richer filtering and search for internships.

