package it.rfazi.webrtcexample.webRTC

import android.content.Context
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

class WebRTCClient(
    private val context: Context,
    private val peerConnectionFactory: PeerConnectionFactory,
    private val surfaceViewRenderer: SurfaceViewRenderer
) {
    private var peerConnection: PeerConnection? = null
    private var videoTrack: VideoTrack? = null

    fun initializeRemoteStream() {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )

        peerConnection = peerConnectionFactory.createPeerConnection(
            PeerConnection.RTCConfiguration(iceServers),
            object : PeerConnection.Observer {
                override fun onAddStream(stream: MediaStream?) {
                    stream?.videoTracks?.get(0)?.addSink(surfaceViewRenderer)
                }

                override fun onIceCandidate(candidate: IceCandidate?) {
                    // Send ICE candidate to signaling server
                }

                override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
                override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {}
                override fun onDataChannel(channel: DataChannel?) {}
                override fun onIceConnectionReceivingChange(p0: Boolean) {}
                override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}
                override fun onRemoveStream(stream: MediaStream?) {}
                override fun onRenegotiationNeeded() {}
                override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
            }
        )
    }

    fun setRemoteDescription(sdp: SessionDescription) {
        peerConnection?.setRemoteDescription(
            object : SdpObserver {
                override fun onSetSuccess() {}
                override fun onSetFailure(error: String?) {}
                override fun onCreateSuccess(sdp: SessionDescription?) {}
                override fun onCreateFailure(error: String?) {}
            }, sdp
        )
    }
}
