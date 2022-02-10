package org.khawaja.fileshare.client.android.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.activity.HomeActivity
import org.khawaja.fileshare.client.android.database.model.SharedText
import org.khawaja.fileshare.client.android.database.model.TransferDetail
import org.khawaja.fileshare.client.android.database.model.WebTransfer
import org.khawaja.fileshare.client.android.databinding.*
import org.khawaja.fileshare.client.android.model.DateSectionContentModel
import org.khawaja.fileshare.client.android.model.ListItem
import org.khawaja.fileshare.client.android.protocol.isIncoming
import org.khawaja.fileshare.client.android.viewholder.DateSectionViewHolder
import org.khawaja.fileshare.client.android.viewholder.SharedTextViewHolder
import org.khawaja.fileshare.client.android.viewholder.TransferDetailViewHolder
import org.khawaja.fileshare.client.android.viewholder.WebTransferViewHolder
import org.khawaja.fileshare.client.android.viewmodel.ClientPickerViewModel
import org.khawaja.fileshare.client.android.viewmodel.EmptyContentViewModel
import org.khawaja.fileshare.client.android.viewmodel.TransferManagerViewModel
import org.khawaja.fileshare.client.android.viewmodel.TransfersViewModel
import org.khawaja.fileshare.client.android.viewmodel.content.TransferStateContentViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeListContainer.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomePinnedContainer : Fragment() {


    /*private val clientPickerViewModel: ClientPickerViewModel by viewModels (
        ownerProducer = {requireParentFragment().childFragmentManager.primaryNavigationFragment!!}
    )

    private val managerViewModel: TransferManagerViewModel by viewModels (
        ownerProducer = {requireParentFragment().childFragmentManager.primaryNavigationFragment!!}
    )


    private val viewModelOne : ViewModelProviders

    private var viewModel: TransfersViewModel by viewModels (
        ownerProducer = {requireParentFragment().childFragmentManager.primaryNavigationFragment!!}
    )*/

    //private val viewModel by sharedView<TransfersViewModel>(from = { parentFragment?.parentFragment })








    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_list_container, container, false)
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        val viewModel = ViewModelProviders.of(requireActivity()).get(TransfersViewModel::class.java)

        val clientPickerViewModel = ViewModelProviders.of(requireActivity()).get(ClientPickerViewModel::class.java)

        val managerViewModel  = ViewModelProviders.of(requireActivity()).get(TransferManagerViewModel::class.java)




        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val emptyView = LayoutEmptyContentBinding.bind(view.findViewById(R.id.emptyView))
        val gibSubscriberListener = { transferDetail: TransferDetail ->
            viewModel.subscribe(transferDetail)
        }




        val adapter = TransferHistoryAdapterPinned(gibSubscriberListener) {
            when (it) {
                is TransferHistoryAdapterPinned.Click.Local -> {
                    lifecycleScope.launch {
                        val transfer = viewModel.getTransfer(it.detail.id) ?: return@launch
                        when (it.clickType) {
                            TransferDetailViewHolder.ClickType.Default -> {
                                HomeActivity.backPressedCount = 1
                                findNavController().navigate(
                                    TransferHistoryFragmentDirections.actionTransferHistoryFragmentToNavTransferDetails(
                                        transfer
                                    )
                                )
                            }
                            TransferDetailViewHolder.ClickType.Reject -> {
                                val client = viewModel.getClient(transfer.clientUid) ?: return@launch
                                managerViewModel.rejectTransferRequest(transfer, client)
                                //Toast.makeText(context, "rejected", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                val client = viewModel.getClient(transfer.clientUid) ?: return@launch
                                managerViewModel.toggleTransferOperation(transfer, client, it.detail)
                                //Toast.makeText(context, "accepted", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                is TransferHistoryAdapterPinned.Click.Text -> findNavController().navigate(
                    TransferHistoryFragmentDirections.actionTransferHistoryFragmentToNavTextEditor(
                        sharedText = it.text
                    )
                )
                is TransferHistoryAdapterPinned.Click.Web -> findNavController().navigate(
                    TransferHistoryFragmentDirections.actionTransferHistoryFragmentToWebTransferDetailsFragment(
                        it.transfer
                    )
                )
            }
        }





        val emptyContentViewModel = EmptyContentViewModel()

        emptyView.viewModel = emptyContentViewModel
        emptyView.emptyText.setText(R.string.empty_transfers_list)
        emptyView.emptyText.textSize = resources.getDimension(R.dimen.font_size_tiny)
        emptyView.emptyText.setTextColor(Color.BLACK)
        emptyView.emptyTextsub.setText(R.string.empty_transfer_list_sub)
        emptyView.emptyImage.setImageResource(R.drawable.ic_compare_arrows_white_24dp)
        emptyView.executePendingBindings()

        adapter.setHasStableIds(true)

        recyclerView.adapter = adapter;


        viewModel.transfersPinned.observe(viewLifecycleOwner) {
            Log.d("submit_list_called:", "normal")
            adapter.submitList(it)
            emptyContentViewModel.with(recyclerView, it.isNotEmpty())
        }

        clientPickerViewModel.registerForTransferRequests(viewLifecycleOwner) { transfer, hasPin ->
            recyclerView.smoothScrollToPosition(0)
        }




    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeListContainer.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeListContainer().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}




class TransferHistoryAdapterPinned(
    private val gibSubscriberListener: (detail: TransferDetail) -> LiveData<TransferStateContentViewModel>,
    private val clickListener: (click: Click) -> Unit
) : ListAdapter<ListItem, RecyclerView.ViewHolder>(TransfersItemCallbackPinned()), Filterable {


    private var list = mutableListOf<TransferDetail>()


    private val detailClick: (TransferDetail, TransferDetailViewHolder.ClickType) -> Unit = { detail, clickType ->
        clickListener(Click.Local(detail, clickType))
    }

    private val textClick: (SharedText) -> Unit = {
        clickListener(Click.Text(it))
    }

    private val webClick: (WebTransfer) -> Unit = {
        clickListener(Click.Web(it))
    }

    /*fun setData(list: List<ListItem>){
        this.listTemp = list.
        submitList(listTemp)
    }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {

            VIEW_TYPE_SECTION -> DateSectionViewHolder(
                ListSectionDateBinding.inflate(inflater, parent, false)
            )
            VIEW_TYPE_LOCAL_TRANSFER -> TransferDetailViewHolder(
                gibSubscriberListener, detailClick, ListTransferBinding.inflate(inflater, parent, false)
            )
            VIEW_TYPE_SHARED_TEXT -> SharedTextViewHolder(
                ListSharedTextBinding.inflate(inflater, parent, false),
                textClick,
            )
            VIEW_TYPE_WEB_TRANSFER -> WebTransferViewHolder(
                ListWebTransferBinding.inflate(inflater, parent, false),
                webClick,
            )
            else -> throw IllegalStateException()
        }
    }



    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)

        /*notifyItemRemoved(position)
        notifyItemRangeChanged(position,itemCount)*/
        when (holder) {
            is DateSectionViewHolder -> if (item is DateSectionContentModel) holder.bind(item)
            is TransferDetailViewHolder -> if (item is TransferDetail ) holder.bind(item)
            is SharedTextViewHolder -> if (item is SharedText) holder.bind(item)
            is WebTransferViewHolder -> if (item is WebTransfer) holder.bind(item)
            else -> throw IllegalStateException()
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder is TransferDetailViewHolder) {
            holder.onAppear()
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder is TransferDetailViewHolder) {
            holder.onDisappear()
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is TransferDetailViewHolder) {
            holder.onDestroy()
        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).listId
    }


    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DateSectionContentModel -> VIEW_TYPE_SECTION
            is TransferDetail -> VIEW_TYPE_LOCAL_TRANSFER
            //is SharedText -> VIEW_TYPE_SHARED_TEXT
            //is WebTransfer -> VIEW_TYPE_WEB_TRANSFER
            else -> throw IllegalStateException()
        }
    }

    /*override fun getItemViewType(position: Int): Int {
        val transferItem = list.get(position)

        *//*if(transferItem.direction.isIncoming){
            return
        }else{

        }*//*


       return when (transferItem.direction.isIncoming) {

            is DateSectionContentModel -> VIEW_TYPE_SECTION
            is TransferDetail -> VIEW_TYPE_LOCAL_TRANSFER
            is SharedText -> VIEW_TYPE_SHARED_TEXT
            is WebTransfer -> VIEW_TYPE_WEB_TRANSFER
            else -> throw IllegalStateException()
        }
    }*/



    sealed class Click {
        class Local(val detail: TransferDetail, val clickType: TransferDetailViewHolder.ClickType) : Click()

        class Text(val text: SharedText) : Click()

        class Web(val transfer: WebTransfer) : Click()
    }

    companion object {
        const val VIEW_TYPE_SECTION = 0

        const val VIEW_TYPE_LOCAL_TRANSFER = 1

        const val VIEW_TYPE_SHARED_TEXT = 2

        const val VIEW_TYPE_WEB_TRANSFER = 3
    }

    override fun getFilter(): Filter {

        return customFilter
    }


    private val customFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = mutableListOf<TransferDetail>()
            if (constraint == null || constraint.isEmpty()) {
                filteredList.addAll(list)
            } else {
                for (item in list) {
                    //if (item.toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                    if (item.direction.isIncoming) {
                        filteredList.add(item)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        /*override fun publishResults(constraint: CharSequence?, filterResults: FilterResults?) {
            submitList(filterResults?.values as ArrayList<ListItem>)
        }*/

        override fun publishResults(constraint: CharSequence?, filterResults: FilterResults?) {
            submitList(filterResults?.values as MutableList<TransferDetail> as List<ListItem>?)
        }

    }


    /*private val surahFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList: MutableList<Surah> = ArrayList<Surah>()
            if (constraint == null || constraint.length == 0) {
                filteredList.addAll(surahListFull)
            } else {
                val filterPattern = constraint.toString().toLowerCase().trim { it <= ' ' }
                for (item in surahListFull) {
                    if (item.getSurahName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            surahList.clear()
            surahList.addAll(results.values as List<*>)
            notifyDataSetChanged()
        }
    }*/



}


class TransfersItemCallbackPinned : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem.listId == newItem.listId
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem == newItem
    }
}


