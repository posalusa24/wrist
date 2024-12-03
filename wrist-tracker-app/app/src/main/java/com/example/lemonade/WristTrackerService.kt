package com.example.lemonade

import android.content.ContentResolver
import android.provider.Settings
import android.os.Bundle
import android.os.IBinder
import android.content.Context
import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import android.Manifest
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.app.ServiceCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.lemonade.ui.theme.AppTheme
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import android.net.wifi.ScanResult
import kotlinx.coroutines.*

class WristTrackerService : Service() {
    
    private lateinit var mqttClient: MqttClient
    private lateinit var wifiManager: WifiManager

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())
        
        // Initialize MQTT Client
        val mqttBrokerUrl = "tcp://broker.hivemq.com:1883"
        mqttClient = MqttClient(mqttBrokerUrl, MqttClient.generateClientId(), null)
        mqttClient.connect()
        
        if (mqttClient.isConnected) {
                     Log.d("MQTT", "Connected to MQTT broker.")

        } else {
                     Log.d("MQTT", "Failed MQTT broker.")

        }

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        scanWifiNetworks()

    }

    private fun scanWifiNetworks() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                sendMqttMessage("wrist/8571391", "scanning")
                val success = wifiManager.startScan()
                if (success) {
                    val scanResults = wifiManager.scanResults
                    sendClosestWifiBSSID(scanResults)
                } else {
                    sendMqttMessage("wrist/8571391", "FAIL")
                }
                delay(30000) // Scan every 30 seconds
            }
        }
    }

    private fun sendClosestWifiBSSID(scanResults: List<ScanResult>) {
                sendMqttMessage("wrist/8571391", "Scanning done")
        if (scanResults.isNotEmpty()) {
                sendMqttMessage("wrist/8571391", "Scan results are ready")
            // Sort scan results by signal strength (RSSI)
            val closestNetwork = scanResults.maxByOrNull { it.level }
            closestNetwork?.let {
                val bssid = it.BSSID
                val ssid = it.SSID
                val signalStrength = it.level
                val userid = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

                val message = "scantoken $userid $bssid $ssid $signalStrength"
                sendMqttMessage("wrist/8571391", message)
            }
        } else {
                sendMqttMessage("wrist/8571391", "Scan results are empty")
        }
    }

    private fun sendMqttMessage(topic: String, message: String) {
        if (mqttClient.isConnected) {
            val mqttMessage = MqttMessage(message.toByteArray())
            mqttClient.publish(topic, mqttMessage)
        }
    }

    private fun createNotificationChannel() {
        val channelId = "WristTrackerServiceChannel"
        val channelName = "WRIST Tracker Service"
        val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "WristTrackerServiceChannel")
            .setContentTitle("Wi-Fi Scanning Service")
            .setContentText("Scanning for Wi-Fi networks...")
            .build()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
                sendMqttMessage("wrist/8571391", "lmao ded")
        super.onDestroy()
    }
}
