package com.oksusu.susu.api.metadata.model

import com.oksusu.susu.domain.metadata.domain.ApplicationMetadata

class ApplicationMetadataModel(
    val id: Long,

    /** ios 최소 지원 어플리케이션 버전 */
    val iosMinSupportVersion: String,

    /** aos 최소 지원 어플리케이션 버전 */
    val aosMinSupportVersion: String,

    /** 활성화 여부 / 활성화 : 1, 비활성화 : 0 */
    val isActive: Boolean,
) {
    companion object {
        fun from(metadata: ApplicationMetadata): ApplicationMetadataModel {
            return ApplicationMetadataModel(
                id = metadata.id,
                iosMinSupportVersion = metadata.iosMinSupportVersion,
                aosMinSupportVersion = metadata.aosMinSupportVersion,
                isActive = metadata.isActive
            )
        }
    }
}
