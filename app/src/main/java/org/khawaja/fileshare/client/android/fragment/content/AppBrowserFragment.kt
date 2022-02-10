

package org.khawaja.fileshare.client.android.fragment.content

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.content.App
import org.khawaja.fileshare.client.android.data.MediaRepository
import org.khawaja.fileshare.client.android.data.SelectionRepository
import org.khawaja.fileshare.client.android.databinding.LayoutEmptyContentBinding
import org.khawaja.fileshare.client.android.databinding.ListAppBinding
import org.khawaja.fileshare.client.android.viewmodel.EmptyContentViewModel
import org.khawaja.fileshare.client.android.viewmodel.SharingSelectionViewModel
import javax.inject.Inject

@AndroidEntryPoint
class AppBrowserFragment : Fragment(R.layout.layout_app_browser) {
    private val browserViewModel: AppBrowserViewModel by viewModels()

    private val selectionViewModel: SharingSelectionViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val emptyView = LayoutEmptyContentBinding.bind(view.findViewById(R.id.emptyView))
        val adapter = AppBrowserAdapter { app, clickType ->
            when (clickType) {
                AppBrowserAdapter.ClickType.Launch -> {

                }
                AppBrowserAdapter.ClickType.ToggleSelect -> selectionViewModel.setSelected(app, app.isSelected)
            }
        }
        val emptyContentViewModel = EmptyContentViewModel()

        emptyView.viewModel = emptyContentViewModel
        emptyView.emptyText.setText(R.string.empty_apps_list)
        emptyView.emptyImage.setImageResource(R.drawable.ic_android_head_white_24dp)
        emptyView.executePendingBindings()
        adapter.setHasStableIds(true)
        recyclerView.adapter = adapter

        browserViewModel.allApps.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            emptyContentViewModel.with(recyclerView, it.isNotEmpty())
        }

        selectionViewModel.externalState.observe(viewLifecycleOwner) {
            adapter.notifyDataSetChanged()
        }
    }
}

class AppBrowserAdapter(
    val clickListener: (App, ClickType) -> Unit
) : ListAdapter<App, AppViewHolder>(AppItemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        return AppViewHolder(
            ListAppBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            clickListener,
        )
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).packageName.hashCode().toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_APP
    }

    companion object {
        const val VIEW_TYPE_APP = 0
    }

    enum class ClickType {
        Launch,
        ToggleSelect,
    }
}

class AppItemCallback : DiffUtil.ItemCallback<App>() {
    override fun areItemsTheSame(oldItem: App, newItem: App): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: App, newItem: App): Boolean {
        return oldItem == newItem
    }
}

class AppContentViewModel(app: App) {
    val label = app.label

    val version = app.versionName

    val info = app.info
}

class AppViewHolder(
    private val binding: ListAppBinding,
    private val clickListener: (App, AppBrowserAdapter.ClickType) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {


    protected val defaultPreferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
    fun bind(app: App) {
        binding.viewModel = AppContentViewModel(app)
        binding.root.setOnClickListener {
            clickListener(app, AppBrowserAdapter.ClickType.Launch)
        }

        val cloneActive = defaultPreferences.getBoolean("clone_active", false)

        if (cloneActive==true) {
            app.isSelected = !app.isSelected
            binding.selection.isSelected = app.isSelected
            clickListener(app, AppBrowserAdapter.ClickType.ToggleSelect)
        }

        binding.selection.setOnClickListener {
            app.isSelected = !app.isSelected
            it.isSelected = app.isSelected
            clickListener(app, AppBrowserAdapter.ClickType.ToggleSelect)
        }
        binding.selection.isSelected = app.isSelected
        binding.executePendingBindings()
    }
}

@HiltViewModel
class AppBrowserViewModel @Inject internal constructor(
    mediaRepository: MediaRepository,
    private val selectionRepository: SelectionRepository,
) : ViewModel() {
    val allApps = Transformations.map(mediaRepository.getAllApps()) {
        selectionRepository.whenContains(it) { item, selected -> item.isSelected = selected }
        it
    }
}
