package com.motive.cimbomes.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.motive.cimbomes.R
import com.motive.cimbomes.model.GroupMembers
import com.motive.cimbomes.utils.UniversalImageLoader
import kotlinx.android.synthetic.main.contact_child.view.*
import kotlinx.android.synthetic.main.contactslayout.view.*

class GroupInfoMembersAdapter(var members : ArrayList<GroupMembers>,var ctx :  Context) : RecyclerView.Adapter<GroupInfoMembersAdapter.GroupInfoMembersViewHolder>() {
    class GroupInfoMembersViewHolder(view : View) : RecyclerView.ViewHolder(view){
        var tumLayoutRel = view as RelativeLayout
        var img = tumLayoutRel.imgContactUserInfo
        var nameInfo = tumLayoutRel.txtContactName
        var numberInfo = tumLayoutRel.txtContactStatus
        var imgMember = tumLayoutRel.imgContact
        var isadmin = tumLayoutRel.isAdmin
        fun setData(member : GroupMembers,ctx: Context){
            imgMember.visibility = View.GONE
            nameInfo.text = member.name
            numberInfo.text = member.number
            if (member.image != null){
                UniversalImageLoader.setImage(member.image!!,img,null,"")
            }
            if (member.groupAdmin == true){
                isadmin.visibility = View.VISIBLE
                imgMember.visibility = View.GONE
            }else{
                imgMember.visibility = View.VISIBLE
            }

        }



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupInfoMembersViewHolder {
        var view = LayoutInflater.from(ctx).inflate(R.layout.contactslayout,parent,false)
        return GroupInfoMembersViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupInfoMembersViewHolder, position: Int) {
        holder.setData(members.get(position),ctx)
    }

    override fun getItemCount(): Int {
        return members.size
    }

}