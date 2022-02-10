
package org.khawaja.fileshare.client.android.itemcallback

import androidx.recyclerview.widget.DiffUtil
import org.khawaja.fileshare.client.android.database.model.UTransferItem

class UTransferItemCallback : DiffUtil.ItemCallback<UTransferItem>() {
    override fun areItemsTheSame(oldItem: UTransferItem, newItem: UTransferItem): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: UTransferItem, newItem: UTransferItem): Boolean {
        return oldItem == newItem
    }
}