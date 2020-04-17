package com.github.zeckson.vernam

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey

private const val ANDROID_KEY_STORE = "AndroidKeyStore"
private const val DEFAULT_KEY_NAME = "default_key"
private val defaultCipher = setupCipher()

fun getDefaultCipher() = defaultCipher

private val keyStore = setupKeyStore()

fun getSecretKey(): SecretKey {
    return keyStore.getKey(DEFAULT_KEY_NAME, null) as SecretKey
}

/**
 * Sets up KeyStore
 */
private fun setupKeyStore(): KeyStore {
    val keyStore = try {
        KeyStore.getInstance(ANDROID_KEY_STORE)
    } catch (e: KeyStoreException) {
        throw RuntimeException("Failed to get an instance of KeyStore", e)
    }

    val keyGenerator = try {
        KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEY_STORE
        )
    } catch (e: Exception) {
        when (e) {
            is NoSuchAlgorithmException,
            is NoSuchProviderException ->
                throw RuntimeException("Failed to get an instance of KeyGenerator", e)
            else -> throw e
        }
    }

    // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
    // for your flow. Use of keys is necessary if you need to know if the set of enrolled
    // fingerprints has changed.
    try {
        keyStore.load(null)

        val keyProperties = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        val builder = KeyGenParameterSpec.Builder(DEFAULT_KEY_NAME, keyProperties)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            // Key will be invalid if not authenticated through biometrics
            .setUserAuthenticationRequired(true)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setInvalidatedByBiometricEnrollment(true)

        keyGenerator.run {
            init(builder.build())
            generateKey()
        }
    } catch (e: Exception) {
        when (e) {
            is NoSuchAlgorithmException,
            is InvalidAlgorithmParameterException,
            is CertificateException,
            is IOException -> throw RuntimeException(e)
            else -> throw e
        }
    }
    return keyStore
}


/**
 * Initialize the [Cipher]
 *
 * @return `true` if initialization succeeded, `false` if the lock screen has been disabled or
 * reset after key generation, or if a fingerprint was enrolled after key generation.
 */
fun Cipher.init(mode: Int = Cipher.ENCRYPT_MODE): Boolean {
    return try {
        this.init(mode, getSecretKey())
        true
    } catch (e: Exception) {
        when (e) {
            is KeyPermanentlyInvalidatedException -> false
            is KeyStoreException,
            is CertificateException,
            is UnrecoverableKeyException,
            is IOException,
            is NoSuchAlgorithmException,
            is InvalidKeyException -> throw RuntimeException("Failed to init Cipher", e)
            else -> throw e
        }
    }
}

/**
 * Sets up default cipher
 */
private fun setupCipher(): Cipher {
    return try {
        val cipherString =
            "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
        Cipher.getInstance(cipherString)
    } catch (e: Exception) {
        when (e) {
            is NoSuchAlgorithmException,
            is NoSuchPaddingException ->
                throw RuntimeException("Failed to get an instance of Cipher", e)
            else -> throw e
        }
    }

}


