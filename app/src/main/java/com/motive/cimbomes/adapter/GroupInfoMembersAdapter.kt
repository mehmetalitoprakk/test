package com.motive.cimbomes.adapter


import android.content.Context
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.motive.cimbomes.R
import com.motive.cimbomes.model.GroupMembers
import com.motive.cimbomes.utils.UniversalImageLoader

import kotlinx.android.synthetic.main.contactslayout.view.*

class GroupInfoMembersAdapter(var members : ArrayList<GroupMembers>,var ctx :  Context,private var listener : OnItemClickListener) : RecyclerView.Adapter<GroupInfoMembersAdapter.GroupInfoMembersViewHolder>() {



    inner class GroupInfoMembersViewHolder(view : View) : RecyclerView.ViewHolder(view),View.OnClickListener{
        var tumLayoutRel = view as RelativeLayout
        var img = tumLayoutRel.imgContactUserInfo
        var nameInfo = tumLayoutRel.txtContactName
        var numberInfo = tumLayoutRel.txtContactStatus
        var isadmin = tumLayoutRel.isAdmin
        val numberList = arrayListOf<String>()
        val formattedList = arrayListOf<String>()




        fun setData(member : GroupMembers,ctx: Context){



            val contacts = ctx.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null)
            while (contacts!!.moveToNext()) {
                val name = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                numberList.add(number)
            }


            val gelenNumber = member.number!!.replace("\\s".toRegex(),"").replace("+9","")

            for (i in numberList){
                var number = i.replace("\\s".toRegex(),"").replace("+9","")
                formattedList.add(number)
            }

            if (gelenNumber in formattedList){
                numberInfo.text = member.number
            }else if(member.uid == FirebaseAuth.getInstance().currentUser!!.uid){
                numberInfo.text = member.number
            }else{
                numberInfo.text = ""
            }



            nameInfo.text = member.name + " " + member.surname

            if (member.image != null){
                UniversalImageLoader.setImage(member.image!!,img,null,"")
            }
            if (member.groupAdmin == true){
                isadmin.visibility = View.VISIBLE

            }else{
                isadmin.visibility = View.GONE
            }

        }



        init {
            //Tıklayan kişi admin mi 
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION){
                listener.onItemClick(position)
            }
        }

    }
    interface OnItemClickListener{
        fun onItemClick(position: Int)
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