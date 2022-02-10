package org.khawaja.fileshare.client.android.viewholder

import android.content.SharedPreferences
import android.view.View
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import org.khawaja.fileshare.client.android.adapter.ContactsFileAdapter
import org.khawaja.fileshare.client.android.adapter.FileAdapter
import org.khawaja.fileshare.client.android.databinding.ListFileNouveauBinding
import org.khawaja.fileshare.client.android.model.FileModel
import org.khawaja.fileshare.client.android.viewmodel.content.FileContentViewModel

class ContactsFileViewHolder(
    private val binding: ListFileNouveauBinding,
    private val clickListener: (FileModel, ContactsFileAdapter.ClickType) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    protected val defaultPreferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(binding.root.context)

    fun bind(fileModel: FileModel) {
        binding.viewModel = FileContentViewModel(fileModel)
        binding.root.setOnClickListener {
            clickListener(fileModel, ContactsFileAdapter.ClickType.Default)
        }


        //val cloneActive = defaultPreferences.getBoolean("clone_active", false)

        /*if(cloneActive== true)
        {
            fileModel.isSelected = !fileModel.isSelected
            binding.selection.isSelected = fileModel.isSelected
            clickListener(fileModel, FileAdapter.ClickType.ToggleSelect)
        }

        if(cloneActive == true)
        {
            fileModel.file.getName()
        }*/

        fileModel.isSelected = !fileModel.isSelected
        binding.selection.isSelected = fileModel.isSelected
        clickListener(fileModel, ContactsFileAdapter.ClickType.ToggleSelect)


        /* fileModel.isSelected = !fileModel.isSelected
         binding.selection.isSelected = fileModel.isSelected
         clickListener(fileModel, FileAdapter.ClickType.ToggleSelect)*/

        binding.selection.setOnClickListener {
            fileModel.isSelected = !fileModel.isSelected
            it.isSelected = fileModel.isSelected
            clickListener(fileModel, ContactsFileAdapter.ClickType.ToggleSelect)
        }
        binding.selection.isSelected = fileModel.isSelected
        binding.executePendingBindings()
    }
}
