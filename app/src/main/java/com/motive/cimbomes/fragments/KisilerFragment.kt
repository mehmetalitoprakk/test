package com.motive.cimbomes.fragments

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.motive.cimbomes.R
import com.motive.cimbomes.activity.ChatActivity
import com.motive.cimbomes.adapter.ContactAdapter
import com.motive.cimbomes.model.Contact
import com.motive.cimbomes.model.Users
import kotlinx.android.synthetic.main.fragment_kisiler.*
import java.util.ArrayList


class KisilerFragment : Fragment(),ContactAdapter.OnItemClickListener {
    private lateinit var adapter: ContactAdapter
    private lateinit var recyclerView: RecyclerView
    private var dataList = mutableListOf<Contact>()
    private lateinit var mAuth :FirebaseAuth
    private lateinit var db : DatabaseReference
    var list = arrayListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_kisiler, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        recyclerView = kisilerrecyclerview
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ContactAdapter(this,requireContext())





        val contacts = requireActivity().contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null)
        while (contacts!!.moveToNext()){
            val name = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val number = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            val obj = Contact()
            obj.name = name
            obj.number = number
            obj.kullaniyorMu = false
            list.add(number)
            dataList.add(obj)
        }
        recyclerView.adapter = adapter
        adapter.setDataList(dataList)
        dataList.clear()
        contacts.close()

        db.child("users").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.getValue() != null){
                    for (user in snapshot.children){
                        var kullanici = user.getValue(Users::class.java)
                        if (kullanici != null){
                            if (kullanici.telefonNo != null){
                                var number = kullanici!!.telefonNo!!.replace("\\s".toRegex(),"").replace("+9","")
                                for (i in list){
                                    if (i.replace("\\s".toRegex(),"").replace("+9","") == number){
                                        val contacts = Contact()
                                        if (kullanici!!.uid != mAuth.currentUser!!.uid){
                                            contacts.name = kullanici!!.isim + " " + kullanici.soyisim
                                            contacts.number = kullanici!!.telefonNo
                                            contacts.image = kullanici.profilePic
                                            contacts.uid = kullanici.uid
                                            contacts.kullaniyorMu = true
                                            dataList.add(contacts)
                                        }
                                    }else{

                                    }
                                }
                            }
                            adapter.setDataList(dataList)
                            }
                        }

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })



    }


    override fun onItemClick(position: Int) {
        val clickedItem = dataList[position]
        if (clickedItem.kullaniyorMu == true){
            var intent = Intent(requireContext(),ChatActivity::class.java)
            intent.putExtra("tiklananid",clickedItem.uid)
            intent.putExtra("tiklananisim",clickedItem.name)
            intent.putExtra("tiklananfoto",clickedItem.image)
            startActivity(intent)
        }
    }

}