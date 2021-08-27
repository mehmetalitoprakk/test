package com.motive.cimbomes.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.motive.cimbomes.R
import com.motive.cimbomes.model.Mesaj
import com.motive.cimbomes.utils.EventBusDataEvents
import kotlinx.android.synthetic.main.fragment_group_messages_bottom_sheet.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class GroupMessagesBottomSheet : BottomSheetDialogFragment() {
    var mesajKey = ""
    var gonderenID = ""
    var groupID = ""
    lateinit var mesajG : Mesaj


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_messages_bottom_sheet, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (FirebaseAuth.getInstance().currentUser!!.uid.toString() != gonderenID){
            groupMesajlarBottomSheetHerkestenSil.visibility = View.GONE
            mesajViewGizlenicek.visibility = View.GONE
        }




        groupMesajlarBottomSheetHerkestenSil.setOnClickListener {
            FirebaseDatabase.getInstance().reference.child("groups").child(groupID).child("messages").child(mesajKey).removeValue().addOnSuccessListener {
                Toast.makeText(requireContext(),"Mesaj Silindi",Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }


        groupMesajlarBottomSheetMesajiKaydet.setOnClickListener {
            FirebaseDatabase.getInstance().reference.child("savedMessages").child(FirebaseAuth.getInstance().currentUser!!.uid).child(mesajKey).setValue(mesajG).addOnSuccessListener {
                Toast.makeText(requireContext(),"Kaydedildi",Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
    }


    override fun onAttach(context: Context) {
        EventBus.getDefault().register(this)
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = true)
    internal fun onBilgiAl(mesaj : EventBusDataEvents.SendGroupMessageInfo){
        mesajKey = mesaj.mesajKey
        gonderenID = mesaj.gonderenID
        groupID = mesaj.groupID
        mesajG = mesaj.mesajGroup

    }

}