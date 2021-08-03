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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.motive.cimbomes.R
import com.motive.cimbomes.fragments.ProgressFragment
import com.motive.cimbomes.model.Users
import kotlinx.android.synthetic.main.activity_user_info.*

class UserInfoActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db : DatabaseReference
    private lateinit var storageReference: StorageReference
    var dbPhone = ""
    var verifiID = ""
    var code = ""
    var isim = ""
    var soyisim = ""
    var profilePic = ""
    var uid = ""
    var credential : PhoneAuthCredential? = null
    val RESIM_SEC = 100
    var profilePhotoUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference

        dbPhone = intent.getStringExtra("dbphone")!!

        Log.e("TEL",dbPhone)


        setTouchDelegate(imgBack,100)

        imgBack.setOnClickListener {
            startActivity(Intent(this,ChechPhoneCodeActivity::class.java))
            overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right)
            finish()
        }



        buttonTamamlaUserInfo.setOnClickListener {
            registertoFirebase()
        }

        addPhotoUserInfo.setOnClickListener {
            resimSec()
        }

        TVFotoYükle.setOnClickListener {
            resimSec()
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
        mAuth.createUserWithEmailAndPassword(dbPhone+"@cimbomes.com","shakeydegisicek").addOnSuccessListener {
            var user = Users(isim,soyisim,dbPhone,"",mAuth.currentUser!!.uid,"","",false)
            db.child("users").child(mAuth.currentUser!!.uid).setValue(user).addOnCompleteListener(object : OnCompleteListener<Void>{
                override fun onComplete(p0: Task<Void>) {
                    if (p0.isSuccessful){
                        if (profilePhotoUri != null){
                            storageReference.child("users").child(mAuth.currentUser!!.uid).child("profile_picture").putFile(profilePhotoUri!!)
                                    .addOnSuccessListener {
                                        storageReference.child("users").child(mAuth.currentUser!!.uid).child("profile_picture").downloadUrl
                                                .addOnSuccessListener {
                                                    db.child("users").child(mAuth.currentUser!!.uid).child("profilePic").setValue(it.toString()).addOnSuccessListener {
                                                        progressDialog.dismiss()
                                                        val intent = Intent(this@UserInfoActivity,FeedActivity::class.java)
                                                        startActivity(intent)
                                                        overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left)
                                                        finish()
                                                    }.addOnFailureListener {
                                                        Toast.makeText(this@UserInfoActivity,"Bir hata oluştu, lütfen daha sonra tekrar deneyin.",Toast.LENGTH_SHORT).show()
                                                        progressDialog.dismiss()
                                                    }
                                                }.addOnFailureListener {
                                                    Toast.makeText(this@UserInfoActivity,"Bir hata oluştu, lütfen daha sonra tekrar deneyin.",Toast.LENGTH_SHORT).show()
                                                    progressDialog.dismiss()
                                                }
                                    }.addOnFailureListener {
                                        Toast.makeText(this@UserInfoActivity,"Bir hata oluştu, lütfen daha sonra tekrar deneyin.",Toast.LENGTH_SHORT).show()
                                        progressDialog.dismiss()
                                    }
                        }
                    }else{
                        progressDialog.dismiss()
                        mAuth.currentUser!!.delete().addOnSuccessListener {
                            Toast.makeText(this@UserInfoActivity,"Bir hata oluştu, lütfen daha sonra tekrar deneyin.",Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            })
        }


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
}