package org.khawaja.fileshare.client.android.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.data.SelectionRepository
import org.khawaja.fileshare.client.android.database.model.UTransferItem
import org.khawaja.fileshare.client.android.databinding.LayoutSharingBinding
import org.khawaja.fileshare.client.android.databinding.ListSharingItemBinding
import org.khawaja.fileshare.client.android.itemcallback.UTransferItemCallback
import org.khawaja.fileshare.client.android.util.CommonErrors
import org.khawaja.fileshare.client.android.viewmodel.ClientPickerViewModel
import org.khawaja.fileshare.client.android.viewmodel.SharingState
import org.khawaja.fileshare.client.android.viewmodel.SharingViewModel
import org.khawaja.fileshare.client.android.viewmodel.content.TransferItemContentViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SharingFragment : Fragment(R.layout.layout_sharing) {
    private val args: SharingFragmentArgs by navArgs()

    private val sharingViewModel: SharingViewModel by viewModels()

    private val sharingSelectionsViewModel: SharingSelectionsViewModel by viewModels()

    private val clientPickerViewModel: ClientPickerViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharingSelectionsViewModel.serve(args.contents.toList())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = LayoutSharingBinding.bind(view)
        val adapter = SharingContentAdapter()
        val snackbar = Snackbar.make(binding.listParent, R.string.sending, Snackbar.LENGTH_INDEFINITE)

        binding.shareOnWeb.setOnClickListener {
            findNavController().navigate(SharingFragmentDirections.actionSharingFragmentToWebShareLauncherFragment2())
        }
        binding.sendButton.setOnClickListener {
            findNavController().navigate(SharingFragmentDirections.pickClient())
        }

        binding.recyclerView.adapter = adapter

        adapter.submitList(args.contents.toList())

        clientPickerViewModel.bridge.observe(viewLifecycleOwner) { bridge ->
            sharingViewModel.consume(bridge, args.groupId, args.contents.toList())
        }

        sharingViewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                is SharingState.Running -> snackbar.setText(R.string.sending).show()
                is SharingState.Success -> {
                    snackbar.dismiss()
                    findNavController().navigate(
                        SharingFragmentDirections.actionSharingFragmentToNavTransferDetails(it.transfer)
                    )
                }
                is SharingState.Error -> snackbar.setText(CommonErrors.messageOf(view.context, it.exception)).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sharingSelectionsViewModel.dropAll()
    }
}

class SharingContentAdapter : ListAdapter<UTransferItem, SharingViewHolder>(UTransferItemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SharingViewHolder {
        return SharingViewHolder(
            ListSharingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: SharingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int): Long = with(getItem(position)) { groupId + id }
}

class SharingViewHolder(private val binding: ListSharingItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(transferItem: UTransferItem) {
        binding.viewModel = TransferItemContentViewModel(transferItem)
        binding.executePendingBindings()
    }
}

@HiltViewModel
class SharingSelectionsViewModel @Inject internal constructor(
    private val selectionRepository: SelectionRepository,
) : ViewModel() {
    fun serve(list: List<Any>) {
        selectionRepository.addAll(list)
    }

    fun dropAll() {
        selectionRepository.clearSelections()
    }
}
