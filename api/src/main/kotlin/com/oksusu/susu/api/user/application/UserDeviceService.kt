package com.oksusu.susu.api.user.application

import com.oksusu.susu.api.exception.ErrorCode
import com.oksusu.susu.api.exception.NotFoundException
import com.oksusu.susu.api.extension.withMDCContext
import com.oksusu.susu.api.user.domain.UserDevice
import com.oksusu.susu.api.user.infrastructure.UserDeviceRepository
import kotlinx.coroutines.Dispatchers
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserDeviceService(
    private val userDeviceRepository: UserDeviceRepository,
) {
    @Transactional
    fun saveSync(userDevice: UserDevice): UserDevice {
        return userDeviceRepository.save(userDevice)
    }

    suspend fun findByUid(uid: Long): UserDevice {
        return withMDCContext(Dispatchers.IO) {
            userDeviceRepository.findByUid(uid)
        } ?: throw NotFoundException(ErrorCode.NOT_FOUND_USER_DEVICE_ERROR)
    }
}
