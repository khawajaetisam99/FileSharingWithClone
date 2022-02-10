
package org.khawaja.fileshare.client.android.fragment.content

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.genonbeta.android.framework.util.Files
import com.simplemobiletools.commons.extensions.toInt
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.content.Video
import org.khawaja.fileshare.client.android.content.VideoBucket
import org.khawaja.fileshare.client.android.data.MediaRepository
import org.khawaja.fileshare.client.android.data.SelectionRepository
import org.khawaja.fileshare.client.android.databinding.LayoutEmptyContentBinding
import org.khawaja.fileshare.client.android.databinding.ListVideoBinding
import org.khawaja.fileshare.client.android.databinding.ListVideoBucketBinding
import org.khawaja.fileshare.client.android.util.Activities
import org.khawaja.fileshare.client.android.viewmodel.EmptyContentViewModel
import org.khawaja.fileshare.client.android.viewmodel.SharingSelectionViewModel
import timber.log.Timber
import javax.inject.Inject
import android.os.Looper
import androidx.navigation.fragment.findNavController
import org.khawaja.fileshare.client.android.fragment.ContentBrowserFragmentDirections


@AndroidEntryPoint
class VideoBrowserFragment : Fragment(R.layout.layout_video_browser) {
    private val browserViewModel: VideoBrowserViewModel by viewModels()

    private val selectionViewModel: SharingSelectionViewModel by activityViewModels()

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            browserViewModel.showBuckets()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val titleView = view.findViewById<TextView>(R.id.titleText)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val emptyView = LayoutEmptyContentBinding.bind(view.findViewById(R.id.emptyView))
        val videoAdapter = VideoBrowserAdapter { video, clickType ->
            when (clickType) {
                VideoBrowserAdapter.ClickType.Default -> Activities.view(view.context, video.uri, video.mimeType)
                VideoBrowserAdapter.ClickType.ToggleSelect -> {
                    selectionViewModel.setSelected(video, video.isSelected)
                }
            }
        }
        val bucketAdapter = VideoBucketBrowserAdapter {
            browserViewModel.showVideos(it)
        }
        val emptyContentViewModel = EmptyContentViewModel()

        emptyView.viewModel = emptyContentViewModel
        emptyView.emptyText.setText(R.string.empty_video_list)
        emptyView.emptyImage.setImageResource(R.drawable.ic_photo_white_24dp)
        emptyView.executePendingBindings()
        videoAdapter.setHasStableIds(true)
        recyclerView.adapter = videoAdapter

        browserViewModel.showingContent.observe(viewLifecycleOwner) {
            when (it) {
                is VideoBrowserViewModel.Content.Buckets -> {
                    backPressedCallback.isEnabled = false
                    titleView.text = getString(R.string.folders)
                    recyclerView.adapter = bucketAdapter
                    bucketAdapter.submitList(it.list)
                    emptyContentViewModel.with(recyclerView, it.list.isNotEmpty())

                }
                is VideoBrowserViewModel.Content.Videos -> {
                    backPressedCallback.isEnabled = true
                    titleView.text = it.videoBucket.name
                    recyclerView.adapter = videoAdapter
                    videoAdapter.submitList(it.list)
                    emptyContentViewModel.with(recyclerView, it.list.isNotEmpty())



                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            recyclerView?.scrollToPosition(it.list.size-1)
                        },
                        1000 // value in milliseconds
                    )

                    /*Handler(Looper.getMainLooper()).post(Runnable {
                        recyclerView?.scrollToPosition(it.list.size-2)
                        //recyclerView?.scrollTo(0,30)
                    })*/


                }
            }

        }
        //recyclerView.canScrollVertically(20)




        selectionViewModel.externalState.observe(viewLifecycleOwner) {
            videoAdapter.notifyDataSetChanged()

            //recyclerView.scrollToPosition(videoAdapter.size() - 1);
        }
    }

    override fun onResume() {
        super.onResume()
        //activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, backPressedCallback)
    }

    override fun onPause() {
        super.onPause()
        backPressedCallback.remove()
    }
}

class VideoBrowserAdapter(
    private val clickListener: (Video, ClickType) -> Unit,
) : ListAdapter<Video, VideoViewHolder>(VideoItemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        return VideoViewHolder(
            ListVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            clickListener,
        )
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_VIDEO
    }

    enum class ClickType {
        Default,
        ToggleSelect,
    }

    companion object {
        const val VIEW_TYPE_VIDEO = 0
    }
}

class VideoItemCallback : DiffUtil.ItemCallback<Video>() {
    override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean {
        return oldItem == newItem
    }
}

class VideoContentViewModel(video: Video) {
    val title = video.title

    val size = Files.formatLength(video.size, false)

    val uri = video.uri
}

class VideoViewHolder(
    private val binding: ListVideoBinding,
    private val clickListener: (Video, VideoBrowserAdapter.ClickType) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    protected val defaultPreferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(binding.root.context)


    fun bind(video: Video) {
        binding.viewModel = VideoContentViewModel(video)
        binding.root.setOnClickListener {
            clickListener(video, VideoBrowserAdapter.ClickType.Default)
        }


        val cloneActive = defaultPreferences.getBoolean("clone_active", false)

        if (cloneActive==true){
            //video.isSelected = !video.isSelected
            video.isSelected = !video.isSelected
            binding.selection.isSelected = video.isSelected
            clickListener(video, VideoBrowserAdapter.ClickType.ToggleSelect)
        }

        /*video.isSelected = !video.isSelected
        binding.selection.isSelected = video.isSelected
        clickListener(video, VideoBrowserAdapter.ClickType.ToggleSelect)*/

        binding.selection.setOnClickListener {
            video.isSelected = !video.isSelected
            it.isSelected = video.isSelected
            clickListener(video, VideoBrowserAdapter.ClickType.ToggleSelect)
        }
        binding.selection.isSelected = video.isSelected
        binding.executePendingBindings()
    }
}


class VideoBucketItemCallback : DiffUtil.ItemCallback<VideoBucket>() {
    override fun areItemsTheSame(oldItem: VideoBucket, newItem: VideoBucket): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: VideoBucket, newItem: VideoBucket): Boolean {
        return oldItem == newItem
    }
}

class VideoBucketContentViewModel(videoBucket: VideoBucket) {
    val name = videoBucket.name

    val thumbnailUri = videoBucket.thumbnailUri
}

class VideoBucketViewHolder(
    private val binding: ListVideoBucketBinding,
    private val clickListener: (VideoBucket) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(videoBucket: VideoBucket) {
        R.layout.list_image_bucket
        binding.viewModel = VideoBucketContentViewModel(videoBucket)
        binding.root.setOnClickListener {
            clickListener(videoBucket)
        }
        binding.executePendingBindings()
    }
}

class VideoBucketBrowserAdapter(
    private val clickListener: (VideoBucket) -> Unit,
) : ListAdapter<VideoBucket, VideoBucketViewHolder>(VideoBucketItemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoBucketViewHolder {
        return VideoBucketViewHolder(
            ListVideoBucketBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            clickListener,
        )
    }

    override fun onBindViewHolder(holder: VideoBucketViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_BUCKET
    }

    companion object {
        const val VIEW_TYPE_BUCKET = 0
    }
}

@HiltViewModel
class VideoBrowserViewModel @Inject internal constructor(
    private val mediaRepository: MediaRepository,
    private val selectionRepository: SelectionRepository,
) : ViewModel() {
    private val _showingContent = MutableLiveData<Content>()

    val showingContent = liveData {
        emitSource(_showingContent)
    }

    fun showBuckets() {
        viewModelScope.launch(Dispatchers.IO) {
            //_showingContent.postValue(Content.Buckets(mediaRepository.getVideoBuckets()))
            Log.d("video_buckets", "showVideos: "+mediaRepository.getVideoBuckets().toString())
            var listOfBuckets = mediaRepository.getVideoBuckets()
            /*for (i in 0 until listOfBuckets.size){
                showVideos(listOfBuckets[i])
            }*/

            showVideosList(listOfBuckets)

        }
    }

    fun showVideos(bucket: VideoBucket) {
        viewModelScope.launch(Dispatchers.IO) {
            val videos = mediaRepository.getVideos(bucket)
            Log.d("video_bucket", "showVideos: "+bucket.toString())
            selectionRepository.whenContains(videos) { item, selected -> item.isSelected = selected }
            _showingContent.postValue(Content.Videos(bucket, videos))
        }
    }

    fun showVideosList(videoBucketList: List<VideoBucket>) {
        viewModelScope.launch(Dispatchers.IO) {


            var videosTobeShown :   List<Video>  = listOf()

            var videoBucketListSize = videoBucketList.size
            Log.d("Videos_to_be_shown", videoBucketListSize.toString())
            for (i in 0 until videoBucketList.size-1) {

                videosTobeShown =  videosTobeShown + mediaRepository.getVideos(videoBucketList[i]) + mediaRepository.getVideos(videoBucketList[i+1])

            }



            //videosTobeShown = mediaRepository.getVideos(videoBucketList.sumOf {i})

            //videoBucketList.reduce{acc, videoBucketList -> acc }



            //videosTobeShown = mediaRepository.getVideos(videoBucketList[0]) + mediaRepository.getVideos(videoBucketList[1]) + mediaRepository.getVideos(videoBucketList[2]) + mediaRepository.getVideos(videoBucketList[3])

            Log.d("Videos_to_be_shown", videosTobeShown.toString())

            selectionRepository.whenContains(videosTobeShown){item, selected -> item.isSelected = selected}

            for( i in 0 until videoBucketList.size){
                _showingContent .postValue(Content.Videos(videoBucketList[i], videosTobeShown))
            }

            //_showingContent .postValue(Content.Videos(videoBucketList[i], videosTobeShown))


           /* val videos = mediaRepository.getVideos(bucket)
            Log.d("video_bucket", "showVideos: "+bucket.toString())
            selectionRepository.whenContains(videos) { item, selected -> item.isSelected = selected }
            _showingContent.postValue(Content.Videos(bucket, videos))*/
        }
    }

    init {
        showBuckets()

        //showVideos()
    }

    sealed class Content {
        class Buckets(val list: List<VideoBucket>) : Content()

        class Videos(val videoBucket: VideoBucket, val list: List<Video>) : Content()
    }
}
