package com.motive.cimbomes.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.motive.cimbomes.R
import com.motive.cimbomes.model.GroupMembers
import com.motive.cimbomes.utils.UniversalImageLoader
import kotlinx.android.synthetic.main.fragment_ayarlar.*


class AyarlarFragment : Fragment() {
    private lateinit var db : DatabaseReference
    private lateinit var mAuth : FirebaseAuth
    private lateinit var name : String
    private lateinit var surname : String
    private lateinit var storage : StorageReference
    val RESIM_SEC = 100
    private var newPhotoUri : Uri? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ayarlar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance().reference


        profile_progress.visibility = View.GONE

        getInfo()

        profile_change_photo.setOnClickListener {
            resimSec()
        }

        profile_kaydet_button.setOnClickListener {
            profile_progress.visibility =View.VISIBLE
            val yeniName = profileName.text.toString()
            val yeniSurname = profileSurname.text.toString()
            profile_change_photo.isEnabled = false
            profile_kaydet_button.isEnabled = false
            if (newPhotoUri != null){
                db.child("users").child(mAuth.currentUser!!.uid).child("isim").setValue(yeniName).addOnSuccessListener {
                    db.child("users").child(mAuth.currentUser!!.uid).child("soyisim").setValue(yeniSurname).addOnSuccessListener {
                        storage.child("users").child(mAuth.currentUser!!.uid).child("profile_picture").putFile(newPhotoUri!!)
                            .addOnSuccessListener{
                                storage.child("users").child(mAuth.currentUser!!.uid).child("profile_picture").downloadUrl.addOnSuccessListener {
                                    val url = it.toString()
                                    db.child("users").child(mAuth.currentUser!!.uid).child("profilePic").setValue(url).addOnSuccessListener {
                                        db.child("grupkonusmalar").child(mAuth.currentUser!!.uid).addListenerForSingleValueEvent(object : ValueEventListener{
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (snapshot.getValue() != null){
                                                    var keys = arrayListOf<String>()
                                                    for (i in snapshot.children){
                                                        keys.add(snapshot.key.toString())
                                                    }
                                                    for (j in keys){
                                                        db.child("groups").child(j).child("member").addListenerForSingleValueEvent(object : ValueEventListener{
                                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                                if (snapshot.getValue() != null){
                                                                    for (i in snapshot.children){
                                                                        val user = i.getValue(GroupMembers::class.java)
                                                                        if (user != null){
                                                                            if (user.uid != null){
                                                                                if (user.uid == mAuth.currentUser!!.uid){
                                                                                    i.ref.child("image").setValue(url).addOnSuccessListener {
                                                                                        i.ref.child("name").setValue(yeniName + " " + yeniSurname).addOnSuccessListener {
                                                                                            UniversalImageLoader.setImage(url,profile_image,null,"")
                                                                                            profile_progress.visibility = View.GONE
                                                                                            profile_change_photo.isEnabled = true
                                                                                            profile_kaydet_button.isEnabled = true
                                                                                            Toast.makeText(requireContext(),"Bilgileriniz sssbaşarıyla güncellendi.",Toast.LENGTH_SHORT).show()
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            override fun onCancelled(error: DatabaseError) {

                                                            }

                                                        })
                                                    }

                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                TODO("Not yet implemented")
                                            }

                                        })
                                    }
                                }

                            }

                    }
                }
            }else{
                db.child("users").child(mAuth.currentUser!!.uid).child("isim").setValue(yeniName).addOnSuccessListener {
                    db.child("users").child(mAuth.currentUser!!.uid).child("soyisim").setValue(yeniSurname).addOnSuccessListener {
                        profile_progress.visibility = View.GONE
                        profile_change_photo.isEnabled = true
                        profile_kaydet_button.isEnabled = true
                        Toast.makeText(requireContext(),"Bilgileriniz başarıyla güncellendi.",Toast.LENGTH_SHORT).show()

                    }
                }
            }
        }


    }

    private fun getInfo() {
        db.child("users").child(mAuth.currentUser!!.uid).child("isim").get().addOnSuccessListener {
            profileName.setText(it.value.toString())
            name = it.value.toString()
            db.child("users").child(mAuth.currentUser!!.uid).child("soyisim").get().addOnSuccessListener {
                profileSurname.setText(it.value.toString())
                surname = it.value.toString()
                db.child("users").child(mAuth.currentUser!!.uid).child("profilePic").get().addOnSuccessListener {
                    if (it.value != null){
                        UniversalImageLoader.setImage(it.value.toString(),profile_image,null,"")
                        profile_progress.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun resimSec() {
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_PICK)
        startActivityForResult(intent,RESIM_SEC)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESIM_SEC && resultCode == AppCompatActivity.RESULT_OK && data!!.data != null){
            if (data.data != null){
                profile_image.setImageURI(data.data)
                newPhotoUri = data.data!!
            }
        }
    }


}