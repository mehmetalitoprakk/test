package com.motive.cimbomes.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import coil.load
import coil.size.Scale
import com.motive.cimbomes.R
import kotlinx.android.synthetic.main.activity_user_detail.*

class UserDetail : AppCompatActivity() {
    private var nameInfo : String? = null
    private var surnameInfo : String? = null
    private var uidInfo : String? = null
    private var phoneInfo : String? = null
    private var imageInfo : String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)

        nameInfo = intent.getStringExtra("nameInfo")
        surnameInfo = intent.getStringExtra("surnameInfo")
        uidInfo = intent.getStringExtra("uidInfo")
        phoneInfo = intent.getStringExtra("phoneInfo")
        imageInfo = intent.getStringExtra("imageInfo")

        userınfoName.text = nameInfo + " " + surnameInfo
        userInfoPhone.text = phoneInfo


        imageInfo?.let {
            usetDetailImage.load(imageInfo){
                crossfade(true)
                crossfade(400)
                placeholder(R.drawable.placeholder)
                scale(Scale.FIT)
            }
            usetDetailImage.setOnClickListener {
                val intent = Intent(this,FullImageActivity::class.java)
                intent.putExtra("fullImage",imageInfo)
                startActivity(intent)
            }
        }


        profileInfoBack.setOnClickListener {
            super.onBackPressed()
        }

    }
}