
package org.khawaja.fileshare.client.android.viewholder

import androidx.recyclerview.widget.RecyclerView
import org.khawaja.fileshare.client.android.databinding.ListLibraryLicenseBinding
import org.khawaja.fileshare.client.android.model.LibraryLicense
import org.khawaja.fileshare.client.android.viewmodel.content.LibraryLicenseContentViewModel

class LibraryLicenseViewHolder(
    private val binding: ListLibraryLicenseBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(libraryLicense: LibraryLicense) {
        binding.viewModel = LibraryLicenseContentViewModel(libraryLicense)
        binding.executePendingBindings()
    }
}