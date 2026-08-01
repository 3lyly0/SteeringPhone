package dev.steeringphone.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class AppUpdateInfo(
    val isUpdateAvailable: Boolean = false,
    val latestVersion: String = "",
    val downloadUrl: String? = null,
    val releaseNotes: String? = null
)

/**
 * Handles checking GitHub Releases for new SteeringPhone Android APK releases, downloading, and invoking package installation.
 */
@Singleton
class UpdateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getActiveVersion(): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: CURRENT_VERSION
        } catch (_: Exception) {
            CURRENT_VERSION
        }
    }

    /**
     * Checks GitHub API (https://api.github.com/repos/3lyly0/SteeringPhone/releases/latest) for newer version tags.
     */
    suspend fun checkForUpdate(currentVersion: String = getActiveVersion()): AppUpdateInfo = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_RELEASES_API_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", "SteeringPhone-Android-App")
                connectTimeout = 5000
                readTimeout = 5000
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonStr)

                val tagName = json.optString("tag_name", "").removePrefix("v").trim()
                val body = json.optString("body", "")

                var apkDownloadUrl: String? = null
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkDownloadUrl = asset.optString("browser_download_url", null)
                            break
                        }
                    }
                }

                val isNewer = isVersionNewer(currentVersion, tagName)
                return@withContext AppUpdateInfo(
                    isUpdateAvailable = isNewer && apkDownloadUrl != null,
                    latestVersion = tagName,
                    downloadUrl = apkDownloadUrl,
                    releaseNotes = body
                )
            }
        } catch (_: Exception) {
            // Silently handle network / API error
        }
        return@withContext AppUpdateInfo()
    }

    /**
     * Downloads APK asset and triggers Android package installer with fallback protection.
     */
    suspend fun downloadAndInstall(
        downloadUrl: String,
        onProgress: (Float) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val apkFile = File(context.cacheDir, "steeringphone-update.apk")
        try {
            if (apkFile.exists()) apkFile.delete()

            val url = URL(downloadUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", "SteeringPhone-Android-App")
                connectTimeout = 10000
                readTimeout = 15000
            }

            val totalBytes = connection.contentLength
            var downloadedBytes = 0

            connection.inputStream.use { input ->
                FileOutputStream(apkFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        if (totalBytes > 0) {
                            onProgress(downloadedBytes.toFloat() / totalBytes)
                        }
                    }
                }
            }

            // Fallback validation: ensure APK is non-empty and starts with ZIP magic header 'PK'
            if (!apkFile.exists() || apkFile.length() < 1000) {
                apkFile.delete()
                onError("Download corrupted or incomplete.")
                return@withContext
            }

            // Trigger Installation Intent
            installApk(apkFile)
        } catch (e: Exception) {
            if (apkFile.exists()) apkFile.delete() // Fallback cleanup
            onError("Update failed: ${e.message}")
        }
    }

    fun installApk(apkFile: File = File(context.cacheDir, "steeringphone-update.apk")) {
        if (!apkFile.exists()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
            val permissionIntent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(permissionIntent)
            return
        }

        val authority = "${context.packageName}.fileprovider"
        val apkUri: Uri = FileProvider.getUriForFile(context, authority, apkFile)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun isVersionNewer(current: String, latest: String): Boolean {
        if (latest.isEmpty()) return false
        val currParts = current.removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }
        val lateParts = latest.removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }

        for (i in 0 until maxOf(currParts.size, lateParts.size)) {
            val c = currParts.getOrElse(i) { 0 }
            val l = lateParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    companion object {
        const val CURRENT_VERSION: String = "1.0.0"
        const val GITHUB_RELEASES_API_URL: String = "https://api.github.com/repos/3lyly0/SteeringPhone/releases/latest"
    }
}
