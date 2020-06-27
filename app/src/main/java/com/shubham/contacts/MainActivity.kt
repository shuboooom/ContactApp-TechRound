package com.shubham.contacts

import android.Manifest
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.shubham.contacts.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    var PROJECTION_NUMBERS = arrayOf(ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER)
    var PROJECTION_DETAILS = arrayOf(ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
    val phones = hashMapOf<Long,ArrayList<String?>>()
    private lateinit var binding : ActivityMainBinding
    private var contactList = mutableListOf<Contact>()
    private var contactAdapter: ContactAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updateCount(0)
        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_CONTACTS)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        LoaderManager.getInstance(this@MainActivity).initLoader(0, null, this@MainActivity);
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        Toast.makeText(this@MainActivity,"Permission denied",Toast.LENGTH_LONG).show()
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) { /* ... */
                        token?.continuePermissionRequest()
                        Toast.makeText(this@MainActivity,"Permission denied",Toast.LENGTH_LONG).show()
                    }
                }).check()

    }

    fun updateCount(count : Int)
    {
        binding.checkedCount.text = getString(R.string.checked_count,count)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {

        //startLoading()
        return when (id) {
            0 -> CursorLoader(
                    this,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    PROJECTION_NUMBERS,
                    null,
                    null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
            )
            else -> CursorLoader(
                    this,
                    ContactsContract.Contacts.CONTENT_URI,
                    PROJECTION_DETAILS,
                    null,
                    null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
            )
        }
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {


        when (loader.id) {
            0 -> {

                if (data != null) {
                    while (!data.isClosed() && data.moveToNext()) {
                        val contactId: Long = data.getLong(0)
                        val phone: String = data.getString(1)
                        var list: MutableList<String?>?
                        if (phones.containsKey(contactId)) {
                            list = phones.get(contactId)
                        } else {
                            list = ArrayList()
                            phones.put(contactId, list)
                        }
                        list?.add(phone)
                    }
                    data.close()
                }
                LoaderManager.getInstance(this@MainActivity)
                        .initLoader(1, null, this)
            }
            1 -> if (data != null) {
                while (!data.isClosed() && data.moveToNext()) {

                    val contactId: Long = data.getLong(0)
                    val name: String = data.getString(1)
                    val photo: String? = data.getString(2)
                    val contactPhones: MutableList<String?>? = phones.get(contactId)

                    if (contactPhones != null) {
                        for (phone in contactPhones) {
                            addContact(contactId, name, phone, photo)
                        }
                    }
                }
                data.close()
                loadAdapter()
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {

    }

    fun addContact(contactId:Long, name:String, phone:String?, photo:String?)
    {
        contactList.add(Contact(contactId,name,phone,photo))
    }

    fun loadAdapter()
    {
        if(contactAdapter==null)
        {
            binding.contactList.apply {
                layoutManager = LinearLayoutManager(this@MainActivity,VERTICAL,false)
                addItemDecoration(DividerItemDecoration(this@MainActivity,VERTICAL))
            }

            contactAdapter = ContactAdapter(contactList){ contact: Contact, b: Boolean ->
                val index = contactList.indexOf(contact)
                val newcon = contactList[index]
                newcon.selected = b
                contactList[index] = newcon
                contactAdapter?.getSelectedCount()?.let { updateCount(it) }

            }
        }


        binding.contactList.adapter = contactAdapter
    }
}