package org.khawaja.fileshare.client.android.fragment.content

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import org.khawaja.fileshare.client.android.R
import javax.inject.Inject
import androidx.core.app.ActivityCompat

import android.os.Build

import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.*

import android.content.pm.PackageManager

import androidx.core.content.ContextCompat
import android.database.Cursor
import android.view.Menu
import android.view.MenuInflater
import android.widget.CheckBox
import androidx.core.content.edit
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import org.khawaja.fileshare.client.android.adapter.CustomAdapter
import org.khawaja.fileshare.client.android.databinding.LayoutEmptyContentBinding
import android.os.Environment

import android.content.res.AssetFileDescriptor
import timber.log.Timber
import java.lang.Exception
import android.content.Intent
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import org.khawaja.fileshare.client.android.adapter.ContactsFileAdapter
import org.khawaja.fileshare.client.android.adapter.FileAdapter
import org.khawaja.fileshare.client.android.util.Activities
import org.khawaja.fileshare.client.android.util.VCardActivity
import org.khawaja.fileshare.client.android.viewmodel.*
import java.io.*


//import org.khawaja.fileshare.client.android.content.ContactData

//import org.khawaja.fileshare.client.android.fragment.content.ContactData






@AndroidEntryPoint
class ContactsBrowserFragment : Fragment (R.layout.layout_contact_browser) {

    ////////////////////////////////////////////////////////////////////////


    private val SIZE_DIVIDER = 100000
    @TargetApi(19)
    private val addAccess = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        val context = context

        if (uri != null && context != null && Build.VERSION.SDK_INT >= 21) {
            viewModel.insertSafFolder(uri)
        }
    }

    private val viewModel: ContactsFileViewModel by viewModels()

    private val selectionViewModel: SharingSelectionViewModel by activityViewModels()

    private lateinit var pathsPopupMenu: PopupMenu

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        private var afterPopup = false

        override fun handleOnBackPressed() {
            if (viewModel.goUp()) {
                afterPopup = false
            } else if (afterPopup) {
                isEnabled = false
                activity?.onBackPressedDispatcher?.onBackPressed()
            } else {
                afterPopup = true
                pathsPopupMenu.show()
            }
        }
    }



    ////////////////////////////////////////////////////////////////////////


    val PERMISSIONS_REQUEST_READ_CONTACTS = 1


    // ArrayList of class ItemsViewModel
    val data = ArrayList<ItemsViewModel>()


    protected val defaultpreferences: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(activity)

    //var cursor: Cursor? = null
    var vCard: ArrayList<String>? = null
    var vfile: String? = null
    var mContext: Context? = null

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vfile = "Contacts" + "_" + System.currentTimeMillis()+".vcf";


        mContext = this.context
        //getVCF();

        var vcardActivity = VCardActivity()

        //vcardActivity.getVCF(requireContext())

        vcardActivity.getVcardString(context,vfile)

        /*val vcfstring = getVcardString()

        Log.d("Storage_path_vcf", vcfstring.toString())*/


        val contactsJsonArray = getContactsJsonArray()
        Log.d("Storage_path_vcf", "json array" + contactsJsonArray)



        //var contacts: List<ContactData> = requireContext().retrieveAllContacts("hello")
        //Log.d("contacts_avaialable:", contacts.toString())


        val selectAllContacts = view.findViewById<CheckBox>(R.id.contactSelectAll)
        selectAllContacts.setOnClickListener {
            defaultpreferences.edit { putBoolean("contacts_selected", true ) }


        }


        requestContactPermission()
        //getContactList()

        val recyclerview = view.findViewById<RecyclerView>(R.id.recyclerView)

        recyclerview.layoutManager = LinearLayoutManager(requireContext())



        val adaptercontacts = CustomAdapter(data)

        recyclerview.adapter = adaptercontacts



        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewFiles)


        val adapter = ContactsFileAdapter { fileModel, clickType ->
            when (clickType) {
                ContactsFileAdapter.ClickType.Default -> {

                    Log.d("set_selected_file", "set_selected_onclick")
                    if (fileModel.file.isDirectory()) {
                        viewModel.requestPath(fileModel.file)
                    } else {
                        Activities.view(view.context, fileModel.file)
                    }
                }

                ContactsFileAdapter.ClickType.ToggleSelect -> {
                    Log.d("set_selected_file", "set selected true")
                    selectionViewModel.setSelected(fileModel, fileModel.isSelected)
                    //Log.d("Child_count", recyclerView.adapter?.itemCount.toString())
                }
            }
            //Log.d("set_selected", "set selected true")
            Log.d("child_count", "file adapter iteratedinfragment")
        }


        adapter.setHasStableIds(true)
        recyclerView.adapter = adapter


        viewModel.files.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            //pbLoading.visibility = View.INVISIBLE
            Log.d("folderfiles_in_list", adapter.itemCount.toString())
            //emptyContentViewModel.with(recyclerView, it.isNotEmpty())
        }

        viewModel.pathTree.observe(viewLifecycleOwner) {
            //pathAdapter.submitList(it)
        }

        /*viewModel.safFolders.observe(viewLifecycleOwner) {

            pathsPopupMenu.menu.clear()
            pathsPopupMenu.setOnMenuItemClickListener { menuItem ->
                if (menuItem.itemId == R.id.storage_folder) {
                    viewModel.requestStorageFolder()
                } else if (menuItem.itemId == R.id.default_storage_folder) {
                    viewModel.requestDefaultStorageFolder()
                } else if (menuItem.groupId == R.id.locations_custom) {
                    viewModel.requestPath(it[menuItem.itemId])
                } else if (menuItem.itemId == R.id.add_storage) {
                    addAccess.launch(null)
                } else if (menuItem.itemId == R.id.clear_storage_list) {
                    viewModel.clearStorageList()
                } else {
                    return@setOnMenuItemClickListener false
                }

                return@setOnMenuItemClickListener true
            }
            pathsPopupMenu.inflate(R.menu.file_browser)
            pathsPopupMenu.menu.findItem(R.id.storage_folder).isVisible = viewModel.isCustomStorageFolder
            pathsPopupMenu.menu.findItem(R.id.clear_storage_list).isVisible = it.isNotEmpty()
            it.forEachIndexed { index, safFolder ->
                pathsPopupMenu.menu.add(R.id.locations_custom, index, Menu.NONE, safFolder.name).apply {
                    setIcon(R.drawable.ic_save_white_24dp)
                }
            }
        }*/

        selectionViewModel.externalState.observe(viewLifecycleOwner) {

            adapter.notifyDataSetChanged()
            Log.d("folderfiles_in_list", adapter.itemCount.toString())
        }

        viewModel.safAdded.observe(viewLifecycleOwner) {
            viewModel.requestPath(it)
            //safAddedSnackbar.show()
        }





    }



    private fun getContactList()  {
        val cr: ContentResolver = requireContext().contentResolver
        val cur: Cursor? = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )
        if ((if (cur != null) cur.getCount() else 0) > 0) {


            //val dir = File(Environment.getExternalStorageDirectory(), "offlinecontactsglobal")
            val dir = Environment.getExternalStorageDirectory()
            if (!dir.exists()) {
                dir.mkdir()
            }

            val vcfFile = File(dir,  "contacts_generated"+".vcf")

            Log.d("Storage_path_vcfabs", vcfFile.absolutePath.toString())
            val fw = FileWriter(vcfFile)
            fw.flush()


            while (cur != null && cur.moveToNext()) {
                val id: String = cur.getString(
                    cur.getColumnIndex(ContactsContract.Contacts._ID)
                )
                val name: String = cur.getString(
                    cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME
                    )
                )
                if (cur.getInt(
                        cur.getColumnIndex(
                            ContactsContract.Contacts.HAS_PHONE_NUMBER
                        )
                    ) > 0
                ) {
                    val pCur: Cursor? = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )



                    while (pCur!!.moveToNext()) {
                        val phoneNo: String = pCur.getString(
                            pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                            )
                        )

                        // This loop will create 20 Views containing
                        // the image with the count of view
                        /*for (i in 1..20) {
                            data.add(ItemsViewModel(R.drawable.round_person_24, "Item " + i))
                        }*/

                        data.add(ItemsViewModel(R.drawable.round_person_24_blue, name, phoneNo))


                        Log.d("contacts_avaialable:", "Name: $name")
                        Log.d("contacts_avaialable:", "Phone Number: $phoneNo")
                        Log.i("phone_Name", "Name: $name")
                        Log.i("phone_number", "Phone Number: $phoneNo")



                        //vfile = "Contacts" + "_" + System.currentTimeMillis()+".vcf";




                        fw.write("BEGIN:VCARD\r\n")
                        fw.write("VERSION:3.0\r\n")
                        fw.write(
                            """N:${name}"""+"\r\n")
                        fw.write(
                            """FN: ${name}"""+"\r\n")
                        /*fw.write(
                            """ORG:${"pgetSurnametoString"}""".trimIndent())
                        fw.write(
                            """TITLE:${"pgetTitletoString"}""".trimIndent())*/
                        fw.write(
                            """TEL;CELL:${phoneNo}"""+"\r\n"
                        )
                        /*fw.write(
                            """TEL;CELL:${"12345"}""".trimIndent()
                        )*/
                        /*fw.write(
                            """ADR;TYPE=WORK:;;${"pgetStreettoString"};${"pgetStreettoString"};${
                                "pgetStreettoString"
                            };${"pgetStreettoString"};${"pgetStreettoString"}""".trimIndent()
                        )
                        fw.write(
                            """EMAIL;TYPE=PREF,INTERNET:${"pgetStreettoString"}""".trimIndent()
                        )*/
                        fw.write("END:VCARD\r\n")






                    }
                    pCur.close()
                }

                /*cur.moveToFirst()
                for (i in 0 until cur.count) {
                    val lookupKey = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)
                    )
                    val uri = Uri.withAppendedPath(
                        ContactsContract.Contacts.CONTENT_VCARD_URI,
                        lookupKey
                    )
                    var fd: AssetFileDescriptor
                    try {
                        fd = requireContext().getContentResolver().openAssetFileDescriptor(uri, "r")!!
                        val fis = fd.createInputStream()
                        val buf = ByteArray(fd.declaredLength.toInt())
                        fis.read(buf)
                        val VCard = String(buf)
                        val path = Environment.getExternalStorageDirectory()
                            .toString() + File.separator + vfile
                        val mFileOutputStream = FileOutputStream(
                            path,
                            true
                        )
                        mFileOutputStream.write(VCard.toByteArray())
                        cur.moveToNext()
                        Log.d("Vcard", VCard)
                        Log.d("Storage_path_vcf", path.toString())
                    } catch (e1: Exception) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace()
                    }
                }*/

            }

            fw.close()


        }
        if (cur != null) {
            cur.close()
        }
    }


    fun requestContactPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.READ_CONTACTS
                    )
                ) {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
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
                    ActivityCompat.requestPermissions(
                        requireActivity(), arrayOf(Manifest.permission.READ_CONTACTS),
                        PERMISSIONS_REQUEST_READ_CONTACTS
                    )
                }
            } else {

                //var contacts: List<ContactData> = requireContext().retrieveAllContacts("hello")

                //Log.d("contacts_avaialable:", contacts.toString())
                   /* val job  = Job()
                    val uiScope = CoroutineScope(Dispatchers.Main + job)

                    uiScope.launch(Dispatchers.IO ){
                        getContactList()
                        withContext(Dispatchers.Main){
                            //getContactList()
                        }
                    }*/
                getContactList()

            }
        } else {

            //var contacts: List<ContactData> = requireContext().retrieveAllContacts("hello")

            //Log.d("contacts_avaialable:", contacts.toString())

            /*val job  = Job()
            val uiScope = CoroutineScope(Dispatchers.Main + job)

            uiScope.launch(Dispatchers.IO ){
                getContactList()
                withContext(Dispatchers.Main){
                    //getContactList()
                }
            }*/


            getContactList()

        }
    }




    fun getVCF() {
        val vfile = "Contacts.vcf"


        //val cr: ContentResolver = requireContext().contentResolver
        /*val cur: Cursor? = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )*/

        val contentResolv = requireContext().contentResolver


        /*val phones: Cursor = requireContext().contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
            null, null, null
        )!!*/

        val phones: Cursor? = contentResolv.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
            null, null, null
        )

        if (phones != null) {

            phones.moveToFirst()
            for (i in 0 until phones.count) {
                val lookupKey = phones.getString(
                    phones
                        .getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)
                )
                val uri = Uri.withAppendedPath(
                    ContactsContract.Contacts.CONTENT_VCARD_URI,
                    lookupKey
                )
                var fd: AssetFileDescriptor
                try {
                    fd = requireContext().getContentResolver().openAssetFileDescriptor(uri, "r")!!
                    val fis = fd.createInputStream()
                    val buf = ByteArray(fd.declaredLength.toInt())
                    fis.read(buf)
                    val VCard = String(buf)
                    val path = Environment.getExternalStorageDirectory()
                        .toString() + File.separator + vfile
                    val mFileOutputStream = FileOutputStream(
                        path,
                        true
                    )
                    mFileOutputStream.write(VCard.toByteArray())
                    phones.moveToNext()
                    Log.d("Vcard", VCard)
                    Log.d("Storage_path_vcf", path.toString())
                } catch (e1: Exception) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace()
                }
            }

        }




    }



    private fun getContactsJsonArray(): JsonArray {

        val resolver: ContentResolver = requireContext().contentResolver
        val cursor = resolver.query(
            ContactsContract.Contacts.CONTENT_URI, null, null, null,
            null
        )
        val mainJsonArray: JsonArray = JsonArray()
        if (cursor!!.count > 0) {
            while (cursor.moveToNext()) {
                val personJsonObj: JsonObject = JsonObject()
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                //val phoneNumber = (cursor.getString( cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER) )).toInt()
                personJsonObj.addProperty("NAME", name.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\""))

                val orgCursor = resolver.query(
                    ContactsContract.Data.CONTENT_URI, null,
                    ContactsContract.Data.CONTACT_ID + "=?", arrayOf(id), null
                )

                val phoneJsonArray = JsonArray()
                val emailJsonArray = JsonArray()

                if (orgCursor!!.count > 0) {
                    while (orgCursor.moveToNext()) {
                        if (orgCursor!!.getString(orgCursor.getColumnIndex(ContactsContract.Data.MIMETYPE)).equals(
                                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
                            )
                        ) {
                            val companyName = orgCursor.getString(orgCursor.getColumnIndex(ContactsContract.Data.DATA1))
                            val designation = orgCursor.getString(orgCursor.getColumnIndex(ContactsContract.Data.DATA4))
                            personJsonObj.addProperty(
                                "ORGANIZATION",
                                companyName.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"")
                            )
                            personJsonObj.addProperty(
                                "DESIGNATION",
                                designation.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"")
                            )

                        } else if (orgCursor!!.getString(orgCursor.getColumnIndex(ContactsContract.Data.MIMETYPE)).equals(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                            )
                        ) {
                            val phoneNum = orgCursor.getString(orgCursor.getColumnIndex(ContactsContract.Data.DATA1))
                            phoneJsonArray.add(phoneNum.replace(" ", "").replace("-", ""))
                        } else if (orgCursor!!.getString(orgCursor.getColumnIndex(ContactsContract.Data.MIMETYPE)).equals(
                                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                            )
                        ) {
                            val emailAddr = orgCursor.getString(orgCursor.getColumnIndex(ContactsContract.Data.DATA1))
                            emailJsonArray.add(emailAddr.replace(" ", ""))
                        }
                    }
                }
                orgCursor.close()
                personJsonObj.add("EMAIL_LIST", emailJsonArray)
                personJsonObj.add("PHONE_NUMBERS", phoneJsonArray)


                //val vcfFile = File(requireContext().getExternalFilesDir(null), "generated.vcf")
                /*val vcfFile = File(Environment.getExternalStorageDirectory(), "contacts_generated.vcf")
                val fw = FileWriter(vcfFile)
                fw.write("BEGIN:VCARD\r\n")
                fw.write("VERSION:3.0\r\n")
                fw.write(
                    """N:${"pgetSurnametoString;;;"}"""+"\r\n")
                fw.write(
                    """FN: ${"pgetSurnametoString"}"""+"\r\n")

                fw.write(
                    """TEL;CELL:${"12345"}"""+"\r\n"
                )

                fw.write("END:VCARD\r\n")
                fw.close()*/

                /*val i = Intent()
                i.action = Intent.ACTION_VIEW
                i.setDataAndType(Uri.fromFile(vcfFile), "text/x-vcard")
                i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(i)*/






                mainJsonArray.add(personJsonObj)
            }

        }
        cursor.close()
        //saveJsonFile(requireContext(), "hello Json", mainJsonArray)
        return mainJsonArray
    }


    //save Json File in Phone

    @Throws(IOException::class)
    fun saveJsonFile(context: Context, jsonString: String?, jsonArray: JsonArray?) {
        //val rootFolder = context.getExternalFilesDir(null)
        val rootFolder  = Environment.getExternalStorageDirectory()
        val jsonFile = File(rootFolder, "contacts_file.json")
        val writer = FileWriter(jsonFile)
        writer.write(jsonArray.toString())
        writer.close()
        Log.d("Storage_path_vcf_path", "json_file_path" +jsonFile.absolutePath.toString())
        //or IOUtils.closeQuietly(writer);
    }

    //son_file_path/storage/emulated/0/Android/data/com.smartswitch.phoneclone.offline.backup.debug/files/file.json




    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    fun Context.isContactExists(
        phoneNumber: String
    ): Boolean {
        val lookupUri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val projection = arrayOf(
            ContactsContract.PhoneLookup._ID,
            ContactsContract.PhoneLookup.NUMBER,
            ContactsContract.PhoneLookup.DISPLAY_NAME
        )
        requireContext().contentResolver.query(lookupUri, projection, null, null, null).use {
            return (it?.moveToFirst() == true)
        }
    }

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    @JvmOverloads
    fun Context.retrieveAllContacts(
        searchPattern: String = "",
        retrieveAvatar: Boolean = true,
        limit: Int = -1,
        offset: Int = -1
    ): List<ContactData> {
        val result: MutableList<ContactData> = mutableListOf()
        requireContext().contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            CONTACT_PROJECTION,
            if (searchPattern.isNotBlank()) "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE '%?%'" else null,
            if (searchPattern.isNotBlank()) arrayOf(searchPattern) else null,
            if (limit > 0 && offset > -1) "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC LIMIT $limit OFFSET $offset"
            else ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC"
        )?.use {
            if (it.moveToFirst()) {
                do {
                    val contactId = it.getLong(it.getColumnIndex(CONTACT_PROJECTION[0]))
                    val name = it.getString(it.getColumnIndex(CONTACT_PROJECTION[2])) ?: ""
                    val hasPhoneNumber = it.getString(it.getColumnIndex(CONTACT_PROJECTION[3])).toInt()
                    val phoneNumber: List<String> = if (hasPhoneNumber > 0) {
                        retrievePhoneNumber(contactId)
                    } else mutableListOf()

                    val avatar = if (retrieveAvatar) retrieveAvatar(contactId) else null
                    result.add(ContactData(contactId, name, phoneNumber, avatar))
                } while (it.moveToNext())
            }
        }
        return result
    }



    private fun Context.retrievePhoneNumber(contactId: Long): List<String> {
        val result: MutableList<String> = mutableListOf()
        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} =?",
            arrayOf(contactId.toString()),
            null
        )?.use {
            if (it.moveToFirst()) {
                do {
                    result.add(it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)))
                } while (it.moveToNext())
            }
        }
        return result
    }

    private fun Context.retrieveAvatar(contactId: Long): Uri? {
        return contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            "${ContactsContract.Data.CONTACT_ID} =? AND ${ContactsContract.Data.MIMETYPE} = '${ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE}'",
            arrayOf(contactId.toString()),
            null
        )?.use {
            if (it.moveToFirst()) {
                val contactUri = ContentUris.withAppendedId(
                    ContactsContract.Contacts.CONTENT_URI,
                    contactId
                )
                Uri.withAppendedPath(
                    contactUri,
                    ContactsContract.Contacts.Photo.CONTENT_DIRECTORY
                )
            } else null
        }
    }

    private val CONTACT_PROJECTION = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.LOOKUP_KEY,
        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
        ContactsContract.Contacts.HAS_PHONE_NUMBER
    )



}

/*@HiltViewModel
class ConactsBrowserViewModel @Inject internal constructor(

    private val contectUtils : ContactData,

):ViewModel(){
    var contacts: List<ContactData> = contectUtils.retrieveAllContacts(context)
    var contacts1: List<ContactData> = retreiveall

}*/







data class ContactData(
    val contactId: Long,
    val name: String,
    val phoneNumber: List<String>,
    val avatar: Uri?
)