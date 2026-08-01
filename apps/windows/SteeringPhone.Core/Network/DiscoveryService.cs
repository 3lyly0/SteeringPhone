using System.Net;
using System.Net.Sockets;
using System.Text;
using SteeringPhone.Core.Protocol;

namespace SteeringPhone.Core.Network;

/// <summary>
/// Responds to Android UDP broadcast pings on port 45678 to enable zero-config auto-discovery.
/// </summary>
public class DiscoveryService
{
    private UdpClient? _listener;
    private CancellationTokenSource? _cts;
    public const int DISCOVERY_PORT = 45678;

    public void Start()
    {
        if (_listener != null) return;

        try
        {
            _listener = new UdpClient();
            _listener.Client.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.ReuseAddress, true);
            _listener.Client.Bind(new IPEndPoint(IPAddress.Any, DISCOVERY_PORT));
            _cts = new CancellationTokenSource();

            Task.Run(() => ListenLoopAsync(_cts.Token));
        }
        catch
        {
            // Port might be in use
        }
    }

    private async Task ListenLoopAsync(CancellationToken ct)
    {
        while (!ct.IsCancellationRequested && _listener != null)
        {
            try
            {
                var result = await _listener.ReceiveAsync(ct);
                var message = Encoding.UTF8.GetString(result.Buffer);

                if (message.Trim() == "STEERINGPHONE_DISCOVER")
                {
                    var hostname = Dns.GetHostName();
                    var responseText = $"STEERINGPHONE_PC:{hostname}:{PacketConstants.UdpDataPort}:UDP";
                    var responseBytes = Encoding.UTF8.GetBytes(responseText);

                    await _listener.SendAsync(responseBytes, responseBytes.Length, result.RemoteEndPoint);
                }
            }
            catch (OperationCanceledException)
            {
                break;
            }
            catch
            {
                // Ignore timeout/socket error
            }
        }
    }

    public void Stop()
    {
        _cts?.Cancel();
        _listener?.Close();
        _listener = null;
    }
}
