package com.motive.cimbomes.model

class GroupKonusma {
    var goruldu : Boolean? = null
    var son_mesaj : String? = null
    var time : Long? = null
    var gonderenID : String? = null
    var groupImage : String? = null
    var groupID : String? = null
    var groupName : String? = null
    var sessiz : Boolean? = null
    constructor(){}
    constructor(
        goruldu: Boolean?,
        son_mesaj: String?,
        time: Long?,
        gonderenID: String?,
        groupImage: String?,
        groupID: String?,
        groupName: String?,
        sessiz: Boolean?
    ) {
        this.goruldu = goruldu
        this.son_mesaj = son_mesaj
        this.time = time
        this.gonderenID = gonderenID
        this.groupImage = groupImage
        this.groupID = groupID
        this.groupName = groupName
        this.sessiz = sessiz
    }

    override fun toString(): String {
        return "GroupKonusma(goruldu=$goruldu, son_mesaj=$son_mesaj, time=$time, gonderenID=$gonderenID, groupImage=$groupImage, groupID=$groupID, groupName=$groupName, sessiz=$sessiz)"
    }


}