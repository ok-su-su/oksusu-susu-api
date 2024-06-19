package com.oksusu.susu.domain.common.encrypt

import com.oksusu.susu.common.encrypt.Encryptor

data class EncryptData(
    val encData: String,
) {
    fun dec(encryptor: Encryptor): String {
        return encryptor.decrypt(this.encData)
    }

    companion object {
        fun enc(plainData: String, encryptor: Encryptor): EncryptData {
            return EncryptData(
                encData = encryptor.encrypt(plainData)
            )
        }
    }
}
