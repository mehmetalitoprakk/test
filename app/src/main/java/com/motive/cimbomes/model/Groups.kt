package com.motive.cimbomes.model

class Groups {
    var groupID : String? = null
    var creator : String? = null
    var member : MutableList<GroupMembers>? = null
    var time : Long? = null
    var image : String? = null
    var groupName : String? = null
    var static : Boolean? = null
    var messages : Mesaj? = null

    constructor(){}
    constructor(
        groupID: String?,
        creator: String?,
        member: MutableList<GroupMembers>?,
        time: Long?,
        image: String?,
        groupName: String?,
        static: Boolean?,
        messages: Mesaj?
    ) {
        this.groupID = groupID
        this.creator = creator
        this.member = member
        this.time = time
        this.image = image
        this.groupName = groupName
        this.static = static
        this.messages = messages
    }

    override fun toString(): String {
        return "Groups(groupID=$groupID, creator=$creator, member=$member, time=$time, image=$image, groupName=$groupName, static=$static, messages=$messages)"
    }


}