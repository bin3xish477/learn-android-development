package com.example.localauth

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

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (this.isBiometricAuthAvailable()) {
            this.performBiometricAuth()
        } else {
            Log.d(
                "MainActivity",
                "closing MainActivity because biometric authentication is unavailable."
            )
        }
    }

    private fun isBiometricAuthAvailable(): Boolean {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d("MainActivity", "Biometric AuthN is available.")
                return true
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE or BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.d("MainActivity", "Biometric AuthN is unavailable.")
                return false
            }
        }
        return true
    }

    private fun performBiometricAuth() {
        this.executor = ContextCompat.getMainExecutor(this)
        this.biometricPrompt =
            BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    Log.d("MainActivity", "Biometric authentication was successful.")
                    this@MainActivity.showToast("Biometric authentication was successful.")
                    super.onAuthenticationSucceeded(result)
                }

                override fun onAuthenticationFailed() {
                    Log.d("MainActivity", "Biometric authentication failed.")
                    this@MainActivity.showToast("Biometric authentication failed.")
                    super.onAuthenticationFailed()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    Log.d("MainActivity", "An error occurred performing biometric authentication.")
                    this@MainActivity.showToast("An error occurred during biometric authentication")
                    super.onAuthenticationError(errorCode, errString)
                }
            })

        this.promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my business-critical app")
            .setSubtitle("Identify yourself human!")
            .build()
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