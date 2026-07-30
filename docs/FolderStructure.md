# SteeringPhone — Folder Structure

Complete annotated folder tree for the SteeringPhone project.

---

```
SteeringPhone/
│
├── .github/                            ← GitHub configuration
│   ├── workflows/
│   │   ├── android-ci.yml              ← Android build, test, lint
│   │   └── windows-ci.yml             ← Windows build, test, format
│   └── ISSUE_TEMPLATE/
│       ├── bug_report.md
│       └── feature_request.md
│
├── apps/                               ← Application source code
│   │
│   ├── android/                        ← Android Studio project root
│   │   ├── app/
│   │   │   └── src/
│   │   │       ├── main/
│   │   │       │   ├── kotlin/
│   │   │       │   │   └── dev/steeringphone/
│   │   │       │   │       │
│   │   │       │   │       ├── DriveApplication.kt        ← Hilt Application class
│   │   │       │   │       ├── MainActivity.kt            ← Single activity host
│   │   │       │   │       │
│   │   │       │   │       ├── core/                      ← App-wide infrastructure
│   │   │       │   │       │   ├── di/                    ← Hilt DI modules
│   │   │       │   │       │   │   ├── AppModule.kt       ← Coroutine dispatchers, logger
│   │   │       │   │       │   │   ├── NetworkModule.kt   ← UDP/WS/ADB clients
│   │   │       │   │       │   │   ├── SensorModule.kt    ← SensorManager, filters
│   │   │       │   │       │   │   └── RepositoryModule.kt← Repository bindings
│   │   │       │   │       │   ├── extensions/
│   │   │       │   │       │   │   ├── FlowExtensions.kt  ← Flow utility operators
│   │   │       │   │       │   │   └── ContextExtensions.kt
│   │   │       │   │       │   └── utils/
│   │   │       │   │       │       ├── Logger.kt          ← Structured logging wrapper
│   │   │       │   │       │       └── MathUtils.kt       ← Angle math, normalization
│   │   │       │   │       │
│   │   │       │   │       ├── domain/                    ← Pure business logic (no Android)
│   │   │       │   │       │   ├── models/                ← Immutable data classes
│   │   │       │   │       │   │   ├── SteeringData.kt    ← Sensor-derived steering state
│   │   │       │   │       │   │   ├── ControllerState.kt ← Full controller snapshot
│   │   │       │   │       │   │   ├── Profile.kt         ← Game profile settings
│   │   │       │   │       │   │   ├── CalibrationData.kt ← Calibration offsets
│   │   │       │   │       │   │   ├── ConnectionInfo.kt  ← Connection state + stats
│   │   │       │   │       │   │   └── PedalMode.kt       ← Enum: BUTTONS / SLIDERS
│   │   │       │   │       │   ├── usecases/
│   │   │       │   │       │   │   ├── steering/
│   │   │       │   │       │   │   │   ├── CalculateSteeringUseCase.kt
│   │   │       │   │       │   │   │   └── ApplySteeringFiltersUseCase.kt
│   │   │       │   │       │   │   ├── connection/
│   │   │       │   │       │   │   │   ├── ConnectUsbUseCase.kt
│   │   │       │   │       │   │   │   ├── ConnectWifiUseCase.kt
│   │   │       │   │       │   │   │   └── DiscoverHostsUseCase.kt
│   │   │       │   │       │   │   ├── calibration/
│   │   │       │   │       │   │   │   └── CalibrateWheelUseCase.kt
│   │   │       │   │       │   │   └── profiles/
│   │   │       │   │       │   │       └── ManageProfilesUseCase.kt
│   │   │       │   │       │   └── repositories/          ← Interfaces only
│   │   │       │   │       │       ├── IProfileRepository.kt
│   │   │       │   │       │       ├── ICalibrationRepository.kt
│   │   │       │   │       │       └── IConnectionRepository.kt
│   │   │       │   │       │
│   │   │       │   │       ├── data/                      ← Repository implementations
│   │   │       │   │       │   ├── repositories/
│   │   │       │   │       │   │   ├── ProfileRepositoryImpl.kt
│   │   │       │   │       │   │   ├── CalibrationRepositoryImpl.kt
│   │   │       │   │       │   │   └── ConnectionRepositoryImpl.kt
│   │   │       │   │       │   ├── serialization/
│   │   │       │   │       │   │   └── ProtocolSerializer.kt  ← Binary packet builder
│   │   │       │   │       │   └── storage/
│   │   │       │   │       │       ├── ProfileDataStore.kt    ← DataStore<Preferences>
│   │   │       │   │       │       └── CalibrationDataStore.kt
│   │   │       │   │       │
│   │   │       │   │       ├── sensors/                   ← Sensor pipeline
│   │   │       │   │       │   ├── SensorManagerWrapper.kt ← Android SensorManager abstraction
│   │   │       │   │       │   ├── SensorFusion.kt        ← Combines accel + gyro
│   │   │       │   │       │   ├── LowPassFilter.kt       ← Configurable α low-pass
│   │   │       │   │       │   ├── ComplementaryFilter.kt ← Gyro drift correction
│   │   │       │   │       │   └── SteeringCalculator.kt  ← Angle → normalized output
│   │   │       │   │       │
│   │   │       │   │       ├── network/                   ← Network transport layer
│   │   │       │   │       │   ├── DrivePacket.kt         ← Binary packet definition
│   │   │       │   │       │   ├── PacketBuilder.kt       ← Serializes DrivePacket → ByteArray
│   │   │       │   │       │   ├── UdpClient.kt           ← High-frequency UDP sender
│   │   │       │   │       │   ├── WebSocketClient.kt     ← Reliable WS transport
│   │   │       │   │       │   ├── AdbForwarder.kt        ← ADB port forwarding helper
│   │   │       │   │       │   └── DiscoveryClient.kt     ← UDP broadcast discovery
│   │   │       │   │       │
│   │   │       │   │       └── features/                  ← UI feature modules
│   │   │       │   │           ├── connection/
│   │   │       │   │           │   ├── ConnectionViewModel.kt
│   │   │       │   │           │   └── ui/
│   │   │       │   │           │       ├── ConnectionScreen.kt
│   │   │       │   │           │       ├── UsbConnectionCard.kt
│   │   │       │   │           │       └── WifiDiscoveryCard.kt
│   │   │       │   │           ├── steering/
│   │   │       │   │           │   ├── SteeringViewModel.kt
│   │   │       │   │           │   └── ui/
│   │   │       │   │           │       ├── SteeringScreen.kt
│   │   │       │   │           │       ├── WheelIndicator.kt
│   │   │       │   │           │       └── ControlButtonsBar.kt
│   │   │       │   │           ├── pedals/
│   │   │       │   │           │   ├── PedalsViewModel.kt
│   │   │       │   │           │   └── ui/
│   │   │       │   │           │       ├── VirtualPedalsScreen.kt
│   │   │       │   │           │       └── SliderPedalsScreen.kt
│   │   │       │   │           ├── calibration/
│   │   │       │   │           │   ├── CalibrationViewModel.kt
│   │   │       │   │           │   └── ui/
│   │   │       │   │           │       ├── CalibrationWizardScreen.kt
│   │   │       │   │           │       └── CalibrationStep.kt
│   │   │       │   │           ├── profiles/
│   │   │       │   │           │   ├── ProfilesViewModel.kt
│   │   │       │   │           │   └── ui/
│   │   │       │   │           │       ├── ProfilesScreen.kt
│   │   │       │   │           │       └── ProfileCard.kt
│   │   │       │   │           └── settings/
│   │   │       │   │               ├── SettingsViewModel.kt
│   │   │       │   │               └── ui/
│   │   │       │   │                   └── SettingsScreen.kt
│   │   │       │   │
│   │   │       │   ├── res/
│   │   │       │   │   ├── values/
│   │   │       │   │   │   ├── strings.xml
│   │   │       │   │   │   └── themes.xml
│   │   │       │   │   └── drawable/
│   │   │       │   └── AndroidManifest.xml
│   │   │       │
│   │   │       ├── test/kotlin/          ← Unit tests (JVM)
│   │   │       └── androidTest/kotlin/   ← Instrumented tests
│   │   │
│   │   ├── build.gradle.kts             ← Root Gradle build script
│   │   ├── settings.gradle.kts          ← Gradle settings
│   │   ├── gradle.properties            ← Kotlin, JVM settings
│   │   └── proguard-rules.pro
│   │
│   └── windows/                         ← .NET 9 / WinUI 3 solution
│       ├── SteeringPhone.Desktop/
│       │   │
│       │   ├── Core/                    ← App infrastructure
│       │   │   ├── DI/
│       │   │   │   └── ServiceCollectionExtensions.cs  ← All service registrations
│       │   │   ├── Logging/
│       │   │   │   └── SerilogConfiguration.cs        ← Serilog sinks + enrichers
│       │   │   └── Configuration/
│       │   │       └── AppSettings.cs                 ← Strongly-typed config
│       │   │
│       │   ├── Domain/                  ← Pure C# business logic
│       │   │   ├── Models/
│       │   │   │   ├── SteeringData.cs
│       │   │   │   ├── ControllerState.cs
│       │   │   │   ├── Profile.cs
│       │   │   │   ├── CalibrationData.cs
│       │   │   │   ├── ConnectedDevice.cs
│       │   │   │   └── ConnectionStats.cs
│       │   │   ├── UseCases/
│       │   │   │   ├── Connection/
│       │   │   │   │   └── ManageConnectionUseCase.cs
│       │   │   │   ├── Controller/
│       │   │   │   │   └── MapToControllerUseCase.cs
│       │   │   │   └── Profiles/
│       │   │   │       └── ManageProfilesUseCase.cs
│       │   │   └── Interfaces/
│       │   │       ├── IVirtualController.cs
│       │   │       ├── IPacketReceiver.cs
│       │   │       ├── IProfileRepository.cs
│       │   │       ├── IConnectionService.cs
│       │   │       └── IDiscoveryService.cs
│       │   │
│       │   ├── Data/                    ← Data layer implementations
│       │   │   ├── Repositories/
│       │   │   │   └── ProfileRepositoryImpl.cs
│       │   │   ├── Database/
│       │   │   │   ├── SteeringPhoneContext.cs        ← EF Core DbContext
│       │   │   │   └── Migrations/
│       │   │   └── Serialization/
│       │   │       └── PacketDeserializer.cs          ← Binary → DrivePacket
│       │   │
│       │   ├── Network/                 ← Transport implementations
│       │   │   ├── UdpServer.cs         ← High-freq UDP receiver
│       │   │   ├── WebSocketServer.cs   ← WebSocket server + control channel
│       │   │   ├── AdbBridge.cs         ← ADB device detection + forwarding
│       │   │   └── DiscoveryService.cs  ← UDP HELLO responder
│       │   │
│       │   ├── Services/                ← Application services
│       │   │   ├── ViGEmController.cs   ← Virtual Xbox 360 via ViGEmBus
│       │   │   ├── InputProcessor.cs    ← Packet → controller values
│       │   │   ├── SteeringMapper.cs    ← Profile-aware axis/button mapping
│       │   │   └── ConnectionManager.cs ← State machine + transport selection
│       │   │
│       │   └── Features/                ← WinUI 3 pages + ViewModels
│       │       ├── Dashboard/
│       │       │   ├── DashboardViewModel.cs
│       │       │   └── Views/
│       │       │       └── DashboardPage.xaml
│       │       ├── DeviceManager/
│       │       │   ├── DeviceManagerViewModel.cs
│       │       │   └── Views/
│       │       │       └── DeviceManagerPage.xaml
│       │       ├── Profiles/
│       │       │   ├── ProfilesViewModel.cs
│       │       │   └── Views/
│       │       │       └── ProfilesPage.xaml
│       │       ├── Calibration/
│       │       │   ├── CalibrationViewModel.cs
│       │       │   └── Views/
│       │       │       └── CalibrationPage.xaml
│       │       ├── Controller/
│       │       │   ├── ControllerTestViewModel.cs
│       │       │   └── Views/
│       │       │       └── ControllerTestPage.xaml
│       │       └── Logs/
│       │           ├── LogsViewModel.cs
│       │           └── Views/
│       │               └── LogsPage.xaml
│       │
│       └── SteeringPhone.Desktop.csproj
│
├── shared/                              ← Platform-agnostic assets
│   ├── protocol/
│   │   └── DrivePacket.md              ← Authoritative packet spec
│   └── documentation/
│       └── diagrams/                   ← Architecture diagrams
│
├── docs/                               ← Project documentation
│   ├── README.md                       → (same as root README.md)
│   ├── Architecture.md
│   ├── Protocol.md
│   ├── Roadmap.md
│   ├── Contributing.md
│   ├── API.md
│   ├── FolderStructure.md              ← This file
│   └── DevelopmentSetup.md
│
├── tests/                              ← Standalone test projects
│   ├── android/
│   │   └── unit/                       ← JVM unit tests (sensor math, protocol)
│   └── windows/
│       ├── unit/                       ← xUnit: domain logic, mapping
│       └── integration/                ← Integration: UDP, WS, ADB
│
├── tools/                              ← Developer utilities
│   ├── adb-setup.ps1                   ← Auto-configure ADB port forwarding
│   └── vigem-installer.ps1            ← Download + install ViGEmBus driver
│
├── assets/                             ← Shared visual assets
│   ├── icons/
│   │   ├── app-icon.png
│   │   └── profiles/                  ← Per-game profile icons
│   └── images/
│       └── screenshots/
│
├── scripts/                            ← CI/CD build scripts
│   ├── build-android.sh
│   └── build-windows.ps1
│
├── README.md                           ← Project entry point
└── LICENSE                             ← MIT License
```

---

## Layer Responsibilities

| Layer | Android | Windows |
|-------|---------|---------|
| **Presentation** | Jetpack Compose screens + ViewModels | WinUI 3 XAML pages + ViewModels |
| **Domain** | Use cases, interfaces, data classes | Use cases, interfaces, record types |
| **Data** | DataStore, Room (future), serialization | EF Core SQLite, packet deserializer |
| **Infrastructure** | SensorManager, Ktor, ADB | ViGEmBus, UDP/WS servers, ADB bridge |

## Naming Conventions

| Kind | Convention | Example |
|------|-----------|---------|
| Interfaces | `I` prefix | `IProfileRepository` |
| ViewModels | `ViewModel` suffix | `SteeringViewModel` |
| Use Cases | `UseCase` suffix | `CalibrateWheelUseCase` |
| Repositories (impl) | `Impl` suffix | `ProfileRepositoryImpl` |
| Screens (Compose) | `Screen` suffix | `SteeringScreen` |
| Pages (WinUI) | `Page` suffix | `DashboardPage` |
| Services | `Service` suffix | `ConnectionManager`, `ViGEmController` |
