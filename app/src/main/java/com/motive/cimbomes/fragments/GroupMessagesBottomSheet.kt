package com.motive.cimbomes.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.motive.cimbomes.R
import com.motive.cimbomes.activity.UserDetail
import com.motive.cimbomes.model.GroupMembers
import com.motive.cimbomes.model.Mesaj
import com.motive.cimbomes.model.Users
import com.motive.cimbomes.utils.EventBusDataEvents
import kotlinx.android.synthetic.main.fragment_group_messages_bottom_sheet.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class GroupMessagesBottomSheet : BottomSheetDialogFragment() {
    var mesajKey = ""
    var gonderenID = ""
    var groupID = ""
    lateinit var mesajG : Mesaj
    var kullaniciGruptaMi = false


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
        groupMesajlarBottomSheetKullanıcıyıGruptanÇıkar.visibility = View.GONE

        var kontrolList = arrayListOf<String>()
        FirebaseDatabase.getInstance().reference.child("groups").child(groupID).child("member").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.getValue() != null){
                    for (i in snapshot.children){
                        if (i.getValue() != null){
                            val user = i.getValue(GroupMembers::class.java)
                            kontrolList.add(user!!.uid.toString())
                        }
                    }
                    if (!kontrolList.contains(gonderenID)){
                        groupMesajlarBottomSheetKullanıcıyıGruptanÇıkar.visibility = View.GONE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


        FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("admin").get().addOnSuccessListener { myAdmin ->
                FirebaseDatabase.getInstance().reference.child("users").child(gonderenID).child("admin").get().addOnSuccessListener { gonderenAdmin ->
                    if (FirebaseAuth.getInstance().currentUser!!.uid.toString() != gonderenID && myAdmin.value == false){
                        Log.e("KONTROL","1. İF")
                        groupMesajlarBottomSheetHerkestenSil.visibility = View.GONE
                        mesajViewGizlenicek.visibility = View.GONE
                        groupMesajlarBottomSheetKullanıcıyıGruptanÇıkar.visibility = View.GONE
                    }else if (gonderenID == FirebaseAuth.getInstance().currentUser!!.uid){
                        Log.e("KONTROL","2. İF")
                        mesajViewGizlenicek.visibility = View.GONE
                        groupMesajlarBottomSheetKullanıcıyıGruptanÇıkar.visibility = View.GONE
                        groupMesajlarBottomSheetHerkestenSil.visibility = View.VISIBLE
                        mesajViewGizlenicek.visibility = View.VISIBLE
                        groupMesajlarBottomSheetBilgi.visibility = View.GONE
                    }else if(myAdmin.value == true && gonderenAdmin.value == true && gonderenID != FirebaseAuth.getInstance().currentUser!!.uid){
                        Log.e("KONTROL","3. İF")
                        groupMesajlarBottomSheetHerkestenSil.visibility = View.VISIBLE
                        mesajViewGizlenicek.visibility = View.GONE
                        groupMesajlarBottomSheetKullanıcıyıGruptanÇıkar.visibility = View.GONE
                        mesajViewGizlenicek.visibility = View.VISIBLE
                        groupMesajlarBottomSheetBilgi.visibility = View.VISIBLE
                    }else if (myAdmin.value == true && gonderenAdmin.value == false && kontrolList.contains(gonderenID)){
                        Log.e("KONTROL","4. İF")
                        groupMesajlarBottomSheetBilgi.visibility = View.VISIBLE
                        groupMesajlarBottomSheetKullanıcıyıGruptanÇıkar.visibility = View.VISIBLE
                    }
                }

        }


        groupMesajlarBottomSheetBilgi.setOnClickListener {
            var name = ""
            var surname = ""
            var phone = ""
            var image = ""
            var uid = ""
            FirebaseDatabase.getInstance().reference.child("users").child(gonderenID).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.getValue() != null){
                        val user = snapshot.getValue(Users::class.java)
                        if (user != null){
                            name = user.isim!!
                            surname = user.soyisim!!
                            phone = user.telefonNo!!
                            image = user.profilePic!!
                            uid = user.uid!!

                            val intent = Intent(requireContext(),UserDetail::class.java)
                            intent.putExtra("nameInfo",name)
                            intent.putExtra("surnameInfo",surname)
                            intent.putExtra("uidInfo",uid)
                            intent.putExtra("phoneInfo",phone)
                            intent.putExtra("imageInfo",image)
                            startActivity(intent)
                            dismiss()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(),"Bir hata oluştu.",Toast.LENGTH_SHORT).show()
                    requireActivity().finish()
                }

            })
        }




        groupMesajlarBottomSheetKullanıcıyıGruptanÇıkar.setOnClickListener {
            FirebaseDatabase.getInstance().reference.child("grupkonusmalar").child(gonderenID).child(groupID).removeValue().addOnSuccessListener {
                FirebaseDatabase.getInstance().reference.child("groups").child(groupID).child("member").addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.getValue() != null){
                            for (i in snapshot.children){
                                val user = i.getValue(GroupMembers::class.java)
                                if (user != null) {
                                    if (user.uid.toString() == gonderenID){
                                        i.ref.removeValue()
                                        dismiss()
                                        Toast.makeText(requireContext(),"Kullanıcı Gruptan Çıkarıldı.",Toast.LENGTH_SHORT).show()
                                        break
                                    }
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }
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