package com.oksusu.susu.api.event.listener

import com.oksusu.susu.api.event.model.CreateUserDeviceEvent
import com.oksusu.susu.api.event.model.UpdateUserDeviceEvent
import com.oksusu.susu.api.user.application.UserDeviceService
import com.oksusu.susu.common.extension.mdcCoroutineScope
import com.oksusu.susu.domain.common.extension.coExecuteOrNull
import com.oksusu.susu.domain.config.database.TransactionTemplates
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class UserDeviceEventListener(
    private val userDeviceService: UserDeviceService,
    private val txTemplates: TransactionTemplates,
) {
    val logger = KotlinLogging.logger { }

    @TransactionalEventListener
    fun createUserDevice(event: CreateUserDeviceEvent) {
        mdcCoroutineScope(Dispatchers.IO + Job(), event.traceId).launch {
            logger.info { "${event.publishAt}에 발행된 ${event.userDevice.uid} 유저 디바이스 정보 저장 실행 시작" }

            txTemplates.writer.coExecuteOrNull {
                userDeviceService.saveSync(event.userDevice)
            }

            logger.info { "${event.publishAt}에 발행된 ${event.userDevice.uid} 유저 디바이스 정보 저장 실행 끝" }
        }
    }

    @TransactionalEventListener
    fun updateUserDevice(event: UpdateUserDeviceEvent) {
        CoroutineScope(Dispatchers.IO + Job()).launch {
            logger.info { "${event.publishAt}에 발행된 ${event.userDevice.uid} 유저 디바이스 정보 업데이트 실행 시작" }

            val userDevice = userDeviceService.findByUid(event.userDevice.uid)

            txTemplates.writer.coExecuteOrNull(Dispatchers.IO) {
                userDevice.apply {
                    applicationVersion = event.userDevice.applicationVersion
                    deviceId = event.userDevice.deviceId
                    deviceSoftwareVersion = event.userDevice.deviceSoftwareVersion
                    lineNumber = event.userDevice.lineNumber
                    networkCountryIso = event.userDevice.networkCountryIso
                    networkOperator = event.userDevice.networkOperator
                    networkOperatorName = event.userDevice.networkOperatorName
                    networkType = event.userDevice.networkType
                    phoneType = event.userDevice.phoneType
                    simSerialNumber = event.userDevice.simSerialNumber
                    simState = event.userDevice.simState
                }.run { userDeviceService.saveSync(this) }
            }

            logger.info { "${event.publishAt}에 발행된 ${event.userDevice.uid} 유저 디바이스 정보 업데이트 실행 끝" }
        }
    }
}
