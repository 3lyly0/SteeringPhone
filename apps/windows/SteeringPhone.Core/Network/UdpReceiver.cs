using System.Net;
using System.Net.Sockets;
using SteeringPhone.Core.Protocol;

namespace SteeringPhone.Core.Network;

/// <summary>
/// High-throughput non-blocking UDP receiver for incoming 43-byte DrivePackets.
/// </summary>
public class UdpReceiver
{
    private UdpClient? _udpClient;
    private CancellationTokenSource? _cts;
    public event Action<DrivePacket>? OnPacketReceived;
    public bool IsListening => _udpClient != null;

    public void Start(int port = PacketConstants.UdpDataPort)
    {
        try
        {
            _udpClient = new UdpClient();
            _udpClient.Client.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.ReuseAddress, true);
            _udpClient.Client.Bind(new IPEndPoint(IPAddress.Any, port));
            _cts = new CancellationTokenSource();

            Task.Run(() => ReceiveLoopAsync(_cts.Token));
        }
        catch
        {
            // Port might be temporarily busy
        }
    }

    private async Task ReceiveLoopAsync(CancellationToken ct)
    {
        while (!ct.IsCancellationRequested && _udpClient != null)
        {
            try
            {
                var result = await _udpClient.ReceiveAsync(ct);
                if (result.Buffer.Length == PacketConstants.PacketSize)
                {
                    if (PacketDeserializer.TryDeserialize(result.Buffer, out var packet, out _))
                    {
                        OnPacketReceived?.Invoke(packet);
                    }
                }
            }
            catch (OperationCanceledException)
            {
                break;
            }
            catch
            {
                // Continue loop on socket error
            }
        }
    }

    public void Stop()
    {
        _cts?.Cancel();
        _udpClient?.Close();
        _udpClient = null;
    }
}
