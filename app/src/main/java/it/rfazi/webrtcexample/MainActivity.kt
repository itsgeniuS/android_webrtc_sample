package it.rfazi.webrtcexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import it.rfazi.webrtcexample.ui.theme.WebRTCExampleTheme
import it.rfazi.webrtcexample.webRtc.WebRTCPlayer
import kotlinx.coroutines.launch
import org.webrtc.EglBase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WebRTCExampleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val coroutineScope = rememberCoroutineScope()
                    val eglBase = remember { EglBase.create() }
                    val webrtcPlayer = remember { WebRTCPlayer(context, eglBase) }

                    Column(modifier = Modifier.fillMaxSize()) {
                        AndroidView(
                            factory = { webrtcPlayer.getView() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16 / 9f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val sdpOffer = webrtcPlayer.fetchSDPOffer()
                                    webrtcPlayer.setRemoteSDP(sdpOffer)
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(text = "Start WebRTC Stream")
                        }
                    }
                }
            }
        }
    }
}
