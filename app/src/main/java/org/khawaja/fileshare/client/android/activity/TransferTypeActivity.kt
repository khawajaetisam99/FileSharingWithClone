package org.khawaja.fileshare.client.android.activity

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import org.khawaja.fileshare.client.android.NavHomeDirections
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.app.Activity
import org.khawaja.fileshare.client.android.fragment.TransferDetailsFragmentArgs
import org.khawaja.fileshare.client.android.util.PrivacyPolicyCustomDialog

class TransferTypeActivity : Activity(), NavigationView.OnNavigationItemSelectedListener {


    private val navigationView: NavigationView by lazy {
        findViewById(R.id.nav_view)
    }

    private val drawerLayout: DrawerLayout by lazy {
        findViewById(R.id.drawer_layout)
    }

    private var pendingMenuItemId = 0

    private val navController by lazy {
        navController(R.id.nav_host_fragment_transfer_details)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer_type)



        //defaultPreferences.edit().putBoolean("clone_active",false).apply()

        defaultPreferences.edit {
            putBoolean("clone_active", true)
        }

        window.statusBarColor = ContextCompat.getColor(this@TransferTypeActivity, R.color.colorPrimary)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.open_navigation_drawer, R.string.close_navigation_drawer
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        drawerLayout.addDrawerListener(
            object : DrawerLayout.SimpleDrawerListener() {
                override fun onDrawerClosed(drawerView: View) {
                    applyAwaitingDrawerAction()
                }
            }
        )




        toolbar.setupWithNavController(navController, AppBarConfiguration(navController.graph, drawerLayout))
        toolbar.setTitleTextColor(Color.WHITE)
        //toolbar.setNavigationIcon(R.drawable.ic_baseline_account_circle_24)
        toolbar.popupTheme = R.style.ToolbarColoredBackArrow
        toolbar.background = resources.getDrawable(R.color.colorPrimary)
        navigationView.setNavigationItemSelectedListener(this)
        navController.addOnDestinationChangedListener { _, destination, _ -> title = destination.label }




    }

    //Alert dialog positive and negative buttons
    val positiveButtonClick1 = { dialog: DialogInterface, which: Int ->
        //Toast.makeText(applicationContext, android.R.string.yes, Toast.LENGTH_SHORT).show()
        finish()

    }
    val negativeButtonClick2 = { dialog: DialogInterface, which: Int ->
        //Toast.makeText(applicationContext, android.R.string.no, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        //super.onBackPressed()


        val count = supportFragmentManager.backStackEntryCount


        if (backPressedCount == 0){

            val builder = android.app.AlertDialog.Builder(this)
            with(builder)
            {
                setTitle(R.string.exitmessage)
                setTheme(R.style.Theme_TrebleShot)
                setCancelable(true)
                setPositiveButton("Ok", DialogInterface.OnClickListener(function = positiveButtonClick1))
                setNegativeButton(android.R.string.no, negativeButtonClick2)
                show()

            }

        }
        else
        {
            super.onBackPressed()
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        openItem(item.itemId)
        return true
    }



    private fun applyAwaitingDrawerAction() {
        if (pendingMenuItemId == 0) {
            return // drawer was opened, but nothing was clicked.
        }

        when (pendingMenuItemId) {
            R.id.edit_profile -> navController.navigate(NavHomeDirections.actionGlobalProfileEditorFragment())
            R.id.manage_clients -> navController.navigate(NavHomeDirections.actionGlobalNavManageDevices())
            R.id.about -> navController.navigate(NavHomeDirections.actionGlobalNavAbout())

            R.id.storageOption ->{
                navController.navigate(NavHomeDirections.actionTransferHistoryFragmentToNavReceive())
            }
            R.id.privacy -> {
                val manager = supportFragmentManager
                val privacyPolicyCustomDialog = PrivacyPolicyCustomDialog()
                privacyPolicyCustomDialog.show(manager, "privacy policy dialog")
            }
            R.id.onboarding -> {
                defaultPreferences.edit { putBoolean("introduction_shown", false) }
                startActivity(Intent(this@TransferTypeActivity, WelcomeActivity::class.java))
            }
            R.id.preferences -> navController.navigate(NavHomeDirections.actionGlobalNavPreferences())
            //R.id.about_uprotocol -> navController.navigate(NavHomeDirections.actionGlobalAboutUprotocolFragment())
            R.id.donate -> try {
                //startActivity(Intent(applicationContext, Class.forName("org.khawaja.fileshare.client.android.activity.DonationActivity")))
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
        }

        pendingMenuItemId = 0
    }

    override fun onResume() {
        super.onResume()

        defaultPreferences.edit {
            putBoolean("clone_active", true)
        }
    }

    /*override fun onStart() {
        super.onStart()

        backPressedCount = 0
        Log.d("home_started", "resumed")

    }

    override fun onStop() {
        super.onStop()
        backPressedCount = 1
        Log.d("home_started", "destroyed")
    }*/



    private fun openItem(@IdRes id: Int) {
        pendingMenuItemId = id
        drawerLayout.close()
    }


    companion object {
        public var backPressedCount = 0
    }
}