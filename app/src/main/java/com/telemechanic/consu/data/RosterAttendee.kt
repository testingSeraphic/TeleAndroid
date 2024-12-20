package com.telemechanic.consu.data

import com.amazonaws.services.chime.sdk.meetings.audiovideo.SignalStrength
import com.amazonaws.services.chime.sdk.meetings.audiovideo.VolumeLevel
import com.amazonaws.services.chime.sdk.meetings.internal.AttendeeStatus

data class RosterAttendee(
    val attendeeId: String,
    val attendeeName: String,
    val volumeLevel: VolumeLevel = VolumeLevel.NotSpeaking,
    val signalStrength: SignalStrength = SignalStrength.High,
    val isActiveSpeaker: Boolean = false,
    val attendeeStatus: AttendeeStatus = AttendeeStatus.Joined
)
