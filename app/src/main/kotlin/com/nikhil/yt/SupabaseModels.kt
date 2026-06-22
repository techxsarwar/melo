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
