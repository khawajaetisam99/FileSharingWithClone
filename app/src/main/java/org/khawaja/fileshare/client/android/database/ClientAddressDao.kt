
package org.khawaja.fileshare.client.android.database

import androidx.room.*
import org.khawaja.fileshare.client.android.database.model.UClientAddress

@Dao
interface ClientAddressDao {
    @Query("SELECT * FROM clientAddress WHERE clientUid = :clientUid")
    suspend fun getAll(clientUid: String): List<UClientAddress>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(address: UClientAddress)
}