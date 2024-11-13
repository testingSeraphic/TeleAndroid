package com.tele.android.viewmodel

import androidx.lifecycle.ViewModel
import com.amazonaws.services.chime.sdk.meetings.audiovideo.AudioVideoFacade
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.backgroundfilter.backgroundblur.BackgroundBlurVideoFrameProcessor
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.backgroundfilter.backgroundreplacement.BackgroundReplacementVideoFrameProcessor
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.capture.CameraCaptureSource
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.gl.DefaultEglCoreFactory
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.gl.EglCoreFactory
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSession
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionConfiguration
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionCredentials
import com.tele.android.utils.CpuVideoProcessor
import com.tele.android.utils.GpuVideoProcessor

class JoinMeetingModel : ViewModel() {
    lateinit var meetingSession: MeetingSession

    val credentials: MeetingSessionCredentials
        get() = meetingSession.configuration.credentials

    val configuration: MeetingSessionConfiguration
        get() = meetingSession.configuration

    val audioVideo: AudioVideoFacade
        get() = meetingSession.audioVideo

    // Graphics/capture related objects
    val eglCoreFactory: EglCoreFactory = DefaultEglCoreFactory()
    lateinit var cameraCaptureSource: CameraCaptureSource
    lateinit var gpuVideoProcessor: GpuVideoProcessor
    lateinit var cpuVideoProcessor: CpuVideoProcessor
    lateinit var backgroundBlurVideoFrameProcessor: BackgroundBlurVideoFrameProcessor
    lateinit var backgroundReplacementVideoFrameProcessor: BackgroundReplacementVideoFrameProcessor

    // Source for screen capture and share, will be set only if created in call
    //var screenShareManager: ScreenShareManager? = null

    // For use with replica promotions, null if not a replica meeting
    var primaryExternalMeetingId: String? = null
}
