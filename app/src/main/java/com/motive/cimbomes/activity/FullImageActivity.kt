package com.motive.cimbomes.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import coil.load
import coil.size.Scale
import com.motive.cimbomes.R
import kotlinx.android.synthetic.main.activity_full_image.*

class FullImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_image)

        val fulImage = intent.getStringExtra("fullImage")

        fullImageView.load(fulImage){
            crossfade(true)
            crossfade(400)
            placeholder(R.drawable.image_placeholder)
            scale(Scale.FIT)
        }
    }

    override fun onBackPressed() {
        finish()
    }
}