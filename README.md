# SteeringPhone

**Transform your Android phone into a professional steering wheel controller for Windows games.**

[![Android CI](https://github.com/3lyly0/SteeringPhone/actions/workflows/android-ci.yml/badge.svg)](https://github.com/3lyly0/SteeringPhone/actions)
[![Windows CI](https://github.com/3lyly0/SteeringPhone/actions/workflows/windows-ci.yml/badge.svg)](https://github.com/3lyly0/SteeringPhone/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## What is SteeringPhone?

SteeringPhone uses your Android phone's **accelerometer and gyroscope** as a real steering wheel. The companion Windows application receives sensor data and injects it into games via a **virtual Xbox 360 controller (ViGEmBus)** — compatible with every game that supports XInput, DirectInput, or Raw Input.

No special hardware. No racing wheel required. Just your phone.

---

## Features

| Category | Details |
|----------|---------|
| **Steering** | Gyro + accelerometer fusion, low-pass filter, exponential curve, deadzone |
| **Controls** | Steering, Throttle, Brake, Hand Brake, Clutch, Gear Up/Down, Horn, Indicators, Headlights, Camera, Pause, Menu, Nitro, Custom |
| **Pedals** | Mode A: large touch buttons · Mode B: pressure sliders |
| **Connection** | USB (ADB, auto-detect, <10ms) · WiFi (auto-discovery, <20ms) |
| **Profiles** | Unlimited per-game profiles (ETS2, ATS, Forza, BeamNG, Assetto Corsa, F1, …) |
| **Calibration** | Step-by-step wizard (center → left → right → save) |
| **Dashboard** | Live latency, FPS, packet loss, battery, signal, steering angle |
| **Performance** | 60–120 Hz update rate, minimal CPU usage |

---

## Supported Games (tested)

- Euro Truck Simulator 2
- American Truck Simulator
- Forza Horizon 4 / 5
- BeamNG.drive
- Assetto Corsa / Competizione
- City Car Driving
- Need for Speed series
- F1 series
- Any game with XInput / DirectInput support

---

## Platform Requirements

| Platform | Requirement |
|----------|-------------|
| **Phone** | Android 8.0+ (API 26), gyroscope sensor required |
| **PC** | Windows 10 v1903+ (64-bit), [ViGEmBus driver](https://github.com/ViGEm/ViGEmBus/releases) |
| **USB mode** | ADB enabled on phone |
| **WiFi mode** | Phone and PC on same network |

---

## Quick Start

### 1. Install ViGEmBus on Windows
Download and install from [ViGEmBus Releases](https://github.com/ViGEm/ViGEmBus/releases).

### 2. Install SteeringPhone Desktop
Download the latest MSI from [Releases](https://github.com/3lyly0/SteeringPhone/releases) and run it.

### 3. Install SteeringPhone on your phone
Install the APK from [Releases](https://github.com/3lyly0/SteeringPhone/releases) or build from source.

### 4. Connect
**USB:** Enable USB debugging on your phone, plug in, SteeringPhone auto-detects.  
**WiFi:** Make sure both devices are on the same network. SteeringPhone auto-discovers via UDP broadcast.

### 5. Calibrate
Run the Calibration Wizard on your phone. Hold the phone flat → rotate left → rotate right → save.

### 6. Play!
Select your game profile and start driving.

---

## Architecture

SteeringPhone uses **Clean Architecture** with MVVM on both platforms:

```
Phone (Android)                   PC (Windows)
────────────────                  ────────────────────
SensorManager                     ViGEmBus
    │                                 │
SensorFusion                     InputProcessor
(Complementary Filter)            (SteeringMapper)
    │                                 │
SteeringCalculator                VirtualController
    │                                 │
PacketBuilder ──── Network ──── PacketDeserializer
(43-byte binary)   (UDP/WS)     (CRC validated)
    │
ConnectionManager (USB/WiFi)
```

Full details → [Architecture.md](docs/Architecture.md)

---

## Protocol

SteeringPhone uses a **custom 43-byte binary protocol** over UDP or WebSocket:

```
Magic(1) | Version(1) | Seq(2) | Timestamp(8) | Steering(4) |
Accel XYZ(12) | GyroZ(4) | Buttons(2) | Throttle(1) | Brake(1) |
Clutch(1) | Battery(1) | Signal(1) | Ping(2) | CRC16(2)
```

Full spec → [Protocol.md](docs/Protocol.md)

---

## Building from Source

See [DevelopmentSetup.md](docs/DevelopmentSetup.md) for full instructions.

**Android:**
```bash
cd apps/android
./gradlew assembleDebug
```

**Windows:**
```powershell
cd apps/windows
dotnet build SteeringPhone.Desktop.sln
```

---

## Contributing

See [Contributing.md](docs/Contributing.md). We welcome PRs for new game profiles, bug fixes, and feature additions.

---

## Roadmap

See [Roadmap.md](docs/Roadmap.md) for planned features and milestones.

---

## License

MIT License. See [LICENSE](LICENSE).

---

## Acknowledgements

- [ViGEmBus](https://github.com/ViGEm/ViGEmBus) by Nefarius Software Solutions
- [Nefarius.ViGEm.Client](https://github.com/ViGEm/ViGEm.NET)
- [Ktor](https://ktor.io/)
- [Jetpack Compose](https://developer.android.com/compose)
