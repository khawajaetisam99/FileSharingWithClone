
package org.khawaja.fileshare.client.android.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.viewmodel.ChangelogViewModel

@AndroidEntryPoint
class ChangelogFragment : BottomSheetDialogFragment() {
    private val viewModel: ChangelogViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_changelog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val changelogText = view.findViewById<TextView>(R.id.changelogText)

        viewModel.changelog.observe(viewLifecycleOwner) {
            changelogText.text = it
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.declareLatestChangelogAsShown()
    }
}
