namespace SteeringPhone.Core.Input;

/// <summary>
/// Converts normalized phone telemetry and button masks into virtual Xbox 360 controller signals.
/// </summary>
public static class InputMapper
{
    /// <summary>
    /// Maps normalized steering float [-1.0, +1.0] to short [-32768, +32767] for Xbox LeftThumbX axis.
    /// </summary>
    public static short MapSteeringToThumbstickX(float steeringAngle)
    {
        var clamped = Math.Clamp(steeringAngle, -1.0f, 1.0f);
        return (short)(clamped * 32767.0f);
    }

    /// <summary>
    /// Maps byte pedal value [0, 255] to trigger byte [0, 255].
    /// </summary>
    public static byte MapPedalToTrigger(byte pedalValue)
    {
        return pedalValue;
    }
}
