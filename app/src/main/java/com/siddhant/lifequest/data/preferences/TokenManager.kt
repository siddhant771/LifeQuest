package com.siddhant.lifequest.data.preferences

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

private val Context.tokenStore by preferencesDataStore(name = "auth_tokens")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val encryptedTokenKey = stringPreferencesKey("access_token_ciphertext")
    private val initializationVectorKey = stringPreferencesKey("access_token_iv")
    private val legacyTokenKey = stringPreferencesKey("access_token")

    @Volatile
    var currentToken: String? = null
        private set

    val token: Flow<String?> = context.tokenStore.data
        .map { preferences ->
            val ciphertext = preferences[encryptedTokenKey]
            val iv = preferences[initializationVectorKey]
            if (ciphertext != null && iv != null) {
                runCatching { decrypt(ciphertext, iv) }.getOrNull()
            } else {
                preferences[legacyTokenKey]
            }
        }
        .onEach { currentToken = it }

    suspend fun migrateLegacyToken() {
        context.tokenStore.edit { preferences ->
            val legacyToken = preferences[legacyTokenKey] ?: return@edit
            val (ciphertext, iv) = encrypt(legacyToken)
            preferences[encryptedTokenKey] = ciphertext
            preferences[initializationVectorKey] = iv
            preferences.remove(legacyTokenKey)
            currentToken = legacyToken
        }
    }

    suspend fun saveToken(token: String) {
        val (ciphertext, iv) = encrypt(token)
        context.tokenStore.edit { preferences ->
            preferences[encryptedTokenKey] = ciphertext
            preferences[initializationVectorKey] = iv
            preferences.remove(legacyTokenKey)
        }
        currentToken = token
    }

    suspend fun clearToken() {
        context.tokenStore.edit { preferences ->
            preferences.remove(encryptedTokenKey)
            preferences.remove(initializationVectorKey)
            preferences.remove(legacyTokenKey)
        }
        currentToken = null
    }

    private fun encrypt(plainText: String): Pair<String, String> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val ciphertext = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(ciphertext, Base64.NO_WRAP) to
            Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
    }

    private fun decrypt(ciphertext: String, iv: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val parameters = GCMParameterSpec(
            GCM_TAG_LENGTH_BITS,
            Base64.decode(iv, Base64.NO_WRAP)
        )
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), parameters)
        val plainText = cipher.doFinal(Base64.decode(ciphertext, Base64.NO_WRAP))
        return String(plainText, StandardCharsets.UTF_8)
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "lifequest_access_token_v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH_BITS = 128
    }
}
