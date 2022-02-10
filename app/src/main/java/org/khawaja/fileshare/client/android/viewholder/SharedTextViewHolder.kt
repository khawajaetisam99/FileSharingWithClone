
package org.khawaja.fileshare.client.android.viewholder

import androidx.recyclerview.widget.RecyclerView
import org.khawaja.fileshare.client.android.database.model.SharedText
import org.khawaja.fileshare.client.android.databinding.ListSharedTextBinding
import org.khawaja.fileshare.client.android.viewmodel.content.SharedTextContentViewModel

class SharedTextViewHolder(
    private val binding: ListSharedTextBinding,
    private val clickListener: (SharedText) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(sharedText: SharedText) {
        binding.viewModel = SharedTextContentViewModel(sharedText)
        binding.container.setOnClickListener { clickListener(sharedText) }
        binding.executePendingBindings()
    }
}
