package org.khawaja.fileshare.client.android.viewholder

import androidx.recyclerview.widget.RecyclerView
import org.khawaja.fileshare.client.android.databinding.ListSectionDateBinding
import org.khawaja.fileshare.client.android.model.DateSectionContentModel
import org.khawaja.fileshare.client.android.viewmodel.content.DateSectionContentViewModel

class DateSectionViewHolder(val binding: ListSectionDateBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(contentModel: DateSectionContentModel) {
        binding.viewModel = DateSectionContentViewModel(contentModel)
        binding.executePendingBindings()
    }
}