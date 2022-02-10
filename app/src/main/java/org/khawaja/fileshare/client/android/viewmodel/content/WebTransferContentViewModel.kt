
package org.khawaja.fileshare.client.android.viewmodel.content

import com.genonbeta.android.framework.util.Files
import org.khawaja.fileshare.client.android.database.model.WebTransfer
import org.khawaja.fileshare.client.android.util.MimeIcons

class WebTransferContentViewModel(transfer: WebTransfer) {
    val name = transfer.name

    val icon = MimeIcons.loadMimeIcon(transfer.mimeType)

    val mimeType = transfer.mimeType

    val size = Files.formatLength(transfer.size)

    val uri = transfer.uri
}
