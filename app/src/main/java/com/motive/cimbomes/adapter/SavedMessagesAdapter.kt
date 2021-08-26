package com.motive.cimbomes.adapter

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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.motive.cimbomes.R
import com.motive.cimbomes.model.Mesaj
import com.motive.cimbomes.model.Users
import com.motive.cimbomes.utils.UniversalImageLoader
import kotlinx.android.synthetic.main.saved_messages_row.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SavedMessagesAdapter(var kaydedilerMesahlar : ArrayList<Mesaj>) : RecyclerView.Adapter<SavedMessagesAdapter.SavedMessagesViewHolder>() {
    class SavedMessagesViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        var tumLayot = view as ConstraintLayout
        var name = tumLayot.savedMessagesName
        var image = tumLayot.savedMessagesImage
        var timeRow = tumLayot.savedMessagesMessageTimeRow
        var timeMessage = tumLayot.savedMessagesMessageTime
        var mesaj = tumLayot.savedMessagesMessage
        var videoContainer = tumLayot.savedMessageVideoContainer
        var videoThumbail = tumLayot.savedMessagesVideoThumbnail
        var imageMessage = tumLayot.savedMessagesImageMessage

        fun setData(savedMessage : Mesaj){
            getUserInfo(savedMessage.user_id.toString())

            if (savedMessage.type == "text"){
                videoContainer.visibility = View.GONE
                imageMessage.visibility = View.GONE
                videoThumbail.visibility = View.GONE
                mesaj.visibility = View.VISIBLE
                mesaj.text = savedMessage.mesaj
                timeMessage.text = convertLongToTime(savedMessage.time!!)
                timeRow.text = convertLongToTime(savedMessage.time!!)
            }else if(savedMessage.type == "image"){
                mesaj.visibility = View.GONE
                videoContainer.visibility = View.GONE
                videoThumbail.visibility = View.GONE
                imageMessage.visibility = View.VISIBLE
                imageMessage.load(savedMessage.mesajResim){
                    memoryCachePolicy(CachePolicy.ENABLED)
                    memoryCacheKey(MemoryCache.Key("my_key_'"))
                    crossfade(true)
                    crossfade(400)
                    placeholder(R.drawable.placeholder)
                    scale(Scale.FIT)
                }
                timeMessage.text = convertLongToTime(savedMessage.time!!)
                timeRow.text = convertLongToTime(savedMessage.time!!)

            }else if (savedMessage.type == "video"){
                mesaj.visibility = View.GONE
                videoContainer.visibility = View.VISIBLE
                videoThumbail.visibility = View.VISIBLE
                imageMessage.visibility = View.GONE
                var thumbnail :Bitmap? = null
                CoroutineScope(Dispatchers.Default).launch {
                    thumbnail = retriveVideoFrameFromVideo(savedMessage.video)
                    videoThumbail.load(thumbnail){
                        memoryCachePolicy(CachePolicy.ENABLED)
                        memoryCacheKey(MemoryCache.Key("my_key_3"))
                        crossfade(true)
                        crossfade(400)
                        placeholder(R.drawable.placeholder)
                        scale(Scale.FIT)
                    }
                }


            }
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

        private fun getUserInfo(userdID : String) {
            FirebaseDatabase.getInstance().reference.child("users").child(userdID).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.getValue() != null){
                        val user = snapshot.getValue(Users::class.java)
                        name.text = user!!.isim + " " + user.soyisim
                        if (user.profilePic != null){
                            UniversalImageLoader.setImage(user!!.profilePic!!,image,null,"")
                        }


                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
        fun convertLongToTime(time: Long): String {
            val date = Date(time)
            val format = SimpleDateFormat("HH:mm")
            return format.format(date)
        }
    }






    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedMessagesViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.saved_messages_row,parent,false)
        return SavedMessagesViewHolder(view)
    }

    override fun onBindViewHolder(holder: SavedMessagesViewHolder, position: Int) {
        holder.setData(kaydedilerMesahlar.get(position))
    }

    override fun getItemCount(): Int {
        return kaydedilerMesahlar.size
    }
}