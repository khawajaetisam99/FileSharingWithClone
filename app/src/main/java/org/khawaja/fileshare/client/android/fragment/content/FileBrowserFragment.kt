
package org.khawaja.fileshare.client.android.fragment.content

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.provider.Settings
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.isOreoPlus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.adapter.FileAdapter
import org.khawaja.fileshare.client.android.databinding.LayoutContentBrowserBinding
import org.khawaja.fileshare.client.android.databinding.LayoutEmptyContentBinding
import org.khawaja.fileshare.client.android.databinding.LayoutFileBrowserBinding

import org.khawaja.fileshare.client.android.databinding.ListPathBinding
import org.khawaja.fileshare.client.android.filemanagerstats.extensions.formatSizeThousand
import org.khawaja.fileshare.client.android.filemanagerstats.helpers.*
import org.khawaja.fileshare.client.android.fragment.ContentBrowserFragment
import org.khawaja.fileshare.client.android.fragment.ContentFragmentStateAdapter
import org.khawaja.fileshare.client.android.model.FileModel
import org.khawaja.fileshare.client.android.util.Activities
import org.khawaja.fileshare.client.android.viewmodel.EmptyContentViewModel
import org.khawaja.fileshare.client.android.viewmodel.FilesViewModel
import org.khawaja.fileshare.client.android.viewmodel.SharingSelectionViewModel
import java.io.File
import java.util.*
import kotlin.collections.HashMap

@AndroidEntryPoint
class FileBrowserFragment : Fragment(R.layout.layout_file_browser) {

    private val SIZE_DIVIDER = 100000
    @TargetApi(19)
    private val addAccess = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        val context = context

        if (uri != null && context != null && Build.VERSION.SDK_INT >= 21) {
            viewModel.insertSafFolder(uri)
        }
    }

    private val viewModel: FilesViewModel by viewModels()

    private val selectionViewModel: SharingSelectionViewModel by activityViewModels()

    private lateinit var pathsPopupMenu: PopupMenu

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        private var afterPopup = false

        override fun handleOnBackPressed() {
            if (viewModel.goUp()) {
                afterPopup = false
            } else if (afterPopup) {
                isEnabled = false
                activity?.onBackPressedDispatcher?.onBackPressed()
            } else {
                afterPopup = true
                pathsPopupMenu.show()
            }
        }
    }


    protected val defaultPreferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(activity)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //val binding = LayoutFileBrowserBinding.bind(view)

        //val binding = LayoutFileBrowserBinding.bind(view)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val emptyView = LayoutEmptyContentBinding.bind(view.findViewById(R.id.emptyView))

        val pbLoading = view.findViewById<ProgressBar>(R.id.insidePBLoading)

        pbLoading.visibility = View.VISIBLE


        val includedLayout = view.findViewById<View>(R.id.cloneLayoutAccess)
        val floatingViewsContainer = view.findViewById<CoordinatorLayout>(R.id.floatingViewsContainer)
        val adapter = FileAdapter { fileModel, clickType ->
            when (clickType) {
                FileAdapter.ClickType.Default -> {

                    Log.d("set_selected_file", "set_selected_onclick")
                    if (fileModel.file.isDirectory()) {
                        viewModel.requestPath(fileModel.file)
                    } else {
                        Activities.view(view.context, fileModel.file)
                    }
                }

                FileAdapter.ClickType.ToggleSelect -> {
                    Log.d("set_selected_file", "set selected true")
                    selectionViewModel.setSelected(fileModel, fileModel.isSelected)
                    //Log.d("Child_count", recyclerView.adapter?.itemCount.toString())
                }
            }
            //Log.d("set_selected", "set selected true")
            Log.d("child_count", "file adapter iteratedinfragment")
        }

        Log.d("child_count_adapter", adapter.itemCount.toString())








        /*var count = recyclerView.childCount
        Log.d("Child_count", count.toString())*/

        /*var count = recyclerView.childCount
        for (i in 0 until count) {

            var adapter = FileAdapter
            *//*val view: View = recyclerView.getChildAt(i)
            if (!view.isActivated) {
                view.isActivated = true
                selection.add(view)
            }*//*
        }*/


        val emptyContentViewModel = EmptyContentViewModel()
        val pathRecyclerView = view.findViewById<RecyclerView>(R.id.pathRecyclerView)
        val pathRecylerViewContainer = view.findViewById<View>(R.id.pathRecyclerViewContainer)
        val pathSelectorButton = view.findViewById<View>(R.id.pathSelectorButton)
        //pathSelectorButton.animation = loadAnimation(context, R.anim.fadeoutfadeinaniminfinite)
        val pathAdapter = PathAdapter {
            viewModel.requestPath(it.file)
        }
        val safAddedSnackbar = Snackbar.make(floatingViewsContainer, R.string.add_success, Snackbar.LENGTH_LONG)

        pathsPopupMenu = PopupMenu(requireContext(), pathSelectorButton).apply {
            MenuCompat.setGroupDividerEnabled(menu, true)
        }
        pathSelectorButton.setOnClickListener {
            if (Build.VERSION.SDK_INT < 21) {
                viewModel.requestStorageFolder()
                return@setOnClickListener
            }

            pathsPopupMenu.show()
        }

        emptyView.viewModel = emptyContentViewModel
        emptyView.emptyText.setText(R.string.empty_files_list)
        emptyView.emptyImage.setImageResource(R.drawable.ic_insert_drive_file_white_24dp)
        emptyView.executePendingBindings()
        adapter.setHasStableIds(true)
        recyclerView.adapter = adapter
        pathAdapter.setHasStableIds(true)
        pathRecyclerView.adapter = pathAdapter

        pathAdapter.registerAdapterDataObserver(
            object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    pathRecyclerView.scrollToPosition(pathAdapter.itemCount - 1)
                }
            }
        )


        recyclerView.adapter?.itemCount.toString()

        var count = recyclerView.childCount

        Log.d("child_count", recyclerView.adapter?.itemCount.toString())
        /*for (i in 0 until count) {

            var adapter = FileAdapter
            val view: View = recyclerView.getChildAt(i)
            if (!view.isActivated) {
                view.isActivated = true
                //selection.add(view)
            }
        }*/



        viewModel.files.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            pbLoading.visibility = View.INVISIBLE
            Log.d("folderfiles_in_list", adapter.itemCount.toString())
            emptyContentViewModel.with(recyclerView, it.isNotEmpty())
        }

        viewModel.pathTree.observe(viewLifecycleOwner) {
            pathAdapter.submitList(it)
        }

        viewModel.safFolders.observe(viewLifecycleOwner) {

            pathsPopupMenu.menu.clear()
            pathsPopupMenu.setOnMenuItemClickListener { menuItem ->
                if (menuItem.itemId == R.id.storage_folder) {
                    viewModel.requestStorageFolder()
                } else if (menuItem.itemId == R.id.default_storage_folder) {
                    viewModel.requestDefaultStorageFolder()
                } else if (menuItem.groupId == R.id.locations_custom) {
                    viewModel.requestPath(it[menuItem.itemId])
                } else if (menuItem.itemId == R.id.add_storage) {
                    addAccess.launch(null)
                } else if (menuItem.itemId == R.id.clear_storage_list) {
                    viewModel.clearStorageList()
                } else {
                    return@setOnMenuItemClickListener false
                }

                return@setOnMenuItemClickListener true
            }
            pathsPopupMenu.inflate(R.menu.file_browser)
            pathsPopupMenu.menu.findItem(R.id.storage_folder).isVisible = viewModel.isCustomStorageFolder
            pathsPopupMenu.menu.findItem(R.id.clear_storage_list).isVisible = it.isNotEmpty()
            it.forEachIndexed { index, safFolder ->
                pathsPopupMenu.menu.add(R.id.locations_custom, index, Menu.NONE, safFolder.name).apply {
                    setIcon(R.drawable.ic_save_white_24dp)
                }
            }
        }

        selectionViewModel.externalState.observe(viewLifecycleOwner) {

            adapter.notifyDataSetChanged()
            Log.d("folderfiles_in_list", adapter.itemCount.toString())
        }

        viewModel.safAdded.observe(viewLifecycleOwner) {
            viewModel.requestPath(it)
            safAddedSnackbar.show()
        }






        ///////////////////////////////////////////////////////////////////////////////////////////

        val cloneActive = defaultPreferences.getBoolean("clone_active", false)

        if(cloneActive== false)
        {
            includedLayout.visibility = View.GONE
            pathRecylerViewContainer.visibility =View.VISIBLE
        }
        else{
            includedLayout.visibility  = View.GONE
            pathRecylerViewContainer.visibility = View.GONE
        }

        view.findViewById<TextView>(R.id.total_space).text = String.format(getString(R.string.total_storage), "…")



        /*val childFragment: MyViewPagerFragment = StorageFragment( )
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.child_fragment_container, childFragment).commit()*/


        // Mime types progresses click listeners
        view.findViewById<RelativeLayout>(R.id.free_space_holder).setOnClickListener {
            try {
                val storageSettingsIntent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
                startActivity(storageSettingsIntent)
            } catch (e: Exception) {
                Log.e("Storage settings error", "onViewCreated: storage setting error")
            }
        }

        //images_holder.setOnClickListener { launchMimetypeActivity(IMAGES) }
        //videos_holder.setOnClickListener { launchMimetypeActivity(VIDEOS) }
        //audio_holder.setOnClickListener { launchMimetypeActivity(AUDIO) }
        //documents_holder.setOnClickListener { launchMimetypeActivity(DOCUMENTS) }
        //archives_holder.setOnClickListener { launchMimetypeActivity(ARCHIVES) }
        //others_holder.setOnClickListener { launchMimetypeActivity(OTHERS) }


        Log.d("_filesfiles" , "_hit_")

        GlobalScope.launch{



            getMainStorageStats(view.context, view)

            /*val filesSize = getSizesByMimeType(view)
            val imagesSize = filesSize[IMAGES]!!
            val videosSize = filesSize[VIDEOS]!!
            val audioSize = filesSize[AUDIO]!!
            val documentsSize = filesSize[DOCUMENTS]!!
            val archivesSize = filesSize[ARCHIVES]!!
            val othersSize = filesSize[OTHERS]!!*/


            //val filesSize = ContentBrowserFragment.filesSizeCompanion
            val imagesSize = ContentBrowserFragment.imagesSizeCompanion
            val videosSize = ContentBrowserFragment.videosSizeCompanion
            val audioSize = ContentBrowserFragment.audioSizeCompanion
            val documentsSize = ContentBrowserFragment.documentsSizeCompanion
            val archivesSize = ContentBrowserFragment.archivesSizeCompanion
            val othersSize = ContentBrowserFragment.othersSizeCompanion

            Log.d("_filesfiles" , "_filesfiles"  + imagesSize + videosSize + audioSize + documentsSize + archivesSize + othersSize)

            /*post {

            }*/
            Handler(Looper.getMainLooper()).post {



                view.findViewById<TextView>(R.id.images_size).text = imagesSize.formatSize()
                view.findViewById<LinearProgressIndicator>(R.id.images_progressbar).progress = (imagesSize / SIZE_DIVIDER).toInt()

                view.findViewById<TextView>(R.id.videos_size).text = videosSize.formatSize()
                view.findViewById<LinearProgressIndicator>(R.id.videos_progressbar).progress = (videosSize / SIZE_DIVIDER).toInt()

                view.findViewById<TextView>(R.id.audio_size).text = audioSize.formatSize()
                view.findViewById<LinearProgressIndicator>(R.id.audio_progressbar).progress = (audioSize / SIZE_DIVIDER).toInt()

                view.findViewById<TextView>(R.id.documents_size).text = documentsSize.formatSize()
                view.findViewById<LinearProgressIndicator>(R.id.documents_progressbar).progress = (documentsSize / SIZE_DIVIDER).toInt()

                view.findViewById<TextView>(R.id.archives_size).text = archivesSize.formatSize()
                view.findViewById<LinearProgressIndicator>(R.id.archives_progressbar).progress = (archivesSize / SIZE_DIVIDER).toInt()

                view.findViewById<TextView>(R.id.others_size).text = othersSize.formatSize()
                view.findViewById<LinearProgressIndicator>(R.id.others_progressbar).progress = (othersSize / SIZE_DIVIDER).toInt()

            }


        }



        getSizes(view)
        setStorageDetails(view)


        //view.findViewById<MaterialCardView>(R.id.universalCloneButton).setOncli

        view.findViewById<LinearProgressIndicator>(R.id.main_storage_usage_progressbar).setIndicatorColor(resources.getColor(R.color.colorPrimary))
        view.findViewById<LinearProgressIndicator>(R.id.main_storage_usage_progressbar).trackColor = resources.getColor(R.color.colorPrimary).adjustAlpha(0.3f)

        val redColor = view.context.resources.getColor(R.color.md_red_700)
        view.findViewById<LinearProgressIndicator>(R.id.images_progressbar).setIndicatorColor(redColor)
        view.findViewById<LinearProgressIndicator>(R.id.images_progressbar).trackColor = redColor.adjustAlpha(0.3f)

        val greenColor = view.context.resources.getColor(R.color.md_green_700)
        view.findViewById<LinearProgressIndicator>(R.id.videos_progressbar).setIndicatorColor(greenColor)
        view.findViewById<LinearProgressIndicator>(R.id.videos_progressbar).trackColor = greenColor.adjustAlpha(0.3f)

        val lightBlueColor = view.context.resources.getColor(R.color.md_light_blue_700)
        view.findViewById<LinearProgressIndicator>(R.id.audio_progressbar).setIndicatorColor(lightBlueColor)
        view.findViewById<LinearProgressIndicator>(R.id.audio_progressbar).trackColor = lightBlueColor.adjustAlpha(0.3f)

        val yellowColor = view.context.resources.getColor(R.color.md_yellow_700)
        view.findViewById<LinearProgressIndicator>(R.id.documents_progressbar).setIndicatorColor(yellowColor)
        view.findViewById<LinearProgressIndicator>(R.id.documents_progressbar).trackColor = yellowColor.adjustAlpha(0.3f)

        val tealColor = view.context.resources.getColor(R.color.md_teal_700)
        view.findViewById<LinearProgressIndicator>(R.id.archives_progressbar) .setIndicatorColor(tealColor)
        view.findViewById<LinearProgressIndicator>(R.id.archives_progressbar).trackColor = tealColor.adjustAlpha(0.3f)

        val pinkColor = view.context.resources.getColor(R.color.md_pink_700)
        view.findViewById<LinearProgressIndicator>(R.id.others_progressbar).setIndicatorColor(pinkColor)
        view.findViewById<LinearProgressIndicator>(R.id.others_progressbar).trackColor = pinkColor.adjustAlpha(0.3f)






        ////////////////////////////////////////////////////////////////////////////////////////////




    }



    override fun onResume() {
        super.onResume()

        val cloneActive = defaultPreferences.getBoolean("clone_active", false)

        if(cloneActive== false)
        {
            activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, backPressedCallback)
        }



        //activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, backPressedCallback)
    }


    override fun onPause() {
        super.onPause()
       // backPressedCallback.remove()

        val cloneActive = defaultPreferences.getBoolean("clone_active", false)

        if(cloneActive== false)
        {
            backPressedCallback.remove()
        }
    }



    ////////////////////////////////////////////////////////////////////////////////////////////



    //Storage stats
    private fun setStorageDetails(view:  View) {

        //view.context.updateTextColors(storage_)

        view.findViewById<LinearProgressIndicator>(R.id.main_storage_usage_progressbar).setIndicatorColor(resources.getColor(R.color.colorPrimary))
        view.findViewById<LinearProgressIndicator>(R.id.main_storage_usage_progressbar).trackColor = resources.getColor(R.color.colorPrimary).adjustAlpha(0.3f)

        val redColor = view.context.resources.getColor(R.color.md_red_700)
        view.findViewById<LinearProgressIndicator>(R.id.images_progressbar).setIndicatorColor(redColor)
        view.findViewById<LinearProgressIndicator>(R.id.images_progressbar).trackColor = redColor.adjustAlpha(0.3f)

        val greenColor = view.context.resources.getColor(R.color.md_green_700)
        view.findViewById<LinearProgressIndicator>(R.id.videos_progressbar).setIndicatorColor(greenColor)
        view.findViewById<LinearProgressIndicator>(R.id.videos_progressbar).trackColor = greenColor.adjustAlpha(0.3f)

        val lightBlueColor = view.context.resources.getColor(R.color.md_light_blue_700)
        view.findViewById<LinearProgressIndicator>(R.id.audio_progressbar).setIndicatorColor(lightBlueColor)
        view.findViewById<LinearProgressIndicator>(R.id.audio_progressbar).trackColor = lightBlueColor.adjustAlpha(0.3f)

        val yellowColor = view.context.resources.getColor(R.color.md_yellow_700)
        view.findViewById<LinearProgressIndicator>(R.id.documents_progressbar).setIndicatorColor(yellowColor)
        view.findViewById<LinearProgressIndicator>(R.id.documents_progressbar).trackColor = yellowColor.adjustAlpha(0.3f)

        val tealColor = view.context.resources.getColor(R.color.md_teal_700)
        view.findViewById<LinearProgressIndicator>(R.id.archives_progressbar) .setIndicatorColor(tealColor)
        view.findViewById<LinearProgressIndicator>(R.id.archives_progressbar).trackColor = tealColor.adjustAlpha(0.3f)

        val pinkColor = view.context.resources.getColor(R.color.md_pink_700)
        view.findViewById<LinearProgressIndicator>(R.id.others_progressbar).setIndicatorColor(pinkColor)
        view.findViewById<LinearProgressIndicator>(R.id.others_progressbar).trackColor = pinkColor.adjustAlpha(0.3f)


    }

    private fun getSizes(view: View ) {
        if (!isOreoPlus()) {
            return
        }



        val mainLooper = Looper.getMainLooper()

        //ensureBackgroundThread

        //Thread(Runnable
        /*GlobalScope.launch{



            getMainStorageStats(view.context, viewBinding)

            val filesSize = getSizesByMimeType(view)
            val imagesSize = filesSize[IMAGES]!!
            val videosSize = filesSize[VIDEOS]!!
            val audioSize = filesSize[AUDIO]!!
            val documentsSize = filesSize[DOCUMENTS]!!
            val archivesSize = filesSize[ARCHIVES]!!
            val othersSize = filesSize[OTHERS]!!

            Log.d("_filesfiles" , "_filesfiles" + filesSize + imagesSize + videosSize + audioSize + documentsSize + archivesSize + othersSize)

            *//*post {

            }*//*
            Handler(Looper.getMainLooper()).post {



            viewBinding.imagesSize.text = imagesSize.formatSize()
            viewBinding.imagesProgressbar.progress = (imagesSize / SIZE_DIVIDER).toInt()

            viewBinding.videosSize.text = videosSize.formatSize()
            viewBinding.videosProgressbar.progress = (videosSize / SIZE_DIVIDER).toInt()

            viewBinding.audioSize.text = audioSize.formatSize()
            viewBinding.audioProgressbar.progress = (audioSize / SIZE_DIVIDER).toInt()

            viewBinding.documentsSize.text = documentsSize.formatSize()
            viewBinding.documentsProgressbar.progress = (documentsSize / SIZE_DIVIDER).toInt()

            viewBinding.archivesSize.text = archivesSize.formatSize()
            viewBinding.archivesProgressbar.progress = (archivesSize / SIZE_DIVIDER).toInt()

            viewBinding.othersSize.text = othersSize.formatSize()
            viewBinding.othersProgressbar.progress = (othersSize / SIZE_DIVIDER).toInt()

            }


        }*/


    }

    private fun getSizesByMimeType(view: View): HashMap<String, Long> {
        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATA
        )

        var imagesSize = 0L
        var videosSize = 0L
        var audioSize = 0L
        var documentsSize = 0L
        var archivesSize = 0L
        var othersSize = 0L
        try {
            view.context.queryCursor(uri, projection) { cursor ->
                try {
                    val mimeType = cursor.getStringValue(MediaStore.Files.FileColumns.MIME_TYPE)?.lowercase(
                        Locale.getDefault())
                    val size = cursor.getLongValue(MediaStore.Files.FileColumns.SIZE)
                    var number = cursor.getLongValue(MediaStore.Files.FileColumns._COUNT)
                    if (mimeType == null) {
                        if (size > 0 && size != 4096L) {
                            val path = cursor.getStringValue(MediaStore.Files.FileColumns.DATA)
                            if (!view.context.getIsPathDirectory(path)) {
                                othersSize += size
                            }
                        }
                        return@queryCursor
                    }

                    when (mimeType.substringBefore("/")) {
                        "image" -> imagesSize += size
                        "video" -> videosSize += size
                        "audio" -> audioSize += size
                        "text" -> documentsSize += size
                        else -> {
                            when {
                                extraDocumentMimeTypes.contains(mimeType) -> documentsSize += size
                                extraAudioMimeTypes.contains(mimeType) -> audioSize += size
                                archiveMimeTypes.contains(mimeType) -> archivesSize += size
                                else -> othersSize += size
                            }
                        }
                    }
                } catch (e: Exception) {
                }
            }
        } catch (e: Exception) {
        }

        val mimeTypeSizes = HashMap<String, Long>().apply {
            put(IMAGES, imagesSize)
            put(VIDEOS, videosSize)
            put(AUDIO, audioSize)
            put(DOCUMENTS, documentsSize)
            put(ARCHIVES, archivesSize)
            put(OTHERS, othersSize)
        }

        return mimeTypeSizes
    }




    @SuppressLint("NewApi")
    private fun getMainStorageStats(context: Context, view: View ) {
        val externalDirs = context.getExternalFilesDirs(null)
        val storageManager = context.getSystemService(AppCompatActivity.STORAGE_SERVICE) as StorageManager

        externalDirs.forEach { file ->
            val storageVolume = storageManager.getStorageVolume(file) ?: return
            if (storageVolume.isPrimary) {
                // internal storage
                val storageStatsManager = context.getSystemService(AppCompatActivity.STORAGE_STATS_SERVICE) as StorageStatsManager
                val uuid = StorageManager.UUID_DEFAULT
                val totalSpace = storageStatsManager.getTotalBytes(uuid)
                val freeSpace = storageStatsManager.getFreeBytes(uuid)

                val mainLooper = Looper.getMainLooper()

                Handler(Looper.getMainLooper()).post {

                    arrayOf(
                        view.findViewById<LinearProgressIndicator>(R.id.main_storage_usage_progressbar),
                        view.findViewById<LinearProgressIndicator>(R.id.images_progressbar),
                        view.findViewById<LinearProgressIndicator>(R.id.videos_progressbar),
                        view.findViewById<LinearProgressIndicator>(R.id.audio_progressbar),
                        view.findViewById<LinearProgressIndicator>(R.id.documents_progressbar),
                        view.findViewById<LinearProgressIndicator>(R.id.archives_progressbar),
                        view.findViewById<LinearProgressIndicator>(R.id.others_progressbar)
                    ).forEach {
                        it.max = (totalSpace / SIZE_DIVIDER).toInt()
                    }

                    view.findViewById<LinearProgressIndicator>(R.id.main_storage_usage_progressbar).progress = ((totalSpace - freeSpace) / SIZE_DIVIDER).toInt()

                    view.findViewById<LinearProgressIndicator>(R.id.main_storage_usage_progressbar).beVisible()
                    view.findViewById<TextView>(R.id.free_space_value).text = freeSpace.formatSizeThousand()
                    view.findViewById<TextView>(R.id.total_space).text = String.format(context.getString(R.string.total_storage), totalSpace.formatSizeThousand())
                    view.findViewById<TextView>(R.id.free_space_label).beVisible()
                    
                    //var text  = view.findViewById<TextView>(R.id.free_space_value).text.toString() + " " + view.findViewById<TextView>(R.id.total_space).text

                    //Log.d("_filesfiles" , "_filesfiles" + text)


                }




                /*post {

                }*/
            } else {
                // sd card
                val totalSpace = file.totalSpace
                val freeSpace = file.freeSpace
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////



}

class PathContentViewModel(fileModel: FileModel) {
    val isRoot = fileModel.file.getUri() == ROOT_URI

    val isFirst = fileModel.file.parent == null

    val title = fileModel.file.getName()

    companion object {
        val ROOT_URI: Uri = Uri.fromFile(File("/"))
    }
}

class FilePathViewHolder constructor(
    private val clickListener: (FileModel) -> Unit,
    private var binding: ListPathBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(fileModel: FileModel) {
        binding.viewModel = PathContentViewModel(fileModel)
        binding.button.setOnClickListener {
            clickListener(fileModel)
        }
        binding.button.isEnabled = fileModel.file.canRead()
        binding.executePendingBindings()
    }
}

class PathAdapter(
    private val clickListener: (FileModel) -> Unit,
) : ListAdapter<FileModel, FilePathViewHolder>(FileModelItemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilePathViewHolder {
        return FilePathViewHolder(
            clickListener,
            ListPathBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: FilePathViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).listId
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_PATH
    }

    companion object {
        const val VIEW_TYPE_PATH = 0
    }
}

class FileModelItemCallback : DiffUtil.ItemCallback<FileModel>() {
    override fun areItemsTheSame(oldItem: FileModel, newItem: FileModel): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: FileModel, newItem: FileModel): Boolean {
        return oldItem.hashCode() == newItem.hashCode()
    }
}
