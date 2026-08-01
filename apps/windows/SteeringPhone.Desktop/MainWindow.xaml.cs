using Microsoft.UI.Dispatching;
using Microsoft.UI.Xaml;
using SteeringPhone.Core.Input;
using SteeringPhone.Core.Network;
using SteeringPhone.Core.Protocol;
using SteeringPhone.Core.Update;
using System.Diagnostics;

namespace SteeringPhone.Desktop
{
    public sealed partial class MainWindow : Window
    {
        private readonly UdpReceiver _udpReceiver;
        private readonly DiscoveryService _discoveryService;
        private readonly AutoUpdater _autoUpdater;
        private readonly ViGEmControllerService _viGEmService;

        private WindowsUpdateInfo? _currentUpdateInfo;
        private long _packetCount = 0;

        public MainWindow()
        {
            this.InitializeComponent();

            _udpReceiver = new UdpReceiver();
            _discoveryService = new DiscoveryService();
            _autoUpdater = new AutoUpdater();
            _viGEmService = new ViGEmControllerService();

            // Initialize kernel virtual Xbox 360 controller
            bool vigemOk = _viGEmService.Initialize();

            _udpReceiver.OnPacketReceived += OnDrivePacketReceived;

            // Start UDP telemetry receiver and LAN auto-discovery service
            _udpReceiver.Start(PacketConstants.UdpDataPort);
            _discoveryService.Start();

            // Asynchronously check for GitHub updates
            CheckForGitHubUpdatesAsync();

            this.Closed += OnWindowClosed;
        }

        private async void CheckForGitHubUpdatesAsync()
        {
            _currentUpdateInfo = await _autoUpdater.CheckForUpdatesAsync();
            if (_currentUpdateInfo != null && _currentUpdateInfo.IsUpdateAvailable)
            {
                DispatcherQueue.TryEnqueue(DispatcherQueuePriority.Normal, () =>
                {
                    UpdateBannerText.Text = $"New Update v{_currentUpdateInfo.LatestVersion} Available!";
                    UpdateBanner.Visibility = Visibility.Visible;
                });
            }
        }

        private async void OnUpdateClick(object sender, RoutedEventArgs e)
        {
            if (_currentUpdateInfo?.DownloadUrl != null)
            {
                UpdateButton.IsEnabled = false;
                UpdateButton.Content = "Downloading...";
                bool success = await _autoUpdater.DownloadAndApplyUpdateAsync(_currentUpdateInfo.DownloadUrl, progress =>
                {
                    DispatcherQueue.TryEnqueue(() =>
                    {
                        UpdateButton.Content = $"{(progress * 100):F0}%";
                    });
                });

                if (!success)
                {
                    DispatcherQueue.TryEnqueue(() =>
                    {
                        UpdateButton.IsEnabled = true;
                        UpdateButton.Content = "Update Failed (Retry)";
                    });
                }
            }
        }

        private void OnDrivePacketReceived(DrivePacket packet)
        {
            _packetCount++;

            // Inject input telemetry into kernel virtual Xbox 360 controller
            _viGEmService.UpdateInput(packet);

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
            try
            {
                _udpReceiver.OnPacketReceived -= OnDrivePacketReceived;
            }
            catch
            {
            }

            try
            {
                _udpReceiver.Stop();
            }
            catch
            {
            }

            try
            {
                _discoveryService.Stop();
            }
            catch
            {
            }

            try
            {
                _viGEmService.Disconnect();
            }
            catch
            {
            }
        }
    }
}
