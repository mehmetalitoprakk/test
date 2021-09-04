package com.motive.cimbomes.activity

import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.TouchDelegate
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.motive.cimbomes.R
import com.motive.cimbomes.fragments.ProgressFragment
import com.motive.cimbomes.model.GroupKonusma
import com.motive.cimbomes.model.GroupMembers
import com.motive.cimbomes.model.Groups
import com.motive.cimbomes.model.Users
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_user_info.*

class UserInfoActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db : DatabaseReference
    private lateinit var storageReference: StorageReference
    var dbPhone : String?  = null
    var isim = ""
    var soyisim = ""
    var profilePic = ""
    var uid = ""
    val RESIM_SEC = 100
    var profilePhotoUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference

        intent.getStringExtra("dbphone")?.let {
            dbPhone = intent.getStringExtra("dbphone")
        }



        setTouchDelegate(imgBack,100)

        imgBack.setOnClickListener {
            startActivity(Intent(this,ChechPhoneCodeActivity::class.java))
            overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right)
            finish()
        }



        buttonTamamlaUserInfo.setOnClickListener {
            val isimKontrol = etUserInfoName.text.toString()
            val soyisimKontrol = etUserInfoSurname.text.toString()
            if (isimKontrol.length < 3){
                etUserInfoName.setError("Adınız en az üç karakterden oluşmalıdır")
            }else if (soyisimKontrol.length < 3){
                etUserInfoSurname.setError("Soyadınız en az üç karakterden oluşmalıdır")
            }else{
                if (profilePhotoUri != null){
                    registertoFirebase()
                }else{
                    Toast.makeText(this,"Lütfen profil fotoğrafı ekleyin.",Toast.LENGTH_SHORT).show()
                }

            }

        }

        addPhotoUserInfo.setOnClickListener {
            resimSec()
        }

        TVFotoYükle.setOnClickListener {
            resimSec()
        }

        kvkk.setOnClickListener {
            startActivity(Intent(this,KvkkActivity::class.java))
        }


    }

    private fun resimSec() {
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_PICK)
        startActivityForResult(intent,RESIM_SEC)
    }

    private fun registertoFirebase() {
        val progressDialog = ProgressFragment()
        progressDialog.show(this.supportFragmentManager,"loading")
        progressDialog.isCancelable = false
        isim = etUserInfoName.text.toString()
        soyisim = etUserInfoSurname.text.toString()

        var user = Users(isim,soyisim,dbPhone,"",mAuth.currentUser!!.uid,"","",false,false)
        db.child("users").child(mAuth.currentUser!!.uid).setValue(user).addOnCompleteListener(object : OnCompleteListener<Void>{
            override fun onComplete(p0: Task<Void>) {
                if (p0.isSuccessful){
                    if (profilePhotoUri != null){
                        storageReference.child("users").child(mAuth.currentUser!!.uid).child("profile_picture").putFile(profilePhotoUri!!)
                            .addOnSuccessListener {
                                storageReference.child("users").child(mAuth.currentUser!!.uid).child("profile_picture").downloadUrl
                                    .addOnSuccessListener {
                                        profilePic = it.toString()
                                        db.child("users").child(mAuth.currentUser!!.uid).child("profilePic").setValue(it.toString()).addOnSuccessListener {
                                            db.child("groups").addListenerForSingleValueEvent(object : ValueEventListener{
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    if (snapshot.getValue() != null){
                                                        for (i in snapshot.children){
                                                            if (i.getValue() != null){
                                                                val group = i.getValue(Groups::class.java)
                                                                if (group!!.static == true){
                                                                    val member = GroupMembers(false,mAuth.currentUser!!.uid,user.isim,user.soyisim,user.telefonNo,false,user.profilePic)
                                                                    val list = mutableListOf<GroupMembers>()
                                                                    i.ref.child("member").addListenerForSingleValueEvent(object : ValueEventListener{
                                                                        override fun onDataChange(
                                                                            snapshot: DataSnapshot
                                                                        ) {
                                                                            if (snapshot.getValue() != null){
                                                                                for (j in snapshot.children){
                                                                                    list.add(j.getValue(GroupMembers::class.java)!!)
                                                                                }
                                                                            }
                                                                            list.add(member)

                                                                            i.ref.child("member").setValue(list).addOnSuccessListener {
                                                                                val konusma = GroupKonusma(false,"Eklendiniz",System.currentTimeMillis(),user.uid,group.image,group.groupID,group.groupName,false)
                                                                                db.child("grupkonusmalar").child(user.uid!!).child(group.groupID!!).setValue(konusma)
                                                                            }

                                                                        }

                                                                        override fun onCancelled(
                                                                            error: DatabaseError
                                                                        ) {

                                                                        }

                                                                    })
                                                                }
                                                            }
                                                        }
                                                        progressDialog.dismiss()
                                                        val intent = Intent(this@UserInfoActivity,FeedActivity::class.java)
                                                        startActivity(intent)
                                                        overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left)
                                                        finish()
                                                    }
                                                    progressDialog.dismiss()
                                                    val intent = Intent(this@UserInfoActivity,FeedActivity::class.java)
                                                    startActivity(intent)
                                                    overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left)
                                                    finish()
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                }

                                            })
                                        }.addOnFailureListener {
                                            Toast.makeText(this@UserInfoActivity,"Bir hata oluştu, lütfen daha sonra tekrar deneyin.",Toast.LENGTH_SHORT).show()
                                            progressDialog.dismiss()
                                        }
                                    }.addOnFailureListener {
                                        Toast.makeText(this@UserInfoActivity,"Bir hata oluştu, lütfen daha sonra tekrar deneyin.",Toast.LENGTH_SHORT).show()
                                        progressDialog.dismiss()
                                    }
                            }.addOnFailureListener {
                                progressDialog.dismiss()
                                Toast.makeText(this@UserInfoActivity,"Bir hata oluştu, lütfen daha sonra tekrar deneyin.",Toast.LENGTH_SHORT).show()

                            }
                    }else{
                        progressDialog.dismiss()
                        Toast.makeText(this@UserInfoActivity,"Lütfen profil fotoğrafı yükleyin.",Toast.LENGTH_SHORT).show()
                    }
                }else{
                    progressDialog.dismiss()
                    mAuth.currentUser!!.delete().addOnSuccessListener {
                        registerProgress.visibility = View.GONE
                        reigsterileriButton.isEnabled = true
                        Toast.makeText(this@UserInfoActivity,"Bir hata oluştu, lütfen daha sonra tekrar deneyin.",Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        })


    }




    private fun setTouchDelegate(view: View, dimen: Int) {
        val parent = view.parent as View
        parent.post {
            val delegateArea = Rect()
            view.getHitRect(delegateArea)
            delegateArea.right += dimen
            delegateArea.left -= dimen
            delegateArea.bottom += dimen
            delegateArea.top -= dimen
            parent.touchDelegate = TouchDelegate(delegateArea, view)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESIM_SEC && resultCode == RESULT_OK && data!!.data != null){
            profilePhotoUri = data.data
            profile_image.setImageURI(profilePhotoUri)
            addPhotoUserInfo.visibility = View.INVISIBLE
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mAuth.currentUser!!.delete()
        Toast.makeText(this,"Profil bilgilerinizi tamamlamadınız,lütfen tekrar SMS onayı yaparak kayıt olun.",Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("On Destroy","On destroy çalıştı")
    }
}