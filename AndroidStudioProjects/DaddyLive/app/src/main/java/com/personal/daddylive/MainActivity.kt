package com.personal.daddylive

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.personal.daddylive.model.VideoOption
import androidx.compose.foundation.clickable
import androidx.compose.ui.composed

class MainActivity : ComponentActivity() {

    private val options = listOf(
        VideoOption("ESPN", 44),
        VideoOption("ESPN 2", 45),
        VideoOption("ESPN 3", 46),
        VideoOption("SEC" ,385),
        VideoOption("Fox Sports" ,39),
        VideoOption("Fox Sports 2" ,40)
    )

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Box(Modifier.fillMaxSize().padding(24.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(options) { opt ->
                            var focused by remember { mutableStateOf(false) }
                            Card(
                                elevation = CardDefaults.cardElevation(if (focused) 10.dp else 2.dp),
                                modifier = Modifier
                                    .size(width = 280.dp, height = 160.dp)
                                    .onFocusChanged { focused = it.isFocused }
                                    .focusable()
                                    .onKeyEvent { false } // keep D-pad default nav
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
 * Simple helper to make a Composable act like a TV-clickable.
 * On Fire TV, the center D-pad press triggers "click" on focused items.
 */
fun Modifier.tvClickable(onClick: () -> Unit) = composed {
    this.clickable(onClick = onClick)
}
