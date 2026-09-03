package com.insaner.fonecheck.ui.screens.connectivity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.nfc.NfcManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── WiFi ────────────────────────────────────────────────────────────────────────

data class WifiState(
    val isAvailable: Boolean = false,
    val isConnected: Boolean = false,
    val ssid: String? = null,
    val signalStrengthDbm: Int? = null,
    val signalLevel: Int? = null,
    val frequencyMhz: Int? = null,
    val linkSpeedMbps: Int? = null,
    val ipAddress: String? = null,
    val gateway: String? = null,
    val dns1: String? = null,
    val dns2: String? = null,
    val wifiStandard: String? = null,
)

// ── Bluetooth ───────────────────────────────────────────────────────────────────

data class BluetoothState(
    val isAvailable: Boolean = false,
    val access: BluetoothAccessCode = BluetoothAccessCode.HARDWARE_ABSENT,
    val isEnabled: Boolean? = null,
    val name: String? = null,
    val bleSupported: Boolean = false,
    val bondedDeviceCount: Int = 0,
)

// ── NFC ─────────────────────────────────────────────────────────────────────────

data class NfcState(
    val isAvailable: Boolean = false,
    val isEnabled: Boolean = false,
    val supportsHostCardEmulation: Boolean = false,
)

// ── GPS ─────────────────────────────────────────────────────────────────────────

enum class GpsFixStatus {
    NOT_STARTED,
    SEARCHING,
    FIXED,
    FAILED,
}

enum class GpsFailureCode {
    TIMEOUT,
    START_FAILED,
}

data class GpsSatelliteInfo(
    val svid: Int,
    val constellation: String,
    val cn0DbHz: Float,
    val usedInFix: Boolean,
    val elevationDeg: Float,
    val azimuthDeg: Float,
)

data class GpsState(
    val isAvailable: Boolean = false,
    val isEnabled: Boolean = false,
    val fixStatus: GpsFixStatus = GpsFixStatus.NOT_STARTED,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracy: Float? = null,
    val altitude: Double? = null,
    val speed: Float? = null,
    val fixTimeMs: Long? = null,
    val satelliteCount: Int = 0,
    val satellitesUsed: Int = 0,
    val satellites: List<GpsSatelliteInfo> = emptyList(),
    val elapsedSearchMs: Long = 0L,
    val failure: GpsFailureCode? = null,
)

// ── Mobile Network ──────────────────────────────────────────────────────────────

data class MobileNetworkState(
    val isAvailable: Boolean = false,
    val isConnected: Boolean = false,
    val operatorName: String? = null,
    val simOperatorName: String? = null,
    val networkType: String? = null,
    val dataState: MobileDataStateCode? = null,
    val signalStrengthDbm: Int? = null,
    val signalLevel: Int? = null,
    val isRoaming: Boolean? = null,
    val phoneType: String? = null,
    val cellId: String? = null,
    val mcc: String? = null,
    val mnc: String? = null,
)

enum class MobileDataStateCode {
    CONNECTED,
    CONNECTING,
    DISCONNECTED,
    SUSPENDED,
    UNKNOWN,
}

private data class CellNetworkSnapshot(
    val signalDbm: Int? = null,
    val signalLevel: Int? = null,
    val cellId: String? = null,
    val mcc: String? = null,
    val mnc: String? = null,
)

// ── Combined ────────────────────────────────────────────────────────────────────

data class ConnectivityTestState(
    val wifi: WifiState = WifiState(),
    val bluetooth: BluetoothState = BluetoothState(),
    val nfc: NfcState = NfcState(),
    val gps: GpsState = GpsState(),
    val mobileNetwork: MobileNetworkState = MobileNetworkState(),
    val expandedSection: ConnectivitySection? = null,
    val hasLocationPermission: Boolean = false,
    val hasPhonePermission: Boolean = false,
)

enum class ConnectivitySection {
    WIFI,
    BLUETOOTH,
    NFC,
    GPS,
    MOBILE_NETWORK,
}

@HiltViewModel
class ConnectivityTestViewModel
    @Inject
    constructor(
        application: Application,
    ) : AndroidViewModel(application) {
        private val context: Context get() = getApplication()

        private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        private val wifiManager = context.getSystemService(WifiManager::class.java)
        private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
        private val locationManager = context.getSystemService(LocationManager::class.java)
        private val telephonyManager = context.getSystemService(TelephonyManager::class.java)
        private val nfcManager = context.getSystemService(NfcManager::class.java)

        private val _state = MutableStateFlow(ConnectivityTestState())
        val state: StateFlow<ConnectivityTestState> = _state.asStateFlow()

        private val gpsSearchGate = GpsSearchGate(GPS_SEARCH_TIMEOUT_MILLIS)
        private val gnssCallbackOwner = CallbackOwner<GnssStatus.Callback>(::unregisterGnssCallback)
        private val locationListenerOwner = CallbackOwner<LocationListener>(::removeLocationListener)
        private var gpsSearchJob: Job? = null
        private var networkCallback: ConnectivityManager.NetworkCallback? = null
        private var bluetoothReceiver: BroadcastReceiver? = null

        init {
            checkPermissions()
            refreshAll()
            registerNetworkCallback()
            registerBluetoothReceiver()
        }

        fun checkPermissions() {
            val hasLocationPermission =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED
            val hasPhonePermission =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_STATE,
                ) == PackageManager.PERMISSION_GRANTED
            if (!hasLocationPermission) {
                gpsSearchGate.cancel()
                releaseGpsCallbacks()
            }
            _state.update {
                it.copy(
                    hasLocationPermission = hasLocationPermission,
                    hasPhonePermission = hasPhonePermission,
                    gps = if (hasLocationPermission) it.gps else it.gps.clearedProtectedFixData(),
                )
            }
        }

        fun onPermissionsGranted() {
            checkPermissions()
            refreshAll()
        }

        fun toggleSection(section: ConnectivitySection) {
            val current = _state.value.expandedSection
            _state.update {
                it.copy(
                    expandedSection = if (current == section) null else section,
                )
            }
        }

        fun refreshAll() {
            refreshWifi()
            refreshBluetooth()
            refreshNfc()
            refreshGpsAvailability()
            refreshMobileNetwork()
        }

        // ── WiFi ────────────────────────────────────────────────────────────────────

        @SuppressLint("MissingPermission")
        @Suppress("kotlin:S3776") // Wi-Fi snapshot assembly keeps one consistent network read.
        private fun refreshWifi() {
            val hasWifi = context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI)
            if (!hasWifi) {
                _state.update { it.copy(wifi = WifiState(isAvailable = false)) }
                return
            }

            val network = connectivityManager.activeNetwork
            val caps = network?.let { connectivityManager.getNetworkCapabilities(it) }
            val wifiCapabilities = caps?.takeIf { it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) }
            val isWifiConnected = wifiCapabilities != null

            var ssid: String? = null
            var signalDbm: Int? = null
            var frequency: Int? = null
            var linkSpeed: Int? = null
            var wifiStandard: String? = null

            if (wifiCapabilities != null) {
                val wifiInfo =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        wifiCapabilities.transportInfo as? WifiInfo
                    } else {
                        @Suppress("DEPRECATION")
                        wifiManager.connectionInfo
                    }
                wifiInfo?.let { info ->
                    if (_state.value.hasLocationPermission) {
                        ssid =
                            info.ssid
                                ?.removePrefix("\"")
                                ?.removeSuffix("\"")
                                ?.takeIf { it != "<unknown ssid>" }
                    }
                    signalDbm = info.rssi.takeIf { it != -127 }
                    frequency = info.frequency.takeIf { it > 0 }
                    linkSpeed = info.linkSpeed.takeIf { it > 0 }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        wifiStandard =
                            when (info.wifiStandard) {
                                ScanResult.WIFI_STANDARD_11AX -> "Wi-Fi 6 (802.11ax)"
                                ScanResult.WIFI_STANDARD_11AC -> "Wi-Fi 5 (802.11ac)"
                                ScanResult.WIFI_STANDARD_11N -> "Wi-Fi 4 (802.11n)"
                                else -> null
                            }
                    }
                }
            }

            val signalLevel = signalDbm?.let(::wifiSignalLevel)

            val linkProperties = network?.let(connectivityManager::getLinkProperties)
            val ipAddress =
                linkProperties
                    ?.linkAddresses
                    ?.firstOrNull { it.address.address.size == IPV4_ADDRESS_BYTES }
                    ?.address
                    ?.hostAddress
            val gateway =
                linkProperties
                    ?.routes
                    ?.firstOrNull { it.isDefaultRoute && it.gateway != null }
                    ?.gateway
                    ?.hostAddress
            val dns1 = linkProperties?.dnsServers?.getOrNull(0)?.hostAddress
            val dns2 = linkProperties?.dnsServers?.getOrNull(1)?.hostAddress

            _state.update {
                it.copy(
                    wifi =
                        WifiState(
                            isAvailable = true,
                            isConnected = isWifiConnected,
                            ssid = ssid,
                            signalStrengthDbm = signalDbm,
                            signalLevel = signalLevel,
                            frequencyMhz = frequency,
                            linkSpeedMbps = linkSpeed,
                            ipAddress = ipAddress,
                            gateway = gateway,
                            dns1 = dns1,
                            dns2 = dns2,
                            wifiStandard = wifiStandard,
                        ),
                )
            }
        }

        private fun registerNetworkCallback() {
            val callback =
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        refreshNetworkState()
                    }

                    override fun onLost(network: Network) {
                        refreshNetworkState()
                    }

                    override fun onCapabilitiesChanged(
                        network: Network,
                        caps: NetworkCapabilities,
                    ) {
                        refreshNetworkState()
                    }
                }
            networkCallback = callback
            connectivityManager.registerDefaultNetworkCallback(callback)
        }

        private fun refreshNetworkState() {
            refreshWifi()
            refreshMobileNetwork()
        }

        private fun wifiSignalLevel(rssiDbm: Int): Int =
            when {
                rssiDbm >= -55 -> 4
                rssiDbm >= -65 -> 3
                rssiDbm >= -75 -> 2
                rssiDbm >= -85 -> 1
                else -> 0
            }

        // ── Bluetooth ───────────────────────────────────────────────────────────────

        @SuppressLint("MissingPermission")
        private fun refreshBluetooth() {
            val adapter = bluetoothManager?.adapter
            val hasBt = context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
            val hasBle = context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

            if (!hasBt || adapter == null) {
                _state.update {
                    it.copy(
                        bluetooth =
                            BluetoothState(
                                isAvailable = false,
                                access = BluetoothAccessCode.HARDWARE_ABSENT,
                                bleSupported = hasBle,
                            ),
                    )
                }
                return
            }

            val hasPermission =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT,
                    ) == PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }

            val access =
                BluetoothAccessPolicy.evaluate(
                    sdkInt = Build.VERSION.SDK_INT,
                    hardwareAvailable = true,
                    permissionGranted = hasPermission,
                )
            val canReadProtectedState =
                access == BluetoothAccessCode.GRANTED || access == BluetoothAccessCode.NOT_REQUIRED
            val protectedState =
                if (canReadProtectedState) {
                    try {
                        BluetoothProtectedState(
                            isEnabled = adapter.isEnabled,
                            name = adapter.name,
                            bondedDeviceCount = adapter.bondedDevices?.size ?: 0,
                        )
                    } catch (_: SecurityException) {
                        null
                    }
                } else {
                    null
                }
            val effectiveAccess =
                if (canReadProtectedState && protectedState == null) {
                    BluetoothAccessCode.PERMISSION_DENIED
                } else {
                    access
                }

            _state.update {
                it.copy(
                    bluetooth =
                        BluetoothState(
                            isAvailable = true,
                            access = effectiveAccess,
                            isEnabled = protectedState?.isEnabled,
                            name = protectedState?.name,
                            bleSupported = hasBle,
                            bondedDeviceCount = protectedState?.bondedDeviceCount ?: 0,
                        ),
                )
            }
        }

        private data class BluetoothProtectedState(
            val isEnabled: Boolean,
            val name: String?,
            val bondedDeviceCount: Int,
        )

        private fun registerBluetoothReceiver() {
            val receiver =
                object : BroadcastReceiver() {
                    override fun onReceive(
                        ctx: Context?,
                        intent: Intent?,
                    ) {
                        if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                            refreshBluetooth()
                        }
                    }
                }
            bluetoothReceiver = receiver
            // Bluetooth framework broadcasts can originate from a privileged non-system UID.
            // The receiver ignores payload data and only refreshes state from Android APIs.
            ContextCompat.registerReceiver(
                context,
                receiver,
                IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED),
                ContextCompat.RECEIVER_EXPORTED,
            )
        }

        // ── NFC ─────────────────────────────────────────────────────────────────────

        private fun refreshNfc() {
            val nfcAdapter = nfcManager?.defaultAdapter
            val hasNfc = context.packageManager.hasSystemFeature(PackageManager.FEATURE_NFC)
            val hasHce = context.packageManager.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)

            _state.update {
                it.copy(
                    nfc =
                        NfcState(
                            isAvailable = hasNfc && nfcAdapter != null,
                            isEnabled = nfcAdapter?.isEnabled == true,
                            supportsHostCardEmulation = hasHce,
                        ),
                )
            }
        }

        // ── GPS ─────────────────────────────────────────────────────────────────────

        private fun refreshGpsAvailability() {
            val hasGps = context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
            val isEnabled = hasGps && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (!hasGps || !isEnabled) {
                gpsSearchGate.cancel()
                releaseGpsCallbacks()
            }
            _state.update {
                val gps = it.gps.copy(isAvailable = hasGps, isEnabled = isEnabled)
                it.copy(
                    gps = if (hasGps && isEnabled) gps else gps.clearedProtectedFixData(),
                )
            }
        }

        @SuppressLint("MissingPermission")
        @Suppress("LongMethod", "kotlin:S3776") // GPS callback ownership is kept atomic to prevent leaks.
        fun startGpsFix() {
            val gps = _state.value.gps
            if (!_state.value.hasLocationPermission || !gps.isAvailable || !gps.isEnabled) return

            val startTime = System.currentTimeMillis()
            val token = gpsSearchGate.start(startTime) ?: return
            _state.update {
                it.copy(
                    gps =
                        it.gps.copy(
                            fixStatus = GpsFixStatus.SEARCHING,
                            elapsedSearchMs = 0L,
                            satellites = emptyList(),
                            satelliteCount = 0,
                            satellitesUsed = 0,
                            latitude = null,
                            longitude = null,
                            accuracy = null,
                            altitude = null,
                            speed = null,
                            fixTimeMs = null,
                            failure = null,
                        ),
                )
            }

            val gnssCallback =
                object : GnssStatus.Callback() {
                    override fun onSatelliteStatusChanged(status: GnssStatus) {
                        if (!gpsSearchGate.isActive(token)) return
                        val sats = mutableListOf<GpsSatelliteInfo>()
                        var usedCount = 0
                        for (i in 0 until status.satelliteCount) {
                            val used = status.usedInFix(i)
                            if (used) usedCount++
                            sats.add(
                                GpsSatelliteInfo(
                                    svid = status.getSvid(i),
                                    constellation = getConstellationName(status.getConstellationType(i)),
                                    cn0DbHz = status.getCn0DbHz(i),
                                    usedInFix = used,
                                    elevationDeg = status.getElevationDegrees(i),
                                    azimuthDeg = status.getAzimuthDegrees(i),
                                ),
                            )
                        }
                        _state.update {
                            it.copy(
                                gps =
                                    it.gps.copy(
                                        satelliteCount = status.satelliteCount,
                                        satellitesUsed = usedCount,
                                        satellites = sats.sortedByDescending { it.cn0DbHz },
                                    ),
                            )
                        }
                    }
                }
            val locationListener =
                LocationListener { location ->
                    if (!gpsSearchGate.complete(token)) return@LocationListener
                    val fixTime = System.currentTimeMillis() - startTime
                    _state.update {
                        it.copy(
                            gps =
                                it.gps.copy(
                                    fixStatus = GpsFixStatus.FIXED,
                                    latitude = location.latitude,
                                    longitude = location.longitude,
                                    accuracy = location.accuracy,
                                    altitude = if (location.hasAltitude()) location.altitude else null,
                                    speed = if (location.hasSpeed()) location.speed else null,
                                    fixTimeMs = fixTime,
                                    elapsedSearchMs = fixTime,
                                    failure = null,
                                ),
                        )
                    }
                    releaseGpsCallbacks()
                }

            try {
                gnssCallbackOwner.replace(gnssCallback)
                val gnssRegistered =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        locationManager.registerGnssStatusCallback(context.mainExecutor, gnssCallback)
                    } else {
                        locationManager.registerGnssStatusCallback(
                            gnssCallback,
                            Handler(Looper.getMainLooper()),
                        )
                    }
                if (!gnssRegistered) {
                    gnssCallbackOwner.clear()
                }
                locationListenerOwner.replace(locationListener)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        GPS_MIN_UPDATE_MILLIS,
                        0f,
                        context.mainExecutor,
                        locationListener,
                    )
                } else {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        GPS_MIN_UPDATE_MILLIS,
                        0f,
                        locationListener,
                    )
                }
            } catch (_: RuntimeException) {
                gpsSearchGate.cancel(token)
                releaseGpsCallbacks()
                _state.update {
                    it.copy(
                        gps = it.gps.copy(fixStatus = GpsFixStatus.FAILED, failure = GpsFailureCode.START_FAILED),
                    )
                }
                return
            }

            gpsSearchJob =
                viewModelScope.launch {
                    while (gpsSearchGate.isActive(token)) {
                        delay(GPS_TICK_MILLIS)
                        val elapsed = System.currentTimeMillis() - startTime
                        when (gpsSearchGate.tick(token, System.currentTimeMillis())) {
                            GpsSearchTick.ACTIVE ->
                                _state.update {
                                    it.copy(gps = it.gps.copy(elapsedSearchMs = elapsed))
                                }

                            GpsSearchTick.TIMED_OUT -> {
                                _state.update {
                                    it.copy(
                                        gps =
                                            it.gps.copy(
                                                fixStatus = GpsFixStatus.FAILED,
                                                elapsedSearchMs = elapsed,
                                                failure = GpsFailureCode.TIMEOUT,
                                            ),
                                    )
                                }
                                releaseGpsCallbacks()
                            }

                            GpsSearchTick.IGNORED -> Unit
                        }
                    }
                }
        }

        fun cancelGpsFix() {
            gpsSearchGate.cancel()
            releaseGpsCallbacks()
            _state.update {
                it.copy(gps = it.gps.clearedProtectedFixData())
            }
        }

        private fun releaseGpsCallbacks() {
            gpsSearchJob?.cancel()
            gpsSearchJob = null
            gnssCallbackOwner.clear()
            locationListenerOwner.clear()
        }

        private fun unregisterGnssCallback(callback: GnssStatus.Callback) {
            try {
                locationManager.unregisterGnssStatusCallback(callback)
            } catch (_: IllegalArgumentException) {
                // The callback was already removed by the platform.
            }
        }

        private fun removeLocationListener(listener: LocationListener) {
            try {
                locationManager.removeUpdates(listener)
            } catch (_: SecurityException) {
                // Revoking location permission also stops delivery; local ownership is still cleared.
            }
        }

        private fun getConstellationName(type: Int): String =
            when (type) {
                GnssStatus.CONSTELLATION_GPS -> "GPS"
                GnssStatus.CONSTELLATION_SBAS -> "SBAS"
                GnssStatus.CONSTELLATION_GLONASS -> "GLONASS"
                GnssStatus.CONSTELLATION_QZSS -> "QZSS"
                GnssStatus.CONSTELLATION_BEIDOU -> "BeiDou"
                GnssStatus.CONSTELLATION_GALILEO -> "Galileo"
                GnssStatus.CONSTELLATION_IRNSS -> "IRNSS"
                else -> "Unknown"
            }

        // ── Mobile Network ──────────────────────────────────────────────────────────

        @SuppressLint("MissingPermission")
        private fun refreshMobileNetwork() {
            val hasTelephony = context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
            if (!hasTelephony) {
                _state.update { it.copy(mobileNetwork = MobileNetworkState(isAvailable = false)) }
                return
            }

            val network = connectivityManager.activeNetwork
            val caps = network?.let { connectivityManager.getNetworkCapabilities(it) }
            val isCellConnected = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true

            val hasPhonePermission = _state.value.hasPhonePermission
            val protectedState = if (hasPhonePermission) readProtectedMobileState() else null

            _state.update {
                it.copy(
                    mobileNetwork =
                        MobileNetworkState(
                            isAvailable = true,
                            isConnected = isCellConnected,
                            operatorName = protectedState?.operatorName,
                            simOperatorName = protectedState?.simOperatorName,
                            networkType = protectedState?.networkType,
                            dataState = protectedState?.dataState,
                            signalStrengthDbm = protectedState?.cell?.signalDbm,
                            signalLevel = protectedState?.cell?.signalLevel,
                            isRoaming = protectedState?.isRoaming,
                            phoneType = protectedState?.phoneType,
                            cellId = protectedState?.cell?.cellId,
                            mcc = protectedState?.cell?.mcc,
                            mnc = protectedState?.cell?.mnc,
                        ),
                )
            }
        }

        private data class ProtectedMobileState(
            val operatorName: String?,
            val simOperatorName: String?,
            val networkType: String?,
            val dataState: MobileDataStateCode,
            val isRoaming: Boolean,
            val phoneType: String,
            val cell: CellNetworkSnapshot?,
        )

        @SuppressLint("MissingPermission")
        private fun readProtectedMobileState(): ProtectedMobileState? =
            try {
                val networkOperator = telephonyManager.networkOperator?.takeIf { it.isNotBlank() }
                val operatorCodes =
                    networkOperator?.takeIf { operator ->
                        operator.length in 5..6 && operator.all(Char::isDigit)
                    }
                val fallbackMcc = operatorCodes?.take(3)
                val fallbackMnc = operatorCodes?.drop(3)

                @Suppress("DEPRECATION")
                val networkType = getNetworkTypeName(telephonyManager.dataNetworkType)
                ProtectedMobileState(
                    operatorName = telephonyManager.networkOperatorName?.takeIf(String::isNotBlank),
                    simOperatorName = telephonyManager.simOperatorName?.takeIf(String::isNotBlank),
                    networkType = networkType,
                    dataState = mobileDataState(telephonyManager.dataState),
                    isRoaming = telephonyManager.isNetworkRoaming,
                    phoneType = phoneType(telephonyManager.phoneType),
                    cell =
                        if (_state.value.hasLocationPermission) {
                            getRegisteredCellSnapshot(fallbackMcc, fallbackMnc)
                        } else {
                            null
                        },
                )
            } catch (_: SecurityException) {
                null
            }

        private fun mobileDataState(state: Int): MobileDataStateCode =
            when (state) {
                TelephonyManager.DATA_CONNECTED -> MobileDataStateCode.CONNECTED
                TelephonyManager.DATA_CONNECTING -> MobileDataStateCode.CONNECTING
                TelephonyManager.DATA_DISCONNECTED -> MobileDataStateCode.DISCONNECTED
                TelephonyManager.DATA_SUSPENDED -> MobileDataStateCode.SUSPENDED
                else -> MobileDataStateCode.UNKNOWN
            }

        @Suppress("DEPRECATION")
        private fun phoneType(type: Int): String =
            when (type) {
                TelephonyManager.PHONE_TYPE_GSM -> "GSM"
                TelephonyManager.PHONE_TYPE_CDMA -> "CDMA"
                TelephonyManager.PHONE_TYPE_SIP -> "SIP"
                else -> "None"
            }

        @SuppressLint("MissingPermission")
        private fun getRegisteredCellSnapshot(
            fallbackMcc: String?,
            fallbackMnc: String?,
        ): CellNetworkSnapshot? {
            val cellInfoList =
                try {
                    telephonyManager.allCellInfo
                } catch (_: SecurityException) {
                    return null
                }
            val registeredCell = cellInfoList?.firstOrNull { it.isRegistered } ?: return null
            val signalSnapshot = getCellSignalSnapshot(registeredCell)
            val networkCodes = getCellNetworkCodes(registeredCell, fallbackMcc, fallbackMnc)

            return CellNetworkSnapshot(
                signalDbm = signalSnapshot?.signalDbm,
                signalLevel = signalSnapshot?.signalLevel,
                cellId = signalSnapshot?.cellId,
                mcc = networkCodes.first,
                mnc = networkCodes.second,
            )
        }

        private fun getCellSignalSnapshot(cellInfo: CellInfo): CellNetworkSnapshot? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo is CellInfoNr) {
                return createCellSignalSnapshot(
                    dbm = cellInfo.cellSignalStrength.dbm,
                    level = cellInfo.cellSignalStrength.level,
                )
            }

            return when (cellInfo) {
                is CellInfoLte ->
                    createCellSignalSnapshot(
                        dbm = cellInfo.cellSignalStrength.dbm,
                        level = cellInfo.cellSignalStrength.level,
                        identity = cellInfo.cellIdentity.ci,
                    )
                is CellInfoGsm,
                is CellInfoWcdma,
                -> createLegacyCellSignalSnapshot(cellInfo)
                else -> null
            }
        }

        private fun createCellSignalSnapshot(
            dbm: Int,
            level: Int,
            identity: Int? = null,
        ): CellNetworkSnapshot =
            CellNetworkSnapshot(
                signalDbm = dbm,
                signalLevel = level,
                cellId = identity?.takeIf { it != Int.MAX_VALUE }?.toString(),
            )

        private fun getCellNetworkCodes(
            cellInfo: CellInfo,
            fallbackMcc: String?,
            fallbackMnc: String?,
        ): Pair<String?, String?> {
            val currentCodes: Pair<String?, String?>? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    when (cellInfo) {
                        is CellInfoLte -> cellInfo.cellIdentity.mccString to cellInfo.cellIdentity.mncString
                        is CellInfoGsm -> cellInfo.cellIdentity.mccString to cellInfo.cellIdentity.mncString
                        is CellInfoWcdma -> cellInfo.cellIdentity.mccString to cellInfo.cellIdentity.mncString
                        else -> null
                    }
                } else {
                    null
                }

            return (currentCodes?.first ?: fallbackMcc) to (currentCodes?.second ?: fallbackMnc)
        }

        @Suppress("DEPRECATION")
        private fun getNetworkTypeName(type: Int): String =
            when (type) {
                TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
                TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
                TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
                TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
                TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO Rev.0"
                TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO Rev.A"
                TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT"
                TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
                TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
                TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
                TelephonyManager.NETWORK_TYPE_IDEN -> "iDen"
                TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO Rev.B"
                TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
                TelephonyManager.NETWORK_TYPE_EHRPD -> "eHRPD"
                TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
                TelephonyManager.NETWORK_TYPE_NR -> "5G NR"
                else -> "Unknown"
            }

        companion object {
            private const val IPV4_ADDRESS_BYTES = 4
            private const val GPS_SEARCH_TIMEOUT_MILLIS = 60_000L
            private const val GPS_TICK_MILLIS = 500L
            private const val GPS_MIN_UPDATE_MILLIS = 100L
        }

        override fun onCleared() {
            cancelGpsFix()
            networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
            bluetoothReceiver?.let {
                try {
                    context.unregisterReceiver(it)
                } catch (_: Exception) {
                    // The receiver may already be unregistered during teardown.
                }
            }
        }

        private fun createLegacyCellSignalSnapshot(cellInfo: CellInfo): CellNetworkSnapshot {
            val gsm = cellInfo as? CellInfoGsm
            if (gsm != null) {
                return createCellSignalSnapshot(
                    dbm = gsm.cellSignalStrength.dbm,
                    level = gsm.cellSignalStrength.level,
                    identity = gsm.cellIdentity.cid,
                )
            }
            val wcdma = cellInfo as CellInfoWcdma
            return createCellSignalSnapshot(
                dbm = wcdma.cellSignalStrength.dbm,
                level = wcdma.cellSignalStrength.level,
                identity = wcdma.cellIdentity.cid,
            )
        }
    }
