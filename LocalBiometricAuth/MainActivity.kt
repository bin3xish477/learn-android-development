package com.bin3xish477.localauth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    companion object {
        const val MAX_LOGIN_ATTEMPTS: Int = 3
    }

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var biometricLoginAttempts: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (this.isBiometricAuthAvailable()) {
            this.handleBiometricAuth()
        } else {
            Log.d(
                "MainActivity",
                "Closing MainActivity because biometric authentication is unavailable."
            )
            this.showToast("Biometric authentication is unavailable.")
            finish()
        }
    }

    private fun isBiometricAuthAvailable(): Boolean {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d("MainActivity", "Class 3 (Strong) Biometric AuthN is available.")
                return true
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE or BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.d("MainActivity", "Class 3 (Strong) Biometric AuthN is unavailable.")
                return false
            }
        }
        return true
    }

    private fun handleBiometricAuth() {
        Log.d("MainActivity", "CurrentLoginAttempts: ${this.biometricLoginAttempts}")
        if (this.biometricLoginAttempts > MAX_LOGIN_ATTEMPTS) {
            this.showToast("You've failed biometric login more than 3 times.")
            finish()
        } else {
            this.executor = ContextCompat.getMainExecutor(this)
            this.biometricPrompt =
                BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        Log.d("MainActivity", "Biometric authentication was successful.")
                        this@MainActivity.showToast("Biometric authentication was successful.")
                        this@MainActivity.startMainAfterAuthSuccess()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Log.d("MainActivity", "Biometric authentication failed.")
                        this@MainActivity.showToast("Biometric authentication failed.")
                        this@MainActivity.biometricLoginAttempts++
                        this@MainActivity.handleBiometricAuth()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        if (errorCode == 11) {
                            Log.d(
                                "MainActivity",
                                "Fingerprint not enrolled for authentication."
                            )
                            this@MainActivity.showToast("Fingerprint not enrolled for authentication.")
                            finish()
                        }
                        Log.d(
                            "MainActivity",
                            "Biometric AuthN error: $errString. Code: $errorCode"
                        )
                        this@MainActivity.showToast("An error occurred during biometric authentication")
                    }
                })

            this.promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my business-critical app")
                .setSubtitle("Identify yourself human!")
                .setAllowedAuthenticators(BIOMETRIC_STRONG)
                .setNegativeButtonText("Use account password.")
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun startMainAfterAuthSuccess() {
        val intent = Intent(this, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
