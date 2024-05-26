package com.oksusu.susu.api

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@EnableAutoConfiguration
@ComponentScan(
    basePackages = [
        "com.oksusu.susu.client",
        "com.oksusu.susu.domain",
        "com.oksusu.susu.common",
        "com.oksusu.susu.batch",
        "com.oksusu.susu.api",
        "com.oksusu.susu.cache"
    ]
)
class ApiIntegrationTestConfiguration
