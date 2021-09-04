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
import com.google.firebase.iid.FirebaseInstanceId
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.motive.cimbomes.R
import com.motive.cimbomes.fragments.*
import com.motive.cimbomes.model.Users
import com.motive.cimbomes.utils.SignOutFragment
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
    lateinit var toolbars : androidx.appcompat.widget.Toolbar
    var isAdmin = false

    var contactPermissinVerildiMi = false
    var storagePermissonVerildiMi = false
    var storageYazmaPermissionVerildiMi = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)
        initImageLoader()


        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference
        onlineControl()
        getFcmTokenForExistsUser()

        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener{
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    if (p0 != null){
                        if (p0!!.areAllPermissionsGranted()){
                            //all permissions granted
                            contactPermissinVerildiMi = true
                            storagePermissonVerildiMi = true
                            storageYazmaPermissionVerildiMi = true
                            setCurrentFragment(AnasayfaFragment())

                        }else{
                            Toast.makeText(this@FeedActivity,"Uygulamayı kullanabilmeniz için erişimlere izin vermeniz gerekiyor.",Toast.LENGTH_LONG).show()
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts("package",getPackageName(), null)
                            intent.data = uri
                            startActivity(intent)
                        }
                    }

                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    if (p1 != null){
                        p1.continuePermissionRequest()
                    }
                }

            }).check()



        var drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        var navView = findViewById<NavigationView>(R.id.navView)
        toolbars = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)


        setSupportActionBar(toolbar)
        navView.bringToFront()
        val header = navView.getHeaderView(0)
        circle = header.findViewById(R.id.headerFoto)
        headerIsim = header.findViewById(R.id.isimSoyisimHeader)
        headerProgress = header.findViewById(R.id.headerprogress)





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
        var menu = navView.menu
        val admin = menu.findItem(R.id.admin)


        db.child("users").child(mAuth.currentUser!!.uid).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var user = snapshot.getValue(Users::class.java)
                headerIsim.text = user!!.isim + " " + user!!.soyisim
                val admin = user.isAdmin
                if (admin!!){
                    menu.findItem(R.id.kullaniciYonetimi).isVisible = true
                    menu.findItem(R.id.admin).isVisible = true
                }else{
                    menu.findItem(R.id.kullaniciYonetimi).isVisible = false
                    menu.findItem(R.id.admin).isVisible = false
                }
                if (user.profilePic != null){
                    UniversalImageLoader.setImage(user.profilePic!!,circle,headerProgress,"")
                }


            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun getFcmTokenForExistsUser() {
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            var token = it.token
            addToDatabaseNewToken(token)
        }
    }
    private fun addToDatabaseNewToken(token: String) {
        if (FirebaseAuth.getInstance().currentUser != null){
            FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("fcmToken")
                .setValue(token)
        }
    }

    private fun onlineControl() {
        db.child("users").child(mAuth.currentUser!!.uid).child("online").setValue(true)
    }

    override fun onDestroy() {
        Log.e("TAG","DESTRO FEED")

        super.onDestroy()


    }

    override fun onPause() {
        db.child("users").child(mAuth.currentUser!!.uid).child("online").setValue(false)
        super.onPause()
    }
    override fun onStop() {

        super.onStop()
        Log.e("TAG","stop  FEED")
    }

    override fun onStart() {
        Log.e("TAG","START FEED")
        db.child("users").child(mAuth.currentUser!!.uid).child("online").setValue(true)
        super.onStart()
    }

    override fun onResume() {
        Log.e("TAG","RESUME FEED")
        db.child("users").child(mAuth.currentUser!!.uid).child("online").setValue(true)
        super.onResume()
    }

    override fun onRestart() {
        Log.e("TAG","RESTART FEED")
        db.child("users").child(mAuth.currentUser!!.uid).child("online").setValue(true)
        super.onRestart()
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
                    Toast.makeText(this,"Lütfen 'Cimbomes' uygulamasının kişilerinize erişmesine izin verin! ",Toast.LENGTH_SHORT).show()
                }

            }
            R.id.home -> {
                if (contactPermissinVerildiMi && storagePermissonVerildiMi && storageYazmaPermissionVerildiMi){
                    setCurrentFragment(AnasayfaFragment())
                    toolbars.setTitle("CimbomES")
                }else{
                    Toast.makeText(this,"Lütfen 'Cimbomes' uygulamasının kişilerinize ve medyanıza erişmesine izin verin! ",Toast.LENGTH_SHORT).show()
                }

            }
            R.id.newGroup -> {
                if (contactPermissinVerildiMi && storagePermissonVerildiMi && storageYazmaPermissionVerildiMi){
                    setCurrentFragment(YeniGrupFragment())
                    toolbars.setTitle("Yeni Grup Oluştur")
                }else{
                    Toast.makeText(this,"Lütfen 'Cimbomes' uygulamasının kişilerinize ve medyanıza erişmesine izin verin! ",Toast.LENGTH_SHORT).show()
                }

            }
            R.id.savedMessages -> {
                if (contactPermissinVerildiMi && storagePermissonVerildiMi && storageYazmaPermissionVerildiMi){
                    setCurrentFragment(KaydedilenMesajlarFragment())
                    toolbars.setTitle("Kaydedilen Mesajlar")
                }else{
                    Toast.makeText(this,"Lütfen 'Cimbomes' uygulamasının kişilerinize ve medyanıza erişmesine izin verin! ",Toast.LENGTH_SHORT).show()
                }

            }
            R.id.settings -> {
                if (contactPermissinVerildiMi && storagePermissonVerildiMi && storageYazmaPermissionVerildiMi){
                    setCurrentFragment(AyarlarFragment())
                    toolbars.setTitle("Ayarlar")
                }else{
                    Toast.makeText(this,"Lütfen 'Cimbomes' uygulamasının kişilerinize ve medyanıza erişmesine izin verin! ",Toast.LENGTH_SHORT).show()
                }

            }
            R.id.logout ->{
                var dialog = SignOutFragment()
                dialog.show(supportFragmentManager, "cikisyapdialoggöster")
            }
            R.id.admin ->{
                setCurrentFragment(GrupYonetimiFragment())
                toolbars.setTitle("Grup Yönetimi")
            }
            R.id.kullaniciYonetimi->{
                setCurrentFragment(KullaniciYonetimiFragment())
                toolbars.setTitle("Kullanıcı Yönetimi")
            }
        }
        return true
    }
}