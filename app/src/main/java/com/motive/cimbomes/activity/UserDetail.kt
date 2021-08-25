package com.motive.cimbomes.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
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
            userInfoPhone.text = " "
        }

        userınfoName.text = nameInfo + " " + surnameInfo



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

    }
}