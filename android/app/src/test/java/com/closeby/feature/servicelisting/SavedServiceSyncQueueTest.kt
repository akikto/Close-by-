package com.closeby.feature.servicelisting

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.closeby.feature.servicelisting.data.local.SavedServiceSyncQueue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SavedServiceSyncQueueTest {

    private lateinit var queue: SavedServiceSyncQueue

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        queue = SavedServiceSyncQueue(context)
        queue.clear()
    }

    @Test
    fun enqueueSaveAndDrain() {
        queue.enqueueSave("svc-1")
        val pending = queue.drain()
        assertEquals(1, pending.size)
        assertTrue(pending[0] is SavedServiceSyncQueue.PendingMutation.Save)
        assertEquals("svc-1", pending[0].serviceId)
    }

    @Test
    fun unsaveOverridesPendingSave() {
        queue.enqueueSave("svc-1")
        queue.enqueueUnsave("svc-1")
        val pending = queue.drain()
        assertEquals(1, pending.size)
        assertTrue(pending[0] is SavedServiceSyncQueue.PendingMutation.Unsave)
    }
}
