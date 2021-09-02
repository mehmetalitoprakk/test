package com.motive.cimbomes.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import coil.load
import coil.size.Scale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.motive.cimbomes.R
import kotlinx.android.synthetic.main.activity_full_image.*

class FullImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_image)

        onlineControl()

        val fulImage = intent.getStringExtra("fullImage")

        fullImageView.load(fulImage){
            crossfade(true)
            crossfade(400)
            placeholder(R.drawable.placeholder)
            scale(Scale.FIT)
        }
    }

    private fun onlineControl() {
        FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("online").setValue(true)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onStop() {
        super.onStop()
        FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("online").setValue(false)
    }


    override fun onStart() {
        super.onStart()
        FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("online").setValue(true)
    }
}