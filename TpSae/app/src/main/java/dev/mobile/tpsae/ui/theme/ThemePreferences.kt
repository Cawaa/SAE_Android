package dev.mobile.tpsae.ui.theme

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Stocke et partage le choix clair/sombre de l'utilisateur. */
object ThemePreferences {

    private const val PREFS_NAME = "tp_sae_theme_prefs"
    private const val KEY_DARK_THEME = "dark_theme"

    private val _darkTheme = MutableStateFlow(false)
    val darkTheme: StateFlow<Boolean> = _darkTheme

    fun init(context: Context) {
        _darkTheme.value = prefs(context).getBoolean(KEY_DARK_THEME, false)
    }

    fun setDarkTheme(context: Context, enabled: Boolean) {
        prefs(context).edit { putBoolean(KEY_DARK_THEME, enabled) }
        _darkTheme.value = enabled
    }

    fun toggle(context: Context) {
        setDarkTheme(context, !_darkTheme.value)
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}


