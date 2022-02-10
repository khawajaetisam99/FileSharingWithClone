package org.khawaja.fileshare.client.android.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import org.khawaja.fileshare.client.android.databinding.ListFileNouveauBinding
import org.khawaja.fileshare.client.android.databinding.ListSectionTitleBinding
import org.khawaja.fileshare.client.android.model.FileModel
import org.khawaja.fileshare.client.android.model.ListItem
import org.khawaja.fileshare.client.android.model.TitleSectionContentModel
import org.khawaja.fileshare.client.android.viewholder.ContactsFileViewHolder
import org.khawaja.fileshare.client.android.viewholder.FileViewHolder
import org.khawaja.fileshare.client.android.viewholder.TitleSectionViewHolder

class ContactsFileAdapter(
    private val clickListener: (FileModel, ClickType) -> Unit
) : ListAdapter<ListItem, ViewHolder>(ContactsFilesItemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = when (viewType) {
        VIEW_TYPE_FILE -> ContactsFileViewHolder(
            ListFileNouveauBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            clickListener
        )
        VIEW_TYPE_SECTION -> TitleSectionViewHolder(
            ListSectionTitleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
        else -> throw UnsupportedOperationException()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("child_count", "file adapter iterated adapter")
        when (val item = getItem(position)) {
            is FileModel -> if (holder is ContactsFileViewHolder) holder.bind(item)
            is TitleSectionContentModel -> if (holder is TitleSectionViewHolder) holder.bind(item)
            else -> throw IllegalStateException()
        }
    }


    override fun getItemId(position: Int): Long {
        return getItem(position).listId
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is FileModel -> VIEW_TYPE_FILE
        is TitleSectionContentModel -> VIEW_TYPE_SECTION
        else -> throw IllegalStateException()
    }

    enum class ClickType {
        Default,
        ToggleSelect
    }

    companion object {
        const val VIEW_TYPE_SECTION = 0

        const val VIEW_TYPE_FILE = 1
    }
}

class ContactsFilesItemCallback : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem.javaClass == newItem.javaClass && oldItem.listId == newItem.listId
    }

    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem !is FileModel || newItem !is FileModel || oldItem.file.getLength() == newItem.file.getLength()
                || oldItem.file.getLastModified() == newItem.file.getLastModified()
    }
}
