package com.motive.cimbomes.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.motive.cimbomes.R
import com.motive.cimbomes.fragments.*
import com.motive.cimbomes.model.Users
import com.motive.cimbomes.utils.UniversalImageLoader
import com.nostra13.universalimageloader.core.ImageLoader
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_feed.*

class FeedActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var circle : CircleImageView
    lateinit var mAuth: FirebaseAuth
    lateinit var db : DatabaseReference
    lateinit var headerIsim : TextView
    lateinit var headerProgress : ProgressBar
    val CONTACT_RQ = 101
    val RECORD_RQ = 102
    lateinit var toolbars : androidx.appcompat.widget.Toolbar

    var contactPermissinVerildiMi = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)
        initImageLoader()
        checkContactPermission(android.Manifest.permission.READ_CONTACTS,"contact",CONTACT_RQ)
        checkContactPermission(Manifest.permission.RECORD_AUDIO,"Record",RECORD_RQ)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference



        var drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        var navView = findViewById<NavigationView>(R.id.navView)
        toolbars = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)


        setSupportActionBar(toolbar)
        navView.bringToFront()
        val header = navView.getHeaderView(0)
        circle = header.findViewById(R.id.headerFoto)
        headerIsim = header.findViewById(R.id.isimSoyisimHeader)
        headerProgress = header.findViewById(R.id.headerprogress)
        setCurrentFragment(AnasayfaFragment())


        db.child("users").child(mAuth.currentUser!!.uid).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var user = snapshot.getValue(Users::class.java)
                headerIsim.text = user!!.isim + user!!.soyisim
                UniversalImageLoader.setImage(user.profilePic!!,circle,headerProgress,"")

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })



        var toogle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbars,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toogle)
        toogle.syncState()

        navView.setNavigationItemSelectedListener(this)


    }

    private fun checkContactPermission(permission:String,name:String,requestCode:Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            when{
                ContextCompat.checkSelfPermission(applicationContext,permission) == PackageManager.PERMISSION_GRANTED ->{
                    Toast.makeText(applicationContext,"$name permission granted",Toast.LENGTH_SHORT).show()
                    contactPermissinVerildiMi = true
                }
                shouldShowRequestPermissionRationale(permission) -> showDialog(permission,name,requestCode)
                else -> ActivityCompat.requestPermissions(this, arrayOf(permission),requestCode)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        fun innerChech(name:String){
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){

                val builder = AlertDialog.Builder(this)
                builder.apply {
                    setMessage("'Cimbomes uygulamasını en verimli şekilde kullanmanız için kişilerinize erişmesine izin vermeniz gerekiyor. İzin verilsin mi?'")
                    setTitle("İzin isteği")
                    setPositiveButton("AYARLARA GİT"){dialog,which ->
                        dialog.cancel()
                        startActivity(Intent(Settings.ACTION_SETTINGS))

                    }
                }
                val dialog = builder.create()
                dialog.show()
            }else{
                Toast.makeText(this,"İzin verildi",Toast.LENGTH_SHORT).show()
                contactPermissinVerildiMi = true
            }
        }
        when(requestCode){
            CONTACT_RQ -> innerChech("contact")
            RECORD_RQ -> innerChech("Record")
        }


    }

    private fun showDialog(permission: String,name: String,requestCode: Int){
        val builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage("Cimbomes uygulamasının kişilerinize erişmesine izin verilsin mi?")
            setTitle("İzin isteği")
            setPositiveButton("Tamam"){dialog,which ->
                ActivityCompat.requestPermissions(this@FeedActivity, arrayOf(permission),requestCode)
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun initImageLoader() {
        var universalImageLoader = UniversalImageLoader(applicationContext)
        ImageLoader.getInstance().init(universalImageLoader.config)
    }

    private fun setCurrentFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.mainContainer, fragment)
            commit()
        }

    }


    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            super.onBackPressed()
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawerLayout.closeDrawer(GravityCompat.START)
        when(item.itemId){
            R.id.contacts -> {
                if (contactPermissinVerildiMi){
                    setCurrentFragment(KisilerFragment())
                    toolbars.setTitle("Kişilerim")
                }else{
                    Toast.makeText(this,"Lütfen 'Cimbomes' uygulamasının kişilerinize erişmesine izin verin.! ",Toast.LENGTH_SHORT).show()
                }

            }
            R.id.home -> {
                setCurrentFragment(AnasayfaFragment())
                toolbars.setTitle("CimbomES")
            }
            R.id.newGroup -> {
                setCurrentFragment(YeniGrupFragment())
                toolbars.setTitle("Yeni Grup Oluştur")
            }
            R.id.savedMessages -> {
                setCurrentFragment(KaydedilenMesajlarFragment())
                toolbars.setTitle("Kaydedilen Mesajlar")
            }
            R.id.settings -> {
                setCurrentFragment(AyarlarFragment())
                toolbars.setTitle("Ayarlar")
            }
            R.id.logout ->{

            }

        }
        return true
    }
}