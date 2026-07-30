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
**Target:** 2 weeks after v0.1.0

- [ ] SensorManager wrapper (120 Hz)
- [ ] Complementary filter (gyro + accel fusion)
- [ ] Low-pass filter
- [ ] SteeringCalculator (normalize, deadzone, curve, sensitivity)
- [ ] Unit tests for all filter math

---

### v0.3.0 — Android Network (Phase 4)
**Target:** 1 week after v0.2.0

- [ ] UDP client (60–120 Hz send)
- [ ] WebSocket client
- [ ] ADB forwarder
- [ ] UDP discovery broadcast
- [ ] Connection state machine
- [ ] Automatic reconnect

---

### v0.4.0 — Android UI (Phase 5)
**Target:** 2 weeks after v0.3.0

- [ ] Material 3 design system
- [ ] Steering screen (live wheel indicator)
- [ ] Virtual pedals screen (Mode A)
- [ ] Slider pedals screen (Mode B)
- [ ] Connection screen (auto-discovery list)
- [ ] Haptic feedback
- [ ] Portrait + landscape support

---

### v0.5.0 — Windows Receiver (Phase 6)
**Target:** 1 week after v0.4.0

- [ ] UDP server
- [ ] WebSocket server
- [ ] ADB bridge
- [ ] Discovery service
- [ ] Packet receiver pipeline
- [ ] Connection manager

---

### v0.6.0 — ViGEm Integration (Phase 7)
**Target:** 1 week after v0.5.0

- [ ] ViGEmBus virtual Xbox 360 controller
- [ ] Steering → left thumbstick X
- [ ] Throttle → right trigger
- [ ] Brake → left trigger
- [ ] All 16 buttons mapped
- [ ] Controller test page

---

### v0.7.0 — Windows UI (Phase 8)
**Target:** 2 weeks after v0.6.0

- [ ] WinUI 3 shell with navigation
- [ ] Dashboard page (live latency, FPS, packet loss, battery, angle)
- [ ] Device Manager page
- [ ] Profiles page (CRUD)
- [ ] Logs page (Serilog sink)

---

### v0.8.0 — Calibration & Profiles (Phase 9)
**Target:** 1 week after v0.7.0

- [ ] Calibration wizard (Android side)
- [ ] Calibration management (Windows side)
- [ ] Per-profile: sensitivity, deadzone, rotation limits
- [ ] Per-profile: button mappings
- [ ] Built-in game presets (ETS2, ATS, Forza, BeamNG, AC, F1)
- [ ] Profile import / export (JSON)

---

### v1.0.0 — Release (Phase 10)
**Target:** 2 weeks after v0.8.0

- [ ] Performance profiling & optimization
- [ ] Full test suite (unit + integration + latency)
- [ ] Installer (MSIX for Windows, APK + Play Store for Android)
- [ ] Documentation finalization
- [ ] GitHub release with binaries

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
