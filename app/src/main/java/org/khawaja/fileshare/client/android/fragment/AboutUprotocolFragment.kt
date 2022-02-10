
package org.khawaja.fileshare.client.android.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.config.AppConfig

class AboutUprotocolFragment : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_about_uprotocol, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val githubButton = view.findViewById<Button>(R.id.githubButton)

        githubButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).setData((Uri.parse(AppConfig.URI_GITHUB_UPROTOCOL))))
        }
    }
}
