package com.oksusu.susu.common.encrypt

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

data class Encryptor(
    private val key: String,
    private val algorithm: String,
) {
    fun encrypt(text: String): String {
        val cipher = Cipher.ENCRYPT_MODE.cipher()
        val encrypted = cipher.doFinal(text.toByteArray(charset(Charsets.UTF_8.name())))

        return Base64.getEncoder().encodeToString(encrypted)
    }

    fun decrypt(text: String): String {
        val cipher = Cipher.DECRYPT_MODE.cipher()
        val decodedBytes = Base64.getDecoder().decode(text)
        val decrypted = cipher.doFinal(decodedBytes)

        return String(decrypted, Charsets.UTF_8)
    }

    private fun Int.cipher(): Cipher {
        val cipher = Cipher.getInstance(algorithm)
        val keySpec = SecretKeySpec(key.toByteArray(), "AES")
        val ivParamSpec = IvParameterSpec(key.toByteArray())

        cipher.init(this, keySpec, ivParamSpec)

        return cipher
    }
}
