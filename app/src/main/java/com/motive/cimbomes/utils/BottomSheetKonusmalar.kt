package com.motive.cimbomes.utils

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
import kotlinx.android.synthetic.main.fragment_bottom_sheet_konusmalar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class BottomSheetKonusmalar : BottomSheetDialogFragment() {
    var uid : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bottom_sheet_konusmalar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetKonusmalarKonusmayıSil.setOnClickListener {
            if (uid != null){
                FirebaseDatabase.getInstance().reference.child("konusmalar").child(FirebaseAuth.getInstance().currentUser!!.uid).child(uid!!).removeValue().addOnSuccessListener {
                    Toast.makeText(requireContext(),"Konuşma başarıyla silindi",Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }



    @Subscribe(sticky = true)
    internal fun onKonusmaBilgiAl(konusmaBilgi : EventBusDataEvents.SendChatInfoOne){
        uid = konusmaBilgi.uid
    }


}