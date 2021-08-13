package com.motive.cimbomes.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.StorageReference
import com.motive.cimbomes.R
import kotlinx.android.synthetic.main.fragment_yeni_grup.*


class YeniGrupFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db : DatabaseReference
    private lateinit var storageReference: StorageReference
    private val RESIM_SEC = 100
    private var profilePhotoUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("KONTROL","ON CREATE")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_yeni_grup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        newGroupMainContainer.visibility = View.VISIBLE




        btnNewGroup.setOnClickListener {
            val groupNameET = etNewGroupName.text.toString().trim()
            if (groupNameET.isNullOrEmpty() || groupNameET.length < 3){
                etNewGroupName.setError("Grup ismi 3 harften fazla olmalıdır.")
                etNewGroupName.requestFocus()
            }else{
                if (profilePhotoUri == null){
                    Toast.makeText(requireContext(),"Grup Fotoğrafı Seçiniz",Toast.LENGTH_SHORT).show()
                }else{
                    var bundle = Bundle()
                    bundle.putString("GroupName",groupNameET)
                    bundle.putString("GroupImageUri",profilePhotoUri.toString())
                    var memberFragment = SelectGroupMemberFragment()
                    memberFragment.arguments = bundle
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.containerFragmnet,SelectGroupMemberFragment())
                        commit()
                        newGroupMainContainer.visibility = View.GONE
                    }




                }
            }
        }


        newGroupAddPhotoImg.setOnClickListener {
            resimSec()
        }

        tvNewPhotoGroup.setOnClickListener {
            resimSec()
        }






    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESIM_SEC && resultCode == AppCompatActivity.RESULT_OK && data!!.data != null){
            profilePhotoUri = data.data
            newGroupProfilePicture.setImageURI(profilePhotoUri)
            newGroupAddPhotoImg.visibility = View.GONE
        }
    }

    private fun resimSec() {
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_PICK)
        startActivityForResult(intent,RESIM_SEC)
    }



}