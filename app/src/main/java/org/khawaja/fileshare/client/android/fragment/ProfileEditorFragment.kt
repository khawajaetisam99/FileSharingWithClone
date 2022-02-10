
package org.khawaja.fileshare.client.android.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.databinding.LayoutProfileEditorBinding
import org.khawaja.fileshare.client.android.viewmodel.UserProfileViewModel

@AndroidEntryPoint
class ProfileEditorFragment : BottomSheetDialogFragment() {
    private val viewModel: UserProfileViewModel by viewModels()

    private val pickPhoto = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            viewModel.saveProfilePicture(uri)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.layout_profile_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = LayoutProfileEditorBinding.bind(view)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.pickPhotoClickListener = View.OnClickListener {
            pickPhoto.launch("image/*")
        }

        binding.executePendingBindings()
    }
}
