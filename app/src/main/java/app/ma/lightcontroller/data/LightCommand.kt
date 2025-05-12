package app.ma.lightcontroller.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class LightCommand {

    @Serializable
    @SerialName("Power")
    data class Power(val isOn: Boolean) : LightCommand()

    @Serializable
    @SerialName("Brightness")
    data class Brightness(val level: Int) : LightCommand()
}