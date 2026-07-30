# SteeringPhone — Development Setup

## Prerequisites

### For Android Development

| Tool | Version | Download |
|------|---------|----------|
| Android Studio | Hedgehog+ | [developer.android.com](https://developer.android.com/studio) |
| JDK | 17+ | Bundled with Android Studio |
| Android SDK | API 26+ | SDK Manager in Android Studio |
| ADB | latest | Bundled with Platform Tools |
| Kotlin | 1.9.x | Bundled with Android Studio |

### For Windows Development

| Tool | Version | Download |
|------|---------|----------|
| Visual Studio 2022 | 17.8+ | [visualstudio.microsoft.com](https://visualstudio.microsoft.com/) |
| .NET 9 SDK | 9.0+ | [dotnet.microsoft.com](https://dotnet.microsoft.com/download) |
| Windows App SDK | 1.4+ | Via Visual Studio Installer |
| Windows SDK | 10.0.19041+ | Via Visual Studio Installer |
| ViGEmBus | 1.22.0+ | [GitHub Releases](https://github.com/ViGEm/ViGEmBus/releases) |

### Optional Tools

- **Git** — version control
- **PowerShell 7+** — for build scripts
- **Android Device** — physical device recommended (emulators have no gyroscope)

---

## Step 1: Clone the Repository

```bash
git clone https://github.com/3lyly0/SteeringPhone.git
cd SteeringPhone
```

---

## Step 2: Install ViGEmBus (Windows, Required)

Download and install the ViGEmBus driver **before** building the Windows app:

```powershell
# Option 1: Run the installer script (requires admin)
powershell -ExecutionPolicy Bypass -File tools\vigem-installer.ps1

# Option 2: Manual download
# https://github.com/ViGEm/ViGEmBus/releases/latest
```

---

## Step 3: Set Up Android App

```bash
cd apps/android

# Verify gradle wrapper
./gradlew --version

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
./gradlew connectedAndroidTest  # requires connected device

# Lint
./gradlew ktlint
```

### Android Studio Import
1. Open Android Studio
2. **File → Open** → select `apps/android/`
3. Wait for Gradle sync
4. Run on device (physical device required for gyroscope)

### Enable USB Debugging on your phone
1. **Settings → About Phone** → tap **Build Number** 7 times
2. **Settings → Developer Options** → enable **USB Debugging**
3. Connect to PC, accept the trust dialog on the phone screen

---

## Step 4: Set Up Windows App

```powershell
cd apps/windows

# Restore packages
dotnet restore

# Build
dotnet build SteeringPhone.Desktop.sln

# Run
dotnet run --project SteeringPhone.Desktop

# Run tests
dotnet test

# Format check
dotnet format --verify-no-changes
```

### Visual Studio
1. Open Visual Studio 2022
2. **File → Open → Solution** → `apps/windows/SteeringPhone.Desktop.sln`
3. Set `SteeringPhone.Desktop` as startup project
4. Press **F5** to run

---

## Step 5: Connect Phone to PC (USB)

```powershell
# 1. Enable USB Debugging on phone (see Step 3 above)
# 2. Connect USB cable
# 3. Run ADB setup script
powershell -File tools\adb-setup.ps1

# Verify device is detected
adb devices

# SteeringPhone Desktop will auto-detect via ADB
```

---

## Step 6: Connect via WiFi

1. Ensure phone and PC are on the **same WiFi network**
2. Start **SteeringPhone Desktop** on PC (it listens for DISCOVER broadcasts)
3. Start **SteeringPhone** on phone (it broadcasts DISCOVER packets every 500 ms)
4. Connection happens automatically in ~1 second

---

## Environment Variables

No environment variables required for local development.

Optional variables for CI:

```
STEERINGPHONE_KEYSTORE_PATH   - Android signing keystore path
STEERINGPHONE_KEYSTORE_PASS   - Keystore password
STEERINGPHONE_KEY_ALIAS       - Key alias
STEERINGPHONE_KEY_PASS        - Key password
```

---

## Project Structure Quick Reference

```
apps/android/    → Android Studio project (Kotlin + Compose)
apps/windows/    → Visual Studio solution (.NET 9 + WinUI 3)
docs/            → All documentation
shared/protocol/ → Protocol specification
tests/           → Standalone test projects
tools/           → Build and setup scripts
assets/          → Icons, images
```

---

## Common Issues

### "ADB device not found"
- Enable USB Debugging on phone
- Accept the trust dialog on the phone screen
- Try a different USB cable (data cable, not charge-only)
- Run `adb kill-server && adb start-server` to reset the ADB daemon

### "ViGEmBus not installed"
- Run `tools\vigem-installer.ps1` as Administrator
- Reboot after installation

### "Connection timeout on WiFi"
- Ensure both devices are on the same SSID
- Open Windows Defender Firewall → Allow `SteeringPhone.Desktop.exe` for private networks
- Some routers block UDP broadcast — use USB mode instead

### Android sensor not detected
- Physical device required (emulators lack real gyroscopes)
- Grant sensor permissions in Android App Settings

### WinUI 3 XAML designer not loading
- Install **Windows App SDK** workload in Visual Studio Installer
- Ensure Windows SDK **10.0.19041.0+** is installed

---

## CI/CD

GitHub Actions workflows (`.github/workflows/`):

| Workflow | Trigger | What it does |
|----------|---------|-------------|
| `android-ci.yml` | PR + push to `main`/`develop` | Build + test + lint |
| `windows-ci.yml` | PR + push to `main`/`develop` | Build + test + format check |

Artifacts (APK + MSIX) are uploaded on every successful build at:
[https://github.com/3lyly0/SteeringPhone/actions](https://github.com/3lyly0/SteeringPhone/actions)
