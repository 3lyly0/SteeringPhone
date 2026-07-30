# SteeringPhone — API Reference

## Overview

SteeringPhone's internal API surface is organized into three categories:

1. **Android → Windows (Binary Packets)** — The primary data stream
2. **Discovery & Handshake (JSON over UDP/WebSocket)** — Session establishment
3. **Windows Internal APIs** — ViGEm controller, profile management

---

## 1. Binary Packet API

See [Protocol.md](Protocol.md) for the full wire format.

### Packet Constants

```kotlin
// Kotlin (Android)
object PacketConstants {
    const val MAGIC: Byte = 0xD5.toByte()
    const val VERSION: Byte = 0x01
    const val SIZE: Int = 43
    const val DISCOVERY_PORT: Int = 45678
    const val WEBSOCKET_PORT: Int = 45679
    const val UDP_DATA_PORT: Int = 45680
    const val HEARTBEAT_INTERVAL_MS: Long = 1000L
    const val CONNECTION_TIMEOUT_MS: Long = 3000L
}
```

```csharp
// C# (Windows)
public static class PacketConstants
{
    public const byte Magic = 0xD5;
    public const byte Version = 0x01;
    public const int Size = 43;
    public const int DiscoveryPort = 45678;
    public const int WebSocketPort = 45679;
    public const int UdpDataPort = 45680;
    public const int HeartbeatIntervalMs = 1000;
    public const int ConnectionTimeoutMs = 3000;
}
```

---

### Button Mask Constants

```kotlin
// Kotlin
object ButtonMask {
    const val THROTTLE: Int        = 1 shl 0
    const val BRAKE: Int           = 1 shl 1
    const val HAND_BRAKE: Int      = 1 shl 2
    const val REVERSE: Int         = 1 shl 3
    const val GEAR_UP: Int         = 1 shl 4
    const val GEAR_DOWN: Int       = 1 shl 5
    const val CLUTCH: Int          = 1 shl 6
    const val HORN: Int            = 1 shl 7
    const val LEFT_INDICATOR: Int  = 1 shl 8
    const val RIGHT_INDICATOR: Int = 1 shl 9
    const val HEADLIGHTS: Int      = 1 shl 10
    const val CAMERA: Int          = 1 shl 11
    const val PAUSE: Int           = 1 shl 12
    const val MENU: Int            = 1 shl 13
    const val NITRO: Int           = 1 shl 14
    const val CUSTOM_1: Int        = 1 shl 15
}
```

---

## 2. Discovery & Handshake API (JSON)

All JSON messages sent over UDP (discovery) or WebSocket (handshake/control).

### `STEERINGPHONE_DISCOVER` (Phone → PC, UDP broadcast)

```json
{
  "type": "STEERINGPHONE_DISCOVER",
  "version": 1,
  "deviceId": "550e8400-e29b-41d4-a716-446655440000",
  "deviceName": "Pixel 7 Pro",
  "platform": "android",
  "appVersion": "1.0.0"
}
```

### `STEERINGPHONE_HELLO` (PC → Phone, UDP unicast)

```json
{
  "type": "STEERINGPHONE_HELLO",
  "version": 1,
  "hostName": "GAMING-PC",
  "host": "192.168.1.42",
  "wsPort": 45679,
  "udpPort": 45680,
  "appVersion": "1.0.0"
}
```

### `CONNECT` (Phone → PC, WebSocket)

```json
{
  "type": "CONNECT",
  "deviceId": "550e8400-e29b-41d4-a716-446655440000",
  "deviceName": "Pixel 7 Pro",
  "capabilities": ["GYRO", "ACCEL", "HAPTIC"]
}
```

### `CONNECTED` (PC → Phone, WebSocket)

```json
{
  "type": "CONNECTED",
  "sessionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "profileName": "Euro Truck Simulator 2",
  "updateRateHz": 120,
  "mode": "UDP"
}
```

### `PING` / `PONG`

```json
{"type": "PING", "ts": 1690000000000000}
{"type": "PONG", "ts": 1690000000000000, "serverTs": 1690000000001500}
```

### `DISCONNECT`

```json
{"type": "DISCONNECT", "reason": "USER_INITIATED"}
```

### `VERSION_MISMATCH`

```json
{
  "type": "VERSION_MISMATCH",
  "receivedVersion": 2,
  "supportedVersion": 1,
  "message": "Please update SteeringPhone Desktop."
}
```

### `HAPTIC` (PC → Phone, optional)

```json
{
  "type": "HAPTIC",
  "pattern": "BUMP",
  "durationMs": 100,
  "intensity": 0.8
}
```

---

## 3. Domain Interfaces (Windows)

### `IVirtualController`

```csharp
public interface IVirtualController : IDisposable
{
    bool IsConnected { get; }
    void SetSteering(float normalized);          // -1.0 to +1.0
    void SetThrottle(byte value);                // 0–255
    void SetBrake(byte value);                   // 0–255
    void SetClutch(byte value);                  // 0–255
    void SetButton(GamepadButton button, bool pressed);
    void Reset();
}
```

### `IPacketReceiver`

```csharp
public interface IPacketReceiver
{
    IObservable<DrivePacket> Packets { get; }
    Task StartAsync(CancellationToken cancellationToken);
    Task StopAsync();
}
```

### `IConnectionService`

```csharp
public interface IConnectionService
{
    IObservable<ConnectionState> State { get; }
    IObservable<ConnectedDevice> DeviceDiscovered { get; }
    Task ConnectAsync(ConnectedDevice device, CancellationToken cancellationToken);
    Task DisconnectAsync();
}
```

### `IProfileRepository`

```csharp
public interface IProfileRepository
{
    Task<IReadOnlyList<Profile>> GetAllAsync();
    Task<Profile?> GetByIdAsync(Guid id);
    Task<Profile> GetActiveAsync();
    Task SaveAsync(Profile profile);
    Task DeleteAsync(Guid id);
    Task SetActiveAsync(Guid id);
}
```

---

## 4. Domain Interfaces (Android)

### `IConnectionRepository`

```kotlin
interface IConnectionRepository {
    val connectionState: Flow<ConnectionState>
    val discoveredHosts: Flow<List<HostInfo>>
    suspend fun connectUsb(): Result<Unit>
    suspend fun connectWifi(host: HostInfo): Result<Unit>
    suspend fun disconnect()
}
```

### `IProfileRepository`

```kotlin
interface IProfileRepository {
    val profiles: Flow<List<Profile>>
    val activeProfile: Flow<Profile>
    suspend fun save(profile: Profile): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
    suspend fun setActive(id: String): Result<Unit>
}
```

### `ICalibrationRepository`

```kotlin
interface ICalibrationRepository {
    val calibration: Flow<CalibrationData>
    suspend fun save(data: CalibrationData): Result<Unit>
    suspend fun reset(): Result<Unit>
}
```

---

## 5. ViGEm Button Mapping

Default mapping (Xbox 360 virtual controller):

| SteeringPhone Button | Xbox 360 | XInput Constant |
|-----------------|----------|-----------------|
| HAND_BRAKE | A | `XUSB_BUTTON_A` |
| REVERSE | B | `XUSB_BUTTON_B` |
| CAMERA | X | `XUSB_BUTTON_X` |
| HORN | Y | `XUSB_BUTTON_Y` |
| GEAR_UP | Right Bumper | `XUSB_BUTTON_RIGHT_SHOULDER` |
| GEAR_DOWN | Left Bumper | `XUSB_BUTTON_LEFT_SHOULDER` |
| PAUSE | Start | `XUSB_BUTTON_START` |
| MENU | Back | `XUSB_BUTTON_BACK` |
| LEFT_INDICATOR | D-Pad Left | `XUSB_BUTTON_DPAD_LEFT` |
| RIGHT_INDICATOR | D-Pad Right | `XUSB_BUTTON_DPAD_RIGHT` |
| HEADLIGHTS | D-Pad Up | `XUSB_BUTTON_DPAD_UP` |
| NITRO | D-Pad Down | `XUSB_BUTTON_DPAD_DOWN` |
| CLUTCH | Left Stick Click | `XUSB_BUTTON_LEFT_THUMB` |
| CUSTOM_1 | Right Stick Click | `XUSB_BUTTON_RIGHT_THUMB` |
| THROTTLE (axis) | Right Trigger | Axis |
| BRAKE (axis) | Left Trigger | Axis |
| STEERING (axis) | Left Thumb X | Axis ±32767 |

All mappings are overridable per profile.
