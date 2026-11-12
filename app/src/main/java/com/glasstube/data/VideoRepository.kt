package com.glasstube.data

import android.content.Context
import android.net.Uri
import com.glasstube.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class Video(
    val id: String,
    val title: String,
    val channel: String,
    val description: String,
    val thumbnailUrl: String,
    val publishedAt: String = ""
)

object YouTubeRepository {
    private const val BASE_URL = "https://www.googleapis.com/youtube/v3/"

    suspend fun popular(): List<Video> {
        return request(
            endpoint = "videos",
            parameters = mapOf(
                "part" to "snippet",
                "chart" to "mostPopular",
                "regionCode" to "IN",
                "maxResults" to "24"
            ),
            idIsObject = false
        )
    }

    suspend fun search(query: String): List<Video> {
        require(query.isNotBlank()) { "Enter something to search for." }
        return request(
            endpoint = "search",
            parameters = mapOf(
                "part" to "snippet",
                "type" to "video",
                "safeSearch" to "strict",
                "maxResults" to "20",
                "q" to query.trim()
            ),
            idIsObject = true
        )
    }

    private suspend fun request(
        endpoint: String,
        parameters: Map<String, String>,
        idIsObject: Boolean
    ): List<Video> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.YT_API_KEY
        if (apiKey.isBlank()) {
            throw IOException(
                "GlassTube needs a YouTube Data API key. Add YT_API_KEY to local.properties, " +
                    "or add it as a GitHub Actions repository secret for APK builds."
            )
        }

        val builder = Uri.parse(BASE_URL + endpoint).buildUpon()
        parameters.forEach { (key, value) -> builder.appendQueryParameter(key, value) }
        builder.appendQueryParameter("key", apiKey)

        val connection = (URL(builder.build().toString()).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 20_000
            setRequestProperty("Accept", "application/json")
        }

        try {
            val status = connection.responseCode
            val responseStream = if (status in 200..299) connection.inputStream else connection.errorStream
            val responseText = responseStream?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (status !in 200..299) {
                val apiMessage = runCatching {
                    JSONObject(responseText).optJSONObject("error")?.optString("message")
                }.getOrNull()
                throw IOException(
                    apiMessage?.takeIf { it.isNotBlank() && it != "null" }
                        ?: "YouTube request failed (HTTP $status)."
                )
            }

            parseVideos(JSONObject(responseText), idIsObject)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseVideos(response: JSONObject, idIsObject: Boolean): List<Video> {
        val items = response.optJSONArray("items") ?: JSONArray()
        return buildList {
            for (index in 0 until items.length()) {
                val item = items.optJSONObject(index) ?: continue
                val idValue = item.opt("id")
                val videoId = when {
                    idIsObject && idValue is JSONObject -> idValue.optString("videoId")
                    idValue is String -> idValue
                    else -> ""
                }
                if (videoId.isBlank() || videoId == "null") continue

                val snippet = item.optJSONObject("snippet") ?: continue
                val thumbnails = snippet.optJSONObject("thumbnails")
                val thumbnail = thumbnails?.optJSONObject("high")
                    ?: thumbnails?.optJSONObject("medium")
                    ?: thumbnails?.optJSONObject("default")

                add(
                    Video(
                        id = videoId,
                        title = snippet.optString("title", "Untitled video"),
                        channel = snippet.optString("channelTitle", "YouTube creator"),
                        description = snippet.optString("description"),
                        thumbnailUrl = thumbnail?.optString("url").orEmpty(),
                        publishedAt = snippet.optString("publishedAt")
                    )
                )
            }
        }
    }
}

class SavedVideosStore(context: Context) {
    private val preferences = context.getSharedPreferences("glasstube_library", Context.MODE_PRIVATE)

    fun load(): List<Video> {
        val json = preferences.getString("saved_videos", "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(json)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val id = item.optString("id")
                    if (id.isBlank()) continue
                    add(
                        Video(
                            id = id,
                            title = item.optString("title"),
                            channel = item.optString("channel"),
                            description = item.optString("description"),
                            thumbnailUrl = item.optString("thumbnailUrl"),
                            publishedAt = item.optString("publishedAt")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun save(videos: List<Video>) {
        val array = JSONArray()
        videos.forEach { video ->
            array.put(
                JSONObject()
                    .put("id", video.id)
                    .put("title", video.title)
                    .put("channel", video.channel)
                    .put("description", video.description)
                    .put("thumbnailUrl", video.thumbnailUrl)
                    .put("publishedAt", video.publishedAt)
            )
        }
        preferences.edit().putString("saved_videos", array.toString()).apply()
    }
}
