package com.motive.cimbomes.utils

class EventBusDataEvents {
    internal class PhoneCodeActivityGonder(var telNo: String?,var verificationID: String?,var code:String?,var name:String?,var surname: String?)
    internal class SendGroupData(var groupName: String,var groupImageUri : String?)
}