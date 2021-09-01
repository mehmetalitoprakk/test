package com.motive.cimbomes.utils

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.motive.cimbomes.R
import com.motive.cimbomes.activity.RegisterActivity


class SignOutFragment : DialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alert = AlertDialog.Builder(requireActivity())
            .setTitle("Çıkış")
            .setMessage("Çıkış yapmak istediğinizden emin misiniz?(Tekrar SMS onayı gerektirir)")
            .setPositiveButton("Çıkış Yap",object : DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(
                        Intent(requireActivity(),RegisterActivity::class.java).addFlags(
                            Intent.FLAG_ACTIVITY_NO_ANIMATION))
                    requireActivity().finish()
                }

            })
            .setNegativeButton("İptal",object : DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dismiss()
                }

            }).create()
        return alert
    }


}