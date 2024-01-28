package com.oksusu.susu.user.model

import jakarta.persistence.Column

interface UserDeviceContext {
    /** 어플리케이션 버전 */
    val applicationVersion: String?
    /** IMEI */
    val deviceId: String?
    /** SW버전 */
    val deviceSoftwareVersion: String?
    /** 전화번호 */
    val lineNumber: String?
    /** 국가코드 */
    val networkCountryIso: String?
    /** 망 사업자코드 */
    val networkOperator: String?
    /** 망 사업자명 */
    val networkOperatorName: String?
    /** 망 시스템 방식 */
    val networkType: String?
    /** 단말기 종류 */
    val phoneType: String?
    /** SIM카드 Serial Number */
    val simSerialNumber: String?
    /** 가입자 ID */
    val simState: String?
}

class UserDeviceContextImpl(
    override val applicationVersion: String?,
    override val deviceId: String?,
    override val deviceSoftwareVersion: String?,
    override val lineNumber: String?,
    override val networkCountryIso: String?,
    override val networkOperator: String?,
    override val networkOperatorName: String?,
    override val networkType: String?,
    override val phoneType: String?,
    override val simSerialNumber: String?,
    override val simState: String?,
) : UserDeviceContext {
    companion object {
        fun getDefault(): UserDeviceContextImpl {
            return UserDeviceContextImpl(
                applicationVersion = null,
                deviceId = null,
                deviceSoftwareVersion = null,
                lineNumber = null,
                networkCountryIso = null,
                networkOperator = null,
                networkOperatorName = null,
                networkType = null,
                phoneType = null,
                simSerialNumber = null,
                simState = null
            )
        }
    }
}

val DEVICE_INFO_HEADERS = listOf(
    "Application-Version",
    "Device-Id",
    "Device-Software-Version",
    "Line-Number",
    "Network-Country-Iso",
    "Network-Operator",
    "Network-Operator-Name",
    "Network-Type",
    "Phone-Type",
    "Sim-Serial-Number",
    "Sim-State"
)
