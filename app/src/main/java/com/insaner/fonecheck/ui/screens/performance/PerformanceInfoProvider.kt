package com.insaner.fonecheck.ui.screens.performance

import android.app.ActivityManager
import android.content.Context
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.CpuCoreFrequency
import com.insaner.fonecheck.domain.model.PerformanceInfo
import com.insaner.fonecheck.runtime.EpochMillisClock
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.time.Instant
import javax.inject.Inject

fun interface PerformanceInfoProvider {
    fun capture(): PerformanceInfo
}

class AndroidPerformanceInfoProvider
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val clock: EpochMillisClock,
    ) : PerformanceInfoProvider {
        override fun capture(): PerformanceInfo {
            val cpuFrequencies = readCpuFrequencies()
            val memory = readMemoryInfo()
            val glInfo = readGlInfo(AndroidGlInfoSession::open)
            return PerformanceInfo(
                cpuModel = readCpuModel(),
                cpuArchitecture = normalizePerformanceText(System.getProperty("os.arch")),
                cpuCores = Runtime.getRuntime().availableProcessors(),
                cpuFrequencies = cpuFrequencies,
                cpuConfidence = PerformanceInfoConfidence.cpu(cpuFrequencies),
                totalRamBytes = memory?.totalMem,
                availableRamBytes = memory?.availMem,
                ramConfidence = if (memory == null) Confidence.LOW else Confidence.HIGH,
                glEsVersion = glInfo.version,
                glRenderer = glInfo.renderer,
                glVendor = glInfo.vendor,
                vulkanFeatureDeclared = hasVulkanFeature(),
                gpuConfidence =
                    if (glInfo.renderer == PerformanceInfo.UNAVAILABLE) {
                        Confidence.LOW
                    } else {
                        Confidence.HIGH
                    },
                capturedAt = Instant.ofEpochMilli(clock.currentTimeMillis()),
            )
        }

        private fun readCpuModel(): String =
            try {
                File("/proc/cpuinfo").useLines { lines ->
                    normalizePerformanceText(
                        lines
                            .firstOrNull { it.startsWith("Hardware") || it.startsWith("model name") }
                            ?.substringAfter(":")
                            ?.trim(),
                    )
                }
            } catch (_: Exception) {
                PerformanceInfo.UNAVAILABLE
            }

        private fun readCpuFrequencies(): List<CpuCoreFrequency> =
            (0 until Runtime.getRuntime().availableProcessors()).map { index ->
                val basePath = "/sys/devices/system/cpu/cpu$index/cpufreq"
                CpuCoreFrequency(
                    coreIndex = index,
                    currentMhz = readFrequency("$basePath/scaling_cur_freq"),
                    minMhz = readFrequency("$basePath/cpuinfo_min_freq"),
                    maxMhz = readFrequency("$basePath/cpuinfo_max_freq"),
                )
            }

        private fun readFrequency(path: String): Long? =
            try {
                File(path).readText().trim().toLongOrNull()?.takeIf { it > 0L }?.div(1_000L)
            } catch (_: Exception) {
                null
            }

        private fun readMemoryInfo(): ActivityManager.MemoryInfo? =
            try {
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                ActivityManager.MemoryInfo().also(activityManager::getMemoryInfo)
            } catch (_: Exception) {
                null
            }

        private fun hasVulkanFeature(): Boolean =
            context.packageManager.systemAvailableFeatures.any { it.name == VULKAN_LEVEL_FEATURE }

        private companion object {
            const val VULKAN_LEVEL_FEATURE = "android.hardware.vulkan.level"
        }
    }

private class AndroidGlInfoSession : GlInfoSession {
    private var display: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var surface: EGLSurface = EGL14.EGL_NO_SURFACE
    private var context: EGLContext = EGL14.EGL_NO_CONTEXT

    override fun version(): String = GLES20.glGetString(GLES20.GL_VERSION) ?: PerformanceInfo.UNAVAILABLE

    override fun renderer(): String = GLES20.glGetString(GLES20.GL_RENDERER) ?: PerformanceInfo.UNAVAILABLE

    override fun vendor(): String = GLES20.glGetString(GLES20.GL_VENDOR) ?: PerformanceInfo.UNAVAILABLE

    override fun close() {
        if (display == EGL14.EGL_NO_DISPLAY) return
        EGL14.eglMakeCurrent(
            display,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_CONTEXT,
        )
        if (context != EGL14.EGL_NO_CONTEXT) EGL14.eglDestroyContext(display, context)
        if (surface != EGL14.EGL_NO_SURFACE) EGL14.eglDestroySurface(display, surface)
        EGL14.eglTerminate(display)
        display = EGL14.EGL_NO_DISPLAY
        surface = EGL14.EGL_NO_SURFACE
        context = EGL14.EGL_NO_CONTEXT
    }

    private fun initialize() {
        display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        check(display != EGL14.EGL_NO_DISPLAY)
        check(EGL14.eglInitialize(display, IntArray(2), 0, IntArray(2), 1))

        val configs = arrayOfNulls<EGLConfig>(1)
        val configCount = IntArray(1)
        check(
            EGL14.eglChooseConfig(
                display,
                intArrayOf(
                    EGL14.EGL_RENDERABLE_TYPE,
                    EGL14.EGL_OPENGL_ES2_BIT,
                    EGL14.EGL_SURFACE_TYPE,
                    EGL14.EGL_PBUFFER_BIT,
                    EGL14.EGL_NONE,
                ),
                0,
                configs,
                0,
                1,
                configCount,
                0,
            ) && configCount[0] > 0,
        )
        val config = checkNotNull(configs[0])
        surface =
            EGL14.eglCreatePbufferSurface(
                display,
                config,
                intArrayOf(EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE),
                0,
            )
        check(surface != EGL14.EGL_NO_SURFACE)
        context =
            EGL14.eglCreateContext(
                display,
                config,
                EGL14.EGL_NO_CONTEXT,
                intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE),
                0,
            )
        check(context != EGL14.EGL_NO_CONTEXT)
        check(EGL14.eglMakeCurrent(display, surface, surface, context))
    }

    companion object {
        fun open(): AndroidGlInfoSession =
            AndroidGlInfoSession().also { session ->
                try {
                    session.initialize()
                } catch (error: Exception) {
                    session.close()
                    throw error
                }
            }
    }
}
