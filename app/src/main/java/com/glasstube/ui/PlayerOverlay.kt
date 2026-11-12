package com.glasstube.ui

import android.graphics.Color as AndroidColor
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.glasstube.data.Video

@Composable
fun PlayerOverlay(
    video: Video,
    isSaved: Boolean,
    onToggleSaved: () -> Unit,
    onClose: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val webViewRef = remember(video.id) { mutableStateOf<WebView?>(null) }

    DisposableEffect(video.id) {
        onDispose {
            webViewRef.value?.stopLoading()
            webViewRef.value?.loadUrl("about:blank")
            webViewRef.value?.destroy()
            webViewRef.value = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (dark) Color(0xFF061114) else Color(0xFFF0F6F4))
    ) {
        AmbientBackdrop()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp, bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GlassIconButton(
                    icon = Icons.Rounded.Close,
                    contentDescription = "Close player",
                    onClick = onClose
                )
                GlassSurface(shape = RoundedCornerShape(30.dp)) {
                    Text(
                        text = "PLAYBACK BY YOUTUBE",
                        color = if (dark) Color.White.copy(alpha = 0.86f) else Color(0xFF23494A),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)
                    )
                }
                GlassIconButton(
                    icon = if (isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    contentDescription = if (isSaved) "Remove from library" else "Save to library",
                    onClick = onToggleSaved
                )
            }

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(220.dp)
                    .clip(RoundedCornerShape(24.dp)),
                factory = { context ->
                    WebView(context).apply {
                        setBackgroundColor(AndroidColor.BLACK)
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = true
                        settings.loadsImagesAutomatically = true
                        webChromeClient = WebChromeClient()
                        webViewClient = WebViewClient()

                        val html = """
                            <!doctype html>
                            <html>
                              <head>
                                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
                                <style>
                                  html, body { margin: 0; padding: 0; width: 100%; height: 100%; background: #000; overflow: hidden; }
                                  iframe { width: 100%; height: 100%; border: 0; }
                                </style>
                              </head>
                              <body>
                                <iframe
                                  src="https://www.youtube.com/embed/${video.id}?playsinline=1&rel=0"
                                  title="YouTube video player"
                                  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                                  referrerpolicy="strict-origin-when-cross-origin"
                                  allowfullscreen>
                                </iframe>
                              </body>
                            </html>
                        """.trimIndent()
                        loadDataWithBaseURL("https://com.glasstube", html, "text/html", "UTF-8", null)
                        webViewRef.value = this
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    video.title,
                    color = if (dark) Color.White else Color(0xFF102528),
                    fontSize = 21.sp,
                    lineHeight = 27.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    video.channel,
                    color = if (dark) Color(0xFF9FCBC5) else Color(0xFF4D7774),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                GlassSurface {
                    Text(
                        video.description.ifBlank { "This video is hosted and played by YouTube." },
                        color = if (dark) Color.White.copy(alpha = 0.76f) else Color(0xFF4F6868),
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(17.dp)
                    )
                }
                Text(
                    "Playback uses YouTube’s official embedded player. YouTube-served ads and player controls may appear.",
                    color = if (dark) Color.White.copy(alpha = 0.46f) else Color(0xFF718887),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
