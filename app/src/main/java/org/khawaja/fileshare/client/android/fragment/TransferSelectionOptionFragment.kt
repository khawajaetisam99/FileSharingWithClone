
package org.khawaja.fileshare.client.android.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.activity.ContentBrowserActivity
import org.khawaja.fileshare.client.android.activity.HomeActivity
import org.khawaja.fileshare.client.android.activity.TransferTypeActivity
import org.khawaja.fileshare.client.android.config.AppConfig

class TransferSelectionOptionFragment : Fragment(R.layout.layout_transfer_selection_option) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    //var preferences = activity?.getSharedPreferences("clone_pref", Context.MODE_PRIVATE)

    var preferences = activity?.getPreferences(Context.MODE_PRIVATE)


    //var cloneSharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.transferDataCard).setOnClickListener {

            preferences?.edit()?.putBoolean("clone_active", true)?.apply()


            //cloneSharedPreferences.edit().putBoolean("clone_active", true).apply()


            var intent = Intent(it.context, HomeActivity::class.java)
            startActivity(intent)
        }
        view.findViewById<View>(R.id.phoneCloneCard).setOnClickListener {
            //startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(AppConfig.URI_ORG_HOME)))

            findNavController().navigate(
                TransferSelectionOptionFragmentDirections.actionTransferSelectionFragmentToCloneSelectionFragment()
            )
        }
    }

    override fun onResume() {
        super.onResume()

        TransferTypeActivity.backPressedCount = 0
    }

    /*override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.about, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.licenses -> findNavController().navigate(
                AboutFragmentDirections.actionAboutFragmentToLicensesFragment()
            )
            R.id.changelog -> findNavController().navigate(
                AboutFragmentDirections.actionAboutFragmentToChangelogFragment2()
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }*/
}
