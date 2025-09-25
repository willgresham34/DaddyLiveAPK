package com.personal.daddylive

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.ui.input.key.type
import androidx.tv.foundation.ExperimentalTvFoundationApi
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items
import com.personal.daddylive.model.VideoOption

class MainActivity : ComponentActivity() {

    private val options = listOf(
        VideoOption("ESPN", 44),
        VideoOption("ESPN 2", 45),
        VideoOption("ESPN 3", 46),
        VideoOption("SEC", 385),
        VideoOption("Fox Sports", 39),
        VideoOption("Fox Sports 2", 40)
    )

    @OptIn(ExperimentalTvFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Box(Modifier.fillMaxSize().padding(24.dp)) {
                    TvLazyVerticalGrid(
                        columns = TvGridCells.Fixed(3),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(options.size) { index ->
                            val opt = options[index]

                            var focused by remember { mutableStateOf(false) }
                            val focusRequester = remember { FocusRequester() }

                            // Auto-focus the very first item once
                            LaunchedEffect(Unit) {
                                if (index == 0) focusRequester.requestFocus()
                            }

                            Card(
                                elevation = CardDefaults.cardElevation(if (focused) 10.dp else 2.dp),
                                modifier = Modifier
                                    .size(width = 280.dp, height = 160.dp)
                                    .focusRequester(focusRequester)
                                    .onFocusChanged { focused = it.isFocused }
                                    .focusableForTv()
                                    .tvClickable {
                                        startActivity(
                                            Intent(this@MainActivity, PlayerActivity::class.java)
                                                .putExtra(PlayerActivity.EXTRA_TITLE, opt.title)
                                                .putExtra(PlayerActivity.EXTRA_STREAM_ID, opt.streamId)
                                        )
                                    }
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = opt.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Make a composable act like a TV button:
 * - DPAD_CENTER / ENTER triggers onClick (via onPreviewKeyEvent)
 * - Clickable without ripple, with Button role for accessibility.
 */
fun Modifier.tvClickable(onClick: () -> Unit) = composed {
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

/**
 * Helper to mark an element focusable for TV without adding extra behavior.
 * (Keeps it explicit and readable next to tvClickable/focusRequester.)
 */
@Composable
fun Modifier.focusableForTv(): Modifier = this.then(
    androidx.compose.ui.Modifier // placeholder to keep the name clear; focusability comes from Card + tvClickable
)
