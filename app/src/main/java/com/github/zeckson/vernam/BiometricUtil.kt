package com.github.zeckson.vernam

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

private const val ANDROID_KEY_STORE = "AndroidKeyStore"
private const val DEFAULT_KEY_NAME = "default_key"

private const val CIPHER_STRING =
    "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"

fun setupInitedEncryptCipher(): Cipher? {
    val cipher = Cipher.getInstance(CIPHER_STRING)
    try {
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
    } catch (e: KeyPermanentlyInvalidatedException) {
        return null
    }

    return cipher
}

fun setupInitedDecryptCipher(iv: ByteArray): Cipher? {
    val cipher = Cipher.getInstance(CIPHER_STRING)
    try {
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), IvParameterSpec(iv))
    } catch (e: KeyPermanentlyInvalidatedException) {
        return null
    }
    return cipher
}

fun getSecretKey(): SecretKey = getOrCreateKey(DEFAULT_KEY_NAME)

/**
 * Sets up KeyStore
 */
private fun getOrCreateKey(keyName: String): SecretKey {
    val keyStore = try {
        KeyStore.getInstance(ANDROID_KEY_STORE)
    } catch (e: KeyStoreException) {
        throw RuntimeException("Failed to get an instance of KeyStore", e)
    }

    try {
        keyStore.load(null)
        val key = keyStore.getKey(keyName, null)
        if (key != null) return key as SecretKey
    } catch (e: Exception) {
        when (e) {
            is NoSuchAlgorithmException,
            is CertificateException,
            is IOException -> throw RuntimeException(e)
            else -> throw e
        }
    }

    // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
    // for your flow. Use of keys is necessary if you need to know if the set of enrolled
    // fingerprints has changed.
    try {
        val keyProperties = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        val builder = KeyGenParameterSpec.Builder(keyName, keyProperties)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            // Key will be invalid if not authenticated through biometrics
            .setUserAuthenticationRequired(true)
            // Invalidate key if biometrics changed
            .setInvalidatedByBiometricEnrollment(true)

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEY_STORE
        )
        keyGenerator.init(builder.build())
        return keyGenerator.generateKey()
    } catch (e: Exception) {
        when (e) {
            is NoSuchAlgorithmException,
            is InvalidAlgorithmParameterException,
            is CertificateException,
            is IOException -> throw RuntimeException(e)
            else -> throw e
        }
    }
}
