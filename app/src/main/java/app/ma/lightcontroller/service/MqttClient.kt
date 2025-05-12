package app.ma.lightcontroller.service

import android.hardware.lights.LightState
import app.ma.lightcontroller.common.MqttConfig
import app.ma.lightcontroller.common.MqttConnectionState
import app.ma.lightcontroller.data.LightCommand
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface MqttClient {
    val connectionState: StateFlow<MqttConnectionState>
    suspend fun connect()
    suspend fun disconnect()
    suspend fun publishCommand(deviceId: String, command: LightCommand)
    fun observeLightState(deviceId: String): Flow<LightState>
}

class PahoMqttClient(
    private val mqttClient: MqttAsyncClient,
    private val json: Json
) : MqttClient {

    private val _connectionState = MutableStateFlow<MqttConnectionState>(MqttConnectionState.Disconnected)
    override val connectionState: StateFlow<MqttConnectionState> = _connectionState

    override suspend fun connect() = suspendCoroutine { continuation ->
        try {
            val connectOptions = MqttConnectOptions().apply {
                isAutomaticReconnect = true
                isCleanSession = true
                connectionTimeout = MqttConfig.CONNECTION_TIMEOUT
                keepAliveInterval = MqttConfig.CONNECTION_KEEP_ALIVE_INTERVAL
            }

            println("MQTT: attempting to connect to ${MqttConfig.BROKER_URL} with client ID ${MqttConfig.CLIENT_ID}")
            mqttClient.connect(connectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    _connectionState.value = MqttConnectionState.Connected
                    println("MQTT: connected successfully")
                    continuation.resume(Unit)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    val error = exception ?: Exception("connection failed")
                    _connectionState.value = MqttConnectionState.Error(error)
                    println("MQTT: connection failed: ${error.message}")
                    println("MQTT: stack trace: ${error.stackTraceToString()}")
                    continuation.resumeWithException(error)
                    throw error
                }
            })
        } catch (e: Exception) {
            _connectionState.value = MqttConnectionState.Error(e)
            println("MQTT: connection exception: ${e.message}")
            println("MQTT: stack trace: ${e.stackTraceToString()}")
            continuation.resumeWithException(e)
        }
    }

    override suspend fun disconnect() = suspendCoroutine { continuation ->
        try {
            println("MQTT: disconnecting")
            mqttClient.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    _connectionState.value = MqttConnectionState.Disconnected
                    println("MQTT: disconnected successfully")
                    continuation.resume(Unit)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    val error = exception ?: Exception("disconnect failed")
                    _connectionState.value = MqttConnectionState.Error(error)
                    println("MQTT: disconnect failed: ${error.message}")
                    continuation.resumeWithException(error)
                }
            })
        } catch (e: Exception) {
            _connectionState.value = MqttConnectionState.Error(e)
            println("MQTT: disconnect exception: ${e.message}")
            continuation.resumeWithException(e)
        }
    }

    override suspend fun publishCommand(deviceId: String, command: LightCommand) {
        connectionState.filterIsInstance<MqttConnectionState.Connected>().first()
        val topic = when (command) {
            is LightCommand.Power -> MqttConfig.Topics.getCommandTopicFor(deviceId)
            is LightCommand.Brightness -> MqttConfig.Topics.getBrightnessTopicFor(deviceId)
        }

        val payload = json.encodeToString(command)
        try {
            println("MQTT: publishing to $topic: $payload")
            mqttClient.publish(topic, payload.toByteArray(), MqttConfig.QOS, false)
        } catch (e: Exception) {
            _connectionState.value = MqttConnectionState.Error(e)
            println("MQTT: publish failed: ${e.message}")
            throw e
        }
    }

    override fun observeLightState(deviceId: String): Flow<LightState> = callbackFlow {
        connectionState.filterIsInstance<MqttConnectionState.Connected>().first()
        val topic = MqttConfig.Topics.getStateTopicFor(deviceId)

        try {
            println("MQTT: subscribing to $topic")
            mqttClient.subscribe(topic, MqttConfig.QOS) { _, message ->
                val payload = String(message.payload)
                try {
                    val lightState = json.decodeFromString<LightState>(payload)
                    println("MQTT: received state: $lightState")
                    trySend(lightState)
                } catch (e: Exception) {
                    println("MQTT: failed to parse state: ${e.message}")
                }
            }
        } catch (e: Exception) {
            _connectionState.value = MqttConnectionState.Error(e)
            println("MQTT: subscribe failed: ${e.message}")
        }

        awaitClose {
            if (mqttClient.isConnected) {
                try {
                    mqttClient.unsubscribe(topic)
                    println("MQTT: unsubscribed from $topic")
                } catch (e: Exception) {
                    println("MQTT: unsubscribe failed: ${e.message}")
                }
            }
        }
    }
}