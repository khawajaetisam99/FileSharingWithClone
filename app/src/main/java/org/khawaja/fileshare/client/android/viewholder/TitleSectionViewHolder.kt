
package org.khawaja.fileshare.client.android.viewholder

import androidx.recyclerview.widget.RecyclerView
import org.khawaja.fileshare.client.android.databinding.ListSectionTitleBinding
import org.khawaja.fileshare.client.android.model.TitleSectionContentModel
import org.khawaja.fileshare.client.android.viewmodel.content.TitleSectionContentViewModel

class TitleSectionViewHolder(val binding: ListSectionTitleBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(contentModel: TitleSectionContentModel) {
        binding.viewModel = TitleSectionContentViewModel(contentModel)
        binding.executePendingBindings()
    }
}