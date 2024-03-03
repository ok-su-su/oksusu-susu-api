package com.oksusu.susu.user.application

import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.extension.withMDCContext
import com.oksusu.susu.user.domain.UserDevice
import com.oksusu.susu.user.infrastructure.UserDeviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        return withContext(Dispatchers.IO.withMDCContext()) {
            userDeviceRepository.findByUid(uid)
        } ?: throw NotFoundException(ErrorCode.NOT_FOUND_USER_DEVICE_ERROR)
    }
}
