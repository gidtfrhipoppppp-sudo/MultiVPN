package com.multivpn.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for VPN configuration
 */
@Entity(tableName = "vpn_config")
data class VpnConfigEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val protocol: String,
    val address: String,
    val port: Int,
    val username: String? = null,
    val password: String? = null,
    val certificatePath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
