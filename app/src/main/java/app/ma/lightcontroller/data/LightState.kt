package app.ma.lightcontroller.data

import kotlinx.serialization.Serializable

@Serializable
data class LightState(
    val isOn: Boolean,
    val brightness: Int,
    val deviceId: String,
    val lastUpdated: Long = System.currentTimeMillis()
)