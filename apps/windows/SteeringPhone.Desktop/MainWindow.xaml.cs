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

            _udpReceiver.OnPacketReceived += OnDrivePacketReceived;

            // Start UDP telemetry receiver and LAN auto-discovery service immediately
            _udpReceiver.Start(PacketConstants.UdpDataPort);
            _discoveryService.Start();

            // Non-blocking asynchronous driver initialization & UI status check
            InitializeViGEmDriverAsync();

            // Asynchronously check for GitHub updates
            CheckForGitHubUpdatesAsync();

            this.Closed += OnWindowClosed;
        }

        private async void InitializeViGEmDriverAsync()
        {
            UpdateDriverStatusUI("Checking ViGEmBus Driver...", isLoading: true);

            bool isInstalled = DriverInstaller.IsDriverInstalled();
            if (isInstalled)
            {
                bool connected = await _viGEmService.InitializeAsync();
                if (connected)
                {
                    UpdateDriverStatusUI("Status: Driver Active & Xbox 360 Controller Ready", isOk: true);
                }
                else
                {
                    UpdateDriverStatusUI($"Status: Driver Error ({_viGEmService.ErrorMessage})", isError: true, showInstallBtn: true);
                }
            }
            else
            {
                UpdateDriverStatusUI("Status: Driver Missing — Installation Required", isWarning: true, showInstallBtn: true);
                // Attempt automatic silent installation in background
                InstallDriverInBackgroundAsync();
            }
        }

        private async void OnInstallDriverClick(object sender, RoutedEventArgs e)
        {
            await InstallDriverInBackgroundAsync();
        }

        private async Task InstallDriverInBackgroundAsync()
        {
            DispatcherQueue.TryEnqueue(() =>
            {
                UpdateDriverStatusUI("Installing ViGEmBus Driver (Please approve UAC if prompted)...", isLoading: true);
            });

            bool success = await DriverInstaller.InstallDriverSilentlyAsync();
            if (success)
            {
                bool connected = await _viGEmService.InitializeAsync();
                DispatcherQueue.TryEnqueue(() =>
                {
                    if (connected)
                    {
                        UpdateDriverStatusUI("Status: Driver Installed & Controller Ready!", isOk: true);
                    }
                    else
                    {
                        UpdateDriverStatusUI("Status: Driver installed, initializing controller...", isOk: true);
                    }
                });
            }
            else
            {
                DispatcherQueue.TryEnqueue(() =>
                {
                    UpdateDriverStatusUI("Status: Driver Installation Failed (Click to Retry)", isError: true, showInstallBtn: true);
                });
            }
        }

        private void UpdateDriverStatusUI(string message, bool isLoading = false, bool isOk = false, bool isWarning = false, bool isError = false, bool showInstallBtn = false)
        {
            DispatcherQueue.TryEnqueue(() =>
            {
                DriverStatusText.Text = message;
                DriverProgressBar.Visibility = isLoading ? Visibility.Visible : Visibility.Collapsed;
                DriverInstallBtn.Visibility = showInstallBtn ? Visibility.Visible : Visibility.Collapsed;

                if (isOk)
                {
                    DriverStatusText.Foreground = new Microsoft.UI.Xaml.Media.SolidColorBrush(Windows.UI.Color.FromArgb(255, 0, 230, 118));
                }
                else if (isWarning)
                {
                    DriverStatusText.Foreground = new Microsoft.UI.Xaml.Media.SolidColorBrush(Windows.UI.Color.FromArgb(255, 0, 229, 255));
                }
                else if (isError)
                {
                    DriverStatusText.Foreground = new Microsoft.UI.Xaml.Media.SolidColorBrush(Windows.UI.Color.FromArgb(255, 229, 57, 53));
                }
            });
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
                double rotationDegrees = packet.SteeringAngle * 90.0;
                WheelTransform.Angle = rotationDegrees;

                AngleText.Text = $"{rotationDegrees:F1}°";
                OutputText.Text = $"Steering Output: {packet.SteeringAngle:F2}";

                ThrottleBar.Value = packet.Throttle;
                ThrottlePercentText.Text = $"{(packet.Throttle / 255.0 * 100):F0}%";

                BrakeBar.Value = packet.Brake;
                BrakePercentText.Text = $"{(packet.Brake / 255.0 * 100):F0}%";

                PacketCountText.Text = _packetCount.ToString("N0");
                BatteryText.Text = $"{packet.BatteryPercentage}%";
                PingText.Text = $"{packet.PingMs} ms";

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
