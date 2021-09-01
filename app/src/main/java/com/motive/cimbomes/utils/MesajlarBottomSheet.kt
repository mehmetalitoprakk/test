package com.motive.cimbomes.utils

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.motive.cimbomes.R
import com.motive.cimbomes.adapter.MessageAdapter
import com.motive.cimbomes.model.Mesaj
import kotlinx.android.synthetic.main.fragment_mesajlar_bottom_sheet.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class MesajlarBottomSheet : BottomSheetDialogFragment() {
    private var mesajKey : String? = null
    private var gonderenID : String? = null
    private var karsitarafuid : String? = null
    private var myUid : String? = null
    private lateinit var db : DatabaseReference
    private lateinit var mAuth : FirebaseAuth
    private var savedMessages = arrayListOf<Mesaj>()
    var gonderenBenMi = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mesajlar_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        myUid = mAuth.currentUser!!.uid
        println(mesajKey + " 8888 ")
        println(karsitarafuid + " 7777")
        println(gonderenID + " 7777 ")


        if (myUid != gonderenID){
                gonderenBenMi = false
                mesajlarBottomSheetHerkestenSil.visibility = View.GONE
        }else{
            gonderenBenMi = true
            mesajlarBottomSheetHerkestenSil.visibility = View.VISIBLE
        }

        mesajlarBottomSheetBendenSil.setOnClickListener {
            mesajiBendenSiL()
        }

        mesajlarBottomSheetHerkestenSil.setOnClickListener {
            mesajiHerkestenSil()
        }

        mesajlarBottomSheetMesajiKaydet.setOnClickListener {
            if (gonderenBenMi){
                db.child("chats").child(myUid!!).child(karsitarafuid!!).child(mesajKey!!).get().addOnSuccessListener {
                    if (it.getValue() != null){
                        var mesaj = it.getValue(Mesaj::class.java)
                        var savedMessageKey = db.child("savedMessages").child(myUid!!).push().key
                        if (savedMessageKey != null){
                            db.child("savedMessages").child(myUid!!).child(savedMessageKey!!).setValue(mesaj).addOnSuccessListener {
                                android.widget.Toast.makeText(requireContext(),"Kaydedildi",
                                    android.widget.Toast.LENGTH_SHORT).show()
                                dismiss()
                            }
                        }

                    }
                }
            }else{
                db.child("chats").child(karsitarafuid!!).child(myUid!!).child(mesajKey!!).get().addOnSuccessListener {
                    if (it.getValue() != null){
                        var mesaj = it.getValue(Mesaj::class.java)
                        var savedMessageKey = db.child("savedMessages").child(myUid!!).push().key
                        if (savedMessageKey != null){
                            db.child("savedMessages").child(myUid!!).child(savedMessageKey!!).setValue(mesaj).addOnSuccessListener {
                                Toast.makeText(requireContext(),"Kaydedildi",Toast.LENGTH_SHORT).show()
                                dismiss()
                            }
                        }

                    }
                }
            }

        }


    }

    private fun mesajiHerkestenSil() {
        db.child("chats").child(myUid!!).child(karsitarafuid!!).child(mesajKey!!).removeValue().addOnSuccessListener {
            db.child("chats").child(karsitarafuid!!).child(myUid!!).child(mesajKey!!).removeValue().addOnSuccessListener {
                Toast.makeText(requireContext(),"Mesaj Herkesten Silindi",Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
    }

    private fun mesajiBendenSiL() {
        db.child("chats").child(myUid!!).child(karsitarafuid!!).child(mesajKey!!).removeValue().addOnSuccessListener {
            Toast.makeText(requireContext(),"Mesaj Sizden Silindi",Toast.LENGTH_SHORT).show()
            dismiss()
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
    internal fun onMesajInfoAl(mesajInfo : EventBusDataEvents.SendMessageInfo){
        mesajKey = mesajInfo.mesajKey
        gonderenID = mesajInfo.gonderenID
        karsitarafuid = mesajInfo.mesajiAlanId
    }

}