package app.ma.lightcontroller.common

import java.util.UUID

object MqttConfig {
    const val BROKER_URL = "tcp://broker.hivemq.com:1883"
    val CLIENT_ID = "SmartLightApp-${UUID.randomUUID()}"

    object Topics {
        private const val BASE = "smart-light"
        fun getStateTopicFor(deviceId: String) = "$BASE/$deviceId/state"
        fun getCommandTopicFor(deviceId: String) = "$BASE/$deviceId/command"
        fun getBrightnessTopicFor(deviceId: String) = "$BASE/$deviceId/brightness"
    }

    const val QOS = 1
    const val CONNECTION_TIMEOUT = 5
    const val CONNECTION_KEEP_ALIVE_INTERVAL = 60
}