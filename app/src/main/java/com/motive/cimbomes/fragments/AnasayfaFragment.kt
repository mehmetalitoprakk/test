package com.motive.cimbomes.fragments

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.motive.cimbomes.R
import com.motive.cimbomes.adapter.GroupKonusmaAdapter
import com.motive.cimbomes.adapter.KonusmalarAdapter
import com.motive.cimbomes.model.GroupKonusma
import com.motive.cimbomes.model.Konusma
import com.motive.cimbomes.utils.BottomSheetKonusmalar
import com.motive.cimbomes.utils.EventBusDataEvents
import kotlinx.android.synthetic.main.fragment_anasayfa.*
import org.greenrobot.eventbus.EventBus


class AnasayfaFragment : Fragment(), KonusmalarAdapter.OnItemLongClickListener {
    private lateinit var mAuth : FirebaseAuth
    private lateinit var db : DatabaseReference
    var tumKonusmalar = ArrayList<Konusma>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var groupsRecyclerView: RecyclerView
    var grupKonusmalar = ArrayList<GroupKonusma>()
    private lateinit var grupAdapter : GroupKonusmaAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var grupLinearLayoutManager: LinearLayoutManager
    private lateinit var adapter : KonusmalarAdapter
    var listenerAtandiMi = false
    private lateinit var groupDb : DatabaseReference
    var grupListenerAtandiMi = false
    var kullanicinOlduguGruplar = arrayListOf<String>()
    var konusmalarKopy = arrayListOf<Konusma>()
    var grupKonusmalarKopy = arrayListOf<GroupKonusma>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_anasayfa, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        staticView.visibility = View.VISIBLE
        grupTiklandi()


        gruplarTV.setOnClickListener {

            grupTiklandi()

        }

        sohbetlerTV.setOnClickListener {
            sohbetlerTiklandı()

        }





        setupGrupKonusmaRecyclerView()

        setupKonusmalarRecyclerView()


    }
    private fun grupTiklandi(){
        gruplarTV.setTypeface(Typeface.DEFAULT_BOLD)
        gruplarView.visibility = View.VISIBLE
        sohbetlerTV.setTypeface(Typeface.DEFAULT)
        sohbetlerView.visibility = View.GONE
        staricGroupsRV.visibility = View.VISIBLE
        chatsRV.visibility = View.INVISIBLE

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                grupAdapter.filter(query!!)

                return false

            }

            override fun onQueryTextChange(newText: String?): Boolean {
                grupAdapter.filter(newText!!)
                return false
            }

        })
    }

    private fun sohbetlerTiklandı(){
        gruplarTV.setTypeface(Typeface.DEFAULT)
        gruplarView.visibility = View.GONE
        sohbetlerTV.setTypeface(Typeface.DEFAULT_BOLD)
        sohbetlerView.visibility =View.VISIBLE
        chatsRV.visibility = View.VISIBLE
        staricGroupsRV.visibility = View.INVISIBLE

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                adapter.filter(query!!)

                return false

            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText!!)
                return false
            }

        })
    }

    private fun grupKonusmalariGetir() {
        groupDb = FirebaseDatabase.getInstance().reference.child("grupkonusmalar").child(mAuth.currentUser!!.uid)
        if (grupListenerAtandiMi == false){
            grupListenerAtandiMi = true
            groupDb.orderByChild("time").addChildEventListener(myGroupListener)
        }

    }




    private fun tumKonusmalariGetir() {
        val myId = mAuth.currentUser!!.uid
        db = FirebaseDatabase.getInstance().reference.child("konusmalar").child(myId)
        if (listenerAtandiMi == false){
            listenerAtandiMi = true
            db.orderByChild("time").addChildEventListener(myListener)

        }


    }



    private var myGroupListener = object : ChildEventListener{
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            var eklenecekGrupKonusma = snapshot.getValue(GroupKonusma::class.java)
            grupKonusmalar.add(0,eklenecekGrupKonusma!!)
            grupKonusmalarKopy.add(0,eklenecekGrupKonusma)
            grupAdapter.notifyItemInserted(0)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            var kontrol = grupKonusmaPositionBul(snapshot.key.toString())
            if (kontrol != -1){
                var guncellenecekGrupKonusma = snapshot.getValue(GroupKonusma::class.java)
                guncellenecekGrupKonusma!!.groupID = snapshot.key
                grupKonusmalar.removeAt(kontrol)
                grupKonusmalarKopy.removeAt(kontrol)
                grupAdapter.notifyItemRemoved(kontrol)
                grupKonusmalar.add(0,guncellenecekGrupKonusma)
                grupKonusmalarKopy.add(0,guncellenecekGrupKonusma)
                grupAdapter.notifyItemInserted(0)
            }
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {

        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

        }

        override fun onCancelled(error: DatabaseError) {

        }


    }



    private var myListener = object : ChildEventListener{
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            var eklenecekKonusma = snapshot.getValue(Konusma::class.java)
            eklenecekKonusma!!.user_id = snapshot.key
            tumKonusmalar.add(0,eklenecekKonusma!!)
            konusmalarKopy.add(0,eklenecekKonusma)
            adapter.notifyItemInserted(0)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            // kontrol - 1 ise yeni bir konusma eklenecek, -1den fazla ise var olan konuşma güncellenecek(arraylistposition)
            var kontrol = konusmaPositionBul(snapshot.key.toString())
            if (kontrol != -1){
                var guncellenecekKonusma = snapshot.getValue(Konusma::class.java)
                guncellenecekKonusma!!.user_id = snapshot.key
                tumKonusmalar.removeAt(kontrol)
                konusmalarKopy.removeAt(kontrol)
                adapter.notifyItemRemoved(kontrol)
                tumKonusmalar.add(0,guncellenecekKonusma)
                konusmalarKopy.add(0,guncellenecekKonusma)
                adapter.notifyItemInserted(0)
            }
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            var kontrol = konusmaPositionBul(snapshot.key.toString())
            if (kontrol != -1){
                var silinecekKonusma = snapshot.getValue(Konusma::class.java)
                silinecekKonusma!!.user_id = snapshot.key
                tumKonusmalar.removeAt(kontrol)
                konusmalarKopy.removeAt(kontrol)
                adapter.notifyItemRemoved(kontrol)
            }
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

        }

        override fun onCancelled(error: DatabaseError) {

        }

    }
    private fun grupKonusmaPositionBul(id: String): Int {
        for (i in 0..grupKonusmalar.size-1){
            var gecici = grupKonusmalar.get(i)
            if (gecici.groupID.equals(id)){
                return i
            }
        }
        return -1
    }

    private fun konusmaPositionBul(userID :String) : Int{
        for (i in 0..tumKonusmalar.size-1){
            var gecici = tumKonusmalar.get(i)
            if (gecici.user_id.equals(userID)){
                return i
            }
        }
        return -1
    }

    override fun onPause() {
        super.onPause()
        tumKonusmalar.clear()
        grupKonusmalar.clear()
        if (listenerAtandiMi == true){
            listenerAtandiMi = false
            db.removeEventListener(myListener)
        }
        if(grupListenerAtandiMi == true){
            grupListenerAtandiMi = false
            groupDb.removeEventListener(myGroupListener)
        }

    }

    override fun onResume() {
        super.onResume()
        tumKonusmalar.clear()
        grupKonusmalar.clear()
        if (listenerAtandiMi == false){
            listenerAtandiMi = true
            adapter.notifyDataSetChanged()
            db.orderByChild("time").addChildEventListener(myListener)
        }
        if (grupListenerAtandiMi == false){
            grupListenerAtandiMi = true
            grupAdapter.notifyDataSetChanged()
            groupDb.orderByChild("time").addChildEventListener(myGroupListener)

        }
    }
    private fun setupGrupKonusmaRecyclerView() {
        groupsRecyclerView = staricGroupsRV
        grupLinearLayoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        grupAdapter = GroupKonusmaAdapter(grupKonusmalar,grupKonusmalarKopy,requireContext())


        groupsRecyclerView.layoutManager = grupLinearLayoutManager
        groupsRecyclerView.adapter = grupAdapter

        grupKonusmalariGetir()

    }




    private fun setupKonusmalarRecyclerView() {
        recyclerView = chatsRV
        linearLayoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        adapter = KonusmalarAdapter(tumKonusmalar,konusmalarKopy,requireContext(),this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter

        tumKonusmalariGetir()

    }

    override fun onItemLongClicked(position: Int): Boolean {
        val v = (requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(
                VibrationEffect.createOneShot(100,
                    VibrationEffect.DEFAULT_AMPLITUDE))
        }
        else {
            v.vibrate(100)
        }
        var clickedChat = tumKonusmalar[position]
        var uid = clickedChat.user_id
        if (uid != null){
            EventBus.getDefault().postSticky(EventBusDataEvents.SendChatInfoOne(uid))
            val dialog = BottomSheetKonusmalar()
            dialog.show(requireActivity().supportFragmentManager,"konsumalarbottomsheet")
        }




        return false
    }



}