
package org.khawaja.fileshare.client.android.viewmodel

import android.view.View
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

class EmptyContentViewModel {
    var hasContent = ObservableBoolean()
    var stateText = ObservableField<String> ()



    var loading = ObservableBoolean()


    fun isLoading(title:String){
        hasContent.set(false)
        stateText.set(title)
        loading.set(true)
    }


    fun isError(title:String){
        hasContent.set(false)
        stateText.set(title)
        loading.set(false)
    }



    fun with(content: RecyclerView?, hasContent: Boolean) {
        this.hasContent.set(hasContent)
        content?.visibility = if (hasContent) View.VISIBLE else View.GONE
        //content?.visibility = View.VISIBLE
    }

}