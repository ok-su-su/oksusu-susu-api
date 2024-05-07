package com.oksusu.susu.domain.encrypt

import com.oksusu.susu.domain.config.encrypt.EncryptConfig
import org.springframework.stereotype.Component
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Component
class Encryptor(
    private val encryptConfig: EncryptConfig
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
        val cipher = Cipher.getInstance(encryptConfig.algorithm)
        val keySpec = SecretKeySpec(encryptConfig.key.toByteArray(), "AES")
        val ivParamSpec = IvParameterSpec(encryptConfig.key.toByteArray())

        cipher.init(this, keySpec, ivParamSpec)

        return cipher
    }
}
