package com.motive.cimbomes.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.motive.cimbomes.R
import com.motive.cimbomes.adapter.GrupYonetimiAdapter
import com.motive.cimbomes.model.GroupMembers
import com.motive.cimbomes.model.Groups
import kotlinx.android.synthetic.main.fragment_grup_yonetimi.*


class GrupYonetimiFragment : Fragment() {
    lateinit var db : DatabaseReference
    lateinit var recyclerView: RecyclerView
    lateinit var mAdapter : GrupYonetimiAdapter
    var staticGroups = arrayListOf<Groups>()
    var adminSayisi = arrayListOf<String>()
    var üyeSayisi = arrayListOf<String>()


    var adminListe = arrayListOf<GroupMembers>()
    var üyeListe = arrayListOf<GroupMembers>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_grup_yonetimi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseDatabase.getInstance().reference


        setupRecylerView()

        db.child("groups").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.getValue() != null){
                    staticGroups.clear()
                    adminSayisi.clear()
                    adminListe.clear()
                    üyeListe.clear()
                    for (i in snapshot.children){
                        adminListe.clear()
                        val grup = i.getValue(Groups::class.java)
                        if (grup != null) {
                            if (grup!!.static == true){
                                staticGroups.add(grup)
                                i.ref.child("member").addValueEventListener(object : ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.getValue() != null){
                                            adminListe.clear()
                                            üyeListe.clear()
                                            for (i in snapshot.children){
                                                var member = i.getValue(GroupMembers::class.java)
                                                if (member != null){
                                                    üyeListe.add(member)
                                                    if (member.groupAdmin != null){
                                                        if (member.groupAdmin!!){
                                                            adminListe.add(member)
                                                        }
                                                    }
                                                }
                                            }
                                            adminSayisi.add(adminListe.size.toString())
                                            üyeSayisi.add(üyeListe.size.toString())
                                            mAdapter.notifyDataSetChanged()
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {

                                    }

                                })
                            }
                        }
                    }





                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun setupRecylerView() {
        recyclerView = recyclerViewKaliciGruplar
        mAdapter = GrupYonetimiAdapter(staticGroups,adminSayisi,requireContext(),üyeSayisi)

        recyclerView.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        recyclerView.adapter = mAdapter

    }


}