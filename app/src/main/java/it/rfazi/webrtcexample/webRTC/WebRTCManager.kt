package it.rfazi.webrtcexample.webRTC

import android.content.Context
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.EglBase
import org.webrtc.PeerConnectionFactory

class WebRTCManager(private val context: Context) {
    private var eglBase: EglBase = EglBase.create()
    private var peerConnectionFactory: PeerConnectionFactory

    init {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .createPeerConnectionFactory()
    }

    fun getPeerConnectionFactory(): PeerConnectionFactory {
        return peerConnectionFactory
    }
}
