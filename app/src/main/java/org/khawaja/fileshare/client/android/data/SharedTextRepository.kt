
package org.khawaja.fileshare.client.android.data

import androidx.lifecycle.LiveData
import org.khawaja.fileshare.client.android.database.SharedTextDao
import org.khawaja.fileshare.client.android.database.model.SharedText
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedTextRepository @Inject constructor(
    private val sharedTextDao: SharedTextDao,
) {
    suspend fun delete(sharedText: SharedText) = sharedTextDao.delete(sharedText)

    fun getSharedTexts(): LiveData<List<SharedText>> = sharedTextDao.getAll()

    suspend fun insert(sharedText: SharedText) = sharedTextDao.insert(sharedText)

    suspend fun update(sharedText: SharedText) = sharedTextDao.update(sharedText)
}