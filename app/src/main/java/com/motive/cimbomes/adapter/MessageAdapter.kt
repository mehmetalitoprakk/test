package com.motive.cimbomes.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.motive.cimbomes.R
import com.motive.cimbomes.model.Mesaj
import kotlinx.android.synthetic.main.chat_child_getter.view.*
import kotlinx.android.synthetic.main.chat_child_sender.view.*
import kotlinx.android.synthetic.main.chat_child_sender.view.tvMessageSender

class MessageAdapter(var mesahlar:ArrayList<Mesaj>,var ctx: Context,var listener:OnItemClickListener) : RecyclerView.Adapter<MessageAdapter.MesajViewHolder>() {
    inner class MesajViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),View.OnClickListener {
        var tumLayout = itemView as ConstraintLayout
        var mesaj = tumLayout.tvMessageSender


        init {
            itemView.setOnClickListener(this)
        }

        fun setData(oankiMesaj: Mesaj){
            mesaj.text = oankiMesaj.mesaj
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MesajViewHolder {

        var myView: View? = null

        if (viewType == 2){
            myView = LayoutInflater.from(ctx).inflate(R.layout.chat_child_getter,parent,false)
        }else if(viewType == 1){
            myView = LayoutInflater.from(ctx).inflate(R.layout.chat_child_sender,parent,false)
        }

        return MesajViewHolder(myView!!)

    }

    override fun onBindViewHolder(holder: MesajViewHolder, position: Int) {
        holder.setData(mesahlar.get(position))
    }

    override fun getItemViewType(position: Int): Int {
        if (mesahlar.get(position).user_id.equals(FirebaseAuth.getInstance().currentUser!!.uid)){
            return 1
        }else{
            return 2
        }
    }

    override fun getItemCount(): Int {
        return mesahlar.size
    }
}