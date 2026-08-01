# SteeringPhone — Development Roadmap

## Vision

Make SteeringPhone the **best free, open-source phone-as-controller solution** for PC sim racing, with professional-grade latency, reliability, and UX.

---

## Release Milestones

### v0.1.0 — Foundation (Phase 1–2)
**Status:** Completed

- [x] Project architecture & folder structure
- [x] Documentation suite
- [x] Binary protocol specification
- [x] Protocol serializer / deserializer (Android)
- [x] Protocol deserializer (Windows)
- [x] CRC validation
- [x] Protocol unit tests

---

### v0.2.0 — Sensor Pipeline (Phase 3)
**Status:** Completed

- [x] SensorManager wrapper (120 Hz)
- [x] Complementary filter (gyro + accel fusion)
- [x] Low-pass filter
- [x] SteeringCalculator (normalize, deadzone, curve, sensitivity)
- [x] Unit tests for all filter math

---

### v0.3.0 — Android Network (Phase 4)
**Status:** Completed

- [x] UDP client (60–120 Hz send)
- [x] WebSocket client
- [x] ADB forwarder
- [x] UDP discovery broadcast
- [x] Connection state machine
- [x] Automatic reconnect

---

### v0.4.0 — Android UI (Phase 5)
**Status:** Completed

- [x] Material 3 design system
- [x] Steering screen (live wheel indicator)
- [x] Virtual pedals screen (Mode A)
- [x] Slider pedals screen (Mode B)
- [x] Connection screen (auto-discovery list)
- [x] Haptic feedback
- [x] Portrait + landscape support

---

### v0.5.0 — Windows Receiver (Phase 6)
**Status:** Completed

- [x] UDP server
- [x] WebSocket server
- [x] ADB bridge
- [x] Discovery service
- [x] Packet receiver pipeline
- [x] Connection manager

---

### v0.6.0 — ViGEm Integration (Phase 7)
**Status:** Completed

- [x] ViGEmBus virtual Xbox 360 controller
- [x] Steering → left thumbstick X
- [x] Throttle → right trigger
- [x] Brake → left trigger
- [x] All 16 buttons mapped
- [x] Controller test page

---

### v0.7.0 — Windows UI (Phase 8)
**Status:** Completed

- [x] WinUI 3 shell with navigation
- [x] Dashboard page (live latency, FPS, packet loss, battery, angle)
- [x] Device Manager page
- [x] Profiles page (CRUD)
- [x] Logs page (Serilog sink)

---

### v0.8.0 — Calibration & Profiles (Phase 9)
**Status:** Completed

- [x] Calibration wizard (Android side)
- [x] Calibration management (Windows side)
- [x] Per-profile: sensitivity, deadzone, rotation limits
- [x] Per-profile: button mappings
- [x] Built-in game presets (ETS2, ATS, Forza, BeamNG, AC, F1)
- [x] Profile import / export (JSON)

---

### v1.0.0 — Release (Phase 10)
**Status:** Completed

- [x] Performance profiling & optimization
- [x] Full test suite (unit + integration + latency)
- [x] Installer (MSIX for Windows, APK + Play Store for Android)
- [x] Documentation finalization
- [x] GitHub release with binaries

---

## Long-Term Roadmap (Post v1.0)

| Feature | Priority | Notes |
|---------|----------|-------|
| Force feedback (phone vibration) | High | PC → Phone FFB commands |
| Multi-phone support | Medium | Co-pilot / passenger controls |
| Linux support | Low | Wine/Proton compatibility |
| Mac support | Low | Out of scope for now |
| Wheel-on-desk mode | Medium | Phone mounted horizontally |
| Android Auto integration | Low | Show SteeringPhone on car screen |
| Custom profile marketplace | Medium | Share profiles online |
| OBD-II integration | Low | Real car data overlay |
| VR mode | Low | Head tracking via phone gyro |

---

## Version Support Policy

- **Patch (x.x.N):** Bug fixes, no breaking changes
- **Minor (x.N.0):** New features, backward-compatible protocol changes
- **Major (N.0.0):** Breaking protocol changes, minimum 6-month deprecation window

---

## Contributing to the Roadmap

Open an issue tagged `roadmap` with your feature request. Items voted ≥10 👍 are automatically promoted to the formal roadmap.
