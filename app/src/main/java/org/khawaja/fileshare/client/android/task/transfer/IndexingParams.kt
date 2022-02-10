package org.khawaja.fileshare.client.android.task.transfer

import org.khawaja.fileshare.client.android.database.model.UClient

data class IndexingParams(
    val groupId: Long,
    val client: UClient,
    val jsonData: String,
    val hasPin: Boolean,
)