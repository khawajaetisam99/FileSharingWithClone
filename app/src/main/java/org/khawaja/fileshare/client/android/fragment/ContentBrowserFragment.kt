package org.khawaja.fileshare.client.android.fragment

import android.annotation.SuppressLint
import android.app.usage.StorageStatsManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.khawaja.fileshare.client.android.R
import org.khawaja.fileshare.client.android.database.model.UTransferItem
import org.khawaja.fileshare.client.android.databinding.LayoutContentBrowserBinding


import org.khawaja.fileshare.client.android.fragment.ContentFragmentStateAdapter.PageItem
import org.khawaja.fileshare.client.android.fragment.content.*
import org.khawaja.fileshare.client.android.util.CommonErrors
import org.khawaja.fileshare.client.android.viewmodel.ClientPickerViewModel
import org.khawaja.fileshare.client.android.viewmodel.SharingSelectionViewModel
import org.khawaja.fileshare.client.android.viewmodel.SharingState
import org.khawaja.fileshare.client.android.viewmodel.SharingViewModel


import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.helpers.isOreoPlus
import org.khawaja.fileshare.client.android.filemanagerstats.activities.MimeTypesActivity
import org.khawaja.fileshare.client.android.filemanagerstats.activities.SimpleActivity
import org.khawaja.fileshare.client.android.filemanagerstats.extensions.formatSizeThousand
import org.khawaja.fileshare.client.android.filemanagerstats.helpers.*




import android.provider.Settings
import android.util.AttributeSet



import javax.inject.Inject
import android.content.Intent.getIntent

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.khawaja.fileshare.client.android.activity.ContentBrowserActivity
import org.khawaja.fileshare.client.android.filemanagerstats.fragments.MyViewPagerFragment
import org.khawaja.fileshare.client.android.filemanagerstats.fragments.StorageFragment
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.log
import android.content.SharedPreferences
import androidx.activity.OnBackPressedCallback
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.android.material.card.MaterialCardView
import timber.log.Timber
import android.util.DisplayMetrics
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.PagerAdapter
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking


@AndroidEntryPoint
class ContentBrowserFragment : Fragment(R.layout.layout_content_browser)
{
    private val SIZE_DIVIDER = 100000
    private val selectionViewModel: SharingSelectionViewModel by activityViewModels()

    private val clientPickerViewModel: ClientPickerViewModel by activityViewModels()

    private val sharingViewModel: SharingViewModel by viewModels()

    private val contentBrowserViewModel: ContentBrowserViewModel by activityViewModels()

    private lateinit var viewBinding: LayoutContentBrowserBinding


    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            //browserViewModel.showBuckets()
        }

    }

    var preferences = activity?.getSharedPreferences("clone_pref", Context.MODE_PRIVATE)


    protected val defaultPreferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(activity)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        defaultPreferences.edit {
            putBoolean("content_sharing_active", false)
        }
    }






    companion object {
        //lateinit var filesSizeCompanion : Long
        var imagesSizeCompanion : Long =0
        var videosSizeCompanion : Long =0
        var audioSizeCompanion : Long=0
        var documentsSizeCompanion : Long=0
        var archivesSizeCompanion : Long=0
        var othersSizeCompanion : Long=0
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = LayoutContentBrowserBinding.bind(view)

        ///////////////////////////////////////////////////////////////////////////////////////////
        viewBinding = LayoutContentBrowserBinding.inflate(layoutInflater)

        binding.totalSpace.text = String.format(getString(R.string.total_storage), "…")


        /*val childFragment: MyViewPagerFragment = StorageFragment( )
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.child_fragment_container, childFragment).commit()*/


        // Mime types progresses click listeners
        binding.freeSpaceHolder.setOnClickListener {
            try {
                //val storageSettingsIntent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
                //startActivity(storageSettingsIntent)
            } catch (e: Exception) {
                Log.e("Storage settings error", "onViewCreated: storage setting error")
            }
        }


        //videos_holder.setOnClickListener { launchMimetypeActivity(VIDEOS) }
        //audio_holder.setOnClickListener { launchMimetypeActivity(AUDIO) }
        //documents_holder.setOnClickListener { launchMimetypeActivity(DOCUMENTS) }
        //archives_holder.setOnClickListener { launchMimetypeActivity(ARCHIVES) }
        //others_holder.setOnClickListener { launchMimetypeActivity(OTHERS) }



        /*fun firstGetTheStorageStatsAsync() = GlobalScope.async{

        }


        fun secondSetAllStorageStatsAsync() = GlobalScope.async{

        }

        runBlocking {

            firstGetTheStorageStatsAsync().await()
            secondSetAllStorageStatsAsync().await()
        }*/





        GlobalScope.async {


            getMainStorageStats(view.context, binding)

            val filesSize = getSizesByMimeType(view)
            val imagesSize = filesSize[IMAGES]!!
            val videosSize = filesSize[VIDEOS]!!
            val audioSize = filesSize[AUDIO]!!
            val documentsSize = filesSize[DOCUMENTS]!!
            val archivesSize = filesSize[ARCHIVES]!!
            val othersSize = filesSize[OTHERS]!!


            //filesSizeCompanion = filesSize.toString()
            imagesSizeCompanion = imagesSize
            videosSizeCompanion = videosSize
            audioSizeCompanion = audioSize
            documentsSizeCompanion = documentsSize
            archivesSizeCompanion = archivesSize
            othersSizeCompanion = archivesSize

            othersSizeCompanion = othersSize

            Log.d(
                "_filesfiles_content",
                "_filesfiles" + filesSize + imagesSize + videosSize + audioSize + documentsSize + archivesSize + othersSize
            )

            /*post {

            }*/
            Handler(Looper.getMainLooper()).post {


                val metrics = requireContext().resources.displayMetrics
                var dp = 20f
                val fpixels = metrics.density * dp

                binding.imagesSize.text = imagesSize.formatSize()
                binding.imagesProgressbar.progress = (imagesSize / SIZE_DIVIDER).toInt() + (videosSize / SIZE_DIVIDER).toInt()

                binding.videosSize.text = videosSize.formatSize()
                binding.videosProgressbar.progress = (videosSize / SIZE_DIVIDER).toInt() + (imagesSize / SIZE_DIVIDER).toInt() + (videosSize / SIZE_DIVIDER).toInt()

                Timber.d(binding.videosProgressbar.progress.toString())
                Timber.tag("progress_width").d(binding.videosProgressbar.progress.toString())

                binding.audioSize.text = audioSize.formatSize()
                binding.audioProgressbar.progress = (audioSize / SIZE_DIVIDER).toInt() + (videosSize / SIZE_DIVIDER).toInt() + (imagesSize / SIZE_DIVIDER).toInt() + (videosSize / SIZE_DIVIDER).toInt()

                binding.documentsSize.text = documentsSize.formatSize()
                binding.documentsProgressbar.progress = (documentsSize / SIZE_DIVIDER).toInt() + (audioSize / SIZE_DIVIDER).toInt() + (videosSize / SIZE_DIVIDER).toInt() + (imagesSize / SIZE_DIVIDER).toInt() + (videosSize / SIZE_DIVIDER).toInt()

                binding.archivesSize.text = archivesSize.formatSize()
                binding.archivesProgressbar.progress = (archivesSize / SIZE_DIVIDER).toInt() + (documentsSize / SIZE_DIVIDER).toInt() + (audioSize / SIZE_DIVIDER).toInt() + (videosSize / SIZE_DIVIDER).toInt() + (imagesSize / SIZE_DIVIDER).toInt() + (videosSize / SIZE_DIVIDER).toInt()

                binding.othersSize.text = othersSize.formatSize()
                binding.othersProgressbar.progress = (othersSize / SIZE_DIVIDER).toInt() + (archivesSize / SIZE_DIVIDER).toInt() + (documentsSize / SIZE_DIVIDER).toInt() + (audioSize / SIZE_DIVIDER).toInt() + (videosSize / SIZE_DIVIDER).toInt() + (imagesSize / SIZE_DIVIDER).toInt() + (videosSize / SIZE_DIVIDER).toInt()

                /*binding.othersSize.text = archivesSize.formatSize()
                binding.othersProgressbar.progress = (archivesSize / SIZE_DIVIDER).toInt()*/

            }


        }



        getSizes(view, viewBinding)
        setStorageDetails(view, viewBinding)


        //binding.mainStorageUsageProgressbar.setIndicatorColor(resources.getColor(R.color.colorPrimary))
        binding.mainStorageUsageProgressbar.setIndicatorColor(view.context.resources.getColor(R.color.colorTranslucent))
        binding.mainStorageUsageProgressbar.trackColor =
            resources.getColor(R.color.colorPrimary).adjustAlpha(0.3f)

        val redColor = view.context.resources.getColor(R.color.md_pink_700)
        binding.imagesProgressbar.setIndicatorColor(redColor)
        binding.imagesProgressbar.trackColor =
            view.context.resources.getColor(R.color.colorTranslucent)// = redColor.adjustAlpha(0.3f)

        val greenColor = view.context.resources.getColor(R.color.md_yellow_700)
        binding.videosProgressbar.setIndicatorColor(greenColor)
        binding.videosProgressbar.trackColor =
            view.context.resources.getColor(R.color.colorTranslucent)// = greenColor.adjustAlpha(0.3f)
        Timber.tag("progress_width").d(binding.videosProgressbar.width.toString())
        Timber.tag("progress_width").d(binding.videosProgressbar.measuredWidth.toString())

        val lightBlueColor = view.context.resources.getColor(R.color.md_teal_700)
        binding.audioProgressbar.setIndicatorColor(lightBlueColor)
        binding.audioProgressbar.trackColor =
            view.context.resources.getColor(R.color.colorTranslucent)// = lightBlueColor.adjustAlpha(0.3f)

        val yellowColor = view.context.resources.getColor(R.color.md_light_blue_700)
        binding.documentsProgressbar.setIndicatorColor(yellowColor)
        binding.documentsProgressbar.trackColor =
            view.context.resources.getColor(R.color.colorTranslucent)// = yellowColor.adjustAlpha(0.3f)

        val tealColor = view.context.resources.getColor(R.color.md_teal_700)
        binding.archivesProgressbar.setIndicatorColor(tealColor)
        binding.archivesProgressbar.trackColor =
            view.context.resources.getColor(R.color.colorTranslucent)
        //= tealColor.adjustAlpha(0.3f)

        val pinkColor = view.context.resources.getColor(R.color.md_deep_purple_700)
        binding.othersProgressbar.setIndicatorColor(pinkColor)
        binding.othersProgressbar.trackColor =
            view.context.resources.getColor(R.color.colorTranslucent)


        ////////////////////////////////////////////////////////////////////////////////////////////

        //var cloneSharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)

        Log.d("int_passed", ContentBrowserActivity.intfileoption.toString())

        //var cloneActivePref = activity?.getPreferences(Context.MODE_PRIVATE)


        //var cloneActive = preferences?.getBoolean("clone_active",true)
        //var cloneActive = cloneActivePref?.getBoolean("clone_active", true)


        val cloneActive = defaultPreferences.getBoolean("clone_active", false)

        val pagerAdapter =
            ContentFragmentStateAdapter(requireContext(), childFragmentManager, lifecycle)
        val snackbar = Snackbar.make(view, R.string.sending, Snackbar.LENGTH_INDEFINITE)



        if (cloneActive == false) {


            pagerAdapter.add(
                PageItem(
                    getString(R.string.photo),
                    ImageBrowserFragment::class.java.name
                )
            )
            pagerAdapter.add(
                PageItem(
                    getString(R.string.video),
                    VideoBrowserFragment::class.java.name
                )
            )
            pagerAdapter.add(
                PageItem(
                    getString(R.string.audios),
                    AudioBrowserFragment::class.java.name
                )
            )
            pagerAdapter.add(PageItem(getString(R.string.app), AppBrowserFragment::class.java.name))
            pagerAdapter.add(
                PageItem(
                    getString(R.string.files),
                    FileBrowserFragment::class.java.name
                )
            )

            pagerAdapter.add(
                PageItem(
                    getString(R.string.contacts),
                    ContactsBrowserFragment::class.java.name
                )
            )

            pagerAdapter.add(
                PageItem(
                    getString(R.string.advance),
                    AdvanceFileBrowserFragment::class.java.name
                )
            )




        } else {
            defaultPreferences.edit {
                putBoolean("images_clicked", false)
            }
            defaultPreferences.edit {
                putBoolean("videos_clicked", false)
            }
            defaultPreferences.edit {
                putBoolean("audios_clicked", false)
            }
            defaultPreferences.edit {
                putBoolean("apps_clicked", false)
            }

            //////////////////////////////////////////////


            //Set default zero status for all radio buttons.

            defaultPreferences.edit {
                putInt("documents_checked", 0)
            }
            defaultPreferences.edit {
                putInt("images_checked", 0)
            }
            defaultPreferences.edit {
                putInt("videos_checked", 0)
            }
            defaultPreferences.edit {
                putInt("audios_checked", 0)
            }
            defaultPreferences.edit {
                putInt("apps_checked", 0)
            }


            // When clone is active, make sure the Pager adapter populates only those fragments which are selected through the radio group
            // All the radio buttons areunchedked by default


            binding.imagesRadioButton.post { binding.imagesRadioButton.setChecked(false) }
            defaultPreferences.edit {
                putInt("images_checked", 0)
            }
            binding.videoRadioButton.post { binding.videoRadioButton.setChecked(false) }
            defaultPreferences.edit {
                putInt("videos_checked", 0)
            }
            binding.audioRadioButton.post { binding.audioRadioButton.setChecked(false) }
            defaultPreferences.edit {
                putInt("audios_checked", 0)
            }
            binding.appsRadioButton.post { binding.appsRadioButton.setChecked(false) }
            defaultPreferences.edit {
                putInt("apps_checked", 0)
            }
            binding.documentRadioButton.post { binding.documentRadioButton.setChecked(false) }
            defaultPreferences.edit {
                putInt("documents_checked", 0)
            }

            binding.imagesRadioButton.setOnClickListener {
                if (binding.imagesRadioButton.isChecked == true) {
                    Log.d("image_check", "image_check box is checked")
                    defaultPreferences.edit {
                        putInt("images_checked", 1)
                    }
                } else {
                    Log.d("image_check", "image_check box is unchecked")
                    defaultPreferences.edit {
                        putInt("images_checked", 0)
                    }
                }
            }

            binding.videoRadioButton.setOnClickListener {
                if (binding.videoRadioButton.isChecked == true) {
                    defaultPreferences.edit {
                        putInt("videos_checked", 2)
                    }
                } else {
                    defaultPreferences.edit {
                        putInt("videos_checked", 0)
                    }
                }
            }


            binding.audioRadioButton.setOnClickListener {
                if (binding.audioRadioButton.isChecked == true) {
                    defaultPreferences.edit {
                        putInt("audios_checked", 3)
                    }
                } else {
                    defaultPreferences.edit {
                        putInt("audios_checked", 0)
                    }

                }
            }

            binding.appsRadioButton.setOnClickListener {
                if (binding.appsRadioButton.isChecked == true) {
                    defaultPreferences.edit {
                        putInt("apps_checked", 4)
                    }
                } else {
                    defaultPreferences.edit {
                        putInt("apps_checked", 0)
                    }
                }
            }

            binding.documentRadioButton.setOnClickListener {
                if (binding.documentRadioButton.isChecked == true) {
                    defaultPreferences.edit {
                        putInt("documents_checked", 5)
                    }
                } else {
                    defaultPreferences.edit {
                        putInt("documents_checked", 0)
                    }

                }
            }


            pagerAdapter.add(
                PageItem(
                    getString(R.string.files),
                    FileBrowserfragmentInitializer::class.java.name
                )
            )
            pagerAdapter.add(
                PageItem(
                    getString(R.string.photo),
                    ImageBrowserFragment::class.java.name
                )
            )
            pagerAdapter.add(
                PageItem(
                    getString(R.string.video),
                    VideoBrowserFragment::class.java.name
                )
            )
            pagerAdapter.add(
                PageItem(
                    getString(R.string.audios),
                    AudioBrowserFragment::class.java.name
                )
            )
            pagerAdapter.add(PageItem(getString(R.string.app), AppBrowserFragment::class.java.name))

            pagerAdapter.add(
                PageItem(
                    getString(R.string.files),
                    FileBrowserFragment::class.java.name
                )
            )


        }

        if (cloneActive == true) {
            binding.universalCloneButton.visibility = View.VISIBLE
        }


        // check if clone is active, and which filter is being selected
        binding.universalCloneButton.setOnClickListener {

            Timber.d("checked_arrays_received      initialized")





            if (defaultPreferences.getBoolean("images_clicked", false) == true) {
                binding.tabLayout.getTabAt(1)?.select()
                Log.d("request_code_all_file", "scrolled to 1")
                findNavController().navigate(
                    ContentBrowserFragmentDirections.actionContentBrowserFragmentToPrepareIndexFragment()
                )
            } else if (defaultPreferences.getBoolean("videos_clicked", false) == true) {
                binding.tabLayout.getTabAt(2)?.select()
                Log.d("request_code_all_file", "scrolled to 2")
                findNavController().navigate(
                    ContentBrowserFragmentDirections.actionContentBrowserFragmentToPrepareIndexFragment()
                )
            } else if (defaultPreferences.getBoolean("audios_clicked", false) == true) {
                binding.tabLayout.getTabAt(3)?.select()
                Log.d("request_code_all_file", "scrolled to 3")
                findNavController().navigate(
                    ContentBrowserFragmentDirections.actionContentBrowserFragmentToPrepareIndexFragment()
                )
            } else if (defaultPreferences.getBoolean("apps_clicked", false) == true) {
                binding.tabLayout.getTabAt(4)?.select()
                Log.d("request_code_all_file", "scrolled to 4")
                findNavController().navigate(
                    ContentBrowserFragmentDirections.actionContentBrowserFragmentToPrepareIndexFragment()
                )
            } else {


                var documentsStatus = defaultPreferences.getInt("documents_checked", 0)
                var imagesStatus = defaultPreferences.getInt("images_checked", 0)
                var videosStatus = defaultPreferences.getInt("videos_checked", 0)
                var audiosStatus = defaultPreferences.getInt("audios_checked", 0)
                var apksStatus = defaultPreferences.getInt("apps_checked", 0)

                //Toast.makeText(context, documentsStatus.toString(), Toast.LENGTH_SHORT).show()

                if (imagesStatus != 0 && videosStatus == 0 && audiosStatus == 0 && apksStatus == 0) {


                    binding.tabLayout.post { binding.tabLayout.getTabAt(1)?.select() }

                    //binding.tabLayout.getTabAt(1)?.select()
                    Log.d("request_code_all_file", "scrolled to 1")

                    binding.universalCloneButton.isEnabled = false


                    binding.pbLoading.pbLoadingWrapper.visibility = View.VISIBLE

                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            binding.pbLoading.pbLoadingWrapper.visibility = View.INVISIBLE
                            binding.universalCloneButton.isEnabled = true
                            findNavController().navigate(
                                ContentBrowserFragmentDirections.actionContentBrowserFragmentToPrepareIndexFragment()
                            )
                        },
                        4000 // value in milliseconds
                    )


                } else if (videosStatus != 0 && imagesStatus == 0 && audiosStatus == 0 && apksStatus == 0) {

                    binding.tabLayout.getTabAt(2)?.select()
                    Log.d("request_code_all_file", "scrolled to 2")

                    binding.universalCloneButton.isEnabled = false


                    binding.pbLoading.pbLoadingWrapper.visibility = View.VISIBLE

                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            binding.pbLoading.pbLoadingWrapper.visibility = View.INVISIBLE
                            binding.universalCloneButton.isEnabled = true

                            findNavController().navigate(
                                ContentBrowserFragmentDirections.actionContentBrowserFragmentToPrepareIndexFragment()
                            )
                        },
                        4000 // value in milliseconds
                    )

                } else if (audiosStatus != 0 && videosStatus == 0 && imagesStatus == 0 && apksStatus == 0) {


                    binding.tabLayout.getTabAt(3)?.select()
                    Log.d("request_code_all_file", "scrolled to 3")
                    binding.universalCloneButton.isEnabled = false


                    binding.pbLoading.pbLoadingWrapper.visibility = View.VISIBLE

                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            binding.pbLoading.pbLoadingWrapper.visibility = View.INVISIBLE
                            binding.universalCloneButton.isEnabled = true

                            findNavController().navigate(
                                ContentBrowserFragmentDirections.actionContentBrowserFragmentToPrepareIndexFragment()
                            )
                        }, 4000 // value in milliseconds
                    )


                } else if (apksStatus != 0 && videosStatus == 0 && audiosStatus == 0 && imagesStatus == 0) {


                    binding.tabLayout.getTabAt(4)?.select()

                    /*binding.storageScrollview1.visibility = View.INVISIBLE
                    binding.contentTabs.visibility = View.VISIBLE*/

                    binding.universalCloneButton.isEnabled = false
                    

                    binding.pbLoading.pbLoadingWrapper.visibility = View.VISIBLE

                    

                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            binding.pbLoading.pbLoadingWrapper.visibility = View.INVISIBLE
                            binding.universalCloneButton.isEnabled = true
                            findNavController().navigate(
                                ContentBrowserFragmentDirections.actionContentBrowserFragmentToPrepareIndexFragment()
                            )
                        },
                        4000 // value in milliseconds
                    )

                    /*binding.tabLayout.getTabAt(4)?.select()
                    Log.d("request_code_all_file", "scrolled to 4")
                    findNavController().navigate(
                        ContentBrowserFragmentDirections.actionContentBrowserFragmentToPrepareIndexFragment()
                    )*/

                } else if (imagesStatus != 0 && videosStatus != 0 && audiosStatus == 0 && apksStatus == 0) {


                    binding.tabLayout.getTabAt(1)?.select()
                    Log.d("request_code_all_file", "scrolled to 1")


                    binding.universalCloneButton.isEnabled = false
                    binding.pbLoading.pbLoadingWrapper.visibility = View.VISIBLE



                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            /*binding.pbLoading.pbLoadingWrapper.visibility = View.INVISIBLE
                            binding.universalCloneButton.isEnabled = true*/

                            binding.tabLayout.getTabAt(2)?.select()
                            Log.d("request_code_all_file", "scrolled to 2")

                        },
                        3000 // value in milliseconds
                    )


                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            binding.pbLoading.pbLoadingWrapper.visibility = View.INVISIBLE
                            binding.universalCloneButton.isEnabled = true


                            findNavController().navigate(
                                ContentBrowserFragmentDirections.actionContentBrowserFragmentToPrepareIndexFragment()
                            )

                        },
                        7000 // value in milliseconds
                    )



                } else if (imagesStatus != 0 && videosStatus == 0 && audiosStatus != 0 && apksStatus == 0) {


                    binding.tabLayout.getTabAt(1)?.select()
                    Log.d("request_code_all_file", "scrolled to 1")


                    binding.universalCloneButton.isEnabled = false
                    binding.pbLoading.pbLoadingWrapper.visibility = View.VISIBLE



                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            /*binding.pbLoading.pbLoadingWrapper.visibility = View.INVISIBLE
                            binding.universalCloneButton.isEnabled = true*/


                            binding.tabLayout.getTabAt(3)?.select()
                            Log.d("request_code_all_file", "scrolled to 3")

                        },
                        4000 // value in milliseconds
                    )

                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            binding.pbLoading.pbLoadingWrapper.visibility = View.INVISIBLE
                            binding.universalCloneButton.isEnabled = true


                            findNavController().navigate(
                                ContentBrowserFragmentDirections.actionContentBrowserFragmentToPrepareIndexFragment()
                            )

                        },
                        6000 // value in milliseconds
                    )




                } else if (imagesStatus != 0 && videosStatus != 0 && audiosStatus != 0 && apksStatus == 0) {


                    binding.tabLayout.getTabAt(1)?.select()
                    Log.d("request_code_all_file", "scrolled to 1")


                    binding.universalCloneButton.isEnabled = false
                    binding.pbLoading.pbLoadingWrapper.visibility = View.VISIBLE



                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            /*binding.pbLoading.pbLoadingWrapper.visibility = View.INVISIBLE
                            binding.universalCloneButton.isEnabled = true*/

                            binding.tabLayout.getTabAt(2)?.select()
                            Log.d("request_code_all_file", "scrolled to 2")

                        },
                        3000 // value in milliseconds
                    )


                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            /*binding.pbLoading.pbLoadingWrapper.visibility = View.INVISIBLE
                            binding.universalCloneButton.isEnabled = true*/

                            binding.tabLayout.getTabAt(3)?.select()
                            Log.d("request_code_all_file", "scrolled to 3")

                        },
                        4000 // value in milliseconds
                    )


                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            binding.pbLoading.pbLoadingWrapper.visibility = View.INVISIBLE
                            binding.universalCloneButton.isEnabled = true

                            findNavController().navigate(
                                ContentBrowserFragmentDirections.actionContentBrowserFragmentToPrepareIndexFragment()
                            )

                        },
                        7000 // value in milliseconds
                    )



                } else if (imagesStatus == 0 && videosStatus != 0 && audiosStatus != 0 && apksStatus == 0) {


                    binding.tabLayout.getTabAt(2)?.select()
                    Log.d("request_code_all_file", "scrolled to 2")


                    binding.universalCloneButton.isEnabled = false
                    binding.pbLoading.pbLoadingWrapper.visibility = View.VISIBLE



                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            /*binding.pbLoading.pbLoadingWrapper.visibility = View.INVISIBLE
                            binding.universalCloneButton.isEnabled = true*/

                            binding.tabLayout.getTabAt(3)?.select()
                            Log.d("request_code_all_file", "scrolled to 3")

                        },
                        4000 // value in milliseconds
                    )

                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            binding.pbLoading.pbLoadingWrapper.visibility = View.INVISIBLE
                            binding.universalCloneButton.isEnabled = true

                            findNavController().navigate(
                                ContentBrowserFragmentDirections.actionContentBrowserFragmentToPrepareIndexFragment()
                            )

                        },
                        6000 // value in milliseconds
                    )



                } else if (imagesStatus == 0 && videosStatus == 0 && audiosStatus != 0 && apksStatus != 0) {



                    binding.tabLayout.getTabAt(3)?.select()
                    Log.d("request_code_all_file", "scrolled to 3")

                    binding.storageScrollview1.visibility = View.INVISIBLE
                    binding.contentTabs.visibility = View.VISIBLE


                    binding.universalCloneButton.isEnabled = false
                    binding.pbLoading.pbLoadingWrapper.visibility = View.VISIBLE



                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            binding.pbLoading.pbLoadingWrapper.visibility = View.INVISIBLE
                            binding.universalCloneButton.isEnabled = true

                            binding.tabLayout.getTabAt(4)?.select()
                            Log.d("request_code_all_file", "scrolled to 4")

                        },
                        4000 // value in milliseconds
                    )

                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            binding.pbLoading.pbLoadingWrapper.visibility = View.INVISIBLE
                            binding.universalCloneButton.isEnabled = true


                            findNavController().navigate(
                                ContentBrowserFragmentDirections.actionContentBrowserFragmentToPrepareIndexFragment()
                            )

                        },
                        7000 // value in milliseconds
                    )



                } else if (imagesStatus == 0 && videosStatus != 0 && audiosStatus != 0 && apksStatus != 0) {


                    binding.tabLayout.getTabAt(2)?.select()
                    Log.d("request_code_all_file", "scrolled to 2")


                    binding.universalCloneButton.isEnabled = false
                    binding.pbLoading.pbLoadingWrapper.visibility = View.VISIBLE



                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            /*binding.pbLoading.pbLoadingWrapper.visibility = View.INVISIBLE
                            binding.universalCloneButton.isEnabled = true*/

                            binding.tabLayout.getTabAt(3)?.select()
                            Log.d("request_code_all_file", "scrolled to 3")

                        },
                        4000 // value in milliseconds
                    )

                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            /*binding.pbLoading.pbLoadingWrapper.visibility = View.INVISIBLE
                            binding.universalCloneButton.isEnabled = true*/

                            binding.tabLayout.getTabAt(4)?.select()
                            Log.d("request_code_all_file", "scrolled to 4")

                        },
                        3000 // value in milliseconds
                    )

                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            binding.pbLoading.pbLoadingWrapper.visibility = View.INVISIBLE
                            binding.universalCloneButton.isEnabled = true

                            findNavController().navigate(
                                ContentBrowserFragmentDirections.actionContentBrowserFragmentToPrepareIndexFragment()
                            )

                        },
                        7000 // value in milliseconds
                    )



                } else if (imagesStatus != 0 && videosStatus != 0 && audiosStatus != 0 && apksStatus != 0) {


                    binding.tabLayout.getTabAt(1)?.select()
                    Log.d("request_code_all_file", "scrolled to 1")



                    binding.universalCloneButton.isEnabled = false
                    binding.pbLoading.pbLoadingWrapper.visibility = View.VISIBLE



                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            binding.pbLoading.pbLoadingWrapper.visibility = View.VISIBLE
                            binding.universalCloneButton.isEnabled = false

                            binding.tabLayout.getTabAt(2)?.select()
                            Log.d("request_code_all_file", "scrolled to 2")

                        },
                        3000 // value in milliseconds
                    )

                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            binding.pbLoading.pbLoadingWrapper.visibility = View.VISIBLE
                            binding.universalCloneButton.isEnabled = false

                            binding.tabLayout.getTabAt(3)?.select()
                            Log.d("request_code_all_file", "scrolled to 3")

                        },
                        4000 // value in milliseconds
                    )

                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            binding.pbLoading.pbLoadingWrapper.visibility = View.VISIBLE
                            binding.universalCloneButton.isEnabled = false

                            binding.tabLayout.getTabAt(4)?.select()
                            Log.d("request_code_all_file", "scrolled to 4")

                        },
                        3000 // value in milliseconds
                    )

                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            binding.pbLoading.pbLoadingWrapper.visibility = View.VISIBLE
                            binding.universalCloneButton.isEnabled = false

                            binding.tabLayout.getTabAt(5)?.select()

                        },
                        7000 // value in milliseconds
                    )


                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            binding.pbLoading.pbLoadingWrapper.visibility = View.INVISIBLE
                            binding.universalCloneButton.isEnabled = true

                            findNavController().navigate(
                                ContentBrowserFragmentDirections.actionContentBrowserFragmentToPrepareIndexFragment()
                            )
                        },
                        12000 // value in milliseconds
                    )



                } else if (documentsStatus == 5 && imagesStatus == 0 && videosStatus == 0 && audiosStatus == 0 && apksStatus == 0) {

                    binding.tabLayout.getTabAt(5)?.select()
                    Log.d("request_code_all_file", "scrolled to 2")

                    binding.universalCloneButton.isEnabled = false
                    binding.pbLoading.pbLoadingWrapper.visibility = View.VISIBLE

                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            binding.pbLoading.pbLoadingWrapper.visibility = View.INVISIBLE
                            binding.universalCloneButton.isEnabled = true

                            findNavController().navigate(
                                ContentBrowserFragmentDirections.actionContentBrowserFragmentToPrepareIndexFragment()
                            )
                        },
                        12000 // value in milliseconds
                    )

                }
            }


            /*var checkedDialogBoxesStatusesArray: IntArray = intArrayOf(documentsStatus, imagesStatus, videosStatus, audiosStatus, apksStatus)

                var checkedDialogBoxesStatusesArrayTemp: IntArray = intArrayOf()

                for (i in 1 until  checkedDialogBoxesStatusesArray.size )
                {

                    if(checkedDialogBoxesStatusesArray[i]!= 0){
                        checkedDialogBoxesStatusesArrayTemp = intArrayOf(i)
                    }

                }

            Toast.makeText(context,checkedDialogBoxesStatusesArrayTemp[0].toString() , Toast.LENGTH_SHORT).show()*/


            //Log.d("checked_arrays_received", checkedDialogBoxesStatusesArrayTemp.toString())


            // check the on-Checked listeners


            /*if(defaultPreferences.getBoolean("images_checked", false) == true){


                    binding.tabLayout.getTabAt(1)?.select()

                }*/


            /*binding.tabLayout.getTabAt(1)?.select()
                Log.d("request_code_all_file", "scrolled to 1")
                binding.tabLayout.getTabAt(2)?.select()
                Log.d("request_code_all_file", "scrolled to 2")
                binding.tabLayout.getTabAt(3)?.select()
                Log.d("request_code_all_file", "scrolled to 3")
                binding.tabLayout.getTabAt(4)?.select()
                Log.d("request_code_all_file", "scrolled to 4")
                findNavController().navigate(
                    ContentBrowserFragmentDirections.actionContentBrowserFragmentToPrepareIndexFragment()
                )*/

            //}

        }


        //pagerAdapter.add(PageItem(getString(R.string.storage),StorageFragment::class.java.name))


        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.adapter = pagerAdapter




        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = pagerAdapter.getItem(position).title

            if (cloneActive == true) {
                binding.tabLayout.visibility = View.INVISIBLE

            }
        }.attach()

        if (cloneActive == true) {
            binding.contentTabs.visibility = View.INVISIBLE

        } else {
            binding.storageScrollview1.visibility = View.INVISIBLE
        }


        /////////////////////////////////////////////////////////////////////////////////
        //Storage stats click listeners


        binding.documentsHolder.setOnClickListener {

            /*defaultPreferences.edit {
                putBoolean("unintentional_back", true)
            }

            defaultPreferences.edit {
                putBoolean("files_clicked", true)
            }

            binding.tabLayout.getTabAt(0)?.select()


            binding.storageScrollview1.visibility = View.INVISIBLE
            binding.contentTabs.visibility = View.VISIBLE*/
        }
        binding.archivesHolder.setOnClickListener {
            /*defaultPreferences.edit {
                putBoolean("archives_clicked", true)
            }

            defaultPreferences.edit {
                putBoolean("unintentional_back", true)
            }

            binding.tabLayout.getTabAt(0)?.select()

            binding.storageScrollview1.visibility = View.INVISIBLE
            binding.contentTabs.visibility = View.VISIBLE*/
        }
        binding.othersHolder.setOnClickListener {

            /*defaultPreferences.edit {
                putBoolean("unintentional_back", true)
            }
            defaultPreferences.edit {
                putBoolean("apps_clicked", true)
            }

            binding.storageScrollview1.visibility = View.INVISIBLE
            binding.contentTabs.visibility = View.VISIBLE
            binding.tabLayout.getTabAt(4)?.select()*/
        }

        binding.imagesHolder.setOnClickListener {


            /*defaultPreferences.edit {
                putBoolean("unintentional_back", true)
            }

            defaultPreferences.edit {
                putBoolean("images_clicked", true)
            }
            binding.storageScrollview1.visibility = View.INVISIBLE
            binding.contentTabs.visibility = View.VISIBLE
            binding.tabLayout.getTabAt(1)?.select()*/
        }
        binding.videosHolder.setOnClickListener {

            /*defaultPreferences.edit {
                putBoolean("unintentional_back", true)
            }

            defaultPreferences.edit {
                putBoolean("videos_clicked", true)
            }

            binding.storageScrollview1.visibility = View.INVISIBLE
            binding.contentTabs.visibility = View.VISIBLE
            binding.tabLayout.getTabAt(2)?.select()*/
        }
        binding.audioHolder.setOnClickListener {

            /*defaultPreferences.edit {
                putBoolean("unintentional_back", true)
            }

            defaultPreferences.edit {
                putBoolean("audios_clicked", true)
            }

            binding.storageScrollview1.visibility = View.INVISIBLE
            binding.contentTabs.visibility = View.VISIBLE
            binding.tabLayout.getTabAt(3)?.select()*/
        }



        sharingViewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                is SharingState.Running -> snackbar.setText(R.string.sending).show()
                is SharingState.Error -> snackbar.setText(
                    CommonErrors.messageOf(
                        view.context,
                        it.exception
                    )
                ).show()
                is SharingState.Success -> {
                    snackbar.dismiss()
                    findNavController().navigate(
                        ContentBrowserFragmentDirections.actionContentBrowserFragmentToNavTransferDetails(
                            it.transfer
                        )
                    )
                }
            }
        }



        clientPickerViewModel.bridge.observe(viewLifecycleOwner) { bridge ->
            val (groupId, items) = contentBrowserViewModel.items ?: return@observe
            sharingViewModel.consume(bridge, groupId, items)
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    //Storage stats
    private fun setStorageDetails(view:  View ,binding: LayoutContentBrowserBinding) {

        //view.context.updateTextColors(storage_)

        binding.mainStorageUsageProgressbar.setIndicatorColor(resources.getColor(R.color.colorPrimary))
        binding.mainStorageUsageProgressbar.trackColor = resources.getColor(R.color.colorPrimary).adjustAlpha(0.3f)

        val redColor = view.context.resources.getColor(R.color.md_red_700)
        binding.imagesProgressbar.setIndicatorColor(redColor)
        binding.imagesProgressbar.trackColor = redColor.adjustAlpha(0.3f)


        val greenColor = view.context.resources.getColor(R.color.md_green_700)
        binding.videosProgressbar.setIndicatorColor(greenColor)
        binding.videosProgressbar.trackColor = greenColor.adjustAlpha(0.3f)

        val lightBlueColor = view.context.resources.getColor(R.color.md_light_blue_700)
        binding.audioProgressbar.setIndicatorColor(lightBlueColor)
        binding.audioProgressbar.trackColor = lightBlueColor.adjustAlpha(0.3f)

        val yellowColor = view.context.resources.getColor(R.color.md_yellow_700)
        binding.documentsProgressbar.setIndicatorColor(yellowColor)
        binding.documentsProgressbar.trackColor = yellowColor.adjustAlpha(0.3f)

        val tealColor = view.context.resources.getColor(R.color.md_teal_700)
        binding.archivesProgressbar .setIndicatorColor(tealColor)
        binding.archivesProgressbar.trackColor = tealColor.adjustAlpha(0.3f)

        val pinkColor = view.context.resources.getColor(R.color.md_pink_700)
        binding.othersProgressbar.setIndicatorColor(pinkColor)
        binding.othersProgressbar.trackColor = pinkColor.adjustAlpha(0.3f)


    }

    private fun getSizes(view: View , viewBinding: LayoutContentBrowserBinding) {
        if (!isOreoPlus()) {
            return
        }



        val mainLooper = Looper.getMainLooper()

        //ensureBackgroundThread

        //Thread(Runnable
        /*GlobalScope.launch{



            getMainStorageStats(view.context, viewBinding)

            val filesSize = getSizesByMimeType(view)
            val imagesSize = filesSize[IMAGES]!!
            val videosSize = filesSize[VIDEOS]!!
            val audioSize = filesSize[AUDIO]!!
            val documentsSize = filesSize[DOCUMENTS]!!
            val archivesSize = filesSize[ARCHIVES]!!
            val othersSize = filesSize[OTHERS]!!

            Log.d("_filesfiles" , "_filesfiles" + filesSize + imagesSize + videosSize + audioSize + documentsSize + archivesSize + othersSize)

            *//*post {

            }*//*
            Handler(Looper.getMainLooper()).post {



            viewBinding.imagesSize.text = imagesSize.formatSize()
            viewBinding.imagesProgressbar.progress = (imagesSize / SIZE_DIVIDER).toInt()

            viewBinding.videosSize.text = videosSize.formatSize()
            viewBinding.videosProgressbar.progress = (videosSize / SIZE_DIVIDER).toInt()

            viewBinding.audioSize.text = audioSize.formatSize()
            viewBinding.audioProgressbar.progress = (audioSize / SIZE_DIVIDER).toInt()

            viewBinding.documentsSize.text = documentsSize.formatSize()
            viewBinding.documentsProgressbar.progress = (documentsSize / SIZE_DIVIDER).toInt()

            viewBinding.archivesSize.text = archivesSize.formatSize()
            viewBinding.archivesProgressbar.progress = (archivesSize / SIZE_DIVIDER).toInt()

            viewBinding.othersSize.text = othersSize.formatSize()
            viewBinding.othersProgressbar.progress = (othersSize / SIZE_DIVIDER).toInt()

            }


        }*/


    }

    private fun getSizesByMimeType(view: View): HashMap<String, Long> {
        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATA
        )

        var imagesSize = 0L
        var videosSize = 0L
        var audioSize = 0L
        var documentsSize = 0L
        var archivesSize = 0L
        var othersSize = 0L
        try {
            view.context.queryCursor(uri, projection) { cursor ->
                try {
                    val mimeType = cursor.getStringValue(MediaStore.Files.FileColumns.MIME_TYPE)?.lowercase(
                        Locale.getDefault())
                    val size = cursor.getLongValue(MediaStore.Files.FileColumns.SIZE)
                    if (mimeType == null) {
                        if (size > 0 && size != 4096L) {
                            val path = cursor.getStringValue(MediaStore.Files.FileColumns.DATA)
                            if (!view.context.getIsPathDirectory(path)) {
                                othersSize += size
                            }
                        }
                        return@queryCursor
                    }

                    when (mimeType.substringBefore("/")) {
                        "image" -> imagesSize += size
                        "video" -> videosSize += size
                        "audio" -> audioSize += size
                        "text" -> documentsSize += size
                        else -> {
                            when {
                                extraDocumentMimeTypes.contains(mimeType) -> documentsSize += size
                                extraAudioMimeTypes.contains(mimeType) -> audioSize += size
                                archiveMimeTypes.contains(mimeType) -> archivesSize += size
                                else -> othersSize += size
                            }
                        }
                    }
                } catch (e: Exception) {
                }
            }
        } catch (e: Exception) {
        }

        val mimeTypeSizes = HashMap<String, Long>().apply {
            put(IMAGES, imagesSize)
            put(VIDEOS, videosSize)
            put(AUDIO, audioSize)
            put(DOCUMENTS, documentsSize)
            put(ARCHIVES, archivesSize)
            put(OTHERS, othersSize)
        }

        return mimeTypeSizes
    }




    @SuppressLint("NewApi")
    private fun getMainStorageStats(context: Context, viewBinding: LayoutContentBrowserBinding ) {
        val externalDirs = context.getExternalFilesDirs(null)
        val storageManager = context.getSystemService(AppCompatActivity.STORAGE_SERVICE) as StorageManager

        externalDirs.forEach { file ->
            val storageVolume = storageManager.getStorageVolume(file) ?: return
            if (storageVolume.isPrimary) {
                // internal storage
                val storageStatsManager = context.getSystemService(AppCompatActivity.STORAGE_STATS_SERVICE) as StorageStatsManager
                val uuid = StorageManager.UUID_DEFAULT
                val totalSpace = storageStatsManager.getTotalBytes(uuid)
                val freeSpace = storageStatsManager.getFreeBytes(uuid)

                val mainLooper = Looper.getMainLooper()

                Handler(Looper.getMainLooper()).post {

                    arrayOf(
                        viewBinding.mainStorageUsageProgressbar, viewBinding.imagesProgressbar, viewBinding.videosProgressbar, viewBinding.audioProgressbar, viewBinding.documentsProgressbar,
                        viewBinding.archivesProgressbar, viewBinding. othersProgressbar
                    ).forEach {
                        it.max = (totalSpace / SIZE_DIVIDER).toInt()
                    }

                    viewBinding.mainStorageUsageProgressbar.progress = ((totalSpace - freeSpace) / SIZE_DIVIDER).toInt()

                    viewBinding.mainStorageUsageProgressbar.beVisible()
                    viewBinding.freeSpaceValue.text = freeSpace.formatSizeThousand()
                    viewBinding.totalSpace.text = String.format(context.getString(R.string.total_storage), totalSpace.formatSizeThousand())
                    viewBinding.freeSpaceLabel.beVisible()
                }




                /*post {

                }*/
            } else {
                // sd card
                val totalSpace = file.totalSpace
                val freeSpace = file.freeSpace
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.sharing, menu)

        val cloneActive = defaultPreferences.getBoolean("clone_active", false)

        if(cloneActive ==  true){
            menu.findItem(R.id.share).setVisible(false)
        }

        val selections = menu.findItem(R.id.selections)
        val share = menu.findItem(R.id.share)
        val shareOnWeb = menu.findItem(R.id.shareOnWeb)


        val cloneButton = view?.findViewById<MaterialCardView>(R.id.universalCloneButton)

        /*selectionViewModel.selectionState.observe(this){


            val enabled = it.isEmpty()
        }*/



        selectionViewModel.selectionState.observe(this) {
            val enable = it.isNotEmpty()

            selections.title = it.size.toString()
            selections.isEnabled = enable
            share.isEnabled = enable
            cloneButton?.isEnabled = enable
            shareOnWeb.isEnabled = enable
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.share -> findNavController().navigate(
                ContentBrowserFragmentDirections.actionContentBrowserFragmentToPrepareIndexFragment()
            )
            R.id.selections -> findNavController().navigate(
                ContentBrowserFragmentDirections.actionContentBrowserFragmentToSelectionsFragment()
            )


        }

        return super.onOptionsItemSelected(item)
    }
}

class ContentFragmentStateAdapter(
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

@HiltViewModel
class ContentBrowserViewModel @Inject internal constructor() : ViewModel() {
    var items: Pair<Long, List<UTransferItem>>? = null
}
