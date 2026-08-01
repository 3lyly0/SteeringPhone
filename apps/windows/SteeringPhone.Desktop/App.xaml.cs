using Microsoft.UI.Xaml;
using Serilog;

namespace SteeringPhone.Desktop
{
    public partial class App : Application
    {
        private Window? m_window;

        public App()
        {
            this.InitializeComponent();
            this.UnhandledException += (sender, e) =>
            {
                Log.Error(e.Exception, "Unhandled application exception: {Message}", e.Message);
                e.Handled = true;
            };

            Log.Logger = new LoggerConfiguration()
                .WriteTo.Console()
                .WriteTo.File("steeringphone_desktop.log", rollingInterval: RollingInterval.Day)
                .CreateLogger();

            Log.Information("SteeringPhone Desktop Initializing...");
        }

        protected override void OnLaunched(Microsoft.UI.Xaml.LaunchActivatedEventArgs args)
        {
            m_window = new MainWindow();
            m_window.Activate();
        }
    }
}
