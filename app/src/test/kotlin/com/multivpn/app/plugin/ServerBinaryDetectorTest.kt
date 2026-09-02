package com.multivpn.app.plugin

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ServerBinaryDetectorTest {

    @Test
    fun nameHints_linuxAmd64_isServer() {
        assertTrue(ServerBinaryDetector.looksLikeServerByName("sing-box-1.0.0-linux-amd64.tar.gz"))
        assertTrue(ServerBinaryDetector.looksLikeServerByName("xray-linux-x86_64.zip"))
    }

    @Test
    fun nameHints_androidArm_isNotServer() {
        assertFalse(ServerBinaryDetector.looksLikeServerByName("sing-box-1.13.18-android-arm64.tar.gz"))
        assertFalse(ServerBinaryDetector.looksLikeServerByName("Xray-android-arm64-v8a.zip"))
        assertFalse(ServerBinaryDetector.looksLikeServerByName("mihomo-android-arm64-v.gz"))
    }

    @Test
    fun nameHints_plainName_isNotFlagged() {
        // A plain binary name without architecture hints is not flagged by name.
        assertFalse(ServerBinaryDetector.looksLikeServerByName("my-custom-core"))
        assertFalse(ServerBinaryDetector.looksLikeServerByName("xray"))
    }

    @Test
    fun nameHints_explicitServer_isServer() {
        assertTrue(ServerBinaryDetector.looksLikeServerByName("tor-server-linux"))
    }

    @Test
    fun elfHeader_nonElfStream_isNotServer() {
        val bytes = "not an elf binary".toByteArray().inputStream()
        assertFalse(ServerBinaryDetector.isServerElf(bytes))
    }

    @Test
    fun elfHeader_arm64Elf_isNotServer() {
        // Minimal 64-bit ELF header targeting AArch64 (e_machine = 0xB7).
        val header = ByteArray(20)
        header[0] = 0x7F
        header[1] = 'E'.code.toByte()
        header[2] = 'L'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = 2 // 64-bit
        header[18] = 0xB7.toByte() // EM_AARCH64 (low byte)
        header[19] = 0
        assertFalse(ServerBinaryDetector.isServerElf(header.inputStream()))
    }

    @Test
    fun elfHeader_x86_64Elf_isServer() {
        // Minimal 64-bit ELF header targeting x86_64 (e_machine = 0x3E).
        val header = ByteArray(20)
        header[0] = 0x7F
        header[1] = 'E'.code.toByte()
        header[2] = 'L'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = 2 // 64-bit
        header[18] = 0x3E.toByte() // EM_X86_64 (low byte)
        header[19] = 0
        assertTrue(ServerBinaryDetector.isServerElf(header.inputStream()))
    }
}
