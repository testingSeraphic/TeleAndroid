package com.telemechanic.consu

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.amazonaws.services.chime.sdk.meetings.audiovideo.AudioVideoConfiguration
import com.amazonaws.services.chime.sdk.meetings.audiovideo.audio.AudioDeviceCapabilities
import com.amazonaws.services.chime.sdk.meetings.audiovideo.audio.AudioMode
import com.amazonaws.services.chime.sdk.meetings.internal.utils.DefaultBackOffRetry
import com.amazonaws.services.chime.sdk.meetings.internal.utils.HttpUtils
import com.amazonaws.services.chime.sdk.meetings.utils.logger.ConsoleLogger
import com.amazonaws.services.chime.sdk.meetings.utils.logger.LogLevel
import com.cometchat.chat.core.AppSettings.AppSettingsBuilder
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.User
import com.telemechanic.consu.databinding.ActivityJoinMeetingBinding
import com.telemechanic.consu.utils.AppConstants
import com.telemechanic.consu.utils.encodeURLParam
import com.telemechanic.consu.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL


class JoinMeeting : AppCompatActivity() {
    private val logger = ConsoleLogger(LogLevel.INFO)
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private val MEETING_REGION = "us-east-1"
    private val WEBRTC_PERMISSION_REQUEST_CODE = 1
    private var meetingID: String? = null
    private var yourName: String? = null
    private var testUrl: String = ""
    private lateinit var binding: ActivityJoinMeetingBinding
    private lateinit var audioVideoConfig: AudioVideoConfiguration

    companion object {
        const val MEETING_RESPONSE_KEY = "MEETING_RESPONSE"
        const val MEETING_ID_KEY = "MEETING_ID"
        const val NAME_KEY = "NAME"
        const val MEETING_ENDPOINT_KEY = "MEETING_ENDPOINT_URL"
        const val AUDIO_MODE_KEY = "AUDIO_MODE"
        const val AUDIO_DEVICE_CAPABILITIES_KEY = "AUDIO_DEVICE_CAPABILITIES"
        const val ENABLE_AUDIO_REDUNDANCY_KEY = "ENABLE_AUDIO_REDUNDANCY"
        const val RECONNECT_TIMEOUT_MS = "RECONNECT_TIMEOUT_MS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_join_meeting)

        val appSettings = AppSettingsBuilder()
            .subscribePresenceForAllUsers()
            .setRegion(AppConstants.REGION)
            .autoEstablishSocketConnection(true)
            .build()
        CometChat.init(
            this,
            AppConstants.APP_ID,
            appSettings,
            object : CometChat.CallbackListener<String>() {
                override fun onSuccess(p0: String?) {
                    Log.d("CometChat", "Initialization completed successfully")
                }

                override fun onError(p0: CometChatException?) {
                    Log.d("CometChat", "Initialization failed with exception: " + p0?.message)
                }
            }
        )

        binding.apply {
            buttonContinue.setOnClickListener {
                joinMeeting()
            }
        }
    }


    private fun login(uid: String) {

        CometChat.login(uid, AppConstants.AUTH_KEY, object : CometChat.CallbackListener<User?>() {
            override fun onSuccess(user: User?) {
                Log.d("CometChat", "Login completed successfully")
                authenticate(testUrl, meetingID, yourName)
            }
            override fun onError(e: CometChatException?) {
                Log.d("CometChat", "Login failed")
            }
        })
    }

    private fun joinMeeting() {
        var mode = AudioMode.Stereo48K
        var audioDeviceCapabilities = AudioDeviceCapabilities.InputAndOutput
        val reconnectTimeoutMs = 180000
        val redundancyEnabled = true
        audioVideoConfig = AudioVideoConfiguration(
            audioMode = mode,
            audioDeviceCapabilities = audioDeviceCapabilities,
            enableAudioRedundancy = redundancyEnabled,
            reconnectTimeoutMs = reconnectTimeoutMs
        )

        meetingID = binding.editMeetingId?.text.toString().trim().replace("\\s+".toRegex(), "+")
        yourName = binding.editName?.text.toString().trim().replace("\\s+".toRegex(), "+")
        testUrl = getTestUrl()

        if (meetingID.isNullOrBlank()) {
            showToast(getString(R.string.user_notification_meeting_id_invalid))
        } else if (yourName.isNullOrBlank()) {
            showToast(getString(R.string.user_notification_attendee_name_invalid))
        } else {
            if (hasPermissionsAlready()) {
                if (binding.editName.text.toString() == "user") {
                    login("gups12")
                } else {
                    login("gurpreet")
                }

            } else {
                val permissions =
                    audioVideoConfig.audioDeviceCapabilities.requiredPermissions() + arrayOf(
                        Manifest.permission.CAMERA
                    )
                ActivityCompat.requestPermissions(this, permissions, WEBRTC_PERMISSION_REQUEST_CODE)
            }
        }
    }

    private fun getTestUrl(): String {
        return getString(R.string.test_url)
    }

    private fun hasPermissionsAlready(): Boolean {
        val permissions = audioVideoConfig.audioDeviceCapabilities.requiredPermissions() + arrayOf(
            Manifest.permission.CAMERA
        )
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissionsList: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissionsList, grantResults)
        when (requestCode) {
            WEBRTC_PERMISSION_REQUEST_CODE -> {
                val isMissingPermission: Boolean =
                    grantResults.isEmpty() || grantResults.any { PackageManager.PERMISSION_GRANTED != it }

                if (isMissingPermission) {
                    showToast(getString(R.string.user_notification_permission_error))
                    return
                }

                if (binding.editName.text.toString() == "user") {
                    login("gups12")
                } else {
                    login("gurpreet")
                }
            }
        }
    }

    private fun authenticate(
        meetingUrl: String,
        meetingId: String?,
        attendeeName: String?
    ) =
        uiScope.launch {
            logger.info(
                "TAG",
                "Joining meeting. meetingUrl: $meetingUrl, meetingId: $meetingId, attendeeName: $attendeeName"
            )
            if (!meetingUrl.startsWith("http")) {
                showToast(getString(R.string.user_notification_meeting_url_error))
            } else {
                binding.progressAuthentication?.visibility = View.VISIBLE

                // val primaryMeetingId = debugSettingsViewModel.primaryMeetingId.value
                val meetingResponseJson: String? =
                    joinMeeting(meetingUrl, meetingId, attendeeName, "primaryMeetingId")

                binding.progressAuthentication?.visibility = View.INVISIBLE

                if (meetingResponseJson == null) {
                    showToast(getString(R.string.user_notification_meeting_start_error))
                } else {
                    val intent = Intent(applicationContext, HomeActivity::class.java).apply {
                        putExtras(
                            bundleOf(
                                MEETING_RESPONSE_KEY to meetingResponseJson,
                                MEETING_ID_KEY to meetingId,
                                NAME_KEY to attendeeName,
                                MEETING_ENDPOINT_KEY to meetingUrl,
                                AUDIO_MODE_KEY to audioVideoConfig.audioMode.value,
                                AUDIO_DEVICE_CAPABILITIES_KEY to audioVideoConfig.audioDeviceCapabilities,
                                ENABLE_AUDIO_REDUNDANCY_KEY to audioVideoConfig.enableAudioRedundancy,
                                RECONNECT_TIMEOUT_MS to audioVideoConfig.reconnectTimeoutMs
                            )
                        )
                    }
                    startActivity(intent)
                }
            }
        }

    private suspend fun joinMeeting(
        meetingUrl: String,
        meetingId: String?,
        attendeeName: String?,
        primaryMeetingId: String?
    ): String? {
        val meetingServerUrl = if (meetingUrl.endsWith("/")) meetingUrl else "$meetingUrl/"
        var url = "${meetingServerUrl}join?title=${encodeURLParam(meetingId)}&name=${
            encodeURLParam(
                attendeeName
            )
        }&region=${encodeURLParam(MEETING_REGION)}"
//        if (!primaryMeetingId.isNullOrEmpty()) {
//            url += "&primaryExternalMeetingId=${encodeURLParam(primaryMeetingId)}"
//        }
        Log.d("gettingUrl", url.toString())
        val response = HttpUtils.post(URL(url), "", DefaultBackOffRetry(), logger)
        return if (response.httpException == null) {
            response.data
        } else {
            logger.error("TAG", "Unable to join meeting. ${response.httpException}")
            null
        }
    }

}