package com.motive.cimbomes.activity

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.TouchDelegate
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import com.dinuscxj.refresh.RecyclerRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.motive.cimbomes.R
import com.motive.cimbomes.adapter.GroupMessagesAdapter
import com.motive.cimbomes.fragments.ProgressFragment
import com.motive.cimbomes.model.GroupKonusma
import com.motive.cimbomes.model.GroupMembers
import com.motive.cimbomes.model.Groups
import com.motive.cimbomes.model.Mesaj
import com.motive.cimbomes.utils.EventBusDataEvents
import com.motive.cimbomes.utils.URIPathHelper
import com.motive.cimbomes.utils.UniversalImageLoader
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_group_chat.*
import kotlinx.android.synthetic.main.fragment_progress.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class GroupChatActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db : DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var mesajlar : ArrayList<Mesaj>
    private lateinit var myRecyclerView : RecyclerView
    private lateinit var groupKey : String
    private lateinit var MyAdapter : GroupMessagesAdapter
    private lateinit var childEventListener : ChildEventListener
    private lateinit var childEventListenerMore : ChildEventListener
    private lateinit var typingRef : DatabaseReference
    val NUMBER_OF_MESSAGE_PER_PAGE = 15

    var mesajPosition = 0
    var ilkMesajID = ""
    var moremesajPos = 0
    var listedeOlanMesajID= ""


    var selectedOptions = "Fotoğraf"
    var videoUri : Uri? = null
    var dosyaTuruResimMi = false

    //fotoğraf gönderme
    val RESIM_SEC = 100
    var resimUri : Uri? = null
    var resimLink = ""
    val VIDEO_SEC = 200
    var video = ""
    var groupImage = ""
    var indexSira = ""
    var groupName = ""
    var members = mutableListOf<GroupMembers>()
    private var statikMi = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference
        mesajlar = ArrayList<Mesaj>()
        groupKey = intent.getStringExtra("GroupKey")!!
        typingRef = FirebaseDatabase.getInstance().reference.child("groups").child(groupKey).child("member")

        setupGroupInfo()
        setupGroupsRecyclerView()
        mesajlariGetir()
        uyelerigetir()
        controlGroup()

        yaziyorBilgisiniGuncelle()
        setTouchDelegate(groupBackBtn,100)
        setTouchDelegate(tvgroupInfo,100)

        groupBackBtn.setOnClickListener {
            startActivity(Intent(this,FeedActivity::class.java))
            overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right)
            finish()
        }

        tvgroupInfo.setOnClickListener {

            val intent = Intent(this,GroupInfoActivity::class.java)
            intent.putExtra("grupID",groupKey)
            intent.putExtra("grupImage",groupImage)
            intent.putExtra("grupName",groupName)
            intent.putExtra("isStatic",statikMi)
            EventBus.getDefault().postSticky(EventBusDataEvents.SendGroupInfo(members))
            startActivity(intent)
        }


        typingRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for ((index,element) in snapshot.children.withIndex()){
                    var user = element.getValue(GroupMembers::class.java)
                    if (user!!.uid == mAuth.currentUser!!.uid){
                        indexSira = index.toString()

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


        imgSendGroup.setOnClickListener {
            var mesajText = editTextMessageGroup.text.toString().trim()
            if (mesajText.length>0){
                var newMesajKey = db.child("groups").child(groupKey).child("messages").push().key.toString()
                var mesaj = Mesaj(mesajText,true,System.currentTimeMillis(),"text",mAuth.currentUser!!.uid,"","","",newMesajKey)


                db.child("groups").child(groupKey).child("messages").child(newMesajKey).setValue(mesaj)

                var konusmaGonderen = GroupKonusma(true,mesajText,System.currentTimeMillis(),mAuth.currentUser!!.uid,groupImage,groupKey,groupName)
                db.child("grupkonusmalar").child(mAuth.currentUser!!.uid).child(groupKey).setValue(konusmaGonderen)
                Log.e("KONTROL",members.toString())

                for (i in members){
                    if (i.uid != mAuth.currentUser!!.uid){
                        var konusmaAlanlar = GroupKonusma(false,mesajText,System.currentTimeMillis(),i.uid,groupImage,groupKey,groupName)
                        db.child("grupkonusmalar").child(i.uid!!).child(groupKey).setValue(konusmaAlanlar)
                    }

                }


                editTextMessageGroup.setText("")


            }
        }

        groupImageView.setOnClickListener {
            val intent = Intent(this,FullImageActivity::class.java)
            intent.putExtra("fullImage",groupImage)
            startActivity(intent)
        }

        groupAddPhotoOrVideo.setOnClickListener {
            showDialog()
        }

        editTextMessageGroup.addTextChangedListener(watcher)


        recyclerRefreshGroupChat.setOnRefreshListener(object : RecyclerRefreshLayout.OnRefreshListener{
            override fun onRefresh() {
                db.child("groups").child(groupKey).child("messages").addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot!!.childrenCount>mesajlar.size){
                            moremesajPos=0
                            dahaFazlaMesajGetir()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

                recyclerRefreshGroupChat.setRefreshing(false)
            }

        })
    }

    private fun uyelerigetir() {
        typingRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.getValue() != null){
                    for (i in snapshot.children){
                        var user = i.getValue(GroupMembers::class.java)
                        members.add(user!!)
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun yaziyorBilgisiniGuncelle() {
        db.child("groups").child(groupKey).child("member").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var yaziyormU = false
                var name = ""
                if (snapshot.getValue() != null){
                    for (i in snapshot.children){
                        var typing = i.getValue(GroupMembers::class.java)
                        if (typing!!.typing == true){
                            name = typing.name!!
                            yaziyormU = true
                            break
                        }


                    }
                    if (yaziyormU){
                        tvgroupInfo.text = name + " " + "yazıyor."
                    }else{
                        tvgroupInfo.text = "Grup bilgisi için dokunun"
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private var watcher = object : TextWatcher{
        var typing = false

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {



            if (!TextUtils.isEmpty(s.toString()) && s.toString().trim().length == 1){
                typing = true
                typingRef.child(indexSira).child("typing").setValue(true)

            }else if (typing && s.toString().trim().length == 0){
                typingRef.child(indexSira).child("typing").setValue(false)
                typing = false
            }
        }

    }


    private fun mesajlariGetir() {
        childEventListener = db.child("groups").child(groupKey).child("messages").addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                var okunanmesaj = snapshot.getValue(Mesaj::class.java)
                mesajlar.add(okunanmesaj!!)
                if (mesajPosition ==0){
                    ilkMesajID = snapshot.key!!
                    listedeOlanMesajID = snapshot.key!!
                }
                mesajPosition++



                MyAdapter.notifyItemInserted(mesajlar.size-1)
                myRecyclerView.scrollToPosition(mesajlar.size-1)

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

    private fun setupGroupInfo() {
        db.child("groups").child(groupKey).child("groupName").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.getValue() != null){
                    groupName = snapshot.value.toString()
                    groupTitleTV.text = snapshot.value.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        db.child("groups").child(groupKey).child("static").get().addOnSuccessListener {
            if (it.value == true){
                statikMi = true
            }else if (it.value == false){
                statikMi = false
            }
        }
        db.child("groups").child(groupKey).child("image").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.getValue() != null){
                    groupImage = snapshot.value.toString()
                    UniversalImageLoader.setImage(groupImage!!,groupImageView,null,"")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun setupGroupsRecyclerView() {
        var myLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        myLayoutManager.stackFromEnd = true
        myRecyclerView = groupChatRV
        myRecyclerView.layoutManager = myLayoutManager

        MyAdapter = GroupMessagesAdapter(mesajlar,this)
        myRecyclerView.adapter = MyAdapter
    }

    private fun dahaFazlaMesajGetir(){
        childEventListenerMore = db.child("groups").child(groupKey).child("messages").orderByKey().endAt(ilkMesajID).limitToLast(NUMBER_OF_MESSAGE_PER_PAGE).addChildEventListener(object : ChildEventListener{
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESIM_SEC && resultCode == RESULT_OK && data!!.data != null){
            dosyaTuruResimMi = true
            resimUri = data.data
            //resimYukle()
            uploadStorage(resimUri!!)

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

                }

                override fun onStart() {
                    dialog.show(supportFragmentManager, "LightCompress")
                    dialog.isCancelable = false

                }

                override fun onSuccess() {
                    dialog.dismiss()
                    uploadStorageVideo(yeniOlusturulanDosyaninKlasoru.path)


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


    private fun resimSec() {
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_PICK)
        startActivityForResult(intent,RESIM_SEC)
    }

    private fun openGalleryForVideo() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_PICK
        startActivityForResult(Intent.createChooser(intent, "Select Video"),VIDEO_SEC)
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


    fun uploadStorage(result: Uri) {

        //var fileUri = Uri.parse(result)
        val Rndomuid = UUID.randomUUID().toString()
        var myref = storageReference.child("groupChats").child(groupKey).child(System.currentTimeMillis().toString()).child(Rndomuid)
        if (result != null){
            val progressDialog = ProgressFragment()
            progressDialog.show(this.supportFragmentManager,"resimloading")
            progressDialog.isCancelable = false
            myref.putFile(result).addOnSuccessListener {
                myref.downloadUrl.addOnSuccessListener {
                    var newMesajKey = db.child("groups").child(groupKey).child("messages").push().key.toString()
                    var fotoMesaj = Mesaj("",true,System.currentTimeMillis(),"image",mAuth.currentUser!!.uid,it.toString(),"","",newMesajKey)

                    db.child("groups").child(groupKey).child("messages").child(newMesajKey).setValue(fotoMesaj)

                    progressDialog.dismiss()


                }.addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this,"Bir hata oluştu", Toast.LENGTH_SHORT).show()
                }
            }.addOnProgressListener(object : OnProgressListener<UploadTask.TaskSnapshot> {
                override fun onProgress(snapshot: UploadTask.TaskSnapshot) {
                    var progress = 100 * snapshot.bytesTransferred / snapshot.totalByteCount
                    progressDialog.tvBilgi.text = "%${progress} yüklendi"
                }

            })

        }
    }

    private fun controlGroup() {
        db.child("groups").child(groupKey).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.getValue() == null){
                    startActivity(Intent(this@GroupChatActivity,FeedActivity::class.java))
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun uploadStorageVideo(result: String) {
        var fileUri = Uri.parse("file://"+result)
        val Rndomuid = UUID.randomUUID().toString()
        var myref = storageReference.child("groupChats").child(groupKey).child(System.currentTimeMillis().toString()).child(Rndomuid)

        if (fileUri != null){

            val progressDialog = ProgressFragment()
            progressDialog.show(this.supportFragmentManager,"videoLoading")
            progressDialog.isCancelable = false
            myref.putFile(fileUri).addOnSuccessListener {
                myref.downloadUrl.addOnSuccessListener {
                    var newMesajKey = db.child("groups").child(groupKey).child("messages").push().key.toString()
                    var videoMesaj = Mesaj("",true,System.currentTimeMillis(),"video",mAuth.currentUser!!.uid,"",it.toString(),"",newMesajKey)

                    db.child("groups").child(groupKey).child("messages").child(newMesajKey).setValue(videoMesaj)

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

    override fun onBackPressed() {
        startActivity(Intent(this,FeedActivity::class.java))
        overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right)
        finish()
    }

    override fun onResume() {
        super.onResume()
        setupGroupInfo()
    }


}