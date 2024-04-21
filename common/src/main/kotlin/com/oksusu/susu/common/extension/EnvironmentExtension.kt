package com.oksusu.susu.common.extension

import com.oksusu.susu.common.config.environment.EnvironmentType.PROFILE_PROD
import com.oksusu.susu.common.config.environment.EnvironmentType.PROFILE_STAGING
import com.oksusu.susu.common.config.environment.EnvironmentType.PROFILE_TEST
import org.springframework.core.env.Environment

fun Environment.isProd(): Boolean {
    return this.activeProfiles.any { it.equals(PROFILE_PROD) }
}

fun Environment.isStaging(): Boolean {
    return this.activeProfiles.any { it.equals(PROFILE_STAGING) }
}

fun Environment.isTest(): Boolean {
    return this.activeProfiles.any { it.equals(PROFILE_TEST) }
}
