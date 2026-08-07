package com.insaner.fonecheck.ui.screens.camera

enum class CameraFacingCode {
    FRONT,
    REAR,
    EXTERNAL,
    UNKNOWN,
}

enum class CameraClassCode {
    STANDARD,
    LOGICAL,
    PHYSICAL_SELECTABLE,
    EXTERNAL,
    UNKNOWN,
}

data class CameraDescriptorReading(
    val cameraId: String,
    val facing: CameraFacingCode,
    val isLogical: Boolean,
    val physicalIds: Set<String> = emptySet(),
)

data class CameraDescriptor(
    val cameraId: String,
    val facing: CameraFacingCode,
    val cameraClass: CameraClassCode,
    val physicalCameraIds: Set<String>,
)

object CameraDescriptorMapper {
    fun map(
        publicIds: Set<String>,
        readings: List<CameraDescriptorReading>,
    ): List<CameraDescriptor> {
        val physicalIds = readings.flatMapTo(mutableSetOf()) { it.physicalIds }
        return readings
            .filter { it.cameraId in publicIds }
            .map { reading ->
                CameraDescriptor(
                    cameraId = reading.cameraId,
                    facing = reading.facing,
                    cameraClass =
                        when {
                            reading.facing == CameraFacingCode.EXTERNAL -> CameraClassCode.EXTERNAL
                            reading.isLogical -> CameraClassCode.LOGICAL
                            reading.cameraId in physicalIds -> CameraClassCode.PHYSICAL_SELECTABLE
                            reading.facing == CameraFacingCode.UNKNOWN -> CameraClassCode.UNKNOWN
                            else -> CameraClassCode.STANDARD
                        },
                    physicalCameraIds = reading.physicalIds,
                )
            }
    }
}

class CameraCaptureGate {
    private var nextToken = 0L
    var activeToken: Long? = null
        private set

    @Synchronized
    fun begin(): Long {
        val token = ++nextToken
        activeToken = token
        return token
    }

    @Synchronized
    fun complete(token: Long): Boolean {
        if (activeToken != token) return false
        activeToken = null
        return true
    }

    @Synchronized
    fun cancel(token: Long) {
        if (activeToken == token) activeToken = null
    }

    @Synchronized
    fun cancelAll() {
        activeToken = null
    }
}
