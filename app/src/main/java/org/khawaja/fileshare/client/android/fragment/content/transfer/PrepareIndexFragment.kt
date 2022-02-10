
package org.khawaja.fileshare.client.android.fragment.content.transfer

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.genonbeta.android.framework.io.DocumentFile
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.simplemobiletools.commons.extensions.getDefaultAlarmSound
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.content.App
import org.khawaja.fileshare.client.android.content.Image
import org.khawaja.fileshare.client.android.content.Song
import org.khawaja.fileshare.client.android.content.Video
import org.khawaja.fileshare.client.android.data.SelectionRepository
import org.khawaja.fileshare.client.android.database.model.UTransferItem
import org.khawaja.fileshare.client.android.fragment.ContentBrowserViewModel
import org.khawaja.fileshare.client.android.model.FileModel
import org.khawaja.fileshare.client.android.util.Progress
import org.khawaja.fileshare.client.android.util.Transfers
import org.monora.uprotocol.core.protocol.Direction
import java.io.File
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class PrepareIndexFragment : BottomSheetDialogFragment() {
    private val viewModel: PrepareIndexViewModel by viewModels()

    private val contentBrowserViewModel: ContentBrowserViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_prepare_index, container, false)
    }

    protected val defaultPreferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(activity)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        defaultPreferences.edit {
            putBoolean("content_sharing_active", true)
        }

        defaultPreferences.edit {
            putBoolean("unintentional_back", true)
        }
        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                is PreparationState.Preparing -> {
                }
                is PreparationState.Ready -> {
                    contentBrowserViewModel.items = it.groupId to it.list
                    findNavController().navigate(
                        PrepareIndexFragmentDirections.actionPrepareIndexFragmentToNavPickClient()
                    )
                }
            }
        }
    }
}

@HiltViewModel
class PrepareIndexViewModel @Inject internal constructor(
    @ApplicationContext context: Context,
    private val selectionRepository: SelectionRepository,
) : ViewModel() {
    private val _state = MutableLiveData<PreparationState>()

    val state = liveData {
        emitSource(_state)
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _state.postValue(PreparationState.Preparing)

            val list = selectionRepository.getSelections()
            val groupId = Random.nextLong()
            val items = mutableListOf<UTransferItem>()
            val progress = Progress(list.size)
            val direction = Direction.Outgoing

            list.forEach {
                if (it is FileModel) {
                    Transfers.createStructure(context, items, progress, groupId, it.file) { _, _ ->

                    }
                } else if (it is App) {
                    val base = DocumentFile.fromFile(File(it.info.sourceDir))
                    val hasSplit = Build.VERSION.SDK_INT >= 21 && it.info.splitSourceDirs != null
                    val name = "${it.label}_${it.versionName}"
                    val baseName = if (hasSplit) base.getName() else "$name.apk"
                    val directory = if (hasSplit) name else null

                    progress.index += 1
                    items.add(
                        UTransferItem(
                            progress.index.toLong(),
                            groupId,
                            baseName,
                            base.getType(),
                            base.getLength(),
                            directory,
                            base.getUri().toString(),
                            direction,
                        )
                    )

                    if (Build.VERSION.SDK_INT >= 21 && hasSplit) {
                        it.info.splitSourceDirs?.forEach { splitPath ->
                            progress.index += 1

                            val split = DocumentFile.fromFile(File(splitPath))
                            val id = progress.index.toLong()

                            items.add(
                                UTransferItem(
                                    id,
                                    groupId,
                                    split.getName(),
                                    split.getType(),
                                    split.getLength(),
                                    directory,
                                    split.getUri().toString(),
                                    direction,
                                )
                            )
                        }
                    }
                } else {
                    progress.index += 1
                    val id = progress.index.toLong()

                    val item = when (it) {
                        is Song -> UTransferItem(
                            id, groupId, it.displayName, it.mimeType, it.size, null, it.uri.toString(), direction
                        )
                        is Image -> UTransferItem(
                            id, groupId, it.displayName, it.mimeType, it.size, null, it.uri.toString(), direction
                        )
                        is Video -> UTransferItem(
                            id, groupId, it.displayName, it.mimeType, it.size, null, it.uri.toString(), direction
                        )
                        else -> {
                            progress.index -= 1
                            Log.e(TAG, "Unknown object type was given ${it.javaClass.simpleName}")
                            return@forEach
                        }
                    }

                    items.add(item)
                }
            }

            _state.postValue(PreparationState.Ready(groupId, items))
        }
    }

    companion object {
        private const val TAG = "PrepareIndexViewModel"
    }
}

sealed class PreparationState {
    object Preparing : PreparationState()

    class Ready(val groupId: Long, val list: List<UTransferItem>) : PreparationState()
}
