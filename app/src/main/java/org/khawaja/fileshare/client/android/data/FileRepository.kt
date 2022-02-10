
package org.khawaja.fileshare.client.android.data

import android.content.Context
import android.net.Uri
import android.nfc.Tag
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.genonbeta.android.framework.io.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.database.SafFolderDao
import org.khawaja.fileshare.client.android.database.model.SafFolder
import org.khawaja.fileshare.client.android.model.FileModel
import java.io.File
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val safFolderDao: SafFolderDao,
) {
    private val contextWeak = WeakReference(context)


    private val strongContext = context
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    var appDirectory: DocumentFile
        get() {
            val context = contextWeak.get()!!
            val defaultPath = defaultAppDirectory
            val preferredPath = Environment.DIRECTORY_DCIM// preferences.getString(KEY_STORAGE_PATH, null)    //Environment.DIRECTORY_DCIM//

            Log.d("storage_path_", preferredPath.toString())

            if (preferredPath != null) {
                try {
                    val storageFolder = DocumentFile.fromUri(context, Uri.parse(preferredPath))
                    if (storageFolder.isDirectory() && storageFolder.canWrite()) return storageFolder
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (defaultPath.isFile) defaultPath.delete()
            if (!defaultPath.isDirectory) defaultPath.mkdirs()
            return DocumentFile.fromFile(defaultPath)
        }
        set(value) {
            preferences.edit {
                putString(KEY_STORAGE_PATH, value.getUri().toString())
            }
        }

    /*val defaultAppDirectory: File by lazy {
        *//*if (Build.VERSION.SDK_INT >= 29) {
            return@lazy context.getExternalFilesDir("FileTransferred")!!
        }*//*

        //var primaryDirectory = Environment.getRootDirectory()

        var primaryDir = Environment.getExternalStorageDirectory() //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!primaryDir.isDirectory && !primaryDir.mkdirs() || !primaryDir.canWrite()) {
            primaryDir = Environment.getExternalStorageDirectory() //Environment.getExternalStorageDirectory()
        }

        File(primaryDir.toString()) // + File.separator + context.getString(R.string.app_name))
    }*/


    val defaultAppDirectory:  File by lazy {


        var primaryDir = Environment.getExternalStorageDirectory()
        if (!primaryDir.isDirectory && !primaryDir .mkdir() || !primaryDir.canWrite() ){
            primaryDir  = Environment.getExternalStorageDirectory()
        }
        File (primaryDir.toString())
    }
    /*val defaultAppDirectory: File by lazy {
        if (Build.VERSION.SDK_INT >= 29) {
            return@lazy context.getExternalFilesDir("TransferredFiles")!!
        }

        var primaryDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!primaryDir.isDirectory && !primaryDir.mkdirs() || !primaryDir.canWrite()) {
            primaryDir = Environment.getExternalStorageDirectory()
        }

        File(primaryDir.toString() + File.separator + context.getString(R.string.app_name))
    }*/




    suspend fun clearStorageList() = safFolderDao.removeAll()

    fun getFileList(file: DocumentFile): List<FileModel> {
        val context = contextWeak.get() ?: return emptyList()

        check(file.isDirectory()) {
            "${file.originalUri} is not a directory."
        }


        return file.listFiles(context).map {
            FileModel(it, it.takeIf { it.isDirectory() }?.listFiles(context)?.size ?: 0)
        }
    }


    // Get all the only pdf files from repository






    fun getSafFolders() = safFolderDao.getAll()


    suspend fun insertFolder(safFolder: SafFolder) = safFolderDao.insert(safFolder)

    companion object {
        const val KEY_STORAGE_PATH = "storage_path"
    }



    // get all files through Media store API


    protected fun getPdfList(): ArrayList<String>? {
        val pdfList: ArrayList<String> = ArrayList()
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
        strongContext.getContentResolver().query(collection, projection, selection, selectionArgs, sortOrder)
            .use { cursor ->
                assert(cursor != null)
                if (cursor!!.moveToFirst()) {
                    val columnData: Int = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                    val columnName: Int =
                        cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    do {
                        pdfList.add(cursor.getString(columnData))
                        Log.d("all_pdf_files_displayed", "getPdf: " + cursor.getString(columnData))
                        //you can get your pdf files
                    } while (cursor.moveToNext())
                }
            }
        return pdfList
    }






}
