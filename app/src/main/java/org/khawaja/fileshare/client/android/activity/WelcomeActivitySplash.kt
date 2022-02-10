package org.khawaja.fileshare.client.android.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils.loadAnimation
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.content.edit
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.DeterminateDrawable
import com.google.android.material.progressindicator.LinearProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.activity.IntroductionFragmentStateAdapter.PageItem
import org.khawaja.fileshare.client.android.app.Activity


import org.khawaja.fileshare.client.android.util.Permissions
import org.khawaja.fileshare.client.android.util.Permissions.Permission
import org.khawaja.fileshare.client.android.util.Resources.attrToRes
import org.khawaja.fileshare.client.android.util.Resources.resToColor
import org.khawaja.fileshare.client.android.viewmodel.UserProfileViewModel
import java.lang.Exception
import android.graphics.PorterDuff
import android.graphics.pdf.PdfDocument
import android.widget.*

import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import org.khawaja.fileshare.client.android.databinding.*
import org.khawaja.fileshare.client.android.util.PrivacyPolicyCustomDialog
import timber.log.Timber


@AndroidEntryPoint
class WelcomeActivitySplash : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*if (hasIntroductionShown()) {
            finish()
        }*/


        // test masterjjjsog
        setContentView(R.layout.layout_welcome_splash)


        skipPermissionRequest = true
        welcomePageDisallowed = true


        val startButton: MaterialButton = findViewById(R.id.startButton)
        startButton.setOnClickListener {
            //startActivity(Intent(this@WelcomeActivitySplash, HomeActivity::class.java))
            Timber.tag("welcome_started").d("welcome activity started")
            startActivity(Intent(this@WelcomeActivitySplash, TransferTypeActivity::class.java))
            finish()
        }

        val privacyPolicyText  : MaterialTextView = findViewById(R.id.privacy_policy_splash)
        privacyPolicyText.setOnClickListener {
            val manager = supportFragmentManager
            val privacyPolicyCustomDialog = PrivacyPolicyCustomDialog()
            privacyPolicyCustomDialog.show(manager, "privacy policy dialog")
        }

    }
}


