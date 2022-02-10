package org.khawaja.fileshare.client.android.viewmodel.content

import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.genonbeta.android.framework.util.Files
import org.khawaja.fileshare.client.android.GlideApp
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.model.FileModel
import org.khawaja.fileshare.client.android.util.MimeIcons
import org.monora.uprotocol.core.transfer.TransferItem

class FileContentViewModel(fileModel: FileModel) {
    val name = fileModel.file.getName()

    val count = fileModel.indexCount

    val isDirectory = fileModel.file.isDirectory()

    val mimeType = fileModel.file.getType()

    val icon = if (isDirectory) R.drawable.ic_folder_white_24dp else MimeIcons.loadMimeIcon(mimeType)

    val indexCount = fileModel.indexCount

    val sizeText by lazy {
        Files.formatLength(fileModel.file.getLength(), false)
    }

    val uri = fileModel.file.getUri()
}
