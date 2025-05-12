package app.ma.lightcontroller.ui.light

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ma.lightcontroller.common.Action
import app.ma.lightcontroller.common.MqttConnectionState
import app.ma.lightcontroller.common.UiMessage
import app.ma.lightcontroller.common.UiMessageManager
import app.ma.lightcontroller.data.LightCommand
import app.ma.lightcontroller.data.repo.SmartLightRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SmartLightUiState(
    val isConnected: Boolean = false,
    val isOn: Boolean = false,
    val brightness: Int = 0,
    val deviceId: String = "light1",
    val isLoading: Boolean = false
)

class SmartLightViewModel(
    private val smartLightRepo: SmartLightRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmartLightUiState(isLoading = true))
    val uiState: StateFlow<SmartLightUiState> = _uiState.asStateFlow()

    private var brightnessPublishJob: Job? = null
    private val brightnessDebounceTime = 500L

    init {
        viewModelScope.launch {
            smartLightRepo.observeConnectionState
                .drop(1)
                .collect { state ->
                    when (state) {
                        is MqttConnectionState.Connected -> {
                            _uiState.update {
                                it.copy(
                                    isConnected = true,
                                    isLoading = false
                                )
                            }
                        }

                        is MqttConnectionState.Disconnected -> {
                            _uiState.update {
                                it.copy(
                                    isConnected = false,
                                    isLoading = false
                                )
                            }
                            showError("Disconnected from server") {
                                reconnect()
                            }
                        }

                        is MqttConnectionState.Error -> {
                            _uiState.update {
                                it.copy(
                                    isConnected = false,
                                    isLoading = false
                                )
                            }
                            showError(state.throwable.message ?: "Connection error") {
                                reconnect()
                            }
                        }
                    }
                }
        }
        connect()
    }

    fun reconnect() {
        connect()
    }

    private fun connect() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                smartLightRepo.connectToMqttBroker()
                _uiState.update { it.copy(isLoading = false) }
                observeLightState()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, isConnected = false) }
                showError(e.message ?: "Connection failed") {
                    reconnect()
                }
            }
        }
    }

    private fun observeLightState() {
        viewModelScope.launch {
            try {
                smartLightRepo.observeLightState(_uiState.value.deviceId)
                    .catch { e ->
                        showError(e.message ?: "Failed to observe light state") {
                            observeLightState()
                        }
                    }
                    .collect { state ->
                        _uiState.update {
                            it.copy(
                                isOn = state.isOn,
                                brightness = state.brightness
                            )
                        }
                    }
            } catch (e: Exception) {
                showError(e.message ?: "Failed to observe light state") {
                    observeLightState()
                }
            }
        }
    }

    fun togglePower() {
        viewModelScope.launch {
            try {
                val newState = !_uiState.value.isOn
                _uiState.update { it.copy(isOn = newState) }
                smartLightRepo.sendCommand(
                    _uiState.value.deviceId,
                    LightCommand.Power(newState)
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isOn = !it.isOn) }
                showError(e.message ?: "Failed to toggle power") {
                    togglePower()
                }
            }
        }
    }

    fun updateBrightness(brightness: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(brightness = brightness) }

            brightnessPublishJob?.cancel()
            brightnessPublishJob = launch {
                delay(brightnessDebounceTime)
                try {
                    smartLightRepo.sendCommand(
                        _uiState.value.deviceId,
                        LightCommand.Brightness(brightness)
                    )
                } catch (e: Exception) {
                    showError(e.message ?: "Failed to update brightness") {
                        updateBrightness(brightness)
                    }
                }
            }
        }
    }

    private suspend fun showError(message: String, retry: () -> Unit) {
        UiMessageManager.sendEvent(
            event = UiMessage(
                message = message,
                action = Action(
                    name = "Retry",
                    action = retry
                )
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            smartLightRepo.disconnectFromMqttBroker()
        }
    }
}