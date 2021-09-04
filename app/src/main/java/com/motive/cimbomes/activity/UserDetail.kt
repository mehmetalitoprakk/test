package com.motive.cimbomes.activity

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import coil.load
import coil.size.Scale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.motive.cimbomes.R
import com.motive.cimbomes.model.BlockedUsers
import kotlinx.android.synthetic.main.activity_user_detail.*
import java.util.*

class UserDetail : AppCompatActivity() {
    private var nameInfo : String? = null
    private var surnameInfo : String? = null
    private var uidInfo : String? = null
    private var phoneInfo : String? = null
    private var imageInfo : String? = null
    private var numberList = arrayListOf<String>()
    private var formattedList = arrayListOf<String>()
    private var from : String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)

        nameInfo = intent.getStringExtra("nameInfo")
        surnameInfo = intent.getStringExtra("surnameInfo")
        uidInfo = intent.getStringExtra("uidInfo")
        phoneInfo = intent.getStringExtra("phoneInfo")
        imageInfo = intent.getStringExtra("imageInfo")
        from = intent.getStringExtra("from")

        onlineControl()


        if (from == "chat"){
            mesajGonderUserInfoContainer.visibility = View.GONE
            userInfoSendMessage.visibility = View.GONE
        }else{
            mesajGonderUserInfoContainer.visibility = View.VISIBLE
            userInfoSendMessage.visibility = View.VISIBLE
        }

        val contacts = this.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null)
        while (contacts!!.moveToNext()) {
            val name = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val number = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            numberList.add(number)
        }

        var gelenNumber = phoneInfo!!.replace("\\s".toRegex(),"").replace("+9","")
        for (i in numberList){
            var number = i.replace("\\s".toRegex(),"").replace("+9","")
            formattedList.add(number)
        }
        if (gelenNumber in formattedList){
            println("Numara var")
            userInfoPhone.text = phoneInfo
        }else{
            println("Numara yok")
            userInfoPhone.text = "Numara bilgisi gizli"
        }

        userınfoName.text = nameInfo + " " + surnameInfo

        blockedKontrol()

        FirebaseDatabase.getInstance().reference.child("users").child(uidInfo!!).child("profilePic").get().addOnSuccessListener {
            if (it.value != null){
                imageInfo = it.value.toString()
                usetDetailImage.load(it.value.toString()){
                    crossfade(true)
                    crossfade(400)
                    placeholder(R.drawable.placeholder)
                    scale(Scale.FIT)

                    usetDetailImage.setOnClickListener {
                        val intent = Intent(this@UserDetail,FullImageActivity::class.java)
                        intent.putExtra("fullImage",imageInfo)
                        startActivity(intent)
                    }
                }
            }

        }



        userInfoSendMessage.setOnClickListener {
            val intent = Intent(this,ChatActivity::class.java)
            intent.putExtra("tiklananid",uidInfo)
            intent.putExtra("tiklananisim",nameInfo + " " + surnameInfo)
            intent.putExtra("tiklananfoto", imageInfo)
            startActivity(intent)
            finish()
        }


        profileInfoBack.setOnClickListener {
            super.onBackPressed()
        }

        userInfoCancel.setOnClickListener {
            finish()
        }

        userInfoSikayetEt.setOnClickListener {
            println("şikayet et tıklandı")
            sikayetEt()
        }

        userDetailBlockUser.setOnClickListener {
            blockUser()
        }
        userDetailUnBlockUser.setOnClickListener {
            unBlockUser()
        }


    }

    private fun blockedKontrol() {
        FirebaseDatabase.getInstance().reference.child("blockedUsers").child(FirebaseAuth.getInstance().currentUser!!.uid).child(uidInfo!!).child("blocked").get().addOnSuccessListener {
            if (it.value != null && it != null){
                if (it.value == true){
                    Log.e("BLOCKED", "TRUE GİRDİ")
                    userDetailBlockUser.visibility = View.GONE
                    userDetailUnBlockUser.visibility = View.VISIBLE
                }else if (it.value == false){
                    Log.e("BLOCKED", "FALSE GİRDİ")
                    userDetailBlockUser.visibility = View.VISIBLE
                    userDetailUnBlockUser.visibility = View.GONE
                }
            }else{
                Log.e("BLOCKED", "ELSE GİRDİ")
                userDetailBlockUser.visibility = View.VISIBLE
                userDetailUnBlockUser.visibility = View.GONE
            }
        }
    }

    private fun unBlockUser() {
        var blocked = BlockedUsers(false)
        FirebaseDatabase.getInstance().reference.child("blockedUsers").child(FirebaseAuth.getInstance().currentUser!!.uid).child(uidInfo!!).setValue(blocked).addOnSuccessListener {
            Toast.makeText(this,"Kişinin engelini kaldırdınız.",Toast.LENGTH_SHORT).show()
            userDetailBlockUser.visibility = View.VISIBLE
            userDetailUnBlockUser.visibility = View.GONE
        }
    }

    private fun blockUser() {
        var blocked = BlockedUsers(true)
        FirebaseDatabase.getInstance().reference.child("blockedUsers").child(FirebaseAuth.getInstance().currentUser!!.uid).child(uidInfo!!).setValue(blocked).addOnSuccessListener {
            Toast.makeText(this,"Kişiyi Engellediniz.",Toast.LENGTH_SHORT).show()
            userDetailBlockUser.visibility = View.GONE
            userDetailUnBlockUser.visibility = View.VISIBLE
        }
    }

    private fun onlineControl() {
        FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("online").setValue(true)
    }

    override fun onStop() {
        super.onStop()
        FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("online").setValue(false)
    }


    override fun onStart() {
        super.onStart()
        FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("online").setValue(true)
    }

    private fun sikayetEt() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayput = inflater.inflate(R.layout.sikayet_et_layout,null)
        val edittext = dialogLayput.findViewById<EditText>(R.id.sikayetSebebi)
        val progressBar = dialogLayput.findViewById<ProgressBar>(R.id.progressSikayet)
        with(builder){
            setTitle("Kullanıcıyı Şikayet Et")
            setPositiveButton("Tamam"){dialog,which ->
                if (edittext.text.toString().isNotEmpty() || edittext.text.toString().length > 3){
                    progressBar.visibility = View.VISIBLE
                    var sikayet = hashMapOf<String,Any>()
                    sikayet.put("sikayetEdenİd", FirebaseAuth.getInstance().currentUser!!.uid.toString())
                    sikayet.put("sikayetEdilenİd",uidInfo.toString())
                    sikayet.put("sikayetSebebi",edittext.text.toString())
                    sikayet.put("time",Calendar.getInstance().time.toString())
                    FirebaseDatabase.getInstance().reference.child("sikayetler").child(FirebaseAuth.getInstance().currentUser!!.uid.toString()).child(uidInfo.toString())
                        .push().setValue(sikayet).addOnSuccessListener {
                            progressBar.visibility = View.GONE
                            Toast.makeText(this@UserDetail,"Şikayetiniz başarıyla gönderildi.",Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }.addOnFailureListener {
                            progressBar.visibility = View.GONE
                            Toast.makeText(this@UserDetail,"Bir hata oluştu, lütfen daha sonra tekrar deneyin.",Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                }
            }
            setNegativeButton("İptal"){ dialog, which ->
                dialog.dismiss()
            }
            setView(dialogLayput)
            show()
        }

    }
}