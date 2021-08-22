package com.motive.cimbomes.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.motive.cimbomes.R
import com.motive.cimbomes.adapter.GroupKonusmaAdapter
import com.motive.cimbomes.adapter.KonusmalarAdapter
import com.motive.cimbomes.model.GroupKonusma
import com.motive.cimbomes.model.GroupMembers
import com.motive.cimbomes.model.Groups
import com.motive.cimbomes.model.Konusma
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.fragment_anasayfa.*


class AnasayfaFragment : Fragment() {
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
        gruplarTV.visibility = View.GONE
        tvSohbetler.visibility = View.GONE


        setupGrupKonusmaRecyclerView()

        setupKonusmalarRecyclerView()


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
            gruplarTV.visibility = View.VISIBLE
            grupKonusmalar.add(0,eklenecekGrupKonusma!!)
            grupAdapter.notifyItemInserted(0)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            var kontrol = grupKonusmaPositionBul(snapshot.key.toString())
            if (kontrol != -1){
                var guncellenecekGrupKonusma = snapshot.getValue(GroupKonusma::class.java)
                guncellenecekGrupKonusma!!.groupID = snapshot.key
                grupKonusmalar.removeAt(kontrol)
                grupAdapter.notifyItemRemoved(kontrol)
                grupKonusmalar.add(0,guncellenecekGrupKonusma)
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
            tvSohbetler.visibility = View.VISIBLE
            tumKonusmalar.add(0,eklenecekKonusma!!)
            adapter.notifyItemInserted(0)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            // kontrol - 1 ise yeni bir konusma eklenecek, -1den fazla ise var olan konuşma güncellenecek(arraylistposition)
            var kontrol = konusmaPositionBul(snapshot.key.toString())
            if (kontrol != -1){
                var guncellenecekKonusma = snapshot.getValue(Konusma::class.java)
                guncellenecekKonusma!!.user_id = snapshot.key
                tumKonusmalar.removeAt(kontrol)
                adapter.notifyItemRemoved(kontrol)
                tumKonusmalar.add(0,guncellenecekKonusma)
                adapter.notifyItemInserted(0)
            }
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {

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
        grupAdapter = GroupKonusmaAdapter(grupKonusmalar,requireContext())

        groupsRecyclerView.layoutManager = grupLinearLayoutManager
        groupsRecyclerView.adapter = grupAdapter

        grupKonusmalariGetir()

    }




    private fun setupKonusmalarRecyclerView() {
        recyclerView = chatsRV
        linearLayoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        adapter = KonusmalarAdapter(tumKonusmalar,requireContext())

        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter

        tumKonusmalariGetir()

    }

}