package com.insaner.fonecheck.ui.screens.simtelephony

import com.insaner.fonecheck.domain.model.DataNetworkObservation
import com.insaner.fonecheck.domain.model.NetworkDisplayObservation
import com.insaner.fonecheck.domain.model.NetworkReadState
import com.insaner.fonecheck.domain.model.indication
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

/** One capture owns one registration. Implementations must unwind partial registration on failure. */
internal interface DataNetworkPlatform {
    val displaySupported: Boolean

    fun hasHardware(): Boolean

    fun hasPermission(): Boolean

    fun dataSubscription(): Int?

    fun readBase(subscription: Int): Int

    fun listen(
        subscription: Int,
        onDisplay: (Int, Int) -> Unit,
        onSubscriptionChanged: () -> Unit,
    ): () -> Unit
}

internal class DataNetworkCapture(
    private val platform: DataNetworkPlatform,
    private val now: () -> Instant,
    private val elapsedMillis: () -> Long,
) {
    @Suppress("ReturnCount") // Each exit captures a distinct permission, hardware, or observation state.
    suspend fun capture(): DataNetworkObservation {
        var base: Int? = null
        var baseAt: Instant? = null
        var baseState = NetworkReadState.PENDING
        var display = NetworkDisplayObservation(NetworkReadState.PENDING)

        fun result(display: NetworkDisplayObservation) = DataNetworkObservation(base, baseAt, baseState, display, now())

        fun unavailable(state: NetworkReadState): DataNetworkObservation {
            baseState = state
            return result(display.copy(state = state))
        }

        val events = Channel<NetworkDisplayObservation>(capacity = 1)
        val accepting = AtomicBoolean(true)
        val changed = AtomicBoolean(false)
        var stop: (() -> Unit)? = null
        try {
            currentCoroutineContext().ensureActive()
            if (!platform.hasHardware()) return unavailable(NetworkReadState.NO_HARDWARE)
            if (!platform.hasPermission()) return unavailable(NetworkReadState.PERMISSION_DENIED)
            val subscription = platform.dataSubscription() ?: return unavailable(NetworkReadState.NO_SUBSCRIPTION)
            val started = elapsedMillis()
            base = platform.readBase(subscription)
            baseAt = now()
            baseState = NetworkReadState.RECEIVED
            if (!platform.displaySupported) {
                currentCoroutineContext().ensureActive()
                val unavailableState = subscriptionUnavailableState(subscription)
                if (unavailableState != null) return unavailable(unavailableState)
                return result(NetworkDisplayObservation(NetworkReadState.UNSUPPORTED))
            }
            stop = listen(subscription, events, accepting, changed)
            display = awaitDisplay(events, started)
            currentCoroutineContext().ensureActive()
            if (!platform.hasPermission()) return unavailable(NetworkReadState.PERMISSION_DENIED)
            if (changed.get() || platform.dataSubscription() != subscription) {
                baseState = NetworkReadState.STALE
                return result(display.copy(state = NetworkReadState.STALE))
            }
            if (display.state == NetworkReadState.RECEIVED) {
                // Read again next to receipt; the pair is still explicitly not an atomic radio measurement.
                baseState = NetworkReadState.FAILED
                base = platform.readBase(subscription)
                baseAt = now()
                baseState = NetworkReadState.RECEIVED
                display = validateDisplay(display, base, started)
            }
            if (changed.get() || platform.dataSubscription() != subscription) {
                baseState = NetworkReadState.STALE
                display = display.copy(state = NetworkReadState.STALE)
            }
            return result(display)
        } catch (_: SecurityException) {
            currentCoroutineContext().ensureActive()
            return unavailable(NetworkReadState.PERMISSION_DENIED)
        } catch (_: RuntimeException) {
            currentCoroutineContext().ensureActive()
            if (baseAt == null) baseState = NetworkReadState.FAILED
            return result(display.copy(state = NetworkReadState.FAILED))
        } finally {
            accepting.set(false)
            events.cancel()
            stopListening(stop)
        }
    }

    private fun subscriptionUnavailableState(subscription: Int): NetworkReadState? =
        when {
            !platform.hasPermission() -> NetworkReadState.PERMISSION_DENIED
            platform.dataSubscription() != subscription -> NetworkReadState.STALE
            else -> null
        }

    private fun listen(
        subscription: Int,
        events: Channel<NetworkDisplayObservation>,
        accepting: AtomicBoolean,
        changed: AtomicBoolean,
    ): () -> Unit =
        platform.listen(
            subscription,
            onDisplay = { type, overrideType ->
                if (accepting.get()) {
                    events.trySend(NetworkDisplayObservation(NetworkReadState.RECEIVED, type, overrideType, now()))
                }
            },
            onSubscriptionChanged = {
                if (accepting.get()) {
                    changed.set(true)
                    events.trySend(NetworkDisplayObservation(NetworkReadState.STALE))
                }
            },
        )

    private suspend fun awaitDisplay(
        events: Channel<NetworkDisplayObservation>,
        started: Long,
    ): NetworkDisplayObservation {
        val remaining = WINDOW_MILLIS - (elapsedMillis() - started)
        return if (remaining > 0) {
            withTimeoutOrNull(remaining) { events.receive() }
                ?: NetworkDisplayObservation(NetworkReadState.TIMED_OUT)
        } else {
            NetworkDisplayObservation(NetworkReadState.TIMED_OUT)
        }
    }

    private fun validateDisplay(
        display: NetworkDisplayObservation,
        base: Int?,
        started: Long,
    ): NetworkDisplayObservation =
        when {
            elapsedMillis() - started >= WINDOW_MILLIS || display.baseType != base ->
                display.copy(state = NetworkReadState.STALE)
            display.indication() == null -> display.copy(state = NetworkReadState.UNRECOGNIZED)
            else -> display
        }

    private fun stopListening(stop: (() -> Unit)?) {
        // Runs on the registration context even when the caller is cancelled.
        try {
            stop?.invoke()
        } catch (_: RuntimeException) {
            // A revoked permission/dead telephony service must not prevent local invalidation.
        }
    }

    private companion object {
        const val WINDOW_MILLIS = 1_200L
    }
}
