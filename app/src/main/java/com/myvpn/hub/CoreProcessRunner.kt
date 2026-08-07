package com.multivpn.app

import android.os.Handler
import android.os.Looper
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CoreProcessRunner {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var process: Process? = null
    private var isRunning = false

    fun start(
        binaryPath: String,
        args: List<String>,
        logger: (String) -> Unit,
        workingDirectory: File? = null
    ) {
        stop()

        val command = mutableListOf(binaryPath)
        command.addAll(args)
        val builder = ProcessBuilder(command)
        workingDirectory?.let { builder.directory(it) }
        builder.redirectErrorStream(false)

        val newProcess = builder.start()
        process = newProcess
        isRunning = true

        val stdoutThread = Thread {
            readStream(newProcess.inputStream, logger)
        }
        val stderrThread = Thread {
            readStream(newProcess.errorStream, logger)
        }
        stdoutThread.start()
        stderrThread.start()

        executor.execute {
            val exitCode = newProcess.waitFor()
            mainHandler.post {
                logger("Process exited with code $exitCode")
            }
            synchronized(this) {
                if (process === newProcess) {
                    process = null
                    isRunning = false
                }
            }
        }
    }

    fun stop() {
        synchronized(this) {
            process?.destroy()
            process = null
            isRunning = false
        }
    }

    fun isRunning(): Boolean = synchronized(this) { isRunning }

    fun shutdown() {
        stop()
        executor.shutdownNow()
    }

    private fun readStream(stream: InputStream, logger: (String) -> Unit) {
        val reader = BufferedReader(InputStreamReader(stream))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            val message = line.orEmpty()
            mainHandler.post { logger(message) }
        }
    }
}
