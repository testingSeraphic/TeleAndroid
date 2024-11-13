package com.tele.android

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Outline
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
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.OrientationEventListener
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
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
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionStatusCode.*
import com.amazonaws.services.chime.sdk.meetings.utils.DefaultModality
import com.amazonaws.services.chime.sdk.meetings.utils.logger.ConsoleLogger
import com.amazonaws.services.chime.sdk.meetings.utils.logger.LogLevel
import com.google.gson.Gson
import com.hbisoft.hbrecorder.HBRecorder
import com.hbisoft.hbrecorder.HBRecorderListener
import com.tele.android.data.RosterAttendee
import com.tele.android.data.VideoCollectionTile
import com.tele.android.databinding.ActivityMainBinding
import com.tele.android.datamodel.JoinMeetingResponse
import com.tele.android.device.AudioDeviceManager
import com.tele.android.service.MicrophoneService
import com.tele.android.utils.CpuVideoProcessor
import com.tele.android.utils.GpuVideoProcessor
import com.tele.android.utils.RoundedOutlineProvider
import com.tele.android.utils.Utils
import com.tele.android.utils.isContentShare
import com.tele.android.viewmodel.JoinMeetingModel
import com.tele.android.viewmodel.MeetingModel
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URISyntaxException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity(),
    RealtimeObserver, AudioVideoObserver, VideoTileObserver,
    MetricsObserver, ActiveSpeakerObserver, DeviceChangeObserver, DataMessageObserver,
    ContentShareObserver, EventAnalyticsObserver, TranscriptEventObserver,
    PrimaryMeetingPromotionObserver, HBRecorderListener {
    private lateinit var binding: ActivityMainBinding
    private var lastOrientation: String? = null
    private lateinit var orientationEventListener: OrientationEventListener
    private lateinit var meetingId: String
    private lateinit var name: String
    private val gson = Gson()
    private var localIsSmall = false
    private var remoteIsSmall = true
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
    private var movingX = 0f
    private var movingY = 0f
    private var movingSmallX = 0f
    private var movingSmallY = 0f

    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.apply {
            makeViewBlur(blurViewTimer, 40f)
            makeViewBlur(blurViewBottom, 60f)
            makeViewBlur(blurViewSideBar, 35f)
            bigVideoSurface.setZOrderOnTop(false)
            videoSmall.setZOrderOnTop(true)
            videoSmall.setZOrderMediaOverlay(true);
            flMovingSurface.visibility = View.GONE
            cursor.visibility = View.GONE
            flMovingSurfaceSmall.visibility = View.GONE
            cursorSmall.visibility = View.GONE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                flSmallVideo.outlineProvider = ViewOutlineProvider.BACKGROUND
                flSmallVideo.clipToOutline = true
            }
        }
        val videoRenderView = binding.bigVideoSurface
        videoRenderView.scalingType = VideoScalingType.AspectFill
        clickListeners()
        meetingId = intent.extras?.getString(JoinMeeting.MEETING_ID_KEY) as String
        name = intent.extras?.getString(JoinMeeting.NAME_KEY) as String
        Log.d("getName",name.toString())
if(name!="user"){
    remoteIsSmall=false
    localIsSmall=true
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
        meetingEndpointUrl = intent.extras?.getString(JoinMeeting.MEETING_ENDPOINT_KEY) as String
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
        hbRecorder?.apply {
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
        binding.flMovingSurface.setOnTouchListener { _, event ->
            if (isCursorEnabled) {
                moveCursor(event.x, event.y)
                true
            } else {
                // Pass touch event to views below if cursor mode is disabled
                false
            }
        }

        binding.flMovingSurfaceSmall.setOnTouchListener { _, event ->
            if (isSmallCursorEnabled) {
                moveSmallCursor(event.x, event.y)
                true
            } else {
                // Pass touch event to views below if cursor mode is disabled
                false
            }
        }

        orientationEventListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                val currentOrientation = when {
                    orientation in 45..135 -> "Landscape Right"
                    orientation in 225..315 -> "Landscape Left"
                    else -> "Portrait"
                }
                if (currentOrientation != lastOrientation) {
                    Log.d("Rotation", "Device orientation changed to: $currentOrientation")
                    lastOrientation = currentOrientation // Update the last orientation

                    when (lastOrientation) {
                        "Landscape Left" -> {
                            binding.tvProviderName.visibility = View.GONE
                            updateConstraintsForLandscapeLeft()
                            rotationOfView(-90f, "left")
                            commonViewsRotation(90f)
                            binding.blurViewBottom.scaleY = -1f
                            binding.blurViewBottom.scaleX = -1f
                            audioVideo.realtimeSendDataMessage(
                                "orientation",
                                "Landscape_left",
                                5000
                            )

                        }

                        "Landscape Right" -> {
                            binding.tvProviderName.visibility = View.GONE
                            updateConstraintsForLandscapeRight()
                            rotationOfView(-90f, "right")
                            commonViewsRotation(-90f)
                            binding.blurViewBottom.scaleY = -1f
                            binding.blurViewBottom.scaleX = 1f
                            audioVideo.realtimeSendDataMessage(
                                "orientation",
                                "Landscape_right",
                                5000
                            )

                        }

                        "Portrait" -> {
                            if (!isBottomBarVisible) {
                                binding.tvProviderName.visibility = View.GONE
                            } else {
                                binding.tvProviderName.visibility = View.VISIBLE
                            }
                            updateConstraintsForPortrait()
                            rotationOfView(0f, "portrait")
                            commonViewsRotation(0f)
                            binding.blurViewBottom.scaleY = 1f
                            binding.blurViewBottom.scaleX = 1f
                            audioVideo.realtimeSendDataMessage("orientation", "Portrait", 5000)

                        }
                    }
                }
            }
        }


        setupButtonsBar()
    }




    private fun moveCursor(x: Float, y: Float) {
        Log.d("getXy", "$x:$y")
        binding.cursor.x = x - binding.cursor.width / 2
        binding.cursor.y = y - binding.cursor.height / 2
        movingX=x
        movingY=y
        toSendCoordsData()
    }

    private fun moveSmallCursor(x: Float, y: Float) {
        Log.d("getXy", "$x:$y")
        binding.cursorSmall.x = x - binding.cursorSmall.width / 2
        binding.cursorSmall.y = y - binding.cursorSmall.height / 2
        movingSmallX=x
        movingSmallY=y
        toSendSmallCoordsData()
    }

    fun toggleCursorMode() {
        isCursorEnabled = !isCursorEnabled
        binding.apply {
            flMovingSurface.isClickable = isCursorEnabled
            flMovingSurface.isFocusable = isCursorEnabled
            flMovingSurface.isFocusableInTouchMode = isCursorEnabled
            if (isCursorEnabled) {
                ibPointer.setImageResource(R.drawable.ic_selected_pointer)

                cursor.visibility=View.VISIBLE
             flMovingSurface.visibility=View.VISIBLE
                if(movingX==0F && movingY==0F) {
                    val centerX = resources.displayMetrics.widthPixels / 2
                    val centerY = resources.displayMetrics.heightPixels / 2
                    binding.cursor.x = centerX.toFloat()- binding.cursor.width / 2
                    binding.cursor.y = centerY.toFloat()- binding.cursor.height / 2
                    movingX=centerX.toFloat()
                    movingY=centerY.toFloat()
                }
                else{
                    binding.cursor.x =movingX - binding.cursor.width / 2
                    binding.cursor.y = movingY- binding.cursor.height / 2
                    movingX=movingX
                    movingY=movingY
                }
                cursorSmall.visibility=View.GONE
                flMovingSurfaceSmall.visibility=View.GONE
            } else {
                ibPointer.setImageResource(R.drawable.ic_pointer)
                cursor.visibility=View.GONE
                flMovingSurface.visibility=View.GONE
            }
        }
    }
    fun toggleSmallCursorMode() {
        isSmallCursorEnabled = !isSmallCursorEnabled
        binding.apply {
            flMovingSurfaceSmall.isClickable = isSmallCursorEnabled
            flMovingSurfaceSmall.isFocusable = isSmallCursorEnabled
            flMovingSurfaceSmall.isFocusableInTouchMode = isSmallCursorEnabled
            if (isSmallCursorEnabled) {
                ibPointer.setImageResource(R.drawable.ic_selected_pointer)
                cursorSmall.visibility=View.VISIBLE
                flMovingSurfaceSmall.visibility=View.VISIBLE
                if(movingSmallX==0F && movingSmallY==0F) {
                    val centerX = binding.videoSmall.width / 2
                    val centerY =  binding.videoSmall.height  / 2
                    binding.cursorSmall.x = centerX.toFloat()- binding.cursorSmall.width / 2
                    binding.cursorSmall.y = centerY.toFloat()- binding.cursorSmall.height / 2
                    movingSmallX=centerX.toFloat()
                    movingSmallY=centerY.toFloat()
                }
                else{
                    binding.cursorSmall.x = movingSmallX - binding.cursorSmall.width / 2
                    binding.cursorSmall.y = movingSmallY - binding.cursorSmall.height / 2
                    movingSmallX=movingSmallX
                    movingSmallY=movingSmallY
                }
                cursor.visibility=View.GONE
                flMovingSurface.visibility=View.GONE
            } else {
                ibPointer.setImageResource(R.drawable.ic_pointer)
                cursorSmall.visibility=View.GONE
                flMovingSurfaceSmall.visibility=View.GONE
            }
        }
    }

    fun toSendCoordsData() {

        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        var cursorPixel = "cursorPixel["+movingX.toFloat().toString()+","+movingY.toFloat().toString()+","+screenWidth.toFloat().toString()+","+screenHeight.toFloat().toString()+"]"
        audioVideo.realtimeSendDataMessage("pointerDataTransfer", cursorPixel, 5000)
    }

    fun toSendSmallCoordsData() {

        val viewWidth = binding.videoSmall.width
        val viewHeight =  binding.videoSmall.height
        var cursorPixel = "cursorPixel["+movingSmallX.toFloat().toString()+","+movingSmallY.toFloat().toString()+","+viewWidth.toFloat().toString()+","+viewHeight.toFloat().toString()+"]"
        audioVideo.realtimeSendDataMessage("pointerDataTransfer", cursorPixel, 5000)
    }

    @SuppressLint("SuspiciousIndentation")
    fun receiveCoordsData(coordinates: ArrayList<String>) {
        val relativeX = coordinates[0].toFloat()
        val relativeY = coordinates[1].toFloat()
        val oppositeWidth = coordinates[2].toFloat()
        val oppositeHeight = coordinates[3].toFloat()
        val relativeOppositeX = relativeX / oppositeWidth
        val relativeOppositeY = relativeY / oppositeHeight


            if(remoteIsSmall && name!="user"){
                val screenWidth = binding.videoSmall.width
                val screenHeight = binding.videoSmall.height
               movingSmallX= relativeOppositeX * screenWidth
               movingSmallY = relativeOppositeY * screenHeight
                binding.cursorSmall.visibility=View.VISIBLE
                binding.flMovingSurfaceSmall.visibility=View.VISIBLE
isSmallCursorEnabled=true
                binding.ibPointer.setImageResource(R.drawable.ic_selected_pointer)
                //audioVideo.realtimeSendDataMessage("pointerAdded", "true", 5000)
                binding.cursorSmall.x = movingSmallX - (binding.cursorSmall.width + 2) / 2
                binding.cursorSmall.y = movingSmallY - (binding.cursorSmall.height + 2) / 2
            }
            else if(!remoteIsSmall && name!="user") {
                val screenWidth = resources.displayMetrics.widthPixels
                val screenHeight = resources.displayMetrics.heightPixels
                movingX = relativeOppositeX * screenWidth
                movingY= relativeOppositeY * screenHeight
                binding.cursor.visibility=View.VISIBLE
                binding.flMovingSurface.visibility=View.VISIBLE
                isCursorEnabled=true
                binding.ibPointer.setImageResource(R.drawable.ic_selected_pointer)
              //  audioVideo.realtimeSendDataMessage("pointerAdded", "true", 5000)
                binding.cursor.x = movingX - (binding.cursor.width + 2) / 2
                binding.cursor.y = movingY - (binding.cursor.height + 2) / 2
            }
            else if(remoteIsSmall && name=="user") {
                val screenWidth = resources.displayMetrics.widthPixels
                val screenHeight = resources.displayMetrics.heightPixels
                movingX = relativeOppositeX * screenWidth
               movingY= relativeOppositeY * screenHeight
                binding.cursor.visibility=View.VISIBLE
                binding.flMovingSurface.visibility=View.VISIBLE
                isCursorEnabled=true
                binding.cursor.x = movingX - (binding.cursor.width + 2) / 2
                binding.cursor.y = movingY - (binding.cursor.height + 2) / 2
                binding.ibPointer.setImageResource(R.drawable.ic_selected_pointer)
                //audioVideo.realtimeSendDataMessage("pointerAdded", "true", 5000)

            }
            else if(!remoteIsSmall && name=="user") {
                val screenWidth = binding.videoSmall.width
                val screenHeight = binding.videoSmall.height
                movingSmallX = relativeOppositeX * screenWidth
                movingSmallY= relativeOppositeY * screenHeight
                binding.cursorSmall.visibility=View.VISIBLE
                binding.flMovingSurfaceSmall.visibility=View.VISIBLE
                isSmallCursorEnabled=true
                binding.ibPointer.setImageResource(R.drawable.ic_selected_pointer)
                //audioVideo.realtimeSendDataMessage("pointerAdded", "true", 5000)
                binding.cursorSmall.x = movingSmallX - (binding.cursorSmall.width + 2) / 2
                binding.cursorSmall.y = movingSmallY - (binding.cursorSmall.height + 2) / 2
            }
        }

    fun removePointer(){
        movingX=0f
        movingY=0f
        movingSmallX=0f
        movingSmallY=0f
        isCursorEnabled=false
        isSmallCursorEnabled=false
        binding.cursor.visibility=View.GONE
        binding.cursorSmall.visibility=View.GONE
        binding.flMovingSurfaceSmall.visibility=View.GONE
        binding.flMovingSurface.visibility=View.GONE
    }


//    fun resolutionStates(){
//        if (!remoteIsSmall ){
//            if self.isPortrait && self.isRemotePortrait {
//                largeView.contentMode = .scaleAspectFill
//            } else if self.isPortrait && !self.isRemotePortrait {
//                largeView.contentMode = .scaleAspectFit
//            } else if !self.isPortrait && !self.isRemotePortrait {
//                largeView.contentMode = .scaleAspectFill
//            } else if self.isRemotePortrait && !self.isPortrait {
//                largeView.contentMode = .scaleAspectFit
//            }
//            smallView.contentMode = self.isPortrait ? .scaleAspectFill : .scaleAspectFit
//        } else {
//            if self.isPortrait && self.isRemotePortrait {
//                smallView.contentMode = .scaleAspectFill
//            } else if self.isPortrait && !self.isRemotePortrait {
//                smallView.contentMode = .scaleAspectFit
//            } else if !self.isPortrait && !self.isRemotePortrait {
//                smallView.contentMode = .scaleAspectFit
//            } else if self.isRemotePortrait && !self.isPortrait {
//                smallView.contentMode = .scaleAspectFill
//            }
//            largeView.contentMode = .scaleAspectFill
//        }
//    }


    private fun createVideoCollectionTile(tileState: VideoTileState): VideoCollectionTile {

        val attendeeId = tileState.attendeeId
        val attendeeName = meetingModel.currentRoster[attendeeId]?.attendeeName ?: ""
        if(!tileState.isLocalTile)
        {
            Log.d("getValueName",attendeeName.toString())
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
            binding.tvProviderName.text=videoCollectionTile.attendeeName
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
        Log.d("Coming01", "coming here")
        // Handle local video view
        meetingModel.localVideoTileState?.let { localTile ->
            audioVideo.resumeRemoteVideoTile(localTile.videoTileState.tileId)
            updateLocalView(localTile)  // A method to bind this tile to the local view
        }
        if (meetingModel.getRemoteVideoTileStates().isNotEmpty()) {
            Log.d("Coming02", "coming here")
            meetingModel.getRemoteVideoTileStates()[0].let {
                Log.d("Coming03", "coming here")
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
        if (remoteIsSmall) {
            audioVideo.bindVideoView(binding.videoSmall, remoteTile.videoTileState.tileId)
        } else {
            audioVideo.bindVideoView(binding.bigVideoSurface, remoteTile.videoTileState.tileId)
        }


    }

    private fun setupButtonsBar() {
        binding.apply {
            ibMic.setImageResource(if (meetingModel.isMuted) R.drawable.ic_selected_mic else R.drawable.ic_mic)
            ibMic.setOnClickListener { toggleMute() }
            ibPointer.setOnClickListener {

                if(remoteIsSmall && name=="user") {
                    toggleCursorMode()
                }
                else if (remoteIsSmall && name!="user"){
                    toggleSmallCursorMode()
                }  else if(!remoteIsSmall && name=="user") {
                    toggleSmallCursorMode()
                }
                else if (!remoteIsSmall && name!="user"){
                    toggleCursorMode()
                }

                if(isSmallCursorEnabled==true||isCursorEnabled==true)
                {
                    audioVideo.realtimeSendDataMessage("pointerAdded", "true", 5000)
                }
                else if (!isSmallCursorEnabled && !isCursorEnabled)
                {
                    audioVideo.realtimeSendDataMessage("pointerRemoved", "true", 5000)
                }

            }
            val audioDeviceCapabilities = AudioDeviceCapabilities.InputAndOutput
            ivDisconnect.setOnClickListener { endMeeting() }
            ivVideoExpand.setOnClickListener {
                localIsSmall = !localIsSmall
                remoteIsSmall = !remoteIsSmall
                if(isCursorEnabled||isSmallCursorEnabled){
                    if(isCursorEnabled){
                        val fullWidth = resources.displayMetrics.widthPixels
                        val fullHeight = resources.displayMetrics.heightPixels
                        val relativeOppositeX = movingX / fullWidth
                        val relativeOppositeY = movingY / fullHeight
                        val viewWidth = binding.videoSmall.width
                        val viewHeight = binding.videoSmall.height
                        movingSmallX= relativeOppositeX * viewWidth
                        movingSmallY = relativeOppositeY * viewHeight
                        toggleCursorMode()
                        toggleSmallCursorMode()
                    }
                    else if(isSmallCursorEnabled){
                        val viewWidth = binding.videoSmall.width
                        val viewHeight = binding.videoSmall.height
                        val relativeOppositeX = movingSmallX / viewWidth
                        val relativeOppositeY = movingSmallY / viewHeight
                        val fullWidth = resources.displayMetrics.widthPixels
                        val fullHeight = resources.displayMetrics.heightPixels
                        movingX= relativeOppositeX * fullWidth
                        movingY = relativeOppositeY * fullHeight
                        toggleCursorMode()
                        toggleSmallCursorMode()
                    }

                }
                else{
                    if(!isCursorEnabled)
                    {
                        binding.cursor.visibility=View.GONE
                        binding.flMovingSurface.visibility=View.GONE

                    }
                    if(!isSmallCursorEnabled)
                    {
                        binding.cursor.visibility=View.GONE
                        binding.flMovingSurfaceSmall.visibility=View.GONE
                    }

                }
                updateSingleVideoView()
            }

            ibScreenshot.setOnClickListener { filePermissionCheck() }
            ivDisconnectBottom.setOnClickListener { endMeeting() }
            ibOpenBottomOptions.setOnClickListener {
                isBottomBarVisible = false
                ivDisconnect.visibility = View.VISIBLE
                ivBottomOptions.visibility = View.VISIBLE
                blurViewBottom.visibility = View.GONE
                ibOpenBottomOptions.visibility = View.GONE


            }
            ivBottomOptions.setOnClickListener {
                isBottomBarVisible = true
                ivDisconnect.visibility = View.GONE
                ivBottomOptions.visibility = View.GONE
                blurViewBottom.visibility = View.VISIBLE
                ibOpenBottomOptions.visibility = View.VISIBLE
                tvProviderName.visibility=View.VISIBLE
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

    private fun updateLocalVideoMirror() {
        binding.bigVideoSurface.mirror =
                // If we are using internal source, base mirror state off that device type
            (audioVideo.getActiveCamera()?.type == MediaDeviceType.VIDEO_FRONT_CAMERA ||
                    // Otherwise (audioVideo.getActiveCamera() == null) use the device type of our external/custom camera capture source
                    (audioVideo.getActiveCamera() == null && cameraCaptureSource?.device?.type == MediaDeviceType.VIDEO_FRONT_CAMERA))
    }

    private val SCREEN_RECORD_REQUEST_CODE = 100
    var mProjectionManager: MediaProjectionManager? = null
    var saveDataIntent: Intent? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun startRecordingScreen() {
        val resultIntent = Utils.SAVE_RESULT
        val getResultCode = Utils.RESULT_CODE
        val intent = MediaProjectionIntentHolder.intent
        try {
            Log.d("GetRecordResult", "data:" + resultIntent.toString())
            Log.d("GetRecordResultCode", "data:" + getResultCode.toString())
            if (intent != null) {
                Log.d("GetRecordResultInside", "data:" + resultIntent.toString())
                recordingStart = true
                hbRecorder.startScreenRecording(intent, getResultCode!!)
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    hbRecorder!!.stopScreenRecording()
                }, 12000)


            } else {
                Log.d("GetRecordResultTesting", "data:")
                // Handler(Looper.getMainLooper()).post { transparentSceneView?.pause() }
                val mediaProjectionManager =
                    getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                val permissionIntent = mediaProjectionManager.createScreenCaptureIntent()
                startActivityForResult(permissionIntent, SCREEN_RECORD_REQUEST_CODE)
            }

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

            Log.d("getRecordingData", "data:" + saveDataIntent.toString())
            Log.d("GetRecordResultCodeInside", "data:$resultCode")
            Utils.SAVE_RESULT = data
            Utils.RESULT_CODE = resultCode
            MediaProjectionIntentHolder.intent = data
            recordingStart = true
            Log.d("printUri", "result:" + uriString.toString())
            hbRecorder?.startScreenRecording(data, resultCode)
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                hbRecorder!!.stopScreenRecording()
            }, 12000)
        }
    }


    private fun endMeeting() {
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
        } else {
            audioVideo.realtimeLocalUnmute()
            binding.ibMic.setImageResource(R.drawable.ic_selected_mic)
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
            Log.d("CameraCheck", "11")
            meetingModel.isLocalVideoStarted = true
            val localVideoConfig = LocalVideoConfiguration(meetingModel.localVideoMaxBitRateKbps)
            if (meetingModel.isUsingCameraCaptureSource) {
                Log.d("CameraCheck", "22")
                if (meetingModel.isUsingGpuVideoProcessor) {
                    Log.d("CameraCheck", "33")
                    cameraCaptureSource.addVideoSink(gpuVideoProcessor)
                    audioVideo.startLocalVideo(gpuVideoProcessor, localVideoConfig)
                } else if (meetingModel.isUsingCpuVideoProcessor) {
                    Log.d("CameraCheck", "44")
                    cameraCaptureSource.addVideoSink(cpuVideoProcessor)
                    audioVideo.startLocalVideo(cpuVideoProcessor, localVideoConfig)
                } else if (meetingModel.isUsingBackgroundBlur) {
                    Log.d("CameraCheck", "55")
                    cameraCaptureSource.addVideoSink(backgroundBlurVideoFrameProcessor)
                    audioVideo.startLocalVideo(backgroundBlurVideoFrameProcessor, localVideoConfig)
                } else if (meetingModel.isUsingBackgroundReplacement) {
                    Log.d("CameraCheck", "66")
                    cameraCaptureSource.addVideoSink(backgroundReplacementVideoFrameProcessor)
                    audioVideo.startLocalVideo(
                        backgroundReplacementVideoFrameProcessor,
                        localVideoConfig
                    )
                } else {
                    Log.d("CameraCheck", "77")
                    audioVideo.startLocalVideo(cameraCaptureSource, localVideoConfig)
                }
                Log.d("CameraCheck", "88")

                cameraCaptureSource.start()
            } else {
                Log.d("CameraCheck", "99")
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
            Log.d("CameraCheck", "10")
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
        audioVideo.addRealtimeDataMessageObserver("orientation", this)
        audioVideo.addRealtimeDataMessageObserver("pointerDataTransfer", this)
        audioVideo.addRealtimeDataMessageObserver("pointerRemoved", this)
        audioVideo.addRealtimeDataMessageObserver("pointerAdded", this)
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


    fun getAudioVideo(): AudioVideoFacade = meetingSessionModel.audioVideo


    fun getMeetingSessionCredentials(): MeetingSessionCredentials = meetingSessionModel.credentials

    fun getEglCoreFactory(): EglCoreFactory = meetingSessionModel.eglCoreFactory

    fun getCameraCaptureSource(): CameraCaptureSource = meetingSessionModel.cameraCaptureSource

    fun getGpuVideoProcessor(): GpuVideoProcessor = meetingSessionModel.gpuVideoProcessor

    fun getCpuVideoProcessor(): CpuVideoProcessor = meetingSessionModel.cpuVideoProcessor
    private fun makeViewBlur(blurView: BlurView, radius: Float) {
        val decorView = window.decorView.rootView as ViewGroup
        blurView.setupWith(decorView, RenderScriptBlur(this@MainActivity)) // or RenderEffectBlur
            .setBlurRadius(15f)

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

    private fun clickListeners() {
        binding.apply {
            ivVideoCompress.setOnClickListener {
                isVideoCompressed=true
                ibShowSmallView.visibility = View.VISIBLE
                clWorking.visibility = View.GONE
                ivVideoCompress.visibility = View.GONE
                ivVideoExpand.visibility = View.GONE
            }
            ibShowSmallView.setOnClickListener {
                isVideoCompressed=false
                ibShowSmallView.visibility = View.GONE
                clWorking.visibility = View.VISIBLE
                ivVideoCompress.visibility = View.VISIBLE
                ivVideoExpand.visibility = View.VISIBLE
            }
            blurViewTimer.setOnClickListener {
                startRecordingScreen()
            }

        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d("Orientation", "Landscape mode")
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d("Orientation", "Portrait mode")
        }
    }

    override fun onResume() {
        super.onResume()
        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable()
        }
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
        orientationEventListener.disable()
        if (Utils.SAVE_RESULT != null) {
            saveDataIntent = Utils.SAVE_RESULT
        }
    }

    private fun updateConstraintsForLandscapeLeft() {
        val constraintSet = ConstraintSet().apply {
            clone(binding.tileContainer)
            connect(
                binding.blurViewSideBar.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START
            )
            connect(
                binding.blurViewSideBar.id,
                ConstraintSet.BOTTOM,
                binding.blurViewBottom.id,
                ConstraintSet.TOP
            )
            clear(binding.blurViewSideBar.id, ConstraintSet.END)
            clear(binding.blurViewSideBar.id, ConstraintSet.TOP)

            connect(
                binding.clWorking.id,
                ConstraintSet.END,
                binding.blurViewTimer.id,
                ConstraintSet.START
            )
            connect(
                binding.clWorking.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP
            )
            clear(binding.clWorking.id, ConstraintSet.BOTTOM)
            clear(binding.clWorking.id, ConstraintSet.START)
            setMargin(binding.clWorking.id, ConstraintSet.END, 35)
            setMargin(binding.clWorking.id, ConstraintSet.TOP, -33)
            setMargin(binding.clScreenshot.id, ConstraintSet.END, 10)
            clear(binding.clScreenshot.id, ConstraintSet.START)
            connect(
                binding.ibOpenBottomOptions.id,
                ConstraintSet.TOP,
                binding.blurViewBottom.id,
                ConstraintSet.TOP
            )

            // Set end to end of parent
            connect(
                binding.ibOpenBottomOptions.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END
            )
            clear(binding.ibOpenBottomOptions.id, ConstraintSet.START)
            clear(binding.ibOpenBottomOptions.id, ConstraintSet.BOTTOM)
            setMargin(binding.ibOpenBottomOptions.id, ConstraintSet.TOP, -18)
            setMargin(binding.ibOpenBottomOptions.id, ConstraintSet.END, 20)


            connect(
                binding.blurViewBottom.id,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM
            )
            clear(binding.blurViewBottom.id, ConstraintSet.TOP)
            setMargin(binding.blurViewSideBar.id, ConstraintSet.BOTTOM, 10)
            setMargin(binding.blurViewBottom.id, ConstraintSet.BOTTOM, -20)


            connect(
                binding.ivDisconnect.id,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM
            )
            clear(binding.ivDisconnect.id, ConstraintSet.TOP)
            setMargin(binding.ivDisconnect.id, ConstraintSet.BOTTOM, 37)
            connect(
                binding.ivBottomOptions.id,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM
            )
            clear(binding.ivBottomOptions.id, ConstraintSet.TOP)
            setMargin(binding.ivBottomOptions.id, ConstraintSet.BOTTOM, 30)


            connect(
                binding.blurViewTimer.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP
            )
            connect(
                binding.blurViewTimer.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END
            )
            clear(binding.blurViewTimer.id, ConstraintSet.BOTTOM)
            clear(binding.blurViewTimer.id, ConstraintSet.START)
            setMargin(binding.blurViewTimer.id, ConstraintSet.TOP, 87)
            setMargin(binding.blurViewTimer.id, ConstraintSet.END, 8)
        }
        constraintSet.applyTo(binding.tileContainer)
    }

    private fun updateConstraintsForLandscapeRight() {
        val bottomConstraintSet = ConstraintSet().apply {
            clone(binding.tileContainer)
            setMargin(binding.clScreenshot.id, ConstraintSet.START, 10)
            clear(binding.clScreenshot.id, ConstraintSet.END)
            connect(
                binding.clWorking.id,
                ConstraintSet.START,
                binding.blurViewTimer.id,
                ConstraintSet.END
            )
            connect(
                binding.clWorking.id,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM
            )
            setMargin(binding.clWorking.id, ConstraintSet.START, 35)
            setMargin(binding.clWorking.id, ConstraintSet.BOTTOM, -10)
            clear(binding.clWorking.id, ConstraintSet.END)
            clear(binding.clWorking.id, ConstraintSet.TOP)

            connect(
                binding.ibOpenBottomOptions.id,
                ConstraintSet.BOTTOM,
                binding.blurViewBottom.id,
                ConstraintSet.BOTTOM
            )

            // Set end to end of parent
            connect(
                binding.ibOpenBottomOptions.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START
            )
            clear(binding.ibOpenBottomOptions.id, ConstraintSet.END)
            clear(binding.ibOpenBottomOptions.id, ConstraintSet.TOP)
            setMargin(binding.ibOpenBottomOptions.id, ConstraintSet.BOTTOM, -18)
            setMargin(binding.ibOpenBottomOptions.id, ConstraintSet.START, 20)


            connect(
                binding.blurViewBottom.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP
            )
            clear(binding.blurViewBottom.id, ConstraintSet.BOTTOM)
            connect(
                binding.blurViewSideBar.id,
                ConstraintSet.TOP,
                binding.blurViewBottom.id,
                ConstraintSet.BOTTOM
            )

            connect(
                binding.ivDisconnect.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP
            )
            clear(binding.ivDisconnect.id, ConstraintSet.BOTTOM)
            setMargin(binding.ivDisconnect.id, ConstraintSet.TOP, 37)
            connect(
                binding.ivBottomOptions.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP
            )
            clear(binding.ivBottomOptions.id, ConstraintSet.BOTTOM)
            setMargin(binding.ivBottomOptions.id, ConstraintSet.TOP, 35)

            connect(
                binding.blurViewSideBar.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END
            )
            clear(binding.blurViewSideBar.id, ConstraintSet.START)
            clear(binding.blurViewSideBar.id, ConstraintSet.BOTTOM)
            setMargin(binding.blurViewSideBar.id, ConstraintSet.TOP, 10)
            setMargin(binding.blurViewBottom.id, ConstraintSet.TOP, -20)

            connect(
                binding.blurViewTimer.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START
            )
            connect(
                binding.blurViewTimer.id,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM
            )
            clear(binding.blurViewTimer.id, ConstraintSet.END)
            clear(binding.blurViewTimer.id, ConstraintSet.TOP)
            setMargin(binding.blurViewTimer.id, ConstraintSet.START, 20)
            setMargin(binding.blurViewTimer.id, ConstraintSet.BOTTOM, 110)
        }
        bottomConstraintSet.applyTo(binding.tileContainer)
    }


    private fun updateConstraintsForPortrait() {
        val constraintSet = ConstraintSet().apply {
            clone(binding.tileContainer)
            clear(binding.clScreenshot.id, ConstraintSet.START)
            clear(binding.clScreenshot.id, ConstraintSet.END)
            connect(
                binding.blurViewBottom.id,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM
            )
            clear(binding.blurViewBottom.id, ConstraintSet.TOP)

            connect(
                binding.ibOpenBottomOptions.id,
                ConstraintSet.TOP,
                binding.blurViewBottom.id,
                ConstraintSet.TOP
            )

            connect(
                binding.ivDisconnect.id,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM
            )
            clear(binding.ivDisconnect.id, ConstraintSet.TOP)

            connect(
                binding.ivBottomOptions.id,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM
            )
            clear(binding.ivBottomOptions.id, ConstraintSet.TOP)
            setMargin(binding.ivBottomOptions.id, ConstraintSet.BOTTOM, 35)
            setMargin(binding.ivDisconnect.id, ConstraintSet.BOTTOM, 37)

            // Set end to end of parent
            connect(
                binding.ibOpenBottomOptions.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START
            )
            clear(binding.ibOpenBottomOptions.id, ConstraintSet.END)
            clear(binding.ibOpenBottomOptions.id, ConstraintSet.BOTTOM)
            setMargin(binding.ibOpenBottomOptions.id, ConstraintSet.TOP, -18)
            setMargin(binding.ibOpenBottomOptions.id, ConstraintSet.START, 20)
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
            clear(binding.clWorking.id, ConstraintSet.BOTTOM)
            clear(binding.clWorking.id, ConstraintSet.START)
            setMargin(binding.clWorking.id, ConstraintSet.END, 30)
            setMargin(binding.clWorking.id, ConstraintSet.TOP, 30)
            connect(
                binding.blurViewSideBar.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END
            )
            connect(
                binding.blurViewSideBar.id,
                ConstraintSet.BOTTOM,
                binding.blurViewBottom.id,
                ConstraintSet.TOP
            )
            setMargin(binding.blurViewSideBar.id, ConstraintSet.BOTTOM, 10)
            setMargin(binding.blurViewBottom.id, ConstraintSet.BOTTOM, -20)
            clear(binding.blurViewSideBar.id, ConstraintSet.START)
            clear(binding.blurViewSideBar.id, ConstraintSet.TOP)

            connect(
                binding.blurViewBottom.id,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM
            )
            clear(binding.blurViewBottom.id, ConstraintSet.TOP)
            connect(
                binding.blurViewTimer.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START
            )
            connect(
                binding.blurViewTimer.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP
            )
            clear(binding.blurViewTimer.id, ConstraintSet.END)
            clear(binding.blurViewTimer.id, ConstraintSet.BOTTOM)
            setMargin(binding.blurViewTimer.id, ConstraintSet.TOP, 30)
            setMargin(binding.blurViewTimer.id, ConstraintSet.START, 30)

        }
        constraintSet.applyTo(binding.tileContainer)
    }

    fun rotationOfView(rotation: Float, orientation: String) {
        binding.apply {
            if (isBottomBarVisible) {
                ibTools.rotation = rotation
                ibChat.rotation = rotation
                ivDisconnectBottom.rotation = rotation
                ibMic.rotation = rotation
                ibSwitchCamera.rotation = rotation
            } else {
                ivDisconnect.rotation = rotation
                ivBottomOptions.rotation = rotation
            }
            if (isToolListVisible) {
                if (orientation == "left") {
                    clScreenshot.rotation = 90f
                    clLabel.rotation = 90f
                    clPen.rotation = 90f
                    clHighlighter.rotation = 90f
                    clPointer.rotation = 90f
                } else {
                    clScreenshot.rotation = rotation
                    clLabel.rotation = rotation
                    clPen.rotation = rotation
                    clHighlighter.rotation = rotation
                    clPointer.rotation = rotation
                }
            }
        }

    }

    fun commonViewsRotation(rotation: Float) {
        binding.apply {
            clWorking.rotation = rotation
            ibShowSmallView.rotation = rotation
            blurViewTimer.rotation = rotation
            ibOpenBottomOptions.rotation = rotation
            ivDisconnect.rotation = rotation
            ivBottomOptions.rotation = rotation
        }
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
                if (timeFormatted == "01:00" && !isInExtraTime) {
                    showExtraTimeDialog()
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


    private fun showExtraTimeDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Extra Time Needed?")
            .setMessage("You have 1 minute left. Would you like to add more time?")
            .setPositiveButton("Yes") { _, _ ->
                isInExtraTime = true

            }
            .setNegativeButton("No") { dialog, _ ->
                isInExtraTime = false
                dialog.dismiss()
            }
            .create()
        if (!isFinishing) {
            dialog.show()
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
            if(meetingModel.videoStatesInCurrentPage.size==2){
               // Toast.makeText(this@MainActivity,"2 users there",Toast.LENGTH_SHORT).show()

                if(!isTimerStarted){
                    isTimerStarted=true
                    if(name=="user") {
                        startCountdownTimer(300000)
                    }
                    else{
                        startCountdownTimer(300000)
                    }
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
        Log.d("resumeTile", "working")
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
        if (tileState.isLocalTile.not()) {  // Check if it’s the remote user
            val width = tileState.videoStreamContentWidth
            val height = tileState.videoStreamContentHeight

            // Determine orientation
            if (width > height) {
                Log.d("getResolution", "changed")
                // Landscape orientation

            } else {
                Log.d("getResolution01", "changed")
                // Portrait orientation

            }
        }
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

    override fun onDataMessageReceived(dataMessage: DataMessage) {
//        val senderId = dataMessage.senderAttendeeId
//        val messageContent = String(dataMessage.data)

        if (dataMessage.topic == "pointerDataTransfer") {
            val messageContent = dataMessage.text()
            val valuesPart = messageContent.substringAfter("[").substringBefore("]")
            val (x, y, width, height) = valuesPart.split(",").map { it }
            try {
                val floatArrayList = ArrayList<String>()
                floatArrayList.add(x)
                floatArrayList.add(y)
                floatArrayList.add(width)
                floatArrayList.add(height)
                Log.d("pointerDataTransfer",floatArrayList.toString())
                println("Received float array list: $floatArrayList")
                receiveCoordsData(floatArrayList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else  if (dataMessage.topic == "orientation") {

            Log.d("getDataOrient","Working")
//            if(String(dataMessage.data)=="Landscape_right")
//                {
//
//                    adjustRenderViewForLandscape()
//
//                }
//            else if(String(dataMessage.data)=="Landscape_left")
//            {
//                adjustRenderViewForLandscape()
//            }
//            else{
//              //  adjustRenderViewForLandscape()
//            }


        } else  if (dataMessage.topic == "pointerAdded") {
            if(name=="user")
            {
                if(localIsSmall)
                {isSmallCursorEnabled=true
                    val centerX = binding.videoSmall.width / 2
                    val centerY =  binding.videoSmall.height  / 2
                    moveCursor(centerX.toFloat(), centerY.toFloat())
                    binding.apply {
                        cursor.visibility = View.GONE
                        flMovingSurface.visibility = View.GONE
                        cursorSmall.visibility=View.VISIBLE
                        flMovingSurfaceSmall.visibility=View.VISIBLE
                    }
                }
                else{
                    isCursorEnabled=true
                    val centerX = resources.displayMetrics.widthPixels / 2
                    val centerY = resources.displayMetrics.heightPixels / 2
                    moveCursor(centerX.toFloat(), centerY.toFloat())
                    binding.apply {
                        cursor.visibility = View.VISIBLE
                        flMovingSurface.visibility = View.VISIBLE
                        cursorSmall.visibility=View.GONE
                        flMovingSurfaceSmall.visibility=View.GONE
                    }
                }

            }
            else{
                if(remoteIsSmall)
                {
                    isSmallCursorEnabled=true
                    val centerX = binding.videoSmall.width / 2
                    val centerY =  binding.videoSmall.height  / 2
                    moveCursor(centerX.toFloat(), centerY.toFloat())
                    binding.apply {
                        cursor.visibility = View.GONE
                        flMovingSurface.visibility = View.GONE
                        cursorSmall.visibility=View.VISIBLE
                        flMovingSurfaceSmall.visibility=View.VISIBLE
                    }
                }
                else{
                    isCursorEnabled=true
                    val centerX = resources.displayMetrics.widthPixels / 2
                    val centerY = resources.displayMetrics.heightPixels / 2
                    moveCursor(centerX.toFloat(), centerY.toFloat())
                    binding.apply {
                        cursor.visibility = View.VISIBLE
                        flMovingSurface.visibility = View.VISIBLE
                        cursorSmall.visibility=View.GONE
                        flMovingSurfaceSmall.visibility=View.GONE
                    }
                }
            }


        } else  if (dataMessage.topic == "pointerRemoved") {
            if(isCursorEnabled)
            {
                toggleCursorMode()
            }
            if(isSmallCursorEnabled)
            {
                toggleSmallCursorMode()
            }
            binding.ibPointer.setImageResource(R.drawable.ic_pointer)

        }
        Log.d("getMessageContent", "messageContent:" + "messageContent.toString()")
    }

    override fun onContentShareStarted() {
    }

    override fun onContentShareStopped(status: ContentShareStatus) {
    }

    override fun onEventReceived(name: EventName, attributes: EventAttributes) {
    }

    override fun onTranscriptEventReceived(transcriptEvent: TranscriptEvent) {

    }

    override val scoreCallbackIntervalMs: Int? get() = 1000


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
                        .toString() + File.separator + "ArModule"
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
            Log.d("geturi_p", mFile.toString())

        } else {
            createFolder()
            hbRecorder.setOutputPath(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                    .toString() + "/HBRecorder"
            )

        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun updateGalleryUri() {
        setOutputPath()
        contentValues()!!.clear()
        contentValues()!!.put(MediaStore.Video.Media.IS_PENDING, 0)
        contentResolver.update(mUri!!, contentValues, null, null)
        Log.d("geturi_abc", mUri.toString())
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun refreshGalleryFile() {
        MediaScannerConnection.scanFile(
            this, arrayOf(hbRecorder!!.filePath), null
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (hbRecorder!!.wasUriSet()) {
                updateGalleryUri();
            } else {
                refreshGalleryFile();
                Log.d("geturi_cfff", mFile.toString())
            }


            var afile = File(hbRecorder!!.filePath)
            Log.d("testing_file", afile.toString())
            recordingStart = false
            afile.let {
                hbRecorder?.filePath?.toUri().let { it1 ->
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
                            Log.d("VideoSave", "Video saved successfully to: $newVideoUri")
                        } catch (e: Exception) {
                            Log.e("VideoSave", "Error saving video: ${e.message}")
                        }
                    } else {
                        Log.e("VideoSave", "Failed to create new video entry in MediaStore")
                    }
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