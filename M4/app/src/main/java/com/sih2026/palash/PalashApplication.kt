package com.sih2026.palash

import android.app.Application
import com.sih2026.palash.data.local.db.DatabaseProvider
import com.sih2026.palash.data.local.seeder.DatabaseSeeder
import com.sih2026.palash.data.network.NetworkConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PalashApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        
        // Configure local development backend URL for debug environment (ADB reverse tunnel tcp:8000)
        if (BuildConfig.DEBUG) {
            NetworkConfig.setDevelopmentBaseUrl("http://127.0.0.1:8000")
        }

        val database = DatabaseProvider.getDatabase(this)
        applicationScope.launch {
            DatabaseSeeder.seedDatabase(database)
            TeacherClassroomServerHolder.server.start()
            com.sih2026.palash.voice.VoiceModelManager.warmUp(this@PalashApplication)
        }
    }
}

object TeacherClassroomServerHolder {
    val server = com.sih2026.palash.classroom.TeacherClassroomServer()
}
