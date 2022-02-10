package org.khawaja.fileshare.client.android.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import android.provider.Settings
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivity

import androidx.core.content.ContextCompat
import com.simplemobiletools.commons.extensions.hasPermission
import com.simplemobiletools.commons.extensions.toast
import hilt_aggregated_deps._org_khawaja_fileshare_client_android_activity_PackageInstallerActivity_GeneratedInjector
import kotlinx.coroutines.awaitAll
import org.khawaja.fileshare.client.android.databinding.*
import java.util.jar.Manifest
import kotlin.math.log
import android.widget.Toast

import android.graphics.Bitmap

import android.os.Environment
import android.text.format.DateFormat
import androidx.annotation.RequiresApi


private var context: Context? = null
@AndroidEntryPoint
class WelcomeActivity : Activity() {

    private var all_files_access_global_permission : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (hasIntroductionShown()) {
            finish()
        }

        context = this


// test masterjjjsog
        setContentView(R.layout.activity_welcome)

        skipPermissionRequest = true
        welcomePageDisallowed = true

        val titleContainer: FrameLayout = findViewById(R.id.titleContainer)
        val titleText: TextView = findViewById(R.id.title)
        val nextButton: ImageButton = findViewById(R.id.nextButton)
        val previousButton: TextView = findViewById(R.id.previousButton)
        val progressBar = findViewById<LinearProgressIndicator>(R.id.progressBarWelcome)

        val viewPager = findViewById<ViewPager2>(R.id.activity_welcome_view_pager)
        val appliedColor: Int = R.attr.colorSecondary.attrToRes(this).resToColor(this)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            val wrapDrawable = DrawableCompat.wrap(progressBar.indeterminateDrawable!!)
            DrawableCompat.setTint(wrapDrawable, ContextCompat.getColor(applicationContext, R.color.colorPrimary))
            progressBar.setIndeterminateDrawable(DrawableCompat.unwrap(wrapDrawable))
        } else {
            progressBar.indeterminateDrawable!!.setColorFilter(ContextCompat.getColor(applicationContext, R.color.colorPrimary), PorterDuff.Mode.SRC_IN)
        }



        val adapter = IntroductionFragmentStateAdapter(this, supportFragmentManager, lifecycle)

        //adapter.add(PageItem(getString(R.string.welcome), IntroductionSplashFragment::class.java.name))
        adapter.add(PageItem(getString(R.string.seamless_sharing), WalkThroughOneFragment::class.java.name))
        adapter.add(PageItem(getString(R.string.manage_files_and_folders), WalkThroughTwoFragment::class.java.name))
        adapter.add(PageItem(getString( R.string.manage_history), WalkThroughThreeFragment::class.java.name))

        val permissionPosition = adapter.itemCount
        if (Build.VERSION.SDK_INT >= 23) adapter.add(
            PageItem(getString(R.string.introduction_set_up_permissions), IntroductionPermissionFragment::class.java.name)
        )


        adapter.add(PageItem(getString(R.string.introduction_set_up_profile), IntroductionProfileFragment::class.java.name))
        //adapter.add(PageItem(getString(R.string.introduction_personalize), IntroductionPrefsFragment::class.java.name))
        //adapter.add(PageItem(getString(R.string.introduction_finished), IntroductionAllSetFragment::class.java.name))

        progressBar.max = (adapter.itemCount - 1) * 100

        previousButton.setOnClickListener {
            if (viewPager.currentItem - 1 >= 0) {
                viewPager.setCurrentItem(viewPager.currentItem - 1, true)
            }
        }



        nextButton.setOnClickListener {

            var all_file_permission_granted = defaultPreferences.getBoolean("all_files", false)

            Log.d("request_code_all_file", all_file_permission_granted.toString() + " inside")


            if (viewPager.currentItem + 1 < adapter.itemCount) {
                viewPager.currentItem = viewPager.currentItem + 1
            } else {
                if (Permissions.checkRunningConditions(applicationContext) || all_file_permission_granted == true) {
                    // end presentation
                    defaultPreferences.edit {
                        putBoolean("introduction_shown", true)
                    }
                    backend.ensureStartedAfterWelcoming()
                    startActivity(Intent(this@WelcomeActivity, TransferTypeActivity::class.java))

                    finish()
                } else {
                    viewPager.setCurrentItem(permissionPosition, true)
                    Toast.makeText(this, R.string.warning_permissions_not_accepted, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    progressBar.progress = position * 100 + (positionOffset * 100).toInt()
                    if (position == 0) {
                        progressBar.alpha = positionOffset
                        previousButton.alpha = positionOffset
                        titleText.alpha = positionOffset
                    } else {
                        progressBar.alpha = 1.0f
                        previousButton.alpha = 1.0f
                        titleText.alpha = 1.0f
                    }
                }

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val lastPage = position == adapter.itemCount - 1
                    TransitionManager.beginDelayedTransition(titleContainer)

                    titleText.text = adapter.getItem(position).title
                    progressBar.visibility = if (lastPage) View.GONE else View.VISIBLE

                    nextButton.setImageResource(
                        if (lastPage) {
                            /*R.drawable.ic_check_white_24dp*/
                            R.drawable.walkthrough_continue_button

                        } else {
                            /*R.drawable.ic_navigate_next_white_24dp*/
                            R.drawable.walkthrough_continue_button
                        }
                    )
                }
            }
        )

        viewPager.adapter = adapter
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("request_code_all_file", requestCode.toString())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("request_code_all_file", requestCode.toString())
    }




    override fun onResume() {
        super.onResume()





        //var all_file_permission_granted = defaultPreferences.getBoolean("all_files", false)
        //Log.d("request_code_all_file", all_file_permission_granted.toString())

        if (Build.VERSION.SDK_INT >=30) {
            if (Environment.isExternalStorageManager()) {
                Log.d("request_code_all_file", "granted Environment")
                defaultPreferences.edit {
                    putBoolean("all_files", true)
                }
            } else {
                Log.d("request_code_all_file", "not granted Environment")
            }
        }

    }

    private fun hasPermissions(
        context: Context?,
        vararg permissions: String
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }
}

class IntroductionSplashFragment : Fragment(R.layout.layout_splash) {
    private lateinit var binding: LayoutSplashBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = LayoutSplashBinding.bind(view)
    }

    override fun onResume() {
        super.onResume()
        slideSplashView()
    }

    private fun slideSplashView() {
        val context = context ?: return
        binding.logo.animation = loadAnimation(context, R.anim.enter_from_bottom_centered)
        binding.appBrand.animation = loadAnimation(context, R.anim.enter_from_bottom)
        binding.welcomeText.animation = loadAnimation(context, android.R.anim.fade_in).also { it.startOffset = 1000 }
    }
}


class WalkThroughOneFragment : Fragment(R.layout.layout_walkthrough_one) {
    private lateinit var binding: LayoutWalkthroughOneBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = LayoutWalkthroughOneBinding.bind(view)
    }
}

class WalkThroughTwoFragment : Fragment( R.layout.layout_walkthrough_two){
    private lateinit var  binding: LayoutWalkthroughTwoBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
         super.onViewCreated(view, savedInstanceState)
        binding = LayoutWalkthroughTwoBinding.bind(view)
    }
}

class WalkThroughThreeFragment : Fragment(R.layout.layout_walkthrough_three){
    private lateinit var binding : LayoutWalkthroughThreeBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = LayoutWalkthroughThreeBinding.bind(view)
    }
}

//class IntroductionWalkthroughFragment : Fragment(R.layout.layout_con)


@AndroidEntryPoint
class IntroductionPermissionFragment : Fragment(R.layout.layout_permissions) {
    private val requestPermissions = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        updatePermissionsState()
    }

    private val adapter = PermissionsAdapter {
        requestPermissions.launch(it.perm.id)
    }.apply {
        setHasStableIds(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = adapter

        updatePermissionsState()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionsState()
    }

    private fun updatePermissionsState() {
        adapter.submitList(
            Permissions.getAll().map {
                CheckedPermission(it, checkSelfPermission(requireContext(), it.id) == PERMISSION_GRANTED)
            }
        )
    }
}

@AndroidEntryPoint
class IntroductionProfileFragment : Fragment(R.layout.layout_set_up_profile) {
    private val viewModel: UserProfileViewModel by viewModels()

    private val pickPhoto = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            viewModel.saveProfilePicture(uri)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = LayoutProfileEditorBinding.bind(view.findViewById(R.id.profile_editor))


        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.pickPhotoClickListener = View.OnClickListener {
            pickPhoto.launch("image/*")
        }

        binding.executePendingBindings()
    }
}

class IntroductionPrefsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preference_introduction_look)
        loadThemeOptionsTo(requireContext(), findPreference("theme"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.setTheme(R.style.custom_color_accent)
        listView.layoutParams = FrameLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER_VERTICAL
        }
    }

    companion object {
        fun loadThemeOptionsTo(context: Context, themePreference: ListPreference?) {
            if (themePreference == null) return

            val valueList: MutableList<String> = arrayListOf("light", "dark")
            val titleList: MutableList<String> = arrayListOf(
                context.getString(R.string.light_theme),
                context.getString(R.string.dark_theme)
            )

            if (Build.VERSION.SDK_INT >= 26) {
                valueList.add("system")
                titleList.add(context.getString(R.string.follow_system_theme))
            } else if (Build.VERSION.SDK_INT >= 21) {
                valueList.add("battery")
                titleList.add(context.getString(R.string.battery_saver_theme))
            }

            themePreference.entries = titleList.toTypedArray()
            themePreference.entryValues = valueList.toTypedArray()
        }
    }
}

class IntroductionAllSetFragment : Fragment(R.layout.layout_introduction_all_set)

class IntroductionFragmentStateAdapter(
    val context: Context, fm: FragmentManager, lifecycle: Lifecycle,
) : FragmentStateAdapter(fm, lifecycle) {
    private val fragments: MutableList<PageItem> = ArrayList()

    private val fragmentFactory: FragmentFactory = fm.fragmentFactory

    fun add(fragment: PageItem) {
        fragments.add(fragment)
    }

    override fun createFragment(position: Int): Fragment {
        val item = getItem(position)
        val fragment = item.fragment ?: fragmentFactory.instantiate(context.classLoader, item.clazz)

        item.fragment = fragment

        return fragment
    }

    override fun getItemCount(): Int = fragments.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun getItem(position: Int): PageItem = synchronized(fragments) { fragments[position] }

    @Parcelize
    data class PageItem(var title: String, var clazz: String) : Parcelable {
        @IgnoredOnParcel
        var fragment: Fragment? = null
    }
}

class PermissionContentViewModel(permission: CheckedPermission) {
    val title = permission.perm.title

    val description = permission.perm.description

    val granted = permission.granted
}


data class CheckedPermission(val perm: Permission, val granted: Boolean)

class PermissionItemCallback : DiffUtil.ItemCallback<CheckedPermission>() {
    override fun areItemsTheSame(oldItem: CheckedPermission, newItem: CheckedPermission): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: CheckedPermission, newItem: CheckedPermission): Boolean {
        return oldItem != newItem
    }
}



class PermissionViewHolder(
    private val clickListener: (CheckedPermission) -> Unit,
    private val binding: ListPermissionBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(permission: CheckedPermission) {
        binding.viewModel = PermissionContentViewModel(permission)
        binding.button.setOnClickListener {
            clickListener(permission)
        }
        if (Build.VERSION.SDK_INT >=30){
            binding.buttonAllFileAccess.visibility = View.VISIBLE
            binding.button.visibility = View.INVISIBLE
        }
        else{
            binding.button.visibility = View.VISIBLE
            binding.buttonAllFileAccess.visibility  = View.INVISIBLE
        }
        binding.buttonAllFileAccess.setOnClickListener {
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
            val uri = Uri.fromParts("package", context?.packageName, null)
            intent.data = uri
            context?.startActivity(intent)
        }
        binding.executePendingBindings()
    }
}

class PermissionsAdapter(
    private val clickListener: (CheckedPermission) -> Unit,
) : ListAdapter<CheckedPermission, PermissionViewHolder>(PermissionItemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionViewHolder {
        return PermissionViewHolder(
            clickListener,
            ListPermissionBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    override fun onBindViewHolder(holder: PermissionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).perm.id.hashCode().toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_PERMISSION
    }

    companion object {
        private const val VIEW_TYPE_PERMISSION = 0
    }
}
