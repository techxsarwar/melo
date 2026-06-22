/*
 * Melo - by ParallelogramFoundation
 * Sarwar Altaf Dar
 * Licensed Under GPL-3.0
 */



package com.nikhil.yt.db.entities

sealed class LocalItem {
    abstract val id: String
    abstract val title: String
    abstract val thumbnailUrl: String?
}
