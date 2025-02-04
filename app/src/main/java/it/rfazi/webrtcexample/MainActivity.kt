package it.rfazi.webrtcexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import it.rfazi.webrtcexample.ui.theme.WebRTCExampleTheme
import it.rfazi.webrtcexample.webRTC.WebRTCClient
import it.rfazi.webrtcexample.webRTC.WebRTCManager
import org.webrtc.EglBase
import org.webrtc.PeerConnection
import org.webrtc.SurfaceViewRenderer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WebRTCExampleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WebRTCStreamScreen()
                }
            }
        }
    }
}

@Composable
fun WebRTCStreamScreen() {
    val context = LocalContext.current
    val surfaceViewRenderer = remember { SurfaceViewRenderer(context) }
    val webRTCManager = remember { WebRTCManager(context) }
    val webRTCClient = remember {
        WebRTCClient(
            context,
            webRTCManager.getPeerConnectionFactory(),
            surfaceViewRenderer
        )
    }

    LaunchedEffect(Unit) {
        webRTCClient.initializeRemoteStream()
    }

    WebRTCVideoView(surfaceViewRenderer)
}

@Composable
fun WebRTCVideoView(surfaceViewRenderer: SurfaceViewRenderer) {
    AndroidView(
        factory = { context ->
            surfaceViewRenderer.apply {
                setMirror(false)
                init(EglBase.create().eglBaseContext, null)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}