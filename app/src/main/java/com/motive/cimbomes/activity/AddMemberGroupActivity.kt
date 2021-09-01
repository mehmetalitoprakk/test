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
import com.motive.cimbomes.utils.BottomSheetEditGroupFragment
import kotlinx.android.synthetic.main.activity_add_member_group.*


class AddMemberGroupActivity : AppCompatActivity(),EditGroupSelectedAdapter.OnItemClickListener,EditGroupContactAdapter.OnItemClickListener {
    private lateinit var selectAdapter: EditGroupContactAdapter
    private lateinit var selectedAdapter : EditGroupSelectedAdapter
    private lateinit var recyclerViewSelected: RecyclerView
    private lateinit var recyclerViewContacts : RecyclerView
    private var dataList = mutableListOf<GroupMembers>()
    private lateinit var db : DatabaseReference
    private var list = arrayListOf<String>()
    private var selectedList = mutableListOf<GroupMembers>()
    private var groupMembersList = mutableListOf<GroupMembers>()
    var removedValue = arrayListOf<GroupMembers>()
    private var groupKey = ""
    var denemeList = arrayListOf<String>()
    var denemeList2 = arrayListOf<String>()

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
                    var memberList = arrayListOf<GroupMembers>()
                    for (user in snapshot.children){
                        var kullanici = user.getValue(Users::class.java)
                        if (kullanici != null){
                            if (kullanici.telefonNo != null){
                                var number = kullanici!!.telefonNo!!.replace("\\s".toRegex(),"").replace("+9","")
                                for (i in list){
                                    if (i.replace("\\s".toRegex(),"").replace("+9","") == number){
                                        val member = GroupMembers()
                                        if (kullanici!!.uid != FirebaseAuth.getInstance().currentUser!!.uid){
                                            member.name = kullanici!!.isim
                                            member.surname = kullanici.soyisim
                                            member.image = kullanici.profilePic
                                            member.typing = false
                                            member.groupAdmin = null
                                            member.number = kullanici.telefonNo
                                            member.uid = kullanici.uid

                                            dataList.add(member)
                                            denemeList.add(kullanici.uid.toString())
                                            memberList.add(member)
                                        }
                                    }
                                }
                            }
                        }

                    }
                    üyeleriGetir(memberList)

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
            val name = i.name
            val uid = i.uid
            val number = i.number
            val surname = i.surname
            val member = GroupMembers(false,uid,name,surname,number,false,i.image)
            groupMembersList.add(member)
            db.child("groups").child(groupKey).child("member").setValue(groupMembersList)
            finish()
        }
    }

    private fun üyeleriGetir(memberList: ArrayList<GroupMembers>) {
        db.child("groups").child(groupKey).child("member").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.getValue() != null){
                    for (i in snapshot.children){
                        val user = i.getValue(GroupMembers::class.java)
                        groupMembersList.add(user!!)
                        for (i in memberList){
                            if (i.uid == user.uid){
                                denemeList2.add(user.uid.toString())
                            }
                        }
                    }

                    denemeList.removeAll(denemeList2)
                    println(denemeList)

                    for (i in denemeList){
                        db.child("users").child(i).get().addOnSuccessListener {
                            val user = it.getValue(Users::class.java)
                            val member = GroupMembers(false,user!!.uid,user.isim,user.soyisim,user.telefonNo,false,user.profilePic)
                            removedValue.add(member)
                            println(removedValue)
                            selectAdapter.setDataList(removedValue)
                        }
                    }





                }





            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }




    override fun onItemClick(position: Int) {
        val clickedItem = removedValue[position]
        var user = GroupMembers(null,clickedItem.uid,clickedItem.name,clickedItem.surname,clickedItem.number,false,clickedItem.image)
        var ekliMi = false
        for (i in selectedList){
            if (i.uid == user.uid){
                ekliMi = true
                Toast.makeText(this,"Kullanıcı zaten ekli.", Toast.LENGTH_SHORT).show()
                break
            }
        }
        if (ekliMi == false){
            removedValue.remove(clickedItem)
            selectedList.add(user)
            selectedAdapter.setDataList(selectedList)
            selectAdapter.setDataList(removedValue)
            Log.e("kontrol",selectedList.toString())
        }
    }

    override fun onItemClickSelected(position: Int) {
        var clickedItem = selectedList[position]
        selectedList.remove(clickedItem)
        removedValue.add(clickedItem)
        selectAdapter.setDataList(removedValue)
        selectedAdapter.setDataList(selectedList)
    }
}