package com.oksusu.susu.domain.common

import com.oksusu.susu.domain.encrypt.Encryptor
import jakarta.persistence.AttributeConverter

abstract class EncryptConverter(
    private val encryptor: Encryptor
) : AttributeConverter<String, String> {
    override fun convertToDatabaseColumn(attribute: String): String {
        return encryptor.encrypt(attribute)
    }

    override fun convertToEntityAttribute(dbData: String): String {
        return encryptor.decrypt(dbData)
    }
}
