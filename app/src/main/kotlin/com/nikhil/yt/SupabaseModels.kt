package com.nikhil.yt

import kotlinx.serialization.Serializable

@Serializable
data class AppUpdate(
    val id: Int = 1,
    val version_code: Int,
    val version_name: String,
    val download_url: String,
    val changelog: String = "",
    val force_update: Boolean = false
)

@Serializable
data class ActiveSession(
    val device_id: String,
    val last_seen_at: String
)

@Serializable
data class BroadcastNotification(
    val id: Int? = null,
    val title: String,
    val message: String,
    val url: String? = null,
    val created_at: String? = null
)

