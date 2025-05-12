package app.ma.lightcontroller.ui.light

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ma.lightcontroller.R

@Composable
fun SmartLightScreen(
    modifier: Modifier = Modifier,
    viewState: SmartLightUiState,
    onToggle: () -> Unit,
    onBrightnessChange: (Int) -> Unit
) {

    Scaffold(
        modifier = modifier,
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                if (viewState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ConnectionStatus(
                            isConnected = viewState.isConnected,
                            isLoading = viewState.isLoading
                        )
                        LightControlCard(
                            viewState = viewState,
                            onToggle = onToggle,
                            onBrightnessChange = onBrightnessChange
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun ConnectionStatus(
    isConnected: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = if (isConnected) "Connected" else "Disconnected",
            style = MaterialTheme.typography.headlineSmall,
            color = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun LightControlCard(
    viewState: SmartLightUiState,
    onToggle: () -> Unit,
    onBrightnessChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LightStateDisplay(
                isOn = viewState.isOn,
                brightness = viewState.brightness
            )
            PowerSwitch(
                isOn = viewState.isOn,
                onToggle = onToggle,
                enabled = viewState.isConnected
            )
            BrightnessControl(
                brightness = viewState.brightness,
                onBrightnessChange = onBrightnessChange,
                enabled = viewState.isConnected && viewState.isOn
            )
        }
    }
}

@Composable
private fun LightStateDisplay(
    isOn: Boolean,
    brightness: Int,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isOn) 1f else 0.9f,
        label = "State display scale"
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .scale(scale),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_light_bulb),
            contentDescription = null,
            tint = if (isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isOn) "Light ON, $brightness%" else "Light OFF",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PowerSwitch(
    isOn: Boolean,
    onToggle: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val thumbAlpha by animateFloatAsState(
        targetValue = if (isOn) 1f else 0.3f,
        label = "Switch thumb alpha"
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Power",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 18.sp
        )
        Switch(
            checked = isOn,
            onCheckedChange = { onToggle() },
            enabled = enabled,
            thumbContent = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_light_bulb),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .alpha(thumbAlpha),
                    tint = if (isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
    }
}

@Composable
private fun BrightnessControl(
    brightness: Int,
    onBrightnessChange: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_brightness),
                contentDescription = "Brightness",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Brightness",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 18.sp
            )
        }
        Slider(
            value = brightness.toFloat(),
            onValueChange = { onBrightnessChange(it.toInt()) },
            valueRange = 0f..100f,
            steps = 99,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = "$brightness%",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}