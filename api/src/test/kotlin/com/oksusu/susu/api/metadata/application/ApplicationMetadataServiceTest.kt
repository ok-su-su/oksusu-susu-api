package com.oksusu.susu.api.metadata.application

import com.oksusu.susu.api.metadata.model.DeviceOS
import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import com.oksusu.susu.domain.metadata.domain.ApplicationMetadata
import com.oksusu.susu.domain.metadata.infrastructure.ApplicationMetadataRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay

class ApplicationMetadataServiceTest : DescribeSpec({
    val logger = KotlinLogging.logger { }

    val mockApplicationMetadataRepository = mockk<ApplicationMetadataRepository>()
    val mockCoroutineExceptionHandler = mockk<ErrorPublishingCoroutineExceptionHandler>()

    every { mockApplicationMetadataRepository.findTop1ByIsActiveOrderByCreatedAtDesc(any()) } returns ApplicationMetadata(
        id = 1L,
        iosMinSupportVersion = "1.1.1",
        aosMinSupportVersion = "1.1.1",
        isActive = true
    )
    every { mockCoroutineExceptionHandler.handler } returns CoroutineExceptionHandler { _, _ -> }

    val service = ApplicationMetadataService(mockApplicationMetadataRepository, mockCoroutineExceptionHandler)

    beforeSpec {
        service.refreshApplicationMetadata()

        delay(100)
    }

    describe("checkApplicationVersion") {
        context("ios 버전을 체크했을 때,") {
            it("버전이 최소 지원 버전 미만이면 강제 업데이트를 해야한다.") {
                service.checkApplicationVersion(DeviceOS.IOS, "0.0.0").needForceUpdate shouldBeEqual true

                service.checkApplicationVersion(DeviceOS.IOS, "1.0.0").needForceUpdate shouldBeEqual true
                service.checkApplicationVersion(DeviceOS.IOS, "0.1.0").needForceUpdate shouldBeEqual true
                service.checkApplicationVersion(DeviceOS.IOS, "0.0.1").needForceUpdate shouldBeEqual true

                service.checkApplicationVersion(DeviceOS.IOS, "1.1.0").needForceUpdate shouldBeEqual true
                service.checkApplicationVersion(DeviceOS.IOS, "1.0.1").needForceUpdate shouldBeEqual true
                service.checkApplicationVersion(DeviceOS.IOS, "0.1.1").needForceUpdate shouldBeEqual true

                service.checkApplicationVersion(DeviceOS.IOS, "1.0.2").needForceUpdate shouldBeEqual true
                service.checkApplicationVersion(DeviceOS.IOS, "0.2.0").needForceUpdate shouldBeEqual true
                service.checkApplicationVersion(DeviceOS.IOS, "0.2.2").needForceUpdate shouldBeEqual true

                service.checkApplicationVersion(DeviceOS.AOS, "0.0.0").needForceUpdate shouldBeEqual true

                service.checkApplicationVersion(DeviceOS.AOS, "1.0.0").needForceUpdate shouldBeEqual true
                service.checkApplicationVersion(DeviceOS.AOS, "0.1.0").needForceUpdate shouldBeEqual true
                service.checkApplicationVersion(DeviceOS.AOS, "0.0.1").needForceUpdate shouldBeEqual true

                service.checkApplicationVersion(DeviceOS.AOS, "1.1.0").needForceUpdate shouldBeEqual true
                service.checkApplicationVersion(DeviceOS.AOS, "1.0.1").needForceUpdate shouldBeEqual true
                service.checkApplicationVersion(DeviceOS.AOS, "0.1.1").needForceUpdate shouldBeEqual true

                service.checkApplicationVersion(DeviceOS.AOS, "1.0.2").needForceUpdate shouldBeEqual true
                service.checkApplicationVersion(DeviceOS.AOS, "0.2.0").needForceUpdate shouldBeEqual true
                service.checkApplicationVersion(DeviceOS.AOS, "0.2.2").needForceUpdate shouldBeEqual true
            }

            it("버전이 최소 지원 버전 이상이면 강제 업데이트를 하지않아도 된다.") {
                service.checkApplicationVersion(DeviceOS.IOS, "1.1.1").needForceUpdate shouldBeEqual false
                service.checkApplicationVersion(DeviceOS.IOS, "1.1.2").needForceUpdate shouldBeEqual false
                service.checkApplicationVersion(DeviceOS.IOS, "1.2.0").needForceUpdate shouldBeEqual false
                service.checkApplicationVersion(DeviceOS.IOS, "1.2.1").needForceUpdate shouldBeEqual false
                service.checkApplicationVersion(DeviceOS.IOS, "2.0.0").needForceUpdate shouldBeEqual false
                service.checkApplicationVersion(DeviceOS.IOS, "2.0.1").needForceUpdate shouldBeEqual false
                service.checkApplicationVersion(DeviceOS.IOS, "2.1.0").needForceUpdate shouldBeEqual false
                service.checkApplicationVersion(DeviceOS.IOS, "2.1.1").needForceUpdate shouldBeEqual false
                service.checkApplicationVersion(DeviceOS.IOS, "2.1.2").needForceUpdate shouldBeEqual false
                service.checkApplicationVersion(DeviceOS.IOS, "2.2.2").needForceUpdate shouldBeEqual false

                service.checkApplicationVersion(DeviceOS.AOS, "1.1.1").needForceUpdate shouldBeEqual false
                service.checkApplicationVersion(DeviceOS.AOS, "1.1.2").needForceUpdate shouldBeEqual false
                service.checkApplicationVersion(DeviceOS.AOS, "1.2.0").needForceUpdate shouldBeEqual false
                service.checkApplicationVersion(DeviceOS.AOS, "1.2.1").needForceUpdate shouldBeEqual false
                service.checkApplicationVersion(DeviceOS.AOS, "2.0.0").needForceUpdate shouldBeEqual false
                service.checkApplicationVersion(DeviceOS.AOS, "2.0.1").needForceUpdate shouldBeEqual false
                service.checkApplicationVersion(DeviceOS.AOS, "2.1.0").needForceUpdate shouldBeEqual false
                service.checkApplicationVersion(DeviceOS.AOS, "2.1.1").needForceUpdate shouldBeEqual false
                service.checkApplicationVersion(DeviceOS.AOS, "2.1.2").needForceUpdate shouldBeEqual false
                service.checkApplicationVersion(DeviceOS.AOS, "2.2.2").needForceUpdate shouldBeEqual false
            }
        }
    }
})
