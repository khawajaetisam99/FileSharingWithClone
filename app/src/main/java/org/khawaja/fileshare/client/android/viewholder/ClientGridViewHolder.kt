package org.khawaja.fileshare.client.android.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.khawaja.fileshare.client.android.databinding.ListClientGridBinding
import org.khawaja.fileshare.client.android.model.ClientRoute
import org.khawaja.fileshare.client.android.viewmodel.content.ClientContentViewModel
import org.khawaja.fileshare.client.android.viewmodel.content.ClientContentViewModel.ClickType

class ClientGridViewHolder(
    private val binding: ListClientGridBinding,
    private val clickListener: (ClientRoute, ClickType) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(clientRoute: ClientRoute) {
        binding.viewModel = ClientContentViewModel(clientRoute.client)
        binding.clickListener = View.OnClickListener { clickListener(clientRoute, ClickType.Default) }
        binding.detailsClickListener = View.OnClickListener { clickListener(clientRoute, ClickType.Details) }
        binding.executePendingBindings()
    }
}