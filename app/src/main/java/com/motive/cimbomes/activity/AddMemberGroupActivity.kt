package com.motive.cimbomes.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.motive.cimbomes.R
import com.motive.cimbomes.adapter.EditGroupContactAdapter
import com.motive.cimbomes.adapter.EditGroupSelectedAdapter
import com.motive.cimbomes.model.Contact
import com.motive.cimbomes.model.GroupMembers
import com.motive.cimbomes.model.Users
import kotlinx.android.synthetic.main.activity_add_member_group.*


class AddMemberGroupActivity : AppCompatActivity(),EditGroupSelectedAdapter.OnItemClickListener,EditGroupContactAdapter.OnItemClickListener {
    private lateinit var selectAdapter: EditGroupContactAdapter
    private lateinit var selectedAdapter : EditGroupSelectedAdapter
    private lateinit var recyclerViewSelected: RecyclerView
    private lateinit var recyclerViewContacts : RecyclerView
    private var dataList = mutableListOf<Contact>()
    private lateinit var db : DatabaseReference
    private var list = arrayListOf<String>()
    private var selectedList = mutableListOf<Contact>()
    private var groupMembersList = mutableListOf<GroupMembers>()
    private var groupKey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_member_group)

        groupKey = intent.getStringExtra("editGroupKey")!!


        db = FirebaseDatabase.getInstance().reference

        recyclerViewContacts = editGroupRecyclerViewContact
        recyclerViewSelected = editGroupRecyclerViewTop

        recyclerViewContacts.layoutManager = LinearLayoutManager(this)
        recyclerViewSelected.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)


        selectAdapter = EditGroupContactAdapter(this)
        selectedAdapter = EditGroupSelectedAdapter(this)


        val contacts = this.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null)
        while (contacts!!.moveToNext()){
            val name = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val number = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            val obj = Contact()
            obj.name = name
            obj.number = number
            obj.kullaniyorMu = false
            list.add(number)
        }
        recyclerViewContacts.adapter = selectAdapter
        recyclerViewSelected.adapter = selectedAdapter

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
                                val member = GroupMembers()
                                if (kullanici!!.uid != FirebaseAuth.getInstance().currentUser!!.uid){
                                    contacts.name = kullanici!!.isim + " " + kullanici.soyisim
                                    contacts.number = kullanici!!.telefonNo
                                    contacts.image = kullanici.profilePic
                                    contacts.uid = kullanici.uid
                                    contacts.kullaniyorMu = true
                                    dataList.add(contacts)
                                }
                            }
                        }
                        üyeleriGetir()
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        editGroupNextButton.setOnClickListener {
            grubuGuncelle()
        }


    }

    private fun grubuGuncelle() {
        for (i in selectedList){
            var name = i.name
            var uid = i.uid
            var number = i.number
            var member = GroupMembers(false,uid,name,"",number,false,i.image)
            groupMembersList.add(member)
        }
    }

    private fun üyeleriGetir() {
        var removedValue = arrayListOf<Contact>()
        db.child("groups").child(groupKey).child("member").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.getValue() != null){
                    for (i in snapshot.children){
                        val user = i.getValue(GroupMembers::class.java)
                        var contact = Contact()
                        contact.name = user!!.name + " " + user.surname
                        contact.number = user!!.number
                        contact.image = user.image
                        contact.uid = user.uid
                        contact.kullaniyorMu = true
                        
                        groupMembersList.add(user!!)
                        if (contact in dataList){
                            removedValue.add(contact)
                        }
                    }

                    dataList.removeAll(removedValue)
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
                Toast.makeText(this,"Kullanıcı zaten ekli.", Toast.LENGTH_SHORT).show()
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