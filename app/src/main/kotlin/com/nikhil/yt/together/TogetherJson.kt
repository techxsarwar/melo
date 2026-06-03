/*
 * Melo - by Sarwar Altaf Dar
 * Sarwar Altaf Dar
 * Licensed Under GPL-3.0
 */

package com.nikhil.yt.together

import kotlinx.serialization.json.Json

object TogetherJson {
    val json: Json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            encodeDefaults = true
            classDiscriminator = "type"
        }
}
