package com.insaner.fonecheck.ui.screens.camera

import android.graphics.Bitmap
import androidx.camera.core.ImageInfo
import androidx.camera.core.ImageProxy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.insaner.fonecheck.runtime.EpochMillisClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.lang.reflect.Proxy

@RunWith(AndroidJUnit4::class)
class CameraCapturePreviewTest {
    @Test
    fun captureCallbackStoresFullResolutionDimensionsInDisplayOrientation() =
        runBlocking {
            val state = MutableStateFlow(CameraTestState())
            val session = CameraCaptureSession(state, EpochMillisClock { 10L }, this)
            val cases =
                listOf(
                    0 to (3200 to 2400),
                    90 to (2400 to 3200),
                    180 to (3200 to 2400),
                    270 to (2400 to 3200),
                )
            for ((rotation, dimensions) in cases) {
                val info =
                    Proxy.newProxyInstance(
                        ImageInfo::class.java.classLoader,
                        arrayOf(ImageInfo::class.java),
                    ) { _, method, _ ->
                        when (method.name) {
                            "getRotationDegrees" -> rotation
                            else -> error("Unexpected image info access: ${method.name}")
                        }
                    } as ImageInfo
                var closed = false
                val image =
                    Proxy.newProxyInstance(
                        ImageProxy::class.java.classLoader,
                        arrayOf(ImageProxy::class.java),
                    ) { _, method, _ ->
                        when (method.name) {
                            "getWidth" -> 3200
                            "getHeight" -> 2400
                            "getImageInfo" -> info
                            "close" -> {
                                closed = true
                                null
                            }
                            else -> error("Unexpected image access: ${method.name}")
                        }
                    } as ImageProxy
                val attempt = requireNotNull(session.begin("rear", 1L))
                session.callback(attempt).onCaptureSuccess(image)
                assertEquals(dimensions.first, state.value.lastCapture?.width)
                assertEquals(dimensions.second, state.value.lastCapture?.height)
                assertTrue(closed)
            }
        }

    @Test
    fun largeCaptureIsDownsampledAndRotatedWithoutChangingItsAspectRatio() {
        val bitmap = Bitmap.createBitmap(3200, 2400, Bitmap.Config.ARGB_8888)
        val bytes =
            ByteArrayOutputStream().use { stream ->
                assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream))
                stream.toByteArray()
            }
        bitmap.recycle()

        val landscape = decodeCapturePreview(bytes, 0)
        assertEquals(1600, landscape.width)
        assertEquals(1200, landscape.height)
        val portrait = decodeCapturePreview(bytes, 90)
        assertEquals(1200, portrait.width)
        assertEquals(1600, portrait.height)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidCaptureCannotBecomeAPreview() {
        decodeCapturePreview(byteArrayOf(1, 2, 3), 0)
    }

    @Test
    fun retakeFailureAndCancellationReleasePreviewAndRejectLateImages() =
        runBlocking {
            val state = MutableStateFlow(CameraTestState())
            val session = CameraCaptureSession(state, EpochMillisClock { 10L }, this)
            val preview: ImageBitmap = Bitmap.createBitmap(16, 12, Bitmap.Config.ARGB_8888).asImageBitmap()
            val first = requireNotNull(session.begin("rear", null))
            assertTrue(session.succeed(first, 3200, 2400, preview))
            assertSame(preview, state.value.capturePreview)
            assertEquals(3200, state.value.lastCapture?.width)

            val retake = requireNotNull(session.begin("rear", null))
            assertNull(state.value.capturePreview)
            assertTrue(session.fail(retake, "camera_capture_error"))
            assertNull(state.value.capturePreview)
            assertFalse(session.succeed(retake, 3200, 2400, preview))
            assertNull(state.value.capturePreview)

            val finalAttempt = requireNotNull(session.begin("rear", null))
            assertTrue(session.succeed(finalAttempt, 3200, 2400, preview))
            session.cancel()
            assertNull(state.value.capturePreview)
            assertFalse(session.succeed(finalAttempt, 3200, 2400, preview))
            assertNull(state.value.capturePreview)
        }
}
