package com.motive.cimbomes.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.motive.cimbomes.R
import com.motive.cimbomes.activity.GroupInfoActivity
import com.motive.cimbomes.model.Groups
import com.motive.cimbomes.utils.UniversalImageLoader
import kotlinx.android.synthetic.main.kalici_gruplar_child.view.*

class GrupYonetimiAdapter(var grups : ArrayList<Groups>,var admins : ArrayList<String>, var context: Context,var üyeSayisi : ArrayList<String>) : RecyclerView.Adapter<GrupYonetimiAdapter.GrupYonetimiViewHolder>() {
    class GrupYonetimiViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        var tumLayout = view as ConstraintLayout
        var grupName = tumLayout.grupNameKaliciGruplar
        var grupImage = tumLayout.imageViewKaliciGruplar
        var üyeSayisi = tumLayout.textView19
        var aadminSayisi = tumLayout.textView20
        var nextButton = tumLayout.grubuGörButtonKaliciGruplar


        fun setData(grup : Groups,adminSayisi : String,ctx : Context,üyeSayi : String){
            grupName.text = grup.groupName
            üyeSayisi.text = üyeSayi
            aadminSayisi.text = adminSayisi
            UniversalImageLoader.setImage(grup.image!!,grupImage,null,"")
            nextButton.setOnClickListener {
                val intent = Intent(ctx,GroupInfoActivity::class.java)
                intent.putExtra("grupID",grup.groupID.toString())
                intent.putExtra("grupImage",grup.image.toString())
                intent.putExtra("grupName",grup.groupName.toString())
                intent.putExtra("isStatic",true)
                ctx.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GrupYonetimiViewHolder {
        return GrupYonetimiViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.kalici_gruplar_child,parent,false))
    }

    override fun onBindViewHolder(holder: GrupYonetimiViewHolder, position: Int) {
        holder.setData(grups.get(position),admins.get(position),context,üyeSayisi.get(position))
    }

    override fun getItemCount(): Int {
        return grups.size
    }

}