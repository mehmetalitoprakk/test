package com.motive.cimbomes.activity

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.motive.cimbomes.R
import com.universalvideoview.UniversalVideoView
import kotlinx.android.synthetic.main.activity_video_view.*

class VideoViewActivity : AppCompatActivity() {
    var url = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_view)

        url = intent.getStringExtra("videoURL")!!
        //onlineControl()

        videoView.setVideoPath(url)
        videoView.setMediaController(media_controller)
        videoView.start()


        videoView.setVideoViewCallback(object : UniversalVideoView.VideoViewCallback{
            override fun onScaleChange(isFullscreen: Boolean) {
                if (isFullscreen){
                    var params = video_layout.layoutParams
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT
                    params.height = ViewGroup.LayoutParams.MATCH_PARENT
                    video_layout.layoutParams = params
                }else{
                    var paramss = videoView.layoutParams
                    paramss.width = ViewGroup.LayoutParams.MATCH_PARENT
                    paramss.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    video_layout.layoutParams = paramss

                }
            }

            override fun onPause(mediaPlayer: MediaPlayer?) {
            }

            override fun onStart(mediaPlayer: MediaPlayer?) {
            }

            override fun onBufferingStart(mediaPlayer: MediaPlayer?) {

            }

            override fun onBufferingEnd(mediaPlayer: MediaPlayer?) {

            }

        })

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
}