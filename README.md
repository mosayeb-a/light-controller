# Smart Light Control

This project allows controlling smart light devices via MQTT, including toggling power and adjusting brightness. The device also publishes its current state. This guide explains how to interact with the app using MQTT.

## MQTT Broker

The app connects to the public HiveMQ broker: `tcp://broker.hivemq.com:1883`

## MQTT Topics

The app uses topic paths in the format:
smart-light/{deviceId}/command
smart-light/{deviceId}/brightness
smart-light/{deviceId}/state

- use `command` to toggle the power on/off.
- use `brightness` to change the brightness level (0–100).
- subscribe to `state` to receive real-time updates of the light’s state.

# Example Messages
### 1. Turn the light ON
**Publish to:** `smart-light/device123/command`

**Payload:**

```json
{ "type": "Power", "isOn": true }
```

### 2. Set brightness to 75%
**Publish to:** `smart-light/device123/brightness`
**Payload:**
```json
{ "type": "Brightness", "level": 75 }
```

### 3. View current light state
**Subscribe to:**
```json
smart-light/device123/state
```
**Example payload received:**
```json
{
  "isOn": true,
  "brightness": 75,
  "deviceId": "device123",
  "lastUpdated": 1712345678901
}
```

## Assumptions

- The app uses a fixed `deviceId` of `light1`. All MQTT topic interactions assume this device ID.
- Devices are responsible for publishing their current state to the `/state` topic after handling a command.
- MQTT messages are not retained by default; no previous state will be delivered on reconnect.
- QoS level is set to 1 for publishing and subscribing, ensuring messages are delivered at least once.
- The system expects valid JSON payloads matching the `LightCommand` structure and does not perform deep validation.

## How to Test with MQTT Explorer

1. Open **MQTT Explorer** and connect to the broker at `broker.hivemq.com` on port `1883`.

2. Use the fixed device ID `light1` in all topic paths.

3. **Test Power Command:**
   - **Topic:** `smart-light/light1/command`
   - **Payload to turn ON:**
     ```json
     { "type": "Power", "isOn": true }
     ```
   - **Payload to turn OFF:**
     ```json
     { "type": "Power", "isOn": false }
     ```

4. **Test Brightness Command:**
   - **Topic:** `smart-light/light1/brightness`
   - **Payload to set brightness to 75%:**
     ```json
     { "type": "Brightness", "level": 75 }
     ```

5. **Request Initial State:**
   - **Topic:** `smart-light/light1/request`
   - **Payload:**
     ```json
     { "action": "get_state" }
     ```
   - This message prompts the device (or a mock) to publish the current state to the state topic.

6. **Observe State Updates:**
   - **Subscribe to:** `smart-light/light1/state`
   - **Example payload received:**
     ```json
     {
       "isOn": true,
       "brightness": 75,
       "deviceId": "light1",
       "lastUpdated": 1712345678901
     }
     ```

## Notes

- The `deviceId` is hardcoded to `light1` in the current app version. Changing it requires editing the source code (`SmartLightUiState`).
- The app:
  - Sends MQTT commands (`Power`, `Brightness`)
  - Subscribes to real-time state updates
  - Publishes a `get_state` request to prompt the device for its initial state
- Ensure your MQTT client can send structured JSON and properly format topics.
- The `LightState` format includes:
  - `isOn`: Boolean
  - `brightness`: Integer (0–100)
  - `deviceId`: String
  - `lastUpdated`: Epoch time in milliseconds

---
