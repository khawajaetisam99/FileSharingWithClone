
package org.khawaja.fileshare.client.android.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import org.khawaja.fileshare.client.android.database.model.SafFolder

@Dao
interface SafFolderDao {
    @Query("SELECT * FROM safFolder ORDER BY name ASC")
    fun getAll(): LiveData<List<SafFolder>>

    @Insert
    suspend fun insert(folder: SafFolder)

    @Query("DELETE FROM safFolder")
    suspend fun removeAll()
}
