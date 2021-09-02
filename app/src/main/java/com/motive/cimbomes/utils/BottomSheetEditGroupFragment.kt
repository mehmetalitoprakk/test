package com.motive.cimbomes.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.motive.cimbomes.R
import com.motive.cimbomes.activity.AddMemberGroupActivity
import com.motive.cimbomes.activity.FeedActivity
import com.motive.cimbomes.fragments.ProgressFragment
import kotlinx.android.synthetic.main.activity_user_info.*
import kotlinx.android.synthetic.main.fragment_bottom_sheet_edit_group.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class BottomSheetEditGroupFragment : BottomSheetDialogFragment() {
    lateinit var db : DatabaseReference
    var isCreator = false
    lateinit var groupKey : String
    lateinit var storage : StorageReference
    val RESIM_SEC = 1
    var newPhotoUri : Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bottom_sheet_edit_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance().reference

        db.child("groups").child(groupKey).child("creator").get().addOnSuccessListener {
            println(it.toString() + "  7777777")
            if (it.value == FirebaseAuth.getInstance().currentUser!!.uid){
                editGroupGrubuSil.visibility = View.VISIBLE
            }else{
                editGroupGrubuSil.visibility = View.GONE
            }
        }



        editGroupChangeName.setOnClickListener {
            showDialog()
        }



        editGroupChangePhoto.setOnClickListener {
                resimSec()
        }

        editGroupAddUser.setOnClickListener {
            kisiEkle()
        }


        editGroupGrubuSil.setOnClickListener {
            grubuSilDialog()
        }





    }

    private fun grubuSil() {
        grubuSilProgress.visibility = View.VISIBLE
        db.child("groups").child(groupKey).removeValue().addOnSuccessListener {
            db.child("grupkonusmalar").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.getValue() != null){
                        for (i in snapshot.children){
                            for (j in i.children){
                                if (j.key == groupKey){
                                    j.ref.removeValue()
                                }
                            }
                        }
                        grubuSilProgress.visibility = View.GONE
                        Toast.makeText(requireContext(),"Grup başarıyla silindi.",Toast.LENGTH_SHORT).show()
                        startActivity(Intent(requireContext(),FeedActivity::class.java))
                        requireActivity().finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
    }

    private fun kisiEkle() {
        val intent = Intent(requireContext(),AddMemberGroupActivity::class.java)
        intent.putExtra("editGroupKey",groupKey)
        startActivity(intent)

    }

    private fun resimSec() {
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_PICK)
        startActivityForResult(intent,RESIM_SEC)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESIM_SEC && resultCode == AppCompatActivity.RESULT_OK && data!!.data != null){
            if (data.data != null){
                newPhotoUri = data.data
                putPhotoFirebase()
            }
        }
    }

    private fun putPhotoFirebase() {
        val progressDialog = ProgressFragment()
        progressDialog.show(requireActivity().supportFragmentManager,"newPhoto")
        progressDialog.isCancelable = false
        storage.child("groups").child(groupKey).child("groupImage").putFile(newPhotoUri!!).addOnSuccessListener {
            storage.child("groups").child(groupKey).child("groupImage").downloadUrl.addOnSuccessListener {
                var imageUrl = it.toString()
                db.child("groups").child(groupKey).child("image").setValue(imageUrl).addOnSuccessListener {
                    db.child("grupkonusmalar").addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.getValue() != null){
                                for (i in snapshot.children){
                                    for (j in i.children){
                                        if (j.key == groupKey){
                                            j.ref.child("groupImage").setValue(imageUrl).addOnSuccessListener {

                                            }.addOnFailureListener {
                                                Toast.makeText(requireContext(),"Bir hata meydana geldi.",Toast.LENGTH_SHORT).show()
                                                progressDialog.dismiss()
                                            }
                                        }
                                    }
                                }
                                progressDialog.dismiss()
                                dismiss()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })
                }.addOnFailureListener {
                    Toast.makeText(requireContext(),"Bir hata meydana geldi.",Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                    dismiss()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(),"Bir hata meydana geldi.",Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
                dismiss()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(),"Bir hata meydana geldi.",Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
            dismiss()
        }
    }


    fun grubuSilDialog(){
        var builder = AlertDialog.Builder(requireContext())

        with(builder){
            setTitle("Grubu Silmek İstediğinize Emin Misiniz ?")
            setPositiveButton("Evet"){dialog,which ->
                grubuSil()
            }
            setNegativeButton("İptal"){ dialog, which ->
                dismiss()
            }
            show()
        }
    }


    private fun showDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayput = inflater.inflate(R.layout.change_groupname_layout,null)
        val edittext = dialogLayput.findViewById<EditText>(R.id.newGroupName)
        val progressBar = dialogLayput.findViewById<ProgressBar>(R.id.progressNewGroupName)

        with(builder){
            setTitle("Grup İsmini Değiştir")
            setPositiveButton("Tamam"){dialog,which ->
                if (edittext.text.toString().isNotEmpty() || edittext.text.toString().length > 3){
                    progressBar.visibility = View.VISIBLE
                    db.child("groups").child(groupKey).child("groupName").setValue(edittext.text.toString()).addOnSuccessListener {
                        db.child("grupkonusmalar").addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.getValue() != null){
                                    for (i in snapshot.children){
                                        println(i.key.toString())
                                        for (j in i.children){
                                            if (j.key == groupKey){
                                                j.ref.child("groupName").setValue(edittext.text.toString()).addOnSuccessListener {
                                                    dismiss()
                                                }.addOnFailureListener {
                                                    Toast.makeText(requireContext(),"Bir hata Meydana geldi",Toast.LENGTH_SHORT).show()
                                                    dismiss()
                                                }
                                            }
                                        }
                                    }
                                }

                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(requireContext(),"Bir hata Meydana geldi",Toast.LENGTH_SHORT).show()
                                dismiss()

                            }

                        })
                    }.addOnFailureListener {
                        progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(),"Bir hata meydana geldi",Toast.LENGTH_SHORT).show()
                    }
                }else{
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(),"Lütfen en az 3 harften oluşan grup ismi giriniz",Toast.LENGTH_SHORT).show()
                }

            }
            setNegativeButton("İptal"){ dialog, which ->
                dismiss()
            }
            setView(dialogLayput)
            show()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = true)
    internal fun onEditBilgiAl(bilgi : EventBusDataEvents.SendEditGroupSheet){
        groupKey = bilgi.groupKey
        isCreator = bilgi.isCreator

    }


}