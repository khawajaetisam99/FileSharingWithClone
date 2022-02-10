
package org.khawaja.fileshare.client.android.viewmodel.content

import android.app.Dialog
import android.view.View
import android.view.Window
import androidx.appcompat.widget.PopupMenu
import com.genonbeta.android.framework.util.Files
import com.google.android.material.button.MaterialButton
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.database.model.TransferDetail
import org.khawaja.fileshare.client.android.protocol.isIncoming
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

class TransferDetailContentViewModel(detail: TransferDetail) {
    val clientNickname = detail.clientNickname

    val sizeText = Files.formatLength(detail.size, false)

    val isFinished = detail.itemsCount == detail.itemsDoneCount

    val isReceiving = detail.direction.isIncoming

    val isPinned = detail.isPinned

    val count = detail.itemsCount

    val dateCreatedFromTransferDetails = detail.dateCreated
    val dateCreated = convertLongToTime(dateCreatedFromTransferDetails)

    val fileDirection = if(isReceiving){
        "Incoming"
    }else {
        "Outgoing"
    }

    val icon = if (isReceiving) {
        R.drawable.ic_arrow_down_white_24dp
    } else {
        R.drawable.bottom_bar_icon_two_blue
    }


    // Home screen Pinned files container testing format
   /* val iconPinned = if (isPinned) {
        R.drawable.ic_fluent_pin_24_unchecked
    } else {
        R.drawable.ic_fluent_pin_24_filled
    }

    val iconPinnedTransferDetails = if (isPinned) {
        R.drawable.pinned_file_white
    } else {
        R.drawable.pinned_files_red
    }*/


    //correct format
    val iconPinned = if (isPinned) {
        R.drawable.ic_fluent_pin_24_filled
    } else {
        R.drawable.ic_fluent_pin_24_unchecked
    }

    val iconPinnedTransferDetails = if (isPinned) {
        R.drawable.pinned_files_red
    } else {
        R.drawable.pinned_file_white
    }


    val finishedIcon = R.drawable.ic_done_white_24dp

    val needsApproval = !detail.accepted && isReceiving

    var onRemove: (() -> Unit)? = null


    private val percentage = with(detail) {
        if (sizeOfDone <= 0) 0 else ((sizeOfDone.toDouble() / size) * 100).toInt()
    }

    val progress = max(1, percentage)

    val percentageText = percentage.toString()

    val waitingApproval = !detail.accepted && !isReceiving



    fun showPopupMenu(view: View) {
        /*PopupMenu(view.context, view).apply {
            inflate(R.menu.transfer_details)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.remove -> onRemove?.invoke()
                    else -> return@setOnMenuItemClickListener false
                }

                true
            }
            show()
        }*/

        val dialog = Dialog(view.context )
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_dialog_layout)
        //val body = dialog.findViewById(R.id.body) as TextView
        //body.text = title
        val btnDelete = dialog.findViewById(R.id.btnDelete) as MaterialButton
        val btncancel = dialog.findViewById(R.id.btnCancel) as MaterialButton
        btnDelete.setOnClickListener {
            onRemove?.invoke()
            dialog.dismiss()
        }
        btncancel.setOnClickListener { dialog.dismiss() }
        dialog.show()


    }


    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd MMM, yyyy  hh:mm aa")
        //val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
        return format.format(date)
    }

}
