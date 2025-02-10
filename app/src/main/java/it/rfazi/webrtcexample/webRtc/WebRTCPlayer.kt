package it.rfazi.webrtcexample.webRtc

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

class WebRTCPlayer(context: Context, private val eglBase: EglBase) {

    companion object {
        const val TAG = "WebRTCPlayer"
//        const val streamUrl = "http://192.168.122.1:1985/rtc/v1/whep/?app=live&stream=livestream"
//        const val streamUrl = "http://10.10.13.95:1985/rtc/v1/whep/?app=live&stream=livestream"
//        const val STREAM_URL = "http://127.0.0.1:1985/rtc/v1/whep/?app=live&stream=livestream"
        //https://310c-111-90-186-31.ngrok-free.app/
        const val STREAM_URL = "https://310c-111-90-186-31.ngrok-free.app/rtc/v1/whep/?app=live&stream=livestream"
    }

    private val peerConnectionFactory: PeerConnectionFactory
    private val peerConnection: PeerConnection?
    private val surfaceViewRenderer: SurfaceViewRenderer

    init {
        // Initialize WebRTC
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )

        // Create factory
        val options = PeerConnectionFactory.Options()
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
//            .setAudioDeviceModule(JavaAudioDeviceModule.builder(context).createAudioDeviceModule())
            .createPeerConnectionFactory()

        // Initialize the video renderer
        surfaceViewRenderer = SurfaceViewRenderer(context).apply {
            init(eglBase.eglBaseContext, null)
            setMirror(false)
        }

        // Create PeerConnection
        peerConnection = peerConnectionFactory.createPeerConnection(
            listOf(
                PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
            ),
            object : PeerConnection.Observer {
                override fun onIceCandidate(candidate: IceCandidate?) {}
                override fun onTrack(transceiver: RtpTransceiver?) {
                    transceiver?.receiver?.track()?.let { mediaStreamTrack ->
                        if (mediaStreamTrack is VideoTrack) {
                            mediaStreamTrack.addSink(surfaceViewRenderer)
                        }
                    }
                }

                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {}
                override fun onIceConnectionReceivingChange(receiving: Boolean) {}
                override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {}
                override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState?) {}
                override fun onSignalingChange(newState: PeerConnection.SignalingState?) {}
                override fun onAddTrack(
                    receiver: RtpReceiver?,
                    mediaStreams: Array<out MediaStream>?
                ) {
                }

                override fun onDataChannel(channel: DataChannel?) {}
                override fun onRenegotiationNeeded() {}
                override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
                override fun onAddStream(stream: MediaStream?) {}
                override fun onRemoveStream(stream: MediaStream?) {}
                override fun onRemoveTrack(receiver: RtpReceiver?) {}
            }
        )
    }

    fun getView(): SurfaceViewRenderer = surfaceViewRenderer

    fun setRemoteSDP(sdpOffer: String) {
        val remoteSdp = SessionDescription(SessionDescription.Type.OFFER, sdpOffer)
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                Log.e(TAG, "onSetSuccess SDP : ${sdp?.description}")
            }

            override fun onSetSuccess() {
                Log.e(TAG, "onSetSuccess : createAnswer")

                peerConnection.createAnswer(object : SdpObserver {
                    override fun onCreateSuccess(answer: SessionDescription?) {
                        answer?.let {
                            peerConnection.setLocalDescription(this, it)
                            CoroutineScope(Dispatchers.IO).launch {
                                sendAnswerToServer(it)
                            }
                        }
                    }

                    override fun onSetSuccess() {
                        Log.e(TAG, "onSetSuccess")
                    }

                    override fun onCreateFailure(error: String?) {
                        Log.e(TAG, "onCreateFailure: $error")
                    }

                    override fun onSetFailure(error: String?) {
                        Log.e(TAG, "onSetFailure: $error")
                    }
                }, MediaConstraints())
            }

            override fun onCreateFailure(error: String?) {}
            override fun onSetFailure(error: String?) {}
        }, remoteSdp)
    }

    suspend fun sendAnswerToServer(answer: SessionDescription) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(STREAM_URL)
            .post(answer.description.toRequestBody("application/sdp".toMediaType()))
            .build()

        withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("Failed to send SDP Answer")
            }
        }
    }

    suspend fun fetchSDPOffer(): String {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(STREAM_URL)
            .post("".toRequestBody("application/sdp".toMediaType()))
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                response.body?.string() ?: throw Exception("Failed to fetch SDP")
            }
        }
    }
}
