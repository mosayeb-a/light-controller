package app.ma.lightcontroller.common

sealed class MqttConnectionState {
    object Connected : MqttConnectionState()
    object Disconnected : MqttConnectionState()
    data class Error(val throwable: Throwable) : MqttConnectionState()
}