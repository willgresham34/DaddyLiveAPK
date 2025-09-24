package com.personal.daddylive

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import androidx.compose.foundation.background
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults


class PlayerActivity : ComponentActivity() {

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_STREAM_ID = "stream_id"
    }

    private fun iframeHtml(segment: String, id: Int): String = """
        <!doctype html>
        <html>
          <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
              html,body { margin:0; padding:0; height:100%; background:#000; }
              .wrap { position:fixed; inset:0; }
            </style>
          </head>
          <body>
            <div class="wrap">
              <iframe
                src="https://dlhd.dad/$segment/stream-$id.php"
                width="100%" 
                height="100%"
                style="border:0;"
                allowfullscreen
                referrerpolicy="no-referrer">
              </iframe>
            </div>
          </body>
        </html>
    """.trimIndent()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val streamId = intent.getIntExtra(EXTRA_STREAM_ID, -1)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Player"
        val segments = listOf("stream", "cast", "watch", "plus", "casting", "player")



        WebView.setWebContentsDebuggingEnabled(true)

        setContent {
            MaterialTheme {
                PlayerScreen(streamId = streamId, title = title, segments = segments)
            }
        }
    }

    @Composable
    fun PlayerScreen(streamId: Int, title: String, segments: List<String>) {
        var selected by remember { mutableStateOf(segments.first()) }
        var loading by remember { mutableStateOf(true) }   // track loading state
        var webViewRef by remember { mutableStateOf<WebView?>(null) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Player card with overlay
            Card(
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(16f / 9f)
            ) {
                Box(Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.mediaPlaybackRequiresUserGesture = false
                                settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        loading = true
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        loading = false
                                    }
                                }

                                loadDataWithBaseURL(
                                    "https://dlhd.dad",
                                    iframeHtml(selected, streamId),
                                    "text/html",
                                    "UTF-8",
                                    null
                                )
                                webViewRef = this
                            }
                        },
                        update = { wv ->
                            wv.loadDataWithBaseURL(
                                "https://dlhd.dad",
                                iframeHtml(selected, streamId),
                                "text/html",
                                "UTF-8",
                                null
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (loading) {
                        // Overlay while loading
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Segment selector row...
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(segments) { seg ->
                    FilterChip(
                        selected = seg == selected,
                        onClick = { selected = seg; loading = true },
                        label = { Text(seg.uppercase()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    }

}
