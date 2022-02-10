
package org.khawaja.fileshare.client.android.itemcallback

import androidx.recyclerview.widget.DiffUtil
import org.khawaja.fileshare.client.android.database.model.UClient

class UClientItemCallback : DiffUtil.ItemCallback<UClient>() {
    override fun areItemsTheSame(oldItem: UClient, newItem: UClient): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: UClient, newItem: UClient): Boolean {
        return oldItem == newItem
    }
}