package com.telemechanic.consu.datamodel

data class RawMessageData(
    val category: String,
    val type: String,
    val resource: String,
    val url: String,
    val attachments: List<Attachment>,
    val entities: Entities,
    val moderation: Moderation
)

data class Attachment(
    var fileName: String,
    var fileExtension: String,
    var fileSize: Int,
    var fileMimeType: String,
    var fileUrl: String
)

data class Entities(
    val sender: EntityDetails,
    val receiver: EntityDetails
)

data class EntityDetails(
    val entity: User,
    val entityType: String
)

data class Moderation(
    val status: String
)
data class User(
    val uid: String,
    val name: String,
    val avatar: String?,
    val link: String?,
    val role: String,
    val metadata: Map<String, Any>?,
    val status: String,
    val statusMessage: String?,
    val lastActiveAt: Long,
    val hasBlockedMe: Boolean,
    val blockedByMe: Boolean,
    val tags: List<Any>?
)