using Microsoft.Win32;
using System.Diagnostics;
using System.Net.Http;
using System.Runtime.Versioning;

namespace SteeringPhone.Core.Input;

/// <summary>
/// Detects ViGEmBus kernel driver presence and performs silent auto-installation if missing.
/// </summary>
[SupportedOSPlatform("windows")]
public static class DriverInstaller
{
    public const string VIGEMBUS_DOWNLOAD_URL = "https://github.com/nefarius/ViGEmBus/releases/download/v1.22.0/ViGEmBus_1.22.0_x64_x86_arm64.exe";

    /// <summary>
    /// Checks if the ViGEmBus Windows service is registered in System Registry.
    /// </summary>
    public static bool IsDriverInstalled()
    {
        try
        {
            using var key = Registry.LocalMachine.OpenSubKey(@"SYSTEM\CurrentControlSet\Services\ViGEmBus");
            return key != null;
        }
        catch
        {
            return false;
        }
    }

    /// <summary>
    /// Downloads signed ViGEmBus installer and executes silent setup (/q).
    /// </summary>
    public static async Task<bool> InstallDriverSilentlyAsync()
    {
        if (IsDriverInstalled()) return true;

        var tempInstallerPath = Path.Combine(Path.GetTempPath(), "ViGEmBus_Setup.exe");
        try
        {
            using var client = new HttpClient();
            client.DefaultRequestHeaders.Add("User-Agent", "SteeringPhone-DriverInstaller");

            var bytes = await client.GetByteArrayAsync(VIGEMBUS_DOWNLOAD_URL);
            await File.WriteAllBytesAsync(tempInstallerPath, bytes);

            if (!File.Exists(tempInstallerPath) || new FileInfo(tempInstallerPath).Length < 1000)
            {
                return false;
            }

            var psi = new ProcessStartInfo
            {
                FileName = tempInstallerPath,
                Arguments = "/q /norestart",
                UseShellExecute = true,
                Verb = "runas",
                CreateNoWindow = true
            };

            var process = Process.Start(psi);
            if (process != null)
            {
                await process.WaitForExitAsync();
                return IsDriverInstalled();
            }
        }
        catch
        {
            // Error executing driver setup
        }
        finally
        {
            if (File.Exists(tempInstallerPath))
            {
                try
                {
                    File.Delete(tempInstallerPath);
                }
                catch
                {
                }
            }
        }

        return IsDriverInstalled();
    }
}
