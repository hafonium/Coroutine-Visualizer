# 🚀 Coroutine Non-blocking Visualizer

An interactive Android application built with **Jetpack Compose** and **Kotlin Coroutines** to visualize non-blocking UI behavior, suspend functions, and thread management during API calls.

---

## 🛠️ Required Tools & Libraries

### Core Prerequisites & Development Tools
* **IDE:** Android Studio Hedgehog (2023.1.1) or newer (Koala / Ladybug recommended).
* **JDK:** Java 11 (Configured in `compileOptions`).
* **Android SDK:** API Level 34 / 36 (Android 14+).

### Key Dependencies & Libraries
* **Jetpack Compose (BOM):** Modern toolkit for native Android UI.
* **Kotlin Coroutines:** For asynchronous and non-blocking programming.
* **Retrofit 2.9.0:** Type-safe HTTP client for Android.
* **OkHttp 4.12.0 & Logging Interceptor:** HTTP networking layer.

---

## ⚙️ Installation & Configuration Steps

1. **Clone or Extract the Repository:**
   ```bash
   git clone https://github.com/hafonium/Coroutine-Visualizer.git
   # or simply extract the provided source code zip file
   ```
   
2. **Open the Project in Android Studio:**
- Launch Android Studio.
- Click on File > Open and navigate to the root directory of the project.
- Click OK to open.

3. **Sync Gradle:**
   - Once the project is opened, Android Studio will prompt you to sync Gradle. Click on **Sync Now** to download all dependencies.

## 📱 Devices, Accounts & External Services Required
- **Device / Emulator:** Physical Android device or Emulator running Android 7.0+ (API Level 24+).
- **External API Service:** Public Cat API (used to fetch random cat image payload).
    - **🌐 Internet Connection:** Required on the device/emulator to perform network requests.
    - **🔑 Account / API Key:** NONE required. The demo uses a free, public endpoint.

## 🚀 Instructions for Running the Project
1. **Set Up Execution Target:**
   - Connect your Android device via USB or start an Android Emulator.
   - Ensure the device/emulator is running and recognized by Android Studio.
2. **Run the Application:**
   - Click on the **Run** button (green play icon) in Android Studio.
   - Select your target device/emulator and click OK.
3. **Interact with the Application:**
   - Once the app launches, you can interact with the UI to visualize coroutine behavior.