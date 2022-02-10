
package org.khawaja.fileshare.client.android.database;

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import org.khawaja.fileshare.client.android.database.model.Transfer
import org.khawaja.fileshare.client.android.database.model.TransferDetail

@Dao
interface TransferDao {
    @Query("SELECT EXISTS(SELECT * FROM transfer WHERE id = :groupId)")
    suspend fun contains(groupId: Long): Boolean

    @Delete
    suspend fun delete(transfer: Transfer)

    @Query("SELECT * FROM transfer WHERE id = :groupId")
    suspend fun get(groupId: Long): Transfer?

    @Query("SELECT * FROM transferDetail WHERE id = :groupId")
    fun getDetail(groupId: Long): LiveData<TransferDetail>

    @Query("SELECT * FROM transferDetail WHERE id = :groupId")
    fun getDetailDirect(groupId: Long): TransferDetail?

    @Query("SELECT * FROM transferDetail ORDER BY dateCreated DESC")
    fun getDetails(): LiveData<List<TransferDetail>>

    @Insert
    suspend fun insert(transfer: Transfer)

    @Update
    suspend fun update(transfer: Transfer)


    @Query("UPDATE transfer SET isPinned=:value WHERE id = :id")
    fun update(id: Long, value: Boolean)
}
