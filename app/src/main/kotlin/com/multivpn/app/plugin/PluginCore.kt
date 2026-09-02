package com.multivpn.app.plugin

/**
 * Describes a downloadable VPN core (e.g. sing-box, Xray, Clash).
 *
 * A core is an external binary fetched from a remote archive, extracted and
 * made executable so the app can run it as a local proxy process.
 */
data class PluginCore(
    val id: String,
    val displayName: String,
    val description: String,
    val downloadUrl: String,
    val fileName: String,
    val archiveFormat: ArchiveFormat,
    val binaryName: String,
    val version: String,
    val architecture: String
)

enum class ArchiveFormat {
    TAR_GZ,
    ZIP,
    GZ
}

/**
 * Runtime state of a core on this device.
 */
enum class CoreState {
    NOT_INSTALLED,
    DOWNLOADING,
    INSTALLING,
    INSTALLED,
    ENABLED,
    ERROR
}

/**
 * Snapshot of a core's current state, including the path to the extracted
 * binary once it has been installed.
 */
data class CoreStatus(
    val core: PluginCore,
    val state: CoreState,
    val binaryPath: String?,
    val message: String?
) {
    companion object {
        fun fresh(core: PluginCore): CoreStatus =
            CoreStatus(core, CoreState.NOT_INSTALLED, null, null)
    }
}

/**
 * Metadata for a custom (user-installed) core, loaded from the persisted
 * sidecar JSON so it can appear in the plugin list alongside catalogue cores.
 */
data class CustomCoreInfo(
    val id: String,
    val displayName: String,
    val binaryName: String,
    val binaryPath: String
)

/**
 * Thrown when a user tries to install a binary that targets a server
 * (x86_64 Linux) rather than an Android device.
 */
class ServerBinaryException(val sourceName: String) :
    Exception("'$sourceName' is a server binary (x86_64 Linux), not an Android client build.")
