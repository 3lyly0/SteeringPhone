using System.Buffers.Binary;
using SteeringPhone.Core.Protocol;
using Xunit;

namespace SteeringPhone.Tests;

public class PacketDeserializerTests
{
    private static byte[] ConstructValidPacketBytes(
        ushort seq = 100,
        long ts = 1690000000000000L,
        float steering = -0.5f,
        float ax = 0.1f, float ay = 9.8f, float az = 0.0f, float gz = 0.5f,
        ushort buttons = (ushort)ButtonMask.ThrottleBtn,
        byte throttle = 200, byte brake = 0, byte clutch = 50,
        byte battery = 90, byte signal = 100, ushort ping = 15)
    {
        byte[] buffer = new byte[PacketConstants.PacketSize];
        buffer[0] = PacketConstants.Magic;
        buffer[1] = PacketConstants.Version;
        BinaryPrimitives.WriteUInt16LittleEndian(buffer.AsSpan(2, 2), seq);
        BinaryPrimitives.WriteInt64LittleEndian(buffer.AsSpan(4, 8), ts);
        BinaryPrimitives.WriteSingleLittleEndian(buffer.AsSpan(12, 4), steering);
        BinaryPrimitives.WriteSingleLittleEndian(buffer.AsSpan(16, 4), ax);
        BinaryPrimitives.WriteSingleLittleEndian(buffer.AsSpan(20, 4), ay);
        BinaryPrimitives.WriteSingleLittleEndian(buffer.AsSpan(24, 4), az);
        BinaryPrimitives.WriteSingleLittleEndian(buffer.AsSpan(28, 4), gz);
        BinaryPrimitives.WriteUInt16LittleEndian(buffer.AsSpan(32, 2), buttons);
        buffer[34] = throttle;
        buffer[35] = brake;
        buffer[36] = clutch;
        buffer[37] = battery;
        buffer[38] = signal;
        BinaryPrimitives.WriteUInt16LittleEndian(buffer.AsSpan(39, 2), ping);

        ushort crc = PacketDeserializer.CalculateCrc16(buffer.AsSpan(0, 41));
        BinaryPrimitives.WriteUInt16LittleEndian(buffer.AsSpan(41, 2), crc);

        return buffer;
    }

    [Fact]
    public void TestDeserializationSuccess()
    {
        byte[] buffer = ConstructValidPacketBytes();
        bool success = PacketDeserializer.TryDeserialize(buffer, out var packet, out var error);

        Assert.True(success, error);
        Assert.Null(error);
        Assert.Equal(PacketConstants.Magic, packet.Magic);
        Assert.Equal(PacketConstants.Version, packet.Version);
        Assert.Equal((ushort)100, packet.SequenceNumber);
        Assert.Equal(1690000000000000L, packet.TimestampUs);
        Assert.Equal(-0.5f, packet.SteeringAngle, 4);
        Assert.Equal(0.1f, packet.AccelX, 4);
        Assert.Equal(9.8f, packet.AccelY, 4);
        Assert.Equal(0.0f, packet.AccelZ, 4);
        Assert.Equal(0.5f, packet.GyroZ, 4);
        Assert.Equal(ButtonMask.ThrottleBtn, packet.ButtonMask);
        Assert.Equal((byte)200, packet.Throttle);
        Assert.Equal((byte)0, packet.Brake);
        Assert.Equal((byte)50, packet.Clutch);
        Assert.Equal((byte)90, packet.BatteryPercentage);
        Assert.Equal((byte)100, packet.SignalQuality);
        Assert.Equal((ushort)15, packet.PingMs);
    }

    [Fact]
    public void TestDeserializationRejectsInvalidMagic()
    {
        byte[] buffer = ConstructValidPacketBytes();
        buffer[0] = 0xFF; // Corrupt magic

        bool success = PacketDeserializer.TryDeserialize(buffer, out _, out var error);
        Assert.False(success);
        Assert.NotNull(error);
        Assert.Contains("Invalid magic byte", error);
    }

    [Fact]
    public void TestDeserializationRejectsCrcMismatch()
    {
        byte[] buffer = ConstructValidPacketBytes();
        buffer[15] ^= 0xFF; // Corrupt body payload without updating CRC

        bool success = PacketDeserializer.TryDeserialize(buffer, out _, out var error);
        Assert.False(success);
        Assert.NotNull(error);
        Assert.Contains("CRC-16 checksum mismatch", error);
    }
}
