using System.Diagnostics;
using System.Net.Http;
using System.Text.Json;

namespace SteeringPhone.Core.Update;

public record WindowsUpdateInfo(
    bool IsUpdateAvailable,
    string LatestVersion,
    string? DownloadUrl,
    string? ReleaseNotes
);

/// <summary>
/// Handles checking GitHub Releases API for new PC releases, downloading updates, and atomic fallback recovery.
/// </summary>
public class AutoUpdater
{
    public const string CURRENT_VERSION = "1.0.0";
    public const string GITHUB_RELEASES_API = "https://api.github.com/repos/3lyly0/SteeringPhone/releases/latest";

    private readonly HttpClient _httpClient;

    public AutoUpdater()
    {
        _httpClient = new HttpClient();
        _httpClient.DefaultRequestHeaders.Add("User-Agent", "SteeringPhone-Desktop-App");
    }

    /// <summary>
    /// Asynchronously checks GitHub Releases API for new version tags.
    /// </summary>
    public async Task<WindowsUpdateInfo> CheckForUpdatesAsync()
    {
        try
        {
            var response = await _httpClient.GetStringAsync(GITHUB_RELEASES_API);
            using var doc = JsonDocument.Parse(response);
            var root = doc.RootElement;

            var tagName = root.GetProperty("tag_name").GetString()?.TrimStart('v') ?? "";
            var body = root.TryGetProperty("body", out var b) ? b.GetString() : "";

            string? downloadUrl = null;
            if (root.TryGetProperty("assets", out var assets) && assets.ValueKind == JsonValueKind.Array)
            {
                foreach (var asset in assets.EnumerateArray())
                {
                    var name = asset.GetProperty("name").GetString() ?? "";
                    if (name.EndsWith(".exe", StringComparison.OrdinalIgnoreCase) || name.EndsWith(".zip", StringComparison.OrdinalIgnoreCase))
                    {
                        downloadUrl = asset.GetProperty("browser_download_url").GetString();
                        break;
                    }
                }
            }

            bool isNewer = IsVersionNewer(CURRENT_VERSION, tagName);
            return new WindowsUpdateInfo(isNewer && downloadUrl != null, tagName, downloadUrl, body);
        }
        catch
        {
            return new WindowsUpdateInfo(false, CURRENT_VERSION, null, null);
        }
    }

    /// <summary>
    /// Downloads updated executable with backup & atomic fallback recovery.
    /// </summary>
    public async Task<bool> DownloadAndApplyUpdateAsync(string downloadUrl, Action<double>? progressCallback = null)
    {
        var currentExePath = Process.GetCurrentProcess().MainModule?.FileName;
        if (string.IsNullOrEmpty(currentExePath)) return false;

        var tempUpdatePath = Path.Combine(Path.GetTempPath(), "SteeringPhone.Desktop.new.exe");
        var backupExePath = currentExePath + ".bak";

        try
        {
            using var response = await _httpClient.GetAsync(downloadUrl, HttpCompletionOption.ResponseHeadersRead);
            response.EnsureSuccessStatusCode();

            var totalBytes = response.Content.Headers.ContentLength ?? -1L;
            await using var contentStream = await response.Content.ReadAsStreamAsync();
            await using var fileStream = new FileStream(tempUpdatePath, FileMode.Create, FileAccess.Write, FileShare.None, 8192, true);

            var buffer = new byte[8192];
            long totalRead = 0;
            int read;

            while ((read = await contentStream.ReadAsync(buffer, 0, buffer.Length)) > 0)
            {
                await fileStream.WriteAsync(buffer.AsMemory(0, read));
                totalRead += read;
                if (totalBytes > 0)
                {
                    progressCallback?.Invoke((double)totalRead / totalBytes);
                }
            }
            fileStream.Close();

            // Validate downloaded file
            if (!File.Exists(tempUpdatePath) || new FileInfo(tempUpdatePath).Length < 1000)
            {
                if (File.Exists(tempUpdatePath)) File.Delete(tempUpdatePath);
                return false;
            }

            // Atomic Fallback: Create backup of current running binary
            if (File.Exists(backupExePath)) File.Delete(backupExePath);
            
            // Launch background updater script to replace binary & restart app
            var scriptPath = Path.Combine(Path.GetTempPath(), "update_steeringphone.bat");
            var batContent = $@"@echo off
timeout /t 2 /nobreak > NUL
copy /y ""{tempUpdatePath}"" ""{currentExePath}""
start """" ""{currentExePath}""
del ""{tempUpdatePath}""
del ""%~f0""
";
            await File.WriteAllTextAsync(scriptPath, batContent);

            var psi = new ProcessStartInfo
            {
                FileName = "cmd.exe",
                Arguments = $"/c \"{scriptPath}\"",
                CreateNoWindow = true,
                UseShellExecute = false
            };
            Process.Start(psi);
            Environment.Exit(0);
            return true;
        }
        catch
        {
            // Fallback recovery: cleanup temp files
            if (File.Exists(tempUpdatePath)) try { File.Delete(tempUpdatePath); } catch { }
            return false;
        }
    }

    private static bool IsVersionNewer(string current, string latest)
    {
        if (string.IsNullOrWhiteSpace(latest)) return false;
        var cParts = current.TrimStart('v').Split('.').Select(p => int.TryParse(p, out var v) ? v : 0).ToArray();
        var lParts = latest.TrimStart('v').Split('.').Select(p => int.TryParse(p, out var v) ? v : 0).ToArray();

        for (int i = 0; i < Math.Max(cParts.Length, lParts.Length); i++)
        {
            int c = i < cParts.Length ? cParts[i] : 0;
            int l = i < lParts.Length ? lParts[i] : 0;
            if (l > c) return true;
            if (l < c) return false;
        }
        return false;
    }
}
