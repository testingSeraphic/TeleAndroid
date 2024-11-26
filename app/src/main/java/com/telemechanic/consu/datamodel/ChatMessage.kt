package com.telemechanic.consu.datamodel

data class ChatMessage(
    val message: String,
    val isReceiver: Boolean, // true if the message is sent by the user
    val isRead: Boolean,
    val messageId: Int,
)