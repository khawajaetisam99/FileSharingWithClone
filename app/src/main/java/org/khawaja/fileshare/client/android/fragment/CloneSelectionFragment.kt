
package org.khawaja.fileshare.client.android.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.activity.ContentBrowserActivity
import org.khawaja.fileshare.client.android.activity.HomeActivity
import org.khawaja.fileshare.client.android.activity.TransferTypeActivity
import org.khawaja.fileshare.client.android.config.AppConfig

class CloneSelectionFragment : Fragment(R.layout.layout_clone_selection_option) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    var preferences = activity?.getSharedPreferences("clone_pref", Context.MODE_PRIVATE)

    //var cloneSharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
    protected val defaultPreferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(activity)


    /*override fun onResume() {
        super.onResume()


        if (defaultPreferences.getBoolean("unintentional_back", true)== true){

            var intent = Intent(context, ContentBrowserActivity::class.java)
            startActivity(intent)

        }



    }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        TransferTypeActivity.backPressedCount = 1


        view.findViewById<View>(R.id.oldPhoneCard).setOnClickListener {
            //startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(AppConfig.URI_ORG_HOME)))

            //cloneSharedPreferences.edit().putBoolean("clone_active", false).apply()
            preferences?.edit()?.putBoolean("clone_active", false)?.apply()

            defaultPreferences.edit {
                putBoolean("unintentional_back", false)
            }

            var intent = Intent(it.context, ContentBrowserActivity::class.java)
            startActivity(intent)
        }
        view.findViewById<View>(R.id.newPhoneCard).setOnClickListener {

            findNavController().navigate(
                CloneSelectionFragmentDirections.pickClient()
            )


            //findNavController().navigate(TransferHistoryFragmentDirections.pickClient())
        }
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
