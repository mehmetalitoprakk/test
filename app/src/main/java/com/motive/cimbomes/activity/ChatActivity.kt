package com.motive.cimbomes.activity

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.TouchDelegate
import android.view.View
import android.view.animation.AnimationUtils
import android.webkit.MimeTypeMap
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dinuscxj.refresh.RecyclerRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.motive.cimbomes.R
import com.motive.cimbomes.adapter.MessageAdapter
import com.motive.cimbomes.fragments.ProgressFragment
import com.motive.cimbomes.model.Mesaj
import com.motive.cimbomes.model.Users
import com.motive.cimbomes.utils.UniversalImageLoader
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_chat.*
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

    //fotoğraf gönderme
    val RESIM_SEC = 100
    var resimUri : Uri? = null
    var resimLink = ""



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

        imgBackChat.setOnClickListener {
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
                var mesajAtan = HashMap<String,Any>()
                mesajAtan.put("mesaj",mesajText)
                mesajAtan.put("goruldu",true)
                mesajAtan.put("time",ServerValue.TIMESTAMP)
                mesajAtan.put("type","text")
                mesajAtan.put("user_id",myID)
                mesajAtan.put("mesajResim",resimLink)

                yeniMesajKey = db.child("chats").child(myID).child(uid).push().key

                db.child("chats").child(myID).child(uid).child(yeniMesajKey!!).setValue(mesajAtan)



                var mesajAlan = HashMap<String,Any>()
                mesajAlan.put("mesaj",mesajText)
                mesajAlan.put("goruldu",false)
                mesajAlan.put("time",ServerValue.TIMESTAMP)
                mesajAlan.put("type","text")
                mesajAlan.put("user_id",myID)
                mesajAlan.put("mesajResim",resimLink)
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
        imgMicChat.setOnClickListener {
            Toast.makeText(this,"Mic tıklandı",Toast.LENGTH_SHORT).show()
        }

        imgAddChat.setOnClickListener {
            resimSec()
        }

    }

    private fun resimSec() {
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_PICK)
        startActivityForResult(intent,RESIM_SEC)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESIM_SEC && resultCode == RESULT_OK && data!!.data != null){
            resimUri = data.data
            resimYukle()
        }
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


                println("Resim Kontrol")

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
                imgMicChat.visibility = View.INVISIBLE
                imgMicChat.isEnabled = false
                imgSendChat.isEnabled = true
            }else{
                imgSendChat.visibility = View.INVISIBLE
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


    private fun resimYukle(){
        val Rndomuid = UUID.randomUUID().toString()
        var myref = storageReference.child("mesajphotos").child(myID).child(uid).child(Rndomuid)
        if (resimUri != null){
            val progressDialog = ProgressFragment()
            progressDialog.show(this.supportFragmentManager,"resimloading")
            progressDialog.isCancelable = false
            myref.putFile(resimUri!!).addOnSuccessListener {
                myref.downloadUrl.addOnSuccessListener {
                    var mesajAtan = HashMap<String,Any>()
                    mesajAtan.put("mesaj","")
                    mesajAtan.put("goruldu",true)
                    mesajAtan.put("time",ServerValue.TIMESTAMP)
                    mesajAtan.put("type","image")
                    mesajAtan.put("user_id",myID)
                    mesajAtan.put("mesajResim",it.toString())

                    var fotoMesajKey = db.child("chats").child(myID).child(uid).push().key

                    db.child("chats").child(myID).child(uid).child(fotoMesajKey!!).setValue(mesajAtan)



                    var mesajAlan = HashMap<String,Any>()
                    mesajAlan.put("mesaj","")
                    mesajAlan.put("goruldu",false)
                    mesajAlan.put("time",ServerValue.TIMESTAMP)
                    mesajAlan.put("type","image")
                    mesajAlan.put("user_id",myID)
                    mesajAlan.put("mesajResim",it.toString())
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
    }

    override fun onBackPressed() {
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
        db.child("chats").child(myID).child(uid).removeEventListener(childEventListener)
    }

    override fun onResume() {
        super.onResume()
        Log.e("KONTROL","onresume girdi")
        dinlenecekYaziyorRef.child("typing").addValueEventListener(typingEventListener)
    }

    override fun onRestart() {
        super.onRestart()
        Log.e("KONTROL","Onrestarta girdi")
        db.child("chats").child(myID).child(uid).addChildEventListener(childEventListener)
    }


}