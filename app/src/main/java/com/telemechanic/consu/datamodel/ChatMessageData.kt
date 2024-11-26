package com.telemechanic.consu.datamodel

data class ChatMessageData (
    var id: Int?=null,
    var text: String?=null, // true if the message is sent by the user
    var sentAt: Long?=null,
    var type: String?=null,
    var senderUid: String?=null,
    var receiverUid: String?=null,
    var attachment: Attachment?=null,
    var rawMessage:RawMessageData?=null
)