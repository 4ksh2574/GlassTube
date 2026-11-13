package com.glasstube.ui

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.border
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.glasstube.BuildConfig
import com.glasstube.data.SavedVideosStore
import com.glasstube.data.Video
import com.glasstube.data.YouTubeRepository
import kotlinx.coroutines.launch
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

private enum class AppPage(val label: String) {
    Home("Home"),
    Search("Search"),
    Library("Library")
}

@Composable
fun GlassTubeApp() {
    val context = LocalContext.current
    val store = remember(context) { SavedVideosStore(context.applicationContext) }
    val scope = rememberCoroutineScope()
    val hazeState = rememberHazeState()

    var page by remember { mutableStateOf(AppPage.Home) }
    var videos by remember { mutableStateOf(emptyList<Video>()) }
    var searchResults by remember { mutableStateOf(emptyList<Video>()) }
    var savedVideos by remember { mutableStateOf(store.load()) }
    var selectedVideo by remember { mutableStateOf<Video?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun runRequest(request: suspend () -> List<Video>, isSearch: Boolean = false) {
        scope.launch {
            loading = true
            errorMessage = null
            try {
                val result = request()
                if (isSearch) searchResults = result else videos = result
            } catch (error: Exception) {
                errorMessage = error.message ?: "Something went wrong while loading videos."
                if (isSearch) searchResults = emptyList() else videos = emptyList()
            } finally {
                loading = false
            }
        }
    }

    fun submitSearch(term: String) {
        searchQuery = term
        page = AppPage.Search
        if (term.isNotBlank()) {
            runRequest({ YouTubeRepository.search(term) }, isSearch = true)
        }
    }

    fun toggleSaved(video: Video) {
        val existing = savedVideos.any { it.id == video.id }
        savedVideos = if (existing) {
            savedVideos.filterNot { it.id == video.id }
        } else {
            listOf(video) + savedVideos
        }
        store.save(savedVideos)
    }

    LaunchedEffect(Unit) {
        if (BuildConfig.YT_API_KEY.isBlank()) {
            errorMessage = "Add a YouTube Data API key to enable video discovery. The app can still be built and installed without it."
        } else {
            runRequest({ YouTubeRepository.popular() })
        }
    }

    MaterialTheme {
        CompositionLocalProvider(LocalGlassHazeState provides hazeState) {
            Box(modifier = Modifier.fillMaxSize()) {
            AmbientBackdrop(Modifier.fillMaxSize().hazeSource(hazeState))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                    .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
            ) {
                AppTopBar(onSearch = { page = AppPage.Search })

                when (page) {
                    AppPage.Home -> HomePage(
                        videos = videos,
                        loading = loading,
                        errorMessage = errorMessage,
                        isSaved = { video -> savedVideos.any { it.id == video.id } },
                        onVideoClick = { selectedVideo = it },
                        onToggleSaved = ::toggleSaved,
                        onRefresh = {
                            errorMessage = null
                            if (BuildConfig.YT_API_KEY.isNotBlank()) {
                                runRequest({ YouTubeRepository.popular() })
                            } else {
                                errorMessage = "Add YT_API_KEY to enable discovery. See the GlassTube README."
                            }
                        },
                        onSearch = ::submitSearch
                    )

                    AppPage.Search -> SearchPage(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        results = searchResults,
                        loading = loading,
                        errorMessage = errorMessage,
                        isSaved = { video -> savedVideos.any { it.id == video.id } },
                        onVideoClick = { selectedVideo = it },
                        onToggleSaved = ::toggleSaved,
                        onSearch = ::submitSearch
                    )

                    AppPage.Library -> LibraryPage(
                        videos = savedVideos,
                        onVideoClick = { selectedVideo = it },
                        onRemove = ::toggleSaved
                    )
                }
            }

            GlassDock(
                selected = page,
                onSelect = { page = it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = 30.dp,
                        end = 30.dp,
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 14.dp
                    )
            )

            selectedVideo?.let { video ->
                PlayerOverlay(
                    video = video,
                    isSaved = savedVideos.any { it.id == video.id },
                    onToggleSaved = { toggleSaved(video) },
                    onClose = { selectedVideo = null }
                )
            }
            }
        }
    }
}

@Composable
private fun AppTopBar(onSearch: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassSurface(modifier = Modifier.size(46.dp), shape = CircleShape) {
                Icon(
                    Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFF63E2D1),
                    modifier = Modifier.align(Alignment.Center).size(27.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    "GlassTube",
                    color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color.White else Color(0xFF102528),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "A calmer way to watch",
                    color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color.White.copy(alpha = 0.52f) else Color(0xFF66817F),
                    fontSize = 11.sp
                )
            }
        }
        GlassIconButton(
            icon = Icons.Rounded.Search,
            contentDescription = "Search videos",
            onClick = onSearch
        )
    }
}

@Composable
private fun HomePage(
    videos: List<Video>,
    loading: Boolean,
    errorMessage: String?,
    isSaved: (Video) -> Boolean,
    onVideoClick: (Video) -> Unit,
    onToggleSaved: (Video) -> Unit,
    onRefresh: () -> Unit,
    onSearch: (String) -> Unit
) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    val topics = remember { listOf("All", "Apple", "Technology", "Gaming", "Music", "Podcasts") }
    var activeTopic by remember { mutableStateOf("All") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            "YOUR WORLD",
                            color = if (dark) Color.White.copy(alpha = 0.56f) else Color(0xFF66817F),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.7.sp
                        )
                        Text(
                            "For you",
                            color = if (dark) Color.White else Color(0xFF102528),
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.7).sp
                        )
                    }
                    Box(Modifier.weight(1f))
                    GlassIconButton(
                        icon = Icons.Rounded.Refresh,
                        contentDescription = "Refresh videos",
                        onClick = onRefresh,
                        size = 38.dp
                    )
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(topics, key = { it }) { topic ->
                        GlassChip(
                            label = topic,
                            selected = activeTopic == topic,
                            onClick = {
                                activeTopic = topic
                                if (topic == "All") onRefresh() else onSearch(topic.lowercase())
                            }
                        )
                    }
                }
            }
        }

        if (errorMessage != null && videos.isEmpty()) {
            item {
                GlassNotice(
                    title = if (BuildConfig.YT_API_KEY.isBlank()) "Connect video discovery" else "Could not load videos",
                    body = errorMessage
                )
            }
        }

        if (loading && videos.isEmpty()) {
            item { LoadingCard() }
        }

        if (videos.isEmpty() && !loading && errorMessage == null) {
            item {
                GlassNotice(
                    title = "Your feed is ready",
                    body = "Choose a topic above or search for something to start exploring."
                )
            }
        }

        if (videos.isNotEmpty()) {
            item {
                Text(
                    "Popular in India",
                    color = if (dark) Color.White else Color(0xFF102528),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 1.dp, bottom = 1.dp)
                )
            }
            items(videos, key = { it.id }) { video ->
                VideoCard(
                    video = video,
                    isSaved = isSaved(video),
                    onClick = { onVideoClick(video) },
                    onToggleSaved = { onToggleSaved(video) }
                )
            }
        }
    }
}

@Composable
private fun SearchPage(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<Video>,
    loading: Boolean,
    errorMessage: String?,
    isSaved: (Video) -> Boolean,
    onVideoClick: (Video) -> Unit,
    onToggleSaved: (Video) -> Unit,
    onSearch: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            "Search",
            color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color.White else Color(0xFF102528),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 14.dp, bottom = 14.dp)
        )
        GlassSearchField(
            value = query,
            onValueChange = onQueryChange,
            onSearch = { onSearch(query) },
            placeholder = "What would you like to watch?"
        )
        if (query.isBlank() && results.isEmpty()) {
            Text(
                "EXPLORE A TOPIC",
                color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color.White.copy(alpha = 0.56f) else Color(0xFF66817F),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
                modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassChip("Music", false, { onSearch("music") })
                GlassChip("Technology", false, { onSearch("technology") })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                GlassChip("Chess", false, { onSearch("chess") })
                GlassChip("Science", false, { onSearch("science") })
            }
        }
        if (errorMessage != null) {
            GlassNotice("Search unavailable", errorMessage, Modifier.padding(top = 16.dp))
        }
        if (loading) {
            LoadingCard(Modifier.padding(top = 18.dp))
        }
        if (!loading && query.isNotBlank() && results.isEmpty() && errorMessage == null) {
            GlassNotice(
                title = "No results yet",
                body = "Try a shorter phrase or a different topic.",
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 16.dp, bottom = 118.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(results, key = { it.id }) { video ->
                VideoCard(
                    video = video,
                    isSaved = isSaved(video),
                    onClick = { onVideoClick(video) },
                    onToggleSaved = { onToggleSaved(video) }
                )
            }
        }
    }
}

@Composable
private fun LibraryPage(
    videos: List<Video>,
    onVideoClick: (Video) -> Unit,
    onRemove: (Video) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 118.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "Your library",
                color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color.White else Color(0xFF102528),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Saved on this device",
                color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color.White.copy(alpha = 0.54f) else Color(0xFF66817F),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 5.dp, bottom = 12.dp)
            )
        }
        if (videos.isEmpty()) {
            item {
                GlassNotice(
                    title = "Nothing saved yet",
                    body = "Tap the bookmark on a video to keep it here for later."
                )
            }
        }
        items(videos, key = { it.id }) { video ->
            VideoCard(
                video = video,
                isSaved = true,
                onClick = { onVideoClick(video) },
                onToggleSaved = { onRemove(video) }
            )
        }
    }
}

@Composable
private fun VideoCard(
    video: Video,
    isSaved: Boolean,
    onClick: () -> Unit,
    onToggleSaved: () -> Unit
) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    val hazeState = LocalGlassHazeState.current
    val interaction = remember { MutableInteractionSource() }
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(0.95f)
                    .aspectRatio(1.42f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (dark) Color(0xFF203C42) else Color(0xFFD6E8E4))
            ) {
                if (video.thumbnailUrl.isNotBlank()) {
                    AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().hazeSource(hazeState)
                    )
                }
                GlassSurface(
                    modifier = Modifier.align(Alignment.Center).size(34.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = "Play video",
                        tint = Color(0xFF3CC7B5),
                        modifier = Modifier.align(Alignment.Center).size(22.dp)
                    )
                }
                GlassSurface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(5.dp).size(30.dp),
                    shape = CircleShape
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onToggleSaved
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                            contentDescription = if (isSaved) "Remove from library" else "Save to library",
                            tint = if (isSaved) Color(0xFF63E2D1) else if (dark) Color.White else Color(0xFF173A3D),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .weight(1.25f)
                    .padding(start = 12.dp, end = 5.dp, top = 4.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    video.title,
                    color = if (dark) Color.White else Color(0xFF102528),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    video.channel,
                    color = if (dark) Color.White.copy(alpha = 0.56f) else Color(0xFF66817F),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun LoadingCard(modifier: Modifier = Modifier) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    GlassSurface(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color(0xFF50CDBD),
                strokeWidth = 2.dp
            )
            Text(
                "Finding something worth watching…",
                color = if (dark) Color.White.copy(alpha = 0.78f) else Color(0xFF456967),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun GlassDock(
    selected: AppPage,
    onSelect: (AppPage) -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    GlassSurface(
        modifier = modifier.width(258.dp).height(62.dp),
        shape = RoundedCornerShape(40.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(6.dp)
        ) {
            val itemWidth = maxWidth / 3
            val activeOffset by animateDpAsState(
                targetValue = itemWidth * selected.ordinal.toFloat(),
                animationSpec = spring(dampingRatio = 0.72f, stiffness = 390f),
                label = "glassDockLens"
            )
            Box(
                modifier = Modifier
                    .offset(x = activeOffset)
                    .width(itemWidth)
                    .height(50.dp)
                    .padding(horizontal = 2.dp, vertical = 1.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = if (dark) 0.28f else 0.82f),
                                Color.White.copy(alpha = if (dark) 0.08f else 0.42f)
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = if (dark) 0.34f else 0.84f), CircleShape)
            )
            Row(modifier = Modifier.fillMaxSize()) {
                DockItem(AppPage.Home, Icons.Rounded.Home, selected == AppPage.Home, onSelect, Modifier.weight(1f))
                DockItem(AppPage.Search, Icons.Rounded.Search, selected == AppPage.Search, onSelect, Modifier.weight(1f))
                DockItem(AppPage.Library, Icons.Rounded.VideoLibrary, selected == AppPage.Library, onSelect, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DockItem(
    page: AppPage,
    icon: ImageVector,
    selected: Boolean,
    onSelect: (AppPage) -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(CircleShape)
            .clickable(interactionSource = interaction, indication = null) { onSelect(page) },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = page.label,
            tint = if (selected) {
                if (dark) Color.White else Color(0xFF176E68)
            } else if (dark) {
                Color.White.copy(alpha = 0.68f)
            } else {
                Color(0xFF466766)
            },
            modifier = Modifier.size(22.dp)
        )
    }
}
