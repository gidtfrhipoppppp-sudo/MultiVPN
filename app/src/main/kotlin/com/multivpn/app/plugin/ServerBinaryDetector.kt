package com.multivpn.app.plugin

import java.io.File
import java.io.InputStream

/**
 * Detects whether a binary archive or file is intended for a server (x86_64
 * Linux) rather than an Android device (arm/arm64).
 *
 * The check is heuristic and combines two signals:
 *  1. Name/URL heuristics — "linux", "server", "amd64", "x86_64" strongly
 *     suggest a server build, while "android", "arm64", "armv7" suggest a
 *     client build.
 *  2. ELF header inspection — an ELF binary's e_machine field reveals the
 *     target architecture: EM_X86_64 (0x3E) is a server build, EM_AARCH64
 *     (0xB7) / EM_ARM (0x28) are valid Android targets.
 */
object ServerBinaryDetector {

    private val SERVER_NAME_HINTS = listOf("linux-x86_64", "linux-amd64", "x86_64", "amd64", "server")
    private val CLIENT_NAME_HINTS = listOf("android", "arm64", "armv7", "aarch64", "arm-")

    /**
     * @return true if the name/url strongly suggests a server (x86_64 Linux) build.
     */
    fun looksLikeServerByName(nameOrUrl: String): Boolean {
        val lower = nameOrUrl.lowercase()
        // Explicit android/arm hint means it's a client build.
        if (CLIENT_NAME_HINTS.any { lower.contains(it) }) return false
        return SERVER_NAME_HINTS.any { lower.contains(it) }
    }

    /**
     * Inspect an ELF file's header to determine if it targets x86_64 (server)
     * rather than arm/arm64 (Android).
     *
     * @param binary the extracted binary file
     * @return true if the binary is an ELF targeting x86_64
     */
    fun isServerElf(binary: File): Boolean {
        if (!binary.exists() || binary.length() < ELF_HEADER_MIN) return false
        return binary.inputStream().use { isServerElf(it) }
    }

    /**
     * Inspect an ELF stream's header. The caller is responsible for closing the stream.
     *
     * @return true if the stream is an ELF targeting x86_64
     */
    fun isServerElf(input: InputStream): Boolean {
        val header = ByteArray(20)
        val read = input.read(header)
        if (read < 20) return false
        // ELF magic: 0x7F 'E' 'L' 'F'
        if (header[0].toInt() and 0xFF != 0x7F) return false
        if (header[1].toInt() != 'E'.code || header[2].toInt() != 'L'.code || header[3].toInt() != 'F'.code) {
            return false
        }
        // ei_class at offset 4: 1 = 32-bit, 2 = 64-bit
        val is64Bit = header[4].toInt() == 2
        // e_machine is at offset 18 (2 bytes, little-endian) for both 32/64-bit ELF.
        val machine = (header[18].toInt() and 0xFF) or ((header[19].toInt() and 0xFF) shl 8)
        return is64Bit && machine == EM_X86_64
    }

    /**
     * Combined verdict: block if the name looks like a server build, or if the
     * extracted binary is an ELF targeting x86_64.
     */
    fun isServerBinary(nameOrUrl: String, binary: File?): Boolean {
        if (looksLikeServerByName(nameOrUrl)) return true
        if (binary != null && isServerElf(binary)) return true
        return false
    }

    private const val ELF_HEADER_MIN = 20L
    private const val EM_X86_64 = 0x3E
}
