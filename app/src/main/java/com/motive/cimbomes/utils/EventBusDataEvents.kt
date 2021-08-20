package com.motive.cimbomes.utils

import com.motive.cimbomes.model.GroupMembers

class EventBusDataEvents {
    internal class PhoneCodeActivityGonder(var telNo: String?,var verificationID: String?,var code:String?,var name:String?,var surname: String?)
    internal class SendGroupData(var groupName: String,var groupImageUri : String?)
    internal class SendGroupMembers(var members : MutableList<GroupMembers>)
    internal class SendGroupInfo(var members : MutableList<GroupMembers>)
    internal class SendBottomSheet(var groupMember : GroupMembers,var groupKey: String, var position : Int)
}