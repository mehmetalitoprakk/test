package com.motive.cimbomes.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.motive.cimbomes.R
import com.motive.cimbomes.adapter.GroupInfoMembersAdapter
import com.motive.cimbomes.model.GroupMembers
import com.motive.cimbomes.utils.EventBusDataEvents
import com.motive.cimbomes.utils.UniversalImageLoader
import kotlinx.android.synthetic.main.activity_group_info.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import coil.load
import coil.size.Scale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.auth.User
import com.motive.cimbomes.model.GroupKonusma
import com.motive.cimbomes.model.Groups
import com.motive.cimbomes.model.Users
import com.motive.cimbomes.utils.BottomSheetEditGroupFragment
import com.motive.cimbomes.utils.BottomSheetFragment
import kotlinx.android.synthetic.main.fragment_bottom_sheet_edit_group.*

class GroupInfoActivity : AppCompatActivity(),GroupInfoMembersAdapter.OnItemClickListener {
    lateinit var db : DatabaseReference
    var groupKey = ""
    var groupName = ""
    var groupImage = ""
    var members = mutableListOf<GroupMembers>()
    lateinit var recyclerView : RecyclerView
    lateinit var mAdapter : GroupInfoMembersAdapter
    lateinit var layoutManager: LinearLayoutManager
    var mList = ArrayList<GroupMembers>()
    var isAdmin = false
    var isCreator = false
    var creatorID = ""
    var isStatic = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_info)
        groupKey = intent.getStringExtra("grupID")!!
        groupImage = intent.getStringExtra("grupImage")!!
        groupName = intent.getStringExtra("grupName")!!
        isStatic = intent.getBooleanExtra("isStatic",false)

        db = FirebaseDatabase.getInstance().reference
        mList.clear()

        setupRecyclerView()

        getinfo()
        controlGroup()


        db.child("groups").child(groupKey).child("member")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.getValue() != null){
                        for (i in snapshot.children){
                            var member = i.getValue(GroupMembers::class.java)
                            if (member!!.uid == FirebaseAuth.getInstance().currentUser!!.uid){
                                if (member.groupAdmin == true){
                                    isAdmin = true
                                    editgroupImageView.visibility = View.VISIBLE
                                }else{
                                    isAdmin = false
                                    editgroupImageView.visibility = View.GONE
                                }
                            }
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        db.child("groups").child(groupKey).child("creator").get().addOnSuccessListener {
            creatorID = it.value.toString()
            if (creatorID == FirebaseAuth.getInstance().currentUser!!.uid){
                isCreator = true
            }
        }



        if (isStatic){
            println("statik tetiklendi")
            getMembers()
        }else{
            println("members tetiklendi")
            getMembers()
        }


        groupInfoBack.setOnClickListener {
            super.onBackPressed()
        }



        editgroupImageView.setOnClickListener {
            setDialog()
        }


        groupInfoName.text = groupName
        groupInfoImage.load(groupImage){
            crossfade(true)
            crossfade(400)
            placeholder(R.drawable.placeholder)
            scale(Scale.FIT)
            groupInfoImage.setOnClickListener {
                val intent = Intent(this@GroupInfoActivity,FullImageActivity::class.java)
                intent.putExtra("fullImage",groupImage)
                startActivity(intent)
            }
        }








    }


    fun userListener(oldMemberList : ArrayList<GroupMembers>,oldMembersID : ArrayList<String>){
        var yeniListe = mutableListOf<GroupMembers>()
        db.child("groups").child(groupKey).child("member").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                for (i in snapshot.children){
                    println("eklendi" + i.getValue(GroupMembers::class.java)!!.name)
                    mList.add(i.getValue(GroupMembers::class.java)!!)
                }
                katilimciTV.text ="${mList.size.toString()} KATILIMCI"
                mAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })



        db.child("users").addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                var user = snapshot.getValue(Users::class.java)
                println(user.toString() + " 77777")
                if (user!!.uid.toString() !in oldMembersID){
                    mList.clear()
                    val newMember = GroupMembers(false,user.uid,user.isim,user.soyisim,user.telefonNo,false,user.profilePic)
                    var newKonusma = GroupKonusma(false,"Eklendiniz",System.currentTimeMillis(),user!!.uid,groupImage,groupKey,groupName)
                    Log.e("HATA", "     " + groupKey)
                    db.child("grupkonusmalar").child(user.uid!!).child(groupKey).setValue(newKonusma).addOnSuccessListener {
                        oldMemberList.add(newMember)
                        yeniListe.addAll(oldMemberList)
                        db.child("groups").child(groupKey).child("member").setValue(yeniListe).addOnSuccessListener {

                        }

                    }
                }
            }


            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun statikListener() {
        val oldMembers = arrayListOf<GroupMembers>()
        val oldMemberID = arrayListOf<String>()
        val membersID = arrayListOf<String>()
        mList.clear()
        db.child("groups").child(groupKey).child("member").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.getValue() != null){
                    for (i in snapshot.children){
                        val member = i.getValue(GroupMembers::class.java)
                        oldMembers.add(member!!)
                        oldMemberID.add(member.uid.toString())
                    }
                    userListener(oldMembers,oldMemberID)
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }

        })



    }

    private fun controlGroup() {
        db.child("groups").child(groupKey).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.getValue() == null){
                    startActivity(Intent(this@GroupInfoActivity,FeedActivity::class.java))
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun getinfo() {
        db.child("groups").child(groupKey).child("groupName").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.getValue() != null){
                    groupName = snapshot.value.toString()
                    groupInfoName.text = groupName
                }
            }

            override fun onCancelled(error: DatabaseError) {
                throw error.toException()
            }
        })
        db.child("groups").child(groupKey).child("image").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.getValue() != null){
                    groupImage = snapshot.value.toString()
                    groupInfoImage.load(groupImage){
                        crossfade(true)
                        crossfade(400)
                        placeholder(R.drawable.placeholder)
                        scale(Scale.FIT)
                        groupInfoImage.setOnClickListener {
                            val intent = Intent(this@GroupInfoActivity,FullImageActivity::class.java)
                            intent.putExtra("fullImage",groupImage)
                            startActivity(intent)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }


    private fun getMembers() {
        mList.clear()
        db.child("groups").child(groupKey).child("member").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.getValue()!= null){
                    mList.clear()
                    for (i in snapshot.children){
                        var member = i.getValue(GroupMembers::class.java)
                        mList.add(member!!)
                    }
                    katilimciTV.text ="${mList.size.toString()} KATILIMCI"
                    mAdapter.notifyDataSetChanged()
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun setupRecyclerView() {
        recyclerView = groupInfoRecyclerView
        layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        mAdapter = GroupInfoMembersAdapter(mList,this,this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = mAdapter

    }

    @Subscribe(sticky = true)
    internal fun onInfoAl(member : EventBusDataEvents.SendGroupInfo){
        members = member.members
        setupRecyclerView()

    }

    override fun onResume() {
        super.onResume()
        Log.e("ONRESUME","ONRESUME ÇALIŞTI")
        getinfo()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)

    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    private fun setDialog() {
        val dialog = BottomSheetEditGroupFragment()
        EventBus.getDefault().postSticky(EventBusDataEvents.SendEditGroupSheet(groupKey,isCreator))
        dialog.show(supportFragmentManager,"editGroup")
    }




    override fun onItemClick(position: Int) {
        var clickedItem = mList.get(position)
        println(clickedItem)
        val bottomSheetDialog = BottomSheetFragment()
        if (clickedItem.uid != FirebaseAuth.getInstance().currentUser!!.uid){
            if (isAdmin){
                EventBus.getDefault().postSticky(EventBusDataEvents.SendBottomSheet(clickedItem,groupKey,position,true))
                bottomSheetDialog.show(supportFragmentManager,"bottomsheet")
            }else{
                EventBus.getDefault().postSticky(EventBusDataEvents.SendBottomSheet(clickedItem,groupKey,position,false))
                bottomSheetDialog.show(supportFragmentManager,"bottomsheet")
            }
        }



    }


}