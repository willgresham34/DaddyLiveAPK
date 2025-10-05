package com.personal.daddylive

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.input.key.type
import com.personal.daddylive.model.VideoOption

class MainActivity : ComponentActivity() {

    private val options = listOf(
        VideoOption("ESPN", 44),
        VideoOption("ESPN 2", 45),
        VideoOption("ESPN 3", 46),
        VideoOption("SEC", 385),
        VideoOption("Fox Sports", 39),
        VideoOption("Fox Sports 2", 40),
        VideoOption("UFC", 86)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Box(Modifier.fillMaxSize().padding(24.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(options.size) { index ->
                            val opt = options[index]
                            FocusableCard(
                                title = opt.title,
                                autoFocus = index == 0,
                                onClick = {
                                    startActivity(
                                        Intent(this@MainActivity, PlayerActivity::class.java)
                                            .putExtra(PlayerActivity.EXTRA_TITLE, opt.title)
                                            .putExtra(PlayerActivity.EXTRA_STREAM_ID, opt.streamId)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/** A TV-friendly card that visibly reacts to focus (scale, ring, shadow) */
@Composable
private fun FocusableCard(
    title: String,
    onClick: () -> Unit,
    autoFocus: Boolean = false,
    width: Dp = 280.dp,
    height: Dp = 160.dp
) {
    var focused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(autoFocus) { if (autoFocus) focusRequester.requestFocus() }

    val scale by animateFloatAsState(if (focused) 1f else .9f, label = "focus-scale")
    val ringThickness = if (focused) 3.dp else 0.dp
    val ringColor = if (focused) MaterialTheme.colorScheme.primary else Color.Transparent
    val elevation = if (focused) 16.dp else 2.dp
    val containerColor =
        if (focused) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant

    val shape = RoundedCornerShape(16.dp)

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier
            .size(width = width, height = height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation, shape, clip = false)
            .border(ringThickness, ringColor, shape)
            .clip(shape)
            .focusRequester(focusRequester)
            .onFocusChanged { focused = it.isFocused }
            .tvClickable(onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // we use shadow() above
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .basicMarquee(iterations = Int.MAX_VALUE) // scrolling label when long
            )
        }
    }
}

/** DPAD_CENTER/ENTER trigger clicks; no ripple; behaves like a TV button */
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
                    onClick()
                    true
                } else false
            } else false
        }
        .clickable(
            interactionSource = interactions,
            indication = null,
            role = Role.Button,
            onClick = onClick
        )
}
