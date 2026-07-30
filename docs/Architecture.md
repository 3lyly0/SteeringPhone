# SteeringPhone — Architecture

## Guiding Principles

SteeringPhone is designed according to **Clean Architecture** with strict separation of concerns across every layer. Each layer only knows about the layer directly beneath it. Domain code never imports framework code.

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────┐
│                   Presentation Layer                     │
│         (Jetpack Compose / WinUI 3 / ViewModels)        │
├─────────────────────────────────────────────────────────┤
│                    Domain Layer                          │
│          (Use Cases, Domain Models, Interfaces)          │
├─────────────────────────────────────────────────────────┤
│                     Data Layer                           │
│       (Repositories, DataStore/SQLite, Serialization)    │
├─────────────────────────────────────────────────────────┤
│                 Infrastructure Layer                     │
│    (Sensors, Network, ViGEm, ADB, OS integrations)      │
└─────────────────────────────────────────────────────────┘
```

---

## Android Architecture

### Dependency Graph

```
DriveApplication
    └── Hilt (DI graph)
            ├── SensorModule → SensorManagerWrapper → SensorFusion → SteeringCalculator
            ├── NetworkModule → UdpClient / WebSocketClient / AdbForwarder / DiscoveryClient
            ├── RepositoryModule → ProfileRepositoryImpl → ProfileDataStore (DataStore<Preferences>)
            │                  → CalibrationRepositoryImpl → CalibrationDataStore
            └── AppModule → CoroutineDispatchers / Logger
```

### Sensor Pipeline

```
Android SensorManager (120 Hz)
    │
    ├── Raw Accelerometer [ax, ay, az] m/s²
    └── Raw Gyroscope [gx, gy, gz] rad/s
            │
    ComplementaryFilter
    (α = 0.98 for gyro trust, 1-α for accel correction)
            │
    LowPassFilter (configurable α, default 0.15)
            │
    SteeringCalculator
    - Normalize to [-1.0, +1.0]
    - Apply deadzone
    - Apply exponential curve
    - Apply sensitivity multiplier
    - Clamp to max rotation angle
            │
    SteeringData (domain model)
            │
    PacketBuilder → 43-byte DrivePacket
            │
    Network Layer (UDP / WebSocket / ADB)
```

### State Management

- All UI state is expressed as `StateFlow<UiState>` in ViewModels
- Side effects go through `SharedFlow<UiEffect>`
- Data layer returns `Flow<T>` for reactive streams and `Result<T>` for one-shots
- No mutable state escapes the ViewModel

### Threading Model

| Layer | Dispatcher |
|-------|-----------|
| UI | `Main` (Compose recomposition) |
| ViewModels | `Main.immediate` for state, `IO` for data ops |
| Sensor callbacks | Dedicated `HandlerThread` (Android sensor thread) |
| Network I/O | `IO` dispatcher (Ktor-managed) |
| Packet assembly | `Default` (CPU-bound math) |

---

## Windows Architecture

### Dependency Graph

```
App.xaml.cs
    └── Microsoft.Extensions.DependencyInjection (IServiceCollection)
            ├── Serilog (ILogger)
            ├── ConnectionManager → UdpServer / WebSocketServer / AdbBridge
            ├── DiscoveryService (UDP broadcast listener)
            ├── PacketDeserializer
            ├── InputProcessor → SteeringMapper → ViGEmController
            ├── ProfileRepositoryImpl → EF Core SQLite
            └── Feature ViewModels (Dashboard, DeviceManager, Profiles, …)
```

### Input Processing Pipeline

```
Network Layer (receives 43-byte packet)
    │
PacketDeserializer
- Magic check
- Version check
- CRC16 validation
- Deserialize fields
    │
InputProcessor (runs on dedicated high-priority thread)
    │
SteeringMapper
- Map steering [-1, +1] → Xbox thumbstick [-32768, +32767]
- Apply profile sensitivity / deadzone
- Exponential curve
    │
ViGEmController (Nefarius.ViGEm.Client)
- IXbox360Controller.SetAxisValue(GAMEPAD_AXIS.LeftThumbX, value)
- Map throttle/brake → triggers (0–255)
- Map buttons → gamepad buttons
    │
ViGEmBus (kernel driver)
    │
Windows XInput / DirectInput API
    │
Game
```

### UI Architecture (WinUI 3)

```
NavigationView (Shell)
    ├── DashboardPage     ← Real-time stats via DispatcherQueue
    ├── DeviceManagerPage ← Device list, pairing controls
    ├── ProfilesPage      ← CRUD profiles
    ├── CalibrationPage   ← Wizard steps
    ├── ControllerTestPage← Live controller visualizer
    └── LogsPage          ← Serilog sink → ObservableCollection
```

All pages use **CommunityToolkit.Mvvm** source-generated ObservableObject and RelayCommand. No code-behind logic — everything in ViewModels.

---

## Networking Architecture

### Connection State Machine

```
DISCONNECTED
    │
    ├── USB path ──── ADB device detected ──── ADB forward 45679 ──── WebSocket connect
    │                                                                        │
    └── WiFi path ─── UDP broadcast (45678) ─── HELLO response ─── UDP/WS connect
                                                                             │
                                                                     CONNECTED ─── STREAMING
                                                                         │
                                                                     Heartbeat timeout ──── RECONNECTING
                                                                                                 │
                                                                                            (retry loop)
```

### Packet Flow

```
Phone (60–120 Hz)
→ Build 43-byte packet
→ Compute CRC16
→ Send via UDP (WiFi) or WebSocket (USB/WiFi reliable)

PC (receive thread)
← Validate magic + version
← Validate CRC16
← Deserialize
← Feed to InputProcessor
← Update UI stats
```

---

## Key Design Decisions

### 1. Custom Binary Protocol vs. Protobuf
A fixed 43-byte packet serializes/deserializes in <1 µs on both platforms. Protobuf adds schema dependencies and ~5–10x overhead for a fixed schema that rarely changes. The custom protocol also allows zero-copy deserialization using `Span<byte>` on Windows.

### 2. UDP for WiFi, WebSocket for USB
UDP gives lowest latency for WiFi. WebSocket (over TCP via ADB forward) is used for USB because ADB forward is already a reliable, ordered channel — there's no benefit to UDP over it.

### 3. Complementary Filter vs. Kalman
Complementary filter gives 95% of Kalman's accuracy at 1% of the CPU cost. For a steering wheel (slow, predictable motion), this is ideal.

### 4. ViGEmBus vs. vJoy
ViGEmBus emulates real Xbox 360 / DualShock 4 hardware. It's the standard used by DS4Windows, XInput Plus, and others. vJoy is older, requires manual axis configuration, and lacks trigger support.

### 5. Hilt vs. Koin
Hilt generates DI code at compile-time — no runtime reflection, no startup overhead. For a latency-sensitive app, this matters.

---

## Performance Targets

| Metric | Target | Architecture Decision |
|--------|--------|----------------------|
| Sensor → packet | <1 ms | Dedicated HandlerThread, pre-allocated ByteArray |
| Packet → ViGEm | <2 ms | High-priority thread, Span<byte> zero-copy |
| WiFi round-trip | <20 ms | UDP, 60–120 Hz, no TCP overhead |
| USB round-trip | <10 ms | ADB forward, WebSocket over loopback |
| CPU (phone) | <5% | Complementary filter, no allocations in hot path |
| CPU (PC) | <2% | Lock-free ring buffer, pooled objects |
