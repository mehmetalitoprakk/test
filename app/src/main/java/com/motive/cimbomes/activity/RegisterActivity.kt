package com.motive.cimbomes.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.motive.cimbomes.R
import com.motive.cimbomes.model.Users
import com.motive.cimbomes.utils.EventBusDataEvents
import kotlinx.android.synthetic.main.activity_register.*
import org.greenrobot.eventbus.EventBus

class RegisterActivity : AppCompatActivity() {
    var countrcode = ""
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mFirestore: FirebaseFirestore
    var phoneNumber = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        mAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        mFirestore = FirebaseFirestore.getInstance()

        reigsterileriButton.isEnabled = false
        reigsterileriButton.backgroundTintList = ContextCompat.getColorStateList(this,R.color.inaktifButton)



        registerphoneET.addTextChangedListener(watcher)


        reigsterileriButton.setOnClickListener {
            registerProgress.visibility = View.VISIBLE
            reigsterileriButton.isEnabled = false
            val telNo = registerphoneET.text.toString()
            countrcode = cpp.selectedCountryCode.toString()
            val fullTelNo = "+"+countrcode+telNo
            phoneNumber = fullTelNo
            if (telNo == "0000000000"){
                val intent = Intent(this@RegisterActivity,AdminPasswordActivity::class.java)
                intent.putExtra("adminid",telNo)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left)

            }else{
                if (isValidPhone(fullTelNo)){
                    if (registerphoneET.text.toString().length == 10){
                        val intent = Intent(this@RegisterActivity,ChechPhoneCodeActivity::class.java)
                        intent.putExtra("phone",phoneNumber)
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
                        finish()


                    }else{
                        tvgecersizphone.visibility = View.VISIBLE
                        registerProgress.visibility = View.GONE
                        reigsterileriButton.isEnabled = true
                    }
                }else{
                    registerProgress.visibility = View.GONE
                    reigsterileriButton.isEnabled = true
                    tvgecersizphone.visibility = View.VISIBLE
                }
            }
        }



    }

    val watcher = object : TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (registerphoneET.text.toString().length == 10){
                reigsterileriButton.isEnabled = true
                reigsterileriButton.backgroundTintList = ContextCompat.getColorStateList(this@RegisterActivity,R.color.teal_700)

            }else{
                reigsterileriButton.isEnabled = false
                reigsterileriButton.backgroundTintList = ContextCompat.getColorStateList(this@RegisterActivity,R.color.inaktifButton)
            }
        }

        override fun afterTextChanged(s: Editable?) {

        }

    }

    private fun isValidPhone(phoneNumber: String) : Boolean{
        if (phoneNumber == null || phoneNumber.length > 13){
            return false
        }
        return android.util.Patterns.PHONE.matcher(phoneNumber).matches()
    }
}