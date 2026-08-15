package com.multivpn.app.plugin

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Catalogue of downloadable VPN cores.
 *
 * The catalogue is fetched from a remote {@code manifest.json} describing each
 * core's download URL, archive format and binary name. Installation is
 * delegated to {@link PluginManager}.
 */
class PluginRepository(
    private val pluginManager: PluginManager,
    private val okHttpClient: OkHttpClient = OkHttpClient(),
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
) {

    private var cachedCores: List<PluginCore> = emptyList()

    /**
     * Fetch the core catalogue from the given manifest URL.
     *
     * @param manifestUrl raw URL pointing to a JSON manifest
     * @param onResult invoked on the calling (main) thread
     */
    fun fetchCatalog(manifestUrl: String, onResult: (Result<List<PluginCore>>) -> Unit) {
        executor.execute {
            try {
                val request = Request.Builder().url(manifestUrl).build()
                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("Manifest request failed: HTTP ${response.code}")
                    }
                    val body = response.body?.string()
                        ?: throw IOException("Manifest body was empty")
                    val cores = parseManifest(JSONObject(body))
                    cachedCores = cores
                    onResult(Result.success(cores))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch plugin catalog")
                onResult(Result.failure(e))
            }
        }
    }

    /** The most recently fetched catalogue (empty before the first fetch). */
    fun availableCores(): List<PluginCore> = cachedCores

    /** Status snapshot for every known core, reflecting install state. */
    fun statuses(): List<CoreStatus> =
        cachedCores.map { core ->
            CoreStatus(
                core = core,
                state = pluginManager.state(core.id),
                binaryPath = pluginManager.binaryPath(core.id),
                message = null
            )
        }

    fun install(pluginManager: PluginManager, core: PluginCore): Boolean =
        pluginManager.isInstalled(core.id)

    fun shutdown() {
        executor.shutdownNow()
    }

    private fun parseManifest(json: JSONObject): List<PluginCore> {
        val array: JSONArray = json.optJSONArray("plugins") ?: JSONArray()
        val cores = mutableListOf<PluginCore>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val format = when (item.optString("archiveFormat", "tar.gz").lowercase()) {
                "zip" -> ArchiveFormat.ZIP
                "gz" -> ArchiveFormat.GZ
                else -> ArchiveFormat.TAR_GZ
            }
            cores.add(
                PluginCore(
                    id = item.optString("id", item.optString("name").lowercase()),
                    displayName = item.optString("name"),
                    description = item.optString("description", ""),
                    downloadUrl = item.optString("downloadUrl"),
                    fileName = item.optString("fileName"),
                    archiveFormat = format,
                    binaryName = item.optString("binaryName", item.optString("id")),
                    version = item.optString("version", ""),
                    architecture = item.optString("architecture", "")
                )
            )
        }
        return cores
    }
}
