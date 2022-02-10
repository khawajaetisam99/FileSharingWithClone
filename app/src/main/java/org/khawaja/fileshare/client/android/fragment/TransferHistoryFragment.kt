
package org.khawaja.fileshare.client.android.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.nfc.Tag
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.activity.ContentBrowserActivity
import org.khawaja.fileshare.client.android.database.model.SharedText
import org.khawaja.fileshare.client.android.database.model.TransferDetail
import org.khawaja.fileshare.client.android.database.model.WebTransfer
import org.khawaja.fileshare.client.android.model.DateSectionContentModel
import org.khawaja.fileshare.client.android.model.ListItem
import org.khawaja.fileshare.client.android.viewmodel.content.TransferStateContentViewModel
import android.os.Environment

import android.os.StatFs
import android.util.Log
import android.view.Window
import android.view.animation.AnimationUtils
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.*
import android.widget.Filter.FilterResults
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.*
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.withContext
import org.khawaja.fileshare.client.android.NavHomeDirections
import org.khawaja.fileshare.client.android.activity.HomeActivity
//import org.khawaja.fileshare.client.android.databinding.*
import org.khawaja.fileshare.client.android.protocol.isIncoming
import org.khawaja.fileshare.client.android.util.CommonErrors
import org.khawaja.fileshare.client.android.viewholder.*
import org.khawaja.fileshare.client.android.viewmodel.*
import org.khawaja.fileshare.client.android.viewmodel.content.SenderClientContentViewModel
import org.monora.uprotocol.core.protocol.Direction
import java.io.File
import java.lang.StringBuilder
import java.util.ArrayList

import org.khawaja.fileshare.client.android.databinding.LayoutTransferHistoryBinding


@AndroidEntryPoint
class TransferHistoryFragment : Fragment(R.layout.layout_transfer_history) {

    /*private val clientPickerViewModel: ClientPickerViewModel by viewModels()

    private val managerViewModel: TransferManagerViewModel by viewModels()

    private val viewModel: TransfersViewModel by viewModels()*/

    // Receive Files view Models initiallization.

    private val clientPickerViewModel: ClientPickerViewModel by activityViewModels()

    private val receiverViewModel: ReceiverViewModel by viewModels()



    override fun onResume() {
        super.onResume()

        HomeActivity.backPressedCount = 0
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = LayoutTransferHistoryBinding.bind(view)




        val stat = StatFs(Environment.getExternalStorageDirectory().path)

        val bytesAvailable = stat.blockSize.toLong() * stat.blockCount.toLong()
        val megAvailable = bytesAvailable / 1048576

        println("Megs: $megAvailable")




        val storageDifferenceTextview = view.findViewById< TextView>(R.id.storageDifference)
        storageDifferenceTextview.setText((getTotalInternalMemorySize() - getAvailableInternalMemorySize()).toString() + " GB" + " of " +getTotalInternalMemorySize().toString()+ " GB")
        var storageIndicatorProgressbar = view.findViewById<LinearProgressIndicator>(R.id.storageprogressbar)

        //calculate and display
        var calculatateStoragePercentage = ((getTotalInternalMemorySize() - getAvailableInternalMemorySize())?.toFloat()?.div(getTotalInternalMemorySize()))
        var calculatateStoragePercentagefinal = calculatateStoragePercentage?.times(100)
        storageIndicatorProgressbar.max = 100
        if (calculatateStoragePercentagefinal != null) {
            storageIndicatorProgressbar.progress = calculatateStoragePercentagefinal.toInt()
        }

        var storagePercentageTextView = view.findViewById<TextView>(R.id.storagePercentage)
        storagePercentageTextView.setText("(" +calculatateStoragePercentagefinal?.toInt().toString() +"% used)")


        Log.e("AvailableMB", "Available MB : $megAvailable"
                + " availableInternal" + getAvailableInternalMemorySize()
                + "Totalinternal" + getTotalInternalMemorySize()
                + "percentage  : " + calculatateStoragePercentage.toString()
                + "percentage  : " + calculatateStoragePercentagefinal)



        view.findViewById<View>(R.id.sendButton).setOnClickListener {
            var intent = Intent(it.context, ContentBrowserActivity::class.java)
            intent.putExtra("fileSelection", 1)
            startActivity(intent)
            /*startActivity(Intent(it.context, ContentBrowserActivity::class.java))*/
        }
        view.findViewById<View>(R.id.receiveButton).setOnClickListener {
            HomeActivity.backPressedCount =1

           /* val dialog = Dialog(view.context )
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.custom_barcode_scan_dialog)
            //val body = dialog.findViewById(R.id.body) as TextView
            //body.text = title
            val codeScannerImageView = dialog.findViewById(R.id.barcode_image_layout) as LinearLayout
            codeScannerImageView.setOnClickListener {

                findNavController().navigate(
                    TransferHistoryFragmentDirections.actionOptionsFragmentToBarcodeScannerFragment()
                )
                dialog.dismiss()

            }
            val btnDelete = dialog.findViewById(R.id.btnDelete) as MaterialButton
            val btncancel = dialog.findViewById(R.id.btnCancel) as MaterialButton
            btncancel.setOnClickListener { dialog.dismiss() }
            dialog.show()*/

            /*findNavController().navigate(
                TransferHistoryFragmentDirections.actionOptionsFragmentToBarcodeScannerFragment()
            )*/

            findNavController().navigate(TransferHistoryFragmentDirections.pickClient())


        //findNavController().navigate(TransferHistoryFragmentDirections.actionTransferHistoryFragmentToNavReceive())
        }

        view.findViewById<View>(R.id.imagesButtonHomeScreen).setOnClickListener {
            var intent = Intent(it.context, ContentBrowserActivity::class.java)
            intent.putExtra("fileSelection", 1)
            startActivity(intent)
        }
        view.findViewById<View>(R.id.videosButtonHomeScreen).setOnClickListener {
            var intent = Intent(it.context, ContentBrowserActivity::class.java)
            intent.putExtra("fileSelection", 2)
            startActivity(intent)
        }
        view.findViewById<View>(R.id.appsButtonHomeScreen).setOnClickListener {
            var intent = Intent(it.context, ContentBrowserActivity::class.java)
            intent.putExtra("fileSelection", 3)
            startActivity(intent)
        }
        view.findViewById<View>(R.id.documentsButtonHomeScreen).setOnClickListener {
            var intent = Intent(it.context, ContentBrowserActivity::class.java)
            intent.putExtra("fileSelection", 4)
            startActivity(intent)
        }

        //Bottom navigation bar Item Click listeners
        view.findViewById<View>(R.id.bottomBarFirstIcon).setOnClickListener {
            view.findViewById<View>(R.id.bottomBarIconOne).background = resources.getDrawable(R.drawable.bottom_bar_icon_one_blue)
            view.findViewById<View>(R.id.bottomBarIconTwo).background = resources.getDrawable(R.drawable.bottom_bar_icon_two_grey)
            view.findViewById<View>(R.id.bottomBarIconThree).background = resources.getDrawable(R.drawable.bottom_bar_icon_three_grey)
            var textViewOne : TextView = view.findViewById(R.id.bottomBarTextOne)
            textViewOne.setTextColor(resources.getColor(R.color.colorPrimary))
            var textViewTwo : TextView = view.findViewById(R.id.bottomBarTextTwo)
            textViewTwo.setTextColor(resources.getColor(R.color.colorOnPrimary))
            var textViewThree : TextView = view.findViewById(R.id.bottomBarTextThree)
            textViewThree.setTextColor(resources.getColor(R.color.colorOnPrimary))
            var textRecentFiles : MaterialTextView = view.findViewById(R.id.recentFiles)
            textRecentFiles.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorPrimary))
            var textPinnedFiles : MaterialTextView = view.findViewById(R.id.pinnedFiles)
            textPinnedFiles.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorOnPrimary))



            val homeFragmentAllList: Fragment = HomeListContainer()
            val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.listViewFragmentContainer, homeFragmentAllList).commit()


        }
        view.findViewById<View>(R.id.bottomBarSecondIcon).setOnClickListener {
            view.findViewById<View>(R.id.bottomBarIconOne).background = resources.getDrawable(R.drawable.bottom_bar_icon_one_grey)
            view.findViewById<View>(R.id.bottomBarIconTwo).background = resources.getDrawable(R.drawable.bottom_bar_icon_two_blue)
            view.findViewById<View>(R.id.bottomBarIconThree).background = resources.getDrawable(R.drawable.bottom_bar_icon_three_grey)

            var textViewOne : TextView = view.findViewById(R.id.bottomBarTextOne)
            textViewOne.setTextColor(resources.getColor(R.color.colorOnPrimary))
            var textViewTwo : TextView = view.findViewById(R.id.bottomBarTextTwo)
            textViewTwo.setTextColor(resources.getColor(R.color.colorPrimary))
            var textViewThree : TextView = view.findViewById(R.id.bottomBarTextThree)
            textViewThree.setTextColor(resources.getColor(R.color.colorOnPrimary))
            var textRecentFiles : MaterialTextView = view.findViewById(R.id.recentFiles)
            textRecentFiles.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorOnPrimary))
            var textPinnedFiles : MaterialTextView = view.findViewById(R.id.pinnedFiles)
            textPinnedFiles.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorOnPrimary))



            val homeFragmentAllList: Fragment = OutgoingListContainer()
            val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.listViewFragmentContainer, homeFragmentAllList).commit()



        }
        view. findViewById<View>(R.id.bottomBarThirdIcon).setOnClickListener {
            view.findViewById<View>(R.id.bottomBarIconOne).background = resources.getDrawable(R.drawable.bottom_bar_icon_one_grey,null)
            view.findViewById<View>(R.id.bottomBarIconTwo).background = resources.getDrawable(R.drawable.bottom_bar_icon_two_grey,null)
            view.findViewById<View>(R.id.bottomBarIconThree).background = resources.getDrawable(R.drawable.bottom_bar_icon_three_blue,null)

            var textViewOne : TextView = view.findViewById(R.id.bottomBarTextOne)
            textViewOne.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorOnPrimary))
            var textViewTwo : TextView = view.findViewById(R.id.bottomBarTextTwo)
            textViewTwo.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorOnPrimary))
            var textViewThree : TextView = view.findViewById(R.id.bottomBarTextThree)
            textViewThree.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorPrimary))
            var textRecentFiles : MaterialTextView = view.findViewById(R.id.recentFiles)
            textRecentFiles.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorOnPrimary))
            var textPinnedFiles : MaterialTextView = view.findViewById(R.id.pinnedFiles)
            textPinnedFiles.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorOnPrimary))



            val homeFragmentAllList: Fragment = IncomingListContainer()
            val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.listViewFragmentContainer, homeFragmentAllList).commit()


        }



        view.findViewById<View>(R.id.recentFiles).setOnClickListener{

            view.findViewById<View>(R.id.bottomBarIconOne).background = resources.getDrawable(R.drawable.bottom_bar_icon_one_blue,null)
            view.findViewById<View>(R.id.bottomBarIconTwo).background = resources.getDrawable(R.drawable.bottom_bar_icon_two_grey,null)
            view.findViewById<View>(R.id.bottomBarIconThree).background = resources.getDrawable(R.drawable.bottom_bar_icon_three_grey,null)

            var textViewOne : TextView = view.findViewById(R.id.bottomBarTextOne)
            textViewOne.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorOnPrimary))
            var textViewTwo : TextView = view.findViewById(R.id.bottomBarTextTwo)
            textViewTwo.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorOnPrimary))
            var textViewThree : TextView = view.findViewById(R.id.bottomBarTextThree)
            textViewThree.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorOnPrimary))
            var textRecentFiles : MaterialTextView = view.findViewById(R.id.recentFiles)
            textRecentFiles.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorPrimary))
            var textPinnedFiles : MaterialTextView = view.findViewById(R.id.pinnedFiles)
            textPinnedFiles.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorOnPrimary))



            val homeFragmentAllList: Fragment = HomeListContainer()
            val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.listViewFragmentContainer, homeFragmentAllList).commit()

        }


        view.findViewById<View>(R.id.pinnedFiles).setOnClickListener{

            view.findViewById<View>(R.id.bottomBarIconOne).background = resources.getDrawable(R.drawable.bottom_bar_icon_one_grey,null)
            view.findViewById<View>(R.id.bottomBarIconTwo).background = resources.getDrawable(R.drawable.bottom_bar_icon_two_grey,null)
            view.findViewById<View>(R.id.bottomBarIconThree).background = resources.getDrawable(R.drawable.bottom_bar_icon_three_grey,null)

            var textViewOne : TextView = view.findViewById(R.id.bottomBarTextOne)
            textViewOne.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorOnPrimary))
            var textViewTwo : TextView = view.findViewById(R.id.bottomBarTextTwo)
            textViewTwo.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorOnPrimary))
            var textViewThree : TextView = view.findViewById(R.id.bottomBarTextThree)
            textViewThree.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorOnPrimary))
            var textRecentFiles : MaterialTextView = view.findViewById(R.id.recentFiles)
            textRecentFiles.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorOnPrimary))
            var textPinnedFiles : MaterialTextView = view.findViewById(R.id.pinnedFiles)
            textPinnedFiles.setTextColor(ContextCompat.getColor(requireActivity(),R.color.colorPrimary))



            val homeFragmentAllList: Fragment = HomePinnedContainer()
            val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.listViewFragmentContainer, homeFragmentAllList).commit()

        }

        view.findViewById<View>(R.id.scrollUpButton).setOnClickListener{

            val homeFragmentAllList: Fragment = HomeListContainer()
            val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.listViewFragmentContainer, homeFragmentAllList).commit()

        }



        val homeFragmentAllList: Fragment = HomeListContainer()
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.listViewFragmentContainer, homeFragmentAllList).commit()

        /*viewModel.transfers.observe(viewLifecycleOwner) {
            Log.d("submit_list_called:", "normal")
            adapter.submitList(it)
            emptyContentViewModel.with(recyclerView, it.isNotEmpty())
        }

        clientPickerViewModel.registerForTransferRequests(viewLifecycleOwner) { transfer, hasPin ->
            recyclerView.smoothScrollToPosition(0)
        }*/





        ///////////////////////////////////////////////////////////////////////



        //receive fragment settings
        //////////////////////////////////////////////////////////////////////////////////
        clientPickerViewModel.bridge.observe(viewLifecycleOwner) { bridge ->
            receiverViewModel.consume(bridge)
        }

        clientPickerViewModel.registerForTransferRequests(viewLifecycleOwner) { transfer, _ ->
            findNavController().navigate(
                NavHomeDirections.actionGlobalNavTransferDetails(transfer)
                //ReceiveFragmentDirections.actionReceiveFragmentToNavTransferDetails(transfer)
            )
        }

        receiverViewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                is GuidanceRequestState.InProgress -> {
                    //binding.statusText.text = getString(R.string.starting)
                }
                is GuidanceRequestState.Success -> {
                    //binding.statusText.text = getString(R.string.sender_accepted)
                }
                is GuidanceRequestState.Finishing -> {
                    //binding.statusText.text = getString(R.string.sender_finishing)
                    //Toast.makeText(context, "Successfully Paired", Toast.LENGTH_SHORT).show()
                }
                is GuidanceRequestState.Error -> {
                    //Toast.makeText(context, "Successfully Paired", Toast.LENGTH_SHORT).show()
                    if (it.exception is NotExpectingException)
                    {
                        //binding.statusText.text = getString(R.string.sender_not_expecting, it.client?.clientNickname)
                    } else  {
                        CommonErrors.messageOf(requireContext(), it.exception)
                    }
                }
            }

            if (it.isInProgress) {
                //binding.progressBar.show()
            } else {
                //binding.progressBar.hide()
            }

            val isError = it is GuidanceRequestState.Error
            val alpha = if (isError) 0.5f else 1.0f
            //binding.image.alpha = alpha
            //binding.text.isEnabled = !isError
            //binding.warningIcon.visibility = if(isError) View.VISIBLE else View.GONE
            //binding.button.isEnabled = !it.isInProgress
            binding.viewModel = SenderClientContentViewModel(it.client)

            binding.executePendingBindings()
        }

        //////////////////////////////////////////////////////////////////////////////////


    }




    fun getAvailableInternalMemorySize(): Long {
        val path: File = Environment.getDataDirectory()
        val stat = StatFs(path.getPath())
        val blockSize = stat.blockSizeLong
        val availableBlocks = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            stat.availableBlocksLong
        } else {
            TODO("VERSION.SDK_INT < JELLY_BEAN_MR2")
        }
        return formatSize(availableBlocks * blockSize)
    }

    fun getTotalInternalMemorySize(): Long {
        val path: File = Environment.getDataDirectory()
        val stat = StatFs(path.getPath())
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        return formatSize(totalBlocks * blockSize)
    }


    fun formatSize(size: Long): Long {
        var size = size
        var suffix: String? = null
        if (size >= 1024) {
            suffix = "KB"
            size /= 1024
            if (size >= 1024) {
                suffix = ""
                size /= (1024 *1024)
            }
        }
        val resultBuffer = (size+2) //StringBuilder(java.lang.Long.toString(size+2))
        /*var commaOffset = resultBuffer.length - 3
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',')
            commaOffset -= 3
        }
        if (suffix != null) resultBuffer.append(suffix)*/
        return resultBuffer  //.toString()
    }

}

fun Fragment.getFragmentNavController(@IdRes id: Int) = activity?.let {
    return@let Navigation.findNavController(it, id)
}







