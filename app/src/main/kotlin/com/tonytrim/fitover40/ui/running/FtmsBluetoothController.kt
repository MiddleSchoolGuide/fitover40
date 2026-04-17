package com.tonytrim.fitover40.ui.running

import android.annotation.SuppressLint
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import com.tonytrim.fitover40.domain.model.BluetoothTreadmillDevice
import java.util.UUID

private val FTMS_SERVICE_UUID: UUID = UUID.fromString("00001826-0000-1000-8000-00805f9b34fb")
private val TREADMILL_DATA_UUID: UUID = UUID.fromString("00002acd-0000-1000-8000-00805f9b34fb")
private val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

class FtmsBluetoothController(
    context: Context,
    private val onDevicesUpdated: (List<BluetoothTreadmillDevice>) -> Unit,
    private val onScanningChanged: (Boolean) -> Unit,
    private val onConnected: (String) -> Unit,
    private val onDisconnected: () -> Unit,
    private val onSpeedSample: (Double) -> Unit,
    private val onStatus: (String) -> Unit
) {
    private val appContext = context.applicationContext
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter
    private val scanner: BluetoothLeScanner? get() = adapter?.bluetoothLeScanner
    private val devices = linkedMapOf<String, BluetoothTreadmillDevice>()

    private var currentGatt: BluetoothGatt? = null
    private var isScanning = false

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            handleScanResult(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            results.forEach(::handleScanResult)
        }

        override fun onScanFailed(errorCode: Int) {
            isScanning = false
            onScanningChanged(false)
            onStatus("BLE scan failed with code $errorCode.")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                currentGatt = gatt
                onConnected(deviceName(gatt.device))
                onStatus("Connected to ${deviceName(gatt.device)}. Discovering FTMS services.")
                if (!hasBluetoothConnectPermission()) {
                    onStatus("Bluetooth permission missing. Unable to discover treadmill services.")
                    return
                }
                discoverServices(gatt)
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                if (hasBluetoothConnectPermission()) {
                    closeGatt(gatt)
                }
                if (currentGatt == gatt) currentGatt = null
                onDisconnected()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val service = gatt.getService(FTMS_SERVICE_UUID)
            val treadmillData = service?.getCharacteristic(TREADMILL_DATA_UUID)
            if (service == null || treadmillData == null) {
                onStatus("Connected device does not expose FTMS treadmill data.")
                if (hasBluetoothConnectPermission()) {
                    disconnectGatt(gatt)
                }
                return
            }
            enableNotifications(gatt, treadmillData)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (characteristic.uuid == TREADMILL_DATA_UUID) {
                parseTreadmillData(characteristic.value)?.let(onSpeedSample)
            }
        }

        @Deprecated("Deprecated in API 33")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            if (characteristic.uuid == TREADMILL_DATA_UUID) {
                parseTreadmillData(value)?.let(onSpeedSample)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        val bleScanner = scanner
        if (adapter?.isEnabled != true || bleScanner == null) {
            onStatus("Bluetooth is unavailable or turned off on this device.")
            return
        }
        devices.clear()
        onDevicesUpdated(emptyList())
        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(FTMS_SERVICE_UUID))
                .build()
        )
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        isScanning = true
        onScanningChanged(true)
        onStatus("Scanning for FTMS treadmills.")
        bleScanner.startScan(filters, settings, scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (!isScanning) return
        scanner?.stopScan(scanCallback)
        isScanning = false
        onScanningChanged(false)
    }

    @SuppressLint("MissingPermission")
    fun connect(address: String) {
        stopScan()
        currentGatt?.close()
        currentGatt = null
        val device = adapter?.getRemoteDevice(address)
        if (device == null) {
            onStatus("Unable to find Bluetooth device $address.")
            return
        }
        onStatus("Connecting to ${deviceName(device)}.")
        currentGatt = device.connectGatt(appContext, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        stopScan()
        currentGatt?.disconnect()
        currentGatt?.close()
        currentGatt = null
        onDisconnected()
    }

    fun close() {
        disconnect()
    }

    private fun handleScanResult(result: ScanResult) {
        val device = result.device
        val name = deviceName(device)
        devices[device.address] = BluetoothTreadmillDevice(
            name = name,
            address = device.address
        )
        onDevicesUpdated(devices.values.toList())
    }

    @SuppressLint("MissingPermission")
    private fun deviceName(device: BluetoothDevice): String =
        device.name?.takeIf { it.isNotBlank() } ?: "FTMS Treadmill"

    @SuppressLint("MissingPermission")
    private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        gatt.setCharacteristicNotification(characteristic, true)
        val cccd = characteristic.getDescriptor(CCCD_UUID)
        if (cccd != null) {
            cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(cccd)
            onStatus("Subscribed to treadmill speed updates.")
        } else {
            onStatus("Treadmill data characteristic missing notification descriptor.")
        }
    }

    private fun parseTreadmillData(value: ByteArray?): Double? {
        if (value == null || value.size < 4) return null
        val instantaneousSpeedRaw = (value[2].toInt() and 0xFF) or ((value[3].toInt() and 0xFF) shl 8)
        return instantaneousSpeedRaw / 100.0
    }

    @SuppressLint("MissingPermission")
    private fun discoverServices(gatt: BluetoothGatt) {
        gatt.discoverServices()
    }

    @SuppressLint("MissingPermission")
    private fun closeGatt(gatt: BluetoothGatt) {
        gatt.close()
    }

    @SuppressLint("MissingPermission")
    private fun disconnectGatt(gatt: BluetoothGatt) {
        gatt.disconnect()
    }

    private fun hasBluetoothConnectPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    }
}
