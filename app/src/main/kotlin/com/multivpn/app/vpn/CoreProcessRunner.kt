package com.multivpn.app.vpn

import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Runs a VPN core binary (sing-box / Xray / mihomo) as a local process.
 *
 * The core is started with {@code run -c <config>} so it listens as a local
 * SOCKS/HTTP proxy (default 127.0.0.1:10808). The VPN service then routes the
 * system traffic through this local proxy via a TUN interface.
 */
class CoreProcessRunner {

    @Volatile
    private var process: Process? = null
    private val running = AtomicBoolean(false)

    /**
     * Start the core binary.
     *
     * @param binaryPath absolute path to the executable core binary
     * @param configPath absolute path to the JSON config file
     * @param onLog invoked on a background thread for each line of core output
     */
    @Throws(IOException::class)
    fun start(binaryPath: String, configPath: String, onLog: (String) -> Unit) {
        stop()
        val binary = File(binaryPath)
        if (!binary.exists()) {
            throw IOException("Core binary not found: $binaryPath")
        }

        val command = listOf(binaryPath, "run", "-c", configPath)
        Timber.d("Starting core: ${command.joinToString(" ")}")

        val builder = ProcessBuilder(command).redirectErrorStream(true)
        val p = builder.start()
        process = p
        running.set(true)

        Thread {
            try {
                BufferedReader(InputStreamReader(p.inputStream)).use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        onLog(line)
                        line = reader.readLine()
                    }
                }
            } catch (e: Exception) {
                if (running.get()) {
                    Timber.e(e, "Core output reader crashed")
                }
            }
        }.start()

        Thread {
            val code = p.waitFor()
            running.set(false)
            onLog("Core process exited with code $code")
            Timber.d("Core process exited with code $code")
        }.start()
    }

    fun stop() {
        running.set(false)
        process?.let {
            it.destroy()
            if (!it.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)) {
                it.destroyForcibly()
            }
        }
        process = null
    }

    fun isRunning(): Boolean = running.get() && process?.isAlive == true
}
