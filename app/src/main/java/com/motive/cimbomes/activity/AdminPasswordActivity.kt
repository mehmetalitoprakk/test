package com.motive.cimbomes.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.motive.cimbomes.R
import kotlinx.android.synthetic.main.activity_admin_password.*

class AdminPasswordActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_password)

        val id = intent.getStringExtra("adminid")
        adminloginbutton.setOnClickListener {
            if (adminoasswordet.text.toString() == "googleAdminPassword"){
                val intent = Intent(this,FeedActivity::class.java)
                FirebaseAuth.getInstance().signInWithEmailAndPassword("google@combomes.com","googleAdminPassword").addOnSuccessListener {
                    startActivity(intent)
                    this.finish()
                }

            }
        }


    }
}