package com.motive.cimbomes.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.motive.cimbomes.R
import com.motive.cimbomes.model.Contact
import com.motive.cimbomes.utils.UniversalImageLoader
import de.hdodenhof.circleimageview.CircleImageView

class SelectedContactAdapter(private var listener : OnItemClickListener) : RecyclerView.Adapter<SelectedContactAdapter.SelectedContactAdapterViewHolder>() {
    var datalist = emptyList<Contact>()

    internal fun setDataList(datalist : List<Contact>){
        this.datalist = datalist
        notifyDataSetChanged()
    }


    inner class SelectedContactAdapterViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView),View.OnClickListener {
     var image : CircleImageView

     init {
         image = itemView.findViewById(R.id.imgSelectedGroupContact)
         itemView.setOnClickListener(this)
     }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION){
                listener.onItemClickSelected(position)
            }
        }

    }



    interface OnItemClickListener{
        fun onItemClickSelected(position: Int)
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SelectedContactAdapterViewHolder {
        return SelectedContactAdapterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.selectedcontacts_child,parent,false))
    }

    override fun onBindViewHolder(holder: SelectedContactAdapterViewHolder, position: Int) {
        if (datalist[position].image != null){
            UniversalImageLoader.setImage(datalist[position].image!!,holder.image,null,"")
        }else{

        }

    }

    override fun getItemCount(): Int {
        return datalist.size
    }
}