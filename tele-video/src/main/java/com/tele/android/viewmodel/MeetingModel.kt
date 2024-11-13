package com.tele.android.viewmodel

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.lifecycle.ViewModel
import com.amazonaws.services.chime.sdk.meetings.audiovideo.AttendeeInfo
import com.amazonaws.services.chime.sdk.meetings.audiovideo.AudioVideoFacade
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.RemoteVideoSource
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.VideoPriority
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.VideoResolution
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.VideoSubscriptionConfiguration
import com.amazonaws.services.chime.sdk.meetings.device.MediaDevice
import com.tele.android.JoinMeeting
import com.tele.android.data.Caption
import com.tele.android.data.Message
import com.tele.android.data.MetricData
import com.tele.android.data.RosterAttendee
import com.tele.android.data.VideoCollectionTile
import com.tele.android.utils.isContentShare
import kotlin.math.ceil
import kotlin.math.min

class MeetingModel : ViewModel() {
    val localTileId = 0
    private val videoTileCountPerPage = 4

    val currentMetrics = mutableMapOf<String, MetricData>()
    val currentRoster = mutableMapOf<String, RosterAttendee>()
    var localVideoTileState: VideoCollectionTile? = null

    private val remoteVideoTileStates = mutableListOf<VideoCollectionTile>()

    private val remoteVideoSourceConfigurations =
        mutableMapOf<RemoteVideoSource, VideoSubscriptionConfiguration>()

    private val contentShareRemoteVideoSourceConfigurations =
        mutableMapOf<RemoteVideoSource, VideoSubscriptionConfiguration>()

    private val pendingVideoSourceConfigurations =
        mutableMapOf<RemoteVideoSource, VideoSubscriptionConfiguration>()

    private val videoSourcesToBeSubscribed =
        mutableMapOf<RemoteVideoSource, VideoSubscriptionConfiguration>()

    private val videoSourcesToBeUnsubscribed = mutableSetOf<RemoteVideoSource>()

    // contains local video tile
    val videoStatesInCurrentPage = mutableListOf<VideoCollectionTile>()
    val userPausedVideoTileIds = mutableSetOf<Int>()
    val currentScreenTiles = mutableListOf<VideoCollectionTile>()
    var currentVideoPageIndex = 0
    var currentMediaDevices = listOf<MediaDevice>()
    var currentMessages = mutableListOf<Message>()
    val currentCaptions = mutableListOf<Caption>()
    val currentCaptionIndices = mutableMapOf<String, Int>()

    var isMuted = false
    var isCameraOn = false
    var isDeviceListDialogOn = false
    var isAdditionalOptionsDialogOn = false
    var isSharingContent = false
    var isLiveTranscriptionEnabled = false
    var lastReceivedMessageTimestamp = 0L
    var tabIndex = 0
    var isUsingCameraCaptureSource = true
    var isLocalVideoStarted = false
    var wasLocalVideoStarted = false
    var isUsingGpuVideoProcessor = false
    var isUsingCpuVideoProcessor = false
    var isUsingBackgroundBlur = false
    var isUsingBackgroundReplacement = false
    var localVideoMaxBitRateKbps = 0
    var isCameraSendAvailable = false
    var isMicrophoneServiceBound = false
    val microphoneServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            isMicrophoneServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isMicrophoneServiceBound = false
        }
    }
    var isScreenShareServiceBound = false
    var screenShareServiceConnection: ServiceConnection? = null

    fun getRemoteVideoTileStates(): List<VideoCollectionTile> {
        return remoteVideoTileStates
    }

    // Add VideoTileState to remoteVideoTileStates, if there is matching video source
    // in pendingVideoSourceConfigurations, move it to remoteVideoSourceConfigurations
    fun addRemoteVideoTileState(state: VideoCollectionTile) {
        remoteVideoTileStates.add(state)

        // Move matching RemoteVideSource from pendingVideoSourceConfigurations
        // to remoteVideoSourceConfigurations
        val attendeeId = state.videoTileState.attendeeId
        val movingVideoSources = pendingVideoSourceConfigurations
            .filter { it.key.attendeeId == attendeeId }
        movingVideoSources.forEach { pendingVideoSourceConfigurations.remove(it.key) }
        remoteVideoSourceConfigurations.putAll(movingVideoSources)
    }

    fun removeRemoteVideoTileState(tileId: Int) {
        remoteVideoTileStates.removeAll { it.videoTileState.tileId == tileId }
    }

    fun updateVideoStatesInCurrentPage() {
        videoStatesInCurrentPage.clear()

        if (localVideoTileState != null) {
            videoStatesInCurrentPage.add(localVideoTileState!!)
        }
        val remoteVideoTileCountPerPage =
            if (localVideoTileState == null) videoTileCountPerPage else (videoTileCountPerPage - 1)
        val remoteVideoStartIndex = currentVideoPageIndex * remoteVideoTileCountPerPage
        val remoteVideoEndIndex =
            min(remoteVideoTileStates.size, remoteVideoStartIndex + remoteVideoTileCountPerPage) - 1
        if (remoteVideoStartIndex <= remoteVideoEndIndex) {
            videoStatesInCurrentPage.addAll(remoteVideoTileStates.slice(remoteVideoStartIndex..remoteVideoEndIndex))
        }
    }

    fun getRemoteVideoSourceConfigurations(): Map<RemoteVideoSource, VideoSubscriptionConfiguration> {
        return remoteVideoSourceConfigurations
    }

    fun setRemoteVideoSourceConfigurations(source: RemoteVideoSource, config: VideoSubscriptionConfiguration) {
        remoteVideoSourceConfigurations[source] = config
    }

    fun updateRemoteVideoStatesBasedOnActiveSpeakers(activeSpeakers: Array<AttendeeInfo>) {
        val activeSpeakerIds = activeSpeakers.map { it.attendeeId }.toHashSet()
        remoteVideoTileStates.sortWith(Comparator<VideoCollectionTile> { lhs, rhs ->
            val lhsIsActiveSpeaker = activeSpeakerIds.contains(lhs.videoTileState.attendeeId)
            val rhsIsActiveSpeaker = activeSpeakerIds.contains(rhs.videoTileState.attendeeId)

            when {
                lhsIsActiveSpeaker && !rhsIsActiveSpeaker -> -1
                !lhsIsActiveSpeaker && rhsIsActiveSpeaker -> 1
                else -> 0
            }
        })
    }

    fun remoteVideoCountInCurrentPage(): Int {
        return videoStatesInCurrentPage.filter { !it.videoTileState.isLocalTile }.size
    }

    fun canGoToPrevVideoPage(): Boolean {
        return currentVideoPageIndex > 0
    }

    fun canGoToNextVideoPage(): Boolean {
        val remoteVideoTileCountPerPage =
            if (localVideoTileState == null) videoTileCountPerPage else (videoTileCountPerPage - 1)
        val maxVideoPageIndex =
            ceil(remoteVideoTileStates.size.toDouble() / remoteVideoTileCountPerPage).toInt() - 1
        return currentVideoPageIndex < maxVideoPageIndex
    }

    fun addVideoSource(source: RemoteVideoSource, config: VideoSubscriptionConfiguration) {
        if (source.isContentShare()) {
            contentShareRemoteVideoSourceConfigurations[source] = config
        } else {
            if (remoteVideoSourceConfigurations[source] == null) {
                pendingVideoSourceConfigurations[source] = config
            } else {
                remoteVideoSourceConfigurations[source] = config
            }
        }
    }

    fun removeVideoSource(source: RemoteVideoSource) {
        remoteVideoSourceConfigurations.remove(source)
        pendingVideoSourceConfigurations.remove(source)
        contentShareRemoteVideoSourceConfigurations.remove(source)
    }

    // A helper function for retrieving remote video sources in current page. It will retrieve
    // video sources in remoteVideoSourceConfigurations based on videoStatesInCurrentPage, if
    // videoStatesInCurrentPage.count() is smaller than videoTileCountPerPage and
    // pendingVideoSourceConfigurations is not empty, it will retrieve
    // (videoTileCountPerPage - videoStatesInCurrentPage.size) items from
    // pendingVideoSourceConfigurations, and add to result
    fun getRemoteVideoSourcesInCurrentPage(): Map<RemoteVideoSource, VideoSubscriptionConfiguration> {
        val result = mutableMapOf<RemoteVideoSource, VideoSubscriptionConfiguration>()
        // remote video sources in current page
        result.putAll(getRemoteVideoSources(videoStatesInCurrentPage))

        var numVideoSourcesNeededFromPending = videoTileCountPerPage - videoStatesInCurrentPage.size
        for (pendingVideoSourceEntry in pendingVideoSourceConfigurations.entries.iterator()) {
            if (numVideoSourcesNeededFromPending == 0) {
                break
            }
            result[pendingVideoSourceEntry.key] = pendingVideoSourceEntry.value
            numVideoSourcesNeededFromPending--
        }
        return result
    }

    // A helper function for retrieving all remote video sources from
    // remoteVideoSourceConfigurations which are not in current page
    private fun remoteVideoSourcesNotInCurrentPage(): Set<RemoteVideoSource> {
        val keysToRemove = getRemoteVideoSourcesInCurrentPage().keys
        return remoteVideoSourceConfigurations.minus(keysToRemove).keys
    }

//    fun updateRemoteVideoSourceSelection() {
//        when (tabIndex) {
//            JoinMeeting.SubTab.Video.position -> {
//                // If showing video screen, only subscribe the video sources on current video
//                // page index, unsubscribe the rest
//                videoSourcesToBeSubscribed.putAll(getRemoteVideoSourcesInCurrentPage())
//                videoSourcesToBeUnsubscribed.addAll(remoteVideoSourcesNotInCurrentPage())
//                videoSourcesToBeUnsubscribed.addAll(contentShareRemoteVideoSourceConfigurations.keys)
//            }
//
//            else -> {
//                videoSourcesToBeUnsubscribed.addAll(remoteVideoSourceConfigurations.keys)
//                videoSourcesToBeUnsubscribed.addAll(contentShareRemoteVideoSourceConfigurations.keys)
//            }
//        }
//    }

    fun updateRemoteVideoSourceSubscription(audioVideo: AudioVideoFacade) {
        if (videoSourcesToBeSubscribed.isEmpty() && videoSourcesToBeUnsubscribed.isEmpty()) {
            return
        }
        audioVideo.updateVideoSourceSubscriptions(videoSourcesToBeSubscribed,
            videoSourcesToBeUnsubscribed.toTypedArray())
        videoSourcesToBeSubscribed.clear()
        videoSourcesToBeUnsubscribed.clear()
    }


    // A helper function for fetching remote video sources/config from
    // remoteVideoSourceConfigurations based on attendee ID, given a list of video tiles,
    private fun getRemoteVideoSources(videoTiles: List<VideoCollectionTile>):
            Map<RemoteVideoSource, VideoSubscriptionConfiguration> {
        val attendeeIds = videoTiles.map { it.videoTileState.attendeeId }.toHashSet()
        return remoteVideoSourceConfigurations.filter { attendeeIds.contains(it.key.attendeeId) }
    }
}
