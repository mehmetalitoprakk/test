package com.motive.cimbomes.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.motive.cimbomes.R
import com.motive.cimbomes.model.Contact
import com.motive.cimbomes.utils.UniversalImageLoader
import de.hdodenhof.circleimageview.CircleImageView

class EditGroupContactAdapter(private var listener : OnItemClickListener) : RecyclerView.Adapter<EditGroupContactAdapter.EditGroupContactViewHolder>() {
    var datalist = emptyList<Contact>()
    internal fun setDataList(datalist : List<Contact>){
        this.datalist = datalist
        notifyDataSetChanged()
    }


    inner class EditGroupContactViewHolder(view : View) : RecyclerView.ViewHolder(view),View.OnClickListener {
        var name : TextView
        var number : TextView
        var image : CircleImageView

        init {
            name = itemView.findViewById(R.id.txtContactName)
            number = itemView.findViewById(R.id.txtContactStatus)
            image = itemView.findViewById(R.id.imgContactUserInfo)
            itemView.setOnClickListener(this)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditGroupContactViewHolder {
        return EditGroupContactViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.contactslayout,parent,false))
    }

    override fun onBindViewHolder(holder: EditGroupContactViewHolder, position: Int) {
        holder.name.text = datalist[position].name
        holder.number.text = datalist.get(position).number
        if (datalist[position].image != null){
            UniversalImageLoader.setImage(datalist[position].image!!,holder.image,null,"")
        }
    }

    override fun getItemCount(): Int {
        return datalist.size
    }
}