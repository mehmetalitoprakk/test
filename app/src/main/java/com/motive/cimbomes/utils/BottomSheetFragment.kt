package com.motive.cimbomes.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.motive.cimbomes.R
import com.motive.cimbomes.activity.ChatActivity
import com.motive.cimbomes.activity.UserDetail
import com.motive.cimbomes.adapter.GroupInfoMembersAdapter
import com.motive.cimbomes.model.GroupMembers
import kotlinx.android.synthetic.main.fragment_bottom_dialog.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class BottomSheetFragment : BottomSheetDialogFragment() {
    lateinit var groupmember : GroupMembers
    lateinit var db : DatabaseReference
    lateinit var groupKey : String
    var isCreator = false
    lateinit var pos : String



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_dialog,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseDatabase.getInstance().reference


        db.child("groups").child(groupKey).child("creator").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var uid = snapshot.getValue().toString()
                if (uid == FirebaseAuth.getInstance().currentUser!!.uid.toString()){
                    isCreator = true
                }
                if (!groupmember.groupAdmin!!){
                    bottomsheetyönetici.visibility = View.VISIBLE
                    bottomShhetNotYonetici.visibility = View.GONE
                }else if (groupmember.groupAdmin!!){
                    if (isCreator){
                        bottomsheetyönetici.visibility = View.GONE
                        bottomShhetNotYonetici.visibility = View.VISIBLE
                    }else if(!isCreator){
                        bottomsheetyönetici.visibility = View.GONE
                        bottomShhetNotYonetici.visibility = View.GONE
                        bottomsheetgruptancıkar.visibility = View.GONE
                    }

                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        bottomsheetgruptancıkar.setOnClickListener {
            //TODO UI GUNCELLENİCEK VE INFO ACTİVİTY DE UYE DEGİLSE ANASAYFA YONLENDİRMESİ YAPILICAK
            Log.e("KONTROL","TIKLANDI")
            db.child("grupkonusmalar").child(groupmember.uid!!).child(groupKey).removeValue().addOnSuccessListener {
                Log.e("KONTROL","konsumalardan silindi")
                db.child("groups").child(groupKey).child("member").addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.getValue()!=null){
                            Log.e("KONTROL","null gelmedi")
                            for ((index,element) in snapshot.children.withIndex()){
                                var user = element.getValue(GroupMembers::class.java)
                                if (user!!.uid == groupmember.uid){
                                    Log.e("KONTROL","user bulundu")
                                    db.child("groups").child(groupKey).child("member").child(index.toString()).removeValue()
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
            }
        }





        bottomsheetbilgi.setOnClickListener {
            val intent = Intent(requireContext(),UserDetail::class.java)
            intent.putExtra("nameInfo",groupmember.name)
            intent.putExtra("surnameInfo",groupmember.surname)
            intent.putExtra("uidInfo",groupmember.uid)
            intent.putExtra("phoneInfo",groupmember.number)
            intent.putExtra("imageInfo",groupmember.image)
            startActivity(intent)
            dismiss()
        }

        bottomsheetgruptancıkar.setOnClickListener {
            db.child("groups").child(groupKey).child("member").child(pos).removeValue().addOnCompleteListener {
                db.child("grupkonusmalar").child(groupmember.uid!!).child(groupKey).removeValue().addOnSuccessListener {
                    dismiss()
                }
            }
        }

        bottomsheetisim.text = groupmember.name + " " + groupmember.surname

        bottomsheetyönetici.setOnClickListener {
            db.child("groups").child(groupKey).child("member").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.getValue() != null){
                        db.child("groups").child(groupKey).child("member").child(pos).child("groupAdmin").setValue(true).addOnCompleteListener {
                            dismiss()
                        }
                    }
                }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }

        bottomshhetMesajGönder.setOnClickListener {
            val intent = Intent(requireContext(),ChatActivity::class.java)
            intent.putExtra("tiklananid",groupmember!!.uid!!)
            intent.putExtra("tiklananisim",groupmember.name + " " + groupmember.surname)
            intent.putExtra("tiklananfoto",groupmember.image)
            startActivity(intent)
            requireActivity().finish()
        }

        bottomShhetNotYonetici.setOnClickListener {
            db.child("groups").child(groupKey).child("member").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.getValue() != null){
                        db.child("groups").child(groupKey).child("member").child(pos).child("groupAdmin").setValue(false).addOnCompleteListener {
                            dismiss()
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
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
    internal fun onMemberAl(member : EventBusDataEvents.SendBottomSheet){
        groupmember = member.groupMember
        groupKey = member.groupKey
        pos = member.position!!.toString()
    }

}