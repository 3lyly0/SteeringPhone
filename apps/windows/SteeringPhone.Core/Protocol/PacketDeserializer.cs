using System.Buffers.Binary;

namespace SteeringPhone.Core.Protocol;

public static class PacketDeserializer
{
    public static ushort CalculateCrc16(ReadOnlySpan<byte> data)
    {
        ushort crc = 0xFFFF;
        foreach (var b in data)
        {
            crc ^= (ushort)(b << 8);
            for (int i = 0; i < 8; i++)
            {
                crc = (crc & 0x8000) != 0
                    ? (ushort)((crc << 1) ^ 0x1021)
                    : (ushort)(crc << 1);
            }
        }
        return crc;
    }

    public static bool TryDeserialize(ReadOnlySpan<byte> buffer, out DrivePacket packet, out string? errorMessage)
    {
        packet = default;
        errorMessage = null;

        if (buffer.Length < PacketConstants.PacketSize)
        {
            errorMessage = $"Buffer size underflow: Expected at least {PacketConstants.PacketSize} bytes, got {buffer.Length}";
            return false;
        }

        byte magic = buffer[0];
        if (magic != PacketConstants.Magic)
        {
            errorMessage = $"Invalid magic byte: 0x{magic:X2}, expected 0x{PacketConstants.Magic:X2}";
            return false;
        }

        byte version = buffer[1];
        if (version != PacketConstants.Version)
        {
            errorMessage = $"Unsupported protocol version: {version}, expected {PacketConstants.Version}";
            return false;
        }

        ushort receivedCrc = BinaryPrimitives.ReadUInt16LittleEndian(buffer.Slice(41, 2));
        ushort expectedCrc = CalculateCrc16(buffer.Slice(0, 41));

        if (receivedCrc != expectedCrc)
        {
            errorMessage = $"CRC-16 checksum mismatch: Received 0x{receivedCrc:X4}, calculated 0x{expectedCrc:X4}";
            return false;
        }

        ushort sequenceNumber = BinaryPrimitives.ReadUInt16LittleEndian(buffer.Slice(2, 2));
        long timestampUs = BinaryPrimitives.ReadInt64LittleEndian(buffer.Slice(4, 8));
        float steeringAngle = BinaryPrimitives.ReadSingleLittleEndian(buffer.Slice(12, 4));
        float accelX = BinaryPrimitives.ReadSingleLittleEndian(buffer.Slice(16, 4));
        float accelY = BinaryPrimitives.ReadSingleLittleEndian(buffer.Slice(20, 4));
        float accelZ = BinaryPrimitives.ReadSingleLittleEndian(buffer.Slice(24, 4));
        float gyroZ = BinaryPrimitives.ReadSingleLittleEndian(buffer.Slice(28, 4));
        ushort rawButtonMask = BinaryPrimitives.ReadUInt16LittleEndian(buffer.Slice(32, 2));
        byte throttle = buffer[34];
        byte brake = buffer[35];
        byte clutch = buffer[36];
        byte batteryPercentage = buffer[37];
        byte signalQuality = buffer[38];
        ushort pingMs = BinaryPrimitives.ReadUInt16LittleEndian(buffer.Slice(39, 2));

        packet = new DrivePacket(
            magic,
            version,
            sequenceNumber,
            timestampUs,
            steeringAngle,
            accelX,
            accelY,
            accelZ,
            gyroZ,
            (ButtonMask)rawButtonMask,
            throttle,
            brake,
            clutch,
            batteryPercentage,
            signalQuality,
            pingMs,
            receivedCrc
        );

        return true;
    }
}
