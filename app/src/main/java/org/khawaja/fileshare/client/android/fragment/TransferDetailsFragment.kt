
package org.khawaja.fileshare.client.android.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.genonbeta.android.framework.io.DocumentFile
import com.genonbeta.android.framework.util.Files
import com.google.android.material.snackbar.Snackbar
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.data.TransferRepository
import org.khawaja.fileshare.client.android.database.model.Transfer
import org.khawaja.fileshare.client.android.database.model.UTransferItem
import org.khawaja.fileshare.client.android.databinding.LayoutTransferDetailsBinding
import org.khawaja.fileshare.client.android.databinding.LayoutTransferItemBinding
import org.khawaja.fileshare.client.android.databinding.ListTransferItemBinding
import org.khawaja.fileshare.client.android.protocol.isIncoming
import org.khawaja.fileshare.client.android.service.backgroundservice.Task
import org.khawaja.fileshare.client.android.util.Activities
import org.khawaja.fileshare.client.android.util.CommonErrors
import org.khawaja.fileshare.client.android.viewmodel.EmptyContentViewModel
import org.khawaja.fileshare.client.android.viewmodel.TransferDetailsViewModel
import org.khawaja.fileshare.client.android.viewmodel.TransferManagerViewModel
import org.khawaja.fileshare.client.android.viewmodel.content.ClientContentViewModel
import org.khawaja.fileshare.client.android.viewmodel.content.TransferDetailContentViewModel
import org.monora.uprotocol.core.protocol.Direction
import org.monora.uprotocol.core.transfer.TransferItem
import javax.inject.Inject

@AndroidEntryPoint
class TransferDetailsFragment : Fragment(R.layout.layout_transfer_details) {
    @Inject
    lateinit var factory: TransferDetailsViewModel.Factory

    private val args: TransferDetailsFragmentArgs by navArgs()

    private val managerViewModel: TransferManagerViewModel by viewModels()

    private val viewModel: TransferDetailsViewModel by viewModels {
        TransferDetailsViewModel.ModelFactory(factory, args.transfer)
    }

    private var pinningvalue = 0

    var pinnedList: List<Transfer>? = null


    //Transfer Items viewModels an factory initialization
    @Inject
    lateinit var factoryTrasnferItems: ItemViewModel.Factory

    private val argsTransferItems: TransferItemFragmentArgs by navArgs()

    private val viewModelTransferItems: ItemViewModel by viewModels {
        ItemViewModel.ModelFactory(factoryTrasnferItems, argsTransferItems.transfer)
    }
    /*override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.layout_transfer_item, container, false)
    }*/



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = LayoutTransferDetailsBinding.bind(view)

        binding.image.setOnClickListener {
            viewModel.client.value?.let {
                findNavController().navigate(
                    TransferDetailsFragmentDirections.actionTransferDetailsFragmentToClientDetailsFragment2(it)
                )
            }
        }


        if(viewModel.transferDetail.value?.isPinned  == false){

        }

        //if (pinnedList!!.size > 0 && pinnedList. .isPinned ==false)



        // show files by default
        /*findNavController().navigate(
            TransferDetailsFragmentDirections.actionTransferDetailsFragmentToTransferItemFragment(args.transfer)
        )*/

        binding.showFilesButton.setOnClickListener {
            findNavController().navigate(
                TransferDetailsFragmentDirections.actionTransferDetailsFragmentToTransferItemFragment(args.transfer)
            )
        }
        binding.toggleButton.setOnClickListener {
            val client = viewModel.client.value ?: return@setOnClickListener
            val detail = viewModel.transferDetail.value ?: return@setOnClickListener

            managerViewModel.toggleTransferOperation(args.transfer, client, detail)
            //binding.toggleButton.visibility = View.GONE
        }
        binding.acceptButtonTransferDetails?.setOnClickListener{
            val client = viewModel.client.value ?: return@setOnClickListener
            val detail = viewModel.transferDetail.value ?: return@setOnClickListener

            managerViewModel.toggleTransferOperation(args.transfer, client, detail)

        }
        binding.rejectButton.setOnClickListener {
            val client = viewModel.client.value ?: return@setOnClickListener
            managerViewModel.rejectTransferRequest(args.transfer, client)
            binding.rejectButton.visibility = View.GONE
        }


        binding.pinFilesTransferLayout?.setOnClickListener {


            if(viewModel.transferDetail.value?.isPinned  == false){
                viewModel.update(viewModel.transferDetail.value!!.id, true)
                binding.pinFilesTransferLayout.setImageResource(R.drawable.pinned_files_red)
            }
            else{

                viewModel.update(viewModel.transferDetail.value!!.id, false)
                binding.pinFilesTransferLayout.setImageResource(R.drawable.pinned_file_white)

            }
            /*val client = viewModel.client.value ?: return@setOnClickListener
            managerViewModel.rejectTransferRequest(args.transfer, client)
            binding.rejectButton.visibility = View.GONE*/

            /*if(binding.pinFilesTransferLayout.background = resources.getDrawable(R.drawable.pinned_file_white))
            binding.pinFilesTransferLayout.setImageResource()*/


        }

        binding.openDirectoryButton.setOnClickListener {
            viewModel.runCatching {
                Activities.view(requireActivity(), viewModel.getTransferStorage())
            }
        }

        viewModel.transferDetail.observe(viewLifecycleOwner) {
            if (it == null) {
                findNavController().navigateUp()
            } else {
                binding.transferViewModel = TransferDetailContentViewModel(it).apply {
                    onRemove = viewModel::remove
                }
                binding.executePendingBindings()
            }
        }

        viewModel.client.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.clientViewModel = ClientContentViewModel(it)
                binding.executePendingBindings()
            }
        }

        viewModel.state.observe(viewLifecycleOwner) {
            binding.stateViewModel = it
            binding.executePendingBindings()

            if (it.state is Task.State.Error) context?.let { context ->
                Snackbar.make(
                    binding.root,
                    CommonErrors.messageOf(context, it.state.error),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        viewModel.rejectionState.observe(viewLifecycleOwner) {
            binding.rejectButton.isEnabled = it == null
        }



        //Transfer Items list adapter and binding initialization
        val adapter = ItemAdapter { item, clickType ->
            when (clickType) {
                ItemAdapter.ClickType.Default -> viewModelTransferItems.open(requireContext(), item)
                ItemAdapter.ClickType.Recover -> viewModelTransferItems.recover(item)
            }
        }
        //val binding = LayoutTransferItemBinding.bind(view)
        val emptyContentViewModel = EmptyContentViewModel()



        //binding.emptyView.emptyView.viewModel = emptyContentViewModel
        binding.emptyView?.emptyText?.setText(R.string.empty_files_list)
        binding.emptyView?.emptyImage?.setImageResource(R.drawable.ic_insert_drive_file_white_24dp)
        binding.emptyView?.executePendingBindings()
        adapter.setHasStableIds(true)
        binding.recyclerView?.adapter = adapter

        viewModelTransferItems.items.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            emptyContentViewModel.with(binding.recyclerView, it.isNotEmpty())
        }



    }

    companion object {
        const val ACTION_TRANSFER_DETAIL = "org.khawaja.fileshare.client.android.action.TRANSFER_DETAIL"
    }




}


//Transfer Items Factory
/*class ItemViewModel @AssistedInject internal constructor(
    private val transferRepository: TransferRepository,
    @Assisted private val transfer: Transfer,
) : ViewModel() {
    val items = transferRepository.getTransferItems(transfer.id)

    fun open(context: Context, item: UTransferItem) {
        val uri = try {
            Uri.parse(item.location)
        } catch (e: Exception) {
            return
        }

        if (item.direction == Direction.Outgoing || item.state == TransferItem.State.Done) {
            try {
                Activities.view(context, DocumentFile.fromUri(context, uri))
            } catch (e: Exception) {
                Activities.view(context, uri, item.mimeType)
            }
        }
    }

    fun recover(item: UTransferItem) {
        if (item.state == TransferItem.State.InvalidatedTemporarily) {
            viewModelScope.launch {
                val originalState = item.state

                item.state = TransferItem.State.Pending
                transferRepository.update(item)

                // The list will not be refreshed if the DiffUtil finds the values are the same, so we give the
                // original value back (it will be refreshed anyway).
                item.state = originalState
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(transfer: Transfer): ItemViewModel
    }

    class ModelFactory(
        private val factory: Factory,
        private val transfer: Transfer,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            check(modelClass.isAssignableFrom(ItemViewModel::class.java)) {
                "Unknown type of view model was requested"
            }
            return factory.create(transfer) as T
        }
    }
}

class ItemContentViewModel(val transferItem: UTransferItem, context: Context) {
    val name = transferItem.name

    val size = Files.formatLength(transferItem.size, false)

    val mimeType = transferItem.mimeType

    val shouldRecover =
        transferItem.direction.isIncoming && transferItem.state == TransferItem.State.InvalidatedTemporarily

    val state = context.getString(
        when (transferItem.state) {
            TransferItem.State.InvalidatedTemporarily -> R.string.interrupted
            TransferItem.State.Invalidated -> R.string.removed
            TransferItem.State.Done -> R.string.completed
            else -> R.string.pending
        }
    )
}

class ItemViewHolder(
    private val clickListener: (item: UTransferItem, clickType: ItemAdapter.ClickType) -> Unit,
    private val binding: ListTransferItemBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(transferItem: UTransferItem) {
        binding.viewModel = ItemContentViewModel(transferItem, binding.root.context)
        binding.root.setOnClickListener {
            clickListener(transferItem, ItemAdapter.ClickType.Default)
        }
        binding.recoverButton.setOnClickListener {
            clickListener(transferItem, ItemAdapter.ClickType.Recover)
        }
        binding.executePendingBindings()
    }
}

class ItemCallback : DiffUtil.ItemCallback<UTransferItem>() {
    override fun areItemsTheSame(oldItem: UTransferItem, newItem: UTransferItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: UTransferItem, newItem: UTransferItem): Boolean {
        return oldItem.dateModified == newItem.dateModified && oldItem.state == newItem.state
    }
}

class ItemAdapter(
    private val clickListener: (item: UTransferItem, clickType: ClickType) -> Unit,
) : ListAdapter<UTransferItem, ItemViewHolder>(ItemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            clickListener,
            ListTransferItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_TRANSFER_ITEM
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).let { it.id + it.groupId }
    }

    enum class ClickType {
        Default,
        Recover,
    }

    companion object {
        const val VIEW_TYPE_TRANSFER_ITEM = 0
    }
}*/


