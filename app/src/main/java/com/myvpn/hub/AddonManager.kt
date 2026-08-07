package com.multivpn.app

import android.content.Context
import android.os.Handler
import android.os.Looper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AddonManager(
    private val okHttpClient: OkHttpClient = OkHttpClient()
) {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun pluginRootDirectory(context: Context): File {
        return File(context.filesDir, PLUGINS_DIRECTORY_NAME).apply { mkdirs() }
    }

    fun downloadAndInstall(
        context: Context,
        addon: AddonSpec,
        callback: (Result<InstalledAddon>) -> Unit
    ) {
        executor.execute {
            try {
                val addonsDir = pluginRootDirectory(context)
                val pluginFolder = File(addonsDir, sanitizeName(addon.name)).apply { mkdirs() }
                val targetFile = File(pluginFolder, addon.fileName)
                downloadFile(addon.url, targetFile)

                val installedBinary = if (addon.isArchive) {
                    extractArchive(targetFile, pluginFolder)
                } else {
                    targetFile
                }

                makeExecutable(installedBinary)
                val installed = InstalledAddon(
                    name = addon.name,
                    path = installedBinary.absolutePath,
                    sourceUrl = addon.url
                )
                mainHandler.post { callback(Result.success(installed)) }
            } catch (throwable: Throwable) {
                mainHandler.post { callback(Result.failure(throwable)) }
            }
        }
    }

    fun fetchAvailablePlugins(repositoryBaseUrl: String, callback: (Result<List<AddonSpec>>) -> Unit) {
        executor.execute {
            try {
                val manifestUrl = buildManifestUrl(repositoryBaseUrl)
                val request = Request.Builder().url(manifestUrl).build()
                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("Manifest request failed with code ${response.code}")
                    }
                    val body = response.body?.string() ?: throw IOException("Manifest body was empty")
                    val plugins = parseManifest(body, repositoryBaseUrl)
                    mainHandler.post { callback(Result.success(plugins)) }
                }
            } catch (throwable: Throwable) {
                mainHandler.post { callback(Result.failure(throwable)) }
            }
        }
    }

    fun shutdown() {
        executor.shutdownNow()
    }

    @Throws(IOException::class)
    private fun downloadFile(url: String, targetFile: File) {
        targetFile.parentFile?.mkdirs()
        val request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Download failed with code ${response.code}")
            }
            response.body?.byteStream()?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: throw IOException("Downloaded response body was empty")
        }
    }

    @Throws(IOException::class)
    private fun extractArchive(archiveFile: File, destinationDir: File): File {
        val process = ProcessBuilder("tar", "-xzf", archiveFile.absolutePath, "-C", destinationDir.absolutePath)
            .redirectErrorStream(true)
            .start()

        process.inputStream.bufferedReader().use { reader ->
            reader.readLines().forEach { _ -> }
        }

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw IOException("Archive extraction failed with exit code $exitCode")
        }

        return findBinary(destinationDir) ?: throw IOException("No runnable binary was found in archive")
    }

    @Throws(IOException::class)
    private fun makeExecutable(file: File) {
        val process = ProcessBuilder("chmod", "755", file.absolutePath)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw IOException("chmod failed: $output")
        }
    }

    private fun findBinary(directory: File): File? {
        val names = listOf("sing-box", "xray", "tor", "tor-browser")
        for (name in names) {
            val candidate = File(directory, name)
            if (candidate.exists() && candidate.isFile()) {
                return candidate
            }
        }

        return directory.listFiles()
            ?.filter { it.isFile && it.canExecute() }
            ?.firstOrNull()
    }

    private fun sanitizeName(name: String): String {
        return name.lowercase().replace(Regex("[^a-z0-9.-]+"), "-")
    }

    private fun buildManifestUrl(repositoryBaseUrl: String): String {
        val base = repositoryBaseUrl.trimEnd('/')
        return "$base/Plugins/manifest.json"
    }

    private fun parseManifest(body: String, repositoryBaseUrl: String): List<AddonSpec> {
        val json = JSONObject(body)
        val pluginsArray = json.optJSONArray("plugins") ?: JSONArray()
        val plugins = mutableListOf<AddonSpec>()
        for (index in 0 until pluginsArray.length()) {
            val pluginJson = pluginsArray.getJSONObject(index)
            val name = pluginJson.optString("name", "Plugin")
            val fileName = pluginJson.optString("fileName", pluginJson.optString("file", "plugin.bin"))
            val archive = pluginJson.optBoolean("archive", true)
            val relativePath = pluginJson.optString("path", pluginJson.optString("file", fileName))
            val downloadUrl = if (pluginJson.has("downloadUrl") && pluginJson.optString("downloadUrl").isNotBlank()) {
                pluginJson.optString("downloadUrl")
            } else {
                val normalizedBase = repositoryBaseUrl.trimEnd('/')
                val normalizedPath = relativePath.trimStart('/')
                "$normalizedBase/$normalizedPath"
            }
            plugins.add(AddonSpec(name, downloadUrl, fileName, archive))
        }
        return plugins
    }

    data class AddonSpec(
        val name: String,
        val url: String,
        val fileName: String,
        val isArchive: Boolean,
        val preferredBinaryName: String? = null
    )

    data class InstalledAddon(
        val name: String,
        val path: String,
        val sourceUrl: String
    )

    companion object {
        const val PLUGINS_DIRECTORY_NAME = "plugins"
    }
}
