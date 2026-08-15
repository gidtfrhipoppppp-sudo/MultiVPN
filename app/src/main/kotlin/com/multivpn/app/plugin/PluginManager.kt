package com.multivpn.app.plugin

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream

/**
 * Downloads, extracts and tracks VPN core binaries.
 *
 * Each core lives under {@code filesDir/cores/<id>/<binaryName>} once installed.
 * Install state is persisted as a small JSON sidecar per core so the list of
 * installed cores survives process restarts.
 */
class PluginManager(
    private val storageDir: File,
    private val okHttpClient: OkHttpClient = OkHttpClient()
) {

    private val states = ConcurrentHashMap<String, CoreState>()
    private val binaryPaths = ConcurrentHashMap<String, String>()

    init {
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        loadPersistedCores()
    }

    /** Directory that holds a single core's binary. */
    private fun coreDir(id: String): File = File(storageDir, id.lowercase())

    /** Path to the extracted binary for a core, or null if not installed. */
    fun binaryPath(id: String): String? = binaryPaths[id.lowercase()]

    /** True if the binary file exists on disk. */
    fun isInstalled(id: String): Boolean =
        binaryPath(id)?.let { File(it).exists() } == true

    /** Runtime state used to render the UI badge. */
    fun state(id: String): CoreState = states[id.lowercase()] ?: CoreState.NOT_INSTALLED

    /**
     * Download the core archive from its downloadUrl, extract the binary,
     * make it executable and persist the install state.
     *
     * @param core the core to install
     * @param onProgress invoked on the main thread with a status line
     * @param onComplete invoked on the main thread with the binary path on success
     *                  or an error message on failure
     */
    fun downloadAndInstall(
        context: Context,
        core: PluginCore,
        onProgress: (String) -> Unit,
        onComplete: (Result<String>) -> Unit
    ) {
        val id = core.id.lowercase()
        states[id] = CoreState.DOWNLOADING
        onProgress("Downloading ${core.displayName}...")

        val client = okHttpClient
        val request = Request.Builder().url(core.downloadUrl).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Download failed: HTTP ${response.code}")
            }
            val body = response.body ?: throw IOException("Empty response body")
            val dir = coreDir(id).apply { mkdirs() }
            val archive = File(dir, core.fileName)
            onProgress("Saving ${core.fileName}...")

            body.byteStream().use { input ->
                FileOutputStream(archive).use { output ->
                    input.copyTo(output)
                }
            }

            states[id] = CoreState.INSTALLING
            onProgress("Extracting ${core.binaryName}...")

            val binary = extractBinary(core, archive, dir)
            makeExecutable(binary)
            binaryPaths[id] = binary.absolutePath
            states[id] = CoreState.INSTALLED
            persistCore(id, binary.absolutePath)

            onProgress("${core.displayName} installed at ${binary.absolutePath}")
            onComplete(Result.success(binary.absolutePath))
        }
    }

    /** Remove a core's binary and persisted state. */
    fun uninstall(id: String): Boolean {
        val key = id.lowercase()
        val dir = coreDir(key)
        if (dir.exists()) {
            dir.deleteRecursively()
        }
        states.remove(key)
        binaryPaths.remove(key)
        File(storageDir, "$key.json").delete()
        return true
    }

    private fun extractBinary(core: PluginCore, archive: File, dir: File): File {
        return when (core.archiveFormat) {
            ArchiveFormat.TAR_GZ -> extractTarGz(archive, dir, core.binaryName)
            ArchiveFormat.ZIP -> extractZip(archive, dir, core.binaryName)
            ArchiveFormat.GZ -> extractGz(archive, dir, core.binaryName)
        }
    }

    private fun extractTarGz(archive: File, dir: File, binaryName: String): File {
        // Use the system tar for cross-platform archives that may include
        // symlinks/permissions tar can't reproduce through the JDK API.
        val process = ProcessBuilder("tar", "-xzf", archive.absolutePath, "-C", dir.absolutePath)
            .redirectErrorStream(true)
            .start()
        process.inputStream.bufferedReader().use { it.readText() }
        val exit = process.waitFor()
        if (exit != 0) {
            throw IOException("tar extraction failed with exit code $exit")
        }
        return findBinary(dir, binaryName)
    }

    private fun extractZip(archive: File, dir: File, binaryName: String): File {
        ZipInputStream(archive.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val target = File(dir, File(entry.name).name)
                if (!entry.isDirectory) {
                    FileOutputStream(target).use { out -> zip.copyTo(out) }
                    if (target.name == binaryName) {
                        return target
                    }
                }
                entry = zip.nextEntry
            }
        }
        return findBinary(dir, binaryName)
    }

    private fun extractGz(archive: File, dir: File, binaryName: String): File {
        val target = File(dir, binaryName)
        GZIPInputStream(archive.inputStream()).use { input ->
            FileOutputStream(target).use { output -> input.copyTo(output) }
        }
        return target
    }

    private fun findBinary(dir: File, binaryName: String): File {
        val direct = File(dir, binaryName)
        if (direct.exists()) return direct
        return dir.walkTopDown()
            .firstOrNull { it.isFile && it.name == binaryName }
            ?: throw IOException("Binary '$binaryName' not found in archive")
    }

    private fun makeExecutable(file: File) {
        val process = ProcessBuilder("chmod", "755", file.absolutePath)
            .redirectErrorStream(true)
            .start()
        process.inputStream.bufferedReader().use { it.readText() }
        val exit = process.waitFor()
        if (exit != 0) {
            Timber.w("chmod 755 failed for ${file.absolutePath} (exit $exit)")
        }
    }

    private fun loadPersistedCores() {
        storageDir.listFiles { f -> f.isFile && f.extension == "json" }
            ?.forEach { file ->
                val id = file.nameWithoutExtension.lowercase()
                try {
                    val json = JSONObject(file.readText())
                    val path = json.optString("binaryPath", "")
                    if (path.isNotEmpty() && File(path).exists()) {
                        states[id] = CoreState.INSTALLED
                        binaryPaths[id] = path
                    } else {
                        file.delete()
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Failed to read persisted state for $id")
                }
            }
    }

    private fun persistCore(id: String, binaryPath: String) {
        val manifest = JSONObject()
            .put("id", id)
            .put("binaryPath", binaryPath)
            .put("installed", true)
        File(storageDir, "$id.json").writeText(manifest.toString())
    }
}
