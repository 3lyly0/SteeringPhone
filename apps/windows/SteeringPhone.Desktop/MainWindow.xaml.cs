using Microsoft.UI.Dispatching;
using Microsoft.UI.Xaml;
using SteeringPhone.Core.Network;
using SteeringPhone.Core.Protocol;
using System.Diagnostics;

namespace SteeringPhone.Desktop
{
    public sealed partial class MainWindow : Window
    {
        private readonly UdpReceiver _udpReceiver;
        private readonly DiscoveryService _discoveryService;
        private long _packetCount = 0;

        public MainWindow()
        {
            this.InitializeComponent();

            _udpReceiver = new UdpReceiver();
            _discoveryService = new DiscoveryService();

            _udpReceiver.OnPacketReceived += OnDrivePacketReceived;

            // Start UDP telemetry receiver and LAN auto-discovery service
            _udpReceiver.Start(PacketConstants.UdpDataPort);
            _discoveryService.Start();

            this.Closed += OnWindowClosed;
        }

        private void OnDrivePacketReceived(DrivePacket packet)
        {
            _packetCount++;

            // Dispatch UI updates to WinUI 3 UI Thread
            DispatcherQueue.TryEnqueue(DispatcherQueuePriority.High, () =>
            {
                // Update Steering Wheel Visual Angle (e.g. angle in degrees = SteeringAngle * 90)
                double rotationDegrees = packet.SteeringAngle * 90.0;
                WheelTransform.Angle = rotationDegrees;

                // Update Telemetry Controls
                AngleText.Text = $"{rotationDegrees:F1}°";
                OutputText.Text = $"Steering Output: {packet.SteeringAngle:F2}";

                ThrottleBar.Value = packet.Throttle;
                ThrottlePercentText.Text = $"{(packet.Throttle / 255.0 * 100):F0}%";

                BrakeBar.Value = packet.Brake;
                BrakePercentText.Text = $"{(packet.Brake / 255.0 * 100):F0}%";

                PacketCountText.Text = _packetCount.ToString("N0");
                BatteryText.Text = $"{packet.BatteryPercentage}%";
                PingText.Text = $"{packet.PingMs} ms";

                // Update Connection Status Badge
                StatusText.Text = "Connected to Phone";
                StatusBadgeBorder.Background = new Microsoft.UI.Xaml.Media.SolidColorBrush(Windows.UI.Color.FromArgb(255, 24, 27, 34));
            });
        }

        private void OnWindowClosed(object sender, WindowEventArgs args)
        {
            _udpReceiver.Stop();
            _discoveryService.Stop();
        }
    }
}
