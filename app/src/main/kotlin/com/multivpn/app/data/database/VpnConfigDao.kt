package com.multivpn.app.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.multivpn.app.data.database.entity.VpnConfigEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for VPN configurations
 */
@Dao
interface VpnConfigDao {

    @Insert
    suspend fun insert(config: VpnConfigEntity)

    @Update
    suspend fun update(config: VpnConfigEntity)

    @Delete
    suspend fun delete(config: VpnConfigEntity)

    @Query("SELECT * FROM vpn_config WHERE id = :id")
    suspend fun getConfigById(id: String): VpnConfigEntity?

    @Query("SELECT * FROM vpn_config")
    fun getAllConfigs(): Flow<List<VpnConfigEntity>>

    @Query("SELECT * FROM vpn_config LIMIT 1")
    suspend fun getFirstConfig(): VpnConfigEntity?

    @Query("DELETE FROM vpn_config")
    suspend fun deleteAll()
}
