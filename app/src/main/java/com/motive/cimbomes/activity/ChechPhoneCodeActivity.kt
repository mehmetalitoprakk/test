package com.motive.cimbomes.activity

import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.TouchDelegate
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.motive.cimbomes.R
import com.motive.cimbomes.model.Users
import kotlinx.android.synthetic.main.activity_chech_phone_code.*
import java.util.concurrent.TimeUnit

class ChechPhoneCodeActivity : AppCompatActivity() {
    var gelenTelNo = ""
    var gelenKod = ""
    private lateinit var mAuth: FirebaseAuth
    private var forceResendingToken : PhoneAuthProvider.ForceResendingToken? = null
    private var mCallBacks : PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null
    private var mVericitaionId : String? = null
    private val TAG = "PHONE_AUTH"






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chech_phone_code)
        buttonPhoneCodeOnayla.isEnabled = false

        imgBackPhoneCode.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
            overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()
        gelenTelNo = intent.getStringExtra("phone")!!
        println(gelenTelNo)


        tvTelefonNo.text = gelenTelNo
        setTouchDelegate(imgBackPhoneCode,100)
        pinview.addTextChangedListener(watcher)

        buttonPhoneCodeOnayla.setOnClickListener {
            phoneauthProgressBar.visibility = View.VISIBLE
            buttonPhoneCodeOnayla.isEnabled = false
            val kullaniciKod = pinview.text.toString()
            verifyPhoneNumberWithCode(mVericitaionId!!,kullaniciKod)
        }

        mCallBacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(phoneAuthCredential : PhoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential)

            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG,e.message.toString())
                phoneauthProgressBar.visibility = View.GONE
                buttonPhoneCodeOnayla.isEnabled = true
                Toast.makeText(this@ChechPhoneCodeActivity,"Bir hata oluştu, ${e.message}",Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(verificationID: String, token: PhoneAuthProvider.ForceResendingToken) {
                mVericitaionId = verificationID
                forceResendingToken = token

            }
        }

        startPhoneNumberVerification(gelenTelNo)


        resendCode.setOnClickListener {
            resendVerificationCode(gelenTelNo,forceResendingToken!!)
            Toast.makeText(this,"Onay Kodu Tekrar Gönderildi",Toast.LENGTH_SHORT).show()
        }

    }
    private fun startPhoneNumberVerification(phone : String){

        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L,TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallBacks!!)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendVerificationCode(phone: String,token: PhoneAuthProvider.ForceResendingToken){

        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L,TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallBacks!!)
            .setForceResendingToken(token)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyPhoneNumberWithCode(verificationId : String,code:String){
        val credential = PhoneAuthProvider.getCredential(verificationId,code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                FirebaseDatabase.getInstance().reference.child("users").addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val telNumbers = arrayListOf<String>()
                        for (i in snapshot.children){
                            val user = i.getValue(Users::class.java)
                            if (user != null){
                                if (user.telefonNo != null){
                                    telNumbers.add(user!!.telefonNo!!)
                                }
                            }

                        }
                        if (telNumbers.contains(gelenTelNo)){
                            getFcmTokenForExistsUser()
                        }else if(gelenTelNo !in telNumbers){
                            getFcmTokenForNotExistsUser()

                        }

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })


            }
            .addOnFailureListener {
                //Login Failure
                Log.e(TAG,it.message.toString())
                Toast.makeText(this,"Bir hata oluştu, ${it.message}",Toast.LENGTH_SHORT).show()
                startActivity(Intent(this,RegisterActivity::class.java))
                overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right)
                finish()

            }
    }

    private fun getFcmTokenForNotExistsUser() {
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            var token = it.token
            addToDatabaseNewTokenForNotExist(token)
        }
    }

    private fun getFcmTokenForExistsUser() {
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            var token = it.token
            addToDatabaseNewToken(token)
        }
    }

    private fun addToDatabaseNewTokenForNotExist(token: String) {
        if (FirebaseAuth.getInstance().currentUser != null){
            FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("fcmToken")
                .setValue(token).addOnSuccessListener {
                    val intent = Intent(this@ChechPhoneCodeActivity,UserInfoActivity::class.java)
                    intent.putExtra("dbphone",gelenTelNo)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left)
                    finish()
                }
        }
    }

    private fun addToDatabaseNewToken(token: String) {
        if (FirebaseAuth.getInstance().currentUser != null){
            FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("fcmToken")
                .setValue(token).addOnSuccessListener {
                    val intent = Intent(this@ChechPhoneCodeActivity,FeedActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left)
                    finish()
                }
        }
    }


    val watcher = object : TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {


        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (pinview.text.toString().length == 6){
                buttonPhoneCodeOnayla.isEnabled = true
                buttonPhoneCodeOnayla.backgroundTintList = ContextCompat.getColorStateList(this@ChechPhoneCodeActivity,R.color.teal_700)
            }else{
                buttonPhoneCodeOnayla.isEnabled = false
                buttonPhoneCodeOnayla.backgroundTintList = ContextCompat.getColorStateList(this@ChechPhoneCodeActivity,R.color.inaktifButton)
            }
        }

        override fun afterTextChanged(s: Editable?) {

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




}