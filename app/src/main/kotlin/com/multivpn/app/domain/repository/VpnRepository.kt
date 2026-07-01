package com.multivpn.app.domain.repository

/**
 * Repository interface for VPN operations
 */
interface VpnRepository {
    suspend fun getVpnState(): Boolean
    suspend fun connectVpn(): Boolean
    suspend fun disconnectVpn(): Boolean
    suspend fun getVpnConfiguration(): VpnConfig?
    suspend fun saveVpnConfiguration(config: VpnConfig)
}

data class VpnConfig(
    val id: String,
    val name: String,
    val protocol: String,
    val address: String,
    val port: Int,
    val username: String? = null,
    val password: String? = null,
    val certificatePath: String? = null
)
