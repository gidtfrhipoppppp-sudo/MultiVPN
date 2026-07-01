package com.multivpn.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.multivpn.app.data.database.entity.VpnConfigEntity

/**
 * Room database for VPN configuration
 */
@Database(
    entities = [VpnConfigEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VpnDatabase : RoomDatabase() {
    abstract fun vpnConfigDao(): VpnConfigDao
}
