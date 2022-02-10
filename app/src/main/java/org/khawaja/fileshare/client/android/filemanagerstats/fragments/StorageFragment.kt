package org.khawaja.fileshare.client.android.filemanagerstats.fragments

import android.annotation.SuppressLint
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Intent
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.provider.Settings
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.helpers.isOreoPlus
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.databinding.LayoutContentBrowserBinding
import org.khawaja.fileshare.client.android.databinding.StorageFragmentBinding
import org.khawaja.fileshare.client.android.filemanagerstats.activities.MimeTypesActivity
import org.khawaja.fileshare.client.android.filemanagerstats.activities.SimpleActivity
import org.khawaja.fileshare.client.android.filemanagerstats.extensions.formatSizeThousand
import org.khawaja.fileshare.client.android.filemanagerstats.helpers.*
//import kotlinx.android.synthetic.main.storage_fragment.view.*

import java.util.*
import kotlin.collections.HashMap

class StorageFragment(context: Context, attributeSet: AttributeSet) : MyViewPagerFragment(context, attributeSet) {
    private val SIZE_DIVIDER = 100000

    private lateinit var viewBinding: StorageFragmentBinding

    override fun setupFragment(activity: SimpleActivity) {

        viewBinding = StorageFragmentBinding.inflate(activity.layoutInflater)
        viewBinding.totalSpace.text = String.format(context.getString(R.string.total_storage), "…")
        getSizes()

        viewBinding.freeSpaceHolder.setOnClickListener {
            try {
                val storageSettingsIntent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
                activity.startActivity(storageSettingsIntent)
            } catch (e: Exception) {
                activity.showErrorToast(e)
            }
        }

        /*images_holder.setOnClickListener { launchMimetypeActivity(IMAGES) }
        videos_holder.setOnClickListener { launchMimetypeActivity(VIDEOS) }
        audio_holder.setOnClickListener { launchMimetypeActivity(AUDIO) }
        documents_holder.setOnClickListener { launchMimetypeActivity(DOCUMENTS) }
        archives_holder.setOnClickListener { launchMimetypeActivity(ARCHIVES) }
        others_holder.setOnClickListener { launchMimetypeActivity(OTHERS) }*/
    }

    override fun refreshFragment() {}

    override fun onResume(textColor: Int, primaryColor: Int) {
        getSizes()
        //context.updateTextColors(storage_fragment)

        viewBinding.mainStorageUsageProgressbar.setIndicatorColor(resources.getColor(R.color.colorPrimary))
        viewBinding.mainStorageUsageProgressbar.trackColor = resources.getColor(R.color.colorPrimary).adjustAlpha(0.3f)

        val redColor = context.resources.getColor(R.color.md_red_700)
        viewBinding.imagesProgressbar.setIndicatorColor(redColor)
        viewBinding.imagesProgressbar.trackColor = redColor.adjustAlpha(0.3f)

        val greenColor = context.resources.getColor(R.color.md_green_700)
        viewBinding.videosProgressbar.setIndicatorColor(greenColor)
        viewBinding.videosProgressbar.trackColor = greenColor.adjustAlpha(0.3f)

        val lightBlueColor = context.resources.getColor(R.color.md_light_blue_700)
        viewBinding.audioProgressbar.setIndicatorColor(lightBlueColor)
        viewBinding.audioProgressbar.trackColor = lightBlueColor.adjustAlpha(0.3f)

        val yellowColor = context.resources.getColor(R.color.md_yellow_700)
        viewBinding.documentsProgressbar.setIndicatorColor(yellowColor)
        viewBinding.documentsProgressbar.trackColor = yellowColor.adjustAlpha(0.3f)

        val tealColor = context.resources.getColor(R.color.md_teal_700)
        viewBinding.archivesProgressbar .setIndicatorColor(tealColor)
        viewBinding.archivesProgressbar.trackColor = tealColor.adjustAlpha(0.3f)

        val pinkColor = context.resources.getColor(R.color.md_pink_700)
        viewBinding.othersProgressbar.setIndicatorColor(pinkColor)
        viewBinding.othersProgressbar.trackColor = pinkColor.adjustAlpha(0.3f)
    }

    private fun launchMimetypeActivity(mimetype: String) {
        Intent(context, MimeTypesActivity::class.java).apply {
            putExtra(SHOW_MIMETYPE, mimetype)
            context.startActivity(this)
        }
    }

    private fun getSizes() {
        if (!isOreoPlus()) {
            return
        }

        ensureBackgroundThread {
            getMainStorageStats(context)


            val filesSize = getSizesByMimeType()
            val imagesSize = filesSize[IMAGES]!!
            val videosSize = filesSize[VIDEOS]!!
            val audioSize = filesSize[AUDIO]!!
            val documentsSize = filesSize[DOCUMENTS]!!
            val archivesSize = filesSize[ARCHIVES]!!
            val othersSize = filesSize[OTHERS]!!

            post {
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
        }
    }

    private fun getSizesByMimeType(): HashMap<String, Long> {
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
            context.queryCursor(uri, projection) { cursor ->
                try {
                    val mimeType = cursor.getStringValue(MediaStore.Files.FileColumns.MIME_TYPE)?.lowercase(Locale.getDefault())
                    val size = cursor.getLongValue(MediaStore.Files.FileColumns.SIZE)
                    if (mimeType == null) {
                        if (size > 0 && size != 4096L) {
                            val path = cursor.getStringValue(MediaStore.Files.FileColumns.DATA)
                            if (!context.getIsPathDirectory(path)) {
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
    private fun getMainStorageStats(context: Context) {
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

                post {
                    arrayOf(
                        viewBinding.mainStorageUsageProgressbar, viewBinding.imagesProgressbar, viewBinding.videosProgressbar, viewBinding.audioProgressbar, viewBinding.documentsProgressbar,
                        viewBinding.archivesProgressbar, viewBinding. othersProgressbar
                    ).forEach {
                        it.max = (totalSpace / SIZE_DIVIDER).toInt()
                    }

                    viewBinding.mainStorageUsageProgressbar.progress = ((totalSpace - freeSpace) / SIZE_DIVIDER).toInt()

                    viewBinding.mainStorageUsageProgressbar.beVisible()
                    viewBinding.freeSpaceValue.text = freeSpace.formatSizeThousand()
                    viewBinding.totalSpace.text = String.format(context.getString(R.string.total_storage), totalSpace.formatSizeThousand())
                    viewBinding.freeSpaceLabel.beVisible()
                }
            } else {
                // sd card
                val totalSpace = file.totalSpace
                val freeSpace = file.freeSpace
            }
        }
    }
}
