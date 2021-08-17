package com.motive.cimbomes.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
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

class GroupInfoActivity : AppCompatActivity() {
    lateinit var db : DatabaseReference
    var groupKey = ""
    var groupName = ""
    var groupImage = ""
    var members = mutableListOf<GroupMembers>()
    lateinit var recyclerView : RecyclerView
    lateinit var mAdapter : GroupInfoMembersAdapter
    lateinit var layoutManager: LinearLayoutManager
    var mList = ArrayList<GroupMembers>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_info)
        groupKey = intent.getStringExtra("grupID")!!
        groupImage = intent.getStringExtra("grupImage")!!
        groupName = intent.getStringExtra("grupName")!!

        db = FirebaseDatabase.getInstance().reference

        groupInfoName.text = groupName
        groupInfoImage.load(groupImage){
            crossfade(true)
            crossfade(400)
            placeholder(R.drawable.image_placeholder)
            scale(Scale.FIT)
            groupInfoImage.setOnClickListener {
                val intent = Intent(this@GroupInfoActivity,FullImageActivity::class.java)
                intent.putExtra("fullImage",groupImage)
                startActivity(intent)
            }
        }







    }

    private fun setupRecyclerView() {
        recyclerView = groupInfoRecyclerView
        layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        for (i in members){
            mList.add(i)
        }
        mAdapter = GroupInfoMembersAdapter(mList,this)
        println(mList)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = mAdapter
    }

    @Subscribe(sticky = true)
    internal fun onInfoAl(member : EventBusDataEvents.SendGroupInfo){
        members = member.members
        setupRecyclerView()
        katilimciTV.text ="${mList.size.toString()} KATILIMCI "

    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }



}