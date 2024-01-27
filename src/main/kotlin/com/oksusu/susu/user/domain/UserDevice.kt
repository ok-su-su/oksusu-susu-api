package com.oksusu.susu.user.domain

import com.oksusu.susu.common.domain.BaseEntity
import com.oksusu.susu.user.model.UserDeviceContext
import jakarta.persistence.*

@Entity
@Table(name = "user_device")
class UserDevice(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = -1,

    val uid: Long,

    @Column(name = "application_version")
    var applicationVersion: String?,

    @Column(name = "device_id")
    var deviceId: String?,

    @Column(name = "device_software_version")
    var deviceSoftwareVersion: String?,

    @Column(name = "line_number")
    var lineNumber: String?,

    @Column(name = "network_country_iso")
    var networkCountryIso: String?,

    @Column(name = "network_operator")
    var networkOperator: String?,

    @Column(name = "network_operator_name")
    var networkOperatorName: String?,

    @Column(name = "network_type")
    var networkType: String?,

    @Column(name = "phone_type")
    var phoneType: String?,

    @Column(name = "sim_serial_number")
    var simSerialNumber: String?,

    @Column(name = "sim_state")
    var simState: String?,
) : BaseEntity() {
    companion object {
        fun of(context: UserDeviceContext, uid: Long): UserDevice {
            return UserDevice(
                uid = uid,
                applicationVersion = context.applicationVersion,
                deviceId = context.deviceId,
                deviceSoftwareVersion = context.deviceSoftwareVersion,
                lineNumber = context.lineNumber,
                networkCountryIso = context.networkCountryIso,
                networkOperator = context.networkOperator,
                networkOperatorName = context.networkOperatorName,
                networkType = context.networkType,
                phoneType = context.phoneType,
                simSerialNumber = context.simSerialNumber,
                simState = context.simState
            )
        }
    }
}
