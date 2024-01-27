package com.oksusu.susu.user.model

interface UserDeviceContext {
    val applicationVersion: String?
    val deviceId: String?
    val deviceSoftwareVersion: String?
    val lineNumber: String?
    val networkCountryIso: String?
    val networkOperator: String?
    val networkOperatorName: String?
    val networkType: String?
    val phoneType: String?
    val simSerialNumber: String?
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
