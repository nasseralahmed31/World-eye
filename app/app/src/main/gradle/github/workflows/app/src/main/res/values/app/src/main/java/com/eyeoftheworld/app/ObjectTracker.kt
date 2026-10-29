package com.eyeoftheworld.app

import android.graphics.RectF
import kotlin.math.max
import kotlin.math.min

data class Detection(
    val labelIndex: Int,
    val confidence: Float,
    val bounds: RectF
)

data class TrackedDetection(
    val id: Int,
    val labelIndex: Int,
    val confidence: Float,
    val bounds: RectF
)

class ObjectTracker(
    private val maxMissedFrames: Int = 5,
    private val minimumIou: Float = 0.25f
) {
    private data class Track(
        val id: Int,
        val labelIndex: Int,
        var confidence: Float,
        var bounds: RectF,
        var missedFrames: Int = 0
    )

    private val tracks = mutableListOf<Track>()
    private var nextId = 1

    @Synchronized
    fun update(detections: List<Detection>): List<TrackedDetection> {
        tracks.forEach { it.missedFrames++ }

        val candidates = mutableListOf<Triple<Float, Track, Detection>>()

        for (track in tracks) {
            for (detection in detections) {
                if (track.labelIndex != detection.labelIndex) continue

                val overlap = iou(track.bounds, detection.bounds)
                if (overlap >= minimumIou) {
                    candidates.add(Triple(overlap, track, detection))
                }
            }
        }

        candidates.sortByDescending { it.first }

        val matchedTracks = mutableSetOf<Int>()
        val matchedDetections = mutableSetOf<Int>()

        for ((_, track, detection) in candidates) {
            val detectionIndex = detections.indexOf(detection)

            if (track.id in matchedTracks ||
                detectionIndex in matchedDetections
            ) continue

            track.bounds = RectF(detection.bounds)
            track.confidence = detection.confidence
            track.missedFrames = 0

            matchedTracks.add(track.id)
            matchedDetections.add(detectionIndex)
        }

        detections.forEachIndexed { index, detection ->
            if (index !in matchedDetections) {
                tracks.add(
                    Track(
                        id = nextId++,
                        labelIndex = detection.labelIndex,
                        confidence = detection.confidence,
                        bounds = RectF(detection.bounds)
                    )
                )
            }
        }

        tracks.removeAll { it.missedFrames > maxMissedFrames }

        return tracks
            .filter { it.missedFrames == 0 }
            .map {
                TrackedDetection(
                    id = it.id,
                    labelIndex = it.labelIndex,
                    confidence = it.confidence,
                    bounds = RectF(it.bounds)
                )
            }
    }

    @Synchronized
    fun reset() {
        tracks.clear()
        nextId = 1
    }

    private fun iou(a: RectF, b: RectF): Float {
        val intersection = RectF(a)
        if (!intersection.intersect(b)) return 0f

        val intersectionArea =
            max(0f, intersection.width()) *
            max(0f, intersection.height())

        val areaA = max(0f, a.width()) * max(0f, a.height())
        val areaB = max(0f, b.width()) * max(0f, b.height())
        val union = areaA + areaB - intersectionArea

        return if (union > 0f) {
            min(1f, intersectionArea / union)
        } else {
            0f
        }
    }
}
