package com.motive.cimbomes.fragments

import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.motive.cimbomes.R
import com.motive.cimbomes.adapter.SelectMemberAdapter
import com.motive.cimbomes.adapter.SelectedContactAdapter
import com.motive.cimbomes.model.Contact
import com.motive.cimbomes.model.Users
import kotlinx.android.synthetic.main.fragment_select_group_member.*

class SelectGroupMemberFragment : Fragment(), SelectMemberAdapter.OnItemClickListener,SelectedContactAdapter.OnItemClickListener {
    private lateinit var selectAdapter: SelectMemberAdapter
    private lateinit var selectedAdapter : SelectedContactAdapter
    private lateinit var recyclerViewSelected: RecyclerView
    private lateinit var recyclerViewContacts : RecyclerView
    private var dataList = mutableListOf<Contact>()
    private lateinit var mAuth : FirebaseAuth
    private lateinit var db : DatabaseReference
    var list = arrayListOf<String>()
    var groupName = ""
    var imageUri = ""
    var groupImage : Uri? = null
    private var selectedList = mutableListOf<Contact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_group_member, container, false)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        recyclerViewSelected = selectedRecyclerView
        recyclerViewContacts = recyclerViewContact

        var bundle = arguments
        if (bundle != null){
            groupName = bundle.getString("GroupName")!!
            imageUri = bundle.getString("GroupImageUri")!!
            groupImage = Uri.parse(imageUri)
        }




        recyclerViewContacts.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewSelected.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        selectAdapter = SelectMemberAdapter(this,requireContext())
        selectedAdapter = SelectedContactAdapter(this)
        recyclerViewSelected.adapter = selectedAdapter


        val contacts = requireActivity().contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null)
        while (contacts!!.moveToNext()){
            val name = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val number = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            val obj = Contact()
            obj.name = name
            obj.number = number
            obj.kullaniyorMu = false
            list.add(number)
            dataList.add(obj)
        }
        recyclerViewContacts.adapter = selectAdapter

        selectAdapter.setDataList(dataList)
        dataList.clear()
        contacts.close()



        db.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.getValue() != null){
                    for (user in snapshot.children){
                        var kullanici = user.getValue(Users::class.java)
                        var number = kullanici!!.telefonNo!!.replace("\\s".toRegex(),"").replace("+9","")
                        for (i in list){
                            if (i.replace("\\s".toRegex(),"").replace("+9","") == number){
                                val contacts = Contact()
                                if (kullanici!!.uid != mAuth.currentUser!!.uid){
                                    contacts.name = kullanici!!.isim + " " + kullanici.soyisim
                                    contacts.number = kullanici!!.telefonNo
                                    contacts.image = kullanici.profilePic
                                    contacts.uid = kullanici.uid
                                    contacts.kullaniyorMu = true
                                    dataList.add(contacts)
                                }
                            }else{

                            }
                        }
                    }
                    selectAdapter.setDataList(dataList)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


    }

    override fun onItemClick(position: Int) {
        val clickedItem = dataList[position]
        var user = Contact(clickedItem.name,clickedItem.number,clickedItem.image,true,clickedItem.uid)
        var ekliMi = false
        for (i in selectedList){
            if (i.uid == user.uid){
                ekliMi = true
                Toast.makeText(requireContext(),"Kullanıcı zaten ekli.",Toast.LENGTH_SHORT).show()
                break
            }
        }
        if (ekliMi == false){
            dataList.remove(clickedItem)
            selectedList.add(user)
            selectedAdapter.setDataList(selectedList)
            selectAdapter.setDataList(dataList)
            Log.e("kontrol",selectedList.toString())
        }


    }

    override fun onItemClickSelected(position: Int) {
        var clickedItem = selectedList[position]
        selectedList.remove(clickedItem)
        dataList.add(clickedItem)
        selectAdapter.setDataList(dataList)
        selectedAdapter.setDataList(selectedList)

    }


}