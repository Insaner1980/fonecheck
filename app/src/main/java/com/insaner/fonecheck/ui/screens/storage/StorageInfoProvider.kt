package com.insaner.fonecheck.ui.screens.storage

import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.insaner.fonecheck.runtime.EpochMillisClock
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.time.Instant
import javax.inject.Inject

data class AppStorageVolumeInfo(
    val isPrimary: Boolean,
    val isRemovable: Boolean,
    val stateCode: String,
    val isMounted: Boolean,
    val totalBytes: Long?,
    val availableBytes: Long?,
)

data class StorageInfo(
    val totalBytes: Long,
    val usedBytes: Long,
    val availableBytes: Long,
    val usagePercent: Double?,
    val internalStorageAccessible: Boolean,
    val appAccessibleVolumes: List<AppStorageVolumeInfo>,
    val capturedAt: Instant,
)

fun interface StorageInfoProvider {
    fun capture(): StorageInfo
}

class AndroidStorageInfoProvider
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val clock: EpochMillisClock,
    ) : StorageInfoProvider {
        override fun capture(): StorageInfo {
            val internalDirectory = context.filesDir
            val internalStats = StatFs(internalDirectory.absolutePath)
            val totalBytes = internalStats.totalBytes.coerceAtLeast(0L)
            val availableBytes = internalStats.availableBytes.coerceIn(0L, totalBytes)

            return StorageInfo(
                totalBytes = totalBytes,
                usedBytes = (totalBytes - availableBytes).coerceAtLeast(0L),
                availableBytes = availableBytes,
                usagePercent = StorageRuntimePolicy.usagePercent(totalBytes, availableBytes),
                internalStorageAccessible = internalDirectory.canRead() && internalDirectory.canWrite(),
                appAccessibleVolumes = appAccessibleVolumes(),
                capturedAt = Instant.ofEpochMilli(clock.currentTimeMillis()),
            )
        }

        // This reads capacity metadata from app-scoped directories; it does not store report data externally.
        @Suppress("kotlin:S5324")
        private fun appAccessibleVolumes(): List<AppStorageVolumeInfo> =
            context.getExternalFilesDirs(null).mapIndexedNotNull { index, directory ->
                directory?.let { externalVolume(it, index == 0) }
            }

        private fun externalVolume(
            directory: File,
            isPrimary: Boolean,
        ): AppStorageVolumeInfo {
            val state =
                runCatching { Environment.getExternalStorageState(directory) }
                    .getOrDefault(Environment.MEDIA_UNKNOWN)
            val mounted = state == Environment.MEDIA_MOUNTED || state == Environment.MEDIA_MOUNTED_READ_ONLY
            val removable = runCatching { Environment.isExternalStorageRemovable(directory) }.getOrDefault(false)
            val stats =
                if (mounted) {
                    runCatching { StatFs(directory.absolutePath) }.getOrNull()
                } else {
                    null
                }
            return AppStorageVolumeInfo(
                isPrimary = isPrimary,
                isRemovable = removable,
                stateCode = state,
                isMounted = mounted,
                totalBytes = stats?.totalBytes,
                availableBytes = stats?.availableBytes,
            )
        }
    }
