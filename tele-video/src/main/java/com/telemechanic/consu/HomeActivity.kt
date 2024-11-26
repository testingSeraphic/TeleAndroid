package com.telemechanic.consu


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.PowerManager
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.PixelCopy
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.services.chime.sdk.meetings.analytics.EventAnalyticsObserver
import com.amazonaws.services.chime.sdk.meetings.analytics.EventAttributes
import com.amazonaws.services.chime.sdk.meetings.analytics.EventName
import com.amazonaws.services.chime.sdk.meetings.audiovideo.AttendeeInfo
import com.amazonaws.services.chime.sdk.meetings.audiovideo.AudioVideoConfiguration
import com.amazonaws.services.chime.sdk.meetings.audiovideo.AudioVideoFacade
import com.amazonaws.services.chime.sdk.meetings.audiovideo.AudioVideoObserver
import com.amazonaws.services.chime.sdk.meetings.audiovideo.PrimaryMeetingPromotionObserver
import com.amazonaws.services.chime.sdk.meetings.audiovideo.SignalUpdate
import com.amazonaws.services.chime.sdk.meetings.audiovideo.TranscriptEvent
import com.amazonaws.services.chime.sdk.meetings.audiovideo.VolumeUpdate
import com.amazonaws.services.chime.sdk.meetings.audiovideo.audio.AudioDeviceCapabilities
import com.amazonaws.services.chime.sdk.meetings.audiovideo.audio.AudioMode
import com.amazonaws.services.chime.sdk.meetings.audiovideo.audio.activespeakerdetector.ActiveSpeakerObserver
import com.amazonaws.services.chime.sdk.meetings.audiovideo.audio.activespeakerpolicy.DefaultActiveSpeakerPolicy
import com.amazonaws.services.chime.sdk.meetings.audiovideo.contentshare.ContentShareObserver
import com.amazonaws.services.chime.sdk.meetings.audiovideo.contentshare.ContentShareStatus
import com.amazonaws.services.chime.sdk.meetings.audiovideo.metric.MetricsObserver
import com.amazonaws.services.chime.sdk.meetings.audiovideo.metric.ObservableMetric
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.LocalVideoConfiguration
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.RemoteVideoSource
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.VideoPauseState
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.VideoPriority
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.VideoResolution
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.VideoScalingType
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.VideoSubscriptionConfiguration
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.VideoTileObserver
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.VideoTileState
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.backgroundfilter.backgroundblur.BackgroundBlurVideoFrameProcessor
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.backgroundfilter.backgroundreplacement.BackgroundReplacementVideoFrameProcessor
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.capture.CameraCaptureSource
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.capture.DefaultCameraCaptureSource
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.capture.DefaultSurfaceTextureCaptureSourceFactory
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.gl.EglCoreFactory
import com.amazonaws.services.chime.sdk.meetings.device.DeviceChangeObserver
import com.amazonaws.services.chime.sdk.meetings.device.MediaDevice
import com.amazonaws.services.chime.sdk.meetings.device.MediaDeviceType
import com.amazonaws.services.chime.sdk.meetings.internal.AttendeeStatus
import com.amazonaws.services.chime.sdk.meetings.realtime.RealtimeObserver
import com.amazonaws.services.chime.sdk.meetings.realtime.TranscriptEventObserver
import com.amazonaws.services.chime.sdk.meetings.realtime.datamessage.DataMessage
import com.amazonaws.services.chime.sdk.meetings.realtime.datamessage.DataMessageObserver
import com.amazonaws.services.chime.sdk.meetings.session.CreateAttendeeResponse
import com.amazonaws.services.chime.sdk.meetings.session.CreateMeetingResponse
import com.amazonaws.services.chime.sdk.meetings.session.DefaultMeetingSession
import com.amazonaws.services.chime.sdk.meetings.session.MediaPlacement
import com.amazonaws.services.chime.sdk.meetings.session.Meeting
import com.amazonaws.services.chime.sdk.meetings.session.MeetingFeatures
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionConfiguration
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionCredentials
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionStatus
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionStatusCode.AudioAuthenticationRejected
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionStatusCode.AudioCallAtCapacity
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionStatusCode.AudioCallEnded
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionStatusCode.AudioDisconnectAudio
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionStatusCode.AudioDisconnected
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionStatusCode.AudioInputDeviceNotResponding
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionStatusCode.AudioInternalServerError
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionStatusCode.AudioJoinedFromAnotherDevice
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionStatusCode.AudioOutputDeviceNotResponding
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionStatusCode.AudioServiceUnavailable
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionStatusCode.ConnectionHealthReconnect
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionStatusCode.Left
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionStatusCode.NetworkBecamePoor
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionStatusCode.OK
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionStatusCode.VideoAtCapacityViewOnly
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionStatusCode.VideoServiceFailed
import com.amazonaws.services.chime.sdk.meetings.utils.logger.ConsoleLogger
import com.amazonaws.services.chime.sdk.meetings.utils.logger.LogLevel
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.MessagesRequest.MessagesRequestBuilder
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.CustomMessage
import com.cometchat.chat.models.MediaMessage
import com.cometchat.chat.models.MessageReceipt
import com.cometchat.chat.models.TextMessage
import com.google.gson.Gson
import com.hbisoft.hbrecorder.HBRecorder
import com.hbisoft.hbrecorder.HBRecorderListener
import com.telemechanic.consu.data.RosterAttendee
import com.telemechanic.consu.data.VideoCollectionTile
import com.telemechanic.consu.databinding.ActivityHomeBinding
import com.telemechanic.consu.databinding.LayoutChatVideoBinding
import com.telemechanic.consu.datamodel.Attachment
import com.telemechanic.consu.datamodel.ChatMessageData
import com.telemechanic.consu.datamodel.JoinMeetingResponse
import com.telemechanic.consu.device.AudioDeviceManager
import com.telemechanic.consu.service.MicrophoneService
import com.telemechanic.consu.utils.CpuVideoProcessor
import com.telemechanic.consu.utils.GpuVideoProcessor
import com.telemechanic.consu.utils.Utils
import com.telemechanic.consu.utils.isContentShare
import com.telemechanic.consu.viewmodel.JoinMeetingModel
import com.telemechanic.consu.viewmodel.MeetingModel
import eightbitlab.com.blurview.BlurView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.URISyntaxException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class HomeActivity : AppCompatActivity(),
    RealtimeObserver, AudioVideoObserver, VideoTileObserver,
    MetricsObserver, ActiveSpeakerObserver, DeviceChangeObserver, DataMessageObserver,
    ContentShareObserver, EventAnalyticsObserver, TranscriptEventObserver,
    PrimaryMeetingPromotionObserver, HBRecorderListener {
    private lateinit var binding: ActivityHomeBinding
    private var lastOrientation: String? = null
    private lateinit var meetingId: String
    private lateinit var name: String
    private val gson = Gson()
    private var localIsSmall = false
    private var remoteIsSmall = false
    private lateinit var audioVideoConfig: AudioVideoConfiguration
    private lateinit var meetingEndpointUrl: String
    private val logger = ConsoleLogger(LogLevel.DEBUG)
    private val meetingSessionModel: JoinMeetingModel by lazy { ViewModelProvider(this)[JoinMeetingModel::class.java] }
    private val meetingModel: MeetingModel by lazy { ViewModelProvider(this)[MeetingModel::class.java] }
    private var PERMISSIONS_REQUEST_CODE = 129
    private lateinit var audioDeviceManager: AudioDeviceManager
    private lateinit var credentials: MeetingSessionCredentials
    private val mutex = Mutex()
    private var isBottomBarVisible = false
    private var isToolListVisible = false
    private lateinit var audioVideo: AudioVideoFacade
    private lateinit var cameraCaptureSource: CameraCaptureSource
    private lateinit var gpuVideoProcessor: GpuVideoProcessor
    private lateinit var cpuVideoProcessor: CpuVideoProcessor
    private lateinit var backgroundBlurVideoFrameProcessor: BackgroundBlurVideoFrameProcessor
    private lateinit var backgroundReplacementVideoFrameProcessor: BackgroundReplacementVideoFrameProcessor
    private lateinit var eglCoreFactory: EglCoreFactory
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private lateinit var powerManager: PowerManager
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private val CONTENT_NAME_SUFFIX = "<<Content>>"
    private lateinit var countdownTimer: CountDownTimer
    private var extraTimeCountdownTimer: CountDownTimer? = null
    private var isInExtraTime = false
    private lateinit var hbRecorder: HBRecorder
    private var isCursorEnabled = false
    private var isTimerStarted = false
    private var isSmallCursorEnabled = false
    private var isVideoCompressed = false
    private var isPortrait = false
    private var isRemotePortrait = false
    private var movingX = 0f
    private var movingY = 0f
    private var movingSmallX = 0f
    private var movingSmallY = 0f
    private var bigViewWidth = 0f
    private var bigViewHeight = 0f
    private var smallViewWidth = 0f
    private var smallViewHeight = 0f
    val listenerID: String = "UNIQUE_LISTENER_ID"
    private var chatDialog: Dialog? = null
    private var clDialogDelete: ConstraintLayout? = null
    private var clDialogRV: RecyclerView? = null
    private var clDialogEtField: EditText? = null
    private var myUid: String? = null
    private var deleteMessageId: Int? = null
    private var oppositeUid: String? = null
    private lateinit var chatMessageAdapter: ChatMessageAdapter

    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val REQUEST_CODE_PERMISSION = 100
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        meetingId = intent.extras?.getString(JoinMeeting.MEETING_ID_KEY) as String
        name = intent.extras?.getString(JoinMeeting.NAME_KEY) as String
        binding.apply {
            makeViewBlur(blurViewExtend, 24f)
            bigVideoSurface.setZOrderOnTop(false)
            videoSmall.setZOrderOnTop(true)
            videoSmall.setZOrderMediaOverlay(true);
            cursor.visibility = View.GONE
            cursorSmall.visibility = View.GONE
            flSmallVideo.outlineProvider = ViewOutlineProvider.BACKGROUND
            flSmallVideo.clipToOutline = true
            // clTesting.setRenderEffect(RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP))

        }
        if (name == "user") {
            myUid = "gups12"
            oppositeUid = "gurpreet"
        } else {
            myUid = "gurpreet"
            oppositeUid = "gups12"
        }

        chatMessageAdapter =
            ChatMessageAdapter(this, myUid!!, object : ChatMessageAdapter.ChatClicks {
                override fun deleteMsgClick(messageId: Int, pos: Int) {
                    runOnUiThread {
                        Log.d("getDeleteClickView", "true")
                        clDialogDelete?.let { deleteView ->
                            deleteView.visibility = View.VISIBLE
                            deleteMessageId = messageId // Store the messageId for deletion
                        }
                    }
                }
            })
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 30+ (API 30 and above)
                window.insetsController?.hide(WindowInsets.Type.statusBars())
            } else {
                // Below Android 30
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_FULLSCREEN // Hides the status bar
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY // Ensures it stays hidden
                        )
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 30+ (API 30 and above)
                window.insetsController?.show(WindowInsets.Type.statusBars())
            } else {
                // Below Android 30
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE // Show status bar
            }
        }
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val rotation = windowManager.defaultDisplay.rotation
            when (rotation) {
                Surface.ROTATION_90 -> {
                    Log.d("HomeOrientation01", "Landscape - Left")
                    isPortrait = false
                    // setVideoContentMode()
                    updateConstraintsForLandscapeLeft()
                    binding.clBottomViewPortrait.visibility = View.GONE
                    binding.blurViewSideBar.visibility = View.GONE
                    binding.clBottomViewLandscape.visibility = View.VISIBLE
                    if (isToolListVisible) {
                        binding.blurViewSideBarLandscape.visibility = View.VISIBLE
                    } else {
                        binding.blurViewSideBarLandscape.visibility = View.GONE
                    }
                    if (isBottomBarVisible) {
                        bottomViewVisibility(true)
                    } else {
                        bottomViewVisibility(false)
                    }

                }

                Surface.ROTATION_270 -> {
                    Log.d("HomeOrientation01", "Landscape - Right")
                    isPortrait = false
                    // setVideoContentMode()
                    updateConstraintsForLandscapeRight()
                    binding.clBottomViewPortrait.visibility = View.GONE
                    binding.blurViewSideBar.visibility = View.GONE
                    binding.clBottomViewLandscape.visibility = View.VISIBLE
                    if (isToolListVisible) {
                        binding.blurViewSideBarLandscape.visibility = View.VISIBLE
                    } else {
                        binding.blurViewSideBarLandscape.visibility = View.GONE
                    }
                    if (isBottomBarVisible) {
                        bottomViewVisibility(true)
                    } else {
                        bottomViewVisibility(false)
                    }
                }

                else -> {
                    Log.d("HomeOrientation01", "Landscape - Unknown rotation")


                }
            }
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d("HomeOrientation01", "Portrait")
            isPortrait = true
            //setVideoContentMode()
            updateConstraintsForPortrait()
            binding.clBottomViewLandscape.visibility = View.GONE
            binding.blurViewSideBarLandscape.visibility = View.GONE
            binding.clBottomViewPortrait.visibility = View.VISIBLE
            if (isToolListVisible) {
                binding.blurViewSideBar.visibility = View.VISIBLE
            } else {
                binding.blurViewSideBar.visibility = View.GONE
            }
            if (isBottomBarVisible) {
                bottomViewVisibility(true)
            } else {
                bottomViewVisibility(false)
            }
        }

        chatListeners()
        val videoRenderView = binding.bigVideoSurface
        videoRenderView.scalingType = VideoScalingType.AspectFill
        commonListeners()
        if (name != "user") {
            remoteIsSmall = false
            localIsSmall = true
        } else {
            remoteIsSmall = true
            localIsSmall = false
        }
        val meetingResponseJson =
            intent.extras?.getString(JoinMeeting.MEETING_RESPONSE_KEY) as String
        val sessionConfig =
            createSessionConfigurationAndExtractPrimaryMeetingInformation(meetingResponseJson)
        val meetingSession = sessionConfig?.let {
            logger.info(
                "CreatingMeetingSession",
                "Creating meeting session for meeting Id: $meetingId"
            )

            DefaultMeetingSession(
                it,
                logger,
                applicationContext,
                meetingSessionModel.eglCoreFactory
            )
        }

        if (meetingSession == null) {
            Toast.makeText(
                applicationContext,
                getString(R.string.user_notification_meeting_start_error),
                Toast.LENGTH_LONG
            ).show()
            finish()
        } else {
            meetingSessionModel.meetingSession = meetingSession
        }
        val surfaceTextureCaptureSourceFactory = DefaultSurfaceTextureCaptureSourceFactory(
            logger,
            meetingSessionModel.eglCoreFactory
        )
        meetingSessionModel.cameraCaptureSource = DefaultCameraCaptureSource(
            applicationContext,
            logger,
            surfaceTextureCaptureSourceFactory
        ).apply {
            eventAnalyticsController = meetingSession?.eventAnalyticsController
        }
        // Add a new parameter for DefaultCameraCaptureSource (videoMaxResolution)
        var resolution: VideoResolution = VideoResolution.VideoResolutionHD
        meetingSession?.let {
            resolution = it.configuration.features.videoMaxResolution
        }
        meetingSessionModel.cameraCaptureSource.setMaxResolution(resolution)
        meetingSessionModel.cpuVideoProcessor =
            CpuVideoProcessor(logger, meetingSessionModel.eglCoreFactory)
        meetingSessionModel.gpuVideoProcessor =
            GpuVideoProcessor(logger, meetingSessionModel.eglCoreFactory)
        credentials = getMeetingSessionCredentials()
        audioVideo = getAudioVideo()
        eglCoreFactory = getEglCoreFactory()
        cameraCaptureSource = getCameraCaptureSource()
        gpuVideoProcessor = getGpuVideoProcessor()
        cpuVideoProcessor = getCpuVideoProcessor()
        // screenShareManager = activity.getScreenShareManager()
        audioDeviceManager = AudioDeviceManager(audioVideo)
        meetingEndpointUrl =
            intent.extras?.getString(JoinMeeting.MEETING_ENDPOINT_KEY) as String
        mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        setupAudioVideoFacadeObservers()
        val audioMode = intent.extras?.getInt(JoinMeeting.AUDIO_MODE_KEY)?.let { intValue ->
            AudioMode.from(intValue, defaultAudioMode = AudioMode.Stereo48K)
        } ?: AudioMode.Stereo48K
        val audioDeviceCapabilities =
            intent.extras?.get(JoinMeeting.AUDIO_DEVICE_CAPABILITIES_KEY) as? AudioDeviceCapabilities
                ?: AudioDeviceCapabilities.InputAndOutput
        val enableAudioRedundancy =
            intent.extras?.getBoolean(JoinMeeting.ENABLE_AUDIO_REDUNDANCY_KEY) as Boolean
        val reconnectTimeoutMs = intent.extras?.getInt(JoinMeeting.RECONNECT_TIMEOUT_MS) as Int
        audioVideoConfig = AudioVideoConfiguration(
            audioMode = audioMode,
            audioDeviceCapabilities = audioDeviceCapabilities,
            enableAudioRedundancy = enableAudioRedundancy,
            reconnectTimeoutMs = reconnectTimeoutMs
        )
        audioVideo.start(audioVideoConfig)
        audioVideo.startRemoteVideo()
        startLocalVideo()

        hbRecorder = HBRecorder(this, this)
        hbRecorder.apply {
            setVideoEncoder("H264")
            enableCustomSettings()
            isAudioEnabled(false)
            setNotificationSmallIconVector(R.drawable.ic_selected_mic)
            setNotificationTitle("Call")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            audioVideoConfig.audioDeviceCapabilities == AudioDeviceCapabilities.InputAndOutput
        ) {
            startForegroundService(
                Intent(
                    this,
                    MicrophoneService::class.java
                ).also { intent ->
                    bindService(
                        intent,
                        meetingModel.microphoneServiceConnection,
                        Context.BIND_AUTO_CREATE
                    )
                }
            )
        }


        // Set an OnTouchListener to the SurfaceView
        binding.bigVideoSurface.setOnTouchListener { _, event ->
            if (isCursorEnabled) {
                moveCursor(event.x, event.y)
                true
            } else {
                // Pass touch event to views below if cursor mode is disabled
                false
            }
        }

        binding.videoSmall.setOnTouchListener { _, event ->
            if (isSmallCursorEnabled) {
                moveSmallCursor(event.x, event.y)
                true
            } else {
                // Pass touch event to views below if cursor mode is disabled
                false
            }
        }

        setupPortraitButtonsBar()
        setupLandscapeButtonsBar()
        // startRecordingScreen()
        filePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.let { fileUri ->
                        Log.d("getFileUri", fileUri.toString())
                        val filePath = getRealPathFromURI(this, fileUri)
                        if (filePath != null) {
                            val messageType = getFileType(filePath)
                            sendMediaMessage(filePath, messageType)
                        } else {
                            val cachedFile = saveUriToCache(this, fileUri)
                            if (cachedFile != null) {
                                val messageType = getFileType(cachedFile.absolutePath)
                                sendMediaMessage(cachedFile.absolutePath, messageType)
                                Log.d("Fallback", "File saved to cache: ${cachedFile.absolutePath}")
                            } else {
                                Toast.makeText(
                                    this,
                                    "Failed to retrieve file path",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }

                        }
                    }
                }
            }

    }

    private fun checkAndRequestPermissions(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

        val listPermissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                REQUEST_CODE_PERMISSION
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.d("Permissions", "All permissions granted")
                // Proceed with accessing files
            } else {
                Log.e("Permissions", "Some permissions are denied")
                Toast.makeText(this, "Permissions are required to access files", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun openFilePicker() {
        //val intent = Intent(Intent.ACTION_GET_CONTENT)
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        filePickerLauncher.launch(Intent.createChooser(intent, "Select a file"))
    }

    private fun getFileType(filePath: String): String {
        return when {
            filePath.endsWith(".jpg", true) || filePath.endsWith(
                ".jpeg",
                true
            ) || filePath.endsWith(".png", true) -> {
                CometChatConstants.MESSAGE_TYPE_IMAGE
            }

            filePath.endsWith(".mp4", true) || filePath.endsWith(".mkv", true) -> {
                CometChatConstants.MESSAGE_TYPE_VIDEO
            }

            filePath.endsWith(".mp3", true) || filePath.endsWith(".wav", true) -> {
                CometChatConstants.MESSAGE_TYPE_AUDIO
            }

            else -> CometChatConstants.MESSAGE_TYPE_FILE
        }
    }


    fun getRealPathFromURI(context: Context, uri: Uri): String? {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":")
            val type = split[0]

            when (uri.authority) {
                "com.android.providers.media.documents" -> {
                    val contentUri = when (type) {
                        "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        "document" -> MediaStore.Files.getContentUri("external")
                        else -> null
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])

                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }

                "com.android.providers.downloads.documents" -> {
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        split[1].toLongOrNull() ?: return null
                    )
                    return getDataColumn(context, contentUri, null, null)
                }
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }

        return null
    }

    fun saveUriToCache(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(context.cacheDir, getFileNameFromUri(this, uri))
            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: IOException) {
            Log.e("TempFile", "Error saving file to cache: ${e.message}")
            null
        }
    }

    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    fileName = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        } else if (uri.scheme == "file") {
            fileName = File(uri.path!!).name
        }
        return fileName
    }


    private fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        val projection = arrayOf(MediaStore.Files.FileColumns.DATA)
        context.contentResolver.query(
            uri ?: return null,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                return cursor.getString(columnIndex)
            }
        }
        return null
    }

    private fun sendMediaMessage(filePath: String, messageType: String) {
        val file = File(filePath)

        if (!file.exists()) {
            Toast.makeText(this, "File not found at: $filePath", Toast.LENGTH_SHORT).show()
            Log.e("CometChat", "File does not exist at path: $filePath")
            return
        }
        if (name == "user") {
            myUid = "gups12"
            oppositeUid = "gurpreet"
        } else {
            oppositeUid = "gups12"
            myUid = "gurpreet"
        }
        val receiverID = oppositeUid// Replace with actual ID
        val receiverType = CometChatConstants.RECEIVER_TYPE_USER // Or RECEIVER_TYPE_GROUP

        val mediaMessage = MediaMessage(receiverID, file, messageType, receiverType)

        CometChat.sendMediaMessage(
            mediaMessage,
            object : CometChat.CallbackListener<MediaMessage>() {
                override fun onSuccess(mediaMessage: MediaMessage) {
                    Log.d("CometChat", "Media message sent successfully: $mediaMessage")
                    var messageBody = ChatMessageData()
                    messageBody.id = mediaMessage.id
                    messageBody.text = mediaMessage.attachment.fileUrl
                    messageBody.type = mediaMessage.type
                    messageBody.sentAt = mediaMessage.sentAt
                    messageBody.senderUid = mediaMessage.sender.uid
                    messageBody.receiverUid = mediaMessage.receiverUid
                    var attachmentData = Attachment(
                        mediaMessage.attachment.fileName,
                        mediaMessage.attachment.fileExtension,
                        mediaMessage.attachment.fileSize,
                        mediaMessage.attachment.fileMimeType,
                        mediaMessage.attachment.fileUrl
                    )
                    messageBody.attachment = attachmentData
                    chatMessageAdapter.addMessage(messageBody)
                    clDialogRV?.scrollToPosition(chatMessageAdapter.itemCount - 1)
                    clDialogEtField?.text!!.clear()
                }

                override fun onError(e: CometChatException) {
                    Log.e("CometChat", "Media message sending failed: ${e.message}")
                    Toast.makeText(
                        this@HomeActivity,
                        "Failed to send media message: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun getContentUri(file: File): Uri {
        return FileProvider.getUriForFile(
            this, // Context
            "${applicationContext.packageName}.fileprovider", // FileProvider authority
            file // The file to convert
        )
    }

    private var isChatWindowOpen = false
    private fun chatListeners() {
        CometChat.addMessageListener(listenerID, object : CometChat.MessageListener() {
            override fun onTextMessageReceived(textMessage: TextMessage) {
                Log.d("CometChat", "Text message received successfully: $textMessage")
                checkUnreadMessages(textMessage.getSender().getUid())
                runOnUiThread {
                    var messageBody = ChatMessageData()
                    messageBody.id = textMessage.id
                    messageBody.text = textMessage.text
                    messageBody.type = textMessage.type
                    messageBody.sentAt = textMessage.sentAt
                    messageBody.senderUid = textMessage.sender.uid
                    messageBody.receiverUid = textMessage.receiverUid
                    chatMessageAdapter.addMessage(messageBody)
                    scrollToLastMessage()
                }
                if (isChatWindowOpen) {
                    CometChat.markAsRead(
                        textMessage.id,
                        textMessage.sender.uid,
                        CometChatConstants.RECEIVER_TYPE_USER,
                        textMessage.sender.uid
                    )
                }
            }

            override fun onMediaMessageReceived(mediaMessage: MediaMessage) {
                Log.d("CometChat", "Media message received successfully: $mediaMessage")
                checkUnreadMessages(mediaMessage.getSender().getUid())
                runOnUiThread {
                    var messageBody = ChatMessageData()
                    messageBody.id = mediaMessage.id
                    messageBody.text = ""
                    messageBody.type = mediaMessage.type
                    messageBody.sentAt = mediaMessage.sentAt
                    messageBody.senderUid = mediaMessage.sender.uid
                    messageBody.receiverUid = mediaMessage.receiverUid
                    var attachmentData = Attachment(
                        mediaMessage.attachment.fileName,
                        mediaMessage.attachment.fileExtension,
                        mediaMessage.attachment.fileSize,
                        mediaMessage.attachment.fileMimeType,
                        mediaMessage.attachment.fileUrl
                    )
                    messageBody.attachment = attachmentData
                    chatMessageAdapter.addMessage(messageBody)
                    scrollToLastMessage()
                }
                if (isChatWindowOpen) {
                    CometChat.markAsRead(
                        mediaMessage.id,
                        mediaMessage.sender.uid,
                        CometChatConstants.RECEIVER_TYPE_USER,
                        mediaMessage.sender.uid
                    )
                }
            }

            override fun onCustomMessageReceived(customMessage: CustomMessage) {
                checkUnreadMessages(customMessage.getSender().getUid())
                Log.d("CometChat", "Custom message received successfully: $customMessage")
            }

            override fun onMessagesDelivered(messageReceipt: MessageReceipt) {
                Log.d("CometChat", "onMessagesDelivered: $messageReceipt")
            }

            override fun onMessagesRead(messageReceipt: MessageReceipt) {
                Log.d("CometChat", "onMessagesRead: $messageReceipt")
                chatMessageAdapter.readMessage(messageReceipt.messageId)
            }

            override fun onMessagesDeliveredToAll(messageReceipt: MessageReceipt) {
                Log.d("CometChat", "onMessagesDeliveredToAll: $messageReceipt")
            }

            override fun onMessagesReadByAll(messageReceipt: MessageReceipt) {
                Log.d("CometChat", "onMessagesReadByAll: $messageReceipt")
            }

            override fun onMessageDeleted(message: BaseMessage) {
                Log.d("CometChat", "Message Edited")
                runOnUiThread {
                    chatMessageAdapter.deleteMessage(message.id)
                    scrollToLastMessage()
                }
            }
        })
    }

    private fun checkUnreadMessages(uid: String) {
        CometChat.getUnreadMessageCountForUser(
            uid,
            object : CometChat.CallbackListener<HashMap<String?, Int?>?>(){
                override fun onSuccess(stringIntegerHashMap: HashMap<String?, Int?>?) {
                    if (stringIntegerHashMap?.containsKey(uid) == true) {
                        val count = stringIntegerHashMap[uid]!!
                        binding.apply {
                            if (count > 0) {
                                if(isBottomBarVisible && !isChatWindowOpen) {
                                    if (isPortrait) {
                                        tvUnreadCountBadgeLandScape.visibility = View.GONE
                                        tvUnreadCountBadge.visibility = View.VISIBLE
                                    } else {
                                        tvUnreadCountBadgeLandScape.visibility = View.VISIBLE
                                        tvUnreadCountBadge.visibility = View.GONE
                                    }
                                }
                                Log.d("UnreadMessages", "Unread messages for user $uid: $count")
                            } else {
                                tvUnreadCountBadge.visibility=View.GONE
                                tvUnreadCountBadgeLandScape.visibility=View.GONE
                                Log.d("UnreadMessages", "No unread messages for user $uid")
                            }
                        }
                    }
                }

                override fun onError(e: CometChatException) {
                    Log.e("UnreadMessagesError", "Error fetching unread messages: " + e.message)
                }
            })
    }


fun previousMessages() {
    val limit: Int = 1
    val UID: String = oppositeUid.toString()

    val messagesRequest = MessagesRequestBuilder()
        .setLimit(limit)
        .setUID(UID)
        .build()

    Log.d("CometChat", "fetching")
    messagesRequest.fetchPrevious(object : CometChat.CallbackListener<List<BaseMessage?>>() {
        override fun onSuccess(list: List<BaseMessage?>) {
            Log.d("CometChat", "previousMessages fetch successfully")
            for (message in list) {
                if (message is TextMessage) {
                    Log.d("CometChat", "Text message received successfully: $message")
                    val rawMessage = (message as TextMessage).rawMessage
                    Log.d("CometChat", "rawMessage: $rawMessage")
                } else if (message is MediaMessage) {
                    Log.d("CometChat", "Media message received successfully: $message")
                }
            }
        }

        override fun onError(e: CometChatException) {
            Log.d("CometChat", "Message fetching failed with exception: " + e.message)
        }
    })
}

private fun scrollToLastMessage() {
    chatDialog?.findViewById<RecyclerView>(R.id.recyMessage)?.scrollToPosition(
        chatMessageAdapter.itemCount - 1
    )
}


fun showBottomSheet() {
    isChatWindowOpen = true
    val dialog = Dialog(this, R.style.TransparentDialog)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

    // Use View Binding to inflate the layout
    val binding = LayoutChatVideoBinding.inflate(layoutInflater)

    // Set the content view to the binding's root
    dialog.setContentView(binding.root)
    binding.apply {
        clDialogDelete = binding.clDelete
        clDialogRV = binding.recyMessage
        clDialogEtField = binding.etMsg
        if (chatMessageAdapter.messagesList.isNotEmpty()) {
            var item = chatMessageAdapter.messagesList[chatMessageAdapter.messagesList.size - 1]
            CometChat.markAsRead(
                item.id!!, item.senderUid!!, CometChatConstants.RECEIVER_TYPE_USER, item.senderUid!!
            )
        }
        // Set up RecyclerView
        recyMessage.layoutManager = LinearLayoutManager(this@HomeActivity)
        recyMessage.adapter = chatMessageAdapter

        // Handle expand image click
        imgImageExpand.setOnClickListener {
            dialog.dismiss()
            isChatWindowOpen = false
        }
        llAttachment.setOnClickListener {
            if (checkAndRequestPermissions()) {
                openFilePicker()
            }
        }

        // Handle dismiss view click
        viewDismiss.setOnClickListener {
            dialog.dismiss()
            isChatWindowOpen = false
        }

        clDelete.setOnClickListener {
            CometChat.deleteMessage(
                deleteMessageId!!,
                object : CometChat.CallbackListener<BaseMessage>() {
                    override fun onSuccess(message: BaseMessage) {
                        Log.d("CometChat", "Message deleted successfully at : " + message.deletedAt)
                        runOnUiThread {
                            chatMessageAdapter.deleteMessage(message.id)
                            scrollToLastMessage()
                            clDelete.visibility = View.GONE
                        }

                    }

                    override fun onError(e: CometChatException) {
                        Log.d("CometChat", e.message!!)
                        clDelete.visibility = View.GONE
                    }
                })
        }

        llChatSend.setOnClickListener {
            if (etMsg.text.toString().isEmpty()) {
                Toast.makeText(
                    this@HomeActivity,
                    "Please Type Something... ",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                var receiverId = if (name == "user") {
                    "gurpreet"
                } else {
                    "gups12"
                }
                val textMessage = TextMessage(
                    receiverId,
                    etMsg.text.toString(),
                    CometChatConstants.RECEIVER_TYPE_USER
                )
                CometChat.sendMessage(
                    textMessage,
                    object : CometChat.CallbackListener<TextMessage>() {
                        override fun onSuccess(textMessage: TextMessage) {

                            var messageBody = ChatMessageData()
                            messageBody.id = textMessage.id
                            messageBody.text = textMessage.text
                            messageBody.type = textMessage.type
                            messageBody.sentAt = textMessage.sentAt
                            messageBody.senderUid = textMessage.sender.uid
                            messageBody.receiverUid = textMessage.receiverUid
                            chatMessageAdapter.addMessage(messageBody)
                            recyMessage.scrollToPosition(chatMessageAdapter.itemCount - 1)
                            etMsg.text.clear()
                            Log.d("CometChat", "Message send successfully:$textMessage")
                            val rawMessage = textMessage.rawMessage
                            previousMessages()
                        }

                        override fun onError(e: CometChatException) {
                            Log.d(
                                "CometChat",
                                "Message sending failed with exception: " + e.message
                            )
                        }
                    })
            }
        }

        // Make the dialog full-screen
        dialog.setCancelable(false)
        dialog.show()
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        chatDialog = dialog

    }
}

//    fun getModerationStatusMessage(chatMessage: TextMessage): String? {
//        return try {
//            val gson = Gson()
//            val rawMessage = JSONObject((message as TextMessage).rawMessage.toString())
//                chatMessage.rawMessage
//            rawMessage?.moderation!!.status
//        } catch (e: Exception) {
//            Log.e("ImageUrlError", "Error extracting URL with Gson: ${e.message}")
//            null
//        }
//    }


fun cometUserLogout() {
    CometChat.logout(object : CometChat.CallbackListener<String>() {
        override fun onSuccess(p0: String?) {
            Log.d("TAG", "Logout completed successfully")
        }

        override fun onError(p0: CometChatException?) {
            Log.d("TAG", "Logout failed with exception: " + p0?.message)
        }
    })
}

private fun setVideoContentMode() {
    binding.apply {
        Log.d("HomeCase2", "1")
        if (!remoteIsSmall) {
            Log.d("HomeCase2", "2")
            if (isPortrait && isRemotePortrait) {
                bigVideoSurface.scalingType = VideoScalingType.AspectFill
            } else if (isPortrait && !isRemotePortrait) {
                Log.d("HomeCase2", "3")
                bigVideoSurface.scalingType = VideoScalingType.AspectFit
            } else if (!isPortrait && !isRemotePortrait) {
                Log.d("HomeCase2", "4")
                bigVideoSurface.scalingType = VideoScalingType.AspectFill
            } else if (isRemotePortrait && !isPortrait) {
                Log.d("HomeCase2", "5")
                bigVideoSurface.scalingType = VideoScalingType.AspectFit
            }
            Log.d("HomeCase2", "isPortrait" + isPortrait)
            videoSmall.scalingType =
                if (isPortrait) VideoScalingType.AspectFill else VideoScalingType.AspectFit
        } else {
            Log.d("HomeCase2", "6")
            if (isPortrait && isRemotePortrait) {
                Log.d("HomeCase2", "7")
                videoSmall.scalingType =
                    VideoScalingType.AspectFill
            } else if (isPortrait && !isRemotePortrait) {
                Log.d("HomeCase2", "8")
                videoSmall.scalingType =
                    VideoScalingType.AspectFit
            } else if (!isPortrait && !isRemotePortrait) {
                Log.d("HomeCase2", "9")
                videoSmall.scalingType =
                    VideoScalingType.AspectFit
            } else if (isRemotePortrait && !isPortrait) {
                Log.d("HomeCase2", "10")
                videoSmall.scalingType =
                    VideoScalingType.AspectFill
            }

            bigVideoSurface.scalingType = VideoScalingType.AspectFill
        }
    }

}

@RequiresApi(Build.VERSION_CODES.Q)
private fun moveCursor(x: Float, y: Float) {
    // Get screen position of bigVideoSurface
    val bigViewWidth = binding.bigVideoSurface.width.toFloat()
    val bigViewHeight = binding.bigVideoSurface.height.toFloat()

    val bigVideoSurfaceLocation =
        getRelativePosition(binding.bigVideoSurface, binding.tileContainer)
    val viewTop = bigVideoSurfaceLocation.first
    var viewLeft = bigVideoSurfaceLocation.second

    val windowInsets =
        WindowInsetsCompat.toWindowInsetsCompat(window.decorView.rootWindowInsets)
    val statusBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top
    val navigationBarHeight =
        windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
    val displayMetrics = resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels
    val screenHeight = displayMetrics.heightPixels
//GMove
    // Calculate padding (black areas) around bigVideoSurface
    val verticalPadding = (screenHeight - binding.bigVideoSurface.height) / 2
    val horizontalPadding = (screenWidth - binding.bigVideoSurface.width) / 2

    // Adjust offsets based on black padding areas
    val adjustedOffsetX = viewLeft.toFloat() + horizontalPadding
    val adjustedOffsetY = viewTop.toFloat() + verticalPadding


    val cursorHalfWidth = binding.cursor.width / 2
    val cursorHalfHeight = binding.cursor.height / 2

    // Calculate min and max bounds within the visible area of bigVideoSurface
    val minX = adjustedOffsetX + cursorHalfWidth
    val maxX = adjustedOffsetX + bigViewWidth - cursorHalfWidth
    val minY = adjustedOffsetY + cursorHalfHeight
    val maxY = adjustedOffsetY + bigViewHeight - cursorHalfHeight

    // Clamp x and y within bounds and adjust cursor position
    val clampedX = (x + adjustedOffsetX).coerceIn(minX, maxX)
    val clampedY = (y + adjustedOffsetY).coerceIn(minY, maxY)

    binding.cursor.x = clampedX - viewLeft - cursorHalfWidth
    binding.cursor.y = clampedY - viewTop - cursorHalfHeight

    movingX = clampedX - viewLeft
    movingY = clampedY - viewTop

    shareCursorData(movingX, movingY)
}


fun calculateNormalizedCoordinates(cursorX: Float, cursorY: Float): Pair<Float, Float> {
    val bigVideoSurfaceLocation =
        getRelativePosition(binding.bigVideoSurface, binding.tileContainer)
    val surfaceX = bigVideoSurfaceLocation.first
    var surfaceY = bigVideoSurfaceLocation.second

    // Get bigVideoSurface dimensions
    val surfaceWidth = binding.bigVideoSurface.width.toFloat()
    val surfaceHeight = binding.bigVideoSurface.height.toFloat()

    // Calculate cursor position relative to bigVideoSurface
//        val relativeCursorX = (cursorX - surfaceX) / surfaceWidth
//        val relativeCursorY = (cursorY - surfaceY) / surfaceHeight

    val relativeCursorX = (cursorX - surfaceX)
    val relativeCursorY = (cursorY - surfaceY)

    return Pair(relativeCursorX, relativeCursorY)
}

// Share the coordinates
fun shareCursorData(cursorX: Float, cursorY: Float) {
    val normalizedCoords = calculateNormalizedCoordinates(cursorX, cursorY)
    val relativeX = normalizedCoords.first
    val relativeY = normalizedCoords.second
    val surfaceWidth = binding.bigVideoSurface.width
    val surfaceHeight = binding.bigVideoSurface.height

    // Send relative coordinates and dimensions
    toSendCoordsData(relativeX, relativeY, surfaceWidth.toFloat(), surfaceHeight.toFloat())
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun moveCursorCenter() {
    val screenHeight = binding.tileContainer.height
    val screenWidth = binding.tileContainer.width
    movingX = screenWidth.toFloat() / 2
    movingY = screenHeight.toFloat() / 2
    if (isCursorEnabled) {
        binding.cursor.x = movingX
        binding.cursor.y = movingY
    } else if (isCursorEnabled) {
        binding.cursorSmall.x = movingX
        binding.cursorSmall.y = movingY
    }

}

private fun moveSmallCursor(x: Float, y: Float) {
    Log.d("HomegetXy", "$x:$y")
    binding.cursorSmall.x = x - binding.cursorSmall.width / 2
    binding.cursorSmall.y = y - binding.cursorSmall.height / 2
    movingSmallX = x
    movingSmallY = y
    toSendSmallCoordsData()

}

fun toggleCursorMode() {
    // isCursorEnabled = !isCursorEnabled
    isCursorEnabled = true
    binding.apply {
        if (isCursorEnabled) {
            Log.d("Homecursorherre", "true")
            ibPointer.setImageResource(R.drawable.ic_selected_pointer)
            ibPointerLandscape.setImageResource(R.drawable.ic_selected_pointer)
            cursor.visibility = View.VISIBLE
            ivCross.visibility = View.VISIBLE
            blurViewSideBar.visibility = View.GONE
            tvProviderName.visibility = View.GONE
            tvProviderNameLandscape.visibility = View.GONE
            bottomViewVisibility(false)
            isVideoCompressed = true
            ibShowSmallView.visibility = View.VISIBLE
            clWorking.visibility = View.GONE
            videoSmall.visibility = View.GONE
            isToolListVisible = false
            blurViewSideBarLandscape.visibility = View.GONE
            ibTools.setImageResource(if (isToolListVisible) R.drawable.ic_cross else R.drawable.ic_tools)
            ibToolsLandScape.setImageResource(if (isToolListVisible) R.drawable.ic_cross else R.drawable.ic_tools)
            val centerX = binding.bigVideoSurface.width.toFloat() / 2
            val centerY = binding.bigVideoSurface.height.toFloat() / 2
            binding.cursor.x = centerX.toFloat() - binding.cursor.width / 2
            binding.cursor.y = centerY.toFloat() - binding.cursor.height / 2
            movingX = centerX.toFloat()
            movingY = centerY.toFloat()

//                } else {
//                    Log.d("cursorherre01","true")
//                    binding.cursor.x = movingX - binding.cursor.width / 2
//                    binding.cursor.y = movingY - binding.cursor.height / 2
//                    movingX = movingX
//                    movingY = movingY
//                }
            cursorSmall.visibility = View.GONE
        }
    }
}

fun toggleSmallCursorMode() {
    isSmallCursorEnabled = true
    binding.apply {
        if (isSmallCursorEnabled) {
            ibPointer.setImageResource(R.drawable.ic_selected_pointer)
            ibPointerLandscape.setImageResource(R.drawable.ic_selected_pointer)
            cursorSmall.visibility = View.VISIBLE
            ivCross.visibility = View.VISIBLE
            blurViewSideBar.visibility = View.GONE
            blurViewSideBarLandscape.visibility = View.GONE
            tvProviderNameLandscape.visibility = View.GONE
            bottomViewVisibility(false)
            isVideoCompressed = true
            ibShowSmallView.visibility = View.VISIBLE
            clWorking.visibility = View.GONE
            videoSmall.visibility = View.GONE
            isToolListVisible = false
            ibTools.setImageResource(if (isToolListVisible) R.drawable.ic_cross else R.drawable.ic_tools)
            ibToolsLandScape.setImageResource(if (isToolListVisible) R.drawable.ic_cross else R.drawable.ic_tools)

            //if (movingSmallX == 0F && movingSmallY == 0F) {
            val centerX = binding.videoSmall.width / 2
            val centerY = binding.videoSmall.height / 2
            binding.cursorSmall.x = centerX.toFloat() - binding.cursorSmall.width / 2
            binding.cursorSmall.y = centerY.toFloat() - binding.cursorSmall.height / 2
            movingSmallX = centerX.toFloat()
            movingSmallY = centerY.toFloat()

//               } else {
//                    binding.cursorSmall.x = movingSmallX - binding.cursorSmall.width / 2
//                    binding.cursorSmall.y = movingSmallY - binding.cursorSmall.height / 2
//                    movingSmallX = movingSmallX
//                    movingSmallY = movingSmallY
//                }
            cursor.visibility = View.GONE
        }
    }
}

fun toSendCoordsData(pointX: Float, pointY: Float, width: Float, height: Float) {
    val windowInsets =
        WindowInsetsCompat.toWindowInsetsCompat(window.decorView.rootWindowInsets)
    val statusBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top
    val navigationBarHeight =
        windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
    val displayMetrics = resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels
    val screenHeight = displayMetrics.heightPixels - statusBarHeight - navigationBarHeight
    bigViewWidth = binding.bigVideoSurface.width.toFloat()
    bigViewHeight = binding.bigVideoSurface.height.toFloat()
    var cursorPixel =
//            if (!remoteIsSmall && isPortrait && !isRemotePortrait) {
//                "cursorPixel[" + movingX.toFloat().toString() + "," + movingY.toFloat()
//                    .toString() + "," + bigViewWidth.toString() + "," + screenHeight.toString() + "]"
//            } else if (!remoteIsSmall && isPortrait && !isRemotePortrait) {
//                "cursorPixel[" + movingX.toFloat().toString() + "," + movingY.toFloat()
//                    .toString() + "," + screenWidth.toString() + "," + bigViewWidth.toString() + "]"
//            } else {
//                "cursorPixel[" + movingX.toFloat().toString() + "," + movingY.toFloat()
//                    .toString() + "," + bigViewWidth.toString() + "," + bigViewHeight.toString() + "]"
//            }
        "cursorPixel[" + pointX.toFloat().toString() + "," + pointY.toFloat()
            .toString() + "," + width.toString() + "," + height.toString() + "]"
    Log.d("HomecursorPixel", cursorPixel.toString())
    audioVideo.realtimeSendDataMessage("pointerDataTransfer", cursorPixel, 5000)
}

fun toSendSmallCoordsData() {

    smallViewWidth = binding.videoSmall.width.toFloat()
    smallViewHeight = binding.videoSmall.height.toFloat()
    val cursorPixel =
        "cursorPixel[" + movingSmallX.toFloat().toString() + "," + movingSmallY.toFloat()
            .toString() + "," + smallViewWidth.toString() + "," + smallViewHeight.toString() + "]"
    audioVideo.realtimeSendDataMessage("pointerDataTransfer", cursorPixel, 5000)
}

@RequiresApi(Build.VERSION_CODES.Q)
@SuppressLint("SuspiciousIndentation")
fun receiveCoordsData(coordinates: ArrayList<String>) {
    var relativeX = coordinates[0].toFloat()
    var relativeY = coordinates[1].toFloat()
    var oppositeWidth = coordinates[2].toFloat()
    var oppositeHeight = coordinates[3].toFloat()
    var relativeOppositeX = relativeX / oppositeWidth
    var relativeOppositeY = relativeY / oppositeHeight

    Log.d(
        "getViewDimen",
        binding.bigVideoSurface.width.toString() + " , " + binding.bigVideoSurface.height.toString()
    )
    //Log.d("getViewScreenDimen", "$screenWidth , $screenHeight")
    if (remoteIsSmall && name != "user") {

        val smallViewWidth = binding.videoSmall.width.toFloat()
        val smallViewHeight = binding.videoSmall.height.toFloat()
        val smallVideoSurfaceLocation =
            getRelativePosition(binding.cursorSmall, binding.flSmallVideo)
        val surfaceX = smallVideoSurfaceLocation.first
        var surfaceY = smallVideoSurfaceLocation.second
        movingSmallX = relativeOppositeX * smallViewWidth
        movingSmallY = relativeOppositeY * smallViewHeight
        binding.cursorSmall.visibility = View.VISIBLE
        isSmallCursorEnabled = true
        binding.ibPointer.setImageResource(R.drawable.ic_selected_pointer)
        binding.cursorSmall.x = surfaceX + movingSmallX - (binding.cursorSmall.width) / 2
        binding.cursorSmall.y = surfaceY + movingSmallY - (binding.cursorSmall.height) / 2
    } else if (!remoteIsSmall && name != "user") {
//            val bigViewWidth = binding.bigVideoSurface.width.toFloat()
//            val bigViewHeight = binding.bigVideoSurface.height.toFloat()
//            // Calculate the new cursor position based on the relative positions
//            val movingX = relativeOppositeX * bigViewWidth
//            val movingY = relativeOppositeY * bigViewHeight
//            // Get absolute screen coordinates of bigVideoSurface
//            val bigVideoSurfaceLocation = IntArray(2)
//            binding.bigVideoSurface.getLocationInWindow(bigVideoSurfaceLocation)
//            val surfaceX = bigVideoSurfaceLocation[0].toFloat()
//            var surfaceY = bigVideoSurfaceLocation[1].toFloat()
//            val statusBarHeight = getStatusBarHeight()
//            surfaceY -= statusBarHeight
//            // Position the cursor, centered, within bigVideoSurface's bounds
//            binding.cursor.visibility = View.VISIBLE
//            binding.cursor.x = surfaceX + movingX - (binding.cursor.width / 2)
//            binding.cursor.y = surfaceY + movingY - (binding.cursor.height / 2)
//            binding.ibPointer.setImageResource(R.drawable.ic_selected_pointer)


        // Get the dimensions of the receiving bigVideoSurface
        val receiverWidth = binding.bigVideoSurface.width.toFloat()
        val receiverHeight = binding.bigVideoSurface.height.toFloat()

        // Map relative coordinates to the receiving bigVideoSurface
        val mappedX = relativeOppositeX * receiverWidth
        val mappedY = relativeOppositeY * receiverHeight

        val bigVideoSurfaceLocation =
            getRelativePosition(binding.bigVideoSurface, binding.tileContainer)
        val surfaceX = bigVideoSurfaceLocation.first
        var surfaceY = bigVideoSurfaceLocation.second
        binding.cursor.visibility = View.VISIBLE
        // Set the cursor position relative to the new size and location
        binding.cursor.x = mappedX + surfaceX - binding.cursor.width / 2
        binding.cursor.y = mappedY + surfaceY - binding.cursor.height / 2
        binding.ibPointer.setImageResource(R.drawable.ic_selected_pointer)
    } else if (remoteIsSmall && name == "user") {

        val bigViewWidth = binding.bigVideoSurface.width.toFloat()
        val bigViewHeight = binding.bigVideoSurface.height.toFloat()
        // Calculate the new cursor position based on the relative positions
        val movingX = relativeOppositeX * bigViewWidth
        val movingY = relativeOppositeY * bigViewHeight
        // Get absolute screen coordinates of bigVideoSurface
        val bigVideoSurfaceLocation =
            getRelativePosition(binding.bigVideoSurface, binding.tileContainer)
        val surfaceX = bigVideoSurfaceLocation.first
        var surfaceY = bigVideoSurfaceLocation.second


//            val statusBarHeight = getStatusBarHeight()
//            surfaceY -= statusBarHeight
        // Position the cursor, centered, within bigVideoSurface's bounds
        binding.cursor.visibility = View.VISIBLE
        binding.cursor.x = surfaceX + movingX - (binding.cursor.width / 2)
        binding.cursor.y = surfaceY + movingY - (binding.cursor.height / 2)
        binding.ibPointer.setImageResource(R.drawable.ic_selected_pointer)

//            val layoutParams = binding.bigVideoSurface.layoutParams as ViewGroup.MarginLayoutParams
//            val offsetY = layoutParams.topMargin.toFloat()
//            val offsetX = layoutParams.marginStart.toFloat()
//            val bigViewWidth = binding.bigVideoSurface.width.toFloat()
//            val bigViewHeight = binding.bigVideoSurface.height.toFloat()
//            movingX = relativeOppositeX * bigViewWidth + offsetX
//            movingY = relativeOppositeY * bigViewHeight + offsetY
//            binding.cursor.visibility = View.VISIBLE
//            isCursorEnabled = true
//            binding.cursor.x = movingX - (binding.cursor.width) / 2
//            binding.cursor.y = movingY - (binding.cursor.height) / 2
//            binding.ibPointer.setImageResource(R.drawable.ic_selected_pointer)
        //audioVideo.realtimeSendDataMessage("pointerAdded", "true", 5000)

    } else if (!remoteIsSmall && name == "user") {
//            val layoutParams = binding.videoSmall.layoutParams as ViewGroup.MarginLayoutParams
//            val offsetY = layoutParams.topMargin.toFloat()
//            val offsetX = layoutParams.marginStart.toFloat()
        val smallViewWidth = binding.videoSmall.width.toFloat()
        val smallViewHeight = binding.videoSmall.height.toFloat()
        val smallVideoSurfaceLocation =
            getRelativePosition(binding.cursorSmall, binding.flSmallVideo)
        val surfaceX = smallVideoSurfaceLocation.first
        var surfaceY = smallVideoSurfaceLocation.second
        movingSmallX = relativeOppositeX * smallViewWidth
        movingSmallY = relativeOppositeY * smallViewHeight
        binding.cursorSmall.visibility = View.VISIBLE
        isSmallCursorEnabled = true
        binding.ibPointer.setImageResource(R.drawable.ic_selected_pointer)
        //audioVideo.realtimeSendDataMessage("pointerAdded", "true", 5000)
        binding.cursorSmall.x = surfaceX + movingSmallX - (binding.cursorSmall.width) / 2
        binding.cursorSmall.y = surfaceY + movingSmallY - (binding.cursorSmall.height) / 2
    }
}

fun getRelativePosition(childView: View, parentView: View): Pair<Float, Float> {
    // Get the global position of the child view
    val childLocation = IntArray(2)
    childView.getLocationInWindow(childLocation)

    // Get the global position of the parent view
    val parentLocation = IntArray(2)
    parentView.getLocationInWindow(parentLocation)

    // Calculate the relative position
    val relativeX = (childLocation[0] - parentLocation[0]).toFloat()
    val relativeY = (childLocation[1] - parentLocation[1]).toFloat()

    return Pair(relativeX, relativeY)
}

fun removePointer() {
    movingX = 0f
    movingY = 0f
    movingSmallX = 0f
    movingSmallY = 0f
    isCursorEnabled = false
    isSmallCursorEnabled = false
    binding.cursor.visibility = View.GONE
    binding.cursorSmall.visibility = View.GONE
}

private fun getStatusBarHeight(): Int {

//        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
//            val statusBarHeight = insets.systemWindowInsetTop
//            surfaceY -= statusBarHeight
//            insets
//        }
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
}

private fun createVideoCollectionTile(tileState: VideoTileState): VideoCollectionTile {

    val attendeeId = tileState.attendeeId
    val attendeeName = meetingModel.currentRoster[attendeeId]?.attendeeName ?: ""
    if (!tileState.isLocalTile) {
        Log.d("HomegetValueName", attendeeName.toString())
    }
    return VideoCollectionTile(attendeeName, tileState)
}

private fun showVideoTile(tileState: VideoTileState) {
    val videoCollectionTile = createVideoCollectionTile(tileState)
    if (tileState.isLocalTile) {
        meetingModel.localVideoTileState = videoCollectionTile
        updateSingleVideoView()
        subscribeToRemoteVideosInCurrentPage()
    } else {
        meetingModel.addRemoteVideoTileState(videoCollectionTile)
        updateSingleVideoView()
        binding.tvProviderName.text = videoCollectionTile.attendeeName
        binding.tvProviderNameLandscape.text = videoCollectionTile.attendeeName
        audioVideo.bindVideoView(binding.videoSmall, tileState.tileId)
        subscribeToRemoteVideosInCurrentPage()

    }
}


private fun subscribeToRemoteVideosInCurrentPage() {
    meetingModel.updateVideoStatesInCurrentPage()

    val videoSourcesInCurrentPage = meetingModel.getRemoteVideoSourcesInCurrentPage()
    audioVideo.updateVideoSourceSubscriptions(videoSourcesInCurrentPage, emptyArray())
    updateSingleVideoView()
}


private fun updateSingleVideoView() {
    Log.d("HomeComing01", "coming here")
    // Handle local video view
    meetingModel.localVideoTileState?.let { localTile ->
        audioVideo.resumeRemoteVideoTile(localTile.videoTileState.tileId)
        updateLocalView(localTile)  // A method to bind this tile to the local view
    }
    if (meetingModel.getRemoteVideoTileStates().isNotEmpty()) {
        Log.d("HomeComing02", "coming here")
        meetingModel.getRemoteVideoTileStates()[0].let {
            Log.d("HomeComing03", "coming here")
            audioVideo.resumeRemoteVideoTile(it.videoTileState.tileId)
            updateRemoteView(it)
        }
    }


//        val remoteTile = meetingModel.remoteVideoTiles.firstOrNull() // Assuming this is an array or list of remote tiles
//        remoteTile?.let {
//            // Assuming 'it' is the remote video tile and has a method to retrieve the RemoteVideoSource
//            val remoteSource = it.videoTileState.remoteVideoSource
//
//            // Ensure remoteSource is not null before proceeding
//            remoteSource?.let { source ->
//                // Bind the first remote video tile to the remote view
//                audioVideo.bindVideoView(remoteVideoView, it.videoTileState.tileId)
//                audioVideo.resumeRemoteVideoTile(it.videoTileState.tileId)
//
//                // Retrieve the configuration for the identified remote source
//                val config = meetingModel.getRemoteVideoSourceConfigurations()[source]
//                if (config != null) {
//                    val updatedSources = mapOf(source to config)
//                    audioVideo.updateVideoSourceSubscriptions(updatedSources, emptyArray())
//                } else {
//                    // Handle case where config might be null
//                    Log.e("VideoUpdate", "No configuration found for remote source: $source")
//                }
//
//                updateRemoteView(it) // Custom method to set up the remote view
//        }


}

private fun updateLocalView(localTile: VideoCollectionTile) {
    if (localIsSmall) {
        audioVideo.bindVideoView(binding.videoSmall, localTile.videoTileState.tileId)
    } else {
        //binding.bigVideoSurface.mirror = true
        audioVideo.bindVideoView(binding.bigVideoSurface, localTile.videoTileState.tileId)
    }
}

private fun updateRemoteView(remoteTile: VideoCollectionTile) {
    Log.d("HomeGetRemoteIsSmall", remoteIsSmall.toString())
    if (remoteIsSmall) {
        audioVideo.bindVideoView(binding.videoSmall, remoteTile.videoTileState.tileId)
    } else {
        audioVideo.bindVideoView(binding.bigVideoSurface, remoteTile.videoTileState.tileId)
    }


}

@RequiresApi(Build.VERSION_CODES.Q)
private fun setupPortraitButtonsBar() {
    binding.apply {
        ibMic.setImageResource(if (meetingModel.isMuted) R.drawable.ic_selected_mic else R.drawable.ic_mic)
        ibMic.setOnClickListener { toggleMute() }
        ibPointer.setOnClickListener {

            if (remoteIsSmall && name == "user") {
                toggleCursorMode()
            } else if (remoteIsSmall && name != "user") {
                toggleSmallCursorMode()
            } else if (!remoteIsSmall && name == "user") {
                toggleSmallCursorMode()
            } else if (!remoteIsSmall && name != "user") {
                toggleCursorMode()
            }

            if (isSmallCursorEnabled == true || isCursorEnabled == true) {
                audioVideo.realtimeSendDataMessage("pointerAdded", "", 5000)
                pointerAddedLocally()
            }

        }
        ivCross.setOnClickListener {
            clearCross()
            audioVideo.realtimeSendDataMessage("pointerRemoved", "true", 5000)
        }
        ivDisconnect.setOnClickListener { endMeeting() }
        ibScreenshot.setOnClickListener { filePermissionCheck() }
        ivDisconnectBottom.setOnClickListener { endMeeting() }
        ibOpenBottomOptions.setOnClickListener {
            bottomViewVisibility(false)
            tvProviderName.visibility = View.GONE
        }
        ivBottomOptions.setOnClickListener {
            bottomViewVisibility(true)
            tvProviderName.visibility = View.VISIBLE
        }
        ibTools.setOnClickListener {
            if (isToolListVisible) {
                isToolListVisible = false
                blurViewSideBar.visibility = View.GONE
            } else {
                isToolListVisible = true
                blurViewSideBar.visibility = View.VISIBLE
            }
            ibTools.setImageResource(if (isToolListVisible) R.drawable.ic_cross else R.drawable.ic_tools)

        }
        ibSwitchCamera.setOnClickListener {
            if (audioVideo.getActiveCamera() != null) {
                audioVideo.switchCamera()
            } else {
                cameraCaptureSource?.switchCamera()
            }
            updateLocalVideoMirror()

        }
    }
}


@RequiresApi(Build.VERSION_CODES.Q)
private fun setupLandscapeButtonsBar() {
    binding.apply {
        ibMicLandScape.setImageResource(if (meetingModel.isMuted) R.drawable.ic_selected_mic else R.drawable.ic_mic)
        ibMicLandScape.setOnClickListener { toggleMute() }
        ibPointerLandscape.setOnClickListener {

            if (remoteIsSmall && name == "user") {
                toggleCursorMode()
            } else if (remoteIsSmall && name != "user") {
                toggleSmallCursorMode()
            } else if (!remoteIsSmall && name == "user") {
                toggleSmallCursorMode()
            } else if (!remoteIsSmall && name != "user") {
                toggleCursorMode()
            }

            if (isSmallCursorEnabled == true || isCursorEnabled == true) {
                audioVideo.realtimeSendDataMessage("pointerAdded", "true", 5000)
                pointerAddedLocally()
            }
        }
        ivDisconnectLandScape.setOnClickListener { endMeeting() }
        ibScreenshotLandscape.setOnClickListener { filePermissionCheck() }
        ivDisconnectBottomLandScape.setOnClickListener { endMeeting() }
        ibOpenBottomOptionsLandScape.setOnClickListener {
            bottomViewVisibility(false)
        }
        ivBottomOptionsLandScape.setOnClickListener {
            bottomViewVisibility(true)
        }
        ibToolsLandScape.setOnClickListener {
            if (isToolListVisible) {
                isToolListVisible = false
                blurViewSideBarLandscape.visibility = View.GONE
                tvProviderNameLandscape.visibility = View.GONE
            } else {
                isToolListVisible = true
                blurViewSideBarLandscape.visibility = View.VISIBLE
                tvProviderNameLandscape.visibility = View.VISIBLE
            }
            ibToolsLandScape.setImageResource(if (isToolListVisible) R.drawable.ic_cross else R.drawable.ic_tools)

        }
        ibSwitchCameraLandScape.setOnClickListener {
            if (audioVideo.getActiveCamera() != null) {
                audioVideo.switchCamera()
            } else {
                cameraCaptureSource?.switchCamera()
            }
            updateLocalVideoMirror()
        }
    }
}

fun clearCross() {
    isCursorEnabled = false
    isSmallCursorEnabled = false
    binding.apply {
        ibPointer.setImageResource(R.drawable.ic_pointer)
        ibPointerLandscape.setImageResource(R.drawable.ic_pointer)
        cursor.visibility = View.GONE
        ivCross.visibility = View.GONE
        cursorSmall.visibility = View.GONE
    }

}


private fun bottomViewVisibility(boolean: Boolean) {
    binding.apply {
        if (boolean) {
            isBottomBarVisible = true
            ivDisconnect.visibility = View.GONE
            ivDisconnectLandScape.visibility = View.GONE
            ivBottomOptions.visibility = View.GONE
            ivBottomOptionsLandScape.visibility = View.GONE
            blurViewBottom.visibility = View.VISIBLE
            blurViewBottomLandScape.visibility = View.VISIBLE
            ibOpenBottomOptions.visibility = View.VISIBLE
            ibOpenBottomOptionsLandScape.visibility = View.VISIBLE


        } else {
            isBottomBarVisible = false
            ivDisconnect.visibility = View.VISIBLE
            ivDisconnectLandScape.visibility = View.VISIBLE
            ivBottomOptions.visibility = View.VISIBLE
            ivBottomOptionsLandScape.visibility = View.VISIBLE
            blurViewBottom.visibility = View.GONE
            blurViewBottomLandScape.visibility = View.GONE
            ibOpenBottomOptions.visibility = View.GONE
            ibOpenBottomOptionsLandScape.visibility = View.GONE
        }
    }
}

private fun updateLocalVideoMirror() {
    if (!localIsSmall) {
        binding.bigVideoSurface.mirror =
                // If we are using internal source, base mirror state off that device type
            (audioVideo.getActiveCamera()?.type == MediaDeviceType.VIDEO_FRONT_CAMERA ||
                    // Otherwise (audioVideo.getActiveCamera() == null) use the device type of our external/custom camera capture source
                    (audioVideo.getActiveCamera() == null && cameraCaptureSource?.device?.type == MediaDeviceType.VIDEO_FRONT_CAMERA))
        audioVideo.realtimeSendDataMessage(
            "cameraToggle",
            binding.bigVideoSurface.mirror.toString(),
            5000
        )
    } else {
        binding.videoSmall.mirror =
                // If we are using internal source, base mirror state off that device type
            (audioVideo.getActiveCamera()?.type == MediaDeviceType.VIDEO_FRONT_CAMERA ||
                    // Otherwise (audioVideo.getActiveCamera() == null) use the device type of our external/custom camera capture source
                    (audioVideo.getActiveCamera() == null && cameraCaptureSource?.device?.type == MediaDeviceType.VIDEO_FRONT_CAMERA))
        audioVideo.realtimeSendDataMessage(
            "cameraToggle",
            binding.videoSmall.mirror.toString(),
            5000
        )

    }
}

private var isCameraToogle = "false"
private fun updateRemoteVideoMirror() {
    if (isCameraToogle == "true") {
        if (remoteIsSmall) {
            binding.videoSmall.mirror = true
        } else {
            binding.bigVideoSurface.mirror = true
        }
    } else {
        if (remoteIsSmall) {
            binding.videoSmall.mirror = false

        } else {
            binding.bigVideoSurface.mirror = false
        }
    }
}

private val SCREEN_RECORD_REQUEST_CODE = 100
private var mProjectionManager: MediaProjectionManager? = null
private var saveDataIntent: Intent? = null

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
private fun startRecordingScreen() {
    val resultIntent = Utils.SAVE_RESULT
    val getResultCode = Utils.RESULT_CODE
    val intent = MediaProjectionIntentHolder.intent
    try {
        Log.d("HomeGetRecordResult", "data:" + resultIntent.toString())
        Log.d("HomeGetRecordResultCode", "data:" + getResultCode.toString())
//            if (intent != null) {
//                Log.d("HomeGetRecordResultInside", "data:" + resultIntent.toString())
//                recordingStart = true
//                hbRecorder.startScreenRecording(intent, getResultCode!!)
////                val handler = Handler(Looper.getMainLooper())
////                handler.postDelayed({
////                    hbRecorder.stopScreenRecording()
////                }, 12000)
//
//
//            } else {
        Log.d("HomeGetRecordResultTesting", "data:")
        // Handler(Looper.getMainLooper()).post { transparentSceneView?.pause() }
        val mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val permissionIntent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(permissionIntent, SCREEN_RECORD_REQUEST_CODE)
        //       }

    } catch (e: URISyntaxException) {

        e.printStackTrace()
    }
}

object MediaProjectionIntentHolder {
    var intent: Intent? = null
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == SCREEN_RECORD_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
        //Start screen recording
        Utils.M_PROJECTION = mProjectionManager?.getMediaProjection(resultCode, data!!)
        val uriString = data?.toUri(0)
        saveDataIntent = data

        Log.d("HomegetRecordingData", "data:" + saveDataIntent.toString())
        Log.d("HomeGetRecordResultCodeInside", "data:$resultCode")
        Utils.SAVE_RESULT = data
        Utils.RESULT_CODE = resultCode
        MediaProjectionIntentHolder.intent = data
        recordingStart = true
        Log.d("HomeprintUri", "result:" + uriString.toString())
        hbRecorder.startScreenRecording(data, resultCode)
//            val handler = Handler(Looper.getMainLooper())
//            handler.postDelayed({
//                hbRecorder.stopScreenRecording()
//            }, 12000)
    }

}


private fun endMeeting() {
    hbRecorder.stopScreenRecording()
    if (meetingModel.localVideoTileState != null) {
        audioVideo.unbindVideoView(meetingModel.localTileId)
    }
    meetingModel.getRemoteVideoTileStates().forEach {
        audioVideo.unbindVideoView(it.videoTileState.tileId)
    }
    meetingModel.currentScreenTiles.forEach {
        audioVideo.unbindVideoView(it.videoTileState.tileId)
    }
    audioVideo.stopLocalVideo()
    audioVideo.stopContentShare()
    audioVideo.stopRemoteVideo()
    audioVideo.stop()
    audioVideo.realtimeSendDataMessage("endMeeting", "", 5000)
    onBackPressedDispatcher.onBackPressed()
}

override fun onStop() {
    super.onStop()
    meetingModel.wasLocalVideoStarted = meetingModel.isLocalVideoStarted
    if (meetingModel.wasLocalVideoStarted) {
        stopLocalVideo()
    }
    audioVideo.stopRemoteVideo()
}


private fun toggleMute() {
    if (meetingModel.isMuted) {
        audioVideo.realtimeLocalMute()
        binding.ibMic.setImageResource(R.drawable.ic_mic)
        binding.ibMicLandScape.setImageResource(R.drawable.ic_mic)
    } else {
        audioVideo.realtimeLocalUnmute()
        binding.ibMic.setImageResource(R.drawable.ic_selected_mic)
        binding.ibMicLandScape.setImageResource(R.drawable.ic_selected_mic)
    }
    meetingModel.isMuted = !meetingModel.isMuted
}


//    private fun toggleAdditionalOptionsMenu() {
//        refreshAdditionalOptionsDialogItems()
//        additionalOptionsAlertDialogBuilder.create()
//        additionalOptionsAlertDialogBuilder.show()
//        meetingModel.isAdditionalOptionsDialogOn = true
//    }

private fun startLocalVideo() {
    if (!meetingModel.isCameraSendAvailable) {
        Log.d("HomeCameraCheck", "11")
        meetingModel.isLocalVideoStarted = true
        val localVideoConfig = LocalVideoConfiguration(meetingModel.localVideoMaxBitRateKbps)
        if (meetingModel.isUsingCameraCaptureSource) {
            Log.d("HomeCameraCheck", "22")
            if (meetingModel.isUsingGpuVideoProcessor) {
                Log.d("HomeCameraCheck", "33")
                cameraCaptureSource.addVideoSink(gpuVideoProcessor)
                audioVideo.startLocalVideo(gpuVideoProcessor, localVideoConfig)
            } else if (meetingModel.isUsingCpuVideoProcessor) {
                Log.d("HomeCameraCheck", "44")
                cameraCaptureSource.addVideoSink(cpuVideoProcessor)
                audioVideo.startLocalVideo(cpuVideoProcessor, localVideoConfig)
            } else if (meetingModel.isUsingBackgroundBlur) {
                Log.d("HomeCameraCheck", "55")
                cameraCaptureSource.addVideoSink(backgroundBlurVideoFrameProcessor)
                audioVideo.startLocalVideo(backgroundBlurVideoFrameProcessor, localVideoConfig)
            } else if (meetingModel.isUsingBackgroundReplacement) {
                Log.d("HomeCameraCheck", "66")
                cameraCaptureSource.addVideoSink(backgroundReplacementVideoFrameProcessor)
                audioVideo.startLocalVideo(
                    backgroundReplacementVideoFrameProcessor,
                    localVideoConfig
                )
            } else {
                Log.d("HomeCameraCheck", "77")
                audioVideo.startLocalVideo(cameraCaptureSource, localVideoConfig)
            }
            Log.d("HomeCameraCheck", "88")

            cameraCaptureSource.start()
        } else {
            Log.d("HomeCameraCheck", "99")
            audioVideo.startLocalVideo(localVideoConfig)
        }
        if (audioVideo.getActiveCamera() != null) {
            audioVideo.switchCamera()
        } else {
            cameraCaptureSource?.switchCamera()
        }
        updateLocalVideoMirror()
        //buttonCamera.setImageResource(R.drawable.button_camera_on)
    } else {
        Log.d("HomeCameraCheck", "10")
    }
}


private fun stopLocalVideo() {
    meetingModel.isLocalVideoStarted = false
    if (meetingModel.isUsingCameraCaptureSource) {
        cameraCaptureSource.stop()
    }
    audioVideo.stopLocalVideo()
    //buttonCamera.setImageResource(R.drawable.button_camera)
}


private val DATA_MESSAGE_TOPIC = "chat"
private fun setupAudioVideoFacadeObservers() {
    audioVideo.addAudioVideoObserver(this)
    audioVideo.addDeviceChangeObserver(this)
    audioVideo.addMetricsObserver(this)
    audioVideo.addRealtimeObserver(this)
    audioVideo.addRealtimeDataMessageObserver(DATA_MESSAGE_TOPIC, this)
    audioVideo.addRealtimeDataMessageObserver("landscape", this)
    audioVideo.addRealtimeDataMessageObserver("portrait", this)
    audioVideo.addRealtimeDataMessageObserver("incrementTimer", this)
    audioVideo.addRealtimeDataMessageObserver("endMeeting", this)
    audioVideo.addRealtimeDataMessageObserver("pointerDataTransfer", this)
    audioVideo.addRealtimeDataMessageObserver("pointerRemoved", this)
    audioVideo.addRealtimeDataMessageObserver("pointerAdded", this)
    audioVideo.addRealtimeDataMessageObserver("incrementTimerRequested", this)
    audioVideo.addRealtimeDataMessageObserver("incrementTimerApproved", this)
    audioVideo.addRealtimeDataMessageObserver("cameraToggle", this)
    audioVideo.addVideoTileObserver(this)
    audioVideo.addActiveSpeakerObserver(DefaultActiveSpeakerPolicy(), this)
    audioVideo.addContentShareObserver(this)
    audioVideo.addEventAnalyticsObserver(this)
    audioVideo.addRealtimeTranscriptEventObserver(this)
}

private fun removeAudioVideoFacadeObservers() {
    audioVideo.removeAudioVideoObserver(this)
    audioVideo.removeDeviceChangeObserver(this)
    audioVideo.removeMetricsObserver(this)
    audioVideo.removeRealtimeObserver(this)
    audioVideo.removeRealtimeDataMessageObserverFromTopic(DATA_MESSAGE_TOPIC)
    audioVideo.removeRealtimeDataMessageObserverFromTopic("landscape")
    audioVideo.removeRealtimeDataMessageObserverFromTopic("portrait")
    audioVideo.removeRealtimeDataMessageObserverFromTopic("incrementTimer")
    audioVideo.removeRealtimeDataMessageObserverFromTopic("endMeeting")
    audioVideo.removeRealtimeDataMessageObserverFromTopic("pointerDataTransfer")
    audioVideo.removeRealtimeDataMessageObserverFromTopic("pointerRemoved")
    audioVideo.removeRealtimeDataMessageObserverFromTopic("pointerAdded")
    audioVideo.removeRealtimeDataMessageObserverFromTopic("incrementTimerRequested")
    audioVideo.removeRealtimeDataMessageObserverFromTopic("incrementTimerApproved")
    audioVideo.removeRealtimeDataMessageObserverFromTopic("cameraToggle")
    audioVideo.removeVideoTileObserver(this)
    audioVideo.removeActiveSpeakerObserver(this)
    audioVideo.removeContentShareObserver(this)
    audioVideo.removeRealtimeTranscriptEventObserver(this)

}

override fun onDestroy() {
    super.onDestroy()
    removeAudioVideoFacadeObservers()
    if (meetingModel.isMicrophoneServiceBound) {
        unbindService(meetingModel.microphoneServiceConnection)
        meetingModel.isMicrophoneServiceBound = false
    }
}


private fun getAudioVideo(): AudioVideoFacade = meetingSessionModel.audioVideo


private fun getMeetingSessionCredentials(): MeetingSessionCredentials =
    meetingSessionModel.credentials

private fun getEglCoreFactory(): EglCoreFactory = meetingSessionModel.eglCoreFactory

private fun getCameraCaptureSource(): CameraCaptureSource =
    meetingSessionModel.cameraCaptureSource

private fun getGpuVideoProcessor(): GpuVideoProcessor = meetingSessionModel.gpuVideoProcessor

private fun getCpuVideoProcessor(): CpuVideoProcessor = meetingSessionModel.cpuVideoProcessor
private fun makeViewBlur(blurView: BlurView, radius: Float) {
    val decorView = window.decorView as ViewGroup
    val windowBackground: Drawable = window.decorView.background
    blurView.setupWith(decorView) // Root view of the layout
        .setFrameClearDrawable(windowBackground)
        .setBlurRadius(radius)
        .setBlurAutoUpdate(true)


    blurView.apply {
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(
                    0,
                    0,
                    view.width,
                    view.height,
                    radius
                )
            }
        }
        clipToOutline = true
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun commonListeners() {
    binding.apply {
        ivVideoCompress.setOnClickListener {
            isVideoCompressed = true
            ibShowSmallView.visibility = View.VISIBLE
            clWorking.visibility = View.GONE
            videoSmall.visibility = View.GONE

        }
        ibShowSmallView.setOnClickListener {
            isVideoCompressed = false
            ibShowSmallView.visibility = View.GONE
            clWorking.visibility = View.VISIBLE
            videoSmall.visibility = View.VISIBLE
        }
        blurViewTimer.setOnClickListener {
            startRecordingScreen()
        }
        ivVideoExpand.setOnClickListener {
            expandView()
        }
        ibChat.setOnClickListener {
            if (isPortrait) {
                showBottomSheet()
            } else {
                Toast.makeText(
                    this@HomeActivity,
                    "To access Chat, please switch to portrait mode.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        ibChatLandScape.setOnClickListener {
            if (isPortrait) {
                showBottomSheet()
            } else {
                Toast.makeText(
                    this@HomeActivity,
                    "To access Chat, please switch to portrait mode.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnYes.setOnClickListener {
            if (isProviderDialog) {
                isInExtraTime = true
                audioVideo.realtimeSendDataMessage("incrementTimerApproved", "true", 5000)
            } else {
                audioVideo.realtimeSendDataMessage("incrementTimerRequested", "true", 5000)
            }
            blurViewExtend.visibility = View.GONE
        }
        btnNo.setOnClickListener {
            isInExtraTime = false
            blurViewExtend.visibility = View.GONE
        }

    }
}

@RequiresApi(Build.VERSION_CODES.Q)
fun expandView() {
    localIsSmall = !localIsSmall
    remoteIsSmall = !remoteIsSmall
    if (isCursorEnabled || isSmallCursorEnabled) {
        if (isCursorEnabled) {
            val fullWidth = binding.bigVideoSurface.width.toFloat()
            val fullHeight = binding.bigVideoSurface.height.toFloat()
            val relativeOppositeX = movingX / fullWidth
            val relativeOppositeY = movingY / fullHeight
            val viewWidth = binding.videoSmall.width
            val viewHeight = binding.videoSmall.height
            movingSmallX = relativeOppositeX * viewWidth
            movingSmallY = relativeOppositeY * viewHeight
            clearCross()
            audioVideo.realtimeSendDataMessage("pointerRemoved", "true", 5000)
            toggleSmallCursorMode()
            audioVideo.realtimeSendDataMessage("pointerAdded", "true", 5000)
            pointerAddedLocally()

        } else if (isSmallCursorEnabled) {
            val viewWidth = binding.videoSmall.width
            val viewHeight = binding.videoSmall.height
            val relativeOppositeX = movingSmallX / viewWidth
            val relativeOppositeY = movingSmallY / viewHeight
            val fullWidth = binding.bigVideoSurface.width.toFloat()
            val fullHeight = binding.bigVideoSurface.height.toFloat()
            movingX = relativeOppositeX * fullWidth
            movingY = relativeOppositeY * fullHeight
            clearCross()
            audioVideo.realtimeSendDataMessage("pointerRemoved", "true", 5000)
            toggleCursorMode()
            audioVideo.realtimeSendDataMessage("pointerAdded", "true", 5000)
            pointerAddedLocally()
        }

    } else {
        if (!isCursorEnabled) {
            binding.cursor.visibility = View.GONE
        }
        if (!isSmallCursorEnabled) {
            binding.cursor.visibility = View.GONE
        }
    }

    setVideoSize(localIsSmall, isPortrait, isRemotePortrait)
    setVideoContentMode()
    updateSingleVideoView()
}


@RequiresApi(Build.VERSION_CODES.Q)
override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 30+ (API 30 and above)
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            // Below Android 30
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN // Hides the status bar
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY // Ensures it stays hidden
                    )
        }
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 30+ (API 30 and above)
            window.insetsController?.show(WindowInsets.Type.statusBars())
        } else {
            // Below Android 30
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE // Show status bar
        }
    }
    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        Log.d("HomeOrientation", "Landscape mode")
        val rotation = windowManager.defaultDisplay.rotation
        when (rotation) {
            Surface.ROTATION_90 -> {
                Log.d("HomeOrientation", "Landscape - Left")
                isPortrait = false
                audioVideo.realtimeSendDataMessage(
                    "landscape",
                    "",
                    5000
                )
                if (isCursorEnabled || isSmallCursorEnabled) {
                    pointerAddedLocally()
                }
                setVideoSize(localIsSmall, isPortrait, isRemotePortrait)
                setVideoContentMode()
                binding.tvProviderName.visibility = View.GONE
                updateConstraintsForLandscapeLeft()
                binding.clBottomViewPortrait.visibility = View.GONE
                binding.blurViewSideBar.visibility = View.GONE
                binding.clBottomViewLandscape.visibility = View.VISIBLE
                if (isToolListVisible) {

                    binding.blurViewSideBarLandscape.visibility = View.VISIBLE
                    binding.tvProviderNameLandscape.visibility = View.VISIBLE
                } else {
                    binding.blurViewSideBarLandscape.visibility = View.GONE
                    binding.tvProviderNameLandscape.visibility = View.GONE
                }
                if (isBottomBarVisible) {
                    bottomViewVisibility(true)
                } else {
                    bottomViewVisibility(false)
                }

            }

            Surface.ROTATION_270 -> {
                Log.d("HomeOrientation", "Landscape - Right")
                isPortrait = false
                audioVideo.realtimeSendDataMessage(
                    "landscape",
                    "",
                    5000
                )

                if (isCursorEnabled || isSmallCursorEnabled) {
                    pointerAddedLocally()
                }

                setVideoSize(localIsSmall, isPortrait, isRemotePortrait)
                setVideoContentMode()
                binding.tvProviderName.visibility = View.GONE
                updateConstraintsForLandscapeRight()
                binding.clBottomViewPortrait.visibility = View.GONE
                binding.blurViewSideBar.visibility = View.GONE
                binding.clBottomViewLandscape.visibility = View.VISIBLE
                if (isToolListVisible) {
                    binding.blurViewSideBarLandscape.visibility = View.VISIBLE
                    binding.tvProviderNameLandscape.visibility = View.VISIBLE
                } else {
                    binding.blurViewSideBarLandscape.visibility = View.GONE
                    binding.tvProviderNameLandscape.visibility = View.GONE
                }
                if (isBottomBarVisible) {
                    bottomViewVisibility(true)
                } else {
                    bottomViewVisibility(false)
                }
                audioVideo.realtimeSendDataMessage(
                    "landscape",
                    "",
                    5000
                )

            }

            else -> {
                Log.d("HomeOrientation", "Landscape - Unknown rotation")
            }
        }
    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        Log.d("HomeOrientation", "Portrait")
        isPortrait = true

        if (isCursorEnabled || isSmallCursorEnabled) {
            pointerAddedLocally()
            audioVideo.realtimeSendDataMessage("pointerAdded", "", 5000)
        }
        audioVideo.realtimeSendDataMessage("portrait", "", 5000)
        setVideoSize(localIsSmall, isPortrait, isRemotePortrait)
        setVideoContentMode()
        updateConstraintsForPortrait()
        binding.clBottomViewLandscape.visibility = View.GONE
        binding.blurViewSideBarLandscape.visibility = View.GONE
        binding.tvProviderNameLandscape.visibility = View.GONE
        binding.clBottomViewPortrait.visibility = View.VISIBLE
        if (isToolListVisible) {
            binding.blurViewSideBar.visibility = View.VISIBLE
            binding.tvProviderName.visibility = View.VISIBLE
        } else {
            binding.blurViewSideBar.visibility = View.GONE
            binding.tvProviderName.visibility = View.GONE
        }
        if (isBottomBarVisible) {
            bottomViewVisibility(true)
        } else {
            bottomViewVisibility(false)
        }

//                rotationOfView(0f, "portrait")
//                commonViewsRotation(0f)
//                binding.blurViewBottom.scaleY = 1f
//                binding.blurViewBottom.scaleX = 1f


    }

}

@RequiresApi(Build.VERSION_CODES.Q)
fun pointerAddedLocally() {
    binding.ivCross.visibility = View.VISIBLE
    if (name == "user") {
        Log.d("HomePoint6", "working")
        if (localIsSmall) {
            isSmallCursorEnabled = true
            moveCursorCenter()
            binding.apply {
                cursor.visibility = View.GONE
                cursorSmall.visibility = View.VISIBLE
            }
        } else {
            isCursorEnabled = true
            moveCursorCenter()
            binding.apply {
                cursor.visibility = View.VISIBLE
                cursorSmall.visibility = View.GONE
            }
        }

    } else {
        Log.d("HomePoint7", "working")
        if (remoteIsSmall) {
            isSmallCursorEnabled = true
            moveCursorCenter()
            binding.apply {
                cursor.visibility = View.GONE
                cursorSmall.visibility = View.VISIBLE
            }
        } else {
            isCursorEnabled = true
            moveCursorCenter()
            binding.apply {
                cursor.visibility = View.VISIBLE
                cursorSmall.visibility = View.GONE
            }
        }
    }

}

override fun onResume() {
    super.onResume()
    if (!meetingModel.isLocalVideoStarted) {
        audioVideo.startLocalVideo()
        meetingModel.isLocalVideoStarted = true
    }

    // Update video source subscriptions for remote participants
    val updatedSources = meetingModel.getRemoteVideoSourceConfigurations()
    audioVideo.updateVideoSourceSubscriptions(updatedSources, emptyArray())

    // Resume video rendering for any active remote video tiles
    meetingModel.getRemoteVideoTileStates().forEach { tile ->
        audioVideo.resumeRemoteVideoTile(tile.videoTileState.tileId)
        updateRemoteView(tile)
    }
    if (saveDataIntent != null) {
        Utils.SAVE_RESULT = saveDataIntent
    }
}

override fun onPause() {
    super.onPause()
    if (Utils.SAVE_RESULT != null) {
        saveDataIntent = Utils.SAVE_RESULT
    }
}

private fun updateConstraintsForPortrait() {
    val constraintSet = ConstraintSet().apply {
        clone(binding.tileContainer)

        connect(
            binding.clWorking.id,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP
        )
        connect(
            binding.clWorking.id,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END
        )
        setMargin(binding.clWorking.id, ConstraintSet.TOP, 35)
        setMargin(binding.clWorking.id, ConstraintSet.END, 30)
        clear(binding.clWorking.id, ConstraintSet.START)
        clear(binding.clWorking.id, ConstraintSet.BOTTOM)

        connect(
            binding.ibShowSmallView.id,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP
        )
        connect(
            binding.ibShowSmallView.id,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END
        )

        setMargin(binding.ibShowSmallView.id, ConstraintSet.TOP, 35)
        setMargin(binding.ibShowSmallView.id, ConstraintSet.END, 30)
        clear(binding.ibShowSmallView.id, ConstraintSet.START)
        clear(binding.ibShowSmallView.id, ConstraintSet.BOTTOM)



        connect(
            binding.blurViewTimer.id,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP
        )
        connect(
            binding.blurViewTimer.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START
        )
        clear(binding.blurViewTimer.id, ConstraintSet.END)
        clear(binding.blurViewTimer.id, ConstraintSet.BOTTOM)
        setMargin(binding.blurViewTimer.id, ConstraintSet.TOP, 35)
        setMargin(binding.blurViewTimer.id, ConstraintSet.START, 30)

    }
    constraintSet.applyTo(binding.tileContainer)
}

private fun updateConstraintsForLandscapeRight() {
    val constraintSet = ConstraintSet().apply {
        clone(binding.tileContainer)

        connect(
            binding.clWorking.id,
            ConstraintSet.TOP,
            binding.blurViewTimer.id,
            ConstraintSet.BOTTOM
        )
        connect(
            binding.clWorking.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START
        )
        setMargin(binding.clWorking.id, ConstraintSet.TOP, 15)
        setMargin(binding.clWorking.id, ConstraintSet.START, 30)
        clear(binding.clWorking.id, ConstraintSet.END)
        clear(binding.clWorking.id, ConstraintSet.BOTTOM)


        connect(
            binding.ibShowSmallView.id,
            ConstraintSet.TOP,
            binding.blurViewTimer.id,
            ConstraintSet.BOTTOM
        )
        connect(
            binding.ibShowSmallView.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START
        )
        setMargin(binding.ibShowSmallView.id, ConstraintSet.TOP, 15)
        setMargin(binding.ibShowSmallView.id, ConstraintSet.START, 30)
        clear(binding.ibShowSmallView.id, ConstraintSet.END)
        clear(binding.clWorking.id, ConstraintSet.BOTTOM)


        connect(
            binding.blurViewTimer.id,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP
        )
        connect(
            binding.blurViewTimer.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START
        )
        clear(binding.blurViewTimer.id, ConstraintSet.END)
        clear(binding.blurViewTimer.id, ConstraintSet.BOTTOM)
        setMargin(binding.blurViewTimer.id, ConstraintSet.TOP, 35)
        setMargin(binding.blurViewTimer.id, ConstraintSet.START, 30)

    }
    constraintSet.applyTo(binding.tileContainer)
}


private fun updateConstraintsForLandscapeLeft() {
    val constraintSet = ConstraintSet().apply {
        clone(binding.tileContainer)

        connect(
            binding.clWorking.id,
            ConstraintSet.TOP,
            binding.blurViewTimer.id,
            ConstraintSet.BOTTOM
        )
        connect(
            binding.clWorking.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START
        )
        setMargin(binding.clWorking.id, ConstraintSet.TOP, 15)
        setMargin(binding.clWorking.id, ConstraintSet.START, 30)
        clear(binding.clWorking.id, ConstraintSet.END)
        clear(binding.clWorking.id, ConstraintSet.BOTTOM)




        connect(
            binding.ibShowSmallView.id,
            ConstraintSet.TOP,
            binding.blurViewTimer.id,
            ConstraintSet.BOTTOM
        )
        connect(
            binding.ibShowSmallView.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START
        )
        setMargin(binding.ibShowSmallView.id, ConstraintSet.TOP, 15)
        setMargin(binding.ibShowSmallView.id, ConstraintSet.START, 30)
        clear(binding.ibShowSmallView.id, ConstraintSet.END)
        clear(binding.clWorking.id, ConstraintSet.BOTTOM)

        connect(
            binding.blurViewTimer.id,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP
        )
        connect(
            binding.blurViewTimer.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START
        )
        clear(binding.blurViewTimer.id, ConstraintSet.END)
        clear(binding.blurViewTimer.id, ConstraintSet.BOTTOM)
        setMargin(binding.blurViewTimer.id, ConstraintSet.TOP, 35)
        setMargin(binding.blurViewTimer.id, ConstraintSet.START, 30)


    }
    constraintSet.applyTo(binding.tileContainer)
}


override fun onAttendeesDropped(attendeeInfo: Array<AttendeeInfo>) {
    attendeeInfo.forEach { (_, externalUserId) ->
        notifyHandler("$externalUserId dropped")
        logWithFunctionName(
            object {}.javaClass.enclosingMethod?.name,
            "$externalUserId dropped"
        )
    }
}

private fun getAttendeeName(attendeeId: String, externalUserId: String): String {
    if ((attendeeId.isEmpty() || externalUserId.isEmpty())) {
        return "<UNKNOWN>"
    }
    val attendeeName =
        if (externalUserId.contains('#')) externalUserId.split('#')[1] else externalUserId

    return if (attendeeId.isContentShare()) {
        "$attendeeName $CONTENT_NAME_SUFFIX"
    } else {
        attendeeName
    }
}

override fun onAttendeesJoined(attendeeInfo: Array<AttendeeInfo>) {
    onAttendeesJoinedWithStatus(attendeeInfo, AttendeeStatus.Joined)
}

private fun onAttendeesJoinedWithStatus(
    attendeeInfo: Array<AttendeeInfo>,
    status: AttendeeStatus
) {
    uiScope.launch {
        mutex.withLock {
            attendeeInfo.forEach { (attendeeId, externalUserId) ->


                meetingModel.currentRoster.getOrPut(
                    attendeeId
                ) {
                    RosterAttendee(
                        attendeeId,
                        getAttendeeName(attendeeId, externalUserId),
                        attendeeStatus = status
                    )
                }
            }

        }
    }
}

private var primaryMeetingCredentials: MeetingSessionCredentials? = null


private fun startCountdownTimer(timeInMillis: Long) {
    countdownTimer = object : CountDownTimer(timeInMillis, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val minutes = (millisUntilFinished / 1000) / 60
            val seconds = (millisUntilFinished / 1000) % 60
            val timeFormatted = String.format("%02d:%02d", minutes, seconds)
            binding.tvTimer.text = timeFormatted.toString()
            if (timeFormatted == "01:00" && !isInExtraTime && name == "user") {
                showExtraTimeDialogUser()
            }

        }

        override fun onFinish() {
            binding.tvTimer.text = "00:00"
            if (isInExtraTime) {
                startExtraTimeCountdown()
            } else {
                endMeeting()
            }
        }
    }.start()
}

private var isProviderDialog = false

private fun showExtraTimeDialogUser() {
    isProviderDialog = false
    binding.apply {
        blurViewExtend.visibility = View.VISIBLE
        tvHeading.text = "Appointment Will End Soon"
        tvText.text = "Do you want to add more time?"
    }
}


private fun showExtraTimeDialogConfirmation() {
    isProviderDialog = true
    binding.apply {
        blurViewExtend.visibility = View.VISIBLE
        tvHeading.text = "Client Requested Extension"
        tvText.text = "Approve adding more time?"
    }
}


private val checkRenderHandler = Handler(Looper.getMainLooper())
private var isVideoRendering = false
private fun startRenderCheck() {
    checkRenderHandler.postDelayed({
        // Check for rendering state here
//            if (binding.videoSmall.is) {
//                isVideoRendering = true
//            } else {
//                isVideoRendering = false
//            }
        startRenderCheck()
    }, 5000)
}

private fun startExtraTimeCountdown() {
    var elapsedTimeInSeconds = 0 // Start from 0 seconds
    isInExtraTime = false
    extraTimeCountdownTimer =
        object : CountDownTimer(Long.MAX_VALUE, 1000) { // Infinite duration
            override fun onTick(millisUntilFinished: Long) {
                elapsedTimeInSeconds++ // Increment the elapsed time by 1 second on each tick
                val minutes = elapsedTimeInSeconds / 60
                val seconds = elapsedTimeInSeconds % 60
                val timeFormatted = String.format("%02d:%02d", minutes, seconds)
                binding.tvTimer.text = timeFormatted
            }

            override fun onFinish() {
                endMeeting()
            }
        }.start()
}

override fun onAttendeesLeft(attendeeInfo: Array<AttendeeInfo>) {
}

override fun onAttendeesMuted(attendeeInfo: Array<AttendeeInfo>) {
    attendeeInfo.forEach { (attendeeId, externalUserId) ->
        logWithFunctionName(
            object {}.javaClass.enclosingMethod?.name,
            "Attendee with attendeeId $attendeeId and externalUserId $externalUserId muted"
        )
    }
}

override fun onAttendeesUnmuted(attendeeInfo: Array<AttendeeInfo>) {
    attendeeInfo.forEach { (attendeeId, externalUserId) ->
        logger.info(
            "TAG",
            "Attendee with attendeeId $attendeeId and externalUserId $externalUserId unmuted"
        )
    }
}

override fun onSignalStrengthChanged(signalUpdates: Array<SignalUpdate>) {
}

override fun onVolumeChanged(volumeUpdates: Array<VolumeUpdate>) {
}

override fun onAudioSessionCancelledReconnect() {
    notifyHandler("Audio cancelled reconnecting")
    logWithFunctionName(object {}.javaClass.enclosingMethod?.name)
}

override fun onAudioSessionDropped() {
    notifyHandler("Audio session dropped")
    logWithFunctionName(object {}.javaClass.enclosingMethod?.name)
}


private fun setVoiceFocusEnabled(enabled: Boolean) {
    val action = if (enabled) "enable" else "disable"

    val success = audioVideo.realtimeSetVoiceFocusEnabled(enabled)

    if (success) {
        notifyHandler("Voice Focus ${action}d")
    } else {
        notifyHandler("Failed to $action Voice Focus")
    }
}

override fun onAudioSessionStarted(reconnecting: Boolean) {
    notifyHandler(
        "Audio successfully started. reconnecting: $reconnecting"
    )
    // Start Amazon Voice Focus as soon as audio session started
    setVoiceFocusEnabled(true)
    logWithFunctionName(
        object {}.javaClass.enclosingMethod?.name,
        "reconnecting: $reconnecting"
    )

}

override fun onStart() {
    super.onStart()
    if (meetingModel.wasLocalVideoStarted) {
        startLocalVideo()
    }
    audioVideo.startRemoteVideo()
}


override fun onAudioSessionStartedConnecting(reconnecting: Boolean) {
    notifyHandler(
        "Audio started connecting. reconnecting: $reconnecting"
    )
    logWithFunctionName(
        object {}.javaClass.enclosingMethod?.name,
        "reconnecting: $reconnecting"
    )
}

override fun onAudioSessionStopped(sessionStatus: MeetingSessionStatus) {
    notifyHandler(
        "Audio stopped for reason: ${sessionStatus.statusCode}"
    )
    logWithFunctionName(
        object {}.javaClass.enclosingMethod?.name,
        "${sessionStatus.statusCode}"
    )

    if (sessionStatus.statusCode != OK) {
        endMeeting()
    }

}

override fun onCameraSendAvailabilityUpdated(available: Boolean) {
    if (available) {
        meetingModel.isCameraSendAvailable = true
    } else {
        meetingModel.isCameraSendAvailable = false
        notifyHandler("Currently cannot enable video in meeting")
        //refreshNoVideosOrScreenShareAvailableText()
    }
    logWithFunctionName(
        object {}.javaClass.enclosingMethod?.name,
        "Camera Send Available: $available"
    )
}

override fun onConnectionBecamePoor() {
    notifyHandler(
        "Connection quality has become poor"
    )
    logWithFunctionName(
        object {}.javaClass.enclosingMethod?.name
    )
}

override fun onConnectionRecovered() {
    notifyHandler(
        "Connection quality has recovered"
    )
    logWithFunctionName(
        object {}.javaClass.enclosingMethod?.name
    )
}

override fun onRemoteVideoSourceAvailable(sources: List<RemoteVideoSource>) {
    for (source in sources) {
        val config =
            VideoSubscriptionConfiguration(VideoPriority.Medium, VideoResolution.Medium)
        meetingModel.addVideoSource(source, config)
    }

    meetingModel.updateRemoteVideoSourceSubscription(audioVideo)
}

override fun onRemoteVideoSourceUnavailable(sources: List<RemoteVideoSource>) {
    sources.forEach { meetingModel.removeVideoSource(it) }
}

private fun notifyHandler(
    toastMessage: String
) {
    uiScope.launch {
        let {
            //  Toast.makeText(this@MainActivity, toastMessage, Toast.LENGTH_SHORT).show()
        }
    }
}


override fun onVideoSessionStarted(sessionStatus: MeetingSessionStatus) {
    when (sessionStatus.statusCode) {
        VideoAtCapacityViewOnly -> {
            notifyHandler("Currently cannot enable video in meeting")
            stopLocalVideo()
            meetingModel.isCameraOn = !meetingModel.isCameraOn
            // refreshNoVideosOrScreenShareAvailableText()
        }

        OK -> {}
        Left -> {}
        AudioJoinedFromAnotherDevice -> {}
        AudioDisconnectAudio -> {}
        AudioAuthenticationRejected -> {}
        AudioCallAtCapacity -> {}
        AudioCallEnded -> {}
        AudioInternalServerError -> {}
        AudioServiceUnavailable -> {}
        AudioDisconnected -> {}
        ConnectionHealthReconnect -> {}
        NetworkBecamePoor -> {}
        VideoServiceFailed -> {}
        AudioOutputDeviceNotResponding -> {}
        AudioInputDeviceNotResponding -> {}
        null -> {}
    }
    logWithFunctionName(
        object {}.javaClass.enclosingMethod?.name,
        "${sessionStatus.statusCode}"
    )
}

private fun logWithFunctionName(
    fnName: String?,
    msg: String = "",
    logLevel: LogLevel = LogLevel.INFO
) {
    val newMsg = if (fnName == null) msg else "[Function] [$fnName]: $msg"
    when (logLevel) {
        LogLevel.DEBUG -> logger.debug("TAG", newMsg)
        else -> logger.info("TAG", newMsg)
    }
}

override fun onVideoSessionStartedConnecting() {
    notifyHandler("Video started connecting.")
    logWithFunctionName(object {}.javaClass.enclosingMethod?.name)
}

override fun onVideoSessionStopped(sessionStatus: MeetingSessionStatus) {
    notifyHandler(
        "Video stopped for reason: ${sessionStatus.statusCode}"
    )
    logWithFunctionName(
        object {}.javaClass.enclosingMethod?.name,
        "${sessionStatus.statusCode}"
    )
}

override fun onVideoTileAdded(tileState: VideoTileState) {
    uiScope.launch {
        logger.info(
            "TAG",
            "Video tile added, tileId: ${tileState.tileId}, attendeeId: ${tileState.attendeeId}" +
                    ", isContent ${tileState.isContent} with size ${tileState.videoStreamContentWidth}*${tileState.videoStreamContentHeight}"
        )
        if (tileState.isContent && meetingModel.currentScreenTiles.none { it.videoTileState.tileId == tileState.tileId }) {
            showVideoTile(tileState)
        } else {
            if (tileState.isLocalTile) {
                showVideoTile(tileState)
            } else if (meetingModel.getRemoteVideoTileStates()
                    .none { it.videoTileState.tileId == tileState.tileId }
            ) {
                showVideoTile(tileState)
            }
        }
        if (meetingModel.videoStatesInCurrentPage.size == 2) {
            // Toast.makeText(this@MainActivity,"2 users there",Toast.LENGTH_SHORT).show()

            if (!isTimerStarted) {
                isTimerStarted = true
                startCountdownTimer(15 * 60 * 1000)

            }
            Log.d("2Devices", "true")
            if (isPortrait) {
                audioVideo.realtimeSendDataMessage("portrait", "", 5000)
            } else {
                audioVideo.realtimeSendDataMessage("landscape", "", 5000)
            }
        }
    }
}

override fun onVideoTilePaused(tileState: VideoTileState) {
    if (tileState.pauseState == VideoPauseState.PausedForPoorConnection) {
        val collection =
            if (tileState.isContent) meetingModel.currentScreenTiles else meetingModel.videoStatesInCurrentPage
        collection.find { it.videoTileState.tileId == tileState.tileId }.apply {
            this?.setPauseMessageVisibility(View.VISIBLE)
        }
        val attendeeName =
            meetingModel.currentRoster[tileState.attendeeId]?.attendeeName ?: ""
        logWithFunctionName(
            object {}.javaClass.enclosingMethod?.name,
            "$attendeeName video paused"
        )
    }
}

override fun onVideoTileRemoved(tileState: VideoTileState) {
    uiScope.launch {
        val tileId: Int = tileState.tileId

        logger.info(
            "TAG",
            "Video track removed, tileId: $tileId, attendeeId: ${tileState.attendeeId}"
        )
        audioVideo.unbindVideoView(tileId)
    }
}

override fun onVideoTileResumed(tileState: VideoTileState) {
    Log.d("HomeresumeTile", "working")
    var collection = mutableListOf<VideoCollectionTile>()
    if (tileState.isContent) {
        collection = meetingModel.currentScreenTiles
    } else {
        // meetingModel.getRemoteVideoTileStates()

        if (tileState.isLocalTile) {
            showVideoTile(tileState)
        } else if (meetingModel.getRemoteVideoTileStates().none
            { it.videoTileState.tileId == tileState.tileId }
        ) {
            showVideoTile(tileState)
            collection =
                meetingModel.getRemoteVideoTileStates() as MutableList<VideoCollectionTile>
        }
    }
    collection.find { it.videoTileState.tileId == tileState.tileId }.apply {
        this?.setPauseMessageVisibility(View.INVISIBLE)
    }
    val attendeeName = meetingModel.currentRoster[tileState.attendeeId]?.attendeeName ?: ""
    logWithFunctionName(
        object {}.javaClass.enclosingMethod?.name,
        "$attendeeName video resumed"
    )
}

override fun onVideoTileSizeChanged(tileState: VideoTileState) {
    logger.info(
        "TAG",
        "Video stream content size changed to ${tileState.videoStreamContentWidth}*${tileState.videoStreamContentHeight} for tileId: ${tileState.tileId}"
    )
    Log.d("HomePoint9", "working")

    if (tileState.isLocalTile) {  // Check if it’s the remote user
        val width = tileState.videoStreamContentWidth
        val height = tileState.videoStreamContentHeight
//            binding.apply {
//
//                if (isCursorEnabled) {
//                    ibPointer.setImageResource(R.drawable.ic_pointer)
//                    ibPointerLandscape.setImageResource(R.drawable.ic_pointer)
//                    cursor.visibility = View.GONE
//                    ivCross.visibility = View.VISIBLE
//                    cursorSmall.visibility = View.GONE
//                    ibPointer.setImageResource(R.drawable.ic_selected_pointer)
//                    cursor.visibility = View.VISIBLE
//                    val centerX = width / 2
//                    val centerY =height / 2
//                    movingX = centerX.toFloat()
//                    movingY = centerY.toFloat()
//                    bigViewWidth = width.toFloat()
//                    bigViewHeight = height.toFloat()
//                   cursor.x = (movingX - (binding.cursor.width) / 2).toFloat()
//                   cursor.y = (movingY - (binding.cursor.height) / 2).toFloat()
//                    val cursorPixel =
//                        "cursorPixel[" + movingX.toFloat().toString() + "," + movingY.toFloat()
//                            .toString() + "," + bigViewWidth.toString() + "," + bigViewHeight
//                            .toString() + "]"
//                    audioVideo.realtimeSendDataMessage("pointerDataTransfer", cursorPixel, 5000)
//                } else if (isSmallCursorEnabled) {
//                    ibPointer.setImageResource(R.drawable.ic_pointer)
//                    ibPointerLandscape.setImageResource(R.drawable.ic_pointer)
//                    cursor.visibility = View.GONE
//                    ivCross.visibility = View.VISIBLE
//                    cursorSmall.visibility = View.GONE
//                    ibPointerLandscape.setImageResource(R.drawable.ic_selected_pointer)
//                    cursorSmall.visibility = View.VISIBLE
//                    val centerX =width / 2
//                    val centerY = height / 2
//                    movingSmallX = centerX.toFloat()
//                    movingSmallY = centerY.toFloat()
//                    smallViewWidth = width.toFloat()
//                    smallViewHeight = height.toFloat()
//                  cursorSmall.x = movingSmallX - (binding.cursorSmall.width) / 2
//                  cursorSmall.y = movingSmallY - (binding.cursorSmall.height) / 2
//                    val cursorPixel =
//                        "cursorPixel[" + movingX.toFloat().toString() + "," + movingY.toFloat()
//                            .toString() + "," + smallViewWidth.toString() + "," + smallViewHeight
//                            .toString() + "]"
//                    audioVideo.realtimeSendDataMessage("pointerDataTransfer", cursorPixel, 5000)
//                }
//            }
    }
}

// mainViewHeight:2131.0,mainViewWidth:1080.0
private fun setVideoSize(
    isRemoteVideoMaximized: Boolean,
    isPortrait: Boolean,
    isRemotePortrait: Boolean
) {
    Log.d("HomeCase1", "1")
    val windowInsets =
        WindowInsetsCompat.toWindowInsetsCompat(window.decorView.rootWindowInsets)
    val statusBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top
    val navigationBarHeight =
        windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
    val displayMetrics = resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels
    val screenHeight = displayMetrics.heightPixels
    //- statusBarHeight - navigationBarHeight


//GSize
//    val mainViewWidth = binding.bigVideoSurface.width.toFloat()
//        val mainViewHeight = binding.bigVideoSurface.height.toFloat()
    val mainViewWidth = displayMetrics.widthPixels.toFloat()
    val mainViewHeight = displayMetrics.heightPixels.toFloat()
    val smallContainerWidth = binding.clWorking.width.toFloat()
    val smallContainerHeight = binding.clWorking.height.toFloat()
    Log.d(
        "HomeCase1",
        "smallContainerHeight:" + smallContainerHeight + ",smallContainerWidth:" + smallContainerWidth
    )
    Log.d("HomeCase1", "mainViewHeight:" + mainViewHeight + ",mainViewWidth:" + mainViewWidth)

    val largeViewParams = binding.bigVideoSurface.layoutParams as ConstraintLayout.LayoutParams
    val smallViewParams = binding.videoSmall.layoutParams as FrameLayout.LayoutParams

    if (isRemoteVideoMaximized) {
        Log.d("HomeCase1", "2")
        // Set dimensions for largeView
        if (isPortrait && isRemotePortrait || !isPortrait && !isRemotePortrait) {
            Log.d("HomeCase1", "4")
            largeViewParams.width = screenWidth.toInt()
            largeViewParams.height = screenHeight.toInt()
        } else if (isPortrait && !isRemotePortrait) {
            Log.d("HomeCase1", "5")
            largeViewParams.width = mainViewWidth.toInt()
            largeViewParams.height = (mainViewWidth * 9 / 16).toInt()
        } else if (!isPortrait && isRemotePortrait) {
            Log.d("HomeCase1", "6")
            largeViewParams.height = screenHeight.toInt()
            largeViewParams.width = (screenHeight * 9 / 16).toInt()
        }

        // Set dimensions for smallView
        if (isPortrait) {
            Log.d("HomeCase1", "7")
            smallViewParams.width = binding.flSmallVideo.width
            smallViewParams.height = binding.flSmallVideo.height
        } else {
            Log.d("HomeCase1", "8")
            smallViewParams.width = smallContainerWidth.toInt()
            smallViewParams.height = (smallContainerWidth * 9 / 16).toInt()
        }
    } else {
        Log.d("HomeCase1", "3")
        // Set dimensions for smallView
        if (isPortrait && isRemotePortrait || !isPortrait && !isRemotePortrait) {
            Log.d("HomeCase1", "9")
            smallViewParams.width = binding.flSmallVideo.width
            smallViewParams.height = binding.flSmallVideo.height
            largeViewParams.width = screenWidth.toInt()
            largeViewParams.height = screenHeight.toInt()

        } else if (!isPortrait && !isRemotePortrait) {
            smallViewParams.width = smallContainerWidth.toInt()
            smallViewParams.height = smallContainerWidth.toInt() * 9 / 16
            largeViewParams.width = screenWidth.toInt()
            largeViewParams.height = screenHeight.toInt()

        } else if (isPortrait && !isRemotePortrait) {
            Log.d("HomeCase1", "10")
            smallViewParams.width = smallContainerWidth.toInt()
            smallViewParams.height = (smallContainerWidth * 9 / 16).toInt()
            if (mainViewWidth > mainViewHeight) {
                largeViewParams.width = mainViewHeight.toInt()
                largeViewParams.height = mainViewWidth.toInt()
            } else {
                largeViewParams.width = mainViewWidth.toInt()
                largeViewParams.height = mainViewHeight.toInt()
            }
        } else if (!isPortrait && isRemotePortrait) {
            Log.d("HomeCase1", "11")
            smallViewParams.width = smallContainerWidth.toInt()
            smallViewParams.height = smallContainerHeight.toInt()
            if (mainViewHeight > mainViewWidth) {
                largeViewParams.width = mainViewHeight.toInt()
                largeViewParams.height = mainViewWidth.toInt()
            } else {
                largeViewParams.width = mainViewWidth.toInt()
                largeViewParams.height = mainViewHeight.toInt()
            }
        }

        // Set dimensions for largeView

    }

    // Apply the layout params
    binding.bigVideoSurface.layoutParams = largeViewParams
    binding.videoSmall.layoutParams = smallViewParams

    // Request layout update
    binding.root.requestLayout()
}


override fun onMetricsReceived(metrics: Map<ObservableMetric, Any>) {
    logger.debug("TAG", "Media metrics received: $metrics")
//        uiScope.launch {
//            mutex.withLock {
//                meetingModel.currentMetrics.clear()
//                metrics.forEach { (metricsName, metricsValue) ->
//                    meetingModel.currentMetrics[metricsName.name] =
//                        MetricData(metricsName.name, metricsValue.toString())
//                }
//                metricsAdapter.notifyDataSetChanged()
//            }
//        }
}

override fun onActiveSpeakerDetected(attendeeInfo: Array<AttendeeInfo>) {
}

override fun onActiveSpeakerScoreChanged(scores: Map<AttendeeInfo, Double>) {
    val scoresStr =
        scores.map { entry -> "${entry.key.externalUserId}: ${entry.value}" }.joinToString(",")
    logWithFunctionName(
        object {}.javaClass.enclosingMethod?.name,
        scoresStr,
        LogLevel.DEBUG
    )
}

override fun onAudioDeviceChanged(freshAudioDeviceList: List<MediaDevice>) {
}

@RequiresApi(Build.VERSION_CODES.Q)
override fun onDataMessageReceived(dataMessage: DataMessage) {
//        val senderId = dataMessage.senderAttendeeId
//        val messageContent = String(dataMessage.data)

    if (dataMessage.topic == "pointerDataTransfer") {
        Log.d("HomePoint1", "working")
        val messageContent = dataMessage.text()
        val valuesPart = messageContent.substringAfter("[").substringBefore("]")
        val (x, y, width, height) = valuesPart.split(",").map { it }
        try {
            val floatArrayList = ArrayList<String>()
            floatArrayList.add(x)
            floatArrayList.add(y)
            floatArrayList.add(width)
            floatArrayList.add(height)
            Log.d("pointerDataTransfer", floatArrayList.toString())
            println("Received float array list: $floatArrayList")
            receiveCoordsData(floatArrayList)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    } else if (dataMessage.topic == "portrait") {
        Log.d("HomePoint2", "working")
        isRemotePortrait = true
        if (isCursorEnabled || isSmallCursorEnabled) {
            pointerAddedLocally()
        }


        setVideoSize(localIsSmall, isPortrait, isRemotePortrait)
        setVideoContentMode()

    } else if (dataMessage.topic == "cameraToggle") {
        isCameraToogle = String(dataMessage.data)
        updateRemoteVideoMirror()

    } else if (dataMessage.topic == "landscape") {
        Log.d("HomePoint2", "working")
        isRemotePortrait = false
        if (isCursorEnabled || isSmallCursorEnabled) {
            pointerAddedLocally()
        }
        setVideoSize(localIsSmall, isPortrait, isRemotePortrait)
        setVideoContentMode()

    }
//        else if (dataMessage.topic == "incrementTimer") {
//            Log.d("HomePoint3","working")
//            isInExtraTime = true
//
//        }

    else if (dataMessage.topic == "incrementTimerRequested") {
        Log.d("HomePoint003", "working")
        showExtraTimeDialogConfirmation()

    } else if (dataMessage.topic == "incrementTimerApproved") {
        Log.d("HomePoint3", "working")
        isInExtraTime = true

    } else if (dataMessage.topic == "endMeeting") {
        Log.d("HomePoint4", "working")
        endMeeting()

    } else if (dataMessage.topic == "pointerAdded") {
        Log.d("HomePoint5", "working")
        pointerAddedLocally()
        binding.apply {
            blurViewBottomLandScape.visibility = View.GONE
            blurViewSideBarLandscape.visibility = View.GONE
            tvProviderNameLandscape.visibility = View.GONE
            blurViewBottom.visibility = View.GONE
            blurViewSideBar.visibility = View.GONE
            tvProviderName.visibility = View.GONE
            toggleCursorMode()
            if (name == "user") {
                if (localIsSmall) {
                    expandView()
                }
            } else {
                if (remoteIsSmall) {
                    expandView()
                }
            }
        }


    } else if (dataMessage.topic == "pointerRemoved") {
        Log.d("HomePoint8", "working")
        clearCross()
        binding.ibPointer.setImageResource(R.drawable.ic_pointer)
        binding.ibPointerLandscape.setImageResource(R.drawable.ic_pointer)

    }
    Log.d("HomegetMessageContent", "messageContent:" + "messageContent.toString()")
}

override fun onContentShareStarted() {
}

override fun onContentShareStopped(status: ContentShareStatus) {
}

override fun onEventReceived(name: EventName, attributes: EventAttributes) {
}

override fun onTranscriptEventReceived(transcriptEvent: TranscriptEvent) {

}

override val scoreCallbackIntervalMs: Int get() = 1000


private fun createSessionConfigurationAndExtractPrimaryMeetingInformation(response: String?): MeetingSessionConfiguration? {
    if (response.isNullOrBlank()) return null

    return try {
        val joinMeetingResponse = gson.fromJson(response, JoinMeetingResponse::class.java)
        // primaryExternalMeetingId = joinMeetingResponse.joinInfo.primaryExternalMeetingId
        val meetingResp = joinMeetingResponse.joinInfo.meetingResponse.meeting
        val externalMeetingId: String? = meetingResp.ExternalMeetingId
        val mediaPlacement: MediaPlacement = meetingResp.MediaPlacement
        val mediaRegion: String = meetingResp.MediaRegion
        val meetingId: String = meetingResp.MeetingId
        val meetingFeatures: MeetingFeatures = MeetingFeatures(
            meetingResp.MeetingFeatures?.Video?.MaxResolution,
            meetingResp.MeetingFeatures?.Content?.MaxResolution
        )
        val meeting =
            Meeting(
                externalMeetingId,
                mediaPlacement,
                mediaRegion,
                meetingId,
                meetingFeatures
            )
        MeetingSessionConfiguration(
            CreateMeetingResponse(meeting),
            CreateAttendeeResponse(joinMeetingResponse.joinInfo.attendeeResponse.attendee),
            ::urlRewriter
        )
    } catch (exception: Exception) {
        logger.error(
            "TAG",
            "Error creating session configuration: ${exception.localizedMessage}"
        )
        null
    }
}

private fun urlRewriter(url: String): String {
    return url
}

private var marshMellowHelper: MarshMellowHelper? = null
private fun filePermissionCheck() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        marshMellowHelper = MarshMellowHelper(
            this,
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES
            ),
            PERMISSIONS_REQUEST_CODE
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        marshMellowHelper = MarshMellowHelper(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            PERMISSIONS_REQUEST_CODE
        )
    } else {
        marshMellowHelper = MarshMellowHelper(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            PERMISSIONS_REQUEST_CODE
        )
    }
    marshMellowHelper!!.request(object : MarshMellowHelper.PermissionCallback {
        @RequiresApi(Build.VERSION_CODES.N)
        override fun onPermissionGranted() {
            takePhoto()
        }

        override fun onPermissionDenied() {

        }

        override fun onPermissionDeniedBySystem() {

        }
    })


}


private fun takePhoto() {
    val combinedBitmap = Bitmap.createBitmap(
        binding.bigVideoSurface.width, binding.bigVideoSurface.height,
        Bitmap.Config.ARGB_8888
    )
    val handlerThread = HandlerThread("PixelCopier")
    handlerThread.start()

    PixelCopy.request(binding.bigVideoSurface, combinedBitmap, { bigCopyResult ->
        if (bigCopyResult == PixelCopy.SUCCESS) {
            Handler(handlerThread.looper).postDelayed({
                val overlayBitmap = Bitmap.createBitmap(
                    binding.videoSmall.width, binding.videoSmall.height, Bitmap.Config.ARGB_8888
                )
                PixelCopy.request(binding.videoSmall, overlayBitmap, { smallCopyResult ->
                    if (smallCopyResult == PixelCopy.SUCCESS) {
                        val canvas = Canvas(combinedBitmap)
                        canvas.drawBitmap(
                            overlayBitmap,
                            binding.videoSmall.x,
                            binding.videoSmall.y,
                            null
                        )
                        val flippedBitmap = Bitmap.createBitmap(
                            combinedBitmap.width, combinedBitmap.height, combinedBitmap.config
                        )
                        val matrix = Matrix().apply {
                            preScale(
                                -1.0f,
                                1.0f,
                                combinedBitmap.width / 2f,
                                combinedBitmap.height / 2f
                            )
                        }
                        Canvas(flippedBitmap).drawBitmap(combinedBitmap, matrix, null)
                        saveMediaToStorage(flippedBitmap)
                    } else {
                        Toast.makeText(
                            this,
                            "Failed to copy small view: $smallCopyResult",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    handlerThread.quitSafely()
                }, Handler(handlerThread.looper))
            }, 50)
        } else {
            Toast.makeText(this, "Failed to copy big view: $bigCopyResult", Toast.LENGTH_LONG)
                .show()
            handlerThread.quitSafely()
        }
    }, Handler(handlerThread.looper))
}

private fun saveMediaToStorage(bitmap: Bitmap) {
    val filename = "${System.currentTimeMillis()}.jpg"

    val fos: OutputStream?
    val values = ContentValues()
    values.put(MediaStore.Images.Media.DISPLAY_NAME, filename)
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)

        val imageUri: Uri? =
            this.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        fos = imageUri?.let { this.contentResolver?.openOutputStream(it) }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this, "Captured View and saved to Gallery", Toast.LENGTH_SHORT)
                .show()
        }
    } else {
        runOnUiThread {
            val directory = File(
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + "TeleAndroid"
            )
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = System.currentTimeMillis().toString() + ".png"
            val fileee = File(directory, fileName)
            saveImageToStream(bitmap, FileOutputStream(fileee))
            val values = contentValues()
            values.put(MediaStore.Images.Media.DATA, fileee.absolutePath)
            this.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )
            Toast.makeText(this, "Captured View and saved to Gallery", Toast.LENGTH_SHORT)
                .show()
        }
    }
}

private fun contentValues(): ContentValues {
    val values = ContentValues()
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
    values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
    return values
}

private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
    if (outputStream != null) {
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

var mFilename: String? = null
var resolver: ContentResolver? = null
var mUri: Uri? = null
var mFile: File? = null
var contentValues: ContentValues? = null
var recordingStart = false

private fun setOutputPath() {
    mFilename = generateFileName()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        resolver = contentResolver
        contentValues = ContentValues()
        contentValues!!.put(MediaStore.Video.Media.RELATIVE_PATH, "SpeedTest/" + "SpeedTest")
        contentValues!!.put(MediaStore.Video.Media.TITLE, mFilename)
        contentValues!!.put(MediaStore.MediaColumns.DISPLAY_NAME, mFilename)
        contentValues!!.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
        mUri = resolver!!.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
        mFile = mUri?.path?.let { File(it) };
        hbRecorder.fileName = mFilename
        hbRecorder.setOutputUri(mUri)
        Log.d("Homegeturi_p", mFile.toString())

    } else {
        createFolder()
        hbRecorder.setOutputPath(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                .toString() + "/HBRecorder"
        )

    }
}

private fun updateGalleryUri() {
    setOutputPath()
    contentValues().clear()
    contentValues().put(MediaStore.Video.Media.IS_PENDING, 0)
    contentResolver.update(mUri!!, contentValues, null, null)
    Log.d("Homegeturi_abc", mUri.toString())
}

private fun refreshGalleryFile() {
    MediaScannerConnection.scanFile(
        this, arrayOf(hbRecorder.filePath), null
    ) { path, uri ->
        Log.i("ExternalStorage", "Scanned $path:")
        Log.i("ExternalStorage", "-> uri=$uri")
    }
}

private fun generateFileName(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault())
    val curDate = Date(System.currentTimeMillis())
    return formatter.format(curDate).replace(" ", "")
}


private fun createFolder() {
    val f1 = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
        "CarAr"
    )
    if (!f1.exists()) {
        if (f1.mkdirs()) {
            Log.i("Folder ", "created")
        }
    }
}


override fun HBRecorderOnStart() {
    recordingStart = true
}

override fun HBRecorderOnComplete() {

    if (hbRecorder.wasUriSet()) {
        updateGalleryUri();
    } else {
        refreshGalleryFile();
        Log.d("Homegeturi_cfff", mFile.toString())
    }
    val afile = File(hbRecorder.filePath)
    Log.d("Hometesting_file", afile.toString())
    recordingStart = false
    afile.let {
        hbRecorder.filePath?.toUri().let { it1 ->
            val contentResolver = contentResolver

            // Create a content values object
            val contentValues = ContentValues().apply {
                put(
                    MediaStore.Video.Media.DISPLAY_NAME,
                    "recorded_video_${System.currentTimeMillis()}.mp4"
                )
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(
                    MediaStore.Video.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_MOVIES
                ) // For Android 10 and above
            }

            // Insert the new video into the MediaStore
            val newVideoUri = contentResolver.insert(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

            if (newVideoUri != null) {
                // Copy the recorded video from HBRecorder to the new location
                try {
                    val inputStream =
                        contentResolver.openInputStream(it1!!) // The Uri from HBRecorder
                    val outputStream = contentResolver.openOutputStream(newVideoUri)

                    inputStream?.use { input ->
                        outputStream?.use { output ->
                            input.copyTo(output)
                        }
                    }
                    Log.d("HomeVideoSave", "Video saved successfully to: $newVideoUri")
                } catch (e: Exception) {
                    Log.e("VideoSave", "Error saving video: ${e.message}")
                }
            } else {
                Log.e("VideoSave", "Failed to create new video entry in MediaStore")
            }
        }
    }

}

override fun HBRecorderOnError(errorCode: Int, reason: String?) {
}

override fun HBRecorderOnPause() {
}

override fun HBRecorderOnResume() {
}


}