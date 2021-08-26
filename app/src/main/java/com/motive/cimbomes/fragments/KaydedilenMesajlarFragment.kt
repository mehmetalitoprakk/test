package com.motive.cimbomes.fragments

import android.content.Context
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
import com.motive.cimbomes.adapter.SavedMessagesAdapter
import com.motive.cimbomes.model.Mesaj
import com.motive.cimbomes.utils.EventBusDataEvents
import kotlinx.android.synthetic.main.fragment_kaydedilen_mesajlar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class KaydedilenMesajlarFragment : Fragment() {
    lateinit var db : DatabaseReference
    var savedMessages = arrayListOf<Mesaj>()
    lateinit var mRecyclerView: RecyclerView
    lateinit var mAdaoter : SavedMessagesAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_kaydedilen_mesajlar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseDatabase.getInstance().reference
        setupRecyclerView()



        db.child("savedMessages").child(FirebaseAuth.getInstance().currentUser!!.uid).orderByChild("time").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.getValue() != null){
                    savedMessages.clear()
                    for (i in snapshot.children){
                        var mesaj = i.getValue(Mesaj::class.java)
                        savedMessages.add(mesaj!!)
                    }
                    println(savedMessages)
                    mAdaoter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun setupRecyclerView() {
        var myLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
        mRecyclerView = recyclerViewKaydedilenMesajlar
        myLayoutManager.reverseLayout = true
        myLayoutManager.stackFromEnd = true
        mRecyclerView.layoutManager = myLayoutManager


        mAdaoter = SavedMessagesAdapter(savedMessages)
        mRecyclerView.adapter = mAdaoter
    }


}