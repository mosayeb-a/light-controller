package app.ma.lightcontroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ma.lightcontroller.common.AppSnackbar
import app.ma.lightcontroller.common.ObserveAsEvents
import app.ma.lightcontroller.common.UiMessageManager
import app.ma.lightcontroller.ui.light.SmartLightScreen
import app.ma.lightcontroller.ui.light.SmartLightViewModel
import app.ma.lightcontroller.ui.theme.LightControllerTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LightControllerTheme {
                val viewModel: SmartLightViewModel = koinViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                ObserveAsEvents(
                    flow = UiMessageManager.events,
                    snackbarHostState
                ) { event ->
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        val result = snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = event.action?.name,
                            duration = SnackbarDuration.Short
                        )

                        if (result == SnackbarResult.ActionPerformed) {
                            event.action?.action?.invoke()
                        }
                    }
                }
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState,
                            snackbar = { snackbarData ->
                                AppSnackbar(data = snackbarData)
                            }
                        )
                    }
                ) { innerPadding ->
                    innerPadding.let {}
                    SmartLightScreen(
                        viewState = uiState,
                        onToggle = { viewModel.togglePower() },
                        onBrightnessChange = { brightnessLevel ->
                            viewModel.updateBrightness(
                                brightnessLevel
                            )
                        }
                    )
                }
            }
        }
    }
}
