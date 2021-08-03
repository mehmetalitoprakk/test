package com.motive.cimbomes.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.motive.cimbomes.R
import com.motive.cimbomes.model.Contact
import com.motive.cimbomes.utils.UniversalImageLoader
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception

class ContactAdapter(private val listener:OnItemClickListener,var ctx:Context) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {
    var datalist = emptyList<Contact>()
    internal fun setDataList(datalist : List<Contact>){
        this.datalist = datalist
        notifyDataSetChanged()
    }

    inner class ContactViewHolder(view : View) : RecyclerView.ViewHolder(view),View.OnClickListener {
        var number : TextView
        var img : CircleImageView
        var name : TextView

        init {
            number = view.findViewById(R.id.tvKisilerNumber)
            img = view.findViewById(R.id.circleKisiler)
            name = view.findViewById(R.id.tvkisilername)

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.contact_child,parent,false))
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.number.text = datalist[position].number
        holder.name.text = datalist[position].name
        if (datalist[position].image != null){
            UniversalImageLoader.setImage(datalist[position].image!!,holder.img,null,"")
        }else{

        }

    }

    override fun getItemCount(): Int {
        return datalist.size
    }
}