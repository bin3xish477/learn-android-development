package com.bin3xish477.localauth

import android.content.Intent
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.bin3xish477.localauth.databinding.ActivityMainBinding
import java.security.KeyStore
import java.util.UUID
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class MainActivity : AppCompatActivity() {

    companion object {
        const val KEY_NAME = "TokenKey"
        const val MAX_LOGIN_ATTEMPTS: Int = 3

        // used just for this example, otherwise, we have an infinite loop.
        var isLoggedIn: Boolean = false
        lateinit var encryptedUserToken: String
    }

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var binding: ActivityMainBinding
    private var biometricLoginAttempts: Int = 1
    private var userUuidToken: String = getUuid()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isLoggedIn) {
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
        } else {
            this.binding = ActivityMainBinding.inflate(layoutInflater)
            this.binding.userToken.text = "SessionToken = $encryptedUserToken"
            setContentView(this.binding.root)
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
                        isLoggedIn = true
                        Log.d("MainActivity", "Encrypting user UUID($userUuidToken)")
                        val cipher = result.cryptoObject?.cipher
                        if (cipher != null) {
                            val encryptedUuidToken =
                                cipher.doFinal(userUuidToken.toByteArray(Charsets.UTF_8))
                            if (encryptedUuidToken != null) {
                                encryptedUserToken = Base64.encodeToString(encryptedUuidToken, Base64.DEFAULT)
                            }
                        }
                        Log.d(
                            "MainActivity", "Encrypted user UUID: $encryptedUserToken"
                        )
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

            this.generateSecretKey(
                KeyGenParameterSpec.Builder(
                    KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setUserAuthenticationRequired(true)
                    .setInvalidatedByBiometricEnrollment(true)
                    .build()
            )
            val key = this.getKey()
            val cipher = this.getCipher()
            cipher.init(Cipher.ENCRYPT_MODE, key)
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
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

    private fun generateSecretKey(keySpec: KeyGenParameterSpec) {
        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )
        generator.init(keySpec)
        generator.generateKey()
    }

    private fun getKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        return keyStore.getKey(KEY_NAME, null) as SecretKey
    }

    private fun getCipher(): Cipher {
        return Cipher.getInstance(
            "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
        )
    }

    private fun getUuid(): String {
        return UUID.randomUUID().toString()
    }
}
