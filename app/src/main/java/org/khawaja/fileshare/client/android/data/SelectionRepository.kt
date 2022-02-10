
package org.khawaja.fileshare.client.android.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SelectionRepository @Inject constructor() {
    private val selections = mutableListOf<Any>()

    private val _selectionState = MutableLiveData<List<Any>>(selections)

    val selectionState = liveData {
        emitSource(_selectionState)
    }

    fun addAll(list: List<Any>) {
        synchronized(selections) {
            selections.addAll(list)
        }
    }

    fun clearSelections() {
        selections.clear()
    }

    fun getSelections() = ArrayList(selections)

    fun setSelected(obj: Any, selected: Boolean) {
        synchronized(selections) {
            val result = if (selected) selections.add(obj) else selections.remove(obj)

            if (result) {
                _selectionState.postValue(selections)
            }
        }
    }

    fun <T : Any> whenContains(list: List<T>, handler: (item: T, selected: Boolean) -> Unit) {
        synchronized(selections) {
            list.forEach {
                handler(it, selections.contains(it))
            }
        }
    }
}
