package com.sih2026.palash.ui.localization

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import com.sih2026.palash.model.DeviceRole
import java.util.Locale

object PalashStrings {

    /**
     * Resolves a string resource based on the specified role.
     * When [role] is [DeviceRole.TEACHER], resolves using Hindi ("hi") locale configuration.
     * Otherwise, resolves using default context resources.
     */
    fun getString(
        context: Context,
        @StringRes resId: Int,
        vararg formatArgs: Any,
        role: DeviceRole? = DeviceRole.TEACHER
    ): String {
        return if (role == DeviceRole.TEACHER) {
            val hindiConfig = Configuration(context.resources.configuration).apply {
                setLocale(Locale("hi"))
            }
            val hindiContext = context.createConfigurationContext(hindiConfig)
            if (formatArgs.isNotEmpty()) {
                hindiContext.resources.getString(resId, *formatArgs)
            } else {
                hindiContext.resources.getString(resId)
            }
        } else {
            if (formatArgs.isNotEmpty()) {
                context.resources.getString(resId, *formatArgs)
            } else {
                context.resources.getString(resId)
            }
        }
    }
}

/**
     * Composable helper to resolve a string resource for Teacher Mode in Hindi Devanagari.
     */
@Composable
@ReadOnlyComposable
fun teacherStringResource(@StringRes id: Int, vararg formatArgs: Any): String {
    val context = LocalContext.current
    return PalashStrings.getString(context, id, *formatArgs, role = DeviceRole.TEACHER)
}
