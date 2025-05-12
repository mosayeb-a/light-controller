package app.ma.lightcontroller

import android.app.Application
import app.ma.lightcontroller.common.MqttConfig
import app.ma.lightcontroller.data.repo.SmartLightRepository
import app.ma.lightcontroller.data.repo.SmartLightRepositoryImpl
import app.ma.lightcontroller.service.MqttClient
import app.ma.lightcontroller.service.PahoMqttClient
import app.ma.lightcontroller.ui.light.SmartLightViewModel
import kotlinx.serialization.json.Json
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val appModule = module {
            single {
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                    isLenient = true
                }
            }
            single {
                MqttAsyncClient(
                    MqttConfig.BROKER_URL,
                    MqttConfig.CLIENT_ID,
                    MemoryPersistence()
                )
            }
            single<MqttClient> { PahoMqttClient(mqttClient = get(), json = get()) }
            single<SmartLightRepository> { SmartLightRepositoryImpl(mqttClient = get()) }
            viewModel { SmartLightViewModel(smartLightRepo = get()) }

        }
        startKoin {
            androidContext(this@App)
            modules(listOf(appModule))
        }
    }
}