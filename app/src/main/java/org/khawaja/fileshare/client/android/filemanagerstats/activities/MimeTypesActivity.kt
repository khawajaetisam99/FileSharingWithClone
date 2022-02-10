package org.khawaja.fileshare.client.android.filemanagerstats.activities

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewbinding.ViewBinding
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.VIEW_TYPE_GRID
import com.simplemobiletools.commons.helpers.VIEW_TYPE_LIST
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.models.FileDirItem
import com.simplemobiletools.commons.views.MyGridLayoutManager
import com.simplemobiletools.commons.views.MyRecyclerView
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.databinding.ActivityMimetypesBinding
import org.khawaja.fileshare.client.android.filemanagerstats.adapters.ItemsAdapter
import org.khawaja.fileshare.client.android.filemanagerstats.dialogs.ChangeSortingDialog
import org.khawaja.fileshare.client.android.filemanagerstats.dialogs.ChangeViewTypeDialog
import org.khawaja.fileshare.client.android.filemanagerstats.extensions.config
import org.khawaja.fileshare.client.android.filemanagerstats.extensions.tryOpenPathIntent
import org.khawaja.fileshare.client.android.filemanagerstats.helpers.*
import org.khawaja.fileshare.client.android.filemanagerstats.interfaces.ItemOperationsListener
import org.khawaja.fileshare.client.android.filemanagerstats.models.ListItem
/*import kotlinx.android.synthetic.main.activity_mimetypes.*
import kotlinx.android.synthetic.main.items_fragment.view.**/
import java.util.*

class MimeTypesActivity : SimpleActivity(), ItemOperationsListener {
    private var isSearchOpen = false
    private var currentMimeType = ""
    private var lastSearchedText = ""
    private var searchMenuItem: MenuItem? = null
    private var zoomListener: MyRecyclerView.MyZoomListener? = null
    private var storedItems = ArrayList<ListItem>()
    private var currentViewType = VIEW_TYPE_LIST


    private lateinit var  viewBinding: ActivityMimetypesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mimetypes)

        viewBinding = ActivityMimetypesBinding.inflate(layoutInflater)
        currentMimeType = intent.getStringExtra(SHOW_MIMETYPE) ?: return
        title = getString(
            when (currentMimeType) {
                IMAGES -> R.string.images
                VIDEOS -> R.string.videos
                AUDIO -> R.string.audio
                DOCUMENTS -> R.string.documents
                ARCHIVES -> R.string.archives
                OTHERS -> R.string.others
                else -> {
                    toast(R.string.unknown_error_occurred)
                    finish()
                    return
                }
            }
        )

        ensureBackgroundThread {
            reFetchItems()
        }

        val adjustedPrimaryColor = getAdjustedPrimaryColor()
        viewBinding.mimetypesFastscroller.updateColors(adjustedPrimaryColor)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        setupSearch(menu)
        updateMenuItemColors(menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val currentViewType = config.getFolderViewType(currentMimeType)

        menu!!.apply {
            findItem(R.id.add_favorite).isVisible = false
            findItem(R.id.remove_favorite).isVisible = false
            findItem(R.id.go_to_favorite).isVisible = false

            findItem(R.id.toggle_filename).isVisible = currentViewType == VIEW_TYPE_GRID
            findItem(R.id.go_home).isVisible = false
            findItem(R.id.set_as_home).isVisible = false
            findItem(R.id.settings).isVisible = false
            findItem(R.id.about).isVisible = false

            findItem(R.id.temporarily_show_hidden).isVisible = !config.shouldShowHidden
            findItem(R.id.stop_showing_hidden).isVisible = config.temporarilyShowHidden

            findItem(R.id.increase_column_count).isVisible = currentViewType == VIEW_TYPE_GRID && config.fileColumnCnt < MAX_COLUMN_COUNT
            findItem(R.id.reduce_column_count).isVisible = currentViewType == VIEW_TYPE_GRID && config.fileColumnCnt > 1
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort -> showSortingDialog()
            R.id.toggle_filename -> toggleFilenameVisibility()
            R.id.change_view_type -> changeViewType()
            R.id.temporarily_show_hidden -> tryToggleTemporarilyShowHidden()
            R.id.stop_showing_hidden -> tryToggleTemporarilyShowHidden()
            R.id.increase_column_count -> increaseColumnCount()
            R.id.reduce_column_count -> reduceColumnCount()
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }

    override fun refreshFragment() {
        reFetchItems()
    }

    override fun deleteFiles(files: ArrayList<FileDirItem>) {
        deleteFiles(files, false) {
            if (!it) {
                runOnUiThread {
                    toast(R.string.unknown_error_occurred)
                }
            }
        }
    }

    override fun selectedPaths(paths: ArrayList<String>) {}

    override fun searchQueryChanged(text: String) {
        val searchText = text.trim()
        lastSearchedText = searchText
        when {
            searchText.isEmpty() -> {
                viewBinding.mimetypesFastscroller.beVisible()
                getRecyclerAdapter()?.updateItems(storedItems)
                viewBinding.mimetypesPlaceholder.beGoneIf(storedItems.isNotEmpty())
                viewBinding.mimetypesPlaceholder2.beGone()
            }
            searchText.length == 1 -> {
                viewBinding.mimetypesFastscroller.beGone()
                viewBinding.mimetypesPlaceholder.beVisible()
                viewBinding.mimetypesPlaceholder2.beVisible()
            }
            else -> {
                ensureBackgroundThread {
                    if (lastSearchedText != searchText) {
                        return@ensureBackgroundThread
                    }

                    val listItems = storedItems.filter { it.name.contains(searchText, true) } as ArrayList<ListItem>

                    runOnUiThread {
                        getRecyclerAdapter()?.updateItems(listItems, text)
                        viewBinding.mimetypesFastscroller.beVisibleIf(listItems.isNotEmpty())
                        viewBinding.mimetypesPlaceholder.beVisibleIf(listItems.isEmpty())
                        viewBinding.mimetypesPlaceholder2.beGone()
                    }
                }
            }
        }
    }

    override fun setupDateTimeFormat() {}

    override fun setupFontSize() {}

    override fun toggleFilenameVisibility() {
        config.displayFilenames = !config.displayFilenames
        getRecyclerAdapter()?.updateDisplayFilenamesInGrid()
    }

    override fun increaseColumnCount() {
        if (currentViewType == VIEW_TYPE_GRID) {
            config.fileColumnCnt = ++(viewBinding.mimetypesList.layoutManager as MyGridLayoutManager).spanCount
            columnCountChanged()
        }
    }

    override fun reduceColumnCount() {
        if (currentViewType == VIEW_TYPE_GRID) {
            config.fileColumnCnt = --(viewBinding.mimetypesList.layoutManager as MyGridLayoutManager).spanCount
            columnCountChanged()
        }
    }

    override fun finishActMode() {}

    private fun setupSearch(menu: Menu) {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchMenuItem = menu.findItem(R.id.search)
        (searchMenuItem!!.actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            isSubmitButtonEnabled = false
            queryHint = getString(R.string.search)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String) = false

                override fun onQueryTextChange(newText: String): Boolean {
                    if (isSearchOpen) {
                        searchQueryChanged(newText)
                    }
                    return true
                }
            })
        }

        MenuItemCompat.setOnActionExpandListener(searchMenuItem, object : MenuItemCompat.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                isSearchOpen = true
                searchOpened()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                isSearchOpen = false
                searchClosed()
                return true
            }
        })
    }

    fun searchOpened() {
        isSearchOpen = true
        lastSearchedText = ""
    }

    fun searchClosed() {
        isSearchOpen = false
        lastSearchedText = ""

        viewBinding.mimetypesFastscroller.beVisible()
        viewBinding.mimetypesPlaceholder.beGoneIf(storedItems.isNotEmpty())
        viewBinding.mimetypesPlaceholder2.beGone()
    }

    private fun getProperFileDirItems(callback: (ArrayList<FileDirItem>) -> Unit) {
        val fileDirItems = ArrayList<FileDirItem>()
        val showHidden = config.shouldShowHidden
        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED
        )

        try {
            queryCursor(uri, projection) { cursor ->
                try {
                    val fullMimetype = cursor.getStringValue(MediaStore.Files.FileColumns.MIME_TYPE)?.lowercase(Locale.getDefault()) ?: return@queryCursor
                    val name = cursor.getStringValue(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    if (!showHidden && name.startsWith(".")) {
                        return@queryCursor
                    }

                    val size = cursor.getLongValue(MediaStore.Files.FileColumns.SIZE)
                    if (size == 0L) {
                        return@queryCursor
                    }

                    val path = cursor.getStringValue(MediaStore.Files.FileColumns.DATA)
                    val lastModified = cursor.getLongValue(MediaStore.Files.FileColumns.DATE_MODIFIED) * 1000

                    val mimetype = fullMimetype.substringBefore("/")
                    when (currentMimeType) {
                        IMAGES -> {
                            if (mimetype == "image") {
                                fileDirItems.add(FileDirItem(path, name, false, 0, size, lastModified))
                            }
                        }
                        VIDEOS -> {
                            if (mimetype == "video") {
                                fileDirItems.add(FileDirItem(path, name, false, 0, size, lastModified))
                            }
                        }
                        AUDIO -> {
                            if (mimetype == "audio" || extraAudioMimeTypes.contains(fullMimetype)) {
                                fileDirItems.add(FileDirItem(path, name, false, 0, size, lastModified))
                            }
                        }
                        DOCUMENTS -> {
                            if (mimetype == "text" || extraDocumentMimeTypes.contains(fullMimetype)) {
                                fileDirItems.add(FileDirItem(path, name, false, 0, size, lastModified))
                            }
                        }
                        ARCHIVES -> {
                            if (archiveMimeTypes.contains(fullMimetype)) {
                                fileDirItems.add(FileDirItem(path, name, false, 0, size, lastModified))
                            }
                        }
                        OTHERS -> {
                            if (mimetype != "image" && mimetype != "video" && mimetype != "audio" && mimetype != "text" &&
                                !extraAudioMimeTypes.contains(fullMimetype) && !extraDocumentMimeTypes.contains(fullMimetype) &&
                                !archiveMimeTypes.contains(fullMimetype)
                            ) {
                                fileDirItems.add(FileDirItem(path, name, false, 0, size, lastModified))
                            }
                        }
                    }
                } catch (e: Exception) {
                }
            }
        } catch (e: Exception) {
            showErrorToast(e)
        }

        callback(fileDirItems)
    }

    private fun addItems(items: ArrayList<ListItem>) {
        FileDirItem.sorting = config.getFolderSorting(currentMimeType)
        items.sort()

        if (isDestroyed || isFinishing) {
            return
        }

        storedItems = items
        ItemsAdapter(this as SimpleActivity, storedItems, this, viewBinding.mimetypesList , false, null) {
            tryOpenPathIntent((it as ListItem).path, false)
        }.apply {
            setupZoomListener(zoomListener)
            viewBinding.mimetypesList.adapter = this
        }

        if (areSystemAnimationsEnabled) {
            viewBinding.mimetypesList.scheduleLayoutAnimation()
        }

        viewBinding.mimetypesPlaceholder.beVisibleIf(items.isEmpty())
    }

    private fun getListItemsFromFileDirItems(fileDirItems: ArrayList<FileDirItem>): ArrayList<ListItem> {
        val listItems = ArrayList<ListItem>()
        fileDirItems.forEach {
            val listItem = ListItem(it.path, it.name, false, 0, it.size, it.modified, false)
            listItems.add(listItem)
        }
        return listItems
    }

    private fun getRecyclerAdapter() = viewBinding.mimetypesList.adapter as? ItemsAdapter

    private fun showSortingDialog() {
        ChangeSortingDialog(this, currentMimeType) {
            recreateList()
        }
    }

    private fun changeViewType() {
        ChangeViewTypeDialog(this, currentMimeType, true) {
            recreateList()
            setupLayoutManager()
        }
    }

    private fun reFetchItems() {
        getProperFileDirItems { fileDirItems ->
            val listItems = getListItemsFromFileDirItems(fileDirItems)

            runOnUiThread {
                addItems(listItems)
                if (currentViewType != config.getFolderViewType(currentMimeType)) {
                    setupLayoutManager()
                }
            }
        }
    }

    private fun recreateList() {
        val listItems = getRecyclerAdapter()?.listItems
        if (listItems != null) {
            addItems(listItems as ArrayList<ListItem>)
        }
    }

    private fun columnCountChanged() {
        invalidateOptionsMenu()
        getRecyclerAdapter()?.apply {
            notifyItemRangeChanged(0, listItems.size)
        }
    }

    private fun setupLayoutManager() {
        if (config.getFolderViewType(currentMimeType) == VIEW_TYPE_GRID) {
            currentViewType = VIEW_TYPE_GRID
            setupGridLayoutManager()
        } else {
            currentViewType = VIEW_TYPE_LIST
            setupListLayoutManager()
        }

        viewBinding.mimetypesList.adapter = null
        initZoomListener()
        addItems(storedItems)
    }

    private fun setupGridLayoutManager() {
        val layoutManager = viewBinding.mimetypesList.layoutManager as MyGridLayoutManager
        layoutManager.spanCount = config.fileColumnCnt ?: 3

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (getRecyclerAdapter()?.isASectionTitle(position) == true) {
                    layoutManager.spanCount
                } else {
                    1
                }
            }
        }
    }

    private fun setupListLayoutManager() {
        val layoutManager = viewBinding.mimetypesList.layoutManager as MyGridLayoutManager
        layoutManager.spanCount = 1
        zoomListener = null
    }

    private fun initZoomListener() {
        if (config.getFolderViewType(currentMimeType) == VIEW_TYPE_GRID) {
            val layoutManager = viewBinding.mimetypesList.layoutManager as MyGridLayoutManager
            zoomListener = object : MyRecyclerView.MyZoomListener {
                override fun zoomIn() {
                    if (layoutManager.spanCount > 1) {
                        reduceColumnCount()
                        getRecyclerAdapter()?.finishActMode()
                    }
                }

                override fun zoomOut() {
                    if (layoutManager.spanCount < MAX_COLUMN_COUNT) {
                        increaseColumnCount()
                        getRecyclerAdapter()?.finishActMode()
                    }
                }
            }
        } else {
            zoomListener = null
        }
    }

    private fun tryToggleTemporarilyShowHidden() {
        if (config.temporarilyShowHidden) {
            toggleTemporarilyShowHidden(false)
        } else {
            handleHiddenFolderPasswordProtection {
                toggleTemporarilyShowHidden(true)
            }
        }
    }

    private fun toggleTemporarilyShowHidden(show: Boolean) {
        config.temporarilyShowHidden = show
        ensureBackgroundThread {
            reFetchItems()
        }
    }
}
