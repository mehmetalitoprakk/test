package com.motive.cimbomes.model

class Groups {
    var groupID : String? = null
    var creator : String? = null
    var member : GroupMembers? = null
    var time : Long? = null
    var image : String? = null
    var creatorName : String? = null
    var static : Boolean? = null

    constructor(){}
    constructor(
        groupID: String?,
        creator: String?,
        member: GroupMembers?,
        time: Long?,
        image: String?,
        creatorName: String?,
        static: Boolean?
    ) {
        this.groupID = groupID
        this.creator = creator
        this.member = member
        this.time = time
        this.image = image
        this.creatorName = creatorName
        this.static = static
    }

    override fun toString(): String {
        return "Groups(groupID=$groupID, creator=$creator, member=$member, time=$time, image=$image, creatorName=$creatorName, static=$static)"
    }


}