namespace SteeringPhone.Core.Protocol;

[Flags]
public enum ButtonMask : ushort
{
    None = 0,
    ThrottleBtn = 1 << 0,
    BrakeBtn = 1 << 1,
    HandBrake = 1 << 2,
    Reverse = 1 << 3,
    GearUp = 1 << 4,
    GearDown = 1 << 5,
    ClutchBtn = 1 << 6,
    Horn = 1 << 7,
    LeftIndicator = 1 << 8,
    RightIndicator = 1 << 9,
    Headlights = 1 << 10,
    Camera = 1 << 11,
    Pause = 1 << 12,
    Menu = 1 << 13,
    Nitro = 1 << 14,
    Custom1 = 1 << 15
}
