package com.motive.cimbomes.activity

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.TouchDelegate
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import com.devlomi.record_view.OnRecordListener
import com.dinuscxj.refresh.RecyclerRefreshLayout
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.*
import com.motive.cimbomes.R
import com.motive.cimbomes.adapter.MessageAdapter
import com.motive.cimbomes.fragments.ProgressFragment
import com.motive.cimbomes.model.Mesaj
import com.motive.cimbomes.utils.*
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chat_child_getter.*
import kotlinx.android.synthetic.main.fragment_progress.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatActivity : AppCompatActivity(),MessageAdapter.OnItemClickListener {
    private lateinit var uid : String
    private lateinit var name : String
    private lateinit var image : String
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db : DatabaseReference
    private lateinit var typingRef : DatabaseReference
    private lateinit var storage : StorageReference
    private lateinit var mesajlar : ArrayList<Mesaj>
    private lateinit var myID : String
    private lateinit var MyAdapter : MessageAdapter
    private lateinit var myRecyclerView : RecyclerView
    private lateinit var dinlenecekYaziyorRef : DatabaseReference
    private lateinit var storageReference: StorageReference

    //Sayfalama
    val NUMBER_OF_MESSAGE_PER_PAGE = 15
    var mesajPosition = 0
    var ilkMesajID = ""
    var moremesajPos = 0
    var listedeOlanMesajID= ""
    var ekrandaSonGorulmeVarMi = false
    var yeniMesajKey : String? = ""
    var selectedOptions = "Fotoğraf"
    var videoUri : Uri? = null
    var dosyaTuruResimMi = false

    //fotoğraf gönderme
    val RESIM_SEC = 100
    var resimUri : Uri? = null
    var resimLink = ""
    val VIDEO_SEC = 200
    var video = ""
    var auidoPath = ""
    var mediaRecorder = MediaRecorder()

    var videoFullPath = ""



    private lateinit var childEventListener : ChildEventListener
    private lateinit var childEventListenerMore : ChildEventListener



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        uid = intent.getStringExtra("tiklananid")!!
        name = intent.getStringExtra("tiklananisim")!!
        image = intent.getStringExtra("tiklananfoto")!!

        typingContainer.visibility = View.GONE
        gorulduContainer.visibility = View.GONE

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference

        myID = mAuth.currentUser!!.uid
        mesajlar = ArrayList<Mesaj>()
        typingRef = FirebaseDatabase.getInstance().reference.child("konusmalar").child(myID).child(uid)
        dinlenecekYaziyorRef = FirebaseDatabase.getInstance().reference.child("konusmalar").child(uid).child(myID)


        UniversalImageLoader.setImage(image,cirlceChat,null,"")
        chatIsim.text = name

        edittextChat.addTextChangedListener(watcher)

        setupMesajlarRecyclerView()
        mesajlarıGetir()
        setTouchDelegate(imgBackChat,100)

        cirlceChat.setOnClickListener {
            val intent = Intent(this,FullImageActivity::class.java)
            intent.putExtra("fullImage",image)
            startActivity(intent)
        }

        chatIsim.setOnClickListener {
            db.child("users").child(uid).child("telefonNo").get().addOnSuccessListener {
                val number = it.value.toString()
                val intent = Intent(this,UserDetail::class.java)
                intent.putExtra("nameInfo",name)
                intent.putExtra("surnameInfo","")
                intent.putExtra("uidInfo",uid)
                intent.putExtra("phoneInfo",number)
                intent.putExtra("imageInfo",image)
                intent.putExtra("from","chat")
                startActivity(intent)
            }




        }


        imgBackChat.setOnClickListener {
            db.child("chats").child(myID).child(uid).removeEventListener(childEventListener)
            finish()
        }


        refreshChat.setOnRefreshListener(object : RecyclerRefreshLayout.OnRefreshListener{
            override fun onRefresh() {
                db.child("chats").child(myID).child(uid).addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot!!.childrenCount>mesajlar.size){
                            moremesajPos=0
                            dahaFazlaMesajGetir()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

                refreshChat.setRefreshing(false)
            }

        })



        imgSendChat.setOnClickListener {
            var mesajText = edittextChat.text.toString().trim()

            if (mesajText.trim().length>0){
                yeniMesajKey = db.child("chats").child(myID).child(uid).push().key

                var mesajAtan = HashMap<String,Any>()
                mesajAtan.put("mesaj",mesajText)
                mesajAtan.put("goruldu",true)
                mesajAtan.put("time",ServerValue.TIMESTAMP)
                mesajAtan.put("type","text")
                mesajAtan.put("user_id",myID)
                mesajAtan.put("mesajResim",resimLink)
                mesajAtan.put("video",video)
                mesajAtan.put("audio","")
                mesajAtan.put("mesajKey",yeniMesajKey.toString())


                val tim = ServerValue.TIMESTAMP



                db.child("chats").child(myID).child(uid).child(yeniMesajKey!!).setValue(mesajAtan)



                var mesajAlan = HashMap<String,Any>()
                mesajAlan.put("mesaj",mesajText)
                mesajAlan.put("goruldu",false)
                mesajAlan.put("time",ServerValue.TIMESTAMP)
                mesajAlan.put("type","text")
                mesajAlan.put("user_id",myID)
                mesajAlan.put("mesajResim",resimLink)
                mesajAlan.put("video",video)
                mesajAlan.put("audio","")
                mesajAlan.put("mesajKey",yeniMesajKey.toString())
                db.child("chats").child(uid).child(myID).child(yeniMesajKey!!).setValue(mesajAlan)




                var konusmaMesajAtan = HashMap<String,Any>()
                konusmaMesajAtan.put("time",ServerValue.TIMESTAMP)
                konusmaMesajAtan.put("goruldu",true)
                konusmaMesajAtan.put("son_mesaj",mesajText)
                konusmaMesajAtan.put("typing",false)
                db.child("konusmalar").child(myID).child(uid).setValue(konusmaMesajAtan)


                var konusmaMesajAlan = HashMap<String,Any>()
                konusmaMesajAlan.put("time",ServerValue.TIMESTAMP)
                konusmaMesajAlan.put("goruldu",false)
                konusmaMesajAlan.put("son_mesaj",mesajText)
                //konusmaMesajAlan.put("typing",false)
                db.child("konusmalar").child(uid).child(myID).setValue(konusmaMesajAlan)


                edittextChat.setText("")
            }else{
                Toast.makeText(this,"Boş mesaj gönderemezsiniz!",Toast.LENGTH_SHORT).show()
            }






        }


        /*imgMicChat.setOnClickListener {
            imgMicChat.setRecordView(recordView)
            imgMicChat.isListenForRecord = true
        }*/


        /*recordView.setOnRecordListener(object : OnRecordListener{
            override fun onStart() {
                imgAddChat.visibility = View.GONE
                edittextChat.visibility = View.GONE
                imgSendChat.visibility = View.GONE
                recordView.visibility = View.VISIBLE

                setUpRecording()

                try {
                    mediaRecorder.prepare()
                    mediaRecorder.start()
                }catch (e : Exception){
                    e.printStackTrace()
                }


            }

            override fun onCancel() {
                mediaRecorder.reset()
                mediaRecorder.release()
                var file = File(auidoPath)
                if (file.exists()){
                    file.delete()
                }
                imgAddChat.visibility = View.VISIBLE
                edittextChat.visibility = View.VISIBLE
                imgSendChat.visibility = View.VISIBLE
                recordView.visibility = View.GONE


            }

            override fun onFinish(recordTime: Long, limitReached: Boolean) {
                mediaRecorder.stop()
                mediaRecorder.release()
                imgAddChat.visibility = View.VISIBLE
                edittextChat.visibility = View.VISIBLE
                imgSendChat.visibility = View.VISIBLE
                recordView.visibility = View.GONE

                sendRecordingMessage(auidoPath)
            }


            override fun onLessThanSecond() {
                mediaRecorder.reset()
                mediaRecorder.release()
                var file = File(auidoPath)
                if (file.exists()){
                    file.delete()
                }

                mediaRecorder.reset()
                mediaRecorder.release()
                imgAddChat.visibility = View.VISIBLE
                edittextChat.visibility = View.VISIBLE
                imgSendChat.visibility = View.VISIBLE
                recordView.visibility = View.GONE

            }

        })*/


        imgAddChat.setOnClickListener {
            showDialog()
        }

    }


    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
        return format.format(date)
    }

    /*private fun sendRecordingMessage(audioPath : String){
        var storageRef = FirebaseStorage.getInstance().reference
        val Rndomuid = UUID.randomUUID().toString()
        var path = storageRef.child("chatAudios").child(myID).child(uid).child(Rndomuid).child(System.currentTimeMillis().toString())
        var uri = Uri.fromFile(File(audioPath))
        path.putFile(uri).addOnSuccessListener {success ->
            var task : Task<Uri> = success.storage.downloadUrl
            task.addOnCompleteListener { Pathh ->
                if (Pathh.isSuccessful){
                    var url = Pathh.result.toString()

                    var mesajAtan = HashMap<String,Any>()
                    mesajAtan.put("mesaj","")
                    mesajAtan.put("goruldu",true)
                    mesajAtan.put("time",ServerValue.TIMESTAMP)
                    mesajAtan.put("type","audioMessage")
                    mesajAtan.put("user_id",myID)
                    mesajAtan.put("mesajResim","")
                    mesajAtan.put("video","")
                    mesajAtan.put("audio",url)

                    var fotoMesajKey = db.child("chats").child(myID).child(uid).push().key

                    db.child("chats").child(myID).child(uid).child(fotoMesajKey!!).setValue(mesajAtan)



                    var mesajAlan = HashMap<String,Any>()
                    mesajAlan.put("mesaj","")
                    mesajAlan.put("goruldu",false)
                    mesajAlan.put("time",ServerValue.TIMESTAMP)
                    mesajAlan.put("type","audioMessage")
                    mesajAlan.put("user_id",myID)
                    mesajAlan.put("mesajResim","")
                    mesajAlan.put("video","")
                    mesajAlan.put("audio",url)
                    db.child("chats").child(uid).child(myID).child(fotoMesajKey!!).setValue(mesajAlan)




                    var konusmaMesajAtan = HashMap<String,Any>()
                    konusmaMesajAtan.put("time",ServerValue.TIMESTAMP)
                    konusmaMesajAtan.put("goruldu",true)
                    konusmaMesajAtan.put("son_mesaj","Ses")
                    konusmaMesajAtan.put("typing",false)
                    db.child("konusmalar").child(myID).child(uid).setValue(konusmaMesajAtan)


                    var konusmaMesajAlan = HashMap<String,Any>()
                    konusmaMesajAlan.put("time",ServerValue.TIMESTAMP)
                    konusmaMesajAlan.put("goruldu",false)
                    konusmaMesajAlan.put("son_mesaj","Ses")
                    //konusmaMesajAlan.put("typing",false)
                    db.child("konusmalar").child(uid).child(myID).setValue(konusmaMesajAlan)

                }

            }
        }
    }*/

    private fun resimSec() {
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_PICK)
        startActivityForResult(intent,RESIM_SEC)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESIM_SEC && resultCode == RESULT_OK && data!!.data != null){
            dosyaTuruResimMi = true
            resimUri = data.data
            //resimYukle()
            uploadStorage(resimUri!!)
            Log.e("KONTROL PATH",resimUri.toString())
        }


        if (requestCode == VIDEO_SEC && resultCode == RESULT_OK){
            if (data?.data != null){
                dosyaTuruResimMi = false
                val uriPathHelper = URIPathHelper()
                //videoFullPath = uriPathHelper.getPath(this,data.data!!)!!
                videoUri = data.data!!
                compressVideo(videoUri!!)
                /*if (videoFullPath != null){
                    CompressSiliVideo(supportFragmentManager,this,this).execute(videoUri.toString())

                }*/

            }
        }
    }

    private fun compressVideo(video: Uri) {
        var yeniOlusturulanDosyaninKlasoru = File(Environment.getExternalStorageDirectory().absolutePath+"/Android/data/${applicationContext.packageName}/files/Movies/")
        yeniOlusturulanDosyaninKlasoru.parentFile.mkdirs()
        Log.e("KONTROL","İFE GİRMEDİ")
            Log.e("KONTROLCOMPRESS", yeniOlusturulanDosyaninKlasoru.path)
            var dialog = ProgressFragment()
            VideoCompressor.start(
                    this,
                    videoUri,
                    srcPath = null,
                    destPath = yeniOlusturulanDosyaninKlasoru.path,
                    listener = object : CompressionListener {
                        override fun onCancelled() {

                        }

                        override fun onFailure(failureMessage: String) {

                        }

                        override fun onProgress(percent: Float) {
                            Log.e("PROGRESS","%${percent}")

                        }

                        override fun onStart() {
                            dialog.show(supportFragmentManager, "LightCompress")
                            dialog.isCancelable = false

                        }

                        override fun onSuccess() {
                            dialog.dismiss()
                            uploadStorageVideo(yeniOlusturulanDosyaninKlasoru.path)
                            Log.e("KONTROLCOMPRESS", "SUCCCES")

                        }

                    },
                    configureWith = Configuration(
                            quality = VideoQuality.MEDIUM,
                            isMinBitRateEnabled = true,
                            keepOriginalResolution = false,
                            videoHeight = null /*Double, ignore, or null*/,
                            videoWidth = null /*Double, ignore, or null*/,
                            videoBitrate = null /*Int, ignore, or null*/
                    )
            )

    }


    private fun dahaFazlaMesajGetir(){
        childEventListenerMore = db.child("chats").child(myID).child(uid).orderByKey().endAt(ilkMesajID).limitToLast(NUMBER_OF_MESSAGE_PER_PAGE).addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                var okunanmesaj = snapshot.getValue(Mesaj::class.java)
                if (!listedeOlanMesajID.equals(snapshot.key!!)){
                    mesajlar.add(moremesajPos++,okunanmesaj!!)
                }else{
                    listedeOlanMesajID=ilkMesajID
                }

                if (moremesajPos ==1){
                    ilkMesajID = snapshot.key!!
                }


                MyAdapter.notifyDataSetChanged()
                myRecyclerView.scrollToPosition(NUMBER_OF_MESSAGE_PER_PAGE)

                Log.e("KONTROL", ilkMesajID)
                println("RESİM ID" + okunanmesaj!!.mesajResim!!)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }


    fun showDialog(){
        var options = arrayOf("Fotoğraf","Video")
        var builder = AlertDialog.Builder(this)
        builder.setTitle("Ne göndermek istiyorsunuz?")
        builder.setSingleChoiceItems(options,0,object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                selectedOptions = options[which]

            }

        })
        builder.setPositiveButton("Tamam", object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                if (selectedOptions == "Fotoğraf"){
                    resimSec()
                }else if(selectedOptions == "Video"){
                    openGalleryForVideo()
                }
            }

        })
        builder.setNegativeButton("İptal",object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog!!.dismiss()

            }

        })
        builder.show()
    }


    private fun openGalleryForVideo() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_PICK
        startActivityForResult(Intent.createChooser(intent, "Select Video"),VIDEO_SEC)
    }

    /*private fun setUpRecording(){
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)

        var file = File(Environment.getExternalStorageDirectory().absolutePath,"Cimbomes/Media/Recording")
        if (!file.exists()) {
            file.mkdirs()
        }
        auidoPath = file.absolutePath + File.separator + System.currentTimeMillis() + ".3gp"
        mediaRecorder.setOutputFile(auidoPath)
    }*/


    private fun mesajlarıGetir() {

        childEventListener = db.child("chats").child(myID).child(uid).limitToLast(NUMBER_OF_MESSAGE_PER_PAGE).addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                var okunanmesaj = snapshot.getValue(Mesaj::class.java)
                println(okunanmesaj.toString())
                mesajlar.add(okunanmesaj!!)
                if (mesajPosition ==0){
                    ilkMesajID = snapshot.key!!
                    listedeOlanMesajID = snapshot.key!!
                }
                mesajPosition++

                mesajGorulduBilgisiniGuncelle(snapshot!!.key.toString())
                sonMesajGorulduBilgisiniGuncelle(snapshot!!.key.toString())

                MyAdapter.notifyItemInserted(mesajlar.size-1)
                myRecyclerView.scrollToPosition(mesajlar.size-1)



            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                var mesajj = snapshot.getValue(Mesaj::class.java)
                var key = snapshot.key.toString()
                var oncekimesaj = ""
                var removedindex : Int? = null

                for ((index,element) in mesajlar.withIndex()){
                    if (element.mesajKey.toString() == key){
                        removedindex = index
                        if (removedindex != 0 ){
                            var once = mesajlar.get(removedindex - 1)
                            oncekimesaj = once.mesaj.toString()
                        }
                    }
                }
                if (removedindex != null) {
                    if (mesajlar.size == removedindex + 1){
                        db.child("konusmalar").child(myID).child(uid).child("son_mesaj").setValue(oncekimesaj).addOnSuccessListener {
                            mesajlar.removeAt(removedindex!!)
                            MyAdapter.notifyItemRemoved(removedindex)
                        }
                    }else{
                        mesajlar.removeAt(removedindex!!)
                        MyAdapter.notifyItemRemoved(removedindex)
                    }

                }


            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun sonMesajGorulduBilgisiniGuncelle(mesajKey: String?) {
        FirebaseDatabase.getInstance().reference.child("chats").child(uid).child(myID).child(mesajKey!!).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child("goruldu").getValue() == true && snapshot.child("user_id").getValue().toString().equals(myID)){
                    ekrandaSonGorulmeVarMi = true
                    gorulduContainer.visibility = View.VISIBLE
                    gorulduContainer.startAnimation(AnimationUtils.loadAnimation(this@ChatActivity,android.R.anim.fade_in))
                }else{
                    ekrandaSonGorulmeVarMi = false
                    gorulduContainer.visibility = View.GONE
                    gorulduContainer.startAnimation(AnimationUtils.loadAnimation(this@ChatActivity,android.R.anim.fade_out))
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun mesajGorulduBilgisiniGuncelle(mesajID: String) {
        FirebaseDatabase.getInstance().reference.child("chats").child(myID).child(uid).child(mesajID).child("goruldu").setValue(true)
                .addOnCompleteListener {
                    FirebaseDatabase.getInstance().reference.child("konusmalar").child(myID).child(uid).child("goruldu").setValue(true).addOnCompleteListener {

                    }
                }

    }


    private fun setupMesajlarRecyclerView() {
        var myLayoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        myLayoutManager.stackFromEnd = true
        myRecyclerView = rvChat

        myRecyclerView.layoutManager = myLayoutManager
        MyAdapter = MessageAdapter(mesajlar,this,this)
        myRecyclerView.adapter = MyAdapter
    }

    private var typingEventListener = object : ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {

            if (snapshot.getValue() != null){
                if (snapshot.getValue() == true){
                    if (ekrandaSonGorulmeVarMi){
                        gorulduContainer.visibility = View.GONE
                    }
                    typingContainer.visibility = View.VISIBLE
                    typingContainer.startAnimation(AnimationUtils.loadAnimation(this@ChatActivity,android.R.anim.fade_in))
                }else if(snapshot.getValue() == false){

                    if (ekrandaSonGorulmeVarMi){
                        gorulduContainer.visibility = View.VISIBLE
                    }
                    typingContainer.visibility = View.GONE
                    typingContainer.startAnimation(AnimationUtils.loadAnimation(this@ChatActivity,android.R.anim.fade_out))
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {

        }

    }


    val watcher = object : TextWatcher{
        var typing = false
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (edittextChat.text.toString().length > 0){
                imgSendChat.visibility = View.VISIBLE
                imgMicChat.visibility = View.GONE
                imgMicChat.isEnabled = false
                imgSendChat.isEnabled = true
            }else{
                imgSendChat.visibility = View.GONE
                imgMicChat.visibility = View.VISIBLE
                imgMicChat.isEnabled = true
                imgSendChat.isEnabled = false
            }
        }

        override fun afterTextChanged(s: Editable?) {
            if (!TextUtils.isEmpty(s.toString()) && s.toString().trim().length == 1){
                typing = true
                typingRef.child("typing").setValue(true)
            }else if (typing && s.toString().trim().length == 0){
                typingRef.child("typing").setValue(false)
                typing = false
            }
        }

    }





    private fun setTouchDelegate(view: View, dimen: Int) {
        val parent = view.parent as View
        parent.post {
            val delegateArea = Rect()
            view.getHitRect(delegateArea)
            delegateArea.right += dimen
            delegateArea.left -= dimen
            delegateArea.bottom += dimen
            delegateArea.top -= dimen
            parent.touchDelegate = TouchDelegate(delegateArea, view)
        }
    }


    override fun onItemClick(position: Int) {
        var clickedItem = mesajlar[position]
        var mesajKey = clickedItem.mesajKey.toString()
        var gonderenID = clickedItem.user_id.toString()
        println("chat" + mesajKey.toString())
        EventBus.getDefault().postSticky(EventBusDataEvents.SendMessageInfo(mesajKey,gonderenID,uid.toString()))
        val dialog = MesajlarBottomSheet()
        dialog.show(supportFragmentManager,"mesajInfo")
    }

    override fun onBackPressed() {
        db.child("chats").child(myID).child(uid).removeEventListener(childEventListener)
        finish()
    }

    override fun onPause() {
        super.onPause()
        typingRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild("typing")){
                    typingRef.child("typing").setValue(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        Log.e("KONTROL","onpause girdi")
        dinlenecekYaziyorRef.removeEventListener(typingEventListener)

    }



    override fun onStart() {
        super.onStart()
        //AuthListener Eklenicek
        Log.e("KONTROL","onstarta girdi")
    }


    override fun onStop() {
        // auth listener null değilse remove edilicek
        super.onStop()
        Log.e("KONTROL","onstopa girdi")

    }

    override fun onResume() {

        super.onResume()
        Log.e("KONTROL","onresume girdi")
        dinlenecekYaziyorRef.child("typing").addValueEventListener(typingEventListener)

    }

    override fun onRestart() {
        super.onRestart()
        Log.e("KONTROL","Onrestarta girdi")

    }

    fun uploadStorage(result: Uri) {

        //var fileUri = Uri.parse(result)
        val Rndomuid = UUID.randomUUID().toString()
        var myref = storageReference.child("mesajphotos").child(myID).child(uid).child(Rndomuid)
        if (result != null){
            val progressDialog = ProgressFragment()
            progressDialog.show(this.supportFragmentManager,"resimloading")
            progressDialog.isCancelable = false
            myref.putFile(result).addOnSuccessListener {
                myref.downloadUrl.addOnSuccessListener {
                    var mesajAtan = HashMap<String,Any>()
                    var fotoMesajKey = db.child("chats").child(myID).child(uid).push().key
                    mesajAtan.put("mesaj","")
                    mesajAtan.put("goruldu",true)
                    mesajAtan.put("time",ServerValue.TIMESTAMP)
                    mesajAtan.put("type","image")
                    mesajAtan.put("user_id",myID)
                    mesajAtan.put("mesajResim",it.toString())
                    mesajAtan.put("video",video)
                    mesajAtan.put("audio","")
                    mesajAtan.put("mesajKey",fotoMesajKey.toString())


                    db.child("chats").child(myID).child(uid).child(fotoMesajKey!!).setValue(mesajAtan)



                    var mesajAlan = HashMap<String,Any>()
                    mesajAlan.put("mesaj","")
                    mesajAlan.put("goruldu",false)
                    mesajAlan.put("time",ServerValue.TIMESTAMP)
                    mesajAlan.put("type","image")
                    mesajAlan.put("user_id",myID)
                    mesajAlan.put("mesajResim",it.toString())
                    mesajAlan.put("video",video)
                    mesajAlan.put("audio","")
                    mesajAlan.put("mesajKey",fotoMesajKey.toString())
                    db.child("chats").child(uid).child(myID).child(fotoMesajKey!!).setValue(mesajAlan)




                    var konusmaMesajAtan = HashMap<String,Any>()
                    konusmaMesajAtan.put("time",ServerValue.TIMESTAMP)
                    konusmaMesajAtan.put("goruldu",true)
                    konusmaMesajAtan.put("son_mesaj","Fotoğraf")
                    konusmaMesajAtan.put("typing",false)
                    db.child("konusmalar").child(myID).child(uid).setValue(konusmaMesajAtan)


                    var konusmaMesajAlan = HashMap<String,Any>()
                    konusmaMesajAlan.put("time",ServerValue.TIMESTAMP)
                    konusmaMesajAlan.put("goruldu",false)
                    konusmaMesajAlan.put("son_mesaj","Fotograf")
                    //konusmaMesajAlan.put("typing",false)
                    db.child("konusmalar").child(uid).child(myID).setValue(konusmaMesajAlan)
                    progressDialog.dismiss()


                }.addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this,"Bir hata oluştu",Toast.LENGTH_SHORT).show()
                }
            }.addOnProgressListener(object :OnProgressListener<UploadTask.TaskSnapshot>{
                override fun onProgress(snapshot: UploadTask.TaskSnapshot) {
                    var progress = 100 * snapshot.bytesTransferred / snapshot.totalByteCount
                    progressDialog.tvBilgi.text = "%${progress} yüklendi"
                }

            })

        }
    }




    fun uploadStorageVideo(result: String) {
        var fileUri = Uri.parse("file://"+result)
        val Rndomuid = UUID.randomUUID().toString()
        var myref = storageReference.child("mesajVideos").child(myID).child(uid).child(Rndomuid)
        Log.e("KONTROL","VİDEO STORAGE FONKSİYONUNA GİRDİ")
        if (fileUri != null){
            Log.e("KONTROL","VİDEO STORAGE FONKSİYONUNA GİRDİ2")
            val progressDialog = ProgressFragment()
            progressDialog.show(this.supportFragmentManager,"videoLoading")
            progressDialog.isCancelable = false
            myref.putFile(fileUri).addOnSuccessListener {
                myref.downloadUrl.addOnSuccessListener {
                    var fotoMesajKey = db.child("chats").child(myID).child(uid).push().key
                    var mesajAtan = HashMap<String,Any>()
                    mesajAtan.put("mesaj","")
                    mesajAtan.put("goruldu",true)
                    mesajAtan.put("time",ServerValue.TIMESTAMP)
                    mesajAtan.put("type","video")
                    mesajAtan.put("user_id",myID)
                    mesajAtan.put("mesajResim","")
                    mesajAtan.put("video",it.toString())
                    mesajAtan.put("audio","")
                    mesajAtan.put("mesajKey",fotoMesajKey.toString())



                    db.child("chats").child(myID).child(uid).child(fotoMesajKey!!).setValue(mesajAtan)



                    var mesajAlan = HashMap<String,Any>()
                    mesajAlan.put("mesaj","")
                    mesajAlan.put("goruldu",false)
                    mesajAlan.put("time",ServerValue.TIMESTAMP)
                    mesajAlan.put("type","video")
                    mesajAlan.put("user_id",myID)
                    mesajAlan.put("mesajResim","")
                    mesajAlan.put("video",it.toString())
                    mesajAlan.put("audio","")
                    mesajAlan.put("mesajKey",fotoMesajKey.toString())
                    db.child("chats").child(uid).child(myID).child(fotoMesajKey!!).setValue(mesajAlan)




                    var konusmaMesajAtan = HashMap<String,Any>()
                    konusmaMesajAtan.put("time",ServerValue.TIMESTAMP)
                    konusmaMesajAtan.put("goruldu",true)
                    konusmaMesajAtan.put("son_mesaj","Video")
                    konusmaMesajAtan.put("typing",false)
                    db.child("konusmalar").child(myID).child(uid).setValue(konusmaMesajAtan)


                    var konusmaMesajAlan = HashMap<String,Any>()
                    konusmaMesajAlan.put("time",ServerValue.TIMESTAMP)
                    konusmaMesajAlan.put("goruldu",false)
                    konusmaMesajAlan.put("son_mesaj","Video")
                    //konusmaMesajAlan.put("typing",false)
                    db.child("konusmalar").child(uid).child(myID).setValue(konusmaMesajAlan)
                    progressDialog.dismiss()


                }.addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this,"Bir hata oluştu",Toast.LENGTH_SHORT).show()
                }
            }.addOnProgressListener(object :OnProgressListener<UploadTask.TaskSnapshot>{
                override fun onProgress(snapshot: UploadTask.TaskSnapshot) {
                    var progress = 100 * snapshot.bytesTransferred / snapshot.totalByteCount
                    progressDialog.tvBilgi.text = "%${progress} yüklendi"
                }

            })

        }
    }


}