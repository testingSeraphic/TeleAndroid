package com.tele.android.data

import com.amazonaws.services.chime.sdk.meetings.audiovideo.TranscriptItem

data class Caption(
    val speakerName: String?,
    val isPartial: Boolean,
    val content: String,
    val items: Array<TranscriptItem>? = null,
    val entities: MutableSet<String>? = null
)
