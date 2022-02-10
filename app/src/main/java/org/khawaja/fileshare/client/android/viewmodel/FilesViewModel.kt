package org.khawaja.fileshare.client.android.viewmodel

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.genonbeta.android.framework.io.DocumentFile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.data.FileRepository
import org.khawaja.fileshare.client.android.data.SelectionRepository
import org.khawaja.fileshare.client.android.database.model.SafFolder
import org.khawaja.fileshare.client.android.lifecycle.SingleLiveEvent
import org.khawaja.fileshare.client.android.model.FileModel
import org.khawaja.fileshare.client.android.model.ListItem
import org.khawaja.fileshare.client.android.model.TitleSectionContentModel
import java.io.File
import java.lang.ref.WeakReference
import java.text.Collator
import javax.inject.Inject

@HiltViewModel
class FilesViewModel @Inject internal constructor(
    @ApplicationContext context: Context,
    private val fileRepository: FileRepository,
    private val selectionRepository: SelectionRepository,
) : ViewModel() {

    private val context = WeakReference(context)

    private val strongContext = context

    private val textFolder = context.getString(R.string.folder)

    private val textFile = context.getString(R.string.file)

    private val _files = MutableLiveData<List<ListItem>>()

    val files = Transformations.map(
        liveData {
            requestPath(fileRepository.appDirectory)
            emitSource(_files)

        }
    ) {
        selectionRepository.whenContains(it) { item, selected ->
            if (item is FileModel) item.isSelected = selected
        }
        it
    }


    val isCustomStorageFolder: Boolean
        get() = Uri.fromFile(fileRepository.defaultAppDirectory) != fileRepository.appDirectory.getUri()

    private val _path = MutableLiveData<FileModel>()

    val path = liveData {
        emitSource(_path)
    }

    private val _pathTree = MutableLiveData<List<FileModel>>()

    val pathTree = liveData {
        emitSource(_pathTree)
    }

    val safAdded = SingleLiveEvent<SafFolder>()

    val safFolders = fileRepository.getSafFolders()


    var appDirectory
        get() = fileRepository.appDirectory
        set(value) {
            fileRepository.appDirectory = value
        }

    fun clearStorageList() {
        viewModelScope.launch(Dispatchers.IO) {
            fileRepository.clearStorageList()
        }
    }

    fun createFolder(displayName: String): Boolean {
        val currentFolder = path.value ?: return false
        val context = context.get() ?: return false

        if (currentFolder.file.createDirectory(context, displayName) != null) {
            requestPath(currentFolder.file)
            return true
        }
        Log.d("list_of_filesrepo", "creating folder: " + path.value.toString())
        return false
    }

    val contents = ArrayList<ListItem>(0)
    val filesRecursive = ArrayList<FileModel>(0)


    private fun createOrderedFileList(file: DocumentFile): List<ListItem> {
        val pathTree = mutableListOf<FileModel>()

        var pathChild = file
        do {
            pathTree.add(FileModel(pathChild))
        } while (pathChild.parent?.also { pathChild = it } != null)

        pathTree.reverse()
        _pathTree.postValue(pathTree)

        val list = fileRepository.getFileList(file)


        Log.d("list_of_filesrepo", "getFileList: " + list.size.toString())


        //find all files either files or folder make sure both ar files
        //travese all files eg also folder types

        // scroll all the folder types, count and list down only files types.
        // concatinate all the folders and list down all teh files


        // return the global list after traversing all sub folders lists from here


        if (list.isEmpty()) return list

        val collator = Collator.getInstance()
        collator.strength = Collator.TERTIARY

        val sortedList = list.sortedWith(compareBy(collator) {
            it.file.getName()
        })


        for (i in 0 until contents.size - 1) {
            //var onlyListOfAllFolders = getFilesofSpecificDirectory(DocumentFile.fromFile(fileRepository.defaultAppDirectory))
        }


        val fakeList = list


        sortedList.forEach {
            /*if (it.file.isFile()) {
                // If you were given a file, return true if it's a jpg
                if(it.file.getName().lowercase().endsWith("doc") )//  && !list.contains(it))
                    filesRecursive.add(it)
            } else if (it.file.isDirectory()){
                // If it is a directory, check its contents recursively
                 createOrderedFileList(it.file)
            }*/

            if (it.file.isDirectory()) {
                Log.d("list_of_filesrepo", "type : directory" + it.file.filePath)
                //contents.add(it)
                createOrderedFileList(it.file)
            } else if (it.file.isFile()) {


                /*for(i in 0 until sortedList.size-1){

                }*/

                //Log.d("list_of_filesrepoRecu", "listobject : infile "+ filesRecursive.toString())
                //Log.d("list_of_filesrepoRecuO", "listobject : infile "+ it.toString())

                if (!filesRecursive.contains(it)) {
                    Log.d("list_of_filesrepo", "type : file " + it.file.filePath)
                    if (it.file.getName().lowercase()
                            .endsWith("doc") || it.file.getName().lowercase()
                            .endsWith("ppt") || it.file.getName().lowercase()
                            .endsWith("rar") || it.file.getName().lowercase()
                            .endsWith("zip") || it.file.getName().lowercase()
                            .endsWith("xls") || it.file.getName().lowercase()
                            .endsWith("xlx") || it.file.getName().lowercase()
                            .endsWith("xlsx") || it.file.getName().lowercase().endsWith("pdf")
                    )//  && !list.contains(it))
                        filesRecursive.add(it)
                }


                /*Log.d("list_of_filesrepo", "type : file "+ it.file.filePath)
                if(it.file.getName().lowercase().endsWith("doc") )//  && !list.contains(it))
                filesRecursive.add(it)*/
            }
        }


        Log.d("list_of_filesrepoNoDir", "type : NUmber of directories" + contents.size.toString())

        /*for(i in 0 until contents.size-1){
            Log.d("list_of_filesrepoListDr", "type : List of directories"+contents[i].toString())
        }*/



        if (contents.isNotEmpty()) {
            //contents.add(0, TitleSectionContentModel(textFolder))
        }

        if (filesRecursive.isNotEmpty()) {
            //contents.add(TitleSectionContentModel(textFile))
            // contents.addAll(filesRecursive)


            var size = filesRecursive.size
            Log.d(
                "list_of_filesrepoRecu",
                "listobject : infile " + filesRecursive[size - 1].toString()
            )

            if (!contents.contains(filesRecursive[size - 1]))
                contents.add(filesRecursive[size - 1])

        }

        return contents

        /* var sizeTemp = filesRecursive.size

         if(list.contains(filesRecursive[sizeTemp-1]))
             return contents
         else
             return list*/
    }

    fun goUp(): Boolean {
        val paths = pathTree.value ?: return false

        Log.d("list_of_filesrepo", "go up: " + paths.toString())

        if (paths.size < 2) {
            return false
        }

        val iterator = paths.asReversed().listIterator()
        if (iterator.hasNext()) {
            iterator.next() // skip the first one that is already visible
            do {
                val next = iterator.next()
                if (next.file.canRead()) {
                    requestPath(next.file)
                    return true
                }
            } while (iterator.hasNext())
        }

        return false
    }

    @TargetApi(19)
    fun insertSafFolder(uri: Uri) {
        Log.d("list_of_filesrepo", "getaccessuri: " + uri.toString())
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = context.get() ?: return@launch

                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                val document = DocumentFile.fromUri(context, uri, true)
                val safFolder = SafFolder(uri, document.getName())

                try {
                    fileRepository.insertFolder(safFolder)
                } catch (ignored: SQLiteConstraintException) {
                    // The selected path may already exist!
                }

                safAdded.postValue(safFolder)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    /*val pdf = List<File>(0)
    fun getPdfList(): List<File?>? {
        val collection: Uri
        val projection = arrayOf(
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.MIME_TYPE
        )
        val sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        val selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?"
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")
        val selectionArgs = arrayOf(mimeType)
        collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }
        strongContext.contentResolver.query(collection, projection, selection, selectionArgs, sortOrder).use { cursor ->
            assert(cursor != null)
            if (cursor!!.moveToFirst()) {
                val columnData: Int = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                val columnName: Int = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                do {
                    val path: String = cursor.getString(columnData)
                    val file = File(path)
                    if (file.exists()) {
                        //you can get your pdf files
                        pdf.add(File(cursor.getString(columnData)))
                        Log.d(TAG, "getPdf: " + cursor.getString(columnData))
                    }
                } while (cursor.moveToNext())
            }
        }
        return pdf
    }*/

    fun requestDefaultStorageFolder() {
        viewModelScope.launch(Dispatchers.IO) {
            context.get()?.let {
                requestPathInternal(DocumentFile.fromFile(fileRepository.defaultAppDirectory))
            }
        }
    }

    fun requestStorageFolder() {
        viewModelScope.launch(Dispatchers.IO) {
            context.get()?.let {
                requestPathInternal(fileRepository.appDirectory)
            }
        }
    }

    fun requestPath(file: DocumentFile) {

        viewModelScope.launch(Dispatchers.IO) {
            requestPathInternal(file)
        }
    }

    fun requestPath(folder: SafFolder) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                context.get()?.let {
                    requestPathInternal(DocumentFile.fromUri(it, folder.uri, true))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun requestPathInternal(file: DocumentFile) {
        _path.postValue(FileModel(file))
        _files.postValue(createOrderedFileList(file))
    }





}
