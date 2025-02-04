package com.motive.cimbomes.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.motive.cimbomes.R
import com.motive.cimbomes.activity.ChatActivity
import com.motive.cimbomes.model.Konusma
import com.motive.cimbomes.model.Users
import com.motive.cimbomes.utils.TimeAgo
import com.motive.cimbomes.utils.UniversalImageLoader
import kotlinx.android.synthetic.main.konusma_child.view.*

class KonusmalarAdapter(var konsumalar : ArrayList<Konusma>,var konusmalarKopy : ArrayList<Konusma>,var ctx : Context,var listener : OnItemLongClickListener) : RecyclerView.Adapter<KonusmalarAdapter.KonsumaViewHolder>() {
    inner class KonsumaViewHolder(itemVİew : View) : RecyclerView.ViewHolder(itemVİew),View.OnLongClickListener {
        var isimsoyisim = ""
        var pic = ""
        var tumLayout = itemVİew as ConstraintLayout
        var sonMesaj = tumLayout.son_mesaj_konusma_tv
        var sonZaman = tumLayout.tarih_konusma_tv
        var konusmaName = tumLayout.user_name_konusma_tv
        var image = tumLayout.konsuma_circle_img
        var okunduBilgisi = tumLayout.imgOkunmadi

        fun setData(oAnkiKonusma : Konusma,ctx: Context){


            var konusmaText = oAnkiKonusma.son_mesaj.toString()
            konusmaText = konusmaText.replace("\n"," ")
            konusmaText=konusmaText.trim()


            if (konusmaText.length > 30){
                sonMesaj.text = konusmaText.substring(0,30)+"..."
            }else{
                sonMesaj.text = konusmaText
            }
            if (oAnkiKonusma.time != null){
                sonZaman.text = TimeAgo.getTimeAgoForComments(oAnkiKonusma.time!!.toLong())
            }

            if (oAnkiKonusma.goruldu == false){
                okunduBilgisi.visibility = View.VISIBLE
                sonMesaj.setTextColor(ctx.resources.getColor(R.color.black))
                sonMesaj.setTypeface(Typeface.DEFAULT_BOLD)
                konusmaName.setTypeface(Typeface.DEFAULT_BOLD)
                sonZaman.setTextColor(ctx.resources.getColor(R.color.black))
                sonZaman.setTypeface(Typeface.DEFAULT_BOLD)
            }else{
                okunduBilgisi.visibility = View.INVISIBLE
                sonMesaj.setTextColor(ctx.resources.getColor(R.color.defaultext))
                sonMesaj.setTypeface(Typeface.DEFAULT)
                konusmaName.setTypeface(Typeface.DEFAULT)
                sonZaman.setTextColor(ctx.resources.getColor(R.color.defaultext))
                sonZaman.setTypeface(Typeface.DEFAULT)
            }

            tumLayout.setOnClickListener {
                var intent = Intent(ctx,ChatActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                intent.putExtra("tiklananid",oAnkiKonusma.user_id.toString())
                intent.putExtra("tiklananisim",isimsoyisim.toString())
                intent.putExtra("tiklananfoto",pic.toString())
                FirebaseDatabase.getInstance().reference.child("konusmalar").child(FirebaseAuth.getInstance().currentUser!!.uid).child(oAnkiKonusma.user_id.toString()).child("goruldu")
                        .setValue(true)
                        .addOnCompleteListener {
                            ctx.startActivity(intent)
                        }

            }

            sohbetEdilenKullaniciBilgileriniGetir(oAnkiKonusma.user_id.toString())

        }

        private fun sohbetEdilenKullaniciBilgileriniGetir(user_id: String) {
            FirebaseDatabase.getInstance().reference.child("users").child(user_id).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.getValue() != null){
                            var bulunanKullanici =snapshot.getValue(Users::class.java)
                            konusmaName.text = bulunanKullanici!!.isim + " " + bulunanKullanici.soyisim
                            isimsoyisim = bulunanKullanici!!.isim + " " + bulunanKullanici.soyisim
                            var img = bulunanKullanici!!.profilePic.toString()
                            pic = img
                            if (img != null || img != ""){
                                UniversalImageLoader.setImage(img,image,null,"")
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        }

        init {
            tumLayout.setOnLongClickListener(this)
        }

        override fun onLongClick(v: View?): Boolean {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION){
                listener.onItemLongClicked(position)
                return false
            }
            return true
        }


    }

    interface OnItemLongClickListener {
        fun onItemLongClicked(position: Int) : Boolean
    }

    fun filter(text: String) {
        var text = text
        konsumalar.clear()
        if (text.isEmpty()) {
            konsumalar.addAll(konusmalarKopy)
        } else {
            text = text.toLowerCase()
            for (item in konusmalarKopy) {

                if (item.son_mesaj!!.toLowerCase().contains(text) || item.name!!.toLowerCase().contains(text)
                ) {
                    konsumalar.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KonsumaViewHolder {
        var view = LayoutInflater.from(ctx).inflate(R.layout.konusma_child,parent,false)
        return KonsumaViewHolder(view)
    }

    override fun onBindViewHolder(holder: KonsumaViewHolder, position: Int) {
        holder.setData(konsumalar.get(position),ctx)


    }

    override fun getItemCount(): Int {
        return konsumalar.size
    }


}