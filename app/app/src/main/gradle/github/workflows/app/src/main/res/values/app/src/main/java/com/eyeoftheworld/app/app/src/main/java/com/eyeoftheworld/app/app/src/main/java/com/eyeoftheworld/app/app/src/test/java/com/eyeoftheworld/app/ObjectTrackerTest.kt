
package com.eyeoftheworld.app

import android.graphics.RectF
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObjectTrackerTest {

    @Test
    fun sameObjectKeepsIdWhenItMovesSlightly() {
        val tracker = ObjectTracker()

        val first = tracker.update(
            listOf(
                Detection(
                    labelIndex = 7,
                    confidence = 0.9f,
                    bounds = RectF(10f, 10f, 100f, 100f)
                )
            )
        )

        val second = tracker.update(
            listOf(
                Detection(
                    labelIndex = 7,
                    confidence = 0.92f,
                    bounds = RectF(12f, 11f, 102f, 101f)
                )
            )
        )

        assertEquals(first.first().id, second.first().id)
    }

    @Test
    fun resetClearsTracks() {
        val tracker = ObjectTracker()

        tracker.update(
            listOf(
                Detection(
                    labelIndex = 7,
                    confidence = 0.9f,
                    bounds = RectF(10f, 10f, 100f, 100f)
                )
            )
        )

        tracker.reset()

        val result = tracker.update(emptyList())
        assertTrue(result.isEmpty())
    }
}
