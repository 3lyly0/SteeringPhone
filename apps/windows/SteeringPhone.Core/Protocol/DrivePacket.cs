using System.Runtime.InteropServices;

namespace SteeringPhone.Core.Protocol;

[StructLayout(LayoutKind.Sequential, Pack = 1)]
public readonly struct DrivePacket
{
    public byte Magic { get; }
    public byte Version { get; }
    public ushort SequenceNumber { get; }
    public long TimestampUs { get; }
    public float SteeringAngle { get; }
    public float AccelX { get; }
    public float AccelY { get; }
    public float AccelZ { get; }
    public float GyroZ { get; }
    public ButtonMask ButtonMask { get; }
    public byte Throttle { get; }
    public byte Brake { get; }
    public byte Clutch { get; }
    public byte BatteryPercentage { get; }
    public byte SignalQuality { get; }
    public ushort PingMs { get; }
    public ushort Crc16 { get; }

    public DrivePacket(
        byte magic,
        byte version,
        ushort sequenceNumber,
        long timestampUs,
        float steeringAngle,
        float accelX,
        float accelY,
        float accelZ,
        float gyroZ,
        ButtonMask buttonMask,
        byte throttle,
        byte brake,
        byte clutch,
        byte batteryPercentage,
        byte signalQuality,
        ushort pingMs,
        ushort crc16)
    {
        Magic = magic;
        Version = version;
        SequenceNumber = sequenceNumber;
        TimestampUs = timestampUs;
        SteeringAngle = steeringAngle;
        AccelX = accelX;
        AccelY = accelY;
        AccelZ = accelZ;
        GyroZ = gyroZ;
        ButtonMask = buttonMask;
        Throttle = throttle;
        Brake = brake;
        Clutch = clutch;
        BatteryPercentage = batteryPercentage;
        SignalQuality = signalQuality;
        PingMs = pingMs;
        Crc16 = crc16;
    }
}
