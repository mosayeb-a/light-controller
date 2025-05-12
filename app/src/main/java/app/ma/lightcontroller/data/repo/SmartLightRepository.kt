package app.ma.lightcontroller.data.repo

import app.ma.lightcontroller.common.MqttConnectionState
import app.ma.lightcontroller.data.LightCommand
import app.ma.lightcontroller.data.LightState
import app.ma.lightcontroller.service.MqttClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SmartLightRepository {
    suspend fun connectToMqttBroker()
    suspend fun disconnectFromMqttBroker()
    suspend fun sendCommand(deviceId: String, command: LightCommand)
    fun observeLightState(deviceId: String): Flow<LightState>
    val observeConnectionState: Flow<MqttConnectionState>
}


class SmartLightRepositoryImpl(
    private val mqttClient: MqttClient
) : SmartLightRepository {

    override val observeConnectionState: StateFlow<MqttConnectionState> = mqttClient.connectionState

    override suspend fun connectToMqttBroker() {
        mqttClient.connect()
    }

    override suspend fun disconnectFromMqttBroker() {
        mqttClient.disconnect()
    }

    override suspend fun sendCommand(deviceId: String, command: LightCommand) {
        mqttClient.publishCommand(deviceId, command)
    }

    override fun observeLightState(deviceId: String): Flow<LightState> {
        return mqttClient.observeLightState(deviceId)
    }
}