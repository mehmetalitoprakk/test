package com.motive.cimbomes.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.motive.cimbomes.R
import com.motive.cimbomes.activity.ChatActivity
import com.motive.cimbomes.activity.GroupChatActivity
import com.motive.cimbomes.model.GroupKonusma
import com.motive.cimbomes.utils.TimeAgo
import com.motive.cimbomes.utils.UniversalImageLoader
import kotlinx.android.synthetic.main.groupkonusmachild.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class GroupKonusmaAdapter(var konusmalar : ArrayList<GroupKonusma>, var konusmalarKopy : ArrayList<GroupKonusma>,var ctx : Context) : RecyclerView.Adapter<GroupKonusmaAdapter.GroupKonusmaViewHolder>() {
    class GroupKonusmaViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        var grupName = ""
        var pic = ""
        var tumLayout = view as ConstraintLayout
        var imgIV = tumLayout.groupImageKonusma
        var groupNameTV = tumLayout.groupKonusmaName
        var sonMesajTV = tumLayout.groupKonusmaSonMesaj
        var timeTV = tumLayout.groupKonusmaTime
        var goruldu = tumLayout.imgOkunmadıNokta
        var statik = tumLayout.groupKonusmaKaliciGrup


        fun setData(oAnkiKonusma : GroupKonusma, ctx: Context){

            if (oAnkiKonusma != null){
                var konusmaText = oAnkiKonusma.son_mesaj.toString()
                konusmaText = konusmaText.replace("\n"," ")
                konusmaText=konusmaText.trim()

                groupNameTV.text = oAnkiKonusma.groupName
                //TODO SELECT MEMBER FRAGMENT BACK TUSU

                if (konusmaText.length > 30){
                    sonMesajTV.text = konusmaText.substring(0,30)+"..."
                }else if (konusmaText == ""){
                    sonMesajTV.text = "Eklendiniz"
                }
                else{
                    sonMesajTV.text = konusmaText
                }
                if (oAnkiKonusma.time != null){
                    timeTV.text = TimeAgo.getTimeAgoForComments(oAnkiKonusma.time!!.toLong())
                }
                if (oAnkiKonusma.groupName != null){
                    UniversalImageLoader.setImage(oAnkiKonusma.groupImage!!,imgIV,null,"")
                }



                if (oAnkiKonusma.goruldu == false){
                    goruldu.visibility = View.VISIBLE
                    sonMesajTV.setTextColor(ctx.resources.getColor(R.color.black))
                    sonMesajTV.setTypeface(Typeface.DEFAULT_BOLD)
                    groupNameTV.setTypeface(Typeface.DEFAULT_BOLD)
                    timeTV.setTextColor(ctx.resources.getColor(R.color.black))
                    timeTV.setTypeface(Typeface.DEFAULT_BOLD)
                }else{
                    goruldu.visibility = View.INVISIBLE
                    sonMesajTV.setTextColor(ctx.resources.getColor(R.color.defaultext))
                    sonMesajTV.setTypeface(Typeface.DEFAULT)
                    groupNameTV.setTypeface(Typeface.DEFAULT)
                    timeTV.setTextColor(ctx.resources.getColor(R.color.defaultext))
                    timeTV.setTypeface(Typeface.DEFAULT)
                }

                tumLayout.setOnClickListener {
                    var intent = Intent(ctx, GroupChatActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    intent.putExtra("GroupKey",oAnkiKonusma.groupID)
                    FirebaseDatabase.getInstance().reference.child("grupkonusmalar").child(FirebaseAuth.getInstance().currentUser!!.uid).child(oAnkiKonusma.groupID!!).child("goruldu").setValue(true).addOnCompleteListener {
                        ctx.startActivity(intent)
                    }
                }



                grupBilgileriniGetir(oAnkiKonusma.groupID)
                grupStatikMi(oAnkiKonusma.groupID)
            }


        }

        private fun grupStatikMi(key: String?) {
            FirebaseDatabase.getInstance().reference.child("groups").child(key!!).child("static").get().addOnSuccessListener {
                if (it.value == true){
                    statik.visibility = View.VISIBLE
                }else if (it.value == false){
                    statik.visibility = View.GONE
                }
            }
        }

        private fun grupBilgileriniGetir(grupid: String?) {
            FirebaseDatabase.getInstance().reference.child("grupkonusmalar").child(FirebaseAuth.getInstance().currentUser!!.uid).child(grupid!!).addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.getValue() != null){
                        var bulunanGrup = snapshot.getValue(GroupKonusma::class.java)
                        if (bulunanGrup!= null){
                            if (bulunanGrup.son_mesaj != null) {
                                var konusmaText = bulunanGrup.son_mesaj.toString()
                                konusmaText = konusmaText.replace("\n"," ")
                                konusmaText=konusmaText.trim()

                                if (konusmaText.length > 30){
                                    sonMesajTV.text = konusmaText.substring(0,30)+"..."
                                }else if (konusmaText == ""){
                                    sonMesajTV.text = "Eklendiniz"
                                }
                                else{
                                    sonMesajTV.text = konusmaText
                                }

                                if (bulunanGrup.groupName != null) {
                                    groupNameTV.text = bulunanGrup!!.groupName
                                }
                                if (bulunanGrup.time != null) {
                                    timeTV.text =
                                        TimeAgo.getTimeAgoForComments(bulunanGrup.time!!.toLong())
                                }
                                if (bulunanGrup.groupImage != null) {
                                    UniversalImageLoader.setImage(
                                        bulunanGrup.groupImage!!,
                                        imgIV,
                                        null,
                                        ""
                                    )
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }

    }

    fun filter(text: String) {
        var text = text
        konusmalar.clear()
        if (text.isEmpty()) {
            konusmalar.addAll(konusmalarKopy)
        } else {
            text = text.toLowerCase()
            for (item in konusmalarKopy) {

                if (item.son_mesaj!!.toLowerCase().contains(text) || item.groupName!!.toLowerCase().contains(text)
                ) {
                    konusmalar.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }

    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("HH:mm")
        return format.format(date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupKonusmaViewHolder {
        var view = LayoutInflater.from(ctx).inflate(R.layout.groupkonusmachild,parent,false)
        return GroupKonusmaViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupKonusmaViewHolder, position: Int) {
        holder.setData(konusmalar.get(position),ctx)
    }

    override fun getItemCount(): Int {
        return konusmalar.size
    }
}