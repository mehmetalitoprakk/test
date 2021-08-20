package com.motive.cimbomes.utils

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.motive.cimbomes.R
import com.motive.cimbomes.model.GroupMembers
import kotlinx.android.synthetic.main.fragment_bottom_dialog.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class BottomSheetFragment : BottomSheetDialogFragment() {
    lateinit var groupmember : GroupMembers
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_dialog,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomsheetbilgi.setOnClickListener {
            //
        }

        bottomsheetgruptancıkar.setOnClickListener {

        }

        bottomsheetisim.text = groupmember.name + " " + groupmember.surname

        bottomsheetyönetici.setOnClickListener {
            // yönetici yapılıcak
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
    internal fun onMemberAl(member : EventBusDataEvents.SendBottomSheet){
        groupmember = member.groupMember
    }

}