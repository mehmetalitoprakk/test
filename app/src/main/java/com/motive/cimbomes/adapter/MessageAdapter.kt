package com.motive.cimbomes.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.size.Scale
import com.google.firebase.auth.FirebaseAuth
import com.motive.cimbomes.R
import com.motive.cimbomes.activity.FullImageActivity
import com.motive.cimbomes.activity.VideoViewActivity
import com.motive.cimbomes.model.Mesaj
import kotlinx.android.synthetic.main.chat_child_sender.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext


class MessageAdapter(var mesahlar: ArrayList<Mesaj>, var ctx: Context, var listener: OnItemClickListener) : RecyclerView.Adapter<MessageAdapter.MesajViewHolder>() {
    inner class MesajViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),View.OnClickListener {
        var tumLayout = itemView as ConstraintLayout
        var mesaj = tumLayout.tvMessageSender
        var img =tumLayout.imgSenderView
        var imgConainer = tumLayout.imgSenderContainer
        var progress = tumLayout.yukleniyorSenderProgress
        var videoContainer = tumLayout.videoContainer
        var imgViewThumbail = tumLayout.imgViewVideo
        var playButtonVideo = tumLayout.playButtonVideo


        init {
            itemView.setOnClickListener(this)
        }

        fun setData(oankiMesaj: Mesaj){

            if (oankiMesaj.type == "text"){
                mesaj.visibility = View.VISIBLE
                mesaj.text = oankiMesaj.mesaj
                imgConainer.visibility = View.GONE
                videoContainer.visibility = View.GONE
                imgViewThumbail.visibility = View.GONE
                playButtonVideo.visibility = View.GONE
            }else if(oankiMesaj.type == "image"){
                imgConainer.visibility = View.VISIBLE
                mesaj.visibility = View.GONE
                img.visibility = View.VISIBLE
                videoContainer.visibility = View.GONE
                imgViewThumbail.visibility = View.GONE
                playButtonVideo.visibility = View.GONE

                img.load(oankiMesaj.mesajResim){
                    crossfade(true)
                    crossfade(400)
                    placeholder(R.drawable.placeholder)
                    scale(Scale.FIT)
                }

                img.setOnClickListener {
                    val intent = Intent(ctx, FullImageActivity::class.java)
                    intent.putExtra("fullImage", oankiMesaj.mesajResim.toString())
                    ctx.startActivity(intent)

                }

            }else if(oankiMesaj.type == "video"){
                videoContainer.visibility = View.VISIBLE
                mesaj.visibility = View.GONE
                imgConainer.visibility = View.GONE
                img.visibility = View.GONE
                imgViewThumbail.visibility = View.VISIBLE
                playButtonVideo.visibility = View.VISIBLE
                var thumbnail :Bitmap? = null
                CoroutineScope(Dispatchers.Default).launch {
                    thumbnail = retriveVideoFrameFromVideo(oankiMesaj.video)
                    imgViewThumbail.load(thumbnail){
                        memoryCachePolicy(CachePolicy.ENABLED)
                        memoryCacheKey(MemoryCache.Key("my_key"))
                        crossfade(true)
                        crossfade(400)
                        placeholder(R.drawable.placeholder)
                        scale(Scale.FIT)
                    }
                }


                playButtonVideo.setOnClickListener {
                    val intent = Intent(ctx,VideoViewActivity::class.java)
                    intent.putExtra("videoURL",oankiMesaj.video)
                    ctx.startActivity(intent)
                }

            }




        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION){
                listener.onItemClick(position)
            }
        }

    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MesajViewHolder {

        var myView: View? = null

        if (viewType == 2){
            myView = LayoutInflater.from(ctx).inflate(R.layout.chat_child_getter, parent, false)
        }else if(viewType == 1){
            myView = LayoutInflater.from(ctx).inflate(R.layout.chat_child_sender, parent, false)
        }

        return MesajViewHolder(myView!!)

    }

    override fun onBindViewHolder(holder: MesajViewHolder, position: Int) {
        holder.setData(mesahlar.get(position))
        println(mesahlar.get(position).toString())
    }

    override fun getItemViewType(position: Int): Int {
        if (mesahlar.get(position).user_id.equals(FirebaseAuth.getInstance().currentUser!!.uid)){
            return 1
        }else{
            return 2
        }
    }

    override fun getItemCount(): Int {
        return mesahlar.size
    }


    @Throws(Throwable::class)
    suspend fun retriveVideoFrameFromVideo(videoPath: String?): Bitmap? {
        var bitmap: Bitmap? = null
        var mediaMetadataRetriever: MediaMetadataRetriever? = null
        try {
            mediaMetadataRetriever = MediaMetadataRetriever()
            if (Build.VERSION.SDK_INT >= 14) mediaMetadataRetriever.setDataSource(videoPath, HashMap()) else mediaMetadataRetriever.setDataSource(videoPath)
            //   mediaMetadataRetriever.setDataSource(videoPath);
            bitmap = mediaMetadataRetriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST)
        } catch (e: Exception) {
            e.printStackTrace()
            throw Throwable("Exception in retriveVideoFrameFromVideo(String videoPath)" + e.message)
        } finally {
            mediaMetadataRetriever?.release()
        }
        return bitmap
    }

}