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
import com.motive.cimbomes.R
import kotlinx.android.synthetic.main.activity_chech_phone_code.*
import java.util.concurrent.TimeUnit

class ChechPhoneCodeActivity : AppCompatActivity() {
    var gelenTelNo = ""
    lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    var verificationID = ""
    var gelenKod = ""
    lateinit var mAuth: FirebaseAuth
    lateinit var resendToken : PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chech_phone_code)
        buttonPhoneCodeOnayla.isEnabled = false


        mAuth = FirebaseAuth.getInstance()
        gelenTelNo = intent.getStringExtra("phone")!!


        tvTelefonNo.text = gelenTelNo
        setTouchDelegate(imgBackPhoneCode,100)



        imgBackPhoneCode.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
            overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right)
            finish()
        }


        pinview.addTextChangedListener(watcher)



        setupCallback()



        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(gelenTelNo)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS)  // Timeout and unit
            .setActivity(this)                // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()


        PhoneAuthProvider.verifyPhoneNumber(options)



        buttonPhoneCodeOnayla.setOnClickListener {
            val kullaniciKod = pinview.text.toString()
            if (kullaniciKod == gelenKod){
                val intent = Intent(this,UserInfoActivity::class.java)
                intent.putExtra("dbphone",gelenTelNo)
                startActivity(intent)
                finish()
            }else{
                val intent = Intent(this,UserInfoActivity::class.java)
                intent.putExtra("dbphone",gelenTelNo)
                startActivity(intent)
                finish()
                Toast.makeText(this,"Onay kodu hatalı!",Toast.LENGTH_SHORT).show()
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



    private fun setupCallback() {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                if (credential.smsCode.isNullOrEmpty()){
                    gelenKod = credential.smsCode!!
                    println("ON VERİFİ ÇALIŞTI")
                }
                gelenKod = credential.smsCode!!


            }

            override fun onVerificationFailed(e: FirebaseException) {

                Log.e("TAG", "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(this@ChechPhoneCodeActivity,"Telefon numarası istenen formata uygun değil. Lütfen geçerli bir telefon numarası giriniz.",
                        Toast.LENGTH_SHORT).show()
                } else if (e is FirebaseTooManyRequestsException) {
                    Toast.makeText(this@ChechPhoneCodeActivity,"Çok fazla kod gönderildi, lütfen bir süre bekleyin ve tekrar deneyin.",
                        Toast.LENGTH_SHORT).show()
                } else if (e is FirebaseNetworkException){
                    Toast.makeText(this@ChechPhoneCodeActivity,"Lütfen internet bağlantınızı kontrol edin.",
                        Toast.LENGTH_SHORT).show()
                }


            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                println("ON CODE SEND ÇALIŞTI")
                verificationID = verificationId
                resendToken = token
            }
        }
    }
}