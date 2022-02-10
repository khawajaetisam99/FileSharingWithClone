package org.khawaja.fileshare.client.android.binding

import android.widget.EditText
import android.widget.ImageView
import androidx.core.widget.addTextChangedListener
import androidx.databinding.BindingAdapter
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import org.khawaja.fileshare.client.android.GlideApp
import org.khawaja.fileshare.client.android.util.Graphics
import org.khawaja.fileshare.client.android.util.picturePath
import org.khawaja.fileshare.client.android.viewmodel.UserProfileViewModel
import org.monora.uprotocol.core.protocol.Client

@BindingAdapter("listenNicknameChanges")
fun listenNicknameChanges(editText: EditText, viewModel: UserProfileViewModel) {
    editText.addTextChangedListener { editable ->
        viewModel.clientNickname = editable.toString().also { if (it.isEmpty()) return@addTextChangedListener }
    }
}

@BindingAdapter("pictureOf")
fun loadPictureOfClient(imageView: ImageView, client: Client?) {
    if (client == null) return

    try {
        val default = Graphics.createIconBuilder(imageView.context).buildRound(client.clientNickname)

        GlideApp.with(imageView)
            .load(imageView.context.getFileStreamPath(client.picturePath))
            .circleCrop()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(default)
            .error(default)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
    } catch (ignored: Exception) {
    }
}
