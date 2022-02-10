
package org.khawaja.fileshare.client.android.viewmodel

import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import org.khawaja.fileshare.client.android.data.ClientRepository
import org.khawaja.fileshare.client.android.data.SharedTextRepository
import org.khawaja.fileshare.client.android.data.TaskRepository
import org.khawaja.fileshare.client.android.data.TransferRepository
import org.khawaja.fileshare.client.android.data.WebDataRepository
import org.khawaja.fileshare.client.android.database.model.SharedText
import org.khawaja.fileshare.client.android.database.model.Transfer
import org.khawaja.fileshare.client.android.database.model.TransferDetail
import org.khawaja.fileshare.client.android.database.model.UClient
import org.khawaja.fileshare.client.android.database.model.WebTransfer
import org.khawaja.fileshare.client.android.model.DateSectionContentModel
import org.khawaja.fileshare.client.android.model.ListItem
import org.khawaja.fileshare.client.android.protocol.isIncoming
import org.khawaja.fileshare.client.android.task.transfer.TransferParams
import org.khawaja.fileshare.client.android.viewmodel.content.TransferStateContentViewModel
import org.monora.uprotocol.core.protocol.Direction
import javax.inject.Inject

@HiltViewModel
class TransfersIncomingViewModel @Inject internal constructor(
    @ApplicationContext context: Context,
    private val clientRepository: ClientRepository,
    private val sharedTextRepository: SharedTextRepository,
    private val taskRepository: TaskRepository,
    private val transferRepository: TransferRepository,
    private val webDataRepository: WebDataRepository,
) : ViewModel() {
    val transfers = liveData {
        val merger = MediatorLiveData<List<ListItem>>()
        val texts = sharedTextRepository.getSharedTexts()
        val details = transferRepository.getTransferDetails()
        val webTransfers = webDataRepository.getReceivedContents()
        val observer = Observer<List<ListItem>> {
            val mergedList = mutableListOf<ListItem>().apply {
                //details.value?.get(4)?.direction?.isIncoming

                /*if(details.value?.get(4)?.direction.toString() == "incoming"){
                    Log.e("list_not_iterated", ": list", )
                }
                else{
                    texts.value?.let { addAll(it) }
                    details.value?.let { addAll(it) }
                    webTransfers.value?.let { addAll(it) }

                    sortByDescending {
                        when (it) {
                            is TransferDetail -> it.dateCreated
                            is SharedText -> it.created
                            is WebTransfer -> it.dateCreated
                            else -> throw IllegalStateException()
                        }
                    }
                }*/

                //Log.d("viewmodellist4", details.value?.get(4).toString())
                /*texts.value?.let { addAll(it) }
                details.value?.let { addAll(it) }
                webTransfers.value?.let { addAll(it) }

                sortByDescending {
                    when (it) {
                        is TransferDetail -> it.dateCreated
                        is SharedText -> it.created
                        is WebTransfer -> it.dateCreated
                        else -> throw IllegalStateException()
                    }
                }*/

                for (i in 0 until it.size) {
                    Log.d("viewmodellist1", details.value?.get(i)?.direction.toString())

                    if(details.value?.get(i)?.direction.toString().equals("Incoming")){

                        /*texts.value?.let { add(i, it.elementAt(i)) }
                        details.value?.let { add(i, it.elementAt(i)) }
                        webTransfers.value?.let { add(i, it.elementAt(i)) }*/

                        //texts.value?.let { addAll(it) }
                        //details.value?.let { add(it[i]) }
                        details.value?.let { add(it[i]) }
                        //webTransfers.value?.let { add(it[i]) }


                        /*texts.value?.let { add(texts.value!!.get(i))}
                        details.value?.let { add(details.value!!.get(i)) }
                        webTransfers.value?.let { add(webTransfers.value!!.get(i)) }*/


                        Log.d("view_model_loop", details.value?.get(i)?.direction.toString())


                    }
                }

                sortByDescending {
                    when (it) {
                        is TransferDetail -> it.dateCreated
                        is SharedText -> it.created
                        is WebTransfer -> it.dateCreated
                        else -> throw IllegalStateException()
                    }
                }




            }

            val resultList = mutableListOf<ListItem>()
            var previous: DateSectionContentModel? = null

            mergedList.forEach {
                val date = when (it) {
                    is TransferDetail -> it.dateCreated
                    is SharedText -> it.created
                    is WebTransfer -> it.dateCreated
                    else -> throw IllegalStateException()
                }
                val dateText = DateUtils.formatDateTime(context, date, DateUtils.FORMAT_SHOW_DATE)
                if (dateText != previous?.dateText) {
                    resultList.add(DateSectionContentModel(dateText, date).also { model -> previous = model })
                }
                resultList.add(it)
            }

            merger.value = resultList
        }

        merger.addSource(texts, observer)
        merger.addSource(details, observer)
        merger.addSource(webTransfers, observer)

        emitSource(merger)
    }

    suspend fun getTransfer(groupId: Long): Transfer? = transferRepository.getTransfer(groupId)

    suspend fun getClient(clientUid: String): UClient? = clientRepository.getDirect(clientUid)

    fun subscribe(transferDetail: TransferDetail) = taskRepository.subscribeToTask {
        if (it.params is TransferParams && it.params.transfer.id == transferDetail.id) it.params else null
    }.map {
        TransferStateContentViewModel.from(it)
    }
}
