namespace SteeringPhone.Core.Protocol;

public static class PacketConstants
{
    public const byte Magic = 0xD5;
    public const byte Version = 0x01;
    public const int PacketSize = 43;
    public const int DiscoveryPort = 45678;
    public const int WebSocketPort = 45679;
    public const int UdpDataPort = 45680;
    public const int DefaultUdpPort = UdpDataPort;
    public const int DEFAULT_UDP_PORT = UdpDataPort;
    public const long HeartbeatIntervalMs = 1000;
    public const long ConnectionTimeoutMs = 3000;
}
