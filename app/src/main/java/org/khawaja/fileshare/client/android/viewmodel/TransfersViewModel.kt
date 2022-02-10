
package org.khawaja.fileshare.client.android.viewmodel

import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.DisposableHandle
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
import org.khawaja.fileshare.client.android.task.transfer.TransferParams
import org.khawaja.fileshare.client.android.viewmodel.content.TransferStateContentViewModel
import javax.inject.Inject

@HiltViewModel
class TransfersViewModel @Inject internal constructor(
    @ApplicationContext context: Context,
    private val clientRepository: ClientRepository,
    private val sharedTextRepository: SharedTextRepository,
    private val taskRepository: TaskRepository,
    private val transferRepository: TransferRepository,
    private val webDataRepository: WebDataRepository,
) : ViewModel() {


    private var searchFilter = ""

    fun update(result: String, viewLifecycleOwner: LifecycleOwner, ){
        searchFilter = result

        Log.d("viewmodellistupdatefun", searchFilter)

    }






    val transfersSearchFilterList = liveData {
        val merger = MediatorLiveData<List<ListItem>>()
        val texts = sharedTextRepository.getSharedTexts()
        val details = transferRepository.getTransferDetails()
        val webTransfers = webDataRepository.getReceivedContents()
        val observer = Observer<List<ListItem>> {
            val mergedList = mutableListOf<ListItem>().apply {



                for (i in 0 until it.size) {
                    Log.d("viewmodellistadaptrcald", searchFilter)


                    //if(details.value?.get(i)?.clientNickname.toString().equals(""))

                    if(details.value?.get(i)?.clientNickname?.contains(searchFilter) == true){

                        Log.d("viewmodellistnickname", details.value?.get(i)?.clientNickname.toString())

                        details.value?.let { add(it[i]) }

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

//        merger.addSource(texts, observer)
        merger.addSource(details, observer)
//        merger.addSource(webTransfers, observer)

        emitSource(merger)
    }

    val transfersSearchFilterList1 = liveData {
        val merger = MediatorLiveData<List<ListItem>>()
        val texts = sharedTextRepository.getSharedTexts()
        val details = transferRepository.getTransferDetails()
        val webTransfers = webDataRepository.getReceivedContents()
        val observer = Observer<List<ListItem>> {
            val mergedList = mutableListOf<ListItem>().apply {



                for (i in 0 until it.size) {
                    Log.d("viewmodellistadaptrcald", searchFilter)


                    //if(details.value?.get(i)?.clientNickname.toString().equals(""))

                    if(details.value?.get(i)?.clientNickname?.contains(searchFilter) == true){

                        Log.d("viewmodellistnickname", details.value?.get(i)?.clientNickname.toString())

                        details.value?.let { add(it[i]) }

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

//        merger.addSource(texts, observer)
        merger.addSource(details, observer)
//        merger.addSource(webTransfers, observer)

        emitSource(merger)
    }
    val transfersSearchFilterList2 = liveData {
        val merger = MediatorLiveData<List<ListItem>>()
        val texts = sharedTextRepository.getSharedTexts()
        val details = transferRepository.getTransferDetails()
        val webTransfers = webDataRepository.getReceivedContents()
        val observer = Observer<List<ListItem>> {
            val mergedList = mutableListOf<ListItem>().apply {



                for (i in 0 until it.size) {
                    Log.d("viewmodellistadaptrcald", searchFilter)


                    //if(details.value?.get(i)?.clientNickname.toString().equals(""))

                    if(details.value?.get(i)?.clientNickname?.contains(searchFilter) == true){

                        Log.d("viewmodellistnickname", details.value?.get(i)?.clientNickname.toString())

                        details.value?.let { add(it[i]) }

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

//        merger.addSource(texts, observer)
        merger.addSource(details, observer)
//        merger.addSource(webTransfers, observer)

        emitSource(merger)
    }
    val transfersSearchFilterList3 = liveData {
        val merger = MediatorLiveData<List<ListItem>>()
        val texts = sharedTextRepository.getSharedTexts()
        val details = transferRepository.getTransferDetails()
        val webTransfers = webDataRepository.getReceivedContents()
        val observer = Observer<List<ListItem>> {
            val mergedList = mutableListOf<ListItem>().apply {



                for (i in 0 until it.size) {
                    Log.d("viewmodellistadaptrcald", searchFilter)


                    //if(details.value?.get(i)?.clientNickname.toString().equals(""))

                    if(details.value?.get(i)?.clientNickname?.contains(searchFilter) == true){

                        Log.d("viewmodellistnickname", details.value?.get(i)?.clientNickname.toString())

                        details.value?.let { add(it[i]) }

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

//        merger.addSource(texts, observer)
        merger.addSource(details, observer)
//        merger.addSource(webTransfers, observer)

        emitSource(merger)
    }
    val transfersSearchFilterList4 = liveData {
        val merger = MediatorLiveData<List<ListItem>>()
        val texts = sharedTextRepository.getSharedTexts()
        val details = transferRepository.getTransferDetails()
        val webTransfers = webDataRepository.getReceivedContents()
        val observer = Observer<List<ListItem>> {
            val mergedList = mutableListOf<ListItem>().apply {



                for (i in 0 until it.size) {
                    Log.d("viewmodellistadaptrcald", searchFilter)


                    //if(details.value?.get(i)?.clientNickname.toString().equals(""))

                    if(details.value?.get(i)?.clientNickname?.contains(searchFilter) == true){

                        Log.d("viewmodellistnickname", details.value?.get(i)?.clientNickname.toString())

                        details.value?.let { add(it[i]) }

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

//        merger.addSource(texts, observer)
        merger.addSource(details, observer)
//        merger.addSource(webTransfers, observer)

        emitSource(merger)
    }

    val transfersSearchFilterList5 = liveData {
        val merger = MediatorLiveData<List<ListItem>>()
        val texts = sharedTextRepository.getSharedTexts()
        val details = transferRepository.getTransferDetails()
        val webTransfers = webDataRepository.getReceivedContents()
        val observer = Observer<List<ListItem>> {
            val mergedList = mutableListOf<ListItem>().apply {



                for (i in 0 until it.size) {
                    Log.d("viewmodellistadaptrcald", searchFilter)


                    //if(details.value?.get(i)?.clientNickname.toString().equals(""))

                    if(details.value?.get(i)?.clientNickname?.contains(searchFilter) == true){

                        Log.d("viewmodellistnickname", details.value?.get(i)?.clientNickname.toString())

                        details.value?.let { add(it[i]) }

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

//        merger.addSource(texts, observer)
        merger.addSource(details, observer)
//        merger.addSource(webTransfers, observer)

        emitSource(merger)
    }
    val transfersSearchFilterList6 = liveData {
        val merger = MediatorLiveData<List<ListItem>>()
        val texts = sharedTextRepository.getSharedTexts()
        val details = transferRepository.getTransferDetails()
        val webTransfers = webDataRepository.getReceivedContents()
        val observer = Observer<List<ListItem>> {
            val mergedList = mutableListOf<ListItem>().apply {



                for (i in 0 until it.size) {
                    Log.d("viewmodellistadaptrcald", searchFilter)


                    //if(details.value?.get(i)?.clientNickname.toString().equals(""))

                    if(details.value?.get(i)?.clientNickname?.contains(searchFilter) == true){

                        Log.d("viewmodellistnickname", details.value?.get(i)?.clientNickname.toString())

                        details.value?.let { add(it[i]) }

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

//        merger.addSource(texts, observer)
        merger.addSource(details, observer)
//        merger.addSource(webTransfers, observer)

        emitSource(merger)
    }
    val transfersSearchFilterList7 = liveData {
        val merger = MediatorLiveData<List<ListItem>>()
        val texts = sharedTextRepository.getSharedTexts()
        val details = transferRepository.getTransferDetails()
        val webTransfers = webDataRepository.getReceivedContents()
        val observer = Observer<List<ListItem>> {
            val mergedList = mutableListOf<ListItem>().apply {



                for (i in 0 until it.size) {
                    Log.d("viewmodellistadaptrcald", searchFilter)


                    //if(details.value?.get(i)?.clientNickname.toString().equals(""))

                    if(details.value?.get(i)?.clientNickname?.contains(searchFilter) == true){

                        Log.d("viewmodellistnickname", details.value?.get(i)?.clientNickname.toString())

                        details.value?.let { add(it[i]) }

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

//        merger.addSource(texts, observer)
        merger.addSource(details, observer)
//        merger.addSource(webTransfers, observer)

        emitSource(merger)
    }
    val transfersSearchFilterList8 = liveData {
        val merger = MediatorLiveData<List<ListItem>>()
        val texts = sharedTextRepository.getSharedTexts()
        val details = transferRepository.getTransferDetails()
        val webTransfers = webDataRepository.getReceivedContents()
        val observer = Observer<List<ListItem>> {
            val mergedList = mutableListOf<ListItem>().apply {



                for (i in 0 until it.size) {
                    Log.d("viewmodellistadaptrcald", searchFilter)


                    //if(details.value?.get(i)?.clientNickname.toString().equals(""))

                    if(details.value?.get(i)?.clientNickname?.contains(searchFilter) == true){

                        Log.d("viewmodellistnickname", details.value?.get(i)?.clientNickname.toString())

                        details.value?.let { add(it[i]) }

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

//        merger.addSource(texts, observer)
        merger.addSource(details, observer)
//        merger.addSource(webTransfers, observer)

        emitSource(merger)
    }
    val transfersSearchFilterList9 = liveData {
        val merger = MediatorLiveData<List<ListItem>>()
        val texts = sharedTextRepository.getSharedTexts()
        val details = transferRepository.getTransferDetails()
        val webTransfers = webDataRepository.getReceivedContents()
        val observer = Observer<List<ListItem>> {
            val mergedList = mutableListOf<ListItem>().apply {



                for (i in 0 until it.size) {
                    Log.d("viewmodellistadaptrcald", searchFilter)


                    //if(details.value?.get(i)?.clientNickname.toString().equals(""))

                    if(details.value?.get(i)?.clientNickname?.contains(searchFilter) == true){

                        Log.d("viewmodellistnickname", details.value?.get(i)?.clientNickname.toString())

                        details.value?.let { add(it[i]) }

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

//        merger.addSource(texts, observer)
        merger.addSource(details, observer)
//        merger.addSource(webTransfers, observer)

        emitSource(merger)
    }
    val transfersSearchFilterList10 = liveData {
        val merger = MediatorLiveData<List<ListItem>>()
        val texts = sharedTextRepository.getSharedTexts()
        val details = transferRepository.getTransferDetails()
        val webTransfers = webDataRepository.getReceivedContents()
        val observer = Observer<List<ListItem>> {
            val mergedList = mutableListOf<ListItem>().apply {



                for (i in 0 until it.size) {
                    Log.d("viewmodellistadaptrcald", searchFilter)


                    //if(details.value?.get(i)?.clientNickname.toString().equals(""))

                    if(details.value?.get(i)?.clientNickname?.contains(searchFilter) == true){

                        Log.d("viewmodellistnickname", details.value?.get(i)?.clientNickname.toString())

                        details.value?.let { add(it[i]) }

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

//        merger.addSource(texts, observer)
        merger.addSource(details, observer)
//        merger.addSource(webTransfers, observer)

        emitSource(merger)
    }
    val transfersSearchFilterList11 = liveData {
        val merger = MediatorLiveData<List<ListItem>>()
        val texts = sharedTextRepository.getSharedTexts()
        val details = transferRepository.getTransferDetails()
        val webTransfers = webDataRepository.getReceivedContents()
        val observer = Observer<List<ListItem>> {
            val mergedList = mutableListOf<ListItem>().apply {



                for (i in 0 until it.size) {
                    Log.d("viewmodellistadaptrcald", searchFilter)


                    //if(details.value?.get(i)?.clientNickname.toString().equals(""))

                    if(details.value?.get(i)?.clientNickname?.contains(searchFilter) == true){

                        Log.d("viewmodellistnickname", details.value?.get(i)?.clientNickname.toString())

                        details.value?.let { add(it[i]) }

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

//        merger.addSource(texts, observer)
        merger.addSource(details, observer)
//        merger.addSource(webTransfers, observer)

        emitSource(merger)
    }
    val transfersSearchFilterList12 = liveData {
        val merger = MediatorLiveData<List<ListItem>>()
        val texts = sharedTextRepository.getSharedTexts()
        val details = transferRepository.getTransferDetails()
        val webTransfers = webDataRepository.getReceivedContents()
        val observer = Observer<List<ListItem>> {
            val mergedList = mutableListOf<ListItem>().apply {



                for (i in 0 until it.size) {
                    Log.d("viewmodellistadaptrcald", searchFilter)


                    //if(details.value?.get(i)?.clientNickname.toString().equals(""))

                    if(details.value?.get(i)?.clientNickname?.contains(searchFilter) == true){

                        Log.d("viewmodellistnickname", details.value?.get(i)?.clientNickname.toString())

                        details.value?.let { add(it[i]) }

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

//        merger.addSource(texts, observer)
        merger.addSource(details, observer)
//        merger.addSource(webTransfers, observer)

        emitSource(merger)
    }
    val transfersSearchFilterList13 = liveData {
        val merger = MediatorLiveData<List<ListItem>>()
        val texts = sharedTextRepository.getSharedTexts()
        val details = transferRepository.getTransferDetails()
        val webTransfers = webDataRepository.getReceivedContents()
        val observer = Observer<List<ListItem>> {
            val mergedList = mutableListOf<ListItem>().apply {



                for (i in 0 until it.size) {
                    Log.d("viewmodellistadaptrcald", searchFilter)


                    //if(details.value?.get(i)?.clientNickname.toString().equals(""))

                    if(details.value?.get(i)?.clientNickname?.contains(searchFilter) == true){

                        Log.d("viewmodellistnickname", details.value?.get(i)?.clientNickname.toString())

                        details.value?.let { add(it[i]) }

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

//        merger.addSource(texts, observer)
        merger.addSource(details, observer)
//        merger.addSource(webTransfers, observer)

        emitSource(merger)
    }
    val transfersSearchFilterList14 = liveData {
        val merger = MediatorLiveData<List<ListItem>>()
        val texts = sharedTextRepository.getSharedTexts()
        val details = transferRepository.getTransferDetails()
        val webTransfers = webDataRepository.getReceivedContents()
        val observer = Observer<List<ListItem>> {
            val mergedList = mutableListOf<ListItem>().apply {



                for (i in 0 until it.size) {
                    Log.d("viewmodellistadaptrcald", searchFilter)


                    //if(details.value?.get(i)?.clientNickname.toString().equals(""))

                    if(details.value?.get(i)?.clientNickname?.contains(searchFilter) == true){

                        Log.d("viewmodellistnickname", details.value?.get(i)?.clientNickname.toString())

                        details.value?.let { add(it[i]) }

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

//        merger.addSource(texts, observer)
        merger.addSource(details, observer)
//        merger.addSource(webTransfers, observer)

        emitSource(merger)
    }
    val transfersSearchFilterList15 = liveData {
        val merger = MediatorLiveData<List<ListItem>>()
        val texts = sharedTextRepository.getSharedTexts()
        val details = transferRepository.getTransferDetails()
        val webTransfers = webDataRepository.getReceivedContents()
        val observer = Observer<List<ListItem>> {
            val mergedList = mutableListOf<ListItem>().apply {



                for (i in 0 until it.size) {
                    Log.d("viewmodellistadaptrcald", searchFilter)


                    //if(details.value?.get(i)?.clientNickname.toString().equals(""))

                    if(details.value?.get(i)?.clientNickname?.contains(searchFilter) == true){

                        Log.d("viewmodellistnickname", details.value?.get(i)?.clientNickname.toString())

                        details.value?.let { add(it[i]) }

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

//        merger.addSource(texts, observer)
        merger.addSource(details, observer)
//        merger.addSource(webTransfers, observer)

        emitSource(merger)
    }


    ////////////////////////////////////////////////////////////////////////////////////////




    val transfersOutgoing = liveData {
        val merger = MediatorLiveData<List<ListItem>>()
        val texts = sharedTextRepository.getSharedTexts()
        val details = transferRepository.getTransferDetails()
        val webTransfers = webDataRepository.getReceivedContents()
        val observer = Observer<List<ListItem>> {
            val mergedList = mutableListOf<ListItem>().apply {



                for (i in 0 until it.size) {
                    Log.d("viewmodellist1", details.value?.get(i)?.direction.toString())

                    //if(details.value?.get(i)?.clientNickname.toString().equals(""))

                    if(details.value?.get(i)?.direction.toString().equals("Outgoing")){

                        details.value?.let { add(it[i]) }

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

//        merger.addSource(texts, observer)
        merger.addSource(details, observer)
//        merger.addSource(webTransfers, observer)

        emitSource(merger)
    }



    val transfersIncoming = liveData {
        var disposable: DisposableHandle? = null
        val merger = MediatorLiveData<List<ListItem>>()
        val texts = sharedTextRepository.getSharedTexts()
        val details = transferRepository.getTransferDetails()
        val webTransfers = webDataRepository.getReceivedContents()
        val observer = Observer<List<ListItem>> {
            val mergedList = mutableListOf<ListItem>().apply {
                for (i in 0 until it.size) {
                    Log.d("viewmodellist1", details.value?.get(i)?.direction.toString())

                    if(details.value?.get(i)?.direction.toString().equals("Incoming")){


                        details.value?.let { add(it[i]) }

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
//                try{
//                    disposable?.dispose()
//                }catch (ex:Exception){
//
//                }

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

//        merger.addSource(texts, observer)
        merger.addSource(details, observer)
//        merger.addSource(webTransfers, observer)

        disposable  = emitSource(merger)
    }


    val transfers = liveData {
        val merger = MediatorLiveData<List<ListItem>>()
        val texts = sharedTextRepository.getSharedTexts()
        val details = transferRepository.getTransferDetails()
        val webTransfers = webDataRepository.getReceivedContents()
        val observer = Observer<List<ListItem>> {
            val mergedList = mutableListOf<ListItem>().apply {

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

//            if(){
//
//            }

            merger.value = resultList
        }

//        merger.addSource(texts, observer)
        merger.addSource(details, observer)
//        merger.addSource(webTransfers, observer)

        emitSource(merger)
    }



    val transfersPinned = liveData {
        var disposable: DisposableHandle? = null
        val merger = MediatorLiveData<List<ListItem>>()
        val texts = sharedTextRepository.getSharedTexts()
        val details = transferRepository.getTransferDetails()
        val webTransfers = webDataRepository.getReceivedContents()
        val observer = Observer<List<ListItem>> {
            val mergedList = mutableListOf<ListItem>().apply {
                for (i in 0 until it.size) {
                    Log.d("viewmodellist1", details.value?.get(i)?.direction.toString())

                    if(details.value?.get(i)?.isPinned == true){


                        details.value?.let { add(it[i]) }

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
//                try{
//                    disposable?.dispose()
//                }catch (ex:Exception){
//
//                }

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

//        merger.addSource(texts, observer)
        merger.addSource(details, observer)
//        merger.addSource(webTransfers, observer)

        disposable  = emitSource(merger)
    }





    suspend fun getTransfer(groupId: Long): Transfer? = transferRepository.getTransfer(groupId)

    suspend fun getClient(clientUid: String): UClient? = clientRepository.getDirect(clientUid)

    fun subscribe(transferDetail: TransferDetail) = taskRepository.subscribeToTask {
        if (it.params is TransferParams && it.params.transfer.id == transferDetail.id) it.params else null
    }.map {
        TransferStateContentViewModel.from(it)
    }
}
