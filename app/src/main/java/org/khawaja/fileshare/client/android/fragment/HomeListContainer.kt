package org.khawaja.fileshare.client.android.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
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
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeListContainer.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeListContainer : Fragment() {


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




        val searchFilterEditText = view.findViewById<EditText>(R.id.searchFilter)
        val searchFilterButton = view.findViewById<ImageView>(R.id.searchFilterButton)
        val cardViewSearchFilter = view.findViewById<MaterialCardView>(R.id.cardViewSearchFilter)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val emptyView = LayoutEmptyContentBinding.bind(view.findViewById(R.id.emptyView))
        val gibSubscriberListener = { transferDetail: TransferDetail ->
            viewModel.subscribe(transferDetail)
        }





        val adapter = TransferHistoryAdapter(gibSubscriberListener) {
            when (it) {
                is TransferHistoryAdapter.Click.Local -> {
                    lifecycleScope.launch {
                        val transfer = viewModel.getTransfer(it.detail.id) ?: return@launch
                        when (it.clickType) {
                            TransferDetailViewHolder.ClickType.Default -> {
                                HomeActivity.backPressedCount = 1
                                findNavController().navigate(TransferHistoryFragmentDirections.actionTransferHistoryFragmentToNavTransferDetails(transfer))
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
                is TransferHistoryAdapter.Click.Text -> findNavController().navigate(
                    TransferHistoryFragmentDirections.actionTransferHistoryFragmentToNavTextEditor(
                        sharedText = it.text
                    )
                )
                is TransferHistoryAdapter.Click.Web -> findNavController().navigate(
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

        viewModel.transfers.observe(viewLifecycleOwner) {
            Log.d("submit_list_called:", "normal")
            adapter.submitList(it)
            emptyContentViewModel.with(recyclerView, it.isNotEmpty())
        }

        clientPickerViewModel.registerForTransferRequests(viewLifecycleOwner) { transfer, hasPin ->
            recyclerView.smoothScrollToPosition(0)
        }


        cardViewSearchFilter.visibility = View.VISIBLE

        var iteration =0

        searchFilterButton.setOnClickListener {


            viewModel.update(searchFilterEditText.text.toString(), viewLifecycleOwner)



            /*if(viewModel.transfersSearchFilterList.hasObservers()){
                Log.d("viewmodellist", "observer was running")
                viewModel.transfersSearchFilterList.removeObservers(viewLifecycleOwner)
                viewModel.transfersSearchFilterList.removeObserver { viewLifecycleOwner.lifecycleScope }


            }*/
            //viewModel.transfersSearchFilterList.removeObservers(viewLifecycleOwner)

                /*if(viewModel.transfersSearchFilterList.hasActiveObservers()){
                viewModel.transfersSearchFilterList.removeObserver{}
            //viewModel.transfersSearchFilterList.removeObserver{}
            }*/
            if (iteration ==0)
            {
                viewModel.transfersSearchFilterList.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                    emptyContentViewModel.with(recyclerView, it.isNotEmpty())
                }
                clientPickerViewModel.registerForTransferRequests(viewLifecycleOwner) { transfer, hasPin ->
                    recyclerView.smoothScrollToPosition(0)
                }

                ++iteration

            }
            else if(iteration==1){

                viewModel.transfersSearchFilterList1.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                    emptyContentViewModel.with(recyclerView, it.isNotEmpty())
                }
                clientPickerViewModel.registerForTransferRequests(viewLifecycleOwner) { transfer, hasPin ->
                    recyclerView.smoothScrollToPosition(0)
                }

                ++iteration

            }

            else if(iteration==2){

                viewModel.transfersSearchFilterList2.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                    emptyContentViewModel.with(recyclerView, it.isNotEmpty())
                }
                clientPickerViewModel.registerForTransferRequests(viewLifecycleOwner) { transfer, hasPin ->
                    recyclerView.smoothScrollToPosition(0)
                }

                ++iteration

            }

            else if(iteration==3){

                viewModel.transfersSearchFilterList3.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                    emptyContentViewModel.with(recyclerView, it.isNotEmpty())
                }
                clientPickerViewModel.registerForTransferRequests(viewLifecycleOwner) { transfer, hasPin ->
                    recyclerView.smoothScrollToPosition(0)
                }

                ++iteration

            }

            else if(iteration==4){

                viewModel.transfersSearchFilterList4.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                    emptyContentViewModel.with(recyclerView, it.isNotEmpty())
                }
                clientPickerViewModel.registerForTransferRequests(viewLifecycleOwner) { transfer, hasPin ->
                    recyclerView.smoothScrollToPosition(0)
                }

                ++iteration

            }
            else if(iteration==5){

                viewModel.transfersSearchFilterList5.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                    emptyContentViewModel.with(recyclerView, it.isNotEmpty())
                }
                clientPickerViewModel.registerForTransferRequests(viewLifecycleOwner) { transfer, hasPin ->
                    recyclerView.smoothScrollToPosition(0)
                }

                ++iteration

            }
            else if(iteration==6){

                viewModel.transfersSearchFilterList6.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                    emptyContentViewModel.with(recyclerView, it.isNotEmpty())
                }
                clientPickerViewModel.registerForTransferRequests(viewLifecycleOwner) { transfer, hasPin ->
                    recyclerView.smoothScrollToPosition(0)
                }

                ++iteration

            }
            else if(iteration==7){

                viewModel.transfersSearchFilterList7.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                    emptyContentViewModel.with(recyclerView, it.isNotEmpty())
                }
                clientPickerViewModel.registerForTransferRequests(viewLifecycleOwner) { transfer, hasPin ->
                    recyclerView.smoothScrollToPosition(0)
                }

                ++iteration

            }
            else if(iteration==8){

                viewModel.transfersSearchFilterList8.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                    emptyContentViewModel.with(recyclerView, it.isNotEmpty())
                }
                clientPickerViewModel.registerForTransferRequests(viewLifecycleOwner) { transfer, hasPin ->
                    recyclerView.smoothScrollToPosition(0)
                }

                ++iteration

            }
            else if(iteration==9){

                viewModel.transfersSearchFilterList9.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                    emptyContentViewModel.with(recyclerView, it.isNotEmpty())
                }
                clientPickerViewModel.registerForTransferRequests(viewLifecycleOwner) { transfer, hasPin ->
                    recyclerView.smoothScrollToPosition(0)
                }

                ++iteration

            }
            else if(iteration==10){

                viewModel.transfersSearchFilterList10.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                    emptyContentViewModel.with(recyclerView, it.isNotEmpty())
                }
                clientPickerViewModel.registerForTransferRequests(viewLifecycleOwner) { transfer, hasPin ->
                    recyclerView.smoothScrollToPosition(0)
                }

                ++iteration

            }
            else if(iteration==11){

                viewModel.transfersSearchFilterList11.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                    emptyContentViewModel.with(recyclerView, it.isNotEmpty())
                }
                clientPickerViewModel.registerForTransferRequests(viewLifecycleOwner) { transfer, hasPin ->
                    recyclerView.smoothScrollToPosition(0)
                }

                ++iteration

            }
            else if(iteration==12){

                viewModel.transfersSearchFilterList12.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                    emptyContentViewModel.with(recyclerView, it.isNotEmpty())
                }
                clientPickerViewModel.registerForTransferRequests(viewLifecycleOwner) { transfer, hasPin ->
                    recyclerView.smoothScrollToPosition(0)
                }

                ++iteration

            }
            else if(iteration==13){

                viewModel.transfersSearchFilterList13.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                    emptyContentViewModel.with(recyclerView, it.isNotEmpty())
                }
                clientPickerViewModel.registerForTransferRequests(viewLifecycleOwner) { transfer, hasPin ->
                    recyclerView.smoothScrollToPosition(0)
                }

                ++iteration

            }
            else if(iteration==14){

                viewModel.transfersSearchFilterList14.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                    emptyContentViewModel.with(recyclerView, it.isNotEmpty())
                }
                clientPickerViewModel.registerForTransferRequests(viewLifecycleOwner) { transfer, hasPin ->
                    recyclerView.smoothScrollToPosition(0)
                }

                ++iteration

            }
            else if(iteration==15){

                viewModel.transfersSearchFilterList15.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                    emptyContentViewModel.with(recyclerView, it.isNotEmpty())
                }
                clientPickerViewModel.registerForTransferRequests(viewLifecycleOwner) { transfer, hasPin ->
                    recyclerView.smoothScrollToPosition(0)
                }

                --iteration

            }




            //viewModel.transfersSearchFilterList.removeObservers(viewLifecycleOwner)
            //viewModel.transfersSearchFilterList.removeObserver(viewModel.transfersSearchFilterList)



        }

        /*searchFilterEditText.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {

                Log.d("viewmodellist", "after text chage")


                viewModel.update(s.toString())
                viewModel.transfersSearchFilterList.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                    emptyContentViewModel.with(recyclerView, it.isNotEmpty())
                }

                clientPickerViewModel.registerForTransferRequests(viewLifecycleOwner) { transfer, hasPin ->
                    recyclerView.smoothScrollToPosition(0)
                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                Log.d("viewmodellist", "before text  changed")

                *//*viewModel.update(s.toString())
                viewModel.transfersSearchFilterList.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                    emptyContentViewModel.with(recyclerView, it.isNotEmpty())
                }

                clientPickerViewModel.registerForTransferRequests(viewLifecycleOwner) { transfer, hasPin ->
                    recyclerView.smoothScrollToPosition(0)
                }*//*

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                Log.d("viewmodellist", "on text chage")


                *//*viewModel.update(s.toString())
                viewModel.transfersSearchFilterList.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                    emptyContentViewModel.with(recyclerView, it.isNotEmpty())
                }

                clientPickerViewModel.registerForTransferRequests(viewLifecycleOwner) { transfer, hasPin ->
                    recyclerView.smoothScrollToPosition(0)
                }*//*



            }

        })*/

        adapter.registerAdapterDataObserver(object :  RecyclerView.AdapterDataObserver(){

            override fun onChanged() {
                recyclerView.scrollToPosition(0)
            }
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                recyclerView.scrollToPosition(0)
            }
            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                recyclerView.scrollToPosition(0)
            }
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                recyclerView.scrollToPosition(0)
            }
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                recyclerView.scrollToPosition(0)
            }
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                recyclerView.scrollToPosition(0)
            }

        })




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


class TransferHistoryAdapter(
    private val gibSubscriberListener: (detail: TransferDetail) -> LiveData<TransferStateContentViewModel>,
    private val clickListener: (click: Click) -> Unit
) : ListAdapter<ListItem, RecyclerView.ViewHolder>(TransfersItemCallback()), Filterable {


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






}


class TransfersItemCallback : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem.listId == newItem.listId
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem == newItem
    }
}


