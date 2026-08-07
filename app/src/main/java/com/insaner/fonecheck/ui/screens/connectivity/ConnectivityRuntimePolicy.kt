package com.insaner.fonecheck.ui.screens.connectivity

enum class BluetoothAccessCode {
    HARDWARE_ABSENT,
    PERMISSION_DENIED,
    NOT_REQUIRED,
    GRANTED,
}

object BluetoothAccessPolicy {
    fun evaluate(
        sdkInt: Int,
        hardwareAvailable: Boolean,
        permissionGranted: Boolean,
    ): BluetoothAccessCode =
        when {
            !hardwareAvailable -> BluetoothAccessCode.HARDWARE_ABSENT
            sdkInt < ANDROID_12_API_LEVEL -> BluetoothAccessCode.NOT_REQUIRED
            permissionGranted -> BluetoothAccessCode.GRANTED
            else -> BluetoothAccessCode.PERMISSION_DENIED
        }

    private const val ANDROID_12_API_LEVEL = 31
}

enum class GpsSearchTick {
    ACTIVE,
    TIMED_OUT,
    IGNORED,
}

class GpsSearchGate(
    private val timeoutMillis: Long,
) {
    private data class ActiveSearch(
        val token: Long,
        val startedAtMillis: Long,
    )

    private var nextToken = 0L
    private var activeSearch: ActiveSearch? = null

    @Synchronized
    fun start(nowMillis: Long): Long? {
        if (activeSearch != null) return null
        val token = ++nextToken
        activeSearch = ActiveSearch(token, nowMillis)
        return token
    }

    @Synchronized
    fun tick(
        token: Long,
        nowMillis: Long,
    ): GpsSearchTick {
        val search = activeSearch?.takeIf { it.token == token } ?: return GpsSearchTick.IGNORED
        return if (nowMillis - search.startedAtMillis >= timeoutMillis) {
            activeSearch = null
            GpsSearchTick.TIMED_OUT
        } else {
            GpsSearchTick.ACTIVE
        }
    }

    @Synchronized
    fun complete(token: Long): Boolean {
        if (activeSearch?.token != token) return false
        activeSearch = null
        return true
    }

    @Synchronized
    fun cancel(token: Long? = null) {
        if (token == null || activeSearch?.token == token) activeSearch = null
    }

    @Synchronized
    fun isActive(token: Long): Boolean = activeSearch?.token == token
}

class CallbackOwner<T : Any>(
    private val unregister: (T) -> Unit,
) {
    private var callback: T? = null

    @Synchronized
    fun replace(next: T) {
        if (callback === next) return
        val previous = callback
        callback = next
        previous?.let(unregister)
    }

    @Synchronized
    fun clear() {
        val current = callback ?: return
        callback = null
        unregister(current)
    }
}
