package com.insaner.phonecheck.ui.screens.performance

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.pm.FeatureInfo
import android.opengl.EGL14
import android.opengl.GLES20
import android.text.format.Formatter
import androidx.lifecycle.AndroidViewModel
import com.insaner.phonecheck.domain.model.Confidence
import com.insaner.phonecheck.domain.model.CpuCoreFrequency
import com.insaner.phonecheck.domain.model.PerformanceInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PerformanceInfoViewModel @Inject constructor(
    application: Application,
) : AndroidViewModel(application) {

    val performanceInfo: PerformanceInfo = gatherPerformanceInfo(application)

    private fun gatherPerformanceInfo(application: Application): PerformanceInfo {
        val cpuFrequencies = readCpuFrequencies()
        val cpuConfidence = if (cpuFrequencies.isNotEmpty()) Confidence.HIGH else Confidence.LOW

        val activityManager = application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val glInfo = readGlInfo()
        val vulkanSupported = checkVulkanSupport(application)

        return PerformanceInfo(
            cpuModel = readCpuModel(),
            cpuArchitecture = System.getProperty("os.arch") ?: "Unknown",
            cpuCores = Runtime.getRuntime().availableProcessors(),
            cpuFrequencies = cpuFrequencies,
            cpuConfidence = cpuConfidence,
            totalRam = Formatter.formatFileSize(application, memInfo.totalMem),
            availableRam = Formatter.formatFileSize(application, memInfo.availMem),
            ramConfidence = Confidence.HIGH,
            glEsVersion = glInfo.version,
            glRenderer = glInfo.renderer,
            glVendor = glInfo.vendor,
            vulkanSupported = vulkanSupported,
            gpuConfidence = if (glInfo.renderer != "Unknown") Confidence.HIGH else Confidence.LOW,
        )
    }

    private fun readCpuModel(): String {
        return try {
            File("/proc/cpuinfo").useLines { lines ->
                lines.firstOrNull { it.startsWith("Hardware") || it.startsWith("model name") }
                    ?.substringAfter(":")
                    ?.trim()
                    ?: "Unknown"
            }
        } catch (_: Exception) {
            "Unknown"
        }
    }

    private fun readCpuFrequencies(): List<CpuCoreFrequency> {
        val cores = Runtime.getRuntime().availableProcessors()
        return (0 until cores).mapNotNull { i ->
            try {
                val basePath = "/sys/devices/system/cpu/cpu$i/cpufreq"
                val curFreq = readFreqFile("$basePath/scaling_cur_freq")
                val minFreq = readFreqFile("$basePath/cpuinfo_min_freq")
                val maxFreq = readFreqFile("$basePath/cpuinfo_max_freq")
                CpuCoreFrequency(
                    coreIndex = i,
                    currentMhz = curFreq,
                    minMhz = minFreq,
                    maxMhz = maxFreq,
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun readFreqFile(path: String): String {
        return try {
            val khz = File(path).readText().trim().toLong()
            "${khz / 1000} MHz"
        } catch (_: Exception) {
            "N/A"
        }
    }

    private data class GlInfo(
        val version: String,
        val renderer: String,
        val vendor: String,
    )

    private fun readGlInfo(): GlInfo {
        return try {
            val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            val version = IntArray(2)
            EGL14.eglInitialize(display, version, 0, version, 1)

            val configAttribs = intArrayOf(
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                EGL14.EGL_NONE,
            )
            val configs = arrayOfNulls<android.opengl.EGLConfig>(1)
            val numConfigs = IntArray(1)
            EGL14.eglChooseConfig(display, configAttribs, 0, configs, 0, 1, numConfigs, 0)

            val surfaceAttribs = intArrayOf(
                EGL14.EGL_WIDTH, 1,
                EGL14.EGL_HEIGHT, 1,
                EGL14.EGL_NONE,
            )
            val surface = EGL14.eglCreatePbufferSurface(display, configs[0], surfaceAttribs, 0)

            val contextAttribs = intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE,
            )
            val context = EGL14.eglCreateContext(display, configs[0], EGL14.EGL_NO_CONTEXT, contextAttribs, 0)

            EGL14.eglMakeCurrent(display, surface, surface, context)

            val glVersion = GLES20.glGetString(GLES20.GL_VERSION) ?: "Unknown"
            val glRenderer = GLES20.glGetString(GLES20.GL_RENDERER) ?: "Unknown"
            val glVendor = GLES20.glGetString(GLES20.GL_VENDOR) ?: "Unknown"

            EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            EGL14.eglDestroyContext(display, context)
            EGL14.eglDestroySurface(display, surface)
            EGL14.eglTerminate(display)

            GlInfo(glVersion, glRenderer, glVendor)
        } catch (_: Exception) {
            GlInfo("Unknown", "Unknown", "Unknown")
        }
    }

    private fun checkVulkanSupport(application: Application): Boolean {
        val features: Array<FeatureInfo> = application.packageManager.systemAvailableFeatures
        return features.any { it.name == "android.hardware.vulkan.level" }
    }
}
