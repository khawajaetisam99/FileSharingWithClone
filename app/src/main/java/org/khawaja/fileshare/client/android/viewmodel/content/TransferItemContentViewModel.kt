

package org.khawaja.fileshare.client.android.viewmodel.content

import com.genonbeta.android.framework.util.Files
import org.khawaja.fileshare.client.android.database.model.UTransferItem

class TransferItemContentViewModel(val transferItem: UTransferItem) {
    val name = transferItem.name

    val size = Files.formatLength(transferItem.size)

    val mimeType = transferItem.mimeType
}