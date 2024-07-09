package com.oksusu.susu.batch

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@EnableAutoConfiguration
@ComponentScan(
    basePackages = [
        "com.oksusu.susu.client",
        "com.oksusu.susu.domain",
        "com.oksusu.susu.batch",
        "com.oksusu.susu.cache",
        "com.oksusu.susu.common"
    ]
)
class BatchIntegrationTestConfiguration
