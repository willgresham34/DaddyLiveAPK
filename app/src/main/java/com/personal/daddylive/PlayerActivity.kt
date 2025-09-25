package com.personal.daddylive

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.Role

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
              iframe { width:100%; height:100%; border:0; }
            </style>
          </head>
          <body>
            <div class="wrap">
              <iframe
                src="https://dlhd.dad/$segment/stream-$id.php"
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
        var loading by remember { mutableStateOf(true) }
        var iframeFocused by remember { mutableStateOf(false) }   // <-- know when iframe has focus
        var webViewRef by remember { mutableStateOf<WebView?>(null) }

        val playerShape = RoundedCornerShape(16.dp)
        val playerRing = if (iframeFocused) 3.dp else 0.dp
        val playerRingColor = if (iframeFocused) MaterialTheme.colorScheme.primary else Color.Transparent
        val playerShadow = if (iframeFocused) 16.dp else 8.dp
        val playerFocusRequester = remember { FocusRequester() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Player card with focus styling + loading overlay
            Card(
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .aspectRatio(16f / 9f)
                    .border(playerRing, playerRingColor, playerShape)
                    .clip(playerShape)
                    .focusRequester(playerFocusRequester)
                    .onFocusChanged { iframeFocused = it.isFocused } // Compose sees focus on the container
            ) {
                Box(Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                // Make the WebView itself focusable so remote focus can land on it
                                isFocusable = true
                                isFocusableInTouchMode = true

                                // Mirror focus into Compose state
                                onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                                    iframeFocused = hasFocus
                                }

                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.mediaPlaybackRequiresUserGesture = false
                                settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                                if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                                    WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, true)
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        loading = true
                                    }
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        loading = false
                                    }
                                }

                                // Initial load via iframe (not direct loadUrl)
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

            Spacer(Modifier.height(5.dp))

            // Segment selector row with the SAME focus affordance as cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,

            ) {
                Text(
                    text = "$title",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(end = 24.dp)
                )
                Text(
                    text = "Player:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(end = 12.dp)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(segments) { seg ->
                        FocusableSegmentChip(
                            text = seg.uppercase(),
                            selected = seg == selected,
                            onClick = {
                                selected = seg
                                loading = true
                            }
                        )
                    }
                }
            }
        }
    }
}

/* ---------- Focus helpers ---------- */

/** TV-clickable: DPAD_CENTER / ENTER trigger onClick; no ripple */
private fun Modifier.tvClickable(onClick: () -> Unit) = composed {
    val interactions = remember { MutableInteractionSource() }
    this
        .onPreviewKeyEvent { ev ->
            if (ev.type == KeyEventType.KeyDown) {
                val code = ev.nativeKeyEvent.keyCode
                if (
                    code == android.view.KeyEvent.KEYCODE_DPAD_CENTER ||
                    code == android.view.KeyEvent.KEYCODE_ENTER ||
                    code == android.view.KeyEvent.KEYCODE_NUMPAD_ENTER
                ) {
                    onClick(); true
                } else false
            } else false
        }
        .then(
            Modifier.clickable(
                interactionSource = interactions,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
        )
}

/** A FilterChip with visible TV focus: scale + ring + container tint */
@Composable
private fun FocusableSegmentChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1f else .9f, label = "chip-scale")
    val ring = if (focused) 2.dp else 0.dp
    val ringColor = if (focused) MaterialTheme.colorScheme.primary else Color.Transparent
    val shape = RoundedCornerShape(12.dp)

    // Decide container color based on state
    val containerColor =
        when {
            selected && focused -> MaterialTheme.colorScheme.primary
            selected -> MaterialTheme.colorScheme.primaryContainer
            focused -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        }

    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .border(ring, ringColor, shape)
            .clip(shape)
            .onFocusChanged { focused = it.isFocused }
            .tvClickable(onClick)
    ) {
        Surface( // custom surface for background
            color = containerColor,
            shape = shape
        ) {
            Box(
                Modifier
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
