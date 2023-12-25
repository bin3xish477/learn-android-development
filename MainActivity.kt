package com.example.commonencryption

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.commonencryption.databinding.ActivityMainBinding
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import android.util.Base64

class MainActivity : AppCompatActivity() {

    private val keyAlias = "MyAppKey"
    private val exampleStr = "MySuperSecretPasswordThatNobodyShouldKnow!!@@!$%"
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(this.binding.root)

        this.binding.btnEncrypt.setOnClickListener {
            this.generateKey()
            val encryptedBytes = this.encrypt(exampleStr.toByteArray())
            val encB64 = Base64.encode(encryptedBytes, Base64.DEFAULT).toString(Charsets.UTF_8)
            Log.d("MainActivity", encB64)
            this.binding.encryptedText.text = encB64
        }
    }

    private fun generateKey() {
        val keySpecs = KeyGenParameterSpec.Builder(
            this.keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setRandomizedEncryptionRequired(true)
            .build()

        val keyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGenerator.init(keySpecs)
        val key = keyGenerator.generateKey()
    }

    private fun encrypt(data: ByteArray): ByteArray {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        val aesMode =
            "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
        val key = keyStore.getKey(this.keyAlias, null) as SecretKey
        val cipher = Cipher.getInstance(aesMode)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(data)
    }
}