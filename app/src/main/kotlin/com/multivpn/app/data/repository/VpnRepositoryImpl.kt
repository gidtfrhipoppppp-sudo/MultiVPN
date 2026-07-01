package com.multivpn.app.data.repository

import com.multivpn.app.data.database.VpnDatabase
import com.multivpn.app.data.database.entity.VpnConfigEntity
import com.multivpn.app.domain.repository.VpnConfig
import com.multivpn.app.domain.repository.VpnRepository
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation of VPN Repository
 */
class VpnRepositoryImpl @Inject constructor(
    private val database: VpnDatabase
) : VpnRepository {

    private var isConnected = false

    override suspend fun getVpnState(): Boolean {
        return isConnected
    }

    override suspend fun connectVpn(): Boolean {
        return try {
            Timber.d("Connecting to VPN...")
            // TODO: Implement actual VPN connection logic
            isConnected = true
            Timber.d("Connected to VPN")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error connecting to VPN")
            isConnected = false
            throw e
        }
    }

    override suspend fun disconnectVpn(): Boolean {
        return try {
            Timber.d("Disconnecting from VPN...")
            // TODO: Implement actual VPN disconnection logic
            isConnected = false
            Timber.d("Disconnected from VPN")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error disconnecting from VPN")
            throw e
        }
    }

    override suspend fun getVpnConfiguration(): VpnConfig? {
        return try {
            val entity = database.vpnConfigDao().getFirstConfig()
            entity?.toVpnConfig()
        } catch (e: Exception) {
            Timber.e(e, "Error getting VPN configuration")
            null
        }
    }

    override suspend fun saveVpnConfiguration(config: VpnConfig) {
        try {
            val entity = config.toEntity()
            database.vpnConfigDao().insert(entity)
            Timber.d("VPN configuration saved")
        } catch (e: Exception) {
            Timber.e(e, "Error saving VPN configuration")
            throw e
        }
    }

    private fun VpnConfigEntity.toVpnConfig(): VpnConfig {
        return VpnConfig(
            id = id,
            name = name,
            protocol = protocol,
            address = address,
            port = port,
            username = username,
            password = password,
            certificatePath = certificatePath
        )
    }

    private fun VpnConfig.toEntity(): VpnConfigEntity {
        return VpnConfigEntity(
            id = id.ifEmpty { UUID.randomUUID().toString() },
            name = name,
            protocol = protocol,
            address = address,
            port = port,
            username = username,
            password = password,
            certificatePath = certificatePath
        )
    }
}
