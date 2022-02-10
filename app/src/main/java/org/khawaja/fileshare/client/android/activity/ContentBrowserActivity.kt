package org.khawaja.fileshare.client.android.activity

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.app.Activity
import org.khawaja.fileshare.client.android.fragment.ContentBrowserFragmentDirections
import org.khawaja.fileshare.client.android.util.Resources.attrToRes
import org.khawaja.fileshare.client.android.util.Resources.resToDrawable
import android.os.Looper
import androidx.navigation.fragment.findNavController
import androidx.core.app.ActivityCompat

import androidx.core.app.ActivityCompat.requestPermissions

import android.content.DialogInterface

import android.annotation.TargetApi

import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog


/*
Implemented File browser for selection of file.
Option to select multiple files
view model for online phones in a network*/

@AndroidEntryPoint
class ContentBrowserActivity : Activity() {
    private val navController by lazy {
        navController(R.id.nav_host_fragment)
    }


    val PERMISSIONS_REQUEST_READ_CONTACTS = 1

    companion object{

        var intfileoption =0
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_browser)


        requestContactPermission()

        val mIntent = getIntent()
        intfileoption = mIntent.getIntExtra("intVariableName", 0)

        Log.d("int_passed", intfileoption.toString())




        val cloneActive = defaultPreferences.getBoolean("clone_active", false)

        if(cloneActive == true){
            //findViewById<MaterialCardView>(R.id.universalCloneButton).visibility = View.VISIBLE
        }




        window.statusBarColor = ContextCompat.getColor(this@ContentBrowserActivity, R.color.colorPrimary)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitleTextColor(Color.WHITE)
        val appBarLayout = findViewById<AppBarLayout>(R.id.app_bar)
        val toolbarDefaultBg = R.attr.backgroundTopBar.attrToRes(this).resToDrawable(this)



        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            title = destination.label

            val bg = when (destination.id) {
                R.id.contentBrowserFragment, R.id.prepareIndexFragment, R.id.webShareLauncherFragment,
                R.id.selectionsFragment -> null
                else -> toolbarDefaultBg
            }
            if (Build.VERSION.SDK_INT < 16) {
                @Suppress("DEPRECATION")
                appBarLayout.setBackgroundDrawable(bg)
            } else {
                appBarLayout.background = bg
            }
        }





    }




    override fun onBackPressed() {

        val cloneActive = defaultPreferences.getBoolean("clone_active", false)
        if(cloneActive == true){

            if(defaultPreferences.getBoolean("double_click", false) == true && defaultPreferences.getBoolean("content_sharing_active", false) ==false){
                startActivity(Intent(this@ContentBrowserActivity, TransferTypeActivity::class.java))
                finish()
            }
            else{

                //finishes the whole activity
                /*
                val intent = intent
                finish()
                startActivity(intent)*/


                startActivity(getIntent())
                finish()

                //recreate()


                /*startActivity(getIntent())
                finish()
                overridePendingTransition(0, 0)*/
            }

            if (defaultPreferences.getBoolean("content_sharing_active", false) == true){
                //Toast.makeText(this, "contsharing active", Toast.LENGTH_SHORT).show();

                /*startActivity(getIntent())
                finish()*/




                /*val intent = intent
                finish()
                startActivity(intent)*/


                //recreate()
            }



            defaultPreferences.edit {
                putBoolean("double_click", true)
            }
            Toast.makeText(this, "Refreshing Contents...", Toast.LENGTH_LONG).show();
            //super.onBackPressed()


            Handler(Looper.getMainLooper()).postDelayed(
                {
                    defaultPreferences.edit {
                        putBoolean("double_click", false)
                    }
                },
                2000 // value in milliseconds
            )


        }
        else{
            var intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }



        //startActivity(Intent(this@ContentBrowserActivity, HomeActivity::class.java))
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /*override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@ContentBrowserActivity, HomeActivity::class.java))
    }*/

    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            startActivity(Intent(this@ContentBrowserActivity, HomeActivity::class.java))
            return navController.navigateUp()
            finish()

        }

        //return super.onOptionsItemSelected(item)
    }*/


    fun requestContactPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_CONTACTS
                    )
                ) {
                    val builder: AlertDialog.Builder = androidx.appcompat.app.AlertDialog.Builder(this)
                    builder.setTitle("Read Contacts permission")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setMessage("Please enable access to contacts.")
                    builder.setOnDismissListener(DialogInterface.OnDismissListener {
                        requestPermissions(
                            arrayOf(
                                Manifest.permission.READ_CONTACTS
                            ), PERMISSIONS_REQUEST_READ_CONTACTS
                        )
                    })
                    builder.show()
                } else {
                    requestPermissions(
                        this, arrayOf(Manifest.permission.READ_CONTACTS),
                        PERMISSIONS_REQUEST_READ_CONTACTS
                    )
                }
            } else {
                //getContacts()
            }
        } else {
            //getContacts()
        }
    }


}
