package com.motive.cimbomes.model

class GroupKonusma {
    var goruldu : Boolean? = null
    var son_mesaj : String? = null
    var time : Long? = null
    var gonderenID : String? = null
    var groupImage : String? = null

    constructor(){}
    constructor(
        goruldu: Boolean?,
        son_mesaj: String?,
        time: Long?,
        user_id: String?,
        groupImage: String?
    ) {
        this.goruldu = goruldu
        this.son_mesaj = son_mesaj
        this.time = time
        this.gonderenID = user_id
        this.groupImage = groupImage
    }

    override fun toString(): String {
        return "GroupKonusma(goruldu=$goruldu, son_mesaj=$son_mesaj, time=$time, user_id=$gonderenID, groupImage=$groupImage)"
    }
}