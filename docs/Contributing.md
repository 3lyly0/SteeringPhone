# SteeringPhone — Contributing Guide

Thank you for contributing to SteeringPhone!

---

## Ground Rules

1. **No placeholder code.** Every submitted function must have a real implementation.
2. **No TODOs in merged code.** Open an issue instead.
3. **Tests required.** New features need unit tests. Bug fixes need a regression test.
4. **Clean Architecture.** Domain layer must not import Android/Windows framework code.
5. **One PR per feature.** Keep PRs focused and reviewable.

---

## Development Setup

See [DevelopmentSetup.md](DevelopmentSetup.md) for environment setup.

---

## Branching Strategy

```
main              ← stable releases only
  └── develop     ← integration branch
        ├── feature/steering-filter-improvements
        ├── feature/forza-profile
        └── fix/usb-reconnect-crash
```

- Branch from `develop`
- PR back to `develop`
- `develop` → `main` on each release

---

## Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat(android): add complementary filter for gyro fusion
fix(windows): correct CRC calculation for little-endian fields
docs(protocol): clarify PING_MS field range
test(android): add unit tests for SteeringCalculator
perf(windows): use Span<byte> for zero-copy deserialization
```

**Types:** `feat`, `fix`, `docs`, `test`, `perf`, `refactor`, `chore`, `ci`

---

## Code Style

### Kotlin (Android)
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Max line length: 120 characters
- Use `ktlint` for formatting (`./gradlew ktlint`)
- No `!!` (non-null assertion) — handle nullability properly

### C# (Windows)
- Follow [Microsoft C# Coding Conventions](https://docs.microsoft.com/en-us/dotnet/csharp/fundamentals/coding-style/coding-conventions)
- Max line length: 120 characters
- Use `dotnet format` before committing
- Use `record` types for immutable domain models

---

## Adding a Game Profile

1. Create a new entry in `apps/android/src/main/kotlin/dev/steeringphone/features/profiles/data/BuiltInProfiles.kt`
2. Set appropriate sensitivity, deadzone, rotation limit, and button mappings
3. Add a profile icon in `assets/icons/profiles/`
4. Test with the target game and document findings in the PR

---

## Adding a New Button

1. Add the bit to `BUTTON_MASK` in `docs/Protocol.md`
2. Update `DrivePacket.kt` (Android) and `DrivePacket.cs` (Windows)
3. Add constant to `ButtonMask.kt` and `ButtonMask.cs`
4. Update `ViGEmController.cs` to map the new button
5. Add UI control in `ControlButtonsBar.kt`
6. Update protocol version if breaking

---

## Pull Request Checklist

- [ ] Branch is from `develop`
- [ ] All new code has tests
- [ ] `./gradlew test` passes (Android)
- [ ] `dotnet test` passes (Windows)
- [ ] `./gradlew ktlint` passes
- [ ] `dotnet format --verify-no-changes` passes
- [ ] CHANGELOG.md updated
- [ ] No hardcoded strings (use `strings.xml` / resource files)

---

## Reporting Issues

Use GitHub Issues. Include:
- Platform (Android version, Windows version)
- Connection method (USB / WiFi)
- Steps to reproduce
- Expected vs actual behavior
- Logcat output (Android) or SteeringPhone log file (Windows)

---

## License

By contributing, you agree your code will be licensed under the MIT License.
