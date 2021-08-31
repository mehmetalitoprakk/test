package com.motive.cimbomes.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.motive.cimbomes.R

class MainActivity : AppCompatActivity() {
    private val SPLASH_TIME : Long = 3000
    private lateinit var mAuth : FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()

        Handler().postDelayed({
            if (mAuth.currentUser != null){
                startActivity(Intent(this, KvkkActivity::class.java))
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
                finish()
            }else{
                startActivity(Intent(this, RegisterActivity::class.java))
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
                finish()
            }

        }, SPLASH_TIME)
    }
}