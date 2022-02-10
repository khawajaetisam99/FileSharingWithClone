package org.khawaja.fileshare.client.android.activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import org.khawaja.fileshare.client.android.BuildConfig
import org.khawaja.fileshare.client.android.NavHomeDirections
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.app.Activity
import org.khawaja.fileshare.client.android.database.model.SharedText
import org.khawaja.fileshare.client.android.database.model.Transfer
import org.khawaja.fileshare.client.android.database.model.WebTransfer
import org.khawaja.fileshare.client.android.databinding.LayoutUserProfileBinding
import org.khawaja.fileshare.client.android.util.PrivacyPolicyCustomDialog

import org.khawaja.fileshare.client.android.viewmodel.ChangelogViewModel
import org.khawaja.fileshare.client.android.viewmodel.CrashLogViewModel
import org.khawaja.fileshare.client.android.viewmodel.UserProfileViewModel
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import org.khawaja.fileshare.client.android.fragment.TransferHistoryFragment
import org.khawaja.fileshare.client.android.util.Transfers


@AndroidEntryPoint
class HomeActivity : Activity(), NavigationView.OnNavigationItemSelectedListener {
    private val changelogViewModel: ChangelogViewModel by viewModels()

    private val crashLogViewModel: CrashLogViewModel by viewModels()

    private val userProfileViewModel: UserProfileViewModel by viewModels()


    private val userProfileBinding by lazy {
        LayoutUserProfileBinding.bind(navigationView.getHeaderView(0)).also {
            it.viewModel = userProfileViewModel
            it.lifecycleOwner = this
            it.editProfileClickListener = View.OnClickListener {
                openItem(R.id.edit_profile)
            }
        }
    }


    private val navigationView: NavigationView by lazy {
        findViewById(R.id.nav_view)
    }

    private val drawerLayout: DrawerLayout by lazy {
        findViewById(R.id.drawer_layout)
    }

    private var pendingMenuItemId = 0

    private val navController by lazy {
        navController(R.id.nav_host_fragment)
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        window.statusBarColor = ContextCompat.getColor(this@HomeActivity, R.color.colorPrimary)

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

        //defaultPreferences.edit().putBoolean("clone_active",false).apply()

        defaultPreferences.edit {
            putBoolean("clone_active", false)
        }



        //preferences?.edit()?.putBoolean("clone_active", true)?.apply()





        toolbar.setupWithNavController(navController, AppBarConfiguration(navController.graph, drawerLayout))
        toolbar.setTitleTextColor(Color.WHITE)
        //toolbar.setNavigationIcon(R.drawable.ic_baseline_account_circle_24)
        toolbar.popupTheme = R.style.ToolbarColoredBackArrow
        toolbar.background = resources.getDrawable(R.color.colorPrimary)
        navigationView.setNavigationItemSelectedListener(this)
        navController.addOnDestinationChangedListener { _, destination, _ -> title = destination.label }



        userProfileBinding.executePendingBindings()

        if (BuildConfig.FLAVOR == "googlePlay") {
            //navigationView.menu.findItem(R.id.donate).isVisible = true
        }

        if (hasIntroductionShown()) {
            if (changelogViewModel.shouldShowChangelog) {
                //navController.navigate(NavHomeDirections.actionGlobalChangelogFragment())
            } else if (crashLogViewModel.shouldShowCrashLog) {
                //navController.navigate(NavHomeDirections.actionGlobalCrashLogFragment())
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        when (intent?.action) {
            ACTION_OPEN_TRANSFER_DETAILS -> if (intent.hasExtra(EXTRA_TRANSFER)) {
                val transfer = intent.getParcelableExtra<Transfer>(EXTRA_TRANSFER)
                if (transfer != null) {
                    navController.navigate(NavHomeDirections.actionGlobalNavTransferDetails(transfer))
                }
            }
            ACTION_OPEN_SHARED_TEXT -> if (intent.hasExtra(EXTRA_SHARED_TEXT)) {
                val sharedText = intent.getParcelableExtra<SharedText>(EXTRA_SHARED_TEXT)
                if (sharedText != null) {
                    navController.navigate(NavHomeDirections.actionGlobalNavTextEditor(sharedText = sharedText))
                }
            }
            ACTION_OPEN_WEB_TRANSFER -> if (intent.hasExtra(EXTRA_WEB_TRANSFER)) {
                val webTransfer = intent.getParcelableExtra<WebTransfer>(EXTRA_WEB_TRANSFER)
                if (webTransfer != null) {
                    Log.d("HomeActivity", "onNewIntent: $webTransfer")
                    navController.navigate(NavHomeDirections.actionGlobalWebTransferDetailsFragment(webTransfer))
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        openItem(item.itemId)
        return true
    }

    //Alert dialog positive and negative buttons
    val positiveButtonClick1 = { dialog: DialogInterface, which: Int ->
        //Toast.makeText(applicationContext, android.R.string.yes, Toast.LENGTH_SHORT).show()
        finish()

    }
    val negativeButtonClick2 = { dialog: DialogInterface, which: Int ->
        //Toast.makeText(applicationContext, android.R.string.no, Toast.LENGTH_SHORT).show()
    }



    override fun onStart() {
        super.onStart()

        backPressedCount = 0
        Log.d("home_started", "resumed")

    }

    override fun onStop() {
        super.onStop()
        backPressedCount = 1
        Log.d("home_started", "destroyed")
    }

    override fun onBackPressed() {


        val count = supportFragmentManager.backStackEntryCount




        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.close()
        }
        else{
            super.onBackPressed()
        }
        /*else if (count == 0  ){
&& backPressedCount == 0
            val builder = android.app.AlertDialog.Builder(this)
            with(builder)
            {
                setTitle(R.string.exitmessage)
                setTheme(R.style.Theme_TrebleShot)
                setCancelable(true)
                setPositiveButton("ok", DialogInterface.OnClickListener(function = positiveButtonClick1))
                setNegativeButton(android.R.string.no, negativeButtonClick2)
                show()

            }

        }
        else if(count == 0 && backPressedCount != 0) {

            super.onBackPressed()
        }*/


        /*else if (count == 0)
        {

            val builder = android.app.AlertDialog.Builder(this)
            with(builder)
            {
                setTitle(R.string.exitmessage)
                setTheme(R.style.Theme_TrebleShot)
                setCancelable(true)
                setPositiveButton("ok", DialogInterface.OnClickListener(function = positiveButtonClick1))
                setNegativeButton(android.R.string.no, negativeButtonClick2)
                show()

            }

        }
        else
         {
            super.onBackPressed()
         }*/

        Log.d("backstack_count", count.toString())
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
                startActivity(Intent(this@HomeActivity, WelcomeActivity::class.java))
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

    private fun openItem(@IdRes id: Int) {
        pendingMenuItemId = id
        drawerLayout.close()
    }

    companion object {


        public var backPressedCount = 0

        const val ACTION_OPEN_TRANSFER_DETAILS = "org.khawaja.fileshare.client.android.action.OPEN_TRANSFER_DETAILS"

        const val ACTION_OPEN_WEB_TRANSFER = "org.khawaja.fileshare.client.android.action.OPEN_WEB_TRANSFER"

        const val ACTION_OPEN_SHARED_TEXT = "org.khawaja.fileshare.client.android.action.OPEN_SHARED_TEXT"

        const val EXTRA_TRANSFER = "extraTransfer"

        const val EXTRA_WEB_TRANSFER = "extraWebTransfer"

        const val EXTRA_SHARED_TEXT = "extraSharedText"
    }
}
