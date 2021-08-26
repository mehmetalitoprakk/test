package com.motive.cimbomes.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.motive.cimbomes.R
import com.motive.cimbomes.activity.GroupChatActivity
import com.motive.cimbomes.model.GroupKonusma
import com.motive.cimbomes.model.GroupMembers
import com.motive.cimbomes.model.Groups
import com.motive.cimbomes.model.Users
import com.motive.cimbomes.utils.EventBusDataEvents
import kotlinx.android.synthetic.main.fragment_yeni_grup.*
import org.greenrobot.eventbus.EventBus


class YeniGrupFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db : DatabaseReference
    private lateinit var storageReference: StorageReference
    private val RESIM_SEC = 100
    private var profilePhotoUri: Uri? = null
    private var statik = false
    private var members = mutableListOf<GroupMembers>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("KONTROL","ON CREATE")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_yeni_grup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        newGroupMainContainer.visibility = View.VISIBLE

        db = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference
        mAuth = FirebaseAuth.getInstance()

        adminOzellikleriKapat()

        db.child("users").child(mAuth.currentUser!!.uid).child("admin").get().addOnSuccessListener {
            if (it.value == true){
                adminOzellikleriAc()
            }else{
                adminOzellikleriKapat()
            }
        }

        isStatic.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if (isChecked){
                    statik = true
                }else if (!isChecked){
                    statik = false
                }
            }
        })






        btnNewGroup.setOnClickListener {
            if (statik){
                val groupNameET = etNewGroupName.text.toString().trim()
                val grupName = etNewGroupName.text.toString()
                if (groupNameET.isNullOrEmpty() || groupNameET.length < 3){
                    etNewGroupName.setError("Grup ismi 3 harften az olmamalıdır.")
                    etNewGroupName.requestFocus()
                }else if (groupNameET.length > 25){
                    etNewGroupName.setError("Grup ismi 25 harften fazla olmamalıdır.")
                    etNewGroupName.requestFocus()
                }else{
                    if (profilePhotoUri == null){
                        Toast.makeText(requireContext(),"Grup Fotoğrafı Seçiniz",Toast.LENGTH_SHORT).show()
                    }else{
                        staticProgressBar.visibility = View.VISIBLE
                        db.child("users").addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(snapshot.getValue() != null){
                                    for (i in snapshot.children){
                                        val user = i.getValue(Users::class.java)
                                        if (user!!.uid != mAuth.currentUser!!.uid){
                                            val member = GroupMembers(false,user.uid,user.isim,user.soyisim,user.telefonNo,false,user.profilePic)
                                            members.add(member)
                                        }else{
                                            val admin = GroupMembers(true,user.uid,user.isim,user.soyisim,user.telefonNo,false,user.profilePic)
                                            members.add(0,admin)
                                        }
                                    }
                                    createGroup(grupName)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })


                    }
                }

            }else if (!statik){
                val groupNameET = etNewGroupName.text.toString().trim()
                val grupName = etNewGroupName.text.toString()
                if (groupNameET.isNullOrEmpty() || groupNameET.length < 3){
                    etNewGroupName.setError("Grup ismi 3 harften az olmamalıdır.")
                    etNewGroupName.requestFocus()
                }else if (groupNameET.length > 25){
                    etNewGroupName.setError("Grup ismi 25 harften fazla olmamalıdır.")
                    etNewGroupName.requestFocus()
                }else{
                    if (profilePhotoUri == null){
                        Toast.makeText(requireContext(),"Grup Fotoğrafı Seçiniz",Toast.LENGTH_SHORT).show()
                    }else{
                        var bundle = Bundle()

                        var memberFragment = SelectGroupMemberFragment()
                        memberFragment.arguments = bundle
                        requireActivity().supportFragmentManager.beginTransaction().apply {
                            EventBus.getDefault().postSticky(EventBusDataEvents.SendGroupData(grupName,profilePhotoUri.toString()))
                            replace(R.id.containerFragmnet,SelectGroupMemberFragment())
                            commit()
                            newGroupMainContainer.visibility = View.GONE
                        }



                    }
                }
            }

        }


        newGroupAddPhotoImg.setOnClickListener {
            resimSec()
        }

        tvNewPhotoGroup.setOnClickListener {
            resimSec()
        }


    }

    private fun createGroup(name : String) {
        val newGroupKey = db.child("groups").push().key.toString()
        val group = Groups(newGroupKey,mAuth.currentUser!!.uid,members,System.currentTimeMillis(),"",name,true,null)
        db.child("groups").child(newGroupKey).setValue(group).addOnSuccessListener {
            storageReference.child("groups").child(newGroupKey!!).child("groupImage").putFile(profilePhotoUri!!).addOnSuccessListener {
                storageReference.child("groups").child(newGroupKey!!).child("groupImage").downloadUrl.addOnSuccessListener {
                    var groupImageDb = it.toString()
                    db.child("groups").child(newGroupKey).child("image").setValue(groupImageDb).addOnSuccessListener {
                        for (i in members){
                            var gorulduMu = false
                            if (i.uid.toString() == mAuth.currentUser!!.uid){
                                gorulduMu = true
                            }
                            var groupKonusma = GroupKonusma(gorulduMu,"",System.currentTimeMillis(),i.uid,groupImageDb,newGroupKey,name)
                            db.child("grupkonusmalar").child(i.uid!!.toString()).child(newGroupKey).setValue(groupKonusma)
                        }
                        val intent = Intent(requireContext(),GroupChatActivity::class.java)
                        intent.putExtra("GroupKey",newGroupKey)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
            }
        }
    }

    private fun adminOzellikleriKapat() {
        adminProperties.visibility = View.GONE
        adminView1.visibility = View.GONE
        adminView2.visibility = View.GONE
        isStatic.visibility = View.GONE
    }

    private fun adminOzellikleriAc() {
        adminProperties.visibility = View.VISIBLE
        adminView1.visibility = View.VISIBLE
        adminView2.visibility = View.VISIBLE
        isStatic.visibility = View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESIM_SEC && resultCode == AppCompatActivity.RESULT_OK && data!!.data != null){
            profilePhotoUri = data.data
            newGroupProfilePicture.setImageURI(profilePhotoUri)
            newGroupAddPhotoImg.visibility = View.GONE
        }
    }

    private fun resimSec() {
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_PICK)
        startActivityForResult(intent,RESIM_SEC)
    }



}