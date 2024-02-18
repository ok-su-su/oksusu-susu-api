package com.oksusu.susu.user.domain

import com.oksusu.susu.common.domain.BaseEntity
import com.oksusu.susu.user.model.UserDeviceContext
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

/** 유저 디바이스 정보 */
@Entity
@Table(name = "user_device")
class UserDevice(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    /** 유저 id */
    val uid: Long,

    /** 어플리케이션 버전 */
    @Column(name = "application_version")
    var applicationVersion: String?,

    /** IMEI */
    @Column(name = "device_id")
    var deviceId: String?,

    /** SW버전 */
    @Column(name = "device_software_version")
    var deviceSoftwareVersion: String?,

    /** 전화번호 */
    @Column(name = "line_number")
    var lineNumber: String?,

    /** 국가코드 */
    @Column(name = "network_country_iso")
    var networkCountryIso: String?,

    /** 망 사업자코드 */
    @Column(name = "network_operator")
    var networkOperator: String?,

    /** 망 사업자명 */
    @Column(name = "network_operator_name")
    var networkOperatorName: String?,

    /** 망 시스템 방식 */
    @Column(name = "network_type")
    var networkType: String?,

    /** 단말기 종류 */
    @Column(name = "phone_type")
    var phoneType: String?,

    /** SIM카드 Serial Number */
    @Column(name = "sim_serial_number")
    var simSerialNumber: String?,

    /** 가입자 ID */
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
