package com.estef.antiphishingcoach.core.privacy

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity

/**
 * Encapsula la comprobación de autenticación local (biometría o credencial del dispositivo).
 */
class LocalAuthManager {

    fun canAuthenticate(activity: FragmentActivity): Boolean {
        val result = BiometricManager.from(activity).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun buildPromptInfo(
        title: String = "Verificación local",
        subtitle: String = "Desbloquea para continuar"
    ): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
    }
}
