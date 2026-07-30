# SteeringPhone — Binary Communication Protocol

Version: **1.0**  
Status: **Stable**

---

## Overview

SteeringPhone uses a custom binary protocol designed for:
- **Minimal overhead** — 43 bytes per packet
- **Maximum speed** — trivially serializable/deserializable without reflection
- **Reliability** — CRC-16/CCITT checksum on every packet
- **Versioning** — version byte in every packet header
- **Future-proofing** — reserved space in button mask for expansion

---

## Transport Layer

| Mode | Transport | Direction | Port |
|------|-----------|-----------|------|
| WiFi primary | UDP | Phone → PC | 45680 |
| WiFi discovery | UDP broadcast | Phone → PC | 45678 |
| WiFi discovery reply | UDP unicast | PC → Phone | 45678 |
| WiFi reliable | WebSocket (WS) | Phone → PC | 45679 |
| USB | WebSocket over ADB forward | Phone → PC | 45679 |

### Why two WiFi modes?
- **UDP (45680)**: Primary data stream. No ACK overhead. Packet loss is acceptable — stale frames are simply discarded.
- **WebSocket (45679)**: Used as fallback when UDP is blocked by router isolation, and as the transport for connection handshake / configuration messages.

---

## Packet Format

### DrivePacket (43 bytes, all little-endian)

```
 0                   1                   2                   3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
├───────────────┬───────────────┬───────────────────────────────────┤
│  MAGIC (0xD5) │  VERSION(0x01)│         SEQUENCE (u16)            │
├───────────────────────────────────────────────────────────────────┤
│                      TIMESTAMP_US (i64, 8 bytes)                  │
│                                                                   │
├───────────────────────────────────────────────────────────────────┤
│                   STEERING_ANGLE (f32, 4 bytes)                   │
├───────────────────────────────────────────────────────────────────┤
│                      ACCEL_X (f32, 4 bytes)                       │
├───────────────────────────────────────────────────────────────────┤
│                      ACCEL_Y (f32, 4 bytes)                       │
├───────────────────────────────────────────────────────────────────┤
│                      ACCEL_Z (f32, 4 bytes)                       │
├───────────────────────────────────────────────────────────────────┤
│                      GYRO_Z (f32, 4 bytes)                        │
├───────────────────────────────────────────────────────────────────┤
│           BUTTON_MASK (u16)              │  THROTTLE  │  BRAKE    │
├───────────────────────────────────────────────────────────────────┤
│  CLUTCH (u8) │ BATTERY (u8) │ SIGNAL (u8) │    PING_MS (u16)     │
├───────────────────────────────────────────────────────────────────┤
│                     CRC16 (u16, 2 bytes)                          │
└───────────────────────────────────────────────────────────────────┘
```

### Field Definitions

| Offset | Size | Type | Field | Range | Description |
|--------|------|------|-------|-------|-------------|
| 0 | 1 | u8 | MAGIC | `0xD5` | Sync byte. Packet rejected if mismatch. |
| 1 | 1 | u8 | VERSION | `0x01` | Protocol version. |
| 2 | 2 | u16 | SEQUENCE | 0–65535 | Monotonically incrementing, wraps. Used for packet loss detection. |
| 4 | 8 | i64 | TIMESTAMP_US | epoch µs | Phone clock in microseconds since Unix epoch. Used for latency calculation. |
| 12 | 4 | f32 | STEERING_ANGLE | -1.0–+1.0 | Normalized steering. -1.0 = full left, +1.0 = full right. |
| 16 | 4 | f32 | ACCEL_X | m/s² | Raw accelerometer X axis. |
| 20 | 4 | f32 | ACCEL_Y | m/s² | Raw accelerometer Y axis. |
| 24 | 4 | f32 | ACCEL_Z | m/s² | Raw accelerometer Z axis. |
| 28 | 4 | f32 | GYRO_Z | rad/s | Gyroscope Z axis (primary steering axis). |
| 32 | 2 | u16 | BUTTON_MASK | bitfield | See Button Mask table below. |
| 34 | 1 | u8 | THROTTLE | 0–255 | Throttle pedal. 0=released, 255=fully pressed. |
| 35 | 1 | u8 | BRAKE | 0–255 | Brake pedal. |
| 36 | 1 | u8 | CLUTCH | 0–255 | Clutch pedal. |
| 37 | 1 | u8 | BATTERY | 0–100 | Phone battery percentage. |
| 38 | 1 | u8 | SIGNAL | 0–100 | WiFi signal quality (0 if USB). |
| 39 | 2 | u16 | PING_MS | 0–65535 | Last measured round-trip ping in ms. |
| 41 | 2 | u16 | CRC16 | — | CRC-16/CCITT-FALSE over bytes 0–40 (41 bytes). |

**Total: 43 bytes**

---

### Button Mask (u16 bitmask)

| Bit | Constant | Description |
|-----|----------|-------------|
| 0 | `BTN_THROTTLE` | Throttle (Mode A virtual button) |
| 1 | `BTN_BRAKE` | Brake (Mode A virtual button) |
| 2 | `BTN_HAND_BRAKE` | Handbrake |
| 3 | `BTN_REVERSE` | Reverse gear toggle |
| 4 | `BTN_GEAR_UP` | Sequential gear up |
| 5 | `BTN_GEAR_DOWN` | Sequential gear down |
| 6 | `BTN_CLUTCH` | Clutch (Mode A virtual button) |
| 7 | `BTN_HORN` | Horn |
| 8 | `BTN_LEFT_INDICATOR` | Left turn signal |
| 9 | `BTN_RIGHT_INDICATOR` | Right turn signal |
| 10 | `BTN_HEADLIGHTS` | Toggle headlights |
| 11 | `BTN_CAMERA` | Change camera |
| 12 | `BTN_PAUSE` | Pause / ESC |
| 13 | `BTN_MENU` | Menu / Start |
| 14 | `BTN_NITRO` | Nitro / boost |
| 15 | `BTN_CUSTOM_1` | User-configurable custom button |

---

## Discovery Protocol

### Phase 1: Phone broadcasts (UDP, 45678)

Phone sends to `255.255.255.255:45678` every 500 ms:

```json
{
  "type": "STEERINGPHONE_DISCOVER",
  "version": 1,
  "deviceId": "unique-uuid-v4",
  "deviceName": "Pixel 7 Pro",
  "platform": "android",
  "appVersion": "1.0.0"
}
```

### Phase 2: PC responds (UDP, unicast to phone)

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

### Phase 3: Handshake (WebSocket, 45679)

Phone connects and sends:

```json
{
  "type": "CONNECT",
  "deviceId": "unique-uuid-v4",
  "deviceName": "Pixel 7 Pro",
  "capabilities": ["GYRO", "ACCEL", "HAPTIC"]
}
```

PC confirms:

```json
{
  "type": "CONNECTED",
  "sessionId": "session-uuid",
  "profileName": "Euro Truck Simulator 2",
  "updateRateHz": 120
}
```

---

## Heartbeat

Both sides send a JSON heartbeat every 1000 ms:

```json
{"type": "PING", "ts": 1690000000000000}
```

Response:
```json
{"type": "PONG", "ts": 1690000000000000, "serverTs": 1690000000001234}
```

If no packet received in 3000 ms, connection is considered lost and reconnection begins.

---

## CRC Calculation

CRC-16/CCITT-FALSE (polynomial `0x1021`, init `0xFFFF`, no input/output reflection).

Applied over bytes `[0..40]` (all 41 bytes before the CRC field). The two CRC bytes at offsets 41–42 are appended in little-endian order.

### Reference implementation (Kotlin)

```kotlin
fun crc16(data: ByteArray, length: Int): UShort {
    var crc = 0xFFFF
    for (i in 0 until length) {
        crc = crc xor (data[i].toInt() shl 8)
        repeat(8) {
            crc = if ((crc and 0x8000) != 0) (crc shl 1) xor 0x1021 else crc shl 1
            crc = crc and 0xFFFF
        }
    }
    return crc.toUShort()
}
```

### Reference implementation (C#)

```csharp
public static ushort Crc16(ReadOnlySpan<byte> data)
{
    ushort crc = 0xFFFF;
    foreach (var b in data)
    {
        crc ^= (ushort)(b << 8);
        for (int i = 0; i < 8; i++)
            crc = (crc & 0x8000) != 0 ? (ushort)((crc << 1) ^ 0x1021) : (ushort)(crc << 1);
    }
    return crc;
}
```

---

## Version Negotiation

If VERSION in the packet header does not match the supported version, the receiver:
1. Logs a warning
2. Drops the packet
3. Sends a `VERSION_MISMATCH` message over the WebSocket control channel

The control channel always supports reading version 1 JSON messages regardless of binary protocol version.

---

## Future Extensions (v2 planned)

- Additional gyro axes (full 3D orientation via quaternion)
- Force feedback commands (PC → Phone)
- Audio cue commands (PC → Phone)
- Extended button mask (32-bit)
- Multi-phone support (passenger co-pilot seat)
