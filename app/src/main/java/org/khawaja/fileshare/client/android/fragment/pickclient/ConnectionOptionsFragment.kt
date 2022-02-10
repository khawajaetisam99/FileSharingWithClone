package org.khawaja.fileshare.client.android.fragment.pickclient

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.AnimationUtils.loadAnimation
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import dagger.hilt.android.AndroidEntryPoint
import org.khawaja.fileshare.client.android.NavPickClientDirections
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.databinding.LayoutConnectionOptionsBinding
import org.khawaja.fileshare.client.android.databinding.ListClientGridBinding
import org.khawaja.fileshare.client.android.model.ClientRoute
import org.khawaja.fileshare.client.android.viewholder.ClientGridViewHolder
import org.khawaja.fileshare.client.android.viewmodel.ClientPickerViewModel
import org.khawaja.fileshare.client.android.viewmodel.ClientsViewModel
import org.khawaja.fileshare.client.android.viewmodel.EmptyContentViewModel
import org.khawaja.fileshare.client.android.viewmodel.content.ClientContentViewModel
import org.monora.uprotocol.core.protocol.Direction

@AndroidEntryPoint
class ConnectionOptionsFragment : Fragment(R.layout.layout_connection_options) {
    private val args: ConnectionOptionsFragmentArgs by navArgs()

    private val clientsViewModel: ClientsViewModel by viewModels()

    private val clientPickerViewModel: ClientPickerViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = LayoutConnectionOptionsBinding.bind(view)
        val adapter = OnlineClientsAdapter { clientRoute, clickType ->
            when (clickType) {
                ClientContentViewModel.ClickType.Default -> findNavController().navigate(
                    ConnectionOptionsFragmentDirections.actionOptionsFragmentToClientConnectionFragment(
                        clientRoute.client, clientRoute.address
                    )
                )
                /*ClientContentViewModel.ClickType.Details -> findNavController().navigate(
                    ConnectionOptionsFragmentDirections.actionOptionsFragmentToClientDetailsFragment(clientRoute.client)
                )*/
            }
        }
        val emptyContentViewModel = EmptyContentViewModel()

        adapter.setHasStableIds(true)
        binding.emptyView.emptyText.setText(R.string.searching_for_devices)
        //binding.emptyView.emptyText.animation = loadAnimation(context, R.anim.fadeinfadeoutinfinite)
        binding.emptyView.emptyText.setTextColor(Color.BLACK)
        binding.emptyView.emptyTextsub.setText(R.string.empty_online_list_sub)
        binding.emptyView.emptyImage.setImageResource(R.drawable.ic_compare_arrows_white_24dp)
        binding.emptyView.viewModel = emptyContentViewModel
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager?.let {
            if (it is GridLayoutManager) {
                it.orientation = GridLayoutManager.VERTICAL
            }
        }
        binding.senderNoticeViews.visibility = if (args.direction == Direction.Outgoing) View.VISIBLE else View.GONE

        binding.clickListener = View.OnClickListener { v: View ->
            when (v.id) {
                R.id.generateQrCodeButton -> findNavController().navigate(
                    ConnectionOptionsFragmentDirections.actionOptionsFragmentToNetworkManagerFragment(args.direction)
                )
                R.id.scanQrCodeButton -> findNavController().navigate(
                    ConnectionOptionsFragmentDirections.actionOptionsFragmentToBarcodeScannerFragment()
                )

                /*R.id.clientsButton -> findNavController().navigate(
                    ConnectionOptionsFragmentDirections.actionOptionsFragmentToClientsFragment()
                )
                R.id.generateQrCodeButton -> findNavController().navigate(
                    ConnectionOptionsFragmentDirections.actionOptionsFragmentToNetworkManagerFragment(args.direction)
                )
                R.id.manualAddressButton -> findNavController().navigate(
                    ConnectionOptionsFragmentDirections.actionOptionsFragmentToManualConnectionFragment()
                )
                R.id.scanQrCodeButton -> findNavController().navigate(
                    ConnectionOptionsFragmentDirections.actionOptionsFragmentToBarcodeScannerFragment()
                )*/
            }
        }

        binding.executePendingBindings()

        clientsViewModel.onlineClients.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            emptyContentViewModel.with(binding.recyclerView, it.isNotEmpty())
        }

        clientPickerViewModel.registerForGuidanceRequests(viewLifecycleOwner, args.direction) { bridge ->
            clientPickerViewModel.bridge.postValue(bridge)
            findNavController().navigate(NavPickClientDirections.xmlPop())
        }

        clientPickerViewModel.registerForTransferRequests(viewLifecycleOwner) { transfer, _ ->
            if (args.direction == Direction.Incoming) {
                findNavController().navigate(
                    ConnectionOptionsFragmentDirections.actionOptionsFragmentToNavTransferDetails(transfer)
                )
            }
        }
    }
}

class OnlineClientsAdapter(
    private val clickListener: (ClientRoute, ClientContentViewModel.ClickType) -> Unit
) : ListAdapter<ClientRoute, ClientGridViewHolder>(ClientRouteItemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientGridViewHolder {
        return ClientGridViewHolder(
            ListClientGridBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            clickListener
        )
    }

    override fun onBindViewHolder(holder: ClientGridViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).client.uid.hashCode().toLong()
    }
}

class ClientRouteItemCallback : DiffUtil.ItemCallback<ClientRoute>() {
    override fun areItemsTheSame(oldItem: ClientRoute, newItem: ClientRoute): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: ClientRoute, newItem: ClientRoute): Boolean {
        return oldItem == newItem
    }
}
