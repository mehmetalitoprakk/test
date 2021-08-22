package com.motive.cimbomes.fragments

import android.content.Context
import android.content.Intent
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.motive.cimbomes.R
import com.motive.cimbomes.activity.GroupChatActivity
import com.motive.cimbomes.adapter.SelectMemberAdapter
import com.motive.cimbomes.adapter.SelectedContactAdapter
import com.motive.cimbomes.model.*
import com.motive.cimbomes.utils.EventBusDataEvents
import kotlinx.android.synthetic.main.fragment_select_group_member.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class SelectGroupMemberFragment : Fragment(), SelectMemberAdapter.OnItemClickListener,SelectedContactAdapter.OnItemClickListener {
    private lateinit var selectAdapter: SelectMemberAdapter
    private lateinit var selectedAdapter : SelectedContactAdapter
    private lateinit var recyclerViewSelected: RecyclerView
    private lateinit var recyclerViewContacts : RecyclerView
    private var dataList = mutableListOf<Contact>()
    private lateinit var mAuth : FirebaseAuth
    private lateinit var db : DatabaseReference
    private lateinit var storage : StorageReference
    private var list = arrayListOf<String>()
    private var groupName = ""
    private var groupImage : Uri? = null
    private var selectedList = mutableListOf<Contact>()
    private var groupMembersList = mutableListOf<GroupMembers>()
    private var newGroupKey :String? = ""

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
        storage = FirebaseStorage.getInstance().reference
        recyclerViewSelected = selectedRecyclerView
        recyclerViewContacts = recyclerViewContact









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


        db.child("users").child(mAuth.currentUser!!.uid).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var me = snapshot.getValue(Users::class.java)

                var admin = GroupMembers(true,mAuth.currentUser!!.uid,me!!.isim,me.soyisim,me.telefonNo,false,me.profilePic)
                groupMembersList.add(admin)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


        nextButtonContactsGroup.setOnClickListener {
            createGroup()
        }


    }

    private fun createGroup() {

        for(i in selectedList){
            var name = i.name
            var uid = i.uid
            var number = i.number
            var member = GroupMembers(false,uid,name,"",number,false,i.image)
            groupMembersList.add(member)
        }


        newGroupKey = db.child("groups").push().key.toString()
        var newGroup = Groups(newGroupKey,mAuth.currentUser!!.uid,groupMembersList,System.currentTimeMillis(),"",groupName,false,null)

        db.child("groups").child(newGroupKey!!).setValue(newGroup).addOnSuccessListener {
            storage.child("groups").child(newGroupKey!!).child("groupImage").putFile(groupImage!!).addOnSuccessListener {
                storage.child("groups").child(newGroupKey!!).child("groupImage").downloadUrl.addOnSuccessListener {
                    var groupImageDb = it.toString()
                    db.child("groups").child(newGroupKey!!).child("image").setValue(it.toString()).addOnSuccessListener {
                        for (i in groupMembersList){
                            var gorulduMu = false
                            if (i.uid.toString() == mAuth.currentUser!!.uid){
                                gorulduMu = true
                            }
                            var grupKonusma = GroupKonusma(gorulduMu,"",System.currentTimeMillis(),i.uid.toString(),groupImageDb,newGroupKey,groupName)
                            db.child("grupkonusmalar").child(i.uid.toString()!!).child(newGroupKey!!).setValue(grupKonusma)
                        }
                        val intent = Intent(requireContext(),GroupChatActivity::class.java)
                        intent.putExtra("GroupKey",newGroupKey!!)
                        Toast.makeText(requireContext(),"Grup Oluşturuldu",Toast.LENGTH_SHORT).show()
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
            }
        }


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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        EventBus.getDefault().unregister(this)
        super.onDetach()

    }


    @Subscribe(sticky = true)
    internal fun onGrupBilgileriniGetir(grupBilgileri : EventBusDataEvents.SendGroupData){
        groupName = grupBilgileri.groupName
        groupImage = Uri.parse(grupBilgileri.groupImageUri)
    }


}