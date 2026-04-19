package com.fibelatti.photowidget.model

data class GifFrames(
    val frames: List<LocalPhoto>,
    val interval: Long,
) {

    companion object {

        val EMPTY = GifFrames(frames = emptyList(), interval = 0)

        const val MIN_INTERVAL_MS: Long = 20
        const val MAX_INTERVAL_MS: Long = 200
    }
}
