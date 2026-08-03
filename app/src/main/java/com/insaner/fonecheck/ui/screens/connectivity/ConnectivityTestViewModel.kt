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
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.nfc.NfcManager
import android.os.Build
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import javax.inject.Inject

// ── WiFi ────────────────────────────────────────────────────────────────────────

data class WifiState(
    val isAvailable: Boolean = false,
    val isConnected: Boolean = false,
    val ssid: String? = null,
    val bssid: String? = null,
    val signalStrengthDbm: Int? = null,
    val signalLevel: Int? = null,
    val frequencyMhz: Int? = null,
    val linkSpeedMbps: Int? = null,
    val ipAddress: String? = null,
    val gateway: String? = null,
    val dns1: String? = null,
    val dns2: String? = null,
    val macAddress: String? = null,
    val channelWidth: String? = null,
    val wifiStandard: String? = null,
)

// ── Bluetooth ───────────────────────────────────────────────────────────────────

data class BluetoothState(
    val isAvailable: Boolean = false,
    val isEnabled: Boolean = false,
    val name: String? = null,
    val bleSupported: Boolean = false,
    val bondedDeviceCount: Int = 0,
    val bluetoothVersion: String? = null,
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
    val bearing: Float? = null,
    val fixTimeMs: Long? = null,
    val satelliteCount: Int = 0,
    val satellitesUsed: Int = 0,
    val satellites: List<GpsSatelliteInfo> = emptyList(),
    val searchStartTime: Long = 0L,
    val elapsedSearchMs: Long = 0L,
)

// ── Mobile Network ──────────────────────────────────────────────────────────────

data class MobileNetworkState(
    val isAvailable: Boolean = false,
    val isConnected: Boolean = false,
    val operatorName: String? = null,
    val networkOperator: String? = null,
    val simOperatorName: String? = null,
    val networkType: String? = null,
    val dataState: String? = null,
    val signalStrengthDbm: Int? = null,
    val signalLevel: Int? = null,
    val isRoaming: Boolean = false,
    val phoneType: String? = null,
    val cellId: String? = null,
    val mcc: String? = null,
    val mnc: String? = null,
)

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
        val state: StateFlow<ConnectivityTestState> = _state

        private var gnssCallback: GnssStatus.Callback? = null
        private var gpsSearchJob: kotlinx.coroutines.Job? = null
        private var networkCallback: ConnectivityManager.NetworkCallback? = null
        private var bluetoothReceiver: BroadcastReceiver? = null

        init {
            checkPermissions()
            refreshAll()
            registerNetworkCallback()
            registerBluetoothReceiver()
        }

        fun checkPermissions() {
            _state.value =
                _state.value.copy(
                    hasLocationPermission =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                        ) == PackageManager.PERMISSION_GRANTED,
                    hasPhonePermission =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_PHONE_STATE,
                        ) == PackageManager.PERMISSION_GRANTED,
                )
        }

        fun onPermissionsGranted() {
            checkPermissions()
            refreshAll()
        }

        fun toggleSection(section: ConnectivitySection) {
            val current = _state.value.expandedSection
            _state.value =
                _state.value.copy(
                    expandedSection = if (current == section) null else section,
                )
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
        private fun refreshWifi() {
            val hasWifi = context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI)
            if (!hasWifi) {
                _state.value = _state.value.copy(wifi = WifiState(isAvailable = false))
                return
            }

            val network = connectivityManager.activeNetwork
            val caps = network?.let { connectivityManager.getNetworkCapabilities(it) }
            val isWifiConnected = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

            var ssid: String? = null
            var signalDbm: Int? = null
            var frequency: Int? = null
            var linkSpeed: Int? = null
            var channelWidth: String? = null
            var wifiStandard: String? = null

            if (isWifiConnected && caps != null) {
                val wifiInfo =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        caps.transportInfo as? WifiInfo
                    } else {
                        @Suppress("DEPRECATION")
                        wifiManager.connectionInfo
                    }
                wifiInfo?.let { info ->
                    ssid =
                        info.ssid
                            ?.removePrefix("\"")
                            ?.removeSuffix("\"")
                            ?.takeIf { it != "<unknown ssid>" }
                    signalDbm = info.rssi.takeIf { it != -127 }
                    frequency = info.frequency.takeIf { it > 0 }
                    linkSpeed = info.linkSpeed.takeIf { it > 0 }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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

            val signalLevel = signalDbm?.let { WifiManager.calculateSignalLevel(it, 5) }

            // IP + DNS from DhcpInfo (legacy but reliable)
            @Suppress("DEPRECATION")
            val dhcp = wifiManager?.dhcpInfo
            val ipAddress = getLocalIpAddress()
            val gateway = dhcp?.gateway?.takeIf { it != 0 }?.let { intToIp(it) }
            val dns1 = dhcp?.dns1?.takeIf { it != 0 }?.let { intToIp(it) }
            val dns2 = dhcp?.dns2?.takeIf { it != 0 }?.let { intToIp(it) }

            _state.value =
                _state.value.copy(
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
                            channelWidth = channelWidth,
                            wifiStandard = wifiStandard,
                        ),
                )
        }

        private fun registerNetworkCallback() {
            val callback =
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        refreshWifi()
                    }

                    override fun onLost(network: Network) {
                        refreshWifi()
                    }

                    override fun onCapabilitiesChanged(
                        network: Network,
                        caps: NetworkCapabilities,
                    ) {
                        refreshWifi()
                    }
                }
            networkCallback = callback
            connectivityManager.registerDefaultNetworkCallback(callback)
        }

        private fun getLocalIpAddress(): String? =
            try {
                NetworkInterface
                    .getNetworkInterfaces()
                    ?.asSequence()
                    ?.filter { it.name.startsWith("wlan") || it.name.startsWith("eth") }
                    ?.flatMap { it.inetAddresses.asSequence() }
                    ?.firstOrNull { !it.isLoopbackAddress && it is Inet4Address }
                    ?.hostAddress
            } catch (_: SocketException) {
                null
            }

        private fun intToIp(ip: Int): String =
            "${ip and 0xFF}.${ip shr 8 and 0xFF}.${ip shr 16 and 0xFF}.${ip shr 24 and 0xFF}"

        // ── Bluetooth ───────────────────────────────────────────────────────────────

        @SuppressLint("MissingPermission")
        private fun refreshBluetooth() {
            val adapter = bluetoothManager?.adapter
            val hasBt = context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
            val hasBle = context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

            if (!hasBt || adapter == null) {
                _state.value =
                    _state.value.copy(
                        bluetooth = BluetoothState(isAvailable = false, bleSupported = hasBle),
                    )
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

            val name = if (hasPermission) adapter.name else null
            val bondedCount = if (hasPermission) adapter.bondedDevices?.size ?: 0 else 0

            _state.value =
                _state.value.copy(
                    bluetooth =
                        BluetoothState(
                            isAvailable = true,
                            isEnabled = adapter.isEnabled,
                            name = name,
                            bleSupported = hasBle,
                            bondedDeviceCount = bondedCount,
                            bluetoothVersion = detectBluetoothVersion(),
                        ),
                )
        }

        private fun detectBluetoothVersion(): String? {
            val hasBle = context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
            return if (hasBle) "4.0+" else "Classic"
        }

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
            context.registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        }

        // ── NFC ─────────────────────────────────────────────────────────────────────

        private fun refreshNfc() {
            val nfcAdapter = nfcManager?.defaultAdapter
            val hasNfc = context.packageManager.hasSystemFeature(PackageManager.FEATURE_NFC)
            val hasHce = context.packageManager.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)

            _state.value =
                _state.value.copy(
                    nfc =
                        NfcState(
                            isAvailable = hasNfc && nfcAdapter != null,
                            isEnabled = nfcAdapter?.isEnabled == true,
                            supportsHostCardEmulation = hasHce,
                        ),
                )
        }

        // ── GPS ─────────────────────────────────────────────────────────────────────

        private fun refreshGpsAvailability() {
            val hasGps = context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
            val isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            _state.value =
                _state.value.copy(
                    gps =
                        _state.value.gps.copy(
                            isAvailable = hasGps,
                            isEnabled = isEnabled,
                        ),
                )
        }

        @SuppressLint("MissingPermission")
        fun startGpsFix() {
            if (!_state.value.hasLocationPermission) return

            val startTime = System.currentTimeMillis()
            _state.value =
                _state.value.copy(
                    gps =
                        _state.value.gps.copy(
                            fixStatus = GpsFixStatus.SEARCHING,
                            searchStartTime = startTime,
                            elapsedSearchMs = 0L,
                            satellites = emptyList(),
                            satelliteCount = 0,
                            satellitesUsed = 0,
                            latitude = null,
                            longitude = null,
                            accuracy = null,
                            fixTimeMs = null,
                        ),
                )

            // GNSS status callback for satellite info
            val callback =
                object : GnssStatus.Callback() {
                    override fun onSatelliteStatusChanged(status: GnssStatus) {
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
                        _state.value =
                            _state.value.copy(
                                gps =
                                    _state.value.gps.copy(
                                        satelliteCount = status.satelliteCount,
                                        satellitesUsed = usedCount,
                                        satellites = sats.sortedByDescending { it.cn0DbHz },
                                    ),
                            )
                    }
                }
            gnssCallback = callback
            locationManager.registerGnssStatusCallback(callback, null)

            // Request location updates
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                100L,
                0f,
            ) { location ->
                val fixTime = System.currentTimeMillis() - startTime
                _state.value =
                    _state.value.copy(
                        gps =
                            _state.value.gps.copy(
                                fixStatus = GpsFixStatus.FIXED,
                                latitude = location.latitude,
                                longitude = location.longitude,
                                accuracy = location.accuracy,
                                altitude = if (location.hasAltitude()) location.altitude else null,
                                speed = if (location.hasSpeed()) location.speed else null,
                                bearing = if (location.hasBearing()) location.bearing else null,
                                fixTimeMs = fixTime,
                            ),
                    )
                stopGpsFix()
            }

            // Elapsed time ticker + timeout
            gpsSearchJob =
                viewModelScope.launch {
                    val timeout = 60_000L
                    while (_state.value.gps.fixStatus == GpsFixStatus.SEARCHING) {
                        val elapsed = System.currentTimeMillis() - startTime
                        _state.value =
                            _state.value.copy(
                                gps = _state.value.gps.copy(elapsedSearchMs = elapsed),
                            )
                        if (elapsed > timeout) {
                            _state.value =
                                _state.value.copy(
                                    gps = _state.value.gps.copy(fixStatus = GpsFixStatus.FAILED),
                                )
                            stopGpsFix()
                            break
                        }
                        delay(500L)
                    }
                }
        }

        @SuppressLint("MissingPermission")
        fun stopGpsFix() {
            gnssCallback?.let { locationManager.unregisterGnssStatusCallback(it) }
            gnssCallback = null
            gpsSearchJob?.cancel()
            gpsSearchJob = null
            try {
                locationManager.removeUpdates {}
            } catch (_: Exception) {
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
                _state.value =
                    _state.value.copy(
                        mobileNetwork = MobileNetworkState(isAvailable = false),
                    )
                return
            }

            val network = connectivityManager.activeNetwork
            val caps = network?.let { connectivityManager.getNetworkCapabilities(it) }
            val isCellConnected = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true

            val operatorName = telephonyManager.networkOperatorName?.takeIf { it.isNotBlank() }
            val networkOperator = telephonyManager.networkOperator?.takeIf { it.isNotBlank() }
            val simOperatorName = telephonyManager.simOperatorName?.takeIf { it.isNotBlank() }
            val isRoaming = telephonyManager.isNetworkRoaming

            val phoneType =
                when (telephonyManager.phoneType) {
                    TelephonyManager.PHONE_TYPE_GSM -> "GSM"
                    TelephonyManager.PHONE_TYPE_CDMA -> "CDMA"
                    TelephonyManager.PHONE_TYPE_SIP -> "SIP"
                    else -> "None"
                }

            val dataState =
                when (telephonyManager.dataState) {
                    TelephonyManager.DATA_CONNECTED -> "Connected"
                    TelephonyManager.DATA_CONNECTING -> "Connecting"
                    TelephonyManager.DATA_DISCONNECTED -> "Disconnected"
                    TelephonyManager.DATA_SUSPENDED -> "Suspended"
                    else -> "Unknown"
                }

            val operatorCodes =
                networkOperator?.takeIf { operator ->
                    operator.length in 5..6 && operator.all { it.isDigit() }
                }
            val fallbackMcc = operatorCodes?.take(3)
            val fallbackMnc = operatorCodes?.drop(3)
            val hasPhonePermission = _state.value.hasPhonePermission
            val networkType =
                if (hasPhonePermission) {
                    @Suppress("DEPRECATION")
                    getNetworkTypeName(telephonyManager.dataNetworkType)
                } else {
                    null
                }
            val cellSnapshot =
                if (hasPhonePermission) {
                    getRegisteredCellSnapshot(fallbackMcc, fallbackMnc)
                } else {
                    null
                }

            _state.value =
                _state.value.copy(
                    mobileNetwork =
                        MobileNetworkState(
                            isAvailable = true,
                            isConnected = isCellConnected,
                            operatorName = operatorName,
                            networkOperator = networkOperator,
                            simOperatorName = simOperatorName,
                            networkType = networkType,
                            dataState = dataState,
                            signalStrengthDbm = cellSnapshot?.signalDbm,
                            signalLevel = cellSnapshot?.signalLevel,
                            isRoaming = isRoaming,
                            phoneType = phoneType,
                            cellId = cellSnapshot?.cellId,
                            mcc = cellSnapshot?.mcc ?: fallbackMcc,
                            mnc = cellSnapshot?.mnc ?: fallbackMnc,
                        ),
                )
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
                is CellInfoGsm ->
                    createCellSignalSnapshot(
                        dbm = cellInfo.cellSignalStrength.dbm,
                        level = cellInfo.cellSignalStrength.level,
                        identity = cellInfo.cellIdentity.cid,
                    )
                is CellInfoWcdma ->
                    createCellSignalSnapshot(
                        dbm = cellInfo.cellSignalStrength.dbm,
                        level = cellInfo.cellSignalStrength.level,
                        identity = cellInfo.cellIdentity.cid,
                    )
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

        override fun onCleared() {
            super.onCleared()
            stopGpsFix()
            networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
            bluetoothReceiver?.let {
                try {
                    context.unregisterReceiver(it)
                } catch (_: Exception) {
                }
            }
        }
    }
