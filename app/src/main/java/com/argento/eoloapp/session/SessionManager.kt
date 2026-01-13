package com.argento.eoloapp.session

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SessionManager(private val context: Context) {
    private val KEY_ALIAS = "auth_token_key"
    private val TRANSFORMATION = "AES/CBC/PKCS7Padding"
    private val ANDROID_KEYSTORE = "AndroidKeyStore"
    private val PREFS_NAME = "session_prefs"
    private val USER_ID_KEY = "user_id"

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    init {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            generateNewKey()
        }
    }

    private fun generateNewKey() {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(false)
                .build()
        )
        keyGenerator.generateKey()
    }

    private fun getSecretKey(): SecretKey {
        return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    suspend fun saveAuthToken(token: String) = withContext(Dispatchers.IO) {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val encryptedBytes = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
            val iv = cipher.iv

            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sharedPreferences.edit()
                .putString("encrypted_token", Base64.encodeToString(encryptedBytes, Base64.DEFAULT))
                .putString("iv", Base64.encodeToString(iv, Base64.DEFAULT))
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getAuthToken(): String? = withContext(Dispatchers.IO) {
        try {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val encryptedToken = sharedPreferences.getString("encrypted_token", null)
            val ivString = sharedPreferences.getString("iv", null)

            if (encryptedToken != null && ivString != null) {
                val encryptedBytes = Base64.decode(encryptedToken, Base64.DEFAULT)
                val iv = Base64.decode(ivString, Base64.DEFAULT)

                val cipher = Cipher.getInstance(TRANSFORMATION)
                val ivSpec = IvParameterSpec(iv)
                cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), ivSpec)
                val decryptedBytes = cipher.doFinal(encryptedBytes)
                return@withContext String(decryptedBytes, Charsets.UTF_8)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    suspend fun clearAuthToken() = withContext(Dispatchers.IO) {
        try {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sharedPreferences.edit()
                .remove("encrypted_token")
                .remove("iv")
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun saveUserId(userId: String) = withContext(Dispatchers.IO) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(USER_ID_KEY, userId).apply()
    }

    suspend fun getUserId(): String? = withContext(Dispatchers.IO) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return@withContext sharedPreferences.getString(USER_ID_KEY, null)
    }

    suspend fun clearUserId() = withContext(Dispatchers.IO) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().remove(USER_ID_KEY).apply()
    }
}